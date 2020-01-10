package services.description.common;

import static services.description.DescriptionFactory.getObjectTypes;
import static services.description.DescriptionFactory.newState;
import static services.description.DescriptionFactory.newStateHierarchy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import fr.cea.ig.lfw.utils.Iterables;
import models.laboratory.common.description.ObjectType;
import models.laboratory.common.description.State;
import models.laboratory.common.description.StateCategory;
import models.laboratory.common.description.StateHierarchy;
import models.laboratory.common.description.dao.StateCategoryDAO;
import models.utils.ModelDAOs;
import models.utils.dao.DAOException;
import ngl.refactoring.state.States;
import play.data.validation.ValidationError;

public class StateService {

	private static final play.Logger.ALogger logger = play.Logger.of(StateService.class);
	
	private final ModelDAOs mdao;
	
	@Inject
	public StateService(ModelDAOs mdao) {
		this.mdao = mdao;
	}
	
	public void saveData(Map<String, List<ValidationError>> errors) throws DAOException {		

//		DAOHelpers.removeAll(StateHierarchy.class, StateHierarchy.find.get());
//		DAOHelpers.removeAll(State         .class, State         .find.get());
//		DAOHelpers.removeAll(StateCategory .class, StateCategory .find.get());
		mdao.removeAll(StateHierarchy.class);
		mdao.removeAll(State         .class);
		mdao.removeAll(StateCategory .class);

		logger.debug("save categories");
		saveStateCategories(errors);	
		logger.debug("save states");
		saveStates         (errors);
		logger.debug("save state hierarchy");
		saveStatesHierarchy(errors);
		logger.debug("done saveData");
	}


