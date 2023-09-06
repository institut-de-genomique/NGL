package workflows.container;

import org.mongojack.DBQuery;
import org.mongojack.DBQuery.Query;

import models.laboratory.container.instance.Container;
import models.laboratory.container.instance.Content;
import models.utils.InstanceConstants;


public class ContentHelper {
	
	/**
	 * MongoDB query to find a specific (project code, sample code,
	 * optional tag property) content inside containers (container code).
	 * @param container container
	 * @param content   content
	 * @return          MongoDB query
	 */
	public static Query getContentQuery(Container container, Content content) {
		Query contentQuery = DBQuery.is("projectCode", content.projectCode)
				                    .is("sampleCode",  content.sampleCode);
		if (content.properties.containsKey(InstanceConstants.TAG_PROPERTY_NAME)) 
			contentQuery.is("properties.tag.value", content.properties.get(InstanceConstants.TAG_PROPERTY_NAME).value);
		else if(!content.properties.containsKey(InstanceConstants.TAG_PROPERTY_NAME))
			contentQuery.notExists("properties.tag");
		if(content.properties.containsKey(InstanceConstants.SECONDARY_TAG_PROPERTY_NAME))
			contentQuery.is("properties.secondaryTag.value", content.properties.get(InstanceConstants.SECONDARY_TAG_PROPERTY_NAME).value);
		else if(!content.properties.containsKey(InstanceConstants.SECONDARY_TAG_PROPERTY_NAME))
			contentQuery.notExists("properties.secondaryTag");
		
		return DBQuery.is       ("code",     container.code)
		              .elemMatch("contents", contentQuery);
	}

}
