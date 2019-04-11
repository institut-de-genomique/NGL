package controllers.instruments.io.cns.biomekfx;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import controllers.instruments.io.cns.biomekfx.tpl.txt.normalisation_post_pcr_x_to_plate;
import controllers.instruments.io.cns.biomekfx.tpl.txt.normalisation_x_to_plate;
import controllers.instruments.io.cns.biomekfx.tpl.txt.x_to_plate;
import controllers.instruments.io.cns.biomekfx.tpl.txt.x_to_plate_no_calcul;
//import controllers.instruments.io.cns.tecanevo100.SampleSheetPoolLine;
import controllers.instruments.io.utils.AbstractOutput;
import controllers.instruments.io.utils.File;
import controllers.instruments.io.utils.OutputHelper;
//import org.apache.commons.collections.CollectionUtils;
//import play.Logger;
import models.laboratory.experiment.instance.AtomicTransfertMethod;
import models.laboratory.experiment.instance.Experiment;
import models.laboratory.experiment.instance.InputContainerUsed;
import models.laboratory.experiment.instance.OutputContainerUsed;
import validation.ContextValidation;

public class Output extends AbstractOutput {

	@Override
	public File generateFile(Experiment experiment, ContextValidation contextValidation) throws Exception {
		String type = (String)contextValidation.getObject("type");
		
		String content = null;
		//tube / 96-well-plate
		if ("normalisation".equals(type) && "tube".equals(experiment.instrument.inContainerSupportCategoryCode)
				                         && "96-well-plate".equals(experiment.instrument.outContainerSupportCategoryCode)) {
			// feuille de route specifique pour les pools de plaques -> plaque
			content = OutputHelper.format(normalisation_x_to_plate.render(getPlateSampleSheetLines(experiment, "tube")).body());
		} else if ("normalisation".equals(type) && "96-well-plate".equals(experiment.instrument.inContainerSupportCategoryCode)
				&& "96-well-plate".equals(experiment.instrument.outContainerSupportCategoryCode)) {
			// feuille de route specifique pour les pools de plaques -> plaque
			content = OutputHelper.format(normalisation_x_to_plate.render(getPlateSampleSheetLines(experiment, "plate")).body());
		} else if ("normalisation-post-pcr".equals(type) && "96-well-plate".equals(experiment.instrument.inContainerSupportCategoryCode)
				&& "96-well-plate".equals(experiment.instrument.outContainerSupportCategoryCode)) {
			// feuille de route specifique pour les pools de plaques -> plaque
			content = OutputHelper.format(normalisation_post_pcr_x_to_plate.render(getPlateSampleSheetLines(experiment, "plate")).body());
		} else if ("tubes-to-plate".equals(type)) {
			// feuille de route specifique pour les pools de tubes -> plaque
			content = OutputHelper.format(x_to_plate.render(getPlateSampleSheetLines(experiment, "tube")).body());
		} else if ("plates-to-plate".equals(type)) {
			content = OutputHelper.format(x_to_plate.render(getPlateSampleSheetLines(experiment, "plate")).body());
		} else if("plates-to-plate-nocalcul".equals(type)){
			content = OutputHelper.format(x_to_plate_no_calcul.render(getPlateSampleSheetLines(experiment, "plate")).body());
		}else {
			//rna-prep; pcr-purif; normalization-and-pooling a venir.....
			throw new RuntimeException("Biomek-FX sampleSheet io combination not managed : "+experiment.instrument.inContainerSupportCategoryCode+" / "+experiment.instrument.outContainerSupportCategoryCode);
		}
		String suffix ="";
		if ("normalisation".equals(type)){
			suffix="_norm";
		}else if ("normalisation-post-pcr".equals(type)){
			suffix="_norm_post_pcr";
		}else if("plates-to-plate-nocalcul".equals(type)){
			suffix="_no_norm";
		}
		File file = new File(getFileName(experiment)+suffix+".csv", content);
		return file;
	}

	private String getFileName(Experiment experiment) {
//		SimpleDateFormat sdf = new SimpleDateFormat("yyyMMdd");
		return experiment.code.toUpperCase();
	}

	private List<PlateSampleSheetLine> getPlateSampleSheetLines(Experiment experiment, String inputContainerCategory) {

		return experiment.atomicTransfertMethods
			.parallelStream()
			.map(atm -> getPlateSampleSheetLine(atm,inputContainerCategory, experiment))
			.collect(Collectors.toList());		
	}

