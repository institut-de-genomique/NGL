package fr.cea.ig.ngl.dao.projects;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.naming.NamingException;

import fr.cea.ig.ngl.NGLConfig;
import models.laboratory.project.instance.GroupMembers;
import models.utils.dao.DAOException;
import services.projects.members.ActiveDirectoryServicesIbfj;
import services.projects.members.ActiveDirectoryServicesOriginal;

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

	private final ActiveDirectoryServicesIbfj servicesIbfj;

	private final ActiveDirectoryServicesOriginal servicesOriginal;

	private final List<String> attributeNames = Arrays.asList(MembersProjectsUtils.samAccountName, MembersProjectsUtils.displayName);
	
	private NGLConfig config;

	@Inject
	public GroupMembersProjectsDAO(ActiveDirectoryServicesIbfj servicesIbfj, ActiveDirectoryServicesOriginal servicesOriginal, NGLConfig config) {
		this.servicesIbfj=servicesIbfj;
		this.servicesOriginal=servicesOriginal;

		this.config = config;
	}

	public List<GroupMembers> findGroups(String ouName)	{
		try {
			return MembersProjectsUtils.convertListName(this.servicesIbfj.getGroups(ouName));
		} catch (NamingException e) {
			throw new DAOException(e);
		}
	}
	
	public List<GroupMembers> findGroups(String ouName, String pattern)	{
		try {
			List<GroupMembers> listGroupMembers = new ArrayList<GroupMembers>();
			List<String> groupNames = null;
			
			if (config.isADIBFJEnabled()) {
				groupNames = this.servicesIbfj.getGroups(ouName);
			} else if (config.isADOriginalEnabled()) {
				groupNames = this.servicesOriginal.getGroups(ouName);
			}	

			groupNames = groupNames.stream().filter(g->g.startsWith(pattern)).collect(Collectors.toList());
			for(String groupName : groupNames){
				GroupMembers groupMembers = new GroupMembers(groupName);
				groupMembers.displayName=groupMembers.name.replace(pattern, "");
				
				if (config.isADIBFJEnabled()) {
					groupMembers.members=MembersProjectsUtils.convertListMapValue(this.servicesIbfj.getMembers(groupName, attributeNames));
				} else if (config.isADOriginalEnabled()) {
					groupMembers.members=MembersProjectsUtils.convertListMapValue(this.servicesOriginal.getMembers(groupName, attributeNames));
				} 

				listGroupMembers.add(groupMembers);
			}
			return listGroupMembers;
		} catch (NamingException e) {
			throw new DAOException(e);
		}
	}
	
}
