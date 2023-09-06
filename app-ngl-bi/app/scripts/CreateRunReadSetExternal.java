package scripts;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.collections4.map.HashedMap;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;

import com.mongodb.MongoException;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.lfw.controllers.scripts.buffered.ScriptWithExcelBody;
import fr.cea.ig.ngl.dao.readsets.ReadSetsDAO;
import fr.cea.ig.ngl.dao.runs.RunsDAO;
import fr.cea.ig.ngl.dao.samples.SamplesAPI;
import models.laboratory.common.instance.PropertyValue;
import models.laboratory.common.instance.State;
import models.laboratory.common.instance.TBoolean;
import models.laboratory.common.instance.TraceInformation;
import models.laboratory.common.instance.Valuation;
import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.run.instance.File;
import models.laboratory.run.instance.InstrumentUsed;
import models.laboratory.run.instance.ReadSet;
import models.laboratory.run.instance.Run;
import models.laboratory.run.instance.SampleOnContainer;
import models.laboratory.sample.instance.Sample;
import models.utils.InstanceConstants;
import models.utils.dao.DAOException;
import validation.ContextValidation;
/**
 * Attention à adapter selon support
 * Script to create external Run et ReadSet from excel file for NGL-2563
 * First sheet name=dataRun excel file columns
 * Techno | Instrument | Type instrument | Type Run | code Run | date Run | code FC | libProcessTypeCode
 * Second sheet name=dataReadSet
 * Code projet | code echantillon | techno | run code | date run | code flowcell | tag | code readSet | pourcentage | rawData path
 * Third sheet name=properties : properties list to add to sampleOnContainer in ReadSet from Sample
 * @author ejacoby
 *
 */
public class CreateRunReadSetExternal extends ScriptWithExcelBody{

	private final RunsDAO runsDAO;
	private final ReadSetsDAO readsetsDAO;
	private final SamplesAPI samplesAPI;

	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
	@Inject
	public CreateRunReadSetExternal(RunsDAO runsDAO,ReadSetsDAO readsetsDAO, SamplesAPI samplesAPI) {
		this.runsDAO = runsDAO;
		this.readsetsDAO = readsetsDAO;
		this.samplesAPI = samplesAPI;
	}

