package services;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
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
import models.sra.submit.sra.instance.EbiIdentifiers;
import models.sra.submit.sra.instance.Experiment;
import models.sra.submit.sra.instance.Project;
import models.sra.submit.sra.instance.RawData;
import models.sra.submit.sra.instance.ReadSpec;
import models.sra.submit.sra.instance.Run;
import models.sra.submit.sra.instance.Sample;
import models.sra.submit.sra.instance.Study;
import models.sra.submit.util.SraException;
import models.utils.InstanceConstants;

public class XmlToSra {

	private static final play.Logger.ALogger logger = play.Logger.of(XmlToSra.class);

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

	private static String unmaskNoUTF8(String chaine) {
		String retour = chaine;
		retour = retour.replaceAll("#micron#", "µ");
		retour = retour.replaceAll("#ccedil#", "ç");
		retour = retour.replaceAll("#degres#", "°");
		retour = retour.replaceAll("#eaccentaigu#", "é");
		retour = retour.replaceAll("#eaccentgrave#", "è");
		retour = retour.replaceAll("#echapeau#","ê");
		retour = retour.replaceAll("#aaccentgrave#", "à");
		retour = retour.replaceAll("#achapeau#","â");

		retour = retour.replaceAll("#apostrof", "'");
		retour = retour.replaceAll("#chapeau", "^");
		retour =  retour.replaceAll("#prim", "’");
		retour = retour.replaceAll("#rondR", "®");
		retour = retour.replaceAll("#tiretPipe","¦");
		retour = retour.replaceAll("#citation", "“");
		retour = retour.replaceAll("#paragraphe", "▒");
		return retour;

	}
	
	// si la chaine xmlSamples contient le caractere µ alors SAXException declenchee par documentToSample  
	// hack pour substituer µ dans chaine xml, et le remettre dans les differents champs du sample dans methode documentToSample
	private static String maskNoUTF8(String chaine) {
		String retour = chaine;
		retour = retour.replaceAll("µ", "#micron#");
		retour = retour.replaceAll("ç", "#ccedil#");
		retour = retour.replaceAll("°", "#degres#");
		retour = retour.replaceAll("é", "#eaccentaigu#");
		retour = retour.replaceAll("è", "#eaccentgrave#");
		retour = retour.replaceAll("ê", "#echapeau#");
		retour = retour.replaceAll("à", "#aaccentgrave#");
		retour = retour.replaceAll("â", "#achapeau#");

		retour = retour.replaceAll("'", "#apostrof");
		retour = retour.replaceAll("\\^", "#chapeau");
		retour = retour.replaceAll("’", "#prim");
		retour = retour.replaceAll("®", "#rondR");
		retour = retour.replaceAll("¦", "#tiretPipe");
		retour = retour.replaceAll("“", "#citation");
		retour = retour.replaceAll("▒", "#paragraphe");  


		return retour;
	}
	
