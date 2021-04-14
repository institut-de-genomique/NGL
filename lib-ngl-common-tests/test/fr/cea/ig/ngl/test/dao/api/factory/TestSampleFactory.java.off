package fr.cea.ig.ngl.test.dao.api.factory;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import controllers.samples.api.SamplesSearchForm;
import fr.cea.ig.ngl.support.ListFormWrapper;
import fr.cea.ig.ngl.test.resource.RConstant;
import fr.cea.ig.play.test.DevAppTesting;
import models.laboratory.common.instance.Comment;
import models.laboratory.common.instance.TraceInformation;
import models.laboratory.sample.instance.Sample;

/**
 * Factory to create Sample objects.
 * 
 * @author ajosso
 *
 */
public class TestSampleFactory {

//	private static final play.Logger.ALogger logger = play.Logger.of(TestSampleFactory.class);

	public static Sample sample(String user, String projectCode) {
	    Sample s           = new Sample();
	    s.code             = DevAppTesting.newCode(); // "TEST";
	    s.categoryCode     = "default";
	    s.importTypeCode   = "default-import";
	    s.name             = "Sample de test";
	    Set<String> pcodes = new TreeSet<>();
	    pcodes.add(projectCode);
	    s.projectCodes     = pcodes;
	    s.traceInformation = new TraceInformation(user);
	    s.typeCode         = "DNA";
	    s.comments         = comments(user);
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
	 * @throws Exception  exception 
	 */
	public static ListFormWrapper<Sample> wrapper(String projectCode) throws Exception{
		return wrapper(projectCode, null, null);
	}
	
	/**
	 * @param projectCode code of project
	 * @param qmode       querying mode
	 * @param render      rendering mode
	 * @return            wrapper of SamplesSearchForm
	 * @throws Exception  exception
	 */
	public static ListFormWrapper<Sample> wrapper(String projectCode, QueryMode qmode, RenderMode render) throws Exception {
		SamplesSearchForm form = new SamplesSearchForm();
		form.projectCodes      = Arrays.asList(projectCode);
		// Actually the second part of the request returns no results only the first part is important
		form.reportingQuery = "{$or:"
		        + "["
		            + "{$and:["
		                + "{\"typeCode\":\"DNA\","
		                + "\"comments.createUser\":{$in:[\""+RConstant.USER+"\"]}"
		                + "}"
		                + "]},"
		            + "{\"typeCode\":\"plankton\","
		            + "\"properties.taraOA.value\":{$in:[\"OA053\",\"OA054\",\"OA055\",\"OA068\",\"OA069\",\"OA070\",\"OA093\"]},"
		            + "\"properties.taraProtocol.value\":{$in:[\"S023\",\"S320\",\"S20\",\"S300\"]}"
		            + "}"
		        + "]"
		        + "}";
		return new TestListFormWrapperFactory<Sample>().wrapping().apply(form, qmode, render);
	}
	
}
