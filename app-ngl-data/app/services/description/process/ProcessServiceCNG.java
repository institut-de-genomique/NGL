package services.description.process;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;// ajout FDS pour getPETForQCTransfertPurif

import models.laboratory.common.description.Institute;
import models.laboratory.common.description.Level;
import models.laboratory.common.description.PropertyDefinition;
import models.laboratory.common.description.Value;
import models.laboratory.experiment.description.ExperimentType;
import models.laboratory.processes.description.ProcessCategory;
import models.laboratory.processes.description.ProcessExperimentType;
import models.laboratory.processes.description.ProcessType;
import models.utils.dao.DAOException;
import models.utils.dao.DAOHelpers;
import play.data.validation.ValidationError;
import services.description.Constants;
import services.description.DescriptionFactory;
import services.description.common.LevelService;
import services.description.declaration.cng.Nanopore;

public class ProcessServiceCNG  extends AbstractProcessService {

	/* *
	 * Save all Process Categories
	 * @param errors
	 * @throws DAOException 
	 */
	@Override
	public void saveProcessCategories(Map<String, List<ValidationError>> errors) throws DAOException {
		List<ProcessCategory> l = new ArrayList<>();
		
		l.add(DescriptionFactory.newSimpleCategory(ProcessCategory.class, "Prep. librairie pr séquençage Illumina", "library"));          // 27/09/2017 fdsantos NGL-1201 renommage label
		l.add(DescriptionFactory.newSimpleCategory(ProcessCategory.class, "Prep. librairie pr séquençage Nanopore", "nanopore-library")); // 27/09/2017 fdsantos NGL-1201 renommage label
		l.add(DescriptionFactory.newSimpleCategory(ProcessCategory.class, "Normalisation", "normalization"));
		l.add(DescriptionFactory.newSimpleCategory(ProcessCategory.class, "Séquençage", "sequencing"));
		// 28/11/2016 fdsantos JIRA NGL-1164; categorie de processus ne contenant aucune transformation mais uniquement des QC ou transferts...
		//  attention bug connu: manque la puce "terminer" dans le dispatch final.
		l.add(DescriptionFactory.newSimpleCategory(ProcessCategory.class, "Exp satellites", "satellites"));

		DAOHelpers.saveModels(ProcessCategory.class, l, errors);
	}

