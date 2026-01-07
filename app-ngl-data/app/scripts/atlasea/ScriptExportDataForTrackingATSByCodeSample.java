package scripts.atlasea;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.mongojack.DBQuery;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.lfw.controllers.scripts.buffered.Script;
import models.laboratory.resolutions.instance.Resolution;
import models.laboratory.resolutions.instance.ResolutionConfiguration;
import models.laboratory.run.instance.ReadSet;
import models.laboratory.sample.instance.ResolutionHistoryEntry;
import models.laboratory.sample.instance.Sample;
import models.laboratory.sample.instance.reporting.SampleProcess;
import models.utils.InstanceConstants;
import play.Logger;
import scripts.ScriptImportDataCNS;
import validation.ContextValidation;

/**
 * Script permettant d'exporter les données du projets ATLASea pour alimenter la base de données externes de tracking ATLASea
 * 
 */
public class ScriptExportDataForTrackingATSByCodeSample extends Script<ScriptImportDataCNS.Args>{

	private static final String user = "ngl-support";
	public static class Args {

	}
	private TrackingATLASeaServices trackingATLASeaServices;


	@Inject
	public ScriptExportDataForTrackingATSByCodeSample(TrackingATLASeaServices trackingATLASeaServices) {
		this.trackingATLASeaServices = trackingATLASeaServices;
	}

	//TODO merge same nom org. collab
	//TODO revue validite container banque
	//TODO mettre automatiquement done dans assembly selon resolution 
	//TODO Et supprimer colonne résolution valeur
	//TODO mettre les valeurs null en vide
	//TODO enlever les test internes avec nouvelle categorie
	//TODO supprimer la colonne statut GoaT ATLASea
	
