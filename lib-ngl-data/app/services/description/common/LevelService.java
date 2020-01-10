package services.description.common;

//import static services.description.DescriptionFactory.newLevel;

import java.util.List;

import javax.inject.Inject;

import fr.cea.ig.lfw.utils.Iterables;
import models.laboratory.common.description.Level;
import models.utils.ModelDAOs;
import models.utils.dao.DAOException;
import ngl.data.EnumService;

public class LevelService extends EnumService<Level.CODE,Level> {

	@Inject
	public LevelService(ModelDAOs mdao) {
//		super(mdao, Level.CODE.class, code -> newLevel(code.name(), code.name()));
		super(mdao, Level.CODE.class, code -> new Level(code));
	}

	public static List<Level> getLevels(Level.CODE... codes) throws DAOException {
//		List<Level> levels = new ArrayList<>();
//		for (Level.CODE code: codes)
//			levels.add(Level.find.get().findByCode(code.name()));
//		return levels;
		return Iterables.zenThem(codes)
				.map(code -> Level.find.get().findByCode(code.name()))
				.toList();
	}

}

//public class LevelService {
//	
//	public static void main(Map<String, List<ValidationError>> errors) throws DAOException {		
//		saveLevels(errors);		
//	}
//	
//	public static void saveLevels(Map<String,List<ValidationError>> errors) throws DAOException {
//		List<Level> l = new ArrayList<>();
//		for (Level.CODE code : Level.CODE.values()) {
//			l.add(newLevel(code.name(), code.name()));
//		}
//		DAOHelpers.saveModels(Level.class, l, errors);
//	}
//	
//	public static List<Level> getLevels(Level.CODE...codes) throws DAOException {
//		List<Level> levels = new ArrayList<>();
//		for(Level.CODE code: codes){
//			levels.add(Level.find.get().findByCode(code.name()));
//		}
//		return levels;
//	}
//
//}
