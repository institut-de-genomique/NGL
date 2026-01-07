package scripts.from.object;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.mongojack.DBQuery;
import org.mongojack.DBQuery.Query;

import controllers.admin.supports.api.NGLObject;
import controllers.admin.supports.api.NGLObjectsSearchForm;
import controllers.admin.supports.api.objects.ReadSetUpdate;
import controllers.readsets.api.ReadSets;
import fr.cea.ig.MongoDBDAO;
import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.run.instance.ReadSet;
import models.utils.InstanceConstants;
import play.Logger;
import validation.ContextValidation;

public class ReadSetUpdateFromProperties extends ReadSetUpdate {

	public ReadSetUpdateFromProperties(ReadSets readSets) {
		super(readSets);
	}


	@Override
	public void update(NGLObject input, ContextValidation cv) {
		if (NGLObject.Action.add.equals(NGLObject.Action.valueOf(input.action))){
			Logger.debug("Add property to ReadSet "+input.code);
			ReadSet readSet = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, input.code);

			

			String newValue = input.newValue;
			Logger.debug("New value "+newValue);
			Logger.debug("property value is single tyoe");
			if(!readSet.sampleOnContainer.properties.containsKey(input.contentPropertyNameUpdated)){
				readSet.traceInformation.modifyDate=new Date();
				readSet.traceInformation.modifyUser=cv.getUser();
				Logger.debug("ReadSet not contains "+input.contentPropertyNameUpdated);
				PropertySingleValue pv = new PropertySingleValue(newValue);
				readSet.sampleOnContainer.properties.put(input.contentPropertyNameUpdated, pv);
				Logger.debug("Add "+input.contentPropertyNameUpdated+" with value "+pv.getValue()+" for "+input.sampleCode);
				//On garde la validation car ne vérifie pas la description des propriétés content
				readSet.validate(cv);
				if(!cv.hasErrors())
					updateObject(readSet);
				else
					Logger.debug(cv.getErrors().toString());			}
		}else {
			throw new RuntimeException(input.action + " not implemented");
		}
	}

	
}
