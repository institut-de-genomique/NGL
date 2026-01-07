package fr.cea.ig.ngl.dao.api;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.ngl.dao.api.factory.ProtocolFactory;
import fr.cea.ig.ngl.dao.descriptionHistories.DescriptionHistoriesAPI;
import fr.cea.ig.ngl.dao.descriptionHistories.DescriptionHistoriesDAO;
import fr.cea.ig.ngl.dao.protocols.ProtocolsAPI;
import fr.cea.ig.ngl.dao.protocols.ProtocolsDAO;
import fr.cea.ig.ngl.utils.TestUtils;
import models.laboratory.protocol.instance.Protocol;
import play.Logger;
import validation.common.instance.CommonValidationHelper;
import validation.protocol.instance.ProtocolValidationHelper;


/**
 * Test de l'API de l'entité Protocol.
 * 
 * @author asaadi
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ MongoDBDAO.class,  CommonValidationHelper.class, ProtocolValidationHelper.class })
public class ProtocolAPITest {

    private final ProtocolsDAO protocolDAO = Mockito.mock(ProtocolsDAO.class);
	private final DescriptionHistoriesDAO deshDAO = Mockito.mock(DescriptionHistoriesDAO.class);
    private final DescriptionHistoriesAPI descriptionHistoriesAPI =new DescriptionHistoriesAPI(deshDAO);
	private final ProtocolsAPI protocolAPI = new ProtocolsAPI(protocolDAO, descriptionHistoriesAPI);
	
	/**
	 * Si on veut faire des traitements avant l'exécution de la suite de tests. 
	 * Par exemple : initialiser un mock, initialiser une méthode statique, ...
	 */

    @Before
	public void setUp() {
		PowerMockito.mockStatic(MongoDBDAO.class);
		PowerMockito.mockStatic(CommonValidationHelper.class);
		PowerMockito.mockStatic(ProtocolValidationHelper.class);
    };

    @Test
    public void testGetByCode(){
        Protocol protocol = ProtocolFactory.getRandomProtocol(true);

        when(protocolDAO.findByCode(protocol.code)).thenReturn(protocol);

        Protocol protocolApi = protocolAPI.get(protocol.code);

        assertNotNull(protocolApi);
        assertTrue("Different code on testGetByCode()", protocol.code.equals(protocolApi.code));
    };

    @Test
    public void testCreateValid(){
        Protocol protocol = ProtocolFactory.getRandomProtocol(false);
        String randomCode = "test1234512312";
        protocol.code = randomCode;
      
        when(protocolDAO.saveObject(protocol)).thenReturn(protocol);
        Logger.info("23"+ protocol.properties);
        try{
            Protocol protocolCreateApi = protocolAPI.create(protocol,TestUtils.CURRENT_USER);
            Logger.info("2"+ protocolCreateApi.code);
            assertNotNull((protocolCreateApi));
            assertTrue("Different code on testCreateValid()",protocolCreateApi.code.equals(protocol.code));
        }catch(APIException e){
            Logger.error("Exception occured during testCreateValid()");
            fail(e.getMessage());
        }
    };

    @Test
    public void testCreateInvalid(){
        Protocol protocol = ProtocolFactory.getRandomProtocol(true);
       
        when(protocolDAO.saveObject(protocol)).thenReturn(protocol);
        boolean exceptFired = false;
        Protocol protocolCreateApi = null;

        try {
            protocolCreateApi = protocolAPI.create(protocol, TestUtils.CURRENT_USER);
        }catch(APIException e){
            exceptFired = true;
        }

        if(!exceptFired){
            fail("Invalid protocol, should have refused.");
        }

        assertNull(protocolCreateApi);
    };

    @Test
    public void testDeleteValid(){
        Protocol protocol = ProtocolFactory.getRandomProtocol(true);
        
        when(protocolDAO.getObject(any(String.class))).thenReturn(protocol);

        try{
            protocolAPI.remove(protocol.code, TestUtils.CURRENT_USER);
        }catch(APIException e){
            Logger.error("Exception occured during testDeleteValid()");
            fail(e.getMessage());
        }

        when(protocolDAO.findByCode(protocol.code)).thenReturn(null);

        Protocol protocolApi = protocolAPI.get(protocol.code);
        assertNull("Protocol still exist on testDeleteValide()", protocolApi);
    };

    @Test
    public void testDeleteInvalid(){
        Protocol protocol = ProtocolFactory.getRandomProtocol(true);

        when(protocolDAO.getObject(any(String.class))).thenReturn(null);

        boolean exceptFired = false;

        try{
            protocolAPI.remove(protocol.code, TestUtils.CURRENT_USER);
        }catch (APIException e){
            exceptFired= true;
        }

        if(!exceptFired){
            fail("Invalid protocol, should have been refused");
        }
    };

    @Test
    public void testUpdateValid(){
        Protocol protocol = ProtocolFactory.getRandomProtocol(true);
        protocol.code = "abc";
        String comment = "TestComent";
    
        when(protocolDAO.findByCode(protocol.code)).thenReturn(protocol);
        when(protocolDAO.getObject(protocol.code)).thenReturn(protocol);
                 
        Protocol protocolUpdateApi = null;
        try{
            protocolUpdateApi = protocolAPI.update(protocol, TestUtils.CURRENT_USER,comment);            
        }catch (APIException e){
            Logger.error("Exception occured during testUpdateValid()");
            fail(e.getMessage());
        }
        assertNotNull(protocolUpdateApi);
    };

    @Test
    public void testUpdateInvalid(){
        Protocol protocol = mock(Protocol.class);
        
        when(protocolDAO.isCodeExist(protocol.code)).thenReturn(false);
         String comment = "TestComent";
        Protocol protocolUpdateApi = null;
        boolean exceptFired = false;

        try{
            protocolUpdateApi = protocolAPI.update(protocol,TestUtils.CURRENT_USER,comment);
        }catch(APIException e){
           exceptFired = true;
        }

        if(!exceptFired){
             fail("Invalid protocol, should have been refused.");
        }

        assertNull(protocolUpdateApi);
    };
    
}
