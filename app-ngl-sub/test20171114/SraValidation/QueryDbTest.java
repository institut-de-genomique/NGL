package SraValidation;

import java.io.IOException;
import java.util.Date;

import models.laboratory.run.instance.ReadSet;
import models.sra.submit.sra.instance.Experiment;
import models.sra.submit.sra.instance.Run;
import models.sra.submit.util.SraException;
import models.utils.InstanceConstants;

import org.junit.Assert;
import org.junit.Test;
import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;

import play.Logger;
import fr.cea.ig.MongoDBDAO;
import services.SubmissionServices;
import utils.AbstractTestsSRA;
import validation.ContextValidation;

public class QueryDbTest extends AbstractTestsSRA {
	
	@Test
	public void validationRunSuccess() throws IOException, SraException {
		String exp_accession = "ERX1609850";
		Experiment experiment = MongoDBDAO.findOne(InstanceConstants.SRA_EXPERIMENT_COLL_NAME,
				Experiment.class, DBQuery.and(DBQuery.is("accession", exp_accession)));
		if (experiment != null) {
			System.out.println("dans database, experiment_code="+ experiment.code + " et experiment_accession = "+ experiment.accession); 
		} else {
			System.out.println("Aucun experiment dans la base avec le numeros d'accession :"  + exp_accession); 
		}
	
		experiment.readSetCode = "BCM_ADM_ONT_1_FAAA62005_A|BCM_ADM_ONT_1_FAA83469_A|BCM_ADM_ONT_1_FAA84613_A|BCM_ADM_ONT_1_FAA84613_B|BCM_ADM_ONT_1_FAA84655_B";
		experiment.readSetCode ="BCM_CFA_ONT_1_FAA50002_A|BCM_CFA_ONT_1_FAA54985_A|BCM_CFA_ONT_1_FAA56751_A|BCM_CFA_ONT_1_FAA567757_A|BCM_CFA_ONT_1_FAA57368_A";
		String user = "william";
		String separator= "\\|";
		String [] nameReadSet = experiment.readSetCode.split(separator);
		
		for(int i =0; i < nameReadSet.length ; i++) {			
			if (! MongoDBDAO.checkObjectExist(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, "code", nameReadSet[i])){	
				System.out.println(nameReadSet[i] + " absent de la database");
			} else {
				MongoDBDAO.update(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class,
				DBQuery.is("code", nameReadSet[i]),
				DBUpdate.set("submissionState.code", "F-SUB").set("traceInformation.modifyUser", user).set("traceInformation.modifyDate", new Date()));
			}

		}
		
		
	}
	
	
	
	
}
