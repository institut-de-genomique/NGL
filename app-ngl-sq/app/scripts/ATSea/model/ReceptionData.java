package scripts.ATSea.model;

import java.util.Date;

public class ReceptionData {
	
	public String sampleOrigin;
	public String containerCategory;
	public String containerBarcode;
	public String projectCode;
	public String sampleCode;
	public String refCollab;
	public String sampleType="biological-sample";
	public Integer taxonId;
	public Integer WoRMSCode;
	public String goatStatus;
	public String nomOrgCollab;
	public String taxonName;
	public Long taxonSize;
	public String indCode;
	public String lifeCyclePhase;
	public String dissectTissue;
	public Integer weight;
	public String conservationMode;
	public String tempStorage;
	public Date collectDate;
	public Date containerDate;
	public String descContainer;
	public String commentContainer;
	public String meta;
	public String stateContainer;
	public String storage;
	public Date receptionDate;
	public String taxonomicConfidence;
	public String taxonomistNames;
	public String barcodingRef;
	public String geographicLocation;
	public String geographicRegion;
	public String individualType;
	public String sex;
	public String ploidy;
	public String isOtherEukSpecProbability;
	public String individualViability;
	public String sampleDescription;
	public String prepSampleDescription;
	public String isTaxoVoucher;
	public String individualBatch;
	public String packingHour;
	public String initialContainerDescription;
	public String collectHour;
	public String collectMethod;
	public String destination;
	
	public ReceptionData(String sampleOrigin, String containerCategory, String containerBarcode, String projectCode,
			String sampleCode, String refCollab, String sampleType, Integer taxonId, Integer woRMSCode,
			String goatStatus, String nomOrgCollab, String taxonName, Long taxonSize, String indCode,
			String lifeCyclePhase, String dissectTissue, Integer weight, String conservationMode, String tempStorage,
			Date collectDate, Date containerDate, String descContainer, String commentContainer, String meta,
			String stateContainer, String storage, Date receptionDate, String taxonomicConfidence,
			String taxonomistNames, String barcodingRef, String geographicLocation, String geographicRegion,
			String individualType, String sex, String ploidy, String isOtherEukSpecProbability,
			String individualViability, String sampleDescription, String prepSampleDescription, String isTaxoVoucher,
			String individualBatch, String packingHour, String initialContainerDescription, String collectHour, String collectMethod, String destination) {
		super();
		this.sampleOrigin = sampleOrigin;
		this.containerCategory = containerCategory;
		this.containerBarcode = containerBarcode;
		this.projectCode = projectCode;
		this.sampleCode = sampleCode;
		this.refCollab = refCollab;
		this.sampleType = sampleType;
		this.taxonId = taxonId;
		this.WoRMSCode = woRMSCode;
		this.goatStatus = goatStatus;
		this.nomOrgCollab = nomOrgCollab;
		this.taxonName = taxonName;
		this.taxonSize = taxonSize;
		this.indCode = indCode;
		this.lifeCyclePhase = lifeCyclePhase;
		this.dissectTissue = dissectTissue;
		this.weight = weight;
		this.conservationMode = conservationMode;
		this.tempStorage = tempStorage;
		this.collectDate = collectDate;
		this.containerDate = containerDate;
		this.descContainer = descContainer;
		this.commentContainer = commentContainer;
		this.meta = meta;
		this.stateContainer = stateContainer;
		this.storage = storage;
		this.receptionDate = receptionDate;
		this.taxonomicConfidence = taxonomicConfidence;
		this.taxonomistNames = taxonomistNames;
		this.barcodingRef = barcodingRef;
		this.geographicLocation = geographicLocation;
		this.geographicRegion = geographicRegion;
		this.individualType = individualType;
		this.sex = sex;
		this.ploidy = ploidy;
		this.isOtherEukSpecProbability = isOtherEukSpecProbability;
		this.individualViability = individualViability;
		this.sampleDescription = sampleDescription;
		this.prepSampleDescription = prepSampleDescription;
		this.isTaxoVoucher = isTaxoVoucher;
		this.individualBatch = individualBatch;
		this.packingHour = packingHour;
		this.initialContainerDescription = initialContainerDescription;
		this.collectHour=collectHour;
		this.collectMethod=collectMethod;
		this.destination=destination;
	}
	
	
	
}
