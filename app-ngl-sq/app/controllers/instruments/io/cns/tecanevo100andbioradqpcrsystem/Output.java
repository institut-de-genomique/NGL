package controllers.instruments.io.cns.tecanevo100andbioradqpcrsystem;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import controllers.instruments.io.cns.tecanevo100andbioradqpcrsystem.tpl.txt.qpcrquantification;
import controllers.instruments.io.utils.AbstractOutput;
import controllers.instruments.io.utils.File;
import controllers.instruments.io.utils.OutputHelper;
import models.laboratory.experiment.instance.Experiment;
import validation.ContextValidation;

public class Output extends AbstractOutput {

	@Override
	public File generateFile(Experiment experiment, ContextValidation contextValidation) throws Exception {
		String content=null;
		//tube / 96-well-plate
		if ("qpcr-quantification".equals(experiment.typeCode)) {
			content = OutputHelper.format(qpcrquantification.render(getSampleSheetBioradLines(experiment)).body());
		} else {
			throw new RuntimeException(experiment.typeCode + " not managed");
		}
		File file = new File(getFileName(experiment) + ".csv", content);
		return file;
	}
	
	private List<SampleSheetBioradLine> getSampleSheetBioradLines(Experiment experiment) {
		Map<Integer, String> results = experiment.atomicTransfertMethods
			.stream()
			.map(atm -> atm.inputContainerUseds)
			.flatMap(List::stream)
			.filter(icu -> (icu.instrumentProperties != null && icu.instrumentProperties.containsKey("qPCRposition")))
			.collect(Collectors.toMap(icu -> Integer.valueOf(icu.instrumentProperties.get("qPCRposition").value.toString()), icu -> icu.code));
		
		List<SampleSheetBioradLine> sampleSheetBioradLines = new ArrayList<>();
		
		sampleSheetBioradLines.addAll(getSubElt("A", 4, results.get(1)));
		sampleSheetBioradLines.addAll(getSubElt("B", 4, results.get(2)));
		sampleSheetBioradLines.addAll(getSubElt("C", 4, results.get(3)));
		sampleSheetBioradLines.addAll(getSubElt("D", 4, results.get(4)));
		sampleSheetBioradLines.addAll(getSubElt("E", 4, results.get(5)));
		sampleSheetBioradLines.addAll(getSubElt("F", 4, results.get(6)));
		sampleSheetBioradLines.addAll(getSubElt("G", 4, results.get(7)));
		sampleSheetBioradLines.addAll(getSubElt("H", 4, results.get(8)));
		sampleSheetBioradLines.addAll(getSubElt("A", 8, results.get(9)));
		sampleSheetBioradLines.addAll(getSubElt("B", 8, results.get(10)));
		sampleSheetBioradLines.addAll(getSubElt("C", 8, results.get(11)));
		sampleSheetBioradLines.addAll(getSubElt("D", 8, results.get(12)));
		sampleSheetBioradLines.addAll(getSubElt("E", 8, results.get(13)));
		sampleSheetBioradLines.addAll(getSubElt("F", 8, results.get(14)));
		sampleSheetBioradLines.addAll(getSubElt("G", 8, results.get(15)));
		sampleSheetBioradLines.addAll(getSubElt("H", 8, results.get(16)));
		
		if (results.get(17) != null) {
		    sampleSheetBioradLines.add(new SampleSheetBioradLine("A", "12", results.get(17) + "_1"));
		    sampleSheetBioradLines.add(new SampleSheetBioradLine("B", "12", results.get(17) + "_1"));
		    sampleSheetBioradLines.add(new SampleSheetBioradLine("C", "12", results.get(17) + "_2"));
		    sampleSheetBioradLines.add(new SampleSheetBioradLine("D", "12", results.get(17) + "_2"));
		}
		return sampleSheetBioradLines;
	}

	private List<SampleSheetBioradLine> getSubElt(String row, int startIndex, String containerCode) {
	    if(containerCode == null) return Collections.emptyList();
	    return IntStream.range(0, 4).mapToObj(i -> {
	        String column = Integer.toString(startIndex + i);
	        String wellName = containerCode+"_"+((i < 2)?"1":"2");
	        return new SampleSheetBioradLine(row, column, wellName);
	    }).collect(Collectors.toList());
	}

	private String getFileName(Experiment experiment) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyMMdd");
		return experiment.typeCode.toUpperCase()+"_"+experiment.atomicTransfertMethods.get(0).inputContainerUseds.get(0).locationOnContainerSupport.code+"_"+sdf.format(new Date());
	}

}
