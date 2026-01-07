package fr.cea.ig.ngl.dao.api.factory;

import java.util.*;

import fr.cea.ig.ngl.utils.TestUtils;
import models.laboratory.common.instance.PropertyValue;
import models.laboratory.common.instance.TraceInformation;
import models.laboratory.common.instance.property.PropertyListValue;
import models.laboratory.project.instance.BioinformaticParameters;
import models.laboratory.project.instance.Project;
import models.laboratory.project.instance.SystemParameters;
import models.laboratory.project.instance.SystemParameters.ArchiveEntry;
import models.laboratory.project.instance.SystemParameters.ArchiveEntry.Action;

/**
 * Factory pour l'entité "Project".
 * 
 * @author jcharpen - Jordi Charpentier - jcharpen@genoscope.cns.fr
 */
public final class ProjectFactory {

	/**
	 * Méthode permettant de générer une liste de projets aléatoires.
	 * 
	 * @return Une liste de projets générés.
	 */
	public static List<Project> getRandomProjectsList() {
		List<Project> projectList = new ArrayList<>();
		
		for (int i = 0; i < TestUtils.LIST_SIZE; i++) {
			projectList.add(getRandomProject(true));
		}
		
		return projectList;
	}

	public static Project getRandomProject() {
		return getRandomProject(false);
	}
	
	/**
	 * Méthode permettant de générer un objet "Projet" aléatoire.
	 * 
	 * @param withId Booléen permettant de savoir si le projet généré doit avoir un identifiant ou pas.
	 * 
	 * @return Un objet "Projet" aléatoire.
	 */
	public static Project getRandomProject(boolean withId) {
		Project project = new Project();
		
		if (withId) {
			project._id = UUID.randomUUID().toString() + "-" + new Random().nextInt();
		}

		project.state = StateFactory.getNewState();
		
		project.categoryCode = UUID.randomUUID().toString();
		project.code = UUID.randomUUID().toString();
		project.description = UUID.randomUUID().toString();
		project.lastSampleCode = UUID.randomUUID().toString();
		project.name = UUID.randomUUID().toString();
		project.nbCharactersInSampleCode = new Random().nextInt();
		project.typeCode = "typeCodeRandom";
		project.umbrellaProjectCode = UUID.randomUUID().toString();
		
		return project;
	}

	/**
	 * Méthode permettant de générer un objet "Projet" aléatoire.
	 * 
	 * @param withId Booléen permettant de savoir si le projet généré doit avoir un identifiant ou pas.
	 * @param code Le code du projet à générer.
	 * @param lastSampleCode Le "lastSampleCode" du projet à générer.
	 * @param nbLastSampleCode Le nombre de caractères du "lastSampleCode" du projet à générer.
	 * 
	 * @return Un objet "Projet" aléatoire.
	 */
	public static Project getRandomProject(boolean withId, String code, String lastSampleCode, int nbLastSampleCode) {
		Project project = getRandomProject(withId);

		project.code = code;
		project.lastSampleCode = lastSampleCode;
		project.nbCharactersInSampleCode = nbLastSampleCode;

		return project;
	}

	/**
	 * Méthode permettant de mettre à jour un mock de "Project".
	 * Utile dans certains cas bien précis où on doit mocker un project mais avec des valeurs.
	 * 
	 * @param project Le projet à mettre à jour.
	 * @param withId Booléen permettant de savoir si le projet généré doit avoir un identifiant ou pas.
	 * @param withAnalysisTypes Booléen permettant de savoir si on doit générer une propriété "analysisTypes".
	 * 
	 * @return Un objet "Project" mis à jour.
	 */
	public static Project fillRandomProject(Project project, boolean withId, boolean withAnalysisTypes) {
		project = ProjectFactory.fillRandomProject(project, true);
		
		if (withAnalysisTypes) {
			List<Object> listProp = ((PropertyListValue) project.properties.get("analysisTypes")).listValue();
			listProp.add(UUID.randomUUID().toString());

			PropertyListValue pLVal = new PropertyListValue(listProp);

			project.properties.put("analysisTypes", pLVal);
		}

		return project;
	}

	/**
	 * Méthode permettant de mettre à jour un mock de "Project".
	 * Utile dans certains cas bien précis où on doit mocker un project mais avec des valeurs.
	 * 
	 * @param project Le projet à mettre à jour.
	 * @param withId Booléen permettant de savoir si le projet généré doit avoir un identifiant ou pas.
	 * 
	 * @return Un objet "Project" mis à jour.
	 */
	public static Project fillRandomProject(Project project, boolean withId) {
		project = getRandomProject(withId);

		TraceInformation ti = new TraceInformation();
		ti.createUser = TestUtils.CURRENT_USER;
		ti.creationDate = new Date();

		project.traceInformation = ti;

		BioinformaticParameters bioInfParam = new BioinformaticParameters();
		bioInfParam.biologicalAnalysis = Boolean.FALSE;
		bioInfParam.ccrtAutomaticTransfer = Boolean.FALSE;

		project.bioinformaticParameters = bioInfParam;

		Map<String, PropertyValue> projetProperties = new HashMap<>();
		List<String> analysisTypes = new ArrayList<>();
		PropertyValue property = new PropertyListValue(analysisTypes);

		projetProperties.put("analysisTypes", property);
		project.properties = projetProperties;

		return project;
	}
	
	/**
	 * Méthode permettant de générer un projet aléatoire avec une propriété archiveHistory valide.
	 * 
	 * @return Un objet "Project" aléatoire avec la propriété archiveHistory valide.
	 */
	public static Project getRandomProjectValidArchiveHistory() {		
		ArchiveEntry archive = new ArchiveEntry();
		archive.date = new Date();
		archive.action = Action.ARCHIVAGE;
		archive.rawArchiveId = "AAA_9876543200";
		archive.projArchiveId = "AAA_9876543000";
		archive.scratchArchiveId = "AAA_9876543210";

		Project project = getRandomProject(true);	
		project.systemParameters = new SystemParameters();
		project.systemParameters.archiveHistory = Arrays.asList(archive);
		
		return project;
	}
	
	/**
	 * Méthode permettant de générer un projet aléatoire avec un id invalide dans le champ archiveHistory
	 * pour chacun des id existants.
	 * 
	 * @return Un objet "Project" aléatoire avec un ID invalide (niveau format) pour chacun des id existants.
	 */
	public static Project getRandomProjectInvalidArchiveHistoryIdFormatAll() {		
		ArchiveEntry archive = new ArchiveEntry();
		archive.date = new Date();
		archive.action = Action.ARCHIVAGE;
		archive.rawArchiveId = "INVALID";
		archive.projArchiveId = "INVALID";
		archive.scratchArchiveId = "INVALID";

		Project project = getRandomProject(true);	
		project.systemParameters = new SystemParameters();
		project.systemParameters.archiveHistory = Arrays.asList(archive);
		
		return project;
	}
	
	/**
	 * Méthode permettant de générer un projet avec une action invalide dans la propriété archiveHistory.
	 * 
	 * @return Un objet "Project" aléatoire avec un ID invalide dans les archives.
	 */
	public static Project getRandomProjectInvalidActionArchiveHistory() {
		ArchiveEntry archive = new ArchiveEntry();
		archive.date = new Date();
		archive.action = null;
		archive.rawArchiveId = "AAA_1234567191";
		archive.projArchiveId = "AAA_123456789";
		archive.scratchArchiveId = "AAA_9876543210";

		Project project = getRandomProject(true);	
		project.systemParameters = new SystemParameters();
		project.systemParameters.archiveHistory = Arrays.asList(archive);
		
		return project;
	}
}
