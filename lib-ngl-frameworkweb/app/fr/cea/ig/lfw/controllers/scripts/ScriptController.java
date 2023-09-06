package fr.cea.ig.lfw.controllers.scripts;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;

import akka.NotUsed;
import akka.actor.Status;
import akka.stream.OverflowStrategy;
import akka.stream.javadsl.Source;
import akka.util.ByteString;
import fr.cea.ig.lfw.controllers.scripts.buffered.Script;
import fr.cea.ig.lfw.controllers.scripts.chunked.ChunkedOutputScriptBase;
import play.inject.Injector;
import play.mvc.Controller;
import play.mvc.Http.RequestBody;
import play.mvc.Result;

/**
 * Controller that runs scripts (either {@link Script} or {@link ChunkedOutputScriptBase}).
 *  
 * @author vrd
 *
 */
public class ScriptController extends Controller {
	
	private final Injector injector;
	
	// pour dire à play d'utiliser ce constructeur
	@Inject
	// arg pour dire à play de nous fournir un injecteur qui nous permettra d'instancier la classe.
	public ScriptController(Injector injector) { 
		this.injector = injector;
	}
	
	/**
	 * Route target method.
	 * @param clazz script class to instanciate 
	 * @return      HTTP result
	 */
	public Result run(String clazz) {
		try {
			Class<?> c      = Class.forName(clazz);
			Object instance = injector.instanceOf(c);
			if (instance instanceof Script) {
				return ((Script<?>)instance).run();
			} else if (instance instanceof ChunkedOutputScriptBase) {
				return runChunkedScript((ChunkedOutputScriptBase<?>)instance);
			} else {
				return notFound(instance.getClass() + " does not inherit from as script base class");
			}
		} catch (Exception e) {
			return notFound("not found class "+ e.getMessage());
		}
	}
	
	private Result runChunkedScript(ChunkedOutputScriptBase<?> script) {
		final Map<String,String[]> args = play.mvc.Http.Context.current().request().queryString();
		final RequestBody          body = play.mvc.Http.Context.current().request().body();
		Source<ByteString, ?> source = Source.<ByteString>actorRef(256, OverflowStrategy.fail())
				.mapMaterializedValue(sourceActor -> {
					CompletableFuture.runAsync(() -> {
						script.initialize(sourceActor);
						try {
							script.run(args, body);
							sourceActor.tell(new Status.Success(NotUsed.getInstance()), null);
						} catch (Exception e) {
							sourceActor.tell(new Status.Failure(e), null);
						}
					});
					return sourceActor;
				});
		// Serves this stream with 200 OK
		return ok().chunked(source);
	}
	
}

