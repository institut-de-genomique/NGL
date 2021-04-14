package controllers.instruments.io.cng.fluoroskanAlone;

import static services.io.ExcelHelper.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

//import org.apache.poi.ss.usermodel.FormulaEvaluator; 
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import controllers.instruments.io.utils.AbstractInput;
import controllers.instruments.io.utils.InputHelper;
import models.laboratory.common.instance.property.PropertyFileValue;
import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.experiment.instance.Experiment;
import models.laboratory.experiment.instance.InputContainerUsed;
///import models.laboratory.experiment.instance.OutputContainerUsed;
//import models.laboratory.parameter.index.Index;
//import models.utils.InstanceConstants;
import validation.ContextValidation;
import validation.utils.ValidationHelper;

public abstract class FluoroskanAloneInput extends AbstractInput {
	
	/* 21/01/2021 Description du fichier a traiter: EXCEL généré par Fluoroskan
		le nom du fichier doit etre <barcode-plaque>_<G ou D>_xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx.xlsx
		Traiter uniquement l'onglet "Récapitulatif de résultat"    (sans S a résultat  !!!)
	*/
	
	@Override
	public Experiment importFile(Experiment experiment, PropertyFileValue pfv, ContextValidation contextValidation) throws Exception {
		
		InputStream is = new ByteArrayInputStream(pfv.byteValue());
		logger.info ("processing file "+pfv.fullname);
		
		String inputSupportContainerCode=experiment.inputContainerSupportCodes.iterator().next().toString();
		//logger.info ("checking barcode "+inputSupportContainerCode);
		
		// récupérer le secteur de plaque demandé pour l'expérience (peut générer des erreurs)
		String sector = getSector(experiment, contextValidation);
		if (sector != null ) {
			// vérifier que le nom du fichier correspond au barcode de la plaque a traiter + secteur demandé
			String[] parts = pfv.fullname.split("_");
			if ( ! parts[0].equals(inputSupportContainerCode) ||  ! parts[1].equals(sector) ) {
				contextValidation.addError("Erreurs fichier", "Le nom de fichier ne correspond pas a "+inputSupportContainerCode+"_"+sector);
			}
		}
		// plusieurs erreurs possibles...si le fichier n'est pas de l'Excel, ou n'est pas lisible...
		Workbook wb = WorkbookFactory.create(is);
		
		//necessaire ??? voir cas d'erreur possibles dans...
		//FormulaEvaluator evaluator = wb.getCreationHelper().createFormulaEvaluator();

		Sheet sheet = wb.getSheet("Récapitulatif de résultat");
		if (sheet == null ) {
			contextValidation.addError("Erreurs fichier", "experiments.msg.import.sheet.missing","Récapitulatif de résultat");
		}
		
		if (contextValidation.hasErrors()) {
			return experiment;
		}
		/* description de l'onglet a traiter:
		1|Récapitulatif de résultats|             						
		2|				
		3|Général|
		4|
		5|Plaque	|Puits	|Groupe		|Échantillon	|Fluorescence 1 (485/538nm)	|Courbe standard 1 (485/538nm)	|Facteur de dilution 1 (485/538nm)|
		6|gauche_P	|A01	|Groupe 1	|sample0001		|0,5919						|0,2165							|1,082								|
		.....
		53|gauche_P	|H06|.......
		
		au dela la ligne H06 se trouvent des standards de calibration puis des graphiques etc...
		*/
		
		// vérifier qu'on trouve 4 headers précis pour s'assurer que c'est un fichier correct
		
		if ((getStringValue(sheet.getRow(0).getCell(0))==null ) || !(getStringValue(sheet.getRow(0).getCell(0)).equals("Récapitulatif de résultats"))){
			contextValidation.addError("Erreurs fichier", "experiments.msg.import.header-label.missing","1","Récapitulatif de résultats");
		}
		if ((getStringValue(sheet.getRow(2).getCell(0))==null ) || !(getStringValue(sheet.getRow(2).getCell(0)).equals("Général"))){
			contextValidation.addError("Erreurs fichier", "experiments.msg.import.header-label.missing","3", "Général");
		}
		if (( getStringValue(sheet.getRow(4).getCell(1))==null ) || !(getStringValue(sheet.getRow(4).getCell(1)).equals("Puits"))){
			contextValidation.addError("Erreurs fichier", "experiments.msg.import.header-label.missing","5", "Puits");
		}
		// la longueur d'onde peut changer ???
		if (( getStringValue(sheet.getRow(4).getCell(6))==null ) || !(getStringValue(sheet.getRow(4).getCell(6)).equals("Facteur de dilution 1 (485/538nm)"))){
			contextValidation.addError("Erreurs fichier", "experiments.msg.import.header-label.missing","5", "Facteur de dilution 1 (485/538nm)");
		}
		
		if (contextValidation.hasErrors()){
			return experiment;
		}
		
		Map<String, Double> results = new HashMap<>(0);
		
		// traiter les 48 lignes (en commencant en ligne 5)
		for(int i = 5; i < 5+48; i++){
			String platePosition = getStringValue(sheet.getRow(i).getCell(1));
			
			//verifier que c'est une position définie et valide 
			if (ValidationHelper.validateNotEmpty(contextValidation, platePosition, "plate Position; line "+(i+1)) &&
					InputHelper.isPlatePosition(contextValidation,platePosition, 96, (i+1))){
				
				// convertir la position du fichier en position de plaque inputSupport en tenant compte du secteur G ou D (mapping)
				String inputSupportPosition=convertPosition(platePosition, sector);
				
				// vérifier si la position n'a pas déjà été stockée
				if (results.containsKey(inputSupportPosition)) {
					contextValidation.addError("Erreurs fichier","experiments.msg.import.position.duplicate", (i+1), platePosition);
				}
				// vérifier la valeur de concentration
				// si le fichier vient d'une machine avec LOCALE = FR les décimaux utilisent la virgule; OK géré par getNumericValue
				// le cas NaN ne passe pas dans catch (NumberFormatException) !!!
				try {
					Double conc = getNumericValue(sheet.getRow(i).getCell(6));
					// valeur manquante 
					if (null == conc) {
						contextValidation.addError("Erreurs fichier", "experiments.msg.import.value.missing", (i+1),"conc");
					} else {
						// if ( conc == Double.NaN) {  !! NE MARCHE PAS !!!
						if (Double.isNaN(conc)) {
							//logger.info("found NaN");
							conc= null;
						}
						results.put(inputSupportPosition, conc);
					}
					
				} catch (NumberFormatException e) { 
					contextValidation.addError("Erreurs fichier", "experiments.msg.import.value.wrongtype", (i+1),"conc", sheet.getRow(i).getCell(6),"Numérique" );
				}
			}	
		}
		
		//validation: verifier que tous les pluits de la plaque  d'entrée recoivent une concentration (cas d'une ligne manquante dans le fichier)
		if (!contextValidation.hasErrors()){
			experiment.atomicTransfertMethods
				.stream()
				.map(atm -> atm.inputContainerUseds.get(0))
				.forEach(icu -> {
					// !! s'il y a dans l'experience des positions qui ne correspondent pas au secteur demandé
					if ( !belongToSector48( InputHelper.getIcuPosition(icu),sector)) {
						contextValidation.addError("Erreur expérience","La position "+InputHelper.getIcuPosition(icu)+ " ne correspond pas au secteur "
					          + experiment.instrumentProperties.get("sector96").value );
					} else {
						// ne verifier si le hash map contient de valeur que si la position est légitime..
						if (!results.containsKey(InputHelper.getIcuPosition(icu))) {
							contextValidation.addError("Erreurs fichier", "experiments.msg.import.concentration.missing",InputHelper.getIcuPosition(icu));
						}
					}
				});
		}
		
		// set concentration 
		logger.info ("updating experimentProperties => set concentration");
		if (!contextValidation.hasErrors()) {
			experiment.atomicTransfertMethods
				.stream()
				.forEach(atm -> {
					InputContainerUsed icu = atm.inputContainerUseds.get(0);
					// dans une experience de QC pas d'output....
					String icupos=InputHelper.getIcuPosition(icu);
					
					// !! verifier d'abord si experimentProperties existe ! sinon la creer
					if (icu.experimentProperties == null) 
						icu.experimentProperties = new HashMap<>();
					
					PropertySingleValue concPsv = new PropertySingleValue();
					concPsv.value = results.get(icupos);
					
					icu.experimentProperties.put("concentration1", concPsv);
					logger.info ("icupos={} => {}", icupos, concPsv.value);
				});
		}
		
	return experiment;
	}
	
