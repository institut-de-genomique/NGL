package services.description.experiment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import models.laboratory.experiment.description.ExperimentCategory;
import models.laboratory.experiment.description.ExperimentType;
import models.laboratory.experiment.description.ProtocolCategory;
import models.utils.dao.DAOException;
import models.utils.dao.DAOHelpers;
import play.data.validation.ValidationError;
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
import services.description.declaration.cns.RunMGI;
import services.description.declaration.cns.SamplePrep;
import services.description.declaration.cns.Transfert;
//import services.description.instrument.Dépôt;

public class ExperimentServiceCNS extends AbstractExperimentService {

	public List<ProtocolCategory> getProtocolCategories() {
		List<ProtocolCategory> l = new ArrayList<>();
		l.add(new ProtocolCategory("development", "Developpement"));
		l.add(new ProtocolCategory("production",  "Production"));
		return l;
	}
	
	// @SuppressWarnings("unchecked")
	@Override
	public  void saveProtocolCategories(Map<String, List<ValidationError>> errors) throws DAOException {
		DAOHelpers.saveModels(ProtocolCategory.class, getProtocolCategories(), errors);
	}
	
	public List<ExperimentCategory> getExperimentCategories() {
		List<ExperimentCategory> l = new ArrayList<>();
		
		l.add(new ExperimentCategory(ExperimentCategory.CODE.purification,   "Purification"));
		l.add(new ExperimentCategory(ExperimentCategory.CODE.qualitycontrol, "Control qualité"));
		l.add(new ExperimentCategory(ExperimentCategory.CODE.transfert,      "Transfert"));
		l.add(new ExperimentCategory(ExperimentCategory.CODE.transformation, "Transformation"));
		l.add(new ExperimentCategory(ExperimentCategory.CODE.voidprocess,    "Void process"));
		
		return l;
	}
	
	/**
	 * Save all ExperimentCategory.
	 * @param errors        error manager
	 * @throws DAOException DAO problem
	 */
	@Override
	public  void saveExperimentCategories(Map<String,List<ValidationError>> errors) throws DAOException {
		DAOHelpers.saveModels(ExperimentCategory.class, getExperimentCategories(), errors);
	}

	public List<ExperimentType> getExperimentTypes() {
		List<ExperimentType> l = new ArrayList<>();
////ATTENTION a l'ordre de création des experimentType
		
		l.addAll(new Opgen().getExperimentType());
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

//nouveau type d'experience spécifique au MGI
		l.addAll(new RunMGI().getExperimentType());


		return l;
	}
	
	@Override
	public void saveExperimentTypes(Map<String, List<ValidationError>> errors) throws DAOException {
		DAOHelpers.saveModels(ExperimentType.class, getExperimentTypes(), errors);
	}

	@Override
	public void saveExperimentTypeNodes(Map<String, List<ValidationError>> errors) throws DAOException {
//ATTENTION a l'ordre de création des experimentTypeNodes
//		Ex: Nanopore utilise un node qui est déclaré dans Metabarcoding: tag-pcr
//		Si on met Nanopore avant metabarcoding, le previous de l'experience ne va pas etre pris en compte 
//		et dans la vue qui permet de visualiser l'arbre du process on voit bien la bulle tag-pcr mais non reliée a l'expce
		
		new SamplePrep()    .getExperimentTypeNode();
		
		new Opgen()         .getExperimentTypeNode();
		new Bionano()       .getExperimentTypeNode();		
	//	new Nanopore()      .getExperimentTypeNode();
		new BanqueIllumina().getExperimentTypeNode();
		
		new MetaGenomique() .getExperimentTypeNode();
		new MetaTProcess()  .getExperimentTypeNode();
		new MetaBarCoding() .getExperimentTypeNode();
		new RunIllumina()   .getExperimentTypeNode();
		
		new QualityControl().getExperimentTypeNode();
		new Transfert()     .getExperimentTypeNode();
		new Purif()         .getExperimentTypeNode();

		new Nanopore()      .getExperimentTypeNode();
		
		new RunMGI()		.getExperimentTypeNode();

	}
	
	
}
