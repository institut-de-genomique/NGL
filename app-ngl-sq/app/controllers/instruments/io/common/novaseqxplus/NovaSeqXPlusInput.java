package controllers.instruments.io.common.novaseqxplus;

// import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
// import java.io.InputStreamReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathExpression;

import org.mongojack.DBQuery;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

//import org.w3c.dom.NodeList;
import org.w3c.dom.*;


import controllers.instruments.io.utils.AbstractInput;
import fr.cea.ig.MongoDBDAO;
import models.laboratory.common.instance.property.PropertyFileValue;
import models.laboratory.experiment.instance.Experiment;
import models.laboratory.reagent.description.BoxCatalog;
import models.laboratory.reagent.description.KitCatalog;
import models.laboratory.reagent.description.ReagentCatalog;
import models.laboratory.reagent.instance.ReagentUsed;
import models.utils.InstanceConstants;
import validation.ContextValidation;

public class NovaSeqXPlusInput extends AbstractInput {
	
private static final play.Logger.ALogger logger = play.Logger.of(NovaSeqXPlusInput.class);
	
	/* F. Dos Santos NGL-4319: Dépôt NovaSeqXplus : import fichier XML
       Description du fichier à traiter: (lignes importantes)
      
		<?xml version="1.0" encoding="utf-8"?>
		<RunParameters xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema">

 		<Side>A</Side>
		<OutputFolder>//cng-svm-seq-11-h05.seq.cng.fr/JOLIOTCURIX/data/20231006_LH00350_0005_A22CWJFLT3</OutputFolder>
		<RunId>20231006_LH00350_0005_Ayiyiyiy</RunId>
		<RecipeName>10B Sequencing</RecipeName>
 		<ConsumableInfo>
    		<ConsumableInfo>
     			 <SerialNumber>22CWJFLT3</SerialNumber>
     			 <LotNumber>20773012</LotNumber>
     			 <PartNumber>20080370</PartNumber>
     			 <ExpirationDate>2024-12-28T00:00:00+01:00</ExpirationDate>
     			 <Type>FlowCell</Type>
     			 <Mode>10B</Mode>
    		</ConsumableInfo>
			...
			...
		</ConsumableInfo>
		..
	*/	
	
	private class ReagentNode {
		Map<String,String> properties = new HashMap<>(0); 
	}
	
	@Override
	public Experiment importFile(Experiment experiment, PropertyFileValue pfv, ContextValidation contextValidation) {
		logger.debug("importing file " + pfv.fullname );
	 	//System.out.println(">>> import fichier " + pfv.fullname );
		
 		try {
	    	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	    	DocumentBuilder builder = factory.newDocumentBuilder();  /// c'est ici que des erreur de balises peuvent etre vues
	    	InputStream inputStream = new ByteArrayInputStream(pfv.byteValue());
	    	InputSource is = new InputSource(inputStream);
	    	is.setEncoding("UTF-8");
			Document xml = builder.parse(is);
	     	//optional, but recommended
	     	//read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
	     	xml.getDocumentElement().normalize();
	        Element root = xml.getDocumentElement();
	        XPathFactory xpf = XPathFactory.newInstance();
	        XPath xpath = xpf.newXPath();

			// -1 vérification de structure du fichier ...et de consistance
			//logger.debug(">>> -1 vérification de structure du fichier");
			//System.out.println(">>> -1 vérification de structure du fichier");
			checkFile(pfv.fullname, root, xpath, contextValidation);
			if (contextValidation.hasErrors())
		  		return experiment;

			// -2 verification de correspondance entre expérience et fichier
			//logger.debug(">>> -2 vérification de correspondance entre expérience et fichier");
			//System.out.println(">>> -2 vérification de correspondance entre expérience et fichier");
	        checkConsistancy(root, xpath, experiment, contextValidation);
			if (contextValidation.hasErrors())
		  		return experiment;

			// -3 vérifier kit
			//logger.debug(">>> -3 vérifier kit actif");
			//System.out.println(">>> -3 vérifier kit actif");

			KitCatalog kit=getKit(experiment, contextValidation);
			if (contextValidation.hasErrors())
		 	   return experiment;

			// -4 trouver les boites du kit 
			List<BoxCatalog> boxes=getBoxes(kit, contextValidation);  
			if (contextValidation.hasErrors())
		 	   return experiment;

			// -5 import des réactifs trouvés dans le fichier
			//logger.debug(">>> -5 import des réactifs trouvés dans le fichier");
			//System.out.println(">>> -5 import des réactifs trouvés dans le fichier");
			importReagents (kit.code, boxes, root, xpath, experiment, contextValidation);

		} catch (SAXException | ParserConfigurationException e) {
	    		 contextValidation.addError("Erreurs fichier", "fichier XML incorrect :" + e.getMessage());
	    } catch (IOException e) {
	    		 contextValidation.addError("Erreurs fichier", e.getMessage());
	   	} 
		return experiment;
	}

