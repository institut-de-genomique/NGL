package controllers.instruments.io.utils;

import static fr.cea.ig.play.IGGlobals.configuration;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.mongojack.DBQuery;

import fr.cea.ig.MongoDBDAO;
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

public class OutputHelper {

	private static final play.Logger.ALogger logger = play.Logger.of(OutputHelper.class); 
	
	public static String getInstrumentPath(String instrumentCode, boolean addSampleSheet) {
		Instrument instrument = null;
		try {
			instrument = Instrument.find.get().findByCode(instrumentCode);
		} catch (DAOException e) {
			logger.error("DAO error: " + e.getMessage(),e);
		}
		if (instrument != null) {
			if (configuration().hasPath("ngl.path.instrument")) {
				return configuration().getString("ngl.path.instrument")+java.io.File.separator;
			} else if(addSampleSheet) {
				return instrument.path + java.io.File.separator + "SampleSheet" + java.io.File.separator;
			} else {
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
			writer = new OutputStreamWriter(fos, StandardCharsets.UTF_8); // Codec.UTF8().name());			
			writer.write(file.content);
			writer.append("\r\n");
			writer.close();
			fos.close();
		} catch (Exception e) {
			logger.error("Problem to create sample sheet",e);
			logger.error("DAO error: "+e.getMessage(),e);
		}
		
	}
	
	// FDS 07/02/2018 il faut dans certains cas des lignes vides dans le fichier de sortie 
	// => rajout d'un traitement spécifique pour "#" seul sur une ligne
	public static String format(String content){
		if (content != null) {
			return content.trim().replaceAll("(?m)^\\s{1,}", "").replaceAll("\n{2,}", "\n").replaceAll("(?m)^#$","");
		}
		return "";
	}
	
	public static List<Container> getInputContainersFromExperiment(Experiment experiment){
		List<Container> containers = new ArrayList<>();
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
	
	private static String getIndex(String sequence, Integer maxIndexSize) {
		if(null == sequence){
			return StringUtils.repeat("N", maxIndexSize);
		}else if(sequence.length() < maxIndexSize){
			return sequence.concat(StringUtils.repeat("N", maxIndexSize-sequence.length()));
		}else{
			return sequence;
		}
	}
	
	public static String getSequence(Index index, TagModel tagModel, String instrumentTypeCode) {
		return 	getSequence(index, tagModel, instrumentTypeCode, null);
	}
	
	public static String getSequence(Index index, TagModel tagModel, String instrumentTypeCode, Integer position){
		if("NONE".equals(tagModel.tagType)){
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
			throw new RuntimeException("Index not managed "+tagModel.tagType);
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
		if(content.properties.get(propertyName) != null){
			return (String) content.properties.get(propertyName).value;
		}
		return "";
	}
	
	public static Double getContentDoubleProperty(Content content, String propertyName){
		if(content.properties.get(propertyName) != null){
			return  (Double) content.properties.get(propertyName).value;
		}
		return 0.0;
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
		List<PropertyValue> tags = containers.stream()
			.map(container -> container.contents)
			.flatMap(List::stream)
			.filter(c -> c.properties.containsKey(InstanceConstants.TAG_PROPERTY_NAME))
//			.filter(c -> !c.properties.get("tagCategory").equals("MID"))
			.filter(c -> ! "MID".equals(c.properties.get("tagCategory").value))
			.map((Content c) -> c.properties.get(InstanceConstants.TAG_PROPERTY_NAME))
			.collect(Collectors.toList());
		TagModel tagModel = new TagModel();
		if (tags.size() > 0) {
			tagModel.maxTag1Size = 0;
			tagModel.maxTag2Size = 0;
			tagModel.tagType     = "SINGLE-INDEX";
			for (PropertyValue _tag : tags) {
				PropertySingleValue tag = (PropertySingleValue)_tag;
				Index index = getIndex("index-illumina-sequencing", tag.value.toString());
				
				if ("SINGLE-INDEX".equals(index.categoryCode)) {
					if(index.sequence.length() > tagModel.maxTag1Size){
						tagModel.maxTag1Size = index.sequence.length();
					}
				} else if("DUAL-INDEX".equals(index.categoryCode)) {
					tagModel.tagType = "DUAL-INDEX";
					
					String[] sequences = index.sequence.split("-",2);
					if(sequences[0].length() > tagModel.maxTag1Size){
						tagModel.maxTag1Size = sequences[0].length();
					}
					
					if(sequences[1].length() > tagModel.maxTag2Size){
						tagModel.maxTag2Size = sequences[1].length();
					}
				}						
			}
		} else {
			tagModel.tagType = "NONE";
		}	
		return tagModel;
	}
	
	// Cet algorithme est utile pour les robots qui numerotent les plaques 96 en colonne 
	// A1=1, B1=2...A2=9
	public static int getNumberPositionInPlateByColumn(String line, String column) {
		int asciiValue  = line.toCharArray()[0];
		int columnValue = Integer.parseInt(column);
		return (asciiValue - 64) + (columnValue - 1) * 8;
	}
	
	// Cet algorithme est utile pour les robots qui numerotent les plaques 96 en ligne 
		// A1=1, A2=2
	public static int getNumberPositionInPlateByLine(String line, String column) {
		int asciiValue  = line.toCharArray()[0];
		int columnValue = Integer.parseInt(column);
		return (asciiValue-64) * 12 - 12 + columnValue;
	}
	
//	// Cet algorithme est utile pour les robots qui numerotent les plaques 96 en colonne 
//	// 1=A1, 2=B1...9=A2
//	// FDS a tester !!
//	public static String getLineColumnInPlateBycolumn(int position){
//		String line   = Integer.toString((position -1) % 8) +'A';
//		String column = Integer.toString((position -1) / 8);
//		
//		return line + column;
//	}
	
	public static String getTag(InputContainerUsed container) {
		return container.contents.stream().map((Content c) -> (String) c.properties.get(InstanceConstants.TAG_PROPERTY_NAME).value)
				.collect(Collectors.toList()).get(0);
	}

	public static boolean sortBylocationSupportOneToOne(AtomicTransfertMethod atm1, AtomicTransfertMethod atm2){
		InputContainerUsed icu1 = atm1.inputContainerUseds.get(0);
		InputContainerUsed icu2 = atm2.inputContainerUseds.get(0);
		
		return getNumberPositionInPlateByColumn(icu1.locationOnContainerSupport.line, icu1.locationOnContainerSupport.column) < getNumberPositionInPlateByColumn(icu2.locationOnContainerSupport.line, icu2.locationOnContainerSupport.column);
	}

	public static String getContentPropertyIfOne(InputContainerUsed container, String propertyName) {
		List<String> l = container.contents.stream().filter((Content c) -> c.properties.containsKey(propertyName)).map((Content c) -> c.properties.get(propertyName).value.toString())
				.collect(Collectors.toList());
		if (l.size() == 1 ) {
			return l.get(0);
		}
		return null;
	}
	
	public static String getProjectCodeIfOne(InputContainerUsed container) {
		List<String> l = container.contents.stream().map((Content c) -> c.projectCode)
				.collect(Collectors.toList());
		if (l.size() == 1 ) { 
			return l.get(0);
		}
		return null;
	}
	
	public static String getSampleCodeIfOne(InputContainerUsed container) {
		List<String> l = container.contents.stream().map((Content c) -> c.sampleCode)
				.collect(Collectors.toList());
		if (l.size() == 1 ) {
			return l.get(0);
		}
		return null;
	}
	
}
