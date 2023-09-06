package models.sra.submit.sra.instance;

import java.util.ArrayList;
import java.util.List;

import validation.ContextValidation;
import validation.IValidation;
import validation.utils.ValidationHelper;

public class ReadSpec implements IValidation {
	
	public int readIndex;
	public String readClass;
	public String readType;
	public Integer baseCoord;
	public String readLabel;
	public List<String> expectedBaseCallTable = new ArrayList<>(); 

	@Override
	public void validate(ContextValidation contextValidation) {
		contextValidation = contextValidation.appendPath("readSpec");
		ValidationHelper.validateNotEmpty(contextValidation, this.readIndex, "readIndex");
		ValidationHelper.validateNotEmpty(contextValidation, this.readClass, "readClass");
		ValidationHelper.validateNotEmpty(contextValidation, this.readType, "readType");
		if (this.expectedBaseCallTable.size() == 0 ) {
			ValidationHelper.validateNotEmpty(contextValidation, this.baseCoord, "baseCoord");
		}
	}

}
