package services.instance.sample;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.mongojack.DBQuery;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoException;
import com.typesafe.config.ConfigFactory;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.lfw.utils.DoubleKeyMap;
import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.ngl.dao.api.APIException;
import fr.cea.ig.ngl.dao.api.APIValidationException;
import mail.MailServiceException;
import mail.MailServices;
import models.laboratory.common.instance.PropertyValue;
import models.laboratory.common.instance.property.PropertyListValue;
import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.parameter.atsValues.AtsZone;
import models.laboratory.sample.instance.Sample;
import models.utils.InstanceConstants;
import models.utils.dao.DAOException;
import play.Logger;
import rules.services.RulesException;
import services.instance.AbstractImportData;
import services.taxonomy.NCBITaxonomyServices;
import validation.ContextValidation;
import validation.utils.ValidationConstants;

public class UpdateSampleAtlaSea extends AbstractImportData {

    protected final play.Logger.ALogger logger;

    public static final String ATLASEA_PROJECT = "DKB";

    public static final String TRAME_ATLASEA = "reference-genome-biological-sample-reception";

    public static final String DATA_USER = "ngl-data";
    private List<String> modificationsLog = new ArrayList<>();
    private List<String> modificationsLogGenome = new ArrayList<>();

    public UpdateSampleAtlaSea(NGLApplication app) {
        super("UpdateSampleAtlaSea", app);
        logger = play.Logger.of(UpdateSampleAtlaSea.class);
    }

    @Override
    public void runImport(ContextValidation ctx)
            throws SQLException, DAOException, MongoException, RulesException, APIValidationException, APIException {
        modificationsLog.clear();
        modificationsLogGenome.clear();

        updateGoatStatusAndGenomeDKB();
        updateGenomeSizeTrameAtlasea();

        buildAndSendMail();
    }

    private void updateGenomeSizeTrameAtlasea() {
        int batchSize = 100; // Taille du lot
        int pagination = 0; // initial
        List<Sample> samplesAtlaSea;

        do {
           
            samplesAtlaSea = MongoDBDAO.find(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, DBQuery.in("importTypeCode", TRAME_ATLASEA)
            .notIn("projectCodes", ATLASEA_PROJECT).notExists("life"))
                    .skip(pagination)
                    .limit(batchSize)
                    .toList();

            if (!samplesAtlaSea.isEmpty()) {
                Map<String, List<Sample>> samplesGroupedByTaxon = groupSamplesByTaxon(samplesAtlaSea);
                compareGenomeSizeToGoat(samplesGroupedByTaxon);
                pagination += batchSize; // Mise à jour de la pagination pour le prochain lot
            }
        } while (samplesAtlaSea.size() == batchSize);
    }

    private void compareGenomeSizeToGoat(Map<String, List<Sample>> samplesGroupedByTaxon) { 
        for (Map.Entry<String, List<Sample>> entry : samplesGroupedByTaxon.entrySet()) {
            String taxonCode = entry.getKey();
            List<Sample> samples = entry.getValue();

            if (taxonCode == null) {
                for (Sample sample : samples) {
                    ContextValidation ctxValidation = ContextValidation.createUpdateContext(DATA_USER);
                    ctxValidation.addError("taxonCode", ValidationConstants.ERROR_CODE_MISSING_MSG);
                    sample.validate(ctxValidation);

                    if (!ctxValidation.hasErrors()) {
                        MongoDBDAO.update(InstanceConstants.SAMPLE_COLL_NAME, sample);
                    } else {
                        logger.error("(" + sample + ") update failed: " + ctxValidation.getErrors());
                    }
                }
                continue;
            }
           getAndUpdateGenomeField(taxonCode,samples);
        }
    }

    private void updateGoatStatusAndGenomeDKB() {
        int batchSize = 100; // Taille du lot
        int pagination = 0; // initial
        List<Sample> samplesAtlaSea;           
        do {
           
            samplesAtlaSea = MongoDBDAO.find(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, DBQuery.in("projectCodes", ATLASEA_PROJECT))                    
            		.skip(pagination)
                    .limit(batchSize)
                    .toList();

            if (!samplesAtlaSea.isEmpty()) {

                Map<String, List<Sample>> samplesGroupedByCollabScientificName = groupSamplesByCollabScientificName(samplesAtlaSea);

                compareStatusToGoat(samplesGroupedByCollabScientificName);
                updateGenomeSizeDKB(samplesGroupedByCollabScientificName);

                pagination += batchSize; // Mise à jour de la pagination pour le prochain lot
            }
        } while (samplesAtlaSea.size() == batchSize);
    }
    
