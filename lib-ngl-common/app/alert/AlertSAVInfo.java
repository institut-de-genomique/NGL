package alert;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.laboratory.alert.instance.Alert;


/**
 * Class collecting the alerts to display
 * @author ejacoby
 *
 */
public class AlertSAVInfo implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final String alertBAD = "BAD";
	public static final String alertFLAG = "FLAG";

	private String runCode;
	//Key1 : read number
	//Key2 : numberLane
	//Key3 : code alert (BAD, FLAG)
	public Map<String, Map<String, Map<String, Map<String,String>>>> allResults;

	private Map<String, String> cyclesErrRatedResults ;
	
	public boolean flagEvaluate;
	
	public AlertSAVInfo(String runCode) {
		super();
		this.runCode = runCode;
		this.allResults = new HashMap<>();
		this.cyclesErrRatedResults = new HashMap<>();
		flagEvaluate=false;
	}
	
	public void putData(String read, String numberLane, String codeAlert, Map<String, String> value)
	{
		Map<String, Map<String, Map<String,String>>> mapLane = new HashMap<>();
		Map<String, Map<String,String>> mapAlert = new HashMap<>();
		Map<String,String> values = new HashMap<>();
		if(allResults.containsKey(read)){
			mapLane = allResults.get(read);
			if(mapLane.containsKey(numberLane)){
				mapAlert = mapLane.get(numberLane);
				if(mapAlert.containsKey(codeAlert))
					values=mapAlert.get(codeAlert);
			}
		}

		values.putAll(value);
		mapAlert.put(codeAlert, values);
		mapLane.put(numberLane, mapAlert);
		allResults.put(read, mapLane);
	}
	
	public void putCyclesErrRated(String lane, String result) {
		cyclesErrRatedResults.put(lane, result);
	}

	public List<Alert> convertToAlert(String ruleName) {
		List<Alert> alerts = new ArrayList<>();
		
		for(String keyRead : allResults.keySet()){
			Map<String, Map<String, Map<String,String>>> mapLane = allResults.get(keyRead);
			for(String keyLane : mapLane.keySet()){
				Alert alert = new Alert();
				//Code alerte runCode.laneNumber.read
				alert.code=runCode+"."+keyLane+"."+keyRead;
				//Rule name
				alert.ruleName=ruleName;
				//Map propertiesAlert
				alert.propertiesAlert = new HashMap<>();
				Map<String, Map<String,String>> mapAlert = mapLane.get(keyLane);
				for(String keyAlert : mapAlert.keySet()){
					alert.propertiesAlert.put(keyAlert, new ArrayList<>(mapAlert.get(keyAlert).keySet()));
				}
				alerts.add(alert);
			}
		}
		return alerts;
	}
	
	public Map<String, Map<String, Map<String, Map<String, String>>>> getAllResults() {
		return allResults;
	}

	public void setAllResults(
			Map<String, Map<String, Map<String, Map<String, String>>>> allResults) {
		this.allResults = allResults;
	}

	public String getRunCode() {
		return runCode;
	}

	public void setRunCode(String runCode) {
		this.runCode = runCode;
	}

	
	public boolean isFlagEvaluate() {
		return flagEvaluate;
	}

	public void setFlagEvaluate(boolean flagEvaluate) {
		this.flagEvaluate = flagEvaluate;
	}

	@Override
	public String toString()
	{
		String message = "<p><strong>Run "+runCode+"</strong></p>";
		message+="<ul>";
		for(String keyRead : allResults.keySet()){
			message+="<li><strong><p>"+keyRead+"</strong></p>";
			Map<String, Map<String, Map<String,String>>> mapLane = allResults.get(keyRead);
			message+="<TABLE BORDER=\"1\"><TR><TH> Lane </TH><TH> <font COLOR=\"#FF0000\">"+alertBAD+"</font> </TH> <TH><font COLOR=\"#0000FF\"> "+alertFLAG+" </TH></TR> ";

			//Tri keyLane
			List<String> sortedKeyLanes = new ArrayList<>(mapLane.keySet());
			Collections.sort(sortedKeyLanes);
			for(String keyLane : sortedKeyLanes){
				message+="<TR>";
				message+="<TD>Number="+keyLane+"<p>cyclesErrRated="+cyclesErrRatedResults.get(keyLane)+"</p></TD>";
				Map<String, Map<String,String>> mapAlert = mapLane.get(keyLane);

				//Get BAD Messages
				message+="<TD>";
				if(mapAlert.containsKey(alertBAD)){
					Map<String,String> valuesBad = mapAlert.get(alertBAD);
					List<String> sortedKeyValueBad = new ArrayList<>(valuesBad.keySet());
					Collections.sort(sortedKeyValueBad);
					message+="<ul>";
					for(String keyValue : sortedKeyValueBad){
						message+="<li>"+keyValue+"="+valuesBad.get(keyValue)+"</li>";
					}
					message+="</ul>"; 
				}else
					message+="<p></p>";
				message+="</TD>";


				//Get Flag Messages
				message+="<TD>";
				if(mapAlert.containsKey(alertFLAG)){
					Map<String,String> valuesFlag = mapAlert.get(alertFLAG);
					List<String> sortedKeyValueFlag = new ArrayList<>(valuesFlag.keySet());
					Collections.sort(sortedKeyValueFlag);
					message+="<ul>"; 
					for(String keyValue : sortedKeyValueFlag){
						message+="<li>"+keyValue+"="+valuesFlag.get(keyValue)+"</li>";
					}
					message+="</ul>"; 
				}else
					message+="<p></p>";
				message+="</TD>";

				message+="</TR>";
			}
			message+="</TABLE> ";
		}
		message+="<ul>";
		return message;
	}



}
