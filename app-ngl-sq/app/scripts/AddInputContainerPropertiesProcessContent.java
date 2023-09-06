package scripts;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.mongojack.DBQuery;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.lfw.controllers.scripts.chunked.ScriptWithArgs;
import models.laboratory.common.instance.PropertyValue;
import models.laboratory.processes.instance.Process;
import models.utils.InstanceConstants;
import play.Logger;
import validation.ContextValidation;

/**
 * Script reprise histo NGL_2957 process reprise après ajout d'une propriété processus niveau content sur process long
 * 
 *typeCodes : liste des type code processus de reprise
 *newPropertyCode : propriété à propager 
 *
 *Valeur propagée uniquement si la valeur est définie dans le sampleOnInputContainer du process
 *
 *
 * @author ejacoby
 *
 */
public class AddInputContainerPropertiesProcessContent extends ScriptWithArgs<AddInputContainerPropertiesProcessContent.Args>{

	final String user = "ngl-support";

	public static class Args {
		public String[] typeCodes;
		public String newPropertyCode;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void execute(Args args) throws Exception {

		ContextValidation validation = ContextValidation.createUpdateContext(user);
		validation.putObject("RecapContainer", new ArrayList<String>());
		validation.putObject("RecapExperiment", new ArrayList<String>());
		validation.putObject("RecapProcess", new ArrayList<String>());
		validation.putObject("RecapReadSet", new ArrayList<String>());

		//Add Header Recap
		((List<String>)validation.getObject("RecapContainer")).add("Code process,code container,codeProperty,valueProperty");
		((List<String>)validation.getObject("RecapExperiment")).add("Code process,code experiment,codeProperty,valueProperty");
		((List<String>)validation.getObject("RecapProcess")).add("Code process,code process udpated,codeProperty,valueProperty");
		((List<String>)validation.getObject("RecapReadSet")).add("Code process,code readSet,codeProperty,valueProperty");
		for(int i=0; i<args.typeCodes.length; i++) {
			Logger.debug("GET PROCESSUS FOR TYPE CODE "+args.typeCodes[i]);
			List<Process> processes = MongoDBDAO.find(InstanceConstants.PROCESS_COLL_NAME, Process.class, DBQuery.is("typeCode", args.typeCodes[i])).toList();
			for(Process process : processes) {
				//Get inputContainer
				if(process.sampleOnInputContainer.properties.containsKey(args.newPropertyCode)) {
					Logger.debug("Update "+process.code);
					PropertyValue newProperty = process.sampleOnInputContainer.properties.get(args.newPropertyCode);
					Set<String> outputContainerCodes = process.outputContainerCodes;
					Set<String> sampleCodes = process.sampleCodes;
					Set<String> projectCodes = process.projectCodes;
					Set<String> tags = ProcessPropertiesContentHelper.getTagAssignFromProcessContainers(process);
					if(outputContainerCodes!=null && outputContainerCodes.size()>0)
						ProcessPropertiesContentHelper.updateProcessContentPropertiesWithCascade(outputContainerCodes, sampleCodes, projectCodes, tags, args.newPropertyCode, newProperty.getValue().toString(), process.code, validation);
					else
						Logger.debug("NO OUTPUTCONTAINER for process "+process.code);
				}else {
					Logger.error("NO INPUT PROPERTY for process "+process.code);
				}

			}
		}
		ProcessPropertiesContentHelper.createExcelFileRecap(validation);
		Logger.debug("END UPDATE processes");
	}


}
