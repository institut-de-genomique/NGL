package models.util;

import java.util.List;
import play.Logger;

import models.laboratory.common.instance.TBoolean;

public class DataMappingGET {

	public static String getImportTypeCode(boolean tara, boolean adapter) {

		//Logger.debug("Adaptateur "+adapter);
		//Logger.debug("Tara "+tara);
		if(adapter){
			if(tara){
				return "tara-library";
			}
			else { return "library"; }
		}
		else if(tara){
			return "tara-default";
		}
		else {
			return "default-import";
		}
	}
	public static String getObjectTypeCodeFromLims (String eSIToulObjectTypeCode) {

		//Logger.debug("Adaptateur "+adapter);
		//Logger.debug("Tara "+tara);
		Logger.debug("getObjectTypeCodeFromLims : " + eSIToulObjectTypeCode);
		
		if(eSIToulObjectTypeCode.equals("PLAQUE_96")){
			return "96-well-plate";
		}
		else if(eSIToulObjectTypeCode.equals("PLAQUE_384")){
			return "384-well-plate";
		}
		else if(eSIToulObjectTypeCode.equals("TUBE") || eSIToulObjectTypeCode.equals("TUBERTL")){
			return "tube";
		}
		else if(eSIToulObjectTypeCode.equals("POSITION")){
			return "well";
		}
		else {
			return "unknown";
		}
	}
	public static String getCaracteristiqueTypeFromLims (int caracteristiqueTypeId) {

		//Logger.debug("Adaptateur "+adapter);
		//Logger.debug("Tara "+tara);
		if(caracteristiqueTypeId == 279){
			return "96-well-plate";
		}else{
			return "unknown";
		}
		
	}
	// return SampleType code from : ADNg,ARNm,ARNtotal,librairie,pool librairies,DNA,Amplicon,ReadyToLoad,RNA
	public static String getSampleTypeFromLims(String esitoul_type_echantillon) {
		if (esitoul_type_echantillon.equals("ADNg"))
			return "DNA";
		else if (esitoul_type_echantillon.equals("ADN"))
			return "DNA";
		else if (esitoul_type_echantillon.equals("ARNm"))
			return "RNA";
		else if (esitoul_type_echantillon.equals("ARNtotal"))
			return "RNA";
		else if (esitoul_type_echantillon.equals("ARN total"))
			return "RNA";
		else if (esitoul_type_echantillon.equals("DNA"))
			return "DNA";
		else if (esitoul_type_echantillon.equals("Amplicon"))
			return "amplicon";
		else if (esitoul_type_echantillon.equals("ReadyToLoad"))
			return "ReadyToLoad";
		else if (esitoul_type_echantillon.equals("RNA"))
			return "RNA";
		else {
			Logger.error("Erreur mapping Type echantillon : " + esitoul_type_echantillon);}
			
		//Logger.debug("Erreur mapping Type materiel ("+tadnco+")/Type projet ("+tprco+") et Sample Type");
		return esitoul_type_echantillon;
	}

	public static String getStateRunFromLims(TBoolean state) {

		if(state.equals(TBoolean.UNSET)){
			return "F-RG";
		}
		else return "F-V";
		
	}

	public static String getRunTypeCodeMapping(String insCategoryCode) {
		if(insCategoryCode.equals("GA I")){
			return "RGAIIx";
		} else if(insCategoryCode.equals("GA II")){
			return "RGAIIx";
		} else if(insCategoryCode.equals("GA IIx")){
			return "RGAIIx";
		} else if(insCategoryCode.equals("Hi2500")){
			return "RHS2500";
		} else if(insCategoryCode.equals("Hi2500 Fast")){
			return "RHS2500R";
		} else if(insCategoryCode.equals("HiSeq 2000")){
			return "RHS2000";
		} else if(insCategoryCode.equals("MiSeq")){
			return "RMISEQ";
		}
		return null;
	}


