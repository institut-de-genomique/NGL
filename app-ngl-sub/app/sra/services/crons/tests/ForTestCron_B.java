package sra.services.crons.tests;

import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;
import com.mongodb.MongoException;

import controllers.sra.api.StudyAPI;
import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.ngl.NGLConfig;
import fr.cea.ig.ngl.dao.api.APIException;
import fr.cea.ig.ngl.dao.api.APIValidationException;
import mail.MailServiceException;
import mail.MailServices;
import models.sra.submit.sra.instance.EmailAlert;
import models.sra.submit.sra.instance.Study;
import models.utils.dao.DAOException;
import play.Logger;
import rules.services.RulesException;
import services.sra.NGLSUBConfig;
import services.sra.SraException;
import sra.services.scheduler.Tools;
import validation.ContextValidation;



//lancement depuis la classe SubmissionCronScheduler
public class ForTestCron_B implements Runnable {
	private final StudyAPI       	  	   studyAPI;
	private final NGLSUBConfig             config;
	private static final play.Logger.ALogger logger      = play.Logger.of(ForTestCron_B.class);

	@Inject
	public ForTestCron_B(NGLSUBConfig config, StudyAPI studyAPI) {
		this.studyAPI = studyAPI;
		this.config = config;
	}

	@Override
	public void run() {
		logger.debug("Start ForTestCron_B.run()");
		String studyCode = "STUDY_A1TG2JU5H";
		Study study = studyAPI.dao_getObject(studyCode);	
		study.title = "TEST_CRON_B_" + Tools.getStringDate();
		
		if("webin-9_PROD".equals(config.getEbiCurl()) || config.isPROD() ) {
			// rien, on ne veut pas alterer un objet en PROD pour un TEST
			logger.debug("tentative de lancer le test ForTestCron_B en PROD ????");
		} else {
			studyAPI.dao_saveObject(study);
			logger.debug("sauvegarde dans base du study '" + studyCode + "' avec title='" + study.title  +"'");
		}
		logger.debug("End ForTestCron_B.run()");

	}
}
	