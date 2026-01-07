package scripts;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.mongojack.DBQuery;
import org.springframework.util.CollectionUtils;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.MongoDBResult;
import fr.cea.ig.lfw.controllers.scripts.buffered.ScriptWithArgsAndExcelBody;
import fr.cea.ig.ngl.NGLApplication;
import models.laboratory.common.instance.Comment;
import models.laboratory.resolutions.instance.Resolution;
import models.laboratory.resolutions.instance.ResolutionConfiguration;
import models.laboratory.sample.instance.Sample;
import models.utils.InstanceConstants;
import play.libs.Json;
import validation.ContextValidation;

/*
 * Exemple d'Utilisation : localhost:9000/scripts/run/scripts.UpdateSamplesResolutions?jira=SUPSQ-XXXX&header=true&strategy=APPEND
 * 
 * Le script sais gérer l'ajout de plusieurs résolutions sur un même sample, il faut faire une ligne par résolution en répétant à chaque fois le sample.
 * 
 * Le script nécessite en body un Excel avec en colonnes :
 * | Code Sample | Nom résolution | Commentaire (optionnelle) |
 * 
 * Les paramètres du script sont :
 * 
 * jira : 
 * - le ticket de support ou de dévellopement associé
 * header : 
 * - true ou false, permet de savoir s'il faut ignorer la première ligne du fichier
 * strategy : 
 * - la stratégie de correction
 * - CREATE : rajoute une nouvelle résolution, uniquement s'il n'y en a pas déjà
 * - APPEND : rajoute de nouvelles résolutions sans enlever les précédentes
 */
public class UpdateSamplesResolutions extends ScriptWithArgsAndExcelBody<scripts.UpdateSamplesResolutions.Args> {
    
    public static final String USER = "ngl-support";
    
    public static class Args {
        public String jira;
        public String header;
        public String strategy;
        
        private boolean isValidJira() {
            return this.jira.matches("^(SUPSQ|SUPSQCNG|NGL)-\\d+$");
        }
        
        private boolean isValidHeader() {
            return "true".equals(this.header) || "false".equals(this.header);
        }
        
        private boolean isValidStrategy() {
            try {
                Strategy.valueOf(strategy);
                return true;
            } catch(IllegalArgumentException e) {
                return false;
            }
        }
        
        public boolean isThereHeaderLine() {
            return Boolean.parseBoolean(header);
        }
        
        public Strategy getStrategy() {
            return Strategy.valueOf(strategy);
        }
        
        public void validate() {
            if ( ! this.isValidJira() ) {
                throw new RuntimeException("argument jira " +  jira + " qui n'a pas la forme attendue SUPSQ-XXX ou SUPSQCNG-XXX ou NGL-XXX");
            }
            if ( ! isValidHeader() ) {
                throw new RuntimeException("argument header " + header + " incorrect : utiliser 'true' ou 'false'");
            }
            if ( ! isValidStrategy() ) {
                throw new RuntimeException("argument strategy " + strategy + " incorrect : utiliser 'APPEND'");
            }
        }
    }
    
    public static enum Strategy {
        CREATE, APPEND;
        
        static Map<String, String> resolutionCodes;
        
        private Map<String, String> getResolutionMapping() {
            Map<String, String> mapping = new HashMap<>();
            MongoDBResult<ResolutionConfiguration> result = MongoDBDAO.find(InstanceConstants.RESOLUTION_COLL_NAME, ResolutionConfiguration.class, DBQuery.is("objectTypeCode", "Sample"));
            result.cursor.forEachRemaining((ResolutionConfiguration resolutionConfiguration) -> {
                resolutionConfiguration.resolutions.forEach((Resolution resolution) -> mapping.put(resolution.name, resolution.code));
            });
            return mapping;
        }
        
