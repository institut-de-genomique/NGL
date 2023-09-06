package fr.cea.ig.mongo;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import models.utils.ListObject;
import models.utils.ListObjectNumber;

public interface DBObjectConvertor {

	default <T> List<ListObject> convertToListObject(Iterable<T> c, Function<T,String> code, Function<T,String> label) {
		List<ListObject> list = new ArrayList<>();
		for (T t : c) {
			list.add(new ListObject(code.apply(t), label.apply(t)));
		}
		return list;
	}
	
	default <T> List<ListObjectNumber> convertToListObjectNumber(Iterable<T> c, Function<T,Number> code, Function<T,String> label) {
		List<ListObjectNumber> list = new ArrayList<>();
		for (T t : c) {
			list.add(new ListObjectNumber(code.apply(t), label.apply(t)));
		}
		return list;
	}
	
}
