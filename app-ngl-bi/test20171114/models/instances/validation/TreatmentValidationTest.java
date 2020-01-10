package models.instances.validation;

import static org.fest.assertions.Assertions.assertThat;
import static validation.utils.ValidationConstants.ERROR_BADTYPE_MSG;
import static validation.utils.ValidationConstants.ERROR_CODE_NOTEXISTS_MSG;
import static validation.utils.ValidationConstants.ERROR_NOTDEFINED_MSG;
import static validation.utils.ValidationConstants.ERROR_REQUIRED_MSG;
import static validation.utils.ValidationConstants.ERROR_VALUENOTAUTHORIZED_MSG;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.mongojack.DBQuery;

import fr.cea.ig.MongoDBDAO;
import models.laboratory.common.description.Level;
import models.laboratory.common.instance.PropertyValue;
import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.run.instance.ReadSet;
import models.laboratory.run.instance.Run;
import models.laboratory.run.instance.Treatment;
import models.utils.InstanceConstants;
import play.Logger;
import play.data.validation.ValidationError;
import utils.AbstractTestsCNG;
import utils.Constants;
import utils.RunMockHelper;
import validation.ContextValidation;


public class TreatmentValidationTest extends AbstractTestsCNG {	

	@Test
	public void testValidateTreatmentErrorMissingLevel() {
		Boolean b = false;
		String msgErreur = "";

		Treatment t = getNewTreatmentForReadSet();

		ContextValidation ctxVal = new ContextValidation(Constants.TEST_USER); 
		ReadSet readset = RunMockHelper.newReadSet("rdCode");
		ctxVal.putObject("readSet", readset);		
		ctxVal.setCreationMode();

		try {
			t.validate(ctxVal);
		}
		catch(IllegalArgumentException e) {
			System.out.println(e.toString());
			b = true;
			msgErreur = "missing level parameter";
		}		
		assertThat(b).isEqualTo(true);
		assertThat(msgErreur).isEqualTo("missing level parameter");
	}



	@Test
	public void testValidationTreatmentErrorMissingCode() {			
		Treatment t = getNewTreatmentForReadSet();
		t.code =  null;	// NO CODE!	

		ContextValidation ctxVal = new ContextValidation(Constants.TEST_USER); 
		Level.CODE levelCode = Level.CODE.ReadSet; 
		ctxVal.putObject("level", levelCode);
		ReadSet readset = RunMockHelper.newReadSet("rdCode");
		ctxVal.putObject("readSet", readset);		
		ctxVal.setCreationMode();

		t.validate(ctxVal);

		assertThat(ctxVal.errors.size()).isGreaterThan(0);
		assertThat(ctxVal.errors.toString()).contains(ERROR_REQUIRED_MSG);
		assertThat(ctxVal.errors.toString()).contains("code");
	}



	@Test
	public void testValidateTreatmentErrorCodeRequired() {	
		Treatment t = getNewTreatmentForReadSet();
		t.code =  ""; //empty!		

		ContextValidation ctxVal = new ContextValidation(Constants.TEST_USER); 
		Level.CODE levelCode = Level.CODE.ReadSet; 
		ctxVal.putObject("level", levelCode);
		ReadSet readset = RunMockHelper.newReadSet("rdCode");
		ctxVal.putObject("readSet", readset);		
		ctxVal.setCreationMode();

		t.validate(ctxVal);

		assertThat(ctxVal.errors.size()).isGreaterThan(0);
		assertThat(ctxVal.errors.toString()).contains(ERROR_REQUIRED_MSG);
		assertThat(ctxVal.errors.toString()).contains("code");
	}

	@Test
	public void testValidateTreatmentCodeNotExist()
	{
		Treatment t = getNewTreatmentForReadSet();
		t.code =  "badCode"; //empty!		

		ContextValidation ctxVal = new ContextValidation(Constants.TEST_USER); 
		Level.CODE levelCode = Level.CODE.ReadSet; 
		ctxVal.putObject("level", levelCode);
		ReadSet readset = RunMockHelper.newReadSet("rdCode");
		ctxVal.putObject("readSet", readset);		
		ctxVal.setCreationMode();

		t.validate(ctxVal);

		assertThat(ctxVal.errors.size()).isGreaterThan(0);
		assertThat(ctxVal.errors.toString()).contains("code");
	}


	@Test
	public void testValidationTreatmentErrorTypeCodeRequired() {	
		deleteRdCode();

		Treatment t = getNewTreatmentForReadSet();	
		t.typeCode = ""; //vide!

		ContextValidation ctxVal = new ContextValidation(Constants.TEST_USER); 
		Level.CODE levelCode = Level.CODE.ReadSet; 
		ctxVal.putObject("level", levelCode);
		ReadSet readset = RunMockHelper.newReadSet("rdCode");
		ctxVal.putObject("readSet", readset);		
		ctxVal.setCreationMode();

		t.validate(ctxVal);

		assertThat(ctxVal.errors).hasSize(1);
		assertThat(ctxVal.errors.toString()).contains(ERROR_REQUIRED_MSG);
		assertThat(ctxVal.errors.toString()).contains("typeCode");
	}

