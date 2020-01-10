package services.description.common;

import services.description.DescriptionFactory;
import java.util.List;
import java.util.Map;
import models.laboratory.common.description.ObjectType;
import models.utils.dao.DAOException;
import models.utils.dao.DAOHelpers;
import play.data.validation.ValidationError;


public class ObjectTypeService {
	
	public static void main(Map<String, List<ValidationError>> errors) throws DAOException{
		
		//Can not be removed cause integrity constraints ...
		//DAOHelpers.removeAll(ObjectType.class, ObjectType.find);
		
		saveObjectTypes(errors);
	}
	

	
	public static void saveObjectTypes(Map<String,List<ValidationError>> errors) throws DAOException{		
			DAOHelpers.saveModel(ObjectType.class, DescriptionFactory.newDefaultObjectType(ObjectType.CODE.Container.name()), errors);
			DAOHelpers.saveModel(ObjectType.class, DescriptionFactory.newDefaultObjectType(ObjectType.CODE.Project.name() ), errors);
			DAOHelpers.saveModel(ObjectType.class, DescriptionFactory.newDefaultObjectType(ObjectType.CODE.Experiment.name() ), errors);
			DAOHelpers.saveModel(ObjectType.class, DescriptionFactory.newDefaultObjectType(ObjectType.CODE.Process.name() ), errors);
			DAOHelpers.saveModel(ObjectType.class, DescriptionFactory.newDefaultObjectType(ObjectType.CODE.Run.name() ), errors);
			DAOHelpers.saveModel(ObjectType.class, DescriptionFactory.newDefaultObjectType(ObjectType.CODE.ReadSet.name() ), errors);
			//DAOHelpers.saveModel(ObjectType.class, DescriptionFactory.newDefaultObjectType(ObjectType.CODE.File.name() ), errors);
			DAOHelpers.saveModel(ObjectType.class, DescriptionFactory.newDefaultObjectType(ObjectType.CODE.Sample.name() ), errors);
			DAOHelpers.saveModel(ObjectType.class, DescriptionFactory.newDefaultObjectType(ObjectType.CODE.Instrument.name() ), errors);
			DAOHelpers.saveModel(ObjectType.class, DescriptionFactory.newDefaultObjectType(ObjectType.CODE.Reagent.name() ), errors);
			DAOHelpers.saveModel(ObjectType.class, DescriptionFactory.newDefaultObjectType(ObjectType.CODE.Import.name() ), errors);
			DAOHelpers.saveModel(ObjectType.class,DescriptionFactory.newDefaultObjectType(ObjectType.CODE.Treatment.name() ), errors);
			DAOHelpers.saveModel(ObjectType.class,DescriptionFactory.newDefaultObjectType(ObjectType.CODE.Analysis.name() ), errors);
			DAOHelpers.saveModel(ObjectType.class,DescriptionFactory.newDefaultObjectType(ObjectType.CODE.SRASubmission.name() ), errors);
			DAOHelpers.saveModel(ObjectType.class,DescriptionFactory.newDefaultObjectType(ObjectType.CODE.SRAConfiguration.name() ), errors);
			DAOHelpers.saveModel(ObjectType.class,DescriptionFactory.newDefaultObjectType(ObjectType.CODE.SRAExperiment.name() ), errors);
			DAOHelpers.saveModel(ObjectType.class,DescriptionFactory.newDefaultObjectType(ObjectType.CODE.SRASample.name() ), errors);
			DAOHelpers.saveModel(ObjectType.class,DescriptionFactory.newDefaultObjectType(ObjectType.CODE.SRAStudy.name() ), errors);
	}
	
	
}
