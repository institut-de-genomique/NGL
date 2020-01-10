package controllers.migration;		

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;

import controllers.CommonController;
import fr.cea.ig.MongoDBDAO;
import models.laboratory.common.instance.TBoolean;
import models.laboratory.run.instance.ReadSet;
import models.utils.InstanceConstants;
// import play.Logger;
// import play.api.modules.spring.Spring;
import play.mvc.Result;
import validation.ContextValidation;
import workflows.readset.ReadSetWorkflows;

/**
 * Update bioinformatic valuation for TARA data from file
 * SUPSQ-2036
 * @author ejacoby
 *
 */
public class MigrationUpdateReadSetTaraInvalid extends CommonController {
	
	private static final play.Logger.ALogger logger = play.Logger.of(MigrationUpdateReadSetTaraInvalid.class);
	
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmm");
	
	// final static ReadSetWorkflows workflows = Spring.get BeanOfType(ReadSetWorkflows.class);
	private final ReadSetWorkflows workflows;
	
	@Inject
	public MigrationUpdateReadSetTaraInvalid(ReadSetWorkflows workflows) {
		this.workflows = workflows;
	}
	
	public Result migration() throws IOException, ParseException{
		
		MigrationForm form = filledFormQueryString(MigrationForm.class);
		List<ReadSet> readSets = getReadSetToUpdate(form.file);
		
		//Backup readSet 
		backUpReadSets(readSets);
		//Update readSets
		//Test first 
		//ReadSet readSet = readSets.iterator().next();
		//Logger.debug("ReadSet code "+readSet.code);
		for(ReadSet readSet: readSets){
			//new liste resolutions
			Set<String> resolutionCodes = readSet.bioinformaticValuation.resolutionCodes;
			if(resolutionCodes==null)
				resolutionCodes = new HashSet<String>();
			resolutionCodes.add("Info-nvoCritereEval");
			MongoDBDAO.update(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, 
					DBQuery.is("code", readSet.code),  
					DBUpdate.set("bioinformaticValuation.valid", TBoolean.TRUE)
							.set("bioinformaticValuation.resolutionCodes", resolutionCodes)
							.set("bioinformaticValuation.comment", "valide suite aux nouveaux critères validation TARA appliqués aux dépôts des banques à 25%: "+
																	"bact et/ou fungi>5%,dupl paired en raw>20% ou manque info sur dupl paired en raw "+
																	"+ pb repartition en bases non invalidant")
							.set("productionValuation.comment", "valide suite aux nouveaux critères validation TARA appliqués aux dépôts des banques à 25%: "+
									"bact et/ou fungi>5%,dupl paired en raw>20% ou manque info sur dupl paired en raw "+
									"+ pb repartition en bases non invalidant"));	
			readSet = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, readSet.code);
			ContextValidation ctxVal = new ContextValidation(getCurrentUser());
			ctxVal.setUpdateMode();
			workflows.nextState(ctxVal, readSet);
		}
		
		return ok("Migration Finish");

	}
	
	private static List<ReadSet> getReadSetToUpdate(String file) throws IOException, ParseException	{
		SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
		Date date = df.parse("01/01/2016");

		List<ReadSet> readsetsToUpdate = new ArrayList<>();
		BufferedReader reader = new BufferedReader(new FileReader(new File(file)));
		//Read header
		String line = "";
		int nb=0;
		while ((line = reader.readLine()) != null) {
			String sampleCode = line;
			logger.debug("Sample code#"+sampleCode+"#");
			//Get readSet 
			List<ReadSet> readSets = MongoDBDAO.find(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, 
										DBQuery.is("sampleCode", sampleCode).is("state.code", "UA").lessThan("runSequencingStartDate", date)).toList();
			logger.debug("Size after "+readSets.size());
			readsetsToUpdate.addAll(readSets);
			for(ReadSet readSet : readSets){
				logger.debug("ReadSet "+readSet.code);
				logger.debug("Date "+readSet.runSequencingStartDate);
			}
			nb++;
		}
		reader.close();
		logger.debug("Nb Sample "+nb);
		logger.debug("Nb readsets to update "+readsetsToUpdate.size());
		return readsetsToUpdate;
	}

	
	
	private static void backUpReadSets(List<ReadSet> readSets) {
		String backupName = InstanceConstants.READSET_ILLUMINA_COLL_NAME+"_BCK_SUPSQ2036_"+sdf.format(new java.util.Date());
		logger.info("\tCopie "+InstanceConstants.READSET_ILLUMINA_COLL_NAME+" to "+backupName+" start");

		MongoDBDAO.save(backupName, readSets);
		logger.info("\tCopie "+InstanceConstants.READSET_ILLUMINA_COLL_NAME+" to "+backupName+" end");
	}
	
}
