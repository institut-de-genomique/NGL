package services;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.StringUtils;
import org.mongojack.DBQuery;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import fr.cea.ig.MongoDBDAO;
import models.sra.submit.sra.instance.Experiment;
import models.sra.submit.sra.instance.RawData;
import models.sra.submit.sra.instance.ReadSpec;
import models.sra.submit.sra.instance.Run;
import models.sra.submit.sra.instance.Sample;
import models.sra.submit.sra.instance.Study;
import models.sra.submit.util.SraException;
import models.utils.InstanceConstants;

public class XmlToSraOff {

	private static final play.Logger.ALogger logger = play.Logger.of(XmlToSraOff.class);

	public static final String adminComment = "Creation dans le cadre d'une reprise d'historique"; 


	/**
	 * Extrait la liste des samples à partir d'un fichier xml
	 * @param  xmlFile                      fichier xml des enregistrements samples 
	 * @return                              {@literal List<Sample>}
	 * @throws ParserConfigurationException erreur de parsing
	 */
	public static List<Sample> xmlFileToSample(File xmlFile) throws ParserConfigurationException {
		List<Sample> listSamples = new ArrayList<>();
		/*
		 * Etape 1 : récupération d'une instance de la classe "DocumentBuilderFactory"
		 */
		final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

		/*
		 * Etape 2 : création d'un parseur
		 */
		final DocumentBuilder builder = factory.newDocumentBuilder();
		/*
		 * Etape 3 : création d'un Document
		 */

		Document document;
		try {
			document = builder.parse(xmlFile);
			listSamples = documentToSample(document);
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return listSamples;
	}	


	public static List<Sample> xmlToSample(String xmlSamples) throws ParserConfigurationException {
		List<Sample> listSamples = new ArrayList<>();

		/*
		 * Etape 1 : récupération d'une instance de la classe "DocumentBuilderFactory"
		 */
		final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		/*
		 * Etape 2 : création d'un parseur
		 */
		final DocumentBuilder builder = factory.newDocumentBuilder();
		/*
		 * Etape 3 : création d'un Document
		 */
		Document document;
		try {
			InputStream targetStream = new ByteArrayInputStream(xmlSamples.getBytes());
			//				document = builder.parse(xmlSamples);
			document = builder.parse(targetStream);
			listSamples = documentToSample(document);
		} catch (SAXException |IOException e) {
			throw new RuntimeException(e);
		}
		return listSamples;
	}	



	public static List<Sample> documentToSample(Document document) {
		List<Sample> listSamples = new ArrayList<>();			
		//Affiche du prologue
		//			logger.debug("*************PROLOGUE************");
		//			logger.debug("version : " + document.getXmlVersion());
		//			logger.debug("encodage : " + document.getXmlEncoding());      
		//			logger.debug("standalone : " + document.getXmlStandalone());
		//logger.debug("*************PROLOGUE************");
		//logger.debug("version    : {}", document.getXmlVersion());
		//logger.debug("encodage   : {}", document.getXmlEncoding());      
		//logger.debug("standalone : {}", document.getXmlStandalone());
		/*
		 * Etape 4 : récupération de l'Element racine
		 */
		final Element racine = document.getDocumentElement();
		//Affichage de l'élément racine
		//logger.debug("\n*************RACINE************");
		//logger.debug(racine.getNodeName());
		/*
		 * Etape 5 : récupération des samples
		 */
		final NodeList racineNoeuds = racine.getChildNodes();
		final int nbRacineNoeuds = racineNoeuds.getLength();

		for (int i = 0; i<nbRacineNoeuds; i++) {
			if(racineNoeuds.item(i).getNodeType() == Node.ELEMENT_NODE) {
				final Element eltSample = (Element) racineNoeuds.item(i);
				//Affichage d'un sample
				//logger.debug("\n*************SAMPLE************");
				//logger.debug("alias : " + eltSample.getAttribute("alias"));

				String alias = eltSample.getAttribute("alias");
				Sample sample = new Sample();
				//logger.debug("sample alias : " + alias);
				sample.code = alias;

				String accession = eltSample.getAttribute("accession");
				if (StringUtils.isNotBlank(accession)){
					sample.accession = accession;
					//logger.debug("sample alias : " + alias);
				}
//				Le champs EXT_ID apparait dans le rapport de soumission de l'EBI (fichier des AC). 
//				Il apparait dans l'interface de soumission de l'EBI. En revanche, ce champs n'apparait pas 
//				dans le xml qui decrit le sample.
//		    	final Element eltExtId = (Element) eltSample.getElementsByTagName("EXT_ID").item(0);
//		    	if (eltExtId != null) {
//		    		String external_id = eltExtId.getAttribute("accession");
//		    		if (StringUtils.isNotBlank(external_id)) {
//		    			sample.externalId = external_id;
//		    		}
//		    	}
				
				if (eltSample.getElementsByTagName("DESCRIPTION").item(0) != null) {
					String description =  eltSample.getElementsByTagName("DESCRIPTION").item(0).getTextContent();
					if (StringUtils.isNotBlank(description)) {
						sample.description = description;
						//logger.debug("description : " + description);
					}
				}
				if (eltSample.getElementsByTagName("TITLE").item(0) != null) {
					String title =  eltSample.getElementsByTagName("TITLE").item(0).getTextContent();
					if (StringUtils.isNotBlank(title)) {
						sample.title = title;
						//logger.debug("title : " + title);
					}
				}
				final Element eltSampleName = (Element) eltSample.getElementsByTagName("SAMPLE_NAME").item(0);
				if (eltSampleName.getElementsByTagName("COMMON_NAME").item(0)!= null) {
					String commonName = eltSampleName.getElementsByTagName("COMMON_NAME").item(0).getTextContent();
					if (StringUtils.isNotBlank(commonName)) {
						sample.commonName = commonName;
						//logger.debug("commonName : " + commonName);
					}
				}
				if (eltSampleName.getElementsByTagName("SCIENTIFIC_NAME").item(0)!= null) {
					String scientificName = eltSampleName.getElementsByTagName("SCIENTIFIC_NAME").item(0).getTextContent();
					if (StringUtils.isNotBlank(scientificName)) {
						sample.scientificName = scientificName;
						//logger.debug("scientificName : " + scientificName);
					}
				}
				if (eltSampleName.getElementsByTagName("ANONYMIZED_NAME").item(0)!= null) {
					String anonymizedName = eltSampleName.getElementsByTagName("ANONYMIZED_NAME").item(0).getTextContent();
					if (StringUtils.isNotBlank(anonymizedName)) {
						sample.anonymizedName = anonymizedName;
						//logger.debug("anonymizedName : " + anonymizedName);
					}
				}
				if (eltSampleName.getElementsByTagName("TAXON_ID").item(0)!= null) {
					if (StringUtils.isNotBlank(eltSampleName.getElementsByTagName("TAXON_ID").item(0).getTextContent())){
						int taxon_id = new Integer(eltSampleName.getElementsByTagName("TAXON_ID").item(0).getTextContent()).intValue();
						sample.taxonId = new Integer(taxon_id);
						//logger.debug("taxon_id  : " + taxon_id);
					}
				}
				
				
				final Element eltSampleAttributes = (Element) eltSample.getElementsByTagName("SAMPLE_ATTRIBUTES").item(0);
				if (eltSampleAttributes != null) {
					final NodeList racine_attribute = eltSampleAttributes.getElementsByTagName("SAMPLE_ATTRIBUTE");
					final int nb_attribute = racine_attribute.getLength();
					//logger.debug("Nombre d'attributs = " + nb_attribute);
					String sampleAttributes = "";
					for (int j = 0; j<nb_attribute; j++) {
						String 	attribute = "";
						if(racine_attribute.item(j).getNodeType() == Node.ELEMENT_NODE) {
							final Element eltAttribute = (Element) racine_attribute.item(j);
							if (eltAttribute.getElementsByTagName("TAG").item(0) != null) {
								String tag = eltAttribute.getElementsByTagName("TAG").item(0).getTextContent();
								//logger.debug("TAG = " + tag);
								if(tag.equals("ENA-CHECKLIST")) {
									// TAG ajouté par l'ebi ne correspond pas à un tag utilisateur.
									break;
								}
								attribute += "\n    <TAG>" + tag + "</TAG>";
							}			
							if (eltAttribute.getElementsByTagName("VALUE").item(0) != null) {
								String value = eltAttribute.getElementsByTagName("VALUE").item(0).getTextContent();
								//logger.debug("VALUE = " + value);
								attribute += "\n    <VALUE>" + value + "</VALUE>";
							}
							if (eltAttribute.getElementsByTagName("UNITS").item(0) != null) {
								String units = eltAttribute.getElementsByTagName("UNITS").item(0).getTextContent();
								//logger.debug("UNITS = " + units);
								attribute += "\n    <UNITS>" + units + "</UNITS>";
							}		
						}
						if(StringUtils.isNotBlank(attribute)) {
							attribute = "\n  <SAMPLE_ATTRIBUTE>" + attribute + "\n  </SAMPLE_ATTRIBUTE>"; 
							sampleAttributes += attribute;
						}
					}
					if (StringUtils.isNotBlank(sampleAttributes)) {
//						sampleAttributes = "\n<SAMPLE_ATTRIBUTES>" + sampleAttributes + "\n</SAMPLE_ATTRIBUTES>\n"; 
						sample.attributes = sampleAttributes;
					}
				}
				if (!listSamples.contains(sample)){
					listSamples.add(sample);
				} 
			}
		} // end for  
		return listSamples;
	}	



	public static List<Study> documentToStudy(Document document) {
		List<Study> listStudies = new ArrayList<>();

		//Affiche du prologue
		//		logger.debug("*************PROLOGUE************");
		//		logger.debug("version : " + document.getXmlVersion());
		//		logger.debug("encodage : " + document.getXmlEncoding());      
		//		logger.debug("standalone : " + document.getXmlStandalone());
		/*
		 * Etape 4 : récupération de l'Element racine
		 */
		final Element racine = document.getDocumentElement();
		//Affichage de l'élément racine
		//		logger.debug("\n*************RACINE************");
		//		logger.debug(racine.getNodeName());
		//logger.debug("\n*************RACINE************");
		//logger.debug(racine.getNodeName());
		/*
		 * Etape 5 : récupération des samples
		 */
		final NodeList racineNoeuds = racine.getChildNodes();
		final int nbRacineNoeuds = racineNoeuds.getLength();

		for (int i = 0; i<nbRacineNoeuds; i++) {
			if(racineNoeuds.item(i).getNodeType() == Node.ELEMENT_NODE) {
				final Element eltStudy = (Element) racineNoeuds.item(i);
				//Affichage d'un study
				//				logger.debug("\n*************STUDY************");
				//				logger.debug("alias : " + eltStudy.getAttribute("alias"));
				//logger.debug("\n*************STUDY************");
				//logger.debug("alias : " + eltStudy.getAttribute("alias"));

				String alias = eltStudy.getAttribute("alias");
				Study study = new Study();
				//				logger.debug("study alias : " + alias);
				//logger.debug("study alias : " + alias);
				study.code = alias;

				String accession = eltStudy.getAttribute("accession");
				if (StringUtils.isNotBlank(accession)){
					study.accession = accession;
					//					logger.debug("study accession : " + accession);
					//logger.debug("study accession : " + accession);	
				}
//				Le champs EXT_ID apparait dans le rapport de soumission de l'EBI (fichier des AC). 
//				Il apparait dans l'interface de soumission de l'EBI. En revanche, ce champs n'apparait pas 
//				dans le xml qui decrit le study.				
//		    	final Element eltExtId = (Element) eltStudy.getElementsByTagName("EXT_ID").item(0);
//		    	if (eltExtId != null) {
//		    		String external_id = eltExtId.getAttribute("accession");
//		    		if (StringUtils.isNotBlank(external_id)) {
//		    			study.externalId = external_id;
//		    		}
//		    	}
				String center_name = eltStudy.getAttribute("center_name");
				if (StringUtils.isNotBlank(center_name)){
					study.centerName = center_name;
					//					logger.debug("study centerName : " + center_name);
					//logger.debug("study centerName : " + center_name);	
				}

				final Element descriptor = (Element) eltStudy.getElementsByTagName("DESCRIPTOR").item(0);

				if (descriptor.getElementsByTagName("STUDY_TITLE").item(0)!= null) {
					String title = descriptor.getElementsByTagName("STUDY_TITLE").item(0).getTextContent();
					if (StringUtils.isNotBlank(title)) {
						study.title = title;
						//						logger.debug("title : " + title);
						//logger.debug("title : " + title);
					}
				}
				if (descriptor.getElementsByTagName("STUDY_ABSTRACT").item(0)!= null) {
					String studyAbstract = descriptor.getElementsByTagName("STUDY_ABSTRACT").item(0).getTextContent();
					if (StringUtils.isNotBlank(studyAbstract)) {
						study.studyAbstract = studyAbstract;
						//						logger.debug("studyAbstract : " + studyAbstract);
						//logger.debug("studyAbstract : " + studyAbstract);
					}
				}
				if (descriptor.getElementsByTagName("STUDY_DESCRIPTION").item(0)!= null) {
					String description = descriptor.getElementsByTagName("STUDY_DESCRIPTION").item(0).getTextContent();
					if (StringUtils.isNotBlank(description)) {
						study.description = description;
						//						logger.debug("description : " + description);
						//logger.debug("description : " + description);
					}
				}	
				if (descriptor.getElementsByTagName("CENTER_PROJECT_NAME").item(0)!= null) {
					String centerProjectName = descriptor.getElementsByTagName("CENTER_PROJECT_NAME").item(0).getTextContent();
					if (StringUtils.isNotBlank(centerProjectName)) {
						study.centerProjectName = centerProjectName;
						//						logger.debug("centerProjectName : " + centerProjectName);
						//logger.debug("centerProjectName : " + centerProjectName);
					}
				}	
				if (descriptor.getElementsByTagName("STUDY_TYPE").item(0)!= null) {
					final Element eltStudyType = (Element) descriptor.getElementsByTagName("STUDY_TYPE").item(0);
					String existingStudyType = eltStudyType.getAttribute("existing_study_type");
					if (StringUtils.isNotBlank(existingStudyType)) {
						study.existingStudyType = existingStudyType;
					}
				}	
				if (!listStudies.contains(study)){
					listStudies.add(study);
				}
			} 
		} // end for  
		return listStudies;
	}	


	public static List<Study> xmlFileToStudy(File xmlFile) throws ParserConfigurationException {
		List<Study> listStudies = new ArrayList<>();

		/*
		 * Etape 1 : récupération d'une instance de la classe "DocumentBuilderFactory"
		 */
		final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

		/*
		 * Etape 2 : création d'un parseur
		 */
		final DocumentBuilder builder = factory.newDocumentBuilder();
		/*
		 * Etape 3 : création d'un Document
		 */

		Document document;
		try {
			document = builder.parse(xmlFile);
			listStudies = documentToStudy(document);

		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return listStudies;
	}	


	public static List<Study> xmlToStudy(String xmlStudies) throws ParserConfigurationException {
		List<Study> listStudies = new ArrayList<>();

		/*
		 * Etape 1 : récupération d'une instance de la classe "DocumentBuilderFactory"
		 */
		final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

		/*
		 * Etape 2 : création d'un parseur
		 */
		final DocumentBuilder builder = factory.newDocumentBuilder();
		/*
		 * Etape 3 : création d'un Document
		 */
		Document document;
		try {
			InputStream targetStream = new ByteArrayInputStream(xmlStudies.getBytes());
			//				document = builder.parse(xmlStudies);
			document = builder.parse(targetStream);
			listStudies = documentToStudy(document);
		} catch (SAXException |IOException e) {
			throw new RuntimeException(e);
		}
		return listStudies;
	}	


	public static List<Experiment> xmlFileToExperiment(File xmlFile) throws ParserConfigurationException {
		List<Experiment> listExperiments = new ArrayList<>();

		/*
		 * Etape 1 : récupération d'une instance de la classe "DocumentBuilderFactory"
		 */
		final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

		/*
		 * Etape 2 : création d'un parseur
		 */
		final DocumentBuilder builder = factory.newDocumentBuilder();
		/*
		 * Etape 3 : création d'un Document
		 */

		Document document;
		try {
			document = builder.parse(xmlFile);
			listExperiments = documentToExperiment(document);

		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return listExperiments;
	}	

	public static List<Experiment> xmlToExperiment(String xmlExperiments) throws ParserConfigurationException {
		List<Experiment> listExperiments = new ArrayList<>();

		/*
		 * Etape 1 : récupération d'une instance de la classe "DocumentBuilderFactory"
		 */
		final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		/*
		 * Etape 2 : création d'un parseur
		 */
		final DocumentBuilder builder = factory.newDocumentBuilder();
		/*
		 * Etape 3 : création d'un Document
		 */

		Document document;

		try {
			InputStream targetStream = new ByteArrayInputStream(xmlExperiments.getBytes());
			//			document = builder.parse(xmlExperiments);
			document = builder.parse(targetStream);
			listExperiments = documentToExperiment(document);
		} catch (SAXException |IOException e) {
			throw new RuntimeException(e);
		}
		return listExperiments;
	}	




	public static List<Experiment> documentToExperiment(Document document) {
		List<Experiment> listExperiments = new ArrayList<>();

		//Affiche du prologue
		//			logger.debug("*************PROLOGUE************");
		//			logger.debug("version : " + document.getXmlVersion());
		//			logger.debug("encodage : " + document.getXmlEncoding());      
		//			logger.debug("standalone : " + document.getXmlStandalone());
		/*
		 * Etape 4 : récupération de l'Element racine
		 */
		final Element racine = document.getDocumentElement();
		//Affichage de l'élément racine
		//logger.debug("\n*************RACINE************");
		//logger.debug(racine.getNodeName());
		/*
		 * Etape 5 : récupération des experiments
		 */
		final NodeList racineNoeuds = racine.getChildNodes();
		final int nbRacineNoeuds = racineNoeuds.getLength();

		for (int i = 0; i<nbRacineNoeuds; i++) {
			if(racineNoeuds.item(i).getNodeType() == Node.ELEMENT_NODE) {
				final Element eltExperiment = (Element) racineNoeuds.item(i);
				//Affichage d'un experiment
				//logger.debug("\n*************EXPERIMENT************");
				//logger.debug("alias : " + eltExperiment.getAttribute("alias"));

				String alias = eltExperiment.getAttribute("alias");
				Experiment experiment = new Experiment();
				//logger.debug("experiment alias : " + alias);
				experiment.code = alias;

				experiment.readSetCode = experiment.code.replace("exp_", "");
				//logger.debug("experiment.readSetCode = " + experiment.readSetCode);
				String accession = eltExperiment.getAttribute("accession");
				if (StringUtils.isNotBlank(accession)){
					experiment.accession = accession;
					//logger.debug("experiment accession : " + accession);
				}

				if (eltExperiment.getElementsByTagName("TITLE").item(0) != null) {
					String title =  eltExperiment.getElementsByTagName("TITLE").item(0).getTextContent();
					if (StringUtils.isNotBlank(title)) {
						experiment.title = title;
						//logger.debug("title : " + title);
					}
				}
				final Element eltExpStudyRef = (Element) eltExperiment.getElementsByTagName("STUDY_REF").item(0);
				String study_refname = eltExpStudyRef.getAttribute("refname");
				if (StringUtils.isNotBlank(study_refname)) {
					experiment.studyCode = study_refname;
					//logger.debug("study_refname : " + study_refname);
				}
				String study_accession = eltExpStudyRef.getAttribute("accession");
				// A completer : normalement uniquement reference du studyCode au niveau de experiment ?????
				// todo
				if (StringUtils.isNotBlank(study_accession)) {
					experiment.studyAccession = study_accession;
					//logger.debug("study_accession : " + study_accession);
				}
				final Element eltExpPlatform = (Element) eltExperiment.getElementsByTagName("PLATFORM").item(0);
				NodeList nodesTypePlatform = eltExpPlatform.getChildNodes();
				String typePlatform = "";
				for (int l=0; l<nodesTypePlatform.getLength(); l++) {
					Node child = nodesTypePlatform.item(l);
					if (child instanceof Element){
						//logger.debug("hello à l'indide " + l + " tag=" + nodesTypePlatform.item(l).getNodeName());
						typePlatform = nodesTypePlatform.item(l).getNodeName().toLowerCase();
					}
				}

				if ("ILLUMINA".equalsIgnoreCase(typePlatform) || "LS454".equalsIgnoreCase(typePlatform) || "OXFORD_NANOPORE".equalsIgnoreCase(typePlatform)) {
					experiment.typePlatform = typePlatform;
					final Element eltExpPlatformType = (Element) eltExpPlatform.getElementsByTagName(typePlatform).item(0);
					if (eltExpPlatformType.getElementsByTagName("INSTRUMENT_MODEL").item(0) != null) {	
						String instrument_model = eltExpPlatformType.getElementsByTagName("INSTRUMENT_MODEL").item(0).getTextContent();
						if (StringUtils.isNotBlank(instrument_model)){
							//logger.debug("instrument_model : " + instrument_model);
							experiment.instrumentModel = instrument_model;
						}
					}
				} else {
					//						logger.debug("Type de plateforme inconnue" + typePlatform );
					logger.debug("Type de plateforme inconnue" + typePlatform );
				}
				final Element eltExpDesign = (Element) eltExperiment.getElementsByTagName("DESIGN").item(0);
				final Element eltExpSampleDescriptor = (Element) eltExpDesign.getElementsByTagName("SAMPLE_DESCRIPTOR").item(0);
				String sample_refname = eltExpSampleDescriptor.getAttribute("refname");
				if (StringUtils.isNotBlank(sample_refname)) {
					experiment.sampleCode = sample_refname;
					//logger.debug("sample_refname : " + sample_refname);
				}
				String sample_accession = eltExpSampleDescriptor.getAttribute("accession");
				// A completer : normalement uniquement reference du sampleCode au niveau de experiment ?????
				// todo
				if (StringUtils.isNotBlank(sample_accession)) {
					experiment.sampleAccession = sample_accession;
					//logger.debug("sample_accession : " + sample_accession);
				}

				final Element eltExpLibraryDescriptor = (Element) eltExpDesign.getElementsByTagName("LIBRARY_DESCRIPTOR").item(0);

				if (eltExpLibraryDescriptor.getElementsByTagName("LIBRARY_NAME").item(0) != null) {
					String libraryName =  eltExpLibraryDescriptor.getElementsByTagName("LIBRARY_NAME").item(0).getTextContent();
					if (StringUtils.isNotBlank(libraryName)) {
						experiment.libraryName = libraryName;
						//logger.debug("libraryName : " + libraryName);
					}
				}

				if (eltExpLibraryDescriptor.getElementsByTagName("LIBRARY_STRATEGY").item(0) != null) {
					String libraryStrategy =  eltExpLibraryDescriptor.getElementsByTagName("LIBRARY_STRATEGY").item(0).getTextContent();
					if (StringUtils.isNotBlank(libraryStrategy)) {
						experiment.libraryStrategy = libraryStrategy;
						//logger.debug("libraryStrategy : " + libraryStrategy);
					}
				}

				if (eltExpLibraryDescriptor.getElementsByTagName("LIBRARY_SOURCE").item(0) != null) {
					String librarySource =  eltExpLibraryDescriptor.getElementsByTagName("LIBRARY_SOURCE").item(0).getTextContent();
					if (StringUtils.isNotBlank(librarySource)) {
						experiment.librarySource = librarySource;
						//logger.debug("librarySource : " + librarySource);
					}
				}


				if (eltExpLibraryDescriptor.getElementsByTagName("LIBRARY_SELECTION").item(0) != null) {
					String librarySelection =  eltExpLibraryDescriptor.getElementsByTagName("LIBRARY_SELECTION").item(0).getTextContent();
					if (StringUtils.isNotBlank(librarySelection)) {
						experiment.librarySelection = librarySelection;
						//logger.debug("librarySelection : " + librarySelection);
					}
				}

				if (eltExpLibraryDescriptor.getElementsByTagName("LIBRARY_CONSTRUCTION_PROTOCOL").item(0) != null) {
					String libraryConstructionProtocol =  eltExpLibraryDescriptor.getElementsByTagName("LIBRARY_CONSTRUCTION_PROTOCOL").item(0).getTextContent();
					if (StringUtils.isNotBlank(libraryConstructionProtocol)) {
						experiment.libraryConstructionProtocol = libraryConstructionProtocol;
						//logger.debug("libraryConstructionProtocol : " + libraryConstructionProtocol);
					}
				}	

				final Element eltExpLibraryLayout= (Element) eltExpLibraryDescriptor.getElementsByTagName("LIBRARY_LAYOUT").item(0);
				if (eltExpLibraryLayout.getElementsByTagName("SINGLE").item(0) != null) {
					//logger.debug("libraryLayout : single");
					experiment.libraryLayout = "single";
				} 
				if (eltExpLibraryLayout.getElementsByTagName("PAIRED").item(0) != null) {
					//logger.debug("libraryLayout : paired");
					experiment.libraryLayout = "paired";
					final Element eltExpLibraryLayoutPaired = (Element) eltExpLibraryLayout.getElementsByTagName("PAIRED").item(0);

					String nominal_length = eltExpLibraryLayoutPaired.getAttribute("NOMINAL_LENGTH");
					if (StringUtils.isNotBlank(nominal_length)){
						//logger.debug("nominal_length : " + nominal_length);
						experiment.libraryLayoutNominalLength = new Integer(nominal_length);
					}	
				} 

				final Element eltSpotDescriptor = (Element) eltExpDesign.getElementsByTagName("SPOT_DESCRIPTOR").item(0);
				if (eltSpotDescriptor != null) {
					final Element eltSpotDecodeSpec = (Element) eltSpotDescriptor.getElementsByTagName("SPOT_DECODE_SPEC").item(0);
					if (eltSpotDecodeSpec != null) {
						if (eltSpotDecodeSpec.getElementsByTagName("SPOT_LENGTH").item(0) != null) {
							String spotLength = eltSpotDecodeSpec.getElementsByTagName("SPOT_LENGTH").item(0).getTextContent();
							if (StringUtils.isNotBlank(spotLength)) {
								//logger.debug("spotLength : " + spotLength);
								experiment.spotLength = new Long(spotLength);
							}
						}

						final NodeList racine_read_spec = eltSpotDecodeSpec.getElementsByTagName("READ_SPEC");
						final int nb_read_spec = racine_read_spec.getLength();
						experiment.readSpecs = new ArrayList<>();
						String readLabel_1 = "";
						for (int j = 0; j<nb_read_spec; j++) {
							ReadSpec readSpec = new ReadSpec();
							if(racine_read_spec.item(j).getNodeType() == Node.ELEMENT_NODE) {
								final Element eltReadSpec = (Element) racine_read_spec.item(j);

								if (eltReadSpec.getElementsByTagName("READ_INDEX").item(0) != null) {
									String readIndex = eltReadSpec.getElementsByTagName("READ_INDEX").item(0).getTextContent();
									if (StringUtils.isNotBlank(readIndex)) {
										//logger.debug("readIndex : " + readIndex);
										readSpec.readIndex = new Integer(readIndex);									
									}
								}
								if (eltReadSpec.getElementsByTagName("READ_LABEL").item(0) != null) {
									String readLabel = eltReadSpec.getElementsByTagName("READ_LABEL").item(0).getTextContent();
									if (StringUtils.isNotBlank(readLabel)) {
										//logger.debug("readLabel : " + readLabel);
										readSpec.readLabel = readLabel;									

										if (j==1){
											readLabel_1 = readLabel;
										}
										if (j==2) {
											if (readLabel_1.equals("F") && readLabel.equals("R")){
												experiment.libraryLayoutOrientation="forward-reverse";
											}
											if (readLabel_1.equals("R") && readLabel.equals("F")){
												experiment.libraryLayoutOrientation="reverse-forward";
											}
										}

									}
								}
								if (eltReadSpec.getElementsByTagName("READ_CLASS").item(0) != null) {
									String readClass = eltReadSpec.getElementsByTagName("READ_CLASS").item(0).getTextContent();
									if (StringUtils.isNotBlank(readClass)) {
										//logger.debug("readClass : " + readClass);
										readSpec.readClass = readClass;									
									}
								}
								if (eltReadSpec.getElementsByTagName("READ_TYPE").item(0) != null) {
									String readType = eltReadSpec.getElementsByTagName("READ_TYPE").item(0).getTextContent();
									if (StringUtils.isNotBlank(readType)) {
										//logger.debug("readType : " + readType);
										readSpec.readType = readType;									
									}
								}
								if (eltReadSpec.getElementsByTagName("BASE_COORD").item(0) != null) {
									String baseCoord = eltReadSpec.getElementsByTagName("BASE_COORD").item(0).getTextContent();
									if (StringUtils.isNotBlank(baseCoord)) {
										//logger.debug("baseCoord : " + baseCoord);
										readSpec.baseCoord = new Integer(baseCoord);									
									}
									if (j==1) {
										experiment.lastBaseCoord = readSpec.baseCoord;
									}
								}
								// Ajout basecall dans le cas plateforme LS454
								if (eltReadSpec.getElementsByTagName("EXPECTED_BASECALL_TABLE").item(0) != null) {
									//logger.debug("OKKKKKK, EXPECTED_BASECALL_TABLE");
									final Element eltExpectedBasecallTable = (Element) eltReadSpec.getElementsByTagName("EXPECTED_BASECALL_TABLE").item(0);
									final NodeList racine_basecall = eltExpectedBasecallTable.getElementsByTagName("BASECALL");

									if (racine_basecall != null) {
										//logger.debug("OKK BASECALL");
										final int nb_baseCall = racine_basecall.getLength();
										//logger.debug("nb_baseCall = " + nb_baseCall);
										for (int k = 0; k<nb_baseCall; k++) {
											Node nodeBaseCall = racine_basecall.item(k);
											//logger.debug("k=" + k);
											if(nodeBaseCall instanceof Element) {
												final Element eltBasecall = (Element) nodeBaseCall;
												String basecall = eltBasecall.getTextContent();// racine_basecall.getElementsByTagName("BASECALL").item(k).getTextContent();
												//logger.debug("BASECALL ="+ basecall);
												readSpec.expectedBaseCallTable.add(basecall);
												//logger.debug("ok pour ajout BASECALL="+ basecall);
											}
										}
									}
								} //end if expectedBasecall
								experiment.readSpecs.add(readSpec);
							} // end if readspec
						} // end for readspec
					} // end if (eltSpotDecodeSpec == null) {

				}
				// Dans le cas des Illumina Single :
				if(experiment.typePlatform.equalsIgnoreCase("ILLUMINA") && experiment.libraryLayout.equalsIgnoreCase("SINGLE")){
					if (experiment.readSpecs.size() == 1){	
						if("Forward".equalsIgnoreCase(experiment.readSpecs.get(0).readType)){
							experiment.libraryLayoutOrientation = "Forward";
							experiment.lastBaseCoord = experiment.readSpecs.get(0).baseCoord;
						}
					}
				}
				// Dans le cas des Illumina Paired
				if(experiment.typePlatform.equalsIgnoreCase("ILLUMINA") && experiment.libraryLayout.equalsIgnoreCase("PAIRED")){
					// Determiner experiment.lastBaseCoord à partir des readSpec.baseCoord :
					if (experiment.readSpecs.size() == 2){
						if(experiment.readSpecs.get(1).baseCoord >= experiment.readSpecs.get(0).baseCoord) {
							experiment.lastBaseCoord = experiment.readSpecs.get(1).baseCoord;
						} else {
							experiment.lastBaseCoord = experiment.readSpecs.get(0).baseCoord;
						}
						//logger.debug("EXPERIMENT.lastBaseCoordonnée = " + experiment.lastBaseCoord);
					}

					// Determiner experiment.libraryLayoutOrientation à partir des readSpec.READ_LABEL
					if (experiment.readSpecs.size() == 2){
						if("F".equalsIgnoreCase(experiment.readSpecs.get(0).readLabel) &&  "R".equalsIgnoreCase(experiment.readSpecs.get(1).readLabel)) {
							experiment.libraryLayoutOrientation="forward-reverse";
						} 
						if("R".equalsIgnoreCase(experiment.readSpecs.get(0).readLabel) &&  "F".equalsIgnoreCase(experiment.readSpecs.get(1).readLabel)) {
							experiment.libraryLayoutOrientation="reverse-forward";
						} 
						if("F".equalsIgnoreCase(experiment.readSpecs.get(0).readLabel) &&  "F".equalsIgnoreCase(experiment.readSpecs.get(1).readLabel)) {
							experiment.libraryLayoutOrientation="forward-forward";
						} 
						if("R".equalsIgnoreCase(experiment.readSpecs.get(0).readLabel) &&  "R".equalsIgnoreCase(experiment.readSpecs.get(1).readLabel)) {
							experiment.libraryLayoutOrientation="reverse-reverse";
						} 
						//logger.debug("experiment.libraryLayoutOrientation = " + experiment.libraryLayoutOrientation);
					}
				} 

				// Dans le cas des LS454 Single :
				if(experiment.typePlatform.equalsIgnoreCase("LS454") && experiment.libraryLayout.equalsIgnoreCase("SINGLE")){
					if (experiment.readSpecs.size() == 2){	
						if("Forward".equalsIgnoreCase(experiment.readSpecs.get(1).readType)){
							experiment.libraryLayoutOrientation = "Forward";
						}
					}
				}
				// Dans le cas des LS454 Paired :
				if(experiment.typePlatform.equalsIgnoreCase("LS454") && experiment.libraryLayout.equalsIgnoreCase("PAIRED")){
					/*logger.debug("experiment.readSpecs.get(0).readType="+experiment.readSpecs.get(0).readType);
						logger.debug("experiment.readSpecs.get(1).readType="+experiment.readSpecs.get(1).readType);
						logger.debug("experiment.readSpecs.get(2).readType="+experiment.readSpecs.get(2).readType);
						logger.debug("experiment.readSpecs.get(3).readType="+experiment.readSpecs.get(3).readType);
					 */
					// Determiner experiment.libraryLayoutOrientation à partir des readSpec.READ_TYPE
					if (experiment.readSpecs.size() == 4){
						if("Forward".equals(experiment.readSpecs.get(1).readType) &&  "Reverse".equalsIgnoreCase(experiment.readSpecs.get(3).readType)){
							experiment.libraryLayoutOrientation="forward-reverse";
						} 
						if("Reverse".equals(experiment.readSpecs.get(1).readType) &&  "Forward".equalsIgnoreCase(experiment.readSpecs.get(3).readType)){
							experiment.libraryLayoutOrientation="reverse-forward";
						} 
						if("Forward".equals(experiment.readSpecs.get(1).readType) &&  "Forward".equalsIgnoreCase(experiment.readSpecs.get(3).readType)){
							experiment.libraryLayoutOrientation="forward-forward";
						} 
						if("Reverse".equals(experiment.readSpecs.get(1).readType) &&  "Reverse".equalsIgnoreCase(experiment.readSpecs.get(3).readType)){
							experiment.libraryLayoutOrientation="reverse-reverse";
						}
						//logger.debug("experiment.libraryLayoutOrientation = " + experiment.libraryLayoutOrientation);
					}
				}
				if (!listExperiments.contains(experiment)){
					//logger.debug("Ajout de " + experiment.code + " dans listExperiments");
					listExperiments.add(experiment);
				} // end if eltSpotDescriptor

			} // end if nodeExperiment

		} // end for  experiment
		//logger.debug("sortie de forExperiment dans xml2experiment");
		return listExperiments;
	}	

	
	public static List<Run> xmlFileToRun(File xmlFile) throws ParserConfigurationException, ParseException {
		List<Run> listRuns = new ArrayList<>();

		/*
		 * Etape 1 : récupération d'une instance de la classe "DocumentBuilderFactory"
		 */
		final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

		/*
		 * Etape 2 : création d'un parseur
		 */
		final DocumentBuilder builder = factory.newDocumentBuilder();
		/*
		 * Etape 3 : création d'un Document
		 */

		Document document;
		try {
			document = builder.parse(xmlFile);
			listRuns = documentToRun(document);

		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return listRuns;
	}		
	public static List<Run> xmlToRun(String xmlRuns) throws ParserConfigurationException, ParseException {
		List<Run> listRuns = new ArrayList<>();
		/*
		 * Etape 1 : récupération d'une instance de la classe "DocumentBuilderFactory"
		 */
		final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		/*
		 * Etape 2 : création d'un parseur
		 */
		final DocumentBuilder builder = factory.newDocumentBuilder();
		/*
		 * Etape 3 : création d'un Document
		 */
		Document document;
		try {
			InputStream targetStream = new ByteArrayInputStream(xmlRuns.getBytes());
			//				document = builder.parse(xmlRuns);
			document = builder.parse(targetStream);
			listRuns = documentToRun(document);
		} catch (SAXException |IOException e) {
			throw new RuntimeException(e);
		}
		return listRuns;
	}


	public static List<Run> documentToRun(Document document) throws ParseException {
		List<Run> listRuns = new ArrayList<>();

		//Affiche du prologue
//		logger.debug("*************PROLOGUE************");
//		logger.debug("version : " + document.getXmlVersion());
//		logger.debug("encodage : " + document.getXmlEncoding());      
//		logger.debug("standalone : " + document.getXmlStandalone());
		/*
		 * Etape 4 : récupération de l'Element racine
		 */
		final Element racine = document.getDocumentElement();
		//Affichage de l'élément racine
		//logger.debug("\n*************RACINE************");
		//logger.debug(racine.getNodeName());
		/*
		 * Etape 5 : récupération des runs
		 */
		final NodeList racineNoeuds = racine.getChildNodes();
		final int nbRacineNoeuds = racineNoeuds.getLength();

		for (int i = 0; i<nbRacineNoeuds; i++) {
			if(racineNoeuds.item(i).getNodeType() == Node.ELEMENT_NODE) {
				final Element eltRun = (Element) racineNoeuds.item(i);
				//Affichage d'un run
				//logger.debug("\n*************RUN************");
				//logger.debug("alias : " + eltRun.getAttribute("alias"));

				String alias = eltRun.getAttribute("alias");
				Run run = new Run();
				//logger.debug("run alias : " + alias);
				run.code = alias;
				String accession = eltRun.getAttribute("accession");
				if (StringUtils.isNotBlank(accession)){
					run.accession = accession;
					//logger.debug("run alias : " + alias);
				}

				String dateInString = eltRun.getAttribute("run_date");
				//logger.debug("run date : " + dateInString);

				if (StringUtils.isNotBlank(dateInString)){
					dateInString = dateInString.substring(0, 10);
					SimpleDateFormat formatter = new SimpleDateFormat("yyyy-mm-dd");
					Date date = formatter.parse(dateInString);				
					//logger.debug("run dateInString : " + dateInString);
					//logger.debug("run date : " + date);
					run.runDate = date;

				}

				String runCenter = eltRun.getAttribute("run_center");
				if (StringUtils.isNotBlank(runCenter)){
					run.runCenter = runCenter;
					//logger.debug("run center : " + runCenter);
				}

				final Element eltExpRef = (Element) eltRun.getElementsByTagName("EXPERIMENT_REF").item(0);
				String exp_accession = eltExpRef.getAttribute("accession");

				String exp_refname = eltExpRef.getAttribute("refname");
				if (StringUtils.isNotBlank(exp_accession)) {
					run.expAccession = exp_accession;
				}
				if (StringUtils.isNotBlank(exp_refname)) {
					run.expCode = exp_refname;
					//logger.debug("experiment_refname : " + exp_refname);
				} else {
					if (StringUtils.isNotBlank(exp_accession)) {
						Experiment experiment = MongoDBDAO.findOne(InstanceConstants.SRA_EXPERIMENT_COLL_NAME,
								Experiment.class, DBQuery.and(DBQuery.is("accession", exp_accession)));
						if (experiment != null) {
							run.expCode = experiment.code; 
						} else {
							run.expCode = run.code;
							run.expCode = run.expCode.replaceFirst("run_", "exp_");
						}
					}
				}


				final Element eltDataBlock = (Element) eltRun.getElementsByTagName("DATA_BLOCK").item(0);
				//					final Element eltFiles = (Element) eltDataBlock.getElementsByTagName("FILES").item(0);

				final NodeList racine_files = eltDataBlock.getElementsByTagName("FILE");
				if (racine_files != null) {
					final int nb_files = racine_files.getLength();
					//logger.debug("nb_files = " + nb_files);
					for (int k = 0; k<nb_files; k++) {
						Node nodeFile = racine_files.item(k);
						//logger.debug("k=" + k);
						if(nodeFile instanceof Element) {
							final Element eltFile = (Element) nodeFile;
							String filename = eltFile.getAttribute("filename");
							String filetype = eltFile.getAttribute("filetype");
							//								String checksum_method = eltFile.getAttribute("checksum_method");
							String md5 = eltFile.getAttribute("checksum");
							RawData rawData = new RawData();								
//							rawData.relatifName = filename;
							rawData.collabFileName = filename;
							if (filename.contains("/")){
//								rawData.relatifName = filename.substring(filename.lastIndexOf("/")+1);
								rawData.collabFileName = filename.substring(filename.lastIndexOf("/")+1);
							}
							rawData.md5 = md5;
							rawData.extention = filetype;
							run.listRawData.add(rawData);
						}
					}
				}
				if (!listRuns.contains(run)){
					//logger.debug("Ajout de " + run.code + " dans listRuns");
					listRuns.add(run);
				} 
			} 
		}
		return listRuns;
	}


	public static Map<String, String> loadAc(File file_list_AC) throws IOException, SraException {
		if (file_list_AC == null) {
			throw new SraException("Absence du nom du fichier des AC " );
		}
		if (! file_list_AC.exists()){
			throw new SraException("Fichier des AC non present sur disque : "+ file_list_AC.getAbsolutePath());
		}

		try (BufferedReader inputBuffer = new BufferedReader(new FileReader(file_list_AC))) {
			String lg = null;

			Map<String, String> mapAC = new HashMap<>();

			boolean reportEbi = false;
			while ((lg = inputBuffer.readLine()) != null) {
				if (lg.startsWith("<?")){
					// ignorer 
					//logger.debug("ligne ignoree A= '"+ lg+"'");
				} else if (! lg.matches("^\\s*<.*")) {
					// ignorer 
					//logger.debug("ligne ignoree B = '"+ lg+"'");
				} else if (lg.matches("^\\s*$")) {
					// ignorer 
					//logger.debug("ligne ignoree C= '"+ lg+"'");
				} else {
					if (reportEbi==false) {
						//					logger.debug("reportEbi = false");
						//					logger.debug("ligne = '"+ lg+"'");
						//logger.debug("reportEbi = false");
						//logger.debug("ligne = '"+ lg+"'");
						//					String pattern_string = "<RECEIPT\\s+receiptDate=\"(\\S+)\"\\s+submissionFile=\"(\\S+)\"\\s+success=\"true\"";
						//String pattern_string = "<RECEIPT";
						//					java.util.regex.Pattern pattern = Pattern.compile(pattern_string);
						//					Matcher m = pattern.matcher(lg);
						/*if ( ! m.find() ) {
						throw new SraException("Absence de la ligne RECEIPT dans le fichier " + file_list_AC.getAbsolutePath());
					} */
						reportEbi = true;
						//					logger.debug("reportEbi = true");
						//logger.debug("reportEbi = true");
					} else {
						//logger.debug("Traitement des AC :");
						String patternAc = "<(\\S+)\\s+accession=\"(\\S+)\"\\s+alias=\"(\\S+)\"";
						java.util.regex.Pattern pAc = Pattern.compile(patternAc);


						//					logger.debug(lg);
						//logger.debug(lg);
						Matcher mAc = pAc.matcher(lg);
						// Appel de find obligatoire pour pouvoir récupérer $1 ...$n
						if ( ! mAc.find() ) {
							// autre ligne que AC.
						} else {
							//logger.debug("type='"+mAc.group(1)+"', accession='"+mAc.group(2)+"', alias='"+ mAc.group(3)+"'" );
							if (mAc.group(1).equalsIgnoreCase("RUN")){
								//logger.debug("insertion dans mapRun de "+ mAc.group(3) + " et "+ mAc.group(2));
								mapAC.put(mAc.group(3), mAc.group(2));
							} else if (mAc.group(1).equalsIgnoreCase("EXPERIMENT")){
								//logger.debug("insertion dans mapExperiment de " + mAc.group(3) + " et "+ mAc.group(2));
								mapAC.put(mAc.group(3), mAc.group(2));
							} else if (mAc.group(1).equalsIgnoreCase("SAMPLE")){
								//logger.debug("insertion dans mapSample de " + mAc.group(3) + " et "+ mAc.group(2));
								mapAC.put(mAc.group(3), mAc.group(2));
							} else if (mAc.group(1).equalsIgnoreCase("STUDY")){
								mapAC.put(mAc.group(3), mAc.group(2));
								//logger.debug("insertion dans mapStudy de " + mAc.group(3) + " et "+ mAc.group(2));
							} else if (mAc.group(1).equalsIgnoreCase("SUBMISSION")){
								mapAC.put(mAc.group(3), mAc.group(2));
								//logger.debug("insertion dans mapSubmission de "  + mAc.group(3) + " et "+ mAc.group(2));
							} else {

							}
						}// end else
					} // end else
				}
			}
			//inputBuffer.close();
			return mapAC;
		} catch (IOException e) {
			//			e.printStackTrace();
			//logger.debug("error",e);
			throw new RuntimeException(e);
		}
	}

}

