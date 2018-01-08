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

import javax.sql.DataSource;

import models.LimsGETDAO;
import models.laboratory.common.instance.Comment;
import models.laboratory.common.instance.PropertyValue;
import models.laboratory.common.instance.State;
import models.laboratory.common.instance.TBoolean;
import models.laboratory.common.instance.TraceInformation;
import models.laboratory.common.instance.Valuation;
import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.container.instance.Container;
import models.laboratory.container.instance.ContainerSupport;
import models.laboratory.container.instance.Content;
import models.laboratory.container.instance.LocationOnContainerSupport;
import models.laboratory.container.instance.StorageHistory;
import models.laboratory.run.instance.Run;
import models.laboratory.sample.instance.Sample;
import models.util.DataMappingGET;
import models.utils.InstanceConstants;
import models.utils.InstanceHelpers;
import models.utils.dao.DAOException;
import models.utils.instance.ContainerHelper;
import models.utils.instance.ContainerSupportHelper;

import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;

import controllers.MongoCommonController;
import play.Logger;
import scala.concurrent.duration.FiniteDuration;
import services.instance.AbstractImportDataGET;
import validation.ContextValidation;
import fr.cea.ig.MongoDBDAO;

public abstract class ContainerImportGET extends AbstractImportDataGET {
	
	public static Comment updateComment = new Comment("Container est mis à jour", InstanceHelpers.getUser());
	
	public ContainerImportGET(String name,FiniteDuration durationFromStart,
			FiniteDuration durationFromNextIteration) {
		super(name,durationFromStart, durationFromNextIteration);
	}

