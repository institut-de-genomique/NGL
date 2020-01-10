package validation;

import static org.fest.assertions.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Test;

import models.laboratory.common.description.PropertyDefinition;
import models.laboratory.common.instance.PropertyValue;
import models.laboratory.common.instance.TBoolean;
import models.laboratory.common.instance.property.PropertyFileValue;
import models.laboratory.common.instance.property.PropertyImgValue;
import models.laboratory.common.instance.property.PropertyListValue;
import models.laboratory.common.instance.property.PropertyObjectListValue;
import models.laboratory.common.instance.property.PropertyObjectValue;
import models.laboratory.common.instance.property.PropertySingleValue;
import play.data.validation.ValidationError;
import utils.AbstractTests;
import utils.Constants;
import validation.utils.ValidationHelper;

public class ValidationHelperTests extends AbstractTests {
	
	private PropertyDefinition getPropertyFileDefinition() {
		PropertyDefinition pDef = new PropertyDefinition();
		pDef.code = "krona";
		pDef.name = "krona";		
		pDef.active = true;
		pDef.required = true;
		pDef.valueType = "File";
		pDef.propertyValueType = "file";
		return pDef;
	}

	private PropertyDefinition getPropertyImgDefinition() {
		PropertyDefinition pDef = new PropertyDefinition();
		pDef.code = "phylogeneticTree2";
		pDef.name = "phylogeneticTree2";		
		pDef.active = true;
		pDef.required = true;
		pDef.valueType = "File";
		pDef.propertyValueType = "img";
		//pDef.propertyType = "Img";
		return pDef;
	}

	@Test
	public void validatePropertiesFileOK() {
		PropertyFileValue pFileValue = new  PropertyFileValue();
		byte[] data = new byte[] { (byte)0xe0, 0x4f, (byte)0xd0,
			    0x20, (byte)0xea, 0x3a, 0x69, 0x10, (byte)0xa2, (byte)0xd8, 0x08, 0x00, 0x2b,
			    0x30, 0x30, (byte)0x9d };
		pFileValue.value = data;
		pFileValue.fullname = "krona.html";
		pFileValue.extension = "html";
		
		ContextValidation cv = new ContextValidation(Constants.TEST_USER);
		
		PropertyDefinition pDef = getPropertyFileDefinition();
				
		Map<String, PropertyDefinition> hm= new HashMap<>();
		hm.put("krona", pDef);
		
		cv.putObject("propertyDefinitions", hm.values());
		
		pFileValue.validate(cv);
		
		showErrors(cv);
		assertThat(cv.errors.size()).isEqualTo(0); 
		
	}
	
	@Test
	public void validatePropertiesFileErr1() {
		PropertyFileValue pFileValue = new  PropertyFileValue();
		byte[] data = new byte[] { (byte)0xe0, 0x4f, (byte)0xd0,
			    0x20, (byte)0xea, 0x3a, 0x69, 0x10, (byte)0xa2, (byte)0xd8, 0x08, 0x00, 0x2b,
			    0x30, 0x30, (byte)0x9d };
		pFileValue.value = data;
		pFileValue.fullname = "krona.html";
		pFileValue.extension = "";
		ContextValidation cv = new ContextValidation(Constants.TEST_USER); 

		PropertyDefinition pDef = getPropertyFileDefinition();
		
		Map<String, PropertyDefinition> hm= new HashMap<>();
		hm.put("krona", pDef);
		
		cv.putObject("propertyDefinitions", hm.values());
		
		pFileValue.validate(cv);
		
		showErrors(cv);
		assertThat(cv.errors.size()).isEqualTo(1); 
		assertThat(cv.errors.toString()).contains("extension"); 
		assertThat(cv.errors.toString()).contains("error.required");
	}
	
