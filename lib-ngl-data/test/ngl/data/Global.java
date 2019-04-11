package ngl.data;

import java.sql.Connection;
import java.sql.Statement;
import java.util.Arrays;

import javax.inject.Inject;
import javax.sql.DataSource;

import fr.cea.ig.lfw.utils.Iterables;
import fr.cea.ig.lfw.utils.ZenIterable;
import fr.cea.ig.ngl.test.TestAppAuthFactory;
import fr.cea.ig.ngl.tmp.INGLDataDB;
import fr.cea.ig.ngl.tmp.NGLDatabase;
import nglapps.IApplicationData;
import nglapps.cng.CNGApplicationData;
import nglapps.cns.CNSApplicationData;

public class Global {
	
	// Eager binding of the schema check. The schema check should run properly 
	// using either the MySQL or HSQL databases.
	// Repopulation should be limited to only the necessary cases as it's 
	// pretty slow. 
	// Swapping databases could be an option but it requires some probably low
	// level implementation. Yet, with a simple driver interception this may be
	// an option. This requires as many bases (ram bases are free to create) 
	// so this could work with some intercepting driver that will use the already
	// populated database (populating once per test run).
	// Databases are started before the injection so we cannot alter the application
	// configuration that has already been read.
	// A DB driver depending on an application binding looks like it won't work.
	// Lazy connection management could  
	// 
	public static final TestAppAuthFactory af = 
			new TestAppAuthFactory("ngl-data.test.conf")
			// .overrideEagerly(EnsureDBSchema.class)
			// .override(SyncCacheApi.class, TestCache.class)
			// .override(INGLData.class, NGLDataDirect.class)
			//.override(INGLData.class, NGLDataRepop.class)
			.override(INGLDataDB.class, NGLDataDBMulti.class)
			.overrideEagerly(PopulateDB.class);

	public static final TestAppAuthFactory afCNSNoPop =
			new TestAppAuthFactory("ngl-data.test.conf")
			.override(INGLDataDB.class, NGLDataDBMulti.class)
			.configure("institute", "CNS")
			.override(IApplicationData.class, CNSApplicationData.class);
			
	
	// If the database has no schema we must create it. If the schema is created
	// we must then populate the database so all the applications now depend
	// on data (which seems right). The boot sequence of applications would be that
	// data is always right and as such always populate the database. This avoids
	// any problem updating the application as restarting the application should make
	// the application consistent.
	// 
	// Detecting the DB schema is the next problem and the idea is that we have spring
	// as a dependency so we can create the schema before spring is used. 
	// This means that all the static access to spring must be removed which is
	// quite a task (or even impossible). 
	//
	// 
	//
	public static final TestAppAuthFactory afCNS = af
			.configure("institute", "CNS")
			.override(IApplicationData.class, CNSApplicationData.class);
	
	public static final TestAppAuthFactory afCNG = af
			.configure("institute", "CNG")
			.override(IApplicationData.class, CNGApplicationData.class);
	
}

// We try to ensure default database schema.
class EnsureDBSchema {
	
	private static final play.Logger.ALogger logger = play.Logger.of(EnsureDBSchema.class);
	
	private final NGLDatabase db;
	
	
	@Inject
	public EnsureDBSchema(NGLDatabase db) throws Exception {
		logger.debug("DB schema, using {}", db);
		this.db = db;
		run();
	}
	
	// We need some tables to test
	
