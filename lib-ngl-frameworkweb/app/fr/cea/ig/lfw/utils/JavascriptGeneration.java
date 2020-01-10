package fr.cea.ig.lfw.utils;

import play.mvc.Result;
import static play.mvc.Results.ok;

import java.util.Collection;
import java.util.function.Function;

// import jsmessages.JsMessages;

/**
 * Javascript generation support.
 * <p>
 * The generation could make a good use of some intercalate
 * function.  
 * 
 * @author vrd
 *
 */
public class JavascriptGeneration {

	// Build a javascript  map from codes to names
	// replaces : jsCodes() & generateCodeLabel() of some controllers
	
	/**
	 * Key value entries where the key is dotted.
	 * 
	 * @author vrd
	 *
	 */
	public static class Codes {
		
		private boolean first;
		
		private StringBuilder sb;
		
		public Codes() {
			sb    = new StringBuilder();
			first = true;
		}
		
		/**
		 * Add a key and value pair. 
		 * @param key   key
		 * @param name  attribute name
		 * @param value value
		 * @return      this to chain calls
		 */
		public Codes add(String key, String name, String value) {
			optComma();
			sb	.append('"')
				.append(key)
				.append('.')
				.append(name)
				.append("\":\"")
				.append(value)
				.append('"');
			return this;
		}

		/**
		 * Add a collection using function to extract the needed (key,name,value) from the
		 * collection elements.
		 * @param c     collection to add
		 * @param key   element to key function
		 * @param name  element to name function
		 * @param value element to value function
		 * @param <T>   source iterable element type
		 * @return      this to chain calls
		 */
		public <T> Codes add(Iterable<T> c, Function<T,String> key, Function<T,String> name, Function<T,String> value) {
			for (T t : c) 
				add(key.apply(t),name.apply(t),value.apply(t));
			return this;
		}

		/**
		 * Add a collection of collection holders.
		 * @param c     collection of collection holders
		 * @param flat  collection holder to collection function
		 * @param key   element to key function
		 * @param name  element to name function
		 * @param value element to value function
		 * @param <S>   source iterable element type
		 * @param <T>   flattened iterable element type
		 * @return      this to chain calls
		 */
		public <S,T> Codes add(Iterable<S> c, Function<S,Collection<T>> flat, Function<T,String> key, Function<T,String> name, Function<T,String> value) {
			for (S s : c)
				for (T t : flat.apply(s)) 
					add(key.apply(t),name.apply(t),value.apply(t));
			return this;
		}

		/**
		 * Add valuation codes.
		 * @return this to chain calls.
		 */
		public Codes addValuationCodes() {
			return add("valuation", "TRUE",  "Oui")
				  .add("valuation", "FALSE", "Non")
				  .add("valuation", "UNSET", "---");
		}
		
		/**
		 * Add status codes.
		 * @return this to chain calls
		 */
		public Codes addStatusCodes() {
			return add("status",    "TRUE",  "OK" )
				  .add("status",    "FALSE", "KO" )
				  .add("status",    "UNSET", "---");
		}

		/**
		 * Return the built collection as a javascript Codes function.
		 * @return javascript Codes function
		 */
		public Result asCodeFunction() {
			StringBuilder r = 
					new StringBuilder()
						.append("Codes=(function(){var ms={")
						.append(sb)
						.append("};return function(k){if(typeof k == 'object'){for(var i=0;i<k.length&&!ms[k[i]];i++);var m=ms[k[i]]||k[0]}else{m=ms[k]||k}for(i=1;i<arguments.length;i++){m=m.replace('{'+(i-1)+'}',arguments[i])}return m}})();");
			return ok(r.toString()).as("application/javascript");
		}
		
		private void optComma() {
			if (first)
				first = false;
			else
				sb.append(',');
		}
		
	}
	
	public static class Permissions {
		private StringBuilder sb;
		private boolean first;
		public Permissions() {
			sb    = new StringBuilder();
			first = true; 
		}
		
		public Permissions add(String s) {
			optComma();
			sb.append('"')
			  .append(s)
			  .append('"');
			return this;
		}
		
		public Permissions addAll(Iterable<String> c) {
			for (String s : c)
				add(s);
			return this;
		}
		
		public <T> Permissions map(Iterable<T> c, Function<T,String> f) {
			for (T t : c) 
				add(f.apply(t));
			return this;
		}
		
		private void optComma() {
			if (first)
				first = false;
			else
				sb.append(',');
		}
		
		public Result asCodeFunction() {
			StringBuilder r = 
					new StringBuilder()
					.append("Permissions={}; Permissions.check=(function(param){var listPermissions=[")
					.append(sb)
					.append("];return(listPermissions.indexOf(param) != -1);})");
			return ok(r.toString()).as("application/javascript");
		}
		
		public static Result jsPermissions(Iterable<String> s) {
			return new Permissions()
					.addAll(s)
					.asCodeFunction();
		}
		
		public static <T> Result jsPermissions(Iterable<T> s, Function<T,String> f) {
			return new Permissions()
					.map(s,f)
					.asCodeFunction();
		}
		
	}
	
	// public static Result jsMessages(JsMessages messages) {}
	
}
