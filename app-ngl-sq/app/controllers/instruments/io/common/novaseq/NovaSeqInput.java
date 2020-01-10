package controllers.instruments.io.common.novaseq;

// import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
// import java.io.InputStreamReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
// import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory; 

import org.mongojack.DBQuery;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

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

public class NovaSeqInput extends AbstractInput {
	
//	private static final play.Logger.ALogger logger = play.Logger.of(NovaSeqInput.class);
	
	/* F. Dos Santos NGL-1769: Dépôt NovaSeq : import fichier XML
       Description du fichier à traiter:
      
		<?xml version="1.0"?>
		<RunParameters xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
		  <Surface>Both</Surface>
		  <ReadType>PairedEnd</ReadType>
		  <Side>A</Side>
		  <Read1NumberOfCycles>151</Read1NumberOfCycles>
		  <Read2NumberOfCycles>151</Read2NumberOfCycles>
		  <IndexRead1NumberOfCycles>8</IndexRead1NumberOfCycles>
		  <IndexRead2NumberOfCycles>8</IndexRead2NumberOfCycles>
		  <PlannedRead1Cycles>151</PlannedRead1Cycles>
		  <PlannedRead2Cycles>151</PlannedRead2Cycles>
		  <PlannedIndex1ReadCycles>8</PlannedIndex1ReadCycles>
		  <PlannedIndex2ReadCycles>8</PlannedIndex2ReadCycles>
		  <RunNumber>7</RunNumber>
		  <RtaVersion>v3.3.3</RtaVersion>
		  <RecipeVersion>1.2.0</RecipeVersion>
		  <ExperimentName>Test_Formation_S2</ExperimentName>
		  <RfidsInfo>
		    <FlowCellSerialBarcode>H5VJ2DMXX</FlowCellSerialBarcode>
		    <FlowCellPartNumber>A</FlowCellPartNumber>
		    <FlowCellLotNumber>20209948</FlowCellLotNumber>
		    ...
		  </RfidsInfo>
		  ...
		  <RunId>180115_MARIECURIX_0008_AH5TCYDMXX</RunId>
		  ...  
		</RunParameters
	*/	
	
	@Override
	// suppresson throws Exception
	public Experiment importFile(Experiment experiment, PropertyFileValue pfv, ContextValidation contextValidation) {	
		
	     try {
	    	 DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	    	 DocumentBuilder builder = factory.newDocumentBuilder();  /// c'est ici que des erreur de balises peuvent etre vues
	    	 InputStream inputStream = new ByteArrayInputStream(pfv.byteValue());
	    	 InputSource is = new InputSource(inputStream);
	    	 is.setEncoding("UTF-8");
	    	 logger.debug("import fichier >>>" + pfv.fullname );
	 		 
	         Document xml = builder.parse(is);
	         
	     	 //optional, but recommended
	     	 //read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
	     	 xml.getDocumentElement().normalize();

	         Element root = xml.getDocumentElement();
	         XPathFactory xpf = XPathFactory.newInstance();
	         XPath xpath = xpf.newXPath();
	           
		     // verifier le node RfidsInfo avant de faire le reste...
		     String expression="RfidsInfo";
		     String info = xpath.evaluate(expression, root);
		     checkMandatoryXMLTag (contextValidation, expression, info); 
		     if (contextValidation.hasErrors())
		    	 return experiment;
		     
	         checkConsistancy(root, xpath, experiment, contextValidation);
	         importReagents(root, xpath, experiment, contextValidation);
  
	      } catch (SAXException | ParserConfigurationException e) {
	    	  contextValidation.addError("Erreurs fichier", "fichier XML incorrect :" + e.getMessage());
	      } catch (IOException e) {
	    	  contextValidation.addError("Erreurs fichier", e.getMessage());
	      } catch (XPathExpressionException  e) {
	    	  contextValidation.addError("Erreurs interne", e.getMessage());
	      } 
		  return experiment;
	}
	        
