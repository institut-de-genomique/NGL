//package ngl.data;
//
//public class DBSetup {
//	
////	public static final String[] tableNames;
////	public static Map<String,String> tables;
////	public static Map<String,String> dependencies;
//	
////	private static String str(String... ss) {
////		StringBuilder sb = new StringBuilder();
////		for (String s : ss)
////			sb.append(s);
////		return sb.toString();
////	}
//	
////	static {
////		tableNames = new String[] {
////				"object_type",
////				"common_info_type",
////				"analysis_type",
////				"institute",
////				"object_type_hierarchy"
//////				"application",
//////				"common_info_type_institute",
////			};
////
////		tables = new HashMap<>();
////		dependencies = new HashMap<>();
////		try {
////			for (String s : tableNames)
////				addTable(s);
////		} catch (Exception e) {
////			throw new RuntimeException(e);
////		}
////	}
//	
//	
//
////	private static void addTable(String t) throws IOException {
////		String sql = load("ngl/data/tables/" + t + ".sql");
////		tables.put(t,sql);
////	}
//	
//}
