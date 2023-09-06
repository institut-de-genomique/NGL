package scripts;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.inject.Inject;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.lfw.controllers.scripts.buffered.ScriptWithExcelBody;
import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.ngl.NGLConfig;
import models.Constants;
import models.laboratory.sample.instance.Sample;
import models.utils.InstanceConstants;
import play.Logger;
import services.instance.sample.UpdateReportingData;
import validation.ContextValidation;

/**
 * Call reportingData for one sample
 * ReportingData compute statistics on sample 
 * @author ejacoby
 *
 */
public class ScriptUpdateReportingDataWithSampleListFile extends ScriptWithExcelBody{

	
	private final NGLApplication app;

	private final NGLConfig config;

	@Inject
	public ScriptUpdateReportingDataWithSampleListFile(NGLApplication app, NGLConfig config) {
		this.app = app;
		this.config = config;
	}


	@Override
	public void execute(XSSFWorkbook workbook) throws Exception {
		ContextValidation contextError = ContextValidation.createUndefinedContext(Constants.NGL_DATA_USER);
		contextError.addKeyToRootKeyName("import");
		Date date = new Date();
		List<Sample> samples = new ArrayList<Sample>();
		workbook.getSheetAt(0).rowIterator().forEachRemaining(row -> {
			if(row.getRowNum() == 0) return; // skip header
			
			if(row!=null && row.getCell(0)!=null){
				String sampleCode = row.getCell(0).getStringCellValue();
				Sample sample = MongoDBDAO.findByCode(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, sampleCode);
				samples.add(sample);
			}
		});
		Logger.debug("Nb sample to update "+samples.size());
		for(Sample sampleToUpdate: samples) {
			Logger.debug("START update sample "+sampleToUpdate.code);
			UpdateReportingData update = new UpdateReportingData(app, config);
			update.updateProcesses(sampleToUpdate);						
			Logger.debug(new Date()+"sample process created "+sampleToUpdate.code);
			if (sampleToUpdate.processes != null && sampleToUpdate.processes.size() > 0) {
				MongoDBDAO.update(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, DBQuery.is("code", sampleToUpdate.code), 
						DBUpdate.set("processes", sampleToUpdate.processes).set("processesStatistics", sampleToUpdate.processesStatistics).set("processesUpdatedDate", date));
			} else {
				MongoDBDAO.update(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, DBQuery.is("code", sampleToUpdate.code), 
						DBUpdate.unset("processes").unset("processesStatistics").set("processesUpdatedDate", date));

			}
			Logger.debug(new Date()+"end update sample "+sampleToUpdate.code);
		}
		Logger.debug("End update samples");
		
	}


}
