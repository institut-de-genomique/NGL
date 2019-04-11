//package ngl.data;
//
////
//// Try to support automatic data population of whatever database "ngl" is
//// mapped to. This makes the all URL not needed as it is automatically ran
//// at boot time.
//// 
//// We need to identify the database we're connected to as the DDL is not uniform.
//// Providing an indirection to the NGL data source would be done using a custom data source
//// that would be used across the code. The problem is that we need to mix this stuff
//// with a spring initialized class.
//// 
//// We track the database status by database name.
//// Any "new" database for this run is repopulated.
//// 
//// NGL data boot is where we choose the actual way to start NGL data for an application.
//// As we have a binding for NGLDatabase, this would be a proper definition point.
//// A single boot definition requires that we use the data boot as the database provider
//// to remove the requirement of multiple bindings. The data boot implementation is then
//// what specifies the population and the access.
////
//
//public class NGLDataBoot {
//}
////
//////
////// Provide a custom database binding that selects a named instance.
////// The NGL data base must be an interface with a binding.
////@Singleton
////class NGLIndirectDatabase extends NGLDatabase {
////	
////	// Keep a map of application data to database names. The mapping
////	// is supposed to be stable for a VM (either run or test).
////	// The database indirection is linked to the db setup that can be direct
////	// and requires population on each run or indirect and assumes that
////	// enough DBs are present to hold the different database populations.
////	// We need the different population strategies defined at boot.
////	private static Map<Class<?>,String> dbMap = new HashMap<>();
////	
////	@Inject
////	public NGLIndirectDatabase(DBApi dbApi, IApplicationData d) {
////		super(null);
////		// Compute a name from the application data and use the appropriate
////		// database from the dbapi. 
////	}
////	
////}
////
////class EnsureDatabasePopulation {
////	
////	private Map<String,String> productToInitialization =
////			new HashMapBuilder<String,String>()
////			.asMap();
////	
////	public void run(DataSource db) throws Exception {
////		// Known which DB we target.
////		DatabaseMetaData dm = db.getConnection().getMetaData();
////		String productName = dm.getDatabaseProductName();
////		// Fetch database creation script (plain source for mysql, 
////		// transformed for hsql).
////		
////		// Run the population stuff.
////	}
////	
////}
////
