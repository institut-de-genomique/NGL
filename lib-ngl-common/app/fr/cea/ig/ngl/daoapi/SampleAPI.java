package fr.cea.ig.ngl.daoapi;

// Prototype, not used.
// Warnings are suppressed

import java.util.ArrayList;
// import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import fr.cea.ig.ngl.dao.SampleDAO;
import fr.cea.ig.ngl.dao.api.APIException;
import fr.cea.ig.ngl.dao.api.APIValidationException;
import fr.cea.ig.ngl.domain.SampleMeta;
import fr.cea.ig.ngl.domain.Type.Def.Closure;
import fr.cea.ig.ngl.utils.CodeReference;
import models.laboratory.common.instance.Comment;
import models.laboratory.project.instance.Project;
import models.laboratory.sample.description.ImportType;
import models.laboratory.sample.description.SampleCategory;
import models.laboratory.sample.description.SampleType;
// import models.laboratory.common.instance.TraceInformation;
import models.laboratory.sample.instance.Sample;
// import models.utils.InstanceHelpers;
import models.utils.dao.DAOException;
import models.utils.instance.SampleHelper;
import play.libs.Json;
import services.ncbi.NCBITaxon;
import validation.ContextValidation;

@SuppressWarnings("unused")
abstract class CRUDDAO<T> {
	// create
	// read
	// update
	// delete
}

@SuppressWarnings("unused")
abstract class CRUDAPI<T> {
	// create
	// read
	// update
	// delete
}

public class SampleAPI extends CRUDAPI<Sample> {

	// Like a class, more in the line of a Bean definition.
	public static class Type {
		

	}
	
	// "Heavy" call definition to provide a typed interface.
	// Could use an inner class to allow access to API methods.
	// This is a factory for api calls. If the api call provides
	// optional arguments, we're pretty much set.
	// Populating the create call from a Sample object that is built
	// from a query does not look that good. Yet, it's a matter of providing
	// meta info to the controller.
	// The create call is relative to the API.
	// The API then uses the DAO for the persistence that is tied to the
	// API updated sample object. The DAO would then provide a similar Create
	// object that allows a bit more than the API Create object as some fields 
	// are supposed to be modified by the API.
	public class Create {
		
		// Explicit typing of the sample type fields.
		// Should store some kind of 
		private List<Closure<Sample,?>> mods;
		
//		private CodeReference<Sample>         code;
//		private CodeReference<SampleType>     type;
//		private CodeReference<SampleCategory> category;
//		private CodeReference<ImportType>     importType;
//		private Set<CodeReference<Project>>   projects;
//		private CodeReference<NCBITaxon>      taxon;
//		private Boolean                       metagenomic; // This is not optional
//		private Optional<String>              refCollab;
		
		// Constructor defines the mandatory arguments
		public Create(CodeReference<Sample> code, 
				      SampleType            type, 
				      ImportType            importType, 
				      Set<CodeReference<Project>> projects, 
				      NCBITaxon             taxon) {
			this(code,
				 CodeReference.of(type.code),
				 CodeReference.of(type.category.code),
				 CodeReference.of(importType.code),
				 projects,
				 CodeReference.of(taxon.code));
		} 
		public Create(CodeReference<Sample>         code, 
					  CodeReference<SampleType>     type, 
					  CodeReference<SampleCategory> category, 
					  CodeReference<ImportType>     importType, 
					  Set<CodeReference<Project>>   projects, 
					  CodeReference<NCBITaxon>      taxon) {
			mods = new ArrayList<>();
			// Fill the mods with the arguments. 
			mods.add(SampleMeta.code.setup(code));
			mods.add(SampleMeta.type.setup(type));
			mods.add(SampleMeta.category.setup(category));
			mods.add(SampleMeta.projects.setup(projects));
			mods.add(SampleMeta.importType.setup(importType));
			mods.add(SampleMeta.taxon.setup(taxon));
			mods.add(SampleMeta.metagenomic.setup(false));
			// refCollab   = Optional.empty(); // null;
		}
		
		// public Create()
		// Provide methods to fill values
		// This kind of calls can build problems.
		// In fact this not a problem except when building the 
		// update. Still, the update could be smart enough to not
		// build a problematic set when defined multiple times.
		public Create metaGenomics(boolean b)   { mods.add(SampleMeta.metagenomic.setup(b)); return this; }
		//public Create referenceCollab(String s) { mods.add(SampleMeta.refCollab.setup(s); return this; }
		
		// Provide some kind of execute method. We may have some outer instance (should).
		public void execute() throws DAOException, APIException {
			// Provide the mods to run, not the args.
			// create(code,type,category,importType,projects,taxon,metagenomic,refCollab);
			
		}
		
	}
	
