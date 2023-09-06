package controllers.migration.cns;

import java.sql.SQLException;
import java.util.List;

import models.LimsCNSDAO;
import models.laboratory.common.description.Level;
import models.laboratory.container.instance.Container;
import models.laboratory.container.instance.Content;
import models.laboratory.sample.description.ImportType;
import models.laboratory.sample.description.SampleType;
import models.laboratory.sample.instance.Sample;
import models.utils.InstanceConstants;
import models.utils.InstanceHelpers;
import models.utils.dao.DAOException;

import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;

import play.Logger;
import play.Logger.ALogger;
import play.api.modules.spring.Spring;
import play.mvc.Result;
import validation.ContextValidation;
import controllers.CommonController;
import fr.cea.ig.MongoDBDAO;

public class MigrationContentPropertiesContainer   extends CommonController{

	protected static ALogger logger=Logger.of("MigrationContentPropertiesContainer");

	protected static LimsCNSDAO  limsServices = Spring.getBeanOfType(LimsCNSDAO.class);


	

	public static Result migration() {

		updateContentProperties();
		return ok("Migration Content Container Finish");
	}


	private static void updateContentProperties() {
		//Update Container prepaflowcell
		List<Container> containers = MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, Container.class,DBQuery.is("categoryCode", "lane")
				).toList();
		logger.info("Update "+containers.size()+" lanes");
		
		for(Container container:containers){
			logger.info("Treat "+container.code);
			try {
				ContextValidation contextValidation = new ContextValidation("ngl-data");
				
				List<Content> contents =limsServices.findContentsFromContainer("pl_BanquesolexaUneLane @nom_lane=?", container.code);
				for(Content content:contents){
						Sample sample=MongoDBDAO.findByCode(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, content.sampleCode);
						
						content.sampleCategoryCode = sample.categoryCode;
						content.sampleTypeCode = sample.typeCode;
						
						if(sample.importTypeCode !=null){

							InstanceHelpers.copyPropertyValueFromPropertiesDefinition(ImportType.find.findByCode(sample.importTypeCode).getPropertyDefinitionByLevel(Level.CODE.Content), sample.properties,content.properties);
						}
						if(sample.typeCode !=null){

							InstanceHelpers.copyPropertyValueFromPropertiesDefinition(SampleType.find.findByCode(sample.typeCode).getPropertyDefinitionByLevel(Level.CODE.Content), sample.properties, content.properties);
						}
						//content.validate(contextValidation);
				}

				if(contents!=null && contents.size()!=0 && !contextValidation.hasErrors()){
					MongoDBDAO.update(InstanceConstants.CONTAINER_COLL_NAME, Container.class
							,DBQuery.is("code",container.code)
							,DBUpdate.set("contents", contents));
				}else if(contextValidation.hasErrors()){
					contextValidation.displayErrors(logger);
				} else {
					logger.error("No content for container "+container.code);
				}
			} catch (SQLException e) {
				logger.error("Error Update content for container "+container.code);
			} catch (DAOException e) {
				logger.error("Erreur SQL");
			}
		}		 
	}

}
