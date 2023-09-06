package controllers.instruments.io.cng.janus;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// 21/09/2016 expression generique pour toutes les templates feuilles de route
import controllers.instruments.io.cng.janus.tpl.txt.*;
/*
import controllers.instruments.io.cng.janus.tpl.txt.normalization;
import controllers.instruments.io.cng.janus.tpl.txt.normalizationHighVolume;
import controllers.instruments.io.cng.janus.tpl.txt.normalizationPooling_buffer;
import controllers.instruments.io.cng.janus.tpl.txt.normalizationPooling_samples;
import controllers.instruments.io.cng.janus.tpl.txt.pool_PlatesToPlate_buffer;
import controllers.instruments.io.cng.janus.tpl.txt.pool_PlatesToPlate_samples;
import controllers.instruments.io.cng.janus.tpl.txt.transfert_PlatesToPlate;// ajout 14/06/2018
*/
import controllers.instruments.io.utils.AbstractOutput;
import controllers.instruments.io.utils.File;
import controllers.instruments.io.utils.OutputHelper;
import models.laboratory.experiment.instance.Experiment;
import models.laboratory.experiment.instance.InputContainerUsed;
import models.laboratory.experiment.instance.OutputContainerUsed;
import validation.ContextValidation;

// _CTX_PARAM: use AbstractTypedOutput

public class Output extends AbstractOutput {

	private static final play.Logger.ALogger logger = play.Logger.of(Output.class);
	
	@Override
	 public File generateFile(Experiment experiment, ContextValidation contextValidation) throws Exception {
		String content=null;
		String fdrType=null;
		
		//recuperer la valeur de la key "fdrType"dans contextValidation
		Object ftype =contextValidation.getObject("fdrType");
		
		if ("normalization-and-pooling".equals(experiment.typeCode)){
			if ("samples".equals(ftype) ){	
				fdrType="samples";
				logger.info("generation feuille de route Janus / exp="+ experiment.typeCode + "/ type="+ fdrType );
				// 21/09/2016 appeler une methode pour generer la liste des lignes a mettre dans la feuille de route
				content = OutputHelper.format(normalizationPooling_samples.render(getSampleSheetPoolLines(experiment)).body());
				
			} else if ("buffer".equals(ftype)) {
				fdrType="buffer";
				logger.info("generation feuille de route Janus / exp="+ experiment.typeCode + "/ type="+ fdrType );
				content = OutputHelper.format(normalizationPooling_buffer.render(experiment).body());
				
			} else {
				throw new RuntimeException("Janus sampleSheet type not managed : "+experiment.typeCode + "/" +ftype);
			}
			
		// 13/09/2016 finalement il y a aussi 2 feuilles de route pour pooling
		} else if ("pool".equals(experiment.typeCode)){
			if ("samples".equals(ftype) ){	
				fdrType="samples";
				logger.info("generation feuille de route Janus / exp="+ experiment.typeCode + "/ type="+ fdrType );
				// 21/09/2016 appeler une methode pour generer la liste des lignes a mettre dans la feuille de route
				content = OutputHelper.format(pool_PlatesToPlate_samples.render(getSampleSheetPoolLines(experiment)).body());
				
			} else if ("buffer".equals(ftype)) {
				fdrType="buffer";
				logger.info("generation feuille de route Janus / exp="+ experiment.typeCode + "/ type="+ fdrType );
				content = OutputHelper.format(pool_PlatesToPlate_buffer.render(experiment).body());	
				
			} else {
				throw new RuntimeException("Janus sampleSheet type not managed : "+experiment.typeCode + "/" + ftype);
			}
		
		} else if ( "lib-normalization".equals(experiment.typeCode) ){
			// FDS 22/03/2022 NGL-3776 2 FDR distinctes
			if ( "standard".equals(ftype) ){
				fdrType="normale";
				logger.info("generation feuille de route Janus / exp="+ experiment.typeCode + "/ type="+ fdrType);
				content = OutputHelper.format(normalization.render(experiment).body());
				
			} else if ( "highVolume".equals(ftype) ){
				fdrType="GrosVolume-RSB-StorePlate";
				logger.info("generation feuille de route Janus / exp="+ experiment.typeCode + "/ type="+ fdrType);
				content = OutputHelper.format(normalizationHighVolume.render(experiment).body());
			}
		// FDS 19/07/2017 NGL-1519 ajout "additional-normalization"	
		} else if ( "additional-normalization".equals(experiment.typeCode) ){
			// FDS 22/03/2022 NGL-3776 2 FDR distinctes
			if ("standard".equals(ftype) ){
				fdrType="normale";
				logger.info("generation feuille de route Janus / exp="+ experiment.typeCode + "/ type="+ fdrType);
				content = OutputHelper.format(normalization.render(experiment).body());
				
			} else if ( "highVolume".equals(ftype) ){
				fdrType="GrosVolume-RSB-StorePlate";
				logger.info("generation feuille de route Janus / exp="+ experiment.typeCode + "/ type="+ fdrType);
				content = OutputHelper.format(normalizationHighVolume.render(experiment).body());
			}
		// FDS 14/06/2018 NGL-2115 ajout "plates->plate"
		} else if ( "plates-to-plate".equals(experiment.typeCode) ){
			logger.info("generation feuille de route Janus / exp="+ experiment.typeCode );
			// il n'y a pas de pooling mais reutilisation des methodes et classes deja existantes...
			content = OutputHelper.format(transfert_PlatesToPlate.render(getSampleSheetPoolLines(experiment)).body());	
				
		} else {
			// a venir ????
			//    rna-prep; 
			//    pcr-purif; 
			throw new RuntimeException("Janus sampleSheet type not managed for experiment : "+experiment.typeCode);
		}
		
		File file = new File(getFileName(experiment,fdrType )+".csv", content);
		return file;
	}
	
