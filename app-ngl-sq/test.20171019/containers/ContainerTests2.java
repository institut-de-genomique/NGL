package containers;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import models.laboratory.common.instance.PropertyValue;
import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.container.instance.Container;
import models.laboratory.container.instance.Content;
import models.laboratory.processes.instance.Process;
import models.laboratory.project.instance.Project;
import models.utils.CodeHelper;
import models.utils.InstanceConstants;
import models.utils.instance.ContainerHelper;

import org.junit.Test;
import org.mongojack.DBQuery;

import com.fasterxml.jackson.annotation.JsonIgnore;

import play.Logger;
import play.Logger.ALogger;
import play.modules.jongo.MongoDBPlugin;
import utils.AbstractTests;
import utils.Constants;
import validation.ContextValidation;
import fr.cea.ig.MongoDBDAO;

public class ContainerTests2 extends AbstractTests {


	
	@Test
	public void generateSampleCode() throws InterruptedException{
		Project project = new Project();
		project.code = "TST";
		project.nbCharactersInSampleCode = 4;
		MongoDBDAO.save(InstanceConstants.PROJECT_COLL_NAME, project);
		
		for(int i = 0; i < 120; i++){
			project = MongoDBDAO.findByCode(InstanceConstants.PROJECT_COLL_NAME, Project.class, "TST");
			System.out.println(CodeHelper.getInstance().generateSampleCode(project, true));			
		}
		
		MongoDBDAO.deleteByCode(InstanceConstants.PROJECT_COLL_NAME, Project.class, "TST");
		
	}
/**********************************Tests of Containers class methods (Controller)***************************************************/	
	
	
	/*
	@Test
	public void test() {
		
	}
	*/

}
