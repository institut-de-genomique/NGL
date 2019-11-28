package services.description.declaration.cns;

import static services.description.DescriptionFactory.newExperimentType;
import static services.description.DescriptionFactory.newExperimentTypeNode;
import static services.description.DescriptionFactory.newPropertiesDefinition;

import java.awt.Image;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import models.laboratory.common.description.Level;
import models.laboratory.common.description.MeasureCategory;
import models.laboratory.common.description.MeasureUnit;
import models.laboratory.common.description.PropertyDefinition;
import models.laboratory.common.description.Value;
import models.laboratory.common.description.dao.MeasureUnitDAO;
import models.laboratory.experiment.description.ExperimentCategory;
import models.laboratory.experiment.description.ExperimentType;
import models.laboratory.experiment.description.dao.ExperimentCategoryDAO;
import models.laboratory.processes.description.ProcessCategory;
import models.laboratory.processes.description.ProcessType;
import models.laboratory.processes.description.dao.ProcessCategoryDAO;
import services.description.Constants;
import services.description.DescriptionFactory;
import services.description.common.LevelService;
import services.description.common.MeasureService;
import services.description.declaration.AbstractDeclaration;

public class MetaBarCoding extends AbstractDeclaration {

	@Override
	protected List<ExperimentType> getExperimentTypePROD() {	
		return null;
	}
	
	@Override
	protected List<ExperimentType> getExperimentTypeCommon() {
		List<ExperimentType> l = new ArrayList<>();
		ExperimentCategoryDAO ecfind = ExperimentCategory.find.get();

		l.add(newExperimentType("Ext to MetaBarcoding (sans sizing)","ext-to-tag-pcr-and-dna-library",null,-1,
				ecfind.findByCode(ExperimentCategory.CODE.voidprocess.name()), null, null,"OneToOne",
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));

		l.add(newExperimentType("Ext to MetaBarcoding avec sizing (gel)","ext-to-tag-pcr-and-dna-library-with-sizing",null,-1,
				ecfind.findByCode(ExperimentCategory.CODE.voidprocess.name()), null, null,"OneToOne",
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));

		l.add(newExperimentType("Ext to MetaBarcoding double PCR (nested)","ext-to-double-tag-pcr-and-dna-library",null,-1,
				ecfind.findByCode(ExperimentCategory.CODE.voidprocess.name()), null, null,"OneToOne",
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));


		l.add(newExperimentType("Tags-PCR","tag-pcr","TAG",750,
				ecfind.findByCode(ExperimentCategory.CODE.transformation.name()), getPropertyDefinitionsTagPCR(),
				getInstrumentUsedTypes("thermocycler"),"OneToOne", getSampleTypes("amplicon"),true,
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));

		l.add(newExperimentType("Bq DNA Illumina indexée","dna-illumina-indexed-library","LIB",850,
				ecfind.findByCode(ExperimentCategory.CODE.transformation.name()), getPropertyDefinitionsBqDNAIlluminaIndexedLibrary(),
				getInstrumentUsedTypes("hand","biomek-fx"),"OneToOne", null,true,
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));

		l.add(newExperimentType("Amplification/PCR","pcr-amplification-and-purification","PCR",900,
				ecfind.findByCode(ExperimentCategory.CODE.transformation.name()), getPropertyDefinitionsAmpliPurif(),
				getInstrumentUsedTypes("thermocycler"),"OneToOne", null,true,
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));

		l.add(newExperimentType("Amplification/PCR + indexing","indexing-and-pcr-amplification","PCR",900,
				ecfind.findByCode(ExperimentCategory.CODE.transformation.name()), getPropertyDefinitionsAmpliPurifIndexing(),
				getInstrumentUsedTypes("thermocycler"),"OneToOne", null,true,
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));



		l.add(newExperimentType("Sizing (gel)","sizing","SIZ",950,
				ecfind.findByCode(ExperimentCategory.CODE.transformation.name()), getPropertyDefinitionsSizingDEV(),
				getInstrumentUsedTypes("hand"),"OneToMany", null,true,
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));


		l.add(newExperimentType("Spri Select","spri-select","SS",951,
				ecfind.findByCode(ExperimentCategory.CODE.transformation.name()), getPropertyDefinitionsSpriSelect(),
				getInstrumentUsedTypes("hand", "biomek-fx"),"OneToOne", null,true,
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));


		return l;
	}

	@Override
	public List<ExperimentType> getExperimentTypeDEV() {
		return null;
	}

	@Override
	public List<ExperimentType> getExperimentTypeUAT() {
		return null;
	}

	@Override
	protected List<ProcessType> getProcessTypePROD() {
		return null;
	}
	
