package scripts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.lfw.controllers.scripts.buffered.ScriptWithArgsAndExcelBody;
import fr.cea.ig.ngl.NGLApplication;
import models.laboratory.common.instance.Comment;
import models.laboratory.sample.instance.Sample;
import models.utils.InstanceConstants;
import play.Logger;
import play.libs.Json;
import services.taxonomy.Taxon;
import services.taxonomy.TaxonomyServices;
import validation.ContextValidation;

/**
 * Script permettant de modifier les taxonId d'un ensemble d'echantillon CNS
 * Ce script va appeler la validation du sample 
 * Prend en argument le nom du ticket jira en indiquant dans le body, un fichier Excel 
 * avec header de 2 colonnes : colonne 1  avec sampleCode et colonne 2 avec taxonCode
 * Attention a ne pas mettre de valeurs dans la premiere ligne reservée aux intitulées des colonnes.
 * ex de lancement :
 * http://localhost:9000/scripts/run/scripts.UpdateTaxonIdCNS?jira=SUPSQ-5243
 */


public class UpdateTaxonIdCNS extends ScriptWithArgsAndExcelBody <UpdateTaxonIdCNS.Args> {
	private TaxonomyServices        taxonomyServices;
	private final NGLApplication    app;
	private static final play.Logger.ALogger logger = play.Logger.of(UpdateTaxonIdCNS.class);

	public static class Args {
		public String jira;
	}

	@Inject
	public UpdateTaxonIdCNS(NGLApplication app,
			                TaxonomyServices taxonomyServices) {
		this.taxonomyServices = taxonomyServices;
		this.app              = app;

	}
	
	public class SampleInfos  {
		Integer taxonId;
		String  sampleCode;
		Sample  sample;
	}
	
	public List<SampleInfos> parseWorkbook(XSSFWorkbook workbook) throws Exception {
		List<SampleInfos> samplesInfos = new ArrayList<SampleInfos>();
		
		workbook.getSheetAt(0).rowIterator().forEachRemaining(row -> {
			if(row.getRowNum() == 0) return; // skip header
			if(row != null && row.getCell(0) != null && row.getCell(1) != null) {
				String sampleCode = row.getCell(0).getStringCellValue();
				 Integer newTaxonId = (int) row.getCell(1).getNumericCellValue();
				 SampleInfos sampleInfos = new SampleInfos();
				 sampleInfos.sampleCode = sampleCode;
				 sampleInfos.taxonId = newTaxonId;
				 samplesInfos.add(sampleInfos); 
			}
		});
		return samplesInfos;
	}
	
	
	

