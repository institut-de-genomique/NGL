package services.io.reception.mapping;

//import static fr.cea.ig.play.IGGlobals.akkaSystem;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.mongojack.DBQuery;

import fr.cea.ig.DBObject;
import fr.cea.ig.MongoDBDAO;
import models.laboratory.common.instance.TraceInformation;
import models.laboratory.project.instance.Project;
import models.laboratory.reception.instance.AbstractFieldConfiguration;
import models.laboratory.reception.instance.ReceptionConfiguration.Action;
import models.laboratory.sample.description.SampleType;
import models.laboratory.sample.instance.Sample;
import models.utils.CodeHelper;
import models.utils.InstanceConstants;
import models.utils.instance.SampleHelper;
import services.io.reception.Mapping;
import validation.ContextValidation;
import validation.IValidation;
import validation.utils.ValidationConstants;

public class SampleMapping extends Mapping<Sample> {
	
	private static final play.Logger.ALogger logger = play.Logger.of(SampleMapping.class);
	
	private Map<String, Project> lastSampleCodeForProjects = new HashMap<>(0);
	
	/*
	 * 
	 * @param objects : list of all db objects need by type samples, supports, containers
	 * @param configuration : the filed configuration for the current type
	 * @param action
	 * @param contextValidation
	 */
//	@Inject
//	public SampleMapping(Map<String, Map<String, DBObject>> objects, 
//						 Map<String, ? extends AbstractFieldConfiguration> configuration, 
//						 Action action, 
//						 ContextValidation contextValidation,
//						 NGLContext ctx) {
//		super(objects, configuration, action, InstanceConstants.SAMPLE_COLL_NAME, Sample.class, Mapping.Keys.sample, contextValidation);
//	}
	
	@Inject
	public SampleMapping(Map<String, Map<String, DBObject>> objects, 
						 Map<String, ? extends AbstractFieldConfiguration> configuration, 
						 Action action, 
						 ContextValidation contextValidation) {
		super(objects, configuration, action, InstanceConstants.SAMPLE_COLL_NAME, Sample.class, Mapping.Keys.sample, contextValidation);
	}

	/*
	 * convert a file line in Sample
	 * we override the defaut comportment to reused a prexist sample.
	 * @param rowMap
	 * @return
	 */
	@Override
	public Sample convertToDBObject(Map<Integer, String> rowMap) throws Exception{
		Sample object = type.newInstance();
		boolean needPopulate = false;
		if(Action.update.equals(action)){
			object = get(object, rowMap, true);
			needPopulate=true;
		}else if(Action.save.equals(action)){
			Sample objectInDB = get(object, rowMap, false);
			if(null != objectInDB){
				object = objectInDB;
				needPopulate=false;
			}else if(object.code != null){
				Sample objectInObjects = (Sample)objects.get(Mapping.Keys.sample.toString()).get(object.code);
				if(null != objectInObjects){
					object = objectInObjects;
				}
				needPopulate=true;
			}else{
				needPopulate=true;
			}
		}
		
		if(null != object && needPopulate){
			Field[] fields = type.getFields();
			for(Field field : fields){
				populateField(field, object, rowMap);			
			}						
			}
			update(object);
		return object;
	}
	
