package controllers.migration.cns;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;

import org.mongojack.DBQuery;

import controllers.CommonController;
import fr.cea.ig.MongoDBDAO;
import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.container.instance.Container;
import models.laboratory.experiment.instance.Experiment;
import models.laboratory.run.instance.ReadSet;
import models.laboratory.run.instance.Run;
import models.utils.InstanceConstants;
import play.Logger;
import play.mvc.Result;

public class MigrationUpdateFlowcellChemistryNanopore extends CommonController{

	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmm");

	public static Result migration(String fileName) throws IOException{

		Logger.info("Migration sample start");
		//TODO backup readSet
		//TODO backup run
		//TODO backup experiment
		//TODO backup container


		//Read File
		BufferedReader reader = null;
		try {
			//Parse file
			reader = new BufferedReader(new FileReader(new File(fileName)));
			String line = "";
			while ((line = reader.readLine()) != null) {
				//Logger.debug("Line "+line);
				String[] tabLine = line.split(";");
				String runCode = tabLine[0];
				String newFlowcellChemistry = tabLine[1];
				//Logger.debug("Update "+runCode+" with "+newFlowcellChemistry);
				String lineUpdate = runCode+",";
				//Find run
				Run run = MongoDBDAO.findByCode(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, runCode);
				//Update run 
				run.properties.put("flowcellChemistry", new PropertySingleValue(newFlowcellChemistry));
				MongoDBDAO.update(InstanceConstants.RUN_ILLUMINA_COLL_NAME, run);
				//Find ReadSet
				List<ReadSet> readSets = MongoDBDAO.find(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, DBQuery.is("runCode", runCode)).toList();
				for(ReadSet readSet : readSets){
					//Logger.debug("Update readSet "+readSet.code);
					lineUpdate+=readSet.code+",";
					if(readSet.sampleOnContainer!=null){
						readSet.sampleOnContainer.properties.put("flowcellChemistry", new PropertySingleValue(newFlowcellChemistry));
						MongoDBDAO.update(InstanceConstants.READSET_ILLUMINA_COLL_NAME, readSet);
					}
				}
				//Find experiment
				String containerCode = run.containerSupportCode;
				List<Experiment> experiments = MongoDBDAO.find(InstanceConstants.EXPERIMENT_COLL_NAME, Experiment.class, DBQuery.is("typeCode", "nanopore-depot").is("instrumentProperties.containerSupportCode.value", containerCode)).toList();
				for(Experiment experiment : experiments){
					//Logger.debug("Update experiment "+experiment.code);
					lineUpdate+=experiment.code+",";
					//Update experiment
					experiment.instrumentProperties.put("flowcellChemistry", new PropertySingleValue(newFlowcellChemistry));
					experiment.atomicTransfertMethods.stream().forEach(atm->{
						atm.outputContainerUseds.stream().filter(output->output.code.equals(containerCode)).forEach(output->{
							if(output.contents!=null){
								output.contents.stream().forEach(content->{
									content.properties.put("flowcellChemistry", new PropertySingleValue(newFlowcellChemistry));
								});
							}
						});
					});
					//Update experiment
					MongoDBDAO.update(InstanceConstants.EXPERIMENT_COLL_NAME, experiment);
				}
				//Find container
				Container container = MongoDBDAO.findByCode(InstanceConstants.CONTAINER_COLL_NAME, Container.class, containerCode);
				//Logger.debug("Update container "+container.code);
				lineUpdate+=container.code;
				container.contents.stream().forEach(c->{
					c.properties.put("flowcellChemistry", new PropertySingleValue(newFlowcellChemistry));
				});
				MongoDBDAO.update(InstanceConstants.CONTAINER_COLL_NAME, container);
				Logger.debug(lineUpdate);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			reader.close();
		}



		return ok();
	}


}
