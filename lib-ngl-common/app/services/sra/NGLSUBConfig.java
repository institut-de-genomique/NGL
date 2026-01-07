package services.sra;

import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import fr.cea.ig.lfw.LFWConfig;
import fr.cea.ig.ngl.NGLConfig;

//public class NGLSUBConfig extends NGLConfig {
//Si on faisait une sous-classe de NGLConfig on aurait alors toutes les variables des autres applis lors de la completion
public class NGLSUBConfig {
	public static final String NGLSUB_STARTCRONS                        = "NGL-SUB.STARTCRONS";
	public static final String NGLSUB_PROD                              = "NGL-SUB.PROD";
	public static final String EBI_CURL                                 = "ebiCurl";  // intervient dans soumissions utilisateurs et crons

	public static final String SUBMISSION_ROOT_DIRECTORY                = "submissionRootDirectory";
	public static final String ACCESSION_REPORTING_EMAIL_FROM           = "accessionReporting.email.from"; 
	public static final String ACCESSION_REPORTING_EMAIL_TO             = "accessionReporting.email.to";
	public static final String ACCESSION_REPORTING_EMAIL_SUCCESS        = "accessionReporting.email.subject.success";
	public static final String ACCESSION_REPORTING_EMAIL_ERROR          = "accessionReporting.email.subject.error";
	public static final String RELEASE_REPORTING_EMAIL_FROM             = "releaseReporting.email.from"; 
	public static final String RELEASE_REPORTING_EMAIL_TO               = "releaseReporting.email.to";
	public static final String RELEASE_REPORTING_EMAIL_SUCCESS          = "releaseReporting.email.subject.success";
	public static final String RELEASE_REPORTING_EMAIL_ERROR            = "releaseReporting.email.subject.error";
	public static final String UPDATE_REPORTING_EMAIL_FROM              = "updateReporting.email.from"; 
	public static final String UPDATE_REPORTING_EMAIL_TO                = "updateReporting.email.to";
	public static final String UPDATE_REPORTING_EMAIL_SUCCESS           = "updateReporting.email.subject.success";
	public static final String UPDATE_REPORTING_EMAIL_ERROR             = "updateReporting.email.subject.error";
	
	public static final String RELEASE_STUDY_EMAIL_TO                        = "alertReleaseStudy.email.to";
	public static final String RELEASE_STUDY_EMAIL_SUBJECT                   = "alertReleaseStudy.email.subject";
	
	public static final String CREATION_PROJECTS_ATLASEA_ROOT_DIRECTORY      = "creationProjectsAtlasea.rootDirectory";
	public static final String CREATION_PROJECTS_ATLASEA_EMAIL_SUBJECT       = "creationProjectsAtlasea.email.subject.success";
	public static final String CREATION_PROJECTS_ATLASEA_EMAIL_ERROR_SUBJECT = "creationProjectsAtlasea.email.subject.error";
	public static final String CREATION_PROJECTS_ATLASEA_EMAIL_TO            = "creationProjectsAtlasea.email.to.success";
	public static final String CREATION_PROJECTS_ATLASEA_EMAIL_ERROR_TO      = "creationProjectsAtlasea.email.to.error";
	
	private final LFWConfig lfwConfig;
	
	@Inject
	public NGLSUBConfig(LFWConfig lfwConfig) {
		this.lfwConfig = lfwConfig;
	}
	
	public String getNGLSUB_STARTCRONS() {
		String retour = "";
		try {
			retour = lfwConfig.getString(NGLSUB_STARTCRONS);
		} catch(RuntimeException e){
			retour = "";
		}
		return retour;
	}	
	
	public boolean hasPath(String path) {
		return this.lfwConfig.hasPath(path);
	}
	
	public boolean isSTARTCRONS_PROD() {
		if (! hasPath(NGLSUB_STARTCRONS)) {
			return false;
		} else if (getNGLSUB_STARTCRONS().equals("PROD")) {
			return true;
		} else {
			return false;
		}
	}
	
