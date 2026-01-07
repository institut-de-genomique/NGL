package fr.cea.ig.ngl.dao.projects;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.naming.NamingException;

import fr.cea.ig.ngl.NGLConfig;
import models.laboratory.project.instance.Members;
import models.utils.dao.DAOException;
import services.projects.members.ActiveDirectoryServicesIbfj;
import services.projects.members.ActiveDirectoryServicesOriginal;

/**
 * DAO to manage group user project.
 * Uses AD services.
 * 
 * @author ejacoby
 */
@Singleton
public class MembersProjectsDAO {

	private static final String ADMIN_TYPE="admin";
	private static final String USER_TYPE="users";
	
	private ActiveDirectoryServicesOriginal servicesOriginal;
	
	private ActiveDirectoryServicesIbfj servicesIbfj;

	private NGLConfig config;

	@Inject
	public MembersProjectsDAO(ActiveDirectoryServicesOriginal servicesOriginal, ActiveDirectoryServicesIbfj servicesIbfj, NGLConfig config) {
		this.servicesOriginal = servicesOriginal;
		this.servicesIbfj = servicesIbfj;

		this.config = config;
	}

	public Members saveObject(Members members) throws DAOException {
		try {
			String groupProjectName=null;
			String groupAdminProjectName=null;

			if(members.codeProjet!=null){
				groupProjectName=this.servicesOriginal.generateGroupName(members.codeProjet);
				groupAdminProjectName=this.servicesOriginal.generateAdminGroupName(members.codeProjet);
			}

			// Create groupe project unix

			if (config.isADIBFJEnabled()) {
				if (config.isADOriginalEnabled()) {
					int gidChosen = getFirstGidAvailable(config.getActiveDirectoryGIDNumberMin(), config.getActiveDirectoryGIDNumberMax(), config.getActiveDirectoryIbfjGIDNumberMin(), config.getActiveDirectoryIbfjGIDNumberMax());

					this.servicesOriginal.createGroup(groupProjectName, true, gidChosen);

					this.servicesIbfj.createGroup(groupProjectName, true, gidChosen);
				} else {
					this.servicesIbfj.createGroup(groupProjectName, true);
				}
			} else {
				this.servicesOriginal.createGroup(groupProjectName, true);
			}

			// Create admin group

			if (config.isADOriginalEnabled()) {
				this.servicesOriginal.createGroup(groupAdminProjectName, false);
			}

			if (config.isADIBFJEnabled()) {
				this.servicesIbfj.createGroup(groupAdminProjectName, false);
			}
		} catch (NamingException e) {
			throw new DAOException(e);
		}
		return members;
	}

	public int getFirstGidAvailable(int minIbfj, int maxIbfj, int minADHisto, int maxADHisto) throws NamingException {
		List<String> availableGIDListFromADOriginal = this.servicesOriginal.getGIDAvalaibleList(minADHisto, maxADHisto);
		List<String> availableGIDListFromADIBFJ = this.servicesIbfj.getGIDAvalaibleList(minIbfj, maxIbfj);

		List<String> availableGIDFromBothAD = availableGIDListFromADOriginal.stream()
				.distinct()
				.filter(availableGIDListFromADIBFJ::contains)
				.collect(Collectors.toList());

		if (availableGIDFromBothAD.isEmpty()) {
			throw new NamingException("No GID available in both AD.");
		}

		return Integer.valueOf(availableGIDFromBothAD.get(0));
	}

	public Members updateObject(Members members) throws DAOException {
		try {
			for(String user : MembersProjectsUtils.convertListUserMembers(members.users)){
				String groupName = this.servicesOriginal.generateGroupName(members.codeProjet);

				if (config.isADOriginalEnabled()) {
					this.servicesOriginal.addUserToGroup(user, groupName);
				}

				if (config.isADIBFJEnabled()) {
					this.servicesIbfj.addUserToGroup(user, groupName);
				}
			}

			for(String user : MembersProjectsUtils.convertListUserMembers(members.admins)){
				String adminGroupName = this.servicesOriginal.generateAdminGroupName(members.codeProjet);

				if (config.isADOriginalEnabled()) {
					this.servicesOriginal.addUserToGroup(user, adminGroupName);
				}

				if (config.isADIBFJEnabled()) {
					this.servicesIbfj.addUserToGroup(user, adminGroupName);
				}
			}
		} catch (NamingException e) {
			throw new DAOException(e);
		}
		return members;
	}

