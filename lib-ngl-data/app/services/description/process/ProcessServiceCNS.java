package services.description.process;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import models.laboratory.common.description.Level;
import models.laboratory.common.description.PropertyDefinition;
import models.laboratory.processes.description.ProcessCategory;
import models.laboratory.processes.description.ProcessType;
import models.utils.dao.DAOException;
import models.utils.dao.DAOHelpers;
import play.data.validation.ValidationError;
import services.description.DescriptionFactory;
import services.description.common.LevelService;
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

public class ProcessServiceCNS extends AbstractProcessService {

	public List<ProcessCategory> getProcessCategories() {
		List<ProcessCategory> l = new ArrayList<>();
//		l.add(DescriptionFactory.newSimpleCategory(ProcessCategory.class, "Evaluation à reception", "sample-valuation"));
//		l.add(DescriptionFactory.newSimpleCategory(ProcessCategory.class, "Préparation échantillon", "sample-prep"));
//		l.add(DescriptionFactory.newSimpleCategory(ProcessCategory.class, "Banque Illumina", "library"));
//		l.add(DescriptionFactory.newSimpleCategory(ProcessCategory.class, "Banque Nanopore", "nanopore-library"));
//		l.add(DescriptionFactory.newSimpleCategory(ProcessCategory.class, "Sequençage", "sequencing"));		
//		l.add(DescriptionFactory.newSimpleCategory(ProcessCategory.class, "Optical mapping", "mapping"));
//		l.add(DescriptionFactory.newSimpleCategory(ProcessCategory.class, "Réévaluation et/ou exp satellites", "satellites"));
		l.add(new ProcessCategory("sample-valuation", "Evaluation à reception"));
		l.add(new ProcessCategory("sample-prep",      "Préparation échantillon"));
		l.add(new ProcessCategory("library",          "Banque Illumina"));
		l.add(new ProcessCategory("nanopore-library", "Banque Nanopore"));
		l.add(new ProcessCategory("sequencing",       "Sequençage"));		
		l.add(new ProcessCategory("mapping",          "Optical mapping"));
		l.add(new ProcessCategory("satellites",       "Réévaluation et/ou exp satellites"));
		return l;
	}
	
	@Override
	public void saveProcessCategories(Map<String, List<ValidationError>> errors) throws DAOException {
		DAOHelpers.saveModels(ProcessCategory.class, getProcessCategories(), errors);
	}

	public List<ProcessType> getProcessTypes() {
		List<ProcessType> l = new ArrayList<>();
		l.addAll(new QualityControl().getProcessType());
		l.addAll(new Transfert().getProcessType());
		l.addAll(new Purif().getProcessType());
		l.addAll(new MetaBarCoding().getProcessType());
		l.addAll(new MetaTProcess().getProcessType());
		l.addAll(new MetaGenomique().getProcessType());
		l.addAll(new BanqueIllumina().getProcessType());
		l.addAll(new Bionano().getProcessType());
		l.addAll(new Nanopore().getProcessType());
		l.addAll(new RunIllumina().getProcessType());
		l.addAll(new Opgen().getProcessType());
		l.addAll(new SamplePrep().getProcessType());
		return l;
	}
	
	@Override
	public void saveProcessTypes(Map<String, List<ValidationError>> errors) throws DAOException {
		DAOHelpers.saveModels(ProcessType.class, getProcessTypes(), errors);
	}
	
	public static List<PropertyDefinition> getPropertyDefinitionsLib300600() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();	
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Robot à utiliser","autoVsManuel", LevelService.getLevels(Level.CODE.Process),String.class, true, DescriptionFactory.newValues("MAN","ROBOT"), "single",100));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Quantité à engager", "inputQuantity", LevelService.getLevels(Level.CODE.Process),Double.class, true, DescriptionFactory.newValues("250","500"), "single",200));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Objectif Taille de banque finale", "librarySizeGoal", LevelService.getLevels(Level.CODE.Process),String.class,true,DescriptionFactory.newValues("300-600","300-800"), "single",300));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Catégorie Imputation", "imputationCat", LevelService.getLevels(Level.CODE.Process),String.class,true,DescriptionFactory.newValues("PRODUCTION","DEVELOPPEMENT"), "single",400));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Date limite", "deadline", LevelService.getLevels(Level.CODE.Process),Date.class,false, "single",500));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Indexing", "indexing", LevelService.getLevels(Level.CODE.Process),String.class,true,DescriptionFactory.newValues("Single indexing","Dual indexing","Pas d'indexing"), "single",600));
		return propertyDefinitions;
	}
	
}
