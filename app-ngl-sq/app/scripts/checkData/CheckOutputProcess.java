package scripts.checkData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.mongojack.Aggregation;
import org.mongojack.DBProjection;
import org.mongojack.Aggregation.Pipeline;
import org.mongojack.DBProjection.ProjectionBuilder;

import org.mongojack.DBQuery;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.lfw.controllers.scripts.chunked.ScriptWithArgs;
import models.laboratory.container.instance.Container;
import models.laboratory.processes.instance.Process;
import models.utils.InstanceConstants;
import play.Logger;
import scripts.ProcessPropertiesContentHelper;
import validation.ContextValidation;

/**
 * Script permettant de vérifier la cohérence des données après un support (NGL-2803)
 * TODO : a terme regrouper l'ensemble des scripts de vérification à lancer après un ticket de support
 * 
 * Argument : typeCode : filtre sur les typeCode processus 
 * mettre valeur=all si on veut lancer sur l'ensemble des processus de la base de données
 * @author ejacoby
 *
 */
public class CheckOutputProcess extends ScriptWithArgs<CheckOutputProcess.Args>{

	final String user = "ngl-support";

	public static class Args {
		public String[] typeCodes;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void execute(Args args) throws Exception {
		ContextValidation validation = ContextValidation.createUpdateContext(user);
		validation.putObject("RecapBadProcess", new ArrayList<String>());
		validation.putObject("RecapErrorData", new ArrayList<String>());
		((List<String>)validation.getObject("RecapBadProcess")).add("Code process,code bad container");
		((List<String>)validation.getObject("RecapErrorData")).add("Code process,message");
		//Récupérer les processus
		for(int i=0; i<args.typeCodes.length; i++) {
			Logger.debug("GET PROCESSUS FOR TYPE CODE "+args.typeCodes[i]);
			List<Process> codeProcesses = new ArrayList<Process>();
			ProjectionBuilder dbProject = DBProjection.include("code");
			Pipeline<Aggregation.Expression<?>> pipeline =null;
			if(args.typeCodes[i].equals("all")) {
				pipeline = Aggregation.match(DBQuery.exists("outputContainerCodes")).project(dbProject);
			}else {
				pipeline = Aggregation.match(DBQuery.is("typeCode", args.typeCodes[i])).project(dbProject);
			}
			Iterator<Process> it = MongoDBDAO.aggregate(InstanceConstants.PROCESS_COLL_NAME, Process.class, pipeline).iterator();
			it.forEachRemaining((s) -> codeProcesses.add(s));

			//Logger.debug("Nb Process "+processes.size());
			Logger.debug("Nb Process "+codeProcesses.size());
			int nb=1;
			for(Process codeProcess : codeProcesses) {
				Logger.debug("Process "+codeProcess.code+" "+nb+"/"+codeProcesses.size());
				Process process = MongoDBDAO.findByCode(InstanceConstants.PROCESS_COLL_NAME, Process.class, codeProcess.code);
				//Get list code container to check input and output
				List<String> codeContainerToCheck = new ArrayList<String>();
				if(process.outputContainerCodes!=null) {
					codeContainerToCheck.addAll(process.outputContainerCodes);
					codeContainerToCheck.add(process.inputContainerCode);
					List<String> badCodeContainers = new ArrayList<String>();
					for(String codeContainerOut : process.outputContainerCodes) {
						//Get treeOfLife of each container
						Container containerOut = MongoDBDAO.findByCode(InstanceConstants.CONTAINER_COLL_NAME, Container.class, codeContainerOut);
						//Check that treeOfLife contain all outputCOntainerCodes
						//Get treeOfLife from In
						Logger.debug("Container out "+codeContainerOut);
						if(containerOut.treeOfLife!=null && containerOut.treeOfLife.paths!=null) {
							String subTreeOfLife = null;
							for(String path : containerOut.treeOfLife.paths) {
								if(path.contains(process.inputContainerCode)) {
									subTreeOfLife=path.substring(path.indexOf(process.inputContainerCode));
								}
							}
							if(subTreeOfLife!=null) {
								//Logger.debug("Index "+index);
								List<String> listSubTreeOfLife = Arrays.asList(subTreeOfLife.split(","));
								Logger.debug("Sub tree of life "+String.join(",", subTreeOfLife));
								List<String> codeNotInList = listSubTreeOfLife.stream().filter(c->!codeContainerToCheck.contains(c)).collect(Collectors.toList());

								if(codeNotInList.size()>0)
									badCodeContainers.add(codeContainerOut);

							}else {
								((List<String>)validation.getObject("RecapErrorData")).add(process.code+",noSubTreeOfLife");
							}
						}else {
							((List<String>)validation.getObject("RecapErrorData")).add(process.code+",noTreeOfLife for "+codeContainerOut);
						}
					}
					if(badCodeContainers.size()>0) {
						String listBadContainer = String.join(",", badCodeContainers);
						Logger.debug("Find bad container");
						((List<String>)validation.getObject("RecapBadProcess")).add(process.code+","+listBadContainer);
					}
				}
				nb++;
			}
		}
		ProcessPropertiesContentHelper.createExcelFileRecap(validation);
	}
}
