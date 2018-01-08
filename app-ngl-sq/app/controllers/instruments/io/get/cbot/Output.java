package controllers.instruments.io.get.cbot;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import models.laboratory.container.instance.Container;
import models.laboratory.container.instance.Content;
import models.laboratory.experiment.instance.Experiment;
import validation.ContextValidation;
import controllers.instruments.io.get.cbot.tpl.txt.sampleSheet_cbot;
import controllers.instruments.io.utils.AbstractOutput;
import controllers.instruments.io.utils.CsvHelper;
import controllers.instruments.io.utils.File;
import controllers.instruments.io.utils.OutputHelper;
import controllers.instruments.io.utils.TagModel;
import play.Logger;

//set output file .csv
public class Output extends AbstractOutput {
	
	@Override
	public File generateFile(Experiment experiment, ContextValidation contextValidation) {
		String ftype = null;
		String content = null;
		String filename =null;
		
		//get container
		List<Container> containers = OutputHelper.getInputContainersFromExperiment(experiment);
		Logger.debug("Output- containers : "+ containers.size());
//		//get instrument
//		TagModel tagModel = OutputHelper.getTagModel(containers);
//		Logger.debug("Output- tagModel : "+ tagModel.tagType);
		
		content = OutputHelper.format(sampleSheet_cbot.render(experiment,containers).body());
//		Logger.debug("Output- content : "+ content);
//		content = OutputHelper.format(sampleSheet_HS3000.render(experiment,containers,tagModel).body());
		//set destination
		//filename = "C:/Users/Public/Downloads/" + filename;
		//String path=new File("").getAbsolutePath();
		//set file name
			filename = OutputHelper.getInstrumentPath(experiment.instrument.code)+(new SimpleDateFormat("yyyyMMdd")).format(new Date()) + "_" + experiment.instrument.code + "_" + containers.get(0).support.code + ".csv";
		
		//Logger.debug("filename dans Output : "+ OutputHelper.getInstrumentPath(experiment.instrument.code)+", "+ (new SimpleDateFormat("yyyyMMdd")).format(new Date()) + "_" + experiment.instrument.code + "_" + ftype + "_" + containers.get(0).support.code+".csv");
		//String filename = "/tmp/" + (new SimpleDateFormat("yyyyMMdd")).format(new Date()) + "_" + experiment.instrument.code + "_" + ftype + "_" + containers.get(0).support.code+".csv";
		File file = new File(filename, content);
		OutputHelper.writeFile(file);
		return file;
	}
}
