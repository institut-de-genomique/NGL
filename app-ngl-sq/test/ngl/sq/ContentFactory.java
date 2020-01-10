package ngl.sq;

import java.util.ArrayList;
import java.util.HashMap;

import models.laboratory.container.instance.Content;
import models.laboratory.sample.instance.Sample;

// Content factory.
public class ContentFactory {

	// Content creation from a sample and a percentage.
	public static Content createContent(Sample sample, double percentage, String projectCode) {
		Content content = new Content();
		content.sampleCode         = sample.code;
		content.sampleTypeCode     = sample.typeCode;
		content.sampleCategoryCode = sample.categoryCode;
		content.projectCode        = projectCode;
		content.percentage         = percentage;
		content.referenceCollab    = sample.referenceCollab;
		content.taxonCode          = sample.taxonCode;
		content.ncbiScientificName = sample.ncbiScientificName;
		content.properties         = new HashMap<>(); // <String,PropertyValue>(); 
		content.processProperties  = new HashMap<>(); // <String,PropertyValue>();
		content.processComments    = new ArrayList<>();
		return content;
	}
	
}
