package fr.cea.ig.ngl.dao.projects;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.naming.NamingException;

import models.laboratory.project.instance.GroupMembers;
import models.utils.dao.DAOException;
import services.projects.members.ActiveDirectoryServices;

/**
 * API to manage group user project
 * Use AD services
 * @author ejacoby
 *
 */
@Singleton
public class GroupMembersProjectsDAO{

	//TODO remplacer par une interface qui permet n'importe quelle implementation 
	private final ActiveDirectoryServices services;

	@Inject
	public GroupMembersProjectsDAO(ActiveDirectoryServices services) {
		super();
		this.services=services;
	}

	public List<GroupMembers> findGroups(String ouName)
	{
		try {
			return MembersProjectsUtils.convertListName(services.getGroups(ouName));
		} catch (NamingException e) {
			throw new DAOException(e);
		}
	}
	
}
