package controllers.instruments.io.cns.brandlhs;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.stream.Collectors;

import controllers.instruments.io.cns.brandlhs.tpl.txt.normalisation_x_to_plate;
import controllers.instruments.io.cns.brandlhs.tpl.txt.normalisation_x_to_plate_buffer;
import controllers.instruments.io.cns.brandlhs.tpl.txt.normalisation_x_to_plate_buffer_highVol;
import controllers.instruments.io.cns.brandlhs.tpl.txt.normalisation_x_to_plate_highVol;
import controllers.instruments.io.utils.AbstractOutput;
import controllers.instruments.io.utils.File;
import controllers.instruments.io.utils.OutputHelper;
import models.laboratory.experiment.instance.AtomicTransfertMethod;
import models.laboratory.experiment.instance.Experiment;
import models.laboratory.experiment.instance.InputContainerUsed;
import models.laboratory.experiment.instance.OutputContainerUsed;
import validation.ContextValidation;

public class Output extends AbstractOutput {
	
	private static final play.Logger.ALogger logger = play.Logger.of(Output.class);
	
	private static int sampleNum;
	private static Boolean isBuffer;
	// vol seuil pour petit vol
	private int treshold = 20;
	private String name1 = "pipette_P50";
	private String name2 = "pipette_P200";
	
	@Override
	public File generateFile(Experiment experiment, ContextValidation contextValidation) throws Exception {
		String type = (String)contextValidation.getObject("type");

		String adnContent    = null;
		String bufferContent = null;
		File file;
		Boolean isPlaque = "96-well-plate".equals(experiment.instrument.inContainerSupportCategoryCode);	
	
//		if ("normalisation".equals(type) || "normalisation-highVol".equals(type))
//			isBuffer = false;
//		else
//			isBuffer = true;
		isBuffer = ! ("normalisation".equals(type) || "normalisation-highVol".equals(type));
		
		//récupere des infos sur la possible mixité des supports en entrée
		//Pour rappel, de normalisation est une expce oneToOne donc 1 seul icu / ocu
		boolean intube= false;
		boolean inplaque=false;
		boolean isInmixte=false;
	
		for ( AtomicTransfertMethod atm :experiment.atomicTransfertMethods) {			
			if (atm.inputContainerUseds.get(0).categoryCode.equals("well")){
				inplaque=true;
			}else {
				intube=true;
			}
			if (inplaque && intube) {
				isInmixte=true;
			}
			logger.debug("Mixte:"+isInmixte+"!!");
			
		}

		//Plaque + tube en entrée
		if (isInmixte){
			logger.debug("Gilson avec tubes et plaque en entrée");
			throw new Exception("La feuille de route pour: tube + plaques en entrée n'est pas gérée pour le robot Celeste");
		}
		
		
		//tube / 96-well-plate
		if("96-well-plate".equals(experiment.instrument.outContainerSupportCategoryCode)){
			if ("normalisation".equals(type) ){
				List<PlateSampleSheetLine> pssl = getPlateSampleSheetLines(experiment, experiment.instrument.inContainerSupportCategoryCode);
				pssl = checkSampleSheetLines(pssl, isPlaque);

				adnContent = OutputHelper.format(normalisation_x_to_plate.render(pssl).body());
				file = new File(getFileName(experiment)+"_ADN_"+name1+".csv", adnContent);

			}else if ("normalisation-highVol".equals(type) ){
				List<PlateSampleSheetLine> pssl = getPlateSampleSheetLines(experiment, experiment.instrument.inContainerSupportCategoryCode);
				pssl = checkSampleSheetLines(pssl, isPlaque);

				adnContent = OutputHelper.format(normalisation_x_to_plate_highVol.render(pssl).body());
				file = new File(getFileName(experiment)+"_ADN_"+name2+".csv", adnContent);

			}else if("normalisation-buffer".equals(type)){
				List<PlateSampleSheetLine> pssl = getPlateSampleSheetLines(experiment, experiment.instrument.inContainerSupportCategoryCode);
				pssl = checkSampleSheetLines(pssl, isPlaque);

				bufferContent = OutputHelper.format(normalisation_x_to_plate_buffer.render(pssl).body());
				file = new File(getFileName(experiment)+"_Buffer_"+name1+".csv", bufferContent);

			}else if("normalisation-buffer-highVol".equals(type)){
				List<PlateSampleSheetLine> pssl = getPlateSampleSheetLines(experiment, experiment.instrument.inContainerSupportCategoryCode);
				pssl = checkSampleSheetLines(pssl, isPlaque);

				bufferContent = OutputHelper.format(normalisation_x_to_plate_buffer_highVol.render(pssl).body());
				file = new File(getFileName(experiment)+"_Buffer_"+name2+".csv", bufferContent);

			}else{
				throw new RuntimeException("brandlhs sampleSheet io not managed : "+type);
			}
		}else {
			throw new RuntimeException("brandlhs sampleSheet io combination not managed : "+experiment.instrument.inContainerSupportCategoryCode+" / "+experiment.instrument.outContainerSupportCategoryCode+" / "+type);
		}


		return file;
	}

