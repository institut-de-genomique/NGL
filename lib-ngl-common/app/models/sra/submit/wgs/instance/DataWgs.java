package models.sra.submit.wgs.instance;

import validation.ContextValidation;
import validation.IValidation;
import validation.utils.ValidationHelper;

public class DataWgs  implements IValidation {
	public String relatifName;            // nom du fichier != lotSeqName avec extention mais sans chemin
	public String directory;	          // chemin
	public String extention;              // extention .fastq, .fastq.gz, .sff
	public String relatifNameMd5;

	@Override
	public void validate(ContextValidation contextValidation) {
		// Verifer que les champs sont bien renseignés :
		if (ValidationHelper.validateNotEmpty(contextValidation, this.relatifName,"relatifName")) {
			contextValidation.addKeyToRootKeyName("wgsData::relatifName::" + this.relatifName + "::");
		} else {
			contextValidation.addKeyToRootKeyName("relatifName::");
		}
		
		ValidationHelper.validateNotEmpty(contextValidation, this.relatifName , "relatifName");
		ValidationHelper.validateNotEmpty(contextValidation, this.directory , "directory");
		ValidationHelper.validateNotEmpty(contextValidation, this.extention , "extention");
		ValidationHelper.validateNotEmpty(contextValidation, this.relatifNameMd5 , "relatifNameMd5");

		if (ValidationHelper.validateNotEmpty(contextValidation, this.relatifName,"relatifName")) {
			contextValidation.removeKeyFromRootKeyName("rawData::relatifName::" + this.relatifName + "::");
		} else {
			contextValidation.removeKeyFromRootKeyName("wgsData::");
		}
	}

}
