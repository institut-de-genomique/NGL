package controllers;

import java.util.function.Function;

import org.jongo.MongoCursor;
import org.mongojack.DBCursor;
import org.mongojack.DBQuery;

import com.fasterxml.jackson.annotation.JsonIgnore;

import akka.stream.javadsl.Source;
import akka.util.ByteString;
import fr.cea.ig.DBObject;
import fr.cea.ig.lfw.utils.Iterables;
import fr.cea.ig.mongo.MongoStreamer;
import play.libs.Json;

public abstract class DBObjectListForm<T extends DBObject> extends ListForm {

	/**
	 * Define the conversion done if the return mode selected is "list"
	 * @return a transform function
	 */
	@JsonIgnore
	public Function<T, ListObject> conversion() {
		return o -> new ListObject(o.code, o.code);
	}

//	/**
//	 * (have to be implemented into "concrete" xxSearchForm class).
//	 * @return the query object
//	 */
//	@JsonIgnore
//	public DBQuery.Query getQuery() {
//		return null;
//	}
	
	@JsonIgnore
	public abstract DBQuery.Query getQuery();
	
	/**
	 * Define how to return results from input form values.
	 * @return the function to transform results according to the selected mode.
	 */
    @JsonIgnore
	public Function<Iterable<T>, Source<ByteString, ?>> transform() {
		if (datatable) {
			return iterable -> MongoStreamer.streamUDT_(iterable, (obj -> Json.toJson(obj).toString()));
		} else if (list) {
			return iterable -> MongoStreamer.stream(iterable, conversion());
		} else if (count) {

		    /* AJ: NGL-2194
		     * refactoring: manage the count by creating a new object 
		     * which will be the fusion between MongoDBResult and MongoCursor 
		     * to access directly to the count without iterate on the list of results  
		     */
		    return (iterable -> {
                int c = 0;
                if (iterable instanceof MongoCursor) {
                    c = ((MongoCursor<T>) iterable).count();
                } else if (iterable instanceof DBCursor) {
                    c = ((DBCursor<T>) iterable).count();
                } else {
                    c = Iterables.count(iterable);
                }
                return Source.single(ByteString.fromString("{\"result\": "+ c + " }"));
            });

		} else {
			return cursor -> MongoStreamer.stream(cursor);
		}
	}
	
}
