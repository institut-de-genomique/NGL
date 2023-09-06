package models.sra.submit.sra.instance;

import validation.ContextValidation;
import validation.IValidation;
import validation.utils.ValidationHelper;

public class RawData  implements IValidation {
	public String readsetCode;            // code du readset auquel est rattaché ce rawdata
	public String analysisCode;           // code de l'analyse auquel est rattaché ce rawdata si bionano
	public String relatifName;            // nom du fichier != lotSeqName avec extention mais sans chemin
	public String directory;	          // chemin
	public String extention;              // extention .fastq, .fastq.gz, .sff
	public String md5;
	public String location;
	public String collabFileName;         // nom du fichier final à l'EBI : fastq.gz ou sff donné par le champs collabFileName de readset
	public Boolean gzipForSubmission = false;
	public Boolean md5sumForSubmission = false; // md5ForSubmission = false;
	public String instrumentUsedTypeCode;

	@Override
	public void validate(ContextValidation contextValidation) {
		// Verifer que les champs sont bien renseignés :
		contextValidation = contextValidation.appendPath("rawData");
		if (ValidationHelper.validateNotEmpty(contextValidation, relatifName, "relatifName")) 
			contextValidation = contextValidation.appendPath(relatifName);
		ValidationHelper.validateNotEmpty(contextValidation, this.directory , "directory");
		ValidationHelper.validateNotEmpty(contextValidation, this.extention , "extention");
//		ValidationHelper.validateNotEmpty(contextValidation, this.md5 , "md5");
		ValidationHelper.validateNotEmpty(contextValidation, this.location , "location");
	}

}
