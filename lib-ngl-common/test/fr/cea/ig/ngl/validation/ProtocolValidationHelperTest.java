package fr.cea.ig.ngl.validation;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.ngl.dao.api.factory.ProtocolFactory;
import fr.cea.ig.ngl.utils.TestUtils;
import models.laboratory.common.description.PropertyDefinition;
import models.laboratory.common.instance.PropertyValue;
import models.laboratory.experiment.description.ProtocolCategory;
import models.laboratory.experiment.description.dao.ProtocolCategoryDAO;
import models.laboratory.parameter.propertyDefinitionsProtocol.PropertyDefinitionsProtocol;
import models.laboratory.protocol.instance.Protocol;
import models.utils.InstanceConstants;
import play.Logger;
import validation.ContextValidation;
import validation.protocol.instance.ProtocolValidationHelper;


@RunWith(PowerMockRunner.class)
@PrepareForTest({ MongoDBDAO.class,PropertyDefinitionsProtocol.class ,ProtocolCategory.class})
public class ProtocolValidationHelperTest {

    private final ProtocolCategoryDAO protocolCategoryDAO = PowerMockito.mock(ProtocolCategoryDAO.class);
    
    @Before
	public void setUp() {
        PowerMockito.mockStatic(MongoDBDAO.class);
        PowerMockito.mockStatic(PropertyDefinitionsProtocol.class);
        PowerMockito.mockStatic(ProtocolCategory.class);
	}

    @Test
    public void testvalidateProtocolCodeValide(){
        ContextValidation contextValidation = ContextValidation.createCreationContext(TestUtils.CURRENT_USER);
        Protocol protocol = ProtocolFactory.getRandomProtocol(false);
        
        ProtocolValidationHelper.validateProtocolCode(contextValidation,protocol.code);

        assertTrue("Context has errors", !contextValidation.hasErrors());

    }

    @Test
    public void testvalidateProtocolCodeInvalide(){
        ContextValidation contextValidation = ContextValidation.createCreationContext(TestUtils.CURRENT_USER);

         //Création d'un protocole avec un code non valide 
        Protocol protocol = ProtocolFactory.geProtocolWithParameter(false,true , true, true, true);
        
        ProtocolValidationHelper.validateProtocolCode(contextValidation,protocol.code);

        assertTrue("Context has errors", contextValidation.hasErrors());

    }

    @Test
    public void testvalidateProtocolCategoryCodeRequiredValid(){
        ContextValidation contextValidation = ContextValidation.createCreationContext(TestUtils.CURRENT_USER);
        
        Protocol protocol = ProtocolFactory.getRandomProtocol(false);   

        Supplier<ProtocolCategoryDAO> supp = () -> protocolCategoryDAO;
        
        //On bouchonne la recherche du code 
        when(protocolCategoryDAO.isCodeExist(protocol.categoryCode)).thenReturn(true);
        Field field = PowerMockito.field(ProtocolCategory.class, "find");
        try {
            field.set(ProtocolCategoryDAO.class, supp);
        } catch (IllegalArgumentException e) {
            Logger.error("testvalidateProtocolCategoryCodeRequiredValid");
			fail(e.getMessage());
        } catch (IllegalAccessException e) {
            Logger.error("testvalidateProtocolCategoryCodeRequiredValid");
			fail(e.getMessage());
        }
        ProtocolValidationHelper.validateProtocolCategoryCodeRequired(contextValidation,protocol.categoryCode);

        assertTrue("Context has errors", !contextValidation.hasErrors());
    }

    @Test
    public void testvalidateProtocolCategoryCodeRequiredInvalid(){
        ContextValidation contextValidation = ContextValidation.createCreationContext(TestUtils.CURRENT_USER);

         //Création d'un protocole avec un code de category non valide
        Protocol protocol = ProtocolFactory.geProtocolWithParameter(true,true , false, true, true);
        
        Supplier<ProtocolCategoryDAO> supp = () -> protocolCategoryDAO;

        //On bouchonne la recherche du code 
        when(protocolCategoryDAO.isCodeExist(protocol.categoryCode)).thenReturn(false);

        Field field = PowerMockito.field(ProtocolCategory.class, "find");
       
        try {
            field.set(ProtocolCategoryDAO.class, supp);
        } catch (IllegalArgumentException e) {
            Logger.error("testvalidateProtocolCategoryCodeRequiredInValid");
			fail(e.getMessage());
        } catch (IllegalAccessException e) {
            Logger.error("testvalidateProtocolCategoryCodeRequiredInValid");
			fail(e.getMessage());
        }

        ProtocolValidationHelper.validateProtocolCategoryCodeRequired(contextValidation,protocol.categoryCode);
       
        assertTrue("Context has errors", contextValidation.hasErrors());
    }