	private String getFileName(Experiment experiment,String fdrType) {
		StringBuilder fileName = new StringBuilder();
		fileName.append(experiment.typeCode.toUpperCase())
		        .append('_')
		        .append(experiment.atomicTransfertMethods.get(0).outputContainerUseds.get(0).locationOnContainerSupport.code);
		if (fdrType != null)
			fileName.append('_')
			        .append(fdrType);
		fileName.append('_')
		        .append(new SimpleDateFormat("yyyMMdd").format(new Date()));
		return fileName.toString();      
	}
	
	// 21/09/2016 Il faut trier en java les lignes a envoyer au template de la feuille de route ( trop complexe a faire en scala !!)
	//            3 methodes adaptées (simplifiees) depuis tecanevo100/output.java
	private List<SampleSheetPoolLine> getSampleSheetPoolLines(Experiment experiment) {
		Map<String, String> sourceMapping = getSourceMapping(experiment);
		
		List<SampleSheetPoolLine> lines = new ArrayList<>();
		
		experiment.atomicTransfertMethods.forEach(atm -> {
			
			OutputContainerUsed output = atm.outputContainerUseds.get(0);
			
			atm.inputContainerUseds.forEach(input -> {
				lines.add(getSampleSheetPoolLine(input, output, sourceMapping, experiment)); //14/06/2018 ajout experiment en parametre
			});	
		});
		
		return lines;
	}

	//14/06/2018 ajout experiment en parametre
	private SampleSheetPoolLine getSampleSheetPoolLine(InputContainerUsed input, 
			                                           OutputContainerUsed output,
			                                           Map<String, String> sourceMapping,
			                                           Experiment experiment) {
		SampleSheetPoolLine sspl = new SampleSheetPoolLine();
		
		sspl.inputSupportCode = input.locationOnContainerSupport.code;// normallement uniqt pour DEBUG
		sspl.inputSupportContainerPosition = OutputHelper.getNumberPositionInPlateByColumn(input.locationOnContainerSupport.line, input.locationOnContainerSupport.column);
		
		if (! "plates-to-plate".equals(experiment.typeCode) ){
			sspl.inputSupportContainerVolume = input.experimentProperties.get("inputVolume").value.toString(); 
		} else {
			// 14/06/2018 dans le cas de "plates-to-plate" il n'y a pas de pooling, on transfere tout le contenu de l'input !! ( inputVolume n'existe pas..)
			// !! cas de puits sans volume ???
			if (input.volume != null) {
				sspl.inputSupportContainerVolume = input.volume.value.toString(); 
			} else {
				sspl.inputSupportContainerVolume = "0";
			}
		}
		
		sspl.inputSupportSource =  sourceMapping.get(input.locationOnContainerSupport.code);
		sspl.outputSupportPosition = OutputHelper.getNumberPositionInPlateByColumn(output.locationOnContainerSupport.line, output.locationOnContainerSupport.column);
		
		return sspl;
	}
	
	private Map<String, String> getSourceMapping(Experiment experiment) {
		Map<String, String> sources = new HashMap<>();
		
		String[] inputContainerSupportCodes = experiment.inputContainerSupportCodes.toArray(new String[0]);
		Arrays.sort(inputContainerSupportCodes);
		for(int i = 0; i < inputContainerSupportCodes.length ; i++){
			sources.put(inputContainerSupportCodes[i], "Source_"+(i+1)); // HARDCODED "Source_" 
		}
		return sources;
	}
}