    private void checkConsistancy (Element root, XPath xpath, Experiment experiment, ContextValidation contextValidation) {
         // verifier données identiques entre fichier XML et infos expérience 
    	 
    	 try {
		     String expression=null;
		     
		     //-1- position sur sequenceur: NIVEAU 1
		     expression = "Side";
		     String side = xpath.evaluate(expression, root);
		     checkMandatoryXMLTag (contextValidation, expression, side); 
		     // si la balise OK comparer avec  position
		     if ((side.length() > 0) && (! experiment.instrumentProperties.get("position").value.equals(side))) {
		    		 contextValidation.addError("Erreurs fichier", "La position '"+side+"' du fichier ne correspond pas à celle de l'expérience.");
			 } 
		     
		     //-2- barcode de Flowcell  NIVEAU 2=><RfidsInfo>
		     expression = "RfidsInfo/FlowCellSerialBarcode";
		     String flowcellId = xpath.evaluate(expression, root);  
		     checkMandatoryXMLTag (contextValidation, expression, flowcellId );
		     // si la balise OK comparer avec inputContainerSupportCodes: <BARCODE>_<POS>
		     if ((flowcellId.length() > 0) && (! experiment.inputContainerSupportCodes.contains(flowcellId))) {
		    		 contextValidation.addError("Erreurs fichier", "Le barcode flowcell du fichier '"+flowcellId+"' ne correspond pas à celui de l'expérience.");
			 }
		 
			 // -3- barcode du tube de chargement  NIVEAU 2=><RfidsInfo>
		     // !!! pour l'instant il n'y a qu'un seul tube... mais ca va changer avec l'utilisation d'un support de chargement multi-lanes....
		     expression="RfidsInfo/LibraryTubeSerialBarcode";
		     String tubeId = xpath.evaluate(expression, root);
		     checkMandatoryXMLTag (contextValidation, expression, tubeId );
		     // si la balise OK comparer avec novaseqLoadingTube
		     // !! pas obligatoire dans l'experience...et peut etre manquant  
		     if (null == experiment.instrumentProperties.get("novaseqLoadingTube") || null == experiment.instrumentProperties.get("novaseqLoadingTube").value) {
		    	 logger.debug("novaseqLoadingTube= null !!");
		    	 // si on importe le fichier a l'etat terminé on ne peut plus renseigner 'Tube chargement (RFID)' !!!
				 ///contextValidation.addErrors("Erreurs expérience", "Veuillez renseigner 'Tube chargement (RFID)' avant d'importer le fichier.");
				 
		     } else if ((tubeId.length() > 0 ) && ( ! experiment.instrumentProperties.get("novaseqLoadingTube").value.equals(tubeId)) ){
				 contextValidation.addError("Erreurs fichier", "Le tube de chargement '"+tubeId+"' du fichier ne correspond pas à celui de l'expérience.");
		     }
		     
		     //-4- flowcell mode    NIVEAU 2=><RfidsInfo>
		     expression="RfidsInfo/FlowCellMode";
		     String fcMode = xpath.evaluate(expression, root);
		     checkMandatoryXMLTag (contextValidation, expression, fcMode );
		     // si la balise OK comparer avec novaseqFlowcellMode
		     // !! pas obligatoire dans l'experience...et peut etre manquant
		     if (null == experiment.instrumentProperties.get("novaseqFlowcellMode") || null == experiment.instrumentProperties.get("novaseqFlowcellMode").value ) {
		    	 logger.debug("novaseqFlowcellMode= null !!");
		    	 // si on importe le fichier a l'etat terminé on ne peut plus renseigner 'type de flowcell' !!!
				 ///contextValidation.addErrors("Erreurs expérience", "Veuillez renseigner 'type de flowcell' avant d'importer le fichier.");
				 
		     } else if ((fcMode.length() > 0 ) && ( ! experiment.instrumentProperties.get("novaseqFlowcellMode").value.equals(fcMode)) ){
				 contextValidation.addError("Erreurs fichier", "Le type de flowcell '"+ fcMode+"' du fichier ne correspond pas à celui de l'expérience.");
		     }
		     
		     //-5- vérifier le nom de la machine ??? 
		     /* Info Illumina: la balise <InstrumentName> contient toujour l'ID du sequenceur mais la balise <RunId> peut etre parametree pour que le nom
		      * de run contient le nom du PC : <RunId>180115_MARIECURIX_0008_AH5TCYDMXX</RunId>
		      */
		     expression = "RunId";
		     String run = xpath.evaluate(expression, root);
		     checkMandatoryXMLTag (contextValidation, expression, run);
		     if ((run.length() > 0) && (! run.contains(experiment.instrument.code))) {
	    		 contextValidation.addError("Erreurs fichier", "L'instrument dans le nom du run du fichier ne correspond pas à l'instrument de l'expérience.");
		     }
		     
	     } catch (XPathExpressionException e) {
	   	  	 // erreur de (String)xpath.evaluate("XX", root);=> erreur du programmeur
	    	 contextValidation.addError("Erreurs interne", "Probleme XPathExpressionException :" + e.getMessage() );
	     }           
     }
	      
