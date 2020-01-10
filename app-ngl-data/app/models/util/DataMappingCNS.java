package models.util;

import java.util.List;

import models.laboratory.common.instance.TBoolean;

public class DataMappingCNS {

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

	public static String getSampleTypeFromLims(String tadnco,String tprco) {

		if (tadnco.equals("15"))
			return "fosmid";
		else if (tadnco.equals("8"))
			return "plasmid";
		else if (tadnco.equals("2"))
			return "BAC";
		else if (tadnco.equals("1") && !tprco.equals("11"))
			return "gDNA";
		else if (tadnco.equals("1") && tprco.equals("11"))
			return "MeTa-DNA";
		else if (tadnco.equals("16"))
			return "gDNA";
		else if (tadnco.equals("19") || tadnco.equals("6") || tadnco.equals("14"))
			return "amplicon";
		else if (tadnco.equals("12"))
			return "cDNA";
		else if (tadnco.equals("11"))
			return "total-RNA";
		else if (tadnco.equals("18"))
			return "sRNA";
		else if (tadnco.equals("10"))
			return "mRNA";
		else if (tadnco.equals("17"))
			return "chIP";
		else if (tadnco.equals("20"))
			return "depletedRNA";
		else if (tadnco.equals("21"))
			return "aRNA";
		else if (tadnco.equals("22"))
			return "DNAplug";
		else if (tadnco.equals("9") )
			return "unknown";
		//Logger.debug("Erreur mapping Type materiel ("+tadnco+")/Type projet ("+tprco+") et Sample Type");
		return null;
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

	public static Object getTagCategory(String tagCategory) {
		if(tagCategory.equals("D")){
			return "DUAL-INDEX";
		}else if( tagCategory.equals("IND") || tagCategory.equals("A") || tagCategory.equals("C") || tagCategory.equals("T") || tagCategory.equals("G")){
			return "SINGLE-INDEX";
		}
		else if (tagCategory.equals("NUG")){
			return "MID";
		}
		return null;
	}

	
	public static List<String>  getReadSetResolutionsFromCompteRenduLims(Integer compteRenduLims){
		
	return null;
	/*	Conta 
		Conta:indeterminee
		Conta:manip
		Conta:mat ori        
		Indetermine       
		Qlte:duplicat>30    
		Qlte:repartition bases       
		Multiplexage:tags identiques                

		
		//Indetermine
		if(compteRenduLims==0){                                                                                                                       
			return null;
		}
		//Conta
		else if(compteRenduLims==1){                                                                                                                             
			
		}
		//Seqceur
		else if(compteRenduLims==2){                                                                                                                           
		 
		}
		//Manip
		else if(compteRenduLims==3){                                                                                                                             
		 
		}
		//Reactifs (Qlte)
		else if(compteRenduLims==4){                                                                                                                   
		 
		}
		//Qlte
		else if(compteRenduLims==5){                                                                                                                              
		 
		}
		//Lame
		else if(compteRenduLims==6){                                                                                                                              
		 
		}
		//Multiplexage
		else if(compteRenduLims==7){                                                                                                                      
		 
		}
		//Info
		else if(compteRenduLims==8){                                                                                                                              
		 
		}
		//mat ori
		else if(compteRenduLims==9){                                                                                                                           
		 
		}
		//manip
		else if(compteRenduLims==10){                                                                                                                            
		 
		}
		//indeterminee
		else if(compteRenduLims==11){                                                                                                                     
		 
		}
		//optique (camera)
		else if(compteRenduLims==12){                                                                                                                 
		 
		}
		//optique (laser)
		else if(compteRenduLims==13){                                                                                                                  
		 
		}
		//optique (focus lentille)
		else if(compteRenduLims==14){                                                                                                         
		 
		}
		//composant (bloc refrigerant)
		else if(compteRenduLims==15){                                                                                                     
		 
		}
		//composant (pelletier)
		else if(compteRenduLims==16){                                                                                                            
		 
		}
		//composant (probleme vide)
		else if(compteRenduLims==17){                                                                                                        
		 
		}
		//fluidique
		else if(compteRenduLims==18){                                                                                                                        
		 
		}
		//depot
		else if(compteRenduLims==19){                                                                                                                            
		 
		}
		//emPCR
		else if(compteRenduLims==20){                                                                                                                            
		 
		}
		//sequencage
		else if(compteRenduLims==21){                                                                                                                       
		 
		}
		//Q30
		else if(compteRenduLims==22){                                                                                                                              
		 
		}
		//intensite
		else if(compteRenduLims==23){                                                                                                                        
		 
		}
		//error rate
		else if(compteRenduLims==24){                                                                                                                       
		 
		}
		//phasing/prephasing
		else if(compteRenduLims==25){                                                                                                               
		 
		}
		//tags identiques
		else if(compteRenduLims==26){                                                                                                                  
		 
		}
		//logiciel
		else if(compteRenduLims==27){                                                                                                                         
		 
		}
		//PC
		else if(compteRenduLims==28){                                                                                                                               
		 
		}
		//espace disque insuffisant
		else if(compteRenduLims==29){                                                                                                        
		 
		}
		//bq
		else if(compteRenduLims==30){                                                                                                                               
		 
		}
		//Cbot
		else if(compteRenduLims==31){                                                                                                                             
		 
		}
		//lane bouchee
		else if(compteRenduLims==32){                                                                                                                     
		 
		}
		//PE module
		else if(compteRenduLims==33){                                                                                                                        
		 
		}
		//Lec index
		else if(compteRenduLims==34){                                                                                                                        
		 
		}
		//reactifs
		else if(compteRenduLims==35){                                                                                                                         
		 
		}
		//instruments
		else if(compteRenduLims==36){                                                                                                                      
		 
		}
		//indetermine
		else if(compteRenduLims==37){                                                                                                                      
		 
		}
		//non formation clusters sur flowcell
		else if(compteRenduLims==40){                                                                                              
		 
		}
		//repartition bases
		else if(compteRenduLims==41){                                                                                                                
		 
		}
		//duplicat>30
		else if(compteRenduLims==42){                                                                                                                      
		 
		}
		//Conta mat ori + duplicat>30
		else if(compteRenduLims==43){                                                                                                      
		 
		}
		//Conta mat ori + rep bases
		else if(compteRenduLims==44){                                                                                                        
		 
		}
		//Duplicat>30 + rep bases
		else if(compteRenduLims==45){                                                                                                          
		 
		}
		//Conta mat ori + duplicat>30 + rep bases
		else if(compteRenduLims==46){  
			
			
		}
		
    
    	 Run-abandonLane       | ReadSet | CNS  |
		 LIB-pbConstruction    | ReadSet | CNS  |
		 LIB-erreurDepot       | ReadSet | CNS  |
		 Qte-seqValInsuf       | ReadSet | CNS  |
		 Qte-seqUtileInsuf     | ReadSet | CNS  |
		 IND-pbDemultiplex     | ReadSet | CNS  |
		 IND-pbManip           | ReadSet | CNS  |
		 Qlte-Q30              | ReadSet | CNS  |
		 Qlte-repartitionBases | ReadSet | CNS  |
		 Qlte-adapterKmer      | ReadSet | CNS  |
		 Qlte-duplicat         | ReadSet | CNS  |
		 TAXO-contaIndeterm    | ReadSet | CNS  |
		 TAXO-contaManip       | ReadSet | CNS  |
		 TAXO-contaMatOri      | ReadSet | CNS  |
		 TAXO-nonConforme      | ReadSet | CNS  |
		 | TAXO-mitochondrie     | ReadSet | CNS  |
		 | TAXO-chloroplast      | ReadSet | CNS  |
		 | TAXO-virus            | ReadSet | CNS  |
		 | TAXO-bacteria         | ReadSet | CNS  |
		 | TAXO-fungi            | ReadSet | CNS  |
		 | RIBO-percEleve        | ReadSet | CNS  |
		 | MAP-PercentMP         | ReadSet | CNS  |
		 | MAP-tailleMP          | ReadSet | CNS  |
		 | MERG-PercLecMerg      | ReadSet | CNS  |
		 | MERG-MedLecMerg       | ReadSet | CNS  |
		 | PbM-indetermine       | Run     | CNS  |
		 | PbM-chiller           | Run     | CNS  |
		 | PbM-pelletier         | Run     | CNS  |
		 | PbM-fluidiq           | Run     | CNS  |
		 | PbM-laser             | Run     | CNS  |
		 | PbM-camera            | Run     | CNS  |
		 | PbM-lentille          | Run     | CNS  |
		 | PbM-pbVide            | Run     | CNS  |
		 | PbM-PEmodule          | Run     | CNS  |
		 | PbM-cBot              | Run     | CNS  |
		 | PbR-indetermine       | Run     | CNS  |
		 | PbR-FC                | Run     | CNS  |
		 | PbR-cBot              | Run     | CNS  |
		 | PbR-sequencage        | Run     | CNS  |
		 | PbR-indexing          | Run     | CNS  |
		 | PbR-PEmodule          | Run     | CNS  |
		 | PbR-rehybR1           | Run     | CNS  |
		 | PbR-rehybIndexing     | Run     | CNS  |
		 | PbR-rehybR2           | Run     | CNS  |
		 | PbR-erreurReac        | Run     | CNS  |
		 | PbR-ajoutReac         | Run     | CNS  |
		 | Sav-intensite         | Run     | CNS  |
		 | SAV-densiteElevee     | Run     | CNS  |
		 | SAV-densiteFaible     | Run     | CNS  |
		 | SAV-densiteNulle      | Run     | CNS  |
		 | SAV-PF                | Run     | CNS  |
		 | SAV-phasing           | Run     | CNS  |
		 | SAV-prephasing        | Run     | CNS  |
		 | SAV-errorRate         | Run     | CNS  |
		 | SAV-Q30               | Run     | CNS  |
		 | SAV-IndDemultiplex    | Run     | CNS  |
		 | PbI-indetermine       | Run     | CNS  |
		 | PbI-PC                | Run     | CNS  |
		 | PbI-ecran             | Run     | CNS  |
		 | PbI-espDisqInsuf      | Run     | CNS  |
		 | PbI-logiciel          | Run     | CNS  |
		 | PbI-rebootPC          | Run     | CNS  |
		 | PbI-parametrageRun    | Run     | CNS  |
		 | Info-runValidation    | Run     | CNS  |
		 | Info-arretSeq         | Run     | CNS  |
		 | Info_arretLogiciel    | Run     | CNS  |
		 | Info-remboursement    | Run     | CNS  |
		 | Info-FCredeposee      | Run     | CNS  |

		
		*/
	}
	
	/* Premiere version */
	
/*	public static String getStateFromStateTubeLims(int etatTubeLimsCode){

		if(etatTubeLimsCode==0){
			return "IW-E";
		}
		else if(etatTubeLimsCode==1){
			return "IW-V";
			// l'etat n'existe pas encore en prod
			//return "A-QC";
		}
		else if(etatTubeLimsCode==3){
			return "IW-V";
		}
		else if(etatTubeLimsCode==4){
			return "IW-P";
		}
		else if(etatTubeLimsCode==6 || etatTubeLimsCode==2){
			return "IU";
		}
		else if(etatTubeLimsCode==8 || etatTubeLimsCode==9){
			return "IS";
		}
		else if(etatTubeLimsCode==7 || etatTubeLimsCode==10 | etatTubeLimsCode==11 || etatTubeLimsCode==12 || etatTubeLimsCode==13 || etatTubeLimsCode==5){
			return "UA";
		}
		
		return null;
	}
	
*/	
	public static String getStateFromStateTubeLims(int etatTubeLimsCode){
		
		if(etatTubeLimsCode==8 || etatTubeLimsCode==4 ){
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
		if(experimentTypeCode==null){
			return getStateFromStateTubeLims(etatLims);
		}else if(containerCategoryCode.equals("lane")){
			return getStateFromStatePrepaflowcellLims(etatLims);
		}else if(experimentTypeCode.equals("solution-stock")){
			return getStateFromStateSolutionStock(etatLims);
		}else if(experimentTypeCode.equals("pcr-amplification-and-purification") || experimentTypeCode.equals("sizing")){
			return getStateFromStateAmplification(etatLims);
		}
		return null;
	}

	private static String getStateFromStateAmplification(int etatLimsCode) {
		if(etatLimsCode==2 || etatLimsCode==1  ) { return "IW-P"; }
		else if(etatLimsCode==3 || etatLimsCode==4 ||  etatLimsCode==6 || etatLimsCode==7 || etatLimsCode==8 ) { return "UA"; }
		else if( etatLimsCode==5 || etatLimsCode==9 ||  etatLimsCode==10 || etatLimsCode==11 || etatLimsCode==12 || etatLimsCode==13 || etatLimsCode==14  ) { return "IS";}
		return null;
	}
}
