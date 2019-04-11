package fr.cea.ig.ngl.test.dao.api.factory;


import controllers.DBObjectListForm;
import fr.cea.ig.DBObject;
import fr.cea.ig.lfw.LFWApplication;
import fr.cea.ig.lfw.support.LFWRequestParsing;
import fr.cea.ig.ngl.support.ListFormWrapper;
import fr.cea.ig.util.function.F3;

public class TestListFormWrapperFactory<T extends DBObject> {
		
	/**
	 * @return a function to generate the form wrapper from the corresponding DBObjectListForm
	 */
	public <U extends DBObjectListForm<T>> F3<U, QueryMode, RenderMode, ListFormWrapper<T>> wrapping() {
		return (form, qmode, render) -> {	
		    ListFormWrapper<T> wrapper = null;
			if (render != null ) {
				form.list      = render.equals(RenderMode.LIST);
				form.datatable = render.equals(RenderMode.DATATABLE);
				form.count     = render.equals(RenderMode.COUNT);
			}
			
			if(qmode != null) {
				form.aggregate = qmode.equals(QueryMode.AGGREGATE);
				if (form.aggregate) {
				    form.reporting = true;
				} else {
				    form.reporting = qmode.equals(QueryMode.REPORTING);
				}
				
				wrapper = new ListFormWrapper<>(form, 
				        f -> new LFWRequestParsing() {
				            @Override
				            public LFWApplication getLFWApplication() { return null;}
				        }.generateBasicDBObjectFromKeys(f),
				        f -> new LFWRequestParsing() {
				            @Override
				            public LFWApplication getLFWApplication() { return null;}
				        }.generateJSONKeys(f));
			} else {
			wrapper = new ListFormWrapper<>(form, 
					f -> new LFWRequestParsing() {
						@Override
						public LFWApplication getLFWApplication() { return null;}
					}.generateBasicDBObjectFromKeys(f));
			}
			return wrapper;	
		};
	}	
	
}

