package services.description.declaration.cns;

import static services.description.DescriptionFactory.newExperimentType;
import static services.description.DescriptionFactory.newExperimentTypeNode;
import static services.description.DescriptionFactory.newPropertiesDefinition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import models.laboratory.common.description.Level;
import models.laboratory.common.description.PropertyDefinition;
import models.laboratory.common.description.Value;
import models.laboratory.experiment.description.ExperimentCategory;
import models.laboratory.experiment.description.ExperimentType;
import models.laboratory.processes.description.ProcessCategory;
import models.laboratory.processes.description.ProcessType;
import services.description.Constants;
import services.description.DescriptionFactory;
import services.description.common.LevelService;
import services.description.declaration.AbstractDeclaration;
import services.description.experiment.AbstractExperimentService;

public class MetaGenomique extends AbstractDeclaration {

	@Override
	protected List<ExperimentType> getExperimentTypePROD() {
		return null;
	}
	
	 @Override
     protected List<ExperimentType> getExperimentTypeCommon() {
		List<ExperimentType> l = new ArrayList<>();
		
		l.add(newExperimentType("Ext to  MetaGénomique avec sizing (gel)","ext-to-metagenomic-process-with-sizing",null,-1,
		                 ExperimentCategory.find.findByCode(ExperimentCategory.CODE.voidprocess.name()), null, null,"OneToOne",
		                 DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		
		l.add(newExperimentType("Ext to  MetaGénomique avec spri-select","ext-to-metagenomic-process-with-spri-select",null,-1,
		                 ExperimentCategory.find.findByCode(ExperimentCategory.CODE.voidprocess.name()), null, null,"OneToOne",
		                 DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		
		l.add(newExperimentType("Ext to MetaGénomique","ext-to-metagenomic-process",null,-1,
		                 ExperimentCategory.find.findByCode(ExperimentCategory.CODE.voidprocess.name()), null, null,"OneToOne",
		                 DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		
		l.add(newExperimentType("Ext to PCR Free","ext-to-pcr-free-process",null,-1,
                ExperimentCategory.find.findByCode(ExperimentCategory.CODE.voidprocess.name()), null, null,"OneToOne",
                DescriptionFactory.getInstitutes(Constants.CODE.CNS)));

		return l;
     }


	@Override
	protected List<ExperimentType> getExperimentTypeDEV() {
		List<ExperimentType> l = new ArrayList<>();
		
		
		return l;
	}

	@Override
	protected List<ExperimentType> getExperimentTypeUAT() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected List<ProcessType> getProcessTypePROD() {
		
		return null;
	}

	@Override
	protected List<ProcessType> getProcessTypeDEV() {
		List<ProcessType> l = new ArrayList<>();
		

		return l;
	}

	@Override
	protected List<ProcessType> getProcessTypeUAT() {
		// TODO Auto-generated method stub
		return null;
	}
	
	 @Override
     protected List<ProcessType> getProcessTypeCommon() {
             List<ProcessType> l = new ArrayList<>();

             l.add(DescriptionFactory.newProcessType("(Meta)Génomique ou reprise à partir cDNA", "metagenomic-process", ProcessCategory.find.findByCode("library"), 31,
                             getPropertiesMetaGenomique(),
                             Arrays.asList(getPET("ext-to-metagenomic-process",-1)
                            		 		 , getPET("wga-amplification",-1)
                            	        	 , getPET("cdna-synthesis",-1)
                                             , getPET("dna-rna-extraction",-1)
                                             , getPET("fragmentation",0)
                                             , getPET("dna-illumina-indexed-library",1)
                                             , getPET("pcr-amplification-and-purification",2)
                                             , getPET("solution-stock",3)
                                             , getPET("prepa-flowcell",4)
                                             , getPET("prepa-fc-ordered",4)
                                             , getPET("illumina-depot",5)),
                                             getExperimentTypes("fragmentation").get(0), getExperimentTypes("illumina-depot").get(0), getExperimentTypes("ext-to-metagenomic-process").get(0),
                                             DescriptionFactory.getInstitutes(Constants.CODE.CNS)));

             l.add(DescriptionFactory.newProcessType("(Meta)Génomique (spri select)", "metagenomic-process-with-spri-select", ProcessCategory.find.findByCode("library"), 32,
                             getPropertiesMetaGenomiqueWithSpriSelect(),
                             Arrays.asList(getPET("ext-to-metagenomic-process-with-spri-select",-1)
                            		 		 ,getPET("wga-amplification",-1)
                            	             ,getPET("dna-rna-extraction",-1)
                                             , getPET("fragmentation",0)
                                             , getPET("dna-illumina-indexed-library",1)
                                             , getPET("pcr-amplification-and-purification",2)
                                             , getPET("spri-select",3)
                                             , getPET("solution-stock",4)
                                             , getPET("prepa-flowcell",5)
                                             , getPET("prepa-fc-ordered",5)
                                             , getPET("illumina-depot",6)),
                                             getExperimentTypes("fragmentation").get(0), getExperimentTypes("illumina-depot").get(0), getExperimentTypes("ext-to-metagenomic-process-with-spri-select").get(0),
                                             DescriptionFactory.getInstitutes(Constants.CODE.CNS)));


             l.add(DescriptionFactory.newProcessType("(Meta)Génomique avec sizing (gel)", "metagenomic-process-with-sizing", ProcessCategory.find.findByCode("library"), 33,
                             getPropertiesMetaGenomiqueWithSizingDEV(),
                             Arrays.asList(getPET("ext-to-metagenomic-process-with-sizing",-1)
                            		 		 ,getPET("wga-amplification",-1)
                            	             ,getPET("dna-rna-extraction",-1)
                                             , getPET("fragmentation",0)
                                             , getPET("dna-illumina-indexed-library",1)
                                             , getPET("pcr-amplification-and-purification",2)
                                             , getPET("sizing",3)
                                             , getPET("solution-stock",4)
                                             , getPET("prepa-flowcell",5)
                                             , getPET("prepa-fc-ordered",5)
                                             , getPET("illumina-depot",6)),
                                             getExperimentTypes("fragmentation").get(0), getExperimentTypes("illumina-depot").get(0), getExperimentTypes("ext-to-metagenomic-process-with-sizing").get(0),
                                             DescriptionFactory.getInstitutes(Constants.CODE.CNS)));

             
             l.add(DescriptionFactory.newProcessType("PCR Free", "pcr-free-process", ProcessCategory.find.findByCode("library"), 34,
                     getPropertiesPCRFree(),
                     Arrays.asList(getPET("ext-to-pcr-free-process",-1)
                     				,getPET("dna-rna-extraction",-1)
                                     , getPET("fragmentation",0)
                                     , getPET("dna-illumina-indexed-library",1)
                                     , getPET("solution-stock",2)
                                     , getPET("prepa-flowcell",3)
                                     , getPET("prepa-fc-ordered",3)
                                     , getPET("illumina-depot",4)),
                                     getExperimentTypes("fragmentation").get(0), getExperimentTypes("illumina-depot").get(0), getExperimentTypes("ext-to-pcr-free-process").get(0),
                                     DescriptionFactory.getInstitutes(Constants.CODE.CNS)));

             
             return l;
     }

	 
	 private List<PropertyDefinition> getPropertiesMetaGenomiqueWithSpriSelect() {
         List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
         propertyDefinitions.add(newPropertiesDefinition("Type processus Banque", "libProcessTypeCode", LevelService.getLevels(Level.CODE.Process,Level.CODE.Content), String.class, true, null, getLibProcessDC(),
                         null,null,null,"single", 13, true, null, null));
         propertyDefinitions.add(newPropertiesDefinition("Protocole banque DNA", "dnaLibraryProtocol", LevelService.getLevels(Level.CODE.Process), String.class, true, null, DescriptionFactory.newValues("NEB Ultra 2","low cost","super low cost"),
                         null,null,null,"single", 14, true, null, null));
         propertyDefinitions.add(newPropertiesDefinition("Objectif sizing 1", "sizingGoal", LevelService.getLevels(Level.CODE.Process), String.class, true, null,
                         DescriptionFactory.newValues("ss0.6/0.53","ss0.7/0.58","autre"),
                         null,null,null,"single", 16, true, null, null));


         propertyDefinitions.addAll(RunIllumina.getIlluminaDepotProperties());
         return propertyDefinitions;
	 }


	
	private List<PropertyDefinition> getPropertiesMetaGenomiqueWithSizingDEV() {
        List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
        propertyDefinitions.add(newPropertiesDefinition("Type processus Banque", "libProcessTypeCode", LevelService.getLevels(Level.CODE.Process,Level.CODE.Content), String.class, true, null, getLibProcessDB(),
                        null,null,null,"single", 13, true, null, null));
        propertyDefinitions.add(newPropertiesDefinition("Protocole banque DNA", "dnaLibraryProtocol", LevelService.getLevels(Level.CODE.Process), String.class, true, null, DescriptionFactory.newValues("NEB Ultra 2","low cost","super low cost"),
                        null,null,null,"single", 14, true, null, null));
        propertyDefinitions.add(newPropertiesDefinition("Ratio ampure post-pcr", "postPcrAmpureVolume", LevelService.getLevels(Level.CODE.Process), String.class, false, null, null,
                        null,null,null,"single", 15, true, null, null));
        propertyDefinitions.add(newPropertiesDefinition("Objectif sizing 1", "sizingGoal", LevelService.getLevels(Level.CODE.Process), String.class, true, null,
        		DescriptionFactory.newValues("280-310 (F300)", "450-550 (W500)", "550-650 (W600)", "600-700 (W700)","650-750 (W700)", "650-700 (W700)","700-800 (W800)","750-800","autre"),
                null,null,null,"single", 16, true, null, null));


        propertyDefinitions.addAll(RunIllumina.getIlluminaDepotProperties());
        return propertyDefinitions;
}


	private List<PropertyDefinition> getPropertiesMetaGenomique() {
		List<Value> libProcessTypeCodes = getLibProcessDA();
		libProcessTypeCodes.addAll(MetaTProcess.getBanqueProcessTypeMetaRA());
		
		
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		propertyDefinitions.add(newPropertiesDefinition("Type processus Banque", "libProcessTypeCode", LevelService.getLevels(Level.CODE.Process,Level.CODE.Content), String.class, true, null, libProcessTypeCodes, 
				null,null,null,"single", 13, true, null, null));
		propertyDefinitions.add(newPropertiesDefinition("Protocole banque DNA", "dnaLibraryProtocol", LevelService.getLevels(Level.CODE.Process), String.class, true, null, DescriptionFactory.newValues("NEB Ultra 2","low cost","super low cost","autre"), 
				null,null,null,"single", 14, true, null, null));
		propertyDefinitions.add(newPropertiesDefinition("Ratio ampure post-pcr", "postPcrAmpureVolume", LevelService.getLevels(Level.CODE.Process), String.class, false, null, null, 
				null,null,null,"single", 15, true, null, null));

		propertyDefinitions.addAll(RunIllumina.getIlluminaDepotProperties());
		return propertyDefinitions;
	}


	private List<PropertyDefinition> getPropertiesPCRFree() {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		propertyDefinitions.add(newPropertiesDefinition("Type processus Banque", "libProcessTypeCode", LevelService.getLevels(Level.CODE.Process,Level.CODE.Content), String.class, true, null, getLibProcessDE(), 
				null,null,null,"single", 13, true, null, null));
		
		propertyDefinitions.addAll(RunIllumina.getIlluminaDepotProperties());
		return propertyDefinitions;
	}
	

	@Override
	protected void getExperimentTypeNodeCommon() {
		newExperimentTypeNode("ext-to-metagenomic-process", AbstractExperimentService.getExperimentTypes("ext-to-metagenomic-process").get(0), false, false, false, null, null, null, null).save();
		newExperimentTypeNode("ext-to-metagenomic-process-with-sizing", AbstractExperimentService.getExperimentTypes("ext-to-metagenomic-process-with-sizing").get(0), false, false, false, null, null, null, null).save();
		newExperimentTypeNode("ext-to-metagenomic-process-with-spri-select", AbstractExperimentService.getExperimentTypes("ext-to-metagenomic-process-with-spri-select").get(0), false, false, false, null, null, null, null).save();
		newExperimentTypeNode("ext-to-pcr-free-process", AbstractExperimentService.getExperimentTypes("ext-to-pcr-free-process").get(0), false, false, false, null, null, null, null).save();

	}

	@Override
	protected void getExperimentTypeNodeDEV() {
				
		
	}

	@Override
	protected void getExperimentTypeNodePROD() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void getExperimentTypeNodeUAT() {
		// TODO Auto-generated method stub
		
	}
	
	
	public static List<Value> getLibProcessDA() {
		List<Value> values = new ArrayList<>();
		values.add(DescriptionFactory.newValue("DA", "DA - DNAseq"));
		return values;
	}
	
	public static  List<Value> getLibProcessDB() {
        List<Value> values = new ArrayList<>();
        values.add(DescriptionFactory.newValue("DB", "DB - DNAseq avec sizing (gel)"));
        return values;
	}

	public static  List<Value> getLibProcessDC() {
        List<Value> values = new ArrayList<>();
        values.add(DescriptionFactory.newValue("DC", "DC - DNAseq avec spri select"));
        return values;
	}
	
	public static  List<Value> getLibProcessDD() {
		List<Value> values = new ArrayList<>();
		values.add(DescriptionFactory.newValue("DD", "DD - Chromium 10x"));
		return values;
	}
	
	public static  List<Value> getLibProcessDE() {
		List<Value> values = new ArrayList<>();
		values.add(DescriptionFactory.newValue("DE", "DE - PCR Free"));
		return values;
	}
	
	

}
