package services.instance.container;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.ngl.NGLApplication;
import models.Constants;
import models.laboratory.common.instance.PropertyValue;
import models.laboratory.common.instance.TraceInformation;
import models.laboratory.container.instance.Container;
import models.laboratory.sample.instance.Sample;
import models.utils.InstanceConstants;
import models.utils.InstanceHelpers;
import models.utils.dao.DAOException;
import models.utils.instance.ContainerHelper;
import services.instance.AbstractImportDataCNG;
import validation.ContextValidation;

/**
 * Import samples and container from CNG's LIMS to NGL 
 * FDS remplacement de l'appel a Logger par logger
 * FDS 14/01/2016 desactivation import des lanes (plus necessaire depuis la mise en production NGL-SQ 10/2015)
 * 
 * @author dnoisett
 * 
 */

public class ContainerImportCNG extends AbstractImportDataCNG {

//	private static final play.Logger.ALogger logger = play.Logger.of(ContainerImportCNG.class);
	
//	@Inject
//	public ContainerImportCNG (NGLContext ctx) {
//		super("ContainerImportCNG",durationFromStart, durationFromNextIteration, ctx);
//	}
	
//	@Inject
//	public ContainerImportCNG (NGLContext ctx) {
//		super("ContainerImportCNG", ctx);
//	}

	@Inject
	public ContainerImportCNG (NGLApplication app) {
		super("ContainerImportCNG", app);
	}

//	@Override
//	public void runImport() throws SQLException, DAOException {	
//		
//		// NOTE FDS 16/06/2016 la mise a jour de sample ou container apres leur utilisation dans des experiences est dangereuse:
//		// le projetCode, le aliquot_code sont recopiés partout et donc plus a jour !!!
//		// Les mises a jour doivent passer par des outils de migration ponctuels plutot que par l'import automatique...
//		// l'update des support a multiples container n'est pas correctement géré  
//		//     => commenter les methodes updateXXX
//		
//		// -1-  !!! les samples ne sont pas au sens NGL des containers mais sont nécessaires avant tout import de containers...
//	    loadSamples();
//	    //updateSamples();
//		
//		// FDS: loadContainers: le 2 eme param "experiment-type-code" est l'experience d'ou est sensé venir le container 
//		//                      le 3 eme param "importState" est le status NGL du container a importer
//		//      updateContainers  "importState"necessaire
//	    
//		// -2- FDS 14-01-2016 NGL-909 : import des plaques de samples
//		/* Ce sont les containers !!!
//		   si on reprend la methode loadContainers comment alors distinger les plaques de sample des plaques de libraries ??
//		     =>surcharger un peu le parametre containerCategoryCode:  sample-well / library-well au lieu de simplement 'well'
//		   12/04/2016 importer a iw-p= in waiting processus
//		*/	
//	    loadContainers("sample-well",null,"iw-p"); 
//	    //updateContainers("sample-well",null);
//	    
//		// -3- librairies en tube
//	    
//		// -3.1- lib-normalization
//	    loadContainers("tube","lib-normalization","is"); // is=in stock
//	    loadContainers("tube","lib-normalization","iw-p"); //iw-p=in waiting processus
//	    //updateContainers("tube","lib-normalization"); // pas de specificite de status pour la mise a jour
//		
//		// -3.2- denat-dil-lib
//	    loadContainers("tube","denat-dil-lib","is"); //is=in stock
//	    loadContainers("tube","denat-dil-lib","iw-p"); //iw-p=in waiting processus
//	    //updateContainers("tube","denat-dil-lib"); // pas de specificite de status pour la mise a jour
//	    
//		// -4- FDS 15/05/2016 NGL-1044 : import librairies en plaques-96 : lib-normalization et denat-dil-lib
//	    
//		// -4.1- lib-normalization ; importer a l'etat iw-p
//	    //       !! attention probleme connu avec les puits WATER qui sont consideres comme des denat-dil-lib
//		loadContainers("library-well","lib-normalization","iw-p"); 
//		//updateContainers("library-well","lib-normalization");
//		
//		//-4.2- denat-dil-lib ; importer a l'etat iw-p .
//		loadContainers("library-well","denat-dil-lib","iw-p");
//		// updateContainers("library-well","denat-dil-lib");	
//		
//	    /* 14/01/2016 desactivé puisque la creation des flowcells est faite dans NGL-SQ
//		// -5- lanes/flowcell
//		 * 
//		loadContainers("lane","prepa-flowcell",null);
//		updateContainers("lane","prepa-flowcell");
//		*/
//	}
	
