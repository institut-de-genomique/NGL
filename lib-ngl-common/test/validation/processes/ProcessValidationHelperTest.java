package validation.processes;

import static org.fest.assertions.Assertions.assertThat;

//import java.util.List;
import models.laboratory.processes.instance.Process;
import models.laboratory.processes.instance.SampleOnInputContainer;
import models.utils.InstanceConstants;
//import org.junit.Test;
import org.mongojack.DBQuery;
//import play.Logger;
import utils.AbstractTests;
import utils.Constants;
import validation.ContextValidation;
//import validation.sample.instance.SampleValidationHelper;
import fr.cea.ig.MongoDBDAO;

public class ProcessValidationHelperTest extends AbstractTests {	

	/**
	 *  ProcessType
	 * 
	 */
	
	/**
	 *  ProcessCategory
	 * 
	 */
	
	/**
	 *  CurrentExperimentTypeCode
	 * 
	 */
	
	/**
	 *  ExperimentCodes
	 * 
	 */
	
	/**
	 *  StateCode
	 * 
	 */
	
	/**
	 *  SampleOnInputContainer
	 * 
	 */
	
//	@Test
	public void validateSampleOnInputContainer() {
		ContextValidation contextValidation=new ContextValidation(Constants.TEST_USER);
		//ProcessValidationHelper.validateSampleOnInputContainer(soic, contextValidation);
		Process process = MongoDBDAO.find(InstanceConstants.PROCESS_COLL_NAME, Process.class, DBQuery.exists("sampleOnInputContainer.properties.tag")).toList().get(5);				
		assertThat(process).isNotNull();		
		SampleOnInputContainer soic = process.sampleOnInputContainer;
		
		soic.validate(contextValidation);		
		assertThat(contextValidation.errors.size()).isEqualTo(0);
		
		String codeBkp = soic.sampleCode.toString(); 
		soic.sampleCode = "badSampleCode";
		soic.validate(contextValidation);
		assertThat(contextValidation.errors.size()).isEqualTo(1);
		
		soic.sampleCode = codeBkp;
		soic.validate(contextValidation);		
		assertThat(contextValidation.errors.size()).isEqualTo(0);	
	}
	
}
