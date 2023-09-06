package controllers.experiments.api;

import javax.inject.Inject;

import akka.stream.javadsl.Source;
import akka.util.ByteString;
import controllers.NGLController;
import fr.cea.ig.authentication.Authenticated;
import fr.cea.ig.authorization.Authorized;
import fr.cea.ig.lfw.utils.Streamer;
import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.ngl.dao.experiments.ExperimentReagentsAPI;
import fr.cea.ig.ngl.support.NGLForms;
import play.mvc.Result;

public class ExperimentReagents extends NGLController implements NGLForms {

	private final ExperimentReagentsAPI api;

	@Inject
	public ExperimentReagents(NGLApplication app, ExperimentReagentsAPI api) {
		super(app);
		this.api = api;
	}
	
	@Authenticated
	@Authorized.Read
	public Result list() {
	    return globalExceptionHandler(() -> {
			ExperimentSearchForm form = objectFromRequestQueryString(ExperimentSearchForm.class);
			Source<ByteString, ?> resultsAsStream = form.transform().apply(api.listObjects(null, form.getQuery()));
			return Streamer.okStream(resultsAsStream);
	    });
	}

}
