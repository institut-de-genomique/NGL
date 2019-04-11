package controllers.instruments.io.cns.fluoroskan;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import controllers.instruments.io.utils.AbstractInput;
import models.laboratory.common.instance.property.PropertyFileValue;
import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.experiment.instance.Experiment;
import models.laboratory.experiment.instance.InputContainerUsed;
import validation.ContextValidation;

public class Input extends AbstractInput {

	@Override
	public Experiment importFile(Experiment experiment, PropertyFileValue pfv, ContextValidation contextValidation) throws Exception {		
		//		InputStream is = new ByteArrayInputStream(pfv.value);
		InputStream is = new ByteArrayInputStream(pfv.byteValue());

		Workbook wb = WorkbookFactory.create(is);
		Sheet sheet = wb.getSheetAt(0);
		String plateCodeInFile =null;
		String plateCodeInExp = null;
		if(experiment.instrument.inContainerSupportCategoryCode.equals("tube")){
			plateCodeInFile=getStringValue(sheet.getRow(0).getCell(1));
			//Code Container de la cellule A1
			plateCodeInExp=getCodeContainerFirstPositionFluoPlate(experiment);
		}else{
			plateCodeInFile = getStringValue(sheet.getRow(0).getCell(1));
			plateCodeInExp = experiment.inputContainerSupportCodes.iterator().next();
		}

		logger.debug("Start Contextvalidation Error "+ plateCodeInExp +", "+ plateCodeInFile );

		if (plateCodeInExp.equals(plateCodeInFile)) {

			String codePropertiesConcDil   = null;
			String codePropertiesConcFinal = null;
			String codePropertiesDilFactor = null;

			if (experiment.typeCode.equals("reception-fluo-quantification") || 
					experiment.typeCode.equals("fluo-quantification")) {

				String typeQC = getStringValue(sheet.getRow(0).getCell(3));

				logger.debug("Type QC {}", typeQC);
				// Valide typeQC
				if (typeQC == null) {
					contextValidation.addError("Erreur gamme", "Code gamme vide dans fichier");
				} else if (!typeQC.contains(contextValidation.getObject("gamme").toString())) {
					contextValidation.addError("Erreur gamme", "La gamme du fichier "+typeQC+" ne correspond pas au type d'import "+contextValidation.getObject("gamme").toString());
				} else if (typeQC.equals("BR")) {
					codePropertiesConcDil   = "concentrationDilBR1";
					codePropertiesConcFinal = "concentrationBR1";
					codePropertiesDilFactor = "dilutionFactorBR1";
				} else if(typeQC.equals("HS")) {
					codePropertiesConcDil   = "concentrationDilHS1";
					codePropertiesConcFinal = "concentrationHS1";
					codePropertiesDilFactor = "dilutionFactorHS1";
				} else if(typeQC.equals("HS2")) {
					codePropertiesConcDil   = "concentrationDilHS2";
					codePropertiesConcFinal = "concentrationHS2";
					codePropertiesDilFactor = "dilutionFactorHS2";
				} else if (typeQC.equals("HS3")) {
					codePropertiesConcDil   = "concentrationDilHS3";
					codePropertiesConcFinal = "concentrationHS3";
					codePropertiesDilFactor = "dilutionFactorHS3";
				} else {
					contextValidation.addError("Erreur gamme", "Code gamme non géré : "+typeQC);	
				}
			}

			final String codePropertiesConcDilf   = codePropertiesConcDil;
			final String codePropertiesConcFinalf = codePropertiesConcFinal;
			final String codePropertiesDilFactorf = codePropertiesDilFactor;

			Map<String,Double> results = new HashMap<>(0);
//			String[] lines = new String[] { "A", "B", "C", "D", "E", "F", "G", "H" };
			String[] lines = { "A", "B", "C", "D", "E", "F", "G", "H" };
			// line
			for (int i = 17; i < 25; i++) {
				String line = lines[i-17];
				// column
				for (int j = 1; j <= 12; j++) {
					String key = "";
					if(experiment.instrument.inContainerSupportCategoryCode.equals("tube"))
						key = "_" + line + j;
					else
						key = plateCodeInExp + "_" + line + j;
					
					Double concentration1 = getNumericValue(sheet.getRow(i).getCell(j));
					results.put(key,concentration1);
				}								
			}

			if (!contextValidation.hasErrors()) {

				experiment.atomicTransfertMethods
				.stream()
				.map(atm -> atm.inputContainerUseds.get(0))
				.forEach(icu -> {
					String key=null;
					if(experiment.instrument.inContainerSupportCategoryCode.equals("tube") && icu.instrumentProperties!=null && icu.instrumentProperties.get("fluoroskanLine")!=null && icu.instrumentProperties.get("fluoroskanColumn")!=null)
						key = "_" + icu.instrumentProperties.get("fluoroskanLine").getValue() + icu.instrumentProperties.get("fluoroskanColumn").getValue();
					else if(!experiment.instrument.inContainerSupportCategoryCode.equals("tube"))
						key = icu.locationOnContainerSupport.code + "_" + icu.locationOnContainerSupport.line + icu.locationOnContainerSupport.column;
					if(key!=null){
						PropertySingleValue concentrationDil = getPSV(icu,codePropertiesConcDilf);
						concentrationDil.value = results.get(key);
						concentrationDil.unit = "ng/µl";
						if (codePropertiesDilFactorf != null && codePropertiesConcFinalf != null) {
							computeFinalConcentration(icu, concentrationDil, codePropertiesDilFactorf, codePropertiesConcFinalf);
						}
					}
				});
			}

		} else {
			if(experiment.instrument.inContainerSupportCategoryCode.equals("tube")){
				if(StringUtils.isEmpty(plateCodeInExp)){
					contextValidation.addError("Erreurs fichier", "Aucun tube trouvé en position A1 (plaque fluoroskan)");
				}else if(StringUtils.isEmpty(plateCodeInFile)){
					contextValidation.addError("Erreurs fichier", "Code container en position A1 (plaque fluoroskan) non renseigné dans fichier");
				}else
					contextValidation.addError("Erreurs fichier", "Code container en position A1 (plaque fluoroskan) différent de code container renseigné dans fichier : "+plateCodeInFile);
			}
			else
				contextValidation.addError("Erreurs fichier", "Code de plaque incorrecte : "+plateCodeInFile);
		}		
		return experiment;
	}

