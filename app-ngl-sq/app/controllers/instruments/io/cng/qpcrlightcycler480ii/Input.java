package controllers.instruments.io.cng.qpcrlightcycler480ii;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import models.laboratory.common.description.PropertyDefinition;
import models.laboratory.common.instance.PropertyValue;
import models.laboratory.common.instance.property.PropertyFileValue;
import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.experiment.description.ExperimentType;
import models.laboratory.experiment.description.dao.ExperimentTypeDAO;
import models.laboratory.experiment.instance.Experiment;
import models.laboratory.experiment.instance.InputContainerUsed;
import models.laboratory.parameter.index.Index;
import play.Logger;
import validation.ContextValidation;
import validation.utils.ValidationHelper;
import controllers.instruments.io.utils.AbstractInput;
import controllers.instruments.io.utils.InputHelper;

public class Input extends AbstractInput {
	
	
   /* Description du fichier a traiter: TXT TAB délimité généré par LightCycler
	*
	*Experiment: 150929_KAPA-Lib-Quant_PCRFREE_P17  Selected Filter: SYBR Green I / HRM Dye (465-510)
	*Include	Color	Pos		Name		Cp		Concentration	Standard	Status
	*True		255		A1		Sample 1	9.66	4.71E0			0	
	*True		255		A2		Sample 2	9.86	4.12E0			0	
	*True		255		A3		Sample 3	10.01	3.73E0			0	
	*.....    	
	*	 
	*   !!!! si les operateurs cochent/decochent des puits dans le logiciel qui genere le fichier 
	*  l'ordre des puits n'est plus assuré...
	*   Pour les puits en erreur, il n'y a rien dans les colonnes "Cp" et "Concentration"
	*   Ignorer les lignes dont la colonne est > 20, ce sont des controles ( voir aussi remapPosition() )
	*/
	
