package controllers.instruments.io.cng.spectramax;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import controllers.instruments.io.utils.AbstractInput;
import controllers.instruments.io.utils.InputHelper;
import models.laboratory.common.instance.property.PropertyFileValue;
import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.experiment.instance.Experiment;
import validation.ContextValidation;

public class Input extends AbstractInput {
	
   /* Description du fichier a traiter: VERSION TXT...TAB délimité... fichier original recu en UTF-16 Litle Endian
    |##BLOCKS= 1
    |Group: UnknownsDilution
    |Sample		Wells	RFU		Valeur	dilution ng/µl	Dilution_1	ConcStock ng/ul		Moyenne	
    |A10_		A10		216.58	21.19	5.000			5.000		105.949				105.949	
    |A1_		A1		206.00	20.01	5.000			5.000		100.075				100.075	
    |A2_		A2		265.96	26.67	5.000			5.000		133.362				133.362	
    |...
    |...
    |<ligne vide>
    |Group Column	Formula Name	Formula	Precision	Notation
    |1	Sample	!SampleNames	2 decimal places	Numeric
    |2	Wells	!WellIDs	2 decimal places	Numeric
    |3	RFU	!WellValues	2 decimal places	Numeric
    |4	Valeur	InterpX('Std@Standard Curve',RFU)	2 decimal places	Numeric
    |5	dilution	!SampleDescriptor	3 decimal places	Numeric
    |6	Dilution_1	dilution	3 decimal places	Numeric
    |7	ConcStock ng/ul	Valeur*dilution	3 decimal places	Numeric
    |8	Moyenne	Average('ConcStock ng/ul')	3 decimal places	Numeric
    |
    |Group Summaries
    |InRange	R - Outside standard range			0 decimal places	Numeric Notation 
    |MeanResult	Mean Adjusted Result:	Error	Average(Adj.Result@Group#3)	2 decimal places	Numeric Notation 
    |~End 
    |Original Filename: PLAQUE 01; Date Last Saved: 29/05/2017 11:33:45
	*/
	
	@Override
	public Experiment importFile(Experiment experiment,PropertyFileValue pfv, ContextValidation contextValidation) throws Exception {	
			
		// hashMap  pour stocker les concentrations fichier 
		Map<String,SpectramaxData> dataMap = new HashMap<>(0);
		
		// charset detection (N. Wiart)
		byte[] ibuf = pfv.byteValue();
		String charset = "UTF-8"; //par defaut, convient aussi pour de l'ASCII pur
		
		// si le fichier commence par les 2 bytes ff/fe  alors le fichier est encodé en UTF-16 little endian
		if (ibuf.length >= 2 && (0xff & ibuf[0]) == 0xff && (ibuf[1] & 0xff) == 0xfe) {
			charset = "UTF-16LE";
		}
		
		// String unit="";  ne marche pas car le compilateur reclame un objet final...utiliser StringBuilder (N Wiart)
		StringBuilder unit = new StringBuilder();
		
		try (InputStream is = new ByteArrayInputStream(ibuf);
		     BufferedReader reader = new BufferedReader(new InputStreamReader(is, charset))) {
			int n = 0;
			boolean lastwell=false;
			String line="";
			
			// code pour trouver la bonne unité si jamais celle ci est variable dans le fichier!!!
			//if ( fields[?????].matches("(.*)Conc.(.*)")){
			//	unit.append("ng/µL");
			//} else {
			//	unit.append("nM");
			//}
			
			unit.append("ng/µL");
			
			while (((line = reader.readLine()) != null) && !lastwell ){	 
				// attention si le fichier vient d'une machine avec LOCALE = FR les décimaux utilisent la virgule!!!
				String[] cols = line.replace (",", ".").split("\t");
				logger.info ("line "+n+ " "+ cols.length);
				

				
				// vérifier la ligne d'entête (3 ème ligne du fichier)
				if (n == 2) {
					// NGL-3252 vérifier si on a bien du TAB délimité !
					if ( cols.length == 1){
						contextValidation.addError("Erreurs fichier","experiments.msg.import.filetype.unexpected","TAB délimité");
						return experiment;
					}
					if ( ! cols[1].equals("Wells") ) {
						contextValidation.addError("Erreurs fichier","experiments.msg.import.header-label.missing","1", "Wells");
						return experiment;
					}
					if ( ! cols[7].equals("Moyenne") ) {
						contextValidation.addError("Erreurs fichier","experiments.msg.import.header-label.missing","1", "Moyenne");
						return experiment;
					}
				}
				
				// commencer le traitement en sautant les 3 premières lignes
				if (n > 2 ) {
					// ligne vide trouvée=fin des data utiles
					if ( cols[0].equals("")){
						lastwell=true;
						continue;
					} else {
						// description d'une ligne de données:A10_	A10	216.58	21.19	5.000	5.000	105.949	105.949
						if (( cols.length  != 8 )) {
							contextValidation.addError("Erreurs fichier", "experiments.msg.import.linefields.unexpected",n );
							n++;
							continue; // ne pas sortir permet de verifier tout le fichier
						} else {
							String pos96=cols[1];
							// vérifier que c'est une position 96 valide ???
							if ( !InputHelper.isPlatePosition(contextValidation, pos96 , 96, n)){
								n++;
								continue; // ne pas sortir permet de verifier tout le fichier
							} else {
								// Logger.info ("conc moyenne="+cols[7]);
								double conc=Double.parseDouble(cols[7]);
								// si la valeur trouv2ée est négative ????
								
								SpectramaxData data=new SpectramaxData(conc);
								dataMap.put(pos96, data);
							}
						}
					} 
				}
				
				n++;
			} //end while
		}
		if (contextValidation.hasErrors()) { 
			return experiment;
		}
		
		/*
		// Verifier que tous les puits de l'experience ont des données dans le fichier  ???
		if (!contextValidation.hasErrors()) {
			experiment.atomicTransfertMethods
				.stream()
				.map(atm -> atm.inputContainerUseds.get(0))
				.forEach(icu -> {
					String icupos=InputHelper.getCodePosition(icu.code);

				    if (!dataMap.containsKey(icupos) ){
						contextValidation.addErrors("Erreurs fichier", "experiments.msg.import.concentration.missing",icupos);
					} 
				});
		}
		*/
		
		// ne positionner les valeurs que s'il n'y a pas d'erreur a la vérification precedente...
		if (!contextValidation.hasErrors()) {
			experiment.atomicTransfertMethods
				.stream()
				.map(atm -> atm.inputContainerUseds.get(0))
				.forEach(icu -> {
					String icupos=InputHelper.getIcuPosition(icu);				
					PropertySingleValue concentration1 = getOrCreatePSV(icu, "concentration1");
					if(dataMap.containsKey(icupos)){
						concentration1.value = dataMap.get(icupos).concentration;
						// concentration1.unit = unit; ne marche pas si unit n'est pas "final"
						concentration1.unit = unit.toString();
					}
				});
		}
		
		return experiment;
	} // end import 
	
	// pas de size lue par le Spectramax...
	public class SpectramaxData {
		private double concentration;

		public SpectramaxData (double conc) {
			concentration=conc;
		}
	}
}
