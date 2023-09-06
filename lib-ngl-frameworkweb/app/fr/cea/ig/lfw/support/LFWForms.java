package fr.cea.ig.lfw.support;

import static fr.cea.ig.lfw.utils.FunCollections.map;

import java.util.List;

import play.data.Form;

/**
 * Play form utilities.
 * 
 * @author vrd
 *
 */
public interface LFWForms extends /*LFWApplicationHolder,*/ LFWRequestParsing {

	default <T> Form<T> form(Class<T> c) {
		return getLFWApplication().formFactory().form(c);
	}
	
	default <T> Form<T> filledFormQueryString(Class<T> clazz) {
		return filledFormQueryString(form(clazz),clazz);
	}
	
	default <T> Form<T> filledFormQueryString(Form<T> form, Class<T> clazz) {
		return form.fill(objectFromRequestQueryString(clazz));
	}
	
	default <T> Form<T> getFilledForm(Class<T> clazz) {		
		return getFilledForm(form(clazz),clazz);
	}

	default <T> Form<T> getFilledForm(Form<T> form, Class<T> clazz) {		
		return form.fill(objectFromRequestBody(clazz)); 
	}
	
	default <T> List<Form<T>> getFilledFormList(Class<T> clazz) {
		return getFilledFormList(form(clazz),clazz);
	}
	
	default <T> List<Form<T>> getFilledFormList(Form<T> form, Class<T> clazz) {
		return map(objectListFromRequestBody(clazz), o -> form.fill(o));
	}

}
