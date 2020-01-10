package services.description.experiment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.typesafe.config.ConfigFactory;

import models.laboratory.experiment.description.ExperimentCategory;
import models.laboratory.experiment.description.ExperimentType;
import models.laboratory.experiment.description.ProtocolCategory;
import models.utils.dao.DAOException;
import models.utils.dao.DAOHelpers;
import play.data.validation.ValidationError;
import services.description.DescriptionFactory;
import services.description.declaration.cns.BanqueIllumina;
import services.description.declaration.cns.Bionano;
import services.description.declaration.cns.MetaBarCoding;
import services.description.declaration.cns.MetaGenomique;
import services.description.declaration.cns.MetaTProcess;
import services.description.declaration.cns.Nanopore;
import services.description.declaration.cns.Opgen;
import services.description.declaration.cns.Purif;
import services.description.declaration.cns.QualityControl;
import services.description.declaration.cns.RunIllumina;
import services.description.declaration.cns.SamplePrep;
import services.description.declaration.cns.Transfert;

public class ExperimentServiceCNS extends AbstractExperimentService {

	// @SuppressWarnings("unchecked")
	@Override
	public  void saveProtocolCategories(Map<String, List<ValidationError>> errors) throws DAOException {
		List<ProtocolCategory> l = new ArrayList<>();
		l.add(DescriptionFactory.newSimpleCategory(ProtocolCategory.class, "Developpement", "development"));
		l.add(DescriptionFactory.newSimpleCategory(ProtocolCategory.class, "Production", "production"));
		DAOHelpers.saveModels(ProtocolCategory.class, l, errors);

	}
	
	/**
	 * Save all ExperimentCategory.
	 * @param errors        error manager
	 * @throws DAOException DAO problem
	 */
	@Override
	public  void saveExperimentCategories(Map<String,List<ValidationError>> errors) throws DAOException {
		List<ExperimentCategory> l = new ArrayList<>();
		
		l.add(DescriptionFactory.newSimpleCategory(ExperimentCategory.class, "Purification", ExperimentCategory.CODE.purification.name()));
		l.add(DescriptionFactory.newSimpleCategory(ExperimentCategory.class, "Control qualité", ExperimentCategory.CODE.qualitycontrol.name()));
		l.add(DescriptionFactory.newSimpleCategory(ExperimentCategory.class, "Transfert", ExperimentCategory.CODE.transfert.name()));
		l.add(DescriptionFactory.newSimpleCategory(ExperimentCategory.class, "Transformation", ExperimentCategory.CODE.transformation.name()));
		l.add(DescriptionFactory.newSimpleCategory(ExperimentCategory.class, "Void process", ExperimentCategory.CODE.voidprocess.name()));
		
		DAOHelpers.saveModels(ExperimentCategory.class, l, errors);
	}


	@Override
	public void saveExperimentTypes(Map<String, List<ValidationError>> errors) throws DAOException {
		List<ExperimentType> l = new ArrayList<>();

		l.addAll(new Opgen().getExperimentType());
		//	l.addAll(new Nanopore().getExperimentType());
		l.addAll(new Bionano().getExperimentType());
		l.addAll(new QualityControl().getExperimentType());
		l.addAll(new Transfert().getExperimentType());
		l.addAll(new Purif().getExperimentType());

		l.addAll(new SamplePrep().getExperimentType());
		l.addAll(new MetaBarCoding().getExperimentType());
		l.addAll(new MetaGenomique().getExperimentType());
		l.addAll(new MetaTProcess().getExperimentType());
		l.addAll(new BanqueIllumina().getExperimentType());
		l.addAll(new RunIllumina().getExperimentType());
		l.addAll(new Nanopore().getExperimentType());


		if(ConfigFactory.load().getString("ngl.env").equals("DEV") ){

			/*
			l.add(newExperimentType("Ext to Banque","ext-to-library",null,
					ExperimentCategory.find.findByCode(ExperimentCategory.CODE.voidprocess.name()), null, null,"OneToOne", 
					DescriptionFactory.getInstitutes(Constants.CODE.CNS)));

			l.add(newExperimentType("Ampure Non Ampli","ampure-na",null,
					ExperimentCategory.find.findByCode(ExperimentCategory.CODE.purification.name()),
					null, getInstrumentUsedTypes("hand"),"OneToOne", 
					DescriptionFactory.getInstitutes(Constants.CODE.CNS)));

			l.add(newExperimentType("Ampure Ampli","ampure-a",null,
					ExperimentCategory.find.findByCode(ExperimentCategory.CODE.purification.name()), null,
					getInstrumentUsedTypes("hand"),"OneToOne", 
					DescriptionFactory.getInstitutes(Constants.CODE.CNS)));		



			l.add(newExperimentType("Librairie indexée","librairie-indexing",null,400,
					ExperimentCategory.find.findByCode(ExperimentCategory.CODE.transformation.name()), getPropertyDefinitionsLibIndexing(),
					getInstrumentUsedTypes("hand","spri"),"OneToOne", 
					DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
			 */

		}

		DAOHelpers.saveModels(ExperimentType.class, l, errors);

	}

