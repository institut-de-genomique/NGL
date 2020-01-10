package importcns;

import java.sql.SQLException;

import models.Constants;
import models.LimsCNSDAO;
import models.laboratory.parameter.index.Index;
import models.utils.InstanceConstants;
import models.utils.dao.DAOException;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import play.api.modules.spring.Spring;
import services.instance.parameter.IndexImportCNS;
import utils.AbstractTests;
import validation.ContextValidation;
import fr.cea.ig.MongoDBDAO;

public class IndexIlluminaTests extends AbstractTests{

	protected static LimsCNSDAO  limsServices = Spring.getBeanOfType(LimsCNSDAO.class);

	@AfterClass
	public static  void deleteData() throws DAOException, InstantiationException,
	IllegalAccessException, ClassNotFoundException{
	}

	@BeforeClass
	public static  void initData() throws DAOException, InstantiationException,
	IllegalAccessException, ClassNotFoundException, SQLException {
		MongoDBDAO.getCollection(InstanceConstants.PARAMETER_COLL_NAME, Index.class).drop();

	}

	@Test
	public void importIndex() throws SQLException, DAOException{
		ContextValidation contextValidation=new ContextValidation(Constants.NGL_DATA_USER);
		IndexImportCNS.createIndexIllumina(limsServices,contextValidation);
		Assert.assertEquals(contextValidation.errors.size(),0);
	}


}