	@Test
	public void testValidationTreatmentErrorTypeCodeNotExist()
	{
		Treatment t = getNewTreatmentForReadSet();	
		t.typeCode = "badCode"; 
		
		ContextValidation ctxVal = new ContextValidation(Constants.TEST_USER); 
		Level.CODE levelCode = Level.CODE.ReadSet; 
		ctxVal.putObject("level", levelCode);
		ReadSet readset = RunMockHelper.newReadSet("rdCode");
		ctxVal.putObject("readSet", readset);		
		ctxVal.setCreationMode();

		t.validate(ctxVal);

		assertThat(ctxVal.errors).hasSize(1);
		assertThat(ctxVal.errors.toString()).contains("typeCode");
	}

	@Test
	public void testValidateTreatmentErrorCategoryCodeRequired() {
		deleteRdCode(); 

		Treatment t = getNewTreatmentForReadSet();
		t.categoryCode = ""; //vide!

		ContextValidation ctxVal = new ContextValidation(Constants.TEST_USER); 
		Level.CODE levelCode = Level.CODE.ReadSet; 
		ctxVal.putObject("level", levelCode);
		ReadSet readset = RunMockHelper.newReadSet("rdCode");
		ctxVal.putObject("readSet", readset);		
		ctxVal.setCreationMode();

		t.validate(ctxVal);

		assertThat(ctxVal.errors).hasSize(1);
		assertThat(ctxVal.errors.toString()).contains(ERROR_REQUIRED_MSG);
		assertThat(ctxVal.errors.toString()).contains("categoryCode");
	}
	
	@Test
	public void testValidateTreatmentErrorCategoryCodeNotExist()
	{
		Treatment t = getNewTreatmentForReadSet();
		t.categoryCode = "badCode";

		ContextValidation ctxVal = new ContextValidation(Constants.TEST_USER); 
		Level.CODE levelCode = Level.CODE.ReadSet; 
		ctxVal.putObject("level", levelCode);
		ReadSet readset = RunMockHelper.newReadSet("rdCode");
		ctxVal.putObject("readSet", readset);		
		ctxVal.setCreationMode();

		t.validate(ctxVal);

		assertThat(ctxVal.errors).hasSize(1);
		assertThat(ctxVal.errors.toString()).contains("categoryCode");
	}

	@Test
	public void testValidateTreatmentErrorCodeNotExists() {
		Treatment t = getNewTreatmentForReadSet();

		// create treatment
		deleteRdCode();

		ContextValidation ctxVal = new ContextValidation(Constants.TEST_USER); 
		Level.CODE levelCode = Level.CODE.ReadSet; 
		ctxVal.putObject("level", levelCode);
		ReadSet readset = RunMockHelper.newReadSet("rdCode");
		ctxVal.putObject("readSet", readset);		
		ctxVal.setUpdateMode(); //in this case, we must be in update mode

		ReadSet readSetExists = MongoDBDAO.findOne(InstanceConstants.READSET_ILLUMINA_COLL_NAME,ReadSet.class,DBQuery.is("code","rdCode"));
		if(readSetExists==null){

			t.validate(ctxVal);

			assertThat(ctxVal.errors).hasSize(1);
			assertThat(ctxVal.errors.toString()).contains(ERROR_CODE_NOTEXISTS_MSG);
		}
		else {
			System.out.println("method deleteRdCode() doesn't run normally !");
			Assert.assertTrue(false);
		}

	}



	@Test
	public void testValidateTreatmentErrorValueNotDefined() {
		Treatment t = getNewTreatmentForReadSet();

		t.results().get("default").put("bad", new PropertySingleValue("Ouh la la"));

		ContextValidation ctxVal = new ContextValidation(Constants.TEST_USER); 
		Level.CODE levelCode = Level.CODE.ReadSet; 
		ctxVal.putObject("level", levelCode);
		ReadSet readset = RunMockHelper.newReadSet("rdCode");
		ctxVal.putObject("readSet", readset);			
		ctxVal.setCreationMode();

		t.validate(ctxVal);

		assertThat(ctxVal.errors).hasSize(1);
		assertThat(ctxVal.errors.toString()).contains(ERROR_NOTDEFINED_MSG);
	}



