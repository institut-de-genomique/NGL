package models.utils;

import static fr.cea.ig.lfw.utils.Iterables.map;

import java.util.List;

import models.laboratory.common.description.CommonInfoType;
import models.laboratory.project.instance.Project;
import models.laboratory.sample.instance.Sample;

public class ListObjectValue<T> {

	public String name;
	public T      code;
	
	public ListObjectValue() {
		this.name = "";
		this.code = null;
	}
	
	public ListObjectValue(T code, String label) {
		this.name = label;
		this.code = code;
	}
	
//////	@SuppressWarnings({ "rawtypes", "unchecked" })
////	public static List<ListObjectValue> projectToJsonObject(List<Project> projects){
////	@SuppressWarnings({ "rawtypes", "unchecked" })
//	public static List<ListObjectValue<String>> projectToJsonObject(List<Project> projects) {
////		List<ListObjectValue> jo = new ArrayList<ListObjectValue>();
//		List<ListObjectValue<String>> jo = new ArrayList<>();		
//		for (Project p: projects)
//			jo.add(new ListObjectValue<>(p.code, p.name));
//		return jo;
//	}
	public static List<ListObjectValue<String>> projectToJsonObject(List<Project> projects) {
		return map(projects, p -> new ListObjectValue<>(p.code, p.name)).toList();
	}
	
////	@SuppressWarnings({ "rawtypes", "unchecked" })
////	public static List<ListObjectValue> sampleToJsonObject(List<Sample> samples) {
//	public static List<ListObjectValue<String>> sampleToJsonObject(List<Sample> samples) {
////		List<ListObjectValue> jo = new ArrayList<ListObjectValue>();
//		List<ListObjectValue<String>> jo = new ArrayList<>();
//		for (Sample p: samples) {
////			jo.add(new ListObjectValue(p.code, p.name));
//			jo.add(new ListObjectValue<>(p.code, p.name));
//		}		
//		return jo;
//	}

	public static List<ListObjectValue<String>> sampleToJsonObject(List<Sample> samples) {
		return map(samples, s -> new ListObjectValue<>(s.code, s.name)).toList();
	}

//	@SuppressWarnings({ "rawtypes", "unchecked" })
//	public static List<ListObjectValue> from(List<CommonInfoType> values) {
//		List<ListObjectValue> l = new ArrayList<ListObjectValue>(values.size());
//		for(CommonInfoType value : values){
//			l.add(new ListObjectValue(value.code, value.name));
//		}
//		return l;
//	}

//	public static List<ListObjectValue<String>> from(List<CommonInfoType> values) {
//		List<ListObjectValue<String>> l = new ArrayList<>(values.size());
//		for (CommonInfoType value : values)
//			l.add(new ListObjectValue<>(value.code, value.name));
//		return l;
//	}
	public static List<ListObjectValue<String>> from(List<CommonInfoType> values) {
		return map(values, c -> new ListObjectValue<>(c.code, c.name)).toList();
	}
	
}
