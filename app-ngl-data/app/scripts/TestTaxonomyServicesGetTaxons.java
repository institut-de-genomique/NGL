package scripts;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import fr.cea.ig.lfw.controllers.scripts.buffered.Script;
import models.sra.submit.util.SraException;
import services.taxonomy.Taxon;
import services.taxonomy.TaxonomyServices;

/**
 * Script qui prend en entrée un fichier de taxonId et qui va interroger le NCBI ou à defaut l'EBI pour
 * recuperer les taxons correspondants
 * ex de lancement :
 * http://localhost:9000/scripts/run/scripts.TestTaxonomyServicesGetTaxons?fileTaxonCodes=C:\Users\sgas\debug\listeInputTaxonCodes.txt
 */
public class TestTaxonomyServicesGetTaxons  extends Script<TestTaxonomyServicesGetTaxons.Args> {

	public static class Args {
		public String fileTaxonCodes;
	}
	
	private TaxonomyServices taxonomyServices;
	private static final play.Logger.ALogger logger = play.Logger.of(TestTaxonomyServicesGetTaxons.class);

	@Inject
	public TestTaxonomyServicesGetTaxons(TaxonomyServices taxonomyServices) {
		this.taxonomyServices = taxonomyServices;
	}



	
	@Override
	public void execute(Args args) throws Exception {
		if(args.fileTaxonCodes==null) {
			logger.debug("Absence du chemin complet du fichier des taxonCodes");
			printfln("Absence du chemin complet du fichier des taxonCodes");
			return;
		}
		File fileTaxonCodes = new File(args.fileTaxonCodes);
		List<String> taxonCodes = parseFileTaxonCodes(fileTaxonCodes);

		Map<String, Taxon> mapTaxons = taxonomyServices.getTaxons(taxonCodes);
	
		for(String taxonCode : taxonCodes) {
			Taxon taxon = mapTaxons.get(taxonCode);
			if (taxon==null) {
				printfln("   ??   Pas de taxon dans la mapDesTaxons pour le taxonCode " + taxonCode);
				continue;
			}
			if(taxon.error) {
				if(StringUtils.isNotBlank(taxon.errorMessage)) {
					printfln(taxon.errorMessage);
				} else {
					printfln("  ??  Taxon en erreur et pas de message d'erreur pour le taxonCode " + taxonCode);
				}
			} else {
				printfln("taxon valide pour le taxonCode " + taxonCode +  " avec scientificName=" + taxon.scientificName);
			}	
		}
	}
	
	
	public List<String> parseFileTaxonCodes(File fileTaxonCodes) throws Exception {
		List<String> taxonCodes = new ArrayList<>();
		if (! fileTaxonCodes.exists()) {
			throw new Exception("Fichier des taxons n'existe pas sur le fileSystem: "+ fileTaxonCodes.getAbsolutePath());
		}
		String ligne = "";
		String pattern_string = "(\\S+)";
		String pattern_string_c = "([^#]*)#";
		Pattern p_c = Pattern.compile(pattern_string_c);

		Pattern p = Pattern.compile(pattern_string);
		String ebiRelatifName;
		try (BufferedReader input_buffer = new BufferedReader(new FileReader(fileTaxonCodes))) {
			while ((ligne = input_buffer.readLine()) != null) {	
				// ignorer ce qui suit le signe de commentaire
				Matcher m_c = p_c.matcher(ligne);
				if (!m_c.find()) {
				} else {
					ligne = m_c.group(1);
				}
				// ignorer lignes sans caracteres visibles
				if (ligne.matches("^\\s*$")){
					continue;
				}

				Matcher m = p.matcher(ligne);
				if (!m.find()) {
					throw new SraException("Probleme de format avec la ligne : '" + ligne +"'");
				}
				String taxonCode = m.group(1);
				taxonCodes.add(taxonCode);
			}
		} catch (IOException e) {
			throw new SraException("Probleme lors du chargement du fichier ", e);
		}
		return taxonCodes;
	}
}

		
		


	



