package fr.cea.ig.test;

import static models.utils.dao.DAOException.daoAssertNotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import javax.inject.Inject;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;

import fr.cea.ig.DBObject;
import fr.cea.ig.mongo.MongoConfig;
import fr.cea.ig.ngl.dao.GenericMongoDAO;
import fr.cea.ig.ngl.dao.api.APIException;
import fr.cea.ig.ngl.dao.api.APIs;
import fr.cea.ig.ngl.dao.api.GenericAPI;
import fr.cea.ig.play.IGGlobals;
import fr.cea.ig.util.function.T2;
import models.laboratory.container.instance.Container;
import models.laboratory.container.instance.ContainerSupport;
import models.laboratory.experiment.instance.Experiment;
import models.laboratory.processes.instance.Process;
import models.laboratory.project.instance.Project;
import models.laboratory.run.instance.Analysis;
import models.laboratory.run.instance.ReadSet;
import models.laboratory.run.instance.Run;
import models.laboratory.sample.instance.Sample;
import models.utils.InstanceConstants;
import models.utils.dao.DAOException;


/**
 * A way to access NGL persistence system for all Mongo NGL domain objects.
 * <p>
 * We could read or try to read the MongoCollection annotations but it would be even more
 * magic so we keep the definition central. 
 * <p>
 * Despite the raw types warnings all over the place, the construction
 * of the definitions through the add method should guarantee the consistency of the
 * definition types.  
 * <p>
 * With this class, we pay homage to <a href="https://fr.wikipedia.org/wiki/A_Kind_of_Magic_(album)">Queen album "A kind of magic"</a>.
 * 
 * @author vrd
 *
 */
// TODO: rename
public class AKindOfMagic {
	
	private static final play.Logger.ALogger logger = play.Logger.of(AKindOfMagic.class);
	
	@SuppressWarnings("rawtypes")
	private final Map<Class,T2<Supplier<? extends GenericAPI>,String>> definitions;
	
	@Inject
	public AKindOfMagic(APIs apis) {
		definitions = new HashMap<>();
		add(Container.class,        () -> apis.container()       , InstanceConstants.CONTAINER_COLL_NAME);
		add(ContainerSupport.class, () -> apis.containerSupport(), InstanceConstants.CONTAINER_SUPPORT_COLL_NAME);
		add(Project.class,          () -> apis.project(),          InstanceConstants.PROJECT_COLL_NAME);
		add(Sample.class,           () -> apis.sample(),           InstanceConstants.SAMPLE_COLL_NAME);
		add(Experiment.class,       () -> apis.experiment(),       InstanceConstants.EXPERIMENT_COLL_NAME);
		add(Process.class,          () -> apis.process(),          InstanceConstants.PROCESS_COLL_NAME);
		add(Run.class,              () -> apis.run(),              InstanceConstants.RUN_ILLUMINA_COLL_NAME);
		add(ReadSet.class,          () -> apis.readset(),          InstanceConstants.READSET_ILLUMINA_COLL_NAME);
		add(Analysis.class,         () -> apis.analyses(),         InstanceConstants.ANALYSIS_COLL_NAME);
	}
	
	private <T extends DBObject, DAO extends GenericMongoDAO<T>, API extends GenericAPI<DAO, T>> void add(Class<T> c, Supplier<API> a, String cn) {
		@SuppressWarnings("rawtypes")
		T2<Supplier<? extends GenericAPI>,String> t2 = new T2<>(a,cn);
		definitions.put(c, t2);
	}
	
	private <T extends DBObject, DAO extends GenericMongoDAO<T>, API extends GenericAPI<DAO, T>> API getAPI(T t) throws DAOException {
		daoAssertNotNull("object",t);
		Class<?> c        = t.getClass();
		@SuppressWarnings("rawtypes")
		T2<Supplier<? extends GenericAPI>,String> def = definitions.get(c);
		if (def == null)
			throw new RuntimeException("API mapping is missing for " + c);
		@SuppressWarnings("unchecked")
		API capi = (API)def.a.get();
		return capi;
	}
	
	public <T extends DBObject, DAO extends GenericMongoDAO<T>, API extends GenericAPI<DAO, T>> T create(T t, String user) throws DAOException, APIException {
		API api = getAPI(t);
		logger.debug("using API {} to create ({},{}) {}", api, t.getCode(), t.get_id(), t);
		T r = api.create(t,user);
		logger.debug("created {} {}", r.getCode(), r.get_id());
		return r;
	}

	// Destroy an object of class clazz with given id. 
	public void destroy(Class<?> clazz, String id) {
		if (clazz == null)
			throw new IllegalArgumentException("class arg is null");
		if (id == null)
			throw new IllegalArgumentException("id is null");
		@SuppressWarnings("rawtypes")
		T2<Supplier<? extends GenericAPI>,String> def = definitions.get(clazz);
		if (def == null)
			throw new RuntimeException("no collection was found for " + clazz);
		MongoDatabase database = IGGlobals.instanceOf(MongoConfig.class).getDatabase();
		String collectionName = def.b; 
		logger.debug("deleting {} : {}", collectionName, id);
		DeleteResult r = database.getCollection(collectionName).deleteOne(new Document("_id", new ObjectId(id)));
		logger.debug("deleted {} : {}, count:{}", collectionName, id, r.getDeletedCount());
	}
	
}
