package services.description.declaration.cns;

import static services.description.DescriptionFactory.newExperimentType;
import static services.description.DescriptionFactory.newExperimentTypeNode;
import static services.description.DescriptionFactory.newPropertiesDefinition;

import java.awt.Image;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.mongojack.DBQuery;

import fr.cea.ig.MongoDBDAO;
import models.laboratory.common.description.Level;
import models.laboratory.common.description.MeasureCategory;
import models.laboratory.common.description.MeasureUnit;
import models.laboratory.common.description.PropertyDefinition;
import models.laboratory.common.description.Value;
import models.laboratory.experiment.description.ExperimentCategory;
import models.laboratory.experiment.description.ExperimentType;
import models.laboratory.parameter.index.NanoporeIndex;
import models.laboratory.processes.description.ExperimentTypeNode;
import models.laboratory.processes.description.ProcessCategory;
import models.laboratory.processes.description.ProcessType;
import models.utils.InstanceConstants;
import models.utils.dao.DAOException;
import services.description.Constants;
import services.description.DescriptionFactory;
import services.description.common.LevelService;
import services.description.common.MeasureService;
import services.description.declaration.AbstractDeclaration;

public class Nanopore extends AbstractDeclaration{

	@Override
	protected List<ProcessType> getProcessTypeCommon() {
		List<ProcessType> l = new ArrayList<>();
				
		l.add(DescriptionFactory.newProcessType("Run Nanopore", "nanopore-run", 
				ProcessCategory.find.findByCode("sequencing"),62 , 
				getPropertyDefinitionsRunNanopore(),
				Arrays.asList(getPET("ext-to-nanopore-run",-1), 
						getPET("nanopore-library",-1), 
						getPET("nanopore-depot",0)), 
				getExperimentTypes("nanopore-depot").get(0),getExperimentTypes("nanopore-depot").get(0), 
				getExperimentTypes("ext-to-nanopore-run").get(0), 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		
		//Ancien process de fragmentation
		l.add(DescriptionFactory.newProcessType("Frg-rep, Lib ONT, Dépôt", "nanopore-process-library", 
				ProcessCategory.find.findByCode("nanopore-library"),50, 
				getPropertyDefinitionsNanoporeFragmentation(), 
				Arrays.asList(getPET("ext-to-nanopore-process-library",-1),
						getPET("dna-rna-extraction",-1),
						getPET("nanopore-fragmentation",0),
						getPET("nanopore-library",1),
						getPET("nanopore-depot",2)), 
				getExperimentTypes("nanopore-fragmentation").get(0),
				getExperimentTypes("nanopore-depot").get(0), 
				getExperimentTypes("ext-to-nanopore-process-library").get(0), 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS),false));
			
		//Nouveau process de fragmentation
		l.add(DescriptionFactory.newProcessType("Frg, Rep ADN, Lib, Dépôt", "nanopore-frg-rep-lib-depot", 
		ProcessCategory.find.findByCode("nanopore-library"),51, 
				getPropertyDefinitionsNanoporeFragmentation(), 
				Arrays.asList(getPET("ext-to-nanopore-frg-rep-lib-depot",-1),
						getPET("dna-rna-extraction",-1),
						getPET("nanopore-frg",0),
						getPET("nanopore-dna-reparation",1),
						getPET("nanopore-library",2),
						getPET("nanopore-depot",3)), 
				getExperimentTypes("nanopore-frg").get(0),
				getExperimentTypes("nanopore-depot").get(0),
				getExperimentTypes("ext-to-nanopore-frg-rep-lib-depot").get(0),
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
	
		l.add(DescriptionFactory.newProcessType("Frg (sans rep), Lib, Dépôt", "nanopore-frg-lib-depot", 
				ProcessCategory.find.findByCode("nanopore-library"),52, 
				getPropertyDefinitionsNanoporeFragmentation(), 
				Arrays.asList(getPET("dna-rna-extraction",-1),
						getPET("ext-to-nanopore-frg-lib-depot",-1),
						getPET("nanopore-frg",0),
						getPET("nanopore-library",1),
						getPET("nanopore-depot",2)),
				getExperimentTypes("nanopore-frg").get(0),
				getExperimentTypes("nanopore-depot").get(0),
				getExperimentTypes("ext-to-nanopore-frg-lib-depot").get(0),
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
	
		
		l.add(DescriptionFactory.newProcessType("Rep ADN, Lib, Dépôt", "nanopore-rep-lib-depot", 
				ProcessCategory.find.findByCode("nanopore-library"),53, 
				getPropertyDefinitionsNanoporeLibrary(), 
				Arrays.asList(getPET("ext-to-nanopore-rep-lib-depot",-1),
						getPET("nanopore-frg",-1),
						getPET("nanopore-fragmentation",-1),
						getPET("dna-rna-extraction",-1),
						getPET("nanopore-dna-reparation",0),
						getPET("nanopore-library",1),
						getPET("nanopore-depot",2)),
				getExperimentTypes("nanopore-dna-reparation").get(0),
				getExperimentTypes("nanopore-depot").get(0), 
				getExperimentTypes("ext-to-nanopore-rep-lib-depot").get(0),
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		

		l.add(DescriptionFactory.newProcessType("Lib ONT, Dépôt", "nanopore-process-library-no-frg", 
				ProcessCategory.find.findByCode("nanopore-library"),54, 
				getPropertyDefinitionsNanoporeLibrary(),
				Arrays.asList(getPET("ext-to-nanopore-process-library-no-frg",-1),
						getPET("nanopore-fragmentation",-1), 
						getPET("nanopore-frg",-1), 
						getPET("nanopore-dna-reparation",-1), 
						getPET("dna-rna-extraction",-1), 						
						getPET("nanopore-library",0),
						getPET("nanopore-depot",1)), 
				getExperimentTypes("nanopore-library").get(0),
				getExperimentTypes("nanopore-depot").get(0), 
				getExperimentTypes("ext-to-nanopore-process-library-no-frg").get(0),
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
	

		 l.add(DescriptionFactory.newProcessType("Nanopore DEV", "nanopore-process-dev",
	                ProcessCategory.find.findByCode("nanopore-library"),60,
	                getPropertyDefinitionsNanoporeFragmentation(), 
	                Arrays.asList(getPET("ext-to-nanopore-process-dev",-1),
	                       
	                        getPET("nanopore-library",0),
	                         getPET("nanopore-frg",0),
	                        getPET("nanopore-dna-reparation",0),
	                       
	                                           
	                        getPET("nanopore-library",1),
	                         getPET("nanopore-dna-reparation",1),
	                        getPET("nanopore-depot",1),
	               
	                        getPET("nanopore-library",2),
	                        getPET("nanopore-depot",2),
	                       
	                         getPET("nanopore-depot",3)),
	               
	                getExperimentTypes("nanopore-library").get(0),
	                getExperimentTypes("nanopore-depot").get(0),
	                getExperimentTypes("ext-to-nanopore-process-dev").get(0),
	                DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		 
		 l.add(DescriptionFactory.newProcessType("Nanopore DEV v2", "nanopore-process-dev-2",
                 ProcessCategory.find.findByCode("nanopore-library"),61,
	                getPropertyDefinitionsNanoporeFragmentation(),
	                Arrays.asList(getPET("ext-to-nanopore-process-dev-2",-1),
	                       
                 		// Ancien process DEV
	                         getPET("nanopore-frg",0),
	                        getPET("nanopore-dna-reparation",0),
		                       
                           //Ajout des nouvelles spec
                         getPET("nanopore-end-prep",0),
	                                           
                       //Ancien process DEV                
	                         getPET("nanopore-dna-reparation",1),
	                        getPET("nanopore-depot",1),
		                       
                        //Ajout des nouvelles spec
                         getPET("nanopore-end-prep",1),
                         getPET("nanopore-final-ligation",1),
                         getPET("nanopore-pcr",1),
                         getPET("nanopore-barcode-ligation",1),
                         getPET("nanopore-pre-pcr-ligation",1),
	               
                         //Ancien process DEV   
	                        getPET("nanopore-depot",2),
		                       
                         //Ajout des nouvelles spec
                         getPET("nanopore-end-prep",2),
                         getPET("nanopore-final-ligation",2),
                         getPET("nanopore-barcode-ligation",2),
                         getPET("nanopore-pre-pcr-ligation",2),                           
                         getPET("nanopore-pcr",2),
	                       
                         //Ancien process DEV   
                         getPET("nanopore-depot",3),
	               
                 		//Ajout des nouvelles spec
                 		getPET("nanopore-final-ligation",3),
                         getPET("nanopore-barcode-ligation",3),
                         getPET("nanopore-pcr",3),
                         getPET("nanopore-end-prep",3),
		
                         getPET("nanopore-depot",4),
                         getPET("nanopore-final-ligation",4),
                         getPET("nanopore-end-prep",4),
                         getPET("nanopore-barcode-ligation",4),
		
                         getPET("nanopore-depot",5),
                         getPET("nanopore-final-ligation",5),
                         getPET("nanopore-barcode-ligation",5),
	
                         getPET("nanopore-depot",6),
                         getPET("nanopore-final-ligation",6),
                         	
                         getPET("nanopore-depot",7)),
                             
				getExperimentTypes("nanopore-frg").get(0),
				getExperimentTypes("nanopore-depot").get(0),
                 getExperimentTypes("ext-to-nanopore-process-dev-2").get(0),
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
	

		 
		return l;
	}

	@Override
	protected List<ProcessType> getProcessTypePROD() {
//		List<ProcessType> l =new ArrayList<ProcessType>();
		
		/*
		l.add(DescriptionFactory.newProcessType("Lib ONT, Dépôt", "nanopore-process-library-no-frg", 
				ProcessCategory.find.findByCode("nanopore-library"),54, 
				getPropertyDefinitionsNanoporeLibrary(),
				Arrays.asList(getPET("ext-to-nanopore-process-library-no-frg",-1),
						getPET("nanopore-fragmentation",-1), 
						getPET("nanopore-library",0),
						getPET("nanopore-depot",1)), 
				getExperimentTypes("nanopore-library").get(0),
				getExperimentTypes("nanopore-depot").get(0), 
				getExperimentTypes("ext-to-nanopore-process-library-no-frg").get(0),
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		
		l.add(DescriptionFactory.newProcessType("Frg-rep, Lib ONT, Dépôt", "nanopore-process-library", 
				ProcessCategory.find.findByCode("nanopore-library"),50, 
				getPropertyDefinitionsNanoporeFragmentation(), 
				Arrays.asList(getPET("ext-to-nanopore-process-library",-1),
						getPET("dna-rna-extraction",-1),
						getPET("nanopore-fragmentation",0),
						getPET("nanopore-library",1),
						getPET("nanopore-depot",2)), 
				getExperimentTypes("nanopore-fragmentation").get(0),
				getExperimentTypes("nanopore-depot").get(0), 
				getExperimentTypes("ext-to-nanopore-process-library").get(0), 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
			
	*/
		return null;
	}
	@Override
	protected List<ProcessType> getProcessTypeUAT() {
		
		// TODO Auto-generated method stub
		return null;
	}	

	@Override
	protected List<ProcessType> getProcessTypeDEV() {
		
		List<ProcessType> l=new ArrayList<>();
		
		
		
		return l;
	}
	
	@Override
	protected List<ExperimentType> getExperimentTypeCommon() {
		List<ExperimentType> l = new ArrayList<>();

		l.add(newExperimentType("Ext to Frg, Lib ONT, Dépôt","ext-to-nanopore-process-library",null, -1,
				ExperimentCategory.find.findByCode(ExperimentCategory.CODE.voidprocess.name()), null,  null,"OneToOne", 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));

		l.add(newExperimentType("Ext to Lib ONT, Dépôt","ext-to-nanopore-process-library-no-frg",null, -1,
				ExperimentCategory.find.findByCode(ExperimentCategory.CODE.voidprocess.name()), null,  null,"OneToOne", 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));

		l.add(newExperimentType("Ext to Run Nanopore","ext-to-nanopore-run",null,-1,
				ExperimentCategory.find.findByCode(ExperimentCategory.CODE.voidprocess.name()), null,  null,"OneToOne", 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));

		l.add(newExperimentType("Ext to Frg (sans rep), Lib, Dépôt","ext-to-nanopore-frg-lib-depot",null, -1,
				ExperimentCategory.find.findByCode(ExperimentCategory.CODE.voidprocess.name()), null,  null,"OneToOne", 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		
		l.add(newExperimentType("Ext to Frg, Rep ADN, Lib, Dépôt","ext-to-nanopore-frg-rep-lib-depot",null, -1,
				ExperimentCategory.find.findByCode(ExperimentCategory.CODE.voidprocess.name()), null,  null,"OneToOne", 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		
		l.add(newExperimentType("Ext to Rep ADN, Lib, Dépôt","ext-to-nanopore-rep-lib-depot",null, -1,
				ExperimentCategory.find.findByCode(ExperimentCategory.CODE.voidprocess.name()), null,  null,"OneToOne", 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		
		 l.add(newExperimentType("Ext to Process nanopore DEV", "ext-to-nanopore-process-dev",null, -1,
	                ExperimentCategory.find.findByCode(ExperimentCategory.CODE.voidprocess.name()), null,  null,"OneToOne",
	                DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
	     
		
		l.add(newExperimentType("Fragm-Réparation Nanopore","nanopore-fragmentation","FRG",2100,
				ExperimentCategory.find.findByCode(ExperimentCategory.CODE.transformation.name()),
				getPropertyFragmentationNanoporeOld(), 
				getInstrumentUsedTypes("megaruptor2","eppendorf-mini-spin-plus","hand"),"OneToOne",false, 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		
		l.add(newExperimentType("Fragmentation Nanopore","nanopore-frg","FRG",2105,
				ExperimentCategory.find.findByCode(ExperimentCategory.CODE.transformation.name()),
				getPropertyFragmentationNanopore(), 
				getInstrumentUsedTypes("megaruptor2","eppendorf-mini-spin-plus","hand"),"OneToOne", 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
	
		l.add(newExperimentType("Réparation ADN","nanopore-dna-reparation","FFP",2110,
				ExperimentCategory.find.findByCode(ExperimentCategory.CODE.transformation.name()),
				getPropertyReparationNanopore(), 
				getInstrumentUsedTypes("hand"),"OneToOne", 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));

		l.add(newExperimentType("Librairie ONT","nanopore-library","LIB",2120,
				ExperimentCategory.find.findByCode(ExperimentCategory.CODE.transformation.name()),
				getPropertyLibrairieNanopore(),
				getInstrumentUsedTypes("hand","voltrax"),"OneToOne", 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));

		l.add(newExperimentType("Sizing nanopore","nanopore-sizing","SIZ",2400,
				ExperimentCategory.find.findByCode(ExperimentCategory.CODE.purification.name()), 
				getPropertySizingNanopore(),
				getInstrumentUsedTypes("blue-pippin"),"OneToMany", 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS) ));
	
		l.add(newExperimentType("Depot Nanopore","nanopore-depot",null,2200,
				ExperimentCategory.find.findByCode(ExperimentCategory.CODE.transformation.name()), 
				getPropertyDepotNanopore(),	
				getInstrumentUsedTypes("minION","mk1", "mk1b", "promethION"),"OneToOne", 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS) ));
		

		 l.add(newExperimentType("Ext to Process nanopore DEV v2", "ext-to-nanopore-process-dev-2",null, -1,
	                ExperimentCategory.find.findByCode(ExperimentCategory.CODE.voidprocess.name()), null,  null,"OneToOne",
	                DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		
		 l.add(newExperimentType("End Prep","nanopore-end-prep","LIB", 2130,
	                ExperimentCategory.find.findByCode(ExperimentCategory.CODE.transformation.name()),
	                getPropertyEndPrepNanopore(),
	                getInstrumentUsedTypes("hand"),"OneToOne",
					DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		
		 l.add(newExperimentType("Ligation pre-PCR","nanopore-pre-pcr-ligation","LIB", 2140,
	                ExperimentCategory.find.findByCode(ExperimentCategory.CODE.transformation.name()),
	                getPropertyLigPrePcrNanopore(),
	                getInstrumentUsedTypes("hand"),"OneToOne",
					DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
	
		 l.add(newExperimentType("PCR nanopore","nanopore-pcr","LIB", 2150,
	                ExperimentCategory.find.findByCode(ExperimentCategory.CODE.transformation.name()),
	                getPropertyPcrNanopore(),
	                getInstrumentUsedTypes("hand"),"OneToOne",
					DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		 
		 l.add(newExperimentType("Ligation barcode","nanopore-barcode-ligation","LIB", 2160,
	                ExperimentCategory.find.findByCode(ExperimentCategory.CODE.transformation.name()),
	                getPropertyLigBarcodeNanopore(),
	                getInstrumentUsedTypes("hand"),"OneToOne",
					DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		 
		 l.add(newExperimentType("Ligation finale","nanopore-final-ligation","LIB", 2170,
	                ExperimentCategory.find.findByCode(ExperimentCategory.CODE.transformation.name()),
	                getPropertyLigFinaleNanopore(),
	                getInstrumentUsedTypes("hand"),"OneToOne",
					DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		 		
    
		return l;

	}
	
	@Override
	protected List<ExperimentType> getExperimentTypePROD() {
		List<ExperimentType> l = new ArrayList<>();
		
		
		return l;
	}

	@Override
	protected List<ExperimentType> getExperimentTypeUAT() {
		// TODO Auto-generated method stub
		return null;
	}

	
	@Override
	protected List<ExperimentType> getExperimentTypeDEV() {
		 
		List<ExperimentType> l = new ArrayList<>();
	
	
		
	
		
		return l;
	}

	@Override
	protected void getExperimentTypeNodeCommon() {
		//Nanopore
		newExperimentTypeNode("ext-to-nanopore-run", 
				getExperimentTypes("ext-to-nanopore-run").get(0), false, false, false, 
				null, null, null, 
				null).save();
		newExperimentTypeNode("ext-to-nanopore-process-library", 
				getExperimentTypes("ext-to-nanopore-process-library").get(0), false, false, false, 
				null, null, null, 
				null).save();
		newExperimentTypeNode("ext-to-nanopore-process-library-no-frg", 
				getExperimentTypes("ext-to-nanopore-process-library-no-frg").get(0), false, false, false, 
				null, null, null, 
				null).save();	
		
		newExperimentTypeNode("ext-to-nanopore-frg-lib-depot", 
				getExperimentTypes("ext-to-nanopore-frg-lib-depot").get(0), false, false, false, 
				null, null, null, null).save();
		
		newExperimentTypeNode("ext-to-nanopore-frg-rep-lib-depot",
				getExperimentTypes("ext-to-nanopore-frg-rep-lib-depot").get(0), false, false, false, 
				null, null, null, null).save();
		
		newExperimentTypeNode("ext-to-nanopore-rep-lib-depot", 
				getExperimentTypes("ext-to-nanopore-rep-lib-depot").get(0), false, false, false, 
				null, getExperimentTypes("nanopore-sizing"), null, null).save();
		
		newExperimentTypeNode("ext-to-nanopore-process-dev", 
				getExperimentTypes("ext-to-nanopore-process-dev").get(0), false, false, false, 
				null, null, null, 
				null).save();
		
		newExperimentTypeNode("ext-to-nanopore-process-dev-2",
                getExperimentTypes("ext-to-nanopore-process-dev-2").get(0), false, false, false,
                null, null, null, 
                null).save();	 

		newExperimentTypeNode("nanopore-fragmentation",
				getExperimentTypes("nanopore-fragmentation").get(0),false, false,false,
				getExperimentTypeNodes("dna-rna-extraction", "ext-to-nanopore-process-library","ext-to-nanopore-process-dev","ext-to-nanopore-process-dev-2"),null,null,
				getExperimentTypes("aliquoting")).save();
		
		newExperimentTypeNode("nanopore-frg",
				getExperimentTypes("nanopore-frg").get(0),false, false,false,
				getExperimentTypeNodes("dna-rna-extraction","ext-to-nanopore-frg-lib-depot","ext-to-nanopore-frg-rep-lib-depot","ext-to-nanopore-process-library","ext-to-nanopore-process-dev","ext-to-nanopore-process-dev-2"),getExperimentTypes("nanopore-sizing"),null,
				getExperimentTypes("aliquoting","pool-tube")).save();	
	
		newExperimentTypeNode("nanopore-dna-reparation",
				getExperimentTypes("nanopore-dna-reparation").get(0),false, false,false,
				getExperimentTypeNodes("ext-to-nanopore-rep-lib-depot","nanopore-frg","nanopore-fragmentation","dna-rna-extraction","ext-to-nanopore-process-dev","ext-to-nanopore-process-dev-2"),null,null,
				getExperimentTypes("aliquoting","pool-tube")).save();
	
		newExperimentTypeNode("nanopore-library",
				getExperimentTypes("nanopore-library").get(0),false, false,false,
				getExperimentTypeNodes("ext-to-nanopore-process-library-no-frg","dna-rna-extraction","nanopore-fragmentation", "nanopore-frg","nanopore-dna-reparation","ext-to-nanopore-process-dev"),getExperimentTypes("nanopore-sizing"),getExperimentTypes("fluo-quantification"),
				getExperimentTypes("aliquoting","pool-tube")).save();	
	
		newExperimentTypeNode("nanopore-end-prep",
				getExperimentTypes("nanopore-end-prep").get(0),false, false,false,
				null,null,null,
				null).save();
	
		newExperimentTypeNode("nanopore-pre-pcr-ligation",
				getExperimentTypes("nanopore-pre-pcr-ligation").get(0),false, false,false,
				getExperimentTypeNodes("nanopore-end-prep"),null,null,
			null).save();	
				
		newExperimentTypeNode("nanopore-pcr",
				getExperimentTypes("nanopore-pcr").get(0),false, false,false,
				getExperimentTypeNodes("nanopore-pre-pcr-ligation","nanopore-fragmentation","nanopore-frg"),null,null,
				null).save();			
				 
		newExperimentTypeNode("nanopore-barcode-ligation",
				getExperimentTypes("nanopore-barcode-ligation").get(0),false, false,false,
				getExperimentTypeNodes("nanopore-end-prep","nanopore-pcr"),null,null,
				null).save();	
	
		newExperimentTypeNode("nanopore-final-ligation",
				getExperimentTypes("nanopore-final-ligation").get(0),false, false,false,
				getExperimentTypeNodes("nanopore-fragmentation","nanopore-frg","nanopore-barcode-ligation","nanopore-end-prep","nanopore-pcr"),getExperimentTypes("nanopore-sizing"),null,
				getExperimentTypes("aliquoting","pool-tube")).save();	
		
		newExperimentTypeNode("nanopore-depot",
				getExperimentTypes("nanopore-depot").get(0),false, false,false,
			getExperimentTypeNodes("nanopore-library","ext-to-nanopore-run","ext-to-nanopore-process-dev","nanopore-final-ligation"),null,null,
			null).save();
		

		ExperimentTypeNode etn = getExperimentTypeNodes("nanopore-end-prep").get(0);
	    etn.previousExperimentTypeNodes=getExperimentTypeNodes("ext-to-nanopore-process-dev-2","nanopore-fragmentation", "nanopore-frg","nanopore-dna-reparation","nanopore-pcr");
    

//Just update previousExperimentNode 
	etn.update(); 
	
		
	
	
	}

	@Override
	protected void getExperimentTypeNodePROD() {
		/*newExperimentTypeNode("nanopore-fragmentation",
				getExperimentTypes("nanopore-fragmentation").get(0),false, false,false,
				getExperimentTypeNodes("dna-rna-extraction", "ext-to-nanopore-process-library","ext-to-nanopore-process-dev"),null,null,
				getExperimentTypes("aliquoting")).save();	
		
		newExperimentTypeNode("nanopore-frg",
				getExperimentTypes("nanopore-frg").get(0),false, false,false,
				getExperimentTypeNodes("dna-rna-extraction","ext-to-nanopore-frg-lib-depot","ext-to-nanopore-frg-rep-lib-depot","ext-to-nanopore-process-library","ext-to-nanopore-process-dev"),getExperimentTypes("nanopore-sizing"),null,
				getExperimentTypes("aliquoting","pool-tube")).save();	
		
		newExperimentTypeNode("nanopore-dna-reparation",
				getExperimentTypes("nanopore-dna-reparation").get(0),false, false,false,
				getExperimentTypeNodes("ext-to-nanopore-rep-lib-depot","nanopore-frg","nanopore-fragmentation","dna-rna-extraction","ext-to-nanopore-process-dev"),null,null,
				getExperimentTypes("aliquoting","pool-tube")).save();
		
		newExperimentTypeNode("nanopore-library",
				getExperimentTypes("nanopore-library").get(0),false, false,false,
				getExperimentTypeNodes("ext-to-nanopore-process-library-no-frg","dna-rna-extraction","nanopore-fragmentation", "nanopore-frg","nanopore-dna-reparation","ext-to-nanopore-process-dev"),getExperimentTypes("nanopore-sizing"),getExperimentTypes("fluo-quantification"),
				getExperimentTypes("aliquoting","pool-tube")).save();	
				
		newExperimentTypeNode("nanopore-depot",
				getExperimentTypes("nanopore-depot").get(0),false, false,false,
			getExperimentTypeNodes("nanopore-library","ext-to-nanopore-run","ext-to-nanopore-process-dev"),null,null,
			null).save();*/
	}

	@Override
	protected void getExperimentTypeNodeUAT() {
		//newExperimentTypeNode("nanopore-fragmentation",getExperimentTypes("nanopore-fragmentation").get(0),false, false,false,getExperimentTypeNodes("ext-to-nanopore-process-library"),null,getExperimentTypes("qpcr-quantification"),getExperimentTypes("aliquoting")).save();
	}


	@Override
	protected void getExperimentTypeNodeDEV() {
		
		///
		
			

	}

	//NE PAS MODIFIER NI SUPPRIMER (historique)
	private static List<PropertyDefinition> getPropertyFragmentationNanoporeOld() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Nb fragmentations","fragmentionNumber",LevelService.getLevels(Level.CODE.ContainerIn), Integer.class, true, null
				, null ,null,null, "single",11));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Qté totale dans frg","inputFrgQuantity",LevelService.getLevels(Level.CODE.ContainerIn,Level.CODE.Content), Double.class, true,  null
				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_QUANTITY),MeasureUnit.find.findByCode("ng"),MeasureUnit.find.findByCode( "ng"), "single",12));


		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Conc. finale FRG","postFrgConcentration",LevelService.getLevels(Level.CODE.ContainerOut), Double.class, false, null
				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_CONCENTRATION),MeasureUnit.find.findByCode( "ng/µl"),MeasureUnit.find.findByCode( "ng/µl"), "single",13));

		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Qté finale FRG","postFrgQuantity",LevelService.getLevels(Level.CODE.ContainerOut,Level.CODE.Content), Double.class, false, null
				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_QUANTITY),MeasureUnit.find.findByCode( "ng"),MeasureUnit.find.findByCode( "ng"), "single",14));
		/*
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Profil","fragmentionProfile",LevelService.getLevels(Level.CODE.ContainerOut), Image.class, false, null
				,null,null,null, "img",15));
		 */
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Taille réelle","measuredLibrarySize",LevelService.getLevels(Level.CODE.ContainerOut,Level.CODE.Content), Integer.class, false, null
				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_SIZE),MeasureUnit.find.findByCode( "pb"),MeasureUnit.find.findByCode( "pb"), "single",16));

		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Nb réparations","preCRNumber",LevelService.getLevels(Level.CODE.ContainerOut), Integer.class, false, null
				, null ,null,null, "single",17));
		/*propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Conc. finale preCR","postPreCRConcentration",LevelService.getLevels(Level.CODE.ContainerOut), Double.class, false, null
				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_CONCENTRATION),MeasureUnit.find.findByCode( "ng/µl"),MeasureUnit.find.findByCode( "ng/µl"), "single",8));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Qté finale preCR","postPreCRQuantity",LevelService.getLevels(Level.CODE.ContainerOut), Double.class, false,null
				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_QUANTITY),MeasureUnit.find.findByCode( "ng"),MeasureUnit.find.findByCode( "ng"), "single",9));

		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Volume final","measuredVolume",LevelService.getLevels(Level.CODE.ContainerOut), Double.class, false, null
				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"), "single",10));
		 */
		return propertyDefinitions;

	}



	private static List<PropertyDefinition> getPropertyFragmentationNanopore() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		//Containers
		propertyDefinitions.add(newPropertiesDefinition(
				"Nb fragmentations","fragmentionNumber",LevelService.getLevels(Level.CODE.ContainerIn), Integer.class,
				true, null, null , "single",11,true,null,null));
		propertyDefinitions.add(newPropertiesDefinition(
				"Qté totale dans frg","inputFrgQuantity",LevelService.getLevels(Level.CODE.ContainerIn,Level.CODE.Content), Double.class,
				true,  null,null, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_QUANTITY),MeasureUnit.find.findByCode("ng"),MeasureUnit.find.findByCode( "ng"),
				"single",12,true,null,null));
	/*	propertyDefinitions.add(newPropertiesDefinition(
				"Conc. finale FRG","postFrgConcentration",LevelService.getLevels(Level.CODE.ContainerOut,Level.CODE.Content), Double.class,
				false, null, null,MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_CONCENTRATION),MeasureUnit.find.findByCode( "ng/µl"),MeasureUnit.find.findByCode( "ng/µl"),
				"single",130,true,null,null));*/
		propertyDefinitions.add(newPropertiesDefinition(
				"Profil","migrationProfile",LevelService.getLevels(Level.CODE.ContainerOut), Image.class,
				false, null, null ,"img",15,true,null,null));
		propertyDefinitions.add(newPropertiesDefinition(
				"Taille réelle","measuredLibrarySize",LevelService.getLevels(Level.CODE.ContainerOut,Level.CODE.Content), Integer.class, 
				true, "F",null, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_SIZE),MeasureUnit.find.findByCode( "pb"),MeasureUnit.find.findByCode( "pb"),
				"single",16,true,null,null));	
		propertyDefinitions.add(newPropertiesDefinition(
				"Qté finale FRG","postFrgQuantity",LevelService.getLevels(Level.CODE.ContainerOut,Level.CODE.Content), Double.class,
				false, null,null, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_QUANTITY),MeasureUnit.find.findByCode("ng"),MeasureUnit.find.findByCode( "ng"),
				"single",19,false,null,null));
		/*propertyDefinitions.add(newPropertiesDefinition(
				"Volume final","measuredVolume",LevelService.getLevels(Level.CODE.ContainerOut,Level.CODE.Content), Double.class,
				false, null,null, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"), 
				"single",10,true,null,null));*/
	/*	propertyDefinitions.add(newPropertiesDefinition("Nb réparations","preCRNumber",LevelService.getLevels(Level.CODE.ContainerOut), Integer.class,
				 false ,null,null,"single",170,true,null,null));*/
	
		//Experiments
		propertyDefinitions.add(newPropertiesDefinition(
				"Schéma de manips","experimentPlan",LevelService.getLevels(Level.CODE.Experiment), Image.class, 
				false, null, null , "img",16,true,null,null));

		
		return propertyDefinitions;

	}
	
	
	private static List<PropertyDefinition> getPropertyReparationNanopore() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		//Containers
		propertyDefinitions.add(newPropertiesDefinition(
				"Qté engagée","inputQuantity",LevelService.getLevels(Level.CODE.ContainerIn), Double.class,
				false, null, null ,"single",11,true,null,null));
		propertyDefinitions.add(newPropertiesDefinition(
				"Nb réparations","reparationNumber",LevelService.getLevels(Level.CODE.ContainerIn), Integer.class,
				false, null, null ,"single",12,true,null,null));
		
		 propertyDefinitions.add(newPropertiesDefinition("Concentration","outputConcentration",LevelService.getLevels(Level.CODE.ContainerOut), Double.class,
					true, "F",null, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_CONCENTRATION),MeasureUnit.find.findByCode( "ng/µl"),MeasureUnit.find.findByCode( "ng/µl"),
					"single",50,true, null, null));
			
		
		//Experiments
		propertyDefinitions.add(newPropertiesDefinition(
				"Schéma de manips","experimentPlan",LevelService.getLevels(Level.CODE.Experiment), Image.class,
				false, null, null ,"img",15,true,null,null));

		return propertyDefinitions;

	}
	
	private static List<PropertyDefinition> getPropertySizingNanopore() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		//Containers

		propertyDefinitions.add(newPropertiesDefinition(
				"Taille théorique sizing","expectedSize",LevelService.getLevels(Level.CODE.ContainerIn), String.class,
				false, null ,null, "single",12,true,null,null));
		
		propertyDefinitions.add(newPropertiesDefinition(
				"Temps migration","migrationTime",LevelService.getLevels(Level.CODE.ContainerIn), String.class,
				false, null ,null, "single",11,true,null,null));
		
		
		 propertyDefinitions.add(newPropertiesDefinition(
					"Profil","migrationProfile", LevelService.getLevels(Level.CODE.ContainerOut),Image.class,
					 false, null,null,"img",51,true,null,null));	
			
		 propertyDefinitions.add(newPropertiesDefinition(
					"Taille sizing (av. lib)","measuredSizePostSizing",LevelService.getLevels(Level.CODE.ContainerOut,Level.CODE.Content), Integer.class, 
					false, null,null, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_SIZE),MeasureUnit.find.findByCode( "pb"),MeasureUnit.find.findByCode( "pb"),
					"single",60,false,null,null));	
			
		 
		 propertyDefinitions.add(newPropertiesDefinition("Conc. ligation (ap. sizing)","ligationConcentrationPostSizing",LevelService.getLevels(Level.CODE.ContainerOut,Level.CODE.Content), Double.class,
				false, null,null, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_CONCENTRATION),MeasureUnit.find.findByCode( "ng/µl"),MeasureUnit.find.findByCode( "ng/µl"),
				"single",61,false, null, null));
		
		propertyDefinitions.add(newPropertiesDefinition("Qté ligation (ap. sizing)","ligationQuantityPostSizing",LevelService.getLevels(Level.CODE.ContainerOut,Level.CODE.Content), Double.class,
				false, null,null, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_QUANTITY),MeasureUnit.find.findByCode( "ng"),MeasureUnit.find.findByCode( "ng"),
				"single",62,false, null, null));
		
		
		
		//Experiments
		propertyDefinitions.add(newPropertiesDefinition("schéma de manips","experimentPlan",LevelService.getLevels(Level.CODE.Experiment), Image.class,
				false, null, null, "img",16,true,null,null));

		return propertyDefinitions;

	}
	private static List<PropertyDefinition> getPropertyDepotNanopore() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		//Experiments
		propertyDefinitions.add(newPropertiesDefinition(
				"Date réelle de dépôt", "runStartDate", LevelService.getLevels(Level.CODE.Experiment), Date.class,
				true, null,null,"single",300,true,null,null));
		propertyDefinitions.add(newPropertiesDefinition(
				"PDF Report","report",LevelService.getLevels(Level.CODE.Experiment), File.class,
				false,null,null, "file", 310,true,null,null));
		propertyDefinitions.add(newPropertiesDefinition
				("Schéma de manips","experimentPlan",LevelService.getLevels(Level.CODE.Experiment), Image.class,
						false, null ,null,"img",320,true,null,null));
		//Containers
		// Unite a verifier
		//1er tableau
		propertyDefinitions.add(newPropertiesDefinition(
				"Quantité déposée","loadingQuantity",LevelService.getLevels(Level.CODE.ContainerIn,Level.CODE.Content), Double.class,
				true, "N", null,  MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_QUANTITY),MeasureUnit.find.findByCode( "ng"),MeasureUnit.find.findByCode( "ng"),
				"single",8,false, null,"1"));
		
		//2eme tableau
		propertyDefinitions.add(newPropertiesDefinition(
				"Date creation","loadingReport.creationDate",LevelService.getLevels(Level.CODE.ContainerIn, Level.CODE.Content), Date.class,
				false, null,null, "object_list",600,true,null,null));
		propertyDefinitions.add(newPropertiesDefinition(
				"Heure dépot","loadingReport.hour",LevelService.getLevels(Level.CODE.ContainerIn, Level.CODE.Content), String.class,
				false, null,null,"object_list",601,true,null,null));
		propertyDefinitions.add(newPropertiesDefinition(
				"Temps","loadingReport.time",LevelService.getLevels(Level.CODE.ContainerIn, Level.CODE.Content), Long.class,
				false,null,null, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_TIME),MeasureUnit.find.findByCode( "h"),MeasureUnit.find.findByCode( "h"),
				"object_list",602,true, null, null));
		propertyDefinitions.add(newPropertiesDefinition(
				"Volume","loadingReport.volume",LevelService.getLevels(Level.CODE.ContainerIn, Level.CODE.Content), Double.class,
				false, null, null, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"),
				"object_list",603,true, null, null));
		
		//3ème tableau	
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition(
				"Groupe","qcFlowcell.group",LevelService.getLevels(Level.CODE.ContainerOut,Level.CODE.Content), String.class,
				false, null,null, "object_list",700,false,null,null));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition(
				"Nb total pores actifs à réception","qcFlowcell.preLoadingNbActivePores",LevelService.getLevels(Level.CODE.ContainerOut,Level.CODE.Content), Integer.class
				, false,null,null, "object_list",701,true, null,null));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition(
				"Nb total pores actifs lors du dépôt","qcFlowcell.postLoadingNbActivePores",LevelService.getLevels(Level.CODE.ContainerOut,Level.CODE.Content), Integer.class,
				false,null,null, "object_list",702,true, null,null));

		//propertyDefinitions.add(newPropertiesDefinition("Channels with Reads", "minknowChannelsWithReads", LevelService.getLevels(Level.CODE.ContainerOut),Integer.class, false, "single",301));
		//propertyDefinitions.add(newPropertiesDefinition("Events in Reads", "minknowEvents", LevelService.getLevels(Level.CODE.ContainerOut),Double.class, false, "single",302));
		//propertyDefinitions.add(newPropertiesDefinition("Complete reads", "minknowCompleteReads", LevelService.getLevels(Level.CODE.ContainerOut),Integer.class, false, "single",303));
		//propertyDefinitions.add(newPropertiesDefinition("Read count", "metrichorReadCount", LevelService.getLevels(Level.CODE.ContainerOut),Integer.class, false, "single",304));
		//propertyDefinitions.add(newPropertiesDefinition("Total 2D yield", "metrichor2DReadsYield", LevelService.getLevels(Level.CODE.ContainerOut),Integer.class, false, null
				//, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_SIZE),MeasureUnit.find.findByCode( "pb"),MeasureUnit.find.findByCode( "pb"), "single",305));
		//propertyDefinitions.add(newPropertiesDefinition("Longest 2D read", "metrichorMax2DRead", LevelService.getLevels(Level.CODE.ContainerOut),Integer.class, false, null
				//, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_SIZE),MeasureUnit.find.findByCode( "pb"),MeasureUnit.find.findByCode( "pb"),"single",306));
		//propertyDefinitions.add(newPropertiesDefinition("Peak 2D quality score", "metrichorMax2DQualityScore", LevelService.getLevels(Level.CODE.ContainerOut),Double.class, false, "single",307));

		
		
		return propertyDefinitions;
	}

//	private static List<PropertyDefinition> getPropertyDepotNanoporeOld() throws DAOException {
//		List<PropertyDefinition> propertyDefinitions = new ArrayList<PropertyDefinition>();
//		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Date réelle de dépôt", "runStartDate", LevelService.getLevels(Level.CODE.Experiment), Date.class, true, "single",300));
//		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("PDF Report","report",LevelService.getLevels(Level.CODE.Experiment), File.class, false, "file", 400));
//
//		// Unite a verifier
//		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Date creation","loadingReport.creationDate",LevelService.getLevels(Level.CODE.ContainerIn, Level.CODE.Content), Date.class, false, "object_list",600));
//		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Heure dépot","loadingReport.hour",LevelService.getLevels(Level.CODE.ContainerIn, Level.CODE.Content), String.class, false,"object_list",601));
//		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Temps","loadingReport.time",LevelService.getLevels(Level.CODE.ContainerIn, Level.CODE.Content), Long.class, false,null
//				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_TIME),MeasureUnit.find.findByCode( "h"),MeasureUnit.find.findByCode( "h"), "object_list",602));
//		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Volume","loadingReport.volume",LevelService.getLevels(Level.CODE.ContainerIn, Level.CODE.Content), Double.class, false, null
//				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"), "object_list",603));
//
//		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Groupe","qcFlowcell.group",LevelService.getLevels(Level.CODE.ContainerOut, Level.CODE.Content), String.class, false, false, "object_list",700));
//		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Nb pores actifs à réception","qcFlowcell.preLoadingNbActivePores",LevelService.getLevels(Level.CODE.ContainerOut, Level.CODE.Content), Integer.class, false, "object_list",701));
//		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Nb pores actifs lors du dépôt","qcFlowcell.postLoadingNbActivePores",LevelService.getLevels(Level.CODE.ContainerOut, Level.CODE.Content), Integer.class, false, "object_list",702));
//
//		//propertyDefinitions.add(newPropertiesDefinition("Channels with Reads", "minknowChannelsWithReads", LevelService.getLevels(Level.CODE.ContainerOut),Integer.class, false, "single",301));
//		//propertyDefinitions.add(newPropertiesDefinition("Events in Reads", "minknowEvents", LevelService.getLevels(Level.CODE.ContainerOut),Double.class, false, "single",302));
//		//propertyDefinitions.add(newPropertiesDefinition("Complete reads", "minknowCompleteReads", LevelService.getLevels(Level.CODE.ContainerOut),Integer.class, false, "single",303));
//		//propertyDefinitions.add(newPropertiesDefinition("Read count", "metrichorReadCount", LevelService.getLevels(Level.CODE.ContainerOut),Integer.class, false, "single",304));
//		//propertyDefinitions.add(newPropertiesDefinition("Total 2D yield", "metrichor2DReadsYield", LevelService.getLevels(Level.CODE.ContainerOut),Integer.class, false, null
//		//		, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_SIZE),MeasureUnit.find.findByCode( "pb"),MeasureUnit.find.findByCode( "pb"), "single",305));
//		//propertyDefinitions.add(newPropertiesDefinition("Longest 2D read", "metrichorMax2DRead", LevelService.getLevels(Level.CODE.ContainerOut),Integer.class, false, null
//		//		, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_SIZE),MeasureUnit.find.findByCode( "pb"),MeasureUnit.find.findByCode( "pb"),"single",306));
//		//propertyDefinitions.add(newPropertiesDefinition("Peak 2D quality score", "metrichorMax2DQualityScore", LevelService.getLevels(Level.CODE.ContainerOut),Double.class, false, "single",307));
//
//		return propertyDefinitions;
//	}

	
	private static List<PropertyDefinition> getPropertyLibrairieNanopore() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		//Containers
		propertyDefinitions.add(newPropertiesDefinition(
				"Volume engagé","inputVolume", LevelService.getLevels(Level.CODE.ContainerIn),Double.class,
				false, null,null, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"), "single",
				10,true, null, null));
		propertyDefinitions.add(newPropertiesDefinition(
				"Qté engagée dans bq","libraryInputQuantity", LevelService.getLevels(Level.CODE.ContainerIn, Level.CODE.Content),Double.class,
				false, null,null, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_QUANTITY),MeasureUnit.find.findByCode( "ng"),MeasureUnit.find.findByCode( "ng"), "single",
				11,true, null, null));
		propertyDefinitions.add(newPropertiesDefinition(
				"Tag","tag", LevelService.getLevels(Level.CODE.ContainerOut,Level.CODE.Content),String.class,
				false,null, getTagNanopore(),"single",13,true,null,null));
		propertyDefinitions.add(newPropertiesDefinition(
				"Catégorie tag","tagCategory", LevelService.getLevels(Level.CODE.ContainerOut,Level.CODE.Content),String.class,
				false, null, getTagCategoriesNanopore(), "single",14,true,"SINGLE-INDEX",null));
		propertyDefinitions.add(newPropertiesDefinition(
				"Conc. finale End Repair","postEndRepairConcentration", LevelService.getLevels(Level.CODE.ContainerOut),Double.class,
				false, null,null, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_CONCENTRATION),MeasureUnit.find.findByCode( "ng/µl"),MeasureUnit.find.findByCode( "ng/µl"),
				"single",15,true, null, null));
		propertyDefinitions.add(newPropertiesDefinition(
				"Qté finale End Repair","postEndRepairQuantity", LevelService.getLevels(Level.CODE.ContainerOut),Double.class,
				false, null,null, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_QUANTITY),MeasureUnit.find.findByCode( "ng"),MeasureUnit.find.findByCode( "ng"),
				"single",20,true, null, null));
		propertyDefinitions.add(newPropertiesDefinition(
				"Conc. finale dA tailing","postTailingConcentration", LevelService.getLevels(Level.CODE.ContainerOut),Double.class,
				false, null,null, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_CONCENTRATION),MeasureUnit.find.findByCode( "ng/µl"),MeasureUnit.find.findByCode( "ng/µl"),
				"single",30,true, null, null));
		propertyDefinitions.add(newPropertiesDefinition(
				"Qté finale dA tailing","postTailingQuantity", LevelService.getLevels(Level.CODE.ContainerOut),Double.class,
				false, null,null, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_QUANTITY),MeasureUnit.find.findByCode( "ng"),MeasureUnit.find.findByCode( "ng"),
				"single",40,true, null, null));
		propertyDefinitions.add(newPropertiesDefinition(
				"Profil","migrationProfile", LevelService.getLevels(Level.CODE.ContainerOut),Image.class,
				 false, null,null, "img",50,true,null,null));		
		propertyDefinitions.add(newPropertiesDefinition(
				"Conc. finale Ligation","ligationConcentration", LevelService.getLevels(Level.CODE.ContainerOut,Level.CODE.Content),Double.class,
				true, null, null, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_CONCENTRATION),MeasureUnit.find.findByCode( "ng/µl"),MeasureUnit.find.findByCode( "ng/µl"), 
				"single",50,true, null, null));
		propertyDefinitions.add(newPropertiesDefinition(
				"Qté finale Ligation","ligationQuantity", LevelService.getLevels(Level.CODE.ContainerOut,Level.CODE.Content),Double.class,
				true, null, null, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_QUANTITY),MeasureUnit.find.findByCode( "ng"),MeasureUnit.find.findByCode( "ng"),
				"single",60,false, null, null)); 

		//Experiments
		propertyDefinitions.add(newPropertiesDefinition(
				"schéma de manips","experimentPlan",LevelService.getLevels(Level.CODE.Experiment), Image.class,
				false, null, null,  "img",70,true,null,null));
		
		return propertyDefinitions;
	}
	
