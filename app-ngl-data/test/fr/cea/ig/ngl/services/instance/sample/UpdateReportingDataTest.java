package fr.cea.ig.ngl.services.instance.sample;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.function.Supplier;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Transport;

import com.mongodb.BasicDBObject;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mongojack.DBQuery;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.MongoDBResult;
import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.ngl.NGLConfig;
import fr.cea.ig.ngl.dao.api.factory.ExperimentFactory;
import fr.cea.ig.ngl.dao.api.factory.HashMapFactory;
import fr.cea.ig.ngl.dao.api.factory.ProcessFactory;
import fr.cea.ig.ngl.dao.api.factory.ReadsetFactory;
import fr.cea.ig.ngl.dao.api.factory.SampleFactory;
import fr.cea.ig.play.IGGlobals;
import mail.MailServices;
import fr.cea.ig.ngl.TestUtils;
import models.laboratory.common.instance.PropertyValue;
import models.laboratory.experiment.description.ExperimentType;
import models.laboratory.experiment.description.dao.ExperimentTypeDAO;
import models.laboratory.experiment.instance.Experiment;
import models.laboratory.processes.instance.Process;
import models.laboratory.run.instance.ReadSet;
import models.laboratory.sample.instance.Sample;
import models.laboratory.sample.instance.reporting.SampleExperiment;
import models.laboratory.sample.instance.reporting.SampleProcess;
import models.laboratory.sample.instance.reporting.SampleReadSet;
import play.Logger;
import play.inject.Injector;
import play.modules.mongojack.MongoDBPlugin;
import services.instance.sample.UpdateReportingData;
import services.instance.sample.UpdateReportingData.DureeSample;
import workflows.process.ProcWorkflowHelper;

@RunWith(PowerMockRunner.class)
@SuppressWarnings("unchecked")
@PrepareForTest({ MongoDBPlugin.class, MongoDBDAO.class, ExperimentType.class, ConfigFactory.class, IGGlobals.class, Transport.class })
public class UpdateReportingDataTest {

    private Injector injector = mock(Injector.class);

    private Config config = mock(Config.class);

    private NGLConfig configNgl = mock(NGLConfig.class);

    private NGLApplication app = mock(NGLApplication.class);

    private ProcWorkflowHelper procWorkflowHelper = mock(ProcWorkflowHelper.class);

    private Transport transport = mock(Transport.class);

    private MailServices mailServices = mock(MailServices.class);

    private UpdateReportingData updateReportingData;

    @Before
    public void setup() {
        PowerMockito.mockStatic(MongoDBPlugin.class);
        PowerMockito.mockStatic(MongoDBDAO.class);
        PowerMockito.mockStatic(ExperimentType.class);
        PowerMockito.mockStatic(ConfigFactory.class);
        PowerMockito.mockStatic(IGGlobals.class);
        PowerMockito.mockStatic(Transport.class);
        
        Supplier<ExperimentTypeDAO> supp = () -> PowerMockito.mock(ExperimentTypeDAO.class);
		Field field = PowerMockito.field(ExperimentType.class, "find");

		try {
			field.set(ExperimentType.class, supp);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}

        when(app.injector()).thenReturn(injector);
        when(injector.instanceOf(ProcWorkflowHelper.class)).thenReturn(procWorkflowHelper);

        updateReportingData = new UpdateReportingData(app, configNgl);
        updateReportingData.setMailServices(mailServices);
    }

    /**
	 * Méthode permettant de générer une map (type : "HashMap<String, DureeSample>") correspondant à l'objet "SAMPLE_DURATION" du CRON "ReportingData".
	 * 
	 * @return Une HashMap correspondant à l'objet "SAMPLE_DURATION" du CRON "ReportingData".
	 */
	public static HashMap<String, DureeSample> getMapSampleDuration() {
		HashMap<String, DureeSample> res = new HashMap<>();

		for (int i = 0; i < TestUtils.LIST_SIZE; i++) {
			UpdateReportingData.DureeSample dureeSample = new UpdateReportingData.DureeSample(new Date(), new Date(), new Random().nextInt());
			res.put(UUID.randomUUID().toString(), dureeSample);
		}

		return res;
	}

    @Test
    public void testClearSampleList() {      
        updateReportingData.SAMPLE_LIST = SampleFactory.getRandomSampleList();

        assertTrue("SAMPLE_LIST is empty", !updateReportingData.SAMPLE_LIST.isEmpty());

        updateReportingData.clearSampleList();

        assertTrue("SAMPLE_LIST is not empty", updateReportingData.SAMPLE_LIST.isEmpty());
    }

    @Test
    public void testClearMapDuration() {
        updateReportingData.MAP_DURATION = HashMapFactory.getMapDuration();

        assertTrue("MAP_DURATION is empty", !updateReportingData.MAP_DURATION.isEmpty());

        updateReportingData.clearMapDuration();

        assertTrue("MAP_DURATION is not empty", updateReportingData.MAP_DURATION.isEmpty());
    }

