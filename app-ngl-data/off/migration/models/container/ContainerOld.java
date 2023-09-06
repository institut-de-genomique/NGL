package controllers.migration.models.container;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.cea.ig.DBObject;

import models.laboratory.common.instance.Comment;
import models.laboratory.common.instance.PropertyValue;
import models.laboratory.common.instance.State;
import models.laboratory.common.instance.TraceInformation;
import models.laboratory.common.instance.Valuation;
import models.laboratory.container.instance.QualityControlResult;

public class ContainerOld extends DBObject {

		//ContainerCategory Ref
		public String categoryCode;

		public State state;
		public Valuation valuation;

		// Container informations
		public TraceInformation traceInformation;
		public Map<String, PropertyValue> properties;
		public List<Comment> comments;

		//Relation with support
		public LocationOnContainerSupportOld support; 

		//Embedded content with values;
		public List<ContentOld> contents;

		// Embedded QC result, this data are copying from collection QC
		public List<QualityControlResult> qualityControlResults;

		//Stock management 
		public PropertyValue mesuredVolume;
		public PropertyValue mesuredConcentration;
		public PropertyValue mesuredQuantity;

		public List<PropertyValue> calculedVolume;

		// For search optimisation
		public List<String> projectCodes; // getProjets //TODO SET instead of LIST
		public List<String> sampleCodes; // getSamples //TODO SET instead of LIST
		// ExperimentType must be an internal or external experiment ( origine )
		// List for pool experimentType
		public List<String> fromExperimentTypeCodes; // getExperimentType

		// Propager au container de purif ??
		//public String fromExperimentCode; ??
		//public String fromExtractionTypeCode;
		//process
		public String processTypeCode; 

		public List<String> inputProcessCodes;

		public ContainerOld(){
			properties=new HashMap<String, PropertyValue>();
			traceInformation=new TraceInformation();
		}

}