	@Override
	public void execute(XSSFWorkbook workbook) throws Exception {
		//Get list properties to add to sampleOnContainer
		XSSFSheet sheetProperties = workbook.getSheet("properties");
		List<String> propertieNames = new ArrayList<String>();
		sheetProperties.rowIterator().forEachRemaining(row -> {
			if(row.getRowNum() == 0) return; // skip header
			String propertiesValue  = row.getCell(0).getStringCellValue();
			propertieNames.add(propertiesValue);
		});

		List<String> runFailedCodes = new ArrayList<>();

		XSSFSheet sheetDataRun = workbook.getSheet("dataRun");
		sheetDataRun.rowIterator().forEachRemaining(row -> {
			if(row.getRowNum() == 0) return; // skip header
			//Get infos for run
			if(row.getPhysicalNumberOfCells()>7){
				String categoryRun = row.getCell(0).getStringCellValue();
				String codeInstrument = row.getCell(1).getStringCellValue();
				String typeCodeInstrument = row.getCell(2).getStringCellValue();
				String typeCodeRun = row.getCell(3).getStringCellValue();
				String codeRun = row.getCell(4).getStringCellValue();
				String runDateValue = row.getCell(5).getStringCellValue();
				String flowcellCode = row.getCell(6).getStringCellValue();
				String libProcessTypeCode = row.getCell(7).getStringCellValue();

				getLogger().debug("Create run category="+categoryRun+
						",codeInst="+codeInstrument+
						",typeCodeInst="+typeCodeInstrument+
						",typeCodeRun="+typeCodeRun+
						",codeRun="+codeRun+
						",runDate="+runDateValue+
						",flowcellCode="+flowcellCode+
						",libProcessTypeCode"+libProcessTypeCode);
				try {
					Date runDate = sdf.parse(runDateValue);
					//Create Run
					Run run=new Run();
					run.code=codeRun;
					run.categoryCode=categoryRun;
					run.typeCode=typeCodeRun;	
					run.sequencingStartDate=runDate;

					run.state=new State("F-V","ngl-admin");
					run.valuation=new Valuation("ngl-admin", TBoolean.TRUE);
					run.traceInformation=new TraceInformation();
					run.traceInformation.setTraceInformation("ngl-admin");

					run.containerSupportCode=flowcellCode;
					run.instrumentUsed=new InstrumentUsed();
					run.instrumentUsed.typeCode=typeCodeInstrument;
					run.instrumentUsed.code=codeInstrument;

					run.properties.put("libProcessTypeCodes", new PropertySingleValue(libProcessTypeCode));

					ContextValidation ctxVal = ContextValidation.createCreationContext("ngl-admin");
					ctxVal.putObject("external", true);
					run.validate(ctxVal);
					if(!ctxVal.hasErrors()){
						runsDAO.save(run);
					}else{
						getLogger().error("Run cannot be create " + codeRun);
						runFailedCodes.add(codeRun);
					}

				} catch (ParseException e) {
					getLogger().error("Run cannot be create " + codeRun);
					getLogger().debug(e.getMessage(), e);
					runFailedCodes.add(codeRun);
				}

			}
		});

		XSSFSheet sheetDataReadset = workbook.getSheet("dataReadset");
		sheetDataReadset.rowIterator().forEachRemaining(row -> {
			if(row.getRowNum() == 0) return; // skip header
			getLogger().debug("Nb cell "+row.getPhysicalNumberOfCells());
			if(row.getPhysicalNumberOfCells()>12){
				//Get infos for readset
				String projectCode = row.getCell(0).getStringCellValue();
				String sampleCode = row.getCell(1).getStringCellValue();
				String categoryRun = row.getCell(2).getStringCellValue();
				String typeCodeRun = row.getCell(3).getStringCellValue();
				String codeRun = row.getCell(4).getStringCellValue();
				String runDateValue = row.getCell(5).getStringCellValue();
				String codeFlowcell = row.getCell(6).getStringCellValue();
				String categoryTag =  row.getCell(7).getStringCellValue();
				String tag = row.getCell(8).getStringCellValue();
				String codeReadset = row.getCell(9).getStringCellValue();
				Double percentage = row.getCell(10).getNumericCellValue();
				String libProcessTypeCode = row.getCell(11).getStringCellValue();
				String pathRawData = row.getCell(12).getStringCellValue();


				String path = pathRawData.substring(0, pathRawData.lastIndexOf("/"));
				String fileName = pathRawData.substring(pathRawData.lastIndexOf("/")+1);
				String fileLabel = fileName.substring(0, fileName.lastIndexOf("."));

				getLogger().debug("Create readSet projectCode="+projectCode
						+",sampleCode="+sampleCode
						+",categoryRun="+categoryRun
						+",typeCodeRun="+typeCodeRun
						+",codeRun="+codeRun
						+",dateRun="+runDateValue
						+",codeFlowcell="+codeFlowcell
						+",codeReadSet="+codeReadset
						+",pathRawData="+pathRawData
						+",percentage="+percentage
						+",path="+path
						+",fileName="+fileName
						+",fileLabel="+fileLabel);

				try {
					Date runDate = sdf.parse(runDateValue);

					//Find sample 
					Sample sampleDB = samplesAPI.get(sampleCode);

					//Create ReadSet
					ReadSet readSet=new ReadSet();
					readSet.typeCode="rs"+categoryRun;
					readSet.code=codeReadset;

					readSet.state=new State("A","ngl-admin");
					readSet.submissionState = new State("NONE", "ngl-admin");

					readSet.runCode=codeRun;
					readSet.runTypeCode=typeCodeRun;
					readSet.runSequencingStartDate=runDate;
					readSet.laneNumber=1;
					readSet.sampleCode=sampleCode;
					readSet.projectCode=projectCode;
					readSet.path=path;

					readSet.location="CNS";	
					readSet.traceInformation=new TraceInformation();
					readSet.traceInformation.setTraceInformation("ngl-admin");
					readSet.productionValuation=new Valuation("ngl-admin", TBoolean.TRUE);
					readSet.bioinformaticValuation=new Valuation("ngl-admin", TBoolean.TRUE);

					//Create files
					readSet.files=new ArrayList<File>();
					File file = new File();
					file.fullname=fileName;
					file.extension="fastq";
					file.usable=true;
					file.typeCode="RAW";
					file.properties.put("label", new PropertySingleValue(fileLabel));
					file.properties.put("asciiEncoding", new PropertySingleValue("33"));
					readSet.files.add(file);

					//Create SampleOnContainer
					readSet.sampleOnContainer = new SampleOnContainer();
					readSet.sampleOnContainer.projectCode=projectCode;
					readSet.sampleOnContainer.sampleCode=sampleCode;
					readSet.sampleOnContainer.sampleTypeCode=sampleDB.typeCode;
					readSet.sampleOnContainer.sampleCategoryCode=sampleDB.categoryCode;
					readSet.sampleOnContainer.containerSupportCode=codeFlowcell;
					readSet.sampleOnContainer.containerCode=codeFlowcell;
					readSet.sampleOnContainer.percentage=percentage;
					readSet.sampleOnContainer.referenceCollab=sampleDB.referenceCollab;
					readSet.sampleOnContainer.taxonCode=sampleDB.taxonCode;
					readSet.sampleOnContainer.ncbiScientificName=sampleDB.ncbiScientificName;
					readSet.sampleOnContainer.lastUpdateDate=new Date();
					
					readSet.sampleOnContainer.properties=getProperties(propertieNames, sampleDB);
					//Add tag property
					readSet.sampleOnContainer.properties.put("tagCategory", new PropertySingleValue(categoryTag));
					readSet.sampleOnContainer.properties.put("tag", new PropertySingleValue(tag));
					//Add libProcessTypeCode
					readSet.sampleOnContainer.properties.put("libProcessTypeCode", new PropertySingleValue(libProcessTypeCode));
					//Add PROD Context
					readSet.sampleOnContainer.properties.put("devProdContext", new PropertySingleValue("PROD"));
					//Add from property
					readSet.sampleOnContainer.properties.put("fromSampleTypeCode", new PropertySingleValue(sampleDB.life.from.sampleTypeCode));
					readSet.sampleOnContainer.properties.put("fromSampleCode", new PropertySingleValue(sampleDB.life.from.sampleCode));
					readSet.sampleOnContainer.properties.put("fromProjectCode", new PropertySingleValue(sampleDB.life.from.projectCode));
					
					ContextValidation ctxVal = ContextValidation.createCreationContext("ngl-admin");
					readSet.validate(ctxVal);
					if(!ctxVal.hasErrors()){
						MongoDBDAO.update(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class
								,DBQuery.is("code",readSet.runCode)
								,DBUpdate.addToSet("sampleCodes", readSet.sampleCode).addToSet("projectCodes",readSet.projectCode));
						readsetsDAO.save(readSet);
						
					}else{
						getLogger().error("Cannot create Readset" + codeReadset);
						getLogger().debug(ctxVal.getErrors().toString());
					}
				} catch (DAOException e) {
					getLogger().error("ReadSet cannot be create " + codeReadset);
					getLogger().debug(e.getMessage(), e);
					runFailedCodes.add(codeReadset);
				} catch (MongoException e) {
					getLogger().error("ReadSet cannot be create " + codeReadset);
					getLogger().debug(e.getMessage(), e);
					runFailedCodes.add(codeReadset);
				} catch (ParseException e) {
					getLogger().error("ReadSet cannot be create " + codeReadset);
					getLogger().debug(e.getMessage(), e);
					runFailedCodes.add(codeReadset);
				}
			}
		});

	}

	private HashedMap<String,PropertyValue> getProperties(List<String> listPropertieNames, Sample sample)
	{
		HashedMap<String,PropertyValue> properties = new HashedMap<String,PropertyValue>();
		for(String name : listPropertieNames){
			if(sample.properties.containsKey(name)){
				properties.put(name, sample.properties.get(name));
			}
		}
		return properties;
	}
}