    @Test
    public void testClearMapSampleDuration() {
        updateReportingData.MAP_SAMPLE_DURATION = getMapSampleDuration();

        assertTrue("MAP_SAMPLE_DURATION is empty", !updateReportingData.MAP_SAMPLE_DURATION.isEmpty());

        updateReportingData.clearMapSampleDuration();

        assertTrue("MAP_SAMPLE_DURATION is not empty", updateReportingData.MAP_SAMPLE_DURATION.isEmpty());
    }

    @Test
    public void testGetReadsetBeforeNglSQBeforeMarch2015WithResult() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
		Date march2015 = null;
        
        try {
            march2015 = sdf.parse("2015/03/01");
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Sample sample = SampleFactory.getRandomSample(march2015, true);

        MongoDBResult<ReadSet> dbRes = mock(MongoDBResult.class);
        ReadSet readset = ReadsetFactory.getRandomReadset(true, march2015);
		List<ReadSet> readsetList = ReadsetFactory.getRandomReadsetList(march2015);	
						
		when(MongoDBDAO.find(any(String.class), any(Class.class), any(DBQuery.Query.class))).thenReturn(dbRes);
		when(dbRes.toList()).thenReturn(readsetList);
        when(MongoDBDAO.findByCode(any(String.class), any(Class.class), any(String.class), any(BasicDBObject.class))).thenReturn(readset);

        SampleProcess sampleProcess = updateReportingData.getReadSetBeforeNGLSQ(sample);

        assertTrue("Result list is empty", !sampleProcess.readsets.isEmpty());
        assertTrue("Type code is not 'Old LIMS'", sampleProcess.typeCode.equals("Old LIMS"));
    }

    @Test
    public void testGetReadsetBeforeNglSQBeforeMarch2015WithoutResult() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
		Date march2015 = null;
        
        try {
            march2015 = sdf.parse("2015/03/01");
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Sample sample = SampleFactory.getRandomSample(march2015, true);

        MongoDBResult<ReadSet> dbRes = mock(MongoDBResult.class);
		List<ReadSet> readsetList = new ArrayList<>();	
						
		when(MongoDBDAO.find(any(String.class), any(Class.class), any(DBQuery.Query.class))).thenReturn(dbRes);
		when(dbRes.toList()).thenReturn(readsetList);

        SampleProcess sampleProcess = updateReportingData.getReadSetBeforeNGLSQ(sample);

        assertNull("'sampleProcess' is not null", sampleProcess);
    }

    @Test
    public void testComputeStatistics() {
        Sample sample = SampleFactory.getRandomSample(new Date(), false);

        updateReportingData.computeStatistics(sample);

        assertNotNull("'processesStatistics' is null", sample.processesStatistics);
        
        assertTrue("'processesStatistics.readSetTypeCodes' has wrong size", sample.processesStatistics.readSetTypeCodes.size() == 25);
        assertTrue("'processesStatistics.processTypeCodes' has wrong size", sample.processesStatistics.processTypeCodes.size() == 5);
        assertTrue("'processesStatistics.processCategoryCodes' has wrong size", sample.processesStatistics.processCategoryCodes.size() == 5);
    }

    @Test
    public void testComputeStatisticsEmpty() {
        Sample sample = SampleFactory.getRandomSample(new Date(), true);

        updateReportingData.computeStatistics(sample);

        assertNull("'processesStatistics' is not null", sample.processesStatistics);
    }

    @Test
    public void testGetReadsetBeforeNglSQAfterMarch2015() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
		Date march2021 = null;
        
        try {
            march2021 = sdf.parse("2021/03/01");
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Sample sample = SampleFactory.getRandomSample(march2021, true);

        MongoDBResult<ReadSet> dbRes = mock(MongoDBResult.class);
		List<ReadSet> readsetList = new ArrayList<>();	
						
		when(MongoDBDAO.find(any(String.class), any(Class.class), any(DBQuery.Query.class))).thenReturn(dbRes);
		when(dbRes.toList()).thenReturn(readsetList);
        when(MongoDBDAO.findByCode(any(String.class), any(Class.class), any(String.class), any(BasicDBObject.class))).thenReturn(null);

        SampleProcess sampleProcess = updateReportingData.getReadSetBeforeNGLSQ(sample);

        assertTrue("'sampleProcess' is not null", sampleProcess == null);
    }

    // convertToSampleProcess() est compliquée à plus tester en l'état. Le forEach() du cursor est compliqué à mocker par exemple.

    @Test
    public void testConvertToSampleProcessWithoutOutputContainerAndWithoutExperiments() {
        Sample sample = SampleFactory.getRandomSample(new Date(), false);
        Process process = ProcessFactory.getRandomProcess(true, false, false);

        SampleProcess sampleProcess = updateReportingData.convertToSampleProcess(sample, process);

        assertTrue("'code' is not correct", sampleProcess.code.equals(process.code)); 
        assertTrue("'typeCode' is not correct", sampleProcess.typeCode.equals(process.typeCode)); 

        assertNull("'experiments' is not null", sampleProcess.experiments);
        assertNull("'readsets' is not null", sampleProcess.readsets);
    }

