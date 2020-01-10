package controllers.instruments.io.common.hiseq;

import java.util.List;

import models.laboratory.container.instance.Container;
import models.laboratory.experiment.instance.Experiment;
import validation.ContextValidation;
import controllers.instruments.io.common.hiseq.tpl.txt.sampleSheet_1;
import controllers.instruments.io.utils.AbstractOutput;
import controllers.instruments.io.utils.File;
import controllers.instruments.io.utils.OutputHelper;
import controllers.instruments.io.utils.TagModel;

public abstract class HiSeqOutput extends AbstractOutput {

	@Override
	public File generateFile(Experiment experiment, ContextValidation contextValidation) {
		List<Container> containers = OutputHelper.getInputContainersFromExperiment(experiment);
		TagModel tagModel = OutputHelper.getTagModel(containers);
		String content = OutputHelper.format(sampleSheet_1.render(experiment,containers,tagModel).body());
		String filename = OutputHelper.getInstrumentPath(experiment.instrument.code)+containers.get(0).support.code+".csv";
		File file = new File(filename, content);
		OutputHelper.writeFile(file);
		return file;
	}

}
