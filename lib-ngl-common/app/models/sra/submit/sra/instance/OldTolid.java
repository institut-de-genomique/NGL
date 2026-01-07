package models.sra.submit.sra.instance;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class OldTolid {
	public String tolid;
	public String ticket;
	public Date date;
	
//	// Pas bseoin de mettre un constructeur sans argument pour la deserialisation car annotation sur dernier constructeur
//	public OldTolid() {
//		this.date = new Date();
//	}

	public OldTolid(String tolid) {
		this.tolid = tolid;
		this.date = new Date();
	}
	
	@JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
	public OldTolid(@JsonProperty("tolid") String tolid, @JsonProperty("ticket") String ticket, @JsonProperty("date")Date date) {
		this.tolid = tolid;
		this.ticket = ticket;
		this.date  = date;
	}
}


