package fr.cea.ig.play.test;

import java.io.IOException;
import java.util.function.BiConsumer;
// import java.util.function.Consumer;

import com.fasterxml.jackson.databind.JsonNode;
// import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.FloatNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

// import play.Logger;

// Provides path support for json and could be used
// for subclass specific stuff.
public class JsonFacade {

	private static final play.Logger.ALogger logger = play.Logger.of(JsonFacade.class);
	
	private JsonNode data;
	
	public JsonFacade(JsonNode data) {
		this.data = data;
	}
	public JsonFacade set(String path, String value) {
		applyField(path,(n,f) -> { n.set(f,new TextNode(value)); });
		return this;
	}
	public JsonFacade delete(String path) {
		applyField(path,(n,f) -> { n.remove(f); });
		return this;
	}
	
	public JsonFacade set(String path, JsonNode v) {
		applyField(path,(n,f) -> { n.set(f,v); });
		return this;
	}
	//public JsonFacade set(String path, Number value) {
	//	return this;
	//}
	
	public JsonFacade set(String path, int value) {
		applyField(path,(n,f) -> { n.set(f,new IntNode(value)); });
		return this;
	}
	
	public JsonFacade set(String path, float value) {
		applyField(path,(n,f) -> { n.set(f,new FloatNode(value)); });
		return this;
	}
	public JsonFacade copy(JsonFacade f, String sPath, String dPath) {
		JsonNode n = f.get(sPath);
		set(dPath,n);
		return this;
	}
	public JsonNode get(String path) {
		return get(parse(path));
	}
	public JsonNode get(Path p) {
		logger.debug("using path " + p);
		JsonNode n = data;
		while (p != null) {
			n = p.apply(n);
			p = p.child;
		}
		return n;
	}
	public String getString(String path) {
		return get(path).textValue();
	}
	
	public int getInt(String path) {
		throw new RuntimeException("not implemented");
	}
	
	public float getFloat(String path) {
		throw new RuntimeException("not implemented");
	}

	public void applyField(String path, BiConsumer<ObjectNode,String> c) {
		applyField(parse(path),c);
	}
	public void applyField(Path path, BiConsumer<ObjectNode,String> c) {
		logger.debug("apply field " + path);
		JsonNode n = data;
		Path p = path;
		while(p.child != null) {
			JsonNode m = p.apply(n);
			if (m == null)
				throw new RuntimeException("could not apply " + p + " " + n);
			n = m;
			p = p.child;
		}
		if (!(n instanceof ObjectNode)) 
			throw new RuntimeException(path.toString() + " does not point to a json object");
		if (!(p instanceof Path.Field))
			throw new RuntimeException(path.toString() + " does not point to a json object field");
		c.accept((ObjectNode)n, ((Path.Field)p).getName());
	}

	public Path parse(String path) {
		return new PathParser(path).parse();
	}
	
	public JsonNode jsonNode() { return data; }
	
	public static JsonFacade getJsonFacade(String resource) throws IOException {
		return new JsonFacade(JsonHelper.getJson(resource));
	}
	
	static class PathParser {
		String text;
		int index;
		PathParser(String text) {
			this.text = text;
			index = 0;
		}
		void consume(char c) {
			if (eot())
				error("expected '" + c + "', reached end of text");
			char d = text.charAt(index);
			if (d != c)
				error("expected '" + c + "' " + ", got '" + d);
			index++;
		}
		RuntimeException error(String message)  {
			return new RuntimeException(message + " : '" + text.substring(0, index) + "'");
		}
		void consume() {
			consume(getChar());
		}
		char getChar() {
			if (eot())
				throw new RuntimeException("end of text");
			return text.charAt(index);
		}
		public boolean eot() {
			return index >= text.length();
		}
		
		public Path parse() {
			if (eot())
				return null;
			char c = getChar();
			switch (c) {
			case '/': 
				consume('/');
				return parse();
			case '[':
				consume('[');
				return parseIndex();
			default:
				return parseField();
			}
		}
		public Path parseIndex() {
			StringBuilder iText = new StringBuilder();
			char c;
			while (!eot()) {
				c = getChar();
				if (Character.isDigit(c)) {
					iText.append(c);
					consume(c);
				} else if (c == ']') {
					consume(']');
					return new Path.ArrayIndex(Integer.parseInt(iText.toString()), parse());
				} else {
					throw error("expected dgit or ]");
				}
			}
			throw error("expected digit or ]");
		}
		// Had a field start
		public Path parseField() {
			StringBuilder field = new StringBuilder();
			char c;
			while (!eot()) {
				switch (c = getChar()) {
				case '/': 
				case '[':
					return new Path.Field(field.toString(),parse());
				default:
					field.append(c);
					consume(c);
					break;
				}
			}
			return new Path.Field(field.toString(),null);
		}
	}

	static abstract class Path {
		Path child;
		abstract JsonNode apply(JsonNode n);
		static class Field extends Path {
			String name;
			public Field(String n, Path c) {
				name = n;
				child = c;
			}
			public String getName() { return name; }
			@Override
			public JsonNode apply(JsonNode n) {
				JsonNode r = n.get(name);	
				if (r == null)
					throw new RuntimeException("could not find field " + name + " in " + n);
				return r;
			}
			@Override
			public String toString() {
				String s = "{" + name + "}";
				if (child != null)
					return s + child;
				return s;
			}
		}
		static class ArrayIndex extends Path {
			int index;
			public ArrayIndex(int i, Path p) {
				index = i;
				child = p;
			}
			public int getIndex() { return index; }
			@Override
			public JsonNode apply(JsonNode n) {
				JsonNode r = n.get(index);
				if (r == null)
					throw new RuntimeException("could not find element " + index + " in " + n);
				return r;
			}
			@Override
			public String toString() {
				String s = "[" + index + "]";
				if (child != null)
					return s + child;
				return s;
			
			}
		}
	}
	
}
