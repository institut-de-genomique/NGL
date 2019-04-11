package services.io.reception;

import javax.inject.Inject;

import models.laboratory.common.instance.property.PropertyFileValue;
import models.laboratory.reception.instance.ReceptionConfiguration;
import models.laboratory.reception.instance.ReceptionConfiguration.FileType;
import validation.ContextValidation;

public class ReceptionFileService {
	
//	private final NGLContext ctx;
//
//	@Inject
//	public ReceptionFileService(NGLContext ctx) {
//		this.ctx = ctx;
//	}

//	private final NGLApplication app;
//
//	@Inject
//	public ReceptionFileService(NGLApplication app) {
//		this.app = app;
//	}

	@Inject
	public ReceptionFileService() {}
	
	public FileService getFileService(ReceptionConfiguration configuration,	PropertyFileValue fileValue, ContextValidation contextValidation) {		
		if (FileType.excel.equals(configuration.fileType)) {
//			ExcelFileService efs = new ExcelFileService(configuration, fileValue, contextValidation, app);
			ExcelFileService efs = new ExcelFileService(configuration, fileValue, contextValidation);
			return efs;
		} else {
			contextValidation.addError("Error", "FileType : "+configuration.fileType.toString());
			throw new UnsupportedOperationException("FileType : "+configuration.fileType.toString());
		}
	}
	
}
