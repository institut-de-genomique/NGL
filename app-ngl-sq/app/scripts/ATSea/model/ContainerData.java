package scripts.ATSea.model;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import play.Logger;
import scripts.ATSea.ReadExcelServices;
import validation.ContextValidation;

/**
 * Modelisation des données récupérées de Cardops
 * @author ejacoby
 *
 */
public class ContainerData {

	public String numContainer;
	public String categoryContainer;
	public Integer weight;
	public String dissectedTissue;
	public Date packingDate;
	public Date packingHour;
	public String packingName;
	public String containerDestination;
	public String storageMethod;
	public String temperatureStorage;
	public String containerDescription;
	public String bagNb;
	public String dryShipperNb;


	public ContainerData() {
		super();
	}

	public ContainerData(String numContainer, String categoryContainer, Integer weight, String dissectedTissue,
			Date packingDate, Date packingHour, String packingName, String containerDestination, String storageMethod,
			String temperatureStorage, String containerDescription, String bagNb, String dryShipperNb) {
		super();
		this.numContainer = numContainer;
		this.categoryContainer = categoryContainer;
		this.weight = weight;
		this.dissectedTissue = dissectedTissue;
		this.packingDate = packingDate;
		this.packingHour = packingHour;
		this.packingName = packingName;
		this.containerDestination = containerDestination;
		this.storageMethod = storageMethod;
		this.temperatureStorage = temperatureStorage;
		this.containerDescription = containerDescription;
		this.bagNb = bagNb;
		this.dryShipperNb = dryShipperNb;
	}

	public void build(Row row, Map<String, Integer> mappingColSamplingFile, ContextValidation cv, String fileName)
	{
		this.numContainer=ReadExcelServices.getStringCellValue(row, mappingColSamplingFile.get(ATSeaConstant.containerBarcode));
		this.categoryContainer=ReadExcelServices.getStringCellValue(row, mappingColSamplingFile.get(ATSeaConstant.containerCategory));

		if(row.getCell(mappingColSamplingFile.get(ATSeaConstant.weight))==null) {
			this.weight=0;
		}else if(row.getCell(mappingColSamplingFile.get(ATSeaConstant.weight)).getCellType()==Cell.CELL_TYPE_NUMERIC) {
			this.weight=ReadExcelServices.getNumericCellValue(row, mappingColSamplingFile.get(ATSeaConstant.weight));
		}else {
			String value = ReadExcelServices.getStringCellValue(row, mappingColSamplingFile.get(ATSeaConstant.weight));
			if(StringUtils.isNotBlank(value)) {
				((List<String>)cv.getObject("ErreurACorriger")).add(fileName+",,Container,"+numContainer+",erreur format poids valeur numérique attendue");
			}
		}
		this.dissectedTissue=ReadExcelServices.getStringCellValue(row, mappingColSamplingFile.get(ATSeaConstant.dissectTissue));
		if(row.getCell(mappingColSamplingFile.get(ATSeaConstant.packingDate))==null) {
			((List<String>)cv.getObject("ErreurACorriger")).add(fileName+",,Container,"+numContainer+",mauvais format de date");
		}else if(row.getCell(mappingColSamplingFile.get(ATSeaConstant.packingDate)).getCellType()==Cell.CELL_TYPE_STRING) {
			((List<String>)cv.getObject("ErreurACorriger")).add(fileName+",,Container,"+numContainer+",mauvais format de date");
		}else {
			this.packingDate=ReadExcelServices.getDateCellValue(row, mappingColSamplingFile.get(ATSeaConstant.packingDate));
		}
		this.packingHour=ReadExcelServices.getDateCellValue(row, mappingColSamplingFile.get(ATSeaConstant.packingHour));
		this.packingName=ReadExcelServices.getStringCellValue(row, mappingColSamplingFile.get(ATSeaConstant.packingName));
		this.containerDescription=ReadExcelServices.getStringCellValue(row, mappingColSamplingFile.get(ATSeaConstant.descContainer));
		this.storageMethod=ReadExcelServices.getStringCellValue(row, mappingColSamplingFile.get(ATSeaConstant.conservationMode));
		this.temperatureStorage=ReadExcelServices.getStringCellValue(row, mappingColSamplingFile.get(ATSeaConstant.tempStorage));
		if(row.getCell(mappingColSamplingFile.get(ATSeaConstant.bagNb))!=null) {
			if(row.getCell(mappingColSamplingFile.get(ATSeaConstant.bagNb)).getCellType()==Cell.CELL_TYPE_NUMERIC) {
				this.bagNb=""+ReadExcelServices.getNumericCellValue(row, mappingColSamplingFile.get(ATSeaConstant.bagNb));
			}else {
				this.bagNb=ReadExcelServices.getStringCellValue(row, mappingColSamplingFile.get(ATSeaConstant.bagNb));
			}
		}

		if(row.getCell(mappingColSamplingFile.get(ATSeaConstant.dryShipperNb))==null) {
			this.dryShipperNb="";
		}else if(row.getCell(mappingColSamplingFile.get(ATSeaConstant.dryShipperNb)).getCellType()==Cell.CELL_TYPE_NUMERIC) {
			Integer dryShipperNumeric = ReadExcelServices.getNumericCellValue(row, mappingColSamplingFile.get(ATSeaConstant.dryShipperNb));
			this.dryShipperNb=""+dryShipperNumeric;
		}else {
			((List<String>)cv.getObject("ErreurACorriger")).add(fileName+",,Container,"+numContainer+",erreur format sac regroupement valeur numérique attendue");
		}

		//this.dryShipperNb=ReadExcelServices.getStringCellValue(row, mappingColSamplingFile.get(ATSeaConstant.dryShipperNb));
		this.containerDestination=ReadExcelServices.getStringCellValue(row, mappingColSamplingFile.get(ATSeaConstant.destination));
	}

