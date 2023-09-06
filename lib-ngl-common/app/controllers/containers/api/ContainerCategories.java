package controllers.containers.api;


import java.util.List;

import javax.inject.Inject;

import controllers.NGLController;
import fr.cea.ig.authentication.Authenticated;
import fr.cea.ig.authorization.Authorized;
import fr.cea.ig.lfw.Historized;
import fr.cea.ig.lfw.utils.Iterables;
import fr.cea.ig.mongo.DBObjectConvertor;
import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.ngl.support.NGLForms;
import models.laboratory.container.description.ContainerCategory;
import models.utils.ListObject;
import models.utils.dao.DAOException;
import play.data.Form;
import play.mvc.Result;
import play.mvc.Results;
import views.components.datatable.DatatableResponse;

@Historized
public class ContainerCategories extends NGLController implements NGLForms, DBObjectConvertor {

	private final Form<ContainerCategoriesSearchForm> containerCategoriesTypeForm; 

	@Inject
	public ContainerCategories(NGLApplication app) {
		super(app);
		this.containerCategoriesTypeForm = app.formFactory().form(ContainerCategoriesSearchForm.class);
	}

	@Authenticated
	@Authorized.Read
	public Result list() {
		try {
			Form<ContainerCategoriesSearchForm>  containerCategoryFilledForm = filledFormQueryString(containerCategoriesTypeForm, ContainerCategoriesSearchForm.class);
			ContainerCategoriesSearchForm containerCategoriesSearch = containerCategoryFilledForm.get();
			try {
				List<ContainerCategory> containerCategories = ContainerCategory.find.get().findAll();

				if (containerCategoriesSearch.datatable) {
					return okAsJson(new DatatableResponse<>(containerCategories, containerCategories.size())); 
				} else if (containerCategoriesSearch.list) {
//					List<ListObject> lo = new ArrayList<>();
//					for (ContainerCategory cc : containerCategories) {
//						lo.add(new ListObject(cc.code, cc.name));
//					}
//					return okAsJson(lo);
					return okAsJson(Iterables.map(containerCategories, cc -> new ListObject(cc.code, cc.name)).toList());
				} else {
					return okAsJson(containerCategories);
				}
			} catch (DAOException e) {
				logger.error("DAO error: " + e.getMessage(),e);
				return  Results.internalServerError(e.getMessage());
			}
		} catch (Exception e) {
			getLogger().error(e.getMessage());
			return nglGlobalBadRequest(e.getMessage());
		}
	}

}
