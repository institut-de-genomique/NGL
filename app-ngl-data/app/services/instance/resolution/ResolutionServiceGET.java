package services.instance.resolution;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import models.laboratory.experiment.description.ExperimentType;
import models.laboratory.processes.description.ProcessType;
import models.laboratory.resolutions.instance.Resolution;
import models.laboratory.resolutions.instance.ResolutionCategory;
import models.laboratory.resolutions.instance.ResolutionConfiguration;
import models.utils.InstanceConstants;
import models.utils.InstanceHelpers;
import models.utils.dao.DAOException;
import play.Logger;
import play.Logger.ALogger;
import services.instance.InstanceFactory;
import validation.ContextValidation;
import fr.cea.ig.MongoDBDAO;

/**
 * Create Resolutions : for more flexibility, these data are created in a specific collection (in MongoDB) 
 * instead of being created in the description database
 * 23-06-2014  
 * @author dnoisett
 *
 */
public class ResolutionServiceGET {
	

	private static final ALogger logger = Logger.of("ResolutionService");
	private static HashMap<String, ResolutionCategory> resolutionCategories; 
	
	// FDS 15/01 reecriture...
	public static void main(ContextValidation ctx) {	
		
		String inst=play.Play.application().configuration().getString("institute");
		Logger.info("Create and save "+inst+ " resolution categories ...");
		if ( inst.equals("GET") ) {
			Logger.info("Create and save "+inst+ " resolution categories ...");
			
			saveResolutions(ctx, inst);
		}
		else {
			Logger.error("You need to specify only one institute !");
		}
		
		ctx.displayErrors(logger);
	}
	
	// FDS 15/01: fusion en 1 seule methode avec parametre inst
	// FDS 20/01: ajout creation des categories
	public static void saveResolutions(ContextValidation ctx, String inst) {	

			resolutionCategories = createResolutionCategoriesGET();
	
			createRunResolution(ctx); 
			// FDS 15/01: No illumina Depot Resolutions ???
			createExperimentResolution(ctx); 
			createProcessResolution(ctx);
	}

