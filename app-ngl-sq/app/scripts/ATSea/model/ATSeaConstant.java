package scripts.ATSea.model;

import java.util.HashMap;
import java.util.Map;

public class ATSeaConstant {


	public static final String stationNumber = "stationNumber";
	public static final String collectLocalisation = "collectLocalisation";
	public static final String longitudeStart = "longitudeStart";
	public static final String latitudeStart = "latitudeStart";
	public static final String longitudeEnd = "longitudeEnd";
	public static final String latitudeEnd = "latitudeEnd";
	public static final String collectDate = "collectDate";
	public static final String collectHour = "collectHour";
	public static final String collectName = "collectName";
	public static final String habitat = "habitat";
	public static final String depth = "depth";
	public static final String collectMethod = "collectMethod";
	
	public static final String containerCategory = "containerCategory";
	public static final String containerBarcode = "containerBarcode";
	public static final String projectCode ="projectCode";
	public static final String sampleCode ="sampleCode";
	public static final String refCollab = "refCollab";
	public static final String sampleType = "sampleType";
	public static final String taxonId = "taxonId";
	public static final String nomOrgCollab = "nomOrgCollab";
	public static final String taxonName = "taxonName";
	public static final String taxonSize = "taxonSize";
	public static final String indCode = "indCode";
	public static final String lifeCyclePhase = "lifeCyclePhase";
	public static final String dissectTissue= "dissectTissue";
	public static final String weight = "weight";
	public static final String conservationMode = "conservationMode";
	public static final String tempStorage = "tempStorage";
	public static final String containerDate = "containerDate";
	public static final String descContainer = "descContainer";
	public static final String commentContainer = "commentContainer";
	public static final String meta="meta";
	public static final String stateContainer = "stateContainer";
	public static final String storageNGL = "storageNGL";
	public static final String destination = "destination";
	public static final String receptionDate = "receptionDate";
	public static final String sampleOrigin ="sampleOrigin";
	public static final String WoRMSCode = "WoRMSCode";
	public static final String goatStatus = "goatStatus";
	public static final String taxonomicConfidence = "taxonomicConfidence";
	public static final String taxonomistNames = "taxonomistNames";
	public static final String barcodingRef = "barcodingRef";
	public static final String geographicLocation = "geographicLocation";
	public static final String geographicRegion = "geographicRegion";
	public static final String individualType = "individualType";
	public static final String sex = "sex";
	public static final String ploidy = "ploidy";
	public static final String isOtherEukSpecProbability = "isOtherEukSpecProbability";
	public static final String individualViability = "individualViability";
	public static final String sampleDescription = "sampleDescription";
	public static final String prepSampleDescription = "prepSampleDescription";
	public static final String isTaxoVoucher = "isTaxoVoucher";
	public static final String individualBatch = "individualBatch";
	public static final String packingHour = "packingHour";
	public static final String initialContainerDescription = "initialContainerDescription";
	
	
	public static final String individualNumberMNHN = "individualNumberMNHN";
	public static final String individualNumberATS = "individualNumberATS";
	public static final String packingDate = "packingDate";
	public static final String packingName = "packingName";
	public static final String bagNb = "bagNb";
	public static final String dryShipperNb = "dryShipperNb";
	public static final String missionCode = "missionCode";
	public static final String coordinatorName = "coordinatorName";
	public static final String coordinatorAffiliation = "coordinatorAffiliation";
	public static final String barcodingCenter = "barcodingCenter";
	public static final String taxonomistAffiliation = "taxonomistAffiliation";
	
	//Value 
	/**
	 * Mapping du fichier cardops
	 */
	
	public static final Map<String, Integer> mapColDataCardops = new HashMap<String, Integer>(){{
		put(ATSeaConstant.stationNumber,1);
		put(ATSeaConstant.collectLocalisation,7);
		put(ATSeaConstant.longitudeStart,55);
		put(ATSeaConstant.latitudeStart,54);
		put(ATSeaConstant.longitudeEnd,57);
		put(ATSeaConstant.latitudeEnd,56);
		put(ATSeaConstant.collectDate, 21);
		put(ATSeaConstant.collectName,23);
		put(ATSeaConstant.habitat,27);
		put(ATSeaConstant.depth,58);
		put(ATSeaConstant.collectMethod,53);
	}};
	
	/**
	 * Mapping du fichier trame échantillonnage
	 */
	
	public static final Map<String, Integer> mapColDataSamplingFile = new HashMap<String, Integer>(){{
		put(ATSeaConstant.collectDate, 1);
		put(ATSeaConstant.collectHour, 2);
		put(ATSeaConstant.collectLocalisation,5);
		put(ATSeaConstant.latitudeStart,6);
		put(ATSeaConstant.latitudeEnd,7);
		put(ATSeaConstant.longitudeStart,8);
		put(ATSeaConstant.longitudeEnd,9);
		put(ATSeaConstant.depth,10);
		put(ATSeaConstant.collectMethod,12);
		put(ATSeaConstant.collectName,14);
		put(ATSeaConstant.habitat,15);
		put(ATSeaConstant.stationNumber,16);
		
	}};
	
	
	
