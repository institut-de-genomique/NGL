package fr.cea.ig.ngl.validation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

import org.jongo.MongoCursor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mongojack.DBQuery;
import org.mongojack.DBQuery.Query;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.ngl.dao.api.factory.ProjectFactory;
import fr.cea.ig.ngl.dao.projects.ProjectsDAO;
import fr.cea.ig.ngl.utils.TestUtils;
import models.laboratory.common.description.PropertyDefinition;
import models.laboratory.common.instance.PropertyValue;
import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.project.description.ProjectCategory;
import models.laboratory.project.description.ProjectType;
import models.laboratory.project.instance.BioinformaticParameters;
import models.laboratory.project.instance.Project;
import ngl.refactoring.MiniDAO;
import play.Logger;
import validation.ContextValidation;
import validation.common.instance.CommonValidationHelper;
import validation.project.instance.ProjectValidationHelper;

/**
 * Test du helper de validation de l'entité PROJECT.
 * 
 * @author jcharpen - Jordi Charpentier - jcharpen@genoscope.cns.fr
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ ProjectType.class, CommonValidationHelper.class, Project.class, MongoDBDAO.class,
		ProjectCategory.class })
@SuppressWarnings("unchecked")
public class ProjectValidationHelperTest {

	private final MiniDAO<ProjectType> miniDao = PowerMockito.mock(MiniDAO.class);

	private final ProjectsDAO projectDAO = PowerMockito.mock(ProjectsDAO.class);

	private final MongoCursor<Project> mc = PowerMockito.mock(MongoCursor.class);

	/**
	 * Si on veut faire des traitements avant l'exécution de la suite de tests. 
	 * Par exemple : initialiser un mock, initialiser une méthode statique, ...
	 */
	@Before
	public void setUp() {
		PowerMockito.mockStatic(ProjectType.class);
		PowerMockito.mockStatic(CommonValidationHelper.class);
		PowerMockito.mockStatic(MongoDBDAO.class);
		PowerMockito.mockStatic(ProjectCategory.class);
	}

	@Test
	public void testStaticFields() {
		assertEquals("Wrong size for AUTHORIZED_SIZE", 10, ProjectValidationHelper.AUTHORIZED_SIZE);
		assertEquals("Wrong size for AUTHORIZED_ID_FORMAT", "^[a-zA-Z0-9]+_\\d{10}$",
				ProjectValidationHelper.AUTHORIZED_ID_FORMAT);
		assertEquals("Wrong value for AUTHORIZED_ID_FORMAT", "UNKNOWN_ID", ProjectValidationHelper.UNKNOWN_ID);
	}

	@Test
	public void testValidateProjectCategoryCodeRequiredValid() {
		ContextValidation ctxVal = ContextValidation.createCreationContext(TestUtils.CURRENT_USER);
		String categoryCode = "test-category-code";

		Supplier<MiniDAO<ProjectCategory>> supp = () -> PowerMockito.mock(MiniDAO.class);

		Field field = PowerMockito.field(ProjectCategory.class, "miniFind");

		try {
			field.set(ProjectCategory.class, supp);
		} catch (IllegalArgumentException e) {
			Logger.error("Exception occured during testValidateProjectCategoryCodeRequiredValid()");
			fail(e.getMessage());
		} catch (IllegalAccessException e) {
			Logger.error("Exception occured during testValidateProjectCategoryCodeRequiredValid()");
			fail(e.getMessage());
		}

		ProjectValidationHelper.validateProjectCategoryCodeRequired(ctxVal, categoryCode);

		assertTrue("Context has errors", !ctxVal.hasErrors());
	}

	@Test
	public void testValidateUmbrellaProjectCodeOptionalInvalid() {
		ContextValidation ctxVal = ContextValidation.createCreationContext(TestUtils.CURRENT_USER);

		when(MongoDBDAO.checkObjectExist(any(), any(), any())).thenReturn(false);

		ProjectValidationHelper.validateUmbrellaProjectCodeOptional(ctxVal, UUID.randomUUID().toString());

		assertTrue("Context has no error", ctxVal.hasErrors());
	}

	@Test
	public void testValidatePropertiesRequiredValid() throws IllegalArgumentException, IllegalAccessException {
		ContextValidation ctxVal = ContextValidation.createUpdateContext(TestUtils.CURRENT_USER);

		Supplier<MiniDAO<ProjectType>> supp = () -> miniDao;

		Field field = PowerMockito.field(ProjectType.class, "miniFind");
		field.set(ProjectType.class, supp);

		/**
		 * Liste des propriétés possibles. => On met la même propriété que dans le
		 * projet qu'on met à jour pour avoir un test qui passe.
		 */
		List<PropertyDefinition> propDef = new ArrayList<PropertyDefinition>();

		PropertyDefinition propDef1 = new PropertyDefinition();
		propDef1.code = "genre";
		propDef1.required = true;

		propDef.add(propDef1);

		Map<String, PropertyValue> propProj = new HashMap<String, PropertyValue>();
		propProj.put("genre", new PropertySingleValue("payant"));

		Project project = new Project();
		project.code = "AAA";
		project.typeCode = "EXEMPLE";
		project.properties = propProj;

		// Liste des champs qu'on veut mettre à jour dans le projet.
		List<String> fields = new ArrayList<String>();
		fields.add("genre");

		try {
			ProjectType projectType = PowerMockito.mock(ProjectType.class);
			projectType.propertiesDefinitions = propDef;

			PowerMockito.doReturn(projectType)
						.when(CommonValidationHelper.class, "validateCodeForeignRequired", anyObject(), anyObject(), anyString(), anyString(), anyBoolean());
		} catch (Exception e) {
			Logger.error("Exception occured during testValidatePropertiesRequiredValid()");
			fail(e.getMessage());
		}

		ProjectValidationHelper.validatePropertiesRequired(ctxVal, project.typeCode, project.properties, fields);

		assertTrue("Context has errors : ", !ctxVal.hasErrors());
	}

	@Test
	public void testValidatePropertiesRequiredInvalid() {
		ContextValidation ctxVal = ContextValidation.createUpdateContext(TestUtils.CURRENT_USER);

		Supplier<MiniDAO<ProjectType>> supp = () -> miniDao;

		Field field = PowerMockito.field(ProjectType.class, "miniFind");

		try {
			field.set(ProjectType.class, supp);
		} catch (IllegalArgumentException e) {
			Logger.error("Exception occured during testUpdateValid()");
			fail(e.getMessage());
		} catch (IllegalAccessException e) {
			Logger.error("Exception occured during testUpdateValid()");
			fail(e.getMessage());
		}

		/**
		 * Liste des propriétés possibles. => On fait exprès de mettre une propriété qui
		 * n'existe pas dans le projet qu'on met à jour.
		 */
		List<PropertyDefinition> propDef = new ArrayList<PropertyDefinition>();

		PropertyDefinition propDef1 = new PropertyDefinition();
		propDef1.code = "genre";
		propDef1.required = true;

		propDef.add(propDef1);

		Map<String, PropertyValue> propProj = new HashMap<String, PropertyValue>();
		propProj.put("bioProjectManager", new PropertySingleValue(TestUtils.CURRENT_USER));

		Project project = new Project();
		project.code = "AAA";
		project.typeCode = "EXEMPLE";
		project.properties = propProj;

		// Liste des champs qu'on veut mettre à jour dans le projet.
		List<String> fields = new ArrayList<String>();
		fields.add("properties.genre.value");

		try {
			ProjectType projectType = PowerMockito.mock(ProjectType.class);

			when(CommonValidationHelper.validateCodeForeignRequired(ctxVal, ProjectType.miniFind.get(), project.typeCode,
				"typeCode", true)).thenReturn(projectType);
			when(projectType.propertiesDefinitions).thenReturn(propDef);
		} catch (Exception e) {
			Logger.error("Exception occured during testValidatePropertiesRequiredInvalid()");
			Logger.error("e : " + e);
			fail(e.getMessage());
		}
		
		ProjectValidationHelper.validatePropertiesRequired(ctxVal, project.typeCode, project.properties, fields);
	
		assertTrue("Context has no errors", ctxVal.hasErrors());
	}

	@Test
	public void testValidateArchiveHistoryValid() throws IllegalArgumentException, IllegalAccessException {
		Project project = ProjectFactory.getRandomProject(true);
		ProjectType projectType = PowerMockito.mock(ProjectType.class);
		Supplier<MiniDAO<ProjectType>> supp = () -> miniDao;
		ContextValidation ctxVal = ContextValidation.createCreationContext(TestUtils.CURRENT_USER);
		ctxVal.putObject("projectCode", project.code);

		Field field = PowerMockito.field(ProjectType.class, "miniFind");
		field.set(ProjectType.class, supp);

		when(CommonValidationHelper.validateCodeForeignRequired(ctxVal, ProjectType.miniFind.get(), project.typeCode,
				"typeCode", true)).thenReturn(projectType);
		
		Supplier<ProjectsDAO> suppProj = () -> projectDAO;

		field = PowerMockito.field(Project.class, "find");
		field.set(ProjectsDAO.class, suppProj);

		Project validProject = ProjectFactory.getRandomProjectValidArchiveHistory();
		
		doReturn(mc).when(projectDAO).find(any());

		ProjectValidationHelper.validateArchiveHistory(ctxVal, validProject.systemParameters.archiveHistory);

		verify(mc, times(1)).forEach(any());

		assertTrue("Context has errors" + ctxVal.getErrors(), !ctxVal.hasErrors());
	}

	@Test
	public void testValidateArchiveHistoryInvalidIdFormat() throws IllegalArgumentException, IllegalAccessException {
		Project project = ProjectFactory.getRandomProjectInvalidArchiveHistoryIdFormatAll();
		ProjectType projectType = PowerMockito.mock(ProjectType.class);
		Supplier<MiniDAO<ProjectType>> supp = () -> miniDao;
		ContextValidation ctxVal = ContextValidation.createCreationContext(TestUtils.CURRENT_USER);
		ctxVal.putObject("projectCode", project.code);

		Field field = PowerMockito.field(ProjectType.class, "miniFind");
		field.set(ProjectType.class, supp);

		when(CommonValidationHelper.validateCodeForeignRequired(ctxVal, ProjectType.miniFind.get(), project.typeCode,
				"typeCode", true)).thenReturn(projectType);

		Project invalidProject = ProjectFactory.getRandomProjectInvalidArchiveHistoryIdFormatAll();

		ProjectValidationHelper.validateArchiveHistory(ctxVal, invalidProject.systemParameters.archiveHistory);

		assertTrue("Context has no error", ctxVal.hasErrors());
		assertEquals("Not the right amount of errors", 1, ctxVal.errorCount());
		assertEquals("Not the right amount of validation errors", 3,
				ctxVal.getErrors().get("systemParameters.archiveHistory[0]").size());
	}

	@Test
	public void testValidateArchiveHistoryInvalidAction() throws IllegalArgumentException, IllegalAccessException {
		Project project = ProjectFactory.getRandomProjectInvalidActionArchiveHistory();
		ProjectType projectType = PowerMockito.mock(ProjectType.class);
		Supplier<MiniDAO<ProjectType>> supp = () -> miniDao;
		ContextValidation ctxVal = ContextValidation.createCreationContext(TestUtils.CURRENT_USER);
		ctxVal.putObject("projectCode", project.code);

		Field field = PowerMockito.field(ProjectType.class, "miniFind");
		field.set(ProjectType.class, supp);

		when(CommonValidationHelper.validateCodeForeignRequired(ctxVal, ProjectType.miniFind.get(), project.typeCode,
				"typeCode", true)).thenReturn(projectType);

		Project invalidProject = ProjectFactory.getRandomProjectInvalidActionArchiveHistory();

		ProjectValidationHelper.validateArchiveHistory(ctxVal, invalidProject.systemParameters.archiveHistory);

		List<String> allIdArchives = new ArrayList<String>();
		allIdArchives.add("AAA_9876543210");

		Query query = DBQuery.and(
				DBQuery.or(
						DBQuery.in("systemParameters.archiveHistory.rawArchiveId", allIdArchives),
						DBQuery.in("systemParameters.archiveHistory.projArchiveId", allIdArchives),
						DBQuery.in("systemParameters.archiveHistory.scratchArchiveId", allIdArchives)
					),
					DBQuery.notEquals("code", invalidProject.code)
				);

		verify(projectDAO, times(0)).find(query);
		verify(mc, times(0)).iterator();

		assertTrue("Context has no error", ctxVal.hasErrors());
		
		verifyNoMoreInteractions(projectDAO);
		verifyNoMoreInteractions(mc);
	}

	@Test
	public void testValidateArchiveHistoryInvalidId() throws IllegalArgumentException, IllegalAccessException {
		Project project = ProjectFactory.getRandomProjectValidArchiveHistory();
		ProjectType projectType = PowerMockito.mock(ProjectType.class);
		Supplier<MiniDAO<ProjectType>> supp = () -> miniDao;
		ContextValidation ctxVal = ContextValidation.createCreationContext(TestUtils.CURRENT_USER);
		ctxVal.putObject("projectCode", project.code);

		Field field = PowerMockito.field(ProjectType.class, "miniFind");
		field.set(ProjectType.class, supp);

		when(CommonValidationHelper.validateCodeForeignRequired(ctxVal, ProjectType.miniFind.get(), project.typeCode,
				"typeCode", true)).thenReturn(projectType);

		Supplier<ProjectsDAO> suppProj = () -> projectDAO;

		field = PowerMockito.field(Project.class, "find");
		field.set(ProjectsDAO.class, suppProj);

		List<String> allIdArchives = new ArrayList<String>();
		allIdArchives.add("AAA_9876543210");

		doReturn(mc).when(projectDAO).find(any());
		doReturn(ProjectFactory.getRandomProjectsList().iterator()).when(mc).iterator();

		Project validProject = ProjectFactory.getRandomProjectInvalidArchiveHistoryIdFormatAll();

		ProjectValidationHelper.validateArchiveHistory(ctxVal, validProject.systemParameters.archiveHistory);


		assertTrue("Context has no error", ctxVal.hasErrors());
	}

	@Test
	public void testValidateBioformaticParametersRequiredValid() {
		ContextValidation ctxVal = ContextValidation.createCreationContext(TestUtils.CURRENT_USER);
		BioinformaticParameters bioInfParams = new BioinformaticParameters();

		ProjectValidationHelper.validateBioformaticParametersRequired(ctxVal, bioInfParams);

		assertTrue("Context has error", !ctxVal.hasErrors());
	}

	@Test
	public void testValidateBioformaticParametersRequiredInvalid() {
		ContextValidation ctxVal = ContextValidation.createCreationContext(TestUtils.CURRENT_USER);
		BioinformaticParameters bioInfParams = null;

		ProjectValidationHelper.validateBioformaticParametersRequired(ctxVal, bioInfParams);

		assertTrue("Context has no error", ctxVal.hasErrors());
	}

	// @Test
	// Test à corriger : on a un soucis de validation sur ValidationHelper, typage de la propriété (suite MEP BI).
	public void testValidateProjectTypeCodeRequiredValid() throws IllegalArgumentException, IllegalAccessException {
		String typeCode = "default-project";

		ProjectType projectType = PowerMockito.mock(ProjectType.class);
		Supplier<MiniDAO<ProjectType>> supp = () -> miniDao;
		ContextValidation ctxVal = ContextValidation.createCreationContext(TestUtils.CURRENT_USER);

		Field field = PowerMockito.field(ProjectType.class, "miniFind");
		field.set(ProjectType.class, supp);

		Map<String, PropertyValue> propProj = new HashMap<String, PropertyValue>();
		propProj.put("genre", new PropertySingleValue("payant"));

		PropertyDefinition pDef = new PropertyDefinition();
		pDef.code = "genre";
		pDef.valueType = "java.lang.String";

		List<PropertyDefinition> pDefList = new ArrayList<>();
		pDefList.add(pDef);

		when(CommonValidationHelper.validateCodeForeignRequired(ctxVal, ProjectType.miniFind.get(), typeCode,
				"typeCode", true)).thenReturn(projectType);

		doReturn(pDefList).when(projectType).getPropertiesDefinitionProjectLevel();
		
		ProjectValidationHelper.validateProjectTypeCodeRequired(ctxVal, typeCode, propProj);

		assertTrue("Context has error", !ctxVal.hasErrors());
	}

	@Test
	public void testValidateProjectTypeCodeRequiredInvalid() throws IllegalArgumentException, IllegalAccessException {
		String badType = "bad-type";

		ContextValidation ctxVal = ContextValidation.createCreationContext(TestUtils.CURRENT_USER);

		Supplier<MiniDAO<ProjectType>> supp = () -> miniDao;

		Field field = PowerMockito.field(ProjectType.class, "miniFind");
		field.set(ProjectType.class, supp);

		when(CommonValidationHelper.validateCodeForeignRequired(ctxVal, ProjectType.miniFind.get(), badType,
				"typeCode", true)).thenReturn(null);

		ProjectValidationHelper.validateProjectTypeCodeRequired(ctxVal, badType, null);

		assertTrue("Context has error", !ctxVal.hasErrors());
	}
}