	@Override
	public void runImport(ContextValidation contextError) throws SQLException, DAOException {
		
		// NOTE FDS 16/06/2016 la mise a jour de sample ou container apres leur utilisation dans des experiences est dangereuse:
		// le projetCode, le aliquot_code sont recopiés partout et donc plus a jour !!!
		// Les mises a jour doivent passer par des outils de migration ponctuels plutot que par l'import automatique...
		// l'update des support a multiples container n'est pas correctement géré  
		//     => commenter les methodes updateXXX
		
		// -1-  !!! les samples ne sont pas au sens NGL des containers mais sont nécessaires avant tout import de containers...
	    loadSamples(contextError);
	    //updateSamples();
		
		// FDS: loadContainers: le 2 eme param "experiment-type-code" est l'experience d'ou est sensé venir le container 
		//                      le 3 eme param "importState" est le status NGL du container a importer
		//      updateContainers  "importState"necessaire
	    
		// -2- FDS 14-01-2016 NGL-909 : import des plaques de samples
		/* Ce sont les containers !!!
		   si on reprend la methode loadContainers comment alors distinger les plaques de sample des plaques de libraries ??
		     =>surcharger un peu le parametre containerCategoryCode:  sample-well / library-well au lieu de simplement 'well'
		   12/04/2016 importer a iw-p= in waiting processus
		*/	
	    loadContainers(contextError,"sample-well",null,"iw-p"); 
	    //updateContainers("sample-well",null);
	    
		// -3- librairies en tube
	    
		// -3.1- lib-normalization
	    loadContainers(contextError,"tube","lib-normalization","is"); // is=in stock
	    loadContainers(contextError,"tube","lib-normalization","iw-p"); //iw-p=in waiting processus
	    //updateContainers("tube","lib-normalization"); // pas de specificite de status pour la mise a jour
		
		// -3.2- denat-dil-lib
	    loadContainers(contextError,"tube","denat-dil-lib","is"); //is=in stock
	    loadContainers(contextError,"tube","denat-dil-lib","iw-p"); //iw-p=in waiting processus
	    //updateContainers("tube","denat-dil-lib"); // pas de specificite de status pour la mise a jour
	    
		// -4- FDS 15/05/2016 NGL-1044 : import librairies en plaques-96 : lib-normalization et denat-dil-lib
	    
		// -4.1- lib-normalization ; importer a l'etat iw-p
	    //       !! attention probleme connu avec les puits WATER qui sont consideres comme des denat-dil-lib
		loadContainers(contextError,"library-well","lib-normalization","iw-p"); 
		//updateContainers("library-well","lib-normalization");
		
		//-4.2- denat-dil-lib ; importer a l'etat iw-p .
		loadContainers(contextError,"library-well","denat-dil-lib","iw-p");
		// updateContainers("library-well","denat-dil-lib");	
		
	    /* 14/01/2016 desactivé puisque la creation des flowcells est faite dans NGL-SQ
		// -5- lanes/flowcell
		 * 
		loadContainers("lane","prepa-flowcell",null);
		updateContainers("lane","prepa-flowcell");
		*/
	}
		
	public void loadSamples(ContextValidation contextError) throws SQLException, DAOException {
		logger.debug("Start LOADING samples");
			
		//-1- chargement depuis la base source Postgresql
		//Logger.debug("1/3 loading from source database...");
		List<Sample> samples = limsServices.findSampleToCreate(contextError, null) ;
		
		//-2- sauvegarde dans la base cible MongoDb
		//Logger.debug("2/3 saving to dest database...");
		List<Sample> samps=InstanceHelpers.save(InstanceConstants.SAMPLE_COLL_NAME, samples, contextError, true);
		
		//-3- timestamp-er dans la base source Postgresql ce qui a été traité
		//Logger.debug("3/3 updating source database...");
		limsServices.updateLimsSamples(samps, contextError, "creation");
		
		logger.debug("End loading samples");
	}
	
	public void updateSamples(ContextValidation contextError) throws SQLException, DAOException {
		logger.debug("start UPDATING samples");
		
		//-1- chargement depuis la base source Postgresql
		//Logger.debug("1/3 loading from source database...");
		List<Sample>  samples = limsServices.findSampleToModify(contextError, null);
		
		//-2a- trouver les samples concernés dans la base mongoDB et les supprimer
		//Logger.debug("2a/3 delete from dest database...");
		for (Sample sample : samples) {
			Sample oldSample = MongoDBDAO.findByCode(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, sample.code);			
			sample.traceInformation = TraceInformation.updateOrCreateTraceInformation(oldSample.traceInformation, Constants.NGL_DATA_USER);			
			MongoDBDAO.deleteByCode(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, sample.code);
		}
		
		//-2b- sauvegarder les samples dans la base cible MongoDb
		//Logger.debug("2b/3 saving to dest database...");
		List<Sample> samps = InstanceHelpers.save(InstanceConstants.SAMPLE_COLL_NAME, samples, contextError, true);
		
		//-3- mise a jour dans la base source Postgresql ce qui a été traité
		//Logger.debug("3/3 updating source database...");
		limsServices.updateLimsSamples(samps, contextError, "update");
		
		logger.debug("End updating samples");
	}
	
