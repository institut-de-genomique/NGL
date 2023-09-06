package controllers.experiments.api;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import controllers.APICommonController;
import controllers.authorisation.Permission;
import controllers.experiments.api.ExperimentTypeNodesSearchForm.ExperimentTypeNodesSearchParams;
import fr.cea.ig.ngl.NGLApplication;
import models.laboratory.processes.description.ExperimentTypeNode;
import models.utils.ListObject;
import models.utils.dao.DAOException;
import play.data.Form;
import play.libs.Json;
import play.mvc.Result;
import play.mvc.Results;
import views.components.datatable.DatatableResponse;

public class ExperimentTypeNodes extends APICommonController<ExperimentTypeNodesSearchForm> {

	private static final play.Logger.ALogger logger = play.Logger.of(ExperimentTypeNodes.class);
	
	private final Form<ExperimentTypeNodesSearchForm> experimentTypeNodeForm;
	
//	@Inject
//	public ExperimentTypeNodes(NGLContext ctx) {
//		super(ctx, ExperimentTypeNodesSearchForm.class);
//		experimentTypeNodeForm = ctx.form(ExperimentTypeNodesSearchForm.class);
//	}
	
	@Inject
	public ExperimentTypeNodes(NGLApplication ctx) {
		super(ctx, ExperimentTypeNodesSearchForm.class);
		experimentTypeNodeForm = ctx.form(ExperimentTypeNodesSearchForm.class);
	}

	@Permission(value={"reading"})
	public Result get(String code) {
		try {
			ExperimentTypeNode experimentTypeNode = ExperimentTypeNode.find.get().findByCode(code);
			if (experimentTypeNode == null) {
				return notFound();
			} else {
				return ok(Json.toJson(experimentTypeNode));
			}
			
		} catch (DAOException e) {
			return internalServerError(e.getMessage());
		}		
	}
	
	@Permission(value={"reading"})
	public Result list() throws DAOException{
		Form<ExperimentTypeNodesSearchForm>  experimentTypeNodeFilledForm = filledFormQueryString(experimentTypeNodeForm,ExperimentTypeNodesSearchForm.class);
		ExperimentTypeNodesSearchForm experimentTypeNodesSearch = experimentTypeNodeFilledForm.get();
		ExperimentTypeNodesSearchParams experimentTypeNodesParams = experimentTypeNodesSearch.getParams();
		try {
			List<ExperimentTypeNode> experimentTypeNodes = new ArrayList<>();
			
			if (experimentTypeNodesParams.isAtLeastOneParam()){
				experimentTypeNodes = ExperimentTypeNode.find.get().findBySearchParams(experimentTypeNodesParams);
			} else if(CollectionUtils.isNotEmpty(experimentTypeNodesSearch.codes)){
				experimentTypeNodes.addAll(ExperimentTypeNode.find.get().findByCodes(experimentTypeNodesSearch.codes));
			}else if(StringUtils.isNotBlank(experimentTypeNodesSearch.code)){
				experimentTypeNodes.add(ExperimentTypeNode.find.get().findByCode(experimentTypeNodesSearch.code));
			} else{
				experimentTypeNodes = ExperimentTypeNode.find.get().findAll();
			}
			
			if(experimentTypeNodesSearch.datatable){
				return ok(Json.toJson(new DatatableResponse<>(experimentTypeNodes, experimentTypeNodes.size()))); 
			}else if(experimentTypeNodesSearch.list){
				List<ListObject> lop = new ArrayList<>();
				for(ExperimentTypeNode et:experimentTypeNodes){
					lop.add(new ListObject(et.code, et.code));
				}
				return Results.ok(Json.toJson(lop));
			}else{
				return Results.ok(Json.toJson(experimentTypeNodes));
			}
		} catch (DAOException e) {
			logger.error("DAO error: "+e.getMessage(),e);
			return  Results.internalServerError(e.getMessage());
		}	
	}
}
