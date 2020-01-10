package models.utils.instance;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

import models.laboratory.common.instance.PropertyValue;
import models.laboratory.common.instance.State;
import models.laboratory.common.instance.TBoolean;
import models.laboratory.common.instance.TraceInformation;
import models.laboratory.common.instance.Valuation;
import models.laboratory.container.description.ContainerSupportCategory;
import models.laboratory.container.instance.ContainerSupport;
import models.laboratory.container.instance.LocationOnContainerSupport;
import models.utils.dao.DAOException;
import play.Logger;

public class ContainerSupportHelper {

	public static LocationOnContainerSupport getContainerSupportTube(String containerSupportCode){
		LocationOnContainerSupport containerSupport=new LocationOnContainerSupport();
		containerSupport.code=containerSupportCode;	
		containerSupport.categoryCode="tube";
		containerSupport.column="1";
		containerSupport.line="1";
		
		return containerSupport;
	}

	// FDS 13/10/2015 ajouter param string storageCode
	public static LocationOnContainerSupport getContainerSupport(
			String containerCategoryCode, int nbUsableContainer, String containerSupportCode, String x, String y, String storageCode) throws DAOException {

		List<ContainerSupportCategory> containerSupportCategories=ContainerSupportCategory.find.get().findByContainerCategoryCode(containerCategoryCode);

		LocationOnContainerSupport containerSupport=new LocationOnContainerSupport();

		for(int i=0;i<containerSupportCategories.size();i++){
			if(containerSupportCategories.get(i).nbUsableContainer==nbUsableContainer){
				containerSupport.categoryCode=containerSupportCategories.get(i).code;
			}
		}

		if(containerSupport.categoryCode==null){
			containerSupport.categoryCode=containerSupportCategories.get(0).code;
		}

		containerSupport.code=containerSupportCode;	
		containerSupport.column=x;
		containerSupport.line=y;
		
		if ( storageCode != null ) {
			containerSupport.storageCode=storageCode;
			//Logger.debug ("1) getContainerSupport; support "+ containerSupportCode+": storageCode= "+ storageCode);
		}else {
			// normal ou pas qu'il n'y ait pas de storage code  ??
			Logger.warn("storage code null for support code = "+containerSupportCode);
		}
		
		return containerSupport;
	}
	

	//FDS 13/10/2015 recreer une methode avec la meme signature
	public static LocationOnContainerSupport getContainerSupport(
				String containerCategoryCode, int nbUsableContainer, String containerSupportCode, String x, String y) throws DAOException {
		return getContainerSupport( containerCategoryCode, nbUsableContainer, containerSupportCode, x, y, null);
	}
	

	public static ContainerSupport createContainerSupport(String containerSupportCode, 
			                                              PropertyValue sequencingProgramType, 
			                                              String categoryCode, 
			                                              String user) {
		ContainerSupport s = new ContainerSupport(); 

		s.code         = containerSupportCode;	
		s.categoryCode = categoryCode;

		s.state = new State(); 
		s.state.code = "N"; // default value
		s.state.user = user;
		s.state.date = new Date();

		s.traceInformation = new TraceInformation(); 
		s.traceInformation.setTraceInformation(user);
		s.valuation = new Valuation();
		
		s.valuation.valid = TBoolean.UNSET;

		if (sequencingProgramType != null) {
			HashMap<String, PropertyValue> prop = new HashMap<>(); // <String, PropertyValue>();
			prop.put("sequencingProgramType", sequencingProgramType);
			s.properties = prop;
		}

		return s;
	}
		
	
	
}
