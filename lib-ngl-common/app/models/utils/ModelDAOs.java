package models.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import javax.inject.Inject;

import fr.cea.ig.play.IGGlobals;
import models.administration.authorisation.Permission;
import models.administration.authorisation.Role;
import models.administration.authorisation.User;
import models.laboratory.common.description.CommonInfoType;
import models.laboratory.common.description.Institute;
import models.laboratory.common.description.Level;
import models.laboratory.common.description.MeasureCategory;
import models.laboratory.common.description.MeasureUnit;
import models.laboratory.common.description.ObjectType;
import models.laboratory.common.description.PropertyDefinition;
import models.laboratory.common.description.State;
import models.laboratory.common.description.StateCategory;
import models.laboratory.common.description.StateHierarchy;
import models.laboratory.common.description.Value;
import models.laboratory.container.description.ContainerCategory;
import models.laboratory.container.description.ContainerSupportCategory;
import models.laboratory.experiment.description.ExperimentCategory;
import models.laboratory.experiment.description.ExperimentType;
import models.laboratory.experiment.description.ProtocolCategory;
import models.laboratory.instrument.description.InstrumentCategory;
import models.laboratory.instrument.description.InstrumentUsedType;
import models.laboratory.processes.description.ExperimentTypeNode;
import models.laboratory.processes.description.ProcessCategory;
import models.laboratory.processes.description.ProcessType;
import models.laboratory.project.description.ProjectCategory;
import models.laboratory.project.description.ProjectType;
import models.laboratory.project.description.UmbrellaProjectCategory;
import models.laboratory.project.description.UmbrellaProjectType;
import models.laboratory.run.description.AnalysisType;
import models.laboratory.run.description.ReadSetType;
import models.laboratory.run.description.RunCategory;
import models.laboratory.run.description.RunType;
import models.laboratory.run.description.TreatmentCategory;
import models.laboratory.run.description.TreatmentContext;
import models.laboratory.run.description.TreatmentType;
import models.laboratory.sample.description.ImportCategory;
import models.laboratory.sample.description.ImportType;
import models.laboratory.sample.description.SampleCategory;
import models.laboratory.sample.description.SampleType;
import models.utils.dao.AbstractDAO;
import models.utils.dao.DAOException;
import play.data.validation.ValidationError;

/**
 * ModelDAO and Finder instances catalog.
 * Centralized access that provides an easy to use persistence access. 
 * 
 * @author vrd
 *
 */
public class ModelDAOs {
	
	private static final play.Logger.ALogger logger = play.Logger.of(ModelDAOs.class);
	
	public static Supplier<ModelDAOs> instance = () -> IGGlobals.injector().instanceOf(ModelDAOs.class);
	
	@SuppressWarnings("rawtypes")
	private Map<Class,Supplier<AbstractDAO>> map;
	
	@Inject
	public ModelDAOs() {
		map = new HashMap<>();
		// Build the class to DAO mapping so we can replace all the calls
		// from o.save() to daos.save(o) (or other method calls really).
		// AbstractCategory
		registerDAO(ContainerCategory       .class, ContainerCategory       .find);
		registerDAO(ContainerSupportCategory.class, ContainerSupportCategory.find);
		registerDAO(ExperimentCategory      .class, ExperimentCategory      .find);
		registerDAO(ImportCategory          .class, ImportCategory          .find);
		registerDAO(InstrumentCategory      .class, InstrumentCategory      .find);
		registerDAO(MeasureCategory         .class, MeasureCategory         .find);
		registerDAO(ProcessCategory         .class, ProcessCategory         .find);
		registerDAO(UmbrellaProjectCategory .class, UmbrellaProjectCategory .find);
		registerDAO(ProjectCategory         .class, ProjectCategory         .find);
		registerDAO(ProtocolCategory        .class, ProtocolCategory        .find);
		registerDAO(RunCategory             .class, RunCategory             .find);
		registerDAO(SampleCategory          .class, SampleCategory          .find);
		registerDAO(StateCategory           .class, StateCategory           .find);
		registerDAO(TreatmentCategory       .class, TreatmentCategory       .find);
		// CommonInfoType
		registerDAO(CommonInfoType          .class, CommonInfoType          .find);
		registerDAO(AnalysisType            .class, AnalysisType            .find);
		registerDAO(ExperimentType          .class, ExperimentType          .find);
		registerDAO(ImportType              .class, ImportType              .find);
		registerDAO(InstrumentUsedType      .class, InstrumentUsedType      .find);
		registerDAO(ProcessType             .class, ProcessType             .find);
		registerDAO(UmbrellaProjectType     .class, UmbrellaProjectType     .find);
		registerDAO(ProjectType             .class, ProjectType             .find);
		registerDAO(ReadSetType             .class, ReadSetType             .find);
		registerDAO(RunType                 .class, RunType                 .find);
		registerDAO(SampleType              .class, SampleType              .find);
		registerDAO(TreatmentType           .class, TreatmentType           .find);		
		// Model
		registerDAO(ExperimentTypeNode      .class, ExperimentTypeNode      .find);	
		registerDAO(Institute               .class, Institute               .find);
		registerDAO(Level                   .class, Level                   .find);
		registerDAO(MeasureUnit             .class, MeasureUnit             .find);
		registerDAO(ObjectType              .class, ObjectType              .find);
		registerDAO(Permission              .class, Permission              .find);
		registerDAO(PropertyDefinition      .class, PropertyDefinition      .find);		
		registerDAO(Role                    .class, Role                    .find);
		registerDAO(State                   .class, State                   .find);
		registerDAO(StateHierarchy          .class, StateHierarchy          .find);
		registerDAO(TreatmentContext        .class, TreatmentContext        .find);
		registerDAO(User                    .class, User                    .find);
		registerDAO(Value                   .class, Value                   .find);
	}
	
