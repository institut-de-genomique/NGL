package services.sra;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
//import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.sybase.jdbc4.jdbc.SybSQLException;

import models.sra.submit.sra.instance.Experiment;
import models.sra.submit.sra.instance.ReadSpec;
import play.Logger;
import models.sra.submit.sra.instance.Attribute;

import validation.ContextValidation;
import validation.utils.ValidationHelper;

// CTX: check ctx uses
public class SraValidationHelper {
	private static final play.Logger.ALogger logger = play.Logger.of(SraValidationHelper.class);
	private static final String projectAttributeType ="PROJECT_ATTRIBUTE";
	private static final String projectPatternTagValue = "^\\s*<PROJECT_ATTRIBUTE>\\s*<TAG>\\s*([^<>]+)\\s*</TAG>\\s*<VALUE>\\s*([^<>]+)\\s*</VALUE>\\s*</PROJECT_ATTRIBUTE>(.*)";
	private static final String projectPatternTagValueUnits = "^\\s*<PROJECT_ATTRIBUTE>\\s*<TAG>\\s*([^<>]+)\\s*</TAG>\\s*<VALUE>\\s*([^<>]+)\\s*</VALUE>\\s*<UNITS>\\s*([^<>]+)\\s*</UNITS>\\s*</PROJECT_ATTRIBUTE>(.*)";
	private static final String sampleAttributeType ="SAMPLE_ATTRIBUTE";
	private static final String samplePatternTagValue = "^\\s*<SAMPLE_ATTRIBUTE>\\s*<TAG>\\s*([^<>]+)\\s*</TAG>\\s*<VALUE>\\s*([^<>]+)\\s*</VALUE>\\s*</SAMPLE_ATTRIBUTE>(.*)";
	private static final String samplePatternTagValueUnits = "^\\s*<SAMPLE_ATTRIBUTE>\\s*<TAG>\\s*([^<>]+)\\s*</TAG>\\s*<VALUE>\\s*([^<>]+)\\s*</VALUE>\\s*<UNITS>\\s*([^<>]+)\\s*</UNITS>\\s*</SAMPLE_ATTRIBUTE>(.*)";

	public static boolean requiredAndConstraint(ContextValidation contextValidation, String value, Map<String, String> mapValues, String nameField) {
		if (ValidationHelper.validateNotEmpty(contextValidation, value, nameField)) {
			if (mapValues.containsKey(value.toLowerCase())) {
				return true;
			} else {
				contextValidation.addError(nameField + " avec valeur '" + value + "' qui n'appartient pas a la liste des valeurs autorisees :" , mapValues.keySet().toString());
				return false;
			}
		} else {
			// contextValidation mis à jour par required.
			return false;
		}
	}
	public static boolean noRequiredButConstraint(ContextValidation contextValidation, String value, Map<String, String> mapValues, String nameField) {

		if (value != null) {
			if (mapValues.containsKey(value.toLowerCase())) {
				return true;
			} else {
				contextValidation.addError(nameField + " avec valeur '" + value + "' qui n'appartient pas a la liste des valeurs autorisees :" , mapValues.keySet().toString());
				return false;
			}
		} else {
			// contextValidation mis à jour par required.
			return true;
		}
	}
	
	public static boolean noRequiredInt(ContextValidation contextValidation, String value, String nameField) {
		if (value != null) {
			if(! value.matches("^\\d+$")) {
				contextValidation.addError(nameField + " avec valeur '" + value + "' qui ne correspond pas à un entier attendu", null);
				return false;
			}
		}
		return true;
	}
	
	public static boolean validateSampleAttributes(ContextValidation contextValidation, String nameField, String value) {
		return validateAttributes(contextValidation, nameField, value, samplePatternTagValue, samplePatternTagValueUnits);
	}
	
	// NGL-4708
	public static boolean validateProjectAttributes(ContextValidation contextValidation, String nameField, String chaineAttributes) {
		return validateAttributes(contextValidation, nameField, chaineAttributes, projectPatternTagValue, projectPatternTagValueUnits);
	}	
	