	public static HashMap<String, ResolutionCategory> createResolutionCategoriesGET(){	
		HashMap<String, ResolutionCategory> resoCategories = new HashMap<String, ResolutionCategory>();
		//Run
		resoCategories.put("PbM", new ResolutionCategory("Problème machine", (short) 20));
		resoCategories.put("PbR", new ResolutionCategory("Problème réactifs", (short) 30)); 
		resoCategories.put("SAV", new ResolutionCategory("Problème qualité : SAV", (short) 40)); //40 for CNS only
		resoCategories.put("PbI", new ResolutionCategory("Problème informatique", (short) 60));
		resoCategories.put("Info", new ResolutionCategory("Informations", (short) 70));

		
		//Experiment	
		
		resoCategories.put("Default", new ResolutionCategory("Default", (short) 0));
		
		return resoCategories;
	}


	
	public static void createRunResolution(ContextValidation ctx) {
		List<Resolution> l = new ArrayList<Resolution>();
		
		// FDS 16/01 rendre moins verbeux avec variables XXrC
		
		// PbM
		ResolutionCategory PbMrC= resolutionCategories.get("PbM");

		l.add(InstanceFactory.newResolution("indéterminé","PbM-indetermine", PbMrC,  (short) 1));
		l.add(InstanceFactory.newResolution("chiller","PbM-chiller", PbMrC, (short) 2));
		l.add(InstanceFactory.newResolution("pelletier","PbM-pelletier", PbMrC, (short) 3));
		l.add(InstanceFactory.newResolution("fluidique","PbM-fluidiq", PbMrC, (short) 4));
		l.add(InstanceFactory.newResolution("laser","PbM-laser", PbMrC, (short) 5));
		l.add(InstanceFactory.newResolution("camera","PbM-camera", PbMrC, (short) 6));
		l.add(InstanceFactory.newResolution("focus","PbM-focus", PbMrC, (short) 7));    
		l.add(InstanceFactory.newResolution("pb de vide","PbM-pbVide", PbMrC, (short) 8));
		l.add(InstanceFactory.newResolution("PE module","PbM-PEmodule", PbMrC, (short) 9));
		l.add(InstanceFactory.newResolution("cBot","PbM-cBot", PbMrC, (short) 10));		
			
		// PbR
		ResolutionCategory PbRrC= resolutionCategories.get("PbR");
			
		l.add(InstanceFactory.newResolution("indéterminé","PbR-indetermine", PbRrC, (short) 1));
		l.add(InstanceFactory.newResolution("flowcell","PbR-FC", PbRrC, (short) 2));
		l.add(InstanceFactory.newResolution("cBot","PbR-cBot", PbRrC, (short) 3));
		l.add(InstanceFactory.newResolution("séquencage","PbR-sequencage", PbRrC, (short) 4));
		l.add(InstanceFactory.newResolution("indexing","PbR-indexing", PbRrC, (short) 5));
		l.add(InstanceFactory.newResolution("PE module","PbR-PEmodule", PbRrC, (short) 6));
		l.add(InstanceFactory.newResolution("rehyb primer R1","PbR-rehybR1", PbRrC, (short) 7));
		l.add(InstanceFactory.newResolution("rehyb indexing","PbR-rehybIndexing", PbRrC, (short) 8));
		l.add(InstanceFactory.newResolution("rehyb primer R2","PbR-rehybR2", PbRrC, (short) 9));
		l.add(InstanceFactory.newResolution("erreur réactifs","PbR-erreurReac", PbRrC, (short) 10));
		l.add(InstanceFactory.newResolution("rajout réactifs","PbR-ajoutReac", PbRrC, (short) 11));

		// SAV
		ResolutionCategory SAVrC= resolutionCategories.get("SAV");
		
		l.add(InstanceFactory.newResolution("intensité","SAV-intensite", SAVrC, (short) 1));
		l.add(InstanceFactory.newResolution("densité clusters trop élevée","SAV-densiteElevee", SAVrC, (short) 2));
		l.add(InstanceFactory.newResolution("densité clusters trop faible","SAV-densiteFaible", SAVrC, (short) 3));
		l.add(InstanceFactory.newResolution("densité clusters nulle","SAV-densiteNulle", SAVrC, (short) 4));
		l.add(InstanceFactory.newResolution("%PF","SAV-PF", SAVrC, (short) 5));
		l.add(InstanceFactory.newResolution("phasing","SAV-phasing", SAVrC, (short) 6));
		l.add(InstanceFactory.newResolution("prephasing","SAV-prephasing", SAVrC, (short) 7));
		l.add(InstanceFactory.newResolution("error rate","SAV-errorRate", SAVrC, (short) 8));
		l.add(InstanceFactory.newResolution("Q30","SAV-Q30", SAVrC, (short) 9));
		l.add(InstanceFactory.newResolution("indexing / demultiplexage","SAV-IndDemultiplex", SAVrC, (short) 10));
		
		// PbI
		ResolutionCategory PbIrC= resolutionCategories.get("PbI");
		
		l.add(InstanceFactory.newResolution("indéterminé","PbI-indetermine", PbIrC, (short) 1));
		l.add(InstanceFactory.newResolution("PC","PbI-PC", PbIrC, (short) 2));
		l.add(InstanceFactory.newResolution("écran","PbI-ecran", PbIrC, (short) 3));
		l.add(InstanceFactory.newResolution("espace disq insuf","PbI-espDisqInsuf", PbIrC, (short) 4));
		l.add(InstanceFactory.newResolution("logiciel","PbI-logiciel", PbIrC, (short) 5));
		l.add(InstanceFactory.newResolution("reboot PC","PbI-rebootPC", PbIrC, (short) 6));
		l.add(InstanceFactory.newResolution("erreur paramétrage run","PbI-parametrageRun", PbIrC, (short) 7));
		
		// Info
		ResolutionCategory InforC= resolutionCategories.get("Info");
				
		l.add(InstanceFactory.newResolution("run de validation","Info-runValidation", InforC, (short) 1));
		l.add(InstanceFactory.newResolution("arrêt séquenceur","Info-arretSeq", InforC, (short) 2));
		l.add(InstanceFactory.newResolution("arrêt logiciel","Info_arretLogiciel", InforC, (short) 3));
		l.add(InstanceFactory.newResolution("remboursement","Info-remboursement", InforC, (short) 4));
		l.add(InstanceFactory.newResolution("flowcell redéposée","Info-FCredeposee", InforC, (short) 5));		
		
		ResolutionConfiguration r = new ResolutionConfiguration();
		r.code = "runReso";
		r.resolutions = l;
		r.objectTypeCode = "Run";
		ArrayList<String> al = new ArrayList<String>();
		al.add("RHS2000");
		al.add("RHS2500");
		al.add("RHS2500R");
		al.add("RMISEQ");
		al.add("RGAIIx");
		al.add("RARGUS");
		r.typeCodes = al;
		
		MongoDBDAO.deleteByCode(InstanceConstants.RESOLUTION_COLL_NAME, ResolutionConfiguration.class, "runReso");
		InstanceHelpers.save(InstanceConstants.RESOLUTION_COLL_NAME,r,ctx, false);
	}
	
	
	// FDS pas de distingo CNS/CNG ??
	public static void createExperimentResolution(ContextValidation ctx) {	
		List<Resolution> l = getDefaultResolutionCNS();
				
		ResolutionConfiguration r = new ResolutionConfiguration();
		r.code = "experimentReso";
		r.resolutions = l;
		r.objectTypeCode = "Experiment";
		ArrayList<String> al = new ArrayList<String>(); 
		
		MongoDBDAO.deleteByCode(InstanceConstants.RESOLUTION_COLL_NAME, ResolutionConfiguration.class,r.code);
		List<String> typeCodes=MongoDBDAO.getCollection(InstanceConstants.RESOLUTION_COLL_NAME, ResolutionConfiguration.class).distinct("typeCodes");
		
		try {
			List<ExperimentType> expTypes=ExperimentType.find.findAll();
			for(ExperimentType expType:expTypes){
				if(typeCodes == null || !typeCodes.contains(expType.code)){
					Logger.debug("Add experimentType default resolution "+ expType.code);
					al.add(expType.code);
				}	
			}
		} catch (DAOException e) {
			Logger.error("Creation Resolution for ExperimentType error "+e.getMessage());
		}
		
		r.typeCodes = al;
		ctx.setCreationMode();
		InstanceHelpers.save(InstanceConstants.RESOLUTION_COLL_NAME, r,ctx, false);
	}
	
