package fr.cea.ig.lfw.utils;

import java.util.Optional;

import akka.stream.javadsl.Source;
import akka.util.ByteString;

import play.mvc.Result;
import play.http.HttpEntity;

/**
 * Source to HTTP result conversion.
 * The response type is "application/json" and this is not explicit in the method
 * names. 
 * 
 * This class is more like JSONResults in some http package.
 * 
 * @author vrd
 * 
 */
public class Streamer {

	/*
	 * Logger.
	 */
	// private static final play.Logger.ALogger logger = play.Logger.of(Streamer.class);
	
	/**
	 * Source to HTTP (JSON) chunk response.
	 * @param source source to send
	 * @return       OK HTTP result
	 */
	public static Result okChunked(Source<ByteString, ?> source) {
		 return new Result(200, HttpEntity.chunked(source, Optional.of("application/json")));
	}
	
	/**
	 * Source to HTTP (JSON) chunk response.
	 * Transitional re-export of okChunked until a proper stream without 
	 * size is sent. 
	 * @param source source to send
	 * @return       OK HTTP result
	 */
	public static Result okStream(Source<ByteString, ?> source) {
		 return okChunked(source);
	}
	
}