	public static boolean validateAttributes(ContextValidation contextValidation, String nameField, String chaineAttributes, String patternTagValue, String patternTagValueUnits) {
		java.util.regex.Pattern pTV = Pattern.compile(patternTagValue);
		java.util.regex.Pattern pTVU = Pattern.compile(patternTagValueUnits);
		//logger.debug("Dans SraValidationHelper::nameField='" + nameField + "'");
		//logger.debug("Dans SraValidationHelper::value='" + value + "'");
		// Enlever les retours charriots avant de tester la chaine :
		boolean cond = true;
		String aTester = "";
		if(chaineAttributes == null) {
			return cond;
		}
		String RC = System.getProperty("line.separator"); 
//		logger.debug("Dans SraValidationHelper::RC='" + RC + "'");
		if (RC != null) {
			aTester = chaineAttributes.replaceAll(RC,"" );
		}
		aTester = aTester.replaceAll("\n","" ); // le line.separator ne suffit pas si insertion dans base d'un retour charriot
		aTester = aTester.replaceAll("\r","" );
		//logger.debug("aTester = '"+ aTester + "'");
			
		while (StringUtils.isNotBlank(aTester) && cond == true) {
			//logger.debug("recherche pattern");
			java.util.regex.Matcher mTV = pTV.matcher(aTester);
			// Appel de find obligatoire pour pouvoir récupérer $1 ...$n
			if ( ! mTV.find() ) {
				logger.debug("XXXX   La chaine '"+ aTester + "' ne matche pas avec le pattern TV : " +  patternTagValue );
				java.util.regex.Matcher mTVU = pTVU.matcher(aTester);
				if ( ! mTVU.find() ) {
					// autre ligne que tag value units.	
					logger.debug("XXXXX  La chaine '"+ aTester + "' ne matche pas avec le pattern TVU : "  + patternTagValueUnits );
					contextValidation.addError(nameField , "La chaine '"+ aTester + "' ne matche pas avec les patterns " + patternTagValue + " et " + patternTagValueUnits);
					cond = false;
				} else {
					// String tag = mTVU.group(1);
					// String val = mTVU.group(2);	
					// String units = mTVU.group(3);	
					//logger.debug("tag = "+ tag);
					//logger.debug("val = "+ val);
					//logger.debug("units = "+ units);
					//logger.debug(mTVU.group(4));	
					if(StringUtils.isNotBlank(mTVU.group(4))) {
						aTester = mTVU.group(4);
						//logger.debug("tag = " + tag +  " val=" + val + " units=" + units + "  aTester = '" + aTester + "'");
					} else {
						aTester = "";
					}
				}
			} else {
				// String tag = mTV.group(1);
				// String val = mTV.group(2);	
				//logger.debug("tag = "+ tag);
				//logger.debug(mTV.group(3));
				if(StringUtils.isNotBlank(mTV.group(3))) {
					aTester = mTV.group(3);
					//logger.debug("tag = " + tag +  " val="+ val + "  aTester = '" + aTester + "'");
				} else {
					aTester = "";
				}
			}
		}
		return cond;
	}	
	
	// Attention retourne null si attributes n'est pas au bon format sans declencher d'erreur
	public static List<Attribute> getProjectAttributes(String chaineAttributes, ContextValidation ctxVal) {
		logger.debug("xxxxxxxxxxxxxxxxxxxxxxxxxx                        chaineAttributes="+ chaineAttributes);
		
		if(! validateProjectAttributes(ctxVal, "attributes", chaineAttributes)) {
			return null;
		}
		return getAttributes(projectAttributeType, chaineAttributes, projectPatternTagValue, projectPatternTagValueUnits);
	}
	
	public static List<Attribute> getSampleAttributes(String chaineAttributes) {
		String user = "user";
		ContextValidation ctxVal = ContextValidation.createUndefinedContext(user);
		if(! validateSampleAttributes(ctxVal, "attributes", chaineAttributes)) {
			throw new SraValidationException(ctxVal);
		}
		return getAttributes(sampleAttributeType, chaineAttributes, samplePatternTagValue, samplePatternTagValueUnits);
	}
		
	
	public static List<Attribute> getAttributes(String attributeType, String chaineAttributes, String patternTagValue, String patternTagValueUnits) {
		java.util.regex.Pattern pTV = Pattern.compile(patternTagValue);
		java.util.regex.Pattern pTVU = Pattern.compile(patternTagValueUnits);
		//logger.debug("Dans SraValidationHelper::nameField='" + nameField + "'");
		//logger.debug("Dans SraValidationHelper::value='" + value + "'");
		// Enlever les retours charriots avant de tester la chaine :
		List<Attribute> attributes = new ArrayList<Attribute>();
		String aTester = "";
		if(chaineAttributes == null) {
			return attributes;
		}
		String RC = System.getProperty("line.separator"); 
//		logger.debug("Dans SraValidationHelper::RC='" + RC + "'");
		if (RC != null) {
			aTester = chaineAttributes.replaceAll(RC,"" );
		}
		aTester = aTester.replaceAll("\n","" ); // le line.separator ne suffit pas si insertion dans base d'un retour charriot
		aTester = aTester.replaceAll("\r","" );
		//logger.debug("aTester = '"+ aTester + "'");
			
		while (StringUtils.isNotBlank(aTester)) {
			//logger.debug("recherche pattern");
			java.util.regex.Matcher mTV = pTV.matcher(aTester);
			// Appel de find obligatoire pour pouvoir récupérer $1 ...$n
			if ( ! mTV.find() ) {
				//logger.debug("La chaine '"+ aTester + "' ne matche pas avec le pattern TV : " +  patternTagValue );
				java.util.regex.Matcher mTVU = pTVU.matcher(aTester);
				if ( ! mTVU.find() ) {
					// autre ligne que tag value units.	
					//logger.debug("La chaine '"+ aTester + "' ne matche pas avec le pattern TVU : "  + patternTagValueUnits );
					throw new SraException("La chaine '"+ aTester + "' ne matche pas avec les patterns ", patternTagValue + " et " + patternTagValueUnits);
				} else {
					 String tag = mTVU.group(1);
					 String val = mTVU.group(2);	
					 String units = mTVU.group(3);	
					//logger.debug("tag = "+ tag);
					//logger.debug("val = "+ val);
					//logger.debug("units = "+ units);
					//logger.debug(mTVU.group(4));
					Attribute attribute = new Attribute(attributeType, tag, val, units);
					attributes.add(attribute);
					if(StringUtils.isNotBlank(mTVU.group(4))) {
						aTester = mTVU.group(4);
						//logger.debug("tag = " + tag +  " val=" + val + " units=" + units + "  aTester = '" + aTester + "'");
					} else {
						aTester = "";
					}
				}
			} else {
				 String tag = mTV.group(1);
				 String val = mTV.group(2);	
				//logger.debug("tag = "+ tag);
				//logger.debug(mTV.group(3));
				Attribute attribute = new Attribute(attributeType, tag, val);
				attributes.add(attribute);				 
				if(StringUtils.isNotBlank(mTV.group(3))) {
					aTester = mTV.group(3);
					//logger.debug("tag = " + tag +  " val="+ val + "  aTester = '" + aTester + "'");
				} else {
					aTester = "";
				}
			}
		}
		return attributes;
	}	
		
