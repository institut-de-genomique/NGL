package fr.cea.ig.ngl.dao.projects;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.naming.NamingException;

import models.laboratory.project.instance.UserMembers;
import models.utils.dao.DAOException;
import services.projects.members.ActiveDirectoryServicesIbfj;

/**
 * API to manage group user project.
 * Use AD services.
 * 
 * @author ejacoby
 */
@Singleton
public class UserMembersProjectsDAO {

	// EJACOBY: remplacer par une interface qui permet n'importe quelle implementation 
	private final ActiveDirectoryServicesIbfj ibfjServices;

	private final List<String> attributeNames = Arrays.asList(MembersProjectsUtils.samAccountName, MembersProjectsUtils.displayName);
	
	@Inject
	public UserMembersProjectsDAO(ActiveDirectoryServicesIbfj ibfjServices) {
		this.ibfjServices = ibfjServices;
	}

	public UserMembers getObject(String code) throws DAOException {
			try {
				UserMembers userMembers = new UserMembers();
				userMembers.login       = code;
				userMembers.adminGroups = this.ibfjServices.getUserGroupMembers(code);
				return userMembers;
			} catch (NamingException e) {
				e.printStackTrace();
				
				throw new DAOException(e);
			}
	}
	
	public List<UserMembers> findByGroup(String groupName) {
		try {
			return MembersProjectsUtils.convertListMapValue(this.ibfjServices.getMembers(groupName, attributeNames));
		} catch (NamingException e) {
			e.printStackTrace();
			
			return null;
		}
	}
	
	public List<UserMembers> findByOrganizationUnit(String ouName) {
		try {
			return MembersProjectsUtils.convertListMapValue(this.ibfjServices.getMembersByOrganizationUnit(ouName, attributeNames));
		} catch (NamingException e) {
			e.printStackTrace();
			
			return null;
		}
	}
	
}

