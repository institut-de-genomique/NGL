package scripts.ATSea.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Row;

import scripts.ATSea.ReadExcelServices;
import validation.ContextValidation;

/**
 * Modelisation des données récupérées de Cardops
 * @author ejacoby
 *
 */
public class IndividualData {

	public String lifeCyclePhase;
	public String sex;
	public String isOtherEukProbability;
	public String viability;
	public String sampleDescription;
	public String prepSampleDescription;
	public String individualType;
	public String numIndividuMNHN;
	public String numIndividuATS;

	public List<ContainerData> containersData = new ArrayList<ContainerData>();

	
	public IndividualData() {
		super();
	}

	public void build(Row row, Map<String, Integer> mappingColSamplingFile, IndividualData previousIndividu)
	{
		
		lifeCyclePhase=ReadExcelServices.getStringCellValue(row, mappingColSamplingFile.get(ATSeaConstant.lifeCyclePhase));
		if(StringUtils.isBlank(lifeCyclePhase) && previousIndividu!=null && StringUtils.isNotBlank(previousIndividu.lifeCyclePhase))
			lifeCyclePhase=previousIndividu.lifeCyclePhase;
		
		sex=ReadExcelServices.getStringCellValue(row, mappingColSamplingFile.get(ATSeaConstant.sex));
		if(StringUtils.isBlank(sex) && previousIndividu!=null && StringUtils.isNotBlank(previousIndividu.sex))
			sex=previousIndividu.sex;
		
		isOtherEukProbability=ReadExcelServices.getStringCellValue(row, mappingColSamplingFile.get(ATSeaConstant.isOtherEukSpecProbability));
		if(StringUtils.isBlank(isOtherEukProbability) && previousIndividu!=null && StringUtils.isNotBlank(previousIndividu.isOtherEukProbability))
			isOtherEukProbability=previousIndividu.isOtherEukProbability;
		
		sampleDescription=ReadExcelServices.getStringCellValue(row, mappingColSamplingFile.get(ATSeaConstant.sampleDescription));
		if(StringUtils.isBlank(sampleDescription) && previousIndividu!=null && StringUtils.isNotBlank(previousIndividu.sampleDescription))
			sampleDescription=previousIndividu.sampleDescription;
		
		prepSampleDescription=ReadExcelServices.getStringCellValue(row, mappingColSamplingFile.get(ATSeaConstant.prepSampleDescription));
		if(StringUtils.isBlank(prepSampleDescription) && previousIndividu!=null && StringUtils.isNotBlank(previousIndividu.prepSampleDescription))
			prepSampleDescription=previousIndividu.prepSampleDescription;
		
		individualType=ReadExcelServices.getStringCellValue(row, mappingColSamplingFile.get(ATSeaConstant.individualType));
		if(StringUtils.isBlank(individualType) && previousIndividu!=null &&  StringUtils.isNotBlank(previousIndividu.individualType))
			individualType=previousIndividu.individualType;
		
		numIndividuMNHN=ReadExcelServices.getStringCellValue(row, mappingColSamplingFile.get(ATSeaConstant.individualNumberMNHN));
		numIndividuATS=ReadExcelServices.getStringCellValue(row, mappingColSamplingFile.get(ATSeaConstant.individualNumberATS));
		
		viability=ReadExcelServices.getStringCellValue(row, mappingColSamplingFile.get(ATSeaConstant.individualViability));
		if(StringUtils.isBlank(viability) && previousIndividu!=null && StringUtils.isNotBlank(previousIndividu.viability))
			viability=previousIndividu.viability;
	
	}

	@Override
	public String toString() {
		return "IndividualData [lifeCyclePhase=" + lifeCyclePhase + ", sex=" + sex + ", isOtherEukProbability="
				+ isOtherEukProbability + ", viability=" + viability + ", sampleDescription=" + sampleDescription
				+ ", prepSampleDescription=" + prepSampleDescription + ", individualType=" + individualType
				+ ", numIndividuMNHN=" + numIndividuMNHN + ", numIndividuATS=" + numIndividuATS;
	}

	public ContainerData getByNumContainer(String numContainer)
	{
		List<ContainerData> findContainerDatas = containersData.stream().filter(c->c.numContainer.equals(numContainer)).collect(Collectors.toList());
		if(findContainerDatas.size()==1)
			return findContainerDatas.get(0);
		else {
			//TODO message erreur not unique value
			return null;
		}
	}
	
	public void validate(ContextValidation cv, String fileName)
	{
		//REGEX code individu
		if(!numIndividuATS.startsWith("A-")) {
			((List<String>)cv.getObject("Erreur")).add(fileName+",,Individu,"+this.numIndividuATS+",Mauvais code barre (n'est pas au format ATLASea)");
		}
		//Pas possible pour les poissons CE et algues PC
		if(!numIndividuMNHN.startsWith("MNHN-") && !numIndividuMNHN.startsWith("CE-") && !numIndividuMNHN.startsWith("PC")) {
			((List<String>)cv.getObject("Erreur")).add(fileName+",,Individu,"+this.numIndividuMNHN+",Mauvais code barre (n'est pas au format MNHN)");
		}
		
		//Warning if population
		if(StringUtils.isBlank(individualType)) {
			((List<String>)cv.getObject("Erreur")).add(fileName+",,Individu,"+this.numIndividuATS+",La catégorie individu n'est pas renseigné");
		}else if(individualType.equals("Population")) {
			((List<String>)cv.getObject("Erreur")).add(fileName+",,Individu,"+this.numIndividuATS+",La catégorie individu est Population");
		}
		
		//Validation premier code barre genomique = code unique individu ATLASea
		Optional<ContainerData> optFirstGenomiqeContainer = containersData.stream().
			    filter(c -> c.containerDestination.toLowerCase().equals("genoscope")).
			    findFirst();
		if(optFirstGenomiqeContainer.isPresent()) {
			ContainerData firstGenomiqueContainer = optFirstGenomiqeContainer.get();
			if(!firstGenomiqueContainer.numContainer.equals(this.numIndividuATS)) {
				((List<String>)cv.getObject("Erreur")).add(fileName+",,Individu,"+this.numIndividuATS+",Le code individu ATS ne correspond pas au premier tube génomique");
			}
		}
		for(ContainerData container : containersData) {
			container.validate(cv, fileName);
		}
		
	}

}
