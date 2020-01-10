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

public class ContainerTests extends AbstractTests {


	protected static ALogger logger=Logger.of("ContainerTest");
	
/**********************************Tests of ContainerHelper class methods (DAO Helper)***************************************************/	
	@Test
	public void validateCalculPercentageContent() {
		ContextValidation contextValidation = new ContextValidation(Constants.TEST_USER);
		Container cnt =  ContainerTestHelper.getFakeContainer();
				
		//good value
		Content c1 = new Content();
		c1.percentage = 2.00;	
		cnt.contents.add(c1);		
		
		//null value
		Content c2 = new Content();
		c2.percentage = null;
		cnt.contents.add(c2);
		
		//empty value
		Content c3 = new Content();			
		cnt.contents.add(c3);
		
		//Float value greater than 100
		Content c4 = new Content();
		c4.percentage = 300.0;	
		cnt.contents.add(c4);
		
		//Float value less than 0		
		Content c5 = new Content();
		c5.percentage = 40.99;	
		cnt.contents.add(c5);
		
		//Float value between 0 and 1
		Content c6 = new Content();
		c6.percentage = 0.45;	
		cnt.contents.add(c6);
		
		Content c7 = new Content();
		c7.percentage = 0.03;	
		cnt.contents.add(c7);		
		
		//good given ContentPercentage
		ContainerHelper.calculPercentageContent(cnt.contents, 80.0);
		contextValidation.displayErrors(logger);
		assertThat(c1.percentage).isEqualTo(2.00);
		assertThat(c2.percentage).isEqualTo(80.0);
		assertThat(c3.percentage).isEqualTo(80.0);
		assertThat(c4.percentage).isEqualTo(240.0);	
		assertThat(c5.percentage).isEqualTo(32.79);
		assertThat(c6.percentage).isEqualTo(0.36);
		assertThat(c7.percentage).isEqualTo(0.02);
				
		//Big Percentage Content
		c1.percentage = 10.0;
		c2.percentage = null;
		Content c8 = new Content();
		cnt.contents.add(c8);
		c5.percentage = 0.94;
		c6.percentage = 0.98;		
		ContainerHelper.calculPercentageContent(cnt.contents, 380.0);
		contextValidation.displayErrors(logger);
		assertThat(c1.percentage).isEqualTo(38.0);
		assertThat(c2.percentage).isEqualTo(380.0);
		assertThat(c8.percentage).isEqualTo(380.0);
		assertThat(c5.percentage).isEqualTo(3.57);
		assertThat(c6.percentage).isEqualTo(3.72);
		
		//Zero Percentage Content
		c1.percentage = 10.0;
		c2.percentage = null;
		Content c9 = new Content();
		cnt.contents.add(c9);
		c5.percentage = 0.94;
		c6.percentage = 0.98;
		ContainerHelper.calculPercentageContent(cnt.contents, 0.00);
		contextValidation.displayErrors(logger);
		assertThat(c1.percentage).isEqualTo(0.00);
		assertThat(c2.percentage).isEqualTo(0.00);
		assertThat(c9.percentage).isEqualTo(0.00);
		assertThat(c5.percentage).isEqualTo(0.00);
		assertThat(c6.percentage).isEqualTo(0.00);		
	}
	
