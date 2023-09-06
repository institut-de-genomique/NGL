package fr.cea.ig.ngl.dao.projects;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.naming.NamingException;

import fr.cea.ig.ngl.NGLConfig;
import models.laboratory.project.instance.Members;
import models.utils.dao.DAOException;
import services.projects.members.ActiveDirectoryServices;

/**
 * DAO to manage group user project.
 * Uses AD services.
 * 
 * @author ejacoby
 *
 */
@Singleton
public class MembersProjectsDAO {

	private static final String ADMIN_TYPE="admin";
	private static final String USER_TYPE="users";
	
	// EJACOBY: remplacer par une interface qui permet n'importe quelle implementation 
	private final ActiveDirectoryServices services;
	
	private NGLConfig config;

	@Inject
	public MembersProjectsDAO(ActiveDirectoryServices services, NGLConfig config) {
		this.services = services;
		this.config = config;
	}

	public Members saveObject(Members members) throws DAOException {
		try {
			String groupProjectName=null;
			String groupAdminProjectName=null;

			if(members.codeProjet!=null){
				groupProjectName=services.generateGroupName(members.codeProjet);
				groupAdminProjectName=services.generateAdminGroupName(members.codeProjet);
			}

			//Create groupe project unix
			services.createGroup(groupProjectName, true);

			//Create admin group
			services.createGroup(groupAdminProjectName, false);
		} catch (NamingException e) {
			throw new DAOException(e);
		}
		return members;
	}

	public Members updateObject(Members members) throws DAOException {
		//Add list utilisateurs au groupe
		try {
			for(String user : MembersProjectsUtils.convertListUserMembers(members.users)){
				String groupName = services.generateGroupName(members.codeProjet);
				services.addUserToGroup(user, groupName);
			}
			for(String user : MembersProjectsUtils.convertListUserMembers(members.admins)){
				String adminGroupName = services.generateAdminGroupName(members.codeProjet);
				services.addUserToGroup(user, adminGroupName);
			}
		} catch (NamingException e) {
			throw new DAOException(e);
		}
		return members;
	}

	public void removeObject(String projectCode, String login, String type)
	{
		if(type.equals(ADMIN_TYPE)){
			String adminGroupName = services.generateAdminGroupName(projectCode);
			services.removeGroupFromUser(login, adminGroupName);
		}else if(type.equals(USER_TYPE)){
			String groupName = services.generateGroupName(projectCode);
			services.removeGroupFromUser(login, groupName);
		}
	}
	
	public Members removeObject(Members members){
		for(String user : MembersProjectsUtils.convertListUserMembers(members.users)){
			String groupName = services.generateGroupName(members.codeProjet);
			services.removeGroupFromUser(user, groupName);
		}
		for(String user : MembersProjectsUtils.convertListUserMembers(members.admins)){
			String adminGroupName = services.generateAdminGroupName(members.codeProjet);
			services.removeGroupFromUser(user, adminGroupName);
		}
		return members;
	}
	

	/**
	 * Get members of a project.
	 * @param code          code of Project instance
	 * @return              members of a project
	 * @throws DAOException DAO error
	 */
	public Members getObject(String code) throws DAOException {
		try {
			Members members = new Members();
			members.codeProjet=code;
			members.groupName=services.generateGroupName(code);
			List<String> attributeNames = Arrays.asList(MembersProjectsUtils.samAccountName,MembersProjectsUtils.displayName);
			members.users=MembersProjectsUtils.convertListMapValue(services.getMembers(members.groupName, attributeNames));
			members.adminGroupName=services.generateAdminGroupName(code);
			ArrayList<String> groupList = new ArrayList<>();
			groupList.add(config.getString("ad.default.group.admin"));
			members.admins= MembersProjectsUtils.convertListMapValue(services.getMembersAllProjectAdmin(attributeNames), groupList);
			members.admins.addAll(MembersProjectsUtils.convertListMapValue(services.getMembers(members.adminGroupName, attributeNames)));
			
			return members;
		} catch (NamingException e) {
			throw new DAOException(e);
		}
	}

}
