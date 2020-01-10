package controllers.migration.models.container;

import java.util.HashMap;
import java.util.Map;

import models.laboratory.common.instance.PropertyValue;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class ContentOld {

		
		public SampleUsedOld sampleUsed;
		
		// Necessary if not contentType ? Name ?
		// Need to propagate useful properties
		public Map<String,PropertyValue> properties;
		
		public ContentOld(){
			properties=new HashMap<String, PropertyValue>();
		}
		
		@JsonIgnore
		public ContentOld(SampleUsedOld sampleUsed){
			properties=new HashMap<String, PropertyValue>();
			this.sampleUsed=sampleUsed;
		}


	}