	public boolean isSTARTCRONS_TEST() {
		if (! hasPath(NGLSUB_STARTCRONS)) {
			return false;
		} else if (getNGLSUB_STARTCRONS().equals("TEST")) {
			return true;
		} else {
			return false;
		}
	}	

	public boolean isPROD() {
		return lfwConfig.getBoolean(NGLSUB_PROD, false);		
	}	

	public String getString(String path) {
		throw new UnsupportedOperationException();
	}
	
	// -- ACCESSION REPORTING
	public String getAccessionReportingEmailFrom() {
		return lfwConfig.getString(ACCESSION_REPORTING_EMAIL_FROM);
	}
	public String getAccessionReportingEmailTo() {
		return lfwConfig.getString(ACCESSION_REPORTING_EMAIL_TO);
	}

	public String getAccessionReportingEmailSubjectSuccess() {
		return lfwConfig.getString(ACCESSION_REPORTING_EMAIL_SUCCESS);
	}
	public String getAccessionReportingEmailSubjectError() {
		return lfwConfig.getString(ACCESSION_REPORTING_EMAIL_ERROR);
	}

	// -- RELEASE REPORTING
	public String getReleaseReportingEmailFrom() {
		return lfwConfig.getString(RELEASE_REPORTING_EMAIL_FROM);
	}
	public String getReleaseReportingEmailTo() {
		return lfwConfig.getString(RELEASE_REPORTING_EMAIL_TO);
	}
	public String getReleaseReportingEmailSubjectSuccess() {
		return lfwConfig.getString(RELEASE_REPORTING_EMAIL_SUCCESS);
	}
	public String getReleaseReportingEmailSubjectError() {
		return lfwConfig.getString(RELEASE_REPORTING_EMAIL_ERROR);
	}
	
	public String getReleaseStudyEmailTo() {
		return lfwConfig.getString(RELEASE_STUDY_EMAIL_TO);
	}	
	public String getReleaseStudyEmailSubject() {
		return lfwConfig.getString(RELEASE_STUDY_EMAIL_SUBJECT);
	}	
	// -- UPDATE REPORTING
	public String getUpdateReportingEmailFrom() {
		return lfwConfig.getString(UPDATE_REPORTING_EMAIL_FROM);
	}
	public String getUpdateReportingEmailTo() {
		return lfwConfig.getString(UPDATE_REPORTING_EMAIL_TO);
	}
	public String getUpdateReportingEmailSubjectSuccess() {
		return lfwConfig.getString(UPDATE_REPORTING_EMAIL_SUCCESS);
	}
	public String getUpdateReportingEmailSubjectError() {
		return lfwConfig.getString(UPDATE_REPORTING_EMAIL_ERROR);
	}

	// -- ATLASEA project  
	public String getEbiCurl() {
		return lfwConfig.getString(EBI_CURL);
	}
	public String getCreationProjectsAtlaseaRootDirectory() {
		return lfwConfig.getString(CREATION_PROJECTS_ATLASEA_ROOT_DIRECTORY);
	}
	public String getCreationProjectsAtlaseaEmailSubject() {
		return lfwConfig.getString(CREATION_PROJECTS_ATLASEA_EMAIL_SUBJECT);
	}
	public String getCreationProjectsAtlaseaEmailErrorSubject() {
		return lfwConfig.getString(CREATION_PROJECTS_ATLASEA_EMAIL_ERROR_SUBJECT);
	}
	public String getCreationProjectsAtlaseaEmailTo() {
		return lfwConfig.getString(CREATION_PROJECTS_ATLASEA_EMAIL_TO);
	}
	public String getCreationProjectsAtlaseaEmailErrorTo() {
		return lfwConfig.getString(CREATION_PROJECTS_ATLASEA_EMAIL_ERROR_TO);
	}


	
}