	private String getSector(Experiment experiment, ContextValidation contextValidation) {
		if (! experiment.instrumentProperties.containsKey("sector96")) {
			//normalement propriété obligatoie de l'expérience... ne devait pas arriver !!
			contextValidation.addError("Erreur interne","experiments.msg.import.propertie.missing","sector96"); 
			return null;
		}
		PropertySingleValue psv = (PropertySingleValue) experiment.instrumentProperties.get("sector96");
		switch (psv.value.toString()) {
			case "A1-H6"  : return "G";
			case "A7-H12" : return "D"; 
			default:
				contextValidation.addError("Erreur interne","experiments.msg.import.propertie.invalidValue", psv.value.toString(), "sector96"); 
				return null;
		}
	}
	
	private String convertPosition(String platePosition, String sector) {
		// si secteur=G---> rien a faire position fichier = position outputSupport
		// si secteur=D---> décaler de 6 colonnes A1=>A7, B1=>B7
		if ( sector.equals("G") ) { 
			// !! les positions Fluoroskan sont sur 3 positions (A01) alors que celles de NGL sont sur 2 (A1)==> utiliser del02pos
			return InputHelper.del02pos(platePosition);
		} else {
			String row=platePosition.substring(0,1); //premier caractere
			int col=Integer.parseInt(platePosition.substring(1) ); //le reste
			int newcol=col+6;
			return row+newcol ;
		}
	}
	
	private boolean belongToSector48(String platePosition, String sector) {
		//logger.info("sector= {}", sector);
		//logger.info("position dans l experience="+ platePosition + "=> colonne =" + platePosition.substring(1));
		switch (sector) {
			case "G" : if ( Integer.parseInt(platePosition.substring(1)) < 7 ) { return true; } else { return false;}
			case "D" : if ( Integer.parseInt(platePosition.substring(1)) > 6 ) { return true; } else { return false;}
			default:
				logger.info("Incorrect sector !!!");
				return false;
		}
	}
}