	public void removeObject(String projectCode, String login, String type) {
		if(type.equals(ADMIN_TYPE)){
			String adminGroupName = this.servicesOriginal.generateAdminGroupName(projectCode);

			if (config.isADOriginalEnabled()) {
				this.servicesOriginal.removeGroupFromUser(login, adminGroupName);
			}

			if (config.isADIBFJEnabled()) {
				this.servicesIbfj.removeGroupFromUser(login, adminGroupName);
			}
		} else if(type.equals(USER_TYPE)){
			String groupName = this.servicesOriginal.generateGroupName(projectCode);

			if (config.isADOriginalEnabled()) {
				this.servicesOriginal.removeGroupFromUser(login, groupName);
			}

			if (config.isADIBFJEnabled()) {
				this.servicesIbfj.removeGroupFromUser(login, groupName);
			}
		}
	}
	
	public Members removeObject(Members members) {
		for(String user : MembersProjectsUtils.convertListUserMembers(members.users)){
			String groupName = this.servicesOriginal.generateGroupName(members.codeProjet);

			if (config.isADOriginalEnabled()) {
				this.servicesOriginal.removeGroupFromUser(user, groupName);
			}

			if (config.isADIBFJEnabled()) {
				this.servicesIbfj.removeGroupFromUser(user, groupName);
			}
		}

		for(String user : MembersProjectsUtils.convertListUserMembers(members.admins)){
			String adminGroupName = this.servicesOriginal.generateAdminGroupName(members.codeProjet);

			if (config.isADOriginalEnabled()) {
				this.servicesOriginal.removeGroupFromUser(user, adminGroupName);
			}

			if (config.isADIBFJEnabled()) {
				this.servicesIbfj.removeGroupFromUser(user, adminGroupName);
			}
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
			members.groupName=this.servicesOriginal.generateGroupName(code);
			
			List<String> attributeNames = Arrays.asList(MembersProjectsUtils.samAccountName,MembersProjectsUtils.displayName);
			
			if (config.isADIBFJEnabled()) {
				members.users=MembersProjectsUtils.convertListMapValue(this.servicesIbfj.getMembers(members.groupName, attributeNames));
			} else if (config.isADOriginalEnabled()) {
				members.users=MembersProjectsUtils.convertListMapValue(this.servicesOriginal.getMembers(members.groupName, attributeNames));
			} else {
				throw new DAOException("Original AD and IBFJ are disabled.");
			}
			
			members.adminGroupName=this.servicesOriginal.generateAdminGroupName(code);
			
			ArrayList<String> groupList = new ArrayList<>();
			groupList.add(config.getString("ad.default.group.admin"));

			if (config.isADIBFJEnabled()) {
				members.admins= MembersProjectsUtils.convertListMapValue(this.servicesIbfj.getMembersAllProjectAdmin(attributeNames), groupList);
				members.admins.addAll(MembersProjectsUtils.convertListMapValue(this.servicesIbfj.getMembers(members.adminGroupName, attributeNames)));	
			} else if (config.isADOriginalEnabled()) {
				members.admins= MembersProjectsUtils.convertListMapValue(this.servicesOriginal.getMembersAllProjectAdmin(attributeNames), groupList);
				members.admins.addAll(MembersProjectsUtils.convertListMapValue(this.servicesOriginal.getMembers(members.adminGroupName, attributeNames)));
			} else {
				throw new DAOException("Original AD and IBFJ are disabled.");
			}
			
			return members;
		} catch (NamingException e) {
			throw new DAOException(e);
		}
	}

}
