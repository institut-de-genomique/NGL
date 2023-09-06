package controllers.descriptions.tpl;

import javax.inject.Inject;

import controllers.NGLController;
import fr.cea.ig.authentication.Authenticated;
import fr.cea.ig.authorization.Authorized;
import fr.cea.ig.lfw.Historized;
import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.ngl.support.NGLForms;
import fr.cea.ig.ngl.support.NGLJavascript;
import play.mvc.Result;
import views.html.descriptions.home;
import views.html.descriptions.instruments;
import views.html.descriptions.imports;
import views.html.descriptions.protocols;
import views.html.descriptions.experiments;
import views.html.descriptions.mappingprojects;
import views.html.descriptions.newmappingprojects;
import views.html.descriptions.processes;
import views.html.descriptions.detailsprotocol;

public class Descriptions extends NGLController implements NGLJavascript, NGLForms {

	private final home home;
	private final instruments instruments;
	private final imports imports;
	private final protocols protocols;
	private final experiments experiments;
	private final mappingprojects mappingprojects;
	private final newmappingprojects newmappingprojects;
	private final processes processes;
	private final detailsprotocol detailsprotocol;

	@Inject
	public Descriptions(NGLApplication app, home home, instruments instruments, imports imports, protocols protocols,
			experiments experiments, mappingprojects mappingprojects, processes processes, newmappingprojects newmappingprojects,
			detailsprotocol detailsprotocol) {
		super(app);
		this.home = home;
		this.instruments = instruments;
		this.imports = imports;
		this.protocols = protocols;
		this.experiments = experiments;
		this.mappingprojects = mappingprojects;
		this.newmappingprojects = newmappingprojects;
		this.processes = processes;
		this.detailsprotocol = detailsprotocol;

	}

	@Authenticated
	@Historized
	@Authorized.Read
	public Result home(String code) {
		return ok(home.render(code));
	}

	public Result instruments() {
		return ok(instruments.render());
	}

	public Result imports() {
		return ok(imports.render());
	}

	public Result protocols() {
		return ok(protocols.render());
	}

	public Result detailsProtocol() {
		return ok(detailsprotocol.render());
	}

	public Result experiments() {
		return ok(experiments.render());
	}

	public Result mappingProjects() {
		return ok(mappingprojects.render());
	}

	public Result newMappingProjects() {
		return ok(newmappingprojects.render());
	}

	public Result processes() {
		return ok(processes.render());
	}

	public Result javascriptRoutes() {
		return jsRoutes(
				controllers.descriptions.tpl.routes.javascript.Descriptions.home(),
				controllers.descriptions.tpl.routes.javascript.Descriptions.instruments(),
				controllers.descriptions.tpl.routes.javascript.Descriptions.imports(),
				controllers.descriptions.tpl.routes.javascript.Descriptions.protocols(),
				controllers.descriptions.tpl.routes.javascript.Descriptions.experiments(),
				controllers.descriptions.tpl.routes.javascript.Descriptions.mappingProjects(),
				controllers.descriptions.tpl.routes.javascript.Descriptions.newMappingProjects(),
				controllers.descriptions.tpl.routes.javascript.Descriptions.processes(),
				controllers.descriptions.tpl.routes.javascript.Descriptions.detailsProtocol(),
				controllers.instruments.api.routes.javascript.Instruments.list(),
				controllers.instruments.api.routes.javascript.InstrumentCategories.list(),
				controllers.instruments.api.routes.javascript.InstrumentUsedTypes.list(),
				controllers.instruments.api.routes.javascript.Instruments.update(),
				controllers.commons.api.routes.javascript.PropertyDefinitions.list(),
				controllers.commons.api.routes.javascript.Parameters.get(),
				controllers.commons.api.routes.javascript.Parameters.list(),
				controllers.commons.api.routes.javascript.Parameters.save(),
				controllers.samples.api.routes.javascript.ImportTypes.list(),
				controllers.protocols.api.routes.javascript.Protocols.list(),
				controllers.protocols.api.routes.javascript.Protocols.get(),
				controllers.protocols.api.routes.javascript.Protocols.delete(),
				controllers.protocols.api.routes.javascript.Protocols.update(),
				controllers.protocols.api.routes.javascript.Protocols.save(),
				controllers.commons.api.routes.javascript.CommonInfoTypes.list(),
				controllers.experiments.api.routes.javascript.ExperimentTypes.list(),
				controllers.experiments.api.routes.javascript.ExperimentTypeNodes.list(),
				controllers.projects.api.routes.javascript.Projects.list(),
				controllers.processes.api.routes.javascript.ProcessTypes.list(),
				controllers.processes.api.routes.javascript.ProcessCategories.list(),
				controllers.descriptionHistories.api.routes.javascript.DescriptionHistories.save(),
				controllers.descriptionHistories.api.routes.javascript.DescriptionHistories.get(),
				controllers.experiments.api.routes.javascript.Experiments.list());
	}

}
