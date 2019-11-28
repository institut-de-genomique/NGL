package ngl.data;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

// Rough MySQL parsing.

public class Parsing {
	
//	private static final play.Logger.ALogger logger = play.Logger.of(Parsing.class);

	enum State {
		text,linecomment,blockcomment
	}
	
	public static String strip(String input) {
		StringBuilder b = new StringBuilder();
		State state = State.text;
		for (int i=0; i<input.length(); i++) {
			char c = input.charAt(i);
			switch (state) {
			case text:
				if ((c == '/') && input.charAt(i+1) == ('*')) {
					i++;
					state = State.blockcomment;
				} else if ((c == '-') && (input.charAt(i+1) == '-')) {
					state = State.linecomment;
				} else {
					b.append(c);
				}
				break;
			case linecomment:
				if (c == '\n') {
					state = State.text;
					b.append(c);
				}
				break;
			case blockcomment:
				if ((c == '*') && input.charAt(i+1) == ('/')) {
					i++;
					state = State.text;
				}
				break;
			default:
				throw new RuntimeException();
			}
		}
		return b.toString();
	}
	
//	public String mySQLCreation() throws Exception {
//		String schema = Parsing.load("ngl/data/data/ngl.sql");
//		// logger.debug("schema : {}",schema);
//		String[] parts = schema.split(";");
//		Pattern regexDrop = Pattern.compile(".*DROP.*", Pattern.DOTALL);
//		Pattern regexCreate = Pattern.compile(".*CREATE TABLE\\s+(\\S+).*", Pattern.DOTALL);
//		StringBuilder sb = new StringBuilder();
//		for (String part : parts) {
//			
//		}		
//	}
	
	public List<CreateTableParser> run() throws Exception {
		Pattern regex = Pattern.compile(".*DROP.*", Pattern.DOTALL);
//		Matcher regexMatcher = regex.matcher(subjectString);
//		if (regexMatcher.find()) {
//		    ResultString = regexMatcher.group(1);
//		} 
		List<CreateTableParser> result = new ArrayList<>();
		String schema = Parsing.load("ngl/data/ngl.sql");
		// logger.debug("schema : {}",schema);
		String[] parts = schema.split(";");
		for (String part : parts) {
			String s = strip(part);
			if (StringUtils.isAllBlank(s)) {
				// logger.debug("skip empty part : {}", part);
			} else if (regex.matcher(part).find()) {
				//logger.debug("skip drop : {}", part);
			} else {
				s = s.replace("`",""); // kick back quotes out
				s = s.replace("ON DELETE NO ACTION","");
				s = s.replace("ON UPDATE NO ACTION","");
//				String r = s.replaceAll("(s?),\\s+KEY.*,",",");
//				while (!r.equals(s)) {
//					s = r;
//					r = s.replaceAll("(s?),\\s+KEY.*,",",");
//				}
//				s = s.replace("UNIQUE KEY","UNIQUE");
				// logger.debug("part {}",s);
				//pick(CCJSqlParserUtil.parseStatements(s));
				CreateTableParser p = new CreateTableParser(s);
				p.run();
				p.show();
				p.mySQL = part;
				result.add(p);
			}
		}
		return result;
	}

	public static String load(String r) throws IOException {
		try (InputStream is = Parsing.class.getClassLoader().getResourceAsStream(r)) {
			return IOUtils.toString(is, StandardCharsets.UTF_8);
		} catch (Exception e) {
			throw new RuntimeException("failed to find resource '" + r + "'",e);
		}
	}
	
}

