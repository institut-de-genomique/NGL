package controllers.migration.cng;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
// pour test
import java.util.regex.Pattern;

import models.Constants;
import models.LimsCNGDAO;
import models.laboratory.common.instance.TraceInformation;
import models.laboratory.container.instance.Container;
import models.laboratory.container.instance.Content;
import models.laboratory.sample.instance.Sample;
import models.laboratory.sample.description.SampleType;
import models.utils.InstanceConstants;
import models.utils.InstanceHelpers;
import models.utils.dao.DAOException;
import models.utils.instance.ContainerHelper;

import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;
import org.mongojack.JacksonDBCollection;

import play.Logger;
import play.mvc.Result;
import play.api.modules.spring.Spring;
import play.mvc.Result;
import validation.ContextValidation;
import controllers.CommonController;
import fr.cea.ig.MongoDBDAO;

/* TODO: reformat with proper html tags
 * 1) samples importés avant 15/09/2015 avec typeCode='default-sample-cng' alors qu'il est connu dansla base source Solexa...
 *    => mettre les sampleTypeCode et sampleCategoryCode corrects
 * 2) depuis l'ajout de l'import des fichiers banque CNG, le sampleTypeCode des samples "IP" a changé
 *    => modifier "IP-sample" qui avait été positionné correctement (malgré le point 1) en "IP"
 * @author fdsantos
 * 21/03/2017
 */

public class MigrationSampleType extends  CommonController {
	
	private static final String SAMPLE_COLL_NAME_BCK = InstanceConstants.SAMPLE_COLL_NAME + "_BCK_032017";
	private static final String CONTAINER_COLL_NAME_BCK = InstanceConstants.CONTAINER_COLL_NAME + "_BCK_032017";
	protected static LimsCNGDAO limsServices= Spring.getBeanOfType(LimsCNGDAO.class);
	
	public static Result migration() {	
		
		JacksonDBCollection<Sample, String> samplesCollBck = MongoDBDAO.getCollection(SAMPLE_COLL_NAME_BCK, Sample.class);
		JacksonDBCollection<Sample, String> containersCollBck = MongoDBDAO.getCollection(CONTAINER_COLL_NAME_BCK, Sample.class);
		
		if ( (samplesCollBck.count() == 0) && (containersCollBck.count() == 0) ){
			// collections backup vide (inexistante ??)=> faire backup
			backUpCollections();
			
			Logger.info("Migration sampleTypeCodes début...");
			try {
				migrationMissingSampleTypeCode();
				migrationUpdateSampleTypeCodeIP();
			}
			catch(Exception e) {
				Logger.error(e.getMessage());
				// pour plus d'infos...
				e.printStackTrace();
				e.getCause().printStackTrace();	
			}
									
		} else {
			Logger.info("Migration sampleTypeCodes déjà effectuée !");
		}		
			
		Logger.info("Migration sampleTypeCode fin");
		
		// est affiché dans le naviguateur
		return ok("Migration sampleTypesCodes OK");
	}

	private static void backUpCollections() {
		Logger.info("\tCopie "+InstanceConstants.SAMPLE_COLL_NAME+" starts");
		MongoDBDAO.save(SAMPLE_COLL_NAME_BCK, MongoDBDAO.find(InstanceConstants.SAMPLE_COLL_NAME, Sample.class).toList());
		Logger.info("\tCopie "+InstanceConstants.SAMPLE_COLL_NAME+" ended");
	
		Logger.info("\tCopie "+InstanceConstants.CONTAINER_COLL_NAME+" starts");
		MongoDBDAO.save(CONTAINER_COLL_NAME_BCK, MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, Container.class).toList());
		Logger.info("\tCopie "+InstanceConstants.CONTAINER_COLL_NAME+" ended");
	}
	
    // OK !!!!
	private static void migrationMissingSampleTypeCode() throws DAOException{
		
		Logger.info("-1- samples avec typeCode n'ayant pas été correctement importé");
		//ContextValidation contextValidation=new ContextValidation(Constants.NGL_DATA_USER);
		
		// lister les samples dont le typeCode ="default-sample-cng"
		List<Sample> samples = MongoDBDAO.find(InstanceConstants.SAMPLE_COLL_NAME, Sample.class,DBQuery.is("typeCode","default-sample-cng")).toList();
		Logger.debug("Nb de samples a corriger : "+samples.size());
		
		// trouver les vrais typeCodes de tous les samples dans Solexa et stocker dans une map
		Map<String, String> results=limsServices.findOldSampleTypes();
		Logger.debug("Nb de old samples: "+results.size());
		
		for ( Sample samp:samples){		
			 // trouver le sample dans la map
			 if (results.containsKey(samp.code)) {
				 
				String typeCode=results.get(samp.code);
				//Logger.debug("sample barcode "+ samp.code + " trouvé. son type est: "+ typeCode ); 
				SampleType sampleType=null;

				try { 
				  sampleType = SampleType.find.findByCode( typeCode );
				 } catch (DAOException e) {
				 	Logger.debug("OOOOOOOOOOPS...",e);
				}
				
				if ( sampleType==null ) {
					///contextValidation.addErrors("code", "error.codeNotExist", samp.code, typeCode );
					Logger.debug("...typeCode pas trouvé:"+ typeCode +"");
				} else {
					//Logger.debug("...typeCode =>"+ sampleType.code+ " categoryCode=>"+ sampleType.category.code);
					MongoDBDAO.update(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, DBQuery.is("code",samp.code), 
						                                                                DBUpdate.set("typeCode", sampleType.code ).
						                                                                         set("categoryCode", sampleType.category.code ));
				}
			 }
		}
		Logger.info("-1- fini");
	}	
	 
   
  private static void migrationUpdateSampleTypeCodeIP() throws DAOException{
	  
	  Logger.info("-2- update 'IP-sample' SampleTypeCode");
	  // utilisation de cursor et stream() ?? 
	  //           .cursor.forEach(container -> {
	  //  .contents.stream().forEach(content -> {
	  
	  //lister les containers contenant au moins un contents avec sampleTypeCode 'IP-sample'
	   MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, Container.class, DBQuery.is("contents.sampleTypeCode", "IP-sample" ))
	      .cursor.forEach(container -> {
			   container.contents.stream()
				   .filter(content -> content.sampleTypeCode.equals("IP-sample"))
				   .forEach(content -> {
				    	//Logger.debug("container: "+ container.code + ", contents.sampleCode: " + content.sampleCode + ", sampleTypeCode:" + content.sampleTypeCode);
					    content.sampleTypeCode = "IP";
				   });
			MongoDBDAO.update(InstanceConstants.CONTAINER_COLL_NAME, container);	
		});
	   
	   Logger.info("-2- fini");
  }
	
}