package models.utils;

import java.util.List;

import fr.cea.ig.lfw.utils.Iterables;
import models.laboratory.common.description.CommonInfoType;

// TODO : looks like a ListObjectValue<String>
public class ListObject {
	
	public String name;
	public String code;
	
	public ListObject() {
		this("","");
//		this.name = "";
//		this.code = "";
	}
	
	public ListObject(String code, String label) {
		this.name = label;
		this.code = code;
	}
	
	public static List<ListObject> from(List<CommonInfoType> values) {
//		List<ListObject> l = new ArrayList<>(values.size());
//		for (CommonInfoType value : values) {
//			l.add(new ListObject(value.code, value.name));
//		}
//		return l;
		return Iterables.map(values, value -> new ListObject(value.code, value.name)).toList();
	}
	
}

