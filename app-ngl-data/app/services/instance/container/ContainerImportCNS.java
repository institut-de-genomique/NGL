package services.instance.container;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.mongojack.DBQuery;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.ngl.NGLApplication;
import models.Constants;
import models.laboratory.common.instance.Comment;
import models.laboratory.common.instance.PropertyValue;
import models.laboratory.common.instance.State;
import models.laboratory.common.instance.TBoolean;
import models.laboratory.common.instance.Valuation;
import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.container.instance.Container;
import models.laboratory.container.instance.ContainerSupport;
import models.laboratory.container.instance.Content;
import models.laboratory.container.instance.QualityControlResult;
import models.laboratory.sample.instance.Sample;
import models.util.DataMappingCNS;
import models.utils.InstanceConstants;
import models.utils.InstanceHelpers;
import models.utils.dao.DAOException;
import models.utils.instance.ContainerHelper;
import models.utils.instance.ContainerSupportHelper;
import services.instance.AbstractImportDataCNS;
import validation.ContextValidation;

public abstract class ContainerImportCNS extends AbstractImportDataCNS {

	// Required because accessed from static method
	private static final play.Logger.ALogger logger = play.Logger.of(ContainerImportCNS.class);
	
	protected ContainerImportCNS(String name, NGLApplication app) {
		super(name, app);
	}

	public static void saveSampleFromContainer(ContextValidation contextError,List<Container> containers,String sqlContent) throws SQLException, DAOException {
		Sample sample      = null;
		Sample newSample   = null;
		String rootKeyName = null;
	
		List<Container> containersList = new ArrayList<>(containers);
		for (Container container : containersList) {
			List<Content> contents;
			if (sqlContent != null) {	
				contents = new ArrayList<>(limsServices.findContentsFromContainer(sqlContent,container.code));
			} else {
				contents = new ArrayList<>(container.contents);
			}

			for (Content content : contents) {
				/* Sample content not in MongoDB */
				if (!MongoDBDAO.checkObjectExistByCode(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, content.sampleCode)) {
					rootKeyName="sample["+content.sampleCode+"]";
					contextError.addKeyToRootKeyName(rootKeyName);
					
					sample = limsServices.findSampleToCreate(contextError,content.sampleCode);
	
					if (sample != null) {
//						newSample =(Sample) InstanceHelpers.save(InstanceConstants.SAMPLE_COLL_NAME,sample,contextError,true);
						newSample = InstanceHelpers.save(contextError,InstanceConstants.SAMPLE_COLL_NAME,sample,true);
						content.referenceCollab=newSample.referenceCollab;
						content.taxonCode = newSample.taxonCode;
						content.ncbiScientificName = newSample.ncbiScientificName;
						if(!contextError.hasErrors()){
							limsServices.updateMaterielLims(newSample, contextError);
						}
					}
					contextError.removeKeyFromRootKeyName(rootKeyName);
				} else {	
					/* Find sample in Mongodb */
					newSample = MongoDBDAO.findByCode(InstanceConstants.SAMPLE_COLL_NAME,Sample.class, content.sampleCode);
					content.referenceCollab=newSample.referenceCollab;
					content.taxonCode = newSample.taxonCode;
					content.ncbiScientificName = newSample.ncbiScientificName;
				}			
	
				rootKeyName="container["+container.code+"]";
				contextError.addKeyToRootKeyName(rootKeyName);
	
				/* Error : No sample, remove container from list to create */
				if (newSample == null) {
					containers.remove(container);
					contextError.addError("sample","error.codeNotExist", content.sampleCode);
				} else {
					/* From sample, add content in container */
					container.contents.remove(content);
					ContainerHelper.addContent(container, newSample, content);
				}
				contextError.removeKeyFromRootKeyName(rootKeyName);
			}
		}
	}

