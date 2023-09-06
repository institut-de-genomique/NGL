package scripts.tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import fr.cea.ig.ngl.dao.ValuationCriteriaDAO;
import models.laboratory.valuation.instance.Expression;
import models.laboratory.valuation.instance.Property;
import models.laboratory.valuation.instance.ValuationCriteria;
import models.sra.submit.util.SraException;
import validation.ContextValidation;

/**
 * Outils de manipulation de la collection ngl_common.ValuationCriteria
 * 
 * @author sgas
 *
 */
public class ValuationCriteriaTools {
	private static final play.Logger.ALogger logger = play.Logger.of(ValuationCriteriaTools.class);
	private final ValuationCriteriaDAO   valuationCriteriaDAO;

	@Inject
	public ValuationCriteriaTools(ValuationCriteriaDAO   valuationCriteriaDAO) {
		this.valuationCriteriaDAO = valuationCriteriaDAO;
	}
	
	/**
	 * Ecrit par ordre alphabetique, dans le fichier de sortie, les ValuationCriteria de la table ngl_common.ValuationCriteria 
	 * avec ses properties et expressions associées (format plus compact que le json)
	 * 
	 * @param outputFile : fichier de sortie
	 * 
	 * <pre>{@code
	 * ex de fichier de sortie :
	 *  VC-RMISEQ-PE-301-v2.treatments.ngsrg.default.nbBase.value
     *		-  danger : pValue <= 13200000000
     * 		- success : pValue > 13200000000
	 *	VC-RMISEQ-PE-301-v2.treatments.ngsrg.default.percentClusterIlluminaFilter.value
     *		-  danger : pValue <= 80
     *		- success : pValue > 80
     * #--------------------------------------------
	 * # Bilan de la collection valuationCriteria : 
     * #--------------------------------------------
     * # Nombre de valuationCriteria :    51
     * # Nombre de properties        :   634
     * # Nombre expressions          :  1305
     *
     * }</pre>
	 * 
	 */	
	public void writeDbValuationCriteria(File outputFile, List<String> userCodes) {
		
		Map<String, String> mapUserCodes = new HashMap<>();
		for (String code: userCodes) {
			mapUserCodes.put(code,  code);
		}
		
		List<String> valuationCriteriaCodes = new ArrayList<String>();
		Iterable<ValuationCriteria> iterableValuationCriteria = valuationCriteriaDAO.all();
		Iterator<ValuationCriteria> iterator = iterableValuationCriteria.iterator();
		
		
		if(mapUserCodes.isEmpty()) {
			while(iterator.hasNext()) {
				ValuationCriteria valuationCriteria = iterator.next();
				if (! valuationCriteriaCodes.contains(valuationCriteria.code)) {
					valuationCriteriaCodes.add(valuationCriteria.code);
				}
			}
		} else {
			while(iterator.hasNext()) {
				ValuationCriteria valuationCriteria = iterator.next();
				if(mapUserCodes.containsKey(valuationCriteria.code)) {
					if (! valuationCriteriaCodes.contains(valuationCriteria.code)) {
						valuationCriteriaCodes.add(valuationCriteria.code);
					}
				}
			}
		}
		int countCodes = 0;
		int countProperties = 0;
		int countExpressions = 0;
		String chaineAllValuationCriteria = "";
		valuationCriteriaCodes.sort((a, b)->a.compareTo(b));
		for (String valuationCriteriaCode : valuationCriteriaCodes) {
			ValuationCriteria valuationCriteria = valuationCriteriaDAO.getObject(valuationCriteriaCode);
			countCodes++;
			String chaineCode = valuationCriteria.code;
			String chaineAllProperty = "";
			String chaineAllExpressionForProperty = "";
			
			valuationCriteria.properties.sort((a,b)-> a.name.compareTo(b.name));
			for (Property property : valuationCriteria.properties) {
				
				countProperties++;
				chaineAllExpressionForProperty = "";
				//property.expressions.sort((a,b)->a.rule.compareTo(b.rule));
				property.expressions.sort((a,b)->a.result.compareTo(b.result));
				for (Expression element : property.expressions) {
					chaineAllExpressionForProperty = chaineAllExpressionForProperty + "     - " + String.format("%7s : ", element.result) + element.rule + "\n";	
					countExpressions++;
				}
				chaineAllProperty += chaineCode + "." + property.name + "\n" + chaineAllExpressionForProperty;
			}
			if(StringUtils.isBlank(chaineAllProperty)) {
				chaineAllValuationCriteria += chaineCode + "\n";
			} else {
				chaineAllValuationCriteria += chaineAllProperty;
			}
		}
		writeChaineAllValuationCriteria(chaineAllValuationCriteria, outputFile, countCodes, countProperties, countExpressions);
	}
	
	
	private void writeChaineAllValuationCriteria(String chaineAllValuationCriteria, File ouputFile, int countCodes, int countProperties, int countExpressions) {
		try (BufferedWriter output_buffer = new BufferedWriter(new java.io.FileWriter(ouputFile))) { 

			output_buffer.write(String.format("%s",chaineAllValuationCriteria));
			output_buffer.write("\n\n\n");
			output_buffer.write("#----------------------------------------\n");
			output_buffer.write("# Bilan des valuationCriterias : \n");
			output_buffer.write("#----------------------------------------\n");
			output_buffer.write(String.format("# Nombre de valuationCriteria : %5d\n",countCodes));
			output_buffer.write(String.format("# Nombre de properties        : %5d\n",countProperties));
			output_buffer.write(String.format("# Nombre expressions          : %5d\n",countExpressions));
		} catch (IOException e) {
			e.printStackTrace();	
			throw new SraException("Probleme avec le fichier " + e.getMessage());
		}
	}