	public static void createProcessResolution(ContextValidation ctx) {
		List<Resolution> l = new ArrayList<Resolution>();

		l.add(InstanceFactory.newResolution("processus partiel","processus-partiel", resolutionCategories.get("Default"), (short) 1));
		
		ResolutionConfiguration r = new ResolutionConfiguration();
		r.code = "processReso";
		r.resolutions = l;
		r.objectTypeCode = "Process";
		ArrayList<String> al = new ArrayList<String>();
		
		try {
			List<ProcessType> processTypes=ProcessType.find.findAll();
			for(ProcessType processType:processTypes){
					Logger.debug("Add processType default resolution "+ processType.code);
					al.add(processType.code);
			}
		} catch (DAOException e) {
			Logger.error("Creation Resolution for Process Type error "+e.getMessage());
		}
		
		r.typeCodes = al;
		
		MongoDBDAO.deleteByCode(InstanceConstants.RESOLUTION_COLL_NAME, ResolutionConfiguration.class, r.code);
		InstanceHelpers.save(InstanceConstants.RESOLUTION_COLL_NAME, r,ctx, false);
	}
	
	public static List<Resolution> getDefaultResolutionCNS(){
		List<Resolution> l = new ArrayList<Resolution>();
		
		l.add(InstanceFactory.newResolution("déroulement correct",	"correct", resolutionCategories.get("Default"), (short) 1));
		l.add(InstanceFactory.newResolution("problème signalé en commentaire", "pb-commentaire", resolutionCategories.get("Default"), (short) 2));
		l.add(InstanceFactory.newResolution("échec expérience", "echec-experience", resolutionCategories.get("Default"), (short) 3));	

		return l;
	}
}