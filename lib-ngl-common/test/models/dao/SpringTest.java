package models.dao;


import java.util.List;

import org.junit.Assert;

import models.laboratory.common.description.ObjectType;
import models.laboratory.common.description.State;
import models.utils.dao.DAOException;
import utils.AbstractTests;

/**
 * Test sur base vide avec dump.sql
 * @author ejacoby
 *
 */

public class SpringTest extends AbstractTests {

	private void checkObjectType(ObjectType type)
	{
		Assert.assertNotNull(type);
		Assert.assertNotNull(type.id);
		Assert.assertNotNull(type.code);
		Assert.assertNotNull(type.generic);
	}
	
	
	//@Test
	public void testType() throws DAOException
	{
		ObjectType type = ObjectType.find.findByCode("Experiment");
		checkObjectType(type);
	}

	//@Test
	public void testAllTypes() throws DAOException
	{
		List<ObjectType> types = ObjectType.find.findAll();
		Assert.assertNotNull(types.size()>0);
		for(ObjectType type : types){
			checkObjectType(type);
		}
	}

	//@Test
	public void testDeleteType() throws DAOException
	{
		ObjectType type = ObjectType.find.findByCode("UpdateTest");
		type.remove();
		ObjectType objectType = ObjectType.find.findByCode("UpdateTest");
		Assert.assertNull(objectType);
	}

	//@Test
	public void testStateAll() throws DAOException
	{
		List<State> states = State.find.findAll();
		Assert.assertNotNull(states);
		Assert.assertTrue(states.size()>0);
		for(State state : states){
			Assert.assertNotNull(state.id);
		}
	}
	
}
