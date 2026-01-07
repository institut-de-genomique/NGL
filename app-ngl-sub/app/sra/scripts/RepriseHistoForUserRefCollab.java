package sra.scripts;


import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;

import controllers.sra.api.SubmissionAPI;
import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.lfw.controllers.scripts.buffered.Script;
import fr.cea.ig.ngl.dao.sra.SubmissionDAO;
import models.sra.submit.sra.instance.Submission;
import models.sra.submit.sra.instance.UserRefCollabType;
import models.utils.InstanceConstants;
import models.utils.dao.DAOException;
import com.fasterxml.jackson.databind.JsonNode;



/*
 * Exemple de lancement :
 * http://localhost:9000/sra/scripts/run/sra.scripts.RepriseHistoForUserRefCollab
 * @author sgas
 *
 */


// on passe par DAO plutot que par API car certains vieux objets ne franchissent pas la validation.
public class RepriseHistoForUserRefCollab extends Script<RepriseHistoForUserRefCollab.MyParam>{
	// private static final play.Logger.ALogger logger = play.Logger.of(UpdateExperimentForAccessions.class);


	private final SubmissionDAO     submissionDAO;
	private final SubmissionAPI     submissionAPI;

	@Inject
	public RepriseHistoForUserRefCollab(
			SubmissionDAO         submissionDAO,
			SubmissionAPI         submissionAPI) {
		this.submissionDAO         = submissionDAO;
		this.submissionAPI         = submissionAPI;

	}


	// ma structure de controle et stockage des arguments de l'url
	public static class MyParam {
	}

	@Override
	public void execute(MyParam args) throws Exception {
		Date dateDebut = new Date();
		printfln("debut traitement le " +  dateDebut);
		String user = "ngsrg";
		updateModeleForSubmission(user);
		Date dateFin = new Date();
		printfln("debut du traitement le "+ dateDebut + " fin du traitement le " + dateFin);
	} // end execute


	//GSC_CWW_A3BH5CN7K	remplacement de 3 valeurs de cle et alias avec . par ---
	private void updateModeleForSubmission(String user) {
				List<Submission> submissionList = MongoDBDAO.find( InstanceConstants.SRA_SUBMISSION_COLL_NAME, Submission.class, 
						DBQuery.exists("mapUserRefCollab")).toList(); 		
		//List<Submission> submissionList = MongoDBDAO.find( InstanceConstants.SRA_SUBMISSION_COLL_NAME, Submission.class, 
		//		DBQuery.is("code", "GSC_CWW_A3BH5CN7K")).toList();
		printfln("nombre de submission " + submissionList.size());

//		for (Submission submission : submissionList) {
//			try {
//				if(submission.mapUserRefCollab == null || submission.mapUserRefCollab.size()==0 ) {
//					printfln("soumission ignoree = " + submission.code);
//					continue;
//				}
//				submission.userRefCollabs = new ArrayList<UserRefCollabType>();
//				for (Iterator<Entry<String, UserRefCollabType>> iterator = submission.mapUserRefCollab.entrySet().iterator(); iterator.hasNext();) {
//					Entry<String, UserRefCollabType> entry = iterator.next();
//					String alias = entry.getValue().getRefCollabName();
//					String studyAc = entry.getValue().getStudyAc();
//					String sampleAc = entry.getValue().getSampleAc();
//					UserRefCollabType userRefCollabType = new UserRefCollabType(alias, studyAc, sampleAc);
//					if(! submission.userRefCollabs.contains(userRefCollabType)) {
//						submission.userRefCollabs.add(userRefCollabType);
//					}
//				}
//				//submissionDAO.update(submission);
//				submissionDAO.update(DBQuery.is("code", submission.code),
//						DBUpdate.set("userRefCollabs", submission.userRefCollabs)
//								.unset("mapUserRefCollab")); 
//			} catch (Exception e) {
//				printfln( "!!!!!!!!!!!!!!!!  externe    Exception pour le traitement de la soumission " + submission.code + " " + e.getMessage() );
//				e.printStackTrace();
//				continue;
//			}
//			if(StringUtils.isBlank(submission.code)) {
//				printfln("soumission sans code ???");
//			} else {
//				printfln("MAJ de la soumission " + submission.code + " pour modele");
//			}
//		}
	}
		

}
