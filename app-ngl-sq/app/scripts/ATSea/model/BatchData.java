package scripts.ATSea.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.poi.ss.usermodel.Row;

import scripts.ATSea.ReadExcelServices;
import validation.ContextValidation;

/**
 * Modelisation des données récupérées de Cardops
 * @author ejacoby
 *
 */
public class BatchData {

	public String stationCode;
	public String taxon;
	public String taxonomicConfidence;
	public String taxonomistName;
	public String isVoucherTaxo;
	public String numBatchATS;
	public String fileName;
	public Set<String> listCollectDate = new HashSet<String>();

	

	public List<IndividualData> individualsData = new ArrayList<IndividualData>();

	public BatchData() {
		super();
	}

	public void build(Row row, Map<String, Integer> mappingColSamplingFile)
	{
		this.stationCode=ReadExcelServices.getStringCellValue(row, mappingColSamplingFile.get(ATSeaConstant.stationNumber));
		this.taxon=ReadExcelServices.getStringCellValue(row, mappingColSamplingFile.get(ATSeaConstant.taxonName));
		this.taxonomicConfidence=ReadExcelServices.getStringCellValue(row, mappingColSamplingFile.get(ATSeaConstant.taxonomicConfidence));
		this.taxonomistName=ReadExcelServices.getStringCellValue(row, mappingColSamplingFile.get(ATSeaConstant.taxonomistNames));
		this.isVoucherTaxo=ReadExcelServices.getStringCellValue(row, mappingColSamplingFile.get(ATSeaConstant.isTaxoVoucher));
		this.numBatchATS=ReadExcelServices.getStringCellValue(row, mappingColSamplingFile.get(ATSeaConstant.individualBatch));
	}

	@Override
	public String toString() {
		return "BatchData [stationCode=" + stationCode + ", taxon=" + taxon + ", taxonomicConfidence="
				+ taxonomicConfidence + ", taxonomistName=" + taxonomistName + ", isVoucherTaxo=" + isVoucherTaxo
				+ ", numBatchATS=" + numBatchATS ;
	}

	public void validate(ContextValidation cv, String fileName) 
	{
		
		//Validation regex numLot ATS
		if(!numBatchATS.startsWith("A-")) {
			((List<String>)cv.getObject("Erreur")).add(fileName+",,Lot individus,"+numBatchATS+",Mauvais code barre du lot (n'est pas au format ATLASea)");
		}
		
		//Validation voucher		
		//Get voucher container with a code barre ATS
		ContainerData containerData;
		try {
			containerData = getByNumContainer(numBatchATS, cv, fileName);
			if(containerData!=null) {
				//Voucher taxo oui
				if(!this.isVoucherTaxo.toLowerCase().equals("oui")){
					((List<String>)cv.getObject("Erreur")).add(fileName+",,Container,"+containerData.numContainer+",Mauvaise valeur \"Voucher taxo. ?\" : valeur attendue \"oui\" ");
				}
				//Destination 
				if(!containerData.containerDestination.equals("Voucher taxo.")) {
					((List<String>)cv.getObject("Erreur")).add(fileName+",,Container,"+containerData.numContainer+",Mauvaise valeur \"Tube destiné à \" : valeur attendue \"Voucher taxo.\"");
				}
			}else {
				//Voucher taxo non
				if(!this.isVoucherTaxo.toLowerCase().equals("non")){
					((List<String>)cv.getObject("Erreur")).add(fileName+",,Lot,"+this.numBatchATS+",Ligne tube voucher manquante");
				}
			}
		} catch (ATSException e) {
			//Erreur innattendue plusieurs containers pour le même code lot
			e.printStackTrace();
		}
		
		//Validation des individus 
		for(IndividualData ind : individualsData) {
			ind.validate(cv, fileName);
		}
	}

	public ContainerData getByNumContainer(String numContainer, ContextValidation cv, String fileName) throws ATSException {
		List<ContainerData> contDatas = new ArrayList<ContainerData>();
		
		for(IndividualData ind : individualsData) {
			ContainerData contData = ind.getByNumContainer(numContainer);
			if(contData!=null)
				contDatas.add(contData);
		}
		
		if(contDatas.size()==0) {
			return null;
		}else if(contDatas.size()==1) {
			return contDatas.get(0);
		}else {
			//throw Exception pour ne pas avoir à remplir le cv à chaque fois
			((List<String>)cv.getObject("Erreur")).add(fileName+",Container,"+numContainer+",Code container non unique");
			throw new ATSException("Numéro container non unique "+numContainer);
		}
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public Set<String> getListCollectDate() {
		return listCollectDate;
	}

	public void setListCollectDate(Set<String> listCollectDate) {
		this.listCollectDate = listCollectDate;
	}
}
