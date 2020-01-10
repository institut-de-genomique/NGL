package fr.cea.ig.lfw.controllers;

import play.mvc.Result;
import play.mvc.Results;

public  abstract class AbstractScript {
	
	private final play.Logger.ALogger logger;
	
	private final StringBuilder sb;
	
	private boolean windowsLf = false;
	
	public AbstractScript() {
		logger = play.Logger.of(getClass());
		sb     = new StringBuilder();
	}
	
	public Result run() {
		try {
			execute();
			return Results.ok(sb.toString());
		} catch (Exception e) {
			sb.append("***************** ERROR ***********************\n");
			//sb.append(e.getMessage());// println(e);
			sb.append(org.apache.commons.lang.exception.ExceptionUtils.getStackTrace(e));
			sb.append("***************** ERROR ***********************\n");
			logger.error(e.getMessage(), e);
			return Results.notFound(sb.toString());			
		}
	}
	
	public enum LogLevel {
		None,
		Debug, 
		Info
	}
	
	public void println() {
		if (windowsLf)
			sb.append("\r\n");
		else
			sb.append('\n');
	}
	
	public void println(String arg) {
		sb.append(arg);
//		sb.append('\n');
		println();
		log(arg);
//		switch (logLevel()) {
//		case None:
//			break;
//		case Debug : 
//			logger.debug(arg); 
//			break;
//		case Info : 
//			logger.info(arg); 
//			break;
//		default:
//			throw new RuntimeException("unkown log level " + logLevel());
//		}
	}	
	
	public void printfln(String format, Object... args ) {
		String arg = String.format(format, args);
		sb.append(arg);
//		sb.append('\n');
		println();
		log(arg);
	}	
		
	private void log(String s) {
		switch (logLevel()) {
		case None:
			break;
		case Debug : 
			logger.debug(s); 
			break;
		case Info : 
			logger.info(s); 
			break;
		default:
			throw new RuntimeException("unkonw log level " + logLevel());
		}
	}
	
	public LogLevel logLevel() {
		return LogLevel.Debug;
	}	
	
	// methode abstraite qui sera impementée dans les != script
	public abstract void execute() throws Exception;
	
}