    @Test
    public void testConvertToSampleExperiments() {
        Process process = ProcessFactory.getRandomProcess(false, true, true);
        Experiment experiment = ExperimentFactory.getRandomExperimentNanopore();
        
        List<SampleExperiment> sampleExpList = updateReportingData.convertToSampleExperiments(process, experiment);

        assertTrue("'sampleExpList.size()' has wrong size", sampleExpList.size() == 1);
        assertTrue("'code' is not the same", sampleExpList.get(0).code.equals(experiment.code));
        assertTrue("'typeCode' is not the same", sampleExpList.get(0).typeCode.equals(experiment.typeCode));
    }

    @Test
    public void testConvertToSampleExperimentsEmpty() {
        Process process = ProcessFactory.getRandomProcess(false, true, true);
        process.inputContainerCode = UUID.randomUUID().toString();

        Experiment experiment = ExperimentFactory.getRandomExperimentNanopore();
        
        List<SampleExperiment> sampleExpList = updateReportingData.convertToSampleExperiments(process, experiment);

        assertTrue("'sampleExpList.size()' has wrong size", sampleExpList.size() == 0);
    }

    @Test
    public void testFilterPropertiesWithNoFileAndImg() {
        Map<String, PropertyValue> properties = HashMapFactory.getMapProperties();

        Map<String, PropertyValue> resFilter = updateReportingData.filterProperties(properties);

        assertTrue("size has wrong size", properties.size() == 5);
        assertTrue("size are not equals", resFilter.size() == properties.size());
    }

    @Test
    public void testFilterPropertiesWithFileAndImg() {
        Map<String, PropertyValue> properties = null;

        try {
            properties = HashMapFactory.getMapPropertiesWithFileAndImg();
        } catch (IOException e) {
            Logger.error("Exception occured during testFilterPropertiesWithFileAndImg()");
			fail(e.getMessage());
        }

        Map<String, PropertyValue> resFilter = updateReportingData.filterProperties(properties);

        assertTrue("properties.size has wrong size", properties.size() == 7);
        assertTrue("resFilter.size has wrong size", resFilter.size() == 5);
    }

    @Test
    public void testConvertToSampleReadSet() {
        ReadSet readset = ReadsetFactory.getRandomReadset(true, new Date());

        when(MongoDBDAO.findByCode(any(String.class), any(Class.class), any(String.class), any(BasicDBObject.class))).thenReturn(readset);

        SampleReadSet sampleReadSet = updateReportingData.convertToSampleReadSet(readset);

        assertTrue("'code' is not the same", sampleReadSet.code.equals(readset.code));
        assertTrue("'typeCode' is not the same", sampleReadSet.typeCode.equals(readset.typeCode));
        assertTrue("'runCode' is not the same", sampleReadSet.runCode.equals(readset.runCode));
        assertTrue("'runTypeCode' is not the same", sampleReadSet.runTypeCode.equals(readset.runTypeCode));
    }

    /**
     * Autres tests possibles : getEmailExpediteur() et getEmailDestinataires().
     * Idées : 
     * - Mocker la clé voulue 'reporting.email.to' et valider que si on utilise une autre clé ça ne renvoie rien.
     * - Tester le split sur les destinataires en envoyant une chaîne qui ne contient pas de "," pour forcer une erreur.
     */

    @Test
    public void testBuildAndSendMail() {
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

        Date END_CRON_DATE = new Date();
        int NB_TOTAL = 10;

        updateReportingData.MAP_DURATION.put("Global", 10L);

        try {
            updateReportingData.buildAndSendMail(END_CRON_DATE, NB_TOTAL);
        } catch (Exception e) {
            Logger.error("Exception occured during testBuildAndSendMail()");
            fail(e.getMessage());
        }

        try {
            verify(transport, times(1)).send(any());
        } catch (MessagingException e) {
            Logger.error("Exception occured during testBuildAndSendMail()");
            fail(e.getMessage());
        }
    }

    @Test
    public void testBuildMailBody() {
        Date END_CRON_DATE = new Date();
        int NB_TOTAL = 10;

        updateReportingData.MAP_SAMPLE_DURATION = getMapSampleDuration();
        updateReportingData.MAP_DURATION.put("Global", 10L);

        String generatedMail = null;

        try {
            generatedMail = updateReportingData.buildMailBody(END_CRON_DATE, NB_TOTAL);
        } catch (UnsupportedEncodingException e) {
            Logger.error("Exception occured during testBuildMailBody()");
			fail(e.getMessage());
        }

        for (Map.Entry<String, DureeSample> entry : updateReportingData.MAP_SAMPLE_DURATION.entrySet()) {
            String subMess = entry.getKey() + "," + entry.getValue().startDate + "," + entry.getValue().endDate;
            assertTrue("Generated mail does not contain given sample code and / or duration (MAP_SAMPLE_DURATION)", generatedMail.contains(subMess));
        }

        assertTrue("Generated mail does not have correct duration", generatedMail.contains("globale : 10 secondes"));
    }

    @Test
    public void testClearProcessesToLaunchDate() {
        String sampleCode = SampleFactory.getRandomSampleCode();

        updateReportingData.clearProcessesToLaunchDate(sampleCode);

        PowerMockito.verifyStatic();
    }
}
