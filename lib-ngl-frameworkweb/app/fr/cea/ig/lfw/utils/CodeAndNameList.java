package fr.cea.ig.lfw.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;

public class CodeAndNameList extends ArrayList<CodeAndName> {
	
	/**
	 * Eclipse requested.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Default list sorting using {@link #sortByCodeInc()}.
	 * @return sorted list
	 */
	public CodeAndNameList sort() {
		return sortByCodeInc();
	}
	
	public CodeAndNameList sortByCodeInc() {
		Collections.sort(this, (a,b) -> a.code.compareTo(b.code));
		return this;
	}
	
	public CodeAndNameList sortByCodeDec() {
		Collections.sort(this, (a,b) -> b.code.compareTo(a.code));
		return this;
	}
	
	public CodeAndNameList sortByNameInc() {
		Collections.sort(this, (a,b) -> a.name.compareTo(b.name));
		return this;
	}
	
	public CodeAndNameList sortByNameDec() {
		Collections.sort(this, (a,b) -> b.name.compareTo(a.name));
		return this;
	}

	public static <T> CodeAndNameList from(Iterable<T> it, Function<T,String> codeF, Function<T,String> nameF) {
		CodeAndNameList r = new CodeAndNameList();
		for (T t : it)
			r.add(new CodeAndName(codeF.apply(t),nameF.apply(t)));
		return r;
	}

	public static CodeAndNameList from(Map<String,String> m) {
		return from(m, x-> x.getKey(), x -> x.getValue());
	}
	
	public static <A,B> CodeAndNameList from(Map<A,B> m, Function<Map.Entry<A,B>,String> codeF, Function<Map.Entry<A,B>,String> nameF) {
		return from(m.entrySet(), codeF, nameF);
	}
	
}
