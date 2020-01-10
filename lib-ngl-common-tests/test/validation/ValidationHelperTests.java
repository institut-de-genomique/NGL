package validation;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Test;

import fr.cea.ig.lfw.utils.Iterables;
import fr.cea.ig.util.function.CC1;
import fr.cea.ig.util.function.CCActions;
import models.laboratory.common.description.PropertyDefinition;
import models.laboratory.common.instance.PropertyValue;
import models.laboratory.common.instance.TBoolean;
import models.laboratory.common.instance.property.PropertyFileValue;
import models.laboratory.common.instance.property.PropertyImgValue;
import models.laboratory.common.instance.property.PropertyListValue;
import models.laboratory.common.instance.property.PropertyObjectListValue;
import models.laboratory.common.instance.property.PropertyObjectValue;
import models.laboratory.common.instance.property.PropertySingleValue;
import ngl.common.Global;
import play.data.validation.ValidationError;
import utils.Constants;
import validation.utils.ValidationHelper;

public class ValidationHelperTests {

	public static final play.Logger.ALogger logger = play.Logger.of(ValidationHelperTests.class);
	
	private static final CC1<ContextValidation> af =
			Global.afSq.cc1()
			.and(CCActions.f0asCC1(() -> ContextValidation.createUndefinedContext(Constants.TEST_USER)))
			.cc1((app,ctx) -> ctx);

	private PropertyDefinition getPropertyFileDefinition() {
		PropertyDefinition pDef = new PropertyDefinition();
		pDef.code = "krona";
//		pDef.name = "krona";		
		pDef.setName("krona");		
		pDef.active = true;
		pDef.required = true;
		pDef.valueType = "File";
		pDef.propertyValueType = "file";
		return pDef;
	}

	private PropertyDefinition getPropertyImgDefinition() {
		PropertyDefinition pDef = new PropertyDefinition();
		pDef.code = "phylogeneticTree2";
//		pDef.name = "phylogeneticTree2";		
		pDef.setName("phylogeneticTree2");		
		pDef.active = true;
		pDef.required = true;
		pDef.valueType = "File";
		pDef.propertyValueType = "img";
		//pDef.propertyType = "Img";
		return pDef;
	}

	@Test
	public void validatePropertiesFileOK() throws Exception {
		af.accept(cv -> {
			PropertyFileValue pFileValue = new  PropertyFileValue();
			byte[] data = new byte[] { (byte)0xe0, 0x4f, (byte)0xd0,
					0x20, (byte)0xea, 0x3a, 0x69, 0x10, (byte)0xa2, (byte)0xd8, 0x08, 0x00, 0x2b,
					0x30, 0x30, (byte)0x9d };
			pFileValue.value = data;
			pFileValue.fullname = "krona.html";
			pFileValue.extension = "html";

			PropertyDefinition pDef = getPropertyFileDefinition();

			Map<String, PropertyDefinition> hm = new HashMap<>();
			hm.put("krona", pDef);

//			cv.putObject("propertyDefinitions", hm.values());
//			pFileValue.validate(cv);
			pFileValue.validate(cv, hm.values());

			showErrors(cv);
			assertThat(cv.getErrors().size()).isEqualTo(0); 
		});
	}
	
	@Test
	public void validatePropertiesFileErr1() throws Exception {
		af.accept(cv -> {
		PropertyFileValue pFileValue = new  PropertyFileValue();
		byte[] data = new byte[] { (byte)0xe0, 0x4f, (byte)0xd0,
			    0x20, (byte)0xea, 0x3a, 0x69, 0x10, (byte)0xa2, (byte)0xd8, 0x08, 0x00, 0x2b,
			    0x30, 0x30, (byte)0x9d };
		pFileValue.value = data;
		pFileValue.fullname = "krona.html";
		pFileValue.extension = "";

		PropertyDefinition pDef = getPropertyFileDefinition();
		
		Map<String, PropertyDefinition> hm = new HashMap<>();
		hm.put("krona", pDef);
		
//		cv.putObject("propertyDefinitions", hm.values());
//		pFileValue.validate(cv);
		pFileValue.validate(cv, hm.values());
		
		showErrors(cv);
		assertThat(cv.getErrors().size()).isEqualTo(1); 
		assertThat(cv.getErrors().toString()).contains("extension"); 
		assertThat(cv.getErrors().toString()).contains("error.required");
		});
	}
	
	@Test
	public void validatePropertiesFileErr2() throws Exception {
		af.accept(cv -> {
			PropertyFileValue pFileValue = new  PropertyFileValue();
			byte[] data = new byte[] { (byte)0xe0, 0x4f, (byte)0xd0,
					0x20, (byte)0xea, 0x3a, 0x69, 0x10, (byte)0xa2, (byte)0xd8, 0x08, 0x00, 0x2b,
					0x30, 0x30, (byte)0x9d };
			pFileValue.value = data;
			pFileValue.fullname = "";
			pFileValue.extension = "";

			PropertyDefinition pDef = getPropertyFileDefinition();

			Map<String, PropertyDefinition> hm = new HashMap<>();
			hm.put("krona", pDef);

//			cv.putObject("propertyDefinitions", hm.values());
//			pFileValue.validate(cv);
			pFileValue.validate(cv, hm.values());

			showErrors(cv);
			assertThat(cv.getErrors().size()).isEqualTo(2); 
			assertThat(cv.getErrors().toString()).contains("extension");
			assertThat(cv.getErrors().toString()).contains("fullname");
			assertThat(cv.getErrors().toString()).contains("error.required");
		});
	}
	
