package sra.scripts.off;




import javax.inject.Inject;

import sra.scripts.utils.DateTools;
import fr.cea.ig.lfw.controllers.scripts.buffered.Script;
import fr.cea.ig.lfw.utils.Iterables;
import fr.cea.ig.ngl.NGLApplication;
import models.laboratory.common.instance.State;
import models.laboratory.common.instance.TraceInformation;
import play.libs.Json;
import services.XmlToSra;
import validation.ContextValidation;

import static ngl.refactoring.state.SRASubmissionStateNames.SUB_F;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;
import fr.cea.ig.ngl.dao.sra.ProjectDAO;
import models.sra.submit.sra.instance.Project;
import models.sra.submit.util.SraException;


/*
 * Script à utiliser pour charger dans ngl-sub le project umbrella soumis en dehors de l'application
 * {@code http://localhost:9000/sra/scripts/run/sra.scripts.LoadUmbrella?xmlFileUmbrella=C:\Users\sgas\projectUmbrella\PRJEB42618_umbrella.xml}
 * Si parametre absent dans url => declenchement d'une erreur. 
 * @author sgas
 */
// utilise repriseHistorique.forProjects qui repose sur model Project avec SEQUENCING et UMBRELLA => obsolete
@Deprecated
public class LoadUmbrella extends Script<LoadUmbrella.MyParams> {

//public class LoadUmbrella extends ScriptWithArgs<LoadUmbrella.MyParams> {
// Attention utilisation de ScriptWithArgs ne marche pas ici
// Attention import fr.cea.ig.lfw.controllers.scripts.buffered.Script permet bien de faire l'injection de service alors que 
// import fr.cea.ig.lfw.controllers.scripts.chunked.ScriptWithArgs pose probleme => la methode project.validate n'arrive pas à recuperer
// le service injecte projectAPI


	private final ProjectDAO            projectDAO;
	private final NGLApplication        app;

	
	@Inject
	public LoadUmbrella(
				ProjectDAO         projectDAO,
				NGLApplication     app
				) {
		this.projectDAO = projectDAO;
		this.app = app;


		
	}
	
	public static class MyParams {
		//public String user;
		//public String sdate;
		//public String accession;
		public String xmlFileUmbrella;

	}

	/**
	 * Recupere les project umbrella dans le fichier du genoscope (fichier renvoyé par l'EBI ne contient pas les projets enfants)
	 * et les insere dans collection ngl-sub.project.
	 * Attention si project umbrella, le xml de creation envoyé à l'EBI contient bien les projects enfants 
	 * mais le xml visible à l'EBI ne contient plus les projects enfants
	 * @param  accession    accession de type PRJ... permettant d'identifier le project
	 * @param  xmlFile      fichier project.xml envoyé à l'EBI (donc sans PRJEB normalement)
	 * @param  date         Date de la soumission du project umbrella
	 * @param  user         user ayant realisé la soumission
	 * 
	 */
	public void loadUmbrellaFromGenoscopeFile(File xmlFile, String user, String accession, Date date) {	
		
		String xmlProjects = "";
		try {
			if ( xmlFile.exists()) {
				String line = "";
				try (BufferedReader input_buffer = new BufferedReader(new FileReader(xmlFile))) {
					while ((line = input_buffer.readLine()) != null) {	
						xmlProjects += line + "\n";
					}
				} catch (IOException e) {
					throw new SraException("Probleme lors du chargement du fichier ", e);
				}
			} else {
				println("le fichier d'entree n'existe pas");
			}
		    println(xmlProjects);
			
		    XmlToSra repriseHistorique = new XmlToSra();
			
			if( date== null) {
				date = new Date();
			}
			Project project = Iterables.first(repriseHistorique.forProjects(xmlProjects, date)).orElse(null);

			println("project.accession="+project.accession);
			println("project.code="+project.code);
			//println("project.children="+project.childrenProjectAccessions.get(0));
			//println("project.children="+project.childrenProjectAccessions.get(1));


			project.traceInformation = new TraceInformation();
			
			project.traceInformation.createUser = user;
			project.traceInformation.creationDate = date;
			project.state = new State(SUB_F, user);
			project.accession = accession;
			ContextValidation contextValidation = ContextValidation.createCreationContext(project.traceInformation.createUser);
			project.validate(contextValidation);
			if(contextValidation.hasErrors()) {
				println("Probleme lors de la validation");
				//contextValidation.displayErrors(logger, "debug");
				println(Json.prettyPrint(app.errorsAsJson(contextValidation.getErrors())));
				//throw new SraValidationException(contextValidation);
			} else {
				println("Pas de probleme pour le project " + project.accession);
				projectDAO.save(project);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public void execute(MyParams args)  throws Exception {
		//String projectCode = "PRJEB42618"; // cas project umbrella
		//String projectCode = "PRJEB39972"; //cas project sequencing avec 2 locus_tag
		println("file = " + args.xmlFileUmbrella);
		String relatif_file;
		String separator="/";
		if (args.xmlFileUmbrella.contains("/")) {
			separator = "/";
		} else if (args.xmlFileUmbrella.contains("\\")) {
			separator = "\\";
		} else {
			println("Nom du fichier qui n'est pas au format attendu PRJEBXX_dd-mm-yyyy_user");
			throw new RuntimeException();
		}
		relatif_file = args.xmlFileUmbrella.substring(args.xmlFileUmbrella.lastIndexOf(separator)+1, args.xmlFileUmbrella.length());
		String[] words = relatif_file.split("_");
        for (String word : words) {
        	println(word);
        }
        String accession = words[0];
        String user = words[2];
        String[] str_ori_date = words[1].split("-");
        String userYear = str_ori_date[0];
        String userMonth = str_ori_date[1];
        String userDay = str_ori_date[2];
        
        Date date = DateTools.getDate(userDay, userMonth, userYear);
        println("date=" + date);
        //Date date_ex = new Date(2021, 03, 16); 
        //println("date=" + date_ex);
        
		File xmlFile = new File(args.xmlFileUmbrella);
		//Date date= dateFormat.parse(args.sdate);
		
		loadUmbrellaFromGenoscopeFile(xmlFile, user, accession, date);

	}


//	@Override
//	public void execute()  throws Exception {
//		File xmlFile = new File("C:\\Users\\sgas\\projectUmbrella\\project_locus_tag_AYQ.xml");
//		Date date= dateFormat.parse("2021-04-22");
//
//		loadUmbrellaFromGenoscopeFile(xmlFile, "bnoel", "ERPTITI", date);
//
//	}
		
}