	private void checkFile( String fileName, Element root, XPath xpath, ContextValidation contextValidation) {
		try {
			String expression=null;
			String tagValue =null;

			// -1 vérifier présence du node <RunParameters  ... 
			/* checkMandatoryXMLTag ne marche pas car  <RunParameters  est une balise avec des attribut...
			String expression="RunParameters";
		    String tagValue = xpath.evaluate(expression, root);
			checkMandatoryXMLTag (contextValidation, expression, tagValue); 
			 	contextValidation.addError("Erreurs fichier", fileName +" n'est pas un fichier RunParameters de Novaseq X plus");
		    if (contextValidation.hasErrors()) {
		    	return;
			}
			*/

		     // -2 vérifier présence node ConsumableInfo avant de faire le reste...
		    expression="ConsumableInfo";
		    tagValue = xpath.evaluate(expression, root);
		    checkMandatoryXMLTag (contextValidation, expression, tagValue); 
		    if (contextValidation.hasErrors()){
			 	contextValidation.addError("Erreurs fichier", "Le fichier "+ fileName +" n'est pas un fichier RunParameters de Novaseq X plus");
		    	return;
			}

		} catch (XPathExpressionException e) {
	   	  	// erreur de (String)xpath.evaluate("XX", root);=> erreur du programmeur
	    	contextValidation.addError("Erreurs interne", "Probleme XPathExpressionException :" + e.getMessage() );
	    } 	
		return;
	}

