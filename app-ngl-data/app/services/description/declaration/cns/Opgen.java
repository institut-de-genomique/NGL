package services.description.declaration.cns;

import static services.description.DescriptionFactory.newExperimentType;
import static services.description.DescriptionFactory.newExperimentTypeNode;
import static services.description.DescriptionFactory.newPropertiesDefinition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import models.laboratory.common.description.Level;
import models.laboratory.common.description.PropertyDefinition;
import models.laboratory.experiment.description.ExperimentCategory;
import models.laboratory.experiment.description.ExperimentType;
import models.laboratory.processes.description.ProcessCategory;
import models.laboratory.processes.description.ProcessType;
import models.utils.dao.DAOException;
import services.description.Constants;
import services.description.DescriptionFactory;
import services.description.common.LevelService;
import services.description.declaration.AbstractDeclaration;

public class Opgen extends AbstractDeclaration {

	@Override
	protected List<ExperimentType> getExperimentTypeCommon() {
		List<ExperimentType> l = new ArrayList<>();
		//Depot Opgen
		l.add(newExperimentType("Ext to Run Opgen","ext-to-opgen-run",null, -1,
				ExperimentCategory.find.findByCode(ExperimentCategory.CODE.voidprocess.name()), getPropertyDefinitionExtToOpgenDepot(), null,"OneToOne", 
				DescriptionFactory.getInstitutes(Constants.CODE.CNS)));

		l.add(newExperimentType("Depot Opgen", "opgen-depot",null,3500,
				ExperimentCategory.find.findByCode(ExperimentCategory.CODE.transformation.name()), getPropertyDefinitionOpgenDepot(), 
				getInstrumentUsedTypes("ARGUS"), "ManyToOne", DescriptionFactory.getInstitutes(Constants.CODE.CNS)));

		return l;
	}
	
	@Override
	protected List<ExperimentType> getExperimentTypeDEV() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected List<ExperimentType> getExperimentTypePROD() {
		return null;
	}

	@Override
	protected List<ExperimentType> getExperimentTypeUAT() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	protected List<ProcessType> getProcessTypeCommon() {
		List<ProcessType> l=new ArrayList<>();

		l.add(DescriptionFactory.newProcessType("Run Opgen", "opgen-run", 
				ProcessCategory.find.findByCode("mapping"),105 , 
				null, 
				Arrays.asList(getPET("ext-to-opgen-run",-1), 
						getPET("opgen-depot",0)), 
						getExperimentTypes("opgen-depot").get(0),
						getExperimentTypes("opgen-depot").get(0), 
						getExperimentTypes("ext-to-opgen-run").get(0), 
						DescriptionFactory.getInstitutes(Constants.CODE.CNS)));
		return l;
	}

	@Override
	protected List<ProcessType> getProcessTypeDEV() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected List<ProcessType> getProcessTypePROD() {
		return null;
	}

	@Override
	protected List<ProcessType> getProcessTypeUAT() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	protected void getExperimentTypeNodeCommon() {
		newExperimentTypeNode("ext-to-opgen-run", getExperimentTypes("ext-to-opgen-run").get(0), false, false, false, null, null, null, null).save();
		newExperimentTypeNode("opgen-depot",getExperimentTypes("opgen-depot").get(0),false,false, false,getExperimentTypeNodes("ext-to-opgen-run"),null,null,null).save();
		
	}
	
	@Override
	protected void getExperimentTypeNodeDEV() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void getExperimentTypeNodePROD() {
	
	}

	@Override
	protected void getExperimentTypeNodeUAT() {
		// TODO Auto-generated method stub

	}
	
	private static List<PropertyDefinition> getPropertyDefinitionExtToOpgenDepot() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		propertyDefinitions.add(newPropertiesDefinition("Date réelle de dépôt", "runStartDate", LevelService.getLevels(Level.CODE.Experiment), Date.class, false, "single"));
		return propertyDefinitions;
	}	
	
	private static List<PropertyDefinition> getPropertyDefinitionOpgenDepot() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		propertyDefinitions.add(newPropertiesDefinition("Date réelle de dépôt", "runStartDate", LevelService.getLevels(Level.CODE.Experiment), Date.class, true, "single"));
		return propertyDefinitions;
	}

	


}
