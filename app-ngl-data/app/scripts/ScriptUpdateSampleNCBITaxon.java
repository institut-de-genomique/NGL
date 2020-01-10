package scripts;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;
import org.slf4j.MDC;

import com.mongodb.BasicDBObject;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.lfw.controllers.scripts.buffered.Script;
import fr.cea.ig.ngl.NGLApplication;
import models.Constants;
import models.laboratory.sample.instance.Sample;
import models.utils.InstanceConstants;
import play.Logger;
import play.libs.concurrent.Futures;
import services.ncbi.NCBITaxon;
import services.ncbi.TaxonomyServices;

public class ScriptUpdateSampleNCBITaxon extends Script<ScriptUpdateSampleNCBITaxon.Args>{

	public static class Args {
		//Sample code
		public String code;
	}

	private TaxonomyServices taxonomyServices;

	@Inject
	public ScriptUpdateSampleNCBITaxon(TaxonomyServices taxonomyServices) {
		this.taxonomyServices=taxonomyServices;
	}

	@Override
	public void execute(Args args) throws Exception {
		BasicDBObject keys = new BasicDBObject();
		keys.put("code", 1);
		keys.put("taxonCode", 1);
		List<Sample> samples = new ArrayList<Sample>();
		String[] tabSample = args.code.split(",");
		for(int i=0; i<tabSample.length; i++){
			Sample sampleToUpdate = MongoDBDAO.findByCode(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, tabSample[i]);
			samples.add(sampleToUpdate);
		}

		Logger.info("update sample without ncbi data : "+samples.size());
		Map<String, List<Sample>> samplesByTaxon = samples.stream().collect(Collectors.groupingBy(sample -> sample.taxonCode));
		List<CompletionStage<NCBITaxon>> promises = samplesByTaxon.keySet()
				.stream()
				.map(taxonCode -> taxonomyServices.getNCBITaxon(taxonCode))
				.collect(Collectors.toList());

		// Promise.
		Futures.sequence(promises).thenAcceptAsync(
				new Consumer<List<NCBITaxon>>() {
					@Override
					public void accept(List<NCBITaxon> taxons)  {
						taxons.forEach(taxon -> {
							if (taxon.error) {  
								Logger.error(taxon.code, "error to find taxon");
							}
							if (!taxon.exists) {
								Logger.error(taxon.code, "taxon code not exists !!");
							}

							String ncbiScientificName = taxon.getScientificName();
							String ncbiLineage        = taxon.getLineage();

							DBUpdate.Builder builder = DBUpdate.set("traceInformation.modifyDate",new Date() ).set("traceInformation.modifyUser",Constants.NGL_DATA_USER);

							if (ncbiScientificName == null) {
								Logger.error(taxon.code, "no ncbi scientific name");
								builder.set("ncbiScientificName", "no ncbi scientific name");
							} else {
								builder.set("ncbiScientificName", ncbiScientificName);
							}
							if (ncbiLineage == null) {
								Logger.error(taxon.code, "no ncbi lineage");
								builder.set("ncbiLineage", "no ncbi lineage");
							} else {
								builder.set("ncbiLineage", ncbiLineage);
							}
							samplesByTaxon.get(taxon.code).forEach(sample ->{
								//Logger.info("Update sample taxon info "+sample.code+" / "+taxon.code);
								MongoDBDAO.update(InstanceConstants.SAMPLE_COLL_NAME,  Sample.class, 
										DBQuery.is("code", sample.code), builder);	
							});					

						});
						Logger.debug("finish update");
					}
				});				
	}
}
