package controllers.instruments.api;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import controllers.APICommonController;
import fr.cea.ig.ngl.NGLApplication;
import models.laboratory.instrument.description.Instrument;
import models.laboratory.instrument.description.InstrumentUsedType;
import models.utils.dao.DAOException;
import play.data.Form;
import play.libs.Json;
import play.mvc.Result;
import play.mvc.Results;
import views.components.datatable.DatatableResponse;

public class Instruments extends APICommonController<InstrumentsSearchForm> {

	private static final play.Logger.ALogger logger = play.Logger.of(Instruments.class);
	
	private final Form<InstrumentsSearchForm> instrumentSearchForm;
	private final Form<Instrument>            instrumentForm;

//	@Inject
//	public Instruments(NGLContext ctx) {
//		super(ctx, InstrumentsSearchForm.class);
//		instrumentSearchForm = ctx.form(InstrumentsSearchForm.class);
//		instrumentForm = ctx.form(Instrument.class);
//	}
	
	@Inject
	public Instruments(NGLApplication app) {
		super(app, InstrumentsSearchForm.class);
		instrumentSearchForm = app.form(InstrumentsSearchForm.class);
		instrumentForm       = app.form(Instrument.class);
	}

	public Result list() throws DAOException {
		Form<InstrumentsSearchForm> instrumentTypeFilledForm = filledFormQueryString(instrumentSearchForm,InstrumentsSearchForm.class);
		InstrumentsSearchForm instrumentsQueryParams = instrumentTypeFilledForm.get();

		List<Instrument> instruments = new ArrayList<>();
		try {		
			if (instrumentsQueryParams.experimentType != null || instrumentsQueryParams.experimentTypes != null) {
				instruments = Instrument.find.get().findByExperimentTypeQueryParams(instrumentsQueryParams.getInstrumentsQueryParams());
			} else if(instrumentsQueryParams.getInstrumentsQueryParams().isAtLeastOneParam()) {
				instruments = Instrument.find.get().findByQueryParams(instrumentsQueryParams.getInstrumentsQueryParams());
			} else {
				instruments = Instrument.find.get().findAll();
			}
			if (instrumentsQueryParams.datatable) {
				return ok(Json.toJson(new DatatableResponse<>(instruments, instruments.size()))); 
			} else if(instrumentsQueryParams.list) {
				//not used ListObject because need other information to create list (ex: group by active in bt-select)
				return ok(Json.toJson(instruments));
			} else {
				return ok(Json.toJson(instruments));
			}
		} catch (DAOException e) {
			logger.error("DAO error: "+e.getMessage(),e);
			return  Results.internalServerError(e.getMessage());
		}	
	}
	
	public Result get(String code) throws DAOException{
		Instrument instrument =  Instrument.find.get().findByCode(code);
		if (instrument != null) {
			return ok(Json.toJson(instrument));
		} else {
			return notFound();
		}
	}
	
	public Result update(String code) throws DAOException{
		Instrument instrument =  Instrument.find.get().findByCode(code);
		if (instrument == null) {
			return badRequest("Instrument with code "+code+" not exist");
		}
		Form<Instrument> filledForm = getFilledForm(instrumentForm, Instrument.class);
		Instrument instrumentInput = filledForm.get();
		if (code.equals(instrumentInput.code)) {
//			instrumentInput.update();
			Instrument.find.get().update(instrumentInput);
			Instrument.find.get().cleanCache();
			InstrumentUsedType.find.get().cleanCache();
			return ok(Json.toJson(Instrument.find.get().findByCode(code)));
		} else {
			return badRequest("instrument code are not the same");
		}		
	}
	
}
