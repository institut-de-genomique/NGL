package controllers.instruments.io.cns.fluoroskan;

import static services.io.ExcelHelper.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
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

//_CTX_PARAM: use AbstractTypeInput as base class

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
			String codePropertiesConcReelle = null;
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
					codePropertiesConcReelle = "concentrationBR1";
					codePropertiesDilFactor = "dilutionFactorBR1";
				} else if(typeQC.equals("HS")) {
					codePropertiesConcDil   = "concentrationDilHS1";
					codePropertiesConcReelle = "concentrationHS1";
					codePropertiesDilFactor = "dilutionFactorHS1";
				} else if(typeQC.equals("HS2")) {
					codePropertiesConcDil   = "concentrationDilHS2";
					codePropertiesConcReelle = "concentrationHS2";
					codePropertiesDilFactor = "dilutionFactorHS2";
				} else if (typeQC.equals("HS3")) {
					codePropertiesConcDil   = "concentrationDilHS3";
					codePropertiesConcReelle = "concentrationHS3";
					codePropertiesDilFactor = "dilutionFactorHS3";
				} else {
					contextValidation.addError("Erreur gamme", "Code gamme non géré : "+typeQC);	
				}
			}

			final String codePropertiesConcDilf   = codePropertiesConcDil;
			final String codePropertiesConcR = codePropertiesConcReelle;
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
						// valeur manquante 
						if (null == concentration1) {
							contextValidation.addError("Erreurs fichier", "experiments.msg.import.value.missing", (i+1),"conc");
						} else {
							if (Double.isNaN(concentration1)) {
								concentration1= null;
							}
							results.put(key,concentration1);
						}
						
				}								
			}

			if (!contextValidation.hasErrors()) {

				experiment.atomicTransfertMethods
				.stream()
				.map(atm -> atm.inputContainerUseds.get(0))
				.forEach(icu -> {
					String key=null;
					if(experiment.instrument.inContainerSupportCategoryCode.equals("tube") && icu.instrumentProperties!=null && icu.instrumentProperties.get("fluoroskanLine")!=null 
							&& icu.instrumentProperties.get("fluoroskanColumn")!=null)
						key = "_" + icu.instrumentProperties.get("fluoroskanLine").getValue() + icu.instrumentProperties.get("fluoroskanColumn").getValue();
					else if(!experiment.instrument.inContainerSupportCategoryCode.equals("tube"))
						key = icu.locationOnContainerSupport.code + "_" + icu.locationOnContainerSupport.line + icu.locationOnContainerSupport.column;
					if(key!=null){
						PropertySingleValue concentrationDil = getOrCreatePSV(icu,codePropertiesConcDilf);
						concentrationDil.value = results.get(key);
						concentrationDil.unit = "ng/µl";
						//GS 2019/08/23 si on se trouve un jour avec des champs vide dans le fichier à importer du fluoroscan
						// ajouter la condition  && concentrationDil.value != null 
						if (codePropertiesDilFactorf != null && codePropertiesConcR != null) {

							//	 if (concentrationDil.value != null){
							computeConcReelle(icu, concentrationDil, codePropertiesDilFactorf, codePropertiesConcR);

							/* }else{
								 //Cas ou on vide un dosage (import cellule vide ou non numerique
									PropertySingleValue concR = getOrCreatePSV(icu, codePropertiesConcR);
									concR.value= null;
									concR.unit=null;
							}*/
							updateCalculations(icu, experiment.typeCode);

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

	private void cleanCalculations (InputContainerUsed  icu){

		PropertySingleValue quantity1 = getOrCreatePSV(icu,"quantity1");
		PropertySingleValue concentration1 = getOrCreatePSV(icu,"concentration1");
		PropertySingleValue concnM =  getOrCreatePSV(icu,"nMcalculatedConcentration");

		quantity1.value= null;
		concentration1.value=null;
		concnM.value=null;

		quantity1.unit= null;
		concentration1.unit=null;
		concnM.unit = null;
	}

	// Calcul du dosage reel (Conc.) 
	private void computeConcReelle(InputContainerUsed  icu, 
			PropertySingleValue concentrationDil, 
			String              codePropertiesDilFactor,
			String              codePropertiesConcR){
		// Calcul de la conc REELLE pour le type de methode selectionné  (Ex: Dosage HS1reel , dosage BR reel..)
		PropertySingleValue concR = getOrCreatePSV(icu, codePropertiesConcR);

		PropertySingleValue dilutionFactor = getOrCreatePSV(icu,codePropertiesDilFactor);
		if (dilutionFactor.value != null) {
			Integer dilFactor = Integer.valueOf(dilutionFactor.value.toString().split("/",2)[1].trim());
			if (dilFactor != null) {

				//finalConcentration.value = new BigDecimal(dilFactor * (Double)concentrationDil.value).setScale(2, RoundingMode.HALF_UP);	
				//finalConcentration.value = new BigDecimal((dilFactor * (Double)concentrationDil.value));	
				if (concentrationDil != null){
					concR.value = (dilFactor * (Double)concentrationDil.value);					
					concR.unit = concentrationDil.unit;
				}else{
					concR.value = null;
					concR.unit = null;
				}
			} else {	
				concR.value = null;
				concR.unit = null;
				logger.warn("dilfactor is null after convertion {}", dilutionFactor.value);
			}
		}else{
			concR.value = null;
			concR.unit = null;
		}
	}

	//1-  mise a jour de la concentration finale pour le type de methode selectionné  (Ex: Dosage HS1reel , dosage BR reel..)
	//2- mise a jour de la quantité finale selon la methode de calcul sélectionnée
	//2- mise a jour de la concentration calulée en nM 
	private void updateCalculations(InputContainerUsed  icu, String expTypeCode) {

		//Calcul de la concentration finale
		computeConcentration1(icu);	

		//Calcul de la quantité finale
		computeQuantity1(icu);

		//Calcul de la concentration en nM
		// uniquement pour "fluo quantif" et pas "reception fluoquantif"
		if (expTypeCode.equals("fluo-quantification"))
			computeConcnM(icu);

	}	


	//Attention il s'agit là du veritable calcul de la conc finale, tenant compte de la méthode de calcul selectionnée
	private void computeConcentration1(InputContainerUsed  icu){

		PropertySingleValue calcMethod = getOrCreatePSV(icu,"calculationMethod");
		PropertySingleValue concentration1 = getOrCreatePSV(icu,"concentration1");

		//	logger.info("calcMethod "+calcMethod.value);
		PropertySingleValue psvConc;
		if (calcMethod != null && calcMethod.value != null){
			if (calcMethod.value.toString().contains("HS 1 seul")){
				psvConc = getOrCreatePSV(icu,"concentrationHS1");
				concentration1.value= psvConc.value;	
				concentration1.unit = psvConc.unit;
			}else if (calcMethod.value.toString().contains("HS 2 seul")){
				psvConc = getOrCreatePSV(icu,"concentrationHS2");
				concentration1.value= psvConc.value;	
				concentration1.unit = psvConc.unit;
			}else if (calcMethod.value.toString().contains("HS 3 seul")){
				psvConc = getOrCreatePSV(icu,"concentrationHS3");
				concentration1.value= psvConc.value;	
				concentration1.unit = psvConc.unit;
			}else if (calcMethod.value.toString().contains("BR 1 seul")){
				psvConc = getOrCreatePSV(icu,"concentrationBR1");
				concentration1.value= psvConc.value;	
				concentration1.unit = psvConc.unit;
			}else if (calcMethod.value.toString().equals("Moyenne HS1 HS2")){
				PropertySingleValue concHS1 =  getOrCreatePSV(icu,"concentrationHS1");		
				PropertySingleValue concHS2 = getOrCreatePSV(icu,"concentrationHS2");		
				if (concHS1.value != null  && concHS2.value != null ){
					concentration1.value =  ((Double)concHS1.value  + (Double)concHS2.value )/2 ;	
					concentration1.unit = concHS1.unit;
				}else
					concentration1.value=null;
			}else if (calcMethod.value.toString().equals("Moyenne HS2 HS3")){
				PropertySingleValue concHS2 =  getOrCreatePSV(icu,"concentrationHS2");		
				PropertySingleValue concHS3 = getOrCreatePSV(icu,"concentrationHS3");		
				if (concHS3.value != null  && concHS2.value != null ){
					concentration1.value =  ((Double)concHS2.value  + (Double)concHS3.value )/2 ;
					concentration1.unit = concHS2.unit; 
				}else
					concentration1.value=null;
			}else if (calcMethod.value.toString().equals("Moyenne HS1 HS3")){
				PropertySingleValue concHS1 =  getOrCreatePSV(icu,"concentrationHS1");		
				PropertySingleValue concHS3 = getOrCreatePSV(icu,"concentrationHS3");		
				if (concHS3.value != null  && concHS1.value != null ){
					concentration1.value =  ((Double)concHS1.value  + (Double)concHS3.value )/2 ;
					concentration1.unit = concHS1.unit; 
				}else
					concentration1.value=null;
			}else if (calcMethod.value.toString().equals("Moyenne HS1 HS2 HS3")){
				PropertySingleValue concHS1 =  getOrCreatePSV(icu,"concentrationHS1");		
				PropertySingleValue concHS2 =  getOrCreatePSV(icu,"concentrationHS2");		
				PropertySingleValue concHS3 = getOrCreatePSV(icu,"concentrationHS3");		
				if (concHS1.value != null  && concHS2.value != null && concHS3.value != null){
					concentration1.value =  ((Double)concHS1.value  + (Double)concHS2.value + (Double)concHS3.value )/3 ;		
					concentration1.unit = concHS2.unit; 
				}else
					concentration1.value=null;
			}else if (calcMethod.value.toString().contains("BR si >")){
				PropertySingleValue concHS1 =  getOrCreatePSV(icu,"concentrationHS1");		
				PropertySingleValue concBR1 =  getOrCreatePSV(icu,"concentrationBR1");	

				if (concBR1.value != null ){
					if ((Double)concBR1.value > 25){
						concentration1.value =  concBR1.value;	
						concentration1.unit = concBR1.unit; 
					}else if ((Double)concBR1.value <= 25 && concHS1.value != null){
						concentration1.value=  concHS1.value;
						concentration1.unit = concHS1.unit; 
					}
				}else
					concentration1.value=null;
			}else if (calcMethod.value.toString().contains("Non quantifiable")){
				concentration1.value=null;
			} else {
				logger.error(calcMethod+" non gérée!!");
				concentration1.value=null;
			}	
		}
		if (concentration1.value == null){
			concentration1.unit = null;		
			// Si la concentration finale est nulle alors on nettoie qtté finale et conc. calculée
			cleanCalculations(icu);
		}
	}	

	// calcul de la quantité finale, tenant compte de la concentration réelle et du volume final
	private void computeQuantity1(InputContainerUsed  icu){

		PropertySingleValue quantity1 = getOrCreatePSV(icu,"quantity1");
		PropertySingleValue volume1 = getOrCreatePSV(icu,"volume1");
		PropertySingleValue concentration1 = getOrCreatePSV(icu,"concentration1");


		if (volume1 != null && volume1.value != null && concentration1 != null && concentration1.value != null)
			quantity1.value =  new BigDecimal((Double)volume1.value * (Double)concentration1.value).setScale(2, RoundingMode.HALF_UP);
		else
			quantity1.value= null;

		if (quantity1 != null && quantity1.value != null)
			quantity1.unit = "ng";		
		else
			quantity1.unit = null;
		logger.info(icu.code+" quantity1 "+quantity1.value);
	}

	//Calcul de la concentration en nM
	// Elle est fonction de la taille et de la concentration finale
	private void computeConcnM(InputContainerUsed icu){

		PropertySingleValue size = icu.size;
		PropertySingleValue concentration1 = getOrCreatePSV(icu,"concentration1");
		PropertySingleValue concnM = getOrCreatePSV(icu,"nMcalculatedConcentration");

		if (size != null && size.value != null && concentration1 != null && concentration1.value != null){
			concnM.value= (Double)concentration1.value / 660 / (Double)size.value * 1000000;
			concnM.unit = "nM";
		}else{
			concnM.value= null;
			concnM.unit=null;
		}
		logger.info(icu.code+" concnM "+concnM.value);
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
