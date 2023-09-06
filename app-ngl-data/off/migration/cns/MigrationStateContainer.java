package controllers.migration.cns;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import models.Constants;
import models.LimsCNSDAO;
import models.laboratory.common.description.Level;
import models.laboratory.common.description.ObjectType;
import models.laboratory.container.instance.Container;
import models.laboratory.container.instance.ContainerSupport;
import models.laboratory.container.instance.Content;
import models.laboratory.sample.description.ImportType;
import models.laboratory.sample.description.SampleType;
import models.laboratory.sample.instance.Sample;
import models.util.DataMappingCNS;
import models.utils.InstanceConstants;
import models.utils.InstanceHelpers;
import models.utils.dao.DAOException;

import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;
import org.springframework.jdbc.core.RowMapper;

import play.Logger;
import play.Logger.ALogger;
import play.api.modules.spring.Spring;
import play.mvc.Result;
import validation.ContextValidation;
import validation.container.instance.ContainerValidationHelper;
import controllers.CommonController;
import fr.cea.ig.MongoDBDAO;

public class MigrationStateContainer   extends CommonController{

	protected static ALogger logger=Logger.of("MigrationStateContainer");

	protected static LimsCNSDAO  limsServices = Spring.getBeanOfType(LimsCNSDAO.class);


	private static final String CONTAINER_COLL_NAME_BCK = InstanceConstants.CONTAINER_COLL_NAME+"_BCK_ContainerState";

	public static Result migration() {

		List<Container> containersCollBck = MongoDBDAO.find(CONTAINER_COLL_NAME_BCK, Container.class).toList();
		if(containersCollBck.size() == 0){

			Logger.info(">>>>>>>>>>> 1.a Update state Container starts");
			backupContainerCollection();
			// all container
			List<Container> containersList = MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, Container.class).toList();
			Logger.debug("Update state "+containersList.size()+" CONTAINERS");

			//update state container
			//updateStateContainerAndContainerSupport();

			//update sampleCodes in container
			/*for(Container container:containersList){
				List<String> sampleCodes=new ArrayList<String>();
				for(Content content : container.contents){
					InstanceHelpers.addCode(content.sampleCode, sampleCodes);
				}

				MongoDBDAO.update(InstanceConstants.CONTAINER_COLL_NAME, Container.class
						,DBQuery.is("code",container.code),DBUpdate.set("sampleCodes", sampleCodes));
			}*/

			//udpdate property container lane


			Logger.info(">>>>>>>>>>> 1.b Update State Container end");

		} else {
			Logger.info("Update State CONTAINER already execute !");
		}

		updateContentPropertyLibProcessTypeCode();

		List<Container> containers=MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, Container.class).toList();
		return ok("Migration Container "+ containers.size()+ "Finish");
	}


	private static void updateContentPropertyLibProcessTypeCode() {
		//Update Container prepaflowcell
		List<Container> containers = MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, Container.class,DBQuery.and(DBQuery.is("categoryCode", "lane"),DBQuery.notExists("contents.properties.taxonSize"))).toList();

		for(Container container:containers){
			try {
				List<Content> contents =limsServices.findContentsFromContainer("pl_BanquesolexaUneLane @nom_lane=?", container.code);

				for(Content content:contents){
						Sample sample=MongoDBDAO.findByCode(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, content.sampleCode);
						if(sample.importTypeCode !=null){

							InstanceHelpers.copyPropertyValueFromPropertiesDefinition(ImportType.find.findByCode(sample.importTypeCode).getPropertyDefinitionByLevel(Level.CODE.Content), sample.properties,content.properties);
						}
						if(sample.typeCode !=null){

							InstanceHelpers.copyPropertyValueFromPropertiesDefinition(SampleType.find.findByCode(sample.typeCode).getPropertyDefinitionByLevel(Level.CODE.Content), sample.properties,content.properties);
						}
					}


				if(contents!=null && contents.size()!=0){
					MongoDBDAO.update(InstanceConstants.CONTAINER_COLL_NAME, Container.class
							,DBQuery.is("code",container.code)
							,DBUpdate.set("contents", contents));
				}else {
					Logger.error("No content for container "+container.code);
				}
			} catch (SQLException e) {
				Logger.error("Error Update content for container "+container.code);
			} catch (DAOException e) {
				Logger.error("Erreur SQL");
			}
		}		 
	}

	public static void updateStateContainerAndContainerSupport() {

		class StateContainerSupport{
			public Integer stateCode;
			public String containerSupportCode;
			public String containerCategoryCode;
			public StateContainerSupport(){
			}
		}

		String sql="select containerCategoryCode='tube',stateCode=etubco, containerSupportCode=tubnom from Tubeident t, Materielmanip m where m.matmaco=t.matmaco and m.matmaInNGL!=null " +
				"union " +
				"select  containerCategoryCode='lane',stateCode=ematerielco , containerSupportCode=lotrearef from Prepaflowcell p, Materielmanip m, Lotreactif l where p.matmaco=m.matmaco and m.matmaInNGL!=null and l.lotreaco=p.lotreaco ";

		Logger.debug("SQL "+sql);
		List<StateContainerSupport> results = limsServices.jdbcTemplate.query(sql,new Object[]{} 
		,new RowMapper<StateContainerSupport>() {

			@SuppressWarnings("rawtypes")
			public StateContainerSupport mapRow(ResultSet rs, int rowNum) throws SQLException {
				StateContainerSupport stateContainer = new StateContainerSupport();
				stateContainer.stateCode=rs.getInt("stateCode");
				stateContainer.containerSupportCode=rs.getString("containerSupportCode");
				stateContainer.containerCategoryCode=rs.getString("containerCategoryCode");				
				return stateContainer;
			}
		});

		ContextValidation contextValidation =new ContextValidation(Constants.NGL_DATA_USER);

		for(StateContainerSupport stateContainerSupport:results){
			String newStateCode=DataMappingCNS.getState(stateContainerSupport.containerCategoryCode, stateContainerSupport.stateCode,null);
			Logger.debug("New state "+newStateCode+" for ContainerSupport "+stateContainerSupport.containerSupportCode);
			ContainerValidationHelper.validateStateCode(newStateCode, contextValidation);

			if(!contextValidation.hasErrors()){
				MongoDBDAO.update(InstanceConstants.CONTAINER_SUPPORT_COLL_NAME,ContainerSupport.class
						,DBQuery.is("code",stateContainerSupport.containerSupportCode),DBUpdate.set("state.code",newStateCode));
				MongoDBDAO.update(InstanceConstants.CONTAINER_COLL_NAME,Container.class
						,DBQuery.is("support.code",stateContainerSupport.containerSupportCode),DBUpdate.set("state.code",newStateCode),true);
			}
		}

		contextValidation.displayErrors(logger);

	}

	private static void backupContainerCollection() {
		Logger.info("\tCopie "+InstanceConstants.CONTAINER_COLL_NAME+" start");
		MongoDBDAO.save(CONTAINER_COLL_NAME_BCK, MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, Container.class).toList());
		Logger.info("\tCopie "+InstanceConstants.CONTAINER_COLL_NAME+" end");
	}



}