    @Override
    protected List<ProcessType> getProcessTypeCommon() {
            List<ProcessType> l = new ArrayList<>();
    		ProcessCategoryDAO pcfind = ProcessCategory.find.get();

            l.add(DescriptionFactory.newProcessType("MetaBarcoding avec sizing (gel)", "tag-pcr-and-dna-library-with-sizing", pcfind.findByCode("library"), 11,
                            getPropertyMetaBarCodingSizing(),
                            Arrays.asList(getPET("ext-to-tag-pcr-and-dna-library-with-sizing",-1)
                            				,getPET("wga-amplification",-1)
                                            ,getPET("dna-rna-extraction",-1)
                                            ,getPET("dna-extraction",-1)
                                    		,getPET("cdna-synthesis",-1)
                                            ,getPET("tag-pcr",0)
                                            ,getPET("dna-illumina-indexed-library",1)
                                            ,getPET("pcr-amplification-and-purification",2)
                                            ,getPET("sizing",3)
                                            ,getPET("solution-stock",4)
                                            ,getPET("prepa-flowcell",5)
                                            ,getPET("prepa-fc-ordered",5)
                                            ,getPET("illumina-depot",6)),
                                            getExperimentTypes("tag-pcr").get(0), getExperimentTypes("illumina-depot").get(0), getExperimentTypes("ext-to-tag-pcr-and-dna-library-with-sizing").get(0),
                                            DescriptionFactory.getInstitutes(Constants.CODE.CNS)));

            l.add(DescriptionFactory.newProcessType("MetaBarcoding (sans sizing)", "tag-pcr-and-dna-library", pcfind.findByCode("library"), 12,
                            getPropertyMetaBarCodingWithoutSizing(),
                            Arrays.asList(getPET("ext-to-tag-pcr-and-dna-library",-1)
                            				,getPET("wga-amplification",-1)
                                    		,getPET("dna-rna-extraction",-1)
                                    		,getPET("dna-extraction",-1)
                                    		,getPET("cdna-synthesis",-1)
                                            ,getPET("tag-pcr",0)
                                            ,getPET("dna-illumina-indexed-library",1)
                                            ,getPET("pcr-amplification-and-purification",2)
                                            ,getPET("solution-stock",3)
                                            ,getPET("prepa-flowcell",4)
                                            ,getPET("prepa-fc-ordered",4)
                                            ,getPET("illumina-depot",5)),
                                            getExperimentTypes("tag-pcr").get(0), getExperimentTypes("illumina-depot").get(0), getExperimentTypes("ext-to-tag-pcr-and-dna-library").get(0),
                                            DescriptionFactory.getInstitutes(Constants.CODE.CNS)));

            l.add(DescriptionFactory.newProcessType("MetaBarcoding double PCR (nested)", "double-tag-pcr-and-dna-library", pcfind.findByCode("library"), 13,
                    getPropertyMetaBarCodingDouble(),
                    Arrays.asList(getPET("ext-to-double-tag-pcr-and-dna-library",-1)
                    				,getPET("wga-amplification",-1)
                                    ,getPET("dna-rna-extraction",-1)
                                    ,getPET("dna-extraction",-1)
                            		,getPET("cdna-synthesis",-1)
                                    ,getPET("tag-pcr",0)
                                    ,getPET("tag-pcr",1)
                                    ,getPET("dna-illumina-indexed-library",2)
                                    ,getPET("pcr-amplification-and-purification",3)
                                    ,getPET("solution-stock",4)
                                    ,getPET("prepa-flowcell",5)
                                    ,getPET("prepa-fc-ordered",5)
                                    ,getPET("illumina-depot",6)),
                                    getExperimentTypes("tag-pcr").get(0), getExperimentTypes("illumina-depot").get(0), getExperimentTypes("ext-to-double-tag-pcr-and-dna-library").get(0),
                                    DescriptionFactory.getInstitutes(Constants.CODE.CNS)));

            
            return l;
    }

	
	@Override
	public List<ProcessType> getProcessTypeDEV() {
		return null;
	}



	@Override
	public List<ProcessType> getProcessTypeUAT() {
		return null;
	}

	@Override
	protected void getExperimentTypeNodePROD() {
	}
	
