// FDS 08/04/2020 NGL-2928 ajouter les imports fichiers "Quantification standard" et "Nb Cycle determination" à l'existant "MappingQPCR"
//                => retour a l'ancien nom  Qpcrlightcycler480iiAloneInput qui est plus générique
package controllers.instruments.io.cng.qpcrlightcycler480iiAlone;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

import controllers.instruments.io.utils.AbstractInput;
import controllers.instruments.io.utils.InputHelper;
import models.laboratory.common.instance.property.PropertyFileValue;
import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.experiment.instance.Experiment;
import models.laboratory.parameter.map.MapQPCR;
import models.utils.InstanceConstants;
import validation.ContextValidation;

import org.mongojack.DBQuery;
import fr.cea.ig.MongoDBDAO;


/**
 * @author fdsantos
 * Charge un fichier de resultats QPCR de Lightcycler, suivant plusieurs modes
 * experience qpcr quantification            :  mode="mapping"    ancien existant
 *                                           :  mode="standard"   28/04/2020 pas pour l'instant (désactivé dans le javascript)
 * experience qpcr Nb cycles determination   :  mode="nbCycle"    20/08/2020 spécifications définitives du fichier
 */

public class Qpcrlightcycler480iiAloneInput extends AbstractInput {
	
	@Override
	public Experiment importFile(Experiment experiment,PropertyFileValue pfv, ContextValidation contextValidation) throws Exception {	
	
		String mode =null;
		// Assertions des preconditions
		if (contextValidation.getObject("mode") == null) {
			// url mal formée
			contextValidation.addError("Erreur interne", "mode manquant.");
			return experiment;			
		} else {
			mode = contextValidation.getObject("mode").toString();
			logger.info("mode:{}", mode);
		}
		
		switch (mode) {
			/* Pas pour l'instant
			case "standard" :         processStandardQuantifFile (experiment, pfv, mode, contextValidation);
			
			return experiment; */
			case "nbCycle" :          processNbCycleSettingFile (experiment, pfv, mode, contextValidation);
				return experiment;
			case "mapping" :          processMappingFile (experiment, pfv, mode, contextValidation);
				return experiment;	
				
			default : contextValidation.addError("Erreur interne", "mode '" + mode + "' non supporté.");
				return experiment;
		}
	}
	
	/* 
      Description du fichier à traiter: TXT CSV pour mode="standard"
      28/04/2020 pas pour l'instant

	private	Experiment processStandardQuantifFile (Experiment experiment,PropertyFileValue pfv, ContextValidation contextValidation) throws Exception {
		...
	} 
	*/
	
