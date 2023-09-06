package scripts;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
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
import models.laboratory.common.instance.Valuation;
import models.laboratory.common.instance.property.PropertyListValue;
import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.run.instance.Lane;
import models.laboratory.run.instance.ReadSet;
import models.laboratory.run.instance.Run;
import models.laboratory.sample.instance.Sample;
import models.utils.InstanceConstants;
import models.utils.dao.DAOException;
import validation.ContextValidation;
/**
 * Attention à adapter selon support
 * Script to update Run et ReadSet from excel file for NGL-2563
 * First sheet name=dataRun excel file columns
 * OriginCodeRun | Code | libProcessTypeCode
 * Second sheet name=dataReadSet
 * Code projet | code echantillon | techno | run code | date run | code flowcell | tag | code readSet | pourcentage | rawData path
 * Third sheet name=properties : properties list to add to sampleOnContainer in ReadSet from Sample
 * @author ejacoby
 *
 */
public class UpdateRunReadSetExternal extends ScriptWithExcelBody{

	private final RunsDAO runsDAO;
	private final ReadSetsDAO readsetsDAO;
	private final SamplesAPI samplesAPI;

	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
	@Inject
	public UpdateRunReadSetExternal(RunsDAO runsDAO,ReadSetsDAO readsetsDAO, SamplesAPI samplesAPI) {
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
			if(row.getPhysicalNumberOfCells()>3){
				String originCodeRun = row.getCell(0).getStringCellValue();
				String code = row.getCell(1).getStringCellValue();
				String libProcessTypeCode = row.getCell(2).getStringCellValue();
				String runDateValue = row.getCell(3).getStringCellValue();

				getLogger().debug("Create run origin="+originCodeRun+
						",code="+code+
						",libProcessTypeCode="+libProcessTypeCode);
				//Get Run to update
				Run runOrigin = MongoDBDAO.findByCode(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, originCodeRun);
				Run run = MongoDBDAO.findByCode(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, code);
				if(run!=null && runOrigin!=null){
					try {
						//Create Run
						Date runDate = sdf.parse(runDateValue);
						run.sequencingStartDate=runDate;
						run.state=new State("F-V","ngl-admin");
						run.valuation=new Valuation("ngl-admin", TBoolean.TRUE);
						run.deleted=Boolean.TRUE;
						List<String> listProcessTypeCodes = Arrays.asList(libProcessTypeCode);
						run.properties.put("libProcessTypeCodes", new PropertyListValue(listProcessTypeCodes));

						//Add properties for submission 
						run.properties.put("sequencingProgramType", runOrigin.properties.get("sequencingProgramType"));

						//Validate lane
						for(Lane lane : run.lanes){
							lane.valuation=new Valuation("ngl-admin", TBoolean.TRUE);
						}
						
						ContextValidation ctxVal = ContextValidation.createUpdateContext("ngl-admin");
						ctxVal.putObject("external", true);
						run.validate(ctxVal);
						if(!ctxVal.hasErrors()){
							run.treatments.put("ngsrg", runOrigin.treatments.get("ngsrg"));
							run.lanes.get(0).treatments.put("ngsrg", runOrigin.lanes.get(0).treatments.get("ngsrg"));
							runsDAO.update(run);
						}else{
							getLogger().error("Run cannot be create " + code);
							runFailedCodes.add(code);
						}
					} catch (DAOException e) {
						getLogger().error("Run cannot be create " + code);
						runFailedCodes.add(code);
					} catch (ParseException e) {
						getLogger().error("Run cannot be create " + code);
						runFailedCodes.add(code);
					}
				}else{
					getLogger().error("Run cannot be create " + code);
					runFailedCodes.add(code);
				}

			}
		});

		XSSFSheet sheetDataReadset = workbook.getSheet("dataReadset");
		sheetDataReadset.rowIterator().forEachRemaining(row -> {
			if(row.getRowNum() == 0) return; // skip header
			getLogger().debug("Nb cell "+row.getPhysicalNumberOfCells());
			if(row.getPhysicalNumberOfCells()>10){
				//Get infos for readset
				String sampleCode = row.getCell(0).getStringCellValue();
				String categoryRun = row.getCell(1).getStringCellValue();
				String categoryTag = row.getCell(2).getStringCellValue();;
				String tag = row.getCell(3).getStringCellValue();
				String comment = row.getCell(4).getStringCellValue();;
				Double percentage = row.getCell(5).getNumericCellValue();
				Integer libLayoutNominalLength =  (int) row.getCell(6).getNumericCellValue();
				String libProcessTypeCodes = row.getCell(7).getStringCellValue();
				String dateValue = row.getCell(8).getStringCellValue();
				Integer nbUsefulCycleRead1 = (int) row.getCell(9).getNumericCellValue();
				Integer nbUsefulCycleRead2 = (int) row.getCell(10).getNumericCellValue();

			getLogger().debug("Create readSet "
						+",sampleCode="+sampleCode
						+",categoryRun="+categoryRun
						+",percentage="+percentage);

				try {
					//Find sample 
					Sample sampleDB = samplesAPI.get(sampleCode);
					ReadSet readSet = MongoDBDAO.findOne(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, DBQuery.is("sampleCode", sampleDB.code).is("typeCode", "rs"+categoryRun));
					if(readSet!=null){
						//Update ReadSet
						Date runDate = sdf.parse(dateValue);
						readSet.runSequencingStartDate=runDate;
						readSet.state=new State("A","ngl-admin");
						readSet.productionValuation=new Valuation("ngl-admin", TBoolean.TRUE);
						readSet.productionValuation.comment=comment;
						readSet.bioinformaticValuation=new Valuation("ngl-admin", TBoolean.TRUE);
						
						//Create SampleOnContainer
						readSet.sampleOnContainer.sampleTypeCode=sampleDB.typeCode;
						readSet.sampleOnContainer.sampleCategoryCode=sampleDB.categoryCode;
						readSet.sampleOnContainer.percentage=percentage;
						readSet.sampleOnContainer.referenceCollab=sampleDB.referenceCollab;
						readSet.sampleOnContainer.taxonCode=sampleDB.taxonCode;
						readSet.sampleOnContainer.ncbiScientificName=sampleDB.ncbiScientificName;
						readSet.sampleOnContainer.lastUpdateDate=new Date();

						readSet.sampleOnContainer.properties=getProperties(propertieNames, sampleDB);
						//Add tag property
						readSet.sampleOnContainer.properties.put("tagCategory", new PropertySingleValue(categoryTag));
						readSet.sampleOnContainer.properties.put("tag", new PropertySingleValue(tag));
						//Add PROD Context
						readSet.sampleOnContainer.properties.put("devProdContext", new PropertySingleValue("PROD"));
						//Add libLayoutNominalLength
						readSet.sampleOnContainer.properties.put("libLayoutNominalLength", new PropertySingleValue(libLayoutNominalLength));
						//Add libProcessTypeCode
						readSet.sampleOnContainer.properties.put("libProcessTypeCode", new PropertySingleValue(libProcessTypeCodes));
						//Add from property
						readSet.sampleOnContainer.properties.put("fromSampleTypeCode", new PropertySingleValue(sampleDB.life.from.sampleTypeCode));
						readSet.sampleOnContainer.properties.put("fromSampleCode", new PropertySingleValue(sampleDB.life.from.sampleCode));
						readSet.sampleOnContainer.properties.put("fromProjectCode", new PropertySingleValue(sampleDB.life.from.projectCode));

						//Add nbUsefulCycleRead
						readSet.treatments.get("ngsrg").results.get("default").put("nbUsefulCycleRead1", new PropertySingleValue(nbUsefulCycleRead1));
						readSet.treatments.get("ngsrg").results.get("default").put("nbUsefulCycleRead2", new PropertySingleValue(nbUsefulCycleRead2));
						
						ContextValidation ctxVal = ContextValidation.createUpdateContext("ngl-admin");
						readSet.validate(ctxVal);
						if(!ctxVal.hasErrors()){
							MongoDBDAO.update(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class
									,DBQuery.is("code",readSet.runCode)
									,DBUpdate.addToSet("sampleCodes", readSet.sampleCode).addToSet("projectCodes",readSet.projectCode));
							readsetsDAO.update(readSet);
						}else{
							getLogger().error("Cannot create Readset" + readSet.code);
							getLogger().debug(ctxVal.getErrors().toString());
						}
					}else{
						getLogger().error("Cannot create Readset for sample " + sampleCode);
					}
				} catch (DAOException e) {
					getLogger().error("ReadSet cannot be create " + sampleCode);
					getLogger().debug(e.getMessage(), e);
					runFailedCodes.add(sampleCode);
				} catch (MongoException e) {
					getLogger().error("ReadSet cannot be create " + sampleCode);
					getLogger().debug(e.getMessage(), e);
					runFailedCodes.add(sampleCode);
				} catch (ParseException e) {
					getLogger().error("ReadSet cannot be create " + sampleCode);
					getLogger().debug(e.getMessage(), e);
					runFailedCodes.add(sampleCode);

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