	@Override
	public void saveExperimentTypeNodes(Map<String, List<ValidationError>> errors) throws DAOException {

		
		new SamplePrep().getExperimentTypeNode();
		
		new Opgen().getExperimentTypeNode();
		new Bionano().getExperimentTypeNode();		
		new Nanopore().getExperimentTypeNode();
		new BanqueIllumina().getExperimentTypeNode();
		
		new MetaGenomique().getExperimentTypeNode();
		new MetaTProcess().getExperimentTypeNode();
		new MetaBarCoding().getExperimentTypeNode();
		new RunIllumina().getExperimentTypeNode();
		
		new QualityControl().getExperimentTypeNode();
		new Transfert().getExperimentTypeNode();
		new Purif().getExperimentTypeNode();
		
		
		//newExperimentTypeNode("ext-to-qpcr", getExperimentTypes("ext-to-qpcr").get(0), false, false, false, null, null, null, null).save();	
		//newExperimentTypeNode("ext-to-solution-stock", getExperimentTypes("ext-to-solution-stock").get(0), false, false, false, null, null, null, null).save();
				
		/*if(	!ConfigFactory.load().getString("ngl.env").equals("PROD") ){
				
				newExperimentTypeNode("ext-to-library", getExperimentTypes("ext-to-library").get(0), false, false, null, null, null).save();
				newExperimentTypeNode("ext-to-qPCR-norm-fc-depot-illumina", getExperimentTypes("ext-to-qpcr-norm-fc-depot-illumina").get(0), false, false, null, null, null).save();
				newExperimentTypeNode("ext-to-norm-fc-depot-illumina", getExperimentTypes("ext-to-norm-fc-depot-illumina").get(0), false, false, null, null, null).save();
				
				//REM : experimentTypes list confirmées par Julie
				
				newExperimentTypeNode("librairie-indexing", getExperimentTypes("librairie-indexing").get(0), false, false, getExperimentTypeNodes("fragmentation"), 
						getExperimentTypes("ampure-na"), getExperimentTypes("fluo-quantification","chip-migration-pre-pcr")).save();
				
				newExperimentTypeNode("librairie-dualindexing", getExperimentTypes("librairie-dualindexing").get(0), false, false, getExperimentTypeNodes("fragmentation"), 
						getExperimentTypes("ampure-na"), getExperimentTypes("fluo-quantification","chip-migration-pre-pcr")).save();			
									
			}
		*/

	}

//	private static List<PropertyDefinition> getPropertyDefinitionsLibIndexing() throws DAOException {
//		List<PropertyDefinition> propertyDefinitions = new ArrayList<PropertyDefinition>();
//		//Ajouter la liste des index illumina
//		propertyDefinitions.add(newPropertiesDefinition("Tag","tag", LevelService.getLevels(Level.CODE.ContainerIn,Level.CODE.Content),String.class, true, "single"));
//		propertyDefinitions.add(newPropertiesDefinition("Catégorie tag","tagCategory", LevelService.getLevels(Level.CODE.ContainerIn,Level.CODE.Content),String.class, true, getTagCategories(),"single"));
//		propertyDefinitions.add(newPropertiesDefinition("Quantité engagée","inputQuantity", LevelService.getLevels(Level.CODE.ContainerIn),Double.class, false, "single"));
//		propertyDefinitions.add(newPropertiesDefinition("Volume engagé","inputVolume", LevelService.getLevels(Level.CODE.ContainerIn),Double.class, true, "single"));
//		return propertyDefinitions;
//	}
	
}
