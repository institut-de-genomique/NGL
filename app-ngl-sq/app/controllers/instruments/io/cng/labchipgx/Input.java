package controllers.instruments.io.cng.labchipgx;

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
	
//	private static final play.Logger.ALogger logger = play.Logger.of(Input.class);
	
   /* Description du fichier a traiter: TXT CSV généré par labchipGX
    * 10/08/2016 NGL-1030 la taille est de nouveau obligatoire !!                
	*Well Label,Region[200-2000] Conc. (ng/ul),Region[200-2000] Size at Maximum [BP]
	* 07/09/2016 la 2eme colonne peut aussi etre une molarité en nmol/l
	*Well Label,Region[300-900] Molarity (nmol/l),Region[300-900] Size [BP]
	*A01,3.7401558465,540.3455
	*A02,...
	*   attention les valeurs Region [xxx-zzz] sont variables ne pas les prendre en compte pour la verification de l'entete
	*/
	
	@Override
	public Experiment importFile(Experiment experiment,PropertyFileValue pfv, ContextValidation contextValidation) throws Exception {	
			
		// hashMap  pour stocker les concentrations et size du fichier 
		Map<String,LabChipData> dataMap = new HashMap<>(0);
		
		// 07/09/2016 l'unité de concentration est variable suivant les fichiers !! 
		// String unit="";  ne marche pas car en ligne 146 le compilateur reclame un objet final...utiliser StringBuilder (N Wiart)
		StringBuilder unit = new StringBuilder();
		try (InputStream is = new ByteArrayInputStream(pfv.byteValue());
			 BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
			int n = 0;
			String line;

			while ((line = reader.readLine()) != null) {	 
				/// attention si le fichier vient d'une machine avec LOCALE = FR les décimaux utilisent la virgule!!!????
				String[] fields = InputHelper.parseCSVLine (line);

				// verifier la premiere ligne d'entete
				if (n == 0) {
					if ( ! fields[0].matches("Well Label(.*)") ) {
						contextValidation.addError("Erreurs fichier","experiments.msg.import.header-label.missing","1", "Well Label*");
						return experiment;
					}

					if ( ( !  fields[1].matches("(.*)Conc.(.*)")) &&  ( !  fields[1].matches("(.*)Molarity(.*)")) ) {
						contextValidation.addError("Erreurs fichier","experiments.msg.import.header-label.missing","1", "*Conc* or *Molarity*");
						return experiment;
					}
					// trouver la bonne unité
					if ( fields[1].matches("(.*)Conc.(.*)")){
						unit.append("ng/µl");
					} else {
						unit.append("nM");
					}

					if ( ! fields[2].matches("(.*)Size(.*)") ) {
						contextValidation.addError("Erreurs fichier","experiments.msg.import.header-label.missing","1", "*Size*");
						return experiment;
					}
				}

				// commencer le traitement en sautant la 1ere ligne d'entete
				if (n > 0 ) {
					// description d'une ligne de donnees: A01,3.7401558465,551.4705882353,comment (comments est optionnel)
					if (( fields.length  < 3 ) || ( fields.length  > 4 )) {
						contextValidation.addError("Erreurs fichier", "experiments.msg.import.linefields.unexpected",n );
						n++;
						continue;
					}

					String pos384 = fields[0];
					// verifier que c'est une position 384 valide ???
					if ( !InputHelper.isPlatePosition(contextValidation, pos384 , 384, n)){
						n++;
						continue;
					} else {
						// inutile car position deja sur 3 caracteres pour le LabChipGX!!!
						//String pos0384=InputHelper.add02pos(pos384);

						// Attention en CSV les decimaux sont sous forme xxxx,yy si le fichier vient d'un machine avec LOCALE=FR...
						logger.info ("conc="+fields[1]+" size="+fields[2]);
						double conc = Double.parseDouble(fields[1].replace(",","."));
						double sz   = Double.parseDouble(fields[2].replace(",","."));
						int rsz = (int)Math.round(sz); // 19/09/2016: arrondir les sizes a l'entier le plus proche...

						LabChipData data=new LabChipData(conc,rsz);

						dataMap.put(pos384, data);
					}
				} 
				n++;
			} //end while
		}
		
		if (contextValidation.hasErrors()) { 
			return experiment;
		}
		
		// Verifier que tous les puits de l'experience ont des données dans le fichier => GA 18/04/2016 vu avec Julie pas utile
		/* 
		if (!contextValidation.hasErrors()) {
			experiment.atomicTransfertMethods
				.stream()
				.map(atm -> atm.inputContainerUseds.get(0))
				.forEach(icu -> {
					String icupos=InputHelper.getCodePosition(icu.code);
					// ajouter un "0" pour pouvoir comparer...
				    String icupos0=InputHelper.add02pos(icupos);

				    if (!dataMap.containsKey(icupos0) ){
						contextValidation.addErrors("Erreurs fichier", "experiments.msg.import.concentration.missing",icupos0);
					} 
				});
		}
		*/
		
		// ne positionner les valeurs que s'il n'y a pas d'erreur a la verification precedente...
		if (!contextValidation.hasErrors()) {
			experiment.atomicTransfertMethods
				.stream()
				.map(atm -> atm.inputContainerUseds.get(0))
				.forEach(icu -> {
					String icupos=InputHelper.getIcuPosition(icu);
					// ajouter un "0" pour pouvoir comparer...
				    String icupos0 = InputHelper.add02pos(icupos);
						
					PropertySingleValue concentration1 = getOrCreatePSV(icu, "concentration1");
					if (dataMap.containsKey(icupos0)) {
						concentration1.value = dataMap.get(icupos0).concentration;
						// concentration1.unit = unit; ne marche pas si unit n'est pas "final"
						concentration1.unit = unit.toString();
					}
					// 10/08/2016 retour de la taille !!!!
					PropertySingleValue size1 = getOrCreatePSV(icu, "size1");
					if (dataMap.containsKey(icupos0)) {
						size1.value = dataMap.get(icupos0).size;
						size1.unit = "pb";
					}					
				});
		}
		
		return experiment;
	} // end import
	
	// NGL-1030: 10/08/2016: retour de la taille !!!!
	public class LabChipData {
		private double concentration;
		private double size;
		
		public LabChipData(double conc, double sz) {
			concentration = conc;
			size          = sz;
		}
		
	}
}
