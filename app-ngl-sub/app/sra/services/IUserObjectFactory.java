package sra.services;

import java.util.Map;

import services.sra.SraException;

public interface IUserObjectFactory<T> {
	
	public T create(Map<String, String> line) throws SraException;

}
