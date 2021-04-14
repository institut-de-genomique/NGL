package fr.cea.ig.ngl.dao.api.factory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import fr.cea.ig.ngl.utils.TestUtils;
import models.laboratory.project.instance.UserMembers;

/**
 * Factory pour l'entité "UserMembers".
 * 
 * @author jcharpen - Jordi Charpentier - jcharpen@genoscope.cns.fr
 */
public final class UserMembersFactory {
	
	/**
	 * Méthode permettant de générer une liste d'objets "UserMembers" aléatoires.
	 * 
	 * @return Une liste d'objets "UserMembers" aléatoires.
	 */
	public static ArrayList<UserMembers> getRandomUserMembersList() {
		ArrayList<UserMembers> res = new ArrayList<>();

		for (int i = 0; i < TestUtils.LIST_SIZE; i++) {
			res.add(getRandomUserMembers());
		}

		return res;
	}

	/**
	 * Méthode permettant de générer un objet "UserMembers" aléatoire.
	 * 
	 * @return Un objet "UserMembers" aléatoire.
	 */
	public static UserMembers getRandomUserMembers() {
		UserMembers userMembers = new UserMembers();

		userMembers.displayName = UUID.randomUUID().toString();
		userMembers.login = UUID.randomUUID().toString();

		List<String> adminGroups = new ArrayList<>();
		adminGroups.add(UUID.randomUUID().toString());
		adminGroups.add(UUID.randomUUID().toString());

		userMembers.adminGroups = adminGroups;
		
		return userMembers;
	}
}
