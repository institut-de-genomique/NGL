package importcns;

import static org.fest.assertions.Assertions.assertThat;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;

import fr.cea.ig.MongoDBDAO;
import models.Constants;
import models.LimsCNSDAO;
import models.laboratory.common.instance.PropertyValue;
import models.laboratory.common.instance.State;
import models.laboratory.common.instance.TBoolean;
import models.laboratory.common.instance.TraceInformation;
import models.laboratory.common.instance.TransientState;
import models.laboratory.common.instance.Valuation;
import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.container.instance.Container;
import models.laboratory.container.instance.Content;
import models.laboratory.run.instance.Lane;
import models.laboratory.run.instance.ReadSet;
import models.laboratory.run.instance.Run;
import models.laboratory.run.instance.Treatment;
import models.laboratory.sample.instance.Sample;
import models.utils.InstanceConstants;
import models.utils.dao.DAOException;
import models.utils.instance.ContainerHelper;
import play.Logger;
import play.api.modules.spring.Spring;
import services.instance.container.UpdateTaraPropertiesCNS;
import services.instance.run.RunImportCNS;
import utils.AbstractTests;
import validation.ContextValidation;

public class RunIlluminaTests extends AbstractTests{


	public static LimsCNSDAO  limsServices = Spring.getBeanOfType(LimsCNSDAO.class);
	

	@AfterClass
	public static  void deleteData() throws DAOException, InstantiationException,
	IllegalAccessException, ClassNotFoundException{


	}

	@BeforeClass
	public static  void initData() throws DAOException, InstantiationException,
	IllegalAccessException, ClassNotFoundException, SQLException {		
		AllTests.initDataRun();
	}

	




	@Test
	public void importRunCNSTest() throws SQLException, DAOException{

		MongoDBDAO.delete(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class,DBQuery.in("code", AllTests.runDelete));
		MongoDBDAO.delete(InstanceConstants.READSET_ILLUMINA_COLL_NAME, Run.class,DBQuery.in("runCode", AllTests.runCodes));
		createRun();
		ContextValidation contextValidation=new ContextValidation(Constants.NGL_DATA_USER);
		String sql="pl_RunToNGL @runhnoms= \'"+StringUtils.join(AllTests.runCodes,",")+"\'";
		Logger.debug("SQL runs to create :"+sql);
		RunImportCNS.createRuns(sql,contextValidation);
		//Import sans erreurs
		contextValidation.displayErrors(Logger.of(this.getClass().getName()));		
		//	Logger.debug("Error import run "+contextValidation.errors.size());
		Assert.assertEquals(contextValidation.errors.size(), 0);

		//Verifie que tous les runs ont été créés
		List<Run> runCreate=MongoDBDAO.find(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class,DBQuery.in("code", AllTests.runCodes)).toList();
		Assert.assertEquals(runCreate.size(),AllTests.runCodes.size());
		for(Run run :runCreate){
			assertThat(run.properties.get("sequencingProgramType")).isNotNull();
		}

		// Pour chaque run verifie la cohérence
		for(Run run :runCreate){
			for(Lane lane  :run.lanes){
				if(lane.readSetCodes!=null){
					for(String readCode:lane.readSetCodes){
						List<ReadSet> readSets=MongoDBDAO.find(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class,DBQuery.is("code", readCode)).toList();
						Assert.assertTrue(readSets.size()==1);
						Assert.assertNotNull(readSets.get(0).sampleOnContainer);
						Assert.assertTrue(readSets.get(0).projectCode!=null);
						Assert.assertTrue(readSets.get(0).sampleCode!=null);
						Assert.assertTrue(readSets.get(0).runSequencingStartDate!=null);
					}
					if(lane.readSetCodes!=null){
						Assert.assertNotNull(lane.treatments.get("ngsrg").results().get("default").get("percentClusterIlluminaFilter"));
						Assert.assertNotNull(lane.treatments.get("ngsrg").results().get("default").get("seqLossPercent"));
						Assert.assertTrue(run.projectCodes.size()>0);
						Assert.assertTrue(run.sampleCodes.size()>0);
					}
				}
			}
		}

	}