	@Test
	public void testValidateTreatmentErrorBadTypeValue() throws NumberFormatException {
		Treatment t = null; 
		ContextValidation ctxVal = new ContextValidation(Constants.TEST_USER);  
		Level.CODE levelCode = Level.CODE.ReadSet; 
		ctxVal.putObject("level", levelCode);
		ReadSet readset = RunMockHelper.newReadSet("rdCode");
		ctxVal.putObject("readSet", readset);	    		
		ctxVal.setCreationMode();

		t = getNewTreatmentForReadSet();

		t.results().get("default").remove("nbReadIllumina");
		//must generate a error (because of a bad value)
		t.results().get("default").put("nbReadIllumina", new PropertySingleValue("un"));	

		t.validate(ctxVal);

		assertThat(ctxVal.errors).hasSize(1);
		assertThat(ctxVal.errors.toString()).contains(ERROR_BADTYPE_MSG);
	}



	@Test
	public void testValidateTreatmentErrorBadContext() {
		Treatment t = getNewTreatmentForReadSet();

		// new bad context
		Map<String,PropertyValue> m3 = new HashMap<String,PropertyValue>();
		m3.put("nbCluster", new PropertySingleValue(10));
		m3.put("nbBases", new PropertySingleValue(100));
		m3.put("fraction", new PropertySingleValue(33.33));
		m3.put("Q30", new PropertySingleValue(33.33));
		m3.put("qualityScore", new PropertySingleValue(33.33));
		m3.put("nbReadIllumina", new PropertySingleValue(1));

		t.set("read3", m3);

		ContextValidation ctxVal = new ContextValidation(Constants.TEST_USER); 
		Level.CODE levelCode = Level.CODE.ReadSet; 
		ctxVal.putObject("level", levelCode);
		ReadSet readset = RunMockHelper.newReadSet("rdCode");
		ctxVal.putObject("readSet", readset);
		ctxVal.setCreationMode();

		t.validate(ctxVal);

		assertThat(ctxVal.errors).hasSize(1);
		assertThat(ctxVal.errors.toString()).contains(ERROR_VALUENOTAUTHORIZED_MSG);	
	}

	@Test
	public void testValidateTreatmentMissingProperties()
	{
		Treatment t = getTreatmentMissingProperties();

		ContextValidation ctxVal = new ContextValidation(Constants.TEST_USER); 
		Level.CODE levelCode = Level.CODE.ReadSet; 
		ctxVal.putObject("level", levelCode);
		ReadSet readset = RunMockHelper.newReadSet("rdCode");
		ctxVal.putObject("readSet", readset);		
		ctxVal.setCreationMode();

		t.validate(ctxVal);

		Logger.debug("ERROR MISSING PROPERTIES");
		for (Map.Entry<String, List<ValidationError>> entry : ctxVal.errors.entrySet()) {
			Logger.debug(entry.getKey() + "---------------------------------->" + entry.getValue().toString());
		}
		
		assertThat(ctxVal.errors).hasSize(1);
		assertThat(ctxVal.errors.toString()).contains("default.Q30.value");
	}


	private void deleteRdCode() {
		Run runDelete = MongoDBDAO.findOne(InstanceConstants.RUN_ILLUMINA_COLL_NAME,Run.class,DBQuery.is("code","DIDIER_TESTFORTRT"));
		if(runDelete!=null){
			MongoDBDAO.delete(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, runDelete._id);
		}
		ReadSet readSetDelete = MongoDBDAO.findOne(InstanceConstants.READSET_ILLUMINA_COLL_NAME,ReadSet.class,DBQuery.is("code","rdCode"));
		if(readSetDelete!=null){
			MongoDBDAO.delete(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class,readSetDelete._id);
		}
	}	



	private Treatment getNewTreatmentForReadSet() {
		Treatment t = new Treatment();
		t.code =  "ngsrg";		
		t.typeCode = "ngsrg-illumina";
		t.categoryCode = "ngsrg";
		//define map of single property values
		Map<String,PropertyValue> m = new HashMap<String,PropertyValue>();
		m.put("nbCluster", new PropertySingleValue(100)); // valeur simple
		m.put("nbBases", new PropertySingleValue(100));
		m.put("fraction", new PropertySingleValue(100));
		m.put("Q30", new PropertySingleValue(100));
		m.put("qualityScore", new PropertySingleValue(100));
		m.put("nbReadIllumina", new PropertySingleValue(100));
		t.set("default", m);	
		return t;
	}
	
	private Treatment getTreatmentMissingProperties()
	{
		Treatment t = new Treatment();
		t.code =  "ngsrg";		
		t.typeCode = "ngsrg-illumina";
		t.categoryCode = "ngsrg";
		//define map of single property values
		Map<String,PropertyValue> m = new HashMap<String,PropertyValue>();
		m.put("nbCluster", new PropertySingleValue(100)); // valeur simple
		m.put("nbBases", new PropertySingleValue(100));
		m.put("fraction", new PropertySingleValue(100));
		//m.put("Q30", new PropertySingleValue(100));
		m.put("qualityScore", new PropertySingleValue(100));
		m.put("nbReadIllumina", new PropertySingleValue(100));
		t.set("default", m);	
		return t;
	}


}
