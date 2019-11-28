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
import models.utils.dao.DAOException;
import services.description.Constants;
import services.description.DescriptionFactory;
import services.description.common.LevelService;
import services.description.common.MeasureService;
import services.description.declaration.AbstractDeclaration;
import services.description.experiment.AbstractExperimentService;

public class MetaTProcess extends AbstractDeclaration {


	@Override
	protected List<ExperimentType> getExperimentTypeCommon() {
		List<ExperimentType> l = new ArrayList<>();
		ExperimentCategoryDAO ecfind = ExperimentCategory.find.get();

		l.add(newExperimentType("Ext to MetaT cDNA frg","ext-to-cdna-frg-transcriptomic-process",null,-1,
				ecfind.findByCode(ExperimentCategory.CODE.voidprocess.name()), null, null,"OneToOne", 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));

		l.add(newExperimentType("Synthèse cDNA","cdna-synthesis","cDNA",760,
				ecfind.findByCode(ExperimentCategory.CODE.transformation.name()), getPropertyDefinitionsCdnaSynthesis(),
				AbstractExperimentService.getInstrumentUsedTypes("thermocycler"),"OneToOne", null,true,
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));

		
		l.add(newExperimentType("Ext to MetaT bq RNA","ext-to-rna-lib-transcriptomic-process",null,-1,
				ecfind.findByCode(ExperimentCategory.CODE.voidprocess.name()), null, null,"OneToOne", 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));

		l.add(newExperimentType("Banque RNA","rna-illumina-indexed-library","LIB",830,
				ecfind.findByCode(ExperimentCategory.CODE.transformation.name()), getPropertyDefinitionsRNAIlluminaIndexedLibrary(),
				AbstractExperimentService.getInstrumentUsedTypes("thermocycler-and-biomek-fx","thermocycler"),"OneToOne", null,true,
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));

		l.add(newExperimentType("Banque RNA (non indexée)","rna-illumina-library","LIB",831,
				ecfind.findByCode(ExperimentCategory.CODE.transformation.name()), getPropertyDefinitionsRNAIlluminaLibrary(),
				AbstractExperimentService.getInstrumentUsedTypes("thermocycler-and-biomek-fx","thermocycler"),"OneToOne", null,true,
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));

		l.add(newExperimentType("Fragmentation Illumina","fragmentation","FRG",780,
				ecfind.findByCode(ExperimentCategory.CODE.transformation.name()), getPropertyDefinitionFragmentation(),
				getInstrumentUsedTypes("covaris-e220-ext","covaris-e220","biomek-fx-and-covaris-e220"),"OneToOne", 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS) ));
		