	private void createRun() {

		Run run=new Run();
		run.code="140429_FLUOR_H89E9ADXX";
		run.typeCode="RHS2500R";
		run.sequencingStartDate=new Date();
		run.state=new State();
		run.state.code="";
		run.state.user="ngsrg";
		run.state.date=new Date();
		run.containerSupportCode="H89E9ADXX";
		run.dispatch=false;
		run.valuation=new Valuation();

		run.valuation.valid=TBoolean.TRUE;
		run.valuation.date=new Date();
		run.valuation.user="bordelai";
		run.valuation.resolutionCodes=null;
		run.valuation.criteriaCode=null;
		run.valuation.comment="lane 2 : pb : on aurait du deposée les banques BACs ENDs avec une banque equilibre a base car pb identification des clusters pour le sequenceur\nlane 1 : pb d'index pour la bq BFA_PTRCOSW";
		run.properties=new HashMap<String, PropertyValue>();
		run.properties.put("sequencingProgramType",new PropertySingleValue("SR"));
		run.keep=false;
		run.traceInformation=new TraceInformation();
		run.traceInformation.createUser="lims";
		run.treatments.put("sav", new Treatment());
		List<Lane> lanes=new ArrayList<Lane>();
		for(int i=1;i<=8;i++){
			Lane lane=new Lane();
			lane.number=i;
			lane.valuation=new Valuation();
			lane.valuation.valid=TBoolean.TRUE;
			lane.valuation.date=new Date();
			lane.valuation.user="bordelai";
			lane.valuation.resolutionCodes=new HashSet<String>();
			lane.valuation.resolutionCodes.add( "SAV-IndDemultiplex");
			lanes.add(lane);
		}
		run.lanes=lanes;

		MongoDBDAO.save(InstanceConstants.RUN_ILLUMINA_COLL_NAME,run);
	}

	




	
	
	@Test
	public void fusionStateTest(){
		State s1 = new State();
		s1.historical = new HashSet<TransientState>();
		
		TransientState ts1 = new TransientState();
		ts1.code = "IP-RG";
		ts1.date = new Date(1414710000000000L);
		ts1.index = 0;
		ts1.user = "ngsrg";
		s1.historical.add(ts1);
		
		TransientState ts2 = new TransientState();
		ts2.code = "F-RG";
		ts2.date = new Date(1414710000000000L);
		ts2.index = 1;
		ts2.user = "ngsrg";
		s1.historical.add(ts2);
		
		State s2 = new State();
		s2.historical = new HashSet<TransientState>();
		
		TransientState ts3 = new TransientState();
		ts3.code = "N";
		ts3.date = new Date(814710000000000L);
		ts3.index = 0;
		ts3.user = "ngsrg";
		s2.historical.add(ts3);
		
		TransientState ts4 = new TransientState();
		ts4.code = "IP-S";
		ts4.date = new Date(1014710000000000L);
		ts4.index = 1;
		ts4.user = "ngsrg";
		s2.historical.add(ts4);
	
		
		s1 = RunImportCNS.fusionRunStateHistorical(s1,s2);
		
		assertThat(s1.historical.size()).isEqualTo(4);
		
		Iterator<TransientState> iterator = s1.historical.iterator();
		TransientState ts5 = iterator.next();
		
		assertThat(ts5.index).isEqualTo(0);
		assertThat(ts5.code).isEqualTo("N");
		
		ts5 = iterator.next();
		
		assertThat(ts5.index).isEqualTo(1);
		assertThat(ts5.code).isEqualTo("IP-S");
		
		ts5 = iterator.next();
		
		assertThat(ts5.index).isEqualTo(2);
		assertThat(ts5.code).isEqualTo("IP-RG");
		
		ts5 = iterator.next();
		
		assertThat(ts5.index).isEqualTo(3);
		assertThat(ts5.code).isEqualTo("F-RG");
		
		
	}
	
	
	@Test
	public void updateTaraTest() throws SQLException, DAOException{

		Map<String,String> taraSampleCodes=new HashMap<String,String>();
		taraSampleCodes.put("ARD_BLIA","20736");
		//	taraSampleCodes.put("ARC_BLHB","21285");

		List<ReadSet> readSetsBefore=MongoDBDAO.find(InstanceConstants.READSET_ILLUMINA_COLL_NAME,ReadSet.class,DBQuery.in("sampleOnContainer.sampleCode", new ArrayList<String>(taraSampleCodes.keySet()))).toList();
		assertThat(readSetsBefore.size()).isEqualTo(taraSampleCodes.size());
		List<Container> containersBefore=MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME,Container.class,DBQuery.in("sampleCodes", new ArrayList<String>(taraSampleCodes.keySet()))).toList();
		assertThat(containersBefore.size()).isNotEqualTo(0);
		List<Sample> samplesBefore=MongoDBDAO.find(InstanceConstants.SAMPLE_COLL_NAME,Sample.class,DBQuery.in("code", new ArrayList<String>(taraSampleCodes.keySet()))).toList();

