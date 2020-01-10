package models.utils.instance;

import static fr.cea.ig.play.IGGlobals.configuration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.mongojack.DBQuery;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.lfw.utils.Iterables;
import models.laboratory.sample.instance.Sample;
import models.utils.InstanceConstants;
import rules.services.RulesServices6;

public class SampleHelper {
	
//	/**
//	 * Logger.
//	 */
//	private static play.Logger.ALogger logger = play.Logger.of(SampleHelper.class);
	
	/* 10/11/2016 GA DO NOT USED ANYMORE
	public static void updateSampleProperties(String sampleCode, Map<String,PropertyValue>  properties,ContextValidation contextValidation){
		
		if(properties !=null){
			for(Entry<String,PropertyValue> entry :properties.entrySet()){
			
			MongoDBDAO.update(InstanceConstants.SAMPLE_COLL_NAME,Sample.class, 
					DBQuery.is("code",sampleCode),
					DBUpdate.set("properties."+entry.getKey(),entry.getValue())
							.set("traceInformation.modifyUser",contextValidation.getUser())
							.set("traceInformation.modifyDate",new Date() ));
			
			MongoDBDAO.update(InstanceConstants.CONTAINER_COLL_NAME, Run.class, 
					 DBQuery.is("contents.sampleCode", sampleCode),
					DBUpdate.set("contents.$.properties."+entry.getKey(),entry.getValue())
							.set("traceInformation.modifyUser",contextValidation.getUser())
							.set("traceInformation.modifyDate",new Date() ),true);					
			
			MongoDBDAO.update(InstanceConstants.READSET_ILLUMINA_COLL_NAME,ReadSet.class,
					DBQuery.is("sampleOnContainer.sampleCode", sampleCode),
					DBUpdate.set("sampleOnContainer.properties."+entry.getKey(),entry.getValue())
							.set("sampleOnContainer.lastUpdateDate", new Date()),true);

			MongoDBDAO.update(InstanceConstants.PROCESS_COLL_NAME, Process.class,
					DBQuery.is("sampleOnInputContainer.sampleCode", sampleCode),
					DBUpdate.set("sampleOnInputContainer.properties."+ entry.getKey(),entry.getValue()),true);
			}
		} else {
			contextValidation.addErrors("properties", ValidationConstants.ERROR_CODE_NOTEXISTS_MSG, sampleCode);
		}
		
	}
	*/

	// -------------------------------------------------------------------------------
	// arguments reordered
	
//	public static boolean deleteSample_(String sampleCode, ContextValidation contextValidation) {
//		return SampleHelper.deleteSample(sampleCode, contextValidation);
//	}
//
//	/**
//	 * Delete sample.
//	 * @param sampleCode        code of sample to delete from database
//	 * @param contextValidation validation context
//	 * @return                  true if the sample was delete, false otherwise
//	 */
//	//Return true if sample deleted 
//	//Return false if error sample => Sample must be update
//	public static boolean deleteSample(String sampleCode, ContextValidation contextValidation) {
//		ContextValidation ctx = ContextValidation.createUndefinedContext(contextValidation.getUser());
//		if (MongoDBDAO.checkObjectExist(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, DBQuery.is("sampleOnContainer.sampleCode", sampleCode))) {
//			logger.debug("sample {} dans ReadSet", sampleCode);
//			ctx.addError("readSet.sampleOnContainer.sampleCode", "Code {0} existe dans ReadSet" , sampleCode);
//		}		
//		if (MongoDBDAO.checkObjectExist(InstanceConstants.CONTAINER_COLL_NAME, Container.class, DBQuery.notEquals("categoryCode","tube").in("sampleCodes", sampleCode))) {
//			logger.debug("sample {} dans Container", sampleCode);
//			ctx.addError("container.sampleOnContainer.sampleCode","Code {0} existe dans ReadSet" , sampleCode);
//		}
//		if (!ctx.hasErrors()) {
//			MongoDBDAO.delete(InstanceConstants.CONTAINER_COLL_NAME, Container.class, DBQuery.in("sampleCodes", sampleCode).notExists("fromTransformationTypeCodes"));
//			logger.info("delete container for sampleCode {}", sampleCode);
//			MongoDBDAO.delete(InstanceConstants.SAMPLE_COLL_NAME,Sample.class,DBQuery.is("code", sampleCode));
//			logger.info("delete sample for sampleCode {}", sampleCode);
//		} else {
//			return false;
//		}
//		// TO DO: ctx has no errors so this seems not needed
//		contextValidation.getErrors().putAll(ctx.getErrors());
//		return true;
//	}

