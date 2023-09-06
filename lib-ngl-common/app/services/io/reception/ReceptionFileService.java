package services.io.reception;

import javax.inject.Inject;

import models.laboratory.common.instance.property.PropertyFileValue;
import models.laboratory.reception.instance.ReceptionConfiguration;
import validation.ContextValidation;

/**
 * File reception service.
 * <p>
 * Maps a {@link ReceptionConfiguration} to a {@link FileService} through
 * the {@link #getFileService(ReceptionConfiguration, PropertyFileValue, ContextValidation)}
 * method. 
 */
public class ReceptionFileService {

	/**
	 * Default (injected) constructor, could be removed 
	 * (same as default constructor).
	 */
	@Inject
	public ReceptionFileService() {
	}
	
//	public FileService getFileService(ReceptionConfiguration configuration,	PropertyFileValue fileValue, ContextValidation contextValidation) {		
//		if (FileType.excel.equals(configuration.fileType)) {
//			ExcelFileService efs = new ExcelFileService(configuration, fileValue, contextValidation);
//			return efs;
//		} else {
//			contextValidation.addError("Error", "FileType : "+configuration.fileType.toString());
//			throw new UnsupportedOperationException("FileType : "+configuration.fileType.toString());
//		}
//	}
	
	public FileService getFileService(ReceptionConfiguration configuration,	PropertyFileValue fileValue, ContextValidation contextValidation) {		
		switch (configuration.fileType) {
		case excel : return new ExcelFileService(configuration, fileValue, contextValidation);
		default    :
			contextValidation.addError("Error", "FileType : " + configuration.fileType);
			throw new IllegalArgumentException("FileType : " + configuration.fileType);
		}
	}
	
}
