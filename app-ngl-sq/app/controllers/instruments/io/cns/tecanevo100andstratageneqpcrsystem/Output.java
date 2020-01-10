package controllers.instruments.io.cns.tecanevo100andstratageneqpcrsystem;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import controllers.instruments.io.cns.tecanevo100andstratageneqpcrsystem.tpl.txt.qpcrquantification;
import controllers.instruments.io.utils.AbstractOutput;
import controllers.instruments.io.utils.File;
import controllers.instruments.io.utils.OutputHelper;
import models.laboratory.experiment.instance.Experiment;
import validation.ContextValidation;

public class Output extends AbstractOutput {

	@Override
	public File generateFile(Experiment experiment, ContextValidation contextValidation) throws Exception {
//		String type = (String)contextValidation.getObject("type");
		String content=null;
		//tube / 96-well-plate
		if ("qpcr-quantification".equals(experiment.typeCode)) {
			content = OutputHelper.format(qpcrquantification.render(getSampleSheetStratageneLines(experiment)).body());
		} else {
			//rna-prep; pcr-purif; normalization-and-pooling a venir.....
			throw new RuntimeException(experiment.typeCode + " not managed");
		}
		File file = new File(getFileName(experiment) + ".csv", content);
		return file;
	}
	
	private List<SampleSheetStrategeneLine> getSampleSheetStratageneLines(Experiment experiment) {
		Map<Integer, String> results = experiment.atomicTransfertMethods
			.stream()
			.map(atm -> atm.inputContainerUseds)
			.flatMap(List::stream)
			.filter(icu -> (icu.instrumentProperties != null && icu.instrumentProperties.containsKey("qPCRposition")))
			.collect(Collectors.toMap(icu -> Integer.valueOf(icu.instrumentProperties.get("qPCRposition").value.toString()), icu -> icu.code));
		
		List<SampleSheetStrategeneLine> sampleSheetStrategeneLines = new ArrayList<>();
		
		sampleSheetStrategeneLines.addAll(getSubElt("A", 4, results.get(1)));
		sampleSheetStrategeneLines.addAll(getSubElt("B", 4, results.get(2)));
		sampleSheetStrategeneLines.addAll(getSubElt("C", 4, results.get(3)));
		sampleSheetStrategeneLines.addAll(getSubElt("D", 4, results.get(4)));
		sampleSheetStrategeneLines.addAll(getSubElt("E", 4, results.get(5)));
		sampleSheetStrategeneLines.addAll(getSubElt("F", 4, results.get(6)));
		sampleSheetStrategeneLines.addAll(getSubElt("G", 4, results.get(7)));
		sampleSheetStrategeneLines.addAll(getSubElt("H", 4, results.get(8)));
		sampleSheetStrategeneLines.addAll(getSubElt("A", 8, results.get(9)));
		sampleSheetStrategeneLines.addAll(getSubElt("B", 8, results.get(10)));
		sampleSheetStrategeneLines.addAll(getSubElt("C", 8, results.get(11)));
		sampleSheetStrategeneLines.addAll(getSubElt("D", 8, results.get(12)));
		sampleSheetStrategeneLines.addAll(getSubElt("E", 8, results.get(13)));
		sampleSheetStrategeneLines.addAll(getSubElt("F", 8, results.get(14)));
		sampleSheetStrategeneLines.addAll(getSubElt("G", 8, results.get(15)));
		sampleSheetStrategeneLines.addAll(getSubElt("H", 8, results.get(16)));
		
		if (results.get(17) != null) {
			sampleSheetStrategeneLines.add(new SampleSheetStrategeneLine("A12", results.get(17) + "_1"));
			sampleSheetStrategeneLines.add(new SampleSheetStrategeneLine("B12", results.get(17) + "_1"));
			sampleSheetStrategeneLines.add(new SampleSheetStrategeneLine("C12", results.get(17) + "_2"));
			sampleSheetStrategeneLines.add(new SampleSheetStrategeneLine("D12", results.get(17) + "_2"));
		}
		return sampleSheetStrategeneLines;
	}

	private List<SampleSheetStrategeneLine> getSubElt(
			String line, int startIndex, String containerCode) {
		List<SampleSheetStrategeneLine> sampleSheetStrategeneLines = new ArrayList<>(0);
		if(null != containerCode){
			for(int i = 0; i < 4 ; i++){
				String wellId = line+(startIndex+i);
				String wellName = containerCode+"_"+((i < 2)?"1":"2");
				sampleSheetStrategeneLines.add(new SampleSheetStrategeneLine(wellId, wellName));
			}
		}
		return sampleSheetStrategeneLines;
	}

	private String getFileName(Experiment experiment) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyMMdd");
		return experiment.typeCode.toUpperCase()+"_"+experiment.atomicTransfertMethods.get(0).inputContainerUseds.get(0).locationOnContainerSupport.code+"_"+sdf.format(new Date());
	}

}
