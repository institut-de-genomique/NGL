package scripts.ATSea.model;

import java.util.Date;

/**
 * Modelisation des données récupérées de Cardops
 * @author ejacoby
 *
 */
public class CardopsData {
	
	public String stationNumber;
	public Date collectDate;
	public String collectLocalisation;
	public Double latitudeStart;
	public Double longitudeStart;
	public Double latitudeEnd;
	public Double longitudeEnd;
	public Double depth;
	public String collectMethod;
	public String collectName;
	public String habitat;
	
	
	
	
	public CardopsData(String stationNumber, Date collectDate, String collectLocalisation, Double latitudeStart,
			Double longitudeStart, Double latitudeEnd,
			Double longitudeEnd, Double depth, String collectMethod, String collectName, String habitat) {
		super();
		this.stationNumber = stationNumber;
		this.collectDate = collectDate;
		this.collectLocalisation = collectLocalisation;
		this.latitudeStart = latitudeStart;
		this.longitudeStart = longitudeStart;
		this.latitudeEnd = latitudeEnd;
		this.longitudeEnd = longitudeEnd;
		this.depth = depth;
		this.collectMethod = collectMethod;
		this.collectName = collectName;
		this.habitat=habitat;
	}
	
	public String toString()
	{
		return stationNumber+","+collectDate+","+collectLocalisation+","+latitudeStart+","+longitudeStart+","+depth+","+collectMethod+","+collectName+","+habitat;
	}

}
