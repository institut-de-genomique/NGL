package fr.cea.ig.ngl.test.dao.api.factory;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import controllers.samples.api.SamplesSearchForm;
import fr.cea.ig.lfw.LFWApplication;
import fr.cea.ig.lfw.support.LFWRequestParsing;
import fr.cea.ig.ngl.support.ListFormWrapper;
import models.laboratory.common.instance.Comment;
import models.laboratory.common.instance.TraceInformation;
import models.laboratory.project.instance.Project;
import models.laboratory.sample.instance.Sample;

/**
 * Factory to create Sample objects
 * @author ajosso
 *
 */
public class TestSampleFactory {

	private static final play.Logger.ALogger logger = play.Logger.of(TestSampleFactory.class);
			
	public static Sample sample(String user) {
		Project proj = TestProjectFactory.project(user);
		return sample(user, proj);
	}

	public static Sample sample(String user, Project proj) {
		Sample s = new Sample();
		s.code = "TEST";
		s.categoryCode = "default";
		s.importTypeCode = "default-import";
		s.name = "Sample de test";
		Set<String> pcodes = new TreeSet<>();
		pcodes.add(proj.code);
		s.projectCodes = pcodes;
		s.traceInformation = new TraceInformation(user);
		s.typeCode = "DNA";
		s.comments = comments(user);
		return s;
	}

	private static List<Comment> comments(String user){
		return Arrays.asList(new Comment("very usefull comments", user));
	}

	/**
	 * No querying mode or rendering mode are defined in the created form.
	 * 
	 * @param projectCode code of project
	 * @return            wrapper of SamplesSearchForm
	 */
	public static ListFormWrapper<Sample> wrapper(String projectCode){
		return wrapper(projectCode, null, null);
	}
	
	/**
	 * @param projectCode code of project
	 * @param qmode       querying mode
	 * @param render      rendering mode
	 * @return            wrapper of SamplesSearchForm
	 */
	public static ListFormWrapper<Sample> wrapper(String projectCode, QueryMode qmode, RenderMode render) {
		SamplesSearchForm form = new SamplesSearchForm();
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
		
		ListFormWrapper<Sample> wrapper = new ListFormWrapper<>(form, 
				f -> new LFWRequestParsing() {
					@Override
					public LFWApplication getLFWApplication() { return null;}
				}.generateBasicDBObjectFromKeys(f));
		return wrapper;
	}
}