	/* *
	 * Save all Process types
	 * @param errors
	 * @throws DAOException 		
	 * warning "codes" must not have uppercase letters
	 *    par convention les experimentTypes externes aux processus doivent avoir l'indice (-1) dans la methode getPET
	 *    TODO:  renommer les 'ext' en donnant le nom du processus ex: ext-to-denat-dil-lib---> ext-to-illumina-run
	 */
	@Override
	public void saveProcessTypes(Map<String, List<ValidationError>> errors) throws DAOException {
		List<ProcessType> l = new ArrayList<>();
		List<Institute> CNG = DescriptionFactory.getInstitutes(Constants.CODE.CNG); // 04/04/2018; puisque tout se refere uniqt au CNG, alleger l'ecriture...

		/* 04/04/2018 regrouper les processus par categorie 
		 * -----------------"library"----------------------
		 */
		
		// FDS ajout 27/01/2016 -- JIRA NGL-894: processus pour X5; chgt label 16/09/2016
		l.add(DescriptionFactory.newProcessType("WG PCR free (FC ordonnée)", "x5-wg-pcr-free", ProcessCategory.find.findByCode("library"),
				1,
				getPropertyDefinitionsX5WgPcrFree(), 
				Arrays.asList(
						getPET("ext-to-x5-wg-pcr-free",-1), //ordered list of experiment type in process type
						getPET("prep-pcr-free",0),
						getPET("lib-normalization",1), 
						getPET("normalization-and-pooling",1), // ajout 06/09/2017 (NGL-1576)
						getPET("prepa-fc-ordered",2), 
						getPET("illumina-depot",3) ),         
				getExperimentTypes("prep-pcr-free").get(0),         //first experiment type    
				getExperimentTypes("illumina-depot").get(0),        //last  experiment type
				getExperimentTypes("ext-to-x5-wg-pcr-free").get(0), //void  experiment type
				CNG));
		
		// FDS ajout 10/08/2016 JIRA NGL-1047 processus X5_WG NANO; mise en prod 1/09/2016; chgt label 16/06/2016
		// 26/09/2016 modif commence par ("prep-wg-nano",0)
		l.add(DescriptionFactory.newProcessType("WG NANO (FC ordonnée)", "x5-wg-nano", ProcessCategory.find.findByCode("library"),
				2, 
				getPropertyDefinitionsX5WgNanoDNAseq(), 
				Arrays.asList(
						getPET("ext-to-x5-wg-nano",-1), //ordered list of experiment type in process type
						getPET("prep-wg-nano",0),
						getPET("pcr-and-purification",1), 
						getPET("lib-normalization",2),
						getPET("normalization-and-pooling",2),
						getPET("prepa-fc-ordered",3), 
						getPET("illumina-depot",4) ),      
				getExperimentTypes("prep-wg-nano").get(0),      //first experiment type;
				getExperimentTypes("illumina-depot").get(0),    //last  experiment type
				getExperimentTypes("ext-to-x5-wg-nano").get(0), //void  experiment type
				CNG));		
				
		// FDS ajout 31/05/2016 JIRA NGL-1025: processus long type "library"; 18/01/2017 JIRA NGL-1259 renommer en rna-lib-process
		l.add(DescriptionFactory.newProcessType("Prep lib RNAseq", "rna-lib-process", ProcessCategory.find.findByCode("library"),
				3,
				getPropertyDefinitionsRNAlib(), 
				Arrays.asList(
						getPET("ext-to-rna-lib-process",-1), //ordered list of experiment type in process type
						getPET("library-prep",0),
						getPET("pcr-and-purification",1),
						getPET("normalization-and-pooling",2) , 
						getPET("lib-normalization",2) ), // FDS 16/11/2016 ajout d'une 2eme exp de niveau 2 
				getExperimentTypes("library-prep").get(0),              //first experiment type
				getExperimentTypes("normalization-and-pooling").get(0), //last  experiment type (1 des 2 qui sont de niveau le + élevé)
				getExperimentTypes("ext-to-rna-lib-process").get(0),    //void  experiment type
				CNG));		
		
		// FDS ajout 20/02/2017 NGL-1167: processus Chromium 10x WG
		// FDS modification 13/06/2017 NGL-1473: allongement du processus-> illumina depot (renommage ..FC ordonnée)
		l.add(DescriptionFactory.newProcessType("Prep Chromium WG (FC ordonnée)", "wg-chromium-lib-process", ProcessCategory.find.findByCode("library"),
				4,
				getPropertyDefinitionsWgChromium(), 
				Arrays.asList(
						getPET("ext-to-wg-chromium-lib-process",-1), //ordered list of experiment type in process type
						getPET("chromium-gem-generation",0),
						getPET("wg-chromium-lib-prep",1), 
						getPET("lib-normalization",2),
						getPET("normalization-and-pooling",2),
						getPET("prepa-fc-ordered",3),
						getPET("illumina-depot",4)), 			
				getExperimentTypes("chromium-gem-generation").get(0),        //first experiment type    
				getExperimentTypes("illumina-depot").get(0),                 //last  experiment type
				getExperimentTypes("ext-to-wg-chromium-lib-process").get(0), //void  experiment type
				CNG));
		
	
		// FDS ajout 10/07/2017 NGL-1201: processus Capture Sureselect principal (4000/X5 = FC ordonnée)
		// FDS 14/12/2017 NGL-1730 : renommage label (ajout NovaSeq)
		l.add(DescriptionFactory.newProcessType("Prep. Capture prod. (4000 / X5 / NovaSeq)", "capture-prep-process-fc-ord", ProcessCategory.find.findByCode("library"),
				5,
				getPropertyDefinitionsCapture(), 
				Arrays.asList(
						getPET("ext-to-capture-prep-process-fc-ord",-1), //ordered list of experiment type in process type
						getPET("fragmentation",0),
						getPET("sample-prep",1),
						getPET("pcr-and-purification",2),
						getPET("capture",3),
						getPET("pcr-and-indexing",4), 		
						getPET("lib-normalization",5),
						getPET("normalization-and-pooling",5),   // 2 de meme niveau
						getPET("prepa-fc-ordered",6), 
						getPET("illumina-depot",7)),           
				getExperimentTypes("fragmentation").get(0),                      //first experiment type    
				getExperimentTypes("illumina-depot").get(0),                     //last  experiment type
				getExperimentTypes("ext-to-capture-prep-process-fc-ord").get(0), //void  experiment type
				CNG));
		
		// FDS ajout 10/07/2017 NGL-1201: processus Capture Sureselect principal (2000/2500/Miseq/NextSeq)
		// FDS 14/12/2017 NGL-1730 : renommage label (suppression 2000 : ne sont plus en fonction)
		l.add(DescriptionFactory.newProcessType("Prep. Capture prod. (2500 / NextSeq)", "capture-prep-process-fc", ProcessCategory.find.findByCode("library"),
				6,
				getPropertyDefinitionsCapture(), 
				Arrays.asList(
						getPET("ext-to-capture-prep-process-fc",-1), //ordered list of experiment type in process type
						getPET("fragmentation",0),
						getPET("sample-prep",1),
						getPET("pcr-and-purification",2),
						getPET("capture",3),
						getPET("pcr-and-indexing",4), 
						getPET("lib-normalization",5),
						getPET("normalization-and-pooling",5),   // 2 de meme niveau
						getPET("denat-dil-lib",6),
				    	getPET("prepa-flowcell",7),
				    	getPET("illumina-depot",8)),   
				getExperimentTypes("fragmentation").get(0),                   //first experiment type    
				getExperimentTypes("illumina-depot").get(0),                  //last  experiment type
				getExperimentTypes("ext-to-capture-prep-process-fc").get(0),  //void  experiment type
				CNG));  	
		
		// FDS ajout 10/07/2017 NGL-1201: processus Capture Sureselect reprise (1) (4000/X5)
		// FDS 14/12/2017 NGL-1730 : renommage label (ajout NovaSeq)
		l.add(DescriptionFactory.newProcessType("Prep. Capture reprise (1) (4000 / X5 / NovaSeq)", "pcr-capture-pcr-indexing-fc-ord", ProcessCategory.find.findByCode("library"),
				7,
				getPropertyDefinitionsPrcCapturePcrIndexing(),// FDS 19/03/2018 NGL-1906 ajout 
				Arrays.asList(
						getPET("ext-to-pcr-capture-pcr-indexing-fc-ord",-1), //ordered list of experiment type in process type
						getPET("sample-prep",-1), 
						getPET("pcr-and-purification",0),
						getPET("capture",1),
						getPET("pcr-and-indexing",2), 	
						getPET("lib-normalization",3),
						getPET("normalization-and-pooling",3),   // 2 de meme niveau
						getPET("prepa-fc-ordered",4), 
						getPET("illumina-depot",5)),           
				getExperimentTypes("pcr-and-purification").get(0),				      //first experiment type    
				getExperimentTypes("illumina-depot").get(0),                          //last  experiment type
				getExperimentTypes("ext-to-pcr-capture-pcr-indexing-fc-ord").get(0),  //void  experiment type
				CNG));
		
		// FDS ajout 10/07/2017 NGL-1201: processus Capture Sureselect reprise (1) (2000/2500/NextSeq)
		// FDS 14/12/2017 NGL-1730 : renommage label (suppression 2000 : ne sont plus en fonction)
		l.add(DescriptionFactory.newProcessType("Prep. Capture reprise (1) (2500 / NextSeq)", "pcr-capture-pcr-indexing-fc", ProcessCategory.find.findByCode("library"),
				8,
				getPropertyDefinitionsPrcCapturePcrIndexing(), // FDS 19/03/2018 NGL-1906 ajout 
				Arrays.asList(
						getPET("ext-to-pcr-capture-pcr-indexing-fc",-1), //ordered list of experiment type in process type
						getPET("sample-prep",-1), 
						getPET("pcr-and-purification",0),
						getPET("capture",1),
						getPET("pcr-and-indexing",2),
						getPET("lib-normalization",3),
						getPET("normalization-and-pooling",3),   // 2 de meme niveau
						getPET("denat-dil-lib",4),
				    	getPET("prepa-flowcell",5),
				    	getPET("illumina-depot",6)),  
				getExperimentTypes("pcr-and-purification").get(0),				          //first experiment type    
				getExperimentTypes("illumina-depot").get(0),                              //last  experiment type
				getExperimentTypes("ext-to-pcr-capture-pcr-indexing-fc").get(0),  //void  experiment type
				CNG));
		
		// FDS ajout 10/07/2017 NGL-1201: processus Capture Sureselect reprise (2) (4000/X5)
		// FDS 14/12/2017 NGL-1730 : renommage label (ajout NovaSeq)
		l.add(DescriptionFactory.newProcessType("Prep. Capture reprise (2) (4000 / X5 / NovaSeq)", "capture-pcr-indexing-fc-ord", ProcessCategory.find.findByCode("library"),
				9,
				getPropertyDefinitionsCapturePcrIndexing(),
				Arrays.asList(
						getPET("ext-to-capture-pcr-indexing-fc-ord",-1), //ordered list of experiment type in process type
						getPET("pcr-and-purification",-1),
						getPET("capture",0),
						getPET("pcr-and-indexing",1),
						getPET("lib-normalization",2),
						getPET("normalization-and-pooling",2),   // 2 de meme niveau
						getPET("prepa-fc-ordered",3), 
						getPET("illumina-depot",4)),
				getExperimentTypes("capture").get(0),                            //first experiment type    
				getExperimentTypes("pcr-and-indexing").get(0),                   //last  experiment type
				getExperimentTypes("ext-to-capture-pcr-indexing-fc-ord").get(0), //void  experiment type
				CNG));
		
		// FDS ajout 10/07/2017 NGL-1201: processus Capture Sureselect reprise (2) (2000/2500/NextSeq)
		// FDS 14/12/2017 NGL-1730 : renommage label (suppression 2000 : ne sont plus en fonction)
		l.add(DescriptionFactory.newProcessType("Prep. Capture reprise (2) (2500 / NextSeq)", "capture-pcr-indexing-fc", ProcessCategory.find.findByCode("library"),
				10,
				getPropertyDefinitionsCapturePcrIndexing(),
				Arrays.asList(
						getPET("ext-to-capture-pcr-indexing-fc",-1), //ordered list of experiment type in process type
						getPET("pcr-and-purification",-1),
						getPET("capture",0),
						getPET("pcr-and-indexing",1),
						getPET("lib-normalization",2),
						getPET("normalization-and-pooling",2),   // 2 de meme niveau
					    getPET("denat-dil-lib",3),
						getPET("prepa-flowcell",4), 
						getPET("illumina-depot",5)),
				getExperimentTypes("capture").get(0),                        //first experiment type    
				getExperimentTypes("pcr-and-indexing").get(0),               //last  experiment type
				getExperimentTypes("ext-to-capture-pcr-indexing-fc").get(0), //void  experiment type
				CNG));					

		// FDS ajout 10/07/2017 NGL-1201: processus Capture Sureselect reprise (3) (4000/X5 = FC ordonnée)
		// FDS 14/12/2017 NGL-1730 : renommage label (ajout NovaSeq)
		l.add(DescriptionFactory.newProcessType("Processus reprise (3) (4000 / X5 / NovaSeq)", "pcr-indexing-process-fc-ord", ProcessCategory.find.findByCode("library"),
				11,
				null,
				Arrays.asList(
						getPET("ext-to-pcr-indexing-process-fc-ord",-1), //ordered list of experiment type in process type
						getPET("capture",-1),
						getPET("pcr-and-indexing",0), 	
						getPET("lib-normalization",1),
						getPET("normalization-and-pooling",1),   // 2 de meme niveau
						getPET("prepa-fc-ordered",2), 
						getPET("illumina-depot",3)),
				getExperimentTypes("pcr-and-indexing").get(0),                    //first experiment type    
				getExperimentTypes("illumina-depot").get(0),                      //last  experiment type
				getExperimentTypes("ext-to-pcr-indexing-process-fc-ord").get(0),  //void  experiment type
				CNG));
		
		// FDS ajout 10/07/2017 NGL-1201: processus Capture Sureselect reprise (3) (2000/2500/NextSeq)
		// FDS 14/12/2017 NGL-1730 : renommage label (supression 2000 : ne sont plus en fonction)
		l.add(DescriptionFactory.newProcessType("Processus reprise (3) (2500 / NextSeq)", "pcr-indexing-process-fc", ProcessCategory.find.findByCode("library"),		
				12,
				null,
				Arrays.asList(
						getPET("ext-to-pcr-indexing-process-fc",-1), //ordered list of experiment type in process type
						getPET("capture",-1),
						getPET("pcr-and-indexing",0), 
						getPET("lib-normalization",1),
						getPET("normalization-and-pooling",1),   // 2 de meme niveau
						getPET("denat-dil-lib",2),
						getPET("prepa-flowcell",3), 
						getPET("illumina-depot",4)),
				getExperimentTypes("pcr-and-indexing").get(0),                //first experiment type    
				getExperimentTypes("illumina-depot").get(0),                  //last  experiment type
				getExperimentTypes("ext-to-pcr-indexing-process-fc").get(0),  //void  experiment type
				CNG));		
				
		/* OOps trop tot... pas pour la 2.1.2 attendre
		// FDS ajout 06/04/2018 NGL-1727: processus SmallRNASeq 
		l.add(DescriptionFactory.newProcessType("Small RNAseq", "small-rna-seq-process", ProcessCategory.find.findByCode("library"),		
				13,
				getPropertyDefinitionsSmallRNASeq(),
				Arrays.asList(
						getPET("ext-to-small-rna-seq-process",-1), //ordered list of experiment type in process type
						getPET("small-rnaseq-lib-prep",0), 
						getPET("normalization-and-pooling",1),   
						getPET("prepa-fc-ordered",2), 
						getPET("denat-dil-lib",2), // 2 de meme niveau
						getPET("illumina-depot",3),
						getPET("prepa-flowcell",3),// 2 de meme niveau
						getPET("illumina-depot",4)), //  defini une deuxiememe fois ???
				getExperimentTypes("small-rnaseq-lib-prep").get(0),         //first experiment type    
				getExperimentTypes("illumina-depot").get(0),                //last  experiment type
				getExperimentTypes("ext-to-small-rna-seq-process").get(0),  //void  experiment type
				CNG));
		
		// FDS ajout 04/04/2018 NGL-1727: processus BisSeq
		l.add(DescriptionFactory.newProcessType("BisSeq (FC ordonnée)", "bis-seq-process-fc-ord", ProcessCategory.find.findByCode("library"),		
				14,
				getPropertyDefinitionsBisSeq(),
				Arrays.asList(
						getPET("ext-to-bis-seq-process-fc-ord",-1), //ordered list of experiment type in process type
						getPET("bisseq-lib-prep",0), 
						getPET("lib-normalization",1),           // 2 de meme niveau
						getPET("normalization-and-pooling",1),   
						getPET("prepa-fc-ordered",2),
						getPET("illumina-depot",3)),
				getExperimentTypes("bisseq-lib-prep").get(0),                //first experiment type    
				getExperimentTypes("illumina-depot").get(0),                 //last  experiment type
				getExperimentTypes("ext-to-bis-seq-process-fc-ord").get(0),  //void  experiment type
				CNG));		
		*/
		
		
		/* 04/04/2018 regrouper les processus par categorie 
		 *-----------------"normalization"-----------------
		 */
				
		// FDS ajout 12/04/2016 JIRA NGL-894/981 : processus court demarrant a lib-normalization, pas de proprietes; chgt label 15/09/2016; 
		// FDS 13/03/2017 NGL-1167 ajout "wg-chromium-lib-prep" en -1; chgt label 06/09/2017 (NGL-1576); 
		// chgt label  13/09/2017 NGL-1201; erreur=> correction label + ajout pcr+indexing en -1 ; 14/12/2017 NGL-1730 renommage label ajout Novaseq 
		l.add(DescriptionFactory.newProcessType("Norm(+pooling),FC ordonnée, dépôt (4000 / X5 / NovaSeq)", "norm-fc-ordered-depot", ProcessCategory.find.findByCode("normalization"),
				20,
				null,  // pas de propriétés ??
				Arrays.asList(
						getPET("ext-to-norm-fc-ordered-depot",-1), //ordered list of experiment type in process type
						getPET("prep-pcr-free",-1), 
						getPET("pcr-and-purification",-1), 
						getPET("pcr-and-indexing",-1), 
						getPET("wg-chromium-lib-prep",-1),
						getPET("lib-normalization",0), 
						getPET("normalization-and-pooling",0),
						getPET("prepa-fc-ordered",1), 
						getPET("illumina-depot",2) ),           
				getExperimentTypes("lib-normalization").get(0),            //first experiment type
				getExperimentTypes("illumina-depot").get(0),               //last  experiment type
				getExperimentTypes("ext-to-norm-fc-ordered-depot").get(0), //void  experiment type
				CNG));	
		
        // FDS ajout 28/10/2016 JIRA NGL-1025: nouveau processus court pour RNAseq; modif du label=> supprimer RNA; chgt label (NGL-1576); 
		// FDS 13/07/2017 NGL-1201 erreur => correction label + ajout pcr+indexing en -1
		// FDS 14/12/2017 NGL-1730: renomage label (suppression 2000 : plus en fonction)
		l.add(DescriptionFactory.newProcessType("Norm(+pooling), dénat, FC, dépôt (2500 / MiSeq / NextSeq)", "norm-and-pool-denat-fc-depot", ProcessCategory.find.findByCode("normalization"),
				21,   
				null, // pas de propriétés ??
				Arrays.asList(
						getPET("ext-to-norm-and-pool-denat-fc-depot",-1), //ordered list of experiment type in process type
						getPET("pcr-and-purification", -1),	
						getPET("pcr-and-indexing", -1),	
						getPET("normalization-and-pooling",0), 
						getPET("lib-normalization",0),
						getPET("denat-dil-lib",1),
						getPET("prepa-flowcell",2),
						getPET("illumina-depot",3) ),          
				getExperimentTypes("normalization-and-pooling").get(0),           //first experiment type         
				getExperimentTypes("illumina-depot").get(0),                      //last  experiment type
				getExperimentTypes("ext-to-norm-and-pool-denat-fc-depot").get(0), //void  experiment type
				CNG));	
		
		
		/* 04/04/2018 regrouper les processus par categorie 
		 *-----------------"sequencing"--------------------
		 */
		
		// FDS 13/09/2017 NGL-1201: renommage label en "Dénat, prep FC, dépôt (2000/2500/MiSeq/NextSeq)"
		// FDS 14/12/2017 NGL-1730: renommage label (supprimer 2000 : ne sont en fonction)
		l.add(DescriptionFactory.newProcessType("Dénat, prep FC, dépôt (2500 / MiSeq / NextSeq)", "illumina-run", ProcessCategory.find.findByCode("sequencing"),
				40,
		        getPropertyDefinitionsIlluminaDepotCNG("prepa-flowcell"),
				Arrays.asList(
						getPET("ext-to-denat-dil-lib",-1), // ordered list of experiment type in process type
		            	getPET("lib-normalization",-1), 
		            	getPET("normalization-and-pooling",-1),	
		            	getPET("denat-dil-lib",0),
		            	getPET("prepa-flowcell",1),
		            	getPET("illumina-depot",2)),        
				getExperimentTypes("denat-dil-lib").get(0),         //first experiment type
				getExperimentTypes("illumina-depot").get(0),        //last  experiment type
				getExperimentTypes("ext-to-denat-dil-lib").get(0),  //void  experiment type
				CNG));
		
		// FDS 13/09/2017 NGL-1201: renommage label en "Prep FC, dépôt (2000/2500/MiSeq/NextSeq)"
		// FDS 14/12/2017 NGL-1730: renommage label (supprimer 2000 : ne sont en fonction)
		l.add(DescriptionFactory.newProcessType("Prep FC, dépôt (2500 / MiSeq / NextSeq)", "prepfc-depot", ProcessCategory.find.findByCode("sequencing"),
				42,
				getPropertyDefinitionsIlluminaDepotCNG("prepa-flowcell"),
				Arrays.asList(
						getPET("ext-to-prepa-flowcell",-1), //ordered list of experiment type in process type
						getPET("denat-dil-lib",-1),
						getPET("prepa-flowcell",0),
						getPET("illumina-depot",1) ),        
				getExperimentTypes("prepa-flowcell").get(0),        //first experiment type
				getExperimentTypes("illumina-depot").get(0),        //last  experiment type
				getExperimentTypes("ext-to-prepa-flowcell").get(0), //void  experiment type
				CNG));	
		
		// FDS 14/12/2017 NGL-1730: renomage label: ajout NovaSeq
		l.add(DescriptionFactory.newProcessType("Prep FC ordonnée, dépôt (4000 / X5 / NovaSeq)", "prepfcordered-depot", ProcessCategory.find.findByCode("sequencing"),
				43,
				getPropertyDefinitionsIlluminaDepotCNG("prepa-fc-ordered"), 
				Arrays.asList(
						getPET("ext-to-prepa-fc-ordered",-1), //ordered list of experiment type in process type
						getPET("lib-normalization",-1),  
						getPET("normalization-and-pooling",-1),
						getPET("prepa-fc-ordered",0),
						getPET("illumina-depot",1) ),        
				getExperimentTypes("prepa-fc-ordered").get(0),        //first experiment type
				getExperimentTypes("illumina-depot").get(0),          //last  experiment type
				getExperimentTypes("ext-to-prepa-fc-ordered").get(0), //void  experiment type
				CNG));
		
		// FDS 02/06/2017 NGL-1447: duplication 4000/X5 (prep FC ordonnée) avec tranfert en experience de niveau 0
		// FDS 14/12/2017 NGL-1730: renomage label: ajout NovaSeq
		l.add(DescriptionFactory.newProcessType("Transfert puis prep FC ordonnée, dépôt (4000 / X5 / NovaSeq)", "tf-prepfcordered-depot", ProcessCategory.find.findByCode("sequencing"),
				44,
				getPropertyDefinitionsIlluminaDepotCNG("prepa-fc-ordered"), 
				Arrays.asList(
						getPET("ext-to-prepa-fc-ordered",-1), //ordered list of experiment type in process type
						getPET("lib-normalization",-1),  
						getPET("normalization-and-pooling",-1),
						getPET("tubes-to-plate",0),
						getPET("prepa-fc-ordered",0),
						getPET("illumina-depot",1) ),  
				getExperimentTypes("tubes-to-plate").get(0),          //first experiment type
				getExperimentTypes("illumina-depot").get(0),          //last  experiment type
				getExperimentTypes("ext-to-prepa-fc-ordered").get(0), //void  experiment type
				CNG));	


		/* 04/04/2018 regrouper les processus par categorie 
		 *-----------------"satellites"--------------------
		 */
			
		// FDS ajout 28/11/2016 JIRA NGL-1164: nouveau processus pour "QC / TF / Purif" (sans transformation)
		// FDS 10/10/2017 NGL-1625 renommer et utiliser getPETForTransfertQCPurif
		l.add(DescriptionFactory.newProcessType("Transfert puis satellites", "transfert-qc-purif", ProcessCategory.find.findByCode("satellites"), 
				60,
				null, // pas de propriétés ??  
				getPETForTransfertQCPurif(),
				getExperimentTypes("tubes-to-plate").get(0),             //first experiment type ( 1 transfert n'importe lequel...?)
				getExperimentTypes("ext-to-transfert-qc-purif").get(0),  //last  experiment type ( doit etre la ext-to...)
				getExperimentTypes("ext-to-transfert-qc-purif").get(0),  //void  experiment type
				CNG));
		
		// FDS 10/10/2017 NGL-1625: nouveau processus satellite
		l.add(DescriptionFactory.newProcessType("QC puis satellites", "qc-transfert-purif", ProcessCategory.find.findByCode("satellites"), 
				70,
				null, // pas de propriétés ??  
				getPETForQCTransfertPurif(),
				getExperimentTypes("labchip-migration-profile").get(0),  //first experiment type ( 1 qc n'importe lequel...?)
				getExperimentTypes("ext-to-qc-transfert-purif").get(0),  //last  experiment type ( doit etre la ext-to...)
				getExperimentTypes("ext-to-qc-transfert-purif").get(0),  //void  experiment type
				CNG));
		

		
		// FDS ajout 03/03/2017 NGL-1225: processus Nanopore DEV
		l.addAll(new Nanopore().getProcessType());
		
		
		DAOHelpers.saveModels(ProcessType.class, l, errors);
	}
	
	
	// FDS 09/11/2015  -- JIRA 838 : ajout parametre String pour construire 2 listes differentes
	private static List<PropertyDefinition> getPropertyDefinitionsIlluminaDepotCNG(String expType) throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		
		// FDS 04/11/2015 -- JIRA 838 ajout  des HISEQ4000 et HISEQX; utilisation de listes intermediaires...
		List<Value> listSequencers =new ArrayList<>();
		