    private void checkConsistancy (Element root, XPath xpath, Experiment experiment, ContextValidation contextValidation) {
        /* vérifier données identiques entre fichier XML et infos expérience 
			- nom du séquenceur => <OutputFolder> ou <RunId>
			- position sur sequenceur => <Side>
			- barcode de la flowcell => <RunId>
			- type de flowcell => <recipeName>
		  */
    	try {
		    String expression=null;

			// -1- position sur sequenceur: NIVEAU 1
		    expression = "Side";
		    String side = xpath.evaluate(expression, root);
		    checkMandatoryXMLTag (contextValidation, expression, side); 
		    // si la balise OK comparer avec  position
		    if ((side.length() > 0) && (! experiment.instrumentProperties.get("position").value.equals(side))) {
		    		 contextValidation.addError("Erreurs fichier", "La position '"+side+"' du fichier ne correspond pas à celle de l'expérience.");
			} 
		     
			// -2- nom de l'instrument
		    /* Info Illumina: la balise <RunId> peut etre parametree pour que le nom de run contient le nom du PC : 
			<RunId>180115_MARIECURIX_0008_AH5TCYDMXX</RunId>
			==> si ca n'est pas le cas on se rabat sur OutputFolder ???
		      
			  expression = "RunId";
			  String runid = xpath.evaluate(expression, root);
			  checkMandatoryXMLTag (contextValidation, expression, runid);
			  if ((runid.length() > 0) && (! runid.contains(experiment.instrument.code))) {
				  contextValidation.addError("Erreurs fichier", "L'instrument dans le nom du run du fichier ne correspond pas à l'instrument de l'expérience.");
			  }
			*/

 			expression = "OutputFolder";
		    String outputFolder = xpath.evaluate(expression, root);  
		    checkMandatoryXMLTag (contextValidation, expression, outputFolder );

 			if ((outputFolder.length() > 0) && (! outputFolder.contains(experiment.instrument.code))) {
				contextValidation.addError("Erreurs fichier", "L'instrument dans <Outputfolder> du fichier ne correspond pas à celui de l'expérience.");
			}

			// -3- barcode flowcell
			// Il est dans un noeud <ConsumableInfo> de 3eme niveau :-(  => utiliser RunId
		    expression = "RunId";
		    String runId = xpath.evaluate(expression, root);  
		    checkMandatoryXMLTag (contextValidation, expression, runId );
			 
		    if (runId.length() > 0) {
				// spliter runId avec caractere "_" 
				String[] substr = runId.split("_");
				String lastElement=null;
				String flowcellBarcode=null;
				if (substr.length > 0){ 
					//recuperer dernier Element
					lastElement = substr[ substr.length -1];
					// supprimer le caractere inital ( side !)
				    if (lastElement.length() > 0){ 
						flowcellBarcode = lastElement.substring(1) ;
						if ((flowcellBarcode.length() > 0) && (! experiment.inputContainerSupportCodes.contains(flowcellBarcode))) {
							contextValidation.addError("Erreurs fichier", "Le barcode de flowcell '"+ flowcellBarcode +"' du fichier ne correspond pas à celui de l'expérience.");
						}
					}
				}
		 	}

		    // -4- flowcell mode =><RecipeName>
		    expression="RecipeName";
		    String recipe = xpath.evaluate(expression, root);
		    checkMandatoryXMLTag (contextValidation, expression, recipe );
		    // obligatoire dans l'experience en NovaSeq X plus
		    if (null == experiment.instrumentProperties.get("novaseqFlowcellMode") || null == experiment.instrumentProperties.get("novaseqFlowcellMode").value ) {
		    	// si on importe le fichier a l'etat terminé on ne peut plus renseigner 'type de flowcell' !!!
				contextValidation.addError("Erreurs expérience","Veuillez renseigner 'Type flowcell' avant d'importer le fichier."); 
		    } else if ((recipe.length() > 0 ) && ( ! recipe.contains (experiment.instrumentProperties.get("novaseqFlowcellMode").value.toString() ))) {
				contextValidation.addError("Erreurs fichier", "Le type de flowcell '"+ recipe +"' du fichier ne correspond pas à celui de l'expérience.");
		    } 
	     } catch (XPathExpressionException e) {
	   	  	 // erreur de (String)xpath.evaluate("XX", root);=> erreur du programmeur
	    	 contextValidation.addError("Erreurs interne", "Probleme XPathExpressionException :" + e.getMessage() );
	     }    
		 return;       
    }

	// récupérer les boites qui appartiennent a un kit ( le faire une seule fois et ne pas le refaire N fois dans importReagent )
	private List<BoxCatalog> getBoxes(KitCatalog kit, ContextValidation contextValidation) {
		
		//System.out.println(">>> trouver les boites actives de ce kit");
		List<BoxCatalog> boxes = MongoDBDAO.find(InstanceConstants.REAGENT_CATALOG_COLL_NAME, BoxCatalog.class, 
		                         DBQuery.is("category","Box").and(DBQuery.is("kitCatalogCode", kit.code )).and(DBQuery.is("active",true)) ).toList();
		/*if ( null == boxes ){
			la liste boxes est retounée mais avec une longueur 0
		*/
		if ( boxes.size() == 0 ){
		   	contextValidation.addError("Erreurs catalogue", "Pas de boîtes actives trouvées dans le kit '"+ kit.name +"'");
		    return null;
		} else {
			//System.out.println(">>> nbre boxes trouvées :"+ boxes.size() );
			return boxes;
		}
	}