	@Test
	public void validatePropertiesFileErr2() {
		PropertyFileValue pFileValue = new  PropertyFileValue();
		byte[] data = new byte[] { (byte)0xe0, 0x4f, (byte)0xd0,
			    0x20, (byte)0xea, 0x3a, 0x69, 0x10, (byte)0xa2, (byte)0xd8, 0x08, 0x00, 0x2b,
			    0x30, 0x30, (byte)0x9d };
		pFileValue.value = data;
		pFileValue.fullname = "";
		pFileValue.extension = "";
		ContextValidation cv = new ContextValidation(Constants.TEST_USER); 

		PropertyDefinition pDef = getPropertyFileDefinition();
		
		Map<String, PropertyDefinition> hm= new HashMap<>();
		hm.put("krona", pDef);
		
		cv.putObject("propertyDefinitions", hm.values());
		
		pFileValue.validate(cv);
		
		showErrors(cv);
		assertThat(cv.errors.size()).isEqualTo(2); 
		assertThat(cv.errors.toString()).contains("extension");
		assertThat(cv.errors.toString()).contains("fullname");
		assertThat(cv.errors.toString()).contains("error.required");
	}
	
	@Test
	public void validatePropertiesFileImgOK() {
		PropertyImgValue pImgValue = new  PropertyImgValue();
		byte[] data = new byte[] { (byte)0xe0, 0x4f, (byte)0xd0,
			    0x20, (byte)0xea, 0x3a, 0x69, 0x10, (byte)0xa2, (byte)0xd8, 0x08, 0x00, 0x2b,
			    0x30, 0x30, (byte)0x9d };
		pImgValue.value = data;
		pImgValue.fullname = "phylogeneticTree2.jpg";
		pImgValue.extension = "jpg";
		pImgValue.width = 4;
		pImgValue.height = 4;
		
		ContextValidation cv = new ContextValidation(Constants.TEST_USER); 

		PropertyDefinition pDef = getPropertyImgDefinition();
		
		Map<String, PropertyDefinition> hm= new HashMap<>();
		hm.put("phylogeneticTree2", pDef);
		
		cv.putObject("propertyDefinitions", hm.values());
		
		pImgValue.validate(cv);
		
		showErrors(cv);
		assertThat(cv.errors.size()).isEqualTo(0); 	
	}
	
	@Test
	public void validatePropertiesFileImgErr() {
		PropertyImgValue pImgValue = new  PropertyImgValue();
		byte[] data = new byte[] { (byte)0xe0, 0x4f, (byte)0xd0,
			    0x20, (byte)0xea, 0x3a, 0x69, 0x10, (byte)0xa2, (byte)0xd8, 0x08, 0x00, 0x2b,
			    0x30, 0x30, (byte)0x9d };
		pImgValue.value = data;
		pImgValue.fullname = "phylogeneticTree2.jpg";
		pImgValue.extension = "jpg";
		
		ContextValidation cv = new ContextValidation(Constants.TEST_USER); 

		PropertyDefinition pDef = getPropertyImgDefinition();
		
		Map<String, PropertyDefinition> hm= new HashMap<>();
		hm.put("phylogeneticTree2", pDef);
		
		cv.putObject("propertyDefinitions", hm.values());
		
		pImgValue.validate(cv);
		
		showErrors(cv);
		assertThat(cv.errors.size()).isEqualTo(2);
		assertThat(cv.errors.toString()).contains("width");
		assertThat(cv.errors.toString()).contains("height");
		assertThat(cv.errors.toString()).contains("error.required");
	}
	
	@Test
	public void validatePropertiesRequired() {
		ContextValidation cv = new ContextValidation(Constants.TEST_USER);
		ValidationHelper.validateProperties(cv, getPropertiesRequired(), getPropertyDefinitionsRequired());
		showErrors(cv);
		assertThat(cv.errors.size()).isEqualTo(getPropertyDefinitionsRequired().size()+2);
	}


//	private void showErrors(ContextValidation cv) {
//		if (cv.errors.size() > 0) {
//			for(Entry<String, List<ValidationError>> e : cv.errors.entrySet()){
//				System.out.println(e);
//			}
//		}
//	}
	private void showErrors(ContextValidation cv) {
		for (Entry<String, List<ValidationError>> e : cv.errors.entrySet())
			System.out.println(e);
	}
		
