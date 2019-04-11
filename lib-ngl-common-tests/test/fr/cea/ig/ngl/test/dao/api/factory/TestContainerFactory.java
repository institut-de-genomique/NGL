package fr.cea.ig.ngl.test.dao.api.factory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import controllers.containers.api.ContainerSupportsSearchForm;
import controllers.containers.api.ContainersSearchForm;
import fr.cea.ig.ngl.support.ListFormWrapper;
import fr.cea.ig.play.test.DevAppTesting;
import models.laboratory.common.instance.Comment;
import models.laboratory.common.instance.State;
import models.laboratory.common.instance.TraceInformation;
import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.container.instance.Container;
import models.laboratory.container.instance.ContainerSupport;
import models.laboratory.container.instance.Content;
import models.laboratory.container.instance.LocationOnContainerSupport;
import models.laboratory.sample.instance.Sample;
import ngl.refactoring.state.ContainerStateNames;

public class TestContainerFactory {

	private static final String UNIT_NG = "ng";
	// Constants
	public static final double QUANTITY = 1.0;
	public static final double VOL      = 1.0;
	public static final String PLATE_96 = "96-well-plate";
	public static final String TUBE = "tube";
	public static final String WELL = "well";
	
	//private static final play.Logger.ALogger logger = play.Logger.of(TestContainerFactory.class);
	
	
	public static Container container(String user, Sample s, ContainerSupport support) {
		return container(user, Arrays.asList(s), support.code, support.categoryCode);
	}
	
	public static Container container(String user, Collection<Sample> samples, ContainerSupport support) {
		return container(user, samples, support.code, support.categoryCode);
	}
	
	public static ContainerSupport containerSupportTube(String user, Sample s) {
		return containerSupport(user, Arrays.asList(s), TUBE);
	}
	
	public static ContainerSupport containerSupportPlate(String user, Collection<Sample> samples) {
		return containerSupport(user, samples, PLATE_96);
	}
	
	/*
	 * if more than one sample in the list it will create a pool
	 * @param user
	 * @param samples
	 * @param containerSupportCode
	 * @param containerSupportCategoryCode
	 * @return
	 */
	private static Container container(String user, Collection<Sample> samples, String containerSupportCode, String containerSupportCategoryCode) {
		Container c = new Container();
		c.code = DevAppTesting.newCode();
		//c.categoryCode = TUBE; // Validation: check only if the code exists. No validation against support categoryCode.
		c.categoryCode = chooseContainerCategoryCode(containerSupportCategoryCode);
		c.concentration = new PropertySingleValue(QUANTITY/VOL, "ng/µl");
		c.volume = new PropertySingleValue(VOL, "µl");
		c.quantity = new PropertySingleValue(QUANTITY, UNIT_NG);
		c.comments = comments(user);
		c.contents = new ArrayList<>(samples.size());
		c.projectCodes = new HashSet<>();
		c.sampleCodes = new HashSet<>();
		samples.forEach(s -> {
			c.projectCodes.addAll(s.projectCodes);
			c.sampleCodes.add(s.code);
			c.contents.add(content(s, new Double(100/samples.size())));
		});
		c.traceInformation = new TraceInformation(user);
		c.support = support(containerSupportCode, containerSupportCategoryCode);
		c.state = state(user);
		return c;
	}

	private static String chooseContainerCategoryCode(String containerSupportCategoryCode) {
		String categoryCode = null;
		if (TUBE.equals(containerSupportCategoryCode)) {
			categoryCode  = TUBE;
		} else {
			categoryCode = WELL;
		}
		return categoryCode;
	}
	