	@Test
	public void validatePropertiesFileImgOK() throws Exception {
		af.accept(cv -> {
			PropertyImgValue pImgValue = new  PropertyImgValue();
			byte[] data = new byte[] { (byte)0xe0, 0x4f, (byte)0xd0,
					0x20, (byte)0xea, 0x3a, 0x69, 0x10, (byte)0xa2, (byte)0xd8, 0x08, 0x00, 0x2b,
					0x30, 0x30, (byte)0x9d };
			pImgValue.value = data;
			pImgValue.fullname = "phylogeneticTree2.jpg";
			pImgValue.extension = "jpg";
			pImgValue.width = 4;
			pImgValue.height = 4;

			PropertyDefinition pDef = getPropertyImgDefinition();

			Map<String, PropertyDefinition> hm = new HashMap<>();
			hm.put("phylogeneticTree2", pDef);

//			cv.putObject("propertyDefinitions", hm.values());
//			pImgValue.validate(cv);
			pImgValue.validate(cv, hm.values());

			showErrors(cv);
			assertThat(cv.getErrors().size()).isEqualTo(0); 	
		});
	}
	
	@Test
	public void validatePropertiesFileImgErr() throws Exception {
		af.accept(cv -> {
			PropertyImgValue pImgValue = new  PropertyImgValue();
			byte[] data = new byte[] { (byte)0xe0, 0x4f, (byte)0xd0,
					0x20, (byte)0xea, 0x3a, 0x69, 0x10, (byte)0xa2, (byte)0xd8, 0x08, 0x00, 0x2b,
					0x30, 0x30, (byte)0x9d };
			pImgValue.value = data;
			pImgValue.fullname = "phylogeneticTree2.jpg";
			pImgValue.extension = "jpg";

			PropertyDefinition pDef = getPropertyImgDefinition();

			Map<String, PropertyDefinition> hm = new HashMap<>();
			hm.put("phylogeneticTree2", pDef);

//			cv.putObject("propertyDefinitions", hm.values());
//			pImgValue.validate(cv);
			pImgValue.validate(cv, hm.values());

			showErrors(cv);
			assertThat(cv.getErrors().size()).isEqualTo(2);
			assertThat(cv.getErrors().toString()).contains("width");
			assertThat(cv.getErrors().toString()).contains("height");
			assertThat(cv.getErrors().toString()).contains("error.required");
		});
	}
	
	@Test
	public void validatePropertiesRequired() throws Exception {
		af.accept(cv -> {
			Map<String, PropertyValue> properties = getPropertiesRequired();
			List<PropertyDefinition> definitions = getPropertyDefinitionsRequired();
			ValidationHelper.validateProperties(cv, properties, definitions);
			showErrors(cv);
			logger.debug("definitions : {}, errors : {}", definitions.size(), cv.errorCount());
			assertThat(cv.getErrors().size()).isEqualTo(definitions.size()+2);
		});
	}

	private Map<String, PropertyValue> getPropertiesRequired() {
		Map<String, PropertyValue> m = new HashMap<>();
		
		// Generate 4 errors, 3 empty + 1 missing 
		PropertySingleValue propSingle = new PropertySingleValue();
		m.put("single1-1", propSingle);
		m.put("single1-2", propSingle);
		m.put("single1-3", null);
		
		// This generates 3 errors as the propList holds ("1",null,"2 ) in the 3 cases
		PropertyListValue propList = new PropertyListValue();
		propList.value = new ArrayList<>();
		m.put("list2-1", propList);
		m.put("list2-2", propList);
//		propList.value = Arrays.asList(new String[] { "1", null, "2" });
		propList.value = Arrays.asList("1", null, "2");
		m.put("list2-3", propList);
		
		// 3 errors for missing map3
		
		// 2 errors for empty values
		Map<String, Object> mapObject = new HashMap<>();
		mapObject.put("1", "1"); 
		mapObject.put("2", null);
		mapObject.put("3", "");
		PropertyObjectValue propObject = new PropertyObjectValue(mapObject);
		m.put("object4", propObject);
		
		// 
		List<Map<String,Object>> l = new ArrayList<>();
		l.add(mapObject);
		l.add(mapObject);
		l.add(mapObject);
		PropertyObjectListValue propObjectList =  new PropertyObjectListValue(l);
		m.put("listObject5", propObjectList); // no errors
		
		return m;
	}
	
	private List<PropertyDefinition> getPropertyDefinitionsRequired(){
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		
		propertyDefinitions.add(newPropertyDefinition("required1-1", "single1-1",     PropertySingleValue.class, String.class, true, "single"));
		propertyDefinitions.add(newPropertyDefinition("required1-2", "single1-2",     PropertySingleValue.class, String.class, true, "single"));
		propertyDefinitions.add(newPropertyDefinition("required1-3", "single1-3",     PropertySingleValue.class, String.class, true, "single"));
		propertyDefinitions.add(newPropertyDefinition("required1-4", "single1-4",     PropertySingleValue.class, String.class, true, "single"));
		
		propertyDefinitions.add(newPropertyDefinition("required2-1", "list2-1",       PropertyListValue.class,   String.class, true, "list"));
		propertyDefinitions.add(newPropertyDefinition("required2-2", "list2-2",       PropertyListValue.class,   String.class, true, "list"));
		propertyDefinitions.add(newPropertyDefinition("required2-3", "list2-3",       PropertyListValue.class,   String.class, true, "list"));
		
		propertyDefinitions.add(newPropertyDefinition("required3-1", "map3-1",        PropertyObjectValue.class, String.class, true, "object"));
		propertyDefinitions.add(newPropertyDefinition("required3-2", "map3-2",        PropertyObjectValue.class, String.class, true, "object"));
		propertyDefinitions.add(newPropertyDefinition("required3-3", "map3-3",        PropertyObjectValue.class, String.class, true, "object"));
		
		propertyDefinitions.add(newPropertyDefinition("required4.1", "object4.1",     PropertyObjectValue.class, String.class, true, "object"));
		propertyDefinitions.add(newPropertyDefinition("required4.2", "object4.2",     PropertyObjectValue.class, String.class, true, "object"));
		propertyDefinitions.add(newPropertyDefinition("required4.3", "object4.3",     PropertyObjectValue.class, String.class, true, "object"));
		
		propertyDefinitions.add(newPropertyDefinition("required5.1", "listObject5.1", PropertyObjectListValue.class, String.class, true, "object_list"));
		propertyDefinitions.add(newPropertyDefinition("required5.2", "listObject5.2", PropertyObjectListValue.class, String.class, true, "object_list"));
		propertyDefinitions.add(newPropertyDefinition("required5.3", "listObject5.3", PropertyObjectListValue.class, String.class, true, "object_list"));
		
		return propertyDefinitions;
	}
	
