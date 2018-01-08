package services.description.container;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.typesafe.config.ConfigFactory;

import models.laboratory.container.description.ContainerCategory;
import models.laboratory.container.description.ContainerSupportCategory;
import models.utils.dao.DAOException;
import models.utils.dao.DAOHelpers;
import play.data.validation.ValidationError;
import services.description.DescriptionFactory;
import static services.description.DescriptionFactory.*;

public class ContainerService {
	
	public static void main(Map<String, List<ValidationError>> errors) throws DAOException{
		DAOHelpers.removeAll(ContainerSupportCategory.class, ContainerSupportCategory.find);
		DAOHelpers.removeAll(ContainerCategory.class, ContainerCategory.find);

		saveContainerCategories(errors);
		saveContainerSupportCategories(errors);

	}
	
	
	/**
	 * Save All container categories
	 * @param errors
	 * @throws DAOException 
	 */
	public static void saveContainerCategories(Map<String, List<ValidationError>> errors) throws DAOException {
		List<ContainerCategory> l = new ArrayList<ContainerCategory>();
//		l.add(DescriptionFactory.newSimpleCategory(ContainerCategory.class, "Bouteille", "bottle"));
//		l.add(DescriptionFactory.newSimpleCategory(ContainerCategory.class, "Sachet", "bag"));
//		l.add(DescriptionFactory.newSimpleCategory(ContainerCategory.class, "Lame de pétri", "petrislide"));
		l.add(DescriptionFactory.newSimpleCategory(ContainerCategory.class, "Tube", "tube"));
		l.add(DescriptionFactory.newSimpleCategory(ContainerCategory.class, "Puit", "well"));		
		l.add(DescriptionFactory.newSimpleCategory(ContainerCategory.class, "Lane", "lane"));
//		l.add(DescriptionFactory.newSimpleCategory(ContainerCategory.class, "MapCard", "mapcard"));		
		l.add(DescriptionFactory.newSimpleCategory(ContainerCategory.class, "Void", "void"));	
		l.add(DescriptionFactory.newSimpleCategory(ContainerCategory.class, "SMRTCell", "smrtcell"));
//		l.add(DescriptionFactory.newSimpleCategory(ContainerCategory.class, "Irys FC", "irys-fc"));	
		l.add(DescriptionFactory.newSimpleCategory(ContainerCategory.class, "Autre", "other"));	
		DAOHelpers.saveModels(ContainerCategory.class, l, errors);
	}

	/**
	 * Save All support categories
	 * @param errors
	 * @throws DAOException 
	 */
	public static void saveContainerSupportCategories(Map<String, List<ValidationError>> errors) throws DAOException {
		List<ContainerSupportCategory> l = new ArrayList<ContainerSupportCategory>();
		l.add(newContainerSupportCategory("Tube", "tube", 1, 1, 1, ContainerCategory.find.findByCode("tube")));
//		l.add(newContainerSupportCategory("Bouteille", "bottle", 1, 1, 1, ContainerCategory.find.findByCode("bottle")));
//		l.add(newContainerSupportCategory("Sachet", "bag", 1, 1, 1, ContainerCategory.find.findByCode("bag")));
//		l.add(newContainerSupportCategory("Lame de pétri", "petrislide", 1, 1, 1, ContainerCategory.find.findByCode("petrislide")));		
//		l.add(newContainerSupportCategory("MapCard", "mapcard", 1, 1, 1, ContainerCategory.find.findByCode("mapcard")));
		l.add(newContainerSupportCategory("Plaque 96", "96-well-plate", 12, 8, 96, ContainerCategory.find.findByCode("well")));
		l.add(newContainerSupportCategory("Plaque 384", "384-well-plate", 24, 96, 384, ContainerCategory.find.findByCode("well")));
		l.add(newContainerSupportCategory("Flowcell 8", "flowcell-8", 8, 1, 8, ContainerCategory.find.findByCode("lane")));
		l.add(newContainerSupportCategory("Flowcell 4", "flowcell-4", 4, 1, 4, ContainerCategory.find.findByCode("lane")));
		l.add(newContainerSupportCategory("Flowcell 2", "flowcell-2", 2, 1, 2, ContainerCategory.find.findByCode("lane")));
		l.add(newContainerSupportCategory("Flowcell 1", "flowcell-1", 1, 1, 1, ContainerCategory.find.findByCode("lane")));
		l.add(newContainerSupportCategory("Flowcell S1", "flowcell-S1", 2, 1, 2, ContainerCategory.find.findByCode("lane")));
		l.add(newContainerSupportCategory("Flowcell S2", "flowcell-S2", 2, 1, 2, ContainerCategory.find.findByCode("lane")));
		l.add(newContainerSupportCategory("Flowcell S4", "flowcell-S4", 4, 1, 4, ContainerCategory.find.findByCode("lane")));
		l.add(newContainerSupportCategory("SMRT Cell", "smrtcell-150k", 1, 1, 1, ContainerCategory.find.findByCode("smrtcell")));
		l.add(newContainerSupportCategory("Void", "void", 0, 0, 0, ContainerCategory.find.findByCode("void")));
//		l.add(newContainerSupportCategory("Irys Chip", "irys-chip-2", 2, 1, 1, ContainerCategory.find.findByCode("irys-fc")));
		l.add(newContainerSupportCategory("Autre", "other", 1, 1, 1, ContainerCategory.find.findByCode("other")));
		DAOHelpers.saveModels(ContainerSupportCategory.class, l, errors);
		
	}
}