	private void run() throws Exception {
		DataSource ds = db.getDataSource();
		NGLSchemaInfo nsi = new NGLSchemaInfo();
//		DatabaseMetaData meta = ds.getConnection().getMetaData();
//		// Should test each table in some proper order.
//		ResultSet rs = meta.getTables(null, null, "institute", null);
//		while (rs.next()) {
//			logger.debug("  table {}",rs.getString(3));
//			throw new RuntimeException("institute table found");
//		}
//		logger.debug("create schema for 'default'");
//		List<CreateTableParser> tables = new Parsing().run();
//		// Build the dependency graph from the foreign keys.
//		Map<String,Set<String>> deps = new HashMap<>();
//		Map<String,CreateTableParser> parsers = new HashMap<>();
//		// Add dependencies using table FKs.
//		for (CreateTableParser t : tables) {
//			parsers.put(t.getTableName(), t);
//			Set<String> dep = new HashSet<>();
//			deps.put(t.getTableName(), dep);
//			for (CreateTableParser.Constraint c : t.getConstraints()) {
//				if (c instanceof CreateTableParser.FK) {
//					CreateTableParser.FK fk = (CreateTableParser.FK)c;
//					dep.add(fk.tableName);
//				}
//			}
//		}
//		for (Map.Entry<String, Set<String>> e : deps.entrySet())
//			System.out.println("  in - " + e.getKey() + " " + Iterables.zen(e.getValue()).surround("[", ",", "]").asString());
		// Need the dependency relation closure.
//		Map<String,Set<String>> cdeps = closures(deps);
//		// Sort tables by dependency.
//		Comparator<String> c = (a,b) -> {
//			if (cdeps.get(a).contains(b))
//				return 1;
//			if (cdeps.get(b).contains(a))
//				return -1;
//			return 0;
//		};
		// List<String> ots = new ArrayList<>(cdeps.keySet());
		// ots.sort(c); // Relation is not total so this does not work.
//		List<String> ots = depsort(deps);
//		// Reverse iterate to drop tables if it exists.
//		for (int i=ots.size()-1;i>=0;i--) {
//			String tn = ots.get(i);
//		Iterable<String> reverseDeps = filter(nsi.getReverseDependentNames(),descriptionTable);
//		Iterable<String> deps = filter(nsi.getDependentNames(),descriptionTable);
		Iterable<String> reverseDeps = nsi.getReverseDependentNames();
		Iterable<String> deps        = nsi.getDependentNames();
		for (String tn : reverseDeps) {
			try (Connection connection = ds.getConnection();
					Statement statement = connection.createStatement()) {
				String sql = "drop table " + tn;
				// logger.debug("{}",sql);
				statement.executeUpdate(sql);
				logger.info("dropped table '{}'", tn);
			} catch (Exception e) {
				logger.info("table not dropped {} : {}",tn,e.getMessage());
			}
		}
		
		// Run table creation in dependency order
		for (String tn : deps) {
			// System.out.println("  - " + tn + " " + asString(deps.get(tn)));
			// CreateTableParser p = parsers.get(tn);
			CreateTableParser p = nsi.getParser(tn);
			ZenIterable<String> columns = 
				Iterables.map(p.getColumns(), 
						c -> c.getName() + " " + sqlType(c.getType(),c.getSize())
						+ (c.notNull() ? " not null" : " null")
						// + (c.increment() ? " identity" : ""));
						+ (c.increment() ? " generated by default as identity (start with 100)" : ""));
			ZenIterable<String> constraints =
					Iterables.map(p.getConstraints(), c -> sqlConstraint(c));
			String sql =
					Iterables.flatten(Arrays.asList(columns,constraints))
					.surround("create table " + tn + " (\n",",\n", "\n)")
					.asString();
			// System.out.println("## SQL for " + tn + "\n" + sql);
			try (Connection connection = ds.getConnection();
					Statement statement = connection.createStatement()) {
				logger.info("creating table '{}'", tn);
				// logger.debug("{}",sql);
				statement.executeUpdate(sql);
			}
		}
		// Good to go...
		
		// Iterate over table definition and build the HSQL creation script.
		// The table have dependencies, so we can create them in the proper order.
		// The sorting has to be done.
		// 
//		Statement s = ds.getConnection().createStatement();
//		s.addBatch(schema);
//		s.executeBatch();
//		for (Map.Entry<String,String> e : DBSetup.tables.entrySet()) {
//		for (String tableName : DBSetup.tableNames) {
//			String sql = DBSetup.tables.get(tableName);
//			try (Connection connection = ds.getConnection();
//				 Statement statement = connection.createStatement()) {
//				logger.info("creating table '{}'", tableName);
//				logger.debug("{}",sql);
//				statement.executeUpdate(sql);
//			}
//		}
	}
	
	public String sqlConstraint(CreateTableParser.Constraint c) {
		if (c instanceof CreateTableParser.PK) {
			 CreateTableParser.PK pk = ( CreateTableParser.PK)c;
			return "primary key " + asString(pk.getKey());
		} else if (c instanceof CreateTableParser.FK) {
			CreateTableParser.FK fk = (CreateTableParser.FK)c;
			return "foreign key " + asString(fk.key) + " references " + fk.tableName + " " + asString(fk.fkey); 
		} else if (c instanceof CreateTableParser.Unique) {
			CreateTableParser.Unique u = (CreateTableParser.Unique)c;
			return "unique " + asString(u.key);
		} else {
			throw new RuntimeException("not handled " + c);
		}
	}
	