	/* 
      Description du fichier à traiter pour mode="nbCycle"
      20/08/2020	Le fichier est le même que le mode "mapping": TXT TAB delimité mais les colonnes a traiter et le mapping 96<->384 est different
      passer le parametre mode à loadFile
      
	*/
	private	Experiment processNbCycleSettingFile (Experiment experiment, PropertyFileValue pfv, String mode, ContextValidation contextValidation) throws Exception {
		
		/* description de la tranformation effectuée (avec robot Bravo ou Tecan ou même à la main mais sans se tromper !!!!!)
		 * 
		 *  Le fichier a traiter contient les mesures faites sur une plaque 384 puits 
		 *  les puits on été transférés depuis une demi-plaque 96 (6 premieres colonnes)
		 *  il faut donc remaper les résutats trouves dans le fichier vers les puits concernés
		 *
		 * Il s'agit ici d'un mapping très simple, inutile d'utiliser un objet stocké en base:
		 * 
		 * *plaque 96 d'origine (celle pour laquelle NGL doir enregistrer les valeur de concentration):
	   	 *
	   	 *   1.......6 ........12
	  	 *  +-------------------+
		 * A|         |         |
	 	 * B|         |         |
	 	 * ....
	 	 * H|         |         |
	  	 *  +-------------------+
		 *  La demi-plaque utilisée est toujours A1-A6 
		 *  
		 *  les lignes sont transférées vers la plaque 384 en sautant une ligne sur deux
		 *   96 =====> 384
			A1-A6 => A1-A6
			B1-B6 => C1-C6
			C1-C6 => E1-E6
			D1-D6 => G1-G6
			E1-E6 => I1-I6
			F1-F6 => K1-K6
			G1-G6 => M1-M6
			H1-H6 => O1-O6
			
			donc les colonnes sont conservées a l'identique, seules les lignes subissent une transformation
			line96[A,B,C,D,E,F,G,H] <=> line384[A,C,E,G,I,K,M,O]
			
			NOTE: si ce mapping évolue (par exemple 1 plaque 96 complète au lieu d'une demi...)
			réutiliser le système de mapping dans la collection Parameter
			creer un typeCode "map-cycle-parameter" avec un seul tableau map96To384
		*/
		
		Map<String,String> mapLine =new HashMap<>(0);
		//          K    V
		//          96  384
		mapLine.put("A","A");
		mapLine.put("B","C");
		mapLine.put("C","E");
		mapLine.put("D","G");
		mapLine.put("E","I");
		mapLine.put("F","K");
		mapLine.put("G","M");
		mapLine.put("H","O");
		
		// pour stocker le fichier
		Map<String, Double> mapFile384ToCp= loadFile( pfv, mode, contextValidation);
		
		if (contextValidation.hasErrors()) { 
			return experiment;
		} else {
			// positionner les valeurs du fichier dans l'experience
			experiment.atomicTransfertMethods
			.stream()
			.map(atm -> atm.inputContainerUseds.get(0))
			.forEach(icu -> {
				PropertySingleValue optimalPcrCycleNb= getOrCreatePSV(icu, "optimalPcrCycleNb");
				String pos96=InputHelper.getIcuPosition(icu); 
				// dans les specs actuelles seul les puits du sector 0 (48 premiers échantillons) sont mappés
				if(belongToSector96(contextValidation, pos96, 0)){
					// récupérer la pos384 mappée
					String pos384=simpleMapping(pos96, mapLine);
					logger.info("pos96 "+ pos96 + " mapped in "+ pos384);
					if (mapFile384ToCp.containsKey(pos384)) {
						//logger.info("in mapFile384ToCp: key "+ pos384 + " get value=>"+ mapFile384ToCp.get(pos384) );
					
						optimalPcrCycleNb.value = mapFile384ToCp.get(pos384);
					} else {
						logger.info("in mapFile384ToCp: key "+ pos384 + " not found");
						// le fichier ne contient aucune valeur pour cette position => erreur de fichier ??? a voir si faut garder l'erreur ou pas...
						contextValidation.addError("Erreurs fichier", "experiments.msg.import.cp.missing", pos384);
					}
				} else {
					//afficher une erreur signalant que les puits ne sont pas actuellement mappés ??
					contextValidation.addError("Erreur","experiments.msg.import.well.notprocessed", pos96);
				}
			});
		}
		
		return experiment;
	} // end import
	
	
	// code existant transféré depuis MappingLightcyclerQPCR.java.off
	// 20/08/2020passer le parametre mode à loadFile
	private Experiment processMappingFile(Experiment experiment, PropertyFileValue pfv, String mode, ContextValidation contextValidation) throws Exception {

		/* description de la tranformation effectuée (avec robot Bravo ou Tecan ou même à la main mais sans se tromper !!!!!)
		 * 
		 *  Le fichier a traiter contient les mesures faites sur une plaque 384 puits 
		 *  les puits on été transférés depuis une demi-plaque 96 (6 premieres ou 6 dernieres colonnes)
		 *  il faut donc remaper les résutats trouves dans le fichier vers les puits concernés
		 *  1 parametre necessaire: de quelle demi plaque s'agit-il ? colonne 1-6 ou 7-12 ? 
		 *  
	   	 *plaque 96 d'origine (celle pour laquelle NGL doir enregistrer les valeur de concentration):
	   	 *
	   	 *   1.......6 7.......12
	  	 *  +-------------------+
		 * A|         |         |
	 	 * B|         |         |
	 	 * ....
	 	 * H|         |         |
	  	 *  +-------------------+
	     *      ||       ||
	     *      ||        ==>  sector 1 (samples 49 a 96)
	     *       ==>  sector 0 (samples 1 a 48)
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
	  	 *    - 3 en dilution  1/5000  (Prod)   1/10000  (Dev)
	  	 *    - 3 en dilution  1/50000 (Prod)   1/100000 (Dev)
	  	 *    
		 */
		
		
		String mappingMode =null; // 08/04/2020 renommer ce parametre car mode deja utilisé !!!
		String parameterCode=null;// 23/06/2022 utiliser les codes et pas les names!!!! car les names vont être utilisés pour IHM
		// Assertions des preconditions
		if (contextValidation.getObject("mappingMode") == null) {
			// url mal formée
			contextValidation.addError("Erreur interne", "experiments.msg.import.parameter.missing","mappingMode");
			return experiment;			
		} else {
			mappingMode = contextValidation.getObject("mappingMode").toString();
			//logger.info("mappingMode:{}", mappingMode);
		}
		
		// 30/03/2020 NGL-2897 il n'y a pas de Prod Tecan  c'est du dev !!! Renommer 
		//  !!! parameterCodes hardcodés !!!
		switch (mappingMode) {
			case "ProdBravo" : parameterCode="bravo_5_50";
					break;
			case "DevBravo"  : parameterCode="bravo_10_100";
					break;
			case "DevTecan" :  parameterCode="tecan_10_100";
					break;
			default          : contextValidation.addError("Erreur interne", "experiments.msg.import.parameter.invalidValue", mappingMode, "mappingMode" ); 
					return experiment;
		}
	
		MapQPCR mapQPCR= MongoDBDAO.findOne(InstanceConstants.PARAMETER_COLL_NAME, MapQPCR.class, DBQuery.is("typeCode", "map-qpcr-parameter").is("code", parameterCode));
		if ( null == mapQPCR ){
			contextValidation.addError("Erreur interne: map-qpcr-parameter ", "io.error.resultat.notexist", parameterCode); 
			return experiment;
		}
		
		// pour stocker le fichier
		// 20/08/2020 ajout parametre mode
		Map<String, Double> mapFile384ToConc= loadFile( pfv, mode, contextValidation);
		
		int sector = getSector(experiment, contextValidation);
		final MapQPCR mapQPCRFinal =  mapQPCR;
		
		if ( null == mapQPCRFinal.defaultSize ) {
			// le document map-qpcr-parameter est incorrect, ne contient pas de defaultSize !!!
			contextValidation.addError("Erreur interne (qpcr-map)", "io.error.resultat.notexist","taille de librairie par défaut");
		}
		//logger.info("default size :"+mapQPCRFinal.defaultSize);
		
		if (!contextValidation.hasErrors()) {
			experiment.atomicTransfertMethods
			.stream()
			.map(atm -> atm.inputContainerUseds.get(0))
			.forEach(icu -> {
				// FDS 11/02/2019 NGL-2399  puisqu'il est corrigé, utiliser InputHelper.getIcuPosition
				//String pos96 = icu.locationOnContainerSupport.line+""+icu.locationOnContainerSupport.column;
				String pos96 =InputHelper.getIcuPosition(icu);
				// verifier qd meme que les positions dans l'experience correspondent au secteur demandé
				if(belongToSector96(contextValidation, pos96, sector)){
					List<String> listPos384 = mapQPCRFinal.map96To384.get(pos96);
					Double sommeConc = new Double(0);
					int nb=0;
					
					Integer cflz= (Integer)getOrCreatePSV(icu, "correctionFactorLibrarySize").getValue();
					if ( null == cflz || 0 == cflz) {
						// prendre defaultSize.
						cflz=mapQPCRFinal.defaultSize;
					}
					//logger.info("cflz:"+cflz);
					
					for(String pos384 : listPos384){
						//logger.info(">>pos384:"+pos384);
						
						Double conc384 = mapFile384ToConc.get(pos384);
						// et si rien trouvé dans le fichier pour cette position ???
						if ( null == conc384) {
							contextValidation.addError("Erreurs fichier", "experiments.msg.import.concentration.missing", pos384);
							continue;
						}
						//logger.info(">>> conc384 in file:"+conc384);
						
						Integer dil384 = mapQPCRFinal.map384ToDil.get(pos384);
						// et si rien trouvé le qpcr-mapping pour cette position ??
						if ( null == dil384 ) {
							contextValidation.addError("Erreur interne (qpcr-map)","experiments.msg.import.dilution.missing", pos384);
							continue;
						}
						//logger.info(">>> dil384 in map:"+dil384);
						
						// !! tout caster en double pour avoir un calcul correct 
						// formule de calcul:
						//          concFinale =conc384*dil384/1000*(refFrag/cflz)
						Double concFinale = conc384*(double)dil384/1000d*((double)mapQPCRFinal.refFrag/(double)cflz);
						//logger.info(">>>calcul concFinal :"+conc384+"*"+ dil384+ "/ 1000 *("+ mapQPCRFinal.refFrag +"/"+cflz+") =" +concFinal);
						
						/* ne pas compter les puits de conc nulle dans la moyenne */
						if (concFinale > 0){
							sommeConc+=concFinale;
							nb++;
						}
					}
					// attention au cas nb=0 !!
					Double concMoyenne = (nb > 0 ) ? sommeConc/nb : 0d;
					//logger.info("nb="+ nb+";  concMoyenne="+concMoyenne);
					
					PropertySingleValue concentration = getOrCreatePSV(icu, "concentration1");
					concentration.value=concMoyenne;
					concentration.unit = "nM";
					
					// mettre aussi dans l'experience la taille de librairie !
					PropertySingleValue LibrarySize = getOrCreatePSV(icu, "correctionFactorLibrarySize");
					LibrarySize.value=cflz;
				} 
				/* else
				 *  si le puit 96 n'appartient pas au secteur demandé Il est simplement ignoré par l'algo...
				 *  volontaire: permet pour la meme plaque 96 d'importer successivement 2 fichiers en changeant juste le secteur
				 */
			});
		}
		
		return experiment;
	}
	