	@Override
	public void execute(Args args, XSSFWorkbook workbook) throws Exception {
		ContextValidation ctx = ContextValidation.createUpdateContext("ngl-support");
		String ticket = args.jira;
		//NGL-4111
		if ( ! args.jira.matches("^(SUPSQ|SUPSQCNG|NGL)-\\d+$") ) {
			throw new RuntimeException("argument jira " +  args.jira + " qui n'a pas la forme attendue SUPSQ-XXX ou SUPSQCNG-XXX ou NGL-XXX");
		}
		
		// parser le fichier d'entree pour construire les association sampleCode, taxonId et construire la liste des taxonId
		List<SampleInfos> list_samplesInfos =  parseWorkbook(workbook);
		List<Integer> taxonIds = new ArrayList<Integer>();
		Map<String, SampleInfos> mapSamplesInfos = new HashMap<String, SampleInfos>();
		for (SampleInfos sampleInfos : list_samplesInfos) {
			 Integer taxonId = sampleInfos.taxonId;
			  if(! taxonIds.contains(taxonId)) {
				 taxonIds.add(taxonId); 
			  }
			if (mapSamplesInfos.containsKey(sampleInfos.sampleCode)) {
				if(! mapSamplesInfos.get(sampleInfos.sampleCode).taxonId.toString().equals(sampleInfos.taxonId.toString())) {
					// duplication du sample code avec incoherence sur le taxonId :
					ctx.addError("sampleCode " +  sampleInfos.sampleCode, " duplication des sampleCode avec taxonIds differents " + mapSamplesInfos.get(sampleInfos.sampleCode).taxonId + " et " + sampleInfos.taxonId);
				} else {
					// duplication mais pas d'incoherence
				}
			} else {
				mapSamplesInfos.put(sampleInfos.sampleCode, sampleInfos);
			}
		}
		
		
		// Recuperer les samples dans la base de données et les mettre à jour pour taxonId, lineage, scientificName et technicalCommentaire
		for (Iterator<Entry<String, SampleInfos>> iterator = mapSamplesInfos.entrySet().iterator(); iterator.hasNext();) {
			  Entry<String, SampleInfos> entry = iterator.next();
			  String sampleCode = entry.getKey();
			  Integer taxonId = entry.getValue().taxonId;
			  Sample sample = MongoDBDAO.findByCode(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, sampleCode);
			  entry.getValue().sample = sample;
			  String oldTaxonId = sample.taxonCode;
			  String stComment = ticket;
			  if (StringUtils.isNotBlank(oldTaxonId)) {
				  stComment = ticket + " old taxon : "  + oldTaxonId;
			  }
			  if(sample != null) {
				  sample.taxonCode = taxonId.toString();
				  sample.ncbiLineage = null;
				  sample.ncbiScientificName = null;
				  if (sample.technicalComments == null) {
					  sample.technicalComments =  new ArrayList<Comment>(); 
				  } 
				  sample.technicalComments.add(new Comment(stComment, "ngl-support", true));
			  }
			  entry.getValue().sample = sample;
		} // end for
		
		// verifier si tous les taxonId sont submittables :
		for(Integer taxonId : taxonIds) {
			boolean submittable = false; 
			Taxon taxon = taxonomyServices.getEbiTaxon(taxonId.toString());
			if (taxon != null && ! taxon.error && taxon.submittable) {
				submittable = true;
			}
			if (! submittable ) {
				ctx.addError("taxon " + taxonId.toString(), " non soumettable");
			}
		}
		
		// verifier validide des samples et si sample n'a pas de parent :
		for (Iterator<Entry<String, SampleInfos>> iterator = mapSamplesInfos.entrySet().iterator(); iterator.hasNext();) {
			  Entry<String, SampleInfos> entry = iterator.next();
			  String sampleCode = entry.getKey();
			  Sample sample = entry.getValue().sample;
			  if (sample == null) {
					ctx.addError("sample " + sampleCode  + sampleCode, " absent de la base");
			  } else {
				  if(sample.life != null && sample.life.from != null && StringUtils.isNotBlank(sample.life.from.sampleCode) ) {
					ctx.addError("sample " + sampleCode, " a un parent " + sample.life.from.sampleCode +  " et n'est pas modifiable pour le taxonId (modifier le parent)");
				  }
				  sample.validate(ctx);
			  }
		}
		
		// Les erreurs de traceInformation sont ignorées car on ne veut pas ajouter une modifyDate au risque de lancer une
		// cascade de modifications lances par NGL-DATA.
		// C'est ngl-data, qui en lancant des cascades pour mettre à jour ncbiLineage et ncbiScientificName mettra à jour 
		// le traceInformation.
		if(ctx.getErrors().containsKey("traceInformation.modifyDate")) {
			ctx.getErrors().remove("traceInformation.modifyDate");
		}
		if (ctx.getErrors().containsKey("traceInformation.modifyUser")) {
			ctx.getErrors().remove("traceInformation.modifyUser");
		}
		if(ctx.hasErrors()) {
			ctx.displayErrors(logger, "debug");
			println(Json.prettyPrint(app.errorsAsJson(ctx.getErrors())));
			println("Fin du traitement : Aucune modification des samples dans la base");
		} else {
			// mise à jour de la base pour tous les samples:
			int cp = 0;
			for (Iterator<Entry<String, SampleInfos>> iterator = mapSamplesInfos.entrySet().iterator(); iterator.hasNext();) {
				Entry<String, SampleInfos> entry = iterator.next();
				cp++;
				Sample sample = entry.getValue().sample;			
				MongoDBDAO.update(InstanceConstants.SAMPLE_COLL_NAME, sample);
			}
			Logger.info("Fin du traitement : update de " + cp + " samples dans la base");
			println("Fin du traitement : update de " + cp + " samples dans la base");
		}
	}


}
		
