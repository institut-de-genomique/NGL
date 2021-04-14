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