	/* *
	 * 
	 * Create containers, contents and samples from 2 sql queries 
	 * @param contextError
	 * @param sqlContainer
	 * @param containerCategoryCode
	 * @param containerStateCode
	 * @param experimentTypeCode
	 * @param sqlContent
	 * @throws SQLException
	 * @throws DAOException
	 */
	public static void createContainers(ContextValidation contextError, String sqlContainer,String containerCategoryCode,  String containerStateCode, String experimentTypeCode, String sqlContent) throws SQLException, DAOException{
		String rootKeyName = null;
	
		List<Container> containers = limsServices.findContainersToCreate(sqlContainer,contextError, containerCategoryCode,containerStateCode,experimentTypeCode);
	
		ContainerImportCNS.deleteContainerAndContainerSupport(containers);
		
		ContainerImportCNS.saveSampleFromContainer(contextError,containers,sqlContent);
		
		Map<String,PropertyValue> propertiesContainerSupports=new HashMap<>();
		Set<String> supportContainers = new HashSet<>();
		for (Container container : containers) {
			if (!propertiesContainerSupports.containsKey(container.support.code) && container.properties.get("sequencingProgramType") != null) {
				// propertiesContainerSupports.put(container.support.code, container.properties.get("sequencingProgramType"));
//				propertiesContainerSupports.put(container.support.code, (PropertyValue<String>)container.properties.get("sequencingProgramType"));
//				@SuppressWarnings("unchecked")
//				PropertyValue pvs = (PropertyValue)container.properties.get("sequencingProgramType");
				PropertyValue pvs = container.properties.get("sequencingProgramType");
				propertiesContainerSupports.put(container.support.code, pvs);
				container.properties.remove("sequencingProgramType");
			}
			supportContainers.add(container.support.code);
		}

		//List of other containers also in NGL associed to support to create 
		List<Container> containersSupportContainers = MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, Container.class,DBQuery.in("support.code", supportContainers)).toList();
		logger.debug("Nb container  support"+containersSupportContainers.size());
		containersSupportContainers.addAll(containers);
		
//		List<ContainerSupport> containerSupports=
				ContainerHelper.createSupportFromContainers(containersSupportContainers,propertiesContainerSupports, contextError);
	
		List<Container> newContainers = new ArrayList<>();
		
		for (Container container:containers) {
			//Logger.debug("Container :"+container.code+ "nb sample code"+container.sampleCodes.size());
			rootKeyName = "container[" + container.code + "]";
			contextError.addKeyToRootKeyName(rootKeyName);
//			Container result=(Container) InstanceHelpers.save(InstanceConstants.CONTAINER_COLL_NAME,container, contextError,true);
			Container result = InstanceHelpers.save(contextError,InstanceConstants.CONTAINER_COLL_NAME, container,true);
			if (result != null)
				newContainers.add(result);
			contextError.removeKeyFromRootKeyName(rootKeyName);
		}
		
		//Update traceInformation.creationDate
		//for(ContainerSupport cs:containerSupports){
			//MongoDBDAO.update(InstanceConstants.CONTAINER_COLL_NAME, Container.class,DBQuery.is("support.code", cs.code),DBUpdate.set("traceInformation.creationDate", cs.traceInformation.creationDate),true);
		//}

