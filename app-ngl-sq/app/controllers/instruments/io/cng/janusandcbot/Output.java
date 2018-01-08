package controllers.instruments.io.cng.janusandcbot;



import java.text.SimpleDateFormat;
import java.util.Date;

import models.laboratory.experiment.instance.Experiment;
import validation.ContextValidation;
import controllers.instruments.io.cng.janusandcbot.tpl.txt.sampleSheet_1;
import controllers.instruments.io.utils.AbstractOutput;
import controllers.instruments.io.utils.File;
import controllers.instruments.io.utils.OutputHelper;

public class Output extends AbstractOutput {

	@Override
	public File generateFile(Experiment experiment,
			ContextValidation contextValidation) throws Exception {
		Integer stripDestination = Integer.valueOf((String)OutputHelper.getInstrumentProperty(experiment, "stripDestination"));
		
		String content = OutputHelper.format(sampleSheet_1.render(experiment, stripDestination).body());
		File file = new File(getFileName(experiment)+".csv", content);
		return file;
	}
	
	private String getFileName(Experiment experiment) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyMMdd");
		return experiment.typeCode.toUpperCase()+"_"+experiment.atomicTransfertMethods.get(0).outputContainerUseds.get(0).locationOnContainerSupport.code+"_"+sdf.format(new Date());
	}

}
