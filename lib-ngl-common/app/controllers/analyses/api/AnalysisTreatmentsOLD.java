package controllers.analyses.api;

//public class AnalysisTreatmentsOLD extends SubDocumentController<Analysis, Treatment> {
//
////	@Inject
////	public AnalysisTreatments(NGLContext ctx) {
////		super(ctx,InstanceConstants.ANALYSIS_COLL_NAME, Analysis.class, Treatment.class);
////	}
//	
//	@Inject
//	public AnalysisTreatmentsOLD(NGLApplication app) {
//		super(app,InstanceConstants.ANALYSIS_COLL_NAME, Analysis.class, Treatment.class);
//	}
//
//	@Override
//	protected Query getSubObjectQuery(String parentCode, String code){
//		return DBQuery.and(DBQuery.is("code", parentCode), DBQuery.exists("treatments."+code));
//	}
//	
//	@Override
//	protected Collection<Treatment> getSubObjects(Analysis object){
//		return object.treatments.values();
//	}
//	
//	@Override
//	protected Treatment getSubObject(Analysis object, String code){
//		return object.treatments.get(code);
//	}
//	
////	@Permission(value={"writing"})	
//	@Authenticated
//	@Historized
//	@Authorized.Write
//	//@Permission(value={"creation_update_treatments"})
//	// @BodyParser.Of(value = BodyParser.Json.class, maxLength = 5000 * 1024)
//	@BodyParser.Of(value = IGBodyParsers.Json5MB.class)
//	public Result save(String parentCode){
//		Analysis objectInDB = getObject(parentCode);
//		if (objectInDB == null)
//			return notFound();
//		// Supposed to be an exception in 2.5
//		/*else if(request().body().isMaxSizeExceeded()){
//			return badRequest("Max size exceeded");
//		}*/
//		
//		Form<Treatment> filledForm = getSubFilledForm();
//		Treatment inputTreatment = filledForm.get();
////		ContextValidation ctxVal = new ContextValidation(getCurrentUser(), filledForm.errors()); 
////		ContextValidation ctxVal = new ContextValidation(getCurrentUser(), filledForm); 		
////		ctxVal.setCreationMode();
//		ContextValidation ctxVal = ContextValidation.createCreationContext(getCurrentUser(), filledForm);
//		ctxVal.putObject("level", Level.CODE.Analysis);
//		ctxVal.putObject("analysis", objectInDB);
//		inputTreatment.validate(ctxVal);
//		if (!ctxVal.hasErrors()) {
//			updateObject(DBQuery.is("code", parentCode), 
//					DBUpdate.set("treatments."+inputTreatment.code, inputTreatment)
//					.set("traceInformation", getUpdateTraceInformation(objectInDB.traceInformation)));
//			return get(parentCode, inputTreatment.code);
//		}
//		return badRequest(errorsAsJson(ctxVal.getErrors()));		
//	}
//
////	@Permission(value={"writing"})
//	@Authenticated
//	@Historized
//	@Authorized.Write
//	//@Permission(value={"creation_update_treatments"})
//	// @BodyParser.Of(value = BodyParser.Json.class, maxLength = 5000 * 1024)
//	@BodyParser.Of(value = IGBodyParsers.Json5MB.class)
//	public Result update(String parentCode, String code){
//		Analysis objectInDB = getObject(getSubObjectQuery(parentCode, code));
//		if (objectInDB == null)
//			return notFound();
//		Form<Treatment> filledForm = getSubFilledForm();
//		Treatment inputTreatment = filledForm.get();
//		if (code.equals(inputTreatment.code)) {
////		ContextValidation ctxVal = new ContextValidation(getCurrentUser(), filledForm.errors()); 
////			ContextValidation ctxVal = new ContextValidation(getCurrentUser(), filledForm); 
////			ctxVal.setUpdateMode();
//			ContextValidation ctxVal = ContextValidation.createUpdateContext(getCurrentUser(), filledForm); 
//			ctxVal.putObject("level", Level.CODE.Analysis);
//			ctxVal.putObject("analysis", objectInDB);
//			inputTreatment.validate(ctxVal);
//			if (!ctxVal.hasErrors()) { 
//				updateObject(DBQuery.is("code", parentCode), 
//						DBUpdate.set("treatments."+inputTreatment.code, inputTreatment)
//						.set("traceInformation", getUpdateTraceInformation(objectInDB.traceInformation)));
//				return get(parentCode, code);
//			}
//			return badRequest(errorsAsJson(ctxVal.getErrors()));
//		}
//		return badRequest("treatment code are not the same");
//	}
//	
////	@Permission(value={"writing"})	
//	@Authenticated
//	@Historized
//	@Authorized.Write
//	//@Permission(value={"delete_treatments"})
//	public Result delete(String parentCode, String code){
//		Analysis objectInDB = getObject(getSubObjectQuery(parentCode, code));
//		if (objectInDB == null) {
//			return notFound();			
//		}	
//		updateObject(DBQuery.is("code", parentCode), 
//				DBUpdate.unset("treatments."+code)
//				.set("traceInformation", getUpdateTraceInformation(objectInDB.traceInformation)));
//		return ok();		
//	}
//	
////	@Permission(value={"writing"})
//	@Authenticated
//	@Historized
//	@Authorized.Write
//	public  Result deleteAll(String parentCode){
//		Analysis objectInDB = getObject(parentCode);
//		if (objectInDB == null) {
//			return notFound();
//		}
//		updateObject(DBQuery.is("code", parentCode), 
//				DBUpdate.unset("treatments").set("traceInformation", getUpdateTraceInformation(objectInDB.traceInformation)));
//		return ok();
//	}
//	
//}