	// -------------------------------------------------------------------------------
	
	/* 10/11/2016 GA_ DO NOT USED ANYMORE
	public static void updateSampleReferenceCollab(Sample sample, ContextValidation contextError) {
			
			MongoDBDAO.update(InstanceConstants.SAMPLE_COLL_NAME,Sample.class, 
					DBQuery.is("code",sample.code),
					DBUpdate.set("referenceCollab",sample.referenceCollab));
			
			MongoDBDAO.update(InstanceConstants.CONTAINER_COLL_NAME, Container.class, 
					 DBQuery.is("contents.sampleCode", sample.code),
					DBUpdate.set("contents.$.referenceCollab",sample.referenceCollab),true);					
			
			MongoDBDAO.update(InstanceConstants.READSET_ILLUMINA_COLL_NAME,ReadSet.class,
					DBQuery.is("sampleOnContainer.sampleCode", sample.code),
					DBUpdate.set("sampleOnContainer.referenceCollab",sample.referenceCollab),true);

			MongoDBDAO.update(InstanceConstants.PROCESS_COLL_NAME, Process.class,
					DBQuery.is("sampleOnInputContainer.sampleCode", sample.code),
					DBUpdate.set("sampleOnInputContainer.referenceCollab",sample.referenceCollab),true);
		
	}
	*/
	
	/**
	 * Execute drools rules for the creation of a given sample.
	 * @param sample created sample to trigger rules for
	 */
	public static void executeSampleCreationRules(Sample sample) {
		executeRules(sample, "sampleCreation");
	}
	
	private static void executeRules(Sample sample, String rulesName) {
		ArrayList<Object> facts = new ArrayList<>();
		facts.add(sample);
		RulesServices6.getInstance().callRulesWithGettingFacts(configuration().getString("rules.key"), rulesName, facts);
	}

	/**
	 * Set of parent samples of a sample.
	 * @param sampleCode sample code
	 * @return           set of parent sample codes
	 */
	public static Set<String> getSampleParent(String sampleCode) {
		Sample sample = MongoDBDAO.findOne(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, DBQuery.is("code", sampleCode));
		Set<String> sampleCodes = new HashSet<>();
		sampleCodes.add(sampleCode);
		if (sample.life != null && sample.life.path != null) {
			sampleCodes.addAll(Arrays.asList(sample.life.path.substring(1).split(",")));
		}
		return sampleCodes;
	}

	/**
	 * Set of project codes of a sample set.
	 * @param sampleCodes sample codes
	 * @return            set of project codes
	 */
//	public static Set<String> getProjectParent(Set<String> sampleCodes) {
//		Set<String> projectCodes = new HashSet<>();
//		List<Sample> samples = MongoDBDAO.find(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, DBQuery.in("code", sampleCodes)).toList();
//		for (Sample s : samples) {
//			projectCodes.addAll(s.projectCodes);
//		}
//		return projectCodes;
//	}
	public static Set<String> getProjectParent(Set<String> sampleCodes) {
		return Iterables.zen(MongoDBDAO.find(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, DBQuery.in("code", sampleCodes)).cursor)
				        .flatMap(s -> s.projectCodes)
				        .toSet();
	}

}