	//A revoir avec Julie validation bio et prod
	public static String getStateReadSetFromLims(String state,TBoolean validation) {
	//	Logger.debug("State :"+state+", Validation :"+validation);
		if(state.equals("A_traiter") || state.equals("Indefini")){
			return "IW-QC";
		} else if(state.equals("En_traitement")){
			return "IP-QC";
		} else if (state.equals("Traite") && validation.equals(TBoolean.UNSET)){
			return "IW-VQC";
		} else if (state.equals("Traite") && validation.equals(TBoolean.TRUE)){
			return "A";
		} else if (state.equals("Traite") && validation.equals(TBoolean.FALSE)){
			return "UA";
		} else if (state.equals("Non_traite") && validation.equals(TBoolean.TRUE)){
			return "A";
		} else if (state.equals("Non_traite") && validation.equals(TBoolean.FALSE)){
			return "UA";
		} else if(state.equals("Sans_sequence")){
			return "UA";
		}
		
		return null;
	}

	public static String getInstrumentTypeCodeMapping(String insCategoryCode) {
		if(insCategoryCode.equals("GA I")){
			return "GAIIx";
		} else if(insCategoryCode.equals("GA II")){
			return "GAIIx";
		} else if(insCategoryCode.equals("GA IIx")){
			return "GAIIx";
		} else if(insCategoryCode.equals("Hi2500")){
			return "HISEQ2500";
		} else if(insCategoryCode.equals("Hi2500 Fast")){
			return "HISEQ2500";
		} else if(insCategoryCode.equals("HiSeq 2000")){
			return "HISEQ2000";
		} else if(insCategoryCode.equals("MiSeq")){
			return "MISEQ";
		}
		return null;
	}

	public static String getTagCategory(String index){
		Logger.debug("Index : " + index);
		 if ("NoIndex".equals(index)){
			 return "NO-INDEX";
		 }
		 else if(index.indexOf("-") != -1){
			return "DUAL-INDEX";
		}
		 else{
			return "SINGLE-INDEX";
		}
	}

	
	public static List<String>  getReadSetResolutionsFromCompteRenduLims(Integer compteRenduLims){
		
	return null;
	 
	}
	
	
	public static String getStateFromStateTubeLims(int etatTubeLimsCode){
		
		if(etatTubeLimsCode==4){
			return "IW-P";
		}
		else if(etatTubeLimsCode==8){
			return "IS";
		}
		else {
			return "UA";
		}
	}
	
	public static String getStateFromStatePrepaflowcellLims(int etatPrepaLimsCode){
		if(etatPrepaLimsCode==0){ return "IU"; } 
		else if(etatPrepaLimsCode==2 ) { return "A"; }
		else if(etatPrepaLimsCode==3 || etatPrepaLimsCode==4 || etatPrepaLimsCode==5 || etatPrepaLimsCode==6) { return "UA"; }
		return null;
	}
	
	public static String getStateFromStateSolutionStock(int etatPrepaLimsCode){
		if(etatPrepaLimsCode==0 || etatPrepaLimsCode==1 ||  etatPrepaLimsCode==10   ){ return "IU"; } 
		// Attention un container ne peut pas etre à l'etat "A" s'il n'a pas de processus associé
		else if(etatPrepaLimsCode==2 || etatPrepaLimsCode==14 || etatPrepaLimsCode==9 ) { return "IW-P"; }
		else if(etatPrepaLimsCode==3 || etatPrepaLimsCode==4 ||  etatPrepaLimsCode==6 || etatPrepaLimsCode==7) { return "UA"; }
		else if(etatPrepaLimsCode==5 ) { return "IS";}
		return null;
	}

	public static String getState(String containerCategoryCode, int etatLims,String experimentTypeCode) {
		if(containerCategoryCode.equals("tube") && experimentTypeCode==null){
			return getStateFromStateTubeLims(etatLims);
		}else if(containerCategoryCode.equals("well") && experimentTypeCode==null){
			return getStateFromStateTubeLims(etatLims);
		}else if(containerCategoryCode.equals("lane")){
			return getStateFromStatePrepaflowcellLims(etatLims);
		}else if(experimentTypeCode.equals("solution-stock")){
			return getStateFromStateSolutionStock(etatLims);
		}else if(experimentTypeCode.equals("amplification")){
			return getStateFromStateAmplication(etatLims);
		}
		return null;
	}

	private static String getStateFromStateAmplication(int etatLimsCode) {
		if(etatLimsCode==2 || etatLimsCode==14 || etatLimsCode==9 ) { return "IW-P"; }
		else if(etatLimsCode==3 || etatLimsCode==4 ||  etatLimsCode==6 || etatLimsCode==7 ) { return "UA"; }
		else if(etatLimsCode==5 ) { return "IS";}
		else if(etatLimsCode==10) { return "N";}
		return null;
	}
}
