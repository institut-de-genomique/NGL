package scripts;

import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.lfw.controllers.scripts.buffered.ScriptWithExcelBody;
import models.laboratory.run.instance.ReadSet;
import models.laboratory.run.instance.SampleOnContainer;
import models.utils.InstanceConstants;
import models.utils.InstanceHelpers;

public class AddSampleOnContainerReadSetCode extends ScriptWithExcelBody{

	

	@Override
	public void execute(XSSFWorkbook workbook) throws Exception {
		XSSFSheet sheetDataReadSet = workbook.getSheetAt(0);
		sheetDataReadSet.rowIterator().forEachRemaining(row -> {
			if(row.getRowNum() == 0) return; // skip header
			String codeReadSet = row.getCell(0).getStringCellValue();
			getLogger().debug("Code "+codeReadSet);
			ReadSet readSet = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, codeReadSet);
			SampleOnContainer soc = InstanceHelpers.getSampleOnContainer(readSet);
			readSet.sampleOnContainer=soc;
			MongoDBDAO.update(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, 
					DBQuery.is("code", readSet.code),
					DBUpdate.set("sampleOnContainer", soc));
		});
			
	}


}