	private Map<String, PropertyValue> getPropertiesRequired() {
		Map<String, PropertyValue> m = new HashMap<>(); // String, PropertyValue>();
		
		PropertySingleValue propSingle = new PropertySingleValue();
		m.put("single1-1", propSingle);
		m.put("single1-2", propSingle);
		m.put("single1-3", null);
		PropertyListValue propList = new PropertyListValue();
		m.put("list2-1", propList);
		propList.value = new ArrayList<>();
		m.put("list2-2",propList);
		propList.value = Arrays.asList(new String[]{ "1", null, "2" });
		m.put("list2-3", propList);
		
//		Map<String, String> mapObject = new HashMap<String, String>();
		Map<String, Object> mapObject = new HashMap<>();
		mapObject.put("1", "1");
		mapObject.put("2", null);
		mapObject.put("3", "");
		PropertyObjectValue propObject = new PropertyObjectValue(mapObject);
		m.put("object4", propObject);
		
//		List<Map<String,?>> l = new ArrayList<>();
		List<Map<String,Object>> l = new ArrayList<>();
		l.add(mapObject);
		l.add(mapObject);
		l.add(mapObject);
		PropertyObjectListValue propObjectList =  new PropertyObjectListValue(l);
		m.put("listObject5",propObjectList);
		
		return m;
	}
	
	private List<PropertyDefinition> getPropertyDefinitionsRequired(){
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		propertyDefinitions.add(newPropertiesDefinition("required1-1", "single1-1", PropertySingleValue.class, String.class, true, "single"));
		propertyDefinitions.add(newPropertiesDefinition("required1-2", "single1-2", PropertySingleValue.class, String.class, true, "single"));
		propertyDefinitions.add(newPropertiesDefinition("required1-3", "single1-3", PropertySingleValue.class, String.class, true, "single"));
		propertyDefinitions.add(newPropertiesDefinition("required1-4", "single1-4", PropertySingleValue.class, String.class, true, "single"));
		
		propertyDefinitions.add(newPropertiesDefinition("required2-1", "list2-1", PropertyListValue.class, String.class, true, "list"));
		propertyDefinitions.add(newPropertiesDefinition("required2-2", "list2-2", PropertyListValue.class, String.class, true, "list"));
		propertyDefinitions.add(newPropertiesDefinition("required2-3", "list2-3", PropertyListValue.class, String.class, true, "list"));
		
		propertyDefinitions.add(newPropertiesDefinition("required3-1", "map3-1", PropertyObjectValue.class, String.class, true, "object"));
		propertyDefinitions.add(newPropertiesDefinition("required3-2", "map3-2", PropertyObjectValue.class, String.class, true, "object"));
		propertyDefinitions.add(newPropertiesDefinition("required3-3", "map3-3", PropertyObjectValue.class, String.class, true, "object"));
		
		propertyDefinitions.add(newPropertiesDefinition("required4.1", "object4.1", PropertyObjectValue.class, String.class, true, "object"));
		propertyDefinitions.add(newPropertiesDefinition("required4.2", "object4.2", PropertyObjectValue.class, String.class, true, "object"));
		propertyDefinitions.add(newPropertiesDefinition("required4.3", "object4.3", PropertyObjectValue.class, String.class, true, "object"));
		
		propertyDefinitions.add(newPropertiesDefinition("required5.1", "listObject5.1", PropertyListValue.class, String.class, true, "list"));
		propertyDefinitions.add(newPropertiesDefinition("required5.2", "listObject5.2", PropertyListValue.class, String.class, true, "list"));
		propertyDefinitions.add(newPropertiesDefinition("required5.3", "listObject5.3", PropertyListValue.class, String.class, true, "list"));
		
		return propertyDefinitions;
	}
	
	@Test
	public void validatePropertiesOne() {
		ContextValidation cv = new ContextValidation(Constants.TEST_USER);
		ValidationHelper.validateProperties(cv, getPropertiesSingle(), getPropertyDefinitionsSingle());
		showErrors(cv);
		assertThat(cv.errors.size()).isEqualTo(0);	
	}
	
	private Map<String, PropertyValue> getPropertiesSingle(){
		Map<String, PropertyValue> m = new HashMap<>(); // String, PropertyValue>();
		PropertySingleValue propText = new PropertySingleValue("test");
		m.put("String", propText);
		PropertySingleValue propInt = new PropertySingleValue(33);
		m.put("Integer", propInt);
		PropertySingleValue propDouble = new PropertySingleValue(36985214456467789654D);
		m.put("Double", propDouble);
		PropertySingleValue propBoolean = new PropertySingleValue(Boolean.TRUE);
		m.put("Boolean", propBoolean);
		PropertySingleValue propLong = new PropertySingleValue(36985214456467654L);
		m.put("Long", propLong);
		PropertySingleValue propDate = new PropertySingleValue(new Date());
		m.put("Date", propDate);
		PropertySingleValue propTBoolean = new PropertySingleValue(TBoolean.UNSET);
		m.put("TBoolean", propTBoolean);
		return m;
	}
	
