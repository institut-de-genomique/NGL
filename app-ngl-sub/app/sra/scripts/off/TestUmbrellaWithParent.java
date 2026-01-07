package sra.scripts.off;

import java.util.Date;
import javax.inject.Inject;

import controllers.sra.api.AbstractStudyAPI;
import controllers.sra.api.ProjectAPI;
import controllers.sra.api.StudyAPI;
import fr.cea.ig.lfw.controllers.scripts.buffered.Script;
import fr.cea.ig.lfw.utils.Iterables;
import fr.cea.ig.ngl.NGLApplication;
import models.sra.submit.sra.instance.Project;
import sra.services.SraEbiAPI;
import sra.services.XmlToSra;

/*
* Exemple de lancement :
* http://localhost:9000/sra/scripts/run/sra.scripts.TestUmbrellaWithParent
* @author sgas
*/

@SuppressWarnings("unused")
public class TestUmbrellaWithParent   extends Script<TestUmbrellaWithParent.MyParams> {
	
	private final AbstractStudyAPI       		abstStudyAPI;
	private final StudyAPI               		studyAPI;
	private final ProjectAPI                    projectAPI;
	private final SraEbiAPI                        ebiAPI;
	private final NGLApplication                app;

	private static final play.Logger.ALogger logger = play.Logger.of(TestUmbrellaWithParent.class);


	@Inject
	public TestUmbrellaWithParent (
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
	
	public static class MyParams {
		public String accession;
	}
	
	public Project EbiFetchProject(String accession, Date submissionDate) {	
		if (submissionDate == null) {
			submissionDate = new Date();
		}
		try {
			String xmlProjects = ebiAPI.ebiXml(accession, "projects");
			println("xmlProjects ="); 
			println(xmlProjects); 

			XmlToSra repriseHistorique = new XmlToSra();
			return Iterables.first(repriseHistorique.forProjects(xmlProjects, submissionDate)).orElse(null);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void execute(MyParams args) throws Exception {
		Project project = EbiFetchProject(args.accession, null);

			println("project.accession="+project.accession);
			println("project.code="+project.code);
			//println("project.children="+project.childrenProjectAccessions.get(0));
			//println("project.children="+project.childrenProjectAccessions.get(1));

	}
	

}
