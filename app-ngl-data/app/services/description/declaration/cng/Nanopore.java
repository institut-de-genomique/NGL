package services.description.declaration.cng;

import static services.description.DescriptionFactory.newExperimentType;
import static services.description.DescriptionFactory.newExperimentTypeNode;
import static services.description.DescriptionFactory.newPropertiesDefinition;

import java.awt.Image;
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
import models.laboratory.processes.description.ProcessCategory;
import models.laboratory.processes.description.ProcessType;
import models.utils.InstanceConstants;
import models.utils.dao.DAOException;
import services.description.Constants;
import services.description.DescriptionFactory;
import services.description.common.LevelService;
import services.description.common.MeasureService;
import services.description.declaration.AbstractDeclaration;

// FDS copie 31/03/2017 depuis la classe CNS et adaptations
// => voir  AbstractDeclaration.java pour le fonctionnement des Common/PROD/UAT/DEV..

public class Nanopore extends AbstractDeclaration{

	//////////////// PROCESSUS  /////////////////////////////////////
	
	@Override
	protected List<ProcessType> getProcessTypeCommon() {
		List<ProcessType> l=new ArrayList<>();
		
		 // supression nanopore-fragmentation; ajout de cDNAsynthesis; renumerotation

		 l.add(DescriptionFactory.newProcessType("Nanopore DEV", "nanopore-process-dev", ProcessCategory.find.findByCode("nanopore-library"),
				1,
                getPropertyDefinitionsNanoporeFragmentation(), 
                Arrays.asList(getPET("ext-to-nanopore-process-dev",-1), 
                      
                		      getPET("cdna-synthesis",0),
                              getPET("nanopore-frg",0),
                              getPET("nanopore-dna-reparation",0),
                              getPET("nanopore-library",0),
                              
                              getPET("nanopore-frg",1),
                              getPET("nanopore-dna-reparation",1),
                              getPET("nanopore-library",1),
                              getPET("nanopore-depot",1),
                              
                              getPET("nanopore-dna-reparation",2),
                              getPET("nanopore-library",2),
                              getPET("nanopore-depot",2),    
                              
                              getPET("nanopore-library",3),
                              getPET("nanopore-depot",3), 
                              
                              getPET("nanopore-depot",4)), 
                              
                getExperimentTypes("nanopore-library").get(0),            //first experiment type    !!!!mettre celle qui parmis les getPET(0) se retrouve avec le getPET(x) le plus elevé
                                                                          //                         => ici c'est donc nanopore-library qu'on trouve en getPET(  3 )
                getExperimentTypes("nanopore-depot").get(0),              //last  experiment type
                getExperimentTypes("ext-to-nanopore-process-dev").get(0), //void  experiment type
                DescriptionFactory.getInstitutes(Constants.CODE.CNG)));
		return l;
	}

	@Override
	protected List<ProcessType> getProcessTypePROD() {
		List<ProcessType> l=new ArrayList<>();
		
		return l;
	}
	
	@Override
	protected List<ProcessType> getProcessTypeUAT() {	
		List<ProcessType> l=new ArrayList<>(); 
		
		return l;
	}	