	public static List<Container> containers(String user, Collection<Sample> samples, ContainerSupport support) {
		return containers(user, samples, support.code, support.categoryCode);
	}
	private static List<Container> containers(String user, Collection<Sample> samples, String containerSupportCode, String containerSupportCategoryCode) {
		List<Container> containers = new ArrayList<>(samples.size());
		int line = 0;
		int column = 0;
		for(Sample s: samples) {
			Container c = new Container();
			c.code = DevAppTesting.newCode();
			c.categoryCode = chooseContainerCategoryCode(containerSupportCategoryCode);
			c.concentration = new PropertySingleValue(QUANTITY/VOL, "ng/µl");
			c.volume = new PropertySingleValue(VOL, "µl");
			c.quantity = new PropertySingleValue(QUANTITY, UNIT_NG);
			c.comments = comments(user);
			c.contents = Arrays.asList(content(s, new Double(100)));
			c.projectCodes.addAll(s.projectCodes);
			c.sampleCodes.add(s.code);
			c.traceInformation = new TraceInformation(user);
			c.support = support(containerSupportCode, containerSupportCategoryCode, new String(""+ line++), new String(""+ column++));
			c.state = state(user);
			containers.add(c);
		}
		
		return containers;
	}
	
	/*
	 * warning: the state will be changed to N (new) state by create method of API
	 */
	private static State state(String user) {
		State s = new State(ContainerStateNames.A_QC, user);
		s.date = new Date();
		return s;
	}

	public static LocationOnContainerSupport support(String containerSupportCode, String containerSupportCategoryCode) {
		return support(containerSupportCode, containerSupportCategoryCode, "1", "1");
	}
	
	public static LocationOnContainerSupport support(String containerSupportCode, String containerSupportCategoryCode, String line, String colum) {
		LocationOnContainerSupport s = new LocationOnContainerSupport();
		s.code = containerSupportCode;
		s.categoryCode = containerSupportCategoryCode;
		s.line = line;
		s.column = colum;
		return s;
	}

	private static Content content(Sample s, Double percentage) {
		Content c = new Content(s.code, s.typeCode, s.categoryCode);
		c.percentage = percentage;
		c.projectCode = s.projectCodes.iterator().next();
		return c;
	}

	private static List<Comment> comments(String user){
		return Arrays.asList(new Comment("very usefull comments", user));
	}
	
	
	private static ContainerSupport containerSupport(String user, Collection<Sample> samples, String supportCategory) {
		ContainerSupport cs = new ContainerSupport();
		cs.code = DevAppTesting.newCode(); 
		cs.categoryCode = supportCategory;
		cs.comments = comments(user);
		samples.forEach(s -> {
			cs.projectCodes.addAll(s.projectCodes);
			cs.sampleCodes.add(s.code);	
		});
		cs.nbContainers = samples.size();
		cs.nbContents = samples.size();
		return cs;
	}
	
	/**
	 * No querying mode or rendering mode are defined in the created form.
	 * 
	 * @param projectCode code of project
	 * @return            wrapper of ContainersSearchForm
	 * @throws Exception  exception
	 */
	public static ListFormWrapper<Container> wrapper(String projectCode) throws Exception {
		return wrapper(projectCode, null, null);
	}
	
	
	/**
	 * @param projectCode code of project
	 * @param qmode       querying mode
	 * @param render      rendering mode
	 * @return            wrapper of ContainersSearchForm
	 * @throws Exception  exception
	 */
	public static ListFormWrapper<Container> wrapper(String projectCode, QueryMode qmode, RenderMode render) throws Exception {
		ContainersSearchForm form = new ContainersSearchForm();
		form.projectCodes = new HashSet<>(Arrays.asList(projectCode));
		return new TestListFormWrapperFactory<Container>().wrapping().apply(form, qmode, render); 
	}
	
	/**
	 * No querying mode or rendering mode are defined in the created form.
	 * 
	 * @param projectCode code of project
	 * @return            wrapper of ContainersSearchForm
	 * @throws Exception  exception
	 */
	public static ListFormWrapper<ContainerSupport> wrapperSupport(String projectCode) throws Exception{
		return wrapperSupport(projectCode, null, null);
	}
	
	/**
	 * @param projectCode code of project
	 * @param qmode       querying mode
	 * @param render      rendering mode
	 * @return            wrapper of ContainersSearchForm
	 * @throws Exception  exception
	 */
	public static ListFormWrapper<ContainerSupport> wrapperSupport(String projectCode, QueryMode qmode, RenderMode render) throws Exception {
		ContainerSupportsSearchForm form = new ContainerSupportsSearchForm();
		form.projectCodes = Arrays.asList(projectCode);
		return new TestListFormWrapperFactory<ContainerSupport>().wrapping().apply(form, qmode, render);
	}
}
