package models.sra.submit.sra.instance;

import validation.ContextValidation;
import validation.IValidation;
import validation.utils.ValidationHelper;

public class RawData  implements IValidation {
	public String relatifName;            // nom du fichier != lotSeqName avec extention mais sans chemin
	public String directory;	          // chemin
	public String extention;              // extention .fastq, .fastq.gz, .sff
	public String md5;
	public String location;
	
	public Boolean gzipForSubmission = false;
	public String submittedMd5;
	
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
