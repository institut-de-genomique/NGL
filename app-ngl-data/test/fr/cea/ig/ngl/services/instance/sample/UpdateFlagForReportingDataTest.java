package fr.cea.ig.ngl.services.instance.sample;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.UnsupportedEncodingException;
import java.util.*;

import javax.mail.*;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.ngl.NGLConfig;
import fr.cea.ig.ngl.dao.api.factory.*;
import fr.cea.ig.ngl.TestUtils;
import fr.cea.ig.play.IGGlobals;
import mail.MailServiceException;
import mail.MailServices;
import play.Logger;
import play.inject.Injector;
import play.modules.mongojack.MongoDBPlugin;
import services.instance.sample.UpdateFlagForReportingData;
import workflows.process.ProcWorkflowHelper;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ MongoDBPlugin.class, MongoDBDAO.class, ConfigFactory.class, IGGlobals.class, Transport.class })
public class UpdateFlagForReportingDataTest {

    private Injector injector = mock(Injector.class);

    private Config config = mock(Config.class);
    
    private NGLConfig configNgl = mock(NGLConfig.class);

    private NGLApplication app = mock(NGLApplication.class);

    private ProcWorkflowHelper procWorkflowHelper = mock(ProcWorkflowHelper.class);

    private MailServices mailServices = mock(MailServices.class);

    private UpdateFlagForReportingData updateFlagReportingData;

    @Before
    public void setup() {
        PowerMockito.mockStatic(MongoDBPlugin.class);
        PowerMockito.mockStatic(MongoDBDAO.class);
        PowerMockito.mockStatic(ConfigFactory.class);
        PowerMockito.mockStatic(IGGlobals.class);
        PowerMockito.mockStatic(Transport.class);

        when(app.injector()).thenReturn(injector);
        when(injector.instanceOf(ProcWorkflowHelper.class)).thenReturn(procWorkflowHelper);

        updateFlagReportingData = new UpdateFlagForReportingData(app, configNgl);
        updateFlagReportingData.setMailServices(mailServices);
    }

    @Test
    public void testClearMapDuration() {      
        updateFlagReportingData.MAP_DURATION = HashMapFactory.getMapDuration();

        assertTrue("MAP_DURATION is empty", !updateFlagReportingData.MAP_DURATION.isEmpty());

        updateFlagReportingData.clearMapDuration();

        assertTrue("MAP_DURATION is not empty", updateFlagReportingData.MAP_DURATION.isEmpty());
    }

    @Test
    public void testClearSampleCodes() {      
        updateFlagReportingData.SAMPLES = SampleFactory.getRandomSampleTreeSet();

        assertTrue("SAMPLE_CODES is empty", !updateFlagReportingData.SAMPLES.isEmpty());

        updateFlagReportingData.clearSampleCodes();

        assertTrue("SAMPLE_CODES is not empty", updateFlagReportingData.MAP_DURATION.isEmpty());
    }

    @Test
    public void testAddSamplesToReportingLaunchList() {
        String sampleCode = SampleFactory.getRandomSampleCode();

        updateFlagReportingData.addSamplesToReportingLaunchList(sampleCode);

        assertTrue("Given sample code was not added", updateFlagReportingData.SAMPLES.size() == 1);
        assertTrue("Given sample code is not contained in SAMPLE_CODES", updateFlagReportingData.SAMPLES.contains(sampleCode));
    }

    @Test
    public void testRegisterRequestDuration() {
        updateFlagReportingData.registerRequestDuration("RequestTest", 10);

        assertTrue("Size of MAP_DURATION is not 1", updateFlagReportingData.MAP_DURATION.size() == 1);
        assertTrue("'RequestTest' has not the correct value", updateFlagReportingData.MAP_DURATION.get("RequestTest") == 10);
    }

    @Test
    public void testBuildAndSendMail() throws MailServiceException {
        when(ConfigFactory.load()).thenReturn(config);
        when(IGGlobals.configuration()).thenReturn(config);

        when(config.getString("reporting.email.to")).thenReturn(TestUtils.MAIL_TEST);
        when(config.getString("reporting.email.from")).thenReturn(TestUtils.MAIL_TEST);
        when(config.getString("mail.smtp.host")).thenReturn(TestUtils.SMTP_TEST);

        try {
            PowerMockito.doNothing().when(Transport.class, "send", any(Message.class));
        } catch (Exception e) {
            Logger.error("Exception occured during testBuildAndSendMail()");
			fail(e.getMessage());
        }

        Date START_CRON_DATE = new Date();
        Date END_CRON_DATE = new Date();

        TreeSet<String> PROCESS_CODES_LIST = ProcessFactory.getRandomProcessCodesList();
        TreeSet<String> EXP_CODES_LIST = ExperimentFactory.getRandomExperimentCodesList();
        TreeSet<String> RS_CODES_LIST = ReadsetFactory.getRandomReadsetCodesList();

        updateFlagReportingData.MAP_DURATION.put("Global", 10L);

        try {
            updateFlagReportingData.buildAndSendMail(PROCESS_CODES_LIST, EXP_CODES_LIST, RS_CODES_LIST, START_CRON_DATE, END_CRON_DATE);
        } catch (Exception e) {
            Logger.error("Exception occured during testBuildAndSendMail()");
            fail(e.getMessage());
        }

        verify(mailServices, times(1)).sendMail(anyString(), anySetOf(String.class), anyString(), anyString());
    }

    @Test
    public void testBuildMailBody() {
        Date START_CRON_DATE = new Date();
        Date END_CRON_DATE = new Date();

        TreeSet<String> PROCESS_CODES_LIST = ProcessFactory.getRandomProcessCodesList();
        TreeSet<String> EXP_CODES_LIST = ExperimentFactory.getRandomExperimentCodesList();
        TreeSet<String> RS_CODES_LIST = ReadsetFactory.getRandomReadsetCodesList();

        updateFlagReportingData.MAP_DURATION.put("Global", 10L);

        String generatedMail = null;

        try {
            generatedMail = updateFlagReportingData.buildMailBody(PROCESS_CODES_LIST, EXP_CODES_LIST, RS_CODES_LIST, START_CRON_DATE, END_CRON_DATE);
        } catch (UnsupportedEncodingException e) {
            Logger.error("Exception occured during testBuildMailBody()");
			fail(e.getMessage());
        }

        Iterator<String> itProc = PROCESS_CODES_LIST.iterator();

        while (itProc.hasNext()) {
            assertTrue("Generated mail does not contain given codes (PROCESS_CODES_LIST)", generatedMail.contains(itProc.next()));
        }

        Iterator<String> itExp = EXP_CODES_LIST.iterator();

        while (itProc.hasNext()) {
            assertTrue("Generated mail does not contain given codes (EXP_CODES_LIST)", generatedMail.contains(itExp.next()));
        }

        Iterator<String> itRs = RS_CODES_LIST.iterator();

        while (itRs.hasNext()) {
            assertTrue("Generated mail does not contain given codes (RS_CODES_LIST)", generatedMail.contains(itRs.next()));
        }

        assertTrue("Generated mail does not have correct duration", generatedMail.contains("globale : 10 secondes"));
    }
}