	// a utiliser pour debugage 
	public void writeUserValuationCriteria(List<ValuationCriteria> listValuationCriteria, File outputFile) {
		int countCodes = 0;
		int countProperties = 0;
		int countExpressions = 0;
		//Collections.sort(valuationCriteriaCodes);
		String chaineAllValuationCriteria = "";
		listValuationCriteria.sort((a, b)->a.code.compareTo(b.code));
		for (ValuationCriteria valuationCriteria : listValuationCriteria) {
			countCodes++;
			String chaineCode = valuationCriteria.code;
			String chaineAllProperty = "";
			String chaineAllExpressionForProperty = "";
			
			valuationCriteria.properties.sort((a,b)-> a.name.compareTo(b.name));
			for (Property property : valuationCriteria.properties) {
				countProperties++;
				chaineAllExpressionForProperty = "";
				property.expressions.sort((a,b)->a.result.compareTo(b.result));
				for (Expression element : property.expressions) {
					chaineAllExpressionForProperty = chaineAllExpressionForProperty + "     - " + String.format("%7s : ", element.result) + element.rule + "\n";	
					countExpressions++;
				}
				chaineAllProperty += chaineCode + "." + property.name + "\n" + chaineAllExpressionForProperty;
			}
			if(StringUtils.isBlank(chaineAllProperty)) {
				chaineAllValuationCriteria += chaineCode + "\n";
			} else {
				chaineAllValuationCriteria += chaineAllProperty;
			}
		}
		writeChaineAllValuationCriteria(chaineAllValuationCriteria, outputFile, countCodes, countProperties, countExpressions);
	}
	