	@Test
	public void validatePropertiesSingleString() {
		ContextValidation cv = new ContextValidation(Constants.TEST_USER);
		ValidationHelper.validateProperties(cv, getPropertiesSingleString(), getPropertyDefinitionsSingle());
		showErrors(cv);
		assertThat(cv.errors.size()).isEqualTo(0);
	}
	
	private Map<String, PropertyValue> getPropertiesSingleString(){
		Map<String, PropertyValue> m = new HashMap<>(); // String, PropertyValue>();
		PropertySingleValue propString = new PropertySingleValue("test");
		m.put("String", propString);
		PropertySingleValue propInt = new PropertySingleValue(33+"");
		m.put("Integer", propInt);
		PropertySingleValue propDouble = new PropertySingleValue(36985214456467789654D+"");
		m.put("Double", propDouble);
		PropertySingleValue propBoolean  = new PropertySingleValue(Boolean.TRUE.toString());
		m.put("Boolean", propBoolean);
		PropertySingleValue propLong = new PropertySingleValue(36985214456467654L+"");
		m.put("Long", propLong);
		PropertySingleValue propDate = new PropertySingleValue(new Date().getTime()+"");
		m.put("Date", propDate);
		PropertySingleValue propTBoolean = new PropertySingleValue("UNSET");
		m.put("TBoolean", propTBoolean);
		return m;
	}
	
	private List<PropertyDefinition> getPropertyDefinitionsSingle(){
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		propertyDefinitions.add(newPropertiesDefinition("String", "String", PropertySingleValue.class, String.class, true, "single"));
		propertyDefinitions.add(newPropertiesDefinition("Integer", "Integer", PropertySingleValue.class, Integer.class, true, "single"));
		propertyDefinitions.add(newPropertiesDefinition("Double", "Double", PropertySingleValue.class, Double.class, true, "single"));
		propertyDefinitions.add(newPropertiesDefinition("Boolean", "Boolean", PropertySingleValue.class, Boolean.class, true, "single"));
		propertyDefinitions.add(newPropertiesDefinition("Long", "Long", PropertySingleValue.class, Long.class, true, "single"));
		propertyDefinitions.add(newPropertiesDefinition("Date", "Date", PropertySingleValue.class, Date.class, true, "single"));
		propertyDefinitions.add(newPropertiesDefinition("TBoolean", "TBoolean", PropertySingleValue.class, TBoolean.class, true, "single"));
		return propertyDefinitions;
	}
	
	private PropertyDefinition newPropertiesDefinition(String name, String code, Class<?> propertyType, Class<?> valueType, Boolean required, String type) {
		PropertyDefinition pd = new PropertyDefinition();		
		pd.name = name;
		pd.code = code;
		pd.active = true;
		pd.propertyValueType = type;
		pd.valueType = valueType.getName();
		pd.required = required;
		pd.choiceInList = false;		
		return pd;
	}
	
	@Test
	public void validatePropertiesListString() {
		ContextValidation cv = new ContextValidation(Constants.TEST_USER);
		ValidationHelper.validateProperties(cv, getPropertiesListString(), getPropertyDefinitionsList());
		showErrors(cv);
		assertThat(cv.errors.size()).isEqualTo(0);
	}
	
	private Map<String, PropertyValue> getPropertiesListString(){
		Map<String, PropertyValue> m = new HashMap<>(); // String, PropertyValue>();
		PropertyListValue propListString = new PropertyListValue(Arrays.asList("test", "test2", "tes3"));
		m.put("String", propListString);
		PropertyListValue propListInt = new PropertyListValue(Arrays.asList("33","36","65"));
		m.put("Integer", propListInt);
		PropertyListValue propListDouble = new PropertyListValue(Arrays.asList("36985214456467789654"));
		m.put("Double", propListDouble);
		PropertyListValue propListBoolean = new PropertyListValue(Arrays.asList(Boolean.TRUE.toString()));
		m.put("Boolean", propListBoolean);
		PropertyListValue propListLong = new PropertyListValue(Arrays.asList(36985214456467654L+""));
		m.put("Long", propListLong);
		PropertyListValue propListDate = new PropertyListValue(Arrays.asList(new Date().getTime()+""));
		m.put("Date", propListDate);
		PropertyListValue propListTBoolean = new PropertyListValue(Arrays.asList("UNSET"));
		m.put("TBoolean", propListTBoolean);
		return m;
	}
	
