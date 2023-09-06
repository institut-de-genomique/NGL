package controllers.migration.cns;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;

import com.mongodb.BasicDBObject;

import controllers.CommonController;
import controllers.DocumentController;
import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.play.migration.NGLContext;
import models.laboratory.container.instance.Container;
import models.laboratory.processes.instance.Process;
import models.laboratory.run.instance.ReadSet;
import models.laboratory.sample.instance.Sample;
import models.utils.InstanceConstants;
import play.Logger;
import play.mvc.Result;
import services.instance.sample.UpdateSampleNCBITaxonCNS;
import services.ncbi.TaxonomyServices;

public class MigrationUpdateNCBITaxonSample extends DocumentController<Sample> { //CommonController{

	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmm");

private TaxonomyServices taxonomyServices;
	
	@Inject
	public MigrationUpdateNCBITaxonSample(NGLContext ctx, String collectionName, TaxonomyServices taxonomyServices) {
		super(ctx, collectionName, Sample.class);
		this.taxonomyServices = taxonomyServices;
	}
	
	public /*static*/ Result migration(String fileName) throws IOException{

		Logger.info("Migration sample start");
		//backupSample();
		//backupContainer();
		//backupReadSet();
		//backupProcess();

		Map<String, String> taxonCodeScientificName = new HashMap<String, String>();
		Map<String, String> taxonCodeLineageName = new HashMap<String, String>();

		//Read File
		BufferedReader reader = null;
		try {
			//Parse file
			reader = new BufferedReader(new FileReader(new File(fileName)));
			//Read header
			reader.readLine();
			String line = "";
			while ((line = reader.readLine()) != null) {
				String[] tabLine = line.split(";");
				String codeSample = tabLine[0];
				String oldTaxonCode = tabLine[1];
				String newTaxonCode = tabLine[2];

				//Check oldTaxonCode
				Sample sample = MongoDBDAO.findByCode(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, codeSample);
				if(sample.taxonCode.equals(oldTaxonCode)){
					//Get scientificName & Lineage
					String ncbiScientificName=null;
					String ncbiLineage=null;
					if(!taxonCodeScientificName.containsKey(newTaxonCode) || !taxonCodeLineageName.containsKey(newTaxonCode)){
						ncbiScientificName = taxonomyServices.getScientificName(newTaxonCode);
						ncbiLineage=taxonomyServices.getLineage(newTaxonCode);
						if(ncbiScientificName!=null && ncbiLineage!=null){
							taxonCodeScientificName.put(newTaxonCode, ncbiScientificName);
							taxonCodeLineageName.put(newTaxonCode, ncbiLineage);
						}
					}
					ncbiScientificName=taxonCodeScientificName.get(newTaxonCode);
					ncbiLineage=taxonCodeLineageName.get(newTaxonCode);

					if(ncbiScientificName!=null && ncbiLineage!=null){
						Logger.debug(codeSample+" New scientificName & lineage "+ncbiScientificName+"/"+ncbiLineage+" taxonCode "+oldTaxonCode+"->"+newTaxonCode);

						//update sample collection
						MongoDBDAO.update(InstanceConstants.SAMPLE_COLL_NAME,  Sample.class, 
								DBQuery.is("code", sample.code), DBUpdate.set("taxonCode",newTaxonCode).set("ncbiScientificName", ncbiScientificName).set("ncbiLineage", ncbiLineage));
						//update container collection
						MongoDBDAO.update(InstanceConstants.CONTAINER_COLL_NAME, Container.class, DBQuery.elemMatch("contents", DBQuery.is("sampleCode", codeSample)), DBUpdate.set("contents.$.taxonCode", newTaxonCode));
						MongoDBDAO.update(InstanceConstants.CONTAINER_COLL_NAME, Container.class, DBQuery.elemMatch("contents", DBQuery.is("sampleCode", codeSample).exists("ncbiScientificName")), DBUpdate.set("contents.$.ncbiScientificName", ncbiScientificName));
						MongoDBDAO.update(InstanceConstants.CONTAINER_COLL_NAME, Container.class, DBQuery.elemMatch("contents", DBQuery.is("sampleCode", codeSample).exists("ncbiLineage")), DBUpdate.set("contents.$.ncbiLineage", ncbiLineage));

						//Update ReadSet collection for sampleOnContainer
						MongoDBDAO.update(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, DBQuery.is("sampleOnContainer.sampleCode", codeSample), DBUpdate.set("sampleOnContainer.taxonCode", newTaxonCode));
						MongoDBDAO.update(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, DBQuery.is("sampleOnContainer.sampleCode", codeSample).exists("sampleOnContainer.ncbiScientificName"), DBUpdate.set("sampleOnContainer.ncbiScientificName", ncbiScientificName));
						MongoDBDAO.update(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, DBQuery.is("sampleOnContainer.sampleCode", codeSample).exists("sampleOnContainer.ncbiLineage"), DBUpdate.set("sampleOnContainer.ncbiLineage", ncbiLineage));

						//Update process.sampleOnInputContainer 
						MongoDBDAO.update(InstanceConstants.PROCESS_COLL_NAME, Process.class, DBQuery.is("sampleOnInputContainer.sampleCode", codeSample), DBUpdate.set("sampleOnInputContainer.taxonCode", newTaxonCode));
						MongoDBDAO.update(InstanceConstants.PROCESS_COLL_NAME, Process.class, DBQuery.is("sampleOnInputContainer.sampleCode", codeSample).exists("sampleOnInputContainer.ncbiScientificName"), DBUpdate.set("sampleOnInputContainer.ncbiScientificName", ncbiScientificName));
						MongoDBDAO.update(InstanceConstants.PROCESS_COLL_NAME, Process.class, DBQuery.is("sampleOnInputContainer.sampleCode", codeSample).exists("sampleOnInputContainer.ncbiLineage"), DBUpdate.set("sampleOnInputContainer.ncbiLineage", ncbiLineage));
					}else{
						Logger.error("No scientificName or lineage for "+newTaxonCode);
					}
				}else{
					Logger.error("No old taxonCode "+codeSample);
				}
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

	private static void backupSample() {
		String backupName = InstanceConstants.SAMPLE_COLL_NAME+"_BCK_NCBI_"+sdf.format(new java.util.Date());
		Logger.info("\tCopie "+InstanceConstants.SAMPLE_COLL_NAME+" to "+backupName+" start");
		List<Sample> samples = MongoDBDAO.find(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, DBQuery.exists("code")).toList();						

		MongoDBDAO.save(backupName, samples);
		Logger.info("\tCopie "+InstanceConstants.SAMPLE_COLL_NAME+" to "+backupName+" end");

	}

	private static void backupContainer() {
		String backupName = InstanceConstants.CONTAINER_COLL_NAME+"_BCK_NCBI_"+sdf.format(new java.util.Date());
		Logger.info("\tCopie "+InstanceConstants.CONTAINER_COLL_NAME+" to "+backupName+" start");
		List<Container> containers = MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, Container.class).toList();						

		MongoDBDAO.save(backupName, containers);
		Logger.info("\tCopie "+InstanceConstants.CONTAINER_COLL_NAME+" to "+backupName+" end");

	}

	private static void backupProcess() {
		String backupName = InstanceConstants.PROCESS_COLL_NAME+"_BCK_NCBI_"+sdf.format(new java.util.Date());
		Logger.info("\tCopie "+InstanceConstants.PROCESS_COLL_NAME+" to "+backupName+" start");
		List<Process> processes = MongoDBDAO.find(InstanceConstants.PROCESS_COLL_NAME, Process.class).toList();						

		MongoDBDAO.save(backupName, processes);
		Logger.info("\tCopie "+InstanceConstants.PROCESS_COLL_NAME+" to "+backupName+" end");
	}

	private static void backupReadSet() {
		BasicDBObject keys = new BasicDBObject();
		keys.put("treatments", 0);

		String backupName = InstanceConstants.READSET_ILLUMINA_COLL_NAME+"_BCK_NCBI_"+sdf.format(new java.util.Date());
		Logger.info("\tCopie "+InstanceConstants.READSET_ILLUMINA_COLL_NAME+" to "+backupName+" start");
		List<ReadSet> readSets = MongoDBDAO.find(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class,DBQuery.exists("sampleOnContainer"), keys).toList();						

		MongoDBDAO.save(backupName, readSets);
		Logger.info("\tCopie "+InstanceConstants.READSET_ILLUMINA_COLL_NAME+" to "+backupName+" end");
	}

}