 	private KitCatalog getKit( Experiment experiment, ContextValidation contextValidation) {  
		// REM les kitNames sont harcodés ici...
  		String kitName=null;

		String flowcellType=experiment.instrumentProperties.get("novaseqFlowcellMode").value.toString();
		switch(flowcellType) {
			case "10B":
			  kitName="NovaSeq X series Rgt kit 10B";
			  break;
			case "25B":
			  kitName="NovaSeq X series Rgt kit 25B";
			  break;
			case "1.5B":
			  kitName="NovaSeq X series Rgt kit 25B";
			  break;
			default:
			  	contextValidation.addError("Erreurs fichier", "FlowCell mode '"+flowcellType+"' non géré.");
				return null;
		}

		//REM l'association a l'expérience de preparation flowcell ordonnée n'est pas vérifié...elle est implicite !!!
		KitCatalog kit= MongoDBDAO.findOne(InstanceConstants.REAGENT_CATALOG_COLL_NAME, KitCatalog.class, 
		                                    DBQuery.is("category","Kit").and(DBQuery.is("name",kitName ).and(DBQuery.is("active",true)) ));
		if ( null == kit ){
			contextValidation.addError("Erreurs catalogue", "Pas de kit actif nommé '"+kitName+"'");
		}
		return kit;
	}

	// ne controle pas correctement les balise manquantes ??!!
	private void checkMandatoryXMLTag ( ContextValidation contextValidation, String tagName, String tagValue){
        if ( tagValue.equals("NULL") ) {
        	 contextValidation.addError("Erreurs fichier","Balise <"+ tagName+"> incorrecte (NULL)");
         } else if ( tagValue.equals("") ) {
        	 contextValidation.addError("Erreurs fichier","Balise <"+ tagName+"> manquante ou non renseignée (vide)");
         }
	}

	/* Essai de vrai control des balises manquantes....NON fonctionne pas 
	private void checkMandatoryXMLTag2 ( ContextValidation contextValidation, Element root, XPath xpath, String expression){
		try{
			Node node = (Node) xpath.compile(expression);
			if (null == node ){
				contextValidation.addError("Erreurs fichier", "La balise "+ expression +" est manquante");
				contextValidation.addError("Erreurs fichier", "Le fichier n'est pas un fichier RunParameters");
				return;
			}

			String nodeValue = xpath.evaluate(expression, root);
			if ( nodeValue.equals("NULL") ) {
        		 contextValidation.addError("Erreurs fichier","Balise <"+ expression+"> incorrecte (NULL)");
       		} else if ( nodeValue.equals("") ) {
        		contextValidation.addError("Erreurs fichier","Balise <"+ expression+"> non renseignée");
       		}
  		} catch (XPathExpressionException e) {
	   	  	 // erreur de (String)xpath.evaluate("XX", root);=> erreur du programmeur
	    	 contextValidation.addError("Erreurs interne", "Probleme XPathExpressionException :" + e.getMessage() );
	    }  
		return;
	}
	*/

    private void importReagents(String kitCode, List<BoxCatalog> boxes, Element root, XPath xpath,  Experiment experiment, ContextValidation contextValidation) { 
		try {
			/* il faut trouver le node de niveau 1 "ConsumableInfo" qui contient lui meme des nodes enfants avec le meme nom !!!
			  https://stackoverflow.com/questions/31492904/xpath-to-select-list-of-nodes-in-java
		 	  https://stackoverflow.com/questions/51320827/how-can-i-retrieve-the-child-node-using-xpath-java
			  NodeList et Node => import  org.w3c.dom.*;
			  XPathExpression => import de javax.xml.xpath.*

			/ reagents a inserer
					<Type>FlowCell</Type>
		 			<Type>Reagent</Type>
         			<Type>Bufer</Type>
         			<Type>SampleTube</Type>
         			<Type>Lyo</Type>
			*/

            XPathExpression expr = xpath.compile("ConsumableInfo/ConsumableInfo");
            NodeList consumNodes = (NodeList) expr.evaluate(root, XPathConstants.NODESET);

			for (int i = 0; i < consumNodes.getLength(); i++) {
				ReagentNode rgNode = new ReagentNode();
				NodeList nodes=consumNodes.item(i).getChildNodes();

				for (int j = 0; j < nodes.getLength(); j++) {
					Node child = nodes.item(j);
					// https://developer.mozilla.org/fr/docs/Web/API/Node/nodeType  1= Element
					if ( child.getNodeType() == 1 ) {
						//System.out.format( "\n\t Node Name:[%s], Text[%s] ", child.getNodeName(), child.getTextContent() );
						//  stocker la valeur de certains nodes uniquement
						if (child.getNodeName().equals("SerialNumber") ){
							rgNode.properties.put("SerialNumber",child.getTextContent());
						} else if (child.getNodeName().equals("LotNumber") ){
							rgNode.properties.put("LotNumber",child.getTextContent());
						} else if  (child.getNodeName().equals("Type") ){
							rgNode.properties.put("Type",child.getTextContent());
						}
					}
				}
	
				importReagent (kitCode, boxes, rgNode, experiment, contextValidation);
			}
	     } catch (XPathExpressionException e) {
		    	 contextValidation.addError("Erreurs interne", e.getMessage());
		 }      
     }	  
	 