	/**
	 * Creates state categories.
	 * @param errors        error manager
	 * @throws DAOException DAO problem
	 */
	public void saveStateCategories(Map<String,List<ValidationError>> errors) throws DAOException {
		List<StateCategory> l = new ArrayList<>();			
		for (StateCategory.CODE code : StateCategory.CODE.values()) 
//			l.add(DescriptionFactory.newSimpleCategory(StateCategory.class, code.name(), code.name()));
			l.add(new StateCategory(code, code.name()));
//		DAOHelpers.saveModels(StateCategory.class, l, errors);
		mdao.saveModels(StateCategory.class, l, errors);
	}


//	/**
//	 * Create states (in the database objects description).
//	 * @param errors        error manager
//	 * @throws DAOException DAO problem
//	 */
//	public void saveStates(Map<String, List<ValidationError>> errors) throws DAOException {
//		List<State> l = new ArrayList<>();
//		StateCategoryDAO scfind = StateCategory.find.get();
//		//l.add(newState("Not Defined", "ND", true, 0, StateCategory.find.findByCode("N"), getObjectTypes(ObjectType.CODE.Project.name(), ObjectType.CODE.Experiment.name(), ObjectType.CODE.Process.name(), ObjectType.CODE.Run.name(), ObjectType.CODE.ReadSet.name(), ObjectType.CODE.Sample.name(), ObjectType.CODE.Instrument.name(), ObjectType.CODE.Reagent.name(), ObjectType.CODE.Import.name(), ObjectType.CODE.Treatment.name(),ObjectType.CODE.Container.name(),ObjectType.CODE.Analysis.name()), false, "N"));		
//
//
//		l.add(newState("Disponible",   "A",  true, 1000, scfind.findByCode("N"), getObjectTypes(ObjectType.CODE.Container.name(), ObjectType.CODE.ReadSet.name()), true, "F"));
//		l.add(newState("Indisponible", "UA", true, 1001, scfind.findByCode("N"), getObjectTypes(ObjectType.CODE.Container.name(), ObjectType.CODE.ReadSet.name()), true, "F"));
//		l.add(newState("Terminé",      "F",  true, 1000, scfind.findByCode("F"), getObjectTypes(ObjectType.CODE.Container.name(), ObjectType.CODE.Project.name(),  ObjectType.CODE.Experiment.name(), ObjectType.CODE.Process.name(), ObjectType.CODE.Run.name(), ObjectType.CODE.Sample.name(), ObjectType.CODE.Instrument.name(), ObjectType.CODE.Reagent.name(), ObjectType.CODE.Import.name(), ObjectType.CODE.Treatment.name()), true, "F"));		
//
//
//		l.add(newState("Contrôle qualité en attente", "IW-QC", true, 400, scfind.findByCode("IW"), getObjectTypes( ObjectType.CODE.ReadSet.name()), true, "QC"));	
//		l.add(newState("Contrôle qualité en cours",   "IP-QC", true, 450, scfind.findByCode("IP"), getObjectTypes( ObjectType.CODE.ReadSet.name()), true, "QC"));
//		l.add(newState("Contrôle qualité terminé",    "F-QC",  true, 500, scfind.findByCode("F"),  getObjectTypes( ObjectType.CODE.ReadSet.name()), false, "QC"));
//		l.add(newState("Evaluation en attente",       "IW-V",  true, 800, scfind.findByCode("IW"), getObjectTypes(ObjectType.CODE.Run.name(), ObjectType.CODE.Analysis.name()), true, "V"));
//		l.add(newState("Evaluation en cours",         "IP-V",  true, 825, scfind.findByCode("IP"), getObjectTypes(ObjectType.CODE.Run.name()),  true, "V"));
//		l.add(newState("Evaluation terminée",         "F-V",   true, 849, scfind.findByCode("F"),  getObjectTypes(ObjectType.CODE.Run.name(), ObjectType.CODE.Analysis.name()), true, "V"));		
//
//		l.add(newState("En attente de Container",     "IW-C", true, -100, scfind.findByCode("IW"), getObjectTypes(ObjectType.CODE.Process.name()), true, null));		
//		l.add(newState("Nouveau",                     "N",    true, 0, scfind.findByCode("N"), getObjectTypes(ObjectType.CODE.Project.name(), ObjectType.CODE.Experiment.name(), ObjectType.CODE.Process.name(), ObjectType.CODE.Run.name(), ObjectType.CODE.ReadSet.name(), ObjectType.CODE.Sample.name(), ObjectType.CODE.Instrument.name(), ObjectType.CODE.Reagent.name(), ObjectType.CODE.Import.name(), ObjectType.CODE.Treatment.name(),ObjectType.CODE.Container.name(),ObjectType.CODE.Analysis.name(),ObjectType.CODE.SRASubmission.name(),ObjectType.CODE.SRAConfiguration.name()), true, "N"));		
//		l.add(newState("En cours",                    "IP",   true, 500, scfind.findByCode("IP"), getObjectTypes(ObjectType.CODE.Project.name(), ObjectType.CODE.Experiment.name(), ObjectType.CODE.Process.name(), ObjectType.CODE.Sample.name(), ObjectType.CODE.Instrument.name(), ObjectType.CODE.Reagent.name(), ObjectType.CODE.Import.name(), ObjectType.CODE.Treatment.name()), true, null));
//		l.add(newState("En attente de Processus",     "IW-P", true, 100, scfind.findByCode("IW"), getObjectTypes(ObjectType.CODE.Container.name()), true, null));
//		l.add(newState("Expérience en attente",       "IW-E", true, 200, scfind.findByCode("IW"), getObjectTypes(ObjectType.CODE.Container.name()), true, null));
//		l.add(newState("En cours d'utilisation",      "IU",   true, 250, scfind.findByCode("IP"), getObjectTypes(ObjectType.CODE.Container.name()), true, null));
//		l.add(newState("Dispatch en attente",         "IW-D", true, 300, scfind.findByCode("IW"), getObjectTypes(ObjectType.CODE.Container.name()), true, null));
//		l.add(newState("En stock",                    "IS",   true, 1000, scfind.findByCode("N"), getObjectTypes(ObjectType.CODE.Container.name()), true, null));
//		l.add(newState("Disponible transformation",   "A-TM", true, 900, scfind.findByCode("N"), getObjectTypes(ObjectType.CODE.Container.name()), true, null));
//		l.add(newState("Disponible controle qualité", "A-QC", true, 901, scfind.findByCode("N"), getObjectTypes(ObjectType.CODE.Container.name()), true, null));
//		l.add(newState("Disponible purif",            "A-PF", true, 902, scfind.findByCode("N"), getObjectTypes(ObjectType.CODE.Container.name()), true, null));
//		l.add(newState("Disponible transfert",        "A-TF", true, 903, scfind.findByCode("N"), getObjectTypes(ObjectType.CODE.Container.name()), true, null));
//
//		l.add(newState("Séquençage en cours", "IP-S", true, 150, scfind.findByCode("IP"), getObjectTypes(ObjectType.CODE.Run.name()), true, "S"));		
//		l.add(newState("Séquençage terminé",  "F-S", true,  195, scfind.findByCode("F"), getObjectTypes(ObjectType.CODE.Run.name()), false, "S"));	
//		l.add(newState("Séquençage en échec", "FE-S", true, 199, scfind.findByCode("F"), getObjectTypes(ObjectType.CODE.Run.name()), true, "S"));
//
//		l.add(newState("Read generation en attente", "IW-RG", true, 200, scfind.findByCode("IW"), getObjectTypes(ObjectType.CODE.Run.name()), true, "RG"));
//		l.add(newState("Read generation en cours",   "IP-RG", true, 250, scfind.findByCode("IP"), getObjectTypes(ObjectType.CODE.Run.name(), ObjectType.CODE.ReadSet.name()), true, "RG"));
//		l.add(newState("Read generation terminée",   "F-RG",  true, 299, scfind.findByCode("F"), getObjectTypes(ObjectType.CODE.Run.name(), ObjectType.CODE.ReadSet.name()), false, "RG"));
//
//		l.add(newState("EVAL. QC en attente", "IW-VQC", true, 650, scfind.findByCode("IW"), getObjectTypes(ObjectType.CODE.ReadSet.name()), true, "VQC"));
//		l.add(newState("EVAL. QC en cours",   "IP-VQC", true, 675, scfind.findByCode("IP"), getObjectTypes(ObjectType.CODE.ReadSet.name()), true, "VQC"));		
//		l.add(newState("EVAL. QC terminée",   "F-VQC",  true, 699, scfind.findByCode("F"),  getObjectTypes(ObjectType.CODE.ReadSet.name()), false, "VQC"));
//				
//		l.add(newState("Analyse BA en attente", "IW-BA", true, 700, scfind.findByCode("IW"), getObjectTypes(ObjectType.CODE.ReadSet.name()), true, "BA"));
//		l.add(newState("Analyse BA en cours",   "IP-BA", true, 750, scfind.findByCode("IP"), getObjectTypes(ObjectType.CODE.ReadSet.name(),ObjectType.CODE.Analysis.name()), true, "BA"));
//		l.add(newState("Analyse BA terminée",   "F-BA",  true, 799, scfind.findByCode("F"),  getObjectTypes(ObjectType.CODE.ReadSet.name(),ObjectType.CODE.Analysis.name()), false, "BA"));		
//		
//		l.add(newState("Transfert CCRT en attente", "IW-TF", true, 1101, scfind.findByCode("IW"), getObjectTypes(ObjectType.CODE.ReadSet.name()), true, "TF"));
//		l.add(newState("Transfert CCRT en cours",   "IP-TF", true, 1102, scfind.findByCode("IP"), getObjectTypes(ObjectType.CODE.ReadSet.name()), true, "TF"));		
//		l.add(newState("Transfert CCRT terminé",    "F-TF",  true, 1103, scfind.findByCode("F"), getObjectTypes(ObjectType.CODE.ReadSet.name()), false, "TF"));	
//		l.add(newState("Transfert CCRT en echec",   "FE-TF", true, 1103, scfind.findByCode("F"), getObjectTypes(ObjectType.CODE.ReadSet.name()), true, "TF"));	
//		
//		l.add(newState("EVAL. Analyse BA en attente", "IW-VBA", true, 800, scfind.findByCode("IW"), getObjectTypes(ObjectType.CODE.ReadSet.name()), true, "VBA"));
//		l.add(newState("EVAL. Analyse BA terminée",   "F-VBA",  true, 899, scfind.findByCode("F"), getObjectTypes(ObjectType.CODE.ReadSet.name()), false, "VBA"));		
//
//		l.add(newState("Soumission Validée utilisateur", 		   "V-SUB",    true, 2000, scfind.findByCode("IW"), getObjectTypes(ObjectType.CODE.SRASubmission.name(), ObjectType.CODE.SRAStudy.name(),ObjectType.CODE.SRASample.name(), ObjectType.CODE.SRAExperiment.name()), true, null));		
//		l.add(newState("Soumission en attente",                    "IW-SUB",   true, 2001, scfind.findByCode("IW"), getObjectTypes(ObjectType.CODE.SRASubmission.name(), ObjectType.CODE.SRAStudy.name(),ObjectType.CODE.SRASample.name(), ObjectType.CODE.SRAExperiment.name()), true, null));		
//		l.add(newState("Soumission en cours",            		   "IP-SUB",   true, 2002, scfind.findByCode("IP"), getObjectTypes(ObjectType.CODE.SRASubmission.name(),  ObjectType.CODE.SRAStudy.name(),ObjectType.CODE.SRASample.name(), ObjectType.CODE.SRAExperiment.name()), true, null));		
//		l.add(newState("Soumission terminée",           		   "F-SUB",    true, 2003, scfind.findByCode("F"), getObjectTypes(ObjectType.CODE.SRASubmission.name(),  ObjectType.CODE.SRAStudy.name(),ObjectType.CODE.SRASample.name(), ObjectType.CODE.SRAExperiment.name()), true, null));		
//		l.add(newState("Soumission en echec",            		   "FE-SUB",   true, 2004, scfind.findByCode("F"), getObjectTypes(ObjectType.CODE.SRASubmission.name(), ObjectType.CODE.SRAStudy.name(),ObjectType.CODE.SRASample.name(), ObjectType.CODE.SRAExperiment.name()), true, null));		
//		l.add(newState("Soumission pour release à New",            "N-R",      true, 2006, scfind.findByCode("N"), getObjectTypes(ObjectType.CODE.SRASubmission.name(), ObjectType.CODE.SRAStudy.name()), true, null));
//		l.add(newState("Soumission pour release study en attente", "IW-SUB-R", true, 2007, scfind.findByCode("IW"), getObjectTypes(ObjectType.CODE.SRASubmission.name(), ObjectType.CODE.SRAStudy.name()), true, null));		
//		l.add(newState("Soumission pour release study en cours",   "IP-SUB-R", true, 2008, scfind.findByCode("IP"), getObjectTypes(ObjectType.CODE.SRASubmission.name(), ObjectType.CODE.SRAStudy.name()), true, null));		
//		l.add(newState("Soumission pour release study en echec",   "FE-SUB-R", true, 2009, scfind.findByCode("F"), getObjectTypes(ObjectType.CODE.SRASubmission.name(), ObjectType.CODE.SRAStudy.name()), true, null));		
//
//		// DAOHelpers.saveModels(State.class, l, errors);
//		mdao.saveModels(State.class, l, errors);
//	}
	