	@SuppressWarnings("unused")
	public static class Modifiable<T> {}
	
	// A request provides a number of fields that are actually allowed to
	// be part of the update. Given the data object and the update object,
	// we do what's needed.
	// e.g. ("comments", (u,d) -> return u.comments(d.comments))
	// Update instance initialization could be done through the object code.
	// 
	
	// Could define the list of modifiers to apply. This is where the closure 
	// has to provide access to the def.
	public class Update {
		
		// Mandatory object "id".
		private CodeReference<Sample>   code;
		// Optional parameters. This is defined as Optional<FieldType>.
		// private Optional<List<Comment>> comments;
		// List<fr.cea.ig.ngl.domain.Type.Def.Closure<Sample,?>> mods = null;
		List<Closure<Sample,?>> mods;
		
		
		public Update(CodeReference<Sample> code) {
			this.code = code;
			// Apply modifiers (copied data)
			// We use optional arguments for any of the fields to update.
			// comments = Optional.empty();
		}
		
		// Need some support for dirtied values.
		public Update comments(List<Comment> comments) {
			// This should register some meta data and the value.
			// Application to the data is done using some closure. This allows
			// the data to be updated.
			//   - copy data from some T as DTO (set(future,get(past)))
			//   - complete the update builder (set(name,get(future)).
			// this.comments = Optional.of(comments);
			mods.add(SampleMeta.comments.setup(comments));
			return this;
		}
		
		// Execute as the given user.
		public void executeAs(String user) {
			// Generic way to test for updated fields
			if (mods.isEmpty()) return;
			// 
			ContextValidation ctx = new ContextValidation(user); 	
			ctx.setUpdateMode();
			// Fetch the current version of the sample
			Sample past = dao.getByCode(code.getCode()); // more present than past
			// Create current version by cloning and updating.
			Sample future = deepClone(Sample.class,past);
			// Apply field modification.
			// for (Function<Sample,Sample> f : new ArrayList<Function<Sample,Sample>>())
			// 	future = f.apply(future);
			for (Closure<Sample,?> c : mods)
				c.accept(future);
			// Add a traceinfo update closure
			future.getTraceInformation().modificationStamp(ctx, user);
			mods.add(SampleMeta.traceInformation.setup(future.getTraceInformation()));
			// Prepare validation context
			
			// Blah, do validation.
			
			// Generate the update
			if (!ctx.hasErrors()) {
				// Could use the object id, no one cares internally
				// DBQuery.Query    query  = DBQuery.is("code", code);
				// DBUpdate.Builder update = new DBUpdate.Builder();
				
				// There is possibly some equivalence between the update setter and the
				// update builder.
				// update = update.set("traceInformation", future.getTraceInformation());
				// dao.update(query,update); // not that much of a DAO 
						// getBuilder(sampleInForm, queryFieldsForm.fields).set("traceInformation", getUpdateTraceInformation(sampleInDB.traceInformation)));
				// Build the DAO Update object from the API Update instance.
				
			}
			
//			// validateAuthorizedUpdateFields(ctxVal, queryFieldsForm.fields, authorizedUpdateFields);
//			// validateIfFieldsArePresentInForm(ctxVal, queryFieldsForm.fields, filledForm);
//			// if(!filledForm.hasErrors()){
//			if (!ctxVal.hasErrors()) {
//				sampleInForm.comments = InstanceHelpers.updateComments(sampleInForm.comments, ctxVal);
	//
//				TraceInformation ti = sampleInDB.traceInformation;
//				ti.setTraceInformation(getCurrentUser());
	//
//				if(queryFieldsForm.fields.contains("valuation")){
//					sampleInForm.valuation.user = getCurrentUser();
//					sampleInForm.valuation.date = new Date();
//				}
	//
//				if (!ctxVal.hasErrors()) {
//					updateObject(DBQuery.and(DBQuery.is("code", code)), 
//							getBuilder(sampleInForm, queryFieldsForm.fields).set("traceInformation", getUpdateTraceInformation(sampleInDB.traceInformation)));
//					if(queryFieldsForm.fields.contains("code") && null != sampleInForm.code){
//						code = sampleInForm.code;
//					}
//					return ok(Json.toJson(findSample(code)));
//				} else {
//					// return badRequest(filledForm.errors-AsJson());
//					return badRequest(errorsAsJson(ctxVal.getErrors()));
//				}				
//			} else {
//				// return badRequest(filledForm.errors-AsJson());
//				return badRequest(errorsAsJson(ctxVal.getErrors()));
//			}
//			dao.update(sample);

		}
		
	}
	
