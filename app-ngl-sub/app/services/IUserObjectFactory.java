package services;

import java.util.Map;

import models.sra.submit.util.SraException;

public interface IUserObjectFactory<T> {
	
	public T create(Map<String, String> line) throws SraException;

}