	// 22/10/2015 ajout parametre importState pour la reprise
	public void loadContainers(ContextValidation contextError, String containerCategoryCode, String experimentTypeCode, String importState) throws SQLException, DAOException {
		logger.debug("Start loading containers of type:" + containerCategoryCode + " from experiment type: "+ experimentTypeCode);		
		
		//-1- chargement depuis la base source Postgresql
		List<Container> containers = limsServices.findContainerToCreate(contextError, containerCategoryCode, experimentTypeCode, importState);
		
		HashMap<String, PropertyValue> mapCodeSupportSeq = null;
		
		/* 14/01/2016  on n'importe plus de lanes...
		if (containerCategoryCode.equals("lane")) {
			// propriété specifique aux containers "lanes"
			mapCodeSupportSeq = limsServices.setSequencingProgramTypeToContainerSupport(contextError, "creation");
		}
		*/
		
		//-2- création des containerSupports
		ContainerHelper.createSupportFromContainers(containers, mapCodeSupportSeq, contextError);
		
		//-3- sauvegarde dans la base cible MongoDb des containers
		List<Container> ctrs=InstanceHelpers.save(InstanceConstants.CONTAINER_COLL_NAME, containers, contextError, true);
		
		//-4- mise a jours dans la base source Postresql ce qui a été transféré
		// FDS 14/01/2016 differentiencier les cas sample-well, library-well 
		//                on n'importe plus les lanes
		/*if (containerCategoryCode.equals("lane")) {
			limsServices.updateLimsLanes(ctrs, contextError, "creation");		
		}
		else */
		if (containerCategoryCode.equals("tube")) {
			limsServices.updateLimsTubes(ctrs, contextError, "creation");
		} else if (containerCategoryCode.equals("sample-well")) {
			limsServices.updateLimsSamplePlates(ctrs, contextError, "creation");
		} else if (containerCategoryCode.equals("library-well")) {
			limsServices.updateLimsTubePlates(ctrs, contextError, "creation");
		}	
		logger.debug("End loading containers of type " + containerCategoryCode+ " from experiment type: "+ experimentTypeCode);		
	}
	
	// FDS 16/06/2016 attention les mises a jours peuvent poser probleme.... utiliser avec précaution...
	// voir updateSupportFromUpdatedContainers
	public void updateContainers(ContextValidation contextError, String containerCategoryCode, String experimentTypeCode) throws SQLException, DAOException {
		logger.debug("Start updating containers of type: " + containerCategoryCode+ " from experiment type: "+ experimentTypeCode);		
		
		//-1- chargement depuis la base source Postgresql
		List<Container> containers = limsServices.findContainerToModify(contextError, containerCategoryCode, experimentTypeCode);
		
		HashMap<String, PropertyValue> mapCodeSupportSeq = null;
		
		/* 14/01/2016 on n'importe plus les lanes
		if (containerCategoryCode.equals("lane")) {
			mapCodeSupportSeq = limsServices.setSequencingProgramTypeToContainerSupport(contextError, "update");
		}
		*/
		
		//-2- Modifier les containersSupports
		ContainerHelper.updateSupportFromUpdatedContainers(containers, mapCodeSupportSeq, contextError);
		
		//-3- trouver les containers concernés dans la base mongoDB et les supprimer
		for (Container container : containers) {
			Container oldContainer = MongoDBDAO.findByCode(InstanceConstants.CONTAINER_COLL_NAME, Container.class, container.code);
			
			container.traceInformation = TraceInformation.updateOrCreateTraceInformation(oldContainer.traceInformation, Constants.NGL_DATA_USER);
			
			MongoDBDAO.deleteByCode(InstanceConstants.CONTAINER_COLL_NAME, Container.class, container.code);
		}
		
		//-4- sauvegarder dans la base cible MongoDb les container modifiés
		List<Container> ctrs=InstanceHelpers.save(InstanceConstants.CONTAINER_COLL_NAME, containers, contextError, true);
		
		//-5- mise a jour dans la base source Postresql ce qui a été traité
		
		/* 14/01/2016 on ne traite plus les lanes
		if (containerCategoryCode.equals("lane")) {
			limsServices.updateLimsLanes(ctrs, contextError, "update");		
		}
		else */
		if  (containerCategoryCode.equals("tube")) {
			limsServices.updateLimsTubes(ctrs, contextError, "update");
		} else if (containerCategoryCode.equals("sample-well")) {
			limsServices.updateLimsSamplePlates(ctrs, contextError, "update");
		} else if (containerCategoryCode.equals("library-well")) {
			limsServices.updateLimsTubePlates(ctrs, contextError, "update");
		}
		logger.debug("End updating containers of type: " + containerCategoryCode+ " from experiment type: "+ experimentTypeCode);	
	}
	
}
