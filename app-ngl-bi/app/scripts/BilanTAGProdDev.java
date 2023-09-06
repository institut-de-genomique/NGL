package scripts;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.mongojack.DBQuery;
import org.mongojack.DBQuery.Query;

import com.mongodb.BasicDBObject;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.MongoDBResult.Sort;
import fr.cea.ig.lfw.controllers.scripts.buffered.ScriptWithExcelBody;
import models.laboratory.container.instance.Container;
import models.laboratory.processes.instance.Process;
import models.laboratory.run.instance.ReadSet;
import models.utils.InstanceConstants;
import play.Logger;
import validation.ContextValidation;

public class BilanTAGProdDev extends ScriptWithExcelBody{



	final String user = "ngl-support";

	@Override
	@SuppressWarnings("unchecked")
	public void execute(XSSFWorkbook workbook) throws Exception {

		ContextValidation cv = ContextValidation.createUpdateContext(user);
		cv.putObject("RecapReadSet", new ArrayList<String>());
		//Add Header Recap
		((List<String>)cv.getObject("RecapReadSet")).add("Code readset,code sample, type run, run, code flowcell, Date, Etat, Tag projet, tag readset, Processus, categorie processus, type processus, Tag processus, Other Processus, categorie other processus, type other processus, tag other processus");

		BasicDBObject keys = new BasicDBObject();
		keys.put("treatments", 0);

		workbook.getSheetAt(0).rowIterator().forEachRemaining(row -> {
			if(row.getRowNum() == 0) return; // skip header
			if(row!=null && row.getCell(0)!=null && row.getCell(1)!=null){
				String codeProjet = row.getCell(0).getStringCellValue();
				String  tagValue= row.getCell(1).getStringCellValue();

				Logger.debug("Get readset for project "+codeProjet);
				String findTagValue="DEV";
				if(tagValue.equals("DEV")) {
					findTagValue="PROD";
				}
				Logger.debug("Get readset for project "+codeProjet+" with tag "+findTagValue);
				//Get readSet incoherent
				List<ReadSet> readsets = MongoDBDAO.find(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, DBQuery.is("projectCode", codeProjet).is("sampleOnContainer.properties.devProdContext.value", findTagValue),keys).toList();
				for(ReadSet rs : readsets) {
					Logger.debug("Code readset "+rs.code);
					Logger.debug(rs.code+","+rs.sampleCode);
					Logger.debug("Get prod/dev "+rs.sampleOnContainer.properties.get("devProdContext").getValue());			
					//Get processus
					Process process = getProcess(rs);

					String recap = rs.code+","+rs.sampleCode+","+rs.runTypeCode+","+rs.runCode
							+","+rs.sampleOnContainer.containerCode+","+rs.traceInformation.creationDate+","+rs.state.code
							+","+tagValue+","+rs.sampleOnContainer.properties.get("devProdContext").getValue();

					if(process!=null) {
						Logger.debug("Process code "+process.code);
						Logger.debug("PROD/DEV process "+process.sampleOnInputContainer);
						recap+=","+process.code+","+process.categoryCode+","+process.typeCode;
						if(process.properties.containsKey("devProdContext")) {
							recap+=","+process.properties.get("devProdContext").getValue();
						}else {
							recap+=",NONE";
						}
						//Get other processus from inital container of processus order by date
						List<Process> otherProcess = MongoDBDAO.find(InstanceConstants.PROCESS_COLL_NAME, Process.class, DBQuery.or(DBQuery.in("inputContainerCode",process.sampleOnInputContainer.containerCode), DBQuery.in("outputContainerCodes",process.sampleOnInputContainer.containerCode))).sort("traceInformation.creationDate",Sort.ASC).toList();
						String otherProcessCode ="";
						String otherProcessCategory ="";
						String otherProcessType="";
						String otherProcessTag ="";
						for(Process p : otherProcess) {
							otherProcessCode+=p.code+";";
							otherProcessCategory+=p.categoryCode+";";
							otherProcessType+=p.typeCode+";";
							if(p.properties!=null && p.properties.containsKey("devProdContext"))
								otherProcessTag+=p.properties.get("devProdContext").getValue()+";";
						}
						recap+=","+otherProcessCode+","+otherProcessCategory+","+otherProcessType+","+otherProcessTag;
					}else {
						Logger.debug("No Process code");


						//Get multiple process with outputContainerCode and tag
						List<Process> processes = getMultipleProcess(rs);
						String listContainerIn ="";
						if(processes.size()>0) {
							String allProcessCode ="";
							String allProcessCategory="";
							String allProcessType="";
							String allProcessDevProd="";
							for(Process p : processes) {
								allProcessCode+=p.code+";";
								allProcessCategory+=p.categoryCode+";";
								allProcessType+=p.typeCode+";";
								if(p.properties.containsKey("devProdContext")) {
									allProcessDevProd+=p.properties.get("devProdContext").getValue()+";";
								}else {
									allProcessDevProd+="NONE;";
								}
								listContainerIn+=p.inputContainerCode+",";
							}
							recap+=","+allProcessCode+","+allProcessCategory+","+allProcessType+","+allProcessDevProd;
							if(!listContainerIn.equals("")) {
								listContainerIn=listContainerIn.substring(0, listContainerIn.length()-1);
								//Get other processus from inital container of processus order by date
								List<Process> otherProcess = MongoDBDAO.find(InstanceConstants.PROCESS_COLL_NAME, Process.class, DBQuery.or(DBQuery.in("inputContainerCode",listContainerIn), DBQuery.in("outputContainerCodes",listContainerIn))).sort("traceInformation.creationDate",Sort.ASC).toList();
								String otherProcessCode ="";
								String otherProcessCategory ="";
								String otherProcessType="";
								String otherProcessTag ="";
								for(Process p : otherProcess) {
									otherProcessCode+=p.code+";";
									otherProcessCategory+=p.categoryCode+";";
									otherProcessType+=p.typeCode+";";
									if(p.properties!=null && p.properties.containsKey("devProdContext"))
										otherProcessTag+=p.properties.get("devProdContext").getValue()+";";
								}
								recap+=","+otherProcessCode+","+otherProcessCategory+","+otherProcessType+","+otherProcessTag;
							}

						}else {
							recap+=",NONE,NONE,NONE,NONE";
						}

					}


					((List<String>)cv.getObject("RecapReadSet")).add(recap);

				}
			}
		});

		//Create excel file to recap in execution directory
		createExcelFileRecap(cv);


		Logger.info("End of recapTagProdDEV");
	}


