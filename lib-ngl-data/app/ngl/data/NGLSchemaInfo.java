package ngl.data;

import static fr.cea.ig.lfw.utils.Iterables.filter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Schema info extracted from SQL database creation script.
 * A set of table names are excluded from  
 * 
 * @author vrd
 *
 */
public class NGLSchemaInfo {
	
	// Tables that are not description tables.
	public static final Set<String> nonDescriptionTables = 
			new HashSet<>(Arrays.asList("role",
					      	            "permission",
					      	            "team",
					      	            "application",
					      	            "role_permission",
					      	            "user",
					      	            "user_role",
					      	            "user_team",
					      	            // "not_a_table",
					      				"user_application"));
	
	public static final Predicate<String> descriptionTable = 
			n -> ! nonDescriptionTables.contains(n);

	private Map<String,CreateTableParser> parsers;
	private List<String> dependentNames;
	private List<String> inverseDependentNames;
	
	public NGLSchemaInfo() throws Exception {
		// The mySQL script must not be ran directly as some definitions 
		// are not managed by this stuff.
		// String schema = Parsing.load("ngl/data/data/ngl.sql");
		
		List<CreateTableParser> tables = new Parsing().run();
		// Build the dependency graph from the foreign keys.
		Map<String,Set<String>> deps = new HashMap<>();
		parsers = new HashMap<>();
		Set<String> ignored = new HashSet<>();
		// Add dependencies using table FKs.
		for (CreateTableParser t : tables) {
			// Ignore non description tables
			if (descriptionTable.test(t.getTableName())) {
				parsers.put(t.getTableName(), t);
				Set<String> dep = new HashSet<>();
				deps.put(t.getTableName(), dep);
				for (CreateTableParser.Constraint c : t.getConstraints()) {
					if (c instanceof CreateTableParser.FK) {
						CreateTableParser.FK fk = (CreateTableParser.FK)c;
						dep.add(fk.tableName);
					}
				}
			} else {
				ignored.add(t.getTableName());
			}
		}
		// assert that the ignored list has been fully ignored.
		if (!ignored.equals(nonDescriptionTables)) {
			String ts =
					filter(nonDescriptionTables, n -> ! ignored.contains(n))
					.intercalate(" ")
					.asString();
			throw new RuntimeException("some table are defined as ignored but are not part of the schema: " + ts);
		}
		dependentNames = depsort(deps);
		inverseDependentNames = new ArrayList<>(dependentNames);
		Collections.reverse(inverseDependentNames);
	}
	public List<String> getReverseDependentNames() { return inverseDependentNames; }
	public List<String> getDependentNames() { return dependentNames; }
	public CreateTableParser getParser(String name) { return parsers.get(name); }
	private List<String> depsort(Map<String,Set<String>> r) {
		Map<String,Set<String>> tmp = new HashMap<>();
		for (Map.Entry<String,Set<String>> e : r.entrySet())
			tmp.put(e.getKey(),new HashSet<>(e.getValue()));
		//			Map<String,Set<String>> inverse = new HashMap<>();
		//			for (Map.Entry<String,Set<String>, V>)
		List<String> result = new ArrayList<>();
		while (!tmp.isEmpty()) {
			// take the first that has no dependency.
			String todo = null;
			for (Map.Entry<String, Set<String>> e : tmp.entrySet()) {
				if (e.getValue().isEmpty()) {
					todo = e.getKey();
					break;
				}
			}
			if (todo == null) 
				throw new RuntimeException("no candidate (graph cycle)");
			tmp.remove(todo);
			result.add(todo);
			for (Map.Entry<String, Set<String>> e : tmp.entrySet()) {
				e.getValue().remove(todo);
			}			
		}
		return result;
	}

}