	/**
	 * Mapping de la trame reception Config
	 */
	public static final Map<String, Integer> mapColReceptionConfig = new HashMap<String, Integer>(){{
		put(ATSeaConstant.sampleOrigin,0);
		put(ATSeaConstant.receptionDate, 1);
		put(ATSeaConstant.containerCategory, 2);
		put(ATSeaConstant.containerBarcode, 3);
		put(ATSeaConstant.stateContainer,6);
		put(ATSeaConstant.projectCode, 7);
		put(ATSeaConstant.sampleCode,8);
		put(ATSeaConstant.refCollab,9);
		put(ATSeaConstant.sampleType,10);
		put(ATSeaConstant.meta,11);
		put(ATSeaConstant.taxonId,12);
		put(ATSeaConstant.WoRMSCode,13);
		put(ATSeaConstant.goatStatus,14);
		put(ATSeaConstant.nomOrgCollab,15);
		put(ATSeaConstant.taxonomicConfidence,16);
		put(ATSeaConstant.taxonomistNames,17);
		put(ATSeaConstant.barcodingRef,18);
		put(ATSeaConstant.taxonSize,19);
		put(ATSeaConstant.geographicLocation,20);
		put(ATSeaConstant.geographicRegion,21);
		put(ATSeaConstant.collectDate,22);
		put(ATSeaConstant.collectHour,23);
		put(ATSeaConstant.collectMethod,24);
		put(ATSeaConstant.individualType,25);
		put(ATSeaConstant.lifeCyclePhase,26);
		put(ATSeaConstant.sex,27);
		put(ATSeaConstant.ploidy,28);
		put(ATSeaConstant.isOtherEukSpecProbability,29);
		put(ATSeaConstant.individualViability,30);
		put(ATSeaConstant.sampleDescription, 31);
		put(ATSeaConstant.prepSampleDescription, 32);
		put(ATSeaConstant.isTaxoVoucher, 33);
		put(ATSeaConstant.individualBatch, 34);
		put(ATSeaConstant.indCode,35);
		put(ATSeaConstant.dissectTissue,36);
		put(ATSeaConstant.weight,37);
		put(ATSeaConstant.destination, 45);
		put(ATSeaConstant.conservationMode,46);
		put(ATSeaConstant.tempStorage,47);
		put(ATSeaConstant.containerDate,48);
		put(ATSeaConstant.packingHour,49);
		put(ATSeaConstant.initialContainerDescription, 50);
		put(ATSeaConstant.commentContainer,51);
		put(ATSeaConstant.storageNGL, 56);
		
		
	}};
	
	//Mapping de la trame echantillonnage
	public static final Map<String, Integer> mapColDataSampleFileToReception = new HashMap<String, Integer>(){{
		put(ATSeaConstant.sampleOrigin,0);
		put(ATSeaConstant.collectDate,1);
		put(ATSeaConstant.containerCategory, 44);
		put(ATSeaConstant.containerBarcode,40);
		put(ATSeaConstant.taxonName,17);
		put(ATSeaConstant.WoRMSCode,18);
		put(ATSeaConstant.goatStatus,19);
		put(ATSeaConstant.taxonomicConfidence,20);
		put(ATSeaConstant.taxonomistNames,21);
		put(ATSeaConstant.barcodingRef,24);
		put(ATSeaConstant.geographicLocation,3);
		put(ATSeaConstant.geographicRegion,4);
		put(ATSeaConstant.collectDate,1);
		put(ATSeaConstant.collectHour,2);
		put(ATSeaConstant.collectMethod,12);
		put(ATSeaConstant.individualType,37);
		put(ATSeaConstant.lifeCyclePhase,25);
		put(ATSeaConstant.sex,26);
		put(ATSeaConstant.ploidy,27);
		put(ATSeaConstant.isOtherEukSpecProbability,28);
		put(ATSeaConstant.individualViability,29);
		put(ATSeaConstant.sampleDescription, 33);
		put(ATSeaConstant.prepSampleDescription, 38);
		put(ATSeaConstant.isTaxoVoucher, 36);
		put(ATSeaConstant.individualBatch, 35);
		put(ATSeaConstant.indCode,39);
		put(ATSeaConstant.dissectTissue,47);
		put(ATSeaConstant.weight,45);
		put(ATSeaConstant.initialContainerDescription,58);
		put(ATSeaConstant.conservationMode,56);
		put(ATSeaConstant.tempStorage,57);
		put(ATSeaConstant.containerDate,41);
		put(ATSeaConstant.packingHour,42);
		put(ATSeaConstant.destination,55);
		
		
	}};
	
}