	/**
	 * Verifie que la chaine de caracteres qui definit les attributs repond bien à l'expression
	 * reguliere 
	 * {@literal "\\s*<SAMPLE_ATTRIBUTE>\\s*<TAG>\\s*([^<>\\s]+)\\s*</TAG>\\s*<VALUE>\\s*([^<>\\s]+)\\s*</VALUE>\\s*</SAMPLE_ATTRIBUTE>(.*)";}
	 * {@literal "\\s*<SAMPLE_ATTRIBUTE>\\s*<TAG>\\s*([^<>\\s]+)\\s*</TAG>\\s*<VALUE>\\s*([^<>\\s]+)\\s*</VALUE>\\s*<UNITS>\\s*([^<>\\s]+)\\s*</UNITS>\\s*</SAMPLE_ATTRIBUTE>(.*)";}
	 * @param contextValidation contextValidation
	 * @param nameField         nom du champs pour stocker les attributs du sample
	 * @param value             chaine de caracteres correspondant aux attributs
	 * @return                  true si value repond à la regExp, false sinon
	 */
	public static boolean validateSampleAttributesRequired(ContextValidation contextValidation, String nameField, String value) {
		String patternTagValue = "^\\s*<SAMPLE_ATTRIBUTE>\\s*<TAG>\\s*([^<>]+)\\s*</TAG>\\s*<VALUE>\\s*([^<>]+)\\s*</VALUE>\\s*</SAMPLE_ATTRIBUTE>(.*)";
		String patternTagValueUnits = "^\\s*<SAMPLE_ATTRIBUTE>\\s*<TAG>\\s*([^<>]+)\\s*</TAG>\\s*<VALUE>\\s*([^<>]+)\\s*</VALUE>\\s*<UNITS>\\s*([^<>]+)\\s*</UNITS>\\s*</SAMPLE_ATTRIBUTE>(.*)";
		java.util.regex.Pattern pTV = Pattern.compile(patternTagValue);
		java.util.regex.Pattern pTVU = Pattern.compile(patternTagValueUnits);
		//logger.debug("Dans SraValidationHelper::nameField='" + nameField + "'");
		//logger.debug("Dans SraValidationHelper::value='" + value + "'");
		// Enlever les retours charriots avant de tester la chaine :
		//logger.debug("zzzzzzzzzzzzzzzzzzzz                 Dans validateAttributesRequired");
		boolean cond = true;
		String aTester = "";
		if(value == null) {
			contextValidation.addError(nameField , "La chaine des attributs est vide. Elle devrait contenir les tags 'geographic location' et 'collection date' ");
			return false;
		}
		String RC = System.getProperty("line.separator"); 
//		logger.debug("Dans SraValidationHelper::RC='" + RC + "'");
		if (RC != null) {
			aTester = value.replaceAll(RC,"" );
		}
		aTester = aTester.replaceAll("\n","" ); // le line.separator ne suffit pas si insertion dans base d'un retour charriot
		aTester = aTester.replaceAll("\r","" );
		//logger.debug("aTester = '"+ aTester + "'");
		boolean checkLocalisation = false;
		boolean checkDate = false;
		while (StringUtils.isNotBlank(aTester) && cond == true) {
			//logger.debug("recherche pattern");
			java.util.regex.Matcher mTV = pTV.matcher(aTester);
			// Appel de find obligatoire pour pouvoir récupérer $1 ...$n
			if ( ! mTV.find() ) {
				//logger.debug("La chaine '"+ aTester + "' ne matche pas avec le pattern TV : " +  patternTagValue );
				java.util.regex.Matcher mTVU = pTVU.matcher(aTester);
				if ( ! mTVU.find() ) {
					// autre ligne que tag value units.	
					//logger.debug("La chaine '"+ aTester + "' ne matche pas avec le pattern TVU : "  + patternTagValueUnits );
					contextValidation.addError(nameField , "La chaine '"+ aTester + "' ne matche pas avec les patterns ", patternTagValue + " et " + patternTagValueUnits);
					cond = false;
				} else {
					// String tag = mTVU.group(1);
					// String val = mTVU.group(2);	
					// String units = mTVU.group(3);	
					//logger.debug("tag = "+ tag);
					//logger.debug("val = "+ val);
					//logger.debug("units = "+ units);
					//logger.debug(mTVU.group(4));	
					if(StringUtils.isNotBlank(mTVU.group(4))) {
						aTester = mTVU.group(4);
						//logger.debug("tag = " + tag +  " val=" + val + " units=" + units + "  aTester = '" + aTester + "'");
					} else {
						aTester = "";
					}
					
				}
			} else {
				String tag = mTV.group(1);
				// String val = mTV.group(2);	
				//logger.debug("tag = "+ tag);
				//logger.debug(mTV.group(3));
				if(StringUtils.isNotBlank(mTV.group(3))) {
					aTester = mTV.group(3);
					//logger.debug("tag = " + tag +  " val="+ val + "  aTester = '" + aTester + "'");
				} else {
					aTester = "";
				}
				if(StringUtils.isNotBlank(tag)) {
					if (tag.contains("geographic location")) {
						checkLocalisation = true;
					}
					if (tag.contains("collection date")) {
						checkDate = true;
					}
				}
			}
		}
		
		if(!checkLocalisation) {
			contextValidation.addError(nameField , "La chaine des attributs ne contient pas le tag 'geographic location'");
			cond = false;
		}
		if(!checkDate) {
			contextValidation.addError(nameField , "La chaine des attributs ne contient pas le tag 'collection date'");
			cond = false;
		}
				
		return cond;
	}

