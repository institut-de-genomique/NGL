package controllers.instruments.io.cns.gilsonpipetmax;

import java.text.SimpleDateFormat;
import java.util.Date;

import models.laboratory.experiment.instance.Experiment;
import validation.ContextValidation;
import controllers.instruments.io.utils.AbstractOutput;
import controllers.instruments.io.utils.File;

public class Output2 extends AbstractOutput {

	@Override
	public File generateFile(Experiment experiment, ContextValidation contextValidation) throws Exception {
		//		String type = (String)contextValidation.getObject("type");
		String content = null;
		Boolean isPlaque = "96-well-plate".equals(experiment.instrument.inContainerSupportCategoryCode);

		//Le type des entrées et sorties peut être tube / plaque ou les 2

		//if("96-well-plate".equals(experiment.instrument.outContainerSupportCategoryCode)){

		if ("normalisation".equals(experiment.typeCode)){
		//	content = OutputHelper.format(normalisation_x_to_plate.render(experiment).body());
		} else {
			throw new RuntimeException(experiment.typeCode + " not managed");
		}

		File file = new File(getFileName(experiment) + ".csv", content);
		return file;
	}
	
	private String getFileName(Experiment experiment) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyMMdd");
		return experiment.typeCode.toUpperCase() 
			   + "_" + experiment.atomicTransfertMethods.get(0).inputContainerUseds.get(0).locationOnContainerSupport.code
			   + "_" + sdf.format(new Date());
	}

}
