package fr.cea.ig.ngl.support;

import java.util.List;
import java.util.function.Function;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.mongojack.DBQuery;

import com.mongodb.BasicDBObject;

import akka.stream.javadsl.Source;
import akka.util.ByteString;
import controllers.DBObjectListForm;
import fr.cea.ig.DBObject;
import views.components.datatable.IDatatableForm;

/**
 * Wrapper around {@link controllers.ListForm} object. 
 * 
 * @author ajosso
 *
 * @param <T> resource object
 */
public class ListFormWrapper<T extends DBObject> {

	private final DBObjectListForm<T>                     form;
	private final Function<IDatatableForm, BasicDBObject> basicDBObjectGenerator;
	private final Function<IDatatableForm, String>        jsonKeysGenerator;
	
	/**
	 * @param form                     the form 
     * @param basicDBObjectGenerator   a function to generate a BasicDBObject used for query result customization
	 */
	public ListFormWrapper(DBObjectListForm<T> form, Function<IDatatableForm, BasicDBObject> basicDBObjectGenerator) {
		this.form                   = form;
		this.basicDBObjectGenerator = basicDBObjectGenerator;
		this.jsonKeysGenerator      = null;
	}
	
	/**
	 * @param form                     the form 
	 * @param basicDBObjectGenerator   a function to generate a BasicDBObject used for query result customization
	 * @param jsonKeysGenerator        a function to generate a string with keys used for result projection (in JSON format)
	 */
	public ListFormWrapper(DBObjectListForm<T> form, Function<IDatatableForm, BasicDBObject> basicDBObjectGenerator, Function<IDatatableForm, String> jsonKeysGenerator) {
        this.form                   = form;
        this.basicDBObjectGenerator = basicDBObjectGenerator;
        this.jsonKeysGenerator      = jsonKeysGenerator;
    }
	
	public boolean isMongoJackMode() {
		return ! isAggregateMode() && ! isReportingMode(); // default mode
	}

	public boolean isAggregateMode() {
		return form.aggregate && form.reporting && StringUtils.isNotBlank(form.reportingQuery);
	}

	public boolean isReportingMode() {
		return form.reporting && StringUtils.isNotBlank(form.reportingQuery) &&  ! form.aggregate;
	}
	
	public boolean isList() {
		return form.list;
	}
	
	public boolean isCount() {
		return form.count;
	}
	
	public String orderBy() {
		return form.orderBy;
	}
	
	public String reportingQuery() {
		if (isReportingMode() || isAggregateMode()) {
			return form.reportingQuery;
		} else {
			return null;
		}
	}
	
	public DBQuery.Query getQuery() {
		if (isMongoJackMode()) {
			return form.getQuery();
		} else {
			return null;
		}
	} 

	public BasicDBObject getKeys(List<String> defaultKeys) {
		replaceShortcurtKeys(defaultKeys);
		return this.basicDBObjectGenerator.apply(form);
	}
	
	public String getJsonKeys(List<String> defaultKeys) {
        replaceShortcurtKeys(defaultKeys);
        return this.jsonKeysGenerator.apply(form);
    }

    /**
     * Override includes with "code" on list queries.
     * replace value like "default" by the list of default keys
     * @param defaultKeys list of default keys
     */
    private void replaceShortcurtKeys(List<String> defaultKeys) {
        // replace includes by "code" for list queries
    	if(form.list) {
    		if(form.includes().isEmpty() || form.includes().size() > 1 || !form.includes().contains("code")) {
    			form.includes().clear();
    			form.includes().add("code");
    			form.includes().add("name");
    		}
    	// replace "default" keyword with the list of default keys
    	} else if (form.includes().contains("default")) {
            form.includes().remove("default");
            if (CollectionUtils.isNotEmpty(defaultKeys)) {
                form.includes().addAll(defaultKeys);
            }
        }
    }
	
	/**
	 * Define how to return results. 
	 * @return the function to transform results
	 */
	// invalid javadoc : @see controllers.ListForm#transform() Concrete implementation
	public Function<Iterable<T>, Source<ByteString,?>> transform() {
		return form.transform();
	}
	
}