	/**
	 * Validate that the text does not contains any of the forbidden
	 * characters (accentuated, {@literal '&'}).
	 * @param contextValidation validation context
	 * @param nameField         field name (error key)
	 * @param chaine            text to validate
	 */
	public static void validateFreeText(ContextValidation contextValidation, String nameField, String chaine) {
		String forbidden = "(à, &, </, />)";
		if (StringUtils.isNotBlank(chaine)) {
			if (chaine.contains("&") || chaine.contains("à") || chaine.contains("</") || chaine.contains("/>")) {
				contextValidation.addError(nameField, "valeur '" + chaine + "' qui contient des caractères non autorisés " + forbidden);
			}
		}
	}

	/**
	 * Validate that the text does not contains any of the forbidden
	 * characters (accentuated, {@literal '&'}).
	 * @param contextValidation validation context
	 * @param nameField         field name (error key)
	 * @param chaine            text to validate
	 */
	public static void NoBaliseFermante(ContextValidation contextValidation, String nameField, String chaine) {
		String forbidden = "</, />";
		if (StringUtils.isNotBlank(chaine)) {
			if (chaine.contains("</") || chaine.contains("/>")) {
				contextValidation.addError(nameField, "valeur '" + chaine + "' qui contient une balise HTML fermante " + forbidden);
			}
		}
	}	
	/**
	 * Validate that the text does not contains any of the forbidden
	 * characters (accentuated, {@literal '&'}).
	 * @param contextValidation validation context
	 * @param nameField         field name (error key)
	 * @param chaine            text to validate
	 */
	public static void validateFreeTextNoTags(ContextValidation contextValidation, String nameField, String chaine) {
		String forbidden = "(à, &, <, >)";
		if (StringUtils.isNotBlank(chaine)) {
			if (chaine.contains("&") || chaine.contains("à")||chaine.contains("<") || chaine.contains(">")) {
				contextValidation.addError(nameField, "valeur '" + chaine + "' qui contient des caractères non autorisés " + forbidden);
			}
		}
	}
	
	public static void validateReadSpecs(ContextValidation contextValidation, Experiment experiment){
		if ("ILLUMINA".equalsIgnoreCase(experiment.typePlatform)){
			validateReadSpecsILLUMINA(contextValidation, experiment);
		} else if ("LS454".equalsIgnoreCase(experiment.typePlatform)){
			validateReadSpecsLS454(contextValidation, experiment);
		}  else if ("OXFORD_NANOPORE".equalsIgnoreCase(experiment.typePlatform)){
			if (experiment.readSpecs.size() != 0){
				contextValidation.appendPath("readSpecsNanopore")
				                 .addError("",  "Plateforme OXFORD_NANOPORE incompatible avec presence de readspec ");
			}			
		}  else if ("PACBIO_SMRT".equalsIgnoreCase(experiment.typePlatform)){
			if (experiment.readSpecs.size() != 0){
				contextValidation.appendPath("readSpecsNanopore")
				                 .addError("",  "Plateforme PACBIO_SMRT incompatible avec presence de readspec ");
			}
		}  else if ("DNBSEQ".equalsIgnoreCase(experiment.typePlatform)){
			if (experiment.readSpecs.size() != 0){
				contextValidation.appendPath("readSpecsNanopore")
				                 .addError("",  "Plateforme DNBSEQ incompatible avec presence de readspec ");
			}
		} else {
			contextValidation.addError("",  "readSpecs impossibles à evaluer avec platform inconnue " + experiment.typePlatform);
		}
	}
 
