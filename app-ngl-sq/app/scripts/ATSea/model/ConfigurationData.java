package scripts.ATSea.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import play.Logger;
import scripts.ATSea.ReadExcelServices;

public class ConfigurationData {

	public Map<String, Integer> mappingColSamplingFile = new HashMap<String, Integer>();
	public Map<String, Integer> mappingColCompleteData = new HashMap<String, Integer>();
	public Map<String, String> mappingColDefaultValue = new HashMap<String, String>();
	public Set<String> mandatoryProperties = new HashSet<String>();
	
	public ConfigurationData() {
		super();
		this.mappingColSamplingFile = new HashMap<String, Integer>();
		this.mappingColCompleteData = new HashMap<String, Integer>();
		this.mappingColDefaultValue = new HashMap<String, String>();
		this.mandatoryProperties = new HashSet<String>();
	}
	
	public void build(File inputConfigurationFile) throws IOException
	{
		if(inputConfigurationFile.exists() && inputConfigurationFile.canRead()) {
			Logger.debug("Lecture fichier configuration "+inputConfigurationFile.getName());
			FileInputStream fis = new FileInputStream(inputConfigurationFile);
			XSSFWorkbook workbookInputConfiguration = new XSSFWorkbook (fis);
			//Lecture du premier onglet (colonne saisie dans la trame)
			workbookInputConfiguration.getSheetAt(0).rowIterator().forEachRemaining(row -> {
				if(row.getRowNum() == 0) return; // skip header
				mappingColSamplingFile.put(row.getCell(0).getStringCellValue(), (int) row.getCell(2).getNumericCellValue());
				if(row.getCell(3).getBooleanCellValue()) {
					mandatoryProperties.add(row.getCell(0).getStringCellValue());
				}

			});
			//Lecture du second onglet (colonne à completer dans la trame)
			workbookInputConfiguration.getSheetAt(1).rowIterator().forEachRemaining(row -> {
				if(row.getRowNum() == 0) return; // skip header
				mappingColCompleteData.put(row.getCell(0).getStringCellValue(), (int) row.getCell(2).getNumericCellValue());
				if(StringUtils.isNoneBlank(ReadExcelServices.getStringCellValue(row, 4))){
					mappingColDefaultValue.put(row.getCell(0).getStringCellValue(), row.getCell(4).getStringCellValue());
				}
			});
		}else {
			//TODO erreur et pas de traitement de fichier
		}
	}
}
