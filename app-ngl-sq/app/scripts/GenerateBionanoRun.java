package scripts;

import java.util.ArrayList;

import javax.inject.Inject;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import fr.cea.ig.lfw.controllers.scripts.buffered.ScriptWithExcelBody;
import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.ngl.dao.experiments.ExperimentsAPI;
import models.laboratory.experiment.instance.AtomicTransfertMethod;
import models.laboratory.experiment.instance.Experiment;
import play.Logger;
import rules.services.IDrools6Actor;
import validation.ContextValidation;

/**
 * Premier script développé pour le ticket NGL-2966 de reprise de passif Bionano.
 * But du script : Générer les runs Bionano à partir de codes d'expériences.
 * Le script prend en entrée un fichier Excel avec ce format :
 * CODE EXPERIENCE
 * La ligne 1 du fichier ne contient que le libellé de la colonne.
 * 
 * Règles métiers pour le script :
 * - L'expérience existe bien en base.
 * - L'expérience est bien un dépôt Bionano.
 * - L'expérience est terminée (état : "F").
 * 
 * Pour chacunes des expériences respectant ces règles, le script va appeler la règle Drools qui créé le run associé.
 * 
 * @author Jordi CHARPENTIER jcharpen@genoscope.cns.fr 
 */
public class GenerateBionanoRun extends ScriptWithExcelBody {

	private final ExperimentsAPI expAPI;

	private final IDrools6Actor rulesActor;

	@Inject
	public GenerateBionanoRun(ExperimentsAPI expAPI, NGLApplication app) {
		this.expAPI   = expAPI;
		this.rulesActor = app.rules6Actor();
	}

	@Override
	public void execute(XSSFWorkbook workbook) throws Exception { 		
		// Parcours des lignes du premier feuille et du fichier Excel.
		workbook.getSheetAt(0).rowIterator().forEachRemaining(row -> {
			// On ignore la première ligne : c'est le libellé des colonnes.
			if(row.getRowNum() == 0) {
				return; 
			}
			
			if(row != null && row.getCell(0) != null) { // Si la ligne et la cellule ne sont pas vides.
				String expCode = row.getCell(0).getStringCellValue();
				Logger.debug("Code EXPERIMENT : " + expCode);

				Experiment experiment = this.expAPI.get(expCode);

				if (experiment == null) { // Si l'expérience n'existe pas.
					Logger.debug("L'experience n'existe pas.");
				} else {
					if (experiment.typeCode.equals("bionano-depot")) { // Si l'expérience est un dépôt bionano.
						if (experiment.state.code.equals("F")) {
							ContextValidation ctxVal = ContextValidation.createUndefinedContext("jcharpen");
							ArrayList<Object> facts = new ArrayList<>();
								
							for (int i = 0; i < experiment.atomicTransfertMethods.size(); i++) {
								AtomicTransfertMethod atomic = experiment.atomicTransfertMethods.get(i);
								
								if (atomic.viewIndex == null) {
									atomic.viewIndex = i+1; 
								}

								facts.add(atomic);
							}

							facts.add(experiment);
							facts.add(ctxVal);

							// Appel de la règle Drools pour créer le run à partir de l'expérience.
							rulesActor.tellMessage("workflow", facts);
						} else {
							Logger.debug("L'experience n'est pas terminee.");
						}
					} else {
						Logger.debug("L'experience n'est pas un depot bionano.");
					}
				}
			} 

			Logger.debug("===");
		});
	}
}