    private void updateGenomeSizeDKB(Map<String, List<Sample>> samplesGroupedByTaxon){

        for (Map.Entry<String, List<Sample>> entry : samplesGroupedByTaxon.entrySet()) {
            String collabScientificName = entry.getKey();
            List<Sample> samples = entry.getValue();
           

            if (collabScientificName == null) {
                Logger.error ("collabScientificName introuvé avec le code sample");
                continue;
            }

            // Je teste si le collabScientificName == ncbiScientificname si oui j'ai le bon Taxon id
            if(collabScientificName.equals(samples.get(0).ncbiScientificName) || collabScientificName.contains("sp.") || collabScientificName.contains("cf.") || collabScientificName.contains("aff.") ){
              
                getAndUpdateGenomeField(samples.get(0).taxonCode,samples);

            }else{
                try {
                    AtsZone paramDoc = MongoDBDAO.findOne(
                    InstanceConstants.PARAMETER_COLL_NAME,
                    AtsZone.class,
                    DBQuery.is("typeCode", "value-atlasea-zone")
                );

                String cleanedCollabName = cleanSuffixes(collabScientificName, paramDoc.values);

                Logger.info("cleanedCollabName: "+cleanedCollabName);
                   
                    String encodedCollabScientificName = URLEncoder.encode(cleanedCollabName, "UTF-8").replace("+", "%20");;
        
                    // Appel à l'API goat avec le CollabScientificName
                    String goatUrl = "https://goat.genomehubs.org/api/v2/lookup?searchTerm=" + encodedCollabScientificName ;
                    URL url = new URL(goatUrl);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
    
                    // Lire la réponse de l'API
                    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String inputLine;
                    StringBuilder response = new StringBuilder();
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();
    
                    // Convertir la réponse en objet JsonNode
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode jsonTaxon = mapper.readTree(response.toString());

                    // Récupérer le nombre de hits
                int hits = jsonTaxon.path("status").path("hits").asInt();

                hitsRules(hits,cleanedCollabName,samples,jsonTaxon,"Genome");
                          // Fermer la connexion
                    connection.disconnect();

                    Thread.sleep(300);
            } catch (Exception e) {
                e.printStackTrace();
            }
            }
        }
    } 

