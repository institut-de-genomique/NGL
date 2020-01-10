package models.utils;

import java.util.List;

import fr.cea.ig.lfw.utils.Iterables;
import models.laboratory.common.description.CommonInfoType;

public class ListObject {
	
	public String name;
	public String code;
	
	public ListObject() {
		this("","");
	}
	
	public ListObject(String code, String label) {
		this.name = label;
		this.code = code;
	}
	
	public static List<ListObject> from(List<CommonInfoType> values) {
		return Iterables.map(values, value -> new ListObject(value.code, value.name)).toList();
	}
	
}

