package controllers.instruments.io.cns.gilsonpipetmax;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.gargoylesoftware.htmlunit.javascript.host.Console;

import models.laboratory.experiment.instance.Experiment;
import models.laboratory.experiment.instance.InputContainerUsed;
import models.laboratory.experiment.instance.OutputContainerUsed;
import play.Logger;
import validation.ContextValidation;
import controllers.instruments.io.cns.gilsonpipetmax.Output;
import controllers.instruments.io.cns.gilsonpipetmax.GilsonSampleSheetLine;
import controllers.instruments.io.cns.gilsonpipetmax.tpl.txt.*;
import controllers.instruments.io.utils.AbstractOutput;
import controllers.instruments.io.utils.File;
import controllers.instruments.io.utils.OutputHelper;

public class Output extends AbstractOutput {
	private static final play.Logger.ALogger logger = play.Logger.of(Output.class);
	private static int sampleNum;

	@Override
	public File generateFile(Experiment experiment, ContextValidation contextValidation) throws Exception {
		String type = (String)contextValidation.getObject("type");
		String content = null;
		File file;

		Boolean isPlate = "96-well-plate".equals(experiment.instrument.inContainerSupportCategoryCode);
		if(type.contentEquals("help")) {
			List<HelpSampleSheetLine> pssl = getHelpSheetLines(experiment);
			content = OutputHelper.format(help.render(pssl,getProtocolName(experiment)).body());
			file = new File(getHelpFileName(experiment), content);

		}
		//Le type des entrées et sorties peut être tube / plaque ou les 2
		else if ("normalisation".equals(experiment.typeCode)){
			//Plaque en entrée
			if("96-well-plate".equals(experiment.instrument.inContainerSupportCategoryCode)){
				//Plaque vers Plaque
				if("96-well-plate".equals(experiment.instrument.outContainerSupportCategoryCode)){
					if (experiment.inputContainerSupportCodes.size() > 3){
						logger.debug("Gilson ne fonctionne pas avec plus de 3 plaques en entrée!");
						throw new Exception("Gilson ne fonctionne pas avec plus de 3 plaques en entrée!");
					}else {

						List<GilsonSampleSheetLine> pssl = getSampleSheetLines(experiment);
						content = OutputHelper.format(normalisation_x_to_plate.render(pssl,getProtocolName(experiment)).body());
						//PlateSampleSheet pss = getSampleSheet (pssl);
						//content = OutputHelper.format(normalisation_x_to_plate_old.render(experiment).body());

					}


				}else {
					//Plaque => tube
					if("tube".equals(experiment.instrument.outContainerSupportCategoryCode)){
						logger.debug("Cas entrée plaque => tubes non gere!!");
						throw new Exception(" Type de sortie "+experiment.instrument.outContainerSupportCategoryCode + " non géré dans FDR");
					}
				}
			}
			//Tubes en entrée
			else {
				List<GilsonSampleSheetLine> pssl = getSampleSheetLines(experiment);
				content = OutputHelper.format(normalisation_x_to_plate.render(pssl,getProtocolName(experiment)).body());

			}
			file = new File(getFileName(experiment) + ".csv", content);

		} 	else {
			throw new Exception(experiment.typeCode + " not managed");
		}

		return file;
	}

	private List<HelpSampleSheetLine> getHelpSheetLines(Experiment experiment) throws Exception {
		List<HelpSampleSheetLine> lines = new ArrayList<>();
		experiment.atomicTransfertMethods.forEach(atm -> {
			HelpSampleSheetLine shpl = new HelpSampleSheetLine();
			//Expce OneToOne
			InputContainerUsed input = atm.inputContainerUseds.get(0);
			shpl.sampleNameList="";
			shpl.inputContainerCode = input.code ;
			input.contents.forEach(content -> {
				shpl.sampleNameList+=content.sampleCode+" ";
			});
			logger.debug(shpl.inputContainerCode+" "+shpl.sampleNameList);
			lines.add(shpl);
		});

		Collections.sort(lines);		
		for(int i = 0; i < lines.size() ; i++){
			//1 seule plaque en sortie donc 96 tubes max en entrée
			if (i <=  96) {
				lines.get(i).sourcePos= String.valueOf(i+1);
			}else {
				throw new Exception("Une seule plaque gérée en sortie, maximum 96 puits en entrée!");
			}
		}

		return lines;
	}

