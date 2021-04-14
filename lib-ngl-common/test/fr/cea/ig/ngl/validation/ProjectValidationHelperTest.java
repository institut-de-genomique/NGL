package fr.cea.ig.ngl.validation;

import org.jongo.MongoCursor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.ngl.dao.projects.ProjectsDAO;
import fr.cea.ig.ngl.dao.api.factory.ProjectFactory;
import fr.cea.ig.ngl.utils.TestUtils;
import models.laboratory.common.description.PropertyDefinition;
import models.laboratory.common.instance.PropertyValue;
import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.project.description.ProjectCategory;
import models.laboratory.project.description.ProjectType;
import models.laboratory.project.instance.Project;
import ngl.refactoring.MiniDAO;
import validation.ContextValidation;
import validation.common.instance.CommonValidationHelper;
import validation.project.instance.ProjectValidationHelper;
import play.Logger;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

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

	private final Map<String, PropertyDefinition> pDefMap = PowerMockito.mock(HashMap.class);

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
		assertEquals("Wrong size for AUTHORIZED_ID_FORMAT", "^[a-zA-z0-9]+_\\d{10}$",
				ProjectValidationHelper.AUTHORIZED_ID_FORMAT);
		assertEquals("Wrong value for ARCHIVAGE_ACTION", "ARCHIVAGE", ProjectValidationHelper.ARCHIVAGE_ACTION);
		assertEquals("Wrong value for DESARCHIVAGE_ACTION", "DESARCHIVAGE",
				ProjectValidationHelper.DESARCHIVAGE_ACTION);
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
		propDef1.code = "synchroProj";
		propDef1.required = true;

		propDef.add(propDef1);

		Map<String, PropertyValue> propProj = new HashMap<String, PropertyValue>();
		propProj.put("synchroProj", new PropertySingleValue(Boolean.TRUE));

		Project project = new Project();
		project.code = "AAA";
		project.typeCode = "EXEMPLE";
		project.properties = propProj;

		// Liste des champs qu'on veut mettre à jour dans le projet.
		List<String> fields = new ArrayList<String>();
		fields.add("synchroProj");

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

		assertTrue("Context has errors : " + ctxVal.getErrors(), !ctxVal.hasErrors());
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
		propDef1.code = "synchroProj";
		propDef1.required = true;

		propDef.add(propDef1);

		Map<String, PropertyValue> propProj = new HashMap<String, PropertyValue>();
		propProj.put("unixGroup", new PropertySingleValue(Boolean.TRUE));

		Project project = new Project();
		project.code = "AAA";
		project.typeCode = "EXEMPLE";
		project.properties = propProj;

		// Liste des champs qu'on veut mettre à jour dans le projet.
		List<String> fields = new ArrayList<String>();
		fields.add("properties.synchroProj.value");

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

		Field field = PowerMockito.field(ProjectType.class, "miniFind");
		field.set(ProjectType.class, supp);

		PropertyDefinition pDef = new PropertyDefinition();
		pDef.possibleValues = ProjectFactory.getPossibleActionValues();

		pDefMap.put("archiveHistory.action", pDef);

		when(CommonValidationHelper.validateCodeForeignRequired(ctxVal, ProjectType.miniFind.get(), project.typeCode,
				"typeCode", true)).thenReturn(projectType);

		Supplier<ProjectsDAO> suppProj = () -> projectDAO;

		field = PowerMockito.field(Project.class, "find");
		field.set(ProjectsDAO.class, suppProj);

		doReturn(pDefMap).when(projectType).getMapPropertyDefinition();
		doReturn(pDef).when(pDefMap).get("archiveHistory.action");

		ProjectValidationHelper.validateArchiveProperties(ctxVal, project.code, project.typeCode, project.properties,
				false);

		verify(mc, times(0)).iterator();

		assertTrue("Context has errors", !ctxVal.hasErrors());
	}

	@Test
	public void testValidateArchiveHistoryInvalidIdFormat() throws IllegalArgumentException, IllegalAccessException {
		Project project = ProjectFactory.getRandomProjectInvalidArchiveHistoryIdFormatAll();
		ProjectType projectType = PowerMockito.mock(ProjectType.class);
		Supplier<MiniDAO<ProjectType>> supp = () -> miniDao;
		ContextValidation ctxVal = ContextValidation.createCreationContext(TestUtils.CURRENT_USER);

		Field field = PowerMockito.field(ProjectType.class, "miniFind");
		field.set(ProjectType.class, supp);

		PropertyDefinition pDef = new PropertyDefinition();
		pDef.possibleValues = ProjectFactory.getPossibleActionValues();

		pDefMap.put("archiveHistory.action", pDef);

		when(CommonValidationHelper.validateCodeForeignRequired(ctxVal, ProjectType.miniFind.get(), project.typeCode,
				"typeCode", true)).thenReturn(projectType);

		doReturn(pDefMap).when(projectType).getMapPropertyDefinition();
		doReturn(pDef).when(pDefMap).get("archiveHistory.action");

		ProjectValidationHelper.validateArchiveProperties(ctxVal, project.code, project.typeCode, project.properties,
				false);

		assertTrue("Context has no error", ctxVal.hasErrors());
		assertEquals("Not the right amount of errors", 1, ctxVal.errorCount());
		assertEquals("Not the right amount of validation errors", 3,
				ctxVal.getErrors().get("properties.archiveHistory[0]").size());
	}

	@Test
	public void testValidateArchiveHistoryInvalidAction() throws IllegalArgumentException, IllegalAccessException {
		Project project = ProjectFactory.getRandomProjectInvalidActionArchiveHistory();
		ProjectType projectType = PowerMockito.mock(ProjectType.class);
		Supplier<MiniDAO<ProjectType>> supp = () -> miniDao;
		ContextValidation ctxVal = ContextValidation.createCreationContext(TestUtils.CURRENT_USER);

		Field field = PowerMockito.field(ProjectType.class, "miniFind");
		field.set(ProjectType.class, supp);

		PropertyDefinition pDef = new PropertyDefinition();
		pDef.possibleValues = ProjectFactory.getPossibleActionValues();

		pDefMap.put("archiveHistory.action", pDef);

		when(CommonValidationHelper.validateCodeForeignRequired(ctxVal, ProjectType.miniFind.get(), project.typeCode,
				"typeCode", true)).thenReturn(projectType);

		doReturn(pDefMap).when(projectType).getMapPropertyDefinition();
		doReturn(pDef).when(pDefMap).get("archiveHistory.action");

		ProjectValidationHelper.validateArchiveProperties(ctxVal, project.code, project.typeCode, project.properties,
				false);

		List<String> allIdArchives = new ArrayList<String>();
		allIdArchives.add("AAA_9876543210");

		String queryAllIds = String.join("\",\"", allIdArchives);
		queryAllIds = "\"" + queryAllIds + "\"";

		String query = "{ $or: [ { \"properties.archiveHistory.value.rawArchiveId\": { $in: [ " + queryAllIds + "] } },"
				+ "{ \"properties.archiveHistory.value.projArchiveId\": { $in: [" + queryAllIds + "] } }, "
				+ "{ \"properties.archiveHistory.value.scratchArchiveId\": { $in: [ " + queryAllIds + " ] } } ] }";

		verify(projectDAO, times(0)).findByQueryWithProjection(query,
				"{ \"code\": 1, \"properties.archiveHistory\": 1 }");
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

		Field field = PowerMockito.field(ProjectType.class, "miniFind");
		field.set(ProjectType.class, supp);

		PropertyDefinition pDef = new PropertyDefinition();
		pDef.possibleValues = ProjectFactory.getPossibleActionValues();

		pDefMap.put("archiveHistory.action", pDef);

		when(CommonValidationHelper.validateCodeForeignRequired(ctxVal, ProjectType.miniFind.get(), project.typeCode,
				"typeCode", true)).thenReturn(projectType);

		Supplier<ProjectsDAO> suppProj = () -> projectDAO;

		field = PowerMockito.field(Project.class, "find");
		field.set(ProjectsDAO.class, suppProj);

		List<String> allIdArchives = new ArrayList<String>();
		allIdArchives.add("AAA_9876543210");

		String queryAllIds = String.join("\",\"", allIdArchives);
		queryAllIds = "\"" + queryAllIds + "\"";

		String query = "{ $or: [ { \"properties.archiveHistory.value.rawArchiveId\": { $in: [ " + queryAllIds + "] } },"
				+ "{ \"properties.archiveHistory.value.projArchiveId\": { $in: [" + queryAllIds + "] } }, "
				+ "{ \"properties.archiveHistory.value.scratchArchiveId\": { $in: [ " + queryAllIds + " ] } } ] }";

		doReturn(mc).when(projectDAO).findByQueryWithProjection(query,
				"{ \"code\": 1, \"properties.archiveHistory\": 1 }");
		doReturn(ProjectFactory.getRandomProjectsList().iterator()).when(mc).iterator();

		doReturn(pDefMap).when(projectType).getMapPropertyDefinition();
		doReturn(pDef).when(pDefMap).get("archiveHistory.action");

		ProjectValidationHelper.validateArchiveProperties(ctxVal, project.code, project.typeCode, project.properties,
				false);

		verify(projectDAO, times(1)).findByQueryWithProjection(query,
				"{ \"code\": 1, \"properties.archiveHistory\": 1 }");
		verify(mc, times(1)).iterator();

		assertTrue("Context has no error", ctxVal.hasErrors());
	}

	// validateUmbrellaProjectCodeOptional() à tester dans CommonValidationHelper car ce helper ne fait que passe plat pour cette méthode.
}