	@Test
	public void validatePropertiesOne() throws Exception {
		af.accept(cv -> {
			ValidationHelper.validateProperties(cv, getPropertiesSingle(), getPropertyDefinitionsSingle());
			showErrors(cv);
			assertThat(cv.getErrors().size()).isEqualTo(0);
		});
	}
	
//	private Map<String, PropertyValue> getPropertiesSingle() {
//		Map<String, PropertyValue> m = new HashMap<>();
//		PropertySingleValue propText = new PropertySingleValue("test");
//		m.put("String", propText);
//		PropertySingleValue propInt = new PropertySingleValue(33);
//		m.put("Integer", propInt);
//		PropertySingleValue propDouble = new PropertySingleValue(36985214456467789654D);
//		m.put("Double", propDouble);
//		PropertySingleValue propBoolean = new PropertySingleValue(Boolean.TRUE);
//		m.put("Boolean", propBoolean);
//		PropertySingleValue propLong = new PropertySingleValue(36985214456467654L);
//		m.put("Long", propLong);
//		PropertySingleValue propDate = new PropertySingleValue(new Date());
//		m.put("Date", propDate);
//		PropertySingleValue propTBoolean = new PropertySingleValue(TBoolean.UNSET);
//		m.put("TBoolean", propTBoolean);
//		return m;
//	}
	private Map<String, PropertyValue> getPropertiesSingle() {
		Map<String, PropertyValue> m = new HashMap<>();
		m.put("String",   new PropertySingleValue("test"));
		m.put("Integer",  new PropertySingleValue(33));
		m.put("Double",   new PropertySingleValue(36985214456467789654D));
		m.put("Boolean",  new PropertySingleValue(Boolean.TRUE));
		m.put("Long",     new PropertySingleValue(36985214456467654L));
		m.put("Date",     new PropertySingleValue(new Date()));
		m.put("TBoolean", new PropertySingleValue(TBoolean.UNSET));
		return m;
	}
	
	@Test
	public void validatePropertiesSingleString() throws Exception {
		af.accept(cv -> {
			ValidationHelper.validateProperties(cv, getPropertiesSingleString(), getPropertyDefinitionsSingle());
			showErrors(cv);
			assertThat(cv.getErrors().size()).isEqualTo(0);
		});
	}
	
//	private Map<String, PropertyValue> getPropertiesSingleString() {
//		Map<String, PropertyValue> m = new HashMap<>(); // String, PropertyValue>();
//		PropertySingleValue propString = new PropertySingleValue("test");
//		m.put("String", propString);
//		PropertySingleValue propInt = new PropertySingleValue(33+"");
//		m.put("Integer", propInt);
//		PropertySingleValue propDouble = new PropertySingleValue(36985214456467789654D+"");
//		m.put("Double", propDouble);
//		PropertySingleValue propBoolean  = new PropertySingleValue(Boolean.TRUE.toString());
//		m.put("Boolean", propBoolean);
//		PropertySingleValue propLong = new PropertySingleValue(36985214456467654L+"");
//		m.put("Long", propLong);
//		PropertySingleValue propDate = new PropertySingleValue(new Date().getTime()+"");
//		m.put("Date", propDate);
//		PropertySingleValue propTBoolean = new PropertySingleValue("UNSET");
//		m.put("TBoolean", propTBoolean);
//		return m;
//	}
		private Map<String, PropertyValue> getPropertiesSingleString() {
			Map<String, PropertyValue> m = new HashMap<>();
			for (Map.Entry<String, PropertyValue> e : getPropertiesSingle().entrySet())
				if (e.getValue().value instanceof Date)
					m.put(e.getKey(), new PropertySingleValue("" + ((Date)e.getValue().value).getTime()));
				else
					m.put(e.getKey(), new PropertySingleValue(e.getValue().value.toString()));
			return m;
		}
	
	private List<PropertyDefinition> getPropertyDefinitionsSingle(){
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		propertyDefinitions.add(newPropertyDefinition("String",   "String",   PropertySingleValue.class, String.class,   true, "single"));
		propertyDefinitions.add(newPropertyDefinition("Integer",  "Integer",  PropertySingleValue.class, Integer.class,  true, "single"));
		propertyDefinitions.add(newPropertyDefinition("Double",   "Double",   PropertySingleValue.class, Double.class,   true, "single"));
		propertyDefinitions.add(newPropertyDefinition("Boolean",  "Boolean",  PropertySingleValue.class, Boolean.class,  true, "single"));
		propertyDefinitions.add(newPropertyDefinition("Long",     "Long",     PropertySingleValue.class, Long.class,     true, "single"));
		propertyDefinitions.add(newPropertyDefinition("Date",     "Date",     PropertySingleValue.class, Date.class,     true, "single"));
		propertyDefinitions.add(newPropertyDefinition("TBoolean", "TBoolean", PropertySingleValue.class, TBoolean.class, true, "single"));
		return propertyDefinitions;
	}
	
	private PropertyDefinition newPropertyDefinition(String name, String code, Class<?> propertyType, Class<?> valueType, Boolean required, String type) {
		PropertyDefinition pd = new PropertyDefinition();		
		pd.setName(name);
		pd.code              = code;
		pd.active            = true;
		pd.propertyValueType = type;
		pd.valueType         = valueType.getName();
		pd.required          = required;
		pd.choiceInList      = false;		
		return pd;
	}
	