		if ( expType.equals("prepa-flowcell")) {
			// HISEQ2000
			listSequencers.addAll(DescriptionFactory.newValues("HISEQ1", "HISEQ2" , "HISEQ3" , "HISEQ4" ,"HISEQ5" ,"HISEQ6" ,"HISEQ7" ,"HISEQ8"));
			// HISEQ2500
			listSequencers.addAll(DescriptionFactory.newValues("HISEQ9", "HISEQ10", "HISEQ11"));
			// MISEQ
			listSequencers.addAll(DescriptionFactory.newValues("MISEQ1", "MISEQ2"));
			// NEXTSEQ500
			listSequencers.addAll(DescriptionFactory.newValues("NEXTSEQ1"));	
		}
		else if  ( expType.equals("prepa-fc-ordered")) {
			// HISEQX
			listSequencers.addAll(DescriptionFactory.newValues("ASTERIX","DIAGNOSTIX","IDEFIX","OBELIX","PANORAMIX"));		
			// HISEQ4000
			listSequencers.addAll(DescriptionFactory.newValues("FALBALA"));
			// 07/12/2017 NGL-1730 ajout NOVASEQ6000: MARIECURIX
			listSequencers.addAll(DescriptionFactory.newValues("MARIECURIX"));
		}	

		propertyDefinitions.add(
				DescriptionFactory.newPropertiesDefinition("Nom du séquenceur","sequencerName",
						LevelService.getLevels(Level.CODE.Process),String.class, true, listSequencers, "single",150));
		
