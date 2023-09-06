/* 25/10/2016 FDS: creation */

package controllers.instruments.io.cng.epmotion;

import java.text.SimpleDateFormat;
import java.util.Date;

import controllers.instruments.io.cng.epmotion.tpl.txt.sampleSheet_1;
import controllers.instruments.io.utils.AbstractOutput;
import controllers.instruments.io.utils.File;
import controllers.instruments.io.utils.OutputHelper;
import models.laboratory.experiment.instance.Experiment;
import validation.ContextValidation;

public class Output extends AbstractOutput {

	private static final play.Logger.ALogger logger = play.Logger.of(Output.class);
	
	@Override
	public File generateFile(Experiment experiment, ContextValidation contextValidation) throws Exception {
		String FDStestparam="when specs ready";// test passage parametre a la feuille de route...
		
		logger.info("generation feuille de route Epimotion / exp="+ experiment.typeCode );
		String content = OutputHelper.format(sampleSheet_1.render(experiment, FDStestparam).body());
		
		File file = new File(getFileName(experiment)+".csv", content);
		return file;
	}
	
	private String getFileName(Experiment experiment) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyMMdd");
		// c'est le premier container qui donne son nom a la feuille de route. Ce mecanisme était fait pour un ouput container Support plaque
		// pas tres pertinent pour tubes... mettre plutot la plaque input dans le nom ???
		
		// return experiment.typeCode.toUpperCase()+"_"+experiment.atomicTransfertMethods.get(0).outputContainerUseds.get(0).locationOnContainerSupport.code+"_"+sdf.format(new Date());
		return experiment.typeCode.toUpperCase()+"_"+experiment.atomicTransfertMethods.get(0).inputContainerUseds.get(0).locationOnContainerSupport.code+"_"+sdf.format(new Date());
	}
	
}