	@Override
	protected List<ProcessType> getProcessTypeDEV() {
		List<ProcessType> l=new ArrayList<>();     

		return l;
	}

	
	////////////////////////// EXPERIMENTS ////////////////////////////////////////////////
	@Override
	protected List<ExperimentType> getExperimentTypeCommon() {
		List<ExperimentType> l = new ArrayList<>();
		
		// ext to  

		l.add(newExperimentType("Ext to Process DEV", "ext-to-nanopore-process-dev",null, -1,
			    ExperimentCategory.find.findByCode(ExperimentCategory.CODE.voidprocess.name()), null,  null,"OneToOne",
			    DescriptionFactory.getInstitutes(Constants.CODE.CNG)));

		// others 
		
		//--1-- Synthèse cDNA (recuperee dans MetaTprocess CNS)
		l.add(newExperimentType("Synthèse cDNA","cdna-synthesis","cDNA",1700,
				ExperimentCategory.find.findByCode(ExperimentCategory.CODE.transformation.name()), 
				getPropertyCdnaSynthesis(),
				getInstrumentUsedTypes("mastercycler-nexus",
						               "mastercycler-ep-gradient"),
				"OneToOne", 
				DescriptionFactory.getInstitutes(Constants.CODE.CNG)));
		
		//--2-- Fragmentation Nanopore
		l.add(newExperimentType("Fragmentation Nanopore","nanopore-frg","FRG",1800,
				ExperimentCategory.find.findByCode(ExperimentCategory.CODE.transformation.name()),
				getPropertyFragmentationNanopore(), 
				getInstrumentUsedTypes(//"covaris-e210", FDS 18/07/2017 plus utilisé (inactivé dans instrumentService...)
						               "covaris-le220",
						               "covaris-e220",
						               "eppendorf-5424",
						               "mastercycler-nexus"),
				"OneToOne", 
				DescriptionFactory.getInstitutes(Constants.CODE.CNG)));
		
	    //--3-- Réparation ADN
		l.add(newExperimentType("Réparation ADN","nanopore-dna-reparation","FFP",1900,
				ExperimentCategory.find.findByCode(ExperimentCategory.CODE.transformation.name()),
				getPropertyReparationNanopore(), 
				getInstrumentUsedTypes("hand"),
				"OneToOne", 
				DescriptionFactory.getInstitutes(Constants.CODE.CNG)));
		
		//--4-- Librairie ONT
		l.add(newExperimentType("Librairie ONT","nanopore-library","LIB",2000,
				ExperimentCategory.find.findByCode(ExperimentCategory.CODE.transformation.name()),
				getPropertyLibrairieNanopore(),
				getInstrumentUsedTypes("hand"),
				"OneToOne", 
				DescriptionFactory.getInstitutes(Constants.CODE.CNG)));

	    //--5--Dépot Nanopore
		l.add(newExperimentType("Dépot Nanopore","nanopore-depot",null,2100,
				ExperimentCategory.find.findByCode(ExperimentCategory.CODE.transformation.name()), 
				getPropertyDepotNanopore(),	
				getInstrumentUsedTypes("mk1b"), 
				"OneToOne", 
				DescriptionFactory.getInstitutes(Constants.CODE.CNG) ));
		
		// purification optionnelle
		l.add(newExperimentType("Sizing nanopore","nanopore-sizing","SIZ",100, // numerotation des exp purif
		ExperimentCategory.find.findByCode(ExperimentCategory.CODE.purification.name()), 
		getPropertySizingNanopore(),
		getInstrumentUsedTypes("hand"), // A PRECISER PLUS TARD !!!!
		"OneToMany", 
		DescriptionFactory.getInstitutes(Constants.CODE.CNG) ));
		
		return l;

	}
	
	@Override
	protected List<ExperimentType> getExperimentTypePROD() {
		List<ExperimentType> l = new ArrayList<>();
		
		return l;
	}

	@Override
	protected List<ExperimentType> getExperimentTypeUAT() {
		List<ExperimentType> l = new ArrayList<>();
		
		return l;
	}

	
	@Override
	protected List<ExperimentType> getExperimentTypeDEV() {	 
		List<ExperimentType> l = new ArrayList<>();

		return l;
	}

