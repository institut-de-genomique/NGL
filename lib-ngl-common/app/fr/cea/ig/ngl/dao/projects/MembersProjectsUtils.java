package fr.cea.ig.ngl.dao.projects;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import models.laboratory.project.instance.GroupMembers;
import models.laboratory.project.instance.UserMembers;

public class MembersProjectsUtils {

	public static final String samAccountName = "SamAccountName";
	public static final String displayName    = "DisplayName";
	
	/**
	 * Convert UserMember into login UserMember.
	 * @param userMembers user data
	 * @return            list of user logins
	 */
	public static List<String> convertListUserMembers(List<UserMembers> userMembers) {
		return userMembers.stream().map(um->um.login).collect(Collectors.toList());
	}
	
	/**
	 * Convert login into UserMember instance.
	 * @param logins list of logins
	 * @return       list of user data
	 */
	public static List<UserMembers> convertListLogin(List<String> logins) {
		return logins.stream().map(l->new UserMembers(l)).collect(Collectors.toList());
	}
	
	public static List<GroupMembers> convertListName(List<String> names) {
		return names.stream().map(n->new GroupMembers(n)).collect(Collectors.toList());
	}
	
	public static List<UserMembers> convertListMapValue(List<Map<String, String>> attributeValues, ArrayList<String>... groupList) {
		return attributeValues.stream().map(av->new UserMembers(av.get(MembersProjectsUtils.samAccountName),av.get(MembersProjectsUtils.displayName), groupList.length == 1 ? groupList[0] : null)).collect(Collectors.toList());
	}	
}
