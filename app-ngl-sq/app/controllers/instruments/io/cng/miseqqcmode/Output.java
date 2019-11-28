package controllers.instruments.io.cng.miseqqcmode;



import java.util.List;

import controllers.instruments.io.cng.miseqqcmode.tpl.txt.sampleSheet_1;
import controllers.instruments.io.cng.miseqqcmode.tpl.txt.sampleSheet_2;
import controllers.instruments.io.utils.AbstractOutput;
import controllers.instruments.io.utils.File;
import controllers.instruments.io.utils.OutputHelper;
import controllers.instruments.io.utils.TagModel;
import models.laboratory.container.instance.Container;
import models.laboratory.experiment.instance.Experiment;
import validation.ContextValidation;

public class Output extends AbstractOutput {

	@Override
	public File generateFile(Experiment experiment, ContextValidation contextValidation) {
		List<Container> containers = OutputHelper.getInputContainersFromExperiment(experiment);
		TagModel tagModel = OutputHelper.getTagModel(containers);
		String content = null;
		if (!"DUAL-INDEX".equals(tagModel.tagType)) {
			content = OutputHelper.format(sampleSheet_1.render(experiment,containers).body());			
		} else {
			content = OutputHelper.format(sampleSheet_2.render(experiment,containers).body());	
		}
		String filename = OutputHelper.getInstrumentPath(experiment.instrument.code)+experiment.instrumentProperties.get("miseqReagentCassette").value+".csv";
		File file = new File(filename, content);
		
		OutputHelper.writeFile(file);
		return file;
	}

}
