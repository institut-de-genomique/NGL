package importcns;

import static org.fest.assertions.Assertions.assertThat;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import models.Constants;
import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.container.instance.Container;
import models.laboratory.container.instance.ContainerSupport;
import models.laboratory.container.instance.Content;
import models.laboratory.run.instance.ReadSet;
import models.laboratory.sample.instance.Sample;
import models.utils.InstanceConstants;
import models.utils.dao.DAOException;

import org.apache.commons.lang3.StringUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;

import play.Logger;
import play.Logger.ALogger;
import services.instance.container.ContainerImportCNS;
import services.instance.container.UpdateAmpliCNS;
import services.instance.container.UpdateSolutionStockCNS;
import services.instance.sample.UpdateSampleCNS;
import utils.AbstractTests;
import validation.ContextValidation;
import fr.cea.ig.MongoDBDAO;

public class ContainerTests extends AbstractTests {

	ALogger logger=Logger.of("ContainerTests");

	@BeforeClass
	public static  void initData() throws DAOException, InstantiationException,
	IllegalAccessException, ClassNotFoundException, SQLException {		
		AllTests.initDataRun();
		AllTests.initDataRunExt();
	}


	@AfterClass
	public static  void deleteData()  {		
		//Delete sample for next unit test importSolutionStockTest
		MongoDBDAO.deleteByCode(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, "AXD_TQBA");
		MongoDBDAO.deleteByCode(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, "AXD_TQAA");
	}
	//@Test 
	public void importPrepaflowcellTest() throws SQLException, DAOException{

		ContextValidation contextValidation=new ContextValidation(Constants.NGL_DATA_USER);
		String sql="pl_PrepaflowcellToNGL @flowcellNoms=\'"+StringUtils.join(AllTests.prepaCodes,",")+"\'";
		ContainerImportCNS.createContainers(contextValidation,sql,"lane","F","prepa-flowcell","pl_BanquesolexaUneLane @nom_lane=?");
		Assert.assertEquals(contextValidation.errors.size(),0);
		List<Container> containers=MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, Container.class,DBQuery.in("support.code", AllTests.prepaCodes)).toList();
		for(Container container:containers){
			Content cnt = container.contents.iterator().next();
			assertThat(cnt.properties.get("libLayoutNominalLength")).isNotNull();
			assertThat(cnt.properties.get("libProcessTypeCode")).isNotNull();
			assertThat(cnt.properties.get("sequencingProgramType")).isNull();
			assertThat(cnt.properties.get("percentPerLane")).isNotNull();
		}
		Assert.assertTrue(containers.size()>0);
		List<ContainerSupport> containerSupports=MongoDBDAO.find(InstanceConstants.CONTAINER_SUPPORT_COLL_NAME, ContainerSupport.class,DBQuery.in("code", AllTests.prepaCodes)).toList();
		for(ContainerSupport containerSupport:containerSupports){
			assertThat(containerSupport.properties.get("sequencingProgramType")).isNotNull();
		}
		Assert.assertEquals(containerSupports.size(), AllTests.prepaCodes.size());
	}




	//@Test 
	public void importPrepaflowcellExtTest() throws SQLException, DAOException{

		ContextValidation contextValidation=new ContextValidation(Constants.NGL_DATA_USER);
		String sql="pl_PrepaflowcellExtToNGL @runhnoms=\'"+StringUtils.join(AllTests.runExtCodes,",")+"\'";
		ContainerImportCNS.createContainers(contextValidation,sql,"lane","F","prepa-flowcell",null);
		Assert.assertEquals(contextValidation.errors.size(),0);
		List<Container> containers=MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, Container.class,DBQuery.in("support.code", AllTests.prepaExtCodes)).toList();
		for(Container container:containers){
			Content cnt = container.contents.iterator().next();
			assertThat(cnt.properties.get("libLayoutNominalLength")).isNull();
			assertThat(cnt.properties.get("libProcessTypeCode")).isNotNull();
			assertThat(cnt.percentage).isNotNull();
		}
		Assert.assertTrue(containers.size()>0);
		List<ContainerSupport> containerSupports=MongoDBDAO.find(InstanceConstants.CONTAINER_SUPPORT_COLL_NAME, ContainerSupport.class,DBQuery.in("code", AllTests.prepaExtCodes)).toList();
		Assert.assertEquals(containerSupports.size(), AllTests.prepaExtCodes.size());

	}


	/*
	@Test 	
	public void importSolutionStockTest() throws SQLException, DAOException{

		ContextValidation contextValidation=new ContextValidation(Constants.NGL_DATA_USER);
		List<String> solutionStocks=new ArrayList<String>();

		solutionStocks.add("AXD_msCCH_d1");
		//Creation 
		String sql="pl_SolutionStockToNGL @noms=\'"+StringUtils.join(solutionStocks,",")+"\'";

		ContainerImportCNS.createContainers(contextValidation,sql,"tube","F","solution-stock","pl_ContentFromContainer @matmanom=?");

		contextValidation.displayErrors(logger);
		Assert.assertEquals(contextValidation.errors.size(),0);

		List<Container> containers=MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, Container.class,DBQuery.in("support.code", solutionStocks)).toList();
		Assert.assertTrue(containers.size()>0);

		List<ContainerSupport> containerSupports=MongoDBDAO.find(InstanceConstants.CONTAINER_SUPPORT_COLL_NAME, ContainerSupport.class,DBQuery.in("code", solutionStocks)).toList();
		Assert.assertEquals(containerSupports.size(),solutionStocks.size());

		for(Container container:containers){
			Content cnt = container.contents.iterator().next();
			assertThat(cnt.properties.get("libLayoutNominalLength")).isNotNull();
			assertThat(cnt.referenceCollab).isNotNull();
			assertThat(cnt.properties.get("libProcessTypeCode")).isNotNull();
			assertThat(cnt.properties.get("percentPerLane")).isNull();
			assertThat(((PropertySingleValue) container.concentration).unit).isEqualTo("nM");
			assertThat(cnt.percentage).isNotNull();
			for(ContainerSupport support:containerSupports){
				if(support.code.equals(container.support.code)){
					assertThat(container.state.code).isEqualTo(support.state.code);
				}
			}
			//Get sample
			Sample sample = MongoDBDAO.findByCode(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, cnt.sampleCode);
			Logger.debug("Sample "+sample.code+" taxon "+sample.taxonCode);
			assertThat(sample).isNotNull();
			Logger.debug("Sample ncbiLineage "+sample.ncbiLineage+" scientific name "+sample.ncbiScientificName);
			assertThat(sample.ncbiLineage).isNotNull();
			assertThat(sample.ncbiScientificName).isNotNull();
		}

		MongoDBDAO.update(InstanceConstants.CONTAINER_COLL_NAME, Container.class, DBQuery.in("code",solutionStocks), DBUpdate.set("state.code","IW-P"));

		//En reserve update state container
		UpdateSolutionStockCNS.updateSolutionStock(sql + ", @updated=1", contextValidation,"tube","solution-stock");
		contextValidation.displayErrors(logger);
		Assert.assertEquals(contextValidation.errors.size(),0);

		containers=MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, Container.class,DBQuery.in("support.code", solutionStocks)).toList();
		Assert.assertTrue(containers.size()>0);

		for(Container container:containers){
			assertThat(container.state.code).isNotEqualTo("IW-P");
			assertThat(container.state.code).isEqualTo("IS");

		}


	}
*/

	
	/*
	@Test 
	public void importBanqueAmpliTest() throws SQLException, DAOException{

		ContextValidation contextValidation=new ContextValidation(Constants.NGL_DATA_USER);
		List<String> banqueAmpli=new ArrayList<String>();

		banqueAmpli.add("AXD_msCCH_d1");
		//Creation 
		String sql="pl_BanqueAmpliToNGL @noms=\'"+StringUtils.join(banqueAmpli,",")+"\'";

		ContainerImportCNS.createContainers(contextValidation,sql,"tube","F","amplification","pl_ContentFromContainer @matmanom=?");

		contextValidation.displayErrors(logger);
		Assert.assertEquals(contextValidation.errors.size(),0);

		List<Container> containers=MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, Container.class,DBQuery.in("support.code", banqueAmpli)).toList();
		Assert.assertTrue(containers.size()>0);

		List<ContainerSupport> containerSupports=MongoDBDAO.find(InstanceConstants.CONTAINER_SUPPORT_COLL_NAME, ContainerSupport.class,DBQuery.in("code", banqueAmpli)).toList();
		Assert.assertEquals(containerSupports.size(),banqueAmpli.size());

		for(Container container:containers){
			Content cnt = container.contents.iterator().next();
			assertThat(cnt.properties.get("libLayoutNominalLength")).isNotNull();
			assertThat(cnt.referenceCollab).isNotNull();
			assertThat(cnt.properties.get("libProcessTypeCode")).isNotNull();
			assertThat(cnt.properties.get("percentPerLane")).isNull();
			assertThat(((PropertySingleValue) container.concentration).unit).isEqualTo("nM");
			assertThat(cnt.percentage).isNotNull();
			for(ContainerSupport support:containerSupports){
				if(support.code.equals(container.support.code)){
					assertThat(container.state.code).isEqualTo(support.state.code);
				}
			}
			//Get sample
			Sample sample = MongoDBDAO.findByCode(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, cnt.sampleCode);
			Logger.debug("Sample "+sample.code+" taxon "+sample.taxonCode);
			assertThat(sample).isNotNull();
			Logger.debug("Sample ncbiLineage "+sample.ncbiLineage+" scientific name "+sample.ncbiScientificName);
			assertThat(sample.ncbiLineage).isNotNull();
			assertThat(sample.ncbiScientificName).isNotNull();
		}

		MongoDBDAO.update(InstanceConstants.CONTAINER_COLL_NAME, Container.class, DBQuery.in("code",banqueAmpli), DBUpdate.set("state.code","IW-P"));

		//En reserve update state container
		UpdateAmpliCNS.updateAmpliCNS(sql + ", @updated=1", contextValidation,"tube","amplification");
		contextValidation.displayErrors(logger);
		Assert.assertEquals(contextValidation.errors.size(),0);

		containers=MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, Container.class,DBQuery.in("support.code", banqueAmpli)).toList();
		Assert.assertTrue(containers.size()>0);

		for(Container container:containers){
			assertThat(container.state.code).isNotEqualTo("IW-P");
			assertThat(container.state.code).isEqualTo("IS");

		}


	}

	
	//@Test
	public void updateSampleTest() throws SQLException, DAOException{

		List<ReadSet> readSetsBefore=MongoDBDAO.find(InstanceConstants.READSET_ILLUMINA_COLL_NAME,ReadSet.class,DBQuery.in("sampleOnContainer.sampleCode", AllTests.sampleCodes)).toList();
		assertThat(readSetsBefore.size()).isEqualTo(AllTests.sampleCodes.size());
		List<Container> containersBefore=MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME,Container.class,DBQuery.in("sampleCodes", AllTests.sampleCodes)).toList();
		assertThat(containersBefore.size()).isNotEqualTo(0);
		List<Sample> samplesBefore=MongoDBDAO.find(InstanceConstants.SAMPLE_COLL_NAME,Sample.class,DBQuery.in("code", AllTests.sampleCodes)).toList();

		MongoDBDAO.update(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, DBQuery.in("sampleOnContainer.sampleCode", AllTests.sampleCodes),
				DBUpdate.set("sampleOnContainer.properties.taxonSize",new PropertySingleValue(23)),true);
		MongoDBDAO.update(InstanceConstants.CONTAINER_COLL_NAME, Container.class, DBQuery.in("contents.sampleCode", AllTests.sampleCodes),
				DBUpdate.set("contents.$.properties.taxonSize",new PropertySingleValue(23)),true);
		MongoDBDAO.update(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, DBQuery.in("code", AllTests.sampleCodes),
				DBUpdate.set("properties.taxonSize",new PropertySingleValue(23)),true);


		ContextValidation contextValidation=new ContextValidation(Constants.NGL_DATA_USER);
		UpdateSampleCNS.updateSampleFromTara(contextValidation, AllTests.sampleCodes);
		List<Sample> samples=MongoDBDAO.find(InstanceConstants.SAMPLE_COLL_NAME,Sample.class,DBQuery.in("code",AllTests.sampleCodes)).toList();

		assertThat(samples.size()).isEqualTo(AllTests.sampleCodes.size());


	}
	*/
}
