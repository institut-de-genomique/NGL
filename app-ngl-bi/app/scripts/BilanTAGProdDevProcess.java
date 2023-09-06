package scripts;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.mongojack.DBQuery;
import org.mongojack.DBQuery.Query;

import com.mongodb.BasicDBObject;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.lfw.controllers.scripts.buffered.ScriptNoArgs;
import models.laboratory.container.instance.Container;
import models.laboratory.processes.instance.Process;
import models.laboratory.run.instance.ReadSet;
import models.utils.InstanceConstants;
import play.Logger;
import validation.ContextValidation;

public class BilanTAGProdDevProcess extends ScriptNoArgs{

	final String user = "ngl-support";

	@Override
	@SuppressWarnings("unchecked")
	public void execute() throws Exception {

		ContextValidation cv = ContextValidation.createUpdateContext(user);
		cv.putObject("RecapReadSet", new ArrayList<String>());
		//Add Header Recap
		((List<String>)cv.getObject("RecapReadSet")).add("Code readset,code sample, type run, run, code flowcell, Date, Etat, tag readset, Processus, categorie processus, type processus, Tag processus");

		BasicDBObject keys = new BasicDBObject();
		keys.put("treatments", 0);
		Logger.debug("Get all processes with tag prod/dev");
		
		//ProjectionBuilder dbProject = DBProjection.include("code");
		//Pipeline<Expression<?>> pipeline = Aggregation.match(DBQuery.exists("outputContainerCodes")).project(dbProject);
		//List<Process> codeProcesses = MongoDBDAO.getCollection(InstanceConstants.PROCESS_COLL_NAME, Process.class).aggregate(pipeline,Process.class).results();
		
		BasicDBObject keysProcess = new BasicDBObject();
		keysProcess.put("code", 1);
		keysProcess.put("sampleCodes", 1);
		keysProcess.put("properties.devProdContext", 1);
		keysProcess.put("outputContainerCodes", 1);
		keysProcess.put("inputContainerCode", 1);
		keysProcess.put("categoryCode", 1);
		keysProcess.put("typeCode", 1);
		keysProcess.put("sampleOnInputContainer.properties.tag", 1);
		keysProcess.put("sampleOnInputContainer.properties.devProdContext", 1);
		//Get all processes with tag prod/dev
		List<Process> processes = MongoDBDAO.find(InstanceConstants.PROCESS_COLL_NAME, Process.class, DBQuery.exists("properties.devProdContext"),keysProcess).toList();
		List<Process> processesInSampleContainer = MongoDBDAO.find(InstanceConstants.PROCESS_COLL_NAME, Process.class, DBQuery.notExists("properties.devProdContext").exists("sampleOnInputContainer.properties.devProdContext"),keysProcess).toList();
		processes.addAll(processesInSampleContainer);
		for(Process p : processes) {
			String tagDEVPROD = null;
			if(p.properties!=null && p.properties.containsKey("devProdContext"))
				tagDEVPROD=(String) p.properties.get("devProdContext").getValue();
			else {
				Logger.debug("FIND TAG in SAMPLECONTAINER");
				tagDEVPROD=(String) p.sampleOnInputContainer.properties.get("devProdContext").getValue();
			}
			Logger.debug("Process "+p.code+" tagContext "+tagDEVPROD);
			//Recherche readset avec tag different
			if(p.outputContainerCodes==null)
				p.outputContainerCodes=new HashSet<String>();
			Query dbQuery = DBQuery.in("sampleCode", p.sampleCodes)
									.exists("sampleOnContainer.properties.devProdContext")
									.notEquals("sampleOnContainer.properties.devProdContext.value",tagDEVPROD)
									.or(DBQuery.in("sampleOnContainer.containerCode",p.inputContainerCode), DBQuery.in("sampleOnContainer.containerCode",p.outputContainerCodes));
			List<ReadSet> readsets = MongoDBDAO.find(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, dbQuery,keys).toList();
			if(readsets.size()==1) {
				ReadSet rs = readsets.get(0);
				Logger.debug("FIND READSET "+rs.code+" tagContext "+rs.sampleOnContainer.properties.get("devProdContext").getValue());
				((List<String>)cv.getObject("RecapReadSet")).add(rs.code+","+rs.sampleCode+","+rs.runTypeCode+","+rs.runCode+","+rs.sampleOnContainer.containerCode+","+rs.traceInformation.creationDate+","+rs.state.code+","+rs.sampleOnContainer.properties.get("devProdContext").getValue()+","+p.code+","+p.categoryCode+","+p.typeCode+","+tagDEVPROD);
			}else if(readsets.size()>1 && p.sampleOnInputContainer.properties.containsKey("tag")) {
				for(ReadSet rs : readsets) {
					if(rs.sampleOnContainer.properties.get("tag").getValue().equals(p.sampleOnInputContainer.properties.get("tag").getValue())) {
						Logger.debug("FIND READSET WITH TAG "+rs.code+" tagContext "+rs.sampleOnContainer.properties.get("devProdContext").getValue());
						((List<String>)cv.getObject("RecapReadSet")).add(rs.code+","+rs.sampleCode+","+rs.runTypeCode+","+rs.runCode+","+rs.sampleOnContainer.containerCode+","+rs.traceInformation.creationDate+","+rs.state.code+","+rs.sampleOnContainer.properties.get("devProdContext").getValue()+","+p.code+","+p.categoryCode+","+p.typeCode+","+tagDEVPROD);
					}
				}
			}else {
				for(ReadSet rs : readsets) {
					Logger.debug("FIND READSET OTHER "+rs.code+" tagContext "+rs.sampleOnContainer.properties.get("devProdContext").getValue());
					((List<String>)cv.getObject("RecapReadSet")).add(rs.code+","+rs.sampleCode+","+rs.runTypeCode+","+rs.runCode+","+rs.sampleOnContainer.containerCode+","+rs.traceInformation.creationDate+","+rs.state.code+","+rs.sampleOnContainer.properties.get("devProdContext").getValue()+","+p.code+","+p.categoryCode+","+p.typeCode+","+tagDEVPROD);
				}
			}
			
		}


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
		try (OutputStream fileOut = new FileOutputStream("outputScript_recapTagProdDEVProcess.xls")) {
			wb.write(fileOut);
			fileOut.flush();
		}catch(Exception e){
			Logger.debug(e.getMessage());
		}

	}
}
