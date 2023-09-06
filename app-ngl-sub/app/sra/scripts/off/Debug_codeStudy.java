package sra.scripts.off;


import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.mongojack.DBQuery;

import fr.cea.ig.lfw.controllers.scripts.buffered.ScriptNoArgs;
import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.ngl.dao.api.sra.AbstractSampleAPI;
import fr.cea.ig.ngl.dao.api.sra.AbstractStudyAPI;
import fr.cea.ig.ngl.dao.api.sra.ExperimentAPI;
import fr.cea.ig.ngl.dao.api.sra.SubmissionAPI;
import models.sra.submit.sra.instance.AbstractStudy;
import models.sra.submit.sra.instance.Experiment;
import play.libs.Json;
import validation.ContextValidation;


/*
* Script pour ajouter studyCode et sampleCode dans exp lies à la soumission BBK
* Exemple de lancement :
* http://localhost:9000/sra/scripts/run/sra.scripts.off.Debug_codeStudy
* @author sgas
*
*/
public class Debug_codeStudy extends ScriptNoArgs {
//	private static final play.Logger.ALogger logger = play.Logger.of(Debug_BDA.class);
	private final AbstractStudyAPI   abstractStudyAPI;
	private final AbstractSampleAPI  abstractSampleAPI;
	private final SubmissionAPI     submissionAPI;
	private final ExperimentAPI     experimentAPI;
	private final NGLApplication    app;
	private static final play.Logger.ALogger logger = play.Logger.of(Debug_codeStudy.class);

	@Inject
	public Debug_codeStudy(SubmissionAPI     submissionAPI,
					 ExperimentAPI     experimentAPI,
					 AbstractStudyAPI      abstractStudyAPI,
					 AbstractSampleAPI     abstractSampleAPI,
					 NGLApplication    app) {
		this.abstractStudyAPI  = abstractStudyAPI;
		this.abstractSampleAPI = abstractSampleAPI;
		this.submissionAPI     = submissionAPI;
		this.experimentAPI     = experimentAPI;
		this.app               = app;
	}




	@Override
	public void execute() throws Exception {
		List<String>studyAccessions = new ArrayList<String>();
		studyAccessions.add("ERP001578");
		studyAccessions.add("ERP001579");
		studyAccessions.add("ERP001580");
		studyAccessions.add("ERP001581");
		studyAccessions.add("ERP001582");
		studyAccessions.add("ERP001583");
		studyAccessions.add("ERP001584");
		studyAccessions.add("ERP001585");
		studyAccessions.add("ERP002504");
		studyAccessions.add("ERP004877");
		studyAccessions.add("ERP004878");
		studyAccessions.add("ERP005335");
		

		ContextValidation contextValidation = ContextValidation.createUpdateContext("ngsrg"); 

		HashMap<String, String> hmAccessionToCode = new HashMap<String, String>();
		List<Experiment> exp_to_save = new ArrayList<Experiment>();
		String message = "liste a regarder :\n";
		for (String studyAccession : studyAccessions) {
			List<Experiment> listExperiments = experimentAPI.dao_find(DBQuery.is("studyAccession", studyAccession)).toList();
			printfln("Pour le studyAccession %s, %d d'experiments dans base",  studyAccession, listExperiments.size());

			for (Experiment exp: listExperiments) {
				//printfln("experimentCode = " + exp.code);

				AbstractStudy abStudy = abstractStudyAPI.dao_findOne(DBQuery.is("accession", studyAccession));
				if(! hmAccessionToCode.containsKey(abStudy.accession)) {
					hmAccessionToCode.put(abStudy.accession, abStudy.code);
				}
				if (StringUtils.isNotBlank(abStudy.code)) {
					if( ! exp.studyCode.equalsIgnoreCase(abStudy.code)) {
						if(!exp.studyCode.startsWith("NextGen")) {
							message += exp.code + "\n";
						}
						exp.studyCode = abStudy.code;
						exp.traceInformation.modifyUser= "ngsrg";
						exp.traceInformation.modifyDate=new Date();
						exp.validate(contextValidation);
						exp_to_save.add(exp);
					}
				}
			}
		}	
			if(contextValidation.hasErrors()) {
				contextValidation.displayErrors(logger, "debug");
				println(Json.prettyPrint(app.errorsAsJson(contextValidation.getErrors())));				
			} else {
				printfln("ok pour la validation des experiments");				
			}	
				
			for (Entry<String, String> entry : hmAccessionToCode.entrySet()) {
				printfln("- accession '%s' correspond au code '%s' ", entry.getKey(), entry.getValue());
			} 
			printfln("Nombre d'exp a modifier dans base = %d", exp_to_save.size());
			printfln(message);

			//println("sauvegarde de l'experiment " + experiment.code);
			for (Experiment exp: exp_to_save) {
				experimentAPI.dao_saveObject(exp);
			}
			printfln("Fin du traitement");
		
	}
	
}