	// @SafeVarargs
	public static boolean nothing(Optional<?>... optionals) {
		for (Optional<?> o : optionals)
			if (o.isPresent())
				return false;
		return true;
	}
	
	// Moved to the DeepClonable interface
	// @SuppressWarnings("unchecked")
	public static <T> T deepClone(Class<T> c, T t) {
//		return Json.fromJson(Json.toJson(t),(Class<? extends T>)t.getClass());
		return Json.fromJson(Json.toJson(t),c);
	}
	
	// Delete is done by code so a single delete method is ok.
	
//	// Replace by typed methods for the creation.
//	public static final List<String> defaultKeys =  
//			Arrays.asList("code",
//					      "typeCode",
//					      "categoryCode",
//					      "projectCodes",
//					      "referenceCollab",
//					      "properties",
//					      "valuation",
//					      "taxonCode",
//					      "ncbiScientificName",
//					      "comments",
//					      "traceInformation");
//	
//	// Allowed updates
//	private static final List<String> authorizedUpdateFields = Arrays.asList("comments");

	private final SampleDAO dao;
	
	public SampleAPI(SampleDAO dao) {
		this.dao = dao;
	}
	
	public void create(CodeReference<Sample>         code, 
			           CodeReference<SampleType>     type,
			           CodeReference<SampleCategory> category, 
			           CodeReference<ImportType>     importType,
			           Set<CodeReference<Project>>   projects, 
			           CodeReference<NCBITaxon>      taxon, 
			           Boolean                       metagenomic,
			           Optional<String>              refCollab) throws DAOException, APIException {
//		Sample sample = new Sample();
		// Building the query depends on the provided data. The sample
		// object is kind of a DTO. We basically build the sample object
		// part that is the creation data and then build some appropriate query
		// to update the thing.
	}

//	// Probably enumerate the required fields.
//	public Sample create(String user, String code) throws DAOException, APIException {
//		Sample sample = new Sample();
//		sample.code = code;
//		create(sample,user);
//		return sample;
//	}
	
	public void create(Sample sample, String user) throws DAOException, APIException {
		ContextValidation ctx = new ContextValidation(user);
		sample.setTraceCreationStamp(ctx,user);
		if (ctx.hasErrors())
			throw new APIValidationException("validation error", ctx.getErrors());
		ctx.setCreationMode();
		SampleHelper.executeRules(sample, "sampleCreation");
		sample.validate(ctx);
		if (ctx.hasErrors())
			throw new APIValidationException("validation error", ctx.getErrors());
		dao.create(sample);
	}
	
	// Requires the list of updated fields.
	// We simply provide Optional arguments.
	// Or we really implement the method in the Update object 
	// as this is where we have all the information.
	public void update(Sample sample, String user) {
//		ContextValidation ctx = new ContextValidation(user); 	
//		ctx.setUpdateMode();
//		Sample past = dao.getByCode(sample.getCode());
//		// validateAuthorizedUpdateFields(ctxVal, queryFieldsForm.fields, authorizedUpdateFields);
//		// validateIfFieldsArePresentInForm(ctxVal, queryFieldsForm.fields, filledForm);
//		// if(!filledForm.hasErrors()){
//		if (!ctxVal.hasErrors()) {
//			sampleInForm.comments = InstanceHelpers.updateComments(sampleInForm.comments, ctxVal);
//
//			TraceInformation ti = sampleInDB.traceInformation;
//			ti.setTraceInformation(getCurrentUser());
//
//			if(queryFieldsForm.fields.contains("valuation")){
//				sampleInForm.valuation.user = getCurrentUser();
//				sampleInForm.valuation.date = new Date();
//			}
//
//			if (!ctxVal.hasErrors()) {
//				updateObject(DBQuery.and(DBQuery.is("code", code)), 
//						getBuilder(sampleInForm, queryFieldsForm.fields).set("traceInformation", getUpdateTraceInformation(sampleInDB.traceInformation)));
//				if(queryFieldsForm.fields.contains("code") && null != sampleInForm.code){
//					code = sampleInForm.code;
//				}
//				return ok(Json.toJson(findSample(code)));
//			} else {
//				// return badRequest(filledForm.errors-AsJson());
//				return badRequest(errorsAsJson(ctxVal.getErrors()));
//			}				
//		} else {
//			// return badRequest(filledForm.errors-AsJson());
//			return badRequest(errorsAsJson(ctxVal.getErrors()));
//		}
//		dao.update(sample);
	}	
	
}
