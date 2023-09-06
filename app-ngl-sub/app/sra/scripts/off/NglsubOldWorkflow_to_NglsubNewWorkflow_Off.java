package sra.scripts.off;
//package sra.scripts;
//
//
//
//import ngl.refactoring.state.SRASubmissionStateNames;
//import services.ScriptTools;
//import services.ToNglSub3Off;
//
//import java.util.Map.Entry;
//
//import java.util.Iterator;
//
////import java.util.List;
//
//import javax.inject.Inject;
////import org.mongojack.DBQuery;
//
//import org.apache.commons.lang3.StringUtils;
//import org.eclipse.jetty.util.StringUtil;
//import org.mongojack.DBQuery;
//import org.mongojack.DBUpdate;
//
//
//import fr.cea.ig.lfw.controllers.scripts.buffered.ScriptNoArgs;
//import fr.cea.ig.ngl.NGLApplication;
////import fr.cea.ig.ngl.NGLApplication;
//import fr.cea.ig.ngl.dao.api.sra.AbstractSampleAPI;
//import fr.cea.ig.ngl.dao.api.sra.AbstractStudyAPI;
//import fr.cea.ig.ngl.dao.api.sra.ConfigurationAPI;
//import fr.cea.ig.ngl.dao.api.sra.ExperimentAPI;
//import fr.cea.ig.ngl.dao.api.sra.SampleAPI;
//import fr.cea.ig.ngl.dao.api.sra.StudyAPI;
////import fr.cea.ig.ngl.dao.api.sra.StudyAPI;
//import fr.cea.ig.ngl.dao.api.sra.SubmissionAPI;
//import fr.cea.ig.ngl.dao.readsets.ReadSetsAPI;
//import fr.cea.ig.ngl.dao.sra.StudyDAO;
//import models.laboratory.run.instance.ReadSet;
//import models.sra.submit.common.instance.AbstractSample;
//import models.sra.submit.common.instance.AbstractStudy;
//import models.sra.submit.common.instance.Sample;
//import models.sra.submit.common.instance.Study;
////import models.sra.submit.common.instance.Readset;
//import models.sra.submit.common.instance.Submission;
//import models.sra.submit.common.instance.UserRefCollabType;
//import models.sra.submit.sra.instance.Configuration;
//import models.sra.submit.sra.instance.Experiment;
//import models.sra.submit.sra.instance.RawData;
//import models.sra.submit.util.SraException;
////import validation.ContextValidation;
//
//
///*
// * Script a lancer pour passer la base de données d'NGL-SUB dans la version 3.0 ou plus correspondant à la nouvelle version
// * du workflow
// * Une fois le script executé en PROD 
// * - mettre les enumeration obsoletes pour configuration.strategySample et strategyStudy en commentaire
// * - mettre le champ obsolete submission.release en commentaire
// * - mettre le champs obsolete sample.clone en commentaire.
// * - mettre le champs rawData.submittedMd5 obsolete en commentaire
// * http://localhost:9000/sra/scripts/run/sra.scripts.NglsubOldWorkflow_to_NglsubNewWorkflow
// * @author sgas
// *
// */
//public class NglsubOldWorkflow_to_NglsubNewWorkflow extends ScriptNoArgs {
//
//
//	private final ToNglSub3Off       toNglSub3;
//	private final AbstractStudyAPI  abstractStudyAPI;
//	private final StudyAPI  studyAPI;
//	private final StudyDAO  studyDAO;
//
//
//	@Inject
//	public NglsubOldWorkflow_to_NglsubNewWorkflow(ToNglSub3Off       toNglSub3,
//			AbstractStudyAPI  abstractStudyAPI,
//			StudyAPI  studyAPI,
//			StudyDAO  studyDAO) {
//		this.toNglSub3       = toNglSub3;
//		this.abstractStudyAPI= abstractStudyAPI;
//		this.studyAPI= studyAPI;
//		this.studyDAO= studyDAO;
//
//
//	}
//
//	
//
//	@SuppressWarnings("deprecation")
//	@Override
//	public void execute() throws Exception {
//		toNglSub3.executeForConfiguration();
//		printfln("Table Configuration traitée");
//		toNglSub3.executeForSubmission();
//		printfln("Table Soumission traitée");
//		toNglSub3.executeForStudy();
//		printfln("Table Study traitée");
//		toNglSub3.executeForSample();
//		printfln("Table Sample traitée");
//		toNglSub3.executeForExperiment();
//		printfln("Table Experiment traitée");
//	}
//	
//}
//
//
//
//
