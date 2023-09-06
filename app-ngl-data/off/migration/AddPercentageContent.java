package controllers.migration;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.container.instance.Container;
import models.laboratory.container.instance.Content;
import models.utils.InstanceConstants;
import models.utils.instance.ContainerHelper;

import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;

import play.Logger;
import play.mvc.Result;

import com.mongodb.MongoException;

import controllers.CommonController;
import fr.cea.ig.MongoDBDAO;

public class AddPercentageContent extends CommonController{

	private static final Date date = new Date();
	private static final DateFormat df = new SimpleDateFormat("ddMMyy'_'hhmm");
	private static final String today = df.format(date);
	private static final String CONTAINER_COLL_NAME_BCK = InstanceConstants.CONTAINER_COLL_NAME+"_BCK"+"_"+today;


	public static Result migration() {

		List<Container> containersCollBck = MongoDBDAO.find(CONTAINER_COLL_NAME_BCK, Container.class).toList();
		if(containersCollBck.size() == 0){

			Logger.info(">>>>>>>>>>> Migration PercentageContents in Containers starts");

			backupContainerCollection();

			migrePercentageContentsInContainers();

			Logger.info(">>>>>>>>>>> Migration PercentageContents in Containers end");
		} else {
			Logger.info(">>>>>>>>>>> Migration PercentageContents in Containers already execute !");
		}
		Logger.info(">>>>>>>>>>> Migration PercentageContents in Containers finish");
		return ok(">>>>>>>>>>> Migration PercentageContents in Containers finish");
	}


	private static void migrePercentageContentsInContainers(){

		List<Container> containers = MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, Container.class).toList();

		Logger.debug("Migre "+containers.size()+" CONTAINERS");

		for(Container container: containers){	

			int contentsArraySize = container.contents.size();
			//Logger.info("Container: code="+container.code+", "+contentsArraySize+" contents");

			Double equiPercent = ContainerHelper.getEquiPercentValue(contentsArraySize);

			Iterator<Content> itr =  container.contents.iterator();
			for(int i=0;i<contentsArraySize;i++){ 

				Content content = itr.next();
				//Content content = container.contents.get(i);

				if(content.properties.containsKey("percentPerLane")){						
					PropertySingleValue field =(PropertySingleValue) content.properties.get("percentPerLane");
					BigDecimal bg = new BigDecimal(field.value.toString()).setScale(2, RoundingMode.HALF_UP);
					content.percentage= bg.doubleValue();					
					if(content.percentage==0.00){											
						content.percentage= equiPercent;
					}
				}else{	
					content.percentage= equiPercent;
				}
				
			}			
				try{
					MongoDBDAO.update(InstanceConstants.CONTAINER_COLL_NAME, Container.class,
							DBQuery.is("code", container.code),
							DBUpdate.set("contents", container.contents));
				}catch(MongoException e) {
					Logger.error("MongoException type error !");
				}			
		}
	}


	private static void backupContainerCollection() {
		Logger.info("\tCopie "+InstanceConstants.CONTAINER_COLL_NAME+" start");
		MongoDBDAO.save(CONTAINER_COLL_NAME_BCK, MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, Container.class).toList());
		Logger.info("\tCopie "+InstanceConstants.CONTAINER_COLL_NAME+" end");
	}

}