	public String sqlType(String t, String p) {
		switch (t.toLowerCase()) {
		case "tinyint"  : return "tinyint";
		case "smallint" : return "smallint";
		case "int"      : return "int";
		case "bigint"   : return "bigint";
//		case "bigint"   : return "int";
		case "varchar"  : return "varchar(" + p + ")";
//		case "text"     : return "text";
		case "text"     : return "varchar(255)";
		}
		throw new RuntimeException("unhandled type '" + t + "'");
	}
//	private Map<String,Set<String>> closures(Map<String,Set<String>> r) {
//		Map<String,Set<String>> result = new HashMap<>();
//		for (Map.Entry<String,Set<String>> e : r.entrySet())
//			result.put(e.getKey(),closure(r, e.getKey()));
//		return result;
//	}
	private String asString(Iterable<String> i) {
		return Iterables.zen(i).surround("(", ",", ")").asString();
	}
//	private Set<String> closure(Map<String,Set<String>> rel, String origin) {
////		boolean print = origin.equals("analysis_type");
//		Set<String> acc = new HashSet<>();
//		Stack<String> todo = new Stack<>();
//		// todo.addAll(rel.get(origin));
//		todo.add(origin);
//		while (!todo.empty()) {
////			if (print) System.out.println(" - processing " + origin + " " + asString(todo));
//			String rt = todo.pop();
//			for (String dep : rel.get(rt)) {
//				if (!acc.contains(dep)) {
//					acc.add(dep);
//					todo.push(dep);
//				}
//			}
//		}
//		return acc;
//	}

//	// Fun thing is that it does not rely on the closure...
//	// Inverted index allows simple cleanup. 
//	private List<String> depsort(Map<String,Set<String>> r) {
//		Map<String,Set<String>> tmp = new HashMap<>();
//		for (Map.Entry<String,Set<String>> e : r.entrySet())
//			tmp.put(e.getKey(),new HashSet<>(e.getValue()));
////		Map<String,Set<String>> inverse = new HashMap<>();
////		for (Map.Entry<String,Set<String>, V>)
//		List<String> result = new ArrayList<>();
//		while (!tmp.isEmpty()) {
//			// take the first that has no dependency.
//			String todo = null;
//			for (Map.Entry<String, Set<String>> e : tmp.entrySet()) {
//				if (e.getValue().isEmpty()) {
//					todo = e.getKey();
//					break;
//				}
//			}
//			if (todo == null) 
//				throw new RuntimeException("no candidate (graph cycle)");
//			tmp.remove(todo);
//			result.add(todo);
//			for (Map.Entry<String, Set<String>> e : tmp.entrySet()) {
//				e.getValue().remove(todo);
//			}			
//		}
//		return result;
//	}
	
}


// There is no cache problem between application restarts.
///**
// * Test cache implementation so there is no cache persistence between 
// * application restarts.
// * This is declared as a singleton but it does not matter if it's a real singleton
// * or a thread local cache or whatever as it's only a cache.
// * 
// * @author vrd
// *
// */
//@Singleton
//class TestCache implements SyncCacheApi {
//
//	private static final play.Logger.ALogger logger = play.Logger.of(TestCache.class);
//	
//	// Is the cache really caching ?
//	private static final boolean enabled = false;
//	
//	// Unused value
//	private static final int DEFAULT_EXPIRATION = 3600;
//	
//	private Map<String, Object> cache;
//	
//	@Inject
//	public TestCache(ApplicationLifecycle alc) {
//		cache = new HashMap<>();
//		alc.addStopHook(() -> {
//			cache.clear();
//			logger.info("cleared cache");
//			return CompletableFuture.completedFuture(null);
//		});
//	}
//	
//	@SuppressWarnings("unchecked")
//	@Override
//	public <T> T get(String key) {
//		return (T)cache.get(key);
//	}
//
//	@Override
//	public <T> T getOrElseUpdate(String key, Callable<T> block, int expiration) {
//		T t = get(key);
//		if (t == null) {
//			try {
//				t = block.call();
//				set(key,t,expiration);
//			} catch (Exception e) {
//				logger.error("value generation failed",e);
//			}
//		}
//		return t;
//	}
//
//	@Override
//	public <T> T getOrElseUpdate(String key, Callable<T> block) {
//		return getOrElseUpdate(key, block, DEFAULT_EXPIRATION);
//	}
//
//	@Override
//	public void set(String key, Object value, int expiration) {
//		if (enabled) {
//			logger.debug("cache {} : {}", key, value);
//			cache.put(key,value);
//		}
//	}
//
//	@Override
//	public void set(String key, Object value) {
//		set(key,value,DEFAULT_EXPIRATION);
//	}
//
//	@Override
//	public void remove(String key) {
//		cache.remove(key);
//	}
//	
//}
