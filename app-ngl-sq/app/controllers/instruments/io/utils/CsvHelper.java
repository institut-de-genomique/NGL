package controllers.instruments.io.utils;


import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import scala.io.Codec;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.swing.tree.ExpandVetoException;

import models.laboratory.common.instance.PropertyValue;
import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.container.instance.Container;
import models.laboratory.container.instance.Content;
import models.laboratory.experiment.instance.Experiment;
import models.laboratory.instrument.description.Instrument;
import models.laboratory.parameter.index.Index;
import models.utils.dao.DAOException;
import play.Logger;
import play.Play;

// used to help to set export files

public class CsvHelper {
			
	/**
	 * adapt analysis type name for reading by HiSeq3000
	 * 
	 * @param analyseType
	 * @return
	 */
	public static String getFromAnalyseType(String analyseType){
		Logger.debug("getFromAnalyseType analyseType : " + analyseType);
		switch (analyseType.trim()) {
		case "DNA":
		case "RAD-Seq":
		case "ReadyToLoad":
		case "MeDIP-Seq":
		case "ChIP-Seq":
		case "10X":
		case "10X-DeNovo":
		case "10X-WGS":
		case "10X-SingleCell":
			return " illumina_qc";
		case "RNA":
			return " illumina_rnaseq";
		case "16S":
		case "Amplicon":
			return " illumina_diversity_qc";
        case "Bisulfite":
        case "Bisulfite-DNA":
            return " methylseq";
        case "DNA-MP":
            return "illumina_matepair";
        case "RNA-Stranded": //to validate
			return " illumina_rnaseq";
		default:
			return "illumina_qc";
		}
	}

	//catch si le type d'index est 10x
	public static Boolean catch10x(Content content) {
		if (OutputHelper.getContentProperty(content,"tag").startsWith("10X_SI-GA")){
			return true;
		}else {
			return false;
		}
	}
	
	public static String getHiseqAdapter(Content smpl){
		Index index = sampleIndex(smpl);
		if (index == null){
			return "Custom";
		}
		else if (index.name.contains("Nextera")){
			return "Nextera";
		}
		else if (index.supplierName.containsKey("Custom")){
			return "Custom";
		}
		else{
			return "Illumina";
		}
	}
	
	/**
	 * used for adapt sample name for reading by machine
	 * 
	 * @param name as String
	 * @return a same String, after replace, all special character except "-", by "-"
	 */
	public static String checkName(String name) {
        return name.replaceAll("[^\\w-]+", "-").replaceAll("_", "-").replaceAll("[-]{2}", "-");
	}
	
	/**
	 * 
	 * @return string of formated today date
	 */
	public static String getDate() {
		return (new SimpleDateFormat("dd/MM/yyyy")).format(new Date());
	}

	/**
	 * 
	 * @return string of formated run date
	 */
	public static String getRunDate(Experiment experiment) {
		return new SimpleDateFormat("dd/MM/yyyy").format((Date) experiment.experimentProperties.get("runStartDate").value);
	}
	/**
	 * get instrument name by code
	 * 
	 * @param code
	 * @return
	 */
	public static String getInstrumentName(String code){
		Instrument instrument =  Instrument.find.findByCode(code);
		if (instrument != null) {
			return checkName(instrument.name);
		}else{
			return "-";
		}
	}
	
	/**
	 * get sample index
	 * 
	 * @param smpl
	 * @return
	 */
	public static Index sampleIndex(Content smpl) {
		return OutputHelper.getIndex("index-illumina-sequencing",OutputHelper.getContentProperty(smpl,"tag"));
	}

	/**
	 * get index model for experience
	 * 
	 * @param containers
	 * @return
	 */
	public static TagModel getTagModel(List<Container> containers) {
		List<PropertyValue> tags = containers.stream().map((Container container) -> container.contents)
			.flatMap(List::stream)
			.filter(c -> c.properties.containsKey("tag"))
			.filter(c -> c.properties.get("tagCategory") != null)
			.filter(c -> !c.properties.get("tagCategory").equals("MID"))
			.map((Content c) -> c.properties.get("tag"))
			.collect(Collectors.toList())
			;
		TagModel tagModel = new TagModel();
		if(tags.size() > 0){
			tagModel.maxTag1Size = 0;
			tagModel.maxTag2Size = 0;
			tagModel.tagType = "SINGLE-INDEX";
			for(PropertyValue _tag : tags){
				PropertySingleValue tag = (PropertySingleValue)_tag;
				Index index = OutputHelper.getIndex("index-illumina-sequencing", tag.value.toString());
				Logger.debug("getTagModel : " + index);
				
				if("SINGLE-INDEX".equals(index.categoryCode)) {
					if(index.sequence.length() > tagModel.maxTag1Size){
						tagModel.maxTag1Size = index.sequence.length();
					}
				}else if("DUAL-INDEX".equals(index.categoryCode)) {
					tagModel.tagType = "DUAL-INDEX";
					
					String[] sequences = index.sequence.split("-",2);
					if(sequences[0].length() > tagModel.maxTag1Size){
						tagModel.maxTag1Size = sequences[0].length();
					}
					
					if(sequences[1].length() > tagModel.maxTag2Size){
						tagModel.maxTag2Size = sequences[1].length();
					}
				}						
			};
		}else{
			tagModel.tagType = "NO-INDEX";
		}
				
		return tagModel;
	}
	
	public static String reverseComplement(String ind) {
		StringBuilder tagRev = new StringBuilder();
		for (Character base : ind.toCharArray() ) {
		switch (base) {
		case 'T':
			tagRev.append('A');
			break;
		case 'A':
			tagRev.append('T');
			break;
		case 'C':
			tagRev.append('G');
			break;
		case 'G':
			tagRev.append('C');
			break;
		}	
			 
		}
		return tagRev.reverse().toString();
	}
	
	public static String getContentProcessProperties(Content content, String propertyName) {
//		Logger.debug("getContentProcessProperties - " + propertyName + " - " + content.sampleCode);
//		if(content.processProperties != null){
//			Logger.debug("getContentProcessProperties - OK");
//			if(content.processProperties != null && content.processProperties.containsKey(propertyName)){
//				return (String) content.processProperties.get(propertyName).getValue();
//			}
//		}
//		return null;
		return getContentProcessProperties(content, propertyName, null);
	}

	public static String getContentProcessProperties(Content content, String propertyName, Experiment experiment) {
		if(content.processProperties != null){
			Logger.debug("content.ProcessProperties - OK pour " + propertyName);
			if(content.processProperties != null && content.processProperties.containsKey(propertyName)){
				return (String) content.processProperties.get(propertyName).getValue();
			}
		}else if(experiment != null && experiment.inputProcessCodes != null){
			for( String processCode : experiment.inputProcessCodes){
				Logger.debug("experiment.inputProcessCodes - " + processCode + " OK pour " + content.sampleCode);
				if(processCode.matches(content.sampleCode + "(.*)")){
					Logger.debug("experiment.inputProcessCodes - " + processCode);
					return OutputHelper.getProcessProperty(processCode, propertyName);
				}
			}
		}
		Logger.error("content.ProcessProperties - " + propertyName + " null pour " + content.sampleCode);
		return null;
	}
}