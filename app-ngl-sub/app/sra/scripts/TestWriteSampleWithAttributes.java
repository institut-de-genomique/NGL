package sra.scripts;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import fr.cea.ig.lfw.controllers.scripts.buffered.ScriptNoArgs;
import fr.cea.ig.ngl.dao.api.sra.SubmissionAPI;
import models.sra.submit.sra.instance.Submission;
import services.XmlServices;
import validation.ContextValidation;
import services.EbiFileResponseServices;
//http://localhost:9000/sra/scripts/run/sra.scripts.TestWriteSampleWithAttributes
public class TestWriteSampleWithAttributes extends ScriptNoArgs {
	private final XmlServices   xmlServices;
	private SubmissionAPI submissionAPI;
	
	@Inject
	public TestWriteSampleWithAttributes(XmlServices        xmlServices,
			                             
			EbiFileResponseServices ebiFileResponseServices,
			SubmissionAPI         submissionAPI) {
		this.xmlServices   = xmlServices;
		this.submissionAPI = submissionAPI;
	}
	

	@Override
	public void execute() throws Exception {
		File outputFile = new File("\\C:\\Users\\sgas.IBFJ-EVRY\\TEST_2\\sample.xml");
		List<String> sampleCodes = new ArrayList<String>();
		sampleCodes.add("sample_DDS_256318_PE_J60");
		xmlServices.writeSampleXml(sampleCodes , outputFile);
		ContextValidation ctxVal = ContextValidation.createUpdateContext("sgas");
		Submission submission = submissionAPI.get("GSC_DDS_86EJ5AAA9");
		
		File ebiFileResp = new File("\\C:\\Users\\sgas.IBFJ-EVRY\\TEST_2\\retourEBI_GSC_DDS_86EJ5AAA9.txt");
		//mettre loadEbiResp en cond de test avant de lancer :
		//ebiFileResponseServices.loadEbiResp(ctxVal, submission, ebiFileResp );
		println("fin du test");
	}
		
		
}