//	private static List<PropertyDefinition> getPropertyLibrairieNanoporeOld() throws DAOException {
//		List<PropertyDefinition> propertyDefinitions = new ArrayList<PropertyDefinition>();
//		propertyDefinitions.add(newPropertiesDefinition("Volume engagé","inputVolume", LevelService.getLevels(Level.CODE.ContainerIn),Double.class, false, null
//				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"), "single",10));
//		propertyDefinitions.add(newPropertiesDefinition("Qté engagée dans bq","libraryInputQuantity", LevelService.getLevels(Level.CODE.ContainerIn, Level.CODE.Content),Double.class, false, null
//				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_QUANTITY),MeasureUnit.find.findByCode( "ng"),MeasureUnit.find.findByCode( "ng"), "single",11));
//		//		propertyDefinitions.add(newPropertiesDefinition("Taille", "librarySize", LevelService.getLevels(Level.CODE.ContainerOut), Integer.class, true, null
//		//			, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "kb"),MeasureUnit.find.findByCode( "kb"), "single",8));
//
//		propertyDefinitions.add(newPropertiesDefinition("Tag","tag", LevelService.getLevels(Level.CODE.ContainerOut,Level.CODE.Content),String.class, false, getTagNanopore(), "single",13));
//		propertyDefinitions.add(newPropertiesDefinition("Catégorie tag","tagCategory", LevelService.getLevels(Level.CODE.ContainerOut,Level.CODE.Content),String.class, false, getTagCategoriesNanopore(),"SINGLE-INDEX", "single",14));
//
//
//		propertyDefinitions.add(newPropertiesDefinition("Conc. finale End Repair","postEndRepairConcentration", LevelService.getLevels(Level.CODE.ContainerOut),Double.class, false, null
//				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_CONCENTRATION),MeasureUnit.find.findByCode( "ng/µl"),MeasureUnit.find.findByCode( "ng/µl"), "single",15));
//		propertyDefinitions.add(newPropertiesDefinition("Qté finale End Repair","postEndRepairQuality", LevelService.getLevels(Level.CODE.ContainerOut),Double.class, false, null
//				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_QUANTITY),MeasureUnit.find.findByCode( "ng"),MeasureUnit.find.findByCode( "ng"), "single",20));
//
//		propertyDefinitions.add(newPropertiesDefinition("Conc. finale dA tailing","postTailingConcentration", LevelService.getLevels(Level.CODE.ContainerOut),Double.class, false, null
//				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_CONCENTRATION),MeasureUnit.find.findByCode( "ng/µl"),MeasureUnit.find.findByCode( "ng/µl"), "single",30));
//		propertyDefinitions.add(newPropertiesDefinition("Qté finale dA tailing","postTailingQuality", LevelService.getLevels(Level.CODE.ContainerOut),Double.class, false, null
//				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_QUANTITY),MeasureUnit.find.findByCode( "ng"),MeasureUnit.find.findByCode( "ng"), "single",40));
//
//
//		propertyDefinitions.add(newPropertiesDefinition("Conc. finale Ligation","ligationConcentration", LevelService.getLevels(Level.CODE.ContainerOut,Level.CODE.Content),Double.class, true, null
//				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_CONCENTRATION),MeasureUnit.find.findByCode( "ng/µl"),MeasureUnit.find.findByCode( "ng/µl"), "single",50));
//
//		propertyDefinitions.add(newPropertiesDefinition("Qté finale Ligation","ligationQuantity", LevelService.getLevels(Level.CODE.ContainerOut,Level.CODE.Content),Double.class, false, null
//				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_QUANTITY),MeasureUnit.find.findByCode( "ng"),MeasureUnit.find.findByCode( "ng"), "single",60)); 
//
//
//		return propertyDefinitions;
//	}
	
	private static List<PropertyDefinition> getPropertyEndPrepNanopore(){	
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
				
		//Container
			propertyDefinitions.add(newPropertiesDefinition(
					"Volume engagé","inputVolume", LevelService.getLevels(Level.CODE.ContainerIn),Double.class, 
					false, null, null,MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"), 
					"single",10,true, null, null));		
			propertyDefinitions.add(newPropertiesDefinition(
					"Qté engagée","inputQuantity", LevelService.getLevels(Level.CODE.ContainerIn),Double.class,
					false, null,null, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_QUANTITY),MeasureUnit.find.findByCode( "ng"),MeasureUnit.find.findByCode( "ng"), 
					"single",11,false, null, null));		
			
			return propertyDefinitions;
		}
		
		private static List<PropertyDefinition> getPropertyLigPrePcrNanopore(){
			List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
			
			//Container
			propertyDefinitions.add(newPropertiesDefinition(
					"Volume engagé","inputVolume", LevelService.getLevels(Level.CODE.ContainerIn),Double.class, 
					false, null, null,MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"), 
					"single",10,true, null, null));		
			propertyDefinitions.add(newPropertiesDefinition(
					"Qté engagée","inputQuantity", LevelService.getLevels(Level.CODE.ContainerIn),Double.class,
					false, null,null, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_QUANTITY),MeasureUnit.find.findByCode( "ng"),MeasureUnit.find.findByCode( "ng"), 
					"single",11,false, null, null));		
			
			return propertyDefinitions;
		}
		
		private static List<PropertyDefinition> getPropertyPcrNanopore(){
			List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
			
			//Container
			propertyDefinitions.add(newPropertiesDefinition(
					"Profil","migrationProfile", LevelService.getLevels(Level.CODE.ContainerOut),Image.class,
					 false, null,null, "img",49,true,null,null));	
			
			return propertyDefinitions;
		}
		
		private static List<PropertyDefinition> getPropertyLigBarcodeNanopore(){
				List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
			
				//Container
			propertyDefinitions.add(newPropertiesDefinition(
					"Volume engagé","inputVolume", LevelService.getLevels(Level.CODE.ContainerIn),Double.class, 
					false, null, null,MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"), 
					"single",10,true, null, null));			
			propertyDefinitions.add(newPropertiesDefinition(
					"Qté engagée","inputQuantity", LevelService.getLevels(Level.CODE.ContainerIn),Double.class,
					false, null,null, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_QUANTITY),MeasureUnit.find.findByCode( "ng"),MeasureUnit.find.findByCode( "ng"), 
					"single",11,false, null, null));		
				
			propertyDefinitions.add(newPropertiesDefinition(
					"Tag","tag", LevelService.getLevels(Level.CODE.ContainerOut,Level.CODE.Content),String.class,
					false,null, getTagNanopore(),"single",13,true,null,null));
			propertyDefinitions.add(newPropertiesDefinition(
					"Catégorie tag","tagCategory", LevelService.getLevels(Level.CODE.ContainerOut,Level.CODE.Content),String.class,
					false, null, getTagCategoriesNanopore(), "single",14,true,"SINGLE-INDEX",null));	
			propertyDefinitions.add(newPropertiesDefinition(
					"Profil","migrationProfile", LevelService.getLevels(Level.CODE.ContainerOut),Image.class,
					false, null,null, "img",50,true,null,null));	
				
				
				return propertyDefinitions;
		 }
		 
		private static List<PropertyDefinition> getPropertyLigFinaleNanopore(){
			List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
			
			//Container
			propertyDefinitions.add(newPropertiesDefinition(
					"Volume engagé","inputVolume", LevelService.getLevels(Level.CODE.ContainerIn),Double.class, 
					false, null, null,MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"), 
					"single",10,true, null, null));			
			propertyDefinitions.add(newPropertiesDefinition(
					"Qté engagée","inputQuantity", LevelService.getLevels(Level.CODE.ContainerIn),Double.class,
					false, null,null, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_QUANTITY),MeasureUnit.find.findByCode( "ng"),MeasureUnit.find.findByCode( "ng"), 
					"single",11,false, null, null));		
			
			propertyDefinitions.add(newPropertiesDefinition(
					"Profil","migrationProfile", LevelService.getLevels(Level.CODE.ContainerOut),Image.class,
					 false, null,null, "img",50,true,null,null));	
			propertyDefinitions.add(newPropertiesDefinition("Conc. finale ligation","ligationConcentration",LevelService.getLevels(Level.CODE.ContainerOut,Level.CODE.Content), Double.class,
					true, null,null, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_CONCENTRATION),MeasureUnit.find.findByCode( "ng/µl"),MeasureUnit.find.findByCode( "ng/µl"),
					"single",51,true, null, null));			
			propertyDefinitions.add(newPropertiesDefinition("Qté finale ligation","ligationQuantity",LevelService.getLevels(Level.CODE.ContainerOut,Level.CODE.Content), Double.class,
					false, null,null, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_QUANTITY),MeasureUnit.find.findByCode( "ng"),MeasureUnit.find.findByCode( "ng"),
					"single",52,true, null, null));
				
				
				//Experiments
			propertyDefinitions.add(newPropertiesDefinition("schéma de manips","experimentPlan",LevelService.getLevels(Level.CODE.Experiment), Image.class,
					false, null, null, "img",16,true,null,null));
				
			return propertyDefinitions;
				}
		 

	private static List<Value> getTagNanopore() {
		List<NanoporeIndex> indexes = MongoDBDAO.find(InstanceConstants.PARAMETER_COLL_NAME, NanoporeIndex.class, DBQuery.is("typeCode", "index-nanopore-sequencing")).sort("name").toList();
		List<Value> values = new ArrayList<>();
		indexes.forEach(index -> {
			values.add(DescriptionFactory.newValue(index.code, index.name));	
		});

		return values;
	}

	private static List<Value> getTagCategoriesNanopore(){
		List<Value> values = new ArrayList<>();
		values.add(DescriptionFactory.newValue("SINGLE-INDEX", "SINGLE-INDEX"));		
		return values;	
	}

	
	public static List<PropertyDefinition> getPropertyDefinitionsNanoporeFragmentation() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();		
		propertyDefinitions.addAll(getPropertyDefinitionsNanoporeLibrary());
		propertyDefinitions.add(newPropertiesDefinition(
				"Taille banque souhaitée","librarySize",LevelService.getLevels(Level.CODE.Process, Level.CODE.Content),Integer.class,
				true,null, null, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_SIZE),MeasureUnit.find.findByCode( "kb"),MeasureUnit.find.findByCode( "kb"),
				"single",2,true,null,null));
		
		return propertyDefinitions;
	}
	
	public static List<PropertyDefinition> getPropertyDefinitionsNanoporeLibrary() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition(
				"Type processus banque","libProcessTypeCode",LevelService.getLevels(Level.CODE.Process,Level.CODE.Content),String.class,
				true, null,getLibProcessTypeCodeValues(),"single",1, true,"ONT",null));
		
		propertyDefinitions.addAll(getPropertyDefinitionsRunNanopore());
		
		return propertyDefinitions;
	}
	
	public static List<PropertyDefinition> getPropertyDefinitionsRunNanopore() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		
		propertyDefinitions.add(
				newPropertiesDefinition("Dev / Prod ?", "devProdContext", LevelService.getLevels(Level.CODE.Process,Level.CODE.Content), String.class, true, null, 
						DescriptionFactory.newValues("PROD","DEV"),null,null,null,"single", 500, true, null, null)
				);
		
		return propertyDefinitions;
	}

	private static List<Value> getLibProcessTypeCodeValues(){
        List<Value> values = new ArrayList<>();
         values.add(DescriptionFactory.newValue("ONT","ONT - Nanopore"));
         return values;
	}

	
	

}