	private String getFileName(Experiment experiment) {
//		SimpleDateFormat sdf = new SimpleDateFormat("yyyMMdd");
		return experiment.code.toUpperCase();		
	}

	private List<PlateSampleSheetLine> getPlateSampleSheetLines(Experiment experiment, String inputContainerCategory) {
		return  experiment.atomicTransfertMethods
				.parallelStream()
				.map(atm -> getPlateSampleSheetLine(atm,inputContainerCategory, experiment))
				.collect(Collectors.toList());	
	}

	private PlateSampleSheetLine getPlateSampleSheetLine(AtomicTransfertMethod atm, String inputContainerCategory,Experiment experiment) {
//		Map<String, String> sourceMapping = 
				getSourceMapping(experiment);
//		Map<String, String> destPositionMapping = 
				getDestMapping(experiment);

		InputContainerUsed icu = atm.inputContainerUseds.get(0);
		OutputContainerUsed ocu = atm.outputContainerUseds.get(0);
		PlateSampleSheetLine pssl = new PlateSampleSheetLine();

		pssl.inputContainerCode = icu.code;
		pssl.outputContainerCode = ocu.code;

		Double vol = new Double(0);

		if (! isBuffer){
			if(icu.experimentProperties!=null && icu.experimentProperties.containsKey("inputVolume"))
				vol = (Double)icu.experimentProperties.get("inputVolume").value;
			else if(ocu.volume!=null && ocu.volume.value!=null)
				vol = (Double)ocu.volume.value;
			else
				logger.error("Aucun volume renseigné dans l'expérience! ");


			if (vol < treshold){
				pssl.inputVolume = vol.toString().replace(".", ",");
				pssl.inputHighVolume = "0,0";
			}else{
				pssl.inputHighVolume = vol.toString().replace(".", ",");
				pssl.inputVolume = "0,0";
			}
		}

		if (isBuffer){
			if(icu.experimentProperties!=null && icu.experimentProperties.containsKey("bufferVolume"))
				vol = (Double)icu.experimentProperties.get("bufferVolume").value;

			if (vol < treshold){
				pssl.bufferVolume = vol.toString().replace(".", ",");
				pssl.bufferHighVolume = "0,0";
			}else{
				pssl.bufferHighVolume = vol.toString().replace(".", ",");
				pssl.bufferVolume = "0,0";
			}
		}
		pssl.dwell = ocu.locationOnContainerSupport.line.concat(ocu.locationOnContainerSupport.column);

		return pssl;
	}
	
