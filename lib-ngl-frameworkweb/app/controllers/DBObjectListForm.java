package controllers;

import java.util.Arrays;
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

public class DBObjectListForm<T extends DBObject> extends ListForm {

	/**
	 * Define the conversion done if the return mode selected is "list"
	 * @return a transform function
	 */
	@JsonIgnore
	public Function<T, ListObject> conversion() {
//		return o -> { return new ListObject(o.code, o.code); };
		return o -> new ListObject(o.code, o.code);
	}

	/**
	 * (have to be implemented into "concrete" xxSearchForm class).
	 * @return the query object
	 */
	@JsonIgnore
	public DBQuery.Query getQuery() {
		return null;
	}

	/**
	 * Define how to return results from input form values.
	 * @return the function to transform results according to the selected mode.
	 */
    @JsonIgnore
	public Function<Iterable<T>, Source<ByteString, ?>> transform() {
		if (this.datatable) {
			return (iterable -> MongoStreamer.streamUDT_(iterable, (obj -> Json.toJson(obj).toString())));
		} else if (this.list) {
			return (iterable -> MongoStreamer.stream(iterable, conversion()));
		} else if (this.count) {

		    /* TODO AJ NGL-2194
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
                return Source.from(Arrays.asList(ByteString.fromString("{\"result\": "+ c + " }")));
            });

		} else {
			return (cursor -> MongoStreamer.stream(cursor));
		}
	}
	
}