	@Override
	public void getExperimentTypeNodeCommon() {
		
		save(newExperimentTypeNode("ext-to-tag-pcr-and-dna-library", getExperimentTypes("ext-to-tag-pcr-and-dna-library").get(0), false, false, false, null, null, null, null));
		save(newExperimentTypeNode("ext-to-double-tag-pcr-and-dna-library", getExperimentTypes("ext-to-double-tag-pcr-and-dna-library").get(0), false, false, false, null, null, null, null));
		
		save(newExperimentTypeNode("ext-to-tag-pcr-and-dna-library-with-sizing", getExperimentTypes("ext-to-tag-pcr-and-dna-library-with-sizing").get(0), false, false, false, null, null, null, null));
		save(newExperimentTypeNode("tag-pcr",getExperimentTypes("tag-pcr").get(0),true, true,false,getExperimentTypeNodes("wga-amplification", "dna-rna-extraction","dna-extraction","cdna-synthesis", "ext-to-tag-pcr-and-dna-library","ext-to-tag-pcr-and-dna-library-with-sizing","ext-to-double-tag-pcr-and-dna-library")
				,null,getExperimentTypes("fluo-quantification","chip-migration"),getExperimentTypes("pool","tubes-to-plate","plate-to-tubes")));
		
		save(newExperimentTypeNode("tag-pcr-nested",getExperimentTypes("tag-pcr").get(0),true, true,false,getExperimentTypeNodes("tag-pcr")
				,null,getExperimentTypes("fluo-quantification","chip-migration"),getExperimentTypes("pool","tubes-to-plate","plate-to-tubes")));
		
		
		save(newExperimentTypeNode("dna-illumina-indexed-library",getExperimentTypes("dna-illumina-indexed-library").get(0),true, true,false,getExperimentTypeNodes("ext-to-dna-illumina-indexed-library-process","ext-to-dna-illumina-indexed-lib-sizing-process","ext-to-dna-illumina-indexed-lib-spri-select-process","tag-pcr","tag-pcr-nested","fragmentation","ext-to-pcr-free-process-from-frag")
				,getExperimentTypes("post-pcr-ampure"),getExperimentTypes("fluo-quantification"),getExperimentTypes("pool","tubes-to-plate","plate-to-tubes")));
		save(newExperimentTypeNode("pcr-amplification-and-purification",getExperimentTypes("pcr-amplification-and-purification").get(0),true, true,false,getExperimentTypeNodes("ext-to-ampli-spri-select-stk-illumina-depot","ext-to-ampli-sizing-stk-illumina-depot", "ext-to-ampli-stk-illumina-depot","dna-illumina-indexed-library","rna-illumina-indexed-library")
				,getExperimentTypes("post-pcr-ampure"),getExperimentTypes("fluo-quantification","chip-migration"),getExperimentTypes("pool","tubes-to-plate","plate-to-tubes")));
		save(newExperimentTypeNode("indexing-and-pcr-amplification",getExperimentTypes("indexing-and-pcr-amplification").get(0),true, true,false,getExperimentTypeNodes("ext-to-ampli-spri-select-stk-illumina-depot","rna-illumina-library")
				,getExperimentTypes("post-pcr-ampure"),getExperimentTypes("fluo-quantification","chip-migration"),getExperimentTypes("pool","tubes-to-plate","plate-to-tubes")));
		
		save(newExperimentTypeNode("sizing",getExperimentTypes("sizing").get(0),true, true,false,getExperimentTypeNodes("ext-to-ampure-sizing-stk-illumina-depot", "ext-to-sizing-stk-illumina-depot", "pcr-amplification-and-purification")
				,null,getExperimentTypes("fluo-quantification","chip-migration","qpcr-quantification"),getExperimentTypes("pool","tubes-to-plate","plate-to-tubes")));
		
		save(newExperimentTypeNode("spri-select",getExperimentTypes("spri-select").get(0),true, true,false,getExperimentTypeNodes("ext-to-spri-select-stk-illumina-depot","pcr-amplification-and-purification")
				,getExperimentTypes("post-pcr-ampure"),getExperimentTypes("fluo-quantification","chip-migration","qpcr-quantification"),getExperimentTypes("pool","tubes-to-plate","plate-to-tubes")));
	
		/*
		ExperimentTypeNode etn = getExperimentTypeNodes("tag-pcr").get(0);
	    etn.previousExperimentTypeNodes=getExperimentTypeNodes("dna-rna-extraction", "cdna-synthesis", "tag-pcr","ext-to-tag-pcr-and-dna-library","ext-to-tag-pcr-and-dna-library-with-sizing","ext-to-double-tag-pcr-and-dna-library");
	    etn.update();
		*/
	}

	@Override
	public void getExperimentTypeNodeDEV() {
		/*
		newExperimentTypeNode("sizing",getExperimentTypes("sizing").get(0),true, true,false,getExperimentTypeNodes("pcr-amplification-and-purification")
				,null,getExperimentTypes("fluo-quantification","chip-migration","qpcr-quantification"),getExperimentTypes("pool","tubes-to-plate","plate-to-tubes")).save();
		 */
		
	}

	@Override
	public void getExperimentTypeNodeUAT() {
	}



	private List<PropertyDefinition> getPropertyDefinitionsSpriSelect() {
		MeasureUnitDAO     mufind = MeasureUnit.find.get();

		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();

		propertyDefinitions.add(newPropertiesDefinition("Volume engagé", "inputVolume", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, null,
				null, MeasureCategory.find.get().findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),mufind.findByCode( "µL"),mufind.findByCode( "µL"),"single", 13, true, null,null));

		propertyDefinitions.add(newPropertiesDefinition("Taille théorique sizing", "expectedSize", LevelService.getLevels(Level.CODE.ContainerOut,Level.CODE.Content), String.class, true, null,
				DescriptionFactory.newValues("ss0.6/0.53","ss0.7/0.58", "autre"),  MeasureCategory.find.get().findByCode(MeasureService.MEASURE_CAT_CODE_SIZE),mufind.findByCode( "pb"),mufind.findByCode( "pb"),"single", 14, true, null,null));

