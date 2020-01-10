package fr.cea.ig.ngl.test.dao.api.factory;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import controllers.containers.api.ContainerSupportsSearchForm;
import controllers.containers.api.ContainersSearchForm;
import fr.cea.ig.lfw.LFWApplication;
import fr.cea.ig.lfw.support.LFWRequestParsing;
import fr.cea.ig.ngl.support.ListFormWrapper;
import models.laboratory.common.instance.Comment;
import models.laboratory.common.instance.State;
import models.laboratory.common.instance.TraceInformation;
import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.container.instance.Container;
import models.laboratory.container.instance.ContainerSupport;
import models.laboratory.container.instance.Content;
import models.laboratory.container.instance.LocationOnContainerSupport;
import models.laboratory.project.instance.Project;
import models.laboratory.sample.instance.Sample;

public class TestContainerFactory {

	private static final play.Logger.ALogger logger = play.Logger.of(TestContainerFactory.class);
	
	public static Container container(String user, Double vol, Double quantity, Project proj, Sample s, ContainerSupport containerSupport) {
		Container c = new Container();
		c.code = "TEST";
		c.categoryCode = "tube";
		c.concentration = new PropertySingleValue(quantity/vol, "ng/µl");
		c.volume = new PropertySingleValue(vol, "µl");
		c.quantity = new PropertySingleValue(quantity, "ng");
		c.comments = comments(user);
		c.projectCodes.add(proj.code);
		c.sampleCodes.add(s.code);
		c.traceInformation = new TraceInformation(user);
		c.contents = contents(s, proj.code);
		c.support = support(containerSupport);
		c.state = state(user);
		return c;
	}
	
	private static State state(String user) {
		State s = new State("IW-P", user);
		s.date = new Date();
		return s;
	}

	private static LocationOnContainerSupport support(ContainerSupport containerSupport) {
		LocationOnContainerSupport s = new LocationOnContainerSupport();
		s.code = containerSupport.code;
		s.categoryCode = containerSupport.categoryCode;
		return s;
	}

	private static List<Content> contents(Sample s, String projectCode) {
		Content c = new Content(s.code, s.typeCode, s.categoryCode);
		c.percentage = new Double(100);
		c.projectCode = projectCode;
		return Arrays.asList(c);
	}

	private static List<Comment> comments(String user){
		return Arrays.asList(new Comment("very usefull comments", user));
	}
	
	
	public static ContainerSupport containerSupport(String user, Project proj, Sample s) {
		ContainerSupport cs = new ContainerSupport();
		cs.code = "TEST";
		cs.categoryCode = "tube";
		cs.projectCodes.add(proj.code);
		cs.comments = comments(user);
		cs.sampleCodes.add(s.code);
		cs.nbContainers = 1;
		cs.nbContents = 1;
		//cs.storageCode = "Bt20_70_A1";
		return cs;
	}
	
	/**
	 * No querying mode or rendering mode are defined in the created form.
	 * 
	 * @param projectCode code of project
	 * @return            wrapper of ContainersSearchForm
	 */
	public static ListFormWrapper<Container> wrapper(String projectCode){
		return wrapper(projectCode, null, null);
	}
	
	/**
	 * @param projectCode code of project
	 * @param qmode       querying mode
	 * @param render      rendering mode
	 * @return            wrapper of ContainersSearchForm
	 */
	public static ListFormWrapper<Container> wrapper(String projectCode, QueryMode qmode, RenderMode render) {
		ContainersSearchForm form = new ContainersSearchForm();
		form.projectCodes = new HashSet<>(Arrays.asList(projectCode));
		
		if(render != null ) {
			form.list = (render.equals(RenderMode.LIST)) ? true : false;
			form.datatable = (render.equals(RenderMode.DATATABLE)) ? true : false;
			form.count = (render.equals(RenderMode.COUNT)) ? true : false;
		} else {
			logger.debug("no rendering mode defined in form");
		}
		
		if(qmode != null) {
			form.aggregate = (qmode.equals(QueryMode.AGGREGATE)) ? true : false;
			form.reporting = (qmode.equals(QueryMode.REPORTING)) ? true : false;
		} else {
			logger.debug("no querying mode defined in form");
		}
		// unnecessary block because MongoJack request mode is the default one.
		/*if(qmode.equals(QueryMode.MONGOJACK)) {
			form.aggregate = false;
			form.reporting = false;
		}*/
		
		ListFormWrapper<Container> wrapper = new ListFormWrapper<>(form, 
				f -> new LFWRequestParsing() {
					@Override
					public LFWApplication getLFWApplication() { return null;}
				}.generateBasicDBObjectFromKeys(f));
		return wrapper;
	}
	
	/**
	 * No querying mode or rendering mode are defined in the created form.
	 * 
	 * @param projectCode code of project
	 * @return            wrapper of ContainersSearchForm
	 */
	public static ListFormWrapper<ContainerSupport> wrapperSupport(String projectCode){
		return wrapperSupport(projectCode, null, null);
	}
	
	/**
	 * @param projectCode code of project
	 * @param qmode       querying mode
	 * @param render      rendering mode
	 * @return            wrapper of ContainersSearchForm
	 */
	public static ListFormWrapper<ContainerSupport> wrapperSupport(String projectCode, QueryMode qmode, RenderMode render) {
		ContainerSupportsSearchForm form = new ContainerSupportsSearchForm();
		form.projectCodes = Arrays.asList(projectCode);
		
		if(render != null ) {
			form.list = (render.equals(RenderMode.LIST)) ? true : false;
			form.datatable = (render.equals(RenderMode.DATATABLE)) ? true : false;
			form.count = (render.equals(RenderMode.COUNT)) ? true : false;
		} else {
			logger.debug("no rendering mode defined in form");
		}
		
		if(qmode != null) {
			form.aggregate = (qmode.equals(QueryMode.AGGREGATE)) ? true : false;
			form.reporting = (qmode.equals(QueryMode.REPORTING)) ? true : false;
		} else {
			logger.debug("no querying mode defined in form");
		}
		// unnecessary block because MongoJack request mode is the default one.
		/*if(qmode.equals(QueryMode.MONGOJACK)) {
			form.aggregate = false;
			form.reporting = false;
		}*/
		
		ListFormWrapper<ContainerSupport> wrapper = new ListFormWrapper<>(form, 
				f -> new LFWRequestParsing() {
					@Override
					public LFWApplication getLFWApplication() { return null;}
				}.generateBasicDBObjectFromKeys(f));
		return wrapper;
	}
}
