package scripts;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.inject.Inject;

import fr.cea.ig.lfw.controllers.scripts.buffered.Script;
import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.ngl.NGLConfig;
import models.Constants;
import play.Logger;
import services.instance.sample.UpdateFlagForReportingData;
import validation.ContextValidation;

/**
 * Script permettant de détecter les objets récemments modifiés pour qu'ils soient pris en charge par le reporting nocturne de data.
 * Le script prend en entrée une date de début de recherche et une date de fin de recherche.
 * 
 * @author jcharpen - Jordi CHARPENTIER - jcharpen@genoscope.cns.fr
 */
public class ScriptUpdateFlagForReportingDataWithDateRange extends Script<ScriptUpdateFlagForReportingDataWithDateRange.Args> {

	/**
	 * Les arguments du script : "startDate" et "endDate".
	 * Format accepté : "YYYY/MM/DD".
	 * Exemple : "2021/01/01".
	 */
	public static class Args {

		public String startDate;

		public String endDate;
	}

	private NGLApplication app;

	private NGLConfig config;

	private Date START_DATE = null;

	private Date END_DATE = null;

	@Inject
	public ScriptUpdateFlagForReportingDataWithDateRange(NGLApplication app, NGLConfig config) {
		this.app = app;
		this.config = config;
	}

	@Override
	public void execute(Args args) throws Exception {
		Logger.info("Start ScriptUpdateFlagForReportingDataWithDateRange");

		ContextValidation contextError = ContextValidation.createUndefinedContext(Constants.NGL_DATA_USER);
		UpdateFlagForReportingData update = new UpdateFlagForReportingData(app, config);

		handleArgs(args);

		if (hasArgs()) {
			update.runImport(contextError, START_DATE, END_DATE);
		} else {
			update.runImport(contextError);
		}
		
		Logger.info("End ScriptUpdateFlagForReportingDataWithDateRange");
	}

	/**
	 * Méthode permettant de savoir si des arguments valides ont été donnés au script.
	 * Un argument valide est une date correctement formattée (i.e, "YYYY/MM/DD").
	 * 
	 * @return true si des arguments valides ont été fournis au script. false sinon.
	 */
	private boolean hasArgs() {
		if (START_DATE != null && END_DATE != null) {
			return true;
		} 

		return false;
	}

	/**
	 * Méthode permettant de gérer les arguments donnés au script.
	 * Si des arguments valides sont détectés, les variables START_DATE et END_DATE sont remplies.
	 * 
	 * @param args Un objet "Args" représentant les arguments donnés au script. Il peut être vide.
	 * 
	 * @see START_DATE
	 * @see END_DATE
	 */
	private void handleArgs(Args args) {
		try {
			final SimpleDateFormat SDF = new SimpleDateFormat("yyyy/MM/dd");

			if (args.startDate != null) {
				START_DATE = SDF.parse(args.startDate);
			}

			if (args.endDate != null) {
				END_DATE = SDF.parse(args.endDate);
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
}