     private void importReagents (Element root, XPath xpath,  Experiment experiment, ContextValidation contextValidation) {
         /*  barcodes reagents a inserer
		 <FlowCellSerialBarcode>1234DMXX
         <FlowCellLotNumber>20209948
         <LibraryTubeSerialBarcode>NV0017846-LIB
         <LibraryTubeLotNumber>1000001046
         <SbsSerialBarcode>NV2061137-RGSBS
         <SbsLotNumber>20200578
         <ClusterSerialBarcode>NV2049937-RGCPE
         <ClusterLotNumber>20191106
         <BufferSerialBarcode>NV2065139-BUFFR
         <BufferLotNumber>20207676
         */
    	 
    	 // PB on n'a pas le nom des reagents => harcoder relation balise<-->nom reactif/nom boite    	 
    	 Map<String,String[]> reagentTagMap = new HashMap<>(0); 
    	 
    	 // 12/02/2018 !!! 
    	 // il y a 1 boite pour chaque reactif, le nom des reactif n'est pas assez specifique=> trouver d'abord le code de la boite
    	 // et ensuite trouver le reactif qui appartient a cette boite
    	 // il y a un kit par type de flowcell !!!==> utiliser la balise "RfidsInfo/FlowCellMode"
    	 String kitName=null;
    	 
    	 try {
		     String expression = "RfidsInfo/FlowCellMode";
		     String fcMode = xpath.evaluate(expression, root);

	        if  ("S1".equals(fcMode)) {  
	    	  /* SUPSQCNG-607 / NGL-2199
	    	     Pour info,pour le S1 :
	    	     Nom kit : NovaSeq 6000 S1 Rgt kit (300c)
	    		 contient ces boîtes :
	    		 S1 Flow Cell
	    		 S1/S2 Buffer Cartridge
	    		 S1 Cluster Cartridge
	    		 S1/S2 SBS Cartdridge 300 cycles
	    		 NovaSeq 5000/6000 Library Tube
	    	  */
		       kitName="NovaSeq 6000 S1 Rgt kit (300c)";
		       //String boxName="BOX NOVASEQ 6000";
		       //                 TAG name                   reagent name  box name
		       reagentTagMap.put("FlowCell",   new String[]{"FlowCell",   "S1 Flow Cell"}); 
		       reagentTagMap.put("LibraryTube",new String[]{"LibraryTube","NovaSeq 5000/6000 Library Tube"});
		       reagentTagMap.put("Sbs",        new String[]{"Sbs",        "S1/S2 SBS Cartdridge 300 cycles"});
		       reagentTagMap.put("Cluster",    new String[]{"Cluster",    "S1 Cluster Cartridge"});
		       reagentTagMap.put("Buffer",     new String[]{"Buffer" ,    "S1/S2 Buffer Cartridge"});
		       
	       } else if ("S2".equals(fcMode)) {
		      kitName="NovaSeq 6000 S2 Rgt kit (300c)";
		      //String boxName="BOX NOVASEQ 6000";
		      //                 TAG name                   reagent name  box name
		      reagentTagMap.put("FlowCell",   new String[]{"FlowCell",   "S2 Flow Cell"}); 
		      reagentTagMap.put("LibraryTube",new String[]{"LibraryTube","NovaSeq 5000/6000 Library Tube"});
		      reagentTagMap.put("Sbs",        new String[]{"Sbs",        "S1/S2 SBS Cartdridge 300 cycles"});
		      reagentTagMap.put("Cluster",    new String[]{"Cluster",    "S2 Cluster Cartridge"});
		      reagentTagMap.put("Buffer",     new String[]{"Buffer" ,    "S1/S2 Buffer Cartridge"});
		    	 
		   } else if  ("S4".equals(fcMode)) { 
		      /* SUPSQCNG-607 / NGL-2199
		         Pour info,pour le S4 :
		    	 Nom kit : NovaSeq 6000 S4 Rgt kit (300c)
		    	 contient ces boîtes :
		    	 S4 Flow Cell
		    	 S4 Buffer Cartridge
		    	 S4 Cluster Cartridge
		    	 S4 SBS Cartdridge 300 cycles
		    	 NovaSeq 5000/6000 Library Tube
		      */
			   kitName="NovaSeq 6000 S4 Rgt kit (300c)";
			   //String boxName="BOX NOVASEQ 6000";
			   //                 TAG name                   reagent name  box name
			   reagentTagMap.put("FlowCell",   new String[]{"FlowCell",   "S4 Flow Cell"}); 
			   reagentTagMap.put("LibraryTube",new String[]{"LibraryTube","NovaSeq 5000/6000 Library Tube"});
			   reagentTagMap.put("Sbs",        new String[]{"Sbs",        "S4 SBS Cartdridge 300 cycles"});
			   reagentTagMap.put("Cluster",    new String[]{"Cluster",    "S4 Cluster Cartridge"});
			   reagentTagMap.put("Buffer",     new String[]{"Buffer" ,    "S4 Buffer Cartridge"});
			   
		   } else if  ("SP".equals(fcMode)) { 
			   /* 26/08/2019: NGL-2628 
				  noms EXACTS donnés par Céline Besse :                                                         remarques FDS
				  Nom kit : NovaSeq 6000 SP Rgt kit (300c)
				  Nom boîte : 'SP Flow Cell' / Nom du réactif : 'Flow Cell'                            !!! blanc qui n'existe pas pour autres kits !!!
				  Nom boîte : 'SP/S1/S2 Buffer Cartridge' / Nom du réactif : 'Buffer'
				  Nom boîte : 'SP Cluster Cartridge' / Nom du réactif : 'Cluster'
				  Nom boîte : 'SP/S1/S2 SBS Cartridge 300 cycles' / Nom du réactif : 'SBS'             !!! SBS en majuscules !!!!
				  Nom boîte : 'NovaSeq 5000/6000 Library Tube' / Nom du réactif : 'Library Tube'       !!! blanc qui n'existe pas pour autres kits !!!
			    */
			
				kitName="NovaSeq 6000 SP Rgt kit (300c)";
				//String boxName="BOX NOVASEQ 6000";
				//                 TAG name                   reagent name,   box name
				reagentTagMap.put("FlowCell",   new String[]{"Flow Cell",   "SP Flow Cell"}); 
				reagentTagMap.put("LibraryTube",new String[]{"Library Tube","NovaSeq 5000/6000 Library Tube"});
				reagentTagMap.put("Sbs",        new String[]{"SBS",        "SP/S1/S2 SBS Cartridge 300 cycles"});
				reagentTagMap.put("Cluster",    new String[]{"Cluster",    "SP Cluster Cartridge"});
				reagentTagMap.put("Buffer",     new String[]{"Buffer" ,    "SP/S1/S2 Buffer Cartridge"});
				
		     } else {
		    	 // il y aurait encore le S3 a gerer ???
		    	 contextValidation.addError("Erreurs fichier", "FlowCell mode '"+fcMode+"' non géré.");
		    	 return;
		     }    	 
	    	 
	    	 //REM: l'association a l'expérience de dépot n'est pas vérifiée...elle est implicite
	    	 KitCatalog kit= MongoDBDAO.findOne(InstanceConstants.REAGENT_CATALOG_COLL_NAME, KitCatalog.class, DBQuery.is("category","Kit").and(DBQuery.is("name",kitName ).and(DBQuery.is("active",true))));
	    	 if ( null == kit ){
	    		 contextValidation.addError("Erreurs catalogue", "Pas de kit actif nommé '"+kitName+"' dans le catalogue");
	    		 return;
	    	 }
    	
	    	 // 12/02/2018 pair.getValue()  est maintenant un tableau de String
	    	 for (Map.Entry<String, String[]> pair : reagentTagMap.entrySet() ) {
	    		 
	             //Logger.debug(">>>>"+ pair.getKey() + "|"+ pair.getValue()[0] +"/"+  pair.getValue()[1] ); 
	             
	    		 // vérifier si la boite existe; ajouter contrainte sur kit parent !!
	    		 String boxName=pair.getValue()[1];
	    		 
		    	 BoxCatalog box= MongoDBDAO.findOne(InstanceConstants.REAGENT_CATALOG_COLL_NAME, BoxCatalog.class, DBQuery.is("category","Box").and(DBQuery.is("name",boxName ).and(DBQuery.is("active",true)).and(DBQuery.is("kitCatalogCode", kit.code ))));
		    	 if ( null == box ){
		    		 contextValidation.addError("Erreurs catalogue", "Pas de boîte active nommée '"+boxName+"' dans le kit '"+kitName +"'");
		    		 return;
		    	 } 
	    		 
		    	 ReagentUsed reagent=new ReagentUsed(); 
	    		 
	    		 String XMLtag1 = "RfidsInfo/" + pair.getKey() + "SerialBarcode";
	    		 //Logger.debug("XMLtag:"+XMLtag1); 
	    		 String serialBarcode = xpath.evaluate(XMLtag1, root);
	    		 checkMandatoryXMLTag (contextValidation, XMLtag1, serialBarcode );
	    		 
	    		 String XMLtag2 = "RfidsInfo/" + pair.getKey() + "LotNumber";
	      		 //Logger.debug("XMLtag:"+XMLtag2);
	    		 String lotNumber = xpath.evaluate(XMLtag2, root);
	    		 checkMandatoryXMLTag (contextValidation, XMLtag2, lotNumber );
	    		 
	    		 String reagName = pair.getValue()[0];
	    		 //Logger.debug("création reagent used: "+ reagName);
	    		 
	    		 // 12/02/2018 ajouter la contrainte sur la boite parent
	    		 ReagentCatalog reag= MongoDBDAO.findOne(InstanceConstants.REAGENT_CATALOG_COLL_NAME, ReagentCatalog.class, DBQuery.is("category", "Reagent").
	    				 and(DBQuery.is("name", reagName)).and(DBQuery.is("boxCatalogCode", box.code ))); 		 
	    		 if (reag == null) {
	        		 contextValidation.addError("Erreurs catalogue", "Pas de réactif nommé '"+reagName +"' dans le catalogue");
	        	 } else {
		    		 logger.debug("code=" + reag.code);
	        		 
		    		 reagent.kitCatalogCode = kit.code;       // code NGL du kit parent
		    		 reagent.boxCatalogCode = box.code;       // code NGL de la boîte parent
		    		 reagent.reagentCatalogCode = reag.code;  // code NGL du reactif
		    		 //reagent.boxCode="X XX";               // barcode de la boîte parent... info non disponible dans le fichier
		    		 reagent.code = serialBarcode + "_" + lotNumber + "_"; // !!!! les codes doivent se terminer par "_" pour etre filtrables par la suite
		    		 //reagent.description="TEST....";      // rien de pertinent a mettre ?????
					 experiment.reagents.add(reagent); 
	        	 }
	    	 }
	     } catch (XPathExpressionException e) {
		    	 contextValidation.addError("Erreurs interne", e.getMessage());
		 }      
     }	        

	private void checkMandatoryXMLTag ( ContextValidation contextValidation, String tagName, String tagValue){
        if ( tagValue.equals("NULL") ) {
        	 contextValidation.addError("Erreurs fichier","Balise <"+ tagName+"> incorrecte (NULL)");
         } else if ( tagValue.equals("") ) {
        	 contextValidation.addError("Erreurs fichier","Balise <"+ tagName+"> manquante ou non renseignée (vide)");
         }
	}
	
}
