package scripts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.naming.NamingException;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.lfw.controllers.scripts.chunked.ScriptWithArgs;
import fr.cea.ig.ngl.NGLConfig;
import fr.cea.ig.ngl.dao.projects.MembersProjectsAPI;
import fr.cea.ig.ngl.dao.projects.MembersProjectsDAO;
import fr.cea.ig.ngl.dao.projects.MembersProjectsUtils;
import models.laboratory.project.instance.Members;
import models.laboratory.project.instance.Project;
import models.utils.InstanceConstants;
import play.Logger;
import services.projects.members.ActiveDirectoryServicesIbfj;
import services.projects.members.ActiveDirectoryServicesOriginal;

/**
 * @author jcharpen - Jordi CHARPENTIER - jcharpen@genoscope.cns.fr
 */
public class ScriptUpdateMissingADGroup extends ScriptWithArgs<ScriptUpdateMissingADGroup.Args> {

	public static class Args {

	}

	private ActiveDirectoryServicesIbfj adIbfjServices;

	private ActiveDirectoryServicesOriginal oldAdServices;

	private MembersProjectsDAO membersProjectsDAO;

	private MembersProjectsAPI membersProjectsAPI;

	private NGLConfig config;

	private boolean simulation = false;

	@Inject
	public ScriptUpdateMissingADGroup(NGLConfig config, MembersProjectsAPI membersProjectsAPI, MembersProjectsDAO membersProjectsDAO, ActiveDirectoryServicesIbfj adIbfjServices, ActiveDirectoryServicesOriginal oldAdServices) {
		super();

		this.config = config;
		this.adIbfjServices = adIbfjServices;
		this.membersProjectsAPI = membersProjectsAPI;
		this.membersProjectsDAO = membersProjectsDAO;
		this.oldAdServices = oldAdServices;
	}

