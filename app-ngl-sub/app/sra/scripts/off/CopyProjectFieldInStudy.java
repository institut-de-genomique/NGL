package sra.scripts.off;

import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;

import fr.cea.ig.lfw.controllers.scripts.buffered.ScriptNoArgs;
import fr.cea.ig.lfw.utils.Iterables;
import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.ngl.dao.api.sra.AbstractStudyAPI;
import fr.cea.ig.ngl.dao.api.sra.ProjectAPI;
import fr.cea.ig.ngl.dao.api.sra.StudyAPI;
import models.sra.submit.sra.instance.AbstractStudy;
import models.sra.submit.sra.instance.ExternalStudy;
import models.sra.submit.sra.instance.Project;
import models.sra.submit.sra.instance.Study;
import services.SraEbiAPI;
import services.XmlToSra;

/*
* Exemple de lancement :
* http://localhost:9000/sra/scripts/run/sra.scripts.CopyProjectFieldInStudy
* @author sgas
*/
@Deprecated
public class CopyProjectFieldInStudy  extends ScriptNoArgs {
	private final AbstractStudyAPI       		abstStudyAPI;
	private final StudyAPI               		studyAPI;
	private final ProjectAPI                    projectAPI;
	private final SraEbiAPI                        ebiAPI;
	private final NGLApplication                app;

	private static final play.Logger.ALogger logger = play.Logger.of(CopyProjectFieldInStudy.class);


	@Inject
	public CopyProjectFieldInStudy (
			AbstractStudyAPI              abstStudyAPI,
			StudyAPI                      studyAPI,
			ProjectAPI                    projectAPI,
			SraEbiAPI                        ebiAPI,
			NGLApplication                app) {
		this.abstStudyAPI = abstStudyAPI;
		this.studyAPI     = studyAPI;
		this.projectAPI   = projectAPI;
		this.ebiAPI       = ebiAPI;
		this.app          = app;
	}
	
	
	public Project EbiFetchProject(String accession, Date submissionDate) {		
		try {
			String xmlProjects = ebiAPI.ebiXml(accession, "projects");
			XmlToSra repriseHistorique = new XmlToSra();
			return Iterables.first(repriseHistorique.forProjects(xmlProjects, submissionDate)).orElse(null);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public void execute() throws Exception {		

		Iterator<AbstractStudy> iterator = abstStudyAPI.dao_all().iterator();
		while(iterator.hasNext()) {

			AbstractStudy abstStudy = iterator.next();
			if(abstStudy instanceof ExternalStudy) {
				continue;
			}
			Study study = studyAPI.get(abstStudy.code);
			//println("study.code=" + study.code);
			Calendar calendar = Calendar.getInstance();
			Date date  = calendar.getTime();
			String user = "ngsrg";
			
			if (!StringUtils.isBlank(study.externalId)) {
				Project project = projectAPI.dao_findOne(DBQuery.in("accession", study.externalId));
				studyAPI.dao_update(DBQuery.is("code", study.code),
						DBUpdate.set("traceInformation.modifyUser", user)
							.set("traceInformation.modifyDate", date)
							.set("title", project.title)
							.set("description", project.description)
							.set("studyAbstract", project.description));
			} else {
				String studyAbstract = cleanHtml(study.studyAbstract);
				printfln("studyAbstract = %s", studyAbstract);
				studyAPI.dao_update(DBQuery.is("code", study.code),
						DBUpdate.set("traceInformation.modifyUser", user)
							.set("traceInformation.modifyDate", date)
							.set("studyAbstract", studyAbstract)
							.set("description", studyAbstract));
			}
		}
		println("Fin de traitement");
		
	} 
	public String cleanHtml(String input) {
		String output = input;
		output = output.replace("\\n", " ");
		output = output.replace("\n", " ");
		output = output.replace("<", "&lt;");
		output = output.replace("<=", "&le;");
		output = output.replace(">", "&gt;");
		output = output.replace(">=", "&ge;");
		return output;
	}

}
