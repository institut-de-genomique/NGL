package controllers.instruments.io.cns.biomekfx;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


import controllers.instruments.io.cns.biomekfx.Output;
import controllers.instruments.io.cns.biomekfx.tpl.txt.*;
import controllers.instruments.io.utils.AbstractOutput;
import controllers.instruments.io.utils.File;
import controllers.instruments.io.utils.OutputHelper;
import models.laboratory.experiment.instance.AtomicTransfertMethod;
import models.laboratory.experiment.instance.Experiment;
import models.laboratory.experiment.instance.InputContainerUsed;
import models.laboratory.experiment.instance.OutputContainerUsed;
import validation.ContextValidation;

//_CTX_PARAM: use AbstractTypedOutput

public class Output extends AbstractOutput {

	private static final play.Logger.ALogger logger = play.Logger.of(Output.class);
	private static final String inputType="INPUT";
	private static final String outputType="OUTPUT";

	@Override
	public File generateFile(Experiment experiment, ContextValidation contextValidation) throws Exception {
		String type = (String)contextValidation.getObject("type");

		String content = null;
		String fdrType=null;
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
		}else if (experiment.typeCode.equals("pool") && 
					"96-well-plate".equals(experiment.instrument.outContainerSupportCategoryCode) && 
					"96-well-plate".equals(experiment.instrument.inContainerSupportCategoryCode)) {
			Object ftype =contextValidation.getObject("fdrType");
			if ("dna".equals(ftype) ){	
				fdrType="dna";
				logger.info("generation feuille de route Biomek FX / exp="+ experiment.typeCode + "/ type="+ fdrType );
				// 21/09/2016 appeler une methode pour generer la liste des lignes a mettre dans la feuille de route
				content = OutputHelper.format(pool_PlatesToPlate_samples.render(getSampleSheetPoolLines(experiment,inputType)).body());

			} else if ("buffer".equals(ftype)) {
				fdrType="buffer";
				logger.info("generation feuille de route Biomek FX / exp="+ experiment.typeCode + "/ type="+ fdrType );
				content = OutputHelper.format(pool_PlatesToPlate_buffer.render(getSampleSheetPoolLines(experiment,outputType)).body());	

			} else {
				throw new RuntimeException("SampleSheet type not managed : "+experiment.typeCode + "/" +ftype);
			}
		}
		String suffix ="";
		if ("normalisation".equals(type)){
			suffix="_norm";
		}else if ("normalisation-post-pcr".equals(type)){
			suffix="_norm_post_pcr";
		}else if(experiment.typeCode.equals("pool") && fdrType!=null){
			suffix+="-"+fdrType;
		}
		File file = new File(getFileName(experiment)+suffix+".csv", content);
		return file;
	}

	private String getFileName(Experiment experiment) {
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

		InputContainerUsed   icu  = atm.inputContainerUseds.get(0);
		OutputContainerUsed  ocu  = atm.outputContainerUseds.get(0);
		PlateSampleSheetLine pssl = new PlateSampleSheetLine();

		pssl.inputContainerCode = icu.code;
		pssl.outputContainerCode = ocu.code;

		if (icu.experimentProperties != null && icu.experimentProperties.containsKey("inputVolume")) {
			pssl.inputVolume = (Double)icu.experimentProperties.get("inputVolume").value;
		} else if (ocu.volume != null && ocu.volume.value != null) {
			pssl.inputVolume = (Double)ocu.volume.value;
		}
		if (icu.experimentProperties != null && icu.experimentProperties.containsKey("bufferVolume")) {
			pssl.bufferVolume = (Double)icu.experimentProperties.get("bufferVolume").value;
		}
		pssl.dwell = OutputHelper.getNumberPositionInPlateByLine(ocu.locationOnContainerSupport.line, ocu.locationOnContainerSupport.column);

		if ("tube".equals(inputContainerCategory)) {
			pssl.sourceADN = getSourceADN(ocu.locationOnContainerSupport.line, ocu.locationOnContainerSupport.column);
			pssl.swellADN  = getSwellADN(ocu.locationOnContainerSupport.line, ocu.locationOnContainerSupport.column);
		} else if ("plate".equals(inputContainerCategory)) {
			pssl.sourceADN = sourceMapping.get(icu.locationOnContainerSupport.code);
			pssl.swellADN  = OutputHelper.getNumberPositionInPlateByLine(icu.locationOnContainerSupport.line, icu.locationOnContainerSupport.column);
		}

		return pssl;
	}

	private String getSourceADN(String line, String column) {
		int col = Integer.valueOf(column);
		switch (line) {
		case "A" : case "B" : case "C" : case "D" : return col < 7 ? "A1-D6" : "A7-D12";
		case "E" : case "F" : case "G" : case "H" : return col < 7 ? "E1-H6" : "E7-H12";
		default                                   : return null;
		}
	}


	private Integer getSwellADN(String line, String column) {
		int col = Integer.valueOf(column);
		switch (line) {
		case "A" : case "E" : return col < 7 ? col +  0 : col - 6 +  0;
		case "B" : case "F" : return col < 7 ? col +  6 : col - 6 +  6;
		case "C" : case "G" : return col < 7 ? col + 12 : col - 6 + 12;
		case "D" : case "H" : return col < 7 ? col + 18 : col - 6 + 18;
		default             : return null;
		}
	}

	private Map<String, String> getSourceMapping(Experiment experiment) {
		Map<String, String> sources = new HashMap<>();

		String[] inputContainerSupportCodes = experiment.inputContainerSupportCodes.toArray(new String[0]);
		Arrays.sort(inputContainerSupportCodes);
		for (int i = 0; i < inputContainerSupportCodes.length ; i++) {
			sources.put(inputContainerSupportCodes[i], "Src" + (i+1));
		}
		return sources;
	}



	private List<SampleSheetPoolLine> getSampleSheetPoolLines(Experiment experiment, String type) {
		Map<String, String> sourceMapping = getSourceMapping(experiment);

		List<SampleSheetPoolLine> lines = new ArrayList<>();

		experiment.atomicTransfertMethods.forEach(atm -> {


			OutputContainerUsed output = atm.outputContainerUseds.get(0);
			if(type.equals(outputType)){
				lines.add(getSampleSheetPoolLine(null, output, sourceMapping, experiment)); //14/06/2018 ajout experiment en parametre
			}else{
				atm.inputContainerUseds.forEach(input -> {
					lines.add(getSampleSheetPoolLine(input, output, sourceMapping, experiment)); //14/06/2018 ajout experiment en parametre
				});	
			}
		});

		return lines;
	}

	private SampleSheetPoolLine getSampleSheetPoolLine(InputContainerUsed input, 
			OutputContainerUsed output,
			Map<String, String> sourceMapping,
			Experiment experiment) {
		SampleSheetPoolLine sspl = new SampleSheetPoolLine();

		if(input!=null){
			sspl.inputSupportCode = input.locationOnContainerSupport.code;
			sspl.inputSupportContainerPosition = OutputHelper.getNumberPositionInPlateByLine(input.locationOnContainerSupport.line, input.locationOnContainerSupport.column);
			sspl.inputContainerCode = sspl.inputSupportCode+"_"+input.locationOnContainerSupport.line+input.locationOnContainerSupport.column;

			if (! "plates-to-plate".equals(experiment.typeCode) ){
				sspl.inputSupportContainerVolume = (Double)input.experimentProperties.get("inputVolume").value; 
			} else {
				// 14/06/2018 dans le cas de "plates-to-plate" il n'y a pas de pooling, on transfere tout le contenu de l'input !! ( inputVolume n'existe pas..)
				// !! cas de puits sans volume ???
				if (input.volume != null) {
					sspl.inputSupportContainerVolume = (Double)input.volume.value; 
				} else {
					sspl.inputSupportContainerVolume = 0.0;
				}
			}
			sspl.inputSupportSource =  sourceMapping.get(input.locationOnContainerSupport.code);

		}
		if(output.experimentProperties!=null && output.experimentProperties.get("bufferVolume")!=null){
			Double bufferVolume = (Double)output.experimentProperties.get("bufferVolume").value;
			if(bufferVolume<0.0){
				bufferVolume=0.0;
			}
			sspl.outputSupportContainerBufferVolume=bufferVolume;
		}else{
			sspl.outputSupportContainerBufferVolume=0.0;
		}

		sspl.outputSupportCode = output.locationOnContainerSupport.code;
		sspl.outputContainerCode = sspl.outputSupportCode+"_"+output.locationOnContainerSupport.line+output.locationOnContainerSupport.column;
		sspl.outputSupportPosition = OutputHelper.getNumberPositionInPlateByLine(output.locationOnContainerSupport.line, output.locationOnContainerSupport.column);

		return sspl;
	}

}
