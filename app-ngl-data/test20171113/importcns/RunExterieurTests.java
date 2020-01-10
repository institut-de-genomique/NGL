package importcns;

import static org.fest.assertions.Assertions.assertThat;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import models.Constants;
import models.laboratory.container.instance.Container;
import models.laboratory.container.instance.ContainerSupport;
import models.laboratory.run.instance.Run;
import models.utils.InstanceConstants;
import models.utils.dao.DAOException;

import org.apache.commons.lang3.StringUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mongojack.DBQuery;

import play.Logger;

import fr.cea.ig.MongoDBDAO;
import services.instance.container.ContainerImportCNS;
import services.instance.project.ProjectImportCNS;
import services.instance.run.RunImportCNS;
import utils.AbstractTests;
import validation.ContextValidation;

public class RunExterieurTests extends AbstractTests {

	
	
	@AfterClass
	public static  void deleteData() throws DAOException, InstantiationException,
	IllegalAccessException, ClassNotFoundException{


	}

	@BeforeClass
	public static  void initData() throws DAOException, InstantiationException,
	IllegalAccessException, ClassNotFoundException, SQLException {		
		AllTests.initDataRunExt();
	}
		
	@Test 
	public void runExterieurTest() throws SQLException, DAOException{
		MongoDBDAO.delete(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class,DBQuery.in("code", AllTests.runExtCodes));
		MongoDBDAO.delete(InstanceConstants.READSET_ILLUMINA_COLL_NAME, Run.class,DBQuery.in("runCode", AllTests.runExtCodes));

		ContextValidation contextValidation=new ContextValidation(Constants.NGL_DATA_USER);
		String sql="pl_RunExtToNGL @runhnoms= \'"+StringUtils.join(AllTests.runExtCodes,",")+"\'";
		Logger.debug("SQL runs to create :"+sql);
		RunImportCNS.createRuns(sql,contextValidation);
		
		
		contextValidation.displayErrors(Logger.of(this.getClass().getName()));		
		Assert.assertEquals(contextValidation.errors.size(), 0);

		//Verifie que tous les runs ont été créés
		List<Run> runCreate=MongoDBDAO.find(InstanceConstants.RUN_ILLUMINA_COLL_NAME, Run.class,DBQuery.in("code", AllTests.runExtCodes)).toList();
		Assert.assertEquals(runCreate.size(),AllTests.runExtCodes.size());

	}
}
