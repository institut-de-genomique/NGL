package controllers.migration;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import models.laboratory.container.instance.Container;
import models.laboratory.container.instance.Content;
import models.laboratory.processes.instance.Process;
import models.utils.InstanceConstants;
import models.utils.InstanceHelpers;

import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;

import play.Logger;
import play.mvc.Result;

import com.mongodb.MongoException;

import controllers.CommonController;
import fr.cea.ig.MongoDBDAO;

public class AddSampleOnInputContainerInProcess extends CommonController {

	private static final Date date = new Date();
	private static final DateFormat df = new SimpleDateFormat("ddMMyy'_'hhmm");
	private static final String today = df.format(date);
	private static final String PROCESS_COLL_NAME_BCK = InstanceConstants.PROCESS_COLL_NAME + "_BCK" + "_" + today;

	public static Result migration(){
		List<Process> processesCallBack = MongoDBDAO.find(PROCESS_COLL_NAME_BCK, Process.class).toList();
		if (processesCallBack.size() == 0) {
			Logger.info(">>>>>>>>>>> Migration to add content in process starts");
			backupProcessCollection();
			AddSampleOnInputContainer();
			Logger.info(">>>>>>>>>>> Migration to add content in process end");
		} else {
			Logger.info(">>>>>>>>>>> Migration to add content in process already execute !");
		}
		Logger.info(">>>>>>>>>>> Migration to add content in process finish");
		return ok(">>>>>>>>>>> Migration to add content in process finish");
	}

	private static void AddSampleOnInputContainer(){
		List<Process> processes = MongoDBDAO.find(InstanceConstants.PROCESS_COLL_NAME, Process.class, DBQuery.is("typeCode", "opgen-run")).toList();
		Logger.debug("Migre " + processes.size() + " PROCESSES");
		for (Process process : processes) {
			Container container = MongoDBDAO.findByCode(InstanceConstants.CONTAINER_COLL_NAME, Container.class,
					process.inputContainerCode);
			Logger.debug("Container Code="+ container.code);
			for (Content content : container.contents) {
				if ((process.sampleCodes).equals(content.sampleCode)){
					process.sampleOnInputContainer = InstanceHelpers.getSampleOnInputContainer(content,container);
				}
			}
			try {
				MongoDBDAO.update(InstanceConstants.PROCESS_COLL_NAME, Process.class, DBQuery.is("code", process.code),
						DBUpdate.set("sampleOnInputContainer", process.sampleOnInputContainer));
			} catch (MongoException e){
				Logger.error("MongoException type error !");
			}
		}
	}

	private static void backupProcessCollection(){
		Logger.info("\tCopie " + InstanceConstants.PROCESS_COLL_NAME + " start");
		MongoDBDAO.save(PROCESS_COLL_NAME_BCK, MongoDBDAO.find(InstanceConstants.PROCESS_COLL_NAME, Process.class)
				.toList());
		Logger.info("\tCopie " + InstanceConstants.PROCESS_COLL_NAME + " end");
	}

}
