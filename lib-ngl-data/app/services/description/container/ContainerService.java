package services.description.container;

import static services.description.DescriptionFactory.newContainerSupportCategory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import models.laboratory.container.description.ContainerCategory;
import models.laboratory.container.description.ContainerSupportCategory;
import models.laboratory.container.description.dao.ContainerCategoryDAO;
import models.utils.ModelDAOs;
import models.utils.dao.DAOException;
import play.data.validation.ValidationError;

public class ContainerService {
	
	private final ModelDAOs mdao;
	
	public ContainerService(ModelDAOs mdao) {
		this.mdao = mdao;
	}
	
	public void saveData(Map<String, List<ValidationError>> errors) throws DAOException{
		mdao.removeAll(ContainerSupportCategory.class);
		mdao.removeAll(ContainerCategory       .class);
		saveContainerCategories       (errors);
		saveContainerSupportCategories(errors);
	}
	
	/**
	 * Save All container categories.
	 * @param errors        error manager
	 * @throws DAOException DAO problem
	 */
	public void saveContainerCategories(Map<String, List<ValidationError>> errors) throws DAOException {
		List<ContainerCategory> l = new ArrayList<>();
		l.add(new ContainerCategory("tube",       "Tube"));
		l.add(new ContainerCategory("bottle",     "Bouteille"));
		l.add(new ContainerCategory("bag",        "Sachet"));
		l.add(new ContainerCategory("petrislide", "Lame de pétri"));
		l.add(new ContainerCategory("well",       "Puit"));		
		l.add(new ContainerCategory("lane",       "Lane"));
		l.add(new ContainerCategory("mapcard",    "MapCard"));		
		l.add(new ContainerCategory("void",       "Void"));	
		l.add(new ContainerCategory("irys-fc",    "Irys FC"));	
		l.add(new ContainerCategory("saphyr-fc",  "Saphyr FC"));		
		l.add(new ContainerCategory("other",      "Autre"));	
		mdao.saveModels(ContainerCategory.class, l, errors);
	}

	/**
	 * Save All support categories.
	 * @param errors        error manager
	 * @throws DAOException DAO problem
	 */
	public void saveContainerSupportCategories(Map<String, List<ValidationError>> errors) throws DAOException {
		ContainerCategoryDAO ccfind = ContainerCategory.find.get();
		List<ContainerSupportCategory> l = new ArrayList<>();
		l.add(newContainerSupportCategory("Tube",          "tube",            1,  1,   1, ccfind.findByCode("tube")));
		l.add(newContainerSupportCategory("Strip 8",       "strip-8",         1,  8,   8, ccfind.findByCode("tube"))); // FDS ajout 21/02/2017 pour Chromium
		l.add(newContainerSupportCategory("Bouteille",     "bottle",          1,  1,   1, ccfind.findByCode("bottle")));
		l.add(newContainerSupportCategory("Sachet",        "bag",             1,  1,   1, ccfind.findByCode("bag")));
		l.add(newContainerSupportCategory("Lame de pétri", "petrislide",      1,  1,   1, ccfind.findByCode("petrislide")));		
		l.add(newContainerSupportCategory("MapCard",       "mapcard",         1,  1,   1, ccfind.findByCode("mapcard")));
		l.add(newContainerSupportCategory("Plaque 96",     "96-well-plate",  12,  8,  96, ccfind.findByCode("well")));
		l.add(newContainerSupportCategory("Plaque 384",    "384-well-plate", 24, 96, 384, ccfind.findByCode("well")));
		l.add(newContainerSupportCategory("Flowcell 8 pistes",    "flowcell-8",      8,  1,   8, ccfind.findByCode("lane")));//FDS NGL-2393 ajout "pistes" dans  le label
		l.add(newContainerSupportCategory("Flowcell 4 pistes",    "flowcell-4",      4,  1,   4, ccfind.findByCode("lane")));//FDS NGL-2393 ajout "pistes" dans  le label
		l.add(newContainerSupportCategory("Flowcell 2 pistes",    "flowcell-2",      2,  1,   2, ccfind.findByCode("lane")));//FDS NGL-2393 ajout "pistes" dans  le label
		l.add(newContainerSupportCategory("Flowcell 1 piste",     "flowcell-1",      1,  1,   1, ccfind.findByCode("lane")));//FDS NGL-2393 ajout "piste"  dans  le label
		l.add(newContainerSupportCategory("Void",          "void",            0,  0,   0, ccfind.findByCode("void")));
		l.add(newContainerSupportCategory("Irys Chip",     "irys-chip-2",     2,  1,   1, ccfind.findByCode("irys-fc")));
		l.add(newContainerSupportCategory("Saphyr Chip",   "saphyr-chip",     2,  1,   2, ccfind.findByCode("saphyr-fc")));
		l.add(newContainerSupportCategory("Autre",         "other",           1,  1,   1, ccfind.findByCode("other")));
		mdao.saveModels(ContainerSupportCategory.class, l, errors);
	}
	
}
