package controllers.projects.api;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import controllers.NGLController;
import fr.cea.ig.authentication.Authenticated;
import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.ngl.dao.projects.MembersProjectsAPI;
import fr.cea.ig.ngl.support.NGLForms;
import models.laboratory.project.instance.UserMembers;
import play.data.Form;
import play.mvc.Result;

public class UserMembersProjects extends NGLController implements NGLForms {

	private final MembersProjectsAPI membersApi;
	private final Form<UserMembersProjectsSearchForm> searchForm;

	@Inject
	public UserMembersProjects(NGLApplication app, MembersProjectsAPI membersApi) {
		super(app);
		this.membersApi=membersApi;
		this.searchForm = app.formFactory().form(UserMembersProjectsSearchForm.class);

	}

	/**
	 * Méthode permettant de récupérer les membres (admins + non admins) AD d'un projet.
	 * @param code Le code du projet où on veut récupérer les membres.
	 * @return Un objet "UserMembers" comprenant les admins du projet et les membres du projet.
	 */
	@Authenticated
	public Result get(String code) {
		UserMembers userMembers = membersApi.getUserMember(code);

		if (userMembers != null) {
			return okAsJson(userMembers);
		} else {
			return notFound();
		}

	}

	@Authenticated
	public Result list() {
		UserMembersProjectsSearchForm form = filledFormQueryString(searchForm, UserMembersProjectsSearchForm.class).get();
		List<UserMembers> results = getResultFormQueryString(form);
		return okAsJson(results);
	}		
	
	private List<UserMembers> getResultFormQueryString(UserMembersProjectsSearchForm form) {
		List<UserMembers> results = new ArrayList<>();
		
		if(StringUtils.isNotBlank(form.groupName))
			results.addAll(membersApi.getUserMemberByGroup(form.groupName));
		
		if(StringUtils.isNotBlank(form.ouName))
			results.addAll(membersApi.getUserMembersByOrganizationUnit(form.ouName));
		
		return results;
	}
}