    // Mise à jours de la recherche 
    private void compareStatusToGoat(Map<String, List<Sample>> samplesGroupedByTaxon) {
        for (Map.Entry<String, List<Sample>> entry : samplesGroupedByTaxon.entrySet()) {
            String collabScientificName = entry.getKey();
            List<Sample> samples = entry.getValue();

            if (collabScientificName == null) {
                Logger.error ("collabScientificName non trouvé avec le code sample");
                continue;
            }
            //J'exclu les collabScientificName qui sont au genre et je mets leur status en null
            if( (collabScientificName.contains("sp.") || collabScientificName.contains("cf.") || collabScientificName.contains("aff.")) ){
              try {

                if (!collabScientificName.contains("RCC")) {

                    for (Sample sample : samples) {
                        setStatus(sample, null);
                    }
                }else{
                    AtsZone paramDoc = MongoDBDAO.findOne(
                    InstanceConstants.PARAMETER_COLL_NAME,
                    AtsZone.class,
                    DBQuery.is("typeCode", "value-atlasea-zone")
                    );
                
                    String cleanCollab =cleanSuffixesKeepRCC(collabScientificName, paramDoc.values);
                    try {
                        String encodedName = URLEncoder.encode(cleanCollab, "UTF-8")
                                                    .replace("+", "%20");

                        String goatUrl = "https://goat.genomehubs.org/api/v2/lookup?searchTerm=" + encodedName;
                        URL url = new URL(goatUrl);
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        connection.setRequestMethod("GET");

                        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        String inputLine;
                        StringBuilder response = new StringBuilder();

                        while ((inputLine = in.readLine()) != null) {
                            response.append(inputLine);
                        }
                        in.close();

                        ObjectMapper mapper = new ObjectMapper();
                        JsonNode jsonTaxon = mapper.readTree(response.toString());

                        int hits = jsonTaxon.path("status").path("hits").asInt();
                        if (hits > 0) {
                            // → appel Goat via ta méthode existante
                            hitsRules(hits, collabScientificName, samples, jsonTaxon, "Goat");
                        } else {
                            for (Sample s : samples) {
                                setStatus(s, null);
                            }
                        }

                        connection.disconnect();
                        Thread.sleep(300);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    return;
                }
                                
            } catch (Exception e) {
                    e.printStackTrace();
            }
                // Je teste si le collabScientificName == ncbiScientificname si oui j'ai le bon Taxon id
            }else if ( collabScientificName.equals(samples.get(0).ncbiScientificName )){
  
                  getAndUpdateGoatField(samples.get(0).taxonCode, samples);
                   
            }else{
            
                try {
                    
                    AtsZone paramDoc = MongoDBDAO.findOne(
                    InstanceConstants.PARAMETER_COLL_NAME,
                    AtsZone.class,
                    DBQuery.is("typeCode", "value-atlasea-zone")
                );
                //rajouter if 

               
                 String cleanedCollabName = cleanSuffixes(collabScientificName, paramDoc.values);
                 Logger.info("cleanedCollabName: "+cleanedCollabName);
                
                    String encodedCollabScientificName = URLEncoder.encode(cleanedCollabName, "UTF-8").replace("+", "%20");;
                    // Appel à l'API goat avec le CollabScientificName
                    String goatUrl = "https://goat.genomehubs.org/api/v2/lookup?searchTerm=" + encodedCollabScientificName ;
                    URL url = new URL(goatUrl);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
    
                    // Lire la réponse de l'API
                    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String inputLine;
                    StringBuilder response = new StringBuilder();
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();
    
                    // Convertir la réponse en objet JsonNode
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode jsonTaxon = mapper.readTree(response.toString());
    
                   
                    // Récupérer le nombre de hits
                    int hits = jsonTaxon.path("status").path("hits").asInt();
    
                    hitsRules(hits,cleanedCollabName,samples,jsonTaxon,"Goat");
                    
                     // Fermer la connexion
                    connection.disconnect();
    
                    Thread.sleep(300);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }               
    }
    private String cleanSuffixes(String name, List<String> allSuffixes) {
        String cleanedName = name.replaceAll("(?i)\\s*str\\..*$", "").trim();
        List<String> suffixesToRemove;
                Logger.info(name);
        Logger.info(cleanedName);
        suffixesToRemove = allSuffixes;

        boolean suffixRemoved;
        do {
            suffixRemoved = false;
            for (String suffix : suffixesToRemove) {
                if (cleanedName.matches(".*\\s+" + suffix + "$")) {
                    cleanedName = cleanedName.replaceAll("\\s+" + suffix + "$", "").trim();
                    suffixRemoved = true;
                }
            }
        } while (suffixRemoved);
        Logger.info("retour: "+cleanedName);

        return cleanedName;
    }

    private String cleanSuffixesKeepRCC(String name, List<String> allSuffixes) {
         String cleanedName = name;
        List<String> suffixesToRemove = new ArrayList<>();

        for (String suffix : allSuffixes) {
            // On identifie les suffixes RCC
            if (suffix.matches("RCC\\s*\\w+")) {
                continue; // RCC → on garde
            }
            suffixesToRemove.add(suffix);
        }
        boolean suffixRemoved;
        do {
            suffixRemoved = false;
            for (String suffix : allSuffixes) {
                if (cleanedName.matches(".*\\s+" + suffix + "$")) {
                    cleanedName = cleanedName.replaceAll("\\s+" + suffix + "$", "").trim();
                    suffixRemoved = true;
                }
            }
        } while (suffixRemoved);

        return cleanedName;
    }

    private void getAndUpdateGoatField (String taxonCode, List<Sample> samples){

        // Appel à l'API goat avec le code de taxon
        try {
            Logger.info("taxoncode: "+taxonCode);
            String goatUrl = "https://goat.genomehubs.org/api/v2/record?recordId=" + taxonCode;
            URL url = new URL(goatUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            // Lire la réponse de l'API
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            // Convertir la réponse en objet JsonNode
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonTaxon = mapper.readTree(response.toString());

            JsonNode records = jsonTaxon.get("records");
            if (records.size() > 0) {
            
                JsonNode record = records.get(0).get("record");
                if (record != null) {
                    
                    JsonNode scientificNameNode = record.get("scientific_name");
                    String scientificName = scientificNameNode != null ? scientificNameNode.asText() : "NO_NAME";
                    JsonNode attributes = record.get("attributes");
                    if (attributes != null) {
                        
                        // récupération de valeur recherchée :
                        JsonNode longListNode = attributes.get("long_list");
                        JsonNode sequencingStatusAtlaseaNode = attributes.get("sequencing_status_atlasea");
                        JsonNode sequencingStatusNode = attributes.get("sequencing_status");
                       

                        String sequencingStatusAtlasea = sequencingStatusAtlaseaNode != null ? sequencingStatusAtlaseaNode.get("value").asText() : "NO_STATUS";
                      
                        String sequencingStatus = null;
                        
                        List<String> projectStatus = new ArrayList<>();

                        if (longListNode == null) {
                           
                            // Cas où long_list est absent
                            if (sequencingStatusNode != null) {
                                sequencingStatus = sequencingStatusNode.get("value").asText();
                                JsonNode currentGoatProjectNode = attributes.get(sequencingStatus);

                                if (currentGoatProjectNode != null && currentGoatProjectNode.has("value")) {
                                    JsonNode valueArray = currentGoatProjectNode.get("value");
                                    if (valueArray.isArray()) {
                                        for (JsonNode value : valueArray) {
                                            projectStatus.add(value.asText());
                                        }
                                        projectStatus.sort(String::compareTo);
                                    }
                                } else {
                                    projectStatus = Arrays.asList("NO_PROJECTS");
                                }
                            } else {
                                sequencingStatus = "NO_STATUS";
                                sequencingStatusAtlasea = "NO_STATUS";
                                projectStatus = Arrays.asList("NO_PROJECTS");
                            }
                        } else {
                            // Cas où long_list est présent
                            if (sequencingStatusNode != null) {
                                sequencingStatus = sequencingStatusNode.get("value").asText();
                                JsonNode currentGoatProjectNode = attributes.get(sequencingStatus);

                                if (sequencingStatusAtlasea.equals("NO_STATUS")) {
                                    JsonNode valueArray = longListNode.get("value");
                                    for (JsonNode value : valueArray) {
                                        if (value.asText().equals("ATLASEA")) {
                                            sequencingStatusAtlasea = "long_list";
                                        }
                                    }
                                }

                                if (sequencingStatus.equals("data_generation") || sequencingStatus.equals("in_assembly")) {
                                    JsonNode valueArray = attributes.get("in_progress").get("value");
                                    if (valueArray.isArray()) {
                                        for (JsonNode value : valueArray) {
                                            projectStatus.add(value.asText());
                                        }
                                        projectStatus.sort(String::compareTo);
                                    }
                                } else if (currentGoatProjectNode != null && currentGoatProjectNode.has("value")) {
                                    JsonNode valueArray = currentGoatProjectNode.get("value");
                                    if (valueArray.isArray()) {
                                        for (JsonNode value : valueArray) {
                                            projectStatus.add(value.asText());
                                        }
                                        projectStatus.sort(String::compareTo);
                                    }
                                } else {
                                    projectStatus = Arrays.asList("NO_PROJECTS");
                                }

                            } else {
                                sequencingStatus = "long_list";
                                JsonNode currentGoatProjectNode = attributes.get(sequencingStatus);

                                if (currentGoatProjectNode != null && currentGoatProjectNode.has("value")) {
                                    JsonNode valueArray = currentGoatProjectNode.get("value");
                                    if (valueArray.isArray()) {
                                        for (JsonNode value : valueArray) {
                                            projectStatus.add(value.asText());
                                            if (value.asText().equals("ATLASEA")) {
                                                sequencingStatusAtlasea = "long_list";
                                            }
                                        }
                                        projectStatus.sort(String::compareTo);
                                    }
                                } else {
                                    projectStatus = Arrays.asList("NO_PROJECTS");
                                }
                            }
                        }

                       
                        for (Sample sample : samples) {
                            boolean isUpdate = false;
                            // Création des propriétés :
                            StringBuilder modifications = new StringBuilder();
                            
                            PropertyValue currentSequencingStatusAtlasea = sample.properties.get("currentGoatStatusATS");
                            PropertyValue currentSequencingStatus = sample.properties.get("currentGoatStatus");
                            PropertyValue currentInprogressStatus = sample.properties.get("currentGoatProject");

                            String messageBase = sample.projectCodes + ";" + sample.code + ";" + scientificName + ";"
                            + (currentSequencingStatusAtlasea != null ? currentSequencingStatusAtlasea.value : "null") + ";"
                            + sequencingStatusAtlasea + ";"
                            + (currentSequencingStatus != null ? currentSequencingStatus.value : "null") + ";"
                            + sequencingStatus + ";"
                            + (currentInprogressStatus != null ? currentInprogressStatus.value : "null") + ";"
                            + projectStatus;

                            if (currentSequencingStatusAtlasea == null || !sequencingStatusAtlasea.equals(currentSequencingStatusAtlasea.value)) {
                                modifications.append("Statut Atlasea ");

                                PropertyValue sequencingStatusAtlaseaPropertyValue = new PropertySingleValue();
                                sequencingStatusAtlaseaPropertyValue.value = sequencingStatusAtlasea;

                                sample.properties.put("currentGoatStatusATS", sequencingStatusAtlaseaPropertyValue);

                                isUpdate = true;
                            }

                            if (currentSequencingStatus == null || !sequencingStatus.equals(currentSequencingStatus.value)) {
                                modifications.append("Statut courant ");
                                PropertyValue sequencingStatusPropertyValue = new PropertySingleValue();
                                sequencingStatusPropertyValue.value = sequencingStatus;

                                sample.properties.put("currentGoatStatus", sequencingStatusPropertyValue);

                                isUpdate = true;
                            }
                           
                            if (currentInprogressStatus == null || !CollectionUtils.isEqualCollection(projectStatus, (List<String>) currentInprogressStatus.value)) {
                                modifications.append("Projet courant ");
                                PropertyListValue inProgressStatusPropertyValue = new PropertyListValue();
                                inProgressStatusPropertyValue.value = projectStatus;

                                sample.properties.put("currentGoatProject", inProgressStatusPropertyValue);

                                isUpdate = true;
                            }

                            if (isUpdate) {        
                                String logMessage = messageBase+ "; [" + modifications.toString() + "]";
                                modificationsLog.add(logMessage);

                                sample.traceInformation.modificationStamp(DATA_USER);

                                ContextValidation ctxValidation = ContextValidation.createUpdateContext(DATA_USER);
                                sample.validate(ctxValidation);

                                if (!ctxValidation.hasErrors()) {
                                    MongoDBDAO.update(InstanceConstants.SAMPLE_COLL_NAME, sample);
                                } else {
                                    logger.error("(" + sample + ") update failed: " + ctxValidation.getErrors());
                                }
                            }
                        }
                    } else {
                        logger.error("Le champ attributes n'existe pas avec ce code de taxon");
                    }

                } else {
                    logger.error("Le champ record n'existe pas avec ce code de taxon");
                }

            } else {
                // Création de propriété pour les taxon non présent dans GOAT
                logger.error("Le code taxon: " + taxonCode + " n'est pas trouvé dans Goat");
                for (Sample sample : samples) {
                    setStatus(sample,"NO_STATUS");
                }
            }

            // Fermer la connexion
            connection.disconnect();

            Thread.sleep(300);
        } catch (Exception e) {
            e.printStackTrace();
        } 
    }

    private void getAndUpdateGenomeField(String taxonCode,List<Sample> samples){
        try {
            logger.debug("taxon: "+ taxonCode);
            // Appel à l'API goat avec le code de taxon
            String goatUrl = "https://goat.genomehubs.org/api/v2/record?recordId=" + taxonCode;
            URL url = new URL(goatUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            // Lire la réponse de l'API
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            // Convertir la réponse en objet JsonNode
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonTaxon = mapper.readTree(response.toString());

            JsonNode records = jsonTaxon.get("records");
            if (records.size() > 0) {
                JsonNode record = records.get(0).get("record");
                if (record != null) {
                    JsonNode attributes = record.get("attributes");
                    if (attributes != null) {
                        // récupération de valeur recherchée :
                        JsonNode genomeSizeNode = attributes.get("genome_size");
                        Long genomeSizeStatus = genomeSizeNode.get("value").asLong();

                        for (Sample sample : samples) {
                            boolean isUpdate = false;

                            PropertyValue currentGenomeSizeStatus = sample.properties.get("taxonSize");
                            if (currentGenomeSizeStatus == null || !genomeSizeStatus.equals(currentGenomeSizeStatus.value)) {

                                String logMessage = sample.projectCodes+";"+sample.code+";"+sample.ncbiScientificName+";" + ((currentGenomeSizeStatus == null) ? "null" : currentGenomeSizeStatus.value)+";"+genomeSizeStatus;
                               
                                modificationsLogGenome.add(logMessage);
                                

                                if(genomeSizeStatus >0){
                                    PropertyValue genomeSizeStatusPropertyValue = new PropertySingleValue();
                                    genomeSizeStatusPropertyValue.value = genomeSizeStatus;

                                    sample.properties.put("taxonSize", genomeSizeStatusPropertyValue);

                                    isUpdate = true;
                                }else{
                                    logger.error("genomeSizeStatus n'a pas été trouvé ");
                                }
                            }

                            if (isUpdate) {
                                sample.traceInformation.modificationStamp(DATA_USER);

                                ContextValidation ctxValidation = ContextValidation.createUpdateContext(DATA_USER);
                                sample.validate(ctxValidation);

                                if (!ctxValidation.hasErrors()) {
                                    MongoDBDAO.update(InstanceConstants.SAMPLE_COLL_NAME, sample);
                                } else {
                                    logger.error("(" + sample + ") update failed: " + ctxValidation.getErrors());
                                }
                            }
                        }
                    } else {
                        logger.error("Le champ attributes n'existe pas avec ce code de taxon");
                    }

                } else {
                    logger.error("Le champ record n'existe pas avec ce code de taxon");
                }
            } 
             // Fermer la connexion
             connection.disconnect();

             Thread.sleep(300);
         } catch (Exception e) {
             e.printStackTrace();
         } 
    }


    private Map<String, List<Sample>> groupSamplesByTaxon(List<Sample> samples) {
        return samples.stream()
                .collect(Collectors.groupingBy(sample -> sample.taxonCode));
    }

    // NGL-4870 : on modifie la requete on passe par le ncbiScientificName 
    private Map<String, List<Sample>> groupSamplesByCollabScientificName(List<Sample> samples) {
        String propertiesCollabScientificName = "collabScientificName";
        return samples.stream()
                .filter(sample -> sample.properties != null && sample.properties.get(propertiesCollabScientificName) != null)
                .collect(Collectors.groupingBy(sample -> (String) sample.properties.get(propertiesCollabScientificName).value));

    }
   
    private void setStatus(Sample sample, String statut) {
        // Récupérer la valeur actuelle
        String ancienneValeur = sample.properties.get("currentGoatStatus") != null 
                    ? (String) sample.properties.get("currentGoatStatus").value 
                    : null;          
         

            if (!Objects.equals(statut, ancienneValeur) ){
                
            // Mise à jour des propriétés
            PropertyValue noStatusPropertyValue = new PropertySingleValue();
            noStatusPropertyValue.value = statut;
    
            List<String> valueList = new ArrayList<>();
            valueList.add(statut);
    
            PropertyValue noStatusPropertyListValue = new PropertyListValue();
            noStatusPropertyListValue.value = valueList;
            String ancienneValeurcourant = sample.properties.get("currentGoatStatus") != null 
                    ? (String) sample.properties.get("currentGoatStatus").value 
                    : null; 
                    
            String ancienneValeurAts = sample.properties.get("currentGoatStatusATS") != null 
                    ? (String) sample.properties.get("currentGoatStatusATS").value 
                    : null; 

            ArrayList<String> ancienneValeurProject = sample.properties.get("currentGoatProject") != null 
                    ? (ArrayList<String>) sample.properties.get("currentGoatProject").value 
                    : null; 

            sample.properties.put("currentGoatStatusATS", noStatusPropertyValue);
            sample.properties.put("currentGoatStatus", noStatusPropertyValue);
            sample.properties.put("currentGoatProject", noStatusPropertyListValue);

           
    
            // Construction du message de log
            String messageBase = sample.projectCodes + ";" + sample.code + ";" + sample.ncbiScientificName + ";"
                    + ancienneValeurAts+ ";"
                    + statut + ";"
                    + ancienneValeurcourant+ ";"
                    + statut + ";"
                    +ancienneValeurProject + ";"
                    + statut;
    
            String logMessage = messageBase + "; [" + "Statut Atlasea " + "Statut courant " + "Projet courant " + "]";
            modificationsLog.add(logMessage);

    
            // Mise à jour des informations de traçage et validation
            sample.traceInformation.modificationStamp(DATA_USER);
    
            ContextValidation ctxValidation = ContextValidation.createUpdateContext(DATA_USER);
            sample.validate(ctxValidation);
    
            // Mise à jour dans la base de données si validation réussie
            if (!ctxValidation.hasErrors()) {
                MongoDBDAO.update(InstanceConstants.SAMPLE_COLL_NAME, sample);
            } else {
                logger.error("(" + sample + ") mise à jour échouée : " + ctxValidation.getErrors());
            }
        }
    }
   
    private void hitsRules (int hits ,String collabScientificName,List<Sample> samples,JsonNode jsonTaxon,String fieldType){
        //Cas 5 --------------- Pas de Hit
        
        if(hits == 0){
           
            for(Sample sample : samples){

                setStatus(sample, "NO_STATUS" );
                
            };
           
            getAndUpdateGenomeField(samples.get(0).taxonCode,samples);

        }

        //Cas 1 -------------- Hit =1
       
        if(hits == 1){
            // recupere le taxon id
            JsonNode results = jsonTaxon.get("results");

            if (results.size()>0) {
                JsonNode result = results.get(0).get("result");
              
                if (result != null){
                    String taxonId = result.get("taxon_id").asText();
                  
                    if("Goat".equals(fieldType)){
                        getAndUpdateGoatField(taxonId,samples);
                    }else{
                        getAndUpdateGenomeField(taxonId,samples);
                    }
                }
            }
        }

        //Cas 2 ---------------- Hit >2

        if (hits > 1) {
            JsonNode results = jsonTaxon.get("results");

            boolean isFound = false;
            for (int i=0; i<results.size(); ++i){
                JsonNode result = results.get(i).get("result");
       
                String scientific_name = result.get("scientific_name").asText();
                if (collabScientificName.equals(scientific_name)) {

                    String taxonId = result.get("taxon_id").asText();
                   
                    isFound = true;
                    if(fieldType.equals("Goat")){
                        getAndUpdateGoatField(taxonId,samples);
                    }else{
                        getAndUpdateGenomeField(taxonId,samples);
                    }
                    break;

                }
            }
            
            // Cas 3 ------------- Si je n'ai pas de hit qui a la valeur exacte de l'espèce dans scientific name alors je cherche le hit qui a la valeur name=le nom exact de l'espèce avec class="synonym"
            if (isFound == false) {
                int occurence = 0;
                String taxonId = null;

                for (int i=0; i<results.size(); i++){
                    JsonNode result = results.get(i).get("result");
                   
                    JsonNode taxonNames = result.path("taxon_names");
                    if (taxonNames.isArray()) {
                       
                    
                        for (int j=0; j<taxonNames.size(); j++) {
                            JsonNode taxonName = taxonNames.get(j);
                            String name = taxonName.get("name").asText();
                            String className = taxonName.get("class").asText();
                            if (collabScientificName.equals(name) && "synonym".equals(className)) {
                                occurence++;
                            }
                        }
                    }
                }

                for (int i=0; i<results.size(); i++){
                    JsonNode result = results.get(i).get("result");
                   
                    JsonNode taxonNames = result.path("taxon_names");
                    if (taxonNames.isArray()) {
                       
                    
                        for (int j=0; j<taxonNames.size(); j++) {
                            JsonNode taxonName = taxonNames.get(j);
                            String name = taxonName.get("name").asText();
                            String className = taxonName.get("class").asText();
                            if (collabScientificName.equals(name) && "synonym".equals(className)) {
                                taxonId = result.get("taxon_id").asText();
                            }
                        }
                        // Cas 5 : aucun synonym exact → NO_STATUS
                        if (occurence == 0 && taxonId == null) {
                            for (Sample sample : samples) {
                                setStatus(sample, "NO_STATUS");
                            }
                            return;
                        }
                      
                        // Cas 4 -------------------- j'ai plusieurs hits avec le même name dans les synonymes
                        if (occurence > 1) {
                            if(fieldType.equals("Genome")){
                                getAndUpdateGenomeField(samples.get(0).taxonCode,samples);
                            }else{
                                for (Sample sample : samples) {
                                   setStatus(sample, "MULTIPLE_HIT");
                                }
                            }
                            
                        } else if (taxonId != null) {
                            if(fieldType.equals("Goat")){
                                getAndUpdateGoatField(taxonId,samples);
                            }else{
                                getAndUpdateGenomeField(taxonId,samples);
                            }
                        }
                    }
                }
            }

        }
    }
         
    public String pluriel(List<String> projectlist){
        return projectlist.size()>1? "s":"";
        
    }
    
    private static final List<String> FORBIDDEN_STRINGS = Arrays.asList("sp.", "cf.", "aff.");

    private static final String EXPEDITEUR_KEY = "reporting.email.from";

	private static final String DESTINATAIRES_KEY_ATLASEA = "atlasea.goat.reporting.email.to";

    private static final String DESTINATAIRES_KEY_GENOME = "atlasea.genome.reporting.email.to";

	private static final String ENV_KEY = "ngl.env";

    private static final int SEUIL_PJ = 100;

    private MailServices mailServices = new MailServices();


    /**
	 * Méthode permettant de construire le contenu du mail de reporting.
	 * 
	 * @param END_CRON_DATE La date de fin d'exécution du CRON.
	 * @param NB_TOTAL Le nombre total de samples à prendre en charge dans le CRON.
	 * 
	 * @return Une chaîne de caractères représentant le mail de reporting qui sera envoyé.
	 * 
	 * @throws UnsupportedEncodingException Si l'encodage spécifié pour le contenu du mail n'est pas bon.
	 */
	public String buildMailBodyAtlasea() throws UnsupportedEncodingException {
		StringBuilder message = new StringBuilder();
       
        if (!modificationsLog.isEmpty()) {
            if (modificationsLog.size() < SEUIL_PJ) {
                message.append("<br /><b>Modifications effectuées :</b><br />");
                message.append("<br />Projet;Code Sample;Taxon,Statut ATLASea;Statut ATLASea courant;Statut ;Statut courant;Projet;Projet courant;Donnée(s) mise à jour<br />");
                modificationsLog.forEach(modification -> message.append(modification + "<br />"));
            } else {
                message.append("La liste complète des samples avec les modifications provenant de GOAT est en pièce-jointe du mail.<br /><br />");
            }
        } else {
			message.append("Aucune modification de status GOAT effectuée.<br /><br />");
		}
        return new String(message.toString().getBytes(), "iso-8859-1");
    }

    public String buildMailBodyGenome() throws UnsupportedEncodingException {
		StringBuilder message = new StringBuilder();
        if(!modificationsLogGenome.isEmpty()){
            if (modificationsLogGenome.size() < SEUIL_PJ) {
                message.append("<br /><b>Modifications effectuées :</b><br />");
                message.append("<br />Projet;Code Sample;Taxon,Taille du génome;Taille du génome courant <br />");
                modificationsLogGenome.forEach(modification -> message.append(modification + "<br />"));
            } else {
                message.append("La liste complète des samples avec les modifications de la taille du génome est en pièce-jointe du mail.<br /><br />");
            }
        } else {
			message.append("Aucune modification de taille de génome effectuée.<br /><br />");
		}
        

		return new String(message.toString().getBytes(), "iso-8859-1");
	}

    /**
	 * Méthode permettant de récupérer l'expéditeur du mail de reporting.
	 * 
	 * @return Une chaîne de caractères représentant l'expéditeur du mail de reporting.
	 */
	public String getEmailExpediteur() {
		return ConfigFactory.load().getString(EXPEDITEUR_KEY); 
	}

	/**
	 * Méthode permettant de récupérer les destinataires qui vont recevoir le mail de reporting.
	 * 
	 * @return Une chaîne de caractères représentant le / les destinataires du mail de reporting.
	 */
	public HashSet<String> getEmailDestinatairesAtlasea() {
		final String DESTINATAIRES = ConfigFactory.load().getString(DESTINATAIRES_KEY_ATLASEA);   
		return new HashSet<String>(Arrays.asList(DESTINATAIRES.split(",")));
	}

    public HashSet<String> getEmailDestinatairesGenome() {
		final String DESTINATAIRES = ConfigFactory.load().getString(DESTINATAIRES_KEY_GENOME);   
		return new HashSet<String>(Arrays.asList(DESTINATAIRES.split(",")));
	}

	/**
	 * Méthode permettant de récupérer l'environnement pour lequel le CRON est exécuté.
	 * 
	 * @return Une chaîne de caractères représentant l'environnement dans lequel le CRON est exécuté.
	 */
	public String getEnvironment() {
		return ConfigFactory.load().getString(ENV_KEY);
	}

	/**
	 * Méthode permettant de récupérer l'institut pour lequel le CRON est exécuté.
	 * 
	 * @return Une chaîne de caractères représentant l'institut pour lequel le CRON est exécuté.
	 */
	
	/**
	 * Méthode permettant de construire le mail de reporting et de l'envoyer.
	 * 
	 * @param END_CRON_DATE La date de fin d'exécution du CRON.
	 * @param NB_TOTAL Le nombre total d'échantillons traités.
	 * 
	 * @see buildMailBody()
	 * @see MAIL_SERVICES.sendMail(EXPEDITEUR, destinataires, SUBJECT, message)
	 */

    public void buildForSendAtlasea(){

        final String EXPEDITEUR = getEmailExpediteur();
		final HashSet<String> DESTINATAIRES_ATLASEA = getEmailDestinatairesAtlasea();
		final String ENVIRONMENT = getEnvironment();
        try{
            String message = buildMailBodyAtlasea();
            String subject = "[NGL-DATA] [" + ENVIRONMENT + "] [CNS] Exécution des mise à jours de status GOAT (UpdateSampleAtlaSea) terminée";

            if (needAttachmentAtlasea()) {		
                Date currentDate = new Date();
    
                String attachment = buildMailAttachmentAtlasea();
                String fileName = "rapport-reporting-UpdateSampleAtlaSea_Status_GOAT" + currentDate.getTime() + ".txt";
               
                try {
                    BufferedWriter writer = new BufferedWriter(new FileWriter(tmpDirectory() + fileName));
    
                    writer.write(attachment);
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
    
                File file = new File(tmpDirectory() + fileName);
    
                if (!file.exists()) {
                    logger.error("Erreur lors de l'écriture du fichier de log. Il n'a pas été créé.");
                }
    
                int ALMOST_ONE_MB = 900000;
    
                if (file.length() > ALMOST_ONE_MB) {
                    message += "Le fichier de log est trop volumineux. Il est disponible &agrave; cette adresse : " + file.getAbsolutePath();
    
                    mailServices.sendMail(EXPEDITEUR, DESTINATAIRES_ATLASEA, subject, message); 
                } else {   
                    mailServices.sendMail(EXPEDITEUR, DESTINATAIRES_ATLASEA, subject, message, file);    
                }	
            } else {
                mailServices.sendMail(EXPEDITEUR, DESTINATAIRES_ATLASEA, subject, message);    
            }
        } catch (IOException | MailServiceException e) {
			e.printStackTrace();
		}
    }

    public void buildForSendGenome(){

        final String EXPEDITEUR = getEmailExpediteur();
        final HashSet<String> DESTINATAIRES_GENOME = getEmailDestinatairesGenome();
		final String ENVIRONMENT = getEnvironment();

        try {
            String message = buildMailBodyGenome();
            String subject = "[NGL-DATA] [" + ENVIRONMENT + "] [CNS] Exécution des mise à jours des tailles de génomes (UpdateSampleAtlaSea) terminée";

            message = buildMailBodyGenome();
            if (needAttachmentGenome()) {		
				Date currentDate = new Date();

				String attachment = buildMailAttachmentGenome();
				String fileName = "rapport-reporting-UpdateSampleAtlaSea_Genome" + currentDate.getTime() + ".txt";
               
                try {
					BufferedWriter writer = new BufferedWriter(new FileWriter(tmpDirectory() + fileName));
	
					writer.write(attachment);
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}

				File file = new File(tmpDirectory() + fileName);

				if (!file.exists()) {
					logger.error("Erreur lors de l'écriture du fichier de log. Il n'a pas été créé.");
				}

				int ALMOST_ONE_MB = 900000;

				if (file.length() > ALMOST_ONE_MB) {
					message += "Le fichier de log est trop volumineux. Il est disponible &agrave; cette adresse : " + file.getAbsolutePath();

					mailServices.sendMail(EXPEDITEUR, DESTINATAIRES_GENOME, subject, message);
                    
				} else {
					mailServices.sendMail(EXPEDITEUR, DESTINATAIRES_GENOME, subject, message, file);
				}	
			} else { 
				mailServices.sendMail(EXPEDITEUR, DESTINATAIRES_GENOME, subject, message);
			}
		} catch (IOException | MailServiceException e) {
			e.printStackTrace();
		}

    }

    // Envoie des deux mails
	public void buildAndSendMail() {

       buildForSendAtlasea();
       buildForSendGenome();
		
	}

    /**
	 * Méthode permettant de construire la pièce-jointe du mail de reporting.
	 * 
	 * @return Une chaîne de caractères représentant la pièce-jointe du mail de reporting.
	 */
	public String buildMailAttachmentAtlasea() throws UnsupportedEncodingException {
		StringBuilder attachment = new StringBuilder();

        attachment.append("Modifications effectuées :\n");
        attachment.append("Projet,Code Sample,Taxon,Statut ATLASea,Statut ATLASea courant,Statut ,Statut courant,Projet,Projet courant,Donnée(s) mise à jour \n");
		modificationsLog.forEach((modif -> {
			attachment.append(modif + "\n");
		})); 

        return new String(attachment.toString().getBytes(), "utf-8");
    }

    public String buildMailAttachmentGenome() throws UnsupportedEncodingException {
		StringBuilder attachment = new StringBuilder();

        attachment.append("Modifications effectuées :\n");
        attachment.append("Projet,Code Sample,Taxon,Taille du génome,Taille du génome courant \n");
        modificationsLogGenome.forEach((modif -> {
			attachment.append(modif + "\n");
        })); 

		return new String(attachment.toString().getBytes(), "utf-8");
	}

    /**
	 * Méthode permettant de savoir si on doit ajouter une pièce-jointe au mail de reporting.
	 * La règle est la suivante : si on a plus de samples que le seuil (aujourd'hui à 100), alors on doit ajouter une pièce-jointe.
	 * 
	 * @return true si on a besoin d'ajouter une pièce-jointe au mail de reporting. false sinon.
	 * 
	 * @see SEUIL_PJ
	 */
	public boolean needAttachmentAtlasea() {
		return modificationsLog.size() > SEUIL_PJ ;
	}

    public boolean needAttachmentGenome() {
		return modificationsLogGenome.size() > SEUIL_PJ;
	}

    /**
	 * tmpDirectory()
	 * 
	 * @return
	 */
	private static final String tmpDirectory() {
		return ConfigFactory.load().getString("mail.tmp.directory");
	}

}
