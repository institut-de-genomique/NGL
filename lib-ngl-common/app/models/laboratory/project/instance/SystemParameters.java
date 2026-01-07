package models.laboratory.project.instance;

import java.util.Date;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;

import validation.ContextValidation;
import validation.IValidation;
import validation.project.instance.ProjectValidationHelper;
import validation.utils.ValidationHelper;

public class SystemParameters implements IValidation {
	
	public Boolean synchroProj;
	
	public List<ArchiveEntry> archiveHistory;
	
	public QtreeQuota qtreeQuota;

	@Override
	public void validate(ContextValidation contextValidation) {
		ValidationHelper.validateNotEmpty(contextValidation, synchroProj, "systemParameters.synchroProj");
		if(CollectionUtils.isNotEmpty(archiveHistory)) {
			ProjectValidationHelper.validateArchiveHistory(contextValidation, archiveHistory);
		}
		if(ValidationHelper.validateNotEmpty(contextValidation, qtreeQuota, "systemParameters.qtreeQuota")) {
			qtreeQuota.validate(contextValidation);
		}
	}
	
	public static final class ArchiveEntry {
		
		public static enum Action {ARCHIVAGE, DESARCHIVAGE};
		
		public Date date;
		
		public String scratchArchiveId;
		
		public String projArchiveId;
		
		public String rawArchiveId;
		
		public Action action;

		@JsonGetter("action")
		public String getAction() {
			return action.name();
		}

		@JsonSetter("action")
		public void setAction(String action) {
			this.action = Action.valueOf(action);
		}
		
	}
	
	public static final class QtreeQuota implements IValidation {
		
		public Integer proj;
		
		public Integer rawdata;
		
		public Integer scratch;

		@Override
		public void validate(ContextValidation contextValidation) {
			ValidationHelper.validateNotEmpty(contextValidation, proj, "systemParameters.qtreeQuota.proj");
			ValidationHelper.validateNotEmpty(contextValidation, rawdata, "systemParameters.qtreeQuota.rawdata");
			ValidationHelper.validateNotEmpty(contextValidation, scratch, "systemParameters.qtreeQuota.scratch");
		}
		
	}

}
