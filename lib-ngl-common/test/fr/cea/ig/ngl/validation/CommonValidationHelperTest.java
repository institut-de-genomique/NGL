package fr.cea.ig.ngl.validation;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mongojack.DBQuery;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.ngl.dao.api.factory.ContainerFactory;
import fr.cea.ig.ngl.dao.api.factory.ContainerSupportFactory;
import fr.cea.ig.ngl.dao.api.factory.StateFactory;
import fr.cea.ig.ngl.utils.TestUtils;
import models.administration.authorisation.User;
import models.administration.authorisation.description.dao.UserDAO;
import models.laboratory.container.instance.Container;
import models.laboratory.container.instance.ContainerSupport;
import models.utils.InstanceConstants;
import play.Logger;
import validation.ContextValidation;
import validation.common.instance.CommonValidationHelper;

/**
 * Test du helper de validation commun.
 * 
 * @author jcharpen - Jordi Charpentier - jcharpen@genoscope.cns.fr
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ MongoDBDAO.class,User.class})
public class CommonValidationHelperTest {


    private final UserDAO userDAO = PowerMockito.mock(UserDAO.class);
    /**
	 * Si on veut faire des traitements avant l'exécution de la suite de tests. 
     * Par exemple : initialiser un mock, initialiser une méthode statique, ...
	 */
    @Before
	public void setUp() {
        PowerMockito.mockStatic(MongoDBDAO.class);
        PowerMockito.mockStatic(UserDAO.class);
        PowerMockito.mockStatic(User.class);

	}
    
    @Test
    public void testValidateCodeSizeValid() {
        ContextValidation contextValidation = ContextValidation.createCreationContext(TestUtils.CURRENT_USER);
        ContainerSupport containerSupport = ContainerSupportFactory.getRandomContainerSupport(true, false, StateFactory.getRandomState());

        CommonValidationHelper.validateFieldMaxSize(contextValidation, "code", containerSupport.code, 30);

        assertTrue("Context has errors", !contextValidation.hasErrors());
    }

    @Test
    public void testValidateCodeSizeInvalid() {
        ContextValidation contextValidation = ContextValidation.createCreationContext(TestUtils.CURRENT_USER);
        ContainerSupport containerSupport = ContainerSupportFactory.getRandomContainerSupportWithSizeMoreThan30();

        CommonValidationHelper.validateFieldMaxSize(contextValidation, "code", containerSupport.code, 30);

        assertTrue("Context has no errors", contextValidation.hasErrors());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testValidateUniqueInstanceCodeValid() {
        ContextValidation contextValidation = ContextValidation.createCreationContext(TestUtils.CURRENT_USER);

        when(MongoDBDAO.checkObjectExistByCode(any(String.class), any(Class.class), any(String.class))).thenReturn(Boolean.TRUE);

        CommonValidationHelper.validateUniqueInstanceCode(contextValidation, "test", Container.class, InstanceConstants.CONTAINER_COLL_NAME);

        assertTrue("Context has no errors", contextValidation.hasErrors());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testValidateUniqueInstanceCodeInvalid() {
        ContextValidation contextValidation = ContextValidation.createCreationContext(TestUtils.CURRENT_USER);

        when(MongoDBDAO.checkObjectExistByCode(any(String.class), any(Class.class), any(String.class))).thenReturn(Boolean.FALSE);

        CommonValidationHelper.validateUniqueInstanceCode(contextValidation, "test", Container.class, InstanceConstants.CONTAINER_COLL_NAME);

        assertTrue("Context has errors", !contextValidation.hasErrors());
    }

    @Test
    public void testValidateUniqueFieldValueValid() {
        Container container = ContainerFactory.getRandomContainer(StateFactory.getFinishState().code);
        ContextValidation contextValidation = ContextValidation.createCreationContext(TestUtils.CURRENT_USER);

        when(MongoDBDAO.checkObjectExist(InstanceConstants.CONTAINER_COLL_NAME, Container.class, "code", container.code)).thenReturn(Boolean.FALSE);

        CommonValidationHelper.validateUniqueFieldValue(contextValidation, "code", container.code, Container.class, InstanceConstants.CONTAINER_COLL_NAME);

        assertTrue("Context has errors", !contextValidation.hasErrors());
    }

    @Test
    public void testValidateUniqueFieldValueInvalid() {
        Container container = ContainerFactory.getRandomContainer(StateFactory.getFinishState().code);
        ContextValidation contextValidation = ContextValidation.createCreationContext(TestUtils.CURRENT_USER);

        when(MongoDBDAO.checkObjectExist(InstanceConstants.CONTAINER_COLL_NAME, Container.class, "code", container.code)).thenReturn(Boolean.TRUE);

        CommonValidationHelper.validateUniqueFieldValue(contextValidation, "code", container.code, Container.class, InstanceConstants.CONTAINER_COLL_NAME);

        assertTrue("Context has no error", contextValidation.hasErrors());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testValidateResolutionCodesValid() {
        String typeCode = "rsillumina";
        ContextValidation ctxVal = ContextValidation.createUndefinedContext(TestUtils.CURRENT_USER);

        Set<String> resoCodes = new HashSet<String>();
        resoCodes.add("Run-abandonLane");

        when(MongoDBDAO.checkObjectExist(any(String.class), any(Class.class), any(DBQuery.Query.class))).thenReturn(Boolean.TRUE);

        CommonValidationHelper.validateResolutionCodes(ctxVal, typeCode, resoCodes);

        assertTrue("Context has errors", !ctxVal.hasErrors());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testValidateResolutionCodesInvalid() {
        String typeCode = "rsillumina";
        ContextValidation ctxVal = ContextValidation.createUndefinedContext(TestUtils.CURRENT_USER);

        Set<String> resoCodes = new HashSet<String>();
        resoCodes.add("Run-abandonLane");

        when(MongoDBDAO.checkObjectExist(any(String.class), any(Class.class), any(DBQuery.Query.class))).thenReturn(Boolean.FALSE);

        CommonValidationHelper.validateResolutionCodes(ctxVal, typeCode, resoCodes);

        assertTrue("Context has no error", ctxVal.hasErrors());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testValidateCriteriaCodeValid() {
        String typeCode = "RHS2000";
        String criteriaCode = "VC-RHS2000-PE-101-v1";

        ContextValidation ctxVal = ContextValidation.createUndefinedContext(TestUtils.CURRENT_USER);

        when(MongoDBDAO.checkObjectExist(any(String.class), any(Class.class), any(DBQuery.Query.class))).thenReturn(Boolean.TRUE);

        CommonValidationHelper.validateCriteriaCode(ctxVal, typeCode, criteriaCode);

        assertTrue("Context has errors", !ctxVal.hasErrors());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testValidateCriteriaCodeInvalid() {
        String typeCode = "RHS2000";
        String criteriaCode = "VC-RHS2000-PE-101-v1";

        ContextValidation ctxVal = ContextValidation.createUndefinedContext(TestUtils.CURRENT_USER);

        when(MongoDBDAO.checkObjectExist(any(String.class), any(Class.class), any(DBQuery.Query.class))).thenReturn(Boolean.FALSE);

        CommonValidationHelper.validateCriteriaCode(ctxVal, typeCode, criteriaCode);

        assertTrue("Context has no error", ctxVal.hasErrors());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testvalidateStringFieldValid(){
        String name = "protcoleName";

        ContextValidation contextValidation =ContextValidation.createUndefinedContext(TestUtils.CURRENT_USER);

        when(MongoDBDAO.checkObjectExist(any(String.class), any(Class.class), any(DBQuery.Query.class))).thenReturn(Boolean.TRUE);

        CommonValidationHelper.validateStringField(contextValidation, name,"name");

        assertTrue("Context has errors", !contextValidation.hasErrors());

    }

    @Test
    @SuppressWarnings("unchecked")
    public void testvalidateStringFieldInvalid(){
        String name = null;

        ContextValidation contextValidation =ContextValidation.createUndefinedContext(TestUtils.CURRENT_USER);

        when(MongoDBDAO.checkObjectExist(any(String.class), any(Class.class), any(DBQuery.Query.class))).thenReturn(Boolean.TRUE);

        CommonValidationHelper.validateStringField(contextValidation, name,"name");

        assertTrue("Context has errors", contextValidation.hasErrors());

    }
    @Test
    public void testValidateUserValid(){
        
        String login = TestUtils.CURRENT_USER ;
        User user = mock(User.class);
        ContextValidation contextValidation =ContextValidation.createUpdateContext(TestUtils.CURRENT_USER);

        Logger.error(""+user.login);
        Supplier<UserDAO> supp = () -> userDAO;


        
       when(userDAO.findByLogin(login)).thenReturn(user);

        Field field = PowerMockito.field(User.class, "find");
        try {
            field.set(UserDAO.class, supp);
        } catch (IllegalArgumentException e) {
            Logger.error("testValidateUserValid");
			fail(e.getMessage());
        } catch (IllegalAccessException e) {
            Logger.error("testValidateUserValid");
			fail(e.getMessage());
        }
      

       CommonValidationHelper.validateUser(contextValidation,login);
       
        assertTrue("Context has errors", !contextValidation.hasErrors());
    }

    @Test
    public void testValidateUserInvalid(){
        String login = null;

        ContextValidation contextValidation =ContextValidation.createUpdateContext(TestUtils.CURRENT_USER);
        CommonValidationHelper.validateUser(contextValidation,login);

        assertTrue("Context has errors", contextValidation.hasErrors());
    }
}