	private List<GilsonSampleSheetLine> getSampleSheetLines(Experiment experiment) {
		Map<String, String> sourceMapping = getSourceMapping(experiment);
		Map<String, String> destPositionMapping = getDestMapping(experiment);

		List<GilsonSampleSheetLine> lines = new ArrayList<>();
	
		experiment.atomicTransfertMethods.forEach(atm -> {
			OutputContainerUsed output = atm.outputContainerUseds.get(0);
			atm.inputContainerUseds.forEach(input -> {
				lines.add(getSampleSheetLine(input, output, sourceMapping, destPositionMapping));
			});

		});

		return lines;
	}

	//	private List<GilsonSampleSheetLine> getTubesSampleSheetLines(Experiment experiment) {
	//		Map<String, String> sourceMapping = getSourceMapping(experiment);
	//		Map<String, String> destPositionMapping = getDestMapping(experiment);
	//
	//		List<GilsonSampleSheetLine> lines = new ArrayList<>();
	//		experiment.atomicTransfertMethods.forEach(atm -> {
	//			OutputContainerUsed output = atm.outputContainerUseds.get(0);
	//			atm.inputContainerUseds.forEach(input -> {
	//				lines.add(getSampleSheetLine(input, output, sourceMapping, destPositionMapping));
	//			});
	//
	//		});
	//
	//		return lines;
	//	}


	//Récupération des infos pour chaque atm
	private GilsonSampleSheetLine getSampleSheetLine(
			InputContainerUsed input, OutputContainerUsed output,
			Map<String, String> sourceMapping,
			Map<String, String> destPositionMapping) {
		GilsonSampleSheetLine sspl = new GilsonSampleSheetLine();

		sspl.inputSupportCode = input.locationOnContainerSupport.code;
		sspl.dnaVol = (Double)input.experimentProperties.get("inputVolume").value;
		sspl.bufferVol = (Double)input.experimentProperties.get("bufferVolume").value;

		//Gestion des entrées
		if (input.categoryCode.equals("well")) {
			//sspl.inputSupportCode = input.code;
			sspl.inputContainerColumn = input.locationOnContainerSupport.column;
			sspl.inputContainerLine = input.locationOnContainerSupport.line;

			Integer supportNum = Integer.parseInt(sourceMapping.get(input.locationOnContainerSupport.code).toString().replace("Src",""));
			sspl.sourcePos = (supportNum-1) * 96 + OutputHelper.getNumberPositionInPlateByColumn(input.locationOnContainerSupport.line, input.locationOnContainerSupport.column);
		}else if (input.categoryCode.equals("tube")){
			sspl.sourcePos=Integer.parseInt(sourceMapping.get(input.locationOnContainerSupport.code).toString().replace("Src",""));
		}

		logger.info(output.categoryCode);
		//Gestion des sorties
	//	if (output.categoryCode.equals("96-well-plate")) {
			sspl.outputSupportCode = output.locationOnContainerSupport.code;

			//Comme 1 seule plaque en sortie
			sspl.destPos = OutputHelper.getNumberPositionInPlateByColumn(output.locationOnContainerSupport.line, output.locationOnContainerSupport.column);
		//}
		return sspl;
	}

	//Création du nom du fichier FDR
	private String getFileName(Experiment experiment) {
		return experiment.code.toUpperCase()+ "_FDR";
	}
	//Création du nom du fichier d'aide au positionnement des tubes
	private String getHelpFileName(Experiment experiment) {
		return experiment.code.toUpperCase();
	}
	private String getProtocolName(Experiment experiment) {
		return (String) experiment.instrumentProperties.get("program").value;
	}

	//Permet d'attribuer une position au support dans le cas où on en a plusieurs
	// on a besoin de de figer un ordre
	//On a choisi de trier les supports par ordre alphabétique 
	private Map<String, String> getSourceMapping(Experiment experiment) {
		Map<String, String> sources = new HashMap<>();

		String[] inputContainerSupportCodes = experiment.inputContainerSupportCodes.toArray(new String[0]);
		Arrays.sort(inputContainerSupportCodes);
		for(int i = 0; i < inputContainerSupportCodes.length ; i++){
			sources.put(inputContainerSupportCodes[i], "Src"+(i+1));
		}
		return sources;
	}
	//Permet d'attribuer une position au support dans le cas où on en a plusieurs
	// on a besoin de de figer un ordre	
	//On a choisi de trier les supports par ordre alphabétique 
	private Map<String, String> getDestMapping(Experiment experiment) {
		Map<String, String> dest = new HashMap<>();

		String[] outputContainerSupportCodes = experiment.outputContainerSupportCodes.toArray(new String[0]);
		Arrays.sort(outputContainerSupportCodes);
		for(int i = 0; i < outputContainerSupportCodes.length ; i++){
			dest.put(outputContainerSupportCodes[i], (i+1)+"");
		}
		return dest;
	}

}