	@Override
	public String toString() {
		return "ContainerData [numContainer=" + numContainer + ", categoryContainer=" + categoryContainer + ", weight="
				+ weight + ", dissectedTissue=" + dissectedTissue + ", packingDate=" + packingDate + ", packingHour="
				+ packingHour + ", packingName=" + packingName + ", containerDestination=" + containerDestination
				+ ", storageMethod=" + storageMethod + ", temperatureStorage=" + temperatureStorage
				+ ", containerDescription=" + containerDescription + ", bagNb=" + bagNb + ", dryShipperNb="
				+ dryShipperNb + "]";
	}

	public void validate(ContextValidation cv, String fileName) {
		if(containerDestination!=null && containerDestination.toLowerCase().equals("genoscope")) {
			Logger.debug("storage method "+storageMethod);
			if(storageMethod!=null && !storageMethod.equals("FF (no preservative)")) {
				((List<String>)cv.getObject("Erreur")).add(fileName+",,Container,"+this.numContainer+",Le mode de conservation est différent de FF alors que c'est un tube Genoscope");
			}
			if(bagNb==null) {
				((List<String>)cv.getObject("Erreur")).add(fileName+",,Container,"+this.numContainer+",numéro de sac manquant pour le tube Genoscope");
			}
		}
		if(containerDestination!=null && storageMethod!=null) {
			if(containerDestination.equals("Voucher taxo.")) {
				if(!storageMethod.equals("EtOH") && !storageMethod.equals("Herbier")) {
					((List<String>)cv.getObject("Erreur")).add(fileName+",,Container,"+this.numContainer+",Le mode de conservation est différent de EtOH alors que c'est un tube Voucher taxo.");
				}
			}
			if(containerDestination.equals("Chimiothèque")) {
				if(bagNb==null) {
					((List<String>)cv.getObject("Erreur")).add(fileName+",,Container,"+this.numContainer+",un numéro de sac a été attribué à un tube chimiothèque");
				}
			}
		}
		if(storageMethod!=null && temperatureStorage!=null) {
			if((storageMethod.equals("EtOH") && !temperatureStorage.equals("RT (20-25°C)")) || (storageMethod.equals("FF (no preservative)") && !temperatureStorage.equals("-80°C"))) {
				((List<String>)cv.getObject("Erreur")).add(fileName+",,Container,"+this.numContainer+",Incohérence mode de conservation et température de stockage");
			}
		}

	}


}