	@SuppressWarnings("rawtypes")
	private <T,U extends AbstractDAO<T>> void registerDAO(Class<T> c, Supplier<U> dao) {
		map.put(c, (Supplier<AbstractDAO>)(() -> dao.get()));
	}
	
	private <T> AbstractDAO<T> getDAOForClass(Class<T> c) {
		if (c == null)
			throw new RuntimeException("null argument class");
		@SuppressWarnings("rawtypes")
		Supplier<AbstractDAO> sDao = map.get(c);
		if (sDao == null)
			throw new RuntimeException("no DAO is defined for " + c);
		// The following line produces a warning with javac but not in eclipse
		AbstractDAO<T> dao = sDao.get();
		if (dao == null)
			throw new RuntimeException("dao supplier failed to provide an instance : " + sDao + " for " + c + " (get:" + sDao.get() + ")");
		logger.debug("dao for class {} : {}", c, dao);
		return dao;
	}
	
	private <T> AbstractDAO<T> getDAOForInstance(T t) {
		@SuppressWarnings("unchecked") 
		Class<T> c = (Class<T>)t.getClass(); // t is an instance of exactly T
		return getDAOForClass(c);
	}
	
	// We expose the AbstractDAO methods.
	public <T extends Model> void remove(T t) throws DAOException	{
		getDAOForInstance(t).remove(t);
	}

	public <T> List<T> findAll(Class<T> c) throws DAOException {
		return getDAOForClass(c).findAll();
	}
		
	public <T> T findById(Class<T> c, Long id) throws DAOException {
		return getDAOForClass(c).findById(id);
	}
	
	public <T extends Model> T findByCode(Class<T> c, String code) throws DAOException {
		return getDAOForClass(c).findByCode(code);
	}
	
	public <T> T findByCodeOrName(Class<T> c, String code) throws DAOException {
		return getDAOForClass(c).findByCodeOrName(code);
	}

	public <T extends Model> List<T> findByCodes(Class<T> c, List<String> codes) throws DAOException {
		return getDAOForClass(c).findByCodes(codes);
	}

	public <T> long save(T value) throws DAOException {
		return getDAOForInstance(value).save(value);
	}

	public <T extends Model> void update(T value) throws DAOException {
		getDAOForInstance(value).update(value);
	}

	public <T extends Model> T fromDB(T m) throws DAOException {
//		@SuppressWarnings("unchecked")
//		T r  = findByCode((Class<T>)m.getClass(), m.code);
//		return r;
		@SuppressWarnings("unchecked")
		T r  = fromDB((Class<T>)m.getClass(), m);
		return r;
	}
	
	public <T extends Model> T fromDB(Class<T> t, T m) throws DAOException {
		T r  = findByCode(t, m.code);
		return r;
	}

	// -- DAOHelper replication
	
	@SuppressWarnings("unchecked")
	public <T extends Model> void saveModel(T model, Map<String,List<ValidationError>> errors) throws DAOException {
		saveModel((Class<T>)model.getClass(), model, errors);
	}
	
	// This does not gather any error. 
	public <T extends Model> void saveModel(Class<T> type, T model, Map<String,List<ValidationError>> errors) throws DAOException {
		T t = fromDB(type,model);
		logger.debug("saveModel - fromDB {} ({}) : {}", model, model.code, t);
		if (t == null) {
			logger.debug("saveModel - {} : {}", type, model.code);
			save(model);
		} else {
			logger.debug("saveModel - already exists {} : {}", type, model.code);
		}
	}

	public <T extends Model> void saveModels(Class<T> type, List<T> models, Map<String,List<ValidationError>> errors) throws DAOException {
		for (T model : models)
			saveModel(type, model, errors);
	}

	public <T extends Model> void removeAll(Class<T> c) throws DAOException {
		AbstractDAO<T> finder = getDAOForClass(c);
		List<T> list = finder.findAll();
		logger.debug("removeAll - {} : {} instances", c, list.size());
		for (T t : list) {
			logger.debug("removeAll - {} {}", c, t.code);
			finder.remove(t);
		}
	}

}