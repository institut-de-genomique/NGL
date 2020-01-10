package controllers.history;


/**
 * User action model
 * 
 * This is the model stored in mongodb
 * 
 * @author ydeshayes
 */

import java.text.SimpleDateFormat;
import java.util.Date;

import fr.cea.ig.DBObject;

public class UserAction extends DBObject{
	public String login;
	public String action;
	public String queryString;
	public String body;
	public Date date;
	public long timeRequest;
	
	
	
	//default constructor for mongodb
	public UserAction(){
		this.login = "";
		this.queryString = "";
		this.body = "";
		this.action = "";
		this.timeRequest = 0;
	    this.date = new Date();
	    this.code = "";
	}
	
	public UserAction(String varLogin,String varQueryString,String varBody, String varAction, long varTimeRequest){
		this.login = varLogin;
		this.queryString = varQueryString;
		this.body = varBody;
		this.action = varAction;
	    this.timeRequest = varTimeRequest;
	    this.date = new Date();
	    this.code = generateCode(login);
	}
	
	private String generateCode(String login){
		return ((new SimpleDateFormat("yyyyMMddHHmmss.SSS")).format(new Date()) + login).toUpperCase();
	}
}