	/* methodes appellées par processMappingFile */
	
	private int getSector(Experiment experiment, ContextValidation contextValidation) {	
		if (! experiment.instrumentProperties.containsKey("sector96")) {
			//normalement propriété obligatoie de l'expérience... ne devait pas arriver !!
			contextValidation.addError("Erreur interne","experiments.msg.import.propertie.missing","sector96"); 
			return 3; // valeur inutilisée...
		}
		PropertySingleValue psv = (PropertySingleValue) experiment.instrumentProperties.get("sector96");
		switch (psv.value.toString()) {
		case  "1-48" : return 0;
		case "49-96" : return 1; 
		default:
			//c'est une liste de choix... ne devait pas arriver !!
			contextValidation.addError("Erreur interne","experiments.msg.import.propertie.invalidValue", psv.value.toString(), "sector96"); 
			return 2; // valeur inutilisée...
		}
	}
	
	private Boolean belongToSector96( ContextValidation contextValidation, String pos96, int sector) {
		// verifier si position 96 valide ??
		if ( ! InputHelper.isPlatePosition(contextValidation, pos96, 96, 0)){
			// isPlatePosition envoie elle meme une erreur dans ContextValidation
			return false;
		}
		
		// les secteurs sont uniquement defini par les colonnes		
		int col96 = Integer.parseInt(pos96.substring(1));
		
		if ( col96 > 0 && col96 < 7   && sector == 0 ){ return true; }
		if ( col96 > 6 && col96 < 13  && sector == 1 ){ return true; }
		
		// si le puit 96 n'appartient pas au secteur demandé
		// Il est simplement ignoré par l'algo
		// pas possible d'ajouter un warning ?   ContextValidation.addWarning n'existe pas !
		return false;
	}
	