	@Override
	public Experiment importFile(Experiment experiment,PropertyFileValue pfv, ContextValidation contextValidation) throws Exception {	
		
		int sector=0;
		
		ExperimentType experimentType = ExperimentType.find.findByCode(experiment.typeCode);
		PropertyDefinition correctionFactorLibrarySizeDefault = experimentType.getMapPropertyDefinition().get("correctionFactorLibrarySize"); 
		
		
		if (experiment.instrumentProperties.containsKey("sector96")){
			PropertySingleValue psv = (PropertySingleValue) experiment.instrumentProperties.get("sector96");
			Logger.info( "sector96="+ psv.value.toString() );
			
			if ( psv.value.toString().equals("1-48") ){ 
				sector=0; 
			}else { 
				sector=1;
			}
			Logger.info( "sector="+sector);
		}else {
			contextValidation.addErrors("Erreur","valeur de 'sector96' non supportée");
			return experiment;
		}
		
		/* 07/04 question pour Julie: ne vaut-il pas mieux avoir un facteur constant pour tout le fichier 
		 * plutot q'une valeur par ligne ???
		 * int correctionFactorLibrarySize= un parametre choisi par l'utilisateur....
		 */
		
		//tableau des facteurs de dilution et leur repetition sur la plaque 384
		double[] fDilution={5000,5000,5000,   50000,50000,50000};
		int nbRep=6; // 2 dilutions avec 3 repetition=> 6; chaque puit 96 initial est traité 6 fois dans la plaque 384
		
		// hashMap  pour stocker les concentrations du fichier
		Map<String,Double> data = new HashMap<String,Double>(0);
		
		// ajout 03/08/2017 charset detection (N. Wiart)
		byte[] ibuf = pfv.value;
		String charset = "UTF-8"; //par defaut, convient aussi pour de l'ASCII pur
		
		// si le fichier commence par les 2 bytes ff/fe  alors le fichier est encodé en UTF-16 little endian
		if (ibuf.length >= 2 && (0xff & ibuf[0]) == 0xff && (ibuf[1] & 0xff) == 0xfe) {
			charset = "UTF-16LE";
		}
		
		InputStream is = new ByteArrayInputStream(ibuf);
		
		// Ce n'est pas un fichier MS-Excel mais un fichier TXT TAB delimité a lire ligne a ligne. 
		// utiliser un bufferReader...Merci Nicolas	!! 03/08/2017 ajout charset
		BufferedReader reader = new BufferedReader(new InputStreamReader(is, charset));
		int n = 0;
		String line;
		
		while ((line = reader.readLine()) != null) {
			/// attention si le fichier vient d'une machine avec LOCALE = FR les décimaux utilisent la virgule!!!
			String[] cols = line.replace (",", ".").split("\t");
			
			// verifier la premiere ligne d'entete
			if ((n == 0) && ( ! line.matches("Experiment:(.*)") ) ){
				contextValidation.addErrors("Erreurs fichier","experiments.msg.import.header-label.missing","2", "Experiment:");
				return experiment;
			}
			// commencer le traitement en sautant la 2eme ligne d'entete
			if (n > 1 ) {
				// description d'une ligne de donnees:
				//  0         1      2       3          4            5             6          7
				//Include	Color	Pos		Name		Cp		Concentration	Standard	Status
				// status est optionnel ????
				//Logger.info ("ligne "+n+": lg="+cols.length );
				if ( cols.length < 7 || cols.length > 8 ) {
					contextValidation.addErrors("Erreurs fichier", "experiments.msg.import.linefields.unexpected",(n+1) );
					continue;
				}
			
				String pos384=cols[2];
				// verifier que c'est une position 384 valide ???
				if ( !InputHelper.isPlatePosition(contextValidation, pos384 , 384, n)){
					continue;
				} else {
				  // !!! il faut une position sur 3 caracteres pour pouvoir par la suite trier dans l'ordre reel !!!
				  String pos0384=InputHelper.add02pos(pos384);
				
				  int col384 = Integer.parseInt(pos384.substring(1));
				  // ignorer les lignes correspondant aux temoins (colonnes 22,23,24)
				  if (col384 > 20 ){ continue; }
			 
				  /* Amelioration 17/05/2016: N'inclure un puit dans le calcul de moyenne que si
				     la colonne include est égale a 'True' ET la colonne color=255
				      =>forcer a 0 ici
				  */	  
				  double concentration;
				  if ( cols[0].equals("True") && cols[1].equals("255") ){ 
					  concentration=Double.parseDouble(cols[5]);   
					  data.put(pos0384, concentration);
				  }
				  else { 
					  data.put(pos0384, 0d);
				  }
				}
		  }
		  ++n;
		}

		reader.close();
		
		if (contextValidation.hasErrors()){ 
			return experiment;
		}
		
		// nouveau HashMap pour les concentrations calculees en nM
		Map<String,Double> results = new HashMap<String,Double>(0);
		
		/* traiter dans l'ordre des positions; 
		 * 6 lignes successives (1 block) doivent correspondre au meme echantillon avec 3 repetions de 2 dilutions
		 */
		SortedSet<String> pos0384 = new TreeSet<String>(data.keySet());
		int nbblock=0;
		int rep=0;;
		double[] listConc= new double[nbRep];
		// 07/04 Voir commentaire plus haut...double rocheFactor= (double)( 452 / correctionFactorLibrarySize); //calculé une seule fois
		
		for (String key : pos0384) { 
			// transformer la concentration du fichier (pM) en nM [ formule donné par Roche ]
			// conc_nM= conc_pM * ( fact_dilution/1000 ) * ( 452 / correctionFactorLibrarySize ) 
			    // 07/04 reporter la correction  plus loin, qd on a la valeur de correctionFactorLibrarySize.....
			    // double concentration_nM =  data.get(key) * (double)( fDilution[rep] / 1000 ) * rocheFactor;
			double concentration_nM =  data.get(key) * (double)( fDilution[rep] / 1000 );
			
			// stocker concentration pour faire moyenne plus tard...
			listConc[rep]=concentration_nM;
			//Logger.info ("pos0384="+key+" CONC (pM)="+  data.get(key) );
			
			nbblock++;
			rep++;
			if ((nbblock % nbRep ) == 0 ) {	
				//nbblock est multiple de 6 => fin d'un block de lignes
				//17/05/2016 calcul de la moyenne des 6 concentrations en ne tenant pas compte des concentrations a 0
				double moyConc_nM= meanNo0(listConc) ;

				// remapper en 96
				String pos96=remapPosition (key, sector, contextValidation );
				results.put(pos96,moyConc_nM );
				
				//Logger.info ("FIN DE BLOCK...pos384="+key+" > pos96="+ pos96+"| MOY CONC="+moyConc_nM);
				//Logger.info (pos96 + " belong to sector "+sector+" ?? "+ belongToSector96(contextValidation, pos96, sector));

				//reinitialiser le tableau 
				listConc= new double[nbRep];
				// reinitialiser le compteur de repetitions
				rep=0;
			}
		}

		// Verifier que tous les puits du secteur concerné par l'import ont tous une concentration
		//  ( verification minimale pour eviter une erreur de choix du fichier initial...)
		if (!contextValidation.hasErrors()) {
			final int sector_arg = sector;
			experiment.atomicTransfertMethods
				.stream()
				.map(atm -> atm.inputContainerUseds.get(0))
				.forEach(icu -> {
					String icupos=InputHelper.getCodePosition(icu.code);
					// 07/04 faut-il garder ce test d'appartenance au secteur si par la suite on ne fait entrer dans l'experience
					// que des containers choisis et pas tous comme actuellement ?
					if ( belongToSector96(contextValidation, icupos, sector_arg)) {
						if (!results.containsKey(icupos) ){
							contextValidation.addErrors("Erreurs fichier", "experiments.msg.import.concentration.missing",icupos);
						} 
					}
				});
		}
		
		// ne positionner les valeurs que s'il n'y a pas d'erreur a la verification precedente...
		if (!contextValidation.hasErrors()) {
			final int sector_arg = sector;
			experiment.atomicTransfertMethods
				.stream()
				.map(atm -> atm.inputContainerUseds.get(0))
				.forEach(icu -> {
					String icupos=InputHelper.getCodePosition(icu.code);
					// idem commentaire ci-dessus
					if ( belongToSector96(contextValidation, icupos, sector_arg)) {
							
						PropertySingleValue concentration = getPSV(icu, "concentration1");
						//Logger.debug ("set concentration for icu "+ icu.code+" ="+ concentration);
						
						// effectuer la correction en utilisant ce que l'utilisateur a defini
						PropertySingleValue cFLSize = getCorrectionFactorLibraySize(icu, correctionFactorLibrarySizeDefault);
						
						if (cFLSize.value != null) {
							// effectuer la correction
							//Logger.info ("cFLSize.value="+cFLSize.value);
							double corFactor = 452.0d / ((double) (Integer) cFLSize.value);
							concentration.value = results.get(icupos) * corFactor;
							concentration.unit = "nM";
							//Logger.info ("corFactor="+ corFactor+"; concentration="+ results.get(icupos) * corFactor);
						} else {
							concentration.value = null;
							concentration.unit = "nM";
						}
					}
				});
		}
		
		
		return experiment;
	}
	
	
	/* description de la tranformation effectué sur le robot:
	 * 
	 *  contient les mesures sur une plaque 384 puits d'une demi plaque 96 ( 6 premieres ou 6 dernieres colonnes)
	 *  il faut donc remaper les resutats trouves dans le fichier vers les puits concernes
	 *  1 parametre necessaire: de quelle demi plaque s'agit-il ? colonne 1-6 ou 7-12 ? 
	 *  
   	 *plaque 96 :
   	 *
   	 *   1.......6 7.......12
  	 *  +-------------------+
	 * A|         |         |
 	 * B|         |         |
 	 * ....
 	 * H|         |         |
  	 * +-------------------+
     *      ||       ||
     *      ||        ==>  sector 1 ( samples 49 a 96 )
     *       ==>  sector 0 ( samples 1 a 48)
     *
	 *
 	 * chaque plaque 384 est composé de 4 zones :
  	 *  3 zones pour les echantillons venant d'une demi plaque 96
  	 *  1 zone  pour les temoins
  	 *
  	 *   1...6.8...13.14...20.22..24
  	 * A|     |      |       |     |
  	 * ....
  	 * P|     |      |       |     |
  	 *  +--------------------------+
  	 *
  	 * les 2 premieres colonnes d'un secteur 96 sont distribuées en colonnes 1 a 6
  	 * les 2 colonnes centrales sont distribuées en colonnes 8 a 13 
  	 * les 2 dernieres colonnes sont distribuées en colonnes 14 a 20
  	 * les colonnes 22 a 24 contiennent des controles (pas des échantillons venant de la plaque 96)=> a ignorer !! 
  	 *
  	 * chaque puit de la plaque initiale 96 est déposé 6 fois sur la plaque 384:
  	 *    - 3 en dilution  1/5000 
  	 *    - 3 en dilution  1/50000 
  	 *    
  	 *    NOTE: pour l'instant au labo ne sont realisées que des demi plaques ( < 48 samples..)=> sector 0 uniqut
	 */
	public String remapPosition(String pos384, int sector, ContextValidation contextValidation) {
		int asciiRow96=0;
		int col96=0;
		
		//recuperer le code ASCII du premier caractere de la position
		int asciiRow384=(int)pos384.charAt(0);
		//recuper la colonne
		int col384 = Integer.parseInt(pos384.substring(1));
		
		//ascii A=65, ascii P=80
		if ( asciiRow384 < 65 || asciiRow384 > 80 ) { 
			contextValidation.addErrors("Erreurs fichier", "experiments.msg.import.illegalRowPosition",pos384); 
			return "ERR-ROW384";
		}
		else
		{
			// transformer le code ascii du row384 en rank (A=1 .... P=16)
			// ne garder que la valeur entiere de la division par 2
			int map=  (asciiRow384 - 65 )/2 ;

			// determiner si c'est une ligne paire ou impaire vi l'operateur modulo 2... 
			if ( (asciiRow384 % 2) == 1 ){ 
				//pair		
				asciiRow96=asciiRow384 -map;
				if      ( col384 > 0  && col384 < 7)  { col96= 1 +(sector*6); }
			    else if ( col384 > 7  && col384 < 14) { col96= 3 +(sector*6); }
			    else if ( col384 > 14 && col384 < 21) { col96= 5 +(sector*6); }
			    else { 
			    	contextValidation.addErrors("Erreurs fichier", "experiments.msg.import.illegalColumnPosition",pos384);
			    	return "ERR-COL384";
			    }
			} else  {  
				//impair
				asciiRow96=asciiRow384 -map -1 ;
				if      ( col384 > 0  && col384 < 7)  { col96= 2 +(sector*6); }
			    else if ( col384 > 7  && col384 < 14) { col96= 4 +(sector*6); }
			    else if ( col384 > 14 && col384 < 21) { col96= 6 +(sector*6); }
			    else { 
			    	contextValidation.addErrors("Erreurs fichier", "experiments.msg.import.illegalColumnPosition",pos384);
			    	return "ERR-COL384";
			    }
			}
		}

		//retransformer asciiRow96  et col96 en string
		String pos96=Character.toString((char)asciiRow96) + col96;
		
		return pos96;		
	}
	
