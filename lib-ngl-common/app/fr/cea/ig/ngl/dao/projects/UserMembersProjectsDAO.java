package fr.cea.ig.ngl.dao.projects;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.naming.NamingException;

import models.laboratory.project.instance.UserMembers;
import models.utils.dao.DAOException;
import services.projects.members.ActiveDirectoryServices;

/**
 * API to manage group user project.
 * Use AD services.
 * 
 * @author ejacoby
 *
 */
@Singleton
public class UserMembersProjectsDAO {

	// EJACOBY: remplacer par une interface qui permet n'importe quelle implementation 
	private final ActiveDirectoryServices services;

	private final List<String> attributeNames = Arrays.asList(MembersProjectsUtils.samAccountName, MembersProjectsUtils.displayName);
	
	@Inject
	public UserMembersProjectsDAO(ActiveDirectoryServices services) {
		this.services = services;
	}

	public UserMembers getObject(String code) throws DAOException {
			try {
				UserMembers userMembers = new UserMembers();
				userMembers.login       = code;
				userMembers.adminGroups = services.getUserGroupMembers(code);
				return userMembers;
			} catch (NamingException e) {
				throw new DAOException(e);
			}
	}
	
	public List<UserMembers> findByGroup(String groupName) {
		try {
			return MembersProjectsUtils.convertListMapValue(services.getMembers(groupName, attributeNames));
		} catch (NamingException e) {
			throw new DAOException(e);
		}
	}
	
	public List<UserMembers> findByOrganizationUnit(String ouName) {
		try {
			return MembersProjectsUtils.convertListMapValue(services.getMembersByOrganizationUnit(ouName, attributeNames));
		} catch (NamingException e) {
			throw new DAOException(e);
		}
	}
	
}