	/* Description du fichier a traiter: TXT TAB délimité généré par LightCycler
	*
	*Experiment: 150929_KAPA-Lib-Quant_PCRFREE_P17  Selected Filter: SYBR Green I / HRM Dye (465-510)
	*Include	Color	Pos		Name		Cp		Concentration	Standard	Status
	*True		255		A1		Sample 1	9.66	4.71E0			0	
	*True		255		A2		Sample 2	9.86	4.12E0			0	
	*True		255		A3		Sample 3	10.01	3.73E0			0	
	*.....    	
	*	 
	*  !!!! si les operateurs cochent/decochent des puits dans le logiciel qui genere le fichier 
	*  l'ordre des puits n'est plus assuré...
	*  Pour les puits en erreur, il n'y a rien dans les colonnes "Cp" et "Concentration"
	*  
	*  20/08/2020 cettte methode est utilisable dans plusieurs modes=> ajouter le parametre mode
	*/
	private Map<String, Double> loadFile (PropertyFileValue pfv, String mode, ContextValidation contextValidation) {
		// hashMap  pour stocker les informations du fichier... ???si on stocke un entier en double PB???
		Map<String,Double> data =new HashMap<>(0);
		
		// charset detection (N. Wiart)
		byte[] ibuf = pfv.byteValue();
		String charset = "UTF-8"; //par defaut, convient aussi pour de l'ASCII pur
			
		// si le fichier commence par les 2 bytes ff/fe  alors le fichier est encodé en UTF-16 little endian
		if (ibuf.length >= 2 && (0xff & ibuf[0]) == 0xff && (ibuf[1] & 0xff) == 0xfe) {
			charset = "UTF-16LE";
		}
			
		InputStream is = new ByteArrayInputStream(ibuf);
		// Ce n'est pas un fichier MS-Excel mais un fichier TXT TAB delimité a lire ligne a ligne. 
		// utiliser un bufferReader...Merci Nicolas	!! 03/08/2017 ajout charset
			/* commentaire de Jean pour améliorer la fiabilité du tout.:
			 * Si on affirme qu'il y a 2 lignes d'entete dont seulement une utile, autant utiliser
			 * cela et ne pas autoriser un fichier vide. Utiliser la deuxieme ligne pour valider
			 * les entetes et eventuellement definir un mapping par nom de colonne
			 */

		try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, charset))) {
			int n = 0;
			String line;

			while ((line = reader.readLine()) != null) {
				// attention si le fichier vient d'une machine avec LOCALE = FR les décimaux utilisent la virgule!!!
				String[] cols = line.replace (",", ".").split("\t");

				// verifier la premiere ligne d'entete
				if ((n == 0) && ( ! line.matches("Experiment:(.*)") ) ){
					contextValidation.addError("Erreurs fichier","experiments.msg.import.header-label.missing","1","Experiment:");
					return data;
				}
				// commencer le traitement en sautant la 2eme ligne d'entete
				if (n > 1 ) {
					// description d'une ligne de données:
					//  0         1      2       3          4            5             6          7
					//Include	Color	Pos		Name		Cp		Concentration	Standard	Status
					// status est optionnel ????
					//Logger.info ("ligne "+n+": lg="+cols.length );
					if ( cols.length < 7 || cols.length > 8 ) {
						contextValidation.addError("Erreurs fichier", "experiments.msg.import.linefields.unexpected", n+1);
						continue;
					}
					
					String pos384 = cols[2];
					// verifier que c'est une position 384 valide ???
					if ( !InputHelper.isPlatePosition(contextValidation, pos384 , 384, n)) {
						// isPlatePosition emmet deja l'erreur.......contextValidation.addError("Erreurs fichier", "erreur line",pos384);
						return data;
					} else {
						// 20/08/2020 plusieurs modes posibles=> ajout switch
						switch (mode) {
							case "mapping" :
								// cas original !!
								/* N'inclure un puit pour le calcul de moyenne ulterieur que si
								   la colonne include est égale a 'True' ET la colonne color=255
								   sinon =>forcer a 0 ici (sera ainsi exclue du calcul plus tard)
								 */	  
								double concentration; // double en minuscule ???
								if (cols[0].equals("True") && cols[1].equals("255")) { 
									concentration=Double.parseDouble(cols[5]);   
									data.put(pos384, concentration);
								} else { 
									//logger.info("excluding well "+pos384);
									data.put(pos384, 0d);
								}
								break;
							case "nbCycle" :
								// cas ajouté 20/08/2020
								// exclure les positions au dela de la colonne 6
								int col384 = Integer.parseInt(pos384.substring(1));
								if (  (cols[0].equals("True") && cols[1].equals("255")) && col384 < 7 ) {
									// la map data est faite pour des Doubles... declarer nbc en double meme si en fait c'est un integer !!
									Double nbc=calculateNbCycle(cols[4]);
									data.put(pos384, nbc);
								}
								break;
							default : contextValidation.addError("Erreur interne", "mode '" + mode + "' non supporté.");
						}
					}
				}
				++n; 
			}
		}catch (IOException e) {
	    	 contextValidation.addError("Erreur interne", e.getMessage());
		}
		
		//	reader.close();
		return data;	
	}
	
	// 20/08/2020
	private Double calculateNbCycle (String cp) {
		// la valeur cp donnée par le LightCycler doit être arrondie à l'entier le plus proche, puis corrigée par une constante empirique...
		// si cette constante devient un jour dependante de facteurs de l'experience, il faudra qu'elle devienne une valeur fournie par l'utilisateur
		
		int cpCorrection=3; // valeur au 20/08/2020 !!!
		
		//convertir la string en Double puis arrondir à l'entier le plus proche et appliquer la correction
		 return (double) (Math.round(Double.parseDouble(cp) ) + cpCorrection);
	}
	
	// 20/08/2020
	private String simpleMapping(String icupos, Map<String,String> mapLine) {
		String line96=String.valueOf(icupos.charAt(0));
		// !! il peut y avoir 2 char pour la colonne(ex: 12 !!)
		String col96=icupos.substring(1);
		
		String line384=mapLine.get(line96);
		// pas de mapping pour les colonnes: col384=col96
		
		return line384+col96;
		
	}

}
