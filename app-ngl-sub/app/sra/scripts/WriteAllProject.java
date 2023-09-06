package sra.scripts;


import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
//import org.mongojack.DBQuery;

import fr.cea.ig.lfw.controllers.scripts.buffered.Script;
import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.ngl.dao.api.sra.AbstractStudyAPI;
import fr.cea.ig.ngl.dao.api.sra.ProjectAPI;
import fr.cea.ig.ngl.dao.api.sra.SubmissionAPI;
import models.sra.submit.sra.instance.Project;
import services.XmlServices;


/*
* Ecrit les metadata d'une soumission

* Exemple de lancement :
* http://localhost:9000/sra/scripts/run/sra.scripts.WriteAllProject?resultDirectory=/env/cns/home...
* @author sgas
*
*/
public class WriteAllProject extends Script<WriteAllProject.MyParam> {
//	private static final play.Logger.ALogger logger = play.Logger.of(Debug_BDA.class);

	private final SubmissionAPI     submissionAPI;
	private final ProjectAPI        projectAPI;
//	private final NGLApplication    app;
    private final XmlServices       xmlServices;
    private final AbstractStudyAPI  abstStudyAPI;

    @Inject
	public WriteAllProject(SubmissionAPI     submissionAPI,
					 XmlServices       xmlServices,
					 ProjectAPI        projectAPI,
					 AbstractStudyAPI  abstStudyAPI,
					 NGLApplication    app) {

		this.submissionAPI     = submissionAPI;
		this.projectAPI        = projectAPI;
		this.abstStudyAPI      = abstStudyAPI;

//		this.app               = app;
		this.xmlServices       = xmlServices;
	}

	// ma structure de controle et stockage des arguments de l'url
	public static class MyParam {
		public String resultDirectory;
	}


	@Override
	public void execute(MyParam args) throws Exception {
		Date date = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		String strDate = formatter.format(date);
		List<Project> listProjects = new ArrayList<Project>();
		//List<Study> listStudies = new ArrayList<Study>();

		for (Project project : projectAPI.dao_all()) {	
			if (project.accession.equals("PRJEB402")) {
				continue;
			}
			if (! project.centerName.equals("GSC")) {
				println("Remplacement du centerName"   + project.centerName  + "par 'GSC' ");
				project.centerName="GSC";
				
				listProjects.add(project);
			}
		}
//		for (AbstractStudy abstStudy: abstStudyAPI.dao_all()) {
//			if(abstStudy instanceof Study) {
//				if(((Study) abstStudy).state.code.equals("SUB-F")) {
//					listStudies.add((Study) abstStudy);
//				}
//			}
//		}
		File outputFileProjects = new File(args.resultDirectory + File.separator + "ALL_umbrella_" + strDate +".xml");
		//File outputFileStudies = new File(args.resultDirectory + File.separator + "ALL_study_" + strDate +".xml");

		xmlServices.specialwriteProjectUmbrellaXml(listProjects, outputFileProjects);	
		//xmlServices.specialWriteStudyXml(listStudies, outputFileStudies);	
		println("Ecriture du fichier des "+ listProjects.size() + " UMBRELLA : " + outputFileProjects);
		//println("Ecriture du fichier des "+ listStudies.size() + " STUDY : " + outputFileStudies);
	}

}