	@Override
	public void execute(scripts.ScriptImportDataCNS.Args args) throws Exception {
		//Entete fichier excel
		ContextValidation cv = ContextValidation.createUpdateContext(user);
		cv.putObject("ExportTrackATS", new ArrayList<String>());
		((List<String>)cv.getObject("ExportTrackATS")).add("Nom taxon Worms,AphiaID, Code taxon NCBI,Nom taxon NCBI,Statut GoaT ATLASea,Provenance,Résolution Catégorie,Résolution,Date de modification résolution,Numéro lot individu,Code individu,Short Read Sequencing,Long Read Sequencing,HiC Sequencing,RNA Sequencing,Assembling,Scaffolding,Curating,Reviewing,Submitting");

		//Récuperer tous les samples DKB les grouper par taxon (nom organisme collab)
		Logger.debug("Get sample DKB");
		Map<String, List<Sample>> mapTaxonSample = MongoDBDAO.find(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, DBQuery.in("projectCodes", "DKB")).toList().stream().collect(Collectors.groupingBy(sample->sample.properties.get("collabScientificName").getValue().toString()));
		//Map<String, List<Sample>> mapTaxonSample = MongoDBDAO.find(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, DBQuery.and(DBQuery.in("projectCodes", "DKB"),DBQuery.is("properties.collabScientificName.value", "Tubulanus polymorphus"))).toList().stream().collect(Collectors.groupingBy(sample->sample.properties.get("collabScientificName").getValue().toString()));

		//Map<String, List<Sample>> mapTaxonSample = MongoDBDAO.find(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, DBQuery.and(DBQuery.in("projectCodes", "DKB"),DBQuery.is("properties.collabScientificName.value", "Chondracanthus acicularis"))).toList().stream().collect(Collectors.groupingBy(sample->sample.properties.get("collabScientificName").getValue().toString()));

		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyy");
		for(String collabName : mapTaxonSample.keySet()) {
			List<Sample> samples = mapTaxonSample.get(collabName);
			for(Sample sample : samples) {

				Logger.debug("Get sample "+sample.code);
				String shortReadState=null;
				String longReadState=null;
				String hicState=null;
				String rnaState=null;
				String assemblyState=null;
				Map<String,String> assemblyStateStep = new HashMap<String, String>();

				
				//Get resolution sample
				//String resolutionsValues = sample.resolutionCodes.stream().map(o->o.toString()).collect(Collectors.joining(";"));
				ResolutionConfiguration resoConf = MongoDBDAO.findByCode(InstanceConstants.RESOLUTION_COLL_NAME, ResolutionConfiguration.class,"sampleReso");
				Set<String> resoCategories = new TreeSet<String>();
				Set<String> resoLabels = new TreeSet<String>();
				Set<String> resolDate = new TreeSet<String>();
				sample.resolutionCodes.stream().forEach(rc->{
					Resolution reso = resoConf.resolutions.stream().filter(r->r.code.equals(rc)).findFirst().get();
					resoCategories.add(reso.category.name);
					resoLabels.add(reso.name);
					//Get last date resolution
					if(sample.resolutionHistory!=null) {
						ResolutionHistoryEntry rhe = sample.resolutionHistory.stream().filter(rh->rh.resolutionCodes.contains(rc)).max(Comparator.comparing(c->c.index)).get();
						resolDate.add(sdf.format(rhe.date));
					}
				});
				String resolutionsValues = String.join(";",resoLabels);
				String resoCategoriestoString = String.join(";", resoCategories);
				String resoDates = String.join(";", resolDate);
				
				
				
				//Get all processes for sample
				List<SampleProcess> processes = new ArrayList<SampleProcess>();
				if(sample.processes!=null)
					processes=sample.processes;

				//Infos short read sequencing
				//Etat prepared, sequenced, submitted
				shortReadState=trackingATLASeaServices.getShortReadState("sampleOnContainer.properties.fromSampleCode.value", sample.code, processes);

				//Infos long read sequencing
				//Etat prepared, sequenced, submitted
				List<ReadSet> readsetsLR = trackingATLASeaServices.getReadsetsLR("sampleOnContainer.properties.fromSampleCode.value", sample.code);
				longReadState=trackingATLASeaServices.getLongReadState(processes, readsetsLR);

				//Infos HiC Sequencing 
				//Etat prepared, sequenced, submitted
				hicState=trackingATLASeaServices.getHicState("sampleOnContainer.properties.fromSampleCode.value", sample.code, processes);

				//TODO problème sample DKB_AAKM ne passe pas la requête
				if(!sample.code.equals("DKB_AAKM")) {
					//Infos rna sequencing 
					//Etat prepared, sequenced, submitted
					rnaState=trackingATLASeaServices.getRnaState("sampleOnContainer.properties.fromSampleCode.value", sample.code, processes);
				}
				//Infos assemblage
				assemblyStateStep=trackingATLASeaServices.getAssemblyState(readsetsLR);

				//Etape Assembling, Scaffolding, Curating, Reviewing, Submitting
				//Attention cas de Caro avec assembling=topup et scaffolding=in waiting pas gérer dans modèle actuel
				Logger.debug(collabName+ " Sample code "+sample.code+" Short Read State "+shortReadState+ " Long read State "+longReadState+ " HiC State "+hicState+" RNA State "+rnaState+" Assembly State "+assemblyState);
				Logger.debug("Map step LRA "+assemblyStateStep.get("LRA")+" SCAF "+assemblyStateStep.get("SCAF")+" CUR "+assemblyStateStep.get("CUR")+" REV "+assemblyStateStep.get("REV")+" SUB "+assemblyStateStep.get("SUB"));

				String wormsCode = null;
				if(sample.properties.containsKey("WoRMSCode"))
						wormsCode=sample.properties.get("WoRMSCode").getValue().toString();
				String statutGoatATS = null;
				if(sample.properties.containsKey("currentGoatStatusATS"))
					statutGoatATS=sample.properties.get("currentGoatStatusATS").getValue().toString();
				//Export dans fichier excel
				((List<String>)cv.getObject("ExportTrackATS")).add(collabName+
						","+wormsCode+
						","+sample.taxonCode+
						","+sample.ncbiScientificName.replace(",", "")+
						","+statutGoatATS+
						","+sample.properties.get("sampleOrigin").getValue().toString()+
						","+resolutionsValues+
						","+resoCategoriestoString+
						","+resoDates+
						","+sample.properties.get("individualBatch").getValue().toString()+
						","+sample.properties.get("individualNumber").getValue().toString()+
						","+shortReadState+
						","+longReadState+
						","+hicState+
						","+rnaState+
						","+assemblyStateStep.get("LRA")+
						","+assemblyStateStep.get("SCAF")+
						","+assemblyStateStep.get("CUR")+
						","+assemblyStateStep.get("REV")+
						","+assemblyStateStep.get("SUB"));
			}


		}
		//Create excel file to recap in execution directory
		createExcelFileRecap(cv);
	}


	/**
	 * Create excel file in execution directory
	 * File recap : list all objects updated
	 * @param cv
	 */
	private void createExcelFileRecap(ContextValidation cv)	{

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
		try (OutputStream fileOut = new FileOutputStream("ExportTrackingATS.xls")) {
			wb.write(fileOut);
		} catch(Exception e) {
			Logger.debug(e.getMessage());
		}
	}
}