        private void addComment(Sample sample, String newCommentText) {
            if(CollectionUtils.isEmpty(sample.comments)) {
                Comment comment = new Comment(newCommentText, USER, false);
                sample.comments = new ArrayList<Comment>();
                sample.comments.add(comment);
            }else {
                Comment currentComment = sample.comments.get(0);
                String currentCommentText = currentComment.comment;
                currentComment.comment = currentCommentText + " ; " + newCommentText;
            }
        }
        
        public void init(Sample sample) {
            switch(this) {
                case CREATE : if(sample.resolutionCodes == null) sample.resolutionCodes = new HashSet<>(); break;
                case APPEND : if(sample.resolutionCodes == null) sample.resolutionCodes = new HashSet<>(); break;
            }
            if(resolutionCodes == null) resolutionCodes = getResolutionMapping();
        }
        
        private boolean applyCreate(ContextValidation ctx, Sample sample, RowContent content) {
            if(!resolutionCodes.containsKey(content.resolution)) { 
                ctx.addError("SampleCode " + sample.code, "Nom de résolution '" + content.resolution + "' non valide!");
                return false;
            }
            if(!sample.resolutionCodes.isEmpty()) {
                return false;
            }
            String resolutionCode = resolutionCodes.get(content.resolution);
            sample.resolutionCodes.add(resolutionCode); 
            if(content.isThereComment()) addComment(sample, content.comment);
            return true;
        }
        
        private boolean applyAppend(ContextValidation ctx, Sample sample, RowContent content) {
            if(!resolutionCodes.containsKey(content.resolution)) { 
                ctx.addError("SampleCode " + sample.code, "Nom de résolution '" + content.resolution + "' non valide!");
                return false;
            }
            String resolutionCode = resolutionCodes.get(content.resolution);
            sample.resolutionCodes.add(resolutionCode); 
            if(content.isThereComment()) addComment(sample, content.comment);
            return true;
        }
        
        public boolean apply(ContextValidation ctx, Sample sample, RowContent content) {
            switch(this) {
                case CREATE : return applyCreate(ctx, sample, content);
                case APPEND : return applyAppend(ctx, sample, content);
                default: return false;
            }
        }
    }
    
    public static class RowContent  {
        String sampleCode;
        String resolution;
        String comment;
        
        public boolean isThereComment() {
            return StringUtils.isNotBlank(comment);
        }
    }
    
    public static class UpdateData {
        Sample sample;
        Set<String> oldResolutionCodes;
        
        public UpdateData(Sample sample, Set<String> oldResolutionCodes) {
            this.sample = sample;
            this.oldResolutionCodes = oldResolutionCodes;
        }

        public void traceUpdate(String jira) {
            if (sample.technicalComments == null)  sample.technicalComments = new ArrayList<Comment>();  
            Comment technicalComment = new Comment(jira + " old resolutionCodes " + oldResolutionCodes, USER, true);
            sample.technicalComments.add(technicalComment);
            sample.traceInformation.setTraceInformation(USER);
        }

        public void validateSample(ContextValidation ctx) {
            sample.validate(ctx);
        }

        public String getSampleCode() {
            return sample.code;
        }

        public Set<String> getOldResolutions() {
            return oldResolutionCodes;
        }

        public Set<String> getNewResolutions() {
            return sample.resolutionCodes;
        }
    }
    
    private final NGLApplication    app;

    @Inject
    public UpdateSamplesResolutions(NGLApplication app) {
        this.app = app;
    }
    
    private Stream<Row> getRows(boolean header, XSSFWorkbook workbook) {
        XSSFSheet firstSheet = workbook.getSheetAt(0);
        Iterator<Row> rowIterator = firstSheet.rowIterator();
        if(header) rowIterator.next();
        int characteristics = Spliterator.DISTINCT | Spliterator.SORTED | Spliterator.ORDERED;
        Spliterator<Row> rowSpliterator = Spliterators.spliteratorUnknownSize(rowIterator, characteristics);
        return StreamSupport.stream(rowSpliterator, false);
    }
    