	@Override
	protected void update(Sample sample) {
		
		if (Action.save.equals(action) && sample._id == null){
			sample.traceInformation = new TraceInformation(contextValidation.getUser());
		
			//update categoryCode by default.
			//FDS 28/02/2018 catch the case when sample.typeCode is not valid
			//FDS 29/03/2018 NGL-1969: remplacer  findByCode  par findByCodeOrName
			SampleType sampleType = SampleType.find.get().findByCodeOrName(sample.typeCode); 
			
			if ( sampleType == null) {
				contextValidation.addError("sample.typeCode", ValidationConstants.ERROR_NOTEXISTS_MSG, sample.typeCode);
			} else {
				sample.categoryCode = sampleType.category.code;
				// il faut ecraser  sample.typeCode d'entree (qui peut etre un Name!) par le code ramené de la base
				sample.typeCode= sampleType.code;
				if (sample.code == null && sample.projectCodes != null && sample.projectCodes.size() == 1) {
					sample.code = generateSampleCode(sample);
				} else if (sample.code == null && sample.projectCodes != null && sample.projectCodes.size() == 0) {
					contextValidation.addError("sample.projectCodes", "no project code found for sample code generation");
				}
				
				if(sample.name == null && sample.code != null){
					sample.name = sample.code;
				}
				
				if(sample.life != null && sample.life.from != null && sample.life.from.sampleCode != null){
					Sample parentSample = MongoDBDAO.findOne(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, DBQuery.is("code",sample.life.from.sampleCode).in("projectCodes", sample.life.from.projectCode));
					if(null != parentSample){
						sample.life.from.sampleTypeCode=parentSample.typeCode;
						if(null != parentSample.life && null != parentSample.life.path){
							sample.life.path=parentSample.life.path+","+parentSample.code;
						}else{
							sample.life.path=","+parentSample.code;
						}
						//force this information 
						sample.properties.putAll(parentSample.properties);	
						if(!parentSample.taxonCode.equals(sample.taxonCode)){
							contextValidation.addError("taxonCode","error.receptionfile.taxonCode.diff", sample.taxonCode, parentSample.taxonCode);
						}
						if(!parentSample.referenceCollab.equals(sample.referenceCollab)){
							contextValidation.addError("referenceCollab","error.receptionfile.referenceCollab.diff", sample.referenceCollab, parentSample.referenceCollab);
						}				
					}else{
						contextValidation.addError("sample", ValidationConstants.ERROR_NOTEXISTS_MSG, sample.life.from.projectCode+" + "+sample.life.from.sampleCode);
					}
				}else{
					sample.life = null;
				}
			}
			
			//Call rules to add properties to sample
			logger.debug("sample "+sample);
			SampleHelper.executeSampleCreationRules(sample);
			logger.debug("sample "+sample);
		} else {
			sample.traceInformation.setTraceInformation(contextValidation.getUser());			
		}
	}

	private String generateSampleCode(Sample sample) {
		String projectCode = sample.projectCodes.iterator().next();

		if (!lastSampleCodeForProjects.containsKey(projectCode)) {
			Project project = MongoDBDAO.findByCode(InstanceConstants.PROJECT_COLL_NAME, Project.class, projectCode);
			lastSampleCodeForProjects.put(projectCode, project);
		}
		Project project = lastSampleCodeForProjects.get(projectCode);
		if (project != null) {
			project.lastSampleCode = CodeHelper.getInstance().generateSampleCode(project, false);
			return project.lastSampleCode;
		} else {
			return null;
		}
	}
	
	@Override
	public void consolidate(Sample sample) {
		
	}	
	
//	@Override
//	public void validate(DBObject c) {
//		ContextValidation cv = new ContextValidation(contextValidation.getUser());
//		cv.setRootKeyName(contextValidation.getRootKeyName());
//		cv.addKeyToRootKeyName(c.code);
//		if (Action.save.equals(action) && c._id == null){
//			cv.setCreationMode();
//		} else {
//			cv.setUpdateMode();
//		}
//		((IValidation)c).validate(cv);
//		if (cv.hasErrors()) {
//			contextValidation.addErrors(cv.errors);
//		}
//		cv.removeKeyFromRootKeyName(c.code);	
//	}
	@Override
	public void validate(DBObject c) {
		ContextValidation cv = 
				(Action.save.equals(action) && c._id == null) ? ContextValidation.createCreationContext(contextValidation.getUser())
				                                              : ContextValidation.createUpdateContext(contextValidation.getUser());
		cv.setRootKeyName(contextValidation.getRootKeyName());
		cv.addKeyToRootKeyName(c.code);
		((IValidation)c).validate(cv);
		if (cv.hasErrors()) {
			contextValidation.addErrors(cv.getErrors());
		}
		cv.removeKeyFromRootKeyName(c.code);	
	}	
	
	@Override
	public void synchronizeMongoDB(DBObject c){
		if (Action.save.equals(action) && c._id == null) {
			Sample sample = (Sample)c;
			CodeHelper.getInstance().updateProjectSampleCodeIfNeeded(sample.projectCodes.iterator().next(), sample.code);
		}
		super.synchronizeMongoDB(c);
	}

	@Override
	public void rollbackInMongoDB(DBObject c){
		if(Action.save.equals(action) && c._id == null){ 
			Sample sample = (Sample)c;
			MongoDBDAO.deleteByCode(collectionName, c.getClass(), c.code);
			
			if(sample.projectCodes.size() == 1){
				String projectCode = sample.projectCodes.iterator().next(); 
				CodeHelper.getInstance().updateProjectSampleCodeWithLastSampleCode(projectCode);
			}else{
				contextValidation.addError("project","problem during rollback to update last sample code on projects "+sample.projectCodes.toString());
			}
		}else if(Action.update.equals(action)){
			//replace by old version of the object
		}		
	}
	
}