	private void createADGroup() throws NamingException {
		List<Project> allProjects = MongoDBDAO.find(InstanceConstants.PROJECT_COLL_NAME, Project.class).toList();

		for (int i = 0; i < allProjects.size(); i++) {
			Project project = allProjects.get(i);

			String projectCodeLowercase = project.code.toLowerCase();

			String groupName = this.adIbfjServices.generateGroupName(projectCodeLowercase);

			int gidChosen = this.membersProjectsDAO.getFirstGidAvailable(config.getActiveDirectoryGIDNumberMin(), config.getActiveDirectoryGIDNumberMax(), config.getActiveDirectoryIbfjGIDNumberMin(), config.getActiveDirectoryIbfjGIDNumberMax());

			try {
				if (isGroupExist(groupName, true)) {
					Logger.error("Groupe '" + groupName + "' existe déjà pour le projet '" + project.code + "'.");
				} else {
					if (!simulation) {
						this.adIbfjServices.createGroup(groupName, true, gidChosen);
						Logger.error("Groupe '" + groupName + "' créé pour le projet '" + project.code + "' sur l'AD IBFJ.");
					} else {
						Logger.error("Simulation - Groupe '" + groupName + "' créé pour le projet '" + project.code + "' sur l'AD IBFJ.");
					}
				}

				if (isGroupExist(groupName, false)) {
					Logger.error("Groupe '" + groupName + "' existe déjà pour le projet '" + project.code + "'.");
				} else {
					if (!simulation) {
						this.oldAdServices.createGroup(groupName, true, gidChosen);
						Logger.error("Groupe '" + groupName + "' créé pour le projet '" + project.code + "' sur l'AD original.");
					} else {
						Logger.error("Simulation - Groupe '" + groupName + "' créé pour le projet '" + project.code + "' sur l'AD original.");
					}	
				}			
			} catch (NamingException e) {
				Logger.error("Le groupe '" + groupName + "' existe déjà sur un des deux AD.");
			} 

			String adminGroupName = this.adIbfjServices.generateAdminGroupName(projectCodeLowercase);

			try {
				if (isGroupExist(adminGroupName, true)) {
					Logger.error("Groupe '" + adminGroupName + "' existe déjà pour le projet '" + project.code + "'.");
				} else {
					if (!simulation) {
						this.adIbfjServices.createGroup(adminGroupName, false);
						Logger.error("Groupe '" + adminGroupName + "' créé pour le projet '" + project.code + "'.");
					} else {
						Logger.error("Simulation - Groupe '" + adminGroupName + "' créé pour le projet '" + project.code + "'.");
					}
				}

				if (isGroupExist(adminGroupName, false)) {
					Logger.error("Groupe '" + adminGroupName + "' existe déjà pour le projet '" + project.code + "'.");
				} else {
					if (!simulation) {
						this.oldAdServices.createGroup(adminGroupName, false);
						Logger.error("Groupe '" + adminGroupName + "' créé pour le projet '" + project.code + "'.");
					} else {
						Logger.error("Simulation - Groupe '" + adminGroupName + "' créé pour le projet '" + project.code + "'.");
					}	
				}			
			} catch (NamingException e) {
				Logger.error("Le groupe '" + adminGroupName + "' existe déjà sur un des deux AD.");
			} 

			List<String> attributeNames = Arrays.asList(MembersProjectsUtils.samAccountName,MembersProjectsUtils.displayName);

			ArrayList<String> groupList = new ArrayList<>();
			// groupList.add(config.getString("ad.default.group.admin"));

			Members members = this.membersProjectsAPI.get(projectCodeLowercase);

			// Obligé de rappeler ça car ça va chercher dans l'AD IBFJ et pas dans l'ancien AD. Forcément, c'est vide.
			members.admins= MembersProjectsUtils.convertListMapValue(this.oldAdServices.getMembersAllProjectAdmin(attributeNames), groupList);
			members.admins.addAll(MembersProjectsUtils.convertListMapValue(this.oldAdServices.getMembers(members.adminGroupName, attributeNames)));

			members.users=MembersProjectsUtils.convertListMapValue(this.oldAdServices.getMembers(members.groupName, attributeNames));

			List<String> defaultMembers = new ArrayList<String>();
				
			try {
				defaultMembers.addAll(this.oldAdServices.getMembers(config.getActiveDirectoryDefaultGroupAccess()));
			} catch (NamingException e) {
				Logger.error("Erreur lors de la récupération des membres du groupe : '" + config.getActiveDirectoryDefaultGroupAccess() + "'.");
				e.printStackTrace();
			}

			for (int j = 0; j < members.users.size(); j++) {
				if (!defaultMembers.contains(members.users.get(j).login)) {
					try {
						this.adIbfjServices.addUserToGroup(members.users.get(j).login, groupName);
						Logger.error("Ajout du membre '" + members.users.get(j).login + "' au groupe '" + groupName + "'.");
					} catch (NamingException e) {
						Logger.error("Erreur lors de l'ajout de l'utilisateur '" + members.users.get(j).login + "' au groupe '" + groupName + "'.");
						e.printStackTrace();
					} 
				} else {
					Logger.error("'" + members.users.get(j).login + "'' n'est pas ajouté au groupe '" + groupName + "'.");
				}			
			}	

			List<String> defaultAdmins = new ArrayList<String>();
				
			try {
				defaultAdmins = this.oldAdServices.getMembers(config.getActiveDirectoryDefaultGroupAdmin());
			} catch (NamingException e) {
				Logger.error("Erreur lors de la récupération des membres du groupe : '" + config.getActiveDirectoryDefaultGroupAdmin() + "'.");
				e.printStackTrace();
			}

			for (int j = 0; j < members.admins.size(); j++) {
				if (!defaultAdmins.contains(members.admins.get(j).login)) {
					 try {
						this.adIbfjServices.addUserToGroup(members.admins.get(j).login, adminGroupName);
						Logger.error("Ajout de l'admin '" + members.admins.get(j).login + "' au groupe '" + adminGroupName + "'.");
					} catch (NamingException e) {
						Logger.error("Erreur lors de l'ajout de l'admin '" + members.admins.get(j).login + "' au groupe '" + adminGroupName + "'.");
						e.printStackTrace();
					} 	
				} else {
					Logger.error("'" + members.admins.get(j).login + "' n'est pas ajouté au groupe '" + adminGroupName + "'.");
				}	
			} 
		}

		Logger.error("end createADGroup");
	}

	private boolean isGroupExist(String groupName, boolean searchIBFJ) {
		boolean exist = true;
		
		if (searchIBFJ) {
			try {
				this.adIbfjServices.getMembers(groupName);
			} catch (NamingException e) {
				// La cause la plus probable de l'erreur ici c'est que le groupe n'existe pas.

				exist = false;
			}
		} else {
			try {
				this.oldAdServices.getMembers(groupName);
			} catch (NamingException e) {
				// La cause la plus probable de l'erreur ici c'est que le groupe n'existe pas.

				exist = false;
			}
		}		

		return exist;
	}

	@Override
	public void execute(Args args) throws NamingException {
		Logger.debug("Start update missing AD Group");
		
		createADGroup();

		Logger.debug("End update missing AD Group");
	}
}