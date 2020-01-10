package services.description.common;

import javax.inject.Inject;

import models.laboratory.common.description.ObjectType;
import models.utils.ModelDAOs;
import ngl.data.EnumService;

public class ObjectTypeService extends EnumService<ObjectType.CODE,ObjectType> {
	
	@Inject
	public ObjectTypeService(ModelDAOs mdao) {
//		super(mdao, ObjectType.CODE.class, code -> DescriptionFactory.newDefaultObjectType(code.name()));
		super(mdao, ObjectType.CODE.class, code -> new ObjectType(code));
	}
	
}

//public class ObjectTypeService {
//	
//	private static ObjectType objectType(ObjectType.CODE code) {
//		return DescriptionFactory.newDefaultObjectType(code.name());
//	}
//	
//	private final ModelDAOs mdao;
//	
//	@Inject
//	public ObjectTypeService(ModelDAOs mdao) {
//		this.mdao = mdao;
//	}
//	
//	public void saveObjectTypes(Map<String,List<ValidationError>> errors) throws DAOException {	
//		for (ObjectType.CODE code : ObjectType.CODE.values())
//			mdao.saveModel(ObjectType.class, objectType(code), errors);
//	}
//		
//}




//class ObjectTypes {
//	
//	private static ObjectType objectType(ObjectType.CODE code) {
//		return DescriptionFactory.newDefaultObjectType(code.name());
//	}
//	
//	public static final List<ObjectType> types;
//	
//	public static final ObjectType
//		Container,
//		Project,
//		Experiment,
//		Process,
//		Run,
//		ReadSet,
////      File,
//		Sample,
//		Instrument,
//		Reagent,
//		Import,
//		Treatment,
//		Analysis,
//		SRASubmission,
//		SRAConfiguration,
//		SRAExperiment,
//		SRASample,
//		SRAStudy;
////				DAOHelpers.saveModel(ObjectType.class, DescriptionFactory.newDefault.name() ),         errors);
////		;
//	static {
//		types = Arrays.asList(
//				Container        = objectType(ObjectType.CODE.Container),
//				Project          = objectType(ObjectType.CODE.Project),
//				Experiment       = objectType(ObjectType.CODE.Experiment),
//				Process          = objectType(ObjectType.CODE.Process),
//				Run              = objectType(ObjectType.CODE.Run),
//				ReadSet          = objectType(ObjectType.CODE.ReadSet),
//				Sample           = objectType(ObjectType.CODE.Sample),
//				Instrument       = objectType(ObjectType.CODE.Instrument),
//				Reagent          = objectType(ObjectType.CODE.Reagent),
//				Import           = objectType(ObjectType.CODE.Import),
//				Treatment        = objectType(ObjectType.CODE.Treatment),
//				Analysis         = objectType(ObjectType.CODE.Analysis),
//				SRASubmission    = objectType(ObjectType.CODE.SRASubmission),
//				SRAConfiguration = objectType(ObjectType.CODE.SRAConfiguration),
//				SRAExperiment    = objectType(ObjectType.CODE.SRAExperiment),
//				SRASample        = objectType(ObjectType.CODE.SRASample),
//				SRAStudy         = objectType(ObjectType.CODE.SRAStudy)
//			);
//	}
//}
//
//public class ObjectTypeService {
//	
//	private static ObjectType objectType(ObjectType.CODE code) {
//		return DescriptionFactory.newDefaultObjectType(code.name());
//	}
//	
//	private static final List<ObjectType> types = Arrays.asList(
//			objectType(ObjectType.CODE.Container),
//			objectType(ObjectType.CODE.Project),
//			objectType(ObjectType.CODE.Experiment),
//			objectType(ObjectType.CODE.Process),
//			objectType(ObjectType.CODE.Run),
//			objectType(ObjectType.CODE.ReadSet),
//			objectType(ObjectType.CODE.Sample),
//			objectType(ObjectType.CODE.Instrument),
//			objectType(ObjectType.CODE.Reagent),
//			objectType(ObjectType.CODE.Import),
//			objectType(ObjectType.CODE.Treatment),
//			objectType(ObjectType.CODE.Analysis),
//			objectType(ObjectType.CODE.SRASubmission),
//			objectType(ObjectType.CODE.SRAConfiguration),
//			objectType(ObjectType.CODE.SRAExperiment),
//			objectType(ObjectType.CODE.SRASample),
//			objectType(ObjectType.CODE.SRAStudy)
//		);
//
//	private final ModelDAOs mdao;
//	
//	public ObjectTypeService(ModelDAOs mdao) {
//		this.mdao = mdao;
//	}
////	public static void main(Map<String, List<ValidationError>> errors) throws DAOException {
////		
////		//Can not be removed cause integrity constraints ...
////		//DAOHelpers.removeAll(ObjectType.class, ObjectType.find);
////		
////		saveObjectTypes(errors);
////	}
//	
////	public void saveObjectTypes(Map<String,List<ValidationError>> errors) throws DAOException {		
//////		DAOHelpers.saveModel(ObjectType.class, DescriptionFactory.newDefaultObjectType(ObjectType.CODE.Container.name()),         errors);
//////		DAOHelpers.saveModel(ObjectType.class, DescriptionFactory.newDefaultObjectType(ObjectType.CODE.Project.name() ),          errors);
//////		DAOHelpers.saveModel(ObjectType.class, DescriptionFactory.newDefaultObjectType(ObjectType.CODE.Experiment.name() ),       errors);
//////		DAOHelpers.saveModel(ObjectType.class, DescriptionFactory.newDefaultObjectType(ObjectType.CODE.Process.name() ),          errors);
//////		DAOHelpers.saveModel(ObjectType.class, DescriptionFactory.newDefaultObjectType(ObjectType.CODE.Run.name() ),              errors);
//////		DAOHelpers.saveModel(ObjectType.class, DescriptionFactory.newDefaultObjectType(ObjectType.CODE.ReadSet.name() ),          errors);
//////		//DAOHelpers.saveModel(ObjectType.class, DescriptionFactory.newDefaultObjectType(ObjectType.CODE.File.name() ), errors);
//////		DAOHelpers.saveModel(ObjectType.class, DescriptionFactory.newDefaultObjectType(ObjectType.CODE.Sample.name() ),           errors);
//////		DAOHelpers.saveModel(ObjectType.class, DescriptionFactory.newDefaultObjectType(ObjectType.CODE.Instrument.name() ),       errors);
//////		DAOHelpers.saveModel(ObjectType.class, DescriptionFactory.newDefaultObjectType(ObjectType.CODE.Reagent.name() ),          errors);
//////		DAOHelpers.saveModel(ObjectType.class, DescriptionFactory.newDefaultObjectType(ObjectType.CODE.Import.name() ),           errors);
//////		DAOHelpers.saveModel(ObjectType.class, DescriptionFactory.newDefaultObjectType(ObjectType.CODE.Treatment.name() ),        errors);
//////		DAOHelpers.saveModel(ObjectType.class, DescriptionFactory.newDefaultObjectType(ObjectType.CODE.Analysis.name() ),         errors);
//////		DAOHelpers.saveModel(ObjectType.class, DescriptionFactory.newDefaultObjectType(ObjectType.CODE.SRASubmission.name() ),    errors);
//////		DAOHelpers.saveModel(ObjectType.class, DescriptionFactory.newDefaultObjectType(ObjectType.CODE.SRAConfiguration.name() ), errors);
//////		DAOHelpers.saveModel(ObjectType.class, DescriptionFactory.newDefaultObjectType(ObjectType.CODE.SRAExperiment.name() ),    errors);
//////		DAOHelpers.saveModel(ObjectType.class, DescriptionFactory.newDefaultObjectType(ObjectType.CODE.SRASample.name() ),        errors);
//////		DAOHelpers.saveModel(ObjectType.class, DescriptionFactory.newDefaultObjectType(ObjectType.CODE.SRAStudy.name() ),         errors);
////		mdao.saveModels(ObjectType.class, types, errors);
////	}
//	public void saveObjectTypes(Map<String,List<ValidationError>> errors) throws DAOException {	
//		for (ObjectType.CODE code : ObjectType.CODE.values())
//			mdao.saveModel(ObjectType.class, objectType(code), errors);
//	}
//		
//}