		MongoDBDAO.update(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, DBQuery.in("sampleOnContainer.sampleCode", new ArrayList<String>(taraSampleCodes.keySet())),
				DBUpdate.set("sampleOnContainer.properties.taraStation",new PropertySingleValue(23)),true);
		MongoDBDAO.update(InstanceConstants.CONTAINER_COLL_NAME, Container.class, DBQuery.in("contents.sampleCode", new ArrayList<String>(taraSampleCodes.keySet())),
				DBUpdate.set("contents.$.properties.taraStation",new PropertySingleValue(23)),true);
		MongoDBDAO.update(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, DBQuery.in("code", new ArrayList<String>(taraSampleCodes.keySet())),
				DBUpdate.set("properties.taraStation",new PropertySingleValue(23)),true);


		ContextValidation contextValidation=new ContextValidation(Constants.NGL_DATA_USER);
		UpdateTaraPropertiesCNS.updateSampleFromTara(contextValidation, new ArrayList<String>(taraSampleCodes.values()));

		List<ReadSet> readSetsAfter=MongoDBDAO.find(InstanceConstants.READSET_ILLUMINA_COLL_NAME,ReadSet.class
				,DBQuery.in("sampleOnContainer.sampleCode", taraSampleCodes.keySet().toArray())).toList();

		List<Container> containersAfter=MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME,Container.class,DBQuery.in("sampleCodes", new ArrayList<String>(taraSampleCodes.keySet()))).toList();
		assertThat(containersBefore.size()).isNotEqualTo(0);
		List<Sample> samplesAfter=MongoDBDAO.find(InstanceConstants.SAMPLE_COLL_NAME,Sample.class,DBQuery.in("code", new ArrayList<String>(taraSampleCodes.keySet()))).toList();

		assertThat(readSetsBefore.size()).isEqualTo(taraSampleCodes.size());
		for(int i=0;i<readSetsBefore.size();i++){
			//readset validation
			assertThat(readSetsBefore.get(i).sampleOnContainer.sampleCode).isEqualTo(readSetsAfter.get(i).sampleOnContainer.sampleCode);
			assertThat(readSetsBefore.get(i).sampleOnContainer.properties.get("taraStation").value).isEqualTo(readSetsAfter.get(i).sampleOnContainer.properties.get("taraStation").value);				

		}

		for(int i=0;i<samplesBefore.size();i++){
			//sample validation
			assertThat(samplesBefore.get(i).code).isEqualTo(samplesAfter.get(i).code);
			assertThat(samplesBefore.get(i).properties.get("taraStation").value).isEqualTo(samplesAfter.get(i).properties.get("taraStation").value);				
		}

		for(int i=0;i<containersBefore.size();i++){
			//container validation
			assertThat(containersBefore.get(i).code).isEqualTo(containersAfter.get(i).code);
			for(Entry<String,String> entry : taraSampleCodes.entrySet()){

				Set<Content> contentsBefore = ContainerHelper.contentFromSampleCode(containersBefore.get(i).contents,entry.getKey());
				Set<Content> contentsAfter = ContainerHelper.contentFromSampleCode(containersBefore.get(i).contents,entry.getKey());

				for(Content contentB : contentsBefore){
					for(Content contentA:contentsAfter){
						assertThat(contentB.properties.get("taraStation").value).isEqualTo(contentA.properties.get("taraStation").value);
					}

				}
			}
		}

	}

}