	private void computeFinalConcentration(InputContainerUsed  icu, 
			                               PropertySingleValue concentrationDil, 
			                               String              codePropertiesDilFactor, 
			                               String              codePropertiesConcFinal) {
		PropertySingleValue dilutionFactor = getPSV(icu,codePropertiesDilFactor);
		if (dilutionFactor.value != null) {
			Integer dilFactor = Integer.valueOf(dilutionFactor.value.toString().split("/",2)[1].trim());
			if (dilFactor != null) {
				PropertySingleValue finalConcentration = getPSV(icu,codePropertiesConcFinal);
				finalConcentration.unit = concentrationDil.unit;
				//finalConcentration.value = new BigDecimal(dilFactor * (Double)concentrationDil.value).setScale(2, RoundingMode.HALF_UP);	
				finalConcentration.value = new BigDecimal((dilFactor * (Double)concentrationDil.value));	
			} else {
				logger.warn("dilfactor is null after convertion {}", dilutionFactor.value);
			}
		}
	}

	private String getCodeContainerFirstPositionFluoPlate(Experiment exp)
	{
		Set<String> codeContainers=new HashSet<>();

		exp.atomicTransfertMethods.stream().map(atm -> atm.inputContainerUseds)
		.flatMap(List::stream)
		.forEach(inputContainer -> {
			if(inputContainer.instrumentProperties!=null && inputContainer.instrumentProperties.get("fluoroskanLine")!=null && inputContainer.instrumentProperties.get("fluoroskanColumn")!=null &&
					inputContainer.instrumentProperties.get("fluoroskanLine").getValue().equals("A") && inputContainer.instrumentProperties.get("fluoroskanColumn").getValue().equals("1"))
				codeContainers.add(inputContainer.code);

		});

		if(codeContainers.size()==1)
			return codeContainers.iterator().next();
		else
			return "";
	}

}