	/**
	 * Parse le fichier d'entree et renvoie la liste des ValuationCriteria correspondants.
	 *  
	 * @param inputFile : fichier d'entree des ValuationCriteria au format suivant :
	 * <pre>{@code
	 * valuationCriteriaCode.Propertie.name
     *  - expression_1.result : expression_1.rule
     *  - expression_2.result : expression_2.rule
     *  
     *  Ex de fichier d'entree :
     *  VC-RMISEQ-PE-301-v2.treatments.ngsrg.default.nbBase.value
     *		-  danger : pValue <= 13200000000
     * 		- success : pValue > 13200000000
	 *	VC-RMISEQ-PE-301-v2.treatments.ngsrg.default.percentClusterIlluminaFilter.value
     *		-  danger : pValue <= 80
     *		- success : pValue > 80
     *	VC-Readset-ARC-ARD-v1.treatments.duplicatesRaw.pairs.estimateDuplicatedReadsPercent.value
     *		-  danger : (context.sampleOnContainer.percentage <= 25) ? pValue > 20 : false
     *		- success : (context.sampleOnContainer.percentage <= 25) ? pValue <= 20 : false
     *		- success : (context.sampleOnContainer.percentage > 25) ? pValue <= 20 : false
     *		- warning : (context.sampleOnContainer.percentage > 25) ? pValue > 20 : false
     * }</pre>
     * 
	 * @return           : List de ValuationCriteria
	 */	
	public List<ValuationCriteria> parseUserFileValuationCriteria(File inputFile) throws Exception {
		String ligne = "";
		String pattern_header = "^\\s*(VC-[A-Za-z0-9-_]+)\\.(.*)";
		Pattern ph = Pattern.compile(pattern_header);
		//String pattern_string_c = "([^#]*)#";
		String pattern_string_c = "^\\s*#.*";
		Pattern pc = Pattern.compile(pattern_string_c);
		String pattern_rule = "\\s*-\\s*(danger|info|warning|success)\\s*:\\s*(.*)";
		Pattern pr = Pattern.compile(pattern_rule);
		List<ValuationCriteria> listUserValuationCriteria = new ArrayList<ValuationCriteria>();
		
		ValuationCriteria userValuationCriteria = new ValuationCriteria();
		Property property = new Property();
		try (BufferedReader input_buffer = new BufferedReader(new FileReader(inputFile))) {
			while ((ligne = input_buffer.readLine()) != null) {					
				// ignorer ce qui suit le signe de commentaire
				Matcher mc = pc.matcher(ligne);
				if (mc.find()) {
					continue;
				}
				// ignorer lignes sans caracteres visibles
				if (ligne.matches("^\\s*$")){
					continue;
				}
				logger.debug("ligne=" + ligne);
				logger.debug("pattern_r=" + pattern_rule);
				Matcher mh = ph.matcher(ligne);
				Matcher mr = pr.matcher(ligne);

				if (mh.find()) {	
					String valuationCriteriaCode = mh.group(1);
					String propertyName = mh.group(2);
					logger.debug("valuationCriteriaCode=" + valuationCriteriaCode);
					logger.debug("propertyName=" + propertyName);
					if ( userValuationCriteria != null && 
						! valuationCriteriaCode.equals(userValuationCriteria.code)) {
						userValuationCriteria = new ValuationCriteria();
						userValuationCriteria.properties = new ArrayList<Property>();
						if (!listUserValuationCriteria.contains(userValuationCriteria)) {
							listUserValuationCriteria.add(userValuationCriteria);
						}
						userValuationCriteria.code = valuationCriteriaCode;
					}
					property = new Property();
					property.name = propertyName;
					//logger.debug("code dans valuationCriteria :" + userValuationCriteria.code);
					userValuationCriteria.properties.add(property);
					property.expressions = new ArrayList<Expression>();
				} else if( mr.find()) {
					String rule = mr.group(2);
					String result = mr.group(1);
					logger.debug("rule=" + rule);
					logger.debug("result=" + result);
					Expression expression = new Expression();
					expression.rule = rule;
					expression.result = result;
					property.expressions.add(expression);
				} else {
					throw new Exception("format non reconnu pour la ligne " + ligne);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();	
			throw new SraException("Probleme avec le fichier " + e.getMessage());
		}
		return listUserValuationCriteria;
	}

	/**
	 * Met à jour la collection ngl_common.ValuationCriteria en ajoutant ou mettant à jour les expressions données par 
	 * le fichier utilisateur des valuationCriteria.
	 * 
	 * @param listUserValuationCriteria : Liste des ValuationCriteria de l'utilisateur avec ses properties. 
	 *                                    Les properties utilisateurs vont s'ajouter à ngl_common.ValuationCriteria 
	 *                                    si elles n'existent pas dans la collection ou remplacer les properties existantes de la collection.
	 * @param user                      : nom utilisateur (pour mettre à jour traceInformation.modifyUser)                  
	 * @throws Exception                : declenche Exception si aucune authentification du user 
	 *                                    ou si ValuationCriteria non valides pour une mise à jour,
	 *                                    avant toute sauvegarde dans la base de données.
	 */
	public void updateOrAddExpressionInValuationCriteria(List<ValuationCriteria>listUserValuationCriteria, String user) throws Exception {
		String messError = "";
		List<ValuationCriteria> listVCforUpdate = new ArrayList<ValuationCriteria>();
		
		//String user = Authentication.getUser(); // authentification qui ne marche pas si on accede depuis script
		Calendar calendar = Calendar.getInstance();
		java.util.Date dateJour = calendar.getTime();

		boolean error = false;
		for (ValuationCriteria userValuationCriteria : listUserValuationCriteria) {
			ValuationCriteria dbValuationCriteria = valuationCriteriaDAO.getObject(userValuationCriteria.code);			
			if (dbValuationCriteria == null) {
				messError += "userValuationCriteria.code absent de la base de donnees";
				error = true;
				continue;
			} 
		}
		if (error) {
			throw new Exception(messError);
		}
		
		for (ValuationCriteria userValuationCriteria : listUserValuationCriteria) {
			Map<String, Property> mapPropertiesForOneValuationCriteria = new HashMap<>();
			ValuationCriteria dbValuationCriteria = valuationCriteriaDAO.getObject(userValuationCriteria.code);

			// construction de mapProperties à partir des donnees de la base :
			for (Property dbProperty : dbValuationCriteria.properties) {
				mapPropertiesForOneValuationCriteria.put(dbProperty.name, dbProperty);
			}

			// mise à jour de mapProperties à partir des données utilisateurs :
			for (Property userProperty : userValuationCriteria.properties) {
				if (! mapPropertiesForOneValuationCriteria.containsKey(userProperty.name)) {
					logger.debug("ajout nouvelle property " + userProperty.name);
					mapPropertiesForOneValuationCriteria.put(userProperty.name, userProperty);
				} else {
					logger.debug("modification de la property " + userProperty.name);
					mapPropertiesForOneValuationCriteria.remove(userProperty.name);
					mapPropertiesForOneValuationCriteria.put(userProperty.name, userProperty);
				}
			}
			// Mise à jour des dbValuationCriteria.property de la base a partir de mapProperties :
			dbValuationCriteria.properties = new ArrayList<Property>();
			dbValuationCriteria.traceInformation.modifyUser = user;
			dbValuationCriteria.traceInformation.modifyDate = dateJour;
			for (Entry<String, Property> entry : mapPropertiesForOneValuationCriteria.entrySet()) {
				//logger.debug(entry.getKey() + " " + entry.getValue());
				dbValuationCriteria.properties.add(entry.getValue());
			}

//			for (Property property : dbValuationCriteria.properties) {
//				logger.debug(dbValuationCriteria.code + "." + property.name);
//				property.expressions.sort((a,b)->a.rule.compareTo(b.rule));
//				for (Expression element : property.expressions) {
//					logger.debug( "     - " + String.format("%7s : ", element.result) + element.rule);	
//				}		
//			}
//			logger.debug(dbValuationCriteria.traceInformation.modifyUser);
//			logger.debug(dbValuationCriteria.traceInformation.modifyDate.toString());
			listVCforUpdate.add(dbValuationCriteria);
		}
		
		ContextValidation ctxVal = ContextValidation.createUpdateContext(user);
		
		boolean erreur = false;
		for (ValuationCriteria dbValuationCriteria : listVCforUpdate) {
			dbValuationCriteria.validate(ctxVal);
			if (ctxVal.hasErrors()) {
				erreur = true;
				ctxVal.displayErrors(logger); // a remplacer par ctxVal.displayErrors.(logger, "debug");
			}
		}	
		if (erreur) {
			throw new Exception("Tentative d'update d'une valuationCriteria non valide, voir log");
		}
		
		int countUpdate = 0;
		for (ValuationCriteria dbValuationCriteria : listVCforUpdate) {
			valuationCriteriaDAO.updateObject(dbValuationCriteria);
			countUpdate++;
		}
		logger.debug("Nombre de mises à jour dans la base ngl_common.ValuationCriteria = " + countUpdate);
	}
	
	

}

