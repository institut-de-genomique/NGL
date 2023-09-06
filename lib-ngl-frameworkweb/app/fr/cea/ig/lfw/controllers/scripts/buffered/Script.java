package fr.cea.ig.lfw.controllers.scripts.buffered;

import java.util.Map;

import fr.cea.ig.lfw.controllers.scripts.ScriptException;
import fr.cea.ig.lfw.controllers.scripts.buffered.example.ScriptNoArgs_impl;
import fr.cea.ig.lfw.controllers.scripts.buffered.example.Script_impl;
import fr.cea.ig.lfw.reflect.ReflectionUtils;
import play.mvc.Http.RequestBody;
import play.mvc.Result;
import play.mvc.Results;

/**
 * Classe abstraite avec une methode {@link Script#execute(Object)} lancée avec les parametres parsés et controlés de l'url.
 * La classe Script prend comme parametre une classe de controle et stockage des arguments de l'url.
 * Tous les champs publics de la classe definissent les parametres obligatoires de l'url, qui doivent etre 
 * associes à des valeurs non nulles. Si l'url contient un parametre absent de la classe de controle des 
 * arguments, une erreur est declenchee.
 * <p>
 * La methode execute sera executé au chargement de l'url
 * de la forme: {@literal http://localhost:9000/sra/scripts/run/NameClasseFille?arg1=val1&...}
 * <p>
 * See {@link Script_impl} or {@link ScriptNoArgs_impl} for an example use.
 * <p>
 * Choose the appropriate super class from the provided ones:
 * <ul>
 *   <li>{@link Script} : script with custom URL parameters</li>
 *   <li>{@link ScriptNoArgs} : no URL parameter script</li>
 *   <li>{@link ScriptWithBody} : no URL parameters and the HTTP request body</li>
 *   <li>{@link ScriptWithArgsAndBody} : URL parameters and HTTP request body</li>
 *   <li>{@link ScriptWithExcelBody} : no URL parameters and an excel request body</li>
 * </ul>
 * 
 * @param <T> argument class
 * 
 * @author sgas
 *
 */
public abstract class Script<T> {
	
	/**
	 * Log level of the print methods ({@link Script#printfln(String, Object...)},
	 * {@link Script#println()}, {@link Script#println(String)}). 
	 */
	public enum LogLevel {
		NONE, DEBUG, INFO
	}
	
	/**
	 * Logger.
	 */
	private final play.Logger.ALogger logger;
	
	/**
	 * HTTP response buffer.
	 */
    private final StringBuilder sb;
	
    /**
     * Windows line feeds ?
     */
	private boolean windowsLf;
	
	/**
	 * Print log level.
	 */
	private LogLevel logLevel;
	
	/**
	 * Print uses logger debug level and Unix line feeds.
	 */
	public Script() {
		this(LogLevel.DEBUG);
	}
	
	/**
	 * Print uses logger specified level and Unix line feeds.
	 * @param logLevel print log level
	 */	
	public Script(LogLevel logLevel) {
		this(logLevel, false);
	}
	
	/**
	 * Print uses logger specified level and specified line feeds.
	 * @param logLevel  print log level
	 * @param windowsLf windows line feed ?
	 */
	public Script(LogLevel logLevel, boolean windowsLf) {
		logger         = play.Logger.of(getClass());
		sb             = new StringBuilder();
		this.windowsLf = windowsLf;
		this.logLevel  = logLevel;
	}
	
	/**
	 * Outputs a line feed that does not appear in the log.
	 */
	public void println() {
		if (windowsLf)
			sb.append("\r\n");
		else
			sb.append('\n');
	}
	
	/**
	 * Outputs the given string to the log and the response.
	 * @param arg string to output
	 */
	public void println(String arg) {
		sb.append(arg);
		println();
		log(arg);
	}	
	
	/**
	 * Outputs the given formatted string to the log and the response.
	 * @param format string format ({@link String#format(String, Object...)}
	 * @param args   string format parameters
	 */
	public void printfln(String format, Object... args) {
		String message = String.format(format, args);
		sb.append(message);
		println();
		log(message);
	}	
		
	private void log(String s) {
		switch (logLevel()) {
		case NONE:
			break;
		case DEBUG : 
			logger.debug(s); 
			break;
		case INFO : 
			logger.info(s); 
			break;
		default:
			throw new ScriptException("unknown log level " + logLevel());
		}
	}
	
	/**
	 * Print log level.
	 * @return print log level
	 */
	public LogLevel logLevel() {
		return logLevel;
	}	
	
	public Result run() throws Exception {
		try {
			T t = buildParametersInstance(argClass(), play.mvc.Http.Context.current().request().queryString());
//			execute(t, play.mvc.Http.Context.current().request().body());
			execute(t);
			return Results.ok(sb.toString());
		} catch (Exception e) {
			sb.append("***************** ERROR ***********************\n");
			sb.append(org.apache.commons.lang.exception.ExceptionUtils.getStackTrace(e));
			sb.append("***************** ERROR ***********************\n");
			logger.error(e.getMessage(), e);
			return Results.notFound(sb.toString());			
		}
	}

	private T buildParametersInstance(Class<T> c, Map<String, String[]> args) throws Exception {
		return ReflectionUtils.readInstance(c, args);
	}

	private Class<T> argClass() {
		return ReflectionUtils.getDefiningClassTypeArgument(Script.class, getClass());
	}
	
	public abstract void execute(T args) throws Exception;

	// --------------------------------------------------------------------
	// -- Logger access and shorthands
	
	public play.Logger.ALogger getLogger() {
        return logger;
    }

	public void debug(String message) { logger.debug(message); }
	public void info (String message) { logger.info (message); } 
	public void warn (String message) { logger.warn (message); }
	public void error(String message) { logger.error(message); }
	
}

abstract class ScriptWithArgsAndBody<T> extends Script<T> {
	
	public ScriptWithArgsAndBody() {
		super();
	}
	
	@Override
	public void execute(T args) throws Exception {
		execute(args, play.mvc.Http.Context.current().request().body());
	}
	
	public abstract void execute(Object args, RequestBody body) throws Exception;
	
}
