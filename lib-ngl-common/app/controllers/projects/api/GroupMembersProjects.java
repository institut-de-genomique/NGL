package controllers.projects.api;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import controllers.NGLController;
import fr.cea.ig.authentication.Authenticated;
import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.ngl.NGLConfig;
import fr.cea.ig.ngl.dao.projects.MembersProjectsAPI;
import fr.cea.ig.ngl.support.NGLForms;
import models.laboratory.project.instance.GroupMembers;
import play.data.Form;
import play.mvc.Result;

public class GroupMembersProjects extends NGLController implements NGLForms {

	private final NGLConfig config;
	private final MembersProjectsAPI                   membersApi;
	private final Form<GroupMembersProjectsSearchForm> searchForm;

	@Inject
	public GroupMembersProjects(NGLApplication     app, 
			                    NGLConfig          config, 
			                    MembersProjectsAPI membersApi) {
		super(app);
		this.config=config;
		this.membersApi = membersApi;
		this.searchForm = app.formFactory().form(GroupMembersProjectsSearchForm.class);
	}

	@Authenticated
	public Result list() {
		GroupMembersProjectsSearchForm form = filledFormQueryString(searchForm, GroupMembersProjectsSearchForm.class).get();
		List<GroupMembers> results = getResultFormQueryString(form);
		return okAsJson(results);
	}		

	private List<GroupMembers> getResultFormQueryString(GroupMembersProjectsSearchForm form) {
		List<GroupMembers> results = new ArrayList<>();

		if(form.applyPatternConfig && StringUtils.isNotBlank((form.ouName))){
			results.addAll(membersApi.getGroupMembers(form.ouName, config.getActiveDirectoryGroupsNameRegex()));
		}else if (StringUtils.isNotBlank((form.ouName))) {
			results.addAll(membersApi.getGroupMembers(form.ouName));
		}

		return results;
	}

}