	@Test
	public void validatePropertiesListString() throws Exception {
		af.accept(cv -> {
			ValidationHelper.validateProperties(cv, getPropertiesListString(), getPropertyDefinitionsList());
			showErrors(cv);
			assertThat(cv.getErrors().size()).isEqualTo(0);
		});
	}
	
//	private Map<String, PropertyValue> getPropertiesListString() {
//		Map<String, PropertyValue> m = new HashMap<>(); // String, PropertyValue>();
//		PropertyListValue propListString = new PropertyListValue(Arrays.asList("test", "test2", "tes3"));
//		m.put("String", propListString);
//		PropertyListValue propListInt = new PropertyListValue(Arrays.asList("33","36","65"));
//		m.put("Integer", propListInt);
//		PropertyListValue propListDouble = new PropertyListValue(Arrays.asList("36985214456467789654"));
//		m.put("Double", propListDouble);
//		PropertyListValue propListBoolean = new PropertyListValue(Arrays.asList(Boolean.TRUE.toString()));
//		m.put("Boolean", propListBoolean);
//		PropertyListValue propListLong = new PropertyListValue(Arrays.asList(36985214456467654L+""));
//		m.put("Long", propListLong);
//		PropertyListValue propListDate = new PropertyListValue(Arrays.asList(new Date().getTime()+""));
//		m.put("Date", propListDate);
//		PropertyListValue propListTBoolean = new PropertyListValue(Arrays.asList("UNSET"));
//		m.put("TBoolean", propListTBoolean);
//		return m;
//	}
	private Map<String, PropertyValue> getPropertiesListString() {
		Map<String, PropertyValue> m = new HashMap<>(); // String, PropertyValue>();
		m.put("String",   new PropertyListValue(Arrays.asList("test", "test2", "tes3")));
		m.put("Integer",  new PropertyListValue(Arrays.asList("33","36","65")));
		m.put("Double",   new PropertyListValue(Arrays.asList("36985214456467789654")));
		m.put("Boolean",  new PropertyListValue(Arrays.asList(Boolean.TRUE.toString())));
		m.put("Long",     new PropertyListValue(Arrays.asList(36985214456467654L+"")));
		m.put("Date",     new PropertyListValue(Arrays.asList(new Date().getTime()+"")));
		m.put("TBoolean", new PropertyListValue(Arrays.asList("UNSET")));
		return m;
	}
	
