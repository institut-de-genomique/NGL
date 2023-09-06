package controllers.migration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;

import controllers.CommonController;
import fr.cea.ig.MongoDBDAO;
import models.laboratory.common.instance.PropertyValue;
import models.laboratory.common.instance.property.PropertyObjectListValue;
import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.run.instance.ReadSet;
import models.laboratory.run.instance.Treatment;
import models.utils.InstanceConstants;
import play.Logger;
import play.mvc.Result;

/**
 * Ticket SUPSQ-1978
 * @author ejacoby
 *
 */
public class MigrationUpdateKraken extends CommonController{

	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmm");
	
	public static Result migration() throws IOException
	{
		backUpReadSet();
				
		
		BufferedReader buf = new BufferedReader(new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream("resultSUPSQ1978.csv")));
		String line = null;
		int nbLine=0;
		int nbNotFound=0;
		int nbNoTaxo=0;
		String readSetNotFound="";
		String readSetWithNoTaxonomy="";
		while((line=buf.readLine())!=null){
			Logger.debug("Line "+line);
			String[] datas = line.split(" ");
			String codeReadSet = datas[0];
			//Find ReadSet in DB
			ReadSet readSet = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, codeReadSet);
			if(readSet==null){
				nbNotFound++;
				readSetNotFound+=codeReadSet+"\n";
			}else{
				//Get Treatment taxonomy
				Treatment treatmentTaxonomy = readSet.treatments.get("taxonomy");
				if(treatmentTaxonomy!=null){
					//Get key results 
					if(treatmentTaxonomy.results().keySet().size()>1){
						Logger.debug("2 reads for "+codeReadSet);
					}else if(treatmentTaxonomy.results.keySet().size()==1){
						Map<String, PropertyValue> resultRead1 = treatmentTaxonomy.results.get("read1");
						
						//Update software
						treatmentTaxonomy.results.get("read1").put("software",new PropertySingleValue("kraken"));
						
						//Get taxon bilan 
						PropertyObjectListValue propertyTaxonBilan = (PropertyObjectListValue) resultRead1.get("taxonBilan");
						//get value of taxonBilan
						List<Map<String, ?>> valuesTaxonBilan = propertyTaxonBilan.value;
						List<Map<String,?>> newValuesTaxonBilan = new ArrayList<Map<String,?>>();
						
						//Create new value Unknown::No hits nbSeq percent
						Map<String, Object> bilanUnknownNoHits = createTaxonBilan("Unknown::No hits", datas[1], datas[2]);
						newValuesTaxonBilan.add(bilanUnknownNoHits);
						Map<String, Object> bilanUnknownNoAssigned = createTaxonBilan("Unknown::Not assigned", datas[3], datas[4]);
						newValuesTaxonBilan.add(bilanUnknownNoAssigned);
						for(Map<String, ?> keyValue : valuesTaxonBilan){
							if(!keyValue.get("taxon").equals("Unknown::No hits") && !keyValue.get("taxon").equals("Unknown::Not assigned") && !keyValue.get("taxon").equals("unclassified")){
								newValuesTaxonBilan.add(keyValue);
							}
						}
						propertyTaxonBilan.value=newValuesTaxonBilan;
						treatmentTaxonomy.results.get("read1").put("taxonBilan", propertyTaxonBilan);
												
						//Get division bilan
						PropertyObjectListValue propertyDivisionBilan = (PropertyObjectListValue) resultRead1.get("divisionBilan");
						List<Map<String, ?>> valuesDivisionBilan = propertyDivisionBilan.value;
						List<Map<String,?>> newValuesDivisionBilan = new ArrayList<Map<String,?>>();
						
						String allTaxonDivision="";
						for(Map<String, ?> keyValue : valuesDivisionBilan){
							allTaxonDivision+=keyValue.get("division")+" ";
							if(!keyValue.get("division").equals("No hits")){
								newValuesDivisionBilan.add(keyValue);
							}
						}
						
						if(!allTaxonDivision.contains("Eukaryota")){
							newValuesDivisionBilan.add(createEmptyDivision("Eukaryota"));
						}
						if(!allTaxonDivision.contains("Bacteria")){
							newValuesDivisionBilan.add(createEmptyDivision("Bacteria"));
						}
						if(!allTaxonDivision.contains("Archaea")){
							newValuesDivisionBilan.add(createEmptyDivision("Archaea"));
						}
						if(!allTaxonDivision.contains("Viruses")){
							newValuesDivisionBilan.add(createEmptyDivision("Viruses"));
						}
						propertyDivisionBilan.value=newValuesDivisionBilan;
						treatmentTaxonomy.results.get("read1").put("divisionBilan", propertyDivisionBilan);
						
						Logger.debug("Update readSet "+codeReadSet);
						//update treatment
						MongoDBDAO.update(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, DBQuery.is("code", codeReadSet),  DBUpdate.set("treatments.taxonomy", treatmentTaxonomy));
						
					}
					
				}else{
					nbNoTaxo++;
					readSetWithNoTaxonomy+=codeReadSet+"\n";
				}
			}
			nbLine++;
		}
		Logger.debug("Nb line "+nbLine);
		buf.close();
		
		
		if(nbNotFound>0 || nbNoTaxo>0){
			Logger.debug("ReadSet not found "+readSetNotFound);
			Logger.debug("ReadSet no taxonomy "+readSetWithNoTaxonomy);
			return badRequest("ReadSet not found "+nbNotFound+" ReadSet no taxonomy "+nbNoTaxo);
		}else
			return ok();
	}

	private static void backUpReadSet() throws IOException
	{
		String backupName = InstanceConstants.READSET_ILLUMINA_COLL_NAME+"_BCK_SUBSQ1978_"+sdf.format(new java.util.Date());
		Logger.info("\tCopie "+InstanceConstants.READSET_ILLUMINA_COLL_NAME+" to "+backupName+" start");
		
		List<ReadSet> readSets = new ArrayList<ReadSet>();
		BufferedReader buf = new BufferedReader(new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream("resultSUPSQ1978.csv")));
		String line="";
		while((line=buf.readLine())!=null){
			String[] datas = line.split(" ");
			String codeReadSet = datas[0];
			//Find ReadSet in DB
			ReadSet readSet = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, codeReadSet);
			if(readSet!=null)
				readSets.add(readSet);
		}
		buf.close();
		MongoDBDAO.save(backupName, readSets);
		Logger.info("\tCopie "+InstanceConstants.READSET_ILLUMINA_COLL_NAME+" to "+backupName+" end");
		
	}
	
	private static Map<String, Object> createEmptyDivision(String divisionName)
	{
		Map<String, Object> division = new HashMap<String, Object>();
		division.put("division", divisionName);
		division.put("nbSeq",new Integer(0));
		division.put("percent", new Double(0));
		return division;
	}
	
	private static Map<String, Object> createTaxonBilan(String taxonName, String nbSeq, String percent)
	{
		Map<String, Object> taxonBilan = new HashMap<String, Object>();
		taxonBilan.put("taxon",taxonName);
		taxonBilan.put("nbSeq",new Integer(nbSeq));
		taxonBilan.put("percent", new Double(percent));
		return taxonBilan;
	}
}
