package fr.cea.ig.ngl.dao.api.factory;

import java.util.*;

import fr.cea.ig.ngl.utils.TestUtils;
import models.laboratory.common.description.Value;
import models.laboratory.common.instance.PropertyValue;
import models.laboratory.common.instance.TraceInformation;
import models.laboratory.common.instance.property.PropertyListValue;
import models.laboratory.project.instance.BioinformaticParameters;
import models.laboratory.project.instance.Project;

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
		Map<String, Object> archive = new HashMap<String, Object>();
		archive.put("date", new Date());
		archive.put("action", "ARCHIVAGE");
		archive.put("rawArchiveId", "AAA_123256789");
		archive.put("projArchiveId", "AAA_123456789");
		archive.put("scratchArchiveId", "AAA_9876543210");
		
		List<Map<String, Object>> pMapVal = new ArrayList<Map<String, Object>>();
		pMapVal.add(archive);

		PropertyListValue pListVal = new PropertyListValue(pMapVal);

		Project project = getRandomProject(true);
		project.properties = new HashMap<String, PropertyValue>();
		project.properties.put("archiveHistory", pListVal);		
		
		return project;
	}
	
	/**
	 * Méthode permettant de générer un projet aléatoire avec un id invalide dans le champ archiveHistory
	 * pour chacun des id existants.
	 * 
	 * @return Un objet "Project" aléatoire avec un ID invalide (niveau format) pour chacun des id existants.
	 */
	public static Project getRandomProjectInvalidArchiveHistoryIdFormatAll() {
		Map<String, Object> archive = new HashMap<String, Object>();
		archive.put("date", new Date());
		archive.put("action", "ARCHIVAGE");
		archive.put("rawArchiveId", "INVALID");
		archive.put("projArchiveId", "AAA_123456789");
		archive.put("scratchArchiveId", "9876543210");
		
		List<Map<String, Object>> pMapVal = new ArrayList<Map<String, Object>>();
		pMapVal.add(archive);

		PropertyListValue pListVal = new PropertyListValue(pMapVal);

		Project project = getRandomProject(true);
		project.properties = new HashMap<String, PropertyValue>();
		project.properties.put("archiveHistory", pListVal);		
		
		return project;
	}
	
	/**
	 * Méthode permettant de générer un projet avec une action invalide dans la propriété archiveHistory.
	 * 
	 * @return Un objet "Project" aléatoire avec un ID invalide dans les archives.
	 */
	public static Project getRandomProjectInvalidActionArchiveHistory() {
		Map<String, Object> archive = new HashMap<String, Object>();
		archive.put("date", new Date());
		archive.put("action", "INVALID");
		archive.put("rawArchiveId", "AAA_1234567191");
		archive.put("projArchiveId", "AAA_1234563891");
		archive.put("scratchArchiveId", "AAA_1234567891");
		
		List<Map<String, Object>> pMapVal = new ArrayList<Map<String, Object>>();
		pMapVal.add(archive);

		PropertyListValue pListVal = new PropertyListValue(pMapVal);

		Project project = getRandomProject(true);
		project.properties = new HashMap<String, PropertyValue>();
		project.properties.put("archiveHistory", pListVal);		
		
		return project;
	}
	
	/**
	 * Méthode permettant de retourner une liste de valeurs possibles pour la propertyDefinition "archiveHistory.action".
	 * 
	 * @return Une liste de valeurs possibles pour la propertyDefinition "archiveHistory.action".
	 */
	public static List<Value> getPossibleActionValues() {
		Value value1 = new Value();
		value1.value = "ARCHIVAGE";

		Value value2 = new Value();
		value2.value = "DESARCHIVAGE";

		List<Value> possibleValues = new ArrayList<Value>();
		possibleValues.add(value1);
		possibleValues.add(value2);
		
		return possibleValues;
	}
}