	// si la chaine xmlSamples contient le caractere µ alors SAXException declenchee par documentToSample  
	// hack pour substituer µ dans chaine xml, et le remettre dans les differents champs du sample dans methode documentToSample
	public static List<Sample> xmlToSample(String xmlSamplesOri) throws ParserConfigurationException {
		List<Sample> listSamples = new ArrayList<>();
		
		// si la chaine xmlSamples contient le caractere µ alors SAXException declenchee par documentToSample  
		// hack pour substituer µ dans chaine xml, et le remettre dans les differents champs du sample dans methode documentToSample
		String xmlSamples = maskNoUTF8(xmlSamplesOri);

		//logger.debug(xmlSamples);
		
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
				final Element identifiers = (Element) eltSample.getElementsByTagName("IDENTIFIERS").item(0);
				if (identifiers.getElementsByTagName("SECONDARY_ID").item(0) != null) {
					String externalId = identifiers.getElementsByTagName("SECONDARY_ID").item(0).getTextContent();
					if (StringUtils.isNotBlank(externalId)) {
						sample.externalId = unmaskNoUTF8(externalId);
						//logger.debug("externalId : " + externalId);
					}
				}
				
				if (identifiers.getElementsByTagName("EXTERNAL_ID").item(0) != null) {
					String externalID = identifiers.getElementsByTagName("EXTERNAL_ID").item(0).getTextContent();
					if (StringUtils.isNotBlank(externalID)) {
						sample.externalId = unmaskNoUTF8(externalID);
						//logger.debug("externalID : " + externalID);
					}
				}	
				
				if (eltSample.getElementsByTagName("DESCRIPTION").item(0) != null) {
					String description =  eltSample.getElementsByTagName("DESCRIPTION").item(0).getTextContent();
					if (StringUtils.isNotBlank(description)) {
						sample.description = unmaskNoUTF8(description);
						//logger.debug("description : " + description);
					}
				}
				if (eltSample.getElementsByTagName("TITLE").item(0) != null) {
					String title =  eltSample.getElementsByTagName("TITLE").item(0).getTextContent();
					if (StringUtils.isNotBlank(title)) {
						sample.title = unmaskNoUTF8(title);
						//logger.debug("title : " + title);
					}
				}
				final Element eltSampleName = (Element) eltSample.getElementsByTagName("SAMPLE_NAME").item(0);
				if (eltSampleName.getElementsByTagName("COMMON_NAME").item(0)!= null) {
					String commonName = eltSampleName.getElementsByTagName("COMMON_NAME").item(0).getTextContent();
					if (StringUtils.isNotBlank(commonName)) {
						sample.commonName = unmaskNoUTF8(commonName);
						//logger.debug("commonName : " + commonName);
					}
				}
				if (eltSampleName.getElementsByTagName("SCIENTIFIC_NAME").item(0)!= null) {
					String scientificName = eltSampleName.getElementsByTagName("SCIENTIFIC_NAME").item(0).getTextContent();
					if (StringUtils.isNotBlank(scientificName)) {
						sample.scientificName = unmaskNoUTF8(scientificName);
						//logger.debug("scientificName : " + scientificName);
					}
				}
				if (eltSampleName.getElementsByTagName("ANONYMIZED_NAME").item(0)!= null) {
					String anonymizedName = eltSampleName.getElementsByTagName("ANONYMIZED_NAME").item(0).getTextContent();
					if (StringUtils.isNotBlank(anonymizedName)) {
						sample.anonymizedName = unmaskNoUTF8(anonymizedName);
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
								if(tag.equals("External Id")) {
									if (eltAttribute.getElementsByTagName("VALUE").item(0) != null) {
										String value = eltAttribute.getElementsByTagName("VALUE").item(0).getTextContent();
										sample.externalId = value;
									}
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
						sample.attributes = unmaskNoUTF8(sampleAttributes);
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
		//logger.debug("*************PROLOGUE************");
		//logger.debug("version : " + document.getXmlVersion());
		//logger.debug("encodage : " + document.getXmlEncoding());      
		//logger.debug("standalone : " + document.getXmlStandalone());
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
		 * Etape 5 : récupération des studies
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
				
				final Element identifiers = (Element) eltStudy.getElementsByTagName("IDENTIFIERS").item(0);
				if (identifiers.getElementsByTagName("SECONDARY_ID").item(0) != null) {
					String externalId = identifiers.getElementsByTagName("SECONDARY_ID").item(0).getTextContent();
					if (StringUtils.isNotBlank(externalId)) {
						study.externalId = unmaskNoUTF8(externalId);
						//logger.debug("externalId : " + externalId);
					}
				}
				if (identifiers.getElementsByTagName("EXTERNAL_ID").item(0) != null) {
					String externalID = identifiers.getElementsByTagName("EXTERNAL_ID").item(0).getTextContent();
					if (StringUtils.isNotBlank(externalID)) {
						study.accession = unmaskNoUTF8(externalID);
						//logger.debug("externalID : " + externalID);
					}
				}	
				final Element descriptor = (Element) eltStudy.getElementsByTagName("DESCRIPTOR").item(0);

				if (descriptor.getElementsByTagName("STUDY_TITLE").item(0)!= null) {
					String title = descriptor.getElementsByTagName("STUDY_TITLE").item(0).getTextContent();
					if (StringUtils.isNotBlank(title)) {
						study.title = unmaskNoUTF8(title);
						//logger.debug("title : " + title);
					}
				}
				if (descriptor.getElementsByTagName("STUDY_ABSTRACT").item(0)!= null) {
					String studyAbstract = descriptor.getElementsByTagName("STUDY_ABSTRACT").item(0).getTextContent();
					if (StringUtils.isNotBlank(studyAbstract)) {
						study.studyAbstract = unmaskNoUTF8(studyAbstract);
						//logger.debug("studyAbstract : " + studyAbstract);
					}
				}
				if (descriptor.getElementsByTagName("STUDY_DESCRIPTION").item(0)!= null) {
					String description = descriptor.getElementsByTagName("STUDY_DESCRIPTION").item(0).getTextContent();
					if (StringUtils.isNotBlank(description)) {
						study.description = unmaskNoUTF8(description);
						//logger.debug("description : " + description);
					}
				}	
				if (descriptor.getElementsByTagName("CENTER_PROJECT_NAME").item(0)!= null) {
					String centerProjectName = descriptor.getElementsByTagName("CENTER_PROJECT_NAME").item(0).getTextContent();
					if (StringUtils.isNotBlank(centerProjectName)) {
						study.centerProjectName = centerProjectName;
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

	// Attention dans le cas des projects umbrella, le fichier project.xml contient bien les projects enfants,
	// mais le xml renvoyé par le site de l'EBI ne contient plus les projects enfants.
	// idem pour locus_tag_prefixs qui n'apparaissent pas dans Webin-9
	@Deprecated
	public static List<Project> documentToProject(Document document) {
		List<Project> listProjects = new ArrayList<>();

		//Affiche du prologue
		//		logger.debug("*************PROLOGUE************");
		//		logger.debug("version : " + document.getXmlVersion());
		//		logger.debug("encodage : " + document.getXmlEncoding());      
		//		logger.debug("standalone : " + document.getXmlStandalone());
		//logger.debug("*************PROLOGUE************");
		//logger.debug("version : " + document.getXmlVersion());
		//logger.debug("encodage : " + document.getXmlEncoding());      
		//logger.debug("standalone : " + document.getXmlStandalone());
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
		 * Etape 5 : récupération des projects
		 */
		final NodeList racineNoeuds = racine.getChildNodes();
		final int nbRacineNoeuds = racineNoeuds.getLength();

		for (int i = 0; i<nbRacineNoeuds; i++) {
			if(racineNoeuds.item(i).getNodeType() == Node.ELEMENT_NODE) {
				final Element eltProject = (Element) racineNoeuds.item(i);
				//Affichage d'un project
				//				logger.debug("\n*************PROJECT************");
				//				logger.debug("alias : " + eltProject.getAttribute("alias"));
				//logger.debug("\n*************PROJECT************");
				//logger.debug("alias : " + eltProject.getAttribute("alias"));

				String alias = eltProject.getAttribute("alias");
				Project project = new Project();
				//logger.debug("project alias : " + alias);
				project.code = alias;
				
				String accession = eltProject.getAttribute("accession");
				if (StringUtils.isNotBlank(accession)){
					project.accession = accession;
				}

				String center_name = eltProject.getAttribute("center_name");
				if (StringUtils.isNotBlank(center_name)){
					project.centerName = center_name;
				}
				
				final Element identifiers = (Element) eltProject.getElementsByTagName("IDENTIFIERS").item(0);

//				if (identifiers.getElementsByTagName("SECONDARY_ID").item(0) != null) {
//					String externalId = identifiers.getElementsByTagName("SECONDARY_ID").item(0).getTextContent();
//					if (StringUtils.isNotBlank(externalId)) {
//						project.externalId = unmaskNoUTF8(externalId);
//						logger.debug("externalId : " + externalId);
//					}
//				}
//				if (identifiers.getElementsByTagName("EXTERNAL_ID").item(0) != null) {
//					String externalID = identifiers.getElementsByTagName("EXTERNAL_ID").item(0).getTextContent();
//					if (StringUtils.isNotBlank(externalID)) {
//						project.externalId = unmaskNoUTF8(externalID);
//						logger.debug("externalID : " + externalID);
//					}
//				}	
				if (eltProject.getElementsByTagName("TITLE").item(0) != null) {
					String title =  eltProject.getElementsByTagName("TITLE").item(0).getTextContent();
					if (StringUtils.isNotBlank(title)) {
						project.title = unmaskNoUTF8(title);
						//logger.debug("title : " + title);
					}
				}
				if (eltProject.getElementsByTagName("DESCRIPTION").item(0)!= null) {
					String description = eltProject.getElementsByTagName("DESCRIPTION").item(0).getTextContent();
					if (StringUtils.isNotBlank(description)) {
						project.description = unmaskNoUTF8(description);
						//logger.debug("description : " + description);
					}
				}

				if (eltProject.getElementsByTagName("NAME").item(0)!= null) {
					String name = eltProject.getElementsByTagName("NAME").item(0).getTextContent();
					if (StringUtils.isNotBlank(name)) {
						project.name = unmaskNoUTF8(name);
						//logger.debug("name : " + name);
					}
				}	
				
				if (eltProject.getElementsByTagName("SUBMISSION_PROJECT").item(0)!= null) {
					final Element eltTypeSubmissionProject = (Element) eltProject.getElementsByTagName("SUBMISSION_PROJECT").item(0);
					if (eltTypeSubmissionProject.getElementsByTagName("SEQUENCING_PROJECT").item(0)!= null) {
						project.submissionProjectType = "SEQUENCING_PROJECT";
						
						final Element eltSequencingProject = (Element) eltTypeSubmissionProject.getElementsByTagName("SEQUENCING_PROJECT").item(0);
						//le locus_tag n'apparait pas sur notre drop box et changement de modele
//						final NodeList locus_tag_list = eltSequencingProject.getElementsByTagName("LOCUS_TAG_PREFIX");
//						final int nb_locus_tag = locus_tag_list.getLength();
//						//logger.debug("nb_locus_tag="+ nb_locus_tag);
//						if(nb_locus_tag > 0) {
//							if(project.locusTagPrefixs == null) {
//								project.locusTagPrefixs = new ArrayList<String>();
//							}
//						}
//						for (int k = 0; k<nb_locus_tag; k++) {
//							project.locusTagPrefixs.add(eltSequencingProject.getElementsByTagName("LOCUS_TAG_PREFIX").item(k).getTextContent());
//						}		
					}
				}
				if (eltProject.getElementsByTagName("UMBRELLA_PROJECT").item(0)!= null) {
					//logger.debug("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX    UMBRELLA_PROJECT OK");
					project.submissionProjectType = "UMBRELLA_PROJECT";
				}
				
				if (eltProject.getElementsByTagName("RELATED_PROJECTS").item(0)!= null) {
					//logger.debug("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX    RELATED_PROJECTS OK");

					final Element eltTypeRelatedProjects = (Element) eltProject.getElementsByTagName("RELATED_PROJECTS").item(0);
					final NodeList relatedProjectlist = eltTypeRelatedProjects.getElementsByTagName("RELATED_PROJECT");
					final int nb_relatedProject = relatedProjectlist.getLength();
					//logger.debug("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX    nombre de project enfants = " + nb_relatedProject);
					if(nb_relatedProject > 0) {
						if(project.childrenProjectAccessions == null) {
							project.childrenProjectAccessions = new ArrayList<String>();
						}
					}
					for (int k = 0; k < nb_relatedProject; k++) {
						//logger.debug("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX    k = " + k);
						final Element eltTypeRelatedProject = (Element) eltTypeRelatedProjects.getElementsByTagName("RELATED_PROJECT").item(k);
						final Element eltChildProject = (Element) eltTypeRelatedProject.getElementsByTagName("CHILD_PROJECT").item(0);
						if (eltChildProject != null) {
							String childProject = eltChildProject.getAttribute("accession");
							//logger.debug("childProject=" + childProject);
							project.childrenProjectAccessions.add(childProject);
						}
					}
				}
				if (!listProjects.contains(project)){
					listProjects.add(project);
				}
			} 
		} // end for  
		return listProjects;
	}	

	

	public static List<EbiIdentifiers> documentToEbiIdentifiers(Document document) {
		if(document == null) {
			return null;
		}
		List<EbiIdentifiers> listEbiIdentifiers = new ArrayList<>();
		//logger.debug("*************PROLOGUE************");
		//logger.debug("version : " + document.getXmlVersion());
		//logger.debug("encodage : " + document.getXmlEncoding());      
		//logger.debug("standalone : " + document.getXmlStandalone());
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
		 * Etape 5 : récupération des projects
		 */
		final NodeList racineNoeuds = racine.getChildNodes();
		final int nbRacineNoeuds = racineNoeuds.getLength();

		for (int i = 0; i<nbRacineNoeuds; i++) {
			if(racineNoeuds.item(i).getNodeType() == Node.ELEMENT_NODE) {
				final Element elt = (Element) racineNoeuds.item(i);
				//Affichage d'un project
				//				logger.debug("\n*************PROJECT************");
				//				logger.debug("alias : " + eltProject.getAttribute("alias"));
				//logger.debug("\n*************PROJECT************");
				//logger.debug("alias : " + elt.getAttribute("alias"));

				String alias = elt.getAttribute("alias");
				EbiIdentifiers ebiIdentifiers = new EbiIdentifiers();
				//logger.debug("project alias : " + alias);
				ebiIdentifiers.setAlias(alias);
				String accession = elt.getAttribute("accession");
				if (StringUtils.isNotBlank(accession)){
					ebiIdentifiers.setAccession(accession);
				}

				final Element identifiers = (Element) elt.getElementsByTagName("IDENTIFIERS").item(0);
				if(identifiers==null) {
					return null;
				}
				if (identifiers.getElementsByTagName("SECONDARY_ID").item(0) != null) {
					String externalId = identifiers.getElementsByTagName("SECONDARY_ID").item(0).getTextContent();
					if (StringUtils.isNotBlank(externalId)) {
						ebiIdentifiers.setExternalId(unmaskNoUTF8(externalId));
						//logger.debug("externalId : " + externalId);
					}
				}
				if (identifiers.getElementsByTagName("EXTERNAL_ID").item(0) != null) {
					String externalId = identifiers.getElementsByTagName("EXTERNAL_ID").item(0).getTextContent();
					if (StringUtils.isNotBlank(externalId)) {
						ebiIdentifiers.setExternalId(unmaskNoUTF8(externalId));
						//logger.debug("externalID : " + externalId);
					}
				}
				if (!listEbiIdentifiers.contains(ebiIdentifiers)){
						listEbiIdentifiers.add(ebiIdentifiers);
				}
			} 
		} // end for  
		return listEbiIdentifiers;
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


	public static List<Study> xmlToStudy(String xmlStudiesOri) throws ParserConfigurationException {
		// si la chaine xmlSamples contient le caractere µ alors SAXException declenchee par documentToSample  
		// hack pour substituer µ dans chaine xml, et le remettre dans les differents champs du study dans methode documentToStudy
		String xmlStudies = maskNoUTF8(xmlStudiesOri);
				
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

	@Deprecated
	public static List<Project> xmlToProject(String xmlProjectsOri) throws ParserConfigurationException {
		// si la chaine xmlSamples contient le caractere µ alors SAXException declenchee par documentToSample  
		// hack pour substituer µ dans chaine xml, et le remettre dans les differents champs du study dans methode documentToStudy
		String xmlProjects = maskNoUTF8(xmlProjectsOri);
		//logger.debug("xmlProjects apres mask = " + xmlProjects);
		List<Project> listProjects = new ArrayList<>();

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
			InputStream targetStream = new ByteArrayInputStream(xmlProjects.getBytes());
			//				document = builder.parse(xmlStudies);
			document = builder.parse(targetStream);
			listProjects = documentToProject(document);
		} catch (SAXException |IOException e) {
			throw new RuntimeException(e);
		}
		return listProjects;
	}	

	public static List<EbiIdentifiers> xmlSraToEbiIdentifiers(String xmlSrasOri) throws ParserConfigurationException {
		// si la chaine xmlSamples contient le caractere µ alors SAXException declenchee par documentToSample  
		// hack pour substituer µ dans chaine xml, et le remettre dans les differents champs du study dans methode documentToStudy
		if(StringUtils.isBlank(xmlSrasOri)) {
			return null;
		}
		String xmlSRA = maskNoUTF8(xmlSrasOri);
		//logger.debug("xmlProjects apres mask = " + xmlProjects);
		List<EbiIdentifiers> listEbiIdentifiers = new ArrayList<>();

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
			InputStream targetStream = new ByteArrayInputStream(xmlSRA.getBytes());
			//				document = builder.parse(xmlStudies);
			document = builder.parse(targetStream);
			listEbiIdentifiers = documentToEbiIdentifiers(document);
		} catch (SAXException |IOException e) {
			throw new RuntimeException(e);
		}
		return listEbiIdentifiers;
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

	public static List<Experiment> xmlToExperiment(String xmlExperimentsOri) throws ParserConfigurationException {
		// si la chaine xmlSamples contient le caractere µ alors SAXException declenchee par documentToSample  
		// hack pour substituer µ dans chaine xml, et le remettre dans les differents champs du sample dans methode documentToSample
		String xmlExperiments = maskNoUTF8(xmlExperimentsOri);

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
		//logger.debug("*************PROLOGUE************");
		//logger.debug("version : " + document.getXmlVersion());
		//logger.debug("encodage : " + document.getXmlEncoding());      
		//logger.debug("standalone : " + document.getXmlStandalone());
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
						experiment.title = unmaskNoUTF8(title);
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
						//logger.debug("typePlatform = " + typePlatform);
					}
				}

				if ("ILLUMINA".equalsIgnoreCase(typePlatform) || "LS454".equalsIgnoreCase(typePlatform) || "OXFORD_NANOPORE".equalsIgnoreCase(typePlatform)) {
					experiment.typePlatform = typePlatform;
					String typePlatFormQuery = typePlatform.toUpperCase();
					//logger.debug("typePlatFormQuery = " + typePlatFormQuery);
					final Element eltExpPlatformType = (Element) eltExpPlatform.getElementsByTagName(typePlatFormQuery).item(0);
					if(eltExpPlatformType != null) {
						if (eltExpPlatformType.getElementsByTagName("INSTRUMENT_MODEL").item(0) != null) {	
							String instrument_model = eltExpPlatformType.getElementsByTagName("INSTRUMENT_MODEL").item(0).getTextContent();
							if (StringUtils.isNotBlank(instrument_model)){
								//logger.debug("instrument_model : " + instrument_model);
								experiment.instrumentModel = instrument_model;
							}
						}
					}
				} else {
					//logger.debug("Type de plateforme inconnue" + typePlatform );
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
						experiment.libraryName = unmaskNoUTF8(libraryName);
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
						experiment.libraryConstructionProtocol = unmaskNoUTF8(libraryConstructionProtocol);
						//logger.debug("libraryConstructionProtocol : " + libraryConstructionProtocol);
					}
				}	

				final Element eltExpLibraryLayout= (Element) eltExpLibraryDescriptor.getElementsByTagName("LIBRARY_LAYOUT").item(0);
				if (eltExpLibraryLayout.getElementsByTagName("SINGLE").item(0) != null) {
					//logger.debug("libraryLayout : single");
					experiment.libraryLayout = "single";
				} 
				
				if (eltExpLibraryLayout.getElementsByTagName("PAIRED").item(0) != null) {
					//logger.debug("XXXXXXXXXXXXXXXXXXXXXXXXXXX   libraryLayout : paired");
					experiment.libraryLayout = "paired";
					final Element eltExpLibraryLayoutPaired = (Element) eltExpLibraryLayout.getElementsByTagName("PAIRED").item(0);
					// 30/11/2022 : ticket SUPSQ-5215 : notion de nominal_length qui a disparu de l'EBI
					String nominal_length = eltExpLibraryLayoutPaired.getAttribute("NOMINAL_LENGTH");
					if (StringUtils.isNotBlank(nominal_length)){
						//logger.debug("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX   nominal_length : " + nominal_length);
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
	
	public static List<Run> xmlToRun(String xmlRunsOri) throws ParserConfigurationException, ParseException {
		// si la chaine xmlSamples contient le caractere µ alors SAXException declenchee par documentToSample  
		// hack pour substituer µ dans chaine xml, et le remettre dans les differents champs du sample dans methode documentToSample
		String xmlRuns = maskNoUTF8(xmlRunsOri);

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
		//logger.debug("*************PROLOGUE************");
		//logger.debug("version : " + document.getXmlVersion());
		//logger.debug("encodage : " + document.getXmlEncoding());      
		//logger.debug("standalone : " + document.getXmlStandalone());
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
							//rawData.relatifName = filename;
							rawData.collabFileName = filename;
							if (filename.contains("/")){
								//rawData.relatifName = filename.substring(filename.lastIndexOf("/")+1);
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


	// Renvoie la liste des samples generés à partir des fichiers xml et completés pour AC, state ...
	public List<Sample> forSamples(File xmlSample, String user) throws IOException, SraException, ParserConfigurationException {	
		// Recuperer ensemble des objets samples à partir du fichier xml
		//		logger.debug("Recuperation des samples");
		List<Sample> listSamples = xmlFileToSample(xmlSample);
		//		logger.debug("Recuperation des AC");

		String pattern = "sample_([A-Z]{2,3})(_|-)";
		String pattern2 = "TARA_([A-Z]{2,3})(_|-)"; // pour recuperer les samples du projet tara declare par nous dans le projet BCB ou ALP

		java.util.regex.Pattern p = Pattern.compile(pattern);
		java.util.regex.Pattern p2 = Pattern.compile(pattern2);


		for (Sample sample : listSamples) {
			//logger.debug("!!!!!!!!!!!!!!!!!!sample : '" + sample.code + "'");
			//if ( ! mapAc.containsKey(sample.code)) {
			//	throw new SraException("Absence de numeros d'accession pour le code '" + sample.code + "'");
			//}
			//logger.debug("Pour le sample : " + sample.code + " numeros d'accession = " + mapAc.get(sample.code));
			// ajouter informations codeProject, accession , status et traceInformation et sauver dans base;

			Matcher m = p.matcher(sample.code);


			// Appel de find obligatoire pour pouvoir récupérer $1 ...$n
			if ( m.find() ) {
				sample.projectCode = m.group(1);
			} else {
				m = p2.matcher(sample.code);
				if ( m.find() ) {
					sample.projectCode = m.group(1);
				}
			}

			//sample.accession = mapAc.get(sample.code);
//			sample.state = new State("F-SUB", user);
//			sample.traceInformation.setTraceInformation(user);
//			sample.adminComment = adminComment;
			//			ContextValidation contextValidation = new ContextValidation(user);
			//			contextValidation.setCreationMode();
			////			contextValidation.getContextObjects().put("type", "sra");
			//			contextValidation.putObject("type", "sra");
		}
		//logger.debug("sortie de forSamples");

		return listSamples;
	}

	// Renvoie la liste des samples generés à partir de la string xml et completés pour AC, state ...
	public Iterable <Sample> forSamples(String xmlSample, Date submissionDate) throws IOException, SraException, ParserConfigurationException {	
		// Recuperer ensemble des objets samples à partir du fichier xml
		//		logger.debug("Recuperation des samples");
		//logger.debug("Recuperation des samples");
		//logger.debug(xmlSample);
		List<Sample> listSamples = xmlToSample(xmlSample);
		//		logger.debug("Recuperation des AC");
		//logger.debug("Recuperation des AC");

		String pattern = "sample_([A-Z]{2,3})(_|-)";
		String pattern2 = "TARA_([A-Z]{2,3})(_|-)"; // pour recuperer les samples du projet tara declare par nous dans le projet BCB ou ALP
		String pattern3 = "virtual_sample_([A-Z]{3})";
		java.util.regex.Pattern p = Pattern.compile(pattern);
		java.util.regex.Pattern p2 = Pattern.compile(pattern2);
		java.util.regex.Pattern p3 = Pattern.compile(pattern3);


		for (Sample sample : listSamples) {
			if (submissionDate != null) {
				sample.firstSubmissionDate = submissionDate;
			}
			//logger.debug("!!!!!!!!!!!!!!!!!!sample : '" + sample.code + "'");
			//if ( ! mapAc.containsKey(sample.code)) {
			//	throw new SraException("Absence de numeros d'accession pour le code '" + sample.code + "'");
			//}
			//logger.debug("Pour le sample : " + sample.code + " numeros d'accession = " + mapAc.get(sample.code));
			// ajouter informations codeProject, accession , status et traceInformation et sauver dans base;

			Matcher m = p.matcher(sample.code);


			// Appel de find obligatoire pour pouvoir récupérer $1 ...$n
			if ( m.find() ) {
				sample.projectCode = m.group(1);
			} else {
				m = p2.matcher(sample.code);
				if ( m.find() ) {
					sample.projectCode = m.group(1);
				} else {
					m = p3.matcher(sample.code);
					if ( m.find() ) {
						sample.projectCode = m.group(1);
					}
				}
			}

			//sample.accession = mapAc.get(sample.code);
//			sample.state = new State("F-SUB", user);
//			sample.traceInformation.setTraceInformation(user);
//			sample.adminComment = adminComment;
			//			ContextValidation contextValidation = new ContextValidation(user);
			//			contextValidation.setCreationMode();
			////			contextValidation.getContextObjects().put("type", "sra");
			//			contextValidation.putObject("type", "sra");
			sample.adminComment = adminComment;
		}
		//logger.debug("sortie de forSamples");

		return listSamples;
	}


	// Renvoie la liste des studies generés à partir des fichiers xml et completés pour AC, state ...
	public List<Study> forStudies(File xmlStudy, String user) throws IOException, SraException, ParserConfigurationException {	
		// Recuperer ensemble des objets study à partir du fichier xml
		//logger.debug("Recuperation des study");
		List<Study> listStudies = xmlFileToStudy(xmlStudy);

//		String pattern1 = "^study_([A-Z]{2,3})$";
//		String pattern2 = "^study_([A-Z]{2,3})(_|-)";
//		String pattern3 = "^project_([A-Z]{2,3})$";
//
//		java.util.regex.Pattern p1 = Pattern.compile(pattern1);
//		java.util.regex.Pattern p2 = Pattern.compile(pattern2);
//		java.util.regex.Pattern p3 = Pattern.compile(pattern3);
//
//		for (Study study : listStudies) {
//			logger.debug("!!!!!!!!!!!!!!!!!!study : '" + study.code + "'");
//
//			Matcher m = p1.matcher(study.code);
//			// Appel de find obligatoire pour pouvoir récupérer $1 ...$n
//			if ( m.find() ) {
//				study.projectCodes.add(m.group(1));
//			} else {
//				m = p2.matcher(study.code);
//				if ( m.find() ) {
//					study.projectCodes.add(m.group(1));
//				} else {
//					m = p3.matcher(study.code);
//					if ( m.find() ) {
//						study.projectCodes.add(m.group(1));
//					}
//				}
//			}
//
////			study.state = new State("F-SUB", user);
////			study.traceInformation.setTraceInformation(user);
//		}
		//logger.debug("sortie de forStudies");

		return listStudies;
	}

	// Renvoie la liste des projects generés à partir d'une string xml et completés pour AC, state ...
	// fonctionne sur model de project SEQUENCING ou UMBRELLA => plus pertinent
	@Deprecated
	public List<Project> forProjects(String xmlProject, Date submissionDate) throws IOException, SraException, ParserConfigurationException {	
		// Recuperer ensemble des objets project à partir de la string xml
		//logger.debug("Recuperation des project");
		List<Project> listProjects = xmlToProject(xmlProject);

		for (Project project : listProjects) {
			//logger.debug("!!!!!!!!!!!!!!!!!!project : '" + project.code + "'");
			if (submissionDate != null) {
				project.firstSubmissionDate = submissionDate;
			}
		}
		//logger.debug("sortie de forProjects");
		return listProjects;
	}


	// Renvoie la liste des studies generés à partir d'une string xml et completés pour AC, state ...
	public List<Study> forStudies(String xmlStudy, Date submissionDate) throws IOException, SraException, ParserConfigurationException {	
		// Recuperer ensemble des objets study à partir de la string xml
		//logger.debug("Recuperation des study");
		List<Study> listStudies = xmlToStudy(xmlStudy);

		String pattern1 = "^study_([A-Z]{2,3})$";
		String pattern2 = "^study_([A-Z]{2,3})(_|-)";
		String pattern3 = "^project_([A-Z]{2,3})$";

		java.util.regex.Pattern p1 = Pattern.compile(pattern1);
		java.util.regex.Pattern p2 = Pattern.compile(pattern2);
		java.util.regex.Pattern p3 = Pattern.compile(pattern3);

		for (Study study : listStudies) {
			//logger.debug("!!!!!!!!!!!!!!!!!!study : '" + study.code + "'");
			if (submissionDate != null) {
				study.firstSubmissionDate = submissionDate;
				Calendar calendar = Calendar.getInstance();
				// installer date de soumission dans calendar et ajouter 2 ans
				// pour avoir date de release :
				calendar.setTime(study.firstSubmissionDate);		
				calendar.add(Calendar.YEAR, 2);
				study.releaseDate  = calendar.getTime();
			}
			Matcher m = p1.matcher(study.code);
			// Appel de find obligatoire pour pouvoir récupérer $1 ...$n
			if ( m.find() ) {
				study.projectCodes.add(m.group(1));
			} else {
				m = p2.matcher(study.code);
				if ( m.find() ) {
					study.projectCodes.add(m.group(1));
				} else {
					m = p3.matcher(study.code);
					if ( m.find() ) {
						study.projectCodes.add(m.group(1));
					}
				}
			} 
			
			
//			study.state = new State("F-SUB", user);
//			study.traceInformation.setTraceInformation(user);
		}
		//logger.debug("sortie de forStudies");

		return listStudies;
	}

	
	
	// Renvoie la liste des experiments generés à partir de la string xml et completés pour AC, state ...
	public List<Experiment> forExperiments(String xmlExperiment, Date submissionDate) throws IOException, SraException, ParserConfigurationException{	
		// Recuperer ensemble des objets experiment à partir de la string xml
		//logger.debug("Recuperation des experiment");
		List<Experiment> listExperiments = xmlToExperiment(xmlExperiment);

		String pattern1 = "^exp_([A-Z]{2,3})_";
		String pattern2 = "^exp_[0-9]\\.TCA\\.([A-Z]{2,3})_";
		String pattern3 = "^exp_[0-9]\\.GAC\\.([A-Z]{2,3})_";
		//String pattern3 = "^study_([A-Z]{3})$";
		//String pattern4 = "^study_([A-Z]{2})$";

		java.util.regex.Pattern p1 = Pattern.compile(pattern1);
		java.util.regex.Pattern p2 = Pattern.compile(pattern2);
		java.util.regex.Pattern p3 = Pattern.compile(pattern3);
		//java.util.regex.Pattern p4 = Pattern.compile(pattern4);

		for (Experiment experiment : listExperiments) {
			//logger.debug("experiment : " + experiment.code);
			if (submissionDate != null) {
				experiment.firstSubmissionDate = submissionDate;
			}
			// Appel de find obligatoire pour pouvoir récupérer $1 ...$n
			Matcher m = p1.matcher(experiment.code);
			if ( m.find() ) {
				experiment.projectCode = m.group(1);
			} else {
				m = p2.matcher(experiment.code);
				if ( m.find() ) {
					experiment.projectCode = m.group(1);
				} else {
					m = p3.matcher(experiment.code);
					if ( m.find() ) {
						experiment.projectCode = m.group(1);
					}/*else {
						m = p4.matcher(experiment.code);
						if ( m.find() ) {
							experiment.projectCode = m.group(1);
						}
					}*/
				}
			}
//			experiment.state = new State("F-SUB", user);
//			experiment.traceInformation.setTraceInformation(user);
//			experiment.adminComment = adminComment;
			/*
			ContextValidation contextValidation = new ContextValidation(user);
			contextValidation.setCreationMode();
			contextValidation.getContextObjects().put("type", "sra");
			 */
		}
		//logger.debug("sortie de forExperiments");
		return listExperiments;
	}

	// Renvoie la liste des runs generés à partir des fichiers xml et completés pour AC, state ...
	public List<Run> forRuns(String xmlRun) throws IOException, SraException, ParserConfigurationException, ParseException {	
		// Recuperer ensemble des objets runs à partir de la string xml
		List<Run> listRuns = xmlToRun(xmlRun);
		//logger.debug("Recuperation de "+ listRuns.size() + " runs");

//		for (Run run : listRuns) {
//			//logger.debug("run : " + run.code);
//			run.adminComment = adminComment;
//		}
		return listRuns;
	}

}

