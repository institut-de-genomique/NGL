package controllers.sra.scripts;

import java.lang.reflect.Method;

import javax.inject.Inject;

import fr.cea.ig.lfw.controllers.AbstractScript;
import play.inject.Injector;
import play.mvc.Controller;
import play.mvc.Result;

public class ScriptController extends Controller {
	//public static final String LILI = "lili";
	private final Injector injector;
	
	// pour dire à play d'utiliser ce constructeur
	@Inject
	// arg pour dire à play de nous fournir un injecteur qui nous permettra d'instancier la classe.
	public ScriptController(Injector injector) { 
		this.injector = injector;
	}
	
	public Result noRest(String clazz, String method) {
		try {
			Class<?> c = Class.forName(clazz);
			Object obj = injector.instanceOf(c);
			Method m = c.getMethod(method);
			//return ok("objet : '" + obj + "' methode:  '"+ method + "'");
			return (Result) m.invoke(obj);
		//} catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException e) {
		} catch (Exception e) {
			return notFound("not found class "+ e.getMessage());
		}
	}
	public Result run(String clazz) {
		try {
			Class<?> c = Class.forName(clazz);
			AbstractScript obj = (AbstractScript) injector.instanceOf(c);
			return obj.run();
		//} catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException e) {
		} catch (Exception e) {
			return notFound("not found class "+ e.getMessage());
		}
	}
	
	public Result script_1() {
		return ok("from script_1");
	}
}