		propertyDefinitions.add(
				DescriptionFactory.newPropertiesDefinition("Position","position"
						, LevelService.getLevels(Level.CODE.Process),String.class, false, DescriptionFactory.newValues("A", "B"), "single",200));
		
		/*  JIRA 781 : les proprietes ci dessous ne sont pas retenues...
		propertyDefinitions.add(
				DescriptionFactory.newPropertiesDefinition("Date prévue (cBot)","cBotExpectedDate"
						, LevelService.getLevels(Level.CODE.Process),Date.class, true, "single",100));
		propertyDefinitions.add(
				DescriptionFactory.newPropertiesDefinition("Nb lanes","numberOfLanes"
						, LevelService.getLevels(Level.CODE.Process),Double.class, true, "single",250));		
		propertyDefinitions.add(
				DescriptionFactory.newPropertiesDefinition("Concentration dilution finale","finalConcentrationLib"
						, LevelService.getLevels(Level.CODE.Process),Double.class, true, null, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_CONCENTRATION), MeasureUnit.find.findByCode("pM"), MeasureUnit.find.findByCode("nM"),
						"single",300));
		propertyDefinitions.add(
				DescriptionFactory.newPropertiesDefinition("% PhiX","phixPercentage"
						, LevelService.getLevels(Level.CODE.Process),Integer.class, true, "single",350));
		//FDS 11-03-2015 =>NGL-356: supression GAIIx, ajout Nextseq, fusion  "Hiseq 2000", "Hiseq 2500 normal"-> "Hiseq 2000/2500N"
		propertyDefinitions.add(
				DescriptionFactory.newPropertiesDefinition("Type séquencage","sequencingType"
						, LevelService.getLevels(Level.CODE.Process),String.class, true, DescriptionFactory.newValues("Hiseq 2000 / 2500 high throughput" , "Hiseq 2500 Fast" , "Miseq" , "Nextseq"), "single",400));
		propertyDefinitions.add(
				DescriptionFactory.newPropertiesDefinition("Type de lectures", "readType"
						, LevelService.getLevels(Level.CODE.Process),String.class, true, DescriptionFactory.newValues("SR","PE"), "single",450));		
		propertyDefinitions.add(
				DescriptionFactory.newPropertiesDefinition("Longueur de lecture", "readLength"
						, LevelService.getLevels(Level.CODE.Process),String.class, true, DescriptionFactory.newValues("50","100","150","250","300","500","600"), "single",500));		
		*/
		
