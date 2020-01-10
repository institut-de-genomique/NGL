package models;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;

import fr.cea.ig.DBObject;
import fr.cea.ig.MongoDBDAO;
import models.laboratory.common.instance.Comment;
import models.laboratory.common.instance.State;
import models.laboratory.common.instance.TBoolean;
import models.laboratory.common.instance.TraceInformation;
import models.laboratory.common.instance.Valuation;
import models.laboratory.container.description.ContainerCategory;
import models.laboratory.container.instance.Container;
import models.laboratory.container.instance.Content;
import models.laboratory.container.instance.LocationOnContainerSupport;
import models.laboratory.experiment.description.ExperimentType;
import models.laboratory.experiment.instance.Experiment;
import models.laboratory.experiment.instance.InputContainerUsed;
import models.laboratory.experiment.instance.OneToOneContainer;
import models.laboratory.experiment.instance.OutputContainerUsed;
import models.laboratory.instrument.description.InstrumentUsedType;
import models.laboratory.instrument.instance.InstrumentUsed;
import models.laboratory.processes.instance.Process;
import models.laboratory.processes.instance.SampleOnInputContainer;
import models.laboratory.project.description.ProjectCategory;
import models.laboratory.project.description.ProjectType;
import models.laboratory.project.instance.Project;
import models.laboratory.resolutions.instance.Resolution;
import models.laboratory.sample.description.SampleType;
import models.laboratory.sample.instance.Sample;
import models.utils.CodeHelper;
import models.utils.InstanceConstants;
import models.utils.InstanceHelpers;
import models.utils.dao.DAOException;
import utils.AbstractTests;
import utils.Constants;

public class InstanceTest extends AbstractTests {

	private static final play.Logger.ALogger logger = play.Logger.of(InstanceTest.class);
	
//	@SuppressWarnings("unchecked")
//	static final Class<DBObject>[] classTest = new Class[]
//			{ Process.class
//		    , Sample.class
//		    , Experiment.class
//		    , Project.class
//		    , Container.class
//		    };

	static final List<Class<? extends DBObject>> classTest = 
			Arrays.asList( Process.class
		                 , Sample.class
		                 , Experiment.class
		                 , Project.class
		                 , Container.class
					     );

	// private static final Map<String, List<ValidationError>> errors = new HashMap<String, List<ValidationError>>();

	static String             id;
	static ProjectType        sProjectType;
	static SampleType         sSampleType;
	static ExperimentType     sexpExperimentType;
	static InstrumentUsedType sIntrumentUsedType;
	static ContainerCategory  sContainerCategory;
	static models.laboratory.common.description.State sState;
	static Resolution         sResolution;

	@BeforeClass
	public static  void initData() throws DAOException {
		//ExperimentType
		sexpExperimentType = ExperimentType.find.findAll().get(0);
	}	

	@AfterClass
	public static  void deleteData() {
	}

	//@Test 
	public void saveInstanceMongo() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		for (Class<? extends DBObject> t : classTest)
			saveDBOject(t);
	}

	//@Test
	public void updateProject() throws DAOException {
		Project project = findObject(Project.class);
		project.name             = "projectName";
		project.comments         = new ArrayList<>();
		project.comments.add(new Comment("comment", "ngl-test"));
		project.traceInformation = new TraceInformation();
		project.traceInformation.setTraceInformation("test");
		project.typeCode         = "projectType";
		project.categoryCode     = "categoryProject";

		project.state            = new State(); 
		project.state.code       = "EtatprojectType";
		project.state.user       = "test";
		project.state.date       = new Date();
				
		MongoDBDAO.save(InstanceConstants.PROJECT_COLL_NAME, project);

		project = findObject(Project.class);
		assertThat(project.code).isEqualTo("ProjectCode");
		assertThat(project.name).isEqualTo("projectName");
		ProjectCategory projectCategory = ProjectCategory.find.findByCode(project.categoryCode);
		assertThat(projectCategory).isNotNull();

		ProjectType projectType = ProjectType.find.findByCode(project.typeCode);
		assertThat(projectType).isNotNull();
		assertThat(project.state).isNotNull();
		assertThat(project.state.code).isEqualTo("EtatprojectType");
		assertThat(project.state.user).isEqualTo("test");
	}

	//@Test
	public void updateSample() {
		Sample sample = findObject(Sample.class);
		sample.name            = "sampleName";
		sample.referenceCollab = "Ref collab";
		
		sample.valuation = new Valuation(); 
		sample.valuation.valid = TBoolean.UNSET;
		
		sample.categoryCode    = "sampleCategory";
		sample.typeCode        = "sampleType";
		sample.projectCodes.add("ProjectCode");

		/*sample.comments=InstanceHelpers.addComment("comment", sample.comments);
		InstanceHelpers.updateTraceInformation(sample.traceInformation); */

		MongoDBDAO.save(Sample.class.getSimpleName(), sample);

		sample=findObject(Sample.class);

		assertThat(sample.code).isEqualTo("SampleCode");
		assertThat(sample.name).isEqualTo("sampleName");
		
		/*assertThat(sample.comments).isNotNull();
		assertThat(sample.comments.get(0).comment).isEqualTo("comment");
		assertThat(sample.traceInformation.modifyUser).isNotNull();
		assertThat(sample.valid).isEqualTo(TBoolean.UNSET);*/

		assertThat(sample.categoryCode).isEqualTo("sampleCategory");
		assertThat(sample.typeCode).isEqualTo("sampleType");
		assertThat(sample.projectCodes.size()).isEqualTo(1);
	}

	//@Test
	public void updateContainer(){
		Container container=findObject(Container.class);

		container.support = new LocationOnContainerSupport();
		container.support.code = "containerName";
		container.categoryCode = "containerCategory";

		container.projectCodes = new HashSet<>();
		container.projectCodes.add("ProjectCode");

		container.sampleCodes = new HashSet<>();
		container.sampleCodes.add("SampleCode");

		container.state = new State(); 
		container.state.code = "Etatcontainer";
		container.state.user = Constants.TEST_USER;
		container.state.date = new Date();

		container.valuation = new Valuation();
		container.valuation.valid = TBoolean.FALSE;
		container.valuation.user  = Constants.TEST_USER;
		container.valuation.date  = new Date(); 
		
		container.contents.add(new Content("SampleCode", "sampleType", "sampleCategory"));

		container.fromTransformationTypeCodes = new HashSet<>();
		container.fromTransformationTypeCodes.add("experimentType");
			
		//public List<QualityControlResult> qualityControlResults; 
		//public List<Proper> volume;
		
		container.comments = new ArrayList<>();
		container.comments.add(new Comment("comment", "ngl-test"));
		container.traceInformation.setTraceInformation("test"); 

		MongoDBDAO.save(Container.class.getSimpleName(), container);
		container=findObject(Container.class);

		assertThat(container.code).isEqualTo("ContainerCode");
		assertThat(container.support.code).isEqualTo("containerName");
		assertThat(container.comments.get(0).comment).isEqualTo("comment");
		assertThat(container.traceInformation.createUser).isEqualTo("test");
		assertThat(container.categoryCode).isEqualTo("containerCategory");
		assertThat(container.projectCodes).isNotEmpty();
		assertThat(container.projectCodes.toArray(new String[0])[0]).isEqualTo("ProjectCode");

		assertThat(container.state.code).isEqualTo("Etatcontainer");

		assertThat(container.sampleCodes).isNotEmpty();
		assertThat(container.sampleCodes.toArray(new String[0])[0]).isEqualTo("SampleCode");

		assertThat(container.contents).isNotEmpty();
		Iterator<Content> iterator = container.contents.iterator();
		Content content =  iterator.next();
		assertThat(content.sampleCode).isEqualTo("SampleCode");
		assertThat(content.sampleCategoryCode).isEqualTo("sampleCategory");
		assertThat(content.sampleTypeCode).isEqualTo("sampleType");
		
		//assertThat(container.contents.get(0).sampleCode).isEqualTo("SampleCode");
		//assertThat(container.contents.get(0).sampleCategoryCode).isEqualTo("sampleCategory");
		//assertThat(container.contents.get(0).sampleTypeCode).isEqualTo("sampleType");

		assertThat(container.fromTransformationTypeCodes).isNotEmpty();
		assertThat(container.fromTransformationTypeCodes.size()).isEqualTo(1);
		assertThat(container.fromTransformationTypeCodes.toArray(new String[0])[0]).isEqualTo("experimentType");
	}

	@Test
	public void updateExperience() {
		MongoDBDAO.delete(InstanceConstants.EXPERIMENT_COLL_NAME, Experiment.class, DBQuery.is("code", "ExperimentCode"));

		Experiment experiment = new Experiment();
		
		experiment.code="ExperimentCode";
		experiment.typeCode="experimentType";
		experiment.categoryCode="experimentCategory";

		experiment.projectCodes = new HashSet<>();
		experiment.projectCodes.add("ProjectCode");

		experiment.sampleCodes = new HashSet<>();
		experiment.sampleCodes.add("SampleCode");

		experiment.instrument = new InstrumentUsed();
		experiment.instrument.categoryCode="instrumentCategory";
		experiment.instrument.code="instrumentCode";

		State state=new State("N","test");
		experiment.state=state;
		experiment.state.resolutionCodes=new HashSet<>();
		experiment.state.resolutionCodes.add("ResolutionCode");

		//TODO
		//public Map<String,PropertyValue> experimentProperties;
		//public Map<String, PropertyValue> instrumentProperties;
		//		public String protocolCode;
		experiment.comments = new ArrayList<>();
		experiment.comments.add(new Comment("comment", "ngl-test"));
		experiment.traceInformation.setTraceInformation("test"); 

		experiment.atomicTransfertMethods= new ArrayList<>();
		for (int i=0; i<10; i++) {
			OneToOneContainer oneToOneContainer = new OneToOneContainer();
			oneToOneContainer.inputContainerUseds = new ArrayList<>();
			oneToOneContainer.inputContainerUseds.add( new InputContainerUsed("containerInput"+i));
			oneToOneContainer.outputContainerUseds = new ArrayList<>();
			oneToOneContainer.outputContainerUseds.add(new OutputContainerUsed("containerOutput"+i));
			experiment.atomicTransfertMethods.add(i,oneToOneContainer);
		}
		
		Experiment newExperiment = MongoDBDAO.save(InstanceConstants.EXPERIMENT_COLL_NAME, experiment);
		
		assertThat(newExperiment.code).isEqualTo(experiment.code);
		//assertThat(newExperiment.state.code).isEqualTo(state.code);
		
		// Test fails but it's x==x so it looks pretty bad
		// assertThat(experiment.state.resolutionCodes.iterator()).isEqualTo(experiment.state.resolutionCodes.iterator());
		// We at least compare things that could be different, not the same as in the previous test
		assertEquals("resolutionCodes", new ArrayList<>(experiment.state.resolutionCodes), 
				                        new ArrayList<>(newExperiment.state.resolutionCodes));
		
		MongoDBDAO.update(InstanceConstants.EXPERIMENT_COLL_NAME, Experiment.class, DBQuery.is("code",experiment.code),DBUpdate.set("state",state));
		newExperiment = MongoDBDAO.findByCode(InstanceConstants.EXPERIMENT_COLL_NAME,Experiment.class,experiment.code);
		
		assertThat(newExperiment.code).isEqualTo(experiment.code);
		assertThat(newExperiment.state.code).isEqualTo(state.code);
	}
	
	//@Test
	public void validateGetSampleOnInputContainer(){
		List<Container> containers = MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, Container.class, DBQuery.exists("contents.properties.tag")).toList();
		Container container = containers.get(0);
		Iterator<Content> iter = container.contents.iterator();
		Content content = iter.next();
		//Content content = container.contents.get(0);
		SampleOnInputContainer sampleOnInputContainer = InstanceHelpers.getSampleOnInputContainer(content, container);
		
		assertThat(sampleOnInputContainer.containerCode).isNotEmpty().isNotNull();
		assertThat(sampleOnInputContainer.containerSupportCode).isNotEmpty().isNotNull();
		assertThat(sampleOnInputContainer.containerConcentration).isNotNull();
		Date d = new Date();
		DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		assertThat(sdf.format(d)).isEqualTo(sdf.format(sampleOnInputContainer.lastUpdateDate));
		assertThat(sampleOnInputContainer.properties).isNotEmpty().isNotNull();			
		assertThat(sampleOnInputContainer.properties.get(InstanceConstants.TAG_PROPERTY_NAME)).isNotNull();
	}
	
	////@Test
	public void removeInstanceMongo(){
		for (Class<? extends DBObject> t : classTest)
			removeDBOject(t);
	}

	public <T extends DBObject> T saveDBOject(Class<T> t) throws InstantiationException, IllegalAccessException, ClassNotFoundException{
		String collection = t.getSimpleName();
		String code       = t.getSimpleName() + "Code";
//		T object          = (T) Class.forName (t.getName()).newInstance();
		T object          = t.newInstance();
		object.code  = code;
		object       = MongoDBDAO.save(collection, object);
		id           = object._id;
		assertThat(object._id).isNotNull();
		return object;
	}

	// public <T extends DBObject> void removeDBOject(Class<T> type) {
	public <T extends DBObject> void removeDBOject(Class<T> type) {
		String collection = type.getSimpleName();
		String code       = type.getSimpleName() + "Code";
		T object = MongoDBDAO.findByCode(collection, type, code);
		MongoDBDAO.delete(collection, object);
		object = MongoDBDAO.findByCode(collection, type, code);
		assertThat(object).isNull();
	}

	public <T extends DBObject> T findObject(Class<T> type){
		String collection = type.getSimpleName();
		String code       = type.getSimpleName()+"Code";
		return MongoDBDAO.findByCode(collection, type, code);		
	}
	
	// TODO: evaluate if this is a real test as it does not look like one
//	@Test
	public void generateExperimentCode() throws InterruptedException {
		Experiment e = new Experiment();
		e.typeCode = "test-tt";
		for (int i = 0; i < 120; i++) {	
//			System.out.println(CodeHelper.getInstance().generateExperimentCode(e));			
			logger.debug("generated experiment code : {}", CodeHelper.getInstance().generateExperimentCode(e));			
		}
	}
	
}
