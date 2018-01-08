package controllers.instruments.io.cns.fluoroskan;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import models.laboratory.common.instance.property.PropertyFileValue;
import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.experiment.instance.Experiment;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import validation.ContextValidation;
import controllers.instruments.io.utils.AbstractInput;

import play.Logger;

public class Input extends AbstractInput {

	@Override
	public Experiment importFile(Experiment experiment,PropertyFileValue pfv,
			ContextValidation contextValidation) throws Exception {		
		InputStream is = new ByteArrayInputStream(pfv.value);
		
		Workbook wb = WorkbookFactory.create(is);
		Sheet sheet = wb.getSheetAt(0);
		
		String plateCodeInFile = getStringValue(sheet.getRow(0).getCell(1));
		String plateCodeInExp = experiment.inputContainerSupportCodes.iterator().next();
		
		if(plateCodeInExp.equals(plateCodeInFile)){

		String codePropertiesConcDil = 	null  ;
			String codePropertiesConcFinal = null ;
			String codePropertiesDilFactor = null ;

			if(experiment.typeCode.equals("reception-fluo-quantification") || 
					experiment.typeCode.equals("fluo-quantification")){
				
				String typeQC= getStringValue(sheet.getRow(0).getCell(3));
				
				Logger.debug("Type QC "+typeQC);
				//Valide typeQC
				if(typeQC==null){
					contextValidation.addErrors("Erreur gamme", "Code gamme vide dans fichier");
				}else if(!typeQC.contains(contextValidation.getObject("gamme").toString())) {
					contextValidation.addErrors("Erreur gamme", "La gamme du fichier "+typeQC+" ne correspond pas au type d'import "+contextValidation.getObject("gamme").toString());
				}else if(typeQC.equals("BR")){
					codePropertiesConcDil="concentrationDilBR1";
					codePropertiesConcFinal="concentrationBR1";
					codePropertiesDilFactor="dilutionFactorBR1";
				}else if(typeQC.equals("HS")){
					codePropertiesConcDil="concentrationDilHS1";
					codePropertiesConcFinal="concentrationHS1";
					codePropertiesDilFactor="dilutionFactorHS1";
				}else if(typeQC.equals("HS2")){
					codePropertiesConcDil="concentrationDilHS2";
					codePropertiesConcFinal="concentrationHS2";
					codePropertiesDilFactor="dilutionFactorHS2";
				}else{
					contextValidation.addErrors("Erreur gamme", "Code gamme non géré : "+typeQC);	
				}
				
			}
			
			final String codePropertiesConcDilf = codePropertiesConcDil ;
			final String codePropertiesConcFinalf = codePropertiesConcFinal ;
			final String codePropertiesDilFactorf = codePropertiesDilFactor ;

			Map<String,Double> results = new HashMap<String,Double>(0);
			String[] lines = new String[]{"A","B","C","D","E","F","G","H"};
			//line
			for(int i = 17; i < 25; i++){
				String line = lines[i-17];
				//column
				for(int j = 1; j <= 12; j++){
					String key = plateCodeInExp+"_"+line+j;
					Double concentration1 = getNumericValue(sheet.getRow(i).getCell(j));
					results.put(key,concentration1);
				}								
			}
			
			//update.
			if(!contextValidation.hasErrors()){
				experiment.atomicTransfertMethods
					.stream()
					.map(atm -> atm.inputContainerUseds.get(0))
					.forEach(icu -> {
						PropertySingleValue concentration1 = getPSV(icu, "concentration1");
						concentration1.value = results.get(icu.code);
						concentration1.unit = "ng/µl";
						
					});
			}
			
		}else{
			contextValidation.addErrors("Erreurs fichier", "Code de plaque incorrecte : "+plateCodeInFile);
		}
		
		return experiment;
	}

	
	
	

}
