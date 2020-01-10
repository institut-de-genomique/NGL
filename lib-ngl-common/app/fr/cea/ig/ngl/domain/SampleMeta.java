package fr.cea.ig.ngl.domain;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import fr.cea.ig.ngl.domain.Type.Def;
import fr.cea.ig.ngl.utils.CodeReference;
import models.laboratory.common.instance.Comment;
import models.laboratory.common.instance.TraceInformation;
import models.laboratory.project.instance.Project;
import models.laboratory.sample.description.ImportType;
import models.laboratory.sample.description.SampleCategory;
import models.laboratory.sample.description.SampleType;
import models.laboratory.sample.instance.Sample;
import services.ncbi.NCBITaxon;

/**
 * Typed definition for an anemic DTO class ({@link models.laboratory.sample.instance.Sample}).
 *  
 * @author vrd
 *
 */
public class SampleMeta {
	// private CodeReference<SampleType>     type;
	// private CodeReference<SampleCategory> category;
	// private CodeReference<ImportType>     importType;
	// private Set<CodeReference<Project>>   projects;
	// private CodeReference<NCBITaxon>      taxon;
	// private Boolean                       metagenomic; // This is not optional, probably a TBoolean
	// private Optional<String>              refCollab;

	// Def is too light, it is beans level without the type.
	public static final Def<Sample,CodeReference<Sample>> code =
			new Def<>("code",
					  (s,c) -> s.code = c.getCode(),
					  s     -> CodeReference.of(s.code));
	
	public static final Def<Sample,CodeReference<SampleType>> type =
			new Def<>("typeCode",
					  (s,c) -> s.typeCode = c.getCode(),
					  s     -> CodeReference.of(s.typeCode));
	
	public static final Def<Sample,CodeReference<SampleCategory>> category =
			new Def<>("categoryCode",
					  (s,c) -> s.categoryCode = c.getCode(),
					  s     -> CodeReference.of(s.categoryCode));	
	
	public static final Def<Sample,CodeReference<ImportType>> importType =
			new Def<>("importTypeCode",
					  (s,c) -> s.importTypeCode = c.getCode(),
					  s     -> CodeReference.of(s.importTypeCode));
		
	// We have to handle embedded objects and this does not look pretty.
	// We probably need an Embedded type.
	// Standard mapping from and to Set<CodeReference<Project>> / Set<String> is
	// a bit of a problem.
	
	static <T> TreeSet<String> codes(Set<CodeReference<T>> s) {
		TreeSet<String> r = new TreeSet<>();
		for (CodeReference<T> c : s)
			r.add(c.getCode());
		return r;
	}
	
	static <T> TreeSet<CodeReference<T>> crefs(Set<String> s) {
		TreeSet<CodeReference<T>> r = new TreeSet<>();
		for (String c : s)
			r.add(CodeReference.of(c));
		return r;
		
	}
	
	// We have a set of project code. This behaves like a set of strings
	// but is logically linked to project codes.
	public static final Def<Sample,Set<CodeReference<Project>>> projects =
			new Def<>("projectCodes",
					  (s,c) -> s.projectCodes = codes(c),
					  s     -> crefs(s.projectCodes));
	
	// This is an embedded object that does not say its type.
	// This is a flat representation. Could try to provide 
	// an implementation of an abstracted NCBITaxon (INCBITaxon).
	// The setters can be mapped onto the container fields.
	// Setting the code directly means that the other fields are not
	// populated. Setting the Taxon should do a full copy.
	public static final Def<Sample,CodeReference<NCBITaxon>> taxon =
			new Def<>((q,s) -> q.set("taxonCode",s.taxonCode),
				      (s,c) -> s.taxonCode = c.getCode(),
					  s     -> CodeReference.of(s.taxonCode));
	
	// This is kind of a problem as there is no field name. The field
	// name is kind of an oversight as there could be no field. There is
	// something like the update generator. We simply update the property value
	// with the given name. Do we facade the field value ? The point is to provide some
	// documentation so the def does not have to be too flexible. We expose the
	// definition as some builder method. The properties map is probably always 
	// copied, so copying a lone data does not look of any interest.
	// Same problem occurs if we create a metagenomic accessor in any facade.
	// The facade could store arbitrary data/state where the meta def cannot. 
	public static final Def<Sample,Boolean> metagenomic =
			new Def<>((q,t) -> { q.set("properties.metagenomic",true); },
					  (s,c) -> { return; },
					  s     -> { // PropertyValue<?> v = s.properties.get("metagenomic");
					             // extract and default the value.
					             return null;
					           });
	
	public static final Def<Sample,List<Comment>> comments = 
			new Def<>("comments",
					  (s,c) -> s.comments = c,
					  s     -> s.comments);
	
	// Trace information is split into the creation info and the update info.
	// Exposing some interface in front of the trace information seems quite natrual.
	// This means that the trace information is updated through this interface and that we
	// pretty much track the information (dirtiness). This looks a bit ORM level.
	// The current problem is that there are other processes accessing the
	// original data and those are not aware of a facade. We assume that we are in control
	// and know everything about the update. We either update the trace information
	// in full or not at at all. The trace information is a know type that
	// can provide a simple set of restrictions.
	public static final Def<Sample,TraceInformation> traceInformation =
			new Def<>("traceInformation",
					  (s,c) -> s.traceInformation = c,
					  s     -> s.getTraceInformation());
	
	// This defines the refcollab as some string where we would expect something like
	// Optional<String>.
	public static final Def<Sample,String> refCollab =
			new Def<>("referenceCollab",
					  (s,c) -> s.referenceCollab = c,
					  s     -> s.referenceCollab);
	
}