	private void importReagent (String kitCode, List<BoxCatalog> boxes, ReagentNode rgNode,  Experiment experiment, ContextValidation contextValidation) {

		ReagentUsed reagentUsed=new ReagentUsed(); 
		Boolean reagentFound=false;
		String rgNodeName = rgNode.properties.get("Type");
		//System.out.println("\n\t....importing reagent :" +	rgNodeName);

		// iterer sur la liste des boxes 
		for(BoxCatalog box : boxes){
			//System.out.println("\n\t\t>>>>>box name :" +box.name);

			// trouver les reagents dans ces boites
			List<ReagentCatalog> boxReagents = MongoDBDAO.find(InstanceConstants.REAGENT_CATALOG_COLL_NAME, ReagentCatalog.class, 
			                                                  DBQuery.is("category","Reagent").and(DBQuery.is("boxCatalogCode", box.code )) ).toList();
			if ( 0 == boxReagents.size()){
		   	 	//contextValidation.addError("Erreurs catalogue", "Aucun réactif trouvé dans la boite " + box.name);
				// pas critique... continuer avec les autres boites
		    	//return;
			} else {
				//System.out.println("\n\t\t>>> nbre reagents trouvés dans cette boite: "+ boxReagents.size() );
				// 1 seul reagent par boite ????? pas la peine de'utiliser "for"...
				for(ReagentCatalog boxReagent : boxReagents){
					// System.out.println("\n\t\t>>>>>>>>>>"+ boxReagent.name);

					if ( boxReagent.name.equals(rgNodeName )){
						//System.out.println("\n\t\t>>>>>>>>>> !! reagent NODE name "+ rgNodeName +" match with reagent in box !!" );
						reagentFound=true;
						addReagent (reagentUsed, kitCode, box, boxReagent, rgNode, experiment, contextValidation );

						return;
					}
				}
				// le reagent peut n'etre pas dans cette boite...
			}
		}
		
		if ( ! reagentFound) {
			// si aucune boîte trouvée => erreur 
			contextValidation.addError("Erreurs catalogue","Aucune boîte ne contient le réactif '"+rgNodeName+"'");
			return;
		}
	}

	private void addReagent(ReagentUsed reagentUsed, String kitCode, BoxCatalog box, ReagentCatalog boxReagent, ReagentNode rgNode, Experiment experiment, ContextValidation contextValidation ) {
		
		//System.out.println("\n\t\t>>> kitCode:"+ kitCode+ " boxCode:"+box.code + " reagentCode:"+ boxReagent.code );
		logger.debug("\nInserting : kitCode:"+ kitCode+ " boxCode:"+box.code + " reagentCode:"+ boxReagent.code );

		reagentUsed.kitCatalogCode = kitCode;       // code NGL du kit parent
		reagentUsed.boxCatalogCode = box.code;       // code NGL de la boîte parent
		reagentUsed.reagentCatalogCode = boxReagent.code;  // code NGL du reactif
		//reagentUsed.boxCode="XXX";               // barcode de la boîte parent... info non disponible dans le fichier
		// !!!! les codes doivent se terminer par "_" pour etre filtrables par la suite
		reagentUsed.code =rgNode.properties.get("SerialNumber") + "_" + rgNode.properties.get("LotNumber") + "_";
		//reagentUsed.description="TEST....";      // rien de pertinent a mettre ?????

		// il faudrait un try au cas ou l'ajout echoue ???????
		experiment.reagents.add(reagentUsed); 

		return;
	}

}