	private Process getProcess(ReadSet rs)
	{
		Query dbQuery = DBQuery.in("sampleCodes", rs.sampleCode).or(DBQuery.in("inputContainerCode",rs.sampleOnContainer.containerCode), DBQuery.in("outputContainerCodes",rs.sampleOnContainer.containerCode));
		List<Process> process = MongoDBDAO.find(InstanceConstants.PROCESS_COLL_NAME, Process.class, dbQuery).toList();

		if(process.size()==1) {
			return process.get(0);
		}else if(process.size()>1) {
			if(rs.sampleOnContainer.properties.containsKey("tag")) {
				dbQuery.is("sampleOnInputContainer.properties.tag.value", rs.sampleOnContainer.properties.get("tag").getValue());
			}
			process=MongoDBDAO.find(InstanceConstants.PROCESS_COLL_NAME, Process.class, dbQuery).toList();
			if(process.size()==1) {
				return process.get(0);
			}else if(process.size()>1){
				return null;
			}else {
				return null;
			}
		}else {
			return null;
		}


	}

	List<Process> getMultipleProcess(ReadSet rs)
	{
		
		Query dbQuery = DBQuery.in("sampleCodes", rs.sampleCode).or(DBQuery.in("inputContainerCode",rs.sampleOnContainer.containerCode), DBQuery.in("outputContainerCodes",rs.sampleOnContainer.containerCode));
		//Get all processus
		List<Process> processes = MongoDBDAO.find(InstanceConstants.PROCESS_COLL_NAME, Process.class, dbQuery).toList();
		if(rs.sampleOnContainer.properties.containsKey("tag")) {
			String tag = (String) rs.sampleOnContainer.properties.get("tag").getValue();
			List<Process> finalProcess = new ArrayList<Process>();
			for(Process process: processes) {
				//Get all outputContainerCode check container tag property
				for(String codeContainer : process.outputContainerCodes) {
					Container container = MongoDBDAO.findByCode(InstanceConstants.CONTAINER_COLL_NAME, Container.class, codeContainer);
					if(container.contents.size()==1 && container.contents.get(0).properties.containsKey("tag") && container.contents.get(0).properties.get("tag").getValue().equals(tag)) {
						finalProcess.add(process);
						break;
					}
				}
			}
			return finalProcess;
		}else {
			return processes;
		}
	}

	/**
	 * Create excel file in execution directory
	 * File recap : list all objects updated
	 * @param cv
	 */
	@SuppressWarnings("unchecked")
	private void createExcelFileRecap(ContextValidation cv)
	{
		Workbook wb = new HSSFWorkbook();
		CreationHelper createHelper = wb.getCreationHelper();
		for(String key : cv.getContextObjects().keySet()){
			Sheet sheet = wb.createSheet(key);
			List<String> recaps = (List<String>) cv.getObject(key);
			int nbLine=0;
			for(String recap : recaps){
				//	Logger.debug("recap: "+recap);
				Row row = sheet.createRow(nbLine);
				String[] tabRecap = recap.split(",");
				for(int i=0;i<tabRecap.length;i++){
					row.createCell(i).setCellValue(
							createHelper.createRichTextString(tabRecap[i]));
				}
				nbLine++;
			}
		}

		// Write the output to a file
		try (OutputStream fileOut = new FileOutputStream("outputScript_recapTagProdDEV.xls")) {
			wb.write(fileOut);
			fileOut.flush();
		}catch(Exception e){
			Logger.debug(e.getMessage());
		}

	}
}