	private List<PropertyDefinition> getPropertyDefinitionsList(){
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		propertyDefinitions.add(newPropertyDefinition("String",   "String",   PropertyListValue.class, String  .class, true, "list"));
		propertyDefinitions.add(newPropertyDefinition("Integer",  "Integer",  PropertyListValue.class, Integer .class, true, "list"));
		propertyDefinitions.add(newPropertyDefinition("Double",   "Double",   PropertyListValue.class, Double  .class, true, "list"));
		propertyDefinitions.add(newPropertyDefinition("Boolean",  "Boolean",  PropertyListValue.class, Boolean .class, true, "list"));
		propertyDefinitions.add(newPropertyDefinition("Long",     "Long",     PropertyListValue.class, Long    .class, true, "list"));
		propertyDefinitions.add(newPropertyDefinition("Date",     "Date",     PropertyListValue.class, Date    .class, true, "list"));
		propertyDefinitions.add(newPropertyDefinition("TBoolean", "TBoolean", PropertyListValue.class, TBoolean.class, true, "list"));
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
	private Map<String, Object> getMap(String... strings) {
		Map<String, Object> m = new HashMap<>();
		int i = 0;
		for (String str : strings)
			m.put(i++ + "", str);
		return m;
	}
	
	public Map<String, Object> getMap_(String... strings) {
		return Iterables
				.zenThem(strings)
				.zip(Iterables.range(0))
				.toMap(p -> p.right.toString(), p -> p.left);
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
	public void validatePropertiesObjectOK() throws Exception {
		af.accept(cv -> {
			ValidationHelper.validateProperties(cv, getPropertiesObjectString(), getPropertyDefinitionsObject());
			showErrors(cv);
			assertThat(cv.getErrors().size()).isEqualTo(0);
		});
	}
	
	private Map<String, PropertyValue> getPropertiesObjectString() {
		Map<String, PropertyValue> m = new HashMap<>(); // String, PropertyValue>();
		PropertyObjectValue propObject = new PropertyObjectValue(getMap("test", "45", "36985214456467789654"));
		m.put("Object", propObject);
		return m;
	}
	
	private List<PropertyDefinition> getPropertyDefinitionsObject() {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		propertyDefinitions.add(newPropertyDefinition("Object.String",  "Object.0", PropertyObjectValue.class, String.class,  true, "object"));
		propertyDefinitions.add(newPropertyDefinition("Object.Integer", "Object.1", PropertyObjectValue.class, Integer.class, true, "object"));
		propertyDefinitions.add(newPropertyDefinition("Object.Double",  "Object.2", PropertyObjectValue.class, Double.class,  true, "object"));		
		return propertyDefinitions;
	}

	// --------------------------------------------------------------
	// Test that dynamicCast conforms to convertStringToType
	
	@SuppressWarnings("deprecation")
	private static void convertStringToTypeAndDynamicCast(String typeName, String value) {
		Object stValue = ValidationHelper.convertStringToType(typeName, value);
		Object dcValue = ValidationHelper.dynamicCast(typeName, value);
		assertSame(stValue.getClass(), dcValue.getClass());
		assertEquals(stValue, dcValue);
	}
	
	private static void stdc(Class<?> type, String value) {
		convertStringToTypeAndDynamicCast(type.getName(), value);
	}
	
	@Test
	public void testDynamicCast() {
		stdc(Integer.class,  "10");
		stdc(Integer.class, "-10");
		stdc(Integer.class, "101");
		stdc(Long.class,     "10");
		stdc(Long.class,    "-10");
		stdc(Long.class,    "101");
		stdc(Float.class,    "10.4");
		stdc(Float.class,   "-10.4");
		stdc(Float.class,   "101.4");
		stdc(Double.class,   "10.44");
		stdc(Double.class,  "-10.44");
		stdc(Double.class,  "101.44");
		stdc(String.class,  "Hello, world !");
	}
	
	private void showErrors(ContextValidation cv) {
		for (Entry<String, List<ValidationError>> e : cv.getErrors().entrySet())
			System.out.println(e);
	}
		
}

//package validation;
//
//import static org.fest.assertions.Assertions.assertThat;
//
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Date;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.Map.Entry;
//
//import org.junit.Test;
//
//import models.laboratory.common.description.PropertyDefinition;
//import models.laboratory.common.instance.PropertyValue;
//import models.laboratory.common.instance.TBoolean;
//import models.laboratory.common.instance.property.PropertyFileValue;
//import models.laboratory.common.instance.property.PropertyImgValue;
//import models.laboratory.common.instance.property.PropertyListValue;
//import models.laboratory.common.instance.property.PropertyObjectListValue;
//import models.laboratory.common.instance.property.PropertyObjectValue;
//import models.laboratory.common.instance.property.PropertySingleValue;
//import play.data.validation.ValidationError;
//import utils.AbstractTests;
//import utils.Constants;
//import validation.utils.ValidationHelper;
//
//public class ValidationHelperTests extends AbstractTests {
//	
//	private PropertyDefinition getPropertyFileDefinition() {
//		PropertyDefinition pDef = new PropertyDefinition();
//		pDef.code = "krona";
////		pDef.name = "krona";		
//		pDef.setName("krona");		
//		pDef.active = true;
//		pDef.required = true;
//		pDef.valueType = "File";
//		pDef.propertyValueType = "file";
//		return pDef;
//	}
//
//	private PropertyDefinition getPropertyImgDefinition() {
//		PropertyDefinition pDef = new PropertyDefinition();
//		pDef.code = "phylogeneticTree2";
////		pDef.name = "phylogeneticTree2";		
//		pDef.setName("phylogeneticTree2");		
//		pDef.active = true;
//		pDef.required = true;
//		pDef.valueType = "File";
//		pDef.propertyValueType = "img";
//		//pDef.propertyType = "Img";
//		return pDef;
//	}
//
//	@Test
//	public void validatePropertiesFileOK() {
//		PropertyFileValue pFileValue = new  PropertyFileValue();
//		byte[] data = new byte[] { (byte)0xe0, 0x4f, (byte)0xd0,
//			    0x20, (byte)0xea, 0x3a, 0x69, 0x10, (byte)0xa2, (byte)0xd8, 0x08, 0x00, 0x2b,
//			    0x30, 0x30, (byte)0x9d };
//		pFileValue.value = data;
//		pFileValue.fullname = "krona.html";
//		pFileValue.extension = "html";
//		
//		ContextValidation cv = new ContextValidation(Constants.TEST_USER);
//		
//		PropertyDefinition pDef = getPropertyFileDefinition();
//				
//		Map<String, PropertyDefinition> hm= new HashMap<>();
//		hm.put("krona", pDef);
//		
//		cv.putObject("propertyDefinitions", hm.values());
//		
//		pFileValue.validate(cv);
//		
//		showErrors(cv);
//		assertThat(cv.errors.size()).isEqualTo(0); 
//		
//	}
//	
//	@Test
//	public void validatePropertiesFileErr1() {
//		PropertyFileValue pFileValue = new  PropertyFileValue();
//		byte[] data = new byte[] { (byte)0xe0, 0x4f, (byte)0xd0,
//			    0x20, (byte)0xea, 0x3a, 0x69, 0x10, (byte)0xa2, (byte)0xd8, 0x08, 0x00, 0x2b,
//			    0x30, 0x30, (byte)0x9d };
//		pFileValue.value = data;
//		pFileValue.fullname = "krona.html";
//		pFileValue.extension = "";
//		ContextValidation cv = new ContextValidation(Constants.TEST_USER); 
//
//		PropertyDefinition pDef = getPropertyFileDefinition();
//		
//		Map<String, PropertyDefinition> hm= new HashMap<>();
//		hm.put("krona", pDef);
//		
//		cv.putObject("propertyDefinitions", hm.values());
//		
//		pFileValue.validate(cv);
//		
//		showErrors(cv);
//		assertThat(cv.errors.size()).isEqualTo(1); 
//		assertThat(cv.errors.toString()).contains("extension"); 
//		assertThat(cv.errors.toString()).contains("error.required");
//	}
//	
//	@Test
//	public void validatePropertiesFileErr2() {
//		PropertyFileValue pFileValue = new  PropertyFileValue();
//		byte[] data = new byte[] { (byte)0xe0, 0x4f, (byte)0xd0,
//			    0x20, (byte)0xea, 0x3a, 0x69, 0x10, (byte)0xa2, (byte)0xd8, 0x08, 0x00, 0x2b,
//			    0x30, 0x30, (byte)0x9d };
//		pFileValue.value = data;
//		pFileValue.fullname = "";
//		pFileValue.extension = "";
//		ContextValidation cv = new ContextValidation(Constants.TEST_USER); 
//
//		PropertyDefinition pDef = getPropertyFileDefinition();
//		
//		Map<String, PropertyDefinition> hm= new HashMap<>();
//		hm.put("krona", pDef);
//		
//		cv.putObject("propertyDefinitions", hm.values());
//		
//		pFileValue.validate(cv);
//		
//		showErrors(cv);
//		assertThat(cv.errors.size()).isEqualTo(2); 
//		assertThat(cv.errors.toString()).contains("extension");
//		assertThat(cv.errors.toString()).contains("fullname");
//		assertThat(cv.errors.toString()).contains("error.required");
//	}
//	
//	@Test
//	public void validatePropertiesFileImgOK() {
//		PropertyImgValue pImgValue = new  PropertyImgValue();
//		byte[] data = new byte[] { (byte)0xe0, 0x4f, (byte)0xd0,
//			    0x20, (byte)0xea, 0x3a, 0x69, 0x10, (byte)0xa2, (byte)0xd8, 0x08, 0x00, 0x2b,
//			    0x30, 0x30, (byte)0x9d };
//		pImgValue.value = data;
//		pImgValue.fullname = "phylogeneticTree2.jpg";
//		pImgValue.extension = "jpg";
//		pImgValue.width = 4;
//		pImgValue.height = 4;
//		
//		ContextValidation cv = new ContextValidation(Constants.TEST_USER); 
//
//		PropertyDefinition pDef = getPropertyImgDefinition();
//		
//		Map<String, PropertyDefinition> hm= new HashMap<>();
//		hm.put("phylogeneticTree2", pDef);
//		
//		cv.putObject("propertyDefinitions", hm.values());
//		
//		pImgValue.validate(cv);
//		
//		showErrors(cv);
//		assertThat(cv.errors.size()).isEqualTo(0); 	
//	}
//	
//	@Test
//	public void validatePropertiesFileImgErr() {
//		PropertyImgValue pImgValue = new  PropertyImgValue();
//		byte[] data = new byte[] { (byte)0xe0, 0x4f, (byte)0xd0,
//			    0x20, (byte)0xea, 0x3a, 0x69, 0x10, (byte)0xa2, (byte)0xd8, 0x08, 0x00, 0x2b,
//			    0x30, 0x30, (byte)0x9d };
//		pImgValue.value = data;
//		pImgValue.fullname = "phylogeneticTree2.jpg";
//		pImgValue.extension = "jpg";
//		
//		ContextValidation cv = new ContextValidation(Constants.TEST_USER); 
//
//		PropertyDefinition pDef = getPropertyImgDefinition();
//		
//		Map<String, PropertyDefinition> hm= new HashMap<>();
//		hm.put("phylogeneticTree2", pDef);
//		
//		cv.putObject("propertyDefinitions", hm.values());
//		
//		pImgValue.validate(cv);
//		
//		showErrors(cv);
//		assertThat(cv.errors.size()).isEqualTo(2);
//		assertThat(cv.errors.toString()).contains("width");
//		assertThat(cv.errors.toString()).contains("height");
//		assertThat(cv.errors.toString()).contains("error.required");
//	}
//	
//	@Test
//	public void validatePropertiesRequired() {
//		ContextValidation cv = new ContextValidation(Constants.TEST_USER);
//		ValidationHelper.validateProperties(cv, getPropertiesRequired(), getPropertyDefinitionsRequired());
//		showErrors(cv);
//		assertThat(cv.errors.size()).isEqualTo(getPropertyDefinitionsRequired().size()+2);
//	}
//
//
////	private void showErrors(ContextValidation cv) {
////		if (cv.errors.size() > 0) {
////			for(Entry<String, List<ValidationError>> e : cv.errors.entrySet()){
////				System.out.println(e);
////			}
////		}
////	}
//	private void showErrors(ContextValidation cv) {
//		for (Entry<String, List<ValidationError>> e : cv.errors.entrySet())
//			System.out.println(e);
//	}
//		
//	private Map<String, PropertyValue> getPropertiesRequired() {
//		Map<String, PropertyValue> m = new HashMap<>(); // String, PropertyValue>();
//		
//		PropertySingleValue propSingle = new PropertySingleValue();
//		m.put("single1-1", propSingle);
//		m.put("single1-2", propSingle);
//		m.put("single1-3", null);
//		PropertyListValue propList = new PropertyListValue();
//		m.put("list2-1", propList);
//		propList.value = new ArrayList<>();
//		m.put("list2-2",propList);
//		propList.value = Arrays.asList(new String[]{ "1", null, "2" });
//		m.put("list2-3", propList);
//		
////		Map<String, String> mapObject = new HashMap<String, String>();
//		Map<String, Object> mapObject = new HashMap<>();
//		mapObject.put("1", "1");
//		mapObject.put("2", null);
//		mapObject.put("3", "");
//		PropertyObjectValue propObject = new PropertyObjectValue(mapObject);
//		m.put("object4", propObject);
//		
////		List<Map<String,?>> l = new ArrayList<>();
//		List<Map<String,Object>> l = new ArrayList<>();
//		l.add(mapObject);
//		l.add(mapObject);
//		l.add(mapObject);
//		PropertyObjectListValue propObjectList =  new PropertyObjectListValue(l);
//		m.put("listObject5",propObjectList);
//		
//		return m;
//	}
//	
//	private List<PropertyDefinition> getPropertyDefinitionsRequired(){
//		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
//		propertyDefinitions.add(newPropertiesDefinition("required1-1", "single1-1", PropertySingleValue.class, String.class, true, "single"));
//		propertyDefinitions.add(newPropertiesDefinition("required1-2", "single1-2", PropertySingleValue.class, String.class, true, "single"));
//		propertyDefinitions.add(newPropertiesDefinition("required1-3", "single1-3", PropertySingleValue.class, String.class, true, "single"));
//		propertyDefinitions.add(newPropertiesDefinition("required1-4", "single1-4", PropertySingleValue.class, String.class, true, "single"));
//		
//		propertyDefinitions.add(newPropertiesDefinition("required2-1", "list2-1", PropertyListValue.class, String.class, true, "list"));
//		propertyDefinitions.add(newPropertiesDefinition("required2-2", "list2-2", PropertyListValue.class, String.class, true, "list"));
//		propertyDefinitions.add(newPropertiesDefinition("required2-3", "list2-3", PropertyListValue.class, String.class, true, "list"));
//		
//		propertyDefinitions.add(newPropertiesDefinition("required3-1", "map3-1", PropertyObjectValue.class, String.class, true, "object"));
//		propertyDefinitions.add(newPropertiesDefinition("required3-2", "map3-2", PropertyObjectValue.class, String.class, true, "object"));
//		propertyDefinitions.add(newPropertiesDefinition("required3-3", "map3-3", PropertyObjectValue.class, String.class, true, "object"));
//		
//		propertyDefinitions.add(newPropertiesDefinition("required4.1", "object4.1", PropertyObjectValue.class, String.class, true, "object"));
//		propertyDefinitions.add(newPropertiesDefinition("required4.2", "object4.2", PropertyObjectValue.class, String.class, true, "object"));
//		propertyDefinitions.add(newPropertiesDefinition("required4.3", "object4.3", PropertyObjectValue.class, String.class, true, "object"));
//		
//		propertyDefinitions.add(newPropertiesDefinition("required5.1", "listObject5.1", PropertyListValue.class, String.class, true, "list"));
//		propertyDefinitions.add(newPropertiesDefinition("required5.2", "listObject5.2", PropertyListValue.class, String.class, true, "list"));
//		propertyDefinitions.add(newPropertiesDefinition("required5.3", "listObject5.3", PropertyListValue.class, String.class, true, "list"));
//		
//		return propertyDefinitions;
//	}
//	
//	@Test
//	public void validatePropertiesOne() {
//		ContextValidation cv = new ContextValidation(Constants.TEST_USER);
//		ValidationHelper.validateProperties(cv, getPropertiesSingle(), getPropertyDefinitionsSingle());
//		showErrors(cv);
//		assertThat(cv.errors.size()).isEqualTo(0);	
//	}
//	
//	private Map<String, PropertyValue> getPropertiesSingle(){
//		Map<String, PropertyValue> m = new HashMap<>(); // String, PropertyValue>();
//		PropertySingleValue propText = new PropertySingleValue("test");
//		m.put("String", propText);
//		PropertySingleValue propInt = new PropertySingleValue(33);
//		m.put("Integer", propInt);
//		PropertySingleValue propDouble = new PropertySingleValue(36985214456467789654D);
//		m.put("Double", propDouble);
//		PropertySingleValue propBoolean = new PropertySingleValue(Boolean.TRUE);
//		m.put("Boolean", propBoolean);
//		PropertySingleValue propLong = new PropertySingleValue(36985214456467654L);
//		m.put("Long", propLong);
//		PropertySingleValue propDate = new PropertySingleValue(new Date());
//		m.put("Date", propDate);
//		PropertySingleValue propTBoolean = new PropertySingleValue(TBoolean.UNSET);
//		m.put("TBoolean", propTBoolean);
//		return m;
//	}
//	
//	@Test
//	public void validatePropertiesSingleString() {
//		ContextValidation cv = new ContextValidation(Constants.TEST_USER);
//		ValidationHelper.validateProperties(cv, getPropertiesSingleString(), getPropertyDefinitionsSingle());
//		showErrors(cv);
//		assertThat(cv.errors.size()).isEqualTo(0);
//	}
//	
//	private Map<String, PropertyValue> getPropertiesSingleString(){
//		Map<String, PropertyValue> m = new HashMap<>(); // String, PropertyValue>();
//		PropertySingleValue propString = new PropertySingleValue("test");
//		m.put("String", propString);
//		PropertySingleValue propInt = new PropertySingleValue(33+"");
//		m.put("Integer", propInt);
//		PropertySingleValue propDouble = new PropertySingleValue(36985214456467789654D+"");
//		m.put("Double", propDouble);
//		PropertySingleValue propBoolean  = new PropertySingleValue(Boolean.TRUE.toString());
//		m.put("Boolean", propBoolean);
//		PropertySingleValue propLong = new PropertySingleValue(36985214456467654L+"");
//		m.put("Long", propLong);
//		PropertySingleValue propDate = new PropertySingleValue(new Date().getTime()+"");
//		m.put("Date", propDate);
//		PropertySingleValue propTBoolean = new PropertySingleValue("UNSET");
//		m.put("TBoolean", propTBoolean);
//		return m;
//	}
//	
//	private List<PropertyDefinition> getPropertyDefinitionsSingle(){
//		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
//		propertyDefinitions.add(newPropertiesDefinition("String", "String", PropertySingleValue.class, String.class, true, "single"));
//		propertyDefinitions.add(newPropertiesDefinition("Integer", "Integer", PropertySingleValue.class, Integer.class, true, "single"));
//		propertyDefinitions.add(newPropertiesDefinition("Double", "Double", PropertySingleValue.class, Double.class, true, "single"));
//		propertyDefinitions.add(newPropertiesDefinition("Boolean", "Boolean", PropertySingleValue.class, Boolean.class, true, "single"));
//		propertyDefinitions.add(newPropertiesDefinition("Long", "Long", PropertySingleValue.class, Long.class, true, "single"));
//		propertyDefinitions.add(newPropertiesDefinition("Date", "Date", PropertySingleValue.class, Date.class, true, "single"));
//		propertyDefinitions.add(newPropertiesDefinition("TBoolean", "TBoolean", PropertySingleValue.class, TBoolean.class, true, "single"));
//		return propertyDefinitions;
//	}
//	
//	private PropertyDefinition newPropertiesDefinition(String name, String code, Class<?> propertyType, Class<?> valueType, Boolean required, String type) {
//		PropertyDefinition pd = new PropertyDefinition();		
////		pd.name = name;
//		pd.setName(name);
//		pd.code = code;
//		pd.active = true;
//		pd.propertyValueType = type;
//		pd.valueType = valueType.getName();
//		pd.required = required;
//		pd.choiceInList = false;		
//		return pd;
//	}
//	
//	@Test
//	public void validatePropertiesListString() {
//		ContextValidation cv = new ContextValidation(Constants.TEST_USER);
//		ValidationHelper.validateProperties(cv, getPropertiesListString(), getPropertyDefinitionsList());
//		showErrors(cv);
//		assertThat(cv.errors.size()).isEqualTo(0);
//	}
//	
//	private Map<String, PropertyValue> getPropertiesListString(){
//		Map<String, PropertyValue> m = new HashMap<>(); // String, PropertyValue>();
//		PropertyListValue propListString = new PropertyListValue(Arrays.asList("test", "test2", "tes3"));
//		m.put("String", propListString);
//		PropertyListValue propListInt = new PropertyListValue(Arrays.asList("33","36","65"));
//		m.put("Integer", propListInt);
//		PropertyListValue propListDouble = new PropertyListValue(Arrays.asList("36985214456467789654"));
//		m.put("Double", propListDouble);
//		PropertyListValue propListBoolean = new PropertyListValue(Arrays.asList(Boolean.TRUE.toString()));
//		m.put("Boolean", propListBoolean);
//		PropertyListValue propListLong = new PropertyListValue(Arrays.asList(36985214456467654L+""));
//		m.put("Long", propListLong);
//		PropertyListValue propListDate = new PropertyListValue(Arrays.asList(new Date().getTime()+""));
//		m.put("Date", propListDate);
//		PropertyListValue propListTBoolean = new PropertyListValue(Arrays.asList("UNSET"));
//		m.put("TBoolean", propListTBoolean);
//		return m;
//	}
//	
//	private List<PropertyDefinition> getPropertyDefinitionsList(){
//		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
//		propertyDefinitions.add(newPropertiesDefinition("String",   "String",   PropertyListValue.class, String.class,   true, "list"));
//		propertyDefinitions.add(newPropertiesDefinition("Integer",  "Integer",  PropertyListValue.class, Integer.class,  true, "list"));
//		propertyDefinitions.add(newPropertiesDefinition("Double",   "Double",   PropertyListValue.class, Double.class,   true, "list"));
//		propertyDefinitions.add(newPropertiesDefinition("Boolean",  "Boolean",  PropertyListValue.class, Boolean.class,  true, "list"));
//		propertyDefinitions.add(newPropertiesDefinition("Long",     "Long",     PropertyListValue.class, Long.class,     true, "list"));
//		propertyDefinitions.add(newPropertiesDefinition("Date",     "Date",     PropertyListValue.class, Date.class,     true, "list"));
//		propertyDefinitions.add(newPropertiesDefinition("TBoolean", "TBoolean", PropertyListValue.class, TBoolean.class, true, "list"));
//		return propertyDefinitions;
//	}
//	
////	private Map<String, String> getMap(String...strings) {
////		Map<String, String> m = new HashMap<String, String>();
////		int i = 0;
////		for(String str: strings){
////			m.put(i+++"", str);
////		}		
////		return m;
////	}
//	private Map<String, Object> getMap(String...strings) {
//		Map<String, Object> m = new HashMap<>();
//		int i = 0;
//		for (String str : strings)
//			m.put(i++ + "", str);
//		return m;
//	}
//
////	private List<PropertyDefinition> getPropertyDefinitionsMap(){
////		List<PropertyDefinition> propertyDefinitions = new ArrayList<PropertyDefinition>();
////		propertyDefinitions.add(newPropertiesDefinition("String", "String", PropertyObjectValue.class, String.class, true, "object"));
////		propertyDefinitions.add(newPropertiesDefinition("Integer", "Integer", PropertyObjectValue.class, Integer.class, true, "object"));
////		propertyDefinitions.add(newPropertiesDefinition("Double", "Double", PropertyObjectValue.class, Double.class, true, "object"));
////		propertyDefinitions.add(newPropertiesDefinition("Boolean", "Boolean", PropertyObjectValue.class, Boolean.class, true, "object"));
////		propertyDefinitions.add(newPropertiesDefinition("Long", "Long", PropertyObjectValue.class, Long.class, true, "object"));
////		propertyDefinitions.add(newPropertiesDefinition("Date", "Date", PropertyObjectValue.class, Date.class, true, "object"));
////		propertyDefinitions.add(newPropertiesDefinition("TBoolean", "TBoolean", PropertyObjectValue.class, TBoolean.class, true, "object"));
////		return propertyDefinitions;
////	}
//	
//	@Test
//	public void validatePropertiesObjectOK() {
//		ContextValidation cv = new ContextValidation(Constants.TEST_USER);
//		ValidationHelper.validateProperties(cv, getPropertiesObjectString(), getPropertyDefinitionsObject());
//		showErrors(cv);
//		assertThat(cv.errors.size()).isEqualTo(0);		
//	}
//	
//	private Map<String, PropertyValue> getPropertiesObjectString() {
//		Map<String, PropertyValue> m = new HashMap<>(); // String, PropertyValue>();
//		PropertyObjectValue propObject = new PropertyObjectValue(getMap("test", "45", "36985214456467789654"));
//		m.put("Object", propObject);
//		return m;
//	}
//	
//	private List<PropertyDefinition> getPropertyDefinitionsObject() {
//		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
//		propertyDefinitions.add(newPropertiesDefinition("Object.String",  "Object.0", PropertyObjectValue.class, String.class,  true, "object"));
//		propertyDefinitions.add(newPropertiesDefinition("Object.Integer", "Object.1", PropertyObjectValue.class, Integer.class, true, "object"));
//		propertyDefinitions.add(newPropertiesDefinition("Object.Double",  "Object.2", PropertyObjectValue.class, Double.class,  true, "object"));		
//		return propertyDefinitions;
//	}
//
//}
