package controllers.instruments.io.cng.cbotV2Alone;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import controllers.instruments.io.utils.AbstractInput;
import models.laboratory.common.instance.property.PropertyFileValue;
import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.experiment.instance.Experiment;
import models.laboratory.reagent.instance.ReagentUsed;
import validation.ContextValidation;

public class CbotV2AloneInput extends AbstractInput {
	
   /* FDS 06/01/207 Description du fichier a traiter: XML généré par Cbot II:
    <?xml version="1.0" encoding="utf-16"?>
    <RunData xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema">
    ....
    <ProtocolName>C:\Illumina\cBot\bin\Recipes\XXXXXXXXXXXXXXXXX </ProtocolName>
    <ExperimentType>PairedEnd</ExperimentType>
    <FlowCellID>FCBARCODE</FlowCellID>
    <RunFolderName>161004_CBOT-C_0003</RunFolderName>
    <TemplateID>STRIPBARCODE</TemplateID>
    <ReagentID>XXXXXXXXXX3</ReagentID>
    ....

	*/
	
	@Override
	public Experiment importFile(Experiment experiment,PropertyFileValue pfv, ContextValidation contextValidation) throws Exception {	
			
		 DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		 DocumentBuilder builder = factory.newDocumentBuilder();
	     
	     try {
//	    	 InputStream inputStream = new ByteArrayInputStream(pfv.value);
	    	 InputStream inputStream = new ByteArrayInputStream(pfv.byteValue());
	    	 // le fichier produit par Illumina n'est PAS en UTF-16 malgré l'entete <?xml version="1.0" encoding="utf-16"?>
	    	 // mais en UTF-8 !!! il faut donc remettre la valeur correcte
	    	 InputSource is = new InputSource(inputStream);
	    	 is.setEncoding("UTF-8");
	    	 //System.out.println("nom du fichier >>>" + pfv.fullname );
	 		 
	         Document xml = builder.parse(is);
	         
	     	 //optional, but recommended
	     	 //read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
	     	 xml.getDocumentElement().normalize();

	         Element root = xml.getDocumentElement();
	         XPathFactory xpf = XPathFactory.newInstance();
	         XPath xpath = xpf.newXPath();
	         
	         // barcode de Flowcell
	         String expression = "FlowCellID";
//	         String flowcellId = (String)xpath.evaluate(expression, root);  
	         String flowcellId = xpath.evaluate(expression, root);  
	         checkMandatoryXMLTag (contextValidation, expression, flowcellId );
 
	         // barcode de la plaque de reactifs
	         expression = "ReagentID";
//	         String reagentId = (String)xpath.evaluate(expression, root);
	         String reagentId = xpath.evaluate(expression, root);
	         checkMandatoryXMLTag (contextValidation, expression, reagentId );
	       
	         // barcode du strip
	         expression = "TemplateID";
//	         String stripId = (String)xpath.evaluate(expression, root);
	         String stripId = xpath.evaluate(expression, root);
	         checkMandatoryXMLTag (contextValidation, expression, stripId);            
	         
	         // nom du rum
	         expression = "RunFolderName";
//	         String runFolder = (String)xpath.evaluate(expression, root);
	         String runFolder = xpath.evaluate(expression, root);
	         checkMandatoryXMLTag (contextValidation, expression, runFolder );
	         
	         // nom du Protocol
	         expression = "ProtocolName";
//	         String protocol = (String)xpath.evaluate(expression, root);
	         String protocol = xpath.evaluate(expression, root);
	         checkMandatoryXMLTag (contextValidation, expression, protocol );
	         
	         // pour commentaire
	         expression = "RecipeVersion";
//	         String recipeVersion = (String)xpath.evaluate(expression, root);
	         String recipeVersion = xpath.evaluate(expression, root);
	         checkMandatoryXMLTag (contextValidation, expression, recipeVersion );
	         
	         // pour commentaire
	         expression = "ReagentVersion";
//	         String reagentVersion = (String)xpath.evaluate(expression, root);
	         String reagentVersion = xpath.evaluate(expression, root);
	         checkMandatoryXMLTag (contextValidation, expression, reagentVersion );
	         
	         //--------------------verifications   ----------------------
	         
	         //-1- vérifier s'il s'agit d'un fichier produit par la cbot choisie
		     // runFolder est de la forme :  DATE_CBOT_NUM
	         if ( runFolder.length() > 1 ) {
	        	 String[] runf = runFolder.split("_");  
	        	 String cbot = null;
		      
	        	 // le nom de la cBot est le 2eme element de runFolder
	        	 // dans NGL les noms de cbot n'ont pas "-",=> le supprimer
	        	 if ( (runf.length == 3 ) && ( runf[1].charAt(4) == '-' ) ){
	        		 //System.out.println("(1) cbot:" +runf[1]);
	        		 
	        		 StringBuilder sb = new StringBuilder(runf[1]);
	        		 sb.deleteCharAt(4);
	        		 cbot = sb.toString();
	        		 //System.out.println("(2) cbot:" + cbot);
	        		 
	        		 if (experiment.instrument.typeCode.equals("cBotV2"))
	        		 {
	        			// l'instrument est une cbot seule
	        			 if ( ! cbot.toUpperCase().equals(experiment.instrument.code.toUpperCase()) ) {
	        				 contextValidation.addError("Erreurs fichier", "Le fichier ne correspond pas à la cBot sélectionnée");
	        			 }
	        		 } else if (experiment.instrument.typeCode.equals("janus-and-cBotV2")) {
	        			 
	        			 //l'instrument code  est 'janus-and-cbotX' 
	        			 String[] janusAndCbot=experiment.instrument.code.split("-");
	        			 String realCbot=janusAndCbot[2];
	        			 //System.out.println("realcbot:" + realCbot);
	        			 if ( ! cbot.toUpperCase().equals(realCbot.toUpperCase()) ) {
	        				 contextValidation.addError("Erreurs fichier", "Le fichier ne correspond pas à la cBot sélectionnée");
	        			 }
	        		 }
	        		 
	        	 } else {
	        		 contextValidation.addError("Erreurs fichier", "'RunFolderName' incorrect");
	        	 }   
	         }
	         
	         //-2- s'il existe, verifier le barcode Flowcell 
	         if (flowcellId.length() > 0 ) {
	        	 if ( ! experiment.instrumentProperties.get("containerSupportCode").value.equals(flowcellId))  {
	        		 contextValidation.addError("Erreurs fichier", "Le barcode flowcell du fichier ne correspond pas à celui qui est déclaré");
	        	 }
			 }
	         
	         //-3- s'il existe, vérifier le barcode Strip 
	         // NGL-1141 le barcode Strip de l'experience n'est plus obligatoire et peut etre manquant
	         if ( null == experiment.instrumentProperties.get("stripCode").value ) {
        		 contextValidation.addError("Erreurs fichier", "Veuillez entrer un barcode de strip avant d'importer le fichier");
        		 
	         } else if ( (stripId.length() > 0 ) && ( ! experiment.instrumentProperties.get("stripCode").value.equals(stripId)) ){
				 contextValidation.addError("Erreurs fichier", "Le barcode strip du fichier ne correspond pas à celui qui est déclaré");
	         }
		      
		     if (contextValidation.hasErrors()){
		    	  return experiment;
		     }      

		     // récupérer le nom du fichier importé
		     experiment.instrumentProperties.put("cbotFile", new PropertySingleValue(pfv.fullname)); 
		      
		     /* infos illumina 27/01/2017 :
		     	L’identifiant d’une plaque cBot de clustering pour FC HiSeq4000 se termine par – PC6
		     	L’identifiant d’une plaque cBot de clustering pour FC HiSeqX se termine par – PC2
		     	L’identifiant d’une plaque cBot de rehyb pour une FC HiSeq 4000 se termine par -RH6
		     	L’identifiant d’une plaque cBot de rehyb pour une FC HiSeqX se termine par -RH2
		     */
		     
		     ReagentUsed reagent=new ReagentUsed();    
		     String reag[] = reagentId.split("-");
		     if ( reag.length != 2 ){
		    	 contextValidation.addError("Erreurs fichier","Barcode réactif '"+reagentId+ "' incorrect!!");
		     } else {
		    	 reagent.code=reagentId;  
		     
		    	 // TESTS !!!! modifier quand le catalogue sera correct !!
		    	 if  (reag[1].equals("PC6")) {
		    		 reagent.kitCatalogCode="TEST:HiSeq4000"; 
		    		 reagent.boxCatalogCode="TEST:HiSeq4000";
		    		 reagent.boxCode="";
		     
		    		 reagent.reagentCatalogCode="0B4B1Q3N8";// ????  HARDCODED reagentCatalogCode 0B4B1Q3N8= PE Cluster Plate V3

		    	 } else if (reag[1].equals("PC2")) {	
		    		 reagent.kitCatalogCode="TEST:HiSeqX"; 
		    		 reagent.boxCatalogCode="TEST:HiSeqX";
		    		 reagent.boxCode="";
		     
		    		 reagent.reagentCatalogCode="0B4B1Q3N8";// ???????  HARDCODED reagentCatalogCode 0B4B1Q3N8= PE Cluster Plate V3
		    	 
		    	 } else if (reag[1].equals("RH6")) {	
		    		 reagent.kitCatalogCode="TEST:RehybHiSeq4000"; 
		    		 reagent.boxCatalogCode="TEST:RehybHiSeq4000";
		    		 reagent.boxCode="";
		     
		    		 reagent.reagentCatalogCode="0B4B1Q3N8";// ???????  HARDCODED reagentCatalogCode 0B4B1Q3N8= PE Cluster Plate V3
		    	 
		    	 } else if (reag[1].equals("RH6")) {	
		    		 reagent.kitCatalogCode="TEST:RehybHiSeqX"; 
		    		 reagent.boxCatalogCode="TEST:RehybHiSeqX";
		    		 reagent.boxCode="";
		     
		    		 reagent.reagentCatalogCode="0B4B1Q3N8";// ???????  HARDCODED reagentCatalogCode 0B4B1Q3N8= PE Cluster Plate V3 
		    	 
		    	 } else {
		    		 // on fait quoi ???
		    		 contextValidation.addError("Erreurs fichier","Réactif '-"+ reag[1]+ "' non géré !!");
		    	 }
		     }
		     
		     reagent.description="Recipe version: "+ recipeVersion+"; Reagent version: " + reagentVersion;
		     experiment.reagents.add(reagent);
		     
	      } catch (SAXException e) {
	    	  contextValidation.addError("Erreurs fichier", "filchier XML incorrect (structure,encodage,...)");
	      } catch (IOException e) {
	    	  contextValidation.addError("Erreurs fichier", "IOException");
	      } catch (XPathExpressionException e) {
	    	  // erreur de (String)xpath.evaluate("XX", root);=> erreur du programmeur
	    	  contextValidation.addError("Erreurs fichier", "Probleme XPathExpressionException !!!");
	      }      
  
		  return experiment;
	}
	
	private void checkMandatoryXMLTag (ContextValidation contextValidation, String tagName, String tagValue){
		//if (null == tagValue )  {
		//	 contextValidation.addErrors("Erreurs fichier","Balise <"+ tagName+"> manquante");
		// } else 
		if ( tagValue.equals("NULL") ) {
			contextValidation.addError("Erreurs fichier","Balise <"+ tagName+"> incorrecte (NULL)");
		} else if ( tagValue.equals("") ) {
			contextValidation.addError("Erreurs fichier","Balise <"+ tagName+"> manquante ou non renseignée (vide)");
		}
	}
	
}
