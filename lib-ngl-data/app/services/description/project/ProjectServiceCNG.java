
package services.description.project;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import models.laboratory.common.description.Level;
import models.laboratory.common.description.PropertyDefinition;
import models.laboratory.project.description.ProjectCategory;
import models.laboratory.project.description.ProjectType;
import models.utils.dao.DAOException;
import models.utils.dao.DAOHelpers;
import play.data.validation.ValidationError;
import services.description.Constants;
import services.description.DescriptionFactory;
import services.description.common.LevelService;

public class ProjectServiceCNG extends AbstractProjectService {

	@Override
	public void saveProjectCategories(Map<String, List<ValidationError>> errors) throws DAOException {
		List<ProjectCategory> l = new ArrayList<>();
		l.add(new ProjectCategory("default", "defaut"));
		DAOHelpers.saveModels(ProjectCategory.class, l, errors);
	}

	@Override
	public void saveProjectTypes(Map<String, List<ValidationError>> errors) throws DAOException{
		List<ProjectType> l = new ArrayList<>();

		l.add(DescriptionFactory.newProjectType("Defaut", "default-project", ProjectCategory.find.get().findByCode("default"), getProjectPropertyDefinitions(), DescriptionFactory.getInstitutes(Constants.CODE.CNG)));

		DAOHelpers.saveModels(ProjectType.class, l, errors);
	}

	private static List<PropertyDefinition> getProjectPropertyDefinitions() throws DAOException {
		List<PropertyDefinition> pds = new ArrayList<>();
		pds.add(DescriptionFactory.newPropertiesDefinition("Synchronisation projet", "synchroProj", LevelService.getLevels(Level.CODE.Project), Boolean.class, true,"single"));
		pds.add(DescriptionFactory.newPropertiesDefinition("Id archive projet", "archiveHistory.projArchiveId", LevelService.getLevels(Level.CODE.Project), String.class, false,"object_list"));
		pds.add(DescriptionFactory.newPropertiesDefinition("Id archive scratch", "archiveHistory.scratchArchiveId", LevelService.getLevels(Level.CODE.Project), String.class, false,"object_list"));
		pds.add(DescriptionFactory.newPropertiesDefinition("Id archive raw", "archiveHistory.rawArchiveId", LevelService.getLevels(Level.CODE.Project), String.class, false,"object_list"));
		pds.add(DescriptionFactory.newPropertiesDefinition("Action", "archiveHistory.action", LevelService.getLevels(Level.CODE.Project), String.class, true, DescriptionFactory.newValues("ARCHIVAGE", "DESARCHIVAGE"), "object_list"));
		pds.add(DescriptionFactory.newPropertiesDefinition("Date archive", "archiveHistory.date", LevelService.getLevels(Level.CODE.Project), Date.class, true,"object_list"));
		pds.add(DescriptionFactory.newPropertiesDefinition("Quota du qtree proj", "qtreeQuota.proj", LevelService.getLevels(Level.CODE.Project), Integer.class, true,"object")); 
		pds.add(DescriptionFactory.newPropertiesDefinition("Quota du qtree rawdata", "qtreeQuota.rawdata", LevelService.getLevels(Level.CODE.Project), Integer.class, true,"object")); 
		pds.add(DescriptionFactory.newPropertiesDefinition("Quota du qtree scratch (Gb)", "qtreeQuota.scratch", LevelService.getLevels(Level.CODE.Project), Integer.class, true,"object")); 

		return pds;
	}
	
}



