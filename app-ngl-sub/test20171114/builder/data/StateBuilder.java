package builder.data;

import java.util.HashSet;

import models.laboratory.common.instance.State;

public class StateBuilder {

	State state = new State();
	
	public StateBuilder withCode(String code)
	{
		state.code=code;
		return this;
	}
	
	public StateBuilder withUser(String user)
	{
		state.user=user;
		return this;
	}
	
	public State build(){
		state.historical=new HashSet<>();
		return state;
	}
	
}
