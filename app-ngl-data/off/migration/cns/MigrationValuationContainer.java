package controllers.migration.cns;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import models.LimsCNSDAO;
import models.laboratory.common.instance.TBoolean;
import models.laboratory.common.instance.Valuation;
import models.laboratory.container.instance.Container;
import models.utils.InstanceConstants;

import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;
import org.springframework.jdbc.core.RowMapper;

import play.Logger;
import play.Logger.ALogger;
import play.api.modules.spring.Spring;
import play.mvc.Result;
import controllers.CommonController;
import fr.cea.ig.MongoDBDAO;

public class MigrationValuationContainer extends CommonController{
	
	protected static ALogger logger=Logger.of("MigrationUpdateSupportPlaque");
	protected static LimsCNSDAO  limsServices = Spring.getBeanOfType(LimsCNSDAO.class);


	public static Result migration() {
		updateValuationContainer();
		return ok("Migration Valuation Container Finish");
	}

	private static void updateValuationContainer() {
		String procedure="select matmanom, valide=tboolean from Materielmanip m, Valide v where m.val=v.valco and emnco=14 and matmaInNGL!=null";
		List<Container> results = limsServices.jdbcTemplate.query(procedure,new Object[]{} 
		,new RowMapper<Container>() {

			@SuppressWarnings("rawtypes")
			public Container mapRow(ResultSet rs, int rowNum) throws SQLException {

				Container container = new Container();
				container.code=rs.getString("matmanom");
				container.valuation=new Valuation();
				container.valuation.valid=TBoolean.valueOf(rs.getString("valide"));
				return container;
			}

		});        

		for(Container c:results){
			if(!MongoDBDAO.checkObjectExistByCode(InstanceConstants.CONTAINER_COLL_NAME, Container.class,c.code )){
				logger.error("Le container n'existe pas :"+c.code);
			}else {
				MongoDBDAO.update(InstanceConstants.CONTAINER_COLL_NAME,Container.class, DBQuery.is("code", c.code),DBUpdate.set("valuation.valid",c.valuation.valid));
			}
		}
	}
}
