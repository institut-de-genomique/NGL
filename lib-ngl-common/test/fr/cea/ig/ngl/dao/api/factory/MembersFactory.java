package fr.cea.ig.ngl.dao.api.factory;

import java.util.ArrayList;
import java.util.UUID;

import models.laboratory.project.instance.Members;
import models.laboratory.project.instance.UserMembers;

/**
 * Factory pour l'entité "Members".
 * 
 * @author jcharpen - Jordi Charpentier - jcharpen@genoscope.cns.fr
 */
public final class MembersFactory {

	/**
	 * Méthode permettant de mettre à jour un mock de "Members".
	 * Utile dans certains cas bien précis où on doit mocker un membres mais avec des valeurs.
	 * 
	 * @param members Le membre à mettre à jour.
	 * @param isValid Booléen permettant de savoir si le membre généré doit être valide (ou non).
	 * 
	 * @return Un objet "Members" mis à jour.
	 */
	public static Members fillRandomMembers(Members members, boolean isValid) {
		if (!isValid) {
			members.admins = new ArrayList<UserMembers>();
			members.users = new ArrayList<UserMembers>();
		} else {
			members.admins = UserMembersFactory.getRandomUserMembersList();
			members.users = UserMembersFactory.getRandomUserMembersList();
		}

		return members;
	}
	
	/**
	 * Méthode permettant de générer un objet "Members" aléatoire.
	 * 
	 * @return Un objet "Members" aléatoire.
	 */
	public static Members getRandomMembers() {
		Members members = new Members();

		members.codeProjet = UUID.randomUUID().toString();
		members.adminGroupName = UUID.randomUUID().toString();
		members.groupName = UUID.randomUUID().toString();

		members.admins = UserMembersFactory.getRandomUserMembersList();
		
		return members;
	}

	/**
	 * Méthode permettant de générer un objet "Members" aléatoire invalide : il manque un champ obligatoire.
	 * 
	 * @return Un objet "Members" aléatoire.
	 */
	public static Members getRandomMembersInvalid() {
		Members members = new Members();

		members.codeProjet = UUID.randomUUID().toString();
		members.adminGroupName = UUID.randomUUID().toString();
		members.groupName = UUID.randomUUID().toString();
		
		return members;
	}
}