    private RowContent toRowContent(Row row) {
        if(row == null) return null;
        Cell sampleCell = row.getCell(0);
        Cell resolutionCell = row.getCell(1);
        Cell commentCell = row.getCell(2);
        if(sampleCell == null || resolutionCell == null) return null;
        RowContent content = new RowContent();
        content.sampleCode = sampleCell.getStringCellValue();
        content.resolution = resolutionCell.getStringCellValue();
        if(commentCell != null) {content.comment = commentCell.getStringCellValue(); }
        return content;      
    }
    
    private boolean checkSampleExists(String code) {
        return MongoDBDAO.checkObjectExistByCode(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, code);
    }
    
    private Sample get(String code) {
        return MongoDBDAO.findByCode(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, code);
    }
    
    private void printIgnored(Sample sample, RowContent content) {
        println("SampleCode " + sample.code + " ignoré pour la résolution '" + content.resolution + "', résolutions existantes : " + sample.resolutionCodes);
    }
    
    private void fillUpdateMap(ContextValidation ctx, Strategy strategy, Map<String, UpdateData> updateMap, RowContent content) {
        String sampleCode = content.sampleCode;
        if(updateMap.containsKey(sampleCode)) {
            Sample sample = updateMap.get(sampleCode).sample;
            boolean treated = strategy.apply(ctx, sample, content);
            if(!treated) { printIgnored(sample, content); return; }
        } else if(!checkSampleExists(sampleCode)) {
            ctx.addError("sampleCode " + sampleCode, " n'existe pas dans la base");
        } else {
            Sample sample = get(sampleCode);
            Set<String> oldResolutionCodes = sample.resolutionCodes == null ? Collections.emptySet() : new HashSet<>(sample.resolutionCodes);
            strategy.init(sample);
            boolean treated = strategy.apply(ctx, sample, content);
            if(!treated) { printIgnored(sample, content); return; }
            UpdateData updateData = new UpdateData(sample, oldResolutionCodes);
            updateMap.put(sample.code, updateData);
        }
    }

    private Collection<UpdateData> getSamplesToUpdateFrom(ContextValidation ctx, Strategy strategy, Stream<Row> rows){
        Map<String, UpdateData> updateMap = new HashMap<>();
        rows
        .map(this::toRowContent)
        .filter(Objects::nonNull)
        .forEach((RowContent content) -> this.fillUpdateMap(ctx, strategy, updateMap, content));
        return updateMap.values();
    }
    
    private void validateUpdate(ContextValidation ctx, UpdateData updateData, String jira) {
        updateData.traceUpdate(jira);
        updateData.validateSample(ctx);
        println("pour le sample " + updateData.getSampleCode() + " : Remplacement des résolutions " + updateData.getOldResolutions() + " par " + updateData.getNewResolutions());
    }
    
    private void logErrors(ContextValidation ctx) {
        println("ERRORS:");
        println(Json.prettyPrint(app.errorsAsJson(ctx.getErrors())));
        println("Fin du traitement : Aucune modification des samples dans la base");
    }
    
    private void saveUpdatedSamples(Collection<UpdateData> sampleUpdates) {
        println("Mise à jour de la base pour les samples");
        sampleUpdates.forEach((UpdateData updateData) -> MongoDBDAO.update(InstanceConstants.SAMPLE_COLL_NAME, updateData.sample));
        println("Fin du traitement : update de " + sampleUpdates.size() + " samples dans la base");
    }

    @Override
    public void execute(Args args, XSSFWorkbook workbook) throws Exception {
        args.validate();
        ContextValidation ctx = ContextValidation.createUpdateContext(USER);
        Stream<Row> rows = this.getRows(args.isThereHeaderLine(), workbook);
        Collection<UpdateData> sampleUpdates = getSamplesToUpdateFrom(ctx, args.getStrategy(), rows);
        sampleUpdates.forEach((UpdateData updateData) -> validateUpdate(ctx, updateData, args.jira));
        if(ctx.hasErrors()) { logErrors(ctx); return; }
        saveUpdatedSamples(sampleUpdates);
    }

}
