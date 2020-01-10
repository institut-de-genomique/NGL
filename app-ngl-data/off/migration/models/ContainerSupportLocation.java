package controllers.migration.models;

import fr.cea.ig.DBObject;

public class ContainerSupportLocation extends DBObject{
	
	public String container;
	public String line;
	public String column;
	public String support;
	
	public String toString(){
		return "Container :"+container+", line :"+line+", column :"+column+", support :"+support;
	}

}
