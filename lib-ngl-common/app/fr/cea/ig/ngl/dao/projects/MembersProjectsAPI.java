package fr.cea.ig.ngl.dao.projects;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import fr.cea.ig.ngl.dao.api.APIValidationException;
import models.laboratory.project.instance.GroupMembers;
import models.laboratory.project.instance.Members;
import models.laboratory.project.instance.UserMembers;
import validation.ContextValidation;

/**
 * API to manage member user project
 * 
 * @author ejacoby
 *
 */
@Singleton
public class MembersProjectsAPI {

	public static final String groupMode = "group";
	public static final String organizationUnitMode = "organizationalUnit";
	
	private final MembersProjectsDAO dao;
	private final UserMembersProjectsDAO userDao;
	private final GroupMembersProjectsDAO groupDao;

	@Inject
	public MembersProjectsAPI(MembersProjectsDAO dao, UserMembersProjectsDAO userDao, GroupMembersProjectsDAO groupDao) {
		this.dao      = dao;
		this.userDao  = userDao;
		this.groupDao = groupDao;
	}

	public Members create(Members members, String currentUser) throws APIValidationException {
//		ContextValidation ctxVal = new ContextValidation(currentUser);
//		ctxVal.setCreationMode();
		ContextValidation ctxVal = ContextValidation.createCreationContext(currentUser);
		members.validate(ctxVal);
		if (ctxVal.hasErrors())
			throw new APIValidationException("invalid input", ctxVal.getErrors());
		return dao.saveObject(members);
	}

	public Members update(Members members, String currentUser) throws APIValidationException {
//		ContextValidation ctxVal = new ContextValidation(currentUser); 
//		ctxVal.setUpdateMode();
	    ContextValidation ctxVal = ContextValidation.createUpdateContext(currentUser);
		members.validate(ctxVal);
		if (ctxVal.hasErrors()) 
			throw new APIValidationException("Invalid Members object", ctxVal.getErrors());		
		dao.updateObject(members);
		return members;
	}
	
	public void delete(String projectCode, String login, String type)
	{
		dao.removeObject(projectCode, login, type);
	}

	
	/**
	 * Get members by project code.
	 * @param code code of Project instance
	 * @return     project members
	 */
	public Members get(String code){
		return dao.getObject(code);
	}
	
	/**
	 * Get UserMembers of Project. 
	 * @param login user login
	 * @return      user members
	 */
	public UserMembers getUserMember(String login) {
		return userDao.getObject(login);
	}
	
	/**
	 * Get all UserMembers from group.
	 * @param groupName group name
	 * @return          user members
	 */
	public List<UserMembers> getUserMemberByGroup(String groupName) {
		return userDao.findByGroup(groupName);
	}
	
	public List<UserMembers> getUserMembersByOrganizationUnit(String ouName) {
		return userDao.findByOrganizationUnit(ouName);
	}
	
	/**
	 * Get all group from organization unit.
	 * @param ouName organization unit name
	 * @return       groups of the organization unit
	 */
	public List<GroupMembers> getGroupMembers(String ouName) {
		return groupDao.findGroups(ouName);
	}
	
	public List<GroupMembers> getGroupMembers(String ouName, String pattern)
	{
		return groupDao.findGroups(ouName, pattern);
	}
	
}
