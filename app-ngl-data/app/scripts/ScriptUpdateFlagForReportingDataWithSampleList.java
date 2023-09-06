package scripts;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import fr.cea.ig.lfw.controllers.scripts.buffered.ScriptWithExcelBody;
import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.ngl.NGLConfig;
import models.Constants;
import play.Logger;
import services.instance.sample.UpdateFlagForReportingData;
import validation.ContextValidation;

/**
 * Script permettant de détecter les objets récemments modifiés pour qu'ils soient pris en charge par le reporting nocturne de data.
 * Ici on écrase le comportement par défaut en spécifiant une liste de code sample dans un Excel.
 * Une colonne par ligne, avec le code sample.
 * 
 * @author jcharpen - Jordi CHARPENTIER - jcharpen@genoscope.cns.fr
 */
public class ScriptUpdateFlagForReportingDataWithSampleList extends ScriptWithExcelBody {

	private NGLApplication app;

	private NGLConfig config;

	@Inject
	public ScriptUpdateFlagForReportingDataWithSampleList(NGLApplication app, NGLConfig config) {
		this.app = app;
		this.config = config;
	}

	@Override
	public void execute(XSSFWorkbook workbook) throws Exception {
		Logger.info("Start ScriptUpdateFlagForReportingDataWithSampleList");

		ContextValidation contextError = ContextValidation.createUndefinedContext(Constants.NGL_DATA_USER);
		UpdateFlagForReportingData update = new UpdateFlagForReportingData(app, config);

		List<String> sampleCodeList = handleArgs(workbook);
		Logger.debug("Nb sample to update "+sampleCodeList.size());
		update.runImport(contextError, sampleCodeList);
		
		Logger.info("End ScriptUpdateFlagForReportingDataWithSampleList");
	}

	/**
	 * Méthode permettant de gérer les arguments donnés au script.
	 * Ici, une liste de code sample.
	 * 
	 * @param workbook Un objet correspondant au Excel donné en paramètres.
	 * 
	 * @return Une liste de chaîne de caractères correspondant à des codes samples qu'on veut mettre à jour.
	 */
	private List<String> handleArgs(XSSFWorkbook workbook) {
		List<String> sampleCodeList = new ArrayList<>();

		workbook.getSheetAt(0).rowIterator().forEachRemaining(row -> {
			String sampleCode = row.getCell(0).getStringCellValue();
			sampleCodeList.add(sampleCode);
		});

		return sampleCodeList;
	}
}