		return propertyDefinitions;
	}

	//FDS ajout 28/01/2016 -- JIRA NGL-894: nouveau processus pour X5
	//FDS 10/08/2016 renommer  en getX5WgPcrFreeLibProcessTypeCodeValues 
	private static List<PropertyDefinition> getPropertyDefinitionsX5WgPcrFree() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
	
		// FDS 21/03/2016 ajout d'une propriete avec liste de choix, de niveau content pour quelle soit propagee
		propertyDefinitions.add(
				DescriptionFactory.newPropertiesDefinition("Type processus librairie","libProcessTypeCode",
						LevelService.getLevels(Level.CODE.Process,Level.CODE.Content), String.class, true, "F",
						getX5WgPcrFreeLibProcessTypeCodeValues(), "single" ,100, null, null, null));

		// FDS 27/10/2016 NGL-1025: ajout expectedCoverage: optionnel, editable, pas de defaut, de niveau content pour quelle soit propagee
		propertyDefinitions.add(
				DescriptionFactory.newPropertiesDefinition("Couverture souhaitée","expectedCoverage",
						LevelService.getLevels(Level.CODE.Process,Level.CODE.Content), String.class, false, "F",
						null, "single" ,101, true, null, null));
		
		// FDS 23/11/2016 SUPSQCNG-424 : ajout sequencingType optionnelle avec liste de choix,  niveau process uniquement
		// FDS 19/12/2017 ajout "NovaSeq 6000"
		propertyDefinitions.add(
				DescriptionFactory.newPropertiesDefinition("Type de séquencage","sequencingType",
						LevelService.getLevels(Level.CODE.Process), String.class, false, "F",
						DescriptionFactory.newValues("Hiseq 4000","Hiseq X","NovaSeq 6000"), "single" ,102, null, null, null));
		
		// FDS 18/01/2017 JIRA NGL-1259 ajout plateWorkLabel: optionnel, niveau process uniquement, editable, pas de default
		propertyDefinitions.add(
				DescriptionFactory.newPropertiesDefinition("Nom de travail plaque","plateWorkLabel",
						LevelService.getLevels(Level.CODE.Process), String.class, false, "F",
						null, "single" ,103, true, null, null));
		
		// FDS 18/01/2017 JIRA NGL-1259 ajout ngsRunWorkLabel: optionnel, niveau process uniquement, editable, pas de defaut
		propertyDefinitions.add(
				DescriptionFactory.newPropertiesDefinition("Nom de travail run NGS","ngsRunWorkLabel",
						LevelService.getLevels(Level.CODE.Process), String.class, false, "F",
						null, "single" ,104, true, null, null));
		
		// FDS 29/03/2018  JIRA NGL-1985 ajout N-plex ?: optionnel, niveau process uniquement, editable, pas de defaut
		propertyDefinitions.add(
				DescriptionFactory.newPropertiesDefinition("N-plex ?","nPlex",
						LevelService.getLevels(Level.CODE.Process), String.class, false, "F",
						null, "single" ,105, true, null, null));
		
		return propertyDefinitions;
	}

	//FDS 10/08/2016 renommer en getX5WgPcrFreeLibProcessTypeCodeValues
	private static List<Value> getX5WgPcrFreeLibProcessTypeCodeValues(){
        List<Value> values = new ArrayList<>();
        
        values.add(DescriptionFactory.newValue("DA","DA - DNAseq"));
         
        return values;
	}
	
	//FDS ajout 31/05/2016 pour JIRA NGL-1025: processus RNASeq; 18/01/2017 remommer en getPropertyDefinitionsRNAseq=> getPropertyDefinitionsRNAlib
	private static List<PropertyDefinition> getPropertyDefinitionsRNAlib() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
	
		propertyDefinitions.add(
				DescriptionFactory.newPropertiesDefinition("Type processus librairie","libProcessTypeCode",
						LevelService.getLevels(Level.CODE.Process,Level.CODE.Content) , String.class, true, "F",
						getRNALibProcessTypeCodeValues(), "single" ,100, null, null, null));

		// FDS 27/10/2016 NGL-1025: ajout expectedCoverage: optionnel, editable, pas de defaut
		propertyDefinitions.add(
				DescriptionFactory.newPropertiesDefinition("Couverture souhaitée","expectedCoverage",
						LevelService.getLevels(Level.CODE.Process,Level.CODE.Content), String.class, false, "F",
						null, "single" ,101, true, null, null));
		
		// FDS 18/01/2017 JIRA NGL-1259 ajout plateWorkLabel: optionnel, niveau process uniquement, editable, pas de defaut
		propertyDefinitions.add(
				DescriptionFactory.newPropertiesDefinition("Nom de travail plaque","plateWorkLabel",
						LevelService.getLevels(Level.CODE.Process), String.class, false, "F",
						null, "single" ,102, true, null, null));
		
		// FDS 18/01/2017 JIRA NGL-1259 ajout ngsRunWorkLabel: optionnel, niveau process uniquement, editable, pas de defaut
		propertyDefinitions.add(
				DescriptionFactory.newPropertiesDefinition("Nom de travail run NGS","ngsRunWorkLabel",
						LevelService.getLevels(Level.CODE.Process), String.class, false, "F",
						null, "single" ,103, true, null, null));
		
		// FDS 29/03/2018  JIRA NGL-1985 ajout N-plex ?: optionnel, niveau process uniquement, editable, pas de defaut
		propertyDefinitions.add(
				DescriptionFactory.newPropertiesDefinition("N-plex ?","nPlex",
						LevelService.getLevels(Level.CODE.Process), String.class, false, "F",
						null, "single" ,104, true, null, null));
		
		return propertyDefinitions;
	}
	
	private static List<Value> getRNALibProcessTypeCodeValues(){
        List<Value> values = new ArrayList<>();

        // !! garder les codes et labels en coherence avec  ImportServiceCNG et RunServiceCNG
        values.add(DescriptionFactory.newValue("RD","RD - ssmRNASeq"));       //single stranded messenger RNA sequencing
        values.add(DescriptionFactory.newValue("RE","RE - sstRNASeq"));       //single stranded total RNA sequencing
        values.add(DescriptionFactory.newValue("RF","RF - sstRNASeqGlobin")); //single stranded total RNA from blood sequencing
        values.add(DescriptionFactory.newValue("RG","RG - mRNASeq"));         //messenger RNA sequencing
        values.add(DescriptionFactory.newValue("RH","RH - sstRNASeqGold"));   //single stranded total RNA Gold
        
        return values;
	}
	
	//FDS ajout 10/08/2016 pour JIRA NGL-1047: processus X5_WG NANO
	private static List<PropertyDefinition> getPropertyDefinitionsX5WgNanoDNAseq() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
	
		propertyDefinitions.add(
				DescriptionFactory.newPropertiesDefinition("Type processus librairie","libProcessTypeCode",
						LevelService.getLevels(Level.CODE.Process,Level.CODE.Content), String.class, true, "F",
						getX5WgNanoLibProcessTypeCodeValues(), "single" ,100, null, null, null));
		
		// FDS 27/10/2016 NGL-1025: ajout expectedCoverage: optionnel, editable, pas de defaut
		propertyDefinitions.add(
				DescriptionFactory.newPropertiesDefinition("Couverture souhaitée","expectedCoverage",
						LevelService.getLevels(Level.CODE.Process,Level.CODE.Content), String.class, false, "F",
						null, "single" ,101, true, null, null));
		
		// FDS 18/01/2017 JIRA NGL-1259 ajout plateWorkLabel: optionnel, niveau process uniquement,editable, pas de defaut
		propertyDefinitions.add(
				DescriptionFactory.newPropertiesDefinition("Nom de travail plaque","plateWorkLabel",
						LevelService.getLevels(Level.CODE.Process), String.class, false, "F",
						null, "single" ,102, true, null, null));
		
		// FDS 18/01/2017 JIRA NGL-1259 ajout ngsRunWorkLabel: optionnel, niveau process uniquement, editable, pas de defaut
		propertyDefinitions.add(
				DescriptionFactory.newPropertiesDefinition("Nom de travail run NGS","ngsRunWorkLabel",
						LevelService.getLevels(Level.CODE.Process), String.class, false, "F",
						null, "single" ,103, true, null, null));
		
		// FDS 29/03/2018  JIRA NGL-1985 ajout N-plex ?: optionnel, niveau process uniquement, editable, pas de defaut
		propertyDefinitions.add(
				DescriptionFactory.newPropertiesDefinition("N-plex ?","nPlex",
						LevelService.getLevels(Level.CODE.Process), String.class, false, "F",
						null, "single" ,104, true, null, null));
		
		return propertyDefinitions;
	}
	

	
	private static List<Value> getX5WgNanoLibProcessTypeCodeValues(){
        List<Value> values = new ArrayList<>();
        
        // !! garder les codes et labels en coherence avec  ImportServiceCNG et RunServiceCNG
        values.add(DescriptionFactory.newValue("DD","DD - PCR-NANO DNASeq"));   
         
        return values;
	}
	
	// FDS ajout 20/02/2017 NGL-1167
	private static List<PropertyDefinition> getPropertyDefinitionsWgChromium() {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		
		propertyDefinitions.add(
					DescriptionFactory.newPropertiesDefinition("Type processus librairie","libProcessTypeCode",
							LevelService.getLevels(Level.CODE.Process, Level.CODE.Content), String.class, true, "F",
							getWgChromiumLibProcessTypeCodeValues(), "single" ,100, null, null, null));

		 return propertyDefinitions;
	}
	
	// FDS ajout 20/02/2017 NGL-1167
	private static List<Value> getWgChromiumLibProcessTypeCodeValues(){
        List<Value> values = new ArrayList<>();
        
        // !! aussi dans RunServiceCNG
        values.add(DescriptionFactory.newValue("DE","DE - Chromium WG"));   
         
        return values;
	}
	
	// FDS 10/10/2017 duplication NGL-1625
	// toutes les transformations en -1
	// ext-to-transfert-qc-purif" en -1
	// 1 transfert n'importe lequel ???? : pool en 0
	private List<ProcessExperimentType> getPETForTransfertQCPurif(){
		List<ProcessExperimentType> pets = ExperimentType.find.findByCategoryCode("transformation")
			.stream()
			.map(et -> getPET(et.code, -1))
			.collect(Collectors.toList());

		pets.add(getPET("ext-to-transfert-qc-purif",-1));
		pets.add(getPET("tubes-to-plate",0));

		return pets;		
	}

	// FDS ajout 10/07/2017 pour JIRA NGL-1201: processus capture
	private static List<PropertyDefinition> getPropertyDefinitionsCapture() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
	
		propertyDefinitions.add(
				DescriptionFactory.newPropertiesDefinition("Type processus librairie","libProcessTypeCode",
						LevelService.getLevels(Level.CODE.Process, Level.CODE.Content), String.class, true, "F",
						getCaptureLibProcessTypeCodeValues(), "single" ,101, null, null, null));
		
		propertyDefinitions.add(
				DescriptionFactory.newPropertiesDefinition("Baits (sondes) prévues","expectedBaits",
						LevelService.getLevels(Level.CODE.Process), String.class, true, "F",
						getCaptureBaitsValues(), "single" ,102, null, null, null));
		
		propertyDefinitions.add(
				DescriptionFactory.newPropertiesDefinition("Protocole / Kit","captureProtocol",
						LevelService.getLevels(Level.CODE.Process, Level.CODE.Content), String.class, true, "F",
						getCaptureProtocolValues(), "single" ,103, null, null, null));
		
		// plateWorkLabel: optionnel,niveau process uniquement
		propertyDefinitions.add(
				DescriptionFactory.newPropertiesDefinition("Nom de travail plaque","plateWorkLabel",
						LevelService.getLevels(Level.CODE.Process), String.class, false, "F",
						null, "single" ,104, true, null, null));
		
		// robotRunWorkLabel: optionnel,niveau process uniquement; 21/11/2017 (chgt nom)
		propertyDefinitions.add(
				DescriptionFactory.newPropertiesDefinition("Nom de travail run (robot)","robotRunWorkLabel",
						LevelService.getLevels(Level.CODE.Process), String.class, false, "F",
						null, "single" ,105, true, null, null));
		
		//N-plex ?: optionnel,niveau process uniquement
		propertyDefinitions.add(
				DescriptionFactory.newPropertiesDefinition("N-plex ?","nPlex",
						LevelService.getLevels(Level.CODE.Process), String.class, false, "F",
						null, "single" ,106, true, null, null));
		
		return propertyDefinitions;
	}
	
	// FDS ajout 10/07/2017 pour JIRA NGL-1201: processus capture
	private static List<PropertyDefinition> getPropertyDefinitionsCapturePcrIndexing() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
	
		propertyDefinitions.add(
				DescriptionFactory.newPropertiesDefinition("Type processus librairie","libProcessTypeCode",
						LevelService.getLevels(Level.CODE.Process, Level.CODE.Content), String.class, true, "F",
						getCaptureLibProcessTypeCodeValues(), "single" ,101, null, null, null));
		
		propertyDefinitions.add(
				DescriptionFactory.newPropertiesDefinition("Baits (sondes) prévues","expectedBaits",
						LevelService.getLevels(Level.CODE.Process), String.class, true, "F",
						getCaptureBaitsValues(), "single" ,102, null, null, null));
		
		propertyDefinitions.add(
				DescriptionFactory.newPropertiesDefinition("Protocole / Kit","captureProtocol",
						LevelService.getLevels(Level.CODE.Process, Level.CODE.Content), String.class, true, "F",
						getCaptureProtocolValues(), "single" ,103, null, null, null));
		
		// NGL-1906: ajout robotRunWorkLabel
		propertyDefinitions.add(
				DescriptionFactory.newPropertiesDefinition("Nom de travail run (robot)","robotRunWorkLabel",
						LevelService.getLevels(Level.CODE.Process), String.class, false, "F",
						null, "single" ,105, true, null, null));

		
		return propertyDefinitions;
	}
	
	// FDS ajout 19/03/2018 JIRA NGL-1906: ajout propriété "robotRunWorkLabel
	private static List<PropertyDefinition> getPropertyDefinitionsPrcCapturePcrIndexing() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		
		propertyDefinitions.add(
				DescriptionFactory.newPropertiesDefinition("Nom de travail run (robot)","robotRunWorkLabel",
						LevelService.getLevels(Level.CODE.Process), String.class, false, "F",
						null, "single" ,105, true, null, null));

		
		return propertyDefinitions;
	}
	
    // FDS ajout 10/07/2017 pour JIRA NGL-1201: processus capture
	// utilisé par process getPropertyDefinitionsCapture ET getPropertyDefinitionsCapturePcrIndexing
	private static List<Value> getCaptureLibProcessTypeCodeValues(){
        List<Value> values = new ArrayList<>();
        
        // Liste evolutive !!!! 05/09/2017 mise en coherence avec RunServiceCNG => c-a-d meme code, meme label 
        values.add(DescriptionFactory.newValue("CP","CP - Agilent : V5 (DefCap013_Ex)"));
        values.add(DescriptionFactory.newValue("CS","CS - Agilent : V5+UTR (DefCap016_Ex)"));
        values.add(DescriptionFactory.newValue("CZ","CZ - Agilent : V6 (DefCap022)"));
        values.add(DescriptionFactory.newValue("CAA","CAA - Agilent : V6+UTR (DefCap023)"));
        values.add(DescriptionFactory.newValue("CAC","CAC - Agilent : V6+Cosmic (DefCap025)"));
        values.add(DescriptionFactory.newValue("CAD","CAD - Roche-Nimblegen : MedExome (DefCap026)"));
        //values.add(DescriptionFactory.newValue("CAE","CAE - Nimblegen : MedExome+Mitome (DefCap027)"));   // plus utilisé, alléger les drop down...
        //values.add(DescriptionFactory.newValue("CAF","CAF - Chromium Whole Exome (DefCap028)"));          // plus utilisé, alléger les drop down...
        values.add(DescriptionFactory.newValue("CAG","CAG - SureSelectXTcustom(PRME) (DefCap029)")); // NGL-2040 ajout
        
        return values;
	}
	
	// FDS ajout 10/07/2017 pour JIRA NGL-1201: processus capture
	// utilisé par processus getPropertyDefinitionsCapture ET getPropertyDefinitionsCapturePcrIndexing
	// !! code dupliqué dans ExperimentServiceCNG
	private static List<Value>getCaptureBaitsValues() {
		 List<Value> values = new ArrayList<>();
		 
		 values.add(DescriptionFactory.newValue("V5",    "V5"));
		 values.add(DescriptionFactory.newValue("V5+UTR","V5+UTR"));
		 values.add(DescriptionFactory.newValue("V6",    "V6"));
		 values.add(DescriptionFactory.newValue("V6+UTR","V6+UTR")); 
		 values.add(DescriptionFactory.newValue("V6+Cosmic","V6+Cosmic")); 
		 values.add(DescriptionFactory.newValue("custom","custom"));
	
    	return values;
	}
	
	// FDS ajout 11/07/2017 pour JIRA NGL-1201: processus capture
	// utilisé par processus getPropertyDefinitionsCapture ET getPropertyDefinitionsCapturePcrIndexing
	private static List<Value>getCaptureProtocolValues() {
		 List<Value> values = new ArrayList<>();
		 
		 // LISTE exacte à nous donner ! sureSelect XT 3µg ; XT 200ng ; XT2 1µg, XT2 100ng
		 values.add(DescriptionFactory.newValue("sureselect-xt-3µg",   "SureSelect XT 3µg"));
		 values.add(DescriptionFactory.newValue("sureselect-xt-200ng", "SureSelect XT 200ng"));
		 values.add(DescriptionFactory.newValue("sureselect-xt2-1µg",  "SureSelect XT2 1µg"));
		 values.add(DescriptionFactory.newValue("sureselect-xt2-100ng","SureSelect XT2 100ng"));
		 
	    return values;
	}
	
	// FDS ajout 04/04/2018 pour NGL-1727 : processus SmallRNASeq
	private static List<PropertyDefinition> getPropertyDefinitionsSmallRNASeq() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
	
		propertyDefinitions.add(
				DescriptionFactory.newPropertiesDefinition("Type processus librairie","libProcessTypeCode",
						LevelService.getLevels(Level.CODE.Process, Level.CODE.Content), String.class, true, "F",
						getSmallRNASeqTypeCodeValues(), "single" ,101, null, null, null));
		
		return propertyDefinitions;
	}
	
    // FDS ajout 04/04/2018 pour NGL-1727 : processus SmallRNASeq
	private static List<Value> getSmallRNASeqTypeCodeValues(){
        List<Value> values = new ArrayList<>();
        
        // Liste evolutive !!!! voir ausii RunServiceCNG 
        values.add(DescriptionFactory.newValue("RB","RB - smallRNASeq"));
        
        return values;
	}
	
	// FDS ajout 04/04/2018 pour NGL-1727 : processus BisSeq
	private static List<PropertyDefinition> getPropertyDefinitionsBisSeq() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
	
		propertyDefinitions.add(
				DescriptionFactory.newPropertiesDefinition("Type processus librairie","libProcessTypeCode",
						LevelService.getLevels(Level.CODE.Process, Level.CODE.Content), String.class, true, "F",
						getBisSeqTypeCodeValues(), "single" ,101, null, null, null));
		
		return propertyDefinitions;
	}
	
    // FDS ajout 04/04/2018 pour NGL-1727 : processus BisSeq
	private static List<Value> getBisSeqTypeCodeValues(){
        List<Value> values = new ArrayList<>();
        
        // Liste evolutive !!!! voir ausii RunServiceCNG 
        values.add(DescriptionFactory.newValue("FD","FD - BisSeq"));
        
        return values;
	}
	
	
	
	// FDS ajout 28/11/2016 NGL-1164
	// toutes les transformation en -1
	// ext-to-qc-transfert-purif" en -1
	// 1 qc n'importe lequel ?????: labchip-migration-profile en 0 
	private List<ProcessExperimentType> getPETForQCTransfertPurif(){
		List<ProcessExperimentType> pets = ExperimentType.find.findByCategoryCode("transformation")
			.stream()
			.map(et -> getPET(et.code, -1))
			.collect(Collectors.toList());

		pets.add(getPET("ext-to-qc-transfert-purif",-1));
		pets.add(getPET("labchip-migration-profile",0));

		return pets;		
	}
	
}