	//////////////////////// NODES /////////////////////////////////////////
	@Override
	protected void getExperimentTypeNodeCommon() {

		// ext  to
		newExperimentTypeNode("ext-to-nanopore-process-dev", getExperimentTypes("ext-to-nanopore-process-dev").get(0), 
				false, false, false, 
				null, // no previous nodes
				null, 
				null, 
				null
				).save();
		
		// others
		newExperimentTypeNode("cdna-synthesis", getExperimentTypes("cdna-synthesis").get(0),
				false, false,false,
				getExperimentTypeNodes("ext-to-nanopore-process-dev"), // previous nodes
				null, // pas purif
				null, // pas qc
				null  // pas transfert
				).save();	
		
		newExperimentTypeNode("nanopore-frg", getExperimentTypes("nanopore-frg").get(0),
				false, false,false,
				getExperimentTypeNodes("ext-to-nanopore-process-dev",
									   "cdna-synthesis"),     // previous nodes
				getExperimentTypes("nanopore-sizing"),        // purif
				null, // pas qc
				null  // pas transfert
				).save();	
		
		newExperimentTypeNode("nanopore-dna-reparation", getExperimentTypes("nanopore-dna-reparation").get(0),
				false, false,false,
				getExperimentTypeNodes("ext-to-nanopore-process-dev",
									   "cdna-synthesis",
						               "nanopore-frg"), // previous nodes
			    null, // pas purif
			    null, // pas qc
				null  // pas transfert
				).save();
		
		newExperimentTypeNode("nanopore-library", getExperimentTypes("nanopore-library").get(0),
				false, false,false,
				getExperimentTypeNodes("ext-to-nanopore-process-dev",
									   "cdna-synthesis",
						               "nanopore-frg",
						               "nanopore-dna-reparation"),  // previous nodes
			    getExperimentTypes("nanopore-sizing"),              // purif
			    null, //pas qc
				getExperimentTypes("aliquoting","pool")             // transfert
				).save();	
				
		newExperimentTypeNode("nanopore-depot", getExperimentTypes("nanopore-depot").get(0),
				false, false,false,
			    getExperimentTypeNodes("nanopore-library"), // previous nodes
			    null,  // pas purif
			    null,  // pas qc
			    null   // pas transfert
			    ).save();
	}

	@Override
	protected void getExperimentTypeNodePROD() {
		
	}

	@Override
	protected void getExperimentTypeNodeUAT() {
	
	}

	@Override
	protected void getExperimentTypeNodeDEV() {
		
	}
	
	//////////////////// EXPERIMENT PROPERTIES /////////////////////////////////////////

	// --1-- Synthèse cDNA 
	private List<PropertyDefinition> getPropertyCdnaSynthesis() {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();

		propertyDefinitions.add(newPropertiesDefinition("Quantité engagée","inputQuantity", LevelService.getLevels(Level.CODE.ContainerIn),Double.class, false, null,null,
				MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_QUANTITY),
				MeasureUnit.find.findByCode("ng"),
				MeasureUnit.find.findByCode("ng"),
				"single",12, true,null,null));

		propertyDefinitions.add(newPropertiesDefinition("Volume engagé", "inputVolume", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, false, null, null, 
				MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),
				MeasureUnit.find.findByCode("µL"),
				MeasureUnit.find.findByCode("µL"),
				"single", 13, true, null,null));
		
		return propertyDefinitions;
	}
	
	//--2-- Fragmentation Nanopore
	private static List<PropertyDefinition> getPropertyFragmentationNanopore() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		
		//Containers IN
		propertyDefinitions.add(newPropertiesDefinition(
				"Nb fragmentations","fragmentionNumber",LevelService.getLevels(Level.CODE.ContainerIn), Integer.class, false, null, null , 
				"single",11,true,null,null));
		
		propertyDefinitions.add(newPropertiesDefinition(
				"Qté totale dans frg","inputFrgQuantity",LevelService.getLevels(Level.CODE.ContainerIn,Level.CODE.Content), Double.class,false,  null,null, 
				MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_QUANTITY),
				MeasureUnit.find.findByCode("ng"),
				MeasureUnit.find.findByCode( "ng"),
				"single",12,true,null,null));
		
		//Containers OUT
	/*	propertyDefinitions.add(newPropertiesDefinition(
				"Conc. finale FRG","postFrgConcentration",LevelService.getLevels(Level.CODE.ContainerOut,Level.CODE.Content), Double.class,
				false, null, null,MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_CONCENTRATION),MeasureUnit.find.findByCode( "ng/µl"),MeasureUnit.find.findByCode( "ng/µl"),
				"single",130,true,null,null));*/
		
		propertyDefinitions.add(newPropertiesDefinition(
				"Profil","migrationProfile",LevelService.getLevels(Level.CODE.ContainerOut), Image.class,false, null, null ,
				"img",50,true,null,null));
		
		propertyDefinitions.add(newPropertiesDefinition(
				"Taille réelle","measuredLibrarySize",LevelService.getLevels(Level.CODE.ContainerOut,Level.CODE.Content), Integer.class, false, null,null, 
				MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_SIZE),
				MeasureUnit.find.findByCode("pb"),
				MeasureUnit.find.findByCode("pb"),
				"single",60,true,null,null));	
		
		propertyDefinitions.add(newPropertiesDefinition(
				"Qté finale frg","postFrgQuantity",LevelService.getLevels(Level.CODE.ContainerOut,Level.CODE.Content), Double.class,false, null,null, 
				MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_QUANTITY),
				MeasureUnit.find.findByCode("ng"),
				MeasureUnit.find.findByCode("ng"),
				"single",70,true,null,null));
		
		/*propertyDefinitions.add(newPropertiesDefinition(
				"Volume final","measuredVolume",LevelService.getLevels(Level.CODE.ContainerOut,Level.CODE.Content), Double.class,
				false, null,null, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"), 
				"single",10,true,null,null));*/
		
	    /*propertyDefinitions.add(newPropertiesDefinition("Nb réparations","preCRNumber",LevelService.getLevels(Level.CODE.ContainerOut), Integer.class,
				 false ,null,null,"single",170,true,null,null));*/
	
		return propertyDefinitions;
	}
	

	// --3-- Reparation Nanopore
	private static List<PropertyDefinition> getPropertyReparationNanopore() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		
		//Containers IN
		propertyDefinitions.add(newPropertiesDefinition(
				"Qté engagée","inputQuantity",LevelService.getLevels(Level.CODE.ContainerIn), Double.class,false, null, null ,
				"single",11,true,null,null));
		
		propertyDefinitions.add(newPropertiesDefinition(
				"Nb réparations","reparationNumber",LevelService.getLevels(Level.CODE.ContainerIn), Integer.class,false, null, null ,
				"single",12,true,null,null));
		
		return propertyDefinitions;
	}
	
	// --4-- Librairie Nanopore (ONT)
	private static List<PropertyDefinition> getPropertyLibrairieNanopore() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		
		//Containers IN
		propertyDefinitions.add(newPropertiesDefinition(
				"Volume engagé","inputVolume", LevelService.getLevels(Level.CODE.ContainerIn),Double.class,false, null,null, 
				MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),
				MeasureUnit.find.findByCode("µL"),
				MeasureUnit.find.findByCode("µL"), 
				"single",10,true, null, null));
		
		propertyDefinitions.add(newPropertiesDefinition(
				"Qté engagée","libraryInputQuantity", LevelService.getLevels(Level.CODE.ContainerIn, Level.CODE.Content),Double.class,false, null,null, 
				MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_QUANTITY),
				MeasureUnit.find.findByCode("ng"),
				MeasureUnit.find.findByCode("ng"), 
				"single",11,true, null, null));
		
		//Containers OUT
		propertyDefinitions.add(newPropertiesDefinition(
				"Tag","tag", LevelService.getLevels(Level.CODE.ContainerOut,Level.CODE.Content),String.class,false,null, getTagNanopore(),
				"single",13,true,null,null));
		
		propertyDefinitions.add(newPropertiesDefinition(
				"Catégorie tag","tagCategory", LevelService.getLevels(Level.CODE.ContainerOut,Level.CODE.Content),String.class,false, null, getTagCategoriesNanopore(),
				"single",14,true,"SINGLE-INDEX",null));

		propertyDefinitions.add(newPropertiesDefinition(
				"Profil","migrationProfile", LevelService.getLevels(Level.CODE.ContainerOut),Image.class,false, null,null, 
				"img",40,true,null,null));	

		propertyDefinitions.add(newPropertiesDefinition(
				"Taille","measuredSize",LevelService.getLevels(Level.CODE.ContainerOut,Level.CODE.Content), Integer.class, false, null,null, 
				MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_SIZE),
				MeasureUnit.find.findByCode("pb"),
				MeasureUnit.find.findByCode("pb"),
				"single",50,true,null,null));	
		
		propertyDefinitions.add(newPropertiesDefinition(
				"Conc. finale Ligation","ligationConcentration", LevelService.getLevels(Level.CODE.ContainerOut,Level.CODE.Content),Double.class,true, null, null, 
				MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_CONCENTRATION),
				MeasureUnit.find.findByCode("ng/µl"),
				MeasureUnit.find.findByCode("ng/µl"), 
				"single",60,true, null, null));
		
		propertyDefinitions.add(newPropertiesDefinition(
				"Qté finale Ligation","ligationQuantity", LevelService.getLevels(Level.CODE.ContainerOut,Level.CODE.Content),Double.class,false, null, null, 
				MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_QUANTITY),
				MeasureUnit.find.findByCode("ng"),
				MeasureUnit.find.findByCode("ng"),
				"single",70,true, null, null)); 
		
		return propertyDefinitions;
	}
	
	// --5-- DepotNanopore
	private static List<PropertyDefinition> getPropertyDepotNanopore() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		
		//Experiments
		propertyDefinitions.add(newPropertiesDefinition(
				"Date réelle de dépôt", "runStartDate", LevelService.getLevels(Level.CODE.Experiment), Date.class,true, null,null,
				"single",300,true,null,null));
		
		//Containers IN
		// Unite a verifier
		//1er tableau
		propertyDefinitions.add(newPropertiesDefinition(
				"Quantité déposée","loadingQuantity",LevelService.getLevels(Level.CODE.ContainerIn,Level.CODE.Content), Double.class,true, "N", null,  
				MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_QUANTITY),
				MeasureUnit.find.findByCode("ng"),
				MeasureUnit.find.findByCode("ng"),
				"single",8,false, null,"1"));
		
		//2eme tableau
		propertyDefinitions.add(newPropertiesDefinition(
				"Date creation","loadingReport.creationDate",LevelService.getLevels(Level.CODE.ContainerIn, Level.CODE.Content), Date.class,false, null,null, 
				"object_list",600,true,null,null));
		
		propertyDefinitions.add(newPropertiesDefinition(
				"Heure dépot","loadingReport.hour",LevelService.getLevels(Level.CODE.ContainerIn, Level.CODE.Content), String.class,false, null,null,
				"object_list",601,true,null,null));
		
		propertyDefinitions.add(newPropertiesDefinition(
				"Temps","loadingReport.time",LevelService.getLevels(Level.CODE.ContainerIn, Level.CODE.Content), Long.class,false,null,null, 
				MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_TIME),
				MeasureUnit.find.findByCode("h"),
				MeasureUnit.find.findByCode("h"),
				"object_list",602,true, null, null));
		
		propertyDefinitions.add(newPropertiesDefinition(
				"Volume","loadingReport.volume",LevelService.getLevels(Level.CODE.ContainerIn, Level.CODE.Content), Double.class,false, null, null, 
				MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),
				MeasureUnit.find.findByCode("µL"),
				MeasureUnit.find.findByCode("µL"),
				"object_list",603,true, null, null));
		
		//Containers OUT
		//3ème tableau	
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition(
				"Groupe","qcFlowcell.group",LevelService.getLevels(Level.CODE.ContainerOut,Level.CODE.Content), String.class,false, null,null, 
				"object_list",700,false,null,null));
		
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition(
				"Nb total pores actifs à réception","qcFlowcell.preLoadingNbActivePores",LevelService.getLevels(Level.CODE.ContainerOut,Level.CODE.Content), 
				Integer.class, false,null,null, 
				"object_list",701,true, null,null));
		
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition(
				"Nb total pores actifs lors du dépôt","qcFlowcell.postLoadingNbActivePores",LevelService.getLevels(Level.CODE.ContainerOut,Level.CODE.Content), 
				Integer.class,false,null,null, 
				"object_list",702,true, null,null));

		return propertyDefinitions;
	}
	
	// -- SizingNanopore
	private static List<PropertyDefinition> getPropertySizingNanopore() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		
		//Containers IN
