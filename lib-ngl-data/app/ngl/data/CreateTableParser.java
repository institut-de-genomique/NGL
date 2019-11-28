package ngl.data;

import java.util.ArrayList;
import java.util.List;

// '"create table" parser' and not 'create "table parser"'
public class CreateTableParser {
	
//	private static play.Logger.ALogger logger = play.Logger.of(CreateTableParser.class);
	
	private String text;
	private int index;
	
	private String tableName;
	private List<Column> columns;
	private List<Constraint> constraints; 
	public String mySQL;
	
	public CreateTableParser(String text) {
		this.text   = text;
		index       = 0;
		columns     = new ArrayList<>();
		constraints = new ArrayList<>();
	}
	
	public void run() {
		accept("CREATE","TABLE");
		tableName = getId();
		//logger.debug("tableName {}",tableName);
		accept("(");
		parseDefs();
		accept(")");
		// We just ignore what's after the the closing parenthesis of the definition.
		// accept("ENGINE=InnoDB DEFAULT CHARSET=latin1");
		// eof();
	}
	
	private void parseDefs() {
		if (lookahead(")"))
			return;
		parseDef(); 
		if (lookahead(",")) {
			accept(",");
			parseDefs();
		}
	}
	
	public void parseDef() {
		if (lookahead("PRIMARY")) {
			primary();
		} else if (lookahead("KEY")) {
			key();
		} else if (lookahead("CONSTRAINT")) {
			constraint();
		} else if (lookahead("UNIQUE")) {
			unique();
		} else {
			String id = getId();
			field(id);
		}
	}
	
	private void unique() {
		accept("UNIQUE","KEY");
		@SuppressWarnings("unused")
		String constraintName = getId();
		constraints.add(new Unique(idList()));
	}
	
	private void primary() {
		accept("PRIMARY","KEY");
		constraints.add(new PK(idList()));
	}
	
	private List<String> idList() {
		accept("(");
		return _idList(new ArrayList<>());
	}
	private List<String> _idList(List<String> ids) {
		ids.add(getId());
		if (lookahead(",")) {
			accept(",");
			return _idList(ids);
		} else if (lookahead(")")) {
			accept(")");
			return ids;
		} else {
			throw new RuntimeException("expected , or )\n" + text.substring(index));
		}
	}
	
	// Assume that this is a uniqueness constraint
	private void key() {
		accept("KEY");
		@SuppressWarnings("unused")
		String constraintName = getId();
		idList();
		// constraints.add(new Unique(idList()));
	}
	
	// This is foreign key information that we need.
	//   this(idList) references that(idList).
	private void constraint() {
		accept("CONSTRAINT");
		@SuppressWarnings("unused")
		String constraintName = getId();
		accept("FOREIGN","KEY");
		List<String> fromKey = idList();
		accept("REFERENCES");
		String tableName = getId();
		List<String> toKey = idList();
		constraints.add(new FK(fromKey,tableName,toKey));
	}
	private void field(String id) {
		//logger.debug("parse field {}", id);
		Column c = new Column();
		columns.add(c);
		c.name = id;
		c.type = getId();
		if (lookahead("(")) {
			accept("(");
			c.size = getId();
			accept(")");
		}
		//logger.debug("parse field modifiers");
		fieldMods(c);
		//logger.debug("parsed field modifers");
		//parseDefs();
	}
	private void fieldMods(Column c) {
		if (lookahead("NOT NULL")) {
			accept("NOT NULL");
			c.notNull = true;
			fieldMods(c);
		} else if (lookahead("AUTO_INCREMENT")) {
			accept("AUTO_INCREMENT");
			c.autoIncrement = true;
			fieldMods(c);
//		} else if (lookahead("DEFAULT NULL")) {
//			// This is a null, doesn't seem to be anything special
//			accept("DEFAULT NULL");
//			fieldMods(c);
		} else if (lookahead("DEFAULT")) {
			accept("DEFAULT");
			if (lookahead("'")) {
				accept("'");
				getId();
				accept("'");
			} else {
				getId();
			}
			fieldMods(c);
		}
		//parseDefs();
	}
	private boolean lookahead(String tk) {
		skipSpaces();
		return tk.equals(text.substring(index, index + tk.length()));
	}
	
	// Would be better to use a-z and other char classes.
	private String getId() {
		skipSpaces();
		int start = index;
		char c = text.charAt(index);
		while (!Character.isWhitespace(c)
				&& c != '('
				&& c != ')'
				&& c != ','
				&& c != '\'') {
			index++;
			c = text.charAt(index);
		}
		String id = text.substring(start, index);
		if (id.length() == 0)
			throw new RuntimeException("expected id\n" + text.substring(index));
		//logger.debug("getId : '{}' \n{}",id,text.substring(index));
		return id;
	}
	
	private void accept(String... tks) {
		for (String tk : tks)
			_accept(tk);
	}
	
	private void _accept(String tk) {
		if (lookahead(tk)) {
			index += tk.length();
			//logger.debug("accepted " + tk + "\n" + text.substring(index));
			return;
		}
		throw new RuntimeException("expected '" + tk + "' is " + text.substring(index));
	}
	
	private void skipSpaces() {
		if (index >= text.length())
			return;
		char c = text.charAt(index);
		while (Character.isWhitespace(c)) {
			index++;
			if (index >= text.length())
				return;
			c = text.charAt(index);
		}
	}
	
//	private void eof() {
//		skipSpaces();
//		if (index < text.length())
//			throw new RuntimeException("expected EOF, is " + text.substring(index));
//	}
	
	public void show() {
//		System.out.println("##################################################");
//		System.out.println("table " + tableName);
//		for (Column c : columns)
//			System.out.println("  " + c.name + " " + c.type + " null:" + (!c.notNull) + " inc:" + c.autoIncrement);
//		System.out.println("##################################################");
	}
	
	static class Column {
		String name;
		String type;
		String size;
		boolean notNull;
		boolean autoIncrement;
		public String getName() { return name; }
		public String getType() { return type; }
		public String getSize() { return size; }
		public boolean notNull() { return notNull; }
		public boolean increment() { return autoIncrement; }
	}
	
	static class Constraint {}
	static class PK extends Constraint {
		List<String> key;
		public PK(List<String> key) { this.key = key; }

		public Iterable<String> getKey() {
			return key;
		}
	}
	static class FK extends Constraint {
		public List<String> key;
		public String tableName;
		public List<String> fkey; 
		public FK(List<String> key, String table, List<String> fKey) {
			this.key = key;
			this.tableName = table;
			this.fkey = fKey;
		}
		
	}
	static class Unique extends Constraint {
		List<String> key;
		public Unique(List<String> key) { this.key = key; }
		
	}
	public List<Constraint> getConstraints() {
		return constraints;
	}

	public String getTableName() {
		return tableName;
	}

	public List<Column> getColumns() {
		return columns;
	}
	
}