	@Test
	public void validateAddContent() {
		
	}
	
	
	@Test
	public void validateFusionContent(){
		List<Content> contents = new ArrayList<Content>();
		
		Content c1 = new Content();
		c1.projectCode ="P1";
		c1.sampleCode = "S1";
		c1.percentage = 0.10d;
		c1.properties.put("p1", new PropertySingleValue("1"));
		c1.properties.put("p2", new PropertySingleValue("2"));
		c1.properties.put("p3", new PropertySingleValue("3"));
		
		contents.add(c1);
		
		Content c2= new Content();
		c2.projectCode ="P1";
		c2.sampleCode = "S2";
		c2.percentage = 0.25d;
		c2.properties.put("p1", new PropertySingleValue("1"));
		c2.properties.put("p2", new PropertySingleValue("2"));
		c2.properties.put("p3", new PropertySingleValue("3"));
		contents.add(c2);
		
		Content c3 = new Content();
		c3.projectCode ="P2";
		c3.sampleCode = "S3";
		c3.percentage = 0.30d;
		c3.properties.put("p1", new PropertySingleValue("1"));
		c3.properties.put("p2", new PropertySingleValue("2.1"));
		c3.properties.put("p3", new PropertySingleValue("3.0"));
		contents.add(c3);
		
		Content c4 = new Content();
		c4.projectCode ="P2";
		c4.sampleCode = "S3";
		c4.percentage = 0.35d;
		c4.properties.put("p1", new PropertySingleValue("1"));
		c4.properties.put("p2", new PropertySingleValue("2.2"));		
		contents.add(c4);
		
		contents = ContainerHelper.fusionContents(contents);
		
		assertThat(contents.size()).isEqualTo(3);
		
		for(Content c : contents){
			
			if("S3".equals(c.sampleCode)){
				assertThat(c.percentage).isEqualTo(0.65);
				assertThat(c.properties.size()).isEqualTo(2);
				assertThat(c.properties.get("p1")).isNotNull();
				assertThat(c.properties.get("p3")).isNotNull();
				assertThat(c.properties.get("p2")).isNull();
			}
			
		}
		
		contents = new ArrayList<Content>();
		Content c5 = new Content();
		c5.projectCode ="P1";
		c5.sampleCode = "S1";
		c5.percentage = 0.50d;
		c5.properties.put("tag", new PropertySingleValue("IND8"));
		c5.properties.put("p1", new PropertySingleValue("1"));
		c5.properties.put("p2", new PropertySingleValue("2"));
		c5.properties.put("p3", new PropertySingleValue("3"));
		
		contents.add(c5);
		
		Content c6= new Content();
		c6.projectCode ="P1";
		c6.sampleCode = "S1";
		c6.percentage = 0.50d;
		c5.properties.put("tag", new PropertySingleValue("IND10"));
		c6.properties.put("p1", new PropertySingleValue("4"));
		c6.properties.put("p2", new PropertySingleValue("5"));
		c6.properties.put("p3", new PropertySingleValue("6"));
		contents.add(c6);
		
		contents = ContainerHelper.fusionContents(contents);
		
		assertThat(contents.size()).isEqualTo(2);
		
		System.out.println("TTTT"+null);
		
	}
/**********************************Tests of Container class methods (DBObject)***************************************************/		
	
	@Test
	public void validateGetCurrentProcesses() {		
		Container cnt =  ContainerTestHelper.getFakeContainer("tube");
		cnt.processCodes=new HashSet<String>();
		Process process=new Process();
		process.code="validateGetCurrentProcesses";
		Process p=MongoDBDAO.save(InstanceConstants.PROCESS_COLL_NAME,process);
		cnt.processCodes.add("validateGetCurrentProcesses");
		List<Process> processes =  getCurrentProcesses(cnt);
		MongoDBDAO.delete(InstanceConstants.PROCESS_COLL_NAME, p);		
		assertThat(processes).isNotNull().isNotEmpty();		
	}
	
	@Test
	public void validateGetNullCurrentProcesses() {		
		Container cnt =  ContainerTestHelper.getFakeContainer();
		cnt.processCodes = null;
		assertThat(getCurrentProcesses(cnt)).isNullOrEmpty();				
	}
	
	public List<Process> getCurrentProcesses(Container c) {
		List<Process> processes=new ArrayList<Process>();
		if(c.processCodes!=null){
			processes= MongoDBDAO.find(InstanceConstants.PROCESS_COLL_NAME, Process.class, DBQuery.in("code",c.processCodes)).toList();
		}
		return processes;
		
	}
	

/**********************************Tests of Containers class methods (Controller)***************************************************/	
	
	
	/*
	@Test
	public void test() {
		
	}
	*/

}
