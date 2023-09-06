package sra.scripts;


//import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

//import java.util.List;

import javax.inject.Inject;
//import org.mongojack.DBQuery;

import fr.cea.ig.lfw.controllers.scripts.buffered.Script;
import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.ngl.dao.api.sra.AbstractSampleAPI;
//import fr.cea.ig.ngl.dao.api.sra.AbstractStudyAPI;
//import fr.cea.ig.ngl.dao.api.sra.ConfigurationAPI;
//import fr.cea.ig.ngl.dao.api.sra.ExperimentAPI;
import fr.cea.ig.ngl.dao.api.sra.SampleAPI;
import models.sra.submit.sra.instance.AbstractSample;
import models.sra.submit.sra.instance.ExternalSample;
import models.sra.submit.sra.instance.Sample;
//import models.sra.submit.common.instance.Readset;
//import models.sra.submit.common.instance.Submission;
//import models.sra.submit.sra.instance.Configuration;
//import models.sra.submit.sra.instance.Experiment;
import validation.ContextValidation;
import play.libs.Json;
import services.SraEbiAPI;
import services.XmlToSra;

import org.apache.commons.lang3.StringUtils;
import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;


/*
 * Script a lancer pour avoir les numeros d'accession associés à une soumission ou plusieurs soumissions.
 * Exemple de lancement :
 * http://localhost:9000/sra/scripts/run/sra.scripts.BilanAc?codes=code_soumission_1&codes=code_soumission_2
 * @author sgas
 *
 */
public class RepriseHistoAttributeSample extends Script<RepriseHistoAttributeSample.MyParam>{
	private static final play.Logger.ALogger logger = play.Logger.of(RepriseHistoAttributeSample.class);


	private final AbstractSampleAPI abstractSampleAPI;
	private final SampleAPI         sampleAPI;
	private final SraEbiAPI            ebiAPI;
	private final NGLApplication    app;

	@Inject
	public RepriseHistoAttributeSample(AbstractSampleAPI abstractSampleAPI,
									   SampleAPI     	 sampleAPI,
					 				   SraEbiAPI     	     ebiAPI,
					 				   NGLApplication    app) {

		this.abstractSampleAPI = abstractSampleAPI;
		this.sampleAPI         = sampleAPI;
		this.ebiAPI     	   = ebiAPI;
		this.app 			   = app;
	}


	// ma structure de controle et stockage des arguments de l'url
	public static class MyParam {
	}


	@Override
	public void execute(MyParam args) throws Exception {
		String user = "ngsrg";
		
		XmlToSra repriseHistorique = new XmlToSra();
//		File fileXmlSamples = new File("/env/cns/home/sgas/samples.xml");
//		Iterable<Sample> listSamples = repriseHistorique.forSamples(fileXmlSamples, user);
		
		List<AbstractSample> list_abstractSample = new ArrayList<>();
		list_abstractSample.add(abstractSampleAPI.dao_findOne(DBQuery.in("accession", "ERS1082883")));
		list_abstractSample.add(abstractSampleAPI.dao_findOne(DBQuery.in("accession", "ERS1082517")));
		list_abstractSample.add(abstractSampleAPI.dao_findOne(DBQuery.in("accession", "ERS1082681")));
		list_abstractSample.add(abstractSampleAPI.dao_findOne(DBQuery.in("accession", "ERS1082367")));
		list_abstractSample.add(abstractSampleAPI.dao_findOne(DBQuery.in("accession", "ERS1082681")));
		int countSample = 0;
		int countSampleAttributes = 0;
		List <Sample> problems = new ArrayList<>();
		for (AbstractSample abstractSample : abstractSampleAPI.dao_all()) {
		//for (AbstractSample abstractSample : 	list_abstractSample) {
			if (abstractSample instanceof ExternalSample) {
				continue;
			}
			if (StringUtils.isBlank(abstractSample.accession)) {
				continue;
			}
			if(abstractSample.code.contains("TARA_")) {
				continue;
			}
			// Recuperer attributs uniquement si sample est bien soumis par nous et possede un AC :
			if (abstractSample instanceof Sample && StringUtils.isNotBlank(abstractSample.accession)) {
				logger.debug("!!!!!!!!!!! "   + abstractSample.code);
//				if(abstractSample.accession.equals("ERS1082914") ){
//					continue;
//				}
				countSample++;
				//logger.debug("countEbiReq = " + countSample);
				//logger.debug("xxxxxxxxxxx sample.code = " + abstractSample.code);
				String xmlSamples = ebiAPI.ebiXml(abstractSample.accession, "samples");
				//logger.debug("vvvvvvvvvvvvvvvvv        XMLSAMPLES = " + xmlSamples);
//				if (xmlSamples.contains(":403")) { // 403 : problemes de droits:
//					// relancer requete 1 fois:
//					xmlSamples = ebiAPI.ebiXml(abstractSample.accession, "samples");
//				}
				
				//Iterable<Sample> listSamples = repriseHistorique.forSamples(fileXmlSamples, user);
				Iterable<Sample> listSamples = repriseHistorique.forSamples(xmlSamples, null);
				for (Sample sample : listSamples) {
					// Effectuer mise à jour dans base ssi sample rapatrié de l'EBI contient attributes
					if (StringUtils.isNotBlank(sample.attributes)) {
						//printfln("Pour le sample '%s', les attributs sont: \n'%s'", sample.code, sample.attributes);
						countSampleAttributes++;
						ContextValidation contextValidation = ContextValidation.createUpdateContext(user); // on veut updater l'experiment sans run pour son run
						// On ne peut pas tester le sample qui vient de l'EBI car en creation, il manque 
						// des infos comme projectCode ou sample.state.code ou sample.traceInformation.createDate
						// et en mode update, il manque le champ _id
						
						Sample sampleDb = sampleAPI.get(abstractSample.code);
						sampleDb.traceInformation.modifyUser = user;
						sampleDb.traceInformation.modifyDate = new Date();
						if(StringUtils.isBlank(sample.attributes)) {
							continue;
						}
						sampleDb.attributes = sample.attributes;
						sampleDb.validate(contextValidation);
//						printfln("sample.attributes = %s\n", sample.attributes);
//						printfln("sampleDb.attributes = %s\n", sampleDb.attributes);
						if(contextValidation.hasErrors()) {
							problems.add(sample);
							printfln("Probleme de validation pour le sample.code=%s", sample.code);
							contextValidation.displayErrors(logger, "debug");
							println(Json.prettyPrint(app.errorsAsJson(contextValidation.getErrors())));				
						} else {
							printfln("ok pour sauvegarde dans db de sample.code=%s", sample.code);	
							sampleAPI.dao_update(DBQuery.is("code", sample.code),
								DBUpdate.set("attributes", sample.attributes)
									.set("traceInformation.modifyDate", sampleDb.traceInformation.modifyDate)
									.set("traceInformation.modifyUser", sampleDb.traceInformation.modifyUser));
						}
					}
				}
		   }
		}
		printfln("Nombre de sample soumis par le CNS=%s et nombre de sample avec attributes autre que ENA = %s\n", countSample, countSampleAttributes);	
		printfln(problems.size() + " samples non valides:\n");
		for (Sample sample: problems) {
			printfln("Problemes pour accession=%s , code=%s\n", sample.accession, sample.code);
		}
	}
}


