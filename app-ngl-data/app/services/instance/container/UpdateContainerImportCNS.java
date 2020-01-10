package services.instance.container;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.ngl.NGLApplication;
import models.Constants;
import models.laboratory.container.instance.Container;
import models.utils.InstanceConstants;
import services.instance.AbstractImportDataCNS;
import validation.ContextValidation;
import validation.container.instance.ContainerValidationHelper;
import validation.utils.ValidationConstants;
import workflows.container.ContWorkflows;

public abstract class UpdateContainerImportCNS extends AbstractImportDataCNS {
	
//	@Inject
//	public UpdateContainerImportCNS(String name,FiniteDuration durationFromStart, FiniteDuration durationFromNextIteration, NGLContext ctx) {
//		super(name,durationFromStart, durationFromNextIteration, ctx);
//	}

//	protected UpdateContainerImportCNS(String name, NGLContext ctx) {
//		super(name, ctx);
//	}
	
	protected UpdateContainerImportCNS(String name, NGLApplication app) {
		super(name, app);
	}

	public void updateContainer(String sql,ContextValidation contextError,String containerCategoryCode,String experimentTypeCode) throws SQLException {
		List<Container> containers = limsServices.findContainersToCreate(sql,contextError, containerCategoryCode,null,experimentTypeCode);
		List<Container> containerUpdated = new ArrayList<>();
		for (Container containerUpdate : containers) {
			Container container = MongoDBDAO.findByCode(InstanceConstants.CONTAINER_COLL_NAME, Container.class,containerUpdate.code);
			if (container == null) {
				contextError.addError("container.code", ValidationConstants.ERROR_CODE_NOTEXISTS_MSG , containerUpdate.code);
				continue; // abort this container processing
			} else if (container.state.code != containerUpdate.state.code) {
				//Update state container
				ContextValidation contextValidation = ContextValidation.createUndefinedContext(Constants.NGL_DATA_USER);
				if (containerUpdate.state.code.equals("IS") && CollectionUtils.isNotEmpty(container.processCodes)) {
				//	contextValidation.addErrors("code", ValidationConstants.ERROR_VALUENOTAUTHORIZED_MSG, container.code);
					logger.warn("Le container "+container.code +" ne peut pas etre mise a l etat IS car elle a des processus");
				} else {
					//ContainerWorkflows.setContainerState(container, containerUpdate.state, contextValidation);
					contextValidation.putObject(ContainerValidationHelper.FIELD_STATE_CONTAINER_CONTEXT, "controllers");
					contextValidation.putObject(ContainerValidationHelper.FIELD_UPDATE_CONTAINER_SUPPORT_STATE, Boolean.TRUE);
					// Spring.get BeanOfType(ContWorkflows.class).setState(contextValidation, container, containerUpdate.state);
					app.injector().instanceOf(ContWorkflows.class).setState(contextValidation, container, containerUpdate.state, "controllers", true);
					contextValidation.removeObject(ContainerValidationHelper.FIELD_STATE_CONTAINER_CONTEXT);
					contextValidation.removeObject(ContainerValidationHelper.FIELD_UPDATE_CONTAINER_SUPPORT_STATE);
				}
				if (!contextValidation.hasErrors()) {
					containerUpdated.add(container);
				} else { 
					contextError.getErrors().putAll(contextValidation.getErrors());
				}
			}
			if (container.valuation == null || container.valuation.valid!=containerUpdate.valuation.valid) {
				MongoDBDAO.update(InstanceConstants.CONTAINER_COLL_NAME,Container.class, DBQuery.is("code", container.code),DBUpdate.set("valuation.valid", containerUpdate.valuation.valid));
			}
			limsServices.updateMaterielmanipLims(containerUpdated, contextError);
		}
	}

}