	/**
	 * Create states (in the database objects description).
	 * @param errors        error manager
	 * @throws DAOException DAO problem
	 */
	public void saveStates(Map<String, List<ValidationError>> errors) throws DAOException {
		List<State> l = new ArrayList<>();
		StateCategoryDAO scfind = StateCategory.find.get();
		for (State s : States.values) {
			List<ObjectType> ts = Iterables.flatten(Iterables.map(s.objectTypes, t -> getObjectTypes(t.code))).toList();
//			List<ObjectType> ts = new ArrayList<>();
//			for (ObjectType t : s.objectTypes)
//				ts.addAll(getObjectTypes(t.code));
			l.add(newState(s.name, s.code, s.active, s.position, scfind.findByCode(s.category.code), ts, s.display, s.functionnalGroup));
		}		
		mdao.saveModels(State.class, l, errors);
	}

	/**
	 * Creates hierarchical relation between states in order to draw states flow (directive workflowChart)
	 * Graphic representation is made by the directive "workflowChart".
	 * @param errors        error manager
	 * @throws DAOException DAO problem
	 */
	public void saveStatesHierarchy(Map<String, List<ValidationError>> errors) throws DAOException{
		List<StateHierarchy> l = new ArrayList<>();

		// ReadSet
		l.add(newStateHierarchy("IP-RG",  "IP-RG",  ObjectType.CODE.ReadSet.name()));
		l.add(newStateHierarchy("F-RG",   "IP-RG",  ObjectType.CODE.ReadSet.name()));

		l.add(newStateHierarchy("IW-QC",  "F-RG",   ObjectType.CODE.ReadSet.name()));
		l.add(newStateHierarchy("IP-QC",  "IW-QC",  ObjectType.CODE.ReadSet.name()));
		l.add(newStateHierarchy("F-QC",   "IP-QC",  ObjectType.CODE.ReadSet.name()));

		l.add(newStateHierarchy("IW-VQC", "F-QC",   ObjectType.CODE.ReadSet.name()));
		l.add(newStateHierarchy("F-VQC",  "IW-VQC", ObjectType.CODE.ReadSet.name()));

		l.add(newStateHierarchy("IW-BA",  "F-VQC",  ObjectType.CODE.ReadSet.name()));
		l.add(newStateHierarchy("IP-BA",  "IW-BA",  ObjectType.CODE.ReadSet.name()));
		l.add(newStateHierarchy("F-BA",   "IP-BA",  ObjectType.CODE.ReadSet.name()));

		l.add(newStateHierarchy("IW-VBA", "F-BA",   ObjectType.CODE.ReadSet.name()));
		l.add(newStateHierarchy("F-VBA",  "IW-VBA", ObjectType.CODE.ReadSet.name()));

		l.add(newStateHierarchy("A",      "F-VBA",  ObjectType.CODE.ReadSet.name()));
		l.add(newStateHierarchy("UA",     "F-VBA",  ObjectType.CODE.ReadSet.name()));

		// Run
		l.add(newStateHierarchy("IP-S",   "IP-S",   ObjectType.CODE.Run.name()));
		l.add(newStateHierarchy("F-S",    "IP-S",   ObjectType.CODE.Run.name()));
		l.add(newStateHierarchy("FE-S",   "IP-S",   ObjectType.CODE.Run.name()));

		l.add(newStateHierarchy("IW-RG",  "F-S",    ObjectType.CODE.Run.name()));
		l.add(newStateHierarchy("IP-RG",  "IW-RG",  ObjectType.CODE.Run.name()));  //different with ReadSet
		l.add(newStateHierarchy("F-RG",   "IP-RG",  ObjectType.CODE.Run.name()));

		l.add(newStateHierarchy("IW-V",   "F-RG",   ObjectType.CODE.Run.name()));
		l.add(newStateHierarchy("IP-V",   "IW-V",   ObjectType.CODE.Run.name()));
		l.add(newStateHierarchy("F-V",    "IP-V",   ObjectType.CODE.Run.name()));

//		DAOHelpers.saveModels(StateHierarchy.class, l, errors);
		mdao.saveModels(StateHierarchy.class, l, errors);
	}

}