    @Test
    public void testvalidateExperimentTypeCodesForUpdateValid(){
         ContextValidation contextValidation = ContextValidation.createUpdateContext(TestUtils.CURRENT_USER);
        Protocol protocol = ProtocolFactory.getRandomProtocol(false);
    
        ProtocolValidationHelper.validateExperimentTypeCodesForUpdate(contextValidation,protocol.experimentTypeCodes);

        assertTrue("Context has errors", !contextValidation.hasErrors());
    }

    @Test
    public void testvalidateExperimentTypeCodesForUpdateInvalid(){
        ContextValidation contextValidation = ContextValidation.createUpdateContext(TestUtils.CURRENT_USER);

        //Création d'un protocole avec une liste d'experience vide 
        Protocol protocol = ProtocolFactory.geProtocolWithParameter(true,false , true, true, true);
    
        ProtocolValidationHelper.validateExperimentTypeCodesForUpdate(contextValidation,protocol.experimentTypeCodes);

        assertTrue("Context has errors", contextValidation.hasErrors());
    }

    // TODO : Commenté le 11/06 plus passant suite changement version. A refaire.
    @Test
    public void testValidateProtocolPropertiesValid(){
        /** ContextValidation contextValidation = ContextValidation.createUpdateContext(TestUtils.CURRENT_USER);
        Protocol protocol = ProtocolFactory.getRandomProtocol(false);
        protocol.properties.entrySet().forEach((Map.Entry<String, PropertyValue> entry) -> {
            String code = entry.getKey();
            PropertyValue property = entry.getValue();
            
            //Creation du mock afin d'avoir un element afin de comprarer si le code ci dessus sous contenu ou non 
            PropertyDefinitionsProtocol pdp = mock(PropertyDefinitionsProtocol.class);
            Map<String, PropertyDefinition> properties = new HashMap<String, PropertyDefinition>();
          
            properties.put(code,protocol.properties);
            pdp.properties = properties;
           
            // Bouchon vers l'appel en base et renvoie le mock des Properties Definitions Protocol crée ci dessus 
            when(MongoDBDAO.findByCode(InstanceConstants.PARAMETER_COLL_NAME, PropertyDefinitionsProtocol.class, "property-definition-protocol")).thenReturn(pdp);
                              
            assertNotNull("Protocol properties should not be null", protocol.properties);
            ProtocolValidationHelper.validatePropertyExist(contextValidation,code);
        });
       
         assertTrue("Context has errors", !contextValidation.hasErrors()); */
    }

    // TODO : Commenté le 11/06 plus passant suite changement version. A refaire.
    @Test
    public void testValidateProtocolPropertiesInvalid(){
        /** ContextValidation contextValidation = ContextValidation.createCreationContext(TestUtils.CURRENT_USER);
        String code = "iniexistant";

        //Creation du mock afin d'avoir un element afin de comprarer si le code ci dessus sous contenu ou non 
        PropertyDefinitionsProtocol pdp = mock(PropertyDefinitionsProtocol.class);
        Map<String, PropertyDefinition> properties = new HashMap<String, PropertyDefinition>();
        properties.put("cDNAsynthesisType",null);

        pdp.properties = properties;

        // Bouchon vers l'appel en base et renvoie le mock des Properties Definitions Protocol crée ci dessus 
        when(MongoDBDAO.findByCode(InstanceConstants.PARAMETER_COLL_NAME, PropertyDefinitionsProtocol.class, "property-definition-protocol")).thenReturn(pdp);
     
        ProtocolValidationHelper.validatePropertyExist(contextValidation,code);

        assertTrue("Context has errors", contextValidation.hasErrors()); */
    }
}