	public static Boolean saveSampleFromContainer(ContextValidation contextError,Container container,String sqlContent, Container containerDB) throws SQLException, DAOException{
	
		Sample sample =null; //instance before creation
		Sample newSample =null; //sample in Mongo
		List<String> refLibraryProjectCodes = null; // list of projects from reference library in e-SiToul
		String rootKeyName=null;
			
			//récuperer des contents
			List<Content> contents;
			Logger.debug("ContainerImportGET - saveSampleFromContainer - container.code =  " + container.code);
			
			//si sql pour ce type de container
			if(sqlContent!=null){	
				Logger.debug("ContainerImportGET - saveSampleFromContainer - sqlContent n'est pas null pour container.code =  " + container.code);
				container.contents.clear();
				contents = new ArrayList<Content>(limsServices.findContentsFromContainer(sqlContent,container.code));	
			}else{
				Logger.debug("ContainerImportGET - saveSampleFromContainer - qlContent est null pour container.code =  " + container.code);
				contents = new ArrayList<Content>(container.contents);
			}	
			
			Logger.debug("ContainerImportGET - saveSampleFromContainer - " + contents.size());
							
			for(Content smplUsd : contents){
				//si l'un des contents n'a pas tout les infos requis - stop
				if(smplUsd.properties.isEmpty()){
					Logger.error("Container " + container.code + " n'a pas été importé");
					return false;
				//si l'index non renceigné - assigner valeur "NoIndex"
				}else if(smplUsd.properties.get("Nom_echantillon_collaborateur") == null){
					Logger.error("Container " + container.code + " n'a pas été importé, car " + smplUsd.sampleCode +" n'a pas de Nom_echantillon_collaborateur");
					return false;
				//si l'index non renceigné - assigner valeur "NoIndex"
				}else if(smplUsd.properties.get("tag") == null){
					smplUsd.properties.put("tag", new PropertySingleValue("NoIndex"));
					Logger.debug("ContainerImportGET saveSampleFromContainer Content " + smplUsd.sampleCode +" n'a pas d'index");
				}
			}
							
				container.sampleCodes.clear();
				for(Content sampleUsed : contents){
					
					Logger.debug("ContainerImportGET - saveSampleFromContainer - /.sampleCode =  " + sampleUsed.sampleCode);
					/* Sample content not in MongoDB */
					if(!MongoDBDAO.checkObjectExistByCode(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, sampleUsed.sampleCode)){
						
						Logger.debug("ContainerImportGET - saveSampleFromContainer - "+ sampleUsed.sampleCode +" n'existe pas dans mongoDB");
						rootKeyName="sample["+sampleUsed.sampleCode+"]";
						contextError.addKeyToRootKeyName(rootKeyName);
						
						Logger.debug("ContainerImportGET - saveSampleFromContainer - avant limsServices.findSampleToCreate  " + sampleUsed.sampleCode);
						sample = limsServices.findSampleToCreate(contextError,sampleUsed.sampleCode);
						
						Logger.debug("ContainerImportGET - saveSampleFromContainer - apres limsServices.findSampleToCreate  " + sample);
						if(sample!=null){
							newSample =(Sample) InstanceHelpers.save(InstanceConstants.SAMPLE_COLL_NAME,sample,contextError,true);
							sampleUsed.referenceCollab=newSample.referenceCollab;
						}
						
						contextError.removeKeyFromRootKeyName(rootKeyName);
						
					}else {	
						/* Find sample in Mongodb */
						Logger.debug("ContainerImportGET - saveSampleFromContainer - "+ sampleUsed.sampleCode + " existe dans mongoDB");
						newSample = MongoDBDAO.findByCode(InstanceConstants.SAMPLE_COLL_NAME,Sample.class, sampleUsed.sampleCode);
						
						Logger.debug("ContainerImportGET - saveSampleFromContainer - "+ newSample.referenceCollab);
						sampleUsed.referenceCollab = newSample.referenceCollab;
						
						/*
						 * check if container to update
						 * update sample projects list by corresponding library projects list from Barcode
						 */
						
						refLibraryProjectCodes = limsServices.findProjectsForBarcode(sampleUsed.sampleCode);
						if (!refLibraryProjectCodes.isEmpty()){
						Logger.debug("ContainerImportGET - saveSampleFromContainer project to update for sample " + newSample.code);
							MongoDBDAO.update(InstanceConstants.SAMPLE_COLL_NAME,  Sample.class, 
									DBQuery.is("code", newSample.code),
									DBUpdate.set("projectCodes", refLibraryProjectCodes));
						}
						
					}	
					sampleUsed.percentage = 100.0 / contents.size();
					
					Logger.debug("ContainerImportGET - saveSampleFromContainer " + sampleUsed.sampleCode);
						
					rootKeyName="container["+container.code+"]";
					contextError.addKeyToRootKeyName(rootKeyName);

					/* Error : No sample, remove container from list to create */
					if(newSample==null){
//						containers.remove(container);
						Logger.error("Container " + container.code + " n'a pas été importé. Sample "
								+ newSample.referenceCollab + " na pas pu être créer");
						contextError.addErrors("sample","error.codeNotExist", sampleUsed.sampleCode);
						return false;
					}else{
						/* From sample, add content in container */
						container.contents.remove(sampleUsed);
						Logger.debug("ContainerImportGET - saveSampleFromContainer - container.code = " + container.code + ", newSample.sampleCode = " + newSample+ ", sampleUsed.sampleCode = " + sampleUsed.sampleCode );
						ContainerHelper.addContent(container, newSample, sampleUsed);
						Logger.debug("ContainerImportGET - saveSampleFromContainer - After addContent = " + container.code + ", newSample.sampleCode = " + newSample+ ", sampleUsed.sampleCode = " + sampleUsed.sampleCode );
					}

					//si container est mis à jour - mettre à jour les contents
					if(containerDB != null){
						//update contents
						for(Content ctDB : containerDB.contents){
							for(Content nContent : container.contents){
								if (nContent.sampleCode.equals(ctDB.sampleCode)){
									UpdateContent(nContent, ctDB);
								}
							}
						}
					}
					Logger.debug("ContainerImportGET - saveSampleFromContainer - after apdate " + contents.size());
					
					if(container.contents.get(0).processProperties != null){
						Logger.debug("ContainerImportGET - saveSampleFromContainer befaur return " + container.contents.get(0).processProperties.size());
					}

					Logger.debug("ContainerImportGET - saveSampleFromContainer - after addContent " + contents.size() + " contents " + contents.get(0).percentage);
					
					container.sampleCodes.add(sampleUsed.sampleCode);
					contextError.removeKeyFromRootKeyName(rootKeyName); 
		
				}
			return true;
	}

	/**
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
	public	static void createContainers(ContextValidation contextError, String sqlContainer,String containerCategoryCode,  String containerStateCode, String experimentTypeCode, String sqlContent) throws SQLException, DAOException{
		Boolean addContainer;
		Boolean newContainer;
		String rootKeyName=null;
		Logger.debug("ContainerImportGET - createContainers, avant limsServices.findContainersToCreate");
		Logger.debug("ContainerImportGET - createContainers, requete sqlContainer : " + sqlContainer);
		List<Container> containers=	limsServices.findContainersToCreate(sqlContainer,contextError, containerCategoryCode,containerStateCode,experimentTypeCode);
		List<Container> allContainersToSupport = new ArrayList<Container>(0);
		Set<Container> impContainers = new HashSet<>();
//		Set<ContainerSupport> upSupports = new HashSet<>();
		
		allContainersToSupport.addAll(containers);
		
		/* 
		 * créer instance ou mettre à jour (si existe) chaque container
		 * avec des contents et des Samples dans Mongo
		 * 		(si une probleme des valeurs dans des parametres des contents
		 * 		ou la création de Sample n'est pas réussi 
		 * 		- container est suprimé de la liste)
		 */
		for (Container containerNew : allContainersToSupport) {
			Logger.debug("ContainerImportGET - createContainers, import container : " + containerNew.code);
			newContainer = true;
			//select containers from same support
			List<Container> containersTmp = MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, Container.class, DBQuery.is("support.code", containerNew.support.code)).toList();
			
			//if container to update or to add into exist support
			if (!containersTmp.isEmpty()){
				Logger.debug("ContainerImportGET - createContainers, support contents : " + containersTmp.size());
				/* créer une liste des containers à metre à jour */
				for (Container containerTmp : containersTmp) {
					Logger.debug("ContainerImportGET - createContainers, container : " + containerTmp.code);
					/* if the container to update */
					if(containerNew.code.equals(containerTmp.code)){
						Logger.debug("ContainerImportGET - createContainers, Container à ajourner " + containerNew.code + " = " + containerTmp.code);
						newContainer = false;
						UpdateContainer(containerNew, containerTmp);
						/* create sample or update content
						 * if error - delete container from containers list */
						if(!saveSampleFromContainer(contextError, containerNew, sqlContent, containerTmp)){
							containers.remove(containerNew);
						}
					/* if an other container exist in the same support */
					}else{
						Logger.debug("ContainerImportGET - createContainers, Container du même support : " + containerTmp.code + " pour " + containerNew.code);
						addContainer = true;
						//add for re-create in support, if not in the import list
						for (Container containerIn : containers) {
							if(addContainer && containerIn.code.equals(containerTmp.code)){
								addContainer = false;
							}
						}
						if(addContainer){
							Logger.debug("ContainerImportGET - createContainers, to add " + containerTmp.code + " " + impContainers.add(containerTmp));
						}
					}
				}
			/* if new container to create with its support */
			}else{
				Logger.debug("Il n'existe pas dans Mongo de support pour container : " + containerNew.code);
			}
			
			/* if new container create sample
			 * if error - delete container from containers list */
			if(newContainer){
				if(!saveSampleFromContainer(contextError, containerNew, sqlContent, null)){
					containers.remove(containerNew);
				}
			}
		}	
	
		/* supprimer les containers à ajourner et leurs supports */
		Logger.debug("ContainerImportGET - createContainers, avant ContainerImportGET.deleteContainerAndContainerSupport");
		ContainerImportGET.deleteContainerAndContainerSupport(containers);
		
		Map<String,PropertyValue<String>> propertiesContainerSupports=new HashMap<String, PropertyValue<String>>();
		for(Container container : containers){
						
			if(!propertiesContainerSupports.containsKey(container.support) && container.properties.get("sequencingProgramType")!=null){
				propertiesContainerSupports.put(container.support.code, container.properties.get("sequencingProgramType"));
				container.properties.remove("sequencingProgramType");
			}
		}
		
		/* ajouter des containers à récréer dans la liste des nouveaux containers
		 * puis créer le support*/
		allContainersToSupport.clear();
		allContainersToSupport.addAll(containers);
		allContainersToSupport.addAll(impContainers);
		
		Logger.debug("ContainerImportGET - createContainers, avant ContainerHelper.createSupportFromContainers");
			ContainerHelper.createSupportFromContainers(allContainersToSupport,propertiesContainerSupports, contextError);
		Logger.debug("ContainerImportGET - createContainers, après ContainerHelper.createSupportFromContainers");
			
/* ACTIVER POUR PROD !!!
 		*
 		* créer ou retrouver dans e-SItoul characteristicDateImportNGL avec la date du jour
 		* et récuperer son id
 		*/
		Integer characteristicDateImportNGLid = null;
		if(! containers.isEmpty()){
			//if container list don't empty get|create characteristic id to associate
			Logger.debug("Before  createCaracteristicDateImportNGL " + play.Play.application().configuration().getString("caracteristicstypeEsitoul.DateImportNgl"));
			characteristicDateImportNGLid = limsServices.createCaracteristicDateImportNGL(Integer.parseInt(play.Play.application().configuration().getString("caracteristicstypeEsitoul.DateImportNgl")));
		}
		
		for(Container container:containers){
			//Logger.debug("Container :"+container.code+ "nb sample code"+container.sampleCodes.size());
			rootKeyName="container["+container.code+"]";
			contextError.addKeyToRootKeyName(rootKeyName);
			Logger.debug("ContainerImportGET - createContainers, avant InstanceHelpers.save avec sampleCodes= " + container.sampleCodes);
			Container result=(Container) InstanceHelpers.save(InstanceConstants.CONTAINER_COLL_NAME,container, contextError,true);
			Logger.debug("ContainerImportGET - createContainers, après InstanceHelpers.save avec sampleCodes= " + container.sampleCodes);

/*
 * ACTIVER POUR PROD !!!
			* si container a été bien créé
			* lier characteristicDateImportNGL au container		
			*/		
			if(result!=null){
				/* container model in NGL don't keep an barcodeid from e-sitoul barre-code 
				 * so, barcode string is used for create new link with characteristic Date_Import_NGL
				 */
				Logger.debug("BARCODE "+ container.code +" caracteristicDateImportNGL : "+ characteristicDateImportNGLid);
				limsServices.linkBarcodeToCaracteristics3(container.code, characteristicDateImportNGLid);
				limsServices.deletSameLinkCaracteristics(container.code, characteristicDateImportNGLid);
			}
			
			contextError.removeKeyFromRootKeyName(rootKeyName);
		}
	
	}

	private static void deleteContainerAndContainerSupport(
			List<Container> containers) {
		for(Container container : containers){
//			//delete de tout les containers associés au support du container, alors les lanes supprimées dans le Lims seront supprimés dans NGL
//			MongoDBDAO.delete(InstanceConstants.CONTAINER_COLL_NAME, Container.class, DBQuery.is("support.code", container.support.code));
			MongoDBDAO.deleteByCode(InstanceConstants.CONTAINER_COLL_NAME, Container.class, container.code);
			MongoDBDAO.deleteByCode(InstanceConstants.CONTAINER_SUPPORT_COLL_NAME, ContainerSupport.class, container.support.code);
		}
	}

	/**
	 * 
	 * Create au niveau Container from a ResultSet
	 * 
	 * The resultset must return fields :code, project, sampleCode, comment, codeSupport, limsCode, receptionDate, measuredConcentration, measuredVolume, mesuredQuantity, indexBq, nbContainer
	 * 
	 * @param rs ResulSet from Query
	 * @param containerCategoryCode 
	 * @param containerStatecode
	 * @return
	 * @throws SQLException
	 * @throws DAOException 
	 */
	public static Container createContainerFromResultSet(ResultSet rs, String containerCategoryCode, String containerStatecode, String experimentTypeCode) throws SQLException, DAOException{
		Logger.debug("ContainerImportGET - createContainerFromResultSet for "+rs.getString("code"));
		Container container = new Container();
				
		if (rs.getString("createUser") != null){
			Logger.debug("container.createUser " + rs.getString("createUser"));
			container.traceInformation.setTraceInformation(rs.getString("createUser"));
		}else{
			container.traceInformation.setTraceInformation(InstanceHelpers.getUser());
		}
		
		container.code=rs.getString("code");
		LocationOnContainerSupport locationOnContainerSupport = new LocationOnContainerSupport(); 
		locationOnContainerSupport.code = rs.getString("localisation"); 
		Integer positionOnSupport = rs.getInt("position");
		Integer ligne = (positionOnSupport -(positionOnSupport % 1000))/1000;
		Integer colonne = positionOnSupport % 1000;
		

		Logger.debug("ContainerImportGET createContainerFromResultSet - categoryCode: " + rs.getString("categoryCode"));
		if ( "INDEFINI".equals(rs.getString("categoryCode"))){
			container.categoryCode=containerCategoryCode;
			return container;
		}

		try {
			container.categoryCode=DataMappingGET.getObjectTypeCodeFromLims(rs.getString("categoryCode"));
		}catch(SQLException ex){
			container.categoryCode=containerCategoryCode;
		}
		


		
		Logger.debug("Container récupéré :" + container.code + ", " + container.categoryCode);
		
		container.comments=new ArrayList<Comment>();				
		container.comments.add(new Comment(rs.getString("comment"),"ngl-data"));
		
		container.state = new State(); 
//		container.state.code=DataMappingGET.getState(container.categoryCode,rs.getInt("etatLims"),experimentTypeCode);
		container.state.code="IW-P";
		
		container.state.user = InstanceHelpers.getUser();
		container.state.date = new Date();

		
		container.valuation = new Valuation();
		container.valuation.valid=TBoolean.UNSET; // instead of valid=null;
		//Logger.debug("ContainerImportGET - createContainerFromResultSet, support du container : " + container.support.code);
		Logger.debug("ContainerImportGET - createContainerFromResultSet - Info support : " + container.categoryCode + ", "+ rs.getInt("nbContainer")+ ", "+ rs.getString("codeSupport")+ ", "+ Integer.toString(colonne)+ ", "+ Integer.toString(ligne));
		//
		
		if ("tube".equals(container.categoryCode)){
			container.support=ContainerSupportHelper.getContainerSupportTube(container.code);
		}else{
			Integer nbPositionObjectType = rs.getInt("nbContainer");
			Integer nbUsablePositions = (nbPositionObjectType % 1000) * (nbPositionObjectType - (nbPositionObjectType % 1000))/1000;
			Logger.debug("ContainerImportGET - createContainerFromResultSet - Info support : nb de positions utilisables " + Integer.toString(nbUsablePositions) + " pour un max_position de " + Integer.toString(nbPositionObjectType));
			Logger.debug("ContainerImportGET - createContainerFromResultSet - avant getContainerSupport : ");
			
			container.support=ContainerSupportHelper.getContainerSupport(container.categoryCode, nbUsablePositions , rs.getString("codeSupport"), Integer.toString(colonne), Integer.toString(ligne),rs.getString("codeSupport"));
		}
		Logger.debug("ContainerImportGET - createContainerFromResultSet, support du container : " + container.support.code);
		container.properties= new HashMap<String, PropertyValue>();

		
		container.properties = limsServices.getCaracteristiquesForContainer(rs.getInt("barcodeid"));
		if (container.properties.containsKey("Concentration_Librairie")){
			PropertySingleValue aa ;
			aa = (PropertySingleValue)container.properties.get("Concentration_Librairie");
			Logger.debug("ContainerImportGET - createContainerFromResultSet, Concentration_Librairie : " + aa.value.toString() + aa.unit);
			container.concentration=new PropertySingleValue(Math.round(Double.parseDouble(aa.value.toString())*100.0)/100.0, aa.unit);
//			container.concentration = new PropertySingleValue(Math.round(rs.getFloat("measuredConcentration")*100.0)/100.0, mesuredConcentrationUnit);
		}
		
		if(rs.getString("receptionDate")!=null){
			container.properties.put("receptionDate",new PropertySingleValue(rs.getString("receptionDate")));
			Logger.debug("ContainerImportGET - createContainerFromResultSet, apres container.properties.put ");
		}

		if(null != experimentTypeCode){
			container.fromTransformationTypeCodes=new HashSet<String>();
			container.fromTransformationTypeCodes.add(experimentTypeCode);	
			Logger.debug("ContainerImportGET - createContainerFromResultSet, apres container.fromTransformationTypeCodes.add ");
		}

		container.projectCodes=new HashSet<String>();					
		List<String> projects = new ArrayList<String>();
		projects = limsServices.findProjectsForContainer(rs.getInt("barcodeid"));
		for (String project : projects) {
			Logger.debug("ContainerImportGET - createContainerFromResultSet - Projet lié à " + container.code + "  : " + project);
			container.projectCodes.add(project);
		}			

		container.sampleCodes=new HashSet<String>();

		if(rs.getString("sampleCode")!=null){
			Logger.debug("ContainerImportGET - createContainerFromResultSet - sampleCode = " + rs.getString("sampleCode"));
			Content sampleUsed=new Content();
			sampleUsed.percentage=0.5;// default value
			sampleUsed.sampleCode=rs.getString("sampleCode");
			sampleUsed.projectCode = container.projectCodes.iterator().next(); 

			//TODO add projectCode
			//Todo replace by method in containerHelper who update sampleCodes from contents
			container.sampleCodes.add(rs.getString("sampleCode"));
			sampleUsed.properties = new HashMap<String, PropertyValue>();
			sampleUsed.properties = limsServices.getCaracteristiquesForContainer(rs.getInt("barcodeid"));
			sampleUsed.properties.put("libProcessTypeCode",new PropertySingleValue(""));// default value
			sampleUsed.properties.put("libLayoutNominalLength", new PropertySingleValue(0));// default value
			
			Logger.debug("ContainerImportGET - createContainerFromResultSet avant container.contents.add(sampleUsed) " + sampleUsed.sampleCode);
			container.contents.add(sampleUsed);
		} 
		
		Logger.debug("ContainerImportGET - createContainerFromResultSet sortie");
		return container;

	}

