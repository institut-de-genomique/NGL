package services.description.common;

import static services.description.DescriptionFactory.newLevel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import models.laboratory.common.description.Level;
import models.utils.dao.DAOException;
import models.utils.dao.DAOHelpers;
import play.data.validation.ValidationError;
public class LevelService {
	
	public static void main(Map<String, List<ValidationError>> errors) throws DAOException{		
		saveLevels(errors);		
	}
	
	public static void saveLevels(Map<String,List<ValidationError>> errors) throws DAOException{
		List<Level> l = new ArrayList<>();
		for (Level.CODE code : Level.CODE.values()) {
			l.add(newLevel(code.name(), code.name()));
		}
		DAOHelpers.saveModels(Level.class, l, errors);
	}
	
	
	public static List<Level> getLevels(Level.CODE...codes) throws DAOException {
		List<Level> levels = new ArrayList<>();
		for(Level.CODE code: codes){
			levels.add(Level.find.findByCode(code.name()));
		}
		return levels;
	}

}