	private PlateSampleSheetLine getPlateSampleSheetLine(AtomicTransfertMethod atm, String inputContainerCategory,Experiment experiment) {
		Map<String, String> sourceMapping = getSourceMapping(experiment);
//		Map<String, String> destPositionMapping = getDestMapping(experiment);

		InputContainerUsed icu = atm.inputContainerUseds.get(0);
		OutputContainerUsed ocu = atm.outputContainerUseds.get(0);
		PlateSampleSheetLine pssl = new PlateSampleSheetLine();

		pssl.inputContainerCode = icu.code;
		pssl.outputContainerCode = ocu.code;

		if (icu.experimentProperties != null && icu.experimentProperties.containsKey("inputVolume")) {
			pssl.inputVolume = (Double)icu.experimentProperties.get("inputVolume").value;
		} else if(ocu.volume != null && ocu.volume.value != null) {
			pssl.inputVolume = (Double)ocu.volume.value;
		}
		if (icu.experimentProperties != null && icu.experimentProperties.containsKey("bufferVolume")) {
			pssl.bufferVolume = (Double)icu.experimentProperties.get("bufferVolume").value;
		}
		pssl.dwell = OutputHelper.getNumberPositionInPlateByLine(ocu.locationOnContainerSupport.line, ocu.locationOnContainerSupport.column);

		if ("tube".equals(inputContainerCategory)) {
			pssl.sourceADN = getSourceADN(ocu.locationOnContainerSupport.line, ocu.locationOnContainerSupport.column);
			pssl.swellADN = getSwellADN(ocu.locationOnContainerSupport.line, ocu.locationOnContainerSupport.column);
		} else if("plate".equals(inputContainerCategory)) {
			pssl.sourceADN = sourceMapping.get(icu.locationOnContainerSupport.code);
			pssl.swellADN = OutputHelper.getNumberPositionInPlateByLine(icu.locationOnContainerSupport.line, icu.locationOnContainerSupport.column);
		}

		return pssl;
	}

	// TODO: define possibly as Set<String>
	private static List<String> grp1 = Arrays.asList("A","B","C","D"); 
	private static List<String> grp2 = Arrays.asList("E","F","G","H"); 

	private String getSourceADN(String line, String column) {
		String value = null;
		Integer col = Integer.valueOf(column);

		if (grp1.contains(line) && col < 7) {
			value = "A1-D6";
		} else if(grp1.contains(line) && col >= 7) {
			value = "A7-D12";
		} else if(grp2.contains(line) && col < 7) {
			value = "E1-H6";
		} else if(grp2.contains(line) && col >= 7) {
			value = "E7-H12";
		}
		return value;
	}

	private Integer getSwellADN(String line, String column) {

		Integer value = null;
		Integer col = Integer.valueOf(column);

		if("A".equals(line) || "E".equals(line)){
			value = (col < 7)?col:col-6;
		} else if("B".equals(line) || "F".equals(line)){
			value = (col < 7)?col+6:col-6+6;
		} else if("C".equals(line) || "G".equals(line)){
			value = (col < 7)?col+12:col-6+12;
		} else if("D".equals(line) || "H".equals(line)){
			value = (col < 7)?col+18:col-6+18;
		}

		return value;
	}

	private Map<String, String> getSourceMapping(Experiment experiment) {
		Map<String, String> sources = new HashMap<>();

		String[] inputContainerSupportCodes = experiment.inputContainerSupportCodes.toArray(new String[0]);
		Arrays.sort(inputContainerSupportCodes);
		for (int i = 0; i < inputContainerSupportCodes.length ; i++) {
			sources.put(inputContainerSupportCodes[i], "Src"+(i+1));
		}
		return sources;
	}

//	private Map<String, String> getDestMapping(Experiment experiment) {
//		Map<String, String> dest = new HashMap<String, String>();
//
//		String[] outputContainerSupportCodes = experiment.outputContainerSupportCodes.toArray(new String[0]);
//		Arrays.sort(outputContainerSupportCodes);
//		for(int i = 0; i < outputContainerSupportCodes.length ; i++){
//			dest.put(outputContainerSupportCodes[i], (i+1)+"");
//		}
//		return dest;
//	}
	
}
