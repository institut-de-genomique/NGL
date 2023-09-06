package controllers.projects.api;

import javax.inject.Inject;

import controllers.NGLController;
import fr.cea.ig.authentication.Authenticated;
import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.ngl.NGLConfig;
import fr.cea.ig.ngl.dao.api.APIValidationException;
import fr.cea.ig.ngl.dao.projects.MembersProjectsAPI;
import fr.cea.ig.ngl.support.NGLForms;
import models.laboratory.project.instance.Members;
import play.data.Form;
import play.mvc.Result;

/**
 * Controller around member project manage
 * To define access to project directory
 * @author ejacoby
 *
 */
public class MembersProjects extends NGLController implements NGLForms{

	private final MembersProjectsAPI membersApi;

	private final Form<Members> membersForm;

	private final NGLConfig config;

	@Inject
	public MembersProjects(NGLApplication app, MembersProjectsAPI membersApi, NGLConfig config) {
		super(app);
		this.membersApi=membersApi;
		this.membersForm=app.formFactory().form(Members.class);
		this.config=config;
	}

	@Authenticated
	public Result save() {
		try {
			Members membersInput = getFilledForm(membersForm, Members.class).get();
			Members members = membersApi.create(membersInput, getCurrentUser());
			return okAsJson(members);
		} catch (APIValidationException e) {
			return badRequestLoggingForValidationException(e);
		}
	}

	@Authenticated
	public Result update() {
		try {
			Members membersInput = getFilledForm(membersForm, Members.class).get();
			Members members = membersApi.update(membersInput, getCurrentUser());
			return okAsJson(members);
		} catch (APIValidationException e) {
			return badRequestLoggingForValidationException(e);
		}
	}

	@Authenticated
	public Result delete(String projectCode, String code, String type)
	{
		membersApi.delete(projectCode, code, type);
		return ok();
	}

	@Authenticated
	/**
	 * 
	 * @param code : project code of Project instance
	 * @return
	 */
	public Result get(String code) {
		//Only if config OK 
		//TODO remove for CNS implementation
		if(config.isCNGInstitute()){
			Members members = membersApi.get(code);
			if(members != null) {
				return okAsJson(members);
			} else {
				return notFound();
			}
		}else{
			return ok();
		}

	}


}
