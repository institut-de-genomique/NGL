package controllers.migration;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;

import controllers.CommonController;
import fr.cea.ig.MongoDBDAO;
import models.laboratory.common.instance.property.PropertyListValue;
import models.laboratory.container.instance.Container;
import models.laboratory.container.instance.ContainerSupport;
import models.laboratory.container.instance.Content;
import models.laboratory.run.instance.Run;
import models.utils.InstanceConstants;
import play.Logger;
import play.mvc.Result;

public class MigrationPropSampleAliquoteCode extends CommonController{

	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmm");

	public static Result migration()
	{
		List<Run> runs = MongoDBDAO.find(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, DBQuery.exists("code")).toList();
		backUpRun(runs);

		for(Run run : runs){
			ContainerSupport cs = MongoDBDAO.findByCode(InstanceConstants.CONTAINER_SUPPORT_COLL_NAME, ContainerSupport.class, run.containerSupportCode);
			if(null != cs){

				List<Container> containers = MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, Container.class, DBQuery.is("support.code", run.containerSupportCode)).toList();

				Set<String> sampleAliquoteCodes = new TreeSet<String>();
				for(Container container:containers){
					for(Content content:container.contents){
						if(content.properties.containsKey("sampleAliquoteCode")){
							sampleAliquoteCodes.add((String)(content.properties.get("sampleAliquoteCode").value));
						}
					}
				}
				if(sampleAliquoteCodes.size() > 0){
					run.properties.put("sampleAliquoteCodes", new PropertyListValue(new ArrayList(sampleAliquoteCodes)));
				}

				MongoDBDAO.update(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class, 
						DBQuery.is("code", run.code),
						DBUpdate.set("properties", run.properties));
			}else{
				Logger.error("ContainerSupport null for "+run.containerSupportCode);
			}
		}
		return ok();
	}

	private static void backUpRun(List<Run> runs)
	{
		String backupName = InstanceConstants.RUN_ILLUMINA_COLL_NAME+"_BCK_NGL916_"+sdf.format(new java.util.Date());
		Logger.info("\tCopie "+InstanceConstants.RUN_ILLUMINA_COLL_NAME+" to "+backupName+" start");

		MongoDBDAO.save(backupName, runs);
		Logger.info("\tCopie "+InstanceConstants.RUN_ILLUMINA_COLL_NAME+" to "+backupName+" end");

	}
}
