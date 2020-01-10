package SraValidation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import models.laboratory.run.instance.ReadSet;
import models.sra.submit.common.instance.Readset;
import models.sra.submit.sra.instance.Experiment;
import models.sra.submit.sra.instance.Run;
import models.sra.submit.util.SraException;
import models.utils.InstanceConstants;
import utils.AbstractTestsSRA;
import services.SubmissionServices;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import fr.cea.ig.MongoDBDAO;

public class nanoporeTest extends AbstractTestsSRA {

	@Test
	public void mon_test() throws IOException, SraException  {
		List<String> readSetCodes = new ArrayList<String>();
		String codeReadSet = "BWW_B_ONT_1_FAB45991_B"; // pas de submissionState
		codeReadSet = "AWK_D_ONT_1_FAA57595_B";
		codeReadSet = "BNZ_C_ONT_1_FAA101310_A";
		
		readSetCodes.add(codeReadSet);
		get_infos(readSetCodes);
	}
	
	
	public void get_infos(List<String> readSetCodes) throws IOException, SraException  {
		List<ReadSet> readSets = new ArrayList<ReadSet>();
		String user = "williTest";
		// Recuperation des readset et verifier 
		for (String readSetCode : readSetCodes) {
			if (StringUtils.isBlank(readSetCode)) {
				continue;
			}
			//System.out.println("!!!!!!!!!!!!!         readSetCode = " + readSetCode);
			ReadSet readSet = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, readSetCode);
			if (readSet == null) {
				System.out.println("Le readSet " + readSet.code + "n'existe pas dans la base ???");
				continue;
			} 
			readSets.add(readSet);
			// Verifier status de soumission :
			
			if (readSet.submissionState != null ){
				System.out.println("Le readSet " + readSet.code + " a un status de soumission à " + readSet.submissionState.code);	
			}
			// Le scientificName est utilisé dans la construction
			// des samples et des experiments. Il est recuperé par NGL-BI au ncbi 
			String laboratorySampleCode = readSet.sampleCode;
			models.laboratory.sample.instance.Sample laboratorySample = MongoDBDAO.findByCode(InstanceConstants.SAMPLE_COLL_NAME, models.laboratory.sample.instance.Sample.class, laboratorySampleCode);
			String taxonId = laboratorySample.taxonCode;
			String scientificName = laboratorySample.ncbiScientificName;
			if (StringUtils.isBlank(scientificName)){
				//scientificName=updateLaboratorySampleForNcbiScientificName(taxonId, contextValidation); fait dans ngl-bi
				System.out.println("Pas de recuperation du nom scientifique pour le sample "+ laboratorySampleCode);
			}
			SubmissionServices submissionServices = new SubmissionServices();
			// Creer l'experiment avec un state.code = 'N'
			Run run = submissionServices.createRunEntity(readSet);
			System.out.println("run.code = " + run.code);
			System.out.println("run.reunCenter = " +run.runCenter); 
			System.out.println("run.expCode = " + run.expCode);
			System.out.println("run.Date = " + run.runDate);
			System.out.println("run.Date = " + run.runDate);
			
			Experiment experiment = submissionServices.createExperimentEntity(readSet, scientificName, user);
			System.out.println("libraryLayoutOrientation = "+experiment.libraryLayoutOrientation);
			System.out.println("lastBaseCoord="+experiment.lastBaseCoord);
			System.out.println("spotLength="+experiment.spotLength);
			System.out.println("libraryLayoutNominalLength="+experiment.libraryLayoutNominalLength);
			
			String laboratorySampleName = laboratorySample.name;
			String clone = laboratorySample.referenceCollab;	
			System.out.println("name = '" + laboratorySampleName +"'");
			System.out.println("clone = '" + clone +"'");
		}	
		
	}	
		
}