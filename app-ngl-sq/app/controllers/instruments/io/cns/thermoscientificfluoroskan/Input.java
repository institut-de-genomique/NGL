package controllers.instruments.io.cns.thermoscientificfluoroskan;

import static services.io.ExcelHelper.getNumericValue;
import static services.io.ExcelHelper.getStringValue;
import static services.io.ExcelHelper.isNumericValue;

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
/**
 * Import file from Thermo Scientific Fluoroskan v6
 * 2 modes of import file results
 * Mode 1 normal : results only in sheet "Facteur dilution"
 * Mode 2 replicat : results only in sheet "Moyenne" + "Facteur dilution" (EPGV)
 * @author ejacoby
 *
 */
public class Input extends AbstractInput {

	@Override
	public Experiment importFile(Experiment experiment, PropertyFileValue pfv, ContextValidation contextValidation) throws Exception {		
		InputStream is = new ByteArrayInputStream(pfv.byteValue());

		Workbook wb = WorkbookFactory.create(is);
		//rollback to import v1 NGL-2762 
		//importFirstVersion(experiment, wb, contextValidation);
		
		//Mode 1 normal : only sheet "Facteur dilution"
		//Mode 2 replicat : sheet "Moyenne" + "Facteur dilution"
		Sheet sheetFactDil = null;
		Sheet sheetMoy = null;
		for(int i=0; i<wb.getNumberOfSheets();i++){
			if(wb.getSheetAt(i).getSheetName().toLowerCase().contains("facteur de dilution"))
				sheetFactDil=wb.getSheetAt(i);
			else if(wb.getSheetAt(i).getSheetName().toLowerCase().contains("moyenne"))
				sheetMoy=wb.getSheetAt(i);
		}
		//Get mode
		Sheet workingSheet = null;
		boolean normalMode = false;
		if(sheetFactDil!=null && sheetMoy==null){
			//Mode 1
			workingSheet = sheetFactDil;
			normalMode=true;
		}else if(sheetMoy!=null){
			//Mode 2
			workingSheet = sheetMoy;
		}		
		
		if(workingSheet!=null){
			String plateCodeInFile =null;
			String plateCodeInExp = null;
			if(experiment.instrument.inContainerSupportCategoryCode.equals("tube")){
				//Code Container de la cellule A1 dans inputContainerUsed instrumentProperties fluoroskan line et column
				plateCodeInExp=getCodeContainerFirstPositionFluoPlate(experiment);
				//TODO attendre retour Julie pour savoir où se trouvera le code container de la position A1
				//Pour le moment pas de controle 
				//plateCodeInFile=getStringValue(sheet.getRow(0).getCell(1));
				plateCodeInFile=plateCodeInExp;
			}else{
				//controle nom plaque dans onglet facteur dilution cellule A2
				plateCodeInFile = getStringValue(workingSheet.getRow(1).getCell(0));
				plateCodeInExp = experiment.inputContainerSupportCodes.iterator().next();
			}

			logger.debug("Start Contextvalidation Error "+ plateCodeInExp +", "+ plateCodeInFile );

			if (plateCodeInExp.equals(plateCodeInFile)) {

				String codePropertiesConcDil   = null;
				String codePropertiesConcReelle = null;
				String codePropertiesDilFactor = null;

				if (experiment.typeCode.equals("reception-fluo-quantification") || 
						experiment.typeCode.equals("fluo-quantification")) {

					//Get Type QC 
					//Mode 1 cell A46 in workingSheet 
					//Mode 2 cell A11 in workingSheet
					//Valeurs possibles : Dosage HS1, Dosage HS2, Dosage HS3 et Dosage BR
					String typeQC = null;
					if(normalMode)
						typeQC = getStringValue(workingSheet.getRow(45).getCell(0));
					else
						typeQC = getStringValue(workingSheet.getRow(10).getCell(0));

					logger.debug("Type QC {}", typeQC);
					// Valide typeQC
					if (typeQC == null) {
						contextValidation.addError("Erreur gamme", "Code gamme vide dans fichier");
					} else if (!typeQC.contains(contextValidation.getObject("gamme").toString())) {
						contextValidation.addError("Erreur gamme", "La gamme du fichier "+typeQC+" ne correspond pas au type d'import "+contextValidation.getObject("gamme").toString());
					} else if (typeQC.contains("Dosage BR")) {
						codePropertiesConcDil   = "concentrationDilBR1";
						codePropertiesConcReelle = "concentrationBR1";
						codePropertiesDilFactor = "dilutionFactorBR1";
					} else if(typeQC.contains("Dosage HS1")) {
						codePropertiesConcDil   = "concentrationDilHS1";
						codePropertiesConcReelle = "concentrationHS1";
						codePropertiesDilFactor = "dilutionFactorHS1";
					} else if(typeQC.contains("Dosage HS2")) {
						codePropertiesConcDil   = "concentrationDilHS2";
						codePropertiesConcReelle = "concentrationHS2";
						codePropertiesDilFactor = "dilutionFactorHS2";
					} else if (typeQC.contains("Dosage HS3")) {
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
				
				//Mode 1 normal : B49 to M56
				//Mode 2 replicat : B14 to M21
				Map<String,Double> results = new HashMap<>(0);
				if(normalMode){
					getExcelRangeResults(results, workingSheet, plateCodeInExp, 48, 56, 1, 12,experiment);
				}else{
					getExcelRangeResults(results, workingSheet, plateCodeInExp, 13, 21, 1, 12,experiment);
				}
				
				if (!contextValidation.hasErrors()) {

					experiment.atomicTransfertMethods
					.stream()
					.map(atm -> atm.inputContainerUseds.get(0))
					.forEach(icu -> {
						String key=null;
						if(experiment.instrument.inContainerSupportCategoryCode.equals("tube") &&
								icu.instrumentProperties!=null &&
								icu.instrumentProperties.get("fluoroskanLine")!=null &&
								icu.instrumentProperties.get("fluoroskanColumn")!=null)
							key = "_" + icu.instrumentProperties.get("fluoroskanLine").getValue() + icu.instrumentProperties.get("fluoroskanColumn").getValue();
						else if(!experiment.instrument.inContainerSupportCategoryCode.equals("tube"))
							key = icu.locationOnContainerSupport.code + "_" + icu.locationOnContainerSupport.line + icu.locationOnContainerSupport.column;
						if(key!=null){

							if(results.containsKey(key)){
								PropertySingleValue concentrationDil = getOrCreatePSV(icu,codePropertiesConcDilf);
								concentrationDil.value = results.get(key);
								concentrationDil.unit = "ng/µl";
								if (codePropertiesDilFactorf != null && codePropertiesConcR != null ) {
									if (concentrationDil.value != null){
										computeConcReelle(icu, concentrationDil, codePropertiesDilFactorf, codePropertiesConcR);

									}else{
										//Cas ou on vide un dosage (import cellule vide ou non numerique
										PropertySingleValue concR = getOrCreatePSV(icu, codePropertiesConcR);
										concR.value= null;
										concR.unit=null;
									}
									updateCalculations(icu, experiment.typeCode);
								}
							}
						}
					});
				}

			} else {
				//TODO changer les messages d'erreur avec nouvelles valeur de cellule
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
		}else{
			contextValidation.addError("Erreurs fichier", "Absence onglet Facteur dilution ou Moyenne");
		}
		return experiment;
	}

	/**
	 * Get results value in excel range
	 * Example : get results from B14 to M21 : lineMin=13, lineMax=21, colMin=1, colMax=12
	 * @param results
	 * @param workingSheet
	 * @param plateCodeInExp
	 * @param lineMin : start line excel range (start from 0) inclusive
	 * @param lineMax : end line excel range exclusive
	 * @param colMin : start colum excel range (start 0 equals A...) inclusive
	 * @param colMax : end column excel range exclusive
	 * @param experiment
	 */
	private void getExcelRangeResults(Map<String,Double> results, Sheet workingSheet, String plateCodeInExp, int lineMin, int lineMax, int colMin, int colMax, Experiment experiment)
	{
		String[] lines = { "A", "B", "C", "D", "E", "F", "G", "H" };
		//iterate excel line
		for (int i = lineMin; i < lineMax; i++) {
			String line = lines[i-lineMin];
			// iterate excel column
			for (int j = colMin; j <= colMax; j++) {
				String key = "";
				if(experiment.instrument.inContainerSupportCategoryCode.equals("tube"))
					key = "_" + line + j;
				else
					key = plateCodeInExp + "_" + line + j;
				//import only positive value 
				//empty result for non numeric or negative value 
				if ( (workingSheet.getRow(i).getCell(j) != null) && (isNumericValue(workingSheet.getRow(i).getCell(j))) ){
					Double concentration1 = getNumericValue(workingSheet.getRow(i).getCell(j));
					if(concentration1>=0)
						results.put(key,concentration1);
					else
						results.put(key,null);
				}else { //Cas où la cellule est vide ou format incorrect
					results.put(key,null);
				}

			}								
		}
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
		concnM.unit = "truc";
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
	
	/**
	 * NGL-2762 : first version before upgrade software Thermo Scientific Fluoroskan v6
	 * @param exp
	 * @param wb
	 */
	private void importFirstVersion(Experiment experiment, Workbook wb, ContextValidation contextValidation)
	{
		//Get sheet "Facteur dilution" => si null erreur
				//Get sheet "Moyenne..." optional
				Sheet sheetFactDil = null;
				Sheet sheetMoy = null;
				for(int i=0; i<wb.getNumberOfSheets();i++){
					if(wb.getSheetAt(i).getSheetName().toLowerCase().contains("facteur de dilution"))
						sheetFactDil=wb.getSheetAt(i);
					else if(wb.getSheetAt(i).getSheetName().toLowerCase().contains("moyenne"))
						sheetMoy=wb.getSheetAt(i);
				}
				if(sheetFactDil!=null){
					//Sheet sheet = wb.getSheetAt(0);
					String plateCodeInFile =null;
					String plateCodeInExp = null;
					if(experiment.instrument.inContainerSupportCategoryCode.equals("tube")){
						//Code Container de la cellule A1
						plateCodeInExp=getCodeContainerFirstPositionFluoPlate(experiment);
						//TODO attendre retour Julie pour savoir où se trouvera le code container de la position A1
						//Pour le moment pas de controle 
						//plateCodeInFile=getStringValue(sheet.getRow(0).getCell(1));
						plateCodeInFile=plateCodeInExp;
					}else{
						//controle nom plaque dans onglet facteur dilution cellule A2
						plateCodeInFile = getStringValue(sheetFactDil.getRow(1).getCell(0));
						plateCodeInExp = experiment.inputContainerSupportCodes.iterator().next();
					}

					logger.debug("Start Contextvalidation Error "+ plateCodeInExp +", "+ plateCodeInFile );

					if (plateCodeInExp.equals(plateCodeInFile)) {

						String codePropertiesConcDil   = null;
						//String codePropertiesConcFinal = null;
						String codePropertiesConcReelle = null;
						String codePropertiesDilFactor = null;

						if (experiment.typeCode.equals("reception-fluo-quantification") || 
								experiment.typeCode.equals("fluo-quantification")) {

							//Get type QC  cellule A11 dans la onglet facteur dilution chercher motif et plus valeur exacte
							//Valeurs possibles : Dosage HS1, Dosage HS2, Dosage HS3 et Dosage BR
							String typeQC = getStringValue(sheetFactDil.getRow(10).getCell(0));

							logger.debug("Type QC {}", typeQC);
							// Valide typeQC
							if (typeQC == null) {
								contextValidation.addError("Erreur gamme", "Code gamme vide dans fichier");
							} else if (!typeQC.contains(contextValidation.getObject("gamme").toString())) {
								contextValidation.addError("Erreur gamme", "La gamme du fichier "+typeQC+" ne correspond pas au type d'import "+contextValidation.getObject("gamme").toString());
							} else if (typeQC.contains("Dosage BR")) {
								codePropertiesConcDil   = "concentrationDilBR1";
								codePropertiesConcReelle = "concentrationBR1";
								codePropertiesDilFactor = "dilutionFactorBR1";
							} else if(typeQC.contains("Dosage HS1")) {
								codePropertiesConcDil   = "concentrationDilHS1";
								codePropertiesConcReelle = "concentrationHS1";
								codePropertiesDilFactor = "dilutionFactorHS1";
							} else if(typeQC.contains("Dosage HS2")) {
								codePropertiesConcDil   = "concentrationDilHS2";
								codePropertiesConcReelle = "concentrationHS2";
								codePropertiesDilFactor = "dilutionFactorHS2";
							} else if (typeQC.contains("Dosage HS3")) {
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
						//Get second sheet 
						logger.debug("Nb sheet "+wb.getNumberOfSheets());

						//onglet moyenne mode replicat plage B14 à M21
						//sinon onglet facteur dilution mode normal B14 à M21
						Sheet sheetResults = sheetFactDil;
						if(sheetMoy!=null)
							sheetResults=sheetMoy;

						Map<String,Double> results = new HashMap<>(0);
						//			String[] lines = new String[] { "A", "B", "C", "D", "E", "F", "G", "H" };
						String[] lines = { "A", "B", "C", "D", "E", "F", "G", "H" };
						//Resultats dans cellule B14 à M21
						for (int i = 13; i < 21; i++) {
							String line = lines[i-13];
							// column
							for (int j = 1; j <= 12; j++) {
								String key = "";
								if(experiment.instrument.inContainerSupportCategoryCode.equals("tube"))
									key = "_" + line + j;
								else
									key = plateCodeInExp + "_" + line + j;
								//que des valeurs numériques (valeurs  postives ou négatives)à importer si non numériques  ne rien importer valeur vide
								if ( (sheetResults.getRow(i).getCell(j) != null) && (isNumericValue(sheetResults.getRow(i).getCell(j))) ){
									Double concentration1 = getNumericValue(sheetResults.getRow(i).getCell(j));
									//	logger.info("Conc "+concentration1+" key "+key);
									results.put(key,concentration1);
								}else { //Cas où la cellule est vide ou format incorrect
									//	logger.info("Conc null: key: "+key);
									results.put(key,null);
								}

							}								
						}

						if (!contextValidation.hasErrors()) {

							experiment.atomicTransfertMethods
							.stream()
							.map(atm -> atm.inputContainerUseds.get(0))
							.forEach(icu -> {
								String key=null;
								if(experiment.instrument.inContainerSupportCategoryCode.equals("tube") &&
										icu.instrumentProperties!=null &&
										icu.instrumentProperties.get("fluoroskanLine")!=null &&
										icu.instrumentProperties.get("fluoroskanColumn")!=null)
									key = "_" + icu.instrumentProperties.get("fluoroskanLine").getValue() + icu.instrumentProperties.get("fluoroskanColumn").getValue();
								else if(!experiment.instrument.inContainerSupportCategoryCode.equals("tube"))
									key = icu.locationOnContainerSupport.code + "_" + icu.locationOnContainerSupport.line + icu.locationOnContainerSupport.column;
								if(key!=null){

									if(results.containsKey(key)){
										PropertySingleValue concentrationDil = getOrCreatePSV(icu,codePropertiesConcDilf);
										concentrationDil.value = results.get(key);
										//	logger.info(" key "+key+" Conc "+concentrationDil.value);
										concentrationDil.unit = "ng/µl";
										if (codePropertiesDilFactorf != null && codePropertiesConcR != null ) {
											if (concentrationDil.value != null){
												computeConcReelle(icu, concentrationDil, codePropertiesDilFactorf, codePropertiesConcR);

											}else{
												//Cas ou on vide un dosage (import cellule vide ou non numerique
												PropertySingleValue concR = getOrCreatePSV(icu, codePropertiesConcR);
												concR.value= null;
												concR.unit=null;
											}
											updateCalculations(icu, experiment.typeCode);
										}
									}
								}
							});
						}

					} else {
						//TODO changer les messages d'erreur avec nouvelles valeur de cellule
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
				}else{
					contextValidation.addError("Erreurs fichier", "Absence onglet Facteur dilution");
				}
	}

}
