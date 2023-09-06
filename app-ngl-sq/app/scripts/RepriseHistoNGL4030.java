package scripts;

import java.util.Date;
import java.util.List;

import org.mongojack.DBQuery;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.lfw.controllers.scripts.chunked.ScriptWithArgs;
import models.laboratory.container.instance.Container;
import models.laboratory.sample.instance.Sample;
import models.utils.InstanceConstants;
import play.Logger;

public class RepriseHistoNGL4030 extends ScriptWithArgs<RepriseHistoNGL4030.Args> {

	public static class Args {

	}

	@Override
	public void execute(Args args) throws Exception {
		Logger.error("Début RepriseHistoNGL4030");

		DBQuery.Query query = DBQuery.exists("contents.taxonCode").notExists("contents.ncbiScientificName");
		List<Container> containersList = MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, Container.class, query).toList();

		containersList.forEach(container -> {
			container.contents.forEach(content -> {
				Logger.error("content.sampleCode : " + content.sampleCode);

				if (!content.sampleCode.startsWith("CEB")) {
					Sample sample = MongoDBDAO.findByCode(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, content.sampleCode);

					if (sample != null) {
						if(null != sample.life && null != sample.life.path){

							boolean isTopTree = false;
							String codeSample = sample.life.from.sampleCode;
		
							while (isTopTree == false) {
								Sample grandParentSample = MongoDBDAO.findOne(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, DBQuery.is("code",codeSample));
								
								if (grandParentSample != null && !grandParentSample.code.startsWith("CEA")) {
									Logger.error("Sample : " + grandParentSample.code);
		
									if (null != grandParentSample.life && null != grandParentSample.life.path) {
										codeSample = grandParentSample.life.from.sampleCode;
									} else {
										isTopTree = true;
		
										Logger.error("Mise à jour du sample : " + grandParentSample.code);
		
										grandParentSample.traceInformation.modifyDate = new Date();
										grandParentSample.traceInformation.modifyUser = "ngl-data";
		
										// MongoDBDAO.update(InstanceConstants.SAMPLE_COLL_NAME, grandParentSample);
									}
								}
							}
						}
					} else {
						Logger.error("sample null pour le code : " + content.sampleCode);
					}
				}
			});
		});

		Logger.error("Fin RepriseHistoNGL4030");
	}
}
