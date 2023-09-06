package scripts;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.mongojack.DBQuery;
import com.mongodb.BasicDBObject;
import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.lfw.controllers.scripts.buffered.Script;
import models.laboratory.sample.instance.Sample;
import models.utils.InstanceConstants;
import services.taxonomy.Taxon;
import services.taxonomy.TaxonomyServices;


//http://localhost:9000/scripts/run/scripts.BilanTaxonInCollectionSamples
public class BilanTaxonInCollectionSamples extends Script<BilanTaxonInCollectionSamples.Args> {
	
	private TaxonomyServices taxonomyServices;

	
	public static class Args {
	}

	@Inject
	public BilanTaxonInCollectionSamples( TaxonomyServices taxonomyServices) {
		this.taxonomyServices = taxonomyServices;
	}

	@Override
	public void execute(Args args) throws Exception {
		Date date = new Date();
		BasicDBObject keys = new BasicDBObject();
		keys.put("code", 1);
		keys.put("taxonCode", 1);
		keys.put("projectCodes", 1);
		keys.put("traceInformation", 1);

		
		List<Sample> samples = MongoDBDAO.find(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, 
				DBQuery.notEquals("taxonCode",null),keys).toList();
		
		//logger.info("verification de la taxonomie des  : " + samples.size() + " samples le "+ date);
		println("verification de la taxonomie des  : "+samples.size() + " samples le " + date);
		Map<String, List<Sample>> samplesByTaxon = samples.stream().collect(Collectors.groupingBy(sample -> sample.taxonCode));
		//logger.info("Recherche de " + samplesByTaxon.keySet().size() + " taxons au NCBI\n");
		println("Recherche de " + samplesByTaxon.keySet().size() + " taxons au NCBI\n");

		ArrayList<String> listTaxonCodes = new ArrayList<String>();
		listTaxonCodes.addAll(samplesByTaxon.keySet());
		//ArrayList<String> listTaxonCodes = new ArrayList<String>();
		//listTaxonCodes.addAll(listTaxonCodes2.subList(1, 30));
		//ArrayList<String> listTaxonCodes = new ArrayList<String> (Arrays.asList("1927911","2711273","3152"));
		
		Map<String, Taxon> mapTaxons = taxonomyServices.getTaxons(listTaxonCodes);
		
		List<String> taxonCodesNotFound = new ArrayList<String>();
		// lister les taxons en entrée qui n'ont pas été trouvés et ceux en erreur :
		int countTaxonError = 0;
		String message_errorTaxon = "Donnee sans taxon ou avec taxon en erreur : \n";
		for (String taxonCode : listTaxonCodes) {
			List<Sample> listSamples = samplesByTaxon.get(taxonCode);
			String listSampleCodes = "";
			int cp = 0;
			List<String> projectCodes = new ArrayList<String>();
			String listProjectCodes = "";
			for(Sample sample :  listSamples) {
				cp++;
				if(cp < 20) {
					listSampleCodes += sample.code + ",";
				} else if (cp == 20) {
					listSampleCodes += "...";
				} else {
					// rien
				}
				for(String projectCode : sample.projectCodes) {
					if (! projectCodes.contains(projectCode)) {
						projectCodes.add(projectCode);
					}
				}
			}
			for (String projectCode : projectCodes) {
				listProjectCodes += projectCode + ",";
			}
			if( ! mapTaxons.containsKey(taxonCode)) {
				message_errorTaxon += "- taxonId " + taxonCode + " : absent, pour les projectCodes : "+ listProjectCodes + " et les sampleCodes : " + listSampleCodes + "\n";
				countTaxonError++;
				taxonCodesNotFound.add(taxonCode);
			} else {
				Taxon taxon = mapTaxons.get(taxonCode);
				if (taxon.error) {
					countTaxonError++;
					taxonCodesNotFound.add(taxonCode);
					String errorMessage = " ";
					if (StringUtils.isNotBlank(taxon.errorMessage)) {
						errorMessage = " : "+ taxon.errorMessage;
					}
					message_errorTaxon += "- taxonId " + taxonCode  + " : " + taxon.errorMessage + ", pour les projectCodes : " + listProjectCodes + " et les sampleCodes : " + listSampleCodes + "\n";
				}
			}
		}
		//logger.debug("------------------------------------------------");
		println("------------------------------------------------");
		//logger.debug("\n" + "Nombre de taxonId en erreur " + countTaxonError);
		println("\n" + "Nombre de taxonId en erreur " + countTaxonError);
		
		//logger.debug("\n" + message_errorTaxon + "\n");
		println("\n" + message_errorTaxon + "\n");

		// lister les taxonId qui sont en erreur parceque le taxonId retour ne correspond pas au taxonId demandé :
		String message_aliasInEbi = "";
		String message_aliasNotEbi = "";
		int count_corrections = 0;
		for(Iterator<Entry<String, Taxon>> iterator = mapTaxons.entrySet().iterator(); iterator.hasNext();) { 
			Entry<String, Taxon> entry = iterator.next();
			Taxon taxon = entry.getValue();
			if(taxon.error && taxon.errorMessage.startsWith(taxonomyServices.getErrorAkaMessage())) {
				Taxon ebiTaxon = taxonomyServices.getEbiTaxon(taxon.code);
				List<Sample> listSamples = samplesByTaxon.get(taxon.akaTaxId);
				String listSampleCodes = "";
				int cp = 0;
				List<String> projectCodes = new ArrayList<String>();
				String listProjectCodes = "";
				for(Sample sample :  listSamples) {
					cp++;
					if(cp < 20) {
						listSampleCodes += sample.code + ",";
					} else if (cp == 20) {
						listSampleCodes += "...";
					} else {
						// rien
					}
					for(String projectCode : sample.projectCodes) {
						if (! projectCodes.contains(projectCode)) {
							projectCodes.add(projectCode);
						}
					}
				}
				for (String projectCode : projectCodes) {
					listProjectCodes += projectCode + ",";
				}
				count_corrections++;
				if(ebiTaxon != null && !ebiTaxon.error) {
					message_aliasInEbi += "- taxonId " + taxon.akaTaxId + " : a remplacer par " + taxon.code + "(existe au NCBI et à l'EBI) pour les projectCodes : " + listProjectCodes  + " et les sampleCodes : " + listSampleCodes + "\n";
				} else {
					message_aliasNotEbi += "- taxonId " + taxon.akaTaxId + " : a remplacer par " + taxon.code + "(seulement au NCBI) pour les projectCodes : " + listProjectCodes  + " et les sampleCodes : " + listSampleCodes + "\n";
				}	
			}
		}

		String noSolution = "";
		for (String taxonCode : listTaxonCodes) {
			List<Sample> listSamples = samplesByTaxon.get(taxonCode);
			String listSampleCodes = "";
			int cp = 0;
			List<String> projectCodes = new ArrayList<String>();
			String listProjectCodes = "";
			String listUsers = "";
			for(Sample sample :  listSamples) {
				cp++;
				if(cp < 20) {
					listSampleCodes += sample.code + ",";
				} else if (cp == 20) {
					listSampleCodes += "...";
				} else {
					// rien
				}
				for(String projectCode : sample.projectCodes) {
					if (! projectCodes.contains(projectCode)) {
						projectCodes.add(projectCode);
					}
				}
				if (! listUsers.contains(sample.traceInformation.createUser)) {
					listUsers += sample.traceInformation.createUser + ",";
				}				
			}
			for (String projectCode : projectCodes) {
				listProjectCodes += projectCode + ",";
			}
			if( ! mapTaxons.containsKey(taxonCode) ) {
				noSolution += "- taxonId "+ taxonCode + " : consulter les users " + listUsers + " pour remplacer ce taxon dans les projectCodes : " + listProjectCodes  + " et les sampleCodes : " + listSampleCodes + "\n";
				countTaxonError++;
			} else {
				Taxon taxon = mapTaxons.get(taxonCode);
				if(taxon.error &&  !taxon.errorMessage.startsWith(taxonomyServices.getErrorAkaMessage())) {
					countTaxonError++;
					String errorMessage = " ";
					if (StringUtils.isNotBlank(mapTaxons.get(taxonCode).errorMessage)) {
						errorMessage = " ("+ errorMessage+")";
					}
					noSolution += "- taxonId "+ taxonCode + " : consulter les users " + listUsers + " pour remplacer ce taxon dans les projectCodes : " + listProjectCodes  + " et les sampleCodes : " + listSampleCodes + "\n";
				}
//				if(!taxon.error) {
//					goodTaxon += "Taxon bien present sur les serveurs : " + taxonCode + "\n";
//				}
			}
		}
		//logger.debug("------------------------------------------------");
		println("------------------------------------------------");
		//logger.debug("Nombre de corrections à faire : " + count_corrections);
		println("Nombre de corrections à faire : " + count_corrections);
		//logger.debug(message_aliasInEbi + "\n");
		println(message_aliasInEbi + "\n");
		//logger.debug(message_aliasNotEbi + "\n");
		println(message_aliasNotEbi + "\n");
		//logger.debug(noSolution + "\n");
		println(noSolution + "\n");
		//println(goodTaxon + "\n");
		
		int cpNoSubmittable = 0;
		String messNoSubmittable = "";
		for (String taxonCode : listTaxonCodes) {
			if( ! mapTaxons.containsKey(taxonCode) ) {
			} else {
				Taxon taxon = mapTaxons.get(taxonCode);
				
//				if (rank.contains("species") || rank.equals("varietas") || rank.equals("strain")) { // species ou subspecies
//				taxon.submittable = true;
//				//logger.error("hhhhhhhhhhhhhhhhhh rank = " + rank + " submittable pour le taxonId " + taxon.code);
//			} else {
//				taxon.submittable = false;
//				logger.error("yyyyyyyyyyyyyyyyyyyyyyyy  rank = " + rank + " noSubmittable pour le taxonId " + taxon.code);
//
//			}
				//if( ! taxon.error && ! taxon.submittable) {
				if( ! taxon.error && StringUtils.isNotBlank(taxon.rank) // on peut avoir des taxons qui viennent de l'EBI sans rank
						          && ! "strain".equalsIgnoreCase(taxon.rank) 
						          && ! "varietas".equalsIgnoreCase(taxon.rank) 
						          && ! "species".equalsIgnoreCase(taxon.rank) 
						          && ! "subspecies".equalsIgnoreCase(taxon.rank)
						          && ! "genotype".equalsIgnoreCase(taxon.rank)) {
			
					Taxon ebiTaxon = taxonomyServices.getEbiTaxon(taxon.code);
					if (ebiTaxon.submittable) {
						//logger.debug("QQQQQQQQQQQQQQQQQQQQQQQQQ             mauvaise detection non submittable pour taxonId "+ taxonCode + " avec rank = " + taxon.rank);
						//println("QQQQQQQQQQQQQQQQQQQQQQQQQ                  mauvaise detection non submittable pour taxonId "+ taxonCode + " avec rank = " + taxon.rank);
					}
					cpNoSubmittable++;
					List<Sample> listSamples = samplesByTaxon.get(taxonCode);
					String listSampleCodes = "";
					int cp = 0;
					List<String> projectCodes = new ArrayList<String>();
					String listProjectCodes = "";
					for(Sample sample :  listSamples) {
						cp++;
						if(cp < 20) {
							listSampleCodes += sample.code + ",";
						} else if (cp == 20) {
							listSampleCodes += "...";
						} else {
							// rien
						}
						for(String projectCode : sample.projectCodes) {
							if (! projectCodes.contains(projectCode)) {
								projectCodes.add(projectCode);
							}
						}
					}
					for (String projectCode : projectCodes) {
						listProjectCodes += projectCode + ",";
					}
					messNoSubmittable += "- taxonId " + taxonCode + " : non soumettable, pour les projectCodes : " + listProjectCodes + " et les sampleCodes : " + listSampleCodes + "\n";
				}
			}
		}	
		//logger.debug("------------------------------------------------");
		println("------------------------------------------------------");
		//logger.debug("Nombre de taxon non soumettable : " + cpNoSubmittable);
		println("Nombre de taxon non soumettable : " + cpNoSubmittable);
		//logger.debug(messNoSubmittable + "\n");
		println(messNoSubmittable + "\n");
		
	
		//logger.info("fin du traitement");
		println("fin du traitement");
		
	}
}