		propertyDefinitions.add(newPropertiesDefinition("Label de travail", "workName", LevelService.getLevels(Level.CODE.ContainerOut,Level.CODE.Container), String.class, false, null, null,
				"single", 100, true, null,null));

		return propertyDefinitions;
	}


	private List<PropertyDefinition> getPropertyDefinitionsSizingDEV() {
		MeasureUnitDAO     mufind = MeasureUnit.find.get();
        List<PropertyDefinition> propertyDefinitions = new ArrayList<>();

        propertyDefinitions.add(newPropertiesDefinition("Volume engagé", "inputVolume", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, null,
                        null, MeasureCategory.find.get().findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),mufind.findByCode( "µL"),mufind.findByCode( "µL"),"single", 13, true, null,null));

        propertyDefinitions.add(newPropertiesDefinition("Taille théorique sizing", "expectedSize", LevelService.getLevels(Level.CODE.ContainerOut,Level.CODE.Content), String.class, true, null,
                        DescriptionFactory.newValues("280-310 (F300)", "400-550 (ITS2)", "450-550 (W500)","550-650 (W600)", "500-650","550-700 (ITS2)", "600-700 (W700)","650-750 (W700)","650-700 (W700)", "650-800", "700-800 (W800)","750-800", "autre"),  MeasureCategory.find.get().findByCode(MeasureService.MEASURE_CAT_CODE_SIZE),mufind.findByCode( "pb"),mufind.findByCode( "pb"),"single", 14, true, null,null));
        				
        propertyDefinitions.add(newPropertiesDefinition("Label de travail", "workName", LevelService.getLevels(Level.CODE.ContainerOut,Level.CODE.Container), String.class, false, null, null,
                        "single", 100, true, null,null));

        return propertyDefinitions;
	}

	private List<PropertyDefinition> getPropertyDefinitionsAmpliPurifIndexing() {
		List<PropertyDefinition> propertyDefinitions = getPropertyDefinitionsAmpliPurif();
		
		propertyDefinitions.add(newPropertiesDefinition("Tag", "tag", LevelService.getLevels(Level.CODE.ContainerOut,Level.CODE.Content), String.class, true, null, 
				null, null,null,null,"single", 55, true, null,null));

		propertyDefinitions.add(newPropertiesDefinition("Catégorie de Tag", "tagCategory", LevelService.getLevels(Level.CODE.ContainerOut,Level.CODE.Content), String.class, true, null, 
				getTagCategoriesIllumina(), null,null,null,"single", 56, false, null,null));

		
		return propertyDefinitions;
	}


	private List<PropertyDefinition> getPropertyDefinitionsAmpliPurif() {
		MeasureUnitDAO     mufind = MeasureUnit.find.get();
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();

		propertyDefinitions.add(newPropertiesDefinition("Volume engagé", "inputVolume", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, null, 
				null, MeasureCategory.find.get().findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),mufind.findByCode( "µL"),mufind.findByCode( "µL"),"single", 12, true, null,null));
		propertyDefinitions.add(newPropertiesDefinition("Quantité engagée","inputQuantity", LevelService.getLevels(Level.CODE.ContainerIn),Double.class, false, null,
				null,MeasureCategory.find.get().findByCode(MeasureService.MEASURE_CAT_CODE_QUANTITY),mufind.findByCode( "ng"),mufind.findByCode( "ng"),"single",13, true,null,null));
		propertyDefinitions.add(newPropertiesDefinition("Nb de PCR", "nbPCR", LevelService.getLevels(Level.CODE.ContainerIn), Integer.class, true, null, 
				null,  null, null, null,"single", 15, true, null,null));
		propertyDefinitions.add(newPropertiesDefinition("Volume / PCR", "PCRvolume", LevelService.getLevels(Level.CODE.ContainerOut), Double.class, true, null, 
				null, MeasureCategory.find.get().findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),mufind.findByCode( "µL"),mufind.findByCode( "µL"),"single", 50, true, null,null));
		
	
		propertyDefinitions.add(newPropertiesDefinition("DNA polymerase", "dnaPolymerase", LevelService.getLevels(Level.CODE.Experiment), String.class, false, null, 
				DescriptionFactory.newValues("taq Q5","taq Kapa","taq Phusion","autre"), null, null, null,"single", 1, false, null,null));
		
		propertyDefinitions.add(newPropertiesDefinition("Nb cycles", "nbCycles", LevelService.getLevels(Level.CODE.Experiment), Integer.class, true, null, null, 
				"single", 3, true, null,null));
		
		return propertyDefinitions;
	}

	private List<PropertyDefinition> getPropertyDefinitionsBqDNAIlluminaIndexedLibrary() {
		MeasureUnitDAO     mufind = MeasureUnit.find.get();
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();

		propertyDefinitions.add(newPropertiesDefinition("Volume engagé",            "inputVolume",          LevelService.getLevels(Level.CODE.ContainerIn),                        Double.class, true,  null, null,                       MeasureCategory.find.get().findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),   mufind.findByCode( "µL"),           mufind.findByCode( "µL"), "single", 12, true,  null, null));
	//	propertyDefinitions.add(newPropertiesDefinition("Quantité engagée",         "inputQuantity",        LevelService.getLevels(Level.CODE.ContainerIn),                        Double.class, false, null, null,                       MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_QUANTITY),       MeasureUnit.find.findByCode( "ng"), MeasureUnit.find.findByCode( "ng"),"single",13, true,null,null));
		propertyDefinitions.add(newPropertiesDefinition("Qté engagée dans bq",      "libraryInputQuantity", LevelService.getLevels(Level.CODE.ContainerIn,  Level.CODE.Content),   Double.class, false, null, null,                       MeasureCategory.find.get().findByCode(MeasureService.MEASURE_CAT_CODE_QUANTITY), mufind.findByCode( "ng"),           mufind.findByCode( "ng"), "single", 13, true,  null, null));
		propertyDefinitions.add(newPropertiesDefinition("Dilution des adaptateurs", "adapterDilution",      LevelService.getLevels(Level.CODE.ContainerOut),                       String.class, false, null, getAdapterDilutionValues(), null,                                                                            null,                               null,                     "single", 14, true, null, null));
		propertyDefinitions.add(newPropertiesDefinition("Tag",                      "tag",                  LevelService.getLevels(Level.CODE.ContainerOut, Level.CODE.Content),   String.class, true,  null, null,                       null,                                                                            null,                               null,                     "single", 15, true,  null, null));
		propertyDefinitions.add(newPropertiesDefinition("Catégorie de Tag",         "tagCategory",          LevelService.getLevels(Level.CODE.ContainerOut, Level.CODE.Content),   String.class, true,  null, getTagCategoriesIllumina(), null,                                                                            null,                               null,                     "single", 16, false, null, null));
		
		
		//Experiments
		propertyDefinitions.add(newPropertiesDefinition("Schéma de manips",         "experimentPlan",       LevelService.getLevels(Level.CODE.Experiment),                         Image.class, false, null, null , "img", 16, true, null, null));

		return propertyDefinitions;
	}


	private List<Value> getAdapterDilutionValues() {
	    return DescriptionFactory.newValues("1/80",
	                                        "1/40",
	                                        "1/40 2µl",
	                                        "1/25",
	                                        "1/15",
	                                        "1/15 2µl",
	                                        "1/12",
	                                        "1/8 2µl",
	                                        "1/5",
	                                        "1/4",
	                                        "1/2");
    }

    private List<PropertyDefinition> getPropertyDefinitionsTagPCR() {
		MeasureUnitDAO     mufind = MeasureUnit.find.get();
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();

		propertyDefinitions.add(newPropertiesDefinition("Volume engagé",    "inputVolume",    		LevelService.getLevels(Level.CODE.ContainerIn),  						Double.class,  true,  null, null, MeasureCategory.find.get().findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),   mufind.findByCode( "µL"), mufind.findByCode( "µL"), "single", 12, true,  null, null));
		propertyDefinitions.add(newPropertiesDefinition("Quantité engagée", "inputQuantity",  		LevelService.getLevels(Level.CODE.ContainerIn),  						Double.class,  false, null, null, MeasureCategory.find.get().findByCode(MeasureService.MEASURE_CAT_CODE_QUANTITY), mufind.findByCode( "ng"), mufind.findByCode( "ng"), "single", 13, true,  null, null));
		propertyDefinitions.add(newPropertiesDefinition("Nb de PCR",        "nbPCR",          		LevelService.getLevels(Level.CODE.ContainerIn),  						Integer.class, true,  null, null, null,                                                                            null,                     null,                     "single", 15, true,  null, null));
		propertyDefinitions.add(newPropertiesDefinition("Tag 2ndaire",      "secondaryTag",   		LevelService.getLevels(Level.CODE.ContainerOut, Level.CODE.Content),  	String.class,  false, null, null, null,                                                                            null,                     null,                     "single", 16, true, null, null));
		propertyDefinitions.add(newPropertiesDefinition("Cat. Tag 2ndaire", "secondaryTagCategory", LevelService.getLevels(Level.CODE.ContainerOut, Level.CODE.Content),  	String.class,  false, null, null, null,                                                                            null,                     null,                     "single", 17, false, null, null));
		propertyDefinitions.add(newPropertiesDefinition("Sample Type",      "sampleTypeCode", 		LevelService.getLevels(Level.CODE.ContainerOut), 						String.class,  true,  "N",  null,                                                                                                                                      "single", 18, false, null, null));
		propertyDefinitions.add(newPropertiesDefinition("Projet",           "projectCode",    		LevelService.getLevels(Level.CODE.ContainerOut), 						String.class,  true,  null, null, null,                                                                            null,                     null,                     "single", 20, false, null, null));
		propertyDefinitions.add(newPropertiesDefinition("Echantillon",      "sampleCode",     		LevelService.getLevels(Level.CODE.ContainerOut), 						String.class,  true,  null, null, null,                                                                            null,                     null,                     "single", 25, false, null, null));

		//Experiments
		propertyDefinitions.add(newPropertiesDefinition("schéma de manips","experimentPlan", 		LevelService.getLevels(Level.CODE.Experiment), 							Image.class, false, null, null, "img", 16, true, null, null));
		
		// Lists of values
		List<Value> dnaPolymeraseValues = DescriptionFactory.newValues("taq Phusion",
																		"FastStart Taq",
																		"Advantage 2",
                														"HotStarTaq",
                														"autre");
		List<Value> amplificationPrimersValues = DescriptionFactory.newValues("16S FL 27F/1492R + Fuhrman primers",
		                                                                      "16S FL 27F/1492R",
		                                                                      "16S FL 27F/1390R",
		                                                                      "16S FL 27F/1390R + Fuhrman primers",
		                                                                      "16S FL 27F/1492R + 16S V4V5 515F/926R-FUSION",
		                                                                      "16S V1V2V3 Prok Sneed2015 27F/519Rmodbio",
		                                                                      "16S V4 Prok 515FF/806R",
		                                                                      "16S V4V5 Archae 517F/958R",
		                                                                      "16S V4V5 515F/926R-FUSION",
		                                                                      "16S V5V6 Prok 784F/1061R",
		                                                                      "16S V3 F342/V4R802",
		                                                                      "16S V3V4 341F/805R_Cave",
		                                                                      "18S V1V2 Metazoaire SSUF04/SSURmod",
		                                                                      "18S V4 Euk V4f (TAReukF1)/V4r (TAReukR)",
		                                                                      "18S V9 1389F/1510R",
		                                                                      "18S V9 1389F/1510R-FUSION",
		                                                                      "18S 0067a deg/NSR399_Cave",
		                                                                      "COI primers m1COIintF/jgHCO2198",
		                                                                      "CP23S primers",
		                                                                      "Fuhrman primers",
		                                                                      "ITS2/SYM_VAR_5.8S2/SYM_VAR_REV",
		                                                                      "ITS2/SYM_VAR_5.8S2/SYM_VAR_REV-FUSION",
		                                                                      "ITSD/ITS2REV",
		                                                                      "ITSintfor2/ITS-Reverse",
		                                                                      "ITS3f/ITS4_KYO1",
		                                                                      "ITS3 KYO2/ITS4_Cave",
		                                                                      "Amp 48-1",
		                                                                      "Amp 48-2",
		                                                                      "MamP007 F/MamP007 R",
		                                                                      "Plant-g F/Plant-g R",
		                                                                      "Arch 519F/915R_Cave",
		                                                                      "autre");
		List<Value> targetedRegionValues = DescriptionFactory.newValues("16S_V4V5",
		                                                                "18S_V9",
		                                                                "16S_Full Length + 16S_V4V5",
		                                                                "ITS2",
		                                                                "ITS2_Fungal",
		                                                                "CP23S",
		                                                                "18S_V4",
		                                                                "COI",
		                                                                "16S_V1V2V3",
		                                                                "16S_V3V4",
		                                                                "16S_V3V4_Bact",
		                                                                "16S_V3V4_Archeal",
		                                                                "16S_V5V6",
		                                                                "18S_V1V2",
		                                                                "18S_microeuk",
		                                                                "16S_V4",
		                                                                "16SFL",
		                                                                "16S_mito",
		                                                                "P6loop_chloro_trnL",
		                                                                "Multi-Amplicons");
		
        propertyDefinitions.add(newPropertiesDefinition("DNA polymerase",                   "dnaPolymerase",          LevelService.getLevels(Level.CODE.Experiment),                                        String.class,  false, null, dnaPolymeraseValues,        null, null, null, "single", 1,  false, "taq Phusion", null));
        propertyDefinitions.add(newPropertiesDefinition("Amorces",                          "amplificationPrimers",   LevelService.getLevels(Level.CODE.Experiment, Level.CODE.Content, Level.CODE.Sample), String.class,  true,  null, amplificationPrimersValues, null, null, null, "single", 2,  true,  null,          null));
        propertyDefinitions.add(newPropertiesDefinition("Région ciblée",                    "targetedRegion",         LevelService.getLevels(Level.CODE.Experiment, Level.CODE.Content, Level.CODE.Sample), String.class,  true,  null, targetedRegionValues,       null, null, null, "single", 3,  true,  null,          null));
		propertyDefinitions.add(newPropertiesDefinition("Nb cycles",                        "nbCycles",               LevelService.getLevels(Level.CODE.Experiment),                                        Integer.class, true,  null, null,                                         "single", 4,  true,  null,          null));
		propertyDefinitions.add(newPropertiesDefinition("Code éch. témoin négatif PCR (1)", "tagPcrBlank1SampleCode", LevelService.getLevels(Level.CODE.ContainerOut, Level.CODE.Content),                  String.class,  true,  "F",  null,                       null, null, null, "single", 26, false, null,          null));
		propertyDefinitions.add(newPropertiesDefinition("Code éch. témoin négatif PCR (2)", "tagPcrBlank2SampleCode", LevelService.getLevels(Level.CODE.ContainerOut, Level.CODE.Content),                  String.class,  true,  "F",  null,                       null, null, null, "single", 27, false, null,          null));

		return propertyDefinitions;
	}
	
	private List<PropertyDefinition> getPropertyMetaBarCodingDouble() {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();	
		propertyDefinitions.add(newPropertiesDefinition("Type processus Banque", "libProcessTypeCode", LevelService.getLevels(Level.CODE.Process,Level.CODE.Content), String.class, true, null, getBanqueProcessTypeMetaTC(), 
				null,null,null,"single", 13, true, "TC", null));
		propertyDefinitions.add(newPropertiesDefinition("Amorces", "amplificationPrimers", LevelService.getLevels(Level.CODE.Process), String.class, true, null, 
				DescriptionFactory.newValues("16S FL + Fuhrman primers"), null,null,null,"single", 14, true, "16S primer + Fuhrman primer", null));
		propertyDefinitions.add(newPropertiesDefinition("Région ciblée", "targetedRegion", LevelService.getLevels(Level.CODE.Process), String.class, true, null,
				DescriptionFactory.newValues("16S_Full Length + 16S_V4V5"),	null,null,null,"single", 15, true, "16S_Full Length + 16S_V4V5", null));
		propertyDefinitions.add(newPropertiesDefinition("Taille amplicon attendue", "expectedAmpliconSize", LevelService.getLevels(Level.CODE.Process,Level.CODE.Content), String.class, true, null, 
				DescriptionFactory.newValues("411/600"),null,null,null,"single", 16, true, "411/600", null));
		
		propertyDefinitions.add(newPropertiesDefinition("Ratio ampure post-pcr", "postPcrAmpureVolume", LevelService.getLevels(Level.CODE.Process), String.class, false, null, null, 
				null,null,null,"single", 17, true, null, null));
		propertyDefinitions.addAll(RunIllumina.getIlluminaDepotProperties());
		return propertyDefinitions;
	}
	
	private List<PropertyDefinition> getPropertyMetaBarCodingWithoutSizing() {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();	
		//Prop communes avec process MetaB
		propertyDefinitions.addAll(getPropertyMetaB());
		
		//Prop spécifiques
		propertyDefinitions.add(newPropertiesDefinition("Type processus Banque", "libProcessTypeCode", LevelService.getLevels(Level.CODE.Process, Level.CODE.Content), String.class, true,  null, getBanqueProcessTypeMetaTA(), null, null, null, "single", 13, true, null, null));
		propertyDefinitions.add(newPropertiesDefinition("Ratio ampure post-pcr", "postPcrAmpureVolume", LevelService.getLevels(Level.CODE.Process),                    String.class, false, null, null,                         null, null, null, "single", 17, true, null, null));
		propertyDefinitions.addAll(RunIllumina.getIlluminaDepotProperties());
		return propertyDefinitions;
	}

	private List<PropertyDefinition> getPropertyMetaBarCodingSizing() {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		//Prop communes avec process MetaB
		propertyDefinitions.addAll(getPropertyMetaB());
				
		List<Value> sizingGoalValues = DescriptionFactory.newValues("400-550 (ITS2)", "500-650","autre");
        List<Value> sizingGoal2Values = DescriptionFactory.newValues("550-700 (ITS2)", "650-800","autre");
		
		// Prop spécifiques
        propertyDefinitions.add(newPropertiesDefinition("Type processus Banque", "libProcessTypeCode",  LevelService.getLevels(Level.CODE.Process, Level.CODE.Content), String.class, true,  null, getBanqueProcessTypeMetaTB(), null, null, null, "single", 13, true, null, null));
        propertyDefinitions.add(newPropertiesDefinition("Objectif sizing 1",     "sizingGoal",          LevelService.getLevels(Level.CODE.Process),                     String.class, true,  null, sizingGoalValues,             null, null, null, "single", 17, true, null, null));
        propertyDefinitions.add(newPropertiesDefinition("Objectif sizing 2",     "sizingGoal2",         LevelService.getLevels(Level.CODE.Process),                     String.class, false, null, sizingGoal2Values,            null, null, null, "single", 18, true, null, null));
		propertyDefinitions.add(newPropertiesDefinition("Ratio ampure post-pcr", "postPcrAmpureVolume", LevelService.getLevels(Level.CODE.Process),                     String.class, false, null, null,                         null, null, null, "single", 19, true, null, null));
		propertyDefinitions.addAll(RunIllumina.getIlluminaDepotProperties());
		return propertyDefinitions;
	}
	
	private List<PropertyDefinition> getPropertyMetaB(){
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		
		List<Value> amplificationPrimersValues = DescriptionFactory.newValues("16S FL + Fuhrman primers",
																			  "16S FL 27F/1492R",
																			  "A-18Sfull_length F / B-18Sfull_length R",
		                                                                      "16S V4 Prok 515FF/806R",
		                                                                      "16S V1V2V3 Prok Sneed2015 27F/519Rmodbio",
		                                                                      "16S V4V5 Archae 517F/958R",
		                                                                      "16S V5V6 Prok 784F/1061R",
		                                                                      "16S V3 F342/V4R802",
		                                                                      "16S V3V4 341F/805R_Cave",
		                                                                      "18S V1V2 Metazoaire SSUF04/SSURmod",
		                                                                      "18S V4 Euk V4f (TAReukF1)/V4r (TAReukR)",
		                                                                      "18S V9 1389F/1510R",
		                                                                      "18S 0067a deg/NSR399_Cave",
		                                                                      "COI primers m1COIintF/jgHCO2198",
		                                                                      "CP23S primers",
		                                                                      "Fuhrman primers",
		                                                                      "ITS2/SYM_VAR_5.8S2/SYM_VAR_REV",
		                                                                      "ITSD/ITS2REV",
		                                                                      "ITSintfor2/ITS-Reverse",
		                                                                      "ITS3f/ITS4_KYO1",
		                                                                      "ITS3 KYO2/ITS4_Cave",
		                                                                      "MamP007 F/MamP007 R",
		                                                                      "Plant-g F/Plant-g R",
		                                                                      "Arch 519F/915R_Cave",
		                                                                      "autre");
        List<Value> targetedRegionValues = DescriptionFactory.newValues("16S_V4V5",
        																"16S_V3V4_Bact",
                                                                        "18S_V9",
                                                                        "16S_Full Length + 16S_V4V5",
                                                                        "ITS2",
                                                                        "ITS2_Fungal",
                                                                        "CP23S",
                                                                        "18S_V4",
                                                                        "18S_microeuk",
                                                                        "COI",
                                                                        "16S_V1V2V3",
                                                                        "16S_V3V4",
                                                                        "16S_V3V4_Archeal",
                                                                        "16S_V5V6",
                                                                        "18S_V1V2",
                                                                        "16S_V4",
                                                                        "16S_mito",
                                                                        "16SFL",
                                                                        "18SFL",
                                                                        "P6loop_chloro_trnL",
                                                                        "autre");
        List<Value> expectedAmpliconSizeValues = DescriptionFactory.newValues("150-170",
                                                                              "170",
                                                                              "180",
                                                                              "250",
                                                                              "280",
                                                                              "300",
                                                                              "300-400",
                                                                              "313",
                                                                              "350",
                                                                              "380",
                                                                              "400",
                                                                              "450",
                                                                              "460",
                                                                              "500",
                                                                              "550",
                                                                              "411/600",
                                                                              "690-710",
                                                                              "60-84",
                                                                              "10-146",
                                                                              "autre");
        //170 -> 150-170, 500 ??, 270 ?? -> pas utilisé, 250 ?? -> autre, 550 ?? -> autre
        //manque : 411/600 (400), 280(250), 150-170(170) 

		
        propertyDefinitions.add(newPropertiesDefinition("Amorces",                  "amplificationPrimers", LevelService.getLevels(Level.CODE.Process),                     String.class, true, null, amplificationPrimersValues, null, null, null, "single", 14, true, null, null));
		propertyDefinitions.add(newPropertiesDefinition("Région ciblée",            "targetedRegion",       LevelService.getLevels(Level.CODE.Process),                     String.class, true, null, targetedRegionValues,	      null, null, null, "single", 15, true, null, null));
		propertyDefinitions.add(newPropertiesDefinition("Taille amplicon attendue", "expectedAmpliconSize", LevelService.getLevels(Level.CODE.Process, Level.CODE.Content), String.class, true, null, expectedAmpliconSizeValues, null, null, null, "single", 16, true, null, null));
		
		return propertyDefinitions;

	}
		
	public static  List<Value> getBanqueProcessTypeMetaTB() {
		List<Value> values = new ArrayList<>();
		values.add(DescriptionFactory.newValue("TB", "TB - Targeted DNAseq avec sizing"));
		return values;
	}
	
	public static  List<Value> getBanqueProcessTypeMetaTA(){
		List<Value> values = new ArrayList<>();
		values.add(DescriptionFactory.newValue("TA", "TA - Targeted DNAseq"));
		return values;
	}
	
	public static  List<Value> getBanqueProcessTypeMetaTC(){
		List<Value> values = new ArrayList<>();
		values.add(DescriptionFactory.newValue("TC", "TC - Targeted DNAseq (nested)"));
		return values;
	}
	
	public static  List<Value> getBanqueProcessTypeN(){
		List<Value> values = new ArrayList<>();
		values.add(DescriptionFactory.newValue("N", "N - Mate-pair Nextera"));
		return values;
	}
	

}
