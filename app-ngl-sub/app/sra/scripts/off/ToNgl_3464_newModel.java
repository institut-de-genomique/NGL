package sra.scripts.off;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;

import fr.cea.ig.lfw.controllers.scripts.buffered.ScriptNoArgs;
import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.ngl.dao.api.sra.AbstractStudyAPI;
import fr.cea.ig.ngl.dao.api.sra.ProjectAPI;
import fr.cea.ig.ngl.dao.api.sra.StudyAPI;
import models.sra.submit.sra.instance.AbstractStudy;
import models.sra.submit.sra.instance.ExternalStudy;
import models.sra.submit.sra.instance.Project;
import models.sra.submit.sra.instance.Study;
import models.sra.submit.util.SraException;
import services.SraEbiAPI;

/*
* Exemple de lancement :
* http://localhost:9000/sra/scripts/run/sra.scripts.ToNgl_3464_newModel
* @author sgas
*/
//utilise repriseHistorique.forProjects qui repose sur model Project avec SEQUENCING et UMBRELLA => obsolete
@Deprecated
public class ToNgl_3464_newModel  extends ScriptNoArgs {
	private final AbstractStudyAPI       		abstStudyAPI;
	private final StudyAPI               		studyAPI;
	private final ProjectAPI                    projectAPI;
	private final NGLApplication                app;

	private static final play.Logger.ALogger logger = play.Logger.of(ToNgl_3464_newModel.class);


	@Inject
	public ToNgl_3464_newModel (
			AbstractStudyAPI              abstStudyAPI,
			StudyAPI                      studyAPI,
			ProjectAPI                    projectAPI,
			SraEbiAPI                        ebiAPI,
			NGLApplication                app) {
		this.abstStudyAPI = abstStudyAPI;
		this.studyAPI     = studyAPI;
		this.projectAPI   = projectAPI;
		this.app          = app;
	}
	

	@Override
	public void execute() throws Exception {		

		Iterator<AbstractStudy> iterator = abstStudyAPI.dao_all().iterator();
		List<Study> listStudyForUpdate = new ArrayList<Study>();
		Calendar calendar = Calendar.getInstance();
		Date date  = calendar.getTime();
		String user = "ngsrg";
		
		while(iterator.hasNext()) {

			AbstractStudy abstStudy = iterator.next();
			if(abstStudy instanceof ExternalStudy) {
				continue;
			}
			Study study = studyAPI.get(abstStudy.code);
			//println("study.code=" + study.code);


			if ( ! study.description.equals(study.studyAbstract)) {
				throw new SraException("Champs studyAbstract et description differents pour le study " + study.code);
			}
			if (StringUtils.isBlank(study.externalId)) {
				continue;
			}
			Project project = projectAPI.dao_findOne(DBQuery.in("accession", study.externalId));
			if(! study.studyAbstract.equals(project.description)) {
				throw new SraException("Champs studyAbstract et project.description differents pour le study " + study.code);
			}
			if(! study.title.equals(project.title)) {
				throw new SraException("Champs studyTitle et project.title differents pour le study " + study.code);
			}
			// marche dans ancien model avec project.locusTagPrefixs
//			if(project.locusTagPrefixs != null) {
//				listStudyForUpdate.add(study);
//				study.locusTagPrefixs = project.locusTagPrefixs;
//			}
		}
		
		for (Study study: listStudyForUpdate) {
				studyAPI.dao_update(DBQuery.is("code", study.code),
						DBUpdate.set("traceInformation.modifyUser", user)
							.set("traceInformation.modifyDate", date)
							.set("locusTagPrefixs", study.locusTagPrefixs));
			
			
		}
		println("Fin de traitement");
		
	} 

}