// ???? STRING pour une taille ????		
		propertyDefinitions.add(newPropertiesDefinition(
				"Taille théorique sizing","expectedSize",LevelService.getLevels(Level.CODE.ContainerIn), String.class,false, null ,null, 
				"single",12,true,null,null));
// ???? STRING pour une duree ????			
		propertyDefinitions.add(newPropertiesDefinition(
				"Temps migration","migrationTime",LevelService.getLevels(Level.CODE.ContainerIn), String.class,false, null ,null, 
				"single",11,true,null,null));
		
		//Containers OUT
		propertyDefinitions.add(newPropertiesDefinition(
				"Profil","migrationProfile", LevelService.getLevels(Level.CODE.ContainerOut),Image.class,false, null,null,
				"img",51,true,null,null));	
			
	    propertyDefinitions.add(newPropertiesDefinition(
				"Taille sizing (av. lib)","measuredSizePostSizing",LevelService.getLevels(Level.CODE.ContainerOut,Level.CODE.Content), Integer.class, false, null,null, 
				MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_SIZE),
				MeasureUnit.find.findByCode("pb"),
				MeasureUnit.find.findByCode("pb"),
				"single",60,false,null,null));	
				 
		propertyDefinitions.add(newPropertiesDefinition("Conc. ligation (ap. sizing)","ligationConcentrationPostSizing",LevelService.getLevels(Level.CODE.ContainerOut,Level.CODE.Content), 
				Double.class,false, null,null, 
				MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_CONCENTRATION),
				MeasureUnit.find.findByCode("ng/µl"),
				MeasureUnit.find.findByCode("ng/µl"),
				"single",61,false, null, null));
		
		propertyDefinitions.add(newPropertiesDefinition("Qté ligation (ap. sizing)","ligationQuantityPostSizing",LevelService.getLevels(Level.CODE.ContainerOut,Level.CODE.Content), 
				Double.class,false, null,null, 
				MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_QUANTITY),
				MeasureUnit.find.findByCode("ng"),
				MeasureUnit.find.findByCode("ng"),
				"single",62,false, null, null));

		return propertyDefinitions;

	}

	
	/////////// PROCESSUS PROPERTIES /////////////////////////////////////////
	
	public static List<PropertyDefinition> getPropertyDefinitionsNanoporeFragmentation() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();		
		
		propertyDefinitions.add(newPropertiesDefinition(
				"Type processus banque","libProcessTypeCode",LevelService.getLevels(Level.CODE.Process,Level.CODE.Content),String.class,true,null, 
				getLibProcessTypeCodeValues(),
				"single" ,1,true, "ONT",null));
		
		propertyDefinitions.add(newPropertiesDefinition(
				"Taille banque souhaitée","librarySize",LevelService.getLevels(Level.CODE.Process, Level.CODE.Content),Integer.class,true,null, null, 
				MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_SIZE),
				MeasureUnit.find.findByCode("kb"),
				MeasureUnit.find.findByCode("kb"),
				"single",2,true,null,null));
		
		return propertyDefinitions;
	}
	
	
	public static List<PropertyDefinition> getPropertyDefinitionsNanoporeLibrary() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition(
				"Type processus banque","libProcessTypeCode",LevelService.getLevels(Level.CODE.Process,Level.CODE.Content),String.class,true, null,
				getLibProcessTypeCodeValues(),
				"single",1, true,"ONT",null));
		
		return propertyDefinitions;
	}

	
	/////////////// List values /////////////////////////////////////////////////////////
	
	private static List<Value> getLibProcessTypeCodeValues(){
        List<Value> values = new ArrayList<>();
         values.add(DescriptionFactory.newValue("ONT","ONT - Nanopore"));
         return values;
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

}
