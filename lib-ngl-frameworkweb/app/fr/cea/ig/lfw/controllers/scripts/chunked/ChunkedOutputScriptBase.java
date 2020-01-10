package fr.cea.ig.lfw.controllers.scripts.chunked;

import java.util.Map;
import java.util.function.Consumer;

import akka.actor.ActorRef;
import akka.util.ByteString;
import fr.cea.ig.lfw.reflect.ReflectionUtils;
import play.mvc.Http.RequestBody;

/**
 * Script with chunk (one line per chunk) output. Printed messages ({@link #println(String)} or {@link #println(String, Object...)}
 * uses the configured line feeds ({@link #useUNIXLF()}, {@link #useWindowsLF()} and the configured
 * logging level ({@link #logPrintAsDebug()}, {@link #logPrintAsInfo()}, {@link #logPrintAsNothing()}).
 * 
 * @author vrd
 *
 * @param <T> script parameter type
 */
public abstract class ChunkedOutputScriptBase<T> {
	
	/**
	 * Logger.
	 */
	protected final play.Logger.ALogger logger;
	
	/**
	 * Line feed.
	 */
	private String lf;
	
	/**
	 * Logger output delegate.
	 */
	private Consumer<String> loggerOutput;
	
	/**
	 * HTTP output actor.
	 */
	private ActorRef output;
	
	/**
	 * UNIX line feeds and debug level logging.
	 */
	public ChunkedOutputScriptBase() {
		logger = play.Logger.of(getClass());
		useUNIXLF();
		logPrintAsDebug();
	}
	
	/**
	 * Use debug logging for printed messages. 
	 */
	public void logPrintAsDebug() { 
		loggerOutput = s -> logger.debug(s); 
	}
	
	/**
	 * Use info logging for printed messages.
	 */
	public void logPrintAsInfo() { 
		loggerOutput = s -> logger.info(s); 
	}
	
	/**
	 * Use no logging for printed messages. 
	 */
	public void logPrintAsNothing() { loggerOutput = s -> {}; } 
	
	/**
	 * Use windows line feeds.
	 */
	public void useWindowsLF() { 
		lf = "\r\n"; 
	}
	
	/**
	 * Use UNIX line feeds.
	 */
	public void useUNIXLF() { 
		lf = "\n"; 
	}
	
	/**
	 * Controller execution entry point.
	 * @param args       URL parameters
	 * @param body       HTTP request body
	 * @throws Exception something went wrong
	 */
	public void run(Map<String,String[]> args, RequestBody body) throws Exception {
		try {
			execute(buildParametersInstance(argClass(), args), body);
		} catch (Exception e) {
			StringBuilder sb = new StringBuilder();
			sb.append("***************** ERROR ***********************\n");
			sb.append(org.apache.commons.lang.exception.ExceptionUtils.getStackTrace(e));
			sb.append("***************** ERROR ***********************\n");
			output(sb.toString());
			logger.error(e.getMessage(), e);
			throw e;
		}
	}
	
	/**
	 * Process an HTTP request.
	 * @param args       URL parameters
	 * @param body       request body
	 * @throws Exception something went wrong
	 */
	public abstract void execute(T args, RequestBody body) throws Exception;
	
	/**
	 * Print a new line.
	 */
	public void println() {
		output(lf);
	}

	/**
	 * Print a plain message.
	 * @param message message
	 */
	public void println(String message) {
		output(message + lf);
		loggerOutput.accept(message);
	}
	
	/**
	 * Print a formated message ({@link String#format(String, Object...)}.
	 * @param message message format
	 * @param args    format parameters
	 */
	public void println(String message, Object... args) {
		println(String.format(message, args));
	}
	
	// -------------------------------------------------------------------------------
	// -- Internals

	/**
	 * Called before the script execution.
	 * @param output HTTP output
	 */
 	public void initialize(ActorRef output) {
		this.output = output;
	}
	
	/**
	 * Build an instance of a given class interpreting the fields from a map of
	 * named values.
	 * @param c          class to build an instance of
	 * @param args       named values to interpret
	 * @return           instance with populated fields.
	 * @throws Exception something went wrong
	 */
	private T buildParametersInstance(Class<T> c, Map<String, String[]> args) throws Exception {
		return ReflectionUtils.readInstance(c, args);
	}

	/**
	 * Parameter argument class.
	 * @return parameter argument class
	 */
	private Class<T> argClass() {
		logger.debug("script class " + getClass());
		return ReflectionUtils.getDefiningClassTypeArgument(ChunkedOutputScriptBase.class, getClass());
	}
	
	/**
	 * Print a string to the HTTP response.
	 * @param message message to print
	 */
	private void output(String message) {
		output.tell(ByteString.fromString(message), null);
	}
	
}


