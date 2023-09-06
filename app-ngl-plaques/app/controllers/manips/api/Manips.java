package controllers.manips.api;

import java.util.List;

import javax.inject.Inject;

import controllers.CommonController;
import controllers.MaterielManipSearch;
import fr.cea.ig.ngl.NGLApplication;
import lims.cns.dao.LimsManipDAO;
import lims.models.Manip;
import play.api.modules.spring.Spring;
import play.data.Form;
import play.libs.Json;
import play.mvc.Result;
import views.components.datatable.DatatableResponse;

public class Manips extends CommonController {

	private static final play.Logger.ALogger logger = play.Logger.of(Manips.class);
	
	private final Form<MaterielManipSearch> manipForm;
	
//	@Inject
//	public Manips(NGLContext ctx) {
////		this.ctx = ctx;
//		manipForm = ctx.form(MaterielManipSearch.class);
//	}
	
	@Inject
	public Manips(NGLApplication app) {
		manipForm = app.form(MaterielManipSearch.class);
	}

	public Result list() {
		Form<MaterielManipSearch> filledForm =  manipForm.bindFromRequest();
		LimsManipDAO  limsManipDAO = Spring.getBeanOfType(LimsManipDAO.class);
		logger.debug("Manip Form :"+filledForm.toString());
		List<Manip> manips = limsManipDAO.findManips(filledForm.get().etmanip,filledForm.get().emateriel, filledForm.get().project);
		logger.debug("Manips nb "+manips.size());
		return ok(Json.toJson(new DatatableResponse<>(manips, manips.size())));
	}

}
