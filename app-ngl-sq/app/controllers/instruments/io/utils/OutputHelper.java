package controllers.instruments.io.utils;



import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import models.laboratory.common.instance.PropertyValue;
import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.container.instance.Container;
import models.laboratory.container.instance.Content;
import models.laboratory.experiment.instance.AbstractContainerUsed;
import models.laboratory.experiment.instance.AtomicTransfertMethod;
import models.laboratory.experiment.instance.Experiment;
import models.laboratory.experiment.instance.InputContainerUsed;
import models.laboratory.instrument.description.Instrument;
import models.laboratory.parameter.index.Index;
import models.utils.InstanceConstants;
import models.utils.dao.DAOException;
import models.laboratory.processes.instance.Process;
import controllers.processes.api.Processes;

import org.apache.commons.lang3.StringUtils;
import org.mongojack.DBQuery;

import play.Logger;
import play.Play;
import scala.io.Codec;
import fr.cea.ig.MongoDBDAO;

public class OutputHelper {

	
	public static String getInstrumentPath(String instrumentCode, boolean addSampleSheet){
		Instrument instrument = null;
		try {
			instrument = Instrument.find.findByCode(instrumentCode);
		} catch (DAOException e) {
			Logger.error("DAO error: "+e.getMessage(),e);
		}
		if(instrument != null){
			if(Play.application().configuration().getString("ngl.path.instrument") != null){
				return Play.application().configuration().getString("ngl.path.instrument")+java.io.File.separator;
			}else if(addSampleSheet){
				return instrument.path+java.io.File.separator+"SampleSheet"+java.io.File.separator;
			}else {
				return instrument.path+java.io.File.separator;
			}
		}
		return null;
	}
	
	public static String getInstrumentPath(String instrumentCode){		
		return getInstrumentPath(instrumentCode, true);
	}
	
	public static void writeFile(File file) {
		Writer writer = null;
		try {
			
			FileOutputStream fos = new FileOutputStream(file.filename);
			writer = new OutputStreamWriter(fos, Codec.UTF8().name());			
			writer.write(file.content);
			writer.append("\r\n");
			writer.close();
			fos.close();
			
		} catch (Exception e) {
			Logger.error("Problem to create sample sheet",e);
			Logger.error("DAO error: "+e.getMessage(),e);
		}
		
	}
	
	public static String format(String content){
		if(content != null){
			return content.trim().replaceAll("(?m)^\\s{1,}", "").replaceAll("\n{2,}", "\n");
		}
		return "";
	}
	
	public static List<Container> getInputContainersFromExperiment(Experiment experiment){
		List<Container> containers = new ArrayList<Container>();
		for(int i=0; i<experiment.atomicTransfertMethods.size();i++){
			for(InputContainerUsed cu : experiment.atomicTransfertMethods.get(i).inputContainerUseds){
				containers.add(MongoDBDAO.findByCode(InstanceConstants.CONTAINER_COLL_NAME, Container.class, cu.code));
			}
		}
		
		return containers;
	}
	
	public static String getOutputContainerUsedCode(AtomicTransfertMethod atomic){		
		return atomic.outputContainerUseds.get(0).code;
	}
	
	public static Index getIndex(String typeCode, String code){
		Index index  = MongoDBDAO.findOne(InstanceConstants.PARAMETER_COLL_NAME, Index.class, DBQuery.is("typeCode", typeCode).and(DBQuery.is("code", code)));
		return index;
	}
	
	public static String getSequence(Index index, TagModel tagModel, String instrumentTypeCode) {
		return 	getSequence(index, tagModel, instrumentTypeCode, null);
	}
	
	
	public static String getSequence(Index index, TagModel tagModel, String instrumentTypeCode, Integer position){
		
		Logger.debug("OutputHelper.getSequence Tag = "+ instrumentTypeCode);
		if("NONE".equals(tagModel.tagType)){
			return null;
		}else if("NoIndex".equals(tagModel.tagType)){
				return null;
		}else if("SINGLE-INDEX".equals(tagModel.tagType)){
			if(null == index || "MID".equals(index.categoryCode)){
				return getIndex(null, tagModel.maxTag1Size);
			} else if("POOL-INDEX".equals(index.categoryCode)) {
				String [] sequences = index.sequence.split("-");
				return getIndex(sequences[position], tagModel.maxTag1Size);
			} else {
				return getIndex(index.sequence, tagModel.maxTag1Size);
			}
		}else if("DUAL-INDEX".equals(tagModel.tagType)){
			String sequence = null;
			if(null == index || "MID".equals(index.categoryCode)){
				sequence = StringUtils.repeat("N", tagModel.maxTag1Size)+"-"+StringUtils.repeat("N", tagModel.maxTag2Size);
			}else if("SINGLE-INDEX".equals(index.categoryCode)){
				sequence = getIndex(index.sequence, tagModel.maxTag1Size)+"-"+getIndex(null, tagModel.maxTag2Size);
			}else if("POOL-INDEX".equals(index.categoryCode)) {
				String [] sequences = index.sequence.split("-");
				sequence = getIndex(sequences[position], tagModel.maxTag1Size)+"-"+getIndex(null, tagModel.maxTag2Size);
			}else {
				String[] sequences = index.sequence.split("-",2);
				sequence = getIndex(sequences[0], tagModel.maxTag1Size)+"-"+getIndex(sequences[1], tagModel.maxTag2Size);
			}
			if("HISEQX".equals(instrumentTypeCode)){
				sequence = sequence.split("-")[0];
			}
			
			return sequence;
		}else{
			throw new RuntimeException("Index not manage "+tagModel.tagType);
		}
	}

	private static String getIndex(String sequence, Integer maxIndexSize) {
		if(null == sequence){
			return StringUtils.repeat("N", maxIndexSize);
		}else if(sequence.length() < maxIndexSize){
			return sequence.concat(StringUtils.repeat("N", maxIndexSize-sequence.length()));
		}else{
			return sequence;
		}
	}
	
	
	
	public static String getSequence(Index index){
		if(index != null && !index.categoryCode.equals("MID")){
			return index.sequence;
		}else{
			return null;
		}
	}
	
	public static String getContentProperty(Content content, String propertyName){
//		Logger.debug("OutputHelper.getContentProperty propertyName = "+ propertyName);
		if(content.properties.get(propertyName) != null){
			return (String) content.properties.get(propertyName).value;
		}
        Logger.debug("OutputHelper.getContentProperty property " + propertyName + " is null for " + content.sampleCode);
		return "";
	}
	
	public static Double getContentDoubleProperty(Content content, String propertyName){
		if(content.properties.get(propertyName) != null){
			return  (Double) content.properties.get(propertyName).value;
		}
		return 0.0;
	}	
	
	public static String getSupplierName(Index tag, String supplierName){
		if(tag!= null && tag.supplierName != null){
			return tag.supplierName.get("illumina");
		}
		
		return "";
	}
	
	public static String getIntrumentBooleanProperties(Experiment experiment,String propertyName){
		if(experiment.instrumentProperties.get(propertyName) != null && Boolean.class.isInstance(experiment.instrumentProperties.get(propertyName).value)){
			if((Boolean) experiment.instrumentProperties.get(propertyName).value){
				return "O";
			}
		}
		return "N";
	}
	
	
	
	public static String getContainerProperty(Container container, String propertyName){
		if(container.properties.get(propertyName) != null && Boolean.class.isInstance(container.properties.get(propertyName).value)){
			if((Boolean) container.properties.get(propertyName).value){
				return "O";
			}
			return "N";
		}
		
		return (String) container.properties.get(propertyName).value;
	}
	
	public static String getInputContainerUsedExperimentProperty(InputContainerUsed container, String propertyName){	
		if(container.experimentProperties.containsKey(propertyName)){
			return container.experimentProperties.get(propertyName).value.toString().replace(".",",") ;
		}else{
			return "";
		}
	}
	
	public static String getInputContainerUsedExperimentProperty(InputContainerUsed container, String propertyName, int scale){
		if(container.experimentProperties.containsKey(propertyName)
				&& !container.experimentProperties.get(propertyName).value.equals("")){
			return new BigDecimal(container.experimentProperties.get(propertyName).value.toString()).setScale(scale, BigDecimal.ROUND_UP).toString().replace(".",",") ;
		}
		return "";
	}
	
	public static Object getExperimentProperty(AbstractContainerUsed container, String propertyName){
			Logger.debug("getExperimentProperty propertyName : " + propertyName );
			Logger.debug("getExperimentProperty container : " + container.code );
			Logger.debug("getExperimentProperty experimentProperties : " + container.experimentProperties.size() );
		if(container.experimentProperties.containsKey(propertyName)){
			return container.experimentProperties.get(propertyName).value;
		}
		return null;
	}
	
	public static Object getInstrumentProperty(AbstractContainerUsed container, String propertyName){
		if(container.instrumentProperties.containsKey(propertyName)){
			return container.instrumentProperties.get(propertyName).value;
		}
		return null;
	}
	
	public static Object getInstrumentProperty(Experiment experiment,String propertyName){
		if(experiment.instrumentProperties.get(propertyName) != null){
			return experiment.instrumentProperties.get(propertyName).value;
		}
		return null;
	}
	
	public static TagModel getTagModel(List<Container> containers) {
		List<PropertyValue> tags = containers.stream().map((Container container) -> container.contents)
			.flatMap(List::stream)
			.filter(c -> c.properties.containsKey("tag"))
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
				Index index = getIndex("index-illumina-sequencing", tag.value.toString());
				
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
	
	// Cet algorithme est utile pour les robots qui numerotent les plaques 96 en colonne 
	// A1=1, B1=2...A2=9
	public static int getNumberPositionInPlateByColumn(String line,String column){
		int asciiValue=(int) line.toCharArray()[0];
		int columnValue=Integer.parseInt(column);
		
		return (asciiValue-64)+(columnValue-1)*8;
	}
	
	// Cet algorithme est utile pour les robots qui numerotent les plaques 96 en ligne 
		// A1=1, A2=2
	public static int getNumberPositionInPlateByLine(String line,String column){
		int asciiValue=(int) line.toCharArray()[0];
		int columnValue=Integer.parseInt(column);
		
		return (asciiValue-64)*12-12+columnValue;
	}
	
	
	// Cet algorithme est utile pour les robots qui numerotent les plaques 96 en colonne 
	// 1=A1, 2=B1...9=A2
	// FDS a tester !!
	public static String getLineColumnInPlateBycolumn(int position){
		String line  = Integer.toString((position -1) % 8) +'A';
		String column= Integer.toString((position -1) / 8);
		
		return line+column;
	}
	
	public static String getTag(InputContainerUsed container) {
		return container.contents.stream().map((Content c) -> (String) c.properties.get("tag").value)
				.collect(Collectors.toList()).get(0);
	}

	public static boolean sortBylocationSupportOneToOne(AtomicTransfertMethod atm1, AtomicTransfertMethod atm2){
		InputContainerUsed icu1 = atm1.inputContainerUseds.get(0);
		InputContainerUsed icu2 = atm2.inputContainerUseds.get(0);
		
		return getNumberPositionInPlateByColumn(icu1.locationOnContainerSupport.line, icu1.locationOnContainerSupport.column) < getNumberPositionInPlateByColumn(icu2.locationOnContainerSupport.line, icu2.locationOnContainerSupport.column);
	}

	public static String getContentPropertyIfOne(InputContainerUsed container, String propertyName) {
		List<String> l = container.contents.stream().map((Content c) -> c.properties.get(propertyName).value.toString())
				.collect(Collectors.toList());
		
		if(l.size() == 1 ){
			return l.get(0);
		}
		return null;
		
	}
	
	public static String getProjectCodeIfOne(InputContainerUsed container) {
		List<String> l = container.contents.stream().map((Content c) -> c.projectCode)
				.collect(Collectors.toList());
		
		if(l.size() == 1 ){
			return l.get(0);
		}
		return null;
		
	}
	
	public static String getSampleCodeIfOne(InputContainerUsed container) {
		List<String> l = container.contents.stream().map((Content c) -> c.sampleCode)
				.collect(Collectors.toList());
		
		if(l.size() == 1 ){
			return l.get(0);
		}
		return null;
		
	}
	
	/**
	 * 
	 * @param processCode - Mongo process code 
	 * @param propertyName - process property name to find
	 * @return - property value string
	 */
	//get property string from process
	public static String getProcessProperty(String processCode, String propertyName) {
		Process process = MongoDBDAO.findByCode(InstanceConstants.PROCESS_COLL_NAME, Process.class, processCode);
		
		if(process != null){
			
			return (String) process.properties.get(propertyName).value;
		}else {
			return null;
		}
	}
}
