package sra.scripts;

import java.io.BufferedWriter;
import java.io.File;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import javax.inject.Inject;


import org.apache.commons.lang3.StringUtils;

import fr.cea.ig.lfw.controllers.scripts.buffered.ScriptNoArgs;
import fr.cea.ig.ngl.dao.api.sra.ConfigurationAPI;
import fr.cea.ig.ngl.dao.api.sra.SubmissionAPI;
import models.sra.submit.sra.instance.Configuration;
import models.sra.submit.sra.instance.Submission;
import models.sra.submit.sra.instance.UserRefCollabType;


/*
 * Script a lancer pour lister les refCollab utilisées dans les soumissions
 * Exemple de lancement :
 * http://localhost:9000/sra/scripts/run/sra.scripts.ListeRefCollab
 
 * @author sgas
 *
 */
public class ListeRefCollab extends ScriptNoArgs {
	
	private final SubmissionAPI submissionAPI;
	private final ConfigurationAPI configurationAPI;

	

	@Inject
	public ListeRefCollab (SubmissionAPI submissionAPI,
						   ConfigurationAPI configurationAPI
				    				  ) {
		this.submissionAPI = submissionAPI;
		this.configurationAPI = configurationAPI;

	}
	
	// Renvoie l'index de la nième occurence du caractere dans chaine
	public int getIndexCaractere(String str, char c, int n) { 
		int pos = str.indexOf(c, 0); 
		while (n-- >= 0 && pos != -1) {
			pos = str.indexOf(c, pos+1); 
		}
		return pos; 
    }


	@Override
	public void execute() throws Exception {
		//writeEbiSubmissionDate(new File(args.outputFile));
		int cp = 0;
		List<String> listSubCodes = new ArrayList<String>();
		listSubCodes.add("CNS_BYQ_AWF_24RF4HJFI");

		java.util.Date courantDate = new java.util.Date();

		File fileSubmission_ok = new File("C:\\Users\\sgas\\debug\\refCollab.txt");
		Iterable<Submission> iterable = submissionAPI.dao_all();
		try (BufferedWriter output_buffer = new BufferedWriter(new java.io.FileWriter(fileSubmission_ok))) {
			
			for (Submission submission: iterable) {
				if (!submission.type.equals(Submission.Type.CREATION)) {
					continue;
				}
				if(submission.mapUserRefCollab!= null) {
					for (Entry<String, UserRefCollabType> mapentry : submission.mapUserRefCollab.entrySet()) {
						cp++;
						//output_buffer.write("refCollab = "+ mapentry.getKey() + "\n");
						output_buffer.write(mapentry.getKey() + "\n");
						println(mapentry.getKey() + "\n");

				    }
				} else {
					if(StringUtils.isBlank(submission.configCode)) {
						continue;
					}
					Configuration conf = configurationAPI.get(submission.configCode);
					if(conf==null) {
						continue;
					}
					switch (conf.strategySample) {
					case STRATEGY_CODE_SAMPLE_REFCOLLAB :
						for(String sampleCode : submission.sampleCodes) {
							//int index = getIndexCaractere(sampleCode, '_', 3);
							int index = StringUtils.ordinalIndexOf(sampleCode, "_", 3) + 1;
							String refCollab = sampleCode;
							if (index > 0) {
								 refCollab = sampleCode.substring(index);
							}
							cp++;
							//output_buffer.write("refCollab = "+ refCollab +  " et sampleCode " + sampleCode + "\n");
							output_buffer.write(refCollab + "\n");

							println( sampleCode +  " | " +  refCollab + "\n");

						}
						break;

					default:
						break;		
					}	
				}
			}
		}

			println("Nombre de refCollab  = " + cp);
		}
			
	}
	
	



