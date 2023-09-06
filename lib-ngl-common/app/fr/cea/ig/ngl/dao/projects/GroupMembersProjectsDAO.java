package fr.cea.ig.ngl.dao.projects;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.naming.NamingException;

import models.laboratory.project.instance.GroupMembers;
import models.utils.dao.DAOException;
import play.Logger;
import services.projects.members.ActiveDirectoryServices;

/**
 * API to manage group user project.
 * Use AD services.
 * 
 * @author ejacoby
 *
 */
@Singleton
public class GroupMembersProjectsDAO {

	// EJACOBY: remplacer par une interface qui permet n'importe quelle implementation 
	private final ActiveDirectoryServices services;

	private final List<String> attributeNames = Arrays.asList(MembersProjectsUtils.samAccountName, MembersProjectsUtils.displayName);
	
	@Inject
	public GroupMembersProjectsDAO(ActiveDirectoryServices services) {
		this.services=services;
	}

	public List<GroupMembers> findGroups(String ouName)	{
		try {
			return MembersProjectsUtils.convertListName(services.getGroups(ouName));
		} catch (NamingException e) {
			throw new DAOException(e);
		}
	}
	
	public List<GroupMembers> findGroups(String ouName, String pattern)	{
		try {
			List<GroupMembers> listGroupMembers = new ArrayList<GroupMembers>();
			List<String> groupNames = services.getGroups(ouName);
			groupNames = groupNames.stream().filter(g->g.startsWith(pattern)).collect(Collectors.toList());
			for(String groupName : groupNames){
				Logger.debug("Group name "+groupName);
				GroupMembers groupMembers = new GroupMembers(groupName);
				groupMembers.displayName=groupMembers.name.replace(pattern, "");
				groupMembers.members=MembersProjectsUtils.convertListMapValue(services.getMembers(groupName, attributeNames));
				listGroupMembers.add(groupMembers);
			}
			return listGroupMembers;
		} catch (NamingException e) {
			throw new DAOException(e);
		}
	}
	
}