	private List<PropertyDefinition> getPropertyDefinitionsList(){
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		propertyDefinitions.add(newPropertiesDefinition("String",   "String",   PropertyListValue.class, String.class,   true, "list"));
		propertyDefinitions.add(newPropertiesDefinition("Integer",  "Integer",  PropertyListValue.class, Integer.class,  true, "list"));
		propertyDefinitions.add(newPropertiesDefinition("Double",   "Double",   PropertyListValue.class, Double.class,   true, "list"));
		propertyDefinitions.add(newPropertiesDefinition("Boolean",  "Boolean",  PropertyListValue.class, Boolean.class,  true, "list"));
		propertyDefinitions.add(newPropertiesDefinition("Long",     "Long",     PropertyListValue.class, Long.class,     true, "list"));
		propertyDefinitions.add(newPropertiesDefinition("Date",     "Date",     PropertyListValue.class, Date.class,     true, "list"));
		propertyDefinitions.add(newPropertiesDefinition("TBoolean", "TBoolean", PropertyListValue.class, TBoolean.class, true, "list"));
		return propertyDefinitions;
	}
	
//	private Map<String, String> getMap(String...strings) {
//		Map<String, String> m = new HashMap<String, String>();
//		int i = 0;
//		for(String str: strings){
//			m.put(i+++"", str);
//		}		
//		return m;
//	}
	private Map<String, Object> getMap(String...strings) {
		Map<String, Object> m = new HashMap<>();
		int i = 0;
		for (String str : strings)
			m.put(i++ + "", str);
		return m;
	}

//	private List<PropertyDefinition> getPropertyDefinitionsMap(){
//		List<PropertyDefinition> propertyDefinitions = new ArrayList<PropertyDefinition>();
//		propertyDefinitions.add(newPropertiesDefinition("String", "String", PropertyObjectValue.class, String.class, true, "object"));
//		propertyDefinitions.add(newPropertiesDefinition("Integer", "Integer", PropertyObjectValue.class, Integer.class, true, "object"));
//		propertyDefinitions.add(newPropertiesDefinition("Double", "Double", PropertyObjectValue.class, Double.class, true, "object"));
//		propertyDefinitions.add(newPropertiesDefinition("Boolean", "Boolean", PropertyObjectValue.class, Boolean.class, true, "object"));
//		propertyDefinitions.add(newPropertiesDefinition("Long", "Long", PropertyObjectValue.class, Long.class, true, "object"));
//		propertyDefinitions.add(newPropertiesDefinition("Date", "Date", PropertyObjectValue.class, Date.class, true, "object"));
//		propertyDefinitions.add(newPropertiesDefinition("TBoolean", "TBoolean", PropertyObjectValue.class, TBoolean.class, true, "object"));
//		return propertyDefinitions;
//	}
	
	@Test
	public void validatePropertiesObjectOK() {
		ContextValidation cv = new ContextValidation(Constants.TEST_USER);
		ValidationHelper.validateProperties(cv, getPropertiesObjectString(), getPropertyDefinitionsObject());
		showErrors(cv);
		assertThat(cv.errors.size()).isEqualTo(0);		
	}
	
	private Map<String, PropertyValue> getPropertiesObjectString() {
		Map<String, PropertyValue> m = new HashMap<>(); // String, PropertyValue>();
		PropertyObjectValue propObject = new PropertyObjectValue(getMap("test", "45", "36985214456467789654"));
		m.put("Object", propObject);
		return m;
	}
	
	private List<PropertyDefinition> getPropertyDefinitionsObject() {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		propertyDefinitions.add(newPropertiesDefinition("Object.String",  "Object.0", PropertyObjectValue.class, String.class,  true, "object"));
		propertyDefinitions.add(newPropertiesDefinition("Object.Integer", "Object.1", PropertyObjectValue.class, Integer.class, true, "object"));
		propertyDefinitions.add(newPropertiesDefinition("Object.Double",  "Object.2", PropertyObjectValue.class, Double.class,  true, "object"));		
		return propertyDefinitions;
	}

}