		/*
		if (experimentTypeCode.equals("solution-stock")) {
			// SQL
			ContainerImportCNS.createProcessFromContainers(containers,"illumina-run","pl_ProcessToNGL @matmanom=?",contextError);
		}
		*/
		limsServices.updateMaterielmanipLims(newContainers,contextError);
	
	}

	/*
	public static void createProcessFromContainers(List<Container> containers, String processTypeCode, String sql, ContextValidation contextError) {
		for(Container container:containers){
			List<Process> processes=limsServices.findProcessToCreate(sql, container,processTypeCode, contextError);
			Set<String> processCodes=new HashSet<String>();
			String rootKeyName=null;
			for(Process process:processes){
				Logger.debug("ContextError mode creation "+contextError.isCreationMode());
				//Logger.debug("Container :"+container.code+ "nb sample code"+container.sampleCodes.size());
				rootKeyName="process["+process.code+"]";
				contextError.addKeyToRootKeyName(rootKeyName);
				InstanceHelpers.save(InstanceConstants.PROCESS_COLL_NAME,process, contextError,true);
				contextError.removeKeyFromRootKeyName(rootKeyName);
				processCodes.add(process.code);
			}
		
			ProcessHelper.updateContainer(container,processTypeCode, processCodes,contextError);
			ProcessHelper.updateContainerSupportFromContainer(container,contextError);
			ProcessWorkflows.nextContainerStateFromNewProcesses(processes, processTypeCode, contextError);	
		}
	
	}
	 */
	private static void deleteContainerAndContainerSupport(List<Container> containers) {
		for(Container container : containers){
			//delete de tout les containers associés au support du container, alors les lanes supprimées dans le Lims seront supprimés dans NGL
			MongoDBDAO.delete(InstanceConstants.CONTAINER_COLL_NAME, Container.class, DBQuery.is("code", container.code));
			MongoDBDAO.deleteByCode(InstanceConstants.CONTAINER_SUPPORT_COLL_NAME, ContainerSupport.class, container.support.code);
		}
	}

	/* *
	 * 
	 * Create au niveau Container from a ResultSet
	 * 
	 * The resultset must return fields :code, project, sampleCode, comment, codeSupport, limsCode, receptionDate, mesuredConcentration, mesuredVolume, mesuredQuantity, indexBq, nbContainer
	 * 
	 * @param rs ResulSet from Query
	 * @param containerCategoryCode 
	 * @param containerStatecode
	 * @return
	 * @throws SQLException
	 * @throws DAOException 
	 */
	private static String getString(ResultSet rs, String name, String defaultValue) {
		try {
			String s = rs.getString("containerCategoryCode");
			if (s == null)
				return defaultValue;
			return s;
		} catch(SQLException e) {
			return defaultValue;
		}
	}
	public static TBoolean getTBoolean(ResultSet rs, String name, TBoolean defaultValue) {
		try {
			return TBoolean.valueOf(rs.getString(name));
		} catch(SQLException e) {
			return defaultValue;
		}
	}
	
	public static Container createContainerFromResultSet(ResultSet rs, String containerCategoryCode, String containerStatecode, String experimentTypeCode) throws SQLException, DAOException{

		Container container = new Container();
		container.traceInformation.setTraceInformation(Constants.NGL_DATA_USER);
		try {
			container.traceInformation.creationDate = rs.getDate("dc");
		} catch(SQLException e) {
		}
		container.code         = rs.getString("code");
		container.categoryCode = getString(rs, "containerCategoryCode", containerCategoryCode);
		container.comments     = new ArrayList<>();				
		container.comments.add(new Comment(rs.getString("comment"), "ngl-test"));
//		try {
//		container.categoryCode = rs.getString("containerCategoryCode");
//	} catch(SQLException e) {
//		container.categoryCode = containerCategoryCode;
//	}
		
		container.state      = new State(); 
		container.state.code = DataMappingCNS.getState(container.categoryCode, rs.getInt("etatLims"), experimentTypeCode);
		container.state.user = Constants.NGL_DATA_USER;
		container.state.date = new Date();

		container.valuation       = new Valuation();
		container.valuation.valid = getTBoolean(rs,"valide",TBoolean.UNSET);
//		try {
//			container.valuation.valid = TBoolean.valueOf(rs.getString("valide"));
//		} catch(SQLException e) {
//			container.valuation.valid = TBoolean.UNSET;
//		}
		
//		String storageCode = null;
//		try {
//			storageCode = rs.getString("storageCode");
//		} catch(SQLException e) {
//		}
		String storageCode = getString(rs, "storageCode", null);
		
		container.support = ContainerSupportHelper.getContainerSupport(container.categoryCode, rs.getInt("nbContainer"), rs.getString("codeSupport"), rs.getString("column"), rs.getString("line"),storageCode);

		container.properties = new HashMap<>();
		container.properties.put("limsCode",new PropertySingleValue(rs.getInt("limsCode")));
		if (rs.getString("sequencingProgramType") != null)
			container.properties.put("sequencingProgramType", new PropertySingleValue(rs.getString("sequencingProgramType")));
		// GA: pass by getDate but need migration on all the tube and plate importing from dblims
		if (rs.getString("receptionDate") != null) {
			// GS container.properties.put("receptionDate",new PropertySingleValue(rs.getString("receptionDate")));	
			container.properties.put("receptionDate",new PropertySingleValue(rs.getDate("receptionDate")));
		}

//		String mesuredConcentrationUnit = "ng/µl";
//		String mesuredSizeUnit          = "pb";
//
//		try {
//			if (rs.getString("measuredConcentrationUnit") != null) {
//				mesuredConcentrationUnit=rs.getString("measuredConcentrationUnit");
//			}
//		} catch(SQLException e){
//		}
//		
//		try {
//			if(rs.getString("measuredSizeUnit") != null) {
//				mesuredSizeUnit=rs.getString("measuredSizeUnit");
//			}
//		} catch(SQLException e) {
//		}
		String mesuredConcentrationUnit = getString(rs, "measuredConcentrationUnit", "ng/µl");
		String mesuredSizeUnit          = getString(rs, "measuredSizeUnit",          "pb");
		
		if (rs.getString("measuredConcentration") != null)
			container.concentration = new PropertySingleValue(Math.round(rs.getFloat("measuredConcentration")*100.0)/100.0, mesuredConcentrationUnit);
		if (rs.getString("measuredVolume") != null)
			container.volume = new PropertySingleValue(Math.round(rs.getFloat("measuredVolume")*100.0)/100.0, "µL");
		if (rs.getString("measuredQuantity") != null)
			container.quantity = new PropertySingleValue(Math.round(rs.getFloat("measuredQuantity")*100.0)/100.0, "ng");
		
		try {
			if (rs.getString("measuredSize") != null) {
			container.size = new PropertySingleValue(rs.getInt("measuredSize"), mesuredSizeUnit);
			}
		} catch(SQLException e) {	
		}
		// List<QualityControlResult> qualityControlResults = new ArrayList<QualityControlResult>();
		try {
			if (rs.getString("concentrationTypeCode") != null) {
				QualityControlResult qcConcentrationResult = new QualityControlResult();
				qcConcentrationResult.typeCode   = rs.getString("concentrationTypeCode");
				qcConcentrationResult.code=qcConcentrationResult.typeCode+"_"+container.code;
				qcConcentrationResult.properties = new HashMap<>(); // <String, PropertyValue>();
				qcConcentrationResult.properties.put("concentration1", container.concentration);
				qcConcentrationResult.date       = rs.getDate("concentrationDate");
				container.qualityControlResults.add(qcConcentrationResult);
			}
		} catch(SQLException e) {
		}
		try {
			if (rs.getString("sizeTypeCode") != null) {
				QualityControlResult qcSizeResult = new QualityControlResult();
				qcSizeResult.typeCode   = rs.getString("sizeTypeCode");
				qcSizeResult.code       = qcSizeResult.typeCode + "_" + container.code;
				qcSizeResult.properties = new HashMap<>(); // <String, PropertyValue>();
				qcSizeResult.properties.put("insertSize", container.size);
				qcSizeResult.date       = rs.getDate("sizeDate");
				container.qualityControlResults.add(qcSizeResult);
			}
		} catch(SQLException e) {
		}
		if (experimentTypeCode != null) {
			container.fromTransformationTypeCodes = new HashSet<>();
			container.fromTransformationTypeCodes.add(experimentTypeCode);	
		}
		container.projectCodes = new HashSet<>();					
		if (rs.getString("project") != null) 					
			container.projectCodes.add(rs.getString("project"));
		if (rs.getString("controlLane") != null)
			container.properties.put("controlLane",new PropertySingleValue(rs.getBoolean("controlLane")));
		container.sampleCodes=new HashSet<>();
		if (rs.getString("sampleCode") != null) {
			Content sampleUsed = new Content();
			sampleUsed.percentage = 100.0;
			sampleUsed.sampleCode = rs.getString("sampleCode");
			if (rs.getString("project") != null)					
				sampleUsed.projectCode = rs.getString("project");
			// GA: add projectCode
			// GA: replace by method in containerHelper who update sampleCodes from contents
			container.sampleCodes.add(rs.getString("sampleCode"));

			if (rs.getString("tag") != null) {
				sampleUsed.properties = new HashMap<>(); // <String, PropertyValue<?>>();
				sampleUsed.properties.put("tag",         new PropertySingleValue(rs.getString("tag")));
				sampleUsed.properties.put("tagCategory", new PropertySingleValue(rs.getString("tagCategory")));
			}
			
			if (rs.getString("libProcessTypeCode") != null) {
				sampleUsed.properties.put("libProcessTypeCode",new PropertySingleValue(rs.getString("libProcessTypeCode")));
			}
			
			if (rs.getString("libLayoutNominalLength") != null) {
				sampleUsed.properties.put("libLayoutNominalLength", new PropertySingleValue(rs.getInt("libLayoutNominalLength")));
			}
			
			if (rs.getString("sampleAliquoteCode") != null) {
				sampleUsed.properties.put("sampleAliquoteCode", new PropertySingleValue(rs.getString("sampleAliquoteCode")));
			}
			container.contents.add(sampleUsed);
		}
		return container;
	}

}