	private List<PlateSampleSheetLine> checkSampleSheetLines (List<PlateSampleSheetLine> psslList, Boolean isPlate){

		List<PlateSampleSheetLine> psslListNew = new LinkedList<>();

		if (isPlate){
			List<String> plateLines = Arrays.asList("A","B","C","D","E","F","G","H"); 	
			List<Integer> colNums = Arrays.asList(1,2,3,4,5,6,7,8,9,10,11,12);
			sampleNum=0;
			psslListNew = filledSampleSheetLines(psslList,plateLines,colNums);

		} else {
			/* on gere finalement les tubes par 4 racks 
			 */

			List<String> plateLines = Arrays.asList("A","B","C","D"); 	
			List<String> plateLines2 = Arrays.asList("E","F","G","H"); 	
			List<Integer> colNums= Arrays.asList(1,2,3,4,5,6);
			List<Integer> colNums2 = Arrays.asList(7,8,9,10,11,12);
			List<String> plateLinesBuf = Arrays.asList("A","B","C","D","E","F","G","H"); 	
			List<Integer> colNumsBuf = Arrays.asList(1,2,3,4,5,6,7,8,9,10,11,12);

			//Logger.debug("isBuffer "+);
			//ADN
			if (! isBuffer){
				sampleNum=0;
				psslListNew = filledSampleSheetLines(psslList,plateLines,colNums);
				psslListNew.addAll(filledSampleSheetLines(psslList,plateLines,colNums2));
				psslListNew.addAll(filledSampleSheetLines(psslList,plateLines2,colNums));
				psslListNew.addAll(filledSampleSheetLines(psslList,plateLines2,colNums2));
			}else {
				sampleNum=0;
				psslListNew = filledSampleSheetLines(psslList,plateLinesBuf,colNumsBuf);
			}

		}

		return psslListNew;	
	}


	private  List<PlateSampleSheetLine> filledSampleSheetLines (List<PlateSampleSheetLine> psslList, List<String> plateLines, List<Integer> colNums){

		boolean found = false;
		List<PlateSampleSheetLine> psslListNew = new LinkedList<>();

		ListIterator<String> LinesItr = plateLines.listIterator();	
		while (LinesItr.hasNext()) {
//			String line =(String) LinesItr.next();		
			String line = LinesItr.next();		
			ListIterator<Integer> colNumsItr = colNums.listIterator();	
			while (colNumsItr.hasNext()) {
//				Integer col =(Integer) colNumsItr.next();		
				Integer col = colNumsItr.next();		
				found = false;

				sampleNum ++;
				ListIterator<PlateSampleSheetLine> psslListItr = psslList.listIterator();	
				while(psslListItr.hasNext()) {
//					PlateSampleSheetLine pssl =(PlateSampleSheetLine) psslListItr.next();					
					PlateSampleSheetLine pssl = psslListItr.next();					
					if (pssl.dwell.equals(line+col)){
						logger.debug("--"+pssl.dwell+" "+sampleNum);
						found           = true;	
						pssl.dwellNum   = sampleNum;
						pssl.sampleName = "Sample "+sampleNum;
						psslListNew.add(pssl);
					}
				}

				if (! found){
					PlateSampleSheetLine psslBlank = new PlateSampleSheetLine();
					psslBlank.dwell=line+col;
					psslBlank.dwellNum=sampleNum;

					psslBlank.sampleName = "Sample "+sampleNum;
					if (! isBuffer) {
						psslBlank.inputVolume = "0,0";
						psslBlank.inputHighVolume = "0,0";
					} else {
						psslBlank.bufferVolume = "0,0";
						psslBlank.bufferHighVolume = "0,0";
					}
					psslListNew.add(psslBlank);
				}
			}
		}
		return psslListNew;	
	}
	
	private Map<String, String> getSourceMapping(Experiment experiment) {
		Map<String, String> sources = new HashMap<>();

		String[] inputContainerSupportCodes = experiment.inputContainerSupportCodes.toArray(new String[0]);
		Arrays.sort(inputContainerSupportCodes);
		for(int i = 0; i < inputContainerSupportCodes.length ; i++){
			sources.put(inputContainerSupportCodes[i], "Src"+(i+1));
		}
		return sources;
	}

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
