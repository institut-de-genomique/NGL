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
import models.laboratory.experiment.description.ExperimentCategory;
import models.laboratory.experiment.description.ExperimentType;
import models.laboratory.processes.description.ProcessCategory;
import models.laboratory.processes.description.ProcessType;
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

             l.add(newExperimentType("Ext to MetaBarcoding (sans sizing)","ext-to-tag-pcr-and-dna-library",null,-1,
                             ExperimentCategory.find.findByCode(ExperimentCategory.CODE.voidprocess.name()), null, null,"OneToOne",
                             DescriptionFactory.getInstitutes(Constants.CODE.CNS)));

             l.add(newExperimentType("Ext to MetaBarcoding avec sizing (gel)","ext-to-tag-pcr-and-dna-library-with-sizing",null,-1,
                             ExperimentCategory.find.findByCode(ExperimentCategory.CODE.voidprocess.name()), null, null,"OneToOne",
                             DescriptionFactory.getInstitutes(Constants.CODE.CNS)));

             l.add(newExperimentType("Ext to MetaBarcoding double PCR (nested)","ext-to-double-tag-pcr-and-dna-library",null,-1,
                     ExperimentCategory.find.findByCode(ExperimentCategory.CODE.voidprocess.name()), null, null,"OneToOne",
                     DescriptionFactory.getInstitutes(Constants.CODE.CNS)));

             
             l.add(newExperimentType("Tags-PCR","tag-pcr","TAG",750,
                             ExperimentCategory.find.findByCode(ExperimentCategory.CODE.transformation.name()), getPropertyDefinitionsTagPCR(),
                             getInstrumentUsedTypes("thermocycler"),"OneToOne", getSampleTypes("amplicon"),true,
                             DescriptionFactory.getInstitutes(Constants.CODE.CNS)));

             l.add(newExperimentType("Bq DNA Illumina indexée","dna-illumina-indexed-library","LIB",850,
                             ExperimentCategory.find.findByCode(ExperimentCategory.CODE.transformation.name()), getPropertyDefinitionsBqDNAIlluminaIndexedLibrary(),
                             getInstrumentUsedTypes("hand","biomek-fx"),"OneToOne", null,true,
                             DescriptionFactory.getInstitutes(Constants.CODE.CNS)));

             l.add(newExperimentType("Amplification/PCR","pcr-amplification-and-purification","PCR",900,
                             ExperimentCategory.find.findByCode(ExperimentCategory.CODE.transformation.name()), getPropertyDefinitionsAmpliPurif(),
                             getInstrumentUsedTypes("thermocycler"),"OneToOne", null,true,
                             DescriptionFactory.getInstitutes(Constants.CODE.CNS)));

             l.add(newExperimentType("Amplification/PCR + indexing","indexing-and-pcr-amplification","PCR",900,
                     ExperimentCategory.find.findByCode(ExperimentCategory.CODE.transformation.name()), getPropertyDefinitionsAmpliPurifIndexing(),
                     getInstrumentUsedTypes("thermocycler"),"OneToOne", null,true,
                     DescriptionFactory.getInstitutes(Constants.CODE.CNS)));

             
             
             l.add(newExperimentType("Sizing (gel)","sizing","SIZ",950,
                             ExperimentCategory.find.findByCode(ExperimentCategory.CODE.transformation.name()), getPropertyDefinitionsSizingDEV(),
                             getInstrumentUsedTypes("hand"),"OneToMany", null,true,
                             DescriptionFactory.getInstitutes(Constants.CODE.CNS)));


             l.add(newExperimentType("Spri Select","spri-select","SS",951,
                             ExperimentCategory.find.findByCode(ExperimentCategory.CODE.transformation.name()), getPropertyDefinitionsSpriSelect(),
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected List<ProcessType> getProcessTypePROD() {
		
		return null;
	}
	
	
    @Override
    protected List<ProcessType> getProcessTypeCommon() {
            List<ProcessType> l = new ArrayList<>();

            l.add(DescriptionFactory.newProcessType("MetaBarcoding avec sizing (gel)", "tag-pcr-and-dna-library-with-sizing", ProcessCategory.find.findByCode("library"), 11,
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

            l.add(DescriptionFactory.newProcessType("MetaBarcoding (sans sizing)", "tag-pcr-and-dna-library", ProcessCategory.find.findByCode("library"), 12,
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

            l.add(DescriptionFactory.newProcessType("MetaBarcoding double PCR (nested)", "double-tag-pcr-and-dna-library", ProcessCategory.find.findByCode("library"), 13,
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void getExperimentTypeNodePROD() {

	}
	
	@Override
	public void getExperimentTypeNodeCommon() {
		
		newExperimentTypeNode("ext-to-tag-pcr-and-dna-library", getExperimentTypes("ext-to-tag-pcr-and-dna-library").get(0), false, false, false, null, null, null, null).save();
		newExperimentTypeNode("ext-to-double-tag-pcr-and-dna-library", getExperimentTypes("ext-to-double-tag-pcr-and-dna-library").get(0), false, false, false, null, null, null, null).save();
		
		newExperimentTypeNode("ext-to-tag-pcr-and-dna-library-with-sizing", getExperimentTypes("ext-to-tag-pcr-and-dna-library-with-sizing").get(0), false, false, false, null, null, null, null).save();
		newExperimentTypeNode("tag-pcr",getExperimentTypes("tag-pcr").get(0),true, true,false,getExperimentTypeNodes("wga-amplification", "dna-rna-extraction","dna-extraction","cdna-synthesis", "ext-to-tag-pcr-and-dna-library","ext-to-tag-pcr-and-dna-library-with-sizing","ext-to-double-tag-pcr-and-dna-library")
				,null,getExperimentTypes("fluo-quantification","chip-migration"),getExperimentTypes("pool","tubes-to-plate","plate-to-tubes")).save();
		
		newExperimentTypeNode("tag-pcr-nested",getExperimentTypes("tag-pcr").get(0),true, true,false,getExperimentTypeNodes("tag-pcr")
				,null,getExperimentTypes("fluo-quantification","chip-migration"),getExperimentTypes("pool","tubes-to-plate","plate-to-tubes")).save();
		
		
		newExperimentTypeNode("dna-illumina-indexed-library",getExperimentTypes("dna-illumina-indexed-library").get(0),true, true,false,getExperimentTypeNodes("ext-to-dna-illumina-indexed-library-process","ext-to-dna-illumina-indexed-lib-sizing-process","ext-to-dna-illumina-indexed-lib-spri-select-process","tag-pcr","tag-pcr-nested","fragmentation")
				,getExperimentTypes("post-pcr-ampure"),getExperimentTypes("fluo-quantification"),getExperimentTypes("pool","tubes-to-plate","plate-to-tubes")).save();
		newExperimentTypeNode("pcr-amplification-and-purification",getExperimentTypes("pcr-amplification-and-purification").get(0),true, true,false,getExperimentTypeNodes("ext-to-ampli-spri-select-stk-illumina-depot","ext-to-ampli-sizing-stk-illumina-depot", "ext-to-ampli-stk-illumina-depot","dna-illumina-indexed-library","rna-illumina-indexed-library")
				,getExperimentTypes("post-pcr-ampure"),getExperimentTypes("fluo-quantification","chip-migration"),getExperimentTypes("pool","tubes-to-plate","plate-to-tubes")).save();
		newExperimentTypeNode("indexing-and-pcr-amplification",getExperimentTypes("indexing-and-pcr-amplification").get(0),true, true,false,getExperimentTypeNodes("ext-to-ampli-spri-select-stk-illumina-depot","rna-illumina-library")
				,getExperimentTypes("post-pcr-ampure"),getExperimentTypes("fluo-quantification","chip-migration"),getExperimentTypes("pool","tubes-to-plate","plate-to-tubes")).save();
		
		newExperimentTypeNode("sizing",getExperimentTypes("sizing").get(0),true, true,false,getExperimentTypeNodes("ext-to-ampure-sizing-stk-illumina-depot", "ext-to-sizing-stk-illumina-depot", "pcr-amplification-and-purification")
				,null,getExperimentTypes("fluo-quantification","chip-migration","qpcr-quantification"),getExperimentTypes("pool","tubes-to-plate","plate-to-tubes")).save();
		
		newExperimentTypeNode("spri-select",getExperimentTypes("spri-select").get(0),true, true,false,getExperimentTypeNodes("ext-to-spri-select-stk-illumina-depot","pcr-amplification-and-purification")
				,getExperimentTypes("post-pcr-ampure"),getExperimentTypes("fluo-quantification","chip-migration","qpcr-quantification"),getExperimentTypes("pool","tubes-to-plate","plate-to-tubes")).save();
	
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
		// TODO Auto-generated method stub
	}

		
	
	 private List<PropertyDefinition> getPropertyDefinitionsSpriSelect() {
         List<PropertyDefinition> propertyDefinitions = new ArrayList<>();

         propertyDefinitions.add(newPropertiesDefinition("Volume engagé", "inputVolume", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, null,
                         null, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"),"single", 13, true, null,null));

         propertyDefinitions.add(newPropertiesDefinition("Taille théorique sizing", "expectedSize", LevelService.getLevels(Level.CODE.ContainerOut,Level.CODE.Content), String.class, true, null,
                         DescriptionFactory.newValues("ss0.6/0.53","ss0.7/0.58", "autre"),  MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_SIZE),MeasureUnit.find.findByCode( "pb"),MeasureUnit.find.findByCode( "pb"),"single", 14, true, null,null));

         propertyDefinitions.add(newPropertiesDefinition("Label de travail", "workName", LevelService.getLevels(Level.CODE.ContainerOut,Level.CODE.Container), String.class, false, null, null,
                         "single", 100, true, null,null));

         return propertyDefinitions;
	 }

	
	private List<PropertyDefinition> getPropertyDefinitionsSizingDEV() {
        List<PropertyDefinition> propertyDefinitions = new ArrayList<>();

        propertyDefinitions.add(newPropertiesDefinition("Volume engagé", "inputVolume", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, null,
                        null, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"),"single", 13, true, null,null));

        propertyDefinitions.add(newPropertiesDefinition("Taille théorique sizing", "expectedSize", LevelService.getLevels(Level.CODE.ContainerOut,Level.CODE.Content), String.class, true, null,
                        DescriptionFactory.newValues("280-310 (F300)", "400-550 (ITS2)", "450-550 (W500)","550-650 (W600)", "500-650","550-700 (ITS2)", "600-700 (W700)","650-750 (W700)","650-700 (W700)", "650-800", "700-800 (W800)","750-800", "autre"),  MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_SIZE),MeasureUnit.find.findByCode( "pb"),MeasureUnit.find.findByCode( "pb"),"single", 14, true, null,null));
        				
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
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();

		propertyDefinitions.add(newPropertiesDefinition("Volume engagé", "inputVolume", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, null, 
				null, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"),"single", 12, true, null,null));
		propertyDefinitions.add(newPropertiesDefinition("Quantité engagée","inputQuantity", LevelService.getLevels(Level.CODE.ContainerIn),Double.class, false, null,
				null,MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_QUANTITY),MeasureUnit.find.findByCode( "ng"),MeasureUnit.find.findByCode( "ng"),"single",13, true,null,null));
		propertyDefinitions.add(newPropertiesDefinition("Nb de PCR", "nbPCR", LevelService.getLevels(Level.CODE.ContainerIn), Integer.class, true, null, 
				null,  null, null, null,"single", 15, true, null,null));
		propertyDefinitions.add(newPropertiesDefinition("Volume / PCR", "PCRvolume", LevelService.getLevels(Level.CODE.ContainerOut), Double.class, true, null, 
				null, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"),"single", 50, true, null,null));
		
	
		propertyDefinitions.add(newPropertiesDefinition("DNA polymerase", "dnaPolymerase", LevelService.getLevels(Level.CODE.Experiment), String.class, false, null, 
				DescriptionFactory.newValues("taq Q5","taq Kapa","autre"), null, null, null,"single", 1, false, null,null));
		
		propertyDefinitions.add(newPropertiesDefinition("Nb cycles", "nbCycles", LevelService.getLevels(Level.CODE.Experiment), Integer.class, true, null, null, 
				"single", 3, true, null,null));
		
		return propertyDefinitions;
	}

	private List<PropertyDefinition> getPropertyDefinitionsBqDNAIlluminaIndexedLibrary() {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();

		propertyDefinitions.add(newPropertiesDefinition("Volume engagé", "inputVolume", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, null, 
				null, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"),"single", 12, true, null,null));

	//	propertyDefinitions.add(newPropertiesDefinition("Quantité engagée","inputQuantity", LevelService.getLevels(Level.CODE.ContainerIn),Double.class, false, null,
		//		null,MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_QUANTITY),MeasureUnit.find.findByCode( "ng"),MeasureUnit.find.findByCode( "ng"),"single",13, true,null,null));

		propertyDefinitions.add(newPropertiesDefinition("Qté engagée dans bq","libraryInputQuantity", LevelService.getLevels(Level.CODE.ContainerIn,Level.CODE.Content),Double.class, false, null,
				null,MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_QUANTITY),MeasureUnit.find.findByCode( "ng"),MeasureUnit.find.findByCode( "ng"),"single",13, true,null,null));

		
		propertyDefinitions.add(newPropertiesDefinition("Tag", "tag", LevelService.getLevels(Level.CODE.ContainerOut,Level.CODE.Content), String.class, true, null, 
				null, null,null,null,"single", 14, true, null,null));

		propertyDefinitions.add(newPropertiesDefinition("Catégorie de Tag", "tagCategory", LevelService.getLevels(Level.CODE.ContainerOut,Level.CODE.Content), String.class, true, null, 
				getTagCategoriesIllumina(), null,null,null,"single", 15, false, null,null));

		
		//Experiments
		propertyDefinitions.add(newPropertiesDefinition(
				"Schéma de manips","experimentPlan",LevelService.getLevels(Level.CODE.Experiment), Image.class, 
				false, null, null , "img",16,true,null,null));

		return propertyDefinitions;
	}


	private List<PropertyDefinition> getPropertyDefinitionsTagPCR() {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();

		propertyDefinitions.add(newPropertiesDefinition("Volume engagé", "inputVolume", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, null, 
				null, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"),"single", 12, true, null,null));
		propertyDefinitions.add(newPropertiesDefinition("Quantité engagée","inputQuantity", LevelService.getLevels(Level.CODE.ContainerIn),Double.class, false, null,
				null,MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_QUANTITY),MeasureUnit.find.findByCode( "ng"),MeasureUnit.find.findByCode( "ng"),"single",13, true,null,null));
		propertyDefinitions.add(newPropertiesDefinition("Nb de PCR", "nbPCR", LevelService.getLevels(Level.CODE.ContainerIn), Integer.class, true, null, 
				null,  null, null, null,"single", 15, true, null,null));

		propertyDefinitions.add(newPropertiesDefinition("Sample Type", "sampleTypeCode", LevelService.getLevels(Level.CODE.ContainerOut), String.class, true, "N", null, 
				"single", 17, false, null,null));
		propertyDefinitions.add(newPropertiesDefinition("Projet", "projectCode", LevelService.getLevels(Level.CODE.ContainerOut), String.class, true, null, 
				null, null ,null ,null ,"single", 20, false, null,null));
		propertyDefinitions.add(newPropertiesDefinition("Echantillon", "sampleCode", LevelService.getLevels(Level.CODE.ContainerOut), String.class, true, null, 
				null, null, null, null,"single", 25, false, null,null));

		//Experiments
		propertyDefinitions.add(newPropertiesDefinition("schéma de manips","experimentPlan",LevelService.getLevels(Level.CODE.Experiment), Image.class,
				false, null, null, "img",16,true,null,null));
		
		propertyDefinitions.add(newPropertiesDefinition("DNA polymerase", "dnaPolymerase", LevelService.getLevels(Level.CODE.Experiment), String.class, false, null, 
				DescriptionFactory.newValues("taq Phusion","FastStart Taq","Advantage 2", "autre"), null, null, null,"single", 1, false, "taq Phusion",null));
		/* OLD VALUE
		propertyDefinitions.add(newPropertiesDefinition("Amorces", "amplificationPrimers", LevelService.getLevels(Level.CODE.Experiment,Level.CODE.Content,Level.CODE.Sample), String.class, true, null, 
				DescriptionFactory.newValues("Fuhrman primer","V9 primer", "16S primer + Fuhrman primer", "ITS2 primer", "ITSintfor2 / ITS-Reverse", "SYM_VAR_5.8S2 / SYM_VAR_REV", 
						"ITSD / ITS2REV","CP23S primers","18S_V4 primer", "COI primer m1COIintF / jgHCO2198", "Sneed2015 27F / 519Rmodbio",
						"16SV4V5 Archae","16SV5V6 Prok","18SV1V2 Metazoaire","16SV4 Procaryote","Amp 48-1", "Amp 48-2", "16S FL 27F/1492R", "autre"),null, null, null,"single", 2, true, null,null));
		*/
		propertyDefinitions.add(newPropertiesDefinition("Amorces", "amplificationPrimers", LevelService.getLevels(Level.CODE.Experiment,Level.CODE.Content,Level.CODE.Sample), String.class, true, null, 
				DescriptionFactory.newValues("16S FL 27F/1492R + Fuhrman primers","16S FL 27F/1492R","16S FL 27F/1390R","16S FL 27F/1390R + Fuhrman primers",
						"16S V1V2V3 Prok Sneed2015 27F/519Rmodbio","16S V4 Prok 515FF/806R","16S V4V5 Archae 517F/958R","16S V5V6 Prok 784F/1061R", "16S V3 F342/V4R802",
						"18S V1V2 Metazoaire SSUF04/SSURmod","18S V4 Euk V4f (TAReukF1)/V4r (TAReukR)","18S V9 1389F/1510R",
						"COI primers m1COIintF/jgHCO2198","CP23S primers","Fuhrman primers","ITS2/SYM_VAR_5.8S2/SYM_VAR_REV","ITSD/ITS2REV",
						"ITSintfor2/ITS-Reverse","Amp 48-1","Amp 48-2","autre"),null, null, null,"single", 2, true, null,null));
		
		
		propertyDefinitions.add(newPropertiesDefinition("Région ciblée", "targetedRegion", LevelService.getLevels(Level.CODE.Experiment,Level.CODE.Content,Level.CODE.Sample), String.class, true, null, 
				DescriptionFactory.newValues("16S_V4V5","18S_V9", "16S_Full Length + 16S_V4V5", "ITS2","CP23S","18S_V4","COI", "16S_V1V2V3",
						"16S_V3V4", "16S_V5V6","18S_V1V2","16S_V4", "16SFL", "Multi-Amplicons"), null, null, null,"single", 3, true, null,null));

		propertyDefinitions.add(newPropertiesDefinition("Nb cycles", "nbCycles", LevelService.getLevels(Level.CODE.Experiment), Integer.class, true, null, null, 
				"single", 4, true, null,null));
		
		propertyDefinitions.add(newPropertiesDefinition("Code éch. témoin négatif PCR (1)", "tagPcrBlank1SampleCode", LevelService.getLevels(Level.CODE.ContainerOut, Level.CODE.Content), String.class, true, "F",
				null, null, null, null,"single", 26, false, null,null));

		propertyDefinitions.add(newPropertiesDefinition("Code éch. témoin négatif PCR (2)", "tagPcrBlank2SampleCode", LevelService.getLevels(Level.CODE.ContainerOut, Level.CODE.Content), String.class, true, "F",
				null, null, null, null,"single", 27, false, null,null));

		
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
		propertyDefinitions.add(newPropertiesDefinition("Type processus Banque", "libProcessTypeCode", LevelService.getLevels(Level.CODE.Process,Level.CODE.Content), String.class, true, null, getBanqueProcessTypeMetaTA(), 
				null,null,null,"single", 13, true, null, null));
		propertyDefinitions.addAll(getPropertyMetaB());
		propertyDefinitions.add(newPropertiesDefinition("Ratio ampure post-pcr", "postPcrAmpureVolume", LevelService.getLevels(Level.CODE.Process), String.class, false, null, null, 
				null,null,null,"single", 17, true, null, null));
		propertyDefinitions.addAll(RunIllumina.getIlluminaDepotProperties());
		return propertyDefinitions;
	}

	private List<PropertyDefinition> getPropertyMetaBarCodingSizing() {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		propertyDefinitions.add(newPropertiesDefinition("Type processus Banque", "libProcessTypeCode", LevelService.getLevels(Level.CODE.Process,Level.CODE.Content), String.class, true, null, getBanqueProcessTypeMetaTB(), 
				null,null,null,"single", 13, true, null, null));
		propertyDefinitions.addAll(getPropertyMetaB());
		propertyDefinitions.add(newPropertiesDefinition("Objectif sizing 1", "sizingGoal", LevelService.getLevels(Level.CODE.Process), String.class, true, null, 
				DescriptionFactory.newValues("400-550 (ITS2)", "500-650","autre"), 
				null,null,null,"single", 17, true, null, null));
		propertyDefinitions.add(newPropertiesDefinition("Objectif sizing 2", "sizingGoal2", LevelService.getLevels(Level.CODE.Process), String.class, false, null, 
				DescriptionFactory.newValues("550-700 (ITS2)", "650-800","autre"), 
				null,null,null,"single", 18, true, null, null));
		propertyDefinitions.add(newPropertiesDefinition("Ratio ampure post-pcr", "postPcrAmpureVolume", LevelService.getLevels(Level.CODE.Process), String.class, false, null, null, 
				null,null,null,"single", 19, true, null, null));
		propertyDefinitions.addAll(RunIllumina.getIlluminaDepotProperties());
		return propertyDefinitions;
	}
	
	private List<PropertyDefinition> getPropertyMetaB(){
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		/*
		propertyDefinitions.add(newPropertiesDefinition("Amorces", "amplificationPrimers", LevelService.getLevels(Level.CODE.Process), String.class, true, null, 
				DescriptionFactory.newValues("Fuhrman primer","V9 primer", "16S primer + Fuhrman primer", "ITS2 primer", "ITSintfor2 / ITS-Reverse", "SYM_VAR_5.8S2 / SYM_VAR_REV", 
						"ITSD / ITS2REV","CP23S primers","18S_V4 primer","COI primer m1COIintF / jgHCO2198","Sneed2015 27F / 519Rmodbio",
						"16SV4V5 Archae","16SV5V6 Prok","18SV1V2 Metazoaire","16SV4 Procaryote","autre"), null,null,null,"single", 14, true, null, null));
		*/
		propertyDefinitions.add(newPropertiesDefinition("Amorces", "amplificationPrimers", LevelService.getLevels(Level.CODE.Process), String.class, true, null, 
				DescriptionFactory.newValues("16S FL + Fuhrman primers","16S V4 Prok 515FF/806R","16S V1V2V3 Prok Sneed2015 27F/519Rmodbio","16S V4V5 Archae 517F/958R",
						"16S V5V6 Prok 784F/1061R","16S V3 F342/V4R802","18S V1V2 Metazoaire SSUF04/SSURmod","18S V4 Euk V4f (TAReukF1)/V4r (TAReukR)","18S V9 1389F/1510R",
						"COI primers m1COIintF/jgHCO2198","CP23S primers","Fuhrman primers","ITS2/SYM_VAR_5.8S2/SYM_VAR_REV","ITSD/ITS2REV",
						"ITSintfor2/ITS-Reverse","autre"), null,null,null,"single", 14, true, null, null));
				
		propertyDefinitions.add(newPropertiesDefinition("Région ciblée", "targetedRegion", LevelService.getLevels(Level.CODE.Process), String.class, true, null,
				DescriptionFactory.newValues("16S_V4V5","18S_V9", "16S_Full Length + 16S_V4V5", "ITS2","CP23S","18S_V4","COI","16S_V1V2V3","16S_V3V4",
						"16S_V5V6","18S_V1V2","16S_V4","autre"),	null,null,null,"single", 15, true, null, null));
		propertyDefinitions.add(newPropertiesDefinition("Taille amplicon attendue", "expectedAmpliconSize", LevelService.getLevels(Level.CODE.Process,Level.CODE.Content), String.class, true, null, 
				DescriptionFactory.newValues("300", "400","170","180","380","313","500","450","460","250","550", "411/600","280","150-170", "690-710"),null,null,null,"single", 16, true, null, null));
		//170 -> 150-170, 500 ??, 270 ?? -> pas utilisé, 250 ?? -> autre, 550 ?? -> autre
		//manque : 411/600 (400), 280(250), 150-170(170) 
		//enlever 270
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
