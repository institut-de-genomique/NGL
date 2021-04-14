package sra.scripts;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import fr.cea.ig.lfw.controllers.scripts.buffered.ScriptNoArgs;
import fr.cea.ig.ngl.dao.api.sra.AbstractStudyAPI;
import fr.cea.ig.ngl.dao.api.sra.ExperimentAPI;
import fr.cea.ig.ngl.dao.api.sra.SubmissionAPI;
import fr.cea.ig.ngl.dao.readsets.ReadSetsAPI;
import fr.cea.ig.ngl.dao.sra.StudyDAO;
import models.laboratory.common.instance.PropertyValue;
import models.laboratory.run.instance.ReadSet;
import models.sra.submit.common.instance.AbstractStudy;
import models.sra.submit.common.instance.Study;
import models.sra.submit.common.instance.Submission;
import models.sra.submit.common.instance.UserRefCollabType;
import models.sra.submit.sra.instance.Experiment;
import models.sra.submit.sra.instance.RawData;
import services.EbiAPI;
import services.XmlServices;
import services.XmlToSra;

/*
 * Script à utiliser pour recharger les md5 d'une soumission a partir de NGL.
 * {@code http://localhost:9000/sra/scripts/run/sra.scripts.Test}
 * <br>
 * Si parametre absent dans url => declenchement d'une erreur.
 *  
 * @author sgas
 *
 */
public class Test extends ScriptNoArgs {
	private final SubmissionAPI submissionAPI;
	private final XmlServices xmlServices;
	private final EbiAPI ebiAPI;
	private final AbstractStudyAPI abstractStudyAPI;
	private static final play.Logger.ALogger logger = play.Logger.of(Test.class);
	private final ReadSetsAPI       readsetsAPI;  // collection readset de ngl-seq

	@Inject
	public Test(ExperimentAPI      experimentAPI,
				SubmissionAPI      submissionAPI,
				XmlServices        xmlServices,
				EbiAPI             ebiAPI,
				AbstractStudyAPI   abstractStudyAPI,
				ReadSetsAPI        readsetsAPI  // collection readset de ngl-seq

				) {
		this.submissionAPI = submissionAPI;
		this.xmlServices   = xmlServices;
		this.ebiAPI        = ebiAPI;
		this.abstractStudyAPI      = abstractStudyAPI;
		this.readsetsAPI = readsetsAPI;  

		
	}
	
//	@Override
//	public void execute() throws Exception {
//		String submissionCode = "GSC_APX_BXT_38AF1N87U";
//		Submission submission = submissionAPI.get(submissionCode);
//		println("submissionCode = " + submission.code);
//		for (Iterator<Entry<String, UserRefCollabType>> iterator = submission.mapUserRefCollab.entrySet().iterator(); iterator.hasNext();) {
//		  Entry<String, UserRefCollabType> entry = iterator.next();
//		  printfln("%10s|%20s|%10s", entry.getKey(),entry.getValue().getStudyAc(),entry.getValue().getSampleAc() );
//		}
//	}
//	
	public Experiment EbiFetchExperiment(String accession, Date submissionDate) {	
		try {
			//logger.debug("accession experiment = " + accession);
			String xmlExperiments = ebiAPI.ebiXml(accession, "experiments");
			//logger.debug(xmlExperiments);
			XmlToSra repriseHistorique = new XmlToSra();
			Iterable<Experiment> listExperiments = repriseHistorique.forExperiments(xmlExperiments, submissionDate);
			if (listExperiments.iterator().hasNext()) {
				return listExperiments.iterator().next();
			}
			return null;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
			
	@Override
	public void execute() throws Exception {
//		for (AbstractStudy absStudy : abstractStudyAPI.dao_all()) {	
//			println("   - StudyCode = " + absStudy.code);
//		}
//		println("fin du test");
		String readSetCode = "CGC_AAAROSRC_6_HYCNYBBXX.IND1";
		ReadSet readSet = readsetsAPI.get(readSetCode);
		List <models.laboratory.run.instance.File> list_files =  readSet.files;
		String dataDir = readSet.path;
		println("dataDir=" + dataDir);
		for (models.laboratory.run.instance.File runInstanceFile: list_files) {
			String runInstanceExtentionFileName = runInstanceFile.extension;
			println(   runInstanceFile.fullname);
			// conditions qui doivent etre suffisantes puisque verification préalable que le readSet
			// est bien valide pour la bioinformatique.
			
					RawData rawData = new RawData();
					//logger.debug("fichier " + runInstanceFile.fullname);
					rawData.extention = runInstanceFile.extension;
					//logger.debug("dataDir "+dataDir);
					rawData.directory = dataDir.replaceFirst("\\/$", ""); // oter / terminal si besoin
					//logger.debug("raw data directory"+rawData.directory);
					rawData.relatifName = runInstanceFile.fullname;
					
					rawData.collabFileName = rawData.relatifName;	
					Set <String> listKeys = runInstanceFile.properties.keySet();  // Obtenir la liste des clés
					for(String k: listKeys){
						logger.debug("cle = " + k);
						PropertyValue propertyValue = runInstanceFile.properties.get(k);
						//logger.debug(propertyValue.toString());
						logger.debug(", value  => "+propertyValue.value);
					} 
					if(runInstanceFile.properties.containsKey("collabFileName")) {
						println(runInstanceFile.fullname + " avec collabFileName");
						PropertyValue propertyValue = runInstanceFile.properties.get("collabFileName");
						//PropertyValue pCollabFileName =  runInstanceFile.properties.get("collabFileName");
						String collabFileName = (String) propertyValue.value;
						if(StringUtils.isNotBlank(collabFileName)) {
							rawData.collabFileName = collabFileName;
							println("ok pour collabFileName " + collabFileName);
						}
					}
					if (rawData.collabFileName.endsWith("fastq")) {
						rawData.collabFileName = rawData.collabFileName.concat(".gz");
						println("collabFileName=" + rawData.collabFileName);
					} 
					
					println("collabFileName = " + rawData.collabFileName);

			}
		
	}
		
		
}