	/*Calcul de moyenne; source: https://openclassrooms.com
	public static double mean(double[] m) {
	    double sum = 0;
	    for (int i = 0; i < m.length; i++) {
	        sum += m[i];
	    }
	    return sum / m.length;
	}*/
	// modification 17/05/2016: ne pas compter les 0 dans la moyenne
	public static double meanNo0(double[] m) {
	
		 double sum = 0;
		 int nb=0;
		 for (int i = 0; i < m.length; i++) {
		 if ( m[i] > 0 ){ sum += m[i];  nb++; }
		 }
		 
		 //attention cas ou m[] est vide => /0 !!
		 if (nb > 0 ) { return sum / nb ;} else { return 0; }
	}
	
	//specifique...ne pas mettre dans InputHelper
	public static Boolean belongToSector96( ContextValidation contextValidation, String pos96, int sector) {
		// verifier si valide ??  on n'a pas de numero de ligne pour le message...
		if ( ! InputHelper.isPlatePosition(contextValidation, pos96, 96, 0)){
			return false;
		}
		
		// les secteurs sont uniquement defini par les colonnes		
		int col96 = Integer.parseInt(pos96.substring(1));
		
		if ( col96 > 0 && col96 < 7   && sector == 0 ){ return true; }
		if ( col96 > 6 && col96 < 13  && sector == 1 ){ return true; }
		
		return false;
	}
	
	private PropertySingleValue getCorrectionFactorLibraySize(
			InputContainerUsed icu, PropertyDefinition correctionFactorLibrarySizeDefault) {
		
		PropertySingleValue cFLSize =  getPSV(icu, "correctionFactorLibrarySize");
		if (cFLSize.value == null){ //get defaultValue
			cFLSize.value = Integer.valueOf(correctionFactorLibrarySizeDefault.defaultValue);
		}
		return cFLSize;
	}

}