	public static void validateReadSpecsILLUMINA(ContextValidation contextValidation, Experiment experiment){
		// Verifier les readSpec :
		//contextValidation.addKeyToRootKeyName("readSpecsIllumina::");
		contextValidation = contextValidation.appendPath("readSpecsIllumina");

		for (ReadSpec readSpec : experiment.readSpecs) {
			readSpec.validate(contextValidation);
		}
		ContextValidation _contextValidation = ContextValidation.createUndefinedContext(contextValidation.getUser());
		
		if (!SraValidationHelper.requiredAndConstraint(_contextValidation, experiment.libraryLayout, VariableSRA.mapLibraryLayout(), "libraryLayout")) {
			contextValidation.addError("",  "impossibles à evaluer sans libraryLayout valide");
			//contextValidation.removeKeyFromRootKeyName("readSpecsIllumina::");
			return;
		}

		if ("SINGLE".equalsIgnoreCase(experiment.libraryLayout) && "ILLUMINA".equalsIgnoreCase(experiment.typePlatform)) {
			if(experiment.readSpecs.size() != 1){
				contextValidation.addError("",  " nbre de readSpec " + experiment.readSpecs.size() + "' incompatible avec libraryLayout = SINGLE ");
				//contextValidation.removeKeyFromRootKeyName("readSpecsIllumina::");
				return;
			}
			ReadSpec readSpec1 = experiment.readSpecs.get(0);
			if ( ValidationHelper.validateNotEmpty(contextValidation, readSpec1.readIndex, "readIndex")
				&& (readSpec1.readIndex != 0)) {
				contextValidation.addError("readSpec1.readIndex :",  "readSpec1 avec mauvais readIndex");
			}
			if ( ValidationHelper.validateNotEmpty(contextValidation, readSpec1.readClass, "readClass")
				&& (!readSpec1.readClass.equalsIgnoreCase("Application Read"))) {
				contextValidation.addError("readSpec1.readClass :",  "readSpec1 avec mauvais readClass (" + readSpec1.readClass + ")");
			}
			if ( ValidationHelper.validateNotEmpty(contextValidation, readSpec1.readType, "readType")
				&& (!readSpec1.readType.equalsIgnoreCase("Forward"))) {
				contextValidation.addError("readSpec1.readType :",  "readSpec1 avec mauvais readType (" + readSpec1.readType + ")");
			}
			if ( ValidationHelper.validateNotEmpty(contextValidation, readSpec1.baseCoord, "baseCoord")
				&& (readSpec1.baseCoord != 1)) {
				contextValidation.addError("readSpec1.baseCoord :",  "readSpec1 avec mauvais baseCoord (" + readSpec1.baseCoord + "!=1)");
			}	
		} else if ("PAIRED".equalsIgnoreCase(experiment.libraryLayout) && "ILLUMINA".equalsIgnoreCase(experiment.typePlatform)) {
			if(experiment.readSpecs.size() != 2){
				contextValidation.addError("",  " nbre de readSpec " + experiment.readSpecs.size() + "' incompatible avec libraryLayout = PAIRED ");
			}
			if(!SraValidationHelper.requiredAndConstraint(_contextValidation, experiment.libraryLayoutOrientation, VariableSRA.mapLibraryLayoutOrientation(), "libraryLayoutOrientation")){
				contextValidation.addError("",  "impossible à evaluer sans libraryLayoutOrientation valide");
				//contextValidation.removeKeyFromRootKeyName("readSpecsIllumina::");
				return;
			}
			if (experiment.libraryLayoutOrientation.equalsIgnoreCase("forward-reverse")) {
				ReadSpec readSpec1 = experiment.readSpecs.get(0);
				if ( ValidationHelper.validateNotEmpty(contextValidation, readSpec1.readIndex, "readIndex")
					&& (readSpec1.readIndex != 0)) {
					contextValidation.addError("readSpec1.readIndex :",  "readSpec1 avec mauvais readIndex");
				}
				if ( ValidationHelper.validateNotEmpty(contextValidation, readSpec1.readLabel, "readLabel")
					&& (!readSpec1.readLabel.equalsIgnoreCase("F"))) {
					contextValidation.addError("readSpec1.readLabel :",  "readSpec1 avec mauvais readLabel (" + readSpec1.readLabel + ")");
				}
				if ( ValidationHelper.validateNotEmpty(contextValidation, readSpec1.readClass, "readClass")
					&& (!readSpec1.readClass.equalsIgnoreCase("Application Read"))) {
					contextValidation.addError("readSpec1.readClass :",  "readSpec1 avec mauvais readClass (" + readSpec1.readClass + ")");
				}
				if ( ValidationHelper.validateNotEmpty(contextValidation, readSpec1.readType, "readType")
					&& (!readSpec1.readType.equalsIgnoreCase("Forward"))) {
					contextValidation.addError("readSpec1.readType :",  "readSpec1 avec mauvais readType (" + readSpec1.readType + ")");
				}
				if ( ValidationHelper.validateNotEmpty(contextValidation, readSpec1.baseCoord, "baseCoord")
					&& (readSpec1.baseCoord != 1)) {
					contextValidation.addError("readSpec1.baseCoord :",  "readSpec1 avec mauvais baseCoord (" + readSpec1.baseCoord + ")");
				}		
				ReadSpec readSpec2 = experiment.readSpecs.get(1);
				if ( ValidationHelper.validateNotEmpty(contextValidation, readSpec2.readIndex, "readIndex")
					&& (readSpec2.readIndex != 1)) {
					contextValidation.addError("readSpec2.readIndex :",  "readSpec2 avec mauvais readIndex");
				}
				if ( ValidationHelper.validateNotEmpty(contextValidation, readSpec2.readLabel, "readLabel")
					&& (!readSpec2.readLabel.equalsIgnoreCase("R"))) {
					contextValidation.addError("readSpec2.readLabel :",  "readSpec2 avec mauvais readLabel (" + readSpec2.readLabel + ")");
				}
				if ( ValidationHelper.validateNotEmpty(contextValidation, readSpec2.readClass, "readClass")
					&& (!readSpec2.readClass.equalsIgnoreCase("Application Read"))) {
					contextValidation.addError("readSpec2.readClass :",  "readSpec2 avec mauvais readClass (" + readSpec1.readClass + ")");
				}
				if ( ValidationHelper.validateNotEmpty(contextValidation, readSpec2.readType, "readType")
					&& (!readSpec2.readType.equalsIgnoreCase("Reverse"))) {
					contextValidation.addError("readSpec2.readType :",  "readSpec2 avec mauvais readType (" + readSpec1.readType + ")");
				}
//				if ( ValidationHelper.validateNotEmpty(contextValidation, readSpec2.baseCoord, "baseCoord")
//					&& (readSpec2.baseCoord == 1)) {
//					contextValidation.addError("readSpec2.baseCoord :",  "readSpec2 avec mauvais baseCoord (" + readSpec1.baseCoord + ")");
//				}	
				// NGL-4630 : preciser champs dans SUB et dans BI
				if ( readSpec2.baseCoord == null) {
					contextValidation.addError("readSpec2.baseCoord (nbCycleRead1) :",  "valeur nulle alors que valeur attendue > 1");	
				} else if (readSpec2.baseCoord == 1){
					contextValidation.addError("readSpec2.baseCoord (nbCycleRead1) :",  "valeur qui devrait etre superieure à 1 (" + readSpec2.baseCoord +")");
				}	
			} else if (experiment.libraryLayoutOrientation.equalsIgnoreCase("reverse-forward")) {
				ReadSpec readSpec1 = experiment.readSpecs.get(0);
				if ( ValidationHelper.validateNotEmpty(contextValidation, readSpec1.readIndex, "readIndex")
					&& (readSpec1.readIndex != 0)) {
					contextValidation.addError("readSpec1.readIndex :",  "readSpec1 avec mauvais readIndex");
				}
				if ( ValidationHelper.validateNotEmpty(contextValidation, readSpec1.readLabel, "readLabel")
					&& (!readSpec1.readLabel.equalsIgnoreCase("R"))) {
					contextValidation.addError("readSpec1.readLabel :",  "readSpec1 avec mauvais readLabel (" + readSpec1.readLabel + ")");
				}
				if ( ValidationHelper.validateNotEmpty(contextValidation, readSpec1.readClass, "readClass")
					&& (!readSpec1.readClass.equalsIgnoreCase("Application Read"))) {
					contextValidation.addError("readSpec1.readClass :",  "readSpec1 avec mauvais readClass (" + readSpec1.readClass + ")");
				}
				if ( ValidationHelper.validateNotEmpty(contextValidation, readSpec1.readType, "readType")
					&& (!readSpec1.readType.equalsIgnoreCase("Reverse"))) {
					contextValidation.addError("readSpec1.readType :",  "readSpec1 avec mauvais readType (" + readSpec1.readType + ")");
				}
				if ( ValidationHelper.validateNotEmpty(contextValidation, readSpec1.baseCoord, "baseCoord")
					&& (readSpec1.baseCoord != 1)) {
					contextValidation.addError("readSpec1.baseCoord :",  "readSpec1 avec mauvais baseCoord (" + readSpec1.baseCoord + ")");
				}	
				ReadSpec readSpec2 = experiment.readSpecs.get(1);
				if ( ValidationHelper.validateNotEmpty(contextValidation, readSpec2.readIndex, "readIndex")
					&& (readSpec2.readIndex != 1)) {
					contextValidation.addError("readSpec2.readIndex :",  "readSpec avec mauvais readIndex");
				}
				if ( ValidationHelper.validateNotEmpty(contextValidation, readSpec2.readLabel, "readLabel")
					&& (!readSpec2.readLabel.equalsIgnoreCase("F"))) {
					contextValidation.addError("readSpec2.readLabel :",  "readSpec avec mauvais readLabel (" + readSpec2.readLabel + ")");
				}
				if ( ValidationHelper.validateNotEmpty(contextValidation, readSpec2.readClass, "readClass")
					&& (!readSpec2.readClass.equalsIgnoreCase("Application Read"))) {
					contextValidation.addError("readSpec2.readClass :",  "readSpec avec mauvais readClass (" + readSpec1.readClass + ")");					}
				if ( ValidationHelper.validateNotEmpty(contextValidation, readSpec2.readType, "readType")
					&& (!readSpec2.readType.equalsIgnoreCase("forward"))) {
					contextValidation.addError("readSpec2.readType :",  "readSpec avec mauvais readType (" + readSpec1.readType + ")");
				}
//				if ( ValidationHelper.validateNotEmpty(contextValidation, readSpec2.baseCoord, "baseCoord")
//					&& (readSpec2.baseCoord == 1)) {
//					contextValidation.addError("readSpec2.baseCoord :",  "readSpec avec mauvais baseCoord (" + readSpec1.baseCoord + ")");
//				}	
				// NGL-4630 : preciser champs dans SUB et dans BI
				if ( readSpec2.baseCoord == null) {
					contextValidation.addError("readSpec2.baseCoord (nbCycleRead1) :",  "valeur nulle alors que valeur attendue > 1");	
				} else if (readSpec2.baseCoord == 1){
					contextValidation.addError("readSpec2.baseCoord (nbCycleRead1) :",  "valeur qui devrait etre superieure à 1 (" + readSpec2.baseCoord + ")");
				}	
			} else {
				contextValidation.addError("",  "impossible à evaluer avec libraryLayoutOrientation != de 'forward-reverse' ou 'reverse-forward' ");
			}
		}
		//contextValidation.removeKeyFromRootKeyName("readSpecsIllumina::");
	}




public static void validateReadSpecsLS454(ContextValidation contextValidation, Experiment experiment){

	// Verifier les readSpec :
	//contextValidation.addKeyToRootKeyName("readSpecsLS454::");
	contextValidation = contextValidation.appendPath("readSpecsLS454");

	for (ReadSpec readSpec : experiment.readSpecs) {
		readSpec.validate(contextValidation);
	}
	ContextValidation _contextValidation = ContextValidation.createUndefinedContext(contextValidation.getUser());
	
	if(!SraValidationHelper.requiredAndConstraint(_contextValidation, experiment.libraryLayout, VariableSRA.mapLibraryLayout(), "libraryLayout")){
		contextValidation.addError("",  "impossibles à evaluer sans libraryLayout valide");
		//contextValidation.removeKeyFromRootKeyName("readSpecsLS454::");
		return;
	}

	if ("SINGLE".equalsIgnoreCase(experiment.libraryLayout) && "LS454".equalsIgnoreCase(experiment.typePlatform)) {
		if(experiment.readSpecs.size() != 2){
			contextValidation.addError("",  " nbre de readSpec " + experiment.readSpecs.size() + "' incompatible avec libraryLayout = SINGLE ");
//			contextValidation.removeKeyFromRootKeyName("readSpecsLS454::");
			return;
		}
		ReadSpec readSpec1 = experiment.readSpecs.get(0);
		if ( ValidationHelper.validateNotEmpty(contextValidation, readSpec1.readIndex, "readIndex")
			&& (readSpec1.readIndex != 0)) {
			contextValidation.addError("readSpec1.readIndex :",  "readSpec1 avec mauvais readIndex(" + readSpec1.readIndex + ")");
		}
		if ( ValidationHelper.validateNotEmpty(contextValidation, readSpec1.readClass, "readClass")
			&& (! "Technical Read".equalsIgnoreCase(readSpec1.readClass))) {
			contextValidation.addError("readSpec1.readClass :",  "readSpec1 avec mauvais readClass (" + readSpec1.readClass + ")");
		}
		if ( ValidationHelper.validateNotEmpty(contextValidation, readSpec1.readType, "readType")
			&& (!"Adapter".equalsIgnoreCase(readSpec1.readType))) {
			contextValidation.addError("readSpec1.readType :",  "readSpec1 avec mauvais readType (" + readSpec1.readType + ")");
		}
		if ( ValidationHelper.validateNotEmpty(contextValidation, readSpec1.baseCoord, "baseCoord")
			&& (readSpec1.baseCoord != 1)) {
			contextValidation.addError("readSpec1.baseCoord :",  "readSpec1 avec mauvais baseCoord (" + readSpec1.baseCoord + ")");
		}	
		ReadSpec readSpec2 = experiment.readSpecs.get(1);
		if ( ValidationHelper.validateNotEmpty(contextValidation, readSpec2.readIndex, "readIndex")
			&& (readSpec2.readIndex != 0)) {
			contextValidation.addError("readSpec2.readIndex :",  "readSpec2 avec mauvais readIndex(" + readSpec2.readIndex + ")");
		}
		if ( ValidationHelper.validateNotEmpty(contextValidation, readSpec2.readClass, "readClass")
			&& (! "Application Read".equalsIgnoreCase(readSpec2.readClass))) {
			contextValidation.addError("readSpec2.readClass :",  "readSpec2 avec mauvais readClass (" + readSpec2.readClass + ")");
		}
		if ( ValidationHelper.validateNotEmpty(contextValidation, readSpec2.readType, "readType")
			&& (!"Forward".equalsIgnoreCase(readSpec2.readType))) {
			contextValidation.addError("readSpec2.readType :",  "readSpec2 avec mauvais readType (" + readSpec2.readType + ")");
		}
		if ( ValidationHelper.validateNotEmpty(contextValidation, readSpec2.baseCoord, "baseCoord")
			&& (readSpec2.baseCoord != 5)) {
			contextValidation.addError("readSpec2.baseCoord :",  "readSpec2 avec mauvais baseCoord (" + readSpec2.baseCoord + ")");
		}			
	} else if ("PAIRED".equalsIgnoreCase(experiment.libraryLayout) && "LS454".equalsIgnoreCase(experiment.typePlatform)) {
		if(experiment.readSpecs.size() != 4){
			contextValidation.addError("",  " nbre de readSpec " + experiment.readSpecs.size() + "' incompatible avec libraryLayout = PAIRED ");
		}
		ReadSpec readSpec1 = experiment.readSpecs.get(0);
		if ( ValidationHelper.validateNotEmpty(contextValidation, readSpec1.readIndex, "readIndex")
			&& (readSpec1.readIndex != 0)) {
			contextValidation.addError("readSpec1.readIndex :",  "readSpec1 avec mauvais readIndex(" + readSpec1.readIndex + ")");
		}
			
		if ( ValidationHelper.validateNotEmpty(contextValidation, readSpec1.readClass, "readClass")
			&& (! "Technical Read".equalsIgnoreCase(readSpec1.readClass))) {
			contextValidation.addError("readSpec1.readClass :",  "readSpec1 avec mauvais readClass (" + readSpec1.readClass + ")");
		}
		if ( ValidationHelper.validateNotEmpty(contextValidation, readSpec1.readType, "readType")
			&& (!"Adapter".equalsIgnoreCase(readSpec1.readType))) {
			contextValidation.addError("readSpec1.readType :",  "readSpec1 avec mauvais readType (" + readSpec1.readType + ")");
		}
		if ( ValidationHelper.validateNotEmpty(contextValidation, readSpec1.baseCoord, "baseCoord")
			&& (readSpec1.baseCoord != 1)) {
			contextValidation.addError("readSpec1.baseCoord :",  "readSpec1 avec mauvais baseCoord (" + readSpec1.baseCoord + ")");
		}	
			
		ReadSpec readSpec2 = experiment.readSpecs.get(1);
		if ( ValidationHelper.validateNotEmpty(contextValidation, readSpec2.readIndex, "readIndex")
			&& (readSpec2.readIndex != 1)) {
			contextValidation.addError("readSpec2.readIndex :",  "readSpec2 avec mauvais readIndex(" + readSpec2.readIndex + ")");
		}
			
		if ( ValidationHelper.validateNotEmpty(contextValidation, readSpec2.readClass, "readClass")
			&& (!readSpec2.readClass.equalsIgnoreCase("Application Read"))) {
			contextValidation.addError("readSpec2.readClass :",  "readSpec2 avec mauvais readClass (" + readSpec2.readClass + ")");
		}
		if ( ValidationHelper.validateNotEmpty(contextValidation, readSpec2.readType, "readType")
			&& (!readSpec2.readType.equalsIgnoreCase("Forward"))) {
			contextValidation.addError("readSpec2.readType :",  "readSpec2 avec mauvais readType (" + readSpec2.readType + ")");
		}
		if ( ValidationHelper.validateNotEmpty(contextValidation, readSpec2.baseCoord, "baseCoord")
			&& (readSpec2.baseCoord != 5)) {
			contextValidation.addError("readSpec2.baseCoord :",  "readSpec2 avec mauvais baseCoord (" + readSpec2.baseCoord + ")");
		}	
		
		ReadSpec readSpec3 = experiment.readSpecs.get(2);
		if ( ValidationHelper.validateNotEmpty(contextValidation, readSpec3.readIndex, "readIndex")
			&& (readSpec3.readIndex != 2)) {
			contextValidation.addError("readSpec3.readIndex :",  "readSpec3 avec mauvais readIndex(" + readSpec3.readIndex + ")");
		}
			
		if ( ValidationHelper.validateNotEmpty(contextValidation, readSpec3.readClass, "readClass")
			&& (!readSpec3.readClass.equalsIgnoreCase("Technical Read"))) {
			contextValidation.addError("readSpec3.readClass :",  "readSpec3 avec mauvais readClass (" + readSpec3.readClass + ")");
		}
		if ( ValidationHelper.validateNotEmpty(contextValidation, readSpec3.readType, "readType")
			&& (!readSpec3.readType.equalsIgnoreCase("Linker"))) {
			contextValidation.addError("readSpec3.readType :",  "readSpec3 avec mauvais readType (" + readSpec3.readType + ")");
		}
		// verifier BASECALL
		if ( ValidationHelper.validateNotEmpty(contextValidation, readSpec3.expectedBaseCallTable.get(0), "baseCall_1")
				&& (!readSpec3.expectedBaseCallTable.get(0).equalsIgnoreCase("TCGTATAACTTCGTATAATGTATGCTATACGAAGTTATTACG"))) {		
			contextValidation.addError("readSpec3.expectedBaseCallTable[0] :",  "readSpec3 avec mauvais baseCall_1 (" + readSpec3.expectedBaseCallTable.get(0) + ")");
		}
		if ( ValidationHelper.validateNotEmpty(contextValidation, readSpec3.expectedBaseCallTable.get(1), "baseCall_2")
				&& (!readSpec3.expectedBaseCallTable.get(1).equalsIgnoreCase("CGTAATAACTTCGTATAGCATACATTATACGAAGTTATACGA"))) {		
			contextValidation.addError("readSpec3.expectedBaseCallTable[1] :",  "readSpec3 avec mauvais baseCall_2 (" + readSpec3.expectedBaseCallTable.get(1) + ")");
		}
		
		ReadSpec readSpec4 = experiment.readSpecs.get(3);
		if ( ValidationHelper.validateNotEmpty(contextValidation, readSpec4.readIndex, "readIndex")
			&& (readSpec4.readIndex != 3)) {
			contextValidation.addError("readSpec4.readIndex :",  "readSpec4 avec mauvais readIndex (" + readSpec4.readIndex + ")");
		}		
		if ( ValidationHelper.validateNotEmpty(contextValidation, readSpec4.readClass, "readClass")
			&& (!readSpec4.readClass.equalsIgnoreCase("Application Read"))) {
			contextValidation.addError("readSpec4.readClass :",  "readSpec avec mauvais readClass (" + readSpec4.readClass + ")");					}
		if ( ValidationHelper.validateNotEmpty(contextValidation, readSpec4.readType, "readType")
			&& (!readSpec4.readType.equalsIgnoreCase("Forward"))) {
			contextValidation.addError("readSpec4.readType :",  "readSpec avec mauvais readType (" + readSpec4.readType + ")");
		}
		if ( ValidationHelper.validateNotEmpty(contextValidation, readSpec2.baseCoord, "baseCoord")
			&& (readSpec4.baseCoord != 47)) {
			contextValidation.addError("readSpec4.baseCoord :",  "readSpec avec mauvais baseCoord (" + readSpec4.baseCoord + ")");
		}
	} else {
			contextValidation.addError("",  "impossible à evaluer avec libraryLayoutOrientation != de 'forward-reverse' ou 'reverse-forward' ");
	}
//		contextValidation.removeKeyFromRootKeyName("readSpecsLS454::");
	}

}




