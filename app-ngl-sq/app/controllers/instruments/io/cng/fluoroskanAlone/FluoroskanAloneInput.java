package controllers.instruments.io.cng.fluoroskanAlone;

import static services.io.ExcelHelper.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.commons.io.FilenameUtils;
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
	   21/09/2021 ajout 1 colonne soustraction de blanc: la colonne "Facteur de dilution" passe à la colonne H (=7)
	   
	   27/01/2023 NGL-4130 nouvelle version de logiciel du Fluoroskan de ThermoFisher 
	     Le fichier .xlsx généré est modifié
	*/
	/**
	 * Logger.
	 */
	///private static final play.Logger.ALogger logger = play.Logger.of(FluoroskanAloneInput.class);
	
	/**
	 * NGL-4130 pour anticiper de nouvelle modifications=> tout parametrer !!
	 * keys and positions
	 */
	private static final String SHEET_KEY="Récapitulatif de résultat_05";
	
	private static final String RECAP_HEADER_KEY="Récapitulatif de résultat";
	private static final int    RECAP_HEADER_COL=0;
	private static final int    RECAP_HEADER_LINE=0;
	private static final String GENERAL_HEADER_KEY="Général";
	private static final int    GENERAL_HEADER_COL=0;
	private static final int    GENERAL_HEADER_LINE=2;
	
	private static final int DATA_HEADER_LINE=4;
	private static final String WELL_KEY="Puits";
	private static final int    WELL_COL=1;
	private static final String DILUTION_FACTOR_KEY="Facteur de dilution 1 (485/538nm)";
	private static final int    DILUTION_FACTOR_COL=6;
	
	private static final int DATA_START_LINE =DATA_HEADER_LINE +1;
	
	@Override
	public Experiment importFile(Experiment experiment, PropertyFileValue pfv, ContextValidation contextValidation) throws Exception {
		
		String inputSupportContainerCode=experiment.inputContainerSupportCodes.iterator().next().toString();
		//logger.debug (">>checking barcode "+inputSupportContainerCode);
		
		// vérifier que le nom du fichier correspond au barcode de la plaque à traiter + secteur demandé
		String sector = checkCorrectFileName(experiment, inputSupportContainerCode, pfv, contextValidation );
		
		// lire le fichier MS-Excel
		Workbook wb= getWorkbook (pfv, contextValidation );
		if ( wb == null) {
			contextValidation.addError("Erreurs fichier","experiments.msg.import.filetype.unexpected","MS-Excel");
			return experiment;
		}	
		
		// vérifier que l'onglet de données est bien présent
		Sheet sheet = wb.getSheet(SHEET_KEY); // chgt nom onglet NGL-4130
		if (sheet == null ) {
			contextValidation.addError("Erreurs fichier", "experiments.msg.import.sheet.missing", SHEET_KEY);
			return experiment;
		}
		
		// vérifier les entetes de l'onglet de données
		checkSheetHeaders (sheet, contextValidation);
		
		// si erreur a ce state, arreter !!
		if (contextValidation.hasErrors()) {
			return experiment;
		}
		
		//===========================================
		
		Map<String, Double> results = new HashMap<>(0);
		
		// traiter les 48 lignes (en commencant en ligne DATA_START_LINE)
		for(int i = DATA_START_LINE; i < DATA_START_LINE +48; i++){
			String platePosition = getStringValue(sheet.getRow(i).getCell(WELL_COL));
			
			//verifier que c'est une position définie et valide 
			// 01/02/2023 essai avec la demi plaque 48 pour exclure des position en colonne > 06
			if (ValidationHelper.validateNotEmpty(contextValidation, platePosition, "plate Position; line "+(i+1)) &&
					InputHelper.isPlatePosition(contextValidation,platePosition, 48, (i+1))){
				
				// convertir la position du fichier en position de plaque inputSupport en tenant compte du secteur G ou D (mapping)
				String inputSupportPosition=convertPosition(platePosition, sector);
				
				// vérifier si la position n'a pas déjà été stockée
				if (results.containsKey(inputSupportPosition)) {
					contextValidation.addError("Erreurs fichier","experiments.msg.import.position.duplicate", (i+1), platePosition);
				}
				// vérifier la valeur de concentration
				// si le fichier vient d'une machine avec LOCALE = FR les décimaux utilisent la virgule; OK géré par getNumericValue
				// le cas "NaN" ne passe pas dans catch (NumberFormatException) !!! => le traitre explicitement
				try {
					//!!!!    la concentration est lue dans la colonne  Facteur de dilution  !!!!!
					Double conc = getNumericValue(sheet.getRow(i).getCell(DILUTION_FACTOR_COL));
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
					contextValidation.addError("Erreurs fichier", "experiments.msg.import.value.wrongtype", (i+1),"conc", sheet.getRow(i).getCell(DILUTION_FACTOR_COL),"Numérique" );
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
		//logger.debug ("updating experimentProperties => set concentration");
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
					//logger.debug (">> icupos={} => {}", icupos, concPsv.value);
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
		//logger.debug("sector= {}", sector);
		//logger.debug("position dans l experience="+ platePosition + "=> colonne =" + platePosition.substring(1));
		switch (sector) {
			case "G" : if ( Integer.parseInt(platePosition.substring(1)) < 7 ) { return true; } else { return false;}
			case "D" : if ( Integer.parseInt(platePosition.substring(1)) > 6 ) { return true; } else { return false;}
			default:
				//logger.debug("Incorrect sector !!!");
				return false;
		}
	}
	
	
	private void checkSheetHeaders (Sheet sheet, ContextValidation contextValidation) {
		/* description de l'onglet a traiter:
		 * 	   21/09/2021 ajout 1 colonne soustraction de blanc: la colonne "Facteur de dilution" passe à la colonne H (=7??????)

		0|Récapitulatif de résultats|             						
		1|				
		2|Général|
		3|
		4|Plaque	|Puits	|Échantillon|Fluorescence 1 (485/538nm) |Soustraction du blanc 1 (485/538nm)|Courbe standard 1 (485/538nm)|Facteur de dilution 1 (485/538nm)|
		5|gauche_P	|A01	|sample0001 |0,5919					 	|0,2266								|0,2165						  |1,082							|
		.....
		52|gauche_P	|H06    |.......
		___________________________________________________________________________________________________________________________________________________________________________
		      A       B         C                 D                                   E                              F                               G 
		     (0)     (1)       (2)               (3)                                 (4)                            (5)                             (6)
		
		au dela la ligne 52 (puit H06) se trouvent des standards de calibration puis des graphiques etc...
		
		27/01/2023 NGL-4130 nouvelle version de logiciel du Fluoroskan de ThermoFisher :colonne "Groupe" en position C supprimée
		   =>décalage des colonnes Echantillons et suivantes vers la gauche !
		*/
		
		// vérifier qu'on trouve 4 headers précis pour s'assurer que c'est un fichier correct
		// NGL-4130: "résultats sans S"
		if ( (getStringValue(sheet.getRow(RECAP_HEADER_LINE).getCell(RECAP_HEADER_COL))==null ) ||
			!(getStringValue(sheet.getRow(RECAP_HEADER_LINE).getCell(RECAP_HEADER_COL)).equals(RECAP_HEADER_KEY))){
			contextValidation.addError("Erreurs fichier", "experiments.msg.import.header-label.missing",RECAP_HEADER_LINE +1,RECAP_HEADER_KEY);
		}
		if ( (getStringValue(sheet.getRow(GENERAL_HEADER_LINE).getCell(GENERAL_HEADER_COL))==null ) || 
			!(getStringValue(sheet.getRow(GENERAL_HEADER_LINE).getCell(GENERAL_HEADER_COL)).equals(GENERAL_HEADER_KEY))){
			contextValidation.addError("Erreurs fichier", "experiments.msg.import.header-label.missing",GENERAL_HEADER_LINE +1, GENERAL_HEADER_KEY);
		}
		if ( (getStringValue(sheet.getRow(DATA_HEADER_LINE).getCell(WELL_COL))==null ) || 
			!(getStringValue(sheet.getRow(DATA_HEADER_LINE).getCell(WELL_COL)).equals(WELL_KEY))){
			contextValidation.addError("Erreurs fichier", "experiments.msg.import.header-label.missing", DATA_HEADER_LINE +1, WELL_KEY);
		}
		// NGL-4130 retour de la colonne "Facteur de dilution 1 (485/538nm)" en position 6
		if ( (getStringValue(sheet.getRow(DATA_HEADER_LINE).getCell(DILUTION_FACTOR_COL))==null ) || 
			!(getStringValue(sheet.getRow(DATA_HEADER_LINE).getCell(DILUTION_FACTOR_COL)).equals(DILUTION_FACTOR_KEY))){
			contextValidation.addError("Erreurs fichier", "experiments.msg.import.header-label.missing",DATA_HEADER_LINE +1, DILUTION_FACTOR_KEY);
		}
	}
	 
	private String checkCorrectFileName(Experiment experiment, String inputSupportContainerCode, PropertyFileValue pfv, ContextValidation contextValidation ) {
		// récupérer le secteur de plaque demandé pour l'expérience (peut positionner des erreurs dans le contexte)
		String sector = getSector(experiment, contextValidation);
		if (sector != null ) {
			String basename=FilenameUtils.getBaseName(pfv.fullname);
			// exemple de fichier valide: A006AJW_D_QuantiT_BR_blanc_P262_1_27_2023%2011_06_58.xlsx
			String[] parts = basename.split("_"); 
			// qd split échoue tout se retrouve dans parts[0]
			if (parts.length == 1 ) {
				contextValidation.addError("Erreurs fichier", "Le nom de fichier n'est pas de la forme <barcode>_<secteur>");
			} else if ( ! parts[0].equals(inputSupportContainerCode) || ! parts[1].equals(sector) ) {
				// les elements execedentaires sont ignorés...
				contextValidation.addError("Erreurs fichier", "Le nom de fichier ne correspond pas à : '"+inputSupportContainerCode+"_"+sector+"'");
			} 
		}
		return sector;
	}
	
	private Workbook getWorkbook (PropertyFileValue pfv, ContextValidation contextValidation ) {
		try {
			InputStream is = new ByteArrayInputStream(pfv.byteValue());
			//logger.debug ("processing file "+pfv.fullname);
			return WorkbookFactory.create(is);
		
		} catch(InvalidFormatException | IllegalArgumentException | IOException e) {
				contextValidation.addError("Erreurs fichier",e.getMessage());
				return null;
		}
	}
}