/**
 * @author okorovina
 * 
 * récopie tous les informations dans no
 * 
 * @param coNew
 * @param coDB
 * 
 * 
 */
	private static void UpdateContainer(Container coNew, Container coDB) {
//		Logger.debug("ContainerImportGET UpdateContainer " + coDB.toString() + " by " + coNew.toString());
		coNew.state = coDB.state;
		coNew.support = coDB.support;
		
		if (coDB.valuation != null){
			coNew.valuation = coDB.valuation;
		}
		
		//update properties
		if(coDB.properties != null){
			for (Map.Entry<String, PropertyValue> propertyDB : coDB.properties.entrySet()) {
				if(!coNew.properties.containsKey(propertyDB.getKey())){
					coNew.properties.put(propertyDB.getKey(), propertyDB.getValue());
				}
			}
		}
		
		//update comments
		List<Comment> comments = coDB.comments;
		comments.addAll(coDB.comments);
		comments.add(updateComment);
		coNew.comments = comments;
		
//		//update contents
//		for(Content ctDB : coDB.contents){
//			for (Content ctNew : coNew.contents){
//				if (ctNew.sampleCode.equals(ctDB.sampleCode)){
//					UpdateContent(ctNew, ctDB);
//				}
//			}
//		}
		
		if(coDB.qualityControlResults != null){
			coNew.qualityControlResults = coDB.qualityControlResults;
		}
		if(coDB.fromTransformationTypeCodes != null){
			coNew.fromTransformationTypeCodes = coDB.fromTransformationTypeCodes;
		}
		if(coDB.fromTransformationCodes != null){
			coNew.fromTransformationCodes = coDB.fromTransformationCodes; 
		}
		if(coDB.processTypeCodes != null){
			coNew.processTypeCodes = coDB.processTypeCodes;
		}
		if(coDB.processCodes != null){
			coNew.processCodes = coDB.processCodes;
		}
		if(coDB.fromPurificationTypeCode != null){
			coNew.fromPurificationTypeCode = coDB.fromPurificationTypeCode;
		}
		if(coDB.fromPurificationCode != null){
			coNew.fromPurificationCode = coDB.fromPurificationCode;
		}
		if(coDB.fromTransfertCode != null){
			coNew.fromTransfertCode = coDB.fromTransfertCode;
		}
		if(coDB.fromTransfertTypeCode != null){
			coNew.fromTransfertTypeCode = coDB.fromTransfertTypeCode;
		}
		if(coDB.treeOfLife != null){
			coNew.treeOfLife = coDB.treeOfLife;
		}
	}
	

	private static void UpdateContent(Content ctNew, Content ctDB) {
		
		if(!ctDB.properties.isEmpty()){
			for (Map.Entry<String, PropertyValue> propertyCDB : ctDB.properties.entrySet()) {
				if(!ctNew.properties.containsKey(propertyCDB.getKey())){
					ctNew.properties.put(propertyCDB.getKey(), propertyCDB.getValue());
				}
			}
		}
		
		if (ctDB.processProperties != null){
			ctNew.processProperties = ctDB.processProperties;
			Logger.debug("ContainerImportGET UpdateContent add processProperties to : " + ctNew.sampleCode);
		}	
		if (ctDB.processComments != null){
					ctNew.processComments = ctDB.processComments;
		}
	}

}