		return l;
	}
	
	@Override
	protected List<ExperimentType> getExperimentTypeDEV() {
		return new ArrayList<>();
	}


	@Override
	protected List<ExperimentType> getExperimentTypePROD() {
		return new ArrayList<>();
	}

	@Override
	protected List<ExperimentType> getExperimentTypeUAT() {
		return null;
	}

	@Override
	protected List<ProcessType> getProcessTypeDEV() {
		return new ArrayList<>();
	}

	@Override
	protected List<ProcessType> getProcessTypePROD() {
		return new ArrayList<>();
	}

	@Override
	protected List<ProcessType> getProcessTypeUAT() {
		return null;
	}

	@Override
	protected List<ProcessType> getProcessTypeCommon() {
		List<ProcessType> l = new ArrayList<>();
		ProcessCategoryDAO pcfind = ProcessCategory.find.get();

		l.add(DescriptionFactory.newProcessType("(Meta)T bq RNA", "rna-lib-transcriptomic-process", pcfind.findByCode("library"), 21,
				getPropertiesMetaTRNA(), 
				Arrays.asList(getPET("ext-to-rna-lib-transcriptomic-process",-1)
						,getPET("dna-rna-extraction",-1)
						,getPET("small-and-large-rna-isolation",-1)
						, getPET("rna-illumina-indexed-library",0)
						, getPET("rna-illumina-library",0)
						, getPET("pcr-amplification-and-purification",1)
						, getPET("indexing-and-pcr-amplification",1)
						, getPET("solution-stock",2)
						, getPET("prepa-flowcell",3)
						, getPET("prepa-fc-ordered",3)
						, getPET("illumina-depot",4)), getExperimentTypes("rna-illumina-indexed-library").get(0), getExperimentTypes("illumina-depot").get(0), getExperimentTypes("ext-to-rna-lib-transcriptomic-process").get(0), DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		
		l.add(DescriptionFactory.newProcessType("MetaT cDNA + FRG", "cDNA-frg-transcriptomic-process", pcfind.findByCode("library"), 22,
				getPropertiesMetaTcDNA(), 
				Arrays.asList(getPET("ext-to-cdna-frg-transcriptomic-process",-1)
						,getPET("dna-rna-extraction",-1)
						,getPET("total-rna-extraction",-1)
						,getPET("small-and-large-rna-isolation",-1)
						, getPET("cdna-synthesis",0)
						, getPET("fragmentation",1)
						, getPET("dna-illumina-indexed-library",2)
						, getPET("rna-illumina-library",2)						
						, getPET("pcr-amplification-and-purification",3)
						, getPET("indexing-and-pcr-amplification",3)
						, getPET("solution-stock",4)
						, getPET("prepa-flowcell",5)
						, getPET("prepa-fc-ordered",5)
						, getPET("illumina-depot",6)), getExperimentTypes("cdna-synthesis").get(0), getExperimentTypes("illumina-depot").get(0), getExperimentTypes("ext-to-cdna-frg-transcriptomic-process").get(0), DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		return l;
	}

	@Override
	protected void getExperimentTypeNodePROD() {
	}
	
	@Override
	protected void getExperimentTypeNodeCommon() {
		save(newExperimentTypeNode("ext-to-cdna-frg-transcriptomic-process", AbstractExperimentService.getExperimentTypes("ext-to-cdna-frg-transcriptomic-process").get(0), false, false, false, null, null, null, null));
		save(newExperimentTypeNode("cdna-synthesis",AbstractExperimentService.getExperimentTypes("cdna-synthesis").get(0),false, false,false, getExperimentTypeNodes("dna-rna-extraction","ext-to-cdna-frg-transcriptomic-process","small-and-large-rna-isolation","total-rna-extraction"),
				null, getExperimentTypes("fluo-quantification","chip-migration"),getExperimentTypes("pool","tubes-to-plate","plate-to-tubes")));
		
		save(newExperimentTypeNode("fragmentation", getExperimentTypes("fragmentation").get(0), false, false, false,	getExperimentTypeNodes("wga-amplification", "cdna-synthesis","dna-rna-extraction","ext-to-metagenomic-process-with-sizing","ext-to-metagenomic-process","ext-to-metagenomic-process-with-spri-select","ext-to-pcr-free-process"),	
				getExperimentTypes("post-pcr-ampure"), getExperimentTypes("chip-migration"), getExperimentTypes("pool","tubes-to-plate","plate-to-tubes")));
		
		save(newExperimentTypeNode("ext-to-rna-lib-transcriptomic-process", AbstractExperimentService.getExperimentTypes("ext-to-rna-lib-transcriptomic-process").get(0), false, false, false, null, null, null, null));
		save(newExperimentTypeNode("rna-illumina-indexed-library",AbstractExperimentService.getExperimentTypes("rna-illumina-indexed-library").get(0),false, false,false,getExperimentTypeNodes("dna-rna-extraction","ext-to-rna-lib-transcriptomic-process","fragmentation", "small-and-large-rna-isolation"),
				null,getExperimentTypes("fluo-quantification","chip-migration"), getExperimentTypes("pool","tubes-to-plate","plate-to-tubes")));	
		save(newExperimentTypeNode("rna-illumina-library",AbstractExperimentService.getExperimentTypes("rna-illumina-library").get(0),false, false,false,getExperimentTypeNodes("dna-rna-extraction","ext-to-rna-lib-transcriptomic-process","fragmentation", "small-and-large-rna-isolation"),
				null,getExperimentTypes("fluo-quantification","chip-migration"), getExperimentTypes("pool","tubes-to-plate","plate-to-tubes")));	
	}

	@Override
	protected void getExperimentTypeNodeDEV() {
	}

	@Override
	protected void getExperimentTypeNodeUAT() {
	}
	
	private List<PropertyDefinition> getPropertyDefinitionsRNAIlluminaLibrary() {
		MeasureUnitDAO     mufind = MeasureUnit.find.get();
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();

		propertyDefinitions.add(newPropertiesDefinition("Qté engagée ds bq RNA","rnaLibInputQuantity", LevelService.getLevels(Level.CODE.ContainerIn, Level.CODE.Content),Double.class, false, null,
			null,MeasureCategory.find.get().findByCode(MeasureService.MEASURE_CAT_CODE_QUANTITY),mufind.findByCode( "ng"),mufind.findByCode( "ng"),"single",12, true,null,null));

			
		propertyDefinitions.add(newPropertiesDefinition("Volume engagé", "inputVolume", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, null, 
				null, MeasureCategory.find.get().findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),mufind.findByCode( "µL"),mufind.findByCode( "µL"),"single", 13, true, null,null));

		//Experiments
		propertyDefinitions.add(newPropertiesDefinition("Schéma de manips","experimentPlan",LevelService.getLevels(Level.CODE.Experiment), Image.class, 
					false, null, null , "img",17,true,null,null));
		
		return propertyDefinitions;
	}
	
	
	private List<PropertyDefinition> getPropertyDefinitionsRNAIlluminaIndexedLibrary() {
		List<PropertyDefinition> propertyDefinitions = getPropertyDefinitionsRNAIlluminaLibrary();

		propertyDefinitions.add(newPropertiesDefinition("Tag", "tag", LevelService.getLevels(Level.CODE.ContainerOut,Level.CODE.Content), String.class, true, null, 
				null, null,null,null,"single", 14, true, null,null));

		propertyDefinitions.add(newPropertiesDefinition("Catégorie de Tag", "tagCategory", LevelService.getLevels(Level.CODE.ContainerOut,Level.CODE.Content), String.class, true, null, 
				getTagCategoriesIllumina(), null,null,null,"single", 15, false, null,null));
		
		return propertyDefinitions;
	}

//	private List<Value> getStrandOrientation(){
//		List<Value> values = new ArrayList<Value>();
//		values.add(DescriptionFactory.newValue("forward", "forward"));		
//		values.add(DescriptionFactory.newValue("reverse", "reverse"));		
//		values.add(DescriptionFactory.newValue("unstranded", "unstranded"));		
//		return values;	
//	}

	private List<PropertyDefinition> getPropertyDefinitionsCdnaSynthesis() {
		MeasureUnitDAO     mufind = MeasureUnit.find.get();
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();

		propertyDefinitions.add(newPropertiesDefinition("Quantité engagée","inputQuantity", LevelService.getLevels(Level.CODE.ContainerIn),Double.class, false, null,
				null,MeasureCategory.find.get().findByCode(MeasureService.MEASURE_CAT_CODE_QUANTITY),mufind.findByCode( "ng"),mufind.findByCode( "ng"),"single",12, true,null,null));

		propertyDefinitions.add(newPropertiesDefinition("Volume engagé", "inputVolume", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, false, null, 
				null, MeasureCategory.find.get().findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),mufind.findByCode( "µL"),mufind.findByCode( "µL"),"single", 13, true, null,null));
		
		return propertyDefinitions;
	}

	
	private static List<PropertyDefinition> getPropertyDefinitionFragmentation() throws DAOException {
		MeasureUnitDAO     mufind = MeasureUnit.find.get();
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		propertyDefinitions.add(newPropertiesDefinition("Volume à prélever", "requiredVolume", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, false, null,
				null, MeasureCategory.find.get().findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),mufind.findByCode( "µL"),mufind.findByCode( "µL"),"single",11, false,null, "1"));

		propertyDefinitions.add(newPropertiesDefinition("Volume engagé", "inputVolume", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, null, 
				null, MeasureCategory.find.get().findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),mufind.findByCode( "µL"),mufind.findByCode( "µL"),"single", 12, false, null,"1"));

		propertyDefinitions.add(newPropertiesDefinition("Quantité à engager","requiredQuantity", LevelService.getLevels(Level.CODE.ContainerIn),Double.class, false, null,
				null,MeasureCategory.find.get().findByCode(MeasureService.MEASURE_CAT_CODE_QUANTITY),mufind.findByCode( "ng"),mufind.findByCode( "ng"),"single",13, true,null,"1"));
		
		propertyDefinitions.add(newPropertiesDefinition("Qté réelle engagée ds FRG","frgInputQuantity", LevelService.getLevels(Level.CODE.ContainerIn,Level.CODE.Content),Double.class, false, null,
				null,MeasureCategory.find.get().findByCode(MeasureService.MEASURE_CAT_CODE_QUANTITY),mufind.findByCode( "ng"),mufind.findByCode( "ng"),"single",14, false,null,"1"));


		propertyDefinitions.add(newPropertiesDefinition("Volume tampon", "bufferVolume", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, null
				,null,MeasureCategory.find.get().findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),mufind.findByCode( "µL"),mufind.findByCode( "µL"),"single",15, false,null,"1"));
		
		return propertyDefinitions;
	}
		
	private List<PropertyDefinition> getPropertiesMetaTcDNA() {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		propertyDefinitions.add(newPropertiesDefinition("Type processus Banque", "libProcessTypeCode", LevelService.getLevels(Level.CODE.Process,Level.CODE.Content), String.class, true, null, 
				getBanqueProcessTypeMetaRA(),
				null,null,null,"single", 13, true, null, null));
		propertyDefinitions.add(newPropertiesDefinition("Type de déplétion", "depletionMethod", LevelService.getLevels(Level.CODE.Process), String.class, true, null, 
				DescriptionFactory.newValues("pas de déplétion","déplétion rRNA prok","déplétion rRNA plante"), 
				null,null,null,"single", 14, true, null, null));
		propertyDefinitions.add(newPropertiesDefinition("Protocole synthese cDNA", "cDNAsynthesisMethod", LevelService.getLevels(Level.CODE.Process), String.class, true, null, 
				DescriptionFactory.newValues("Smarter V4","Ovation RNAseq system v2","Smarter_DEV","NEBNext Single Cell/Low Input"),
				null,null,null,"single", 15, true, null, null));
		propertyDefinitions.add(newPropertiesDefinition("Ratio ampure post-pcr", "postPcrAmpureVolume", LevelService.getLevels(Level.CODE.Process), String.class, false, null, null, 
				null,null,null,"single", 16, true, null, null));
		propertyDefinitions.addAll(RunIllumina.getIlluminaDepotProperties());
		return propertyDefinitions;
	}

	private List<PropertyDefinition> getPropertiesMetaTRNA() {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		propertyDefinitions.add(newPropertiesDefinition("Type processus Banque", "libProcessTypeCode", LevelService.getLevels(Level.CODE.Process,Level.CODE.Content), String.class, true, null, 
				getBanqueProcessTypeMetaTRNA(),
				null,null,null,"single", 13, true, null, null));
		propertyDefinitions.add(newPropertiesDefinition("Type de déplétion", "depletionMethod", LevelService.getLevels(Level.CODE.Process), String.class, true, null, 
				DescriptionFactory.newValues("pas de déplétion","déplétion rRNA prok","déplétion rRNA plante","Déplétion RiboZero Plus","déplétion universelle"),
				null,null,null,"single", 14, true, null, null));
		propertyDefinitions.add(newPropertiesDefinition("Protocole synthese cDNA", "cDNAsynthesisMethod", LevelService.getLevels(Level.CODE.Process), String.class, true, null, 
				DescriptionFactory.newValues("TruSeq Stranded poly A","TruSeq Stranded Proc","Smarter Stranded", "RNA NEB_U2 Stranded", "InDA-C Ovation Universal RNA-Seq","NEXTflex Small RNA","SMARTer smRNA","ZymoSeq RiboFree","A définir"), 
				null,null,null,"single", 15, true, null, null));
		propertyDefinitions.add(newPropertiesDefinition("Ratio ampure post-pcr", "postPcrAmpureVolume", LevelService.getLevels(Level.CODE.Process), String.class, false, null, 
				null, 
				null,null,null,"single", 16, true, null, null));
		propertyDefinitions.addAll(RunIllumina.getIlluminaDepotProperties());
		return propertyDefinitions;
	}

	public static  List<Value> getBanqueProcessTypeMetaRA() {
		List<Value> values = new ArrayList<>();
		values.add(DescriptionFactory.newValue("RA", "RA - RNAseq"));
		return values;
	}
	
	public static List<Value> getBanqueProcessTypeMetaTRNA() {
		List<Value> values = new ArrayList<>();
		values.add(DescriptionFactory.newValue("RB", "RB - RNAseq stranded"));
		values.add(DescriptionFactory.newValue("RC", "RC - Small RNAseq"));
		
		return values;
	}

}
