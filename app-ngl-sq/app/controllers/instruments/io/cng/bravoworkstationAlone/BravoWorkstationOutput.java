/* 14/11/2017 renommé pour etre commun aux differnent instrument hybrides incluant le bravoworkstation */
package controllers.instruments.io.cng.bravoworkstationAlone;

import java.text.SimpleDateFormat;
import java.util.Date;

import controllers.instruments.io.cng.bravoworkstationAlone.tpl.txt.sampleSheet_1;
import controllers.instruments.io.utils.AbstractOutput;
import controllers.instruments.io.utils.File;
import controllers.instruments.io.utils.OutputHelper;
import models.laboratory.experiment.instance.Experiment;
import validation.ContextValidation;

public class BravoWorkstationOutput extends AbstractOutput {

	private static final play.Logger.ALogger logger = play.Logger.of(BravoWorkstationOutput.class);
	
	@Override
	public File generateFile(Experiment experiment, ContextValidation contextValidation) throws Exception {
		logger.info("generation feuille de route BravoWorkstation / exp={}", experiment.typeCode);
		String content = OutputHelper.format(sampleSheet_1.render(experiment).body());
		File file = new File(getFileName(experiment) + ".csv", content);
		return file;
	}
	
	private String getFileName(Experiment experiment) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyMMdd");
		// c'est le premier container qui donne son nom a la feuille de route. Ce mecanisme était fait pour un ouput container Support plaque
		// pas tres pertinent pour tubes... mettre plutot la plaque input dans le nom ???
		return experiment.typeCode.toUpperCase()
				+ "_" + experiment.atomicTransfertMethods.get(0).inputContainerUseds.get(0).locationOnContainerSupport.code
				+ "_" + sdf.format(new Date());
	}
	
}