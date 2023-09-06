package controllers.migration.cns;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import models.LimsCNSDAO;
import models.laboratory.common.instance.PropertyValue;
import models.laboratory.common.instance.property.PropertySingleValue;
import models.laboratory.container.instance.Container;
import models.laboratory.container.instance.Content;
import models.laboratory.run.instance.ReadSet;
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

public class MigrationLibProcessTypeRunExt   extends CommonController {

	protected static ALogger logger=Logger.of("MigrationLibProcessTypeRunExt");
	protected static LimsCNSDAO  limsServices = Spring.getBeanOfType(LimsCNSDAO.class);

	public static Result migration(){

		MigrationLibProcessTypeRunExt migration=new MigrationLibProcessTypeRunExt();

		// Add properties libProcessType in Container.content.properties
		String sql="select code=substring(runhnom,len(runhnom)+2-patindex('%[_]%[_]%',reverse(runhnom)),len(runhnom))+'_'+convert(varchar,pi.pistnum) "
				+ " from Runhd r, Lotsequence rs, Piste pi, Materiel a,  Useadn u, Banquehautdebit bs, Tag tt"
				+" where r.runhco=pi.runhco and pi.pistco=rs.pistco and "
				+" rs.tagkeyseq*=tt.tagkeyseq "
				+" and bs.banco=rs.banco and bs.adnco=a.adnco and a.adnco=u.adnco "
				+" and r.runhnom like '%EXT%' and r.tinsco=36 ";

		Logger.debug("SQL :"+sql);

		List<String> containerCodes=limsServices.jdbcTemplate.query(sql,new RowMapper<String>() {
			@SuppressWarnings("rawtypes")
			public String mapRow(ResultSet rs, int rowNum) throws SQLException {
				return rs.getString("code");
			}
		});

		for(String containerCode:containerCodes){

			Container container=MongoDBDAO.findByCode(InstanceConstants.CONTAINER_COLL_NAME, Container.class, containerCode);
			List<SqlObject> updateObjects=migration.getLibProcessTypeFromContainer(containerCode);

			if(container==null){
				Logger.error("Container "+containerCode +"n 'existe pas");
			}else {
				Logger.debug("Container code :"+container.code + ", taille content :"+updateObjects.size());
				for(Content content :container.contents){
					SqlObject libProcessType=migration.findLibProcessType(updateObjects,content);
					if(content.properties==null){
						content.properties=new HashMap<String, PropertyValue>();
					}
					if(libProcessType==null){
						Logger.error("LibProcessType null for "+content.sampleCode);
					}else {
						content.properties.put("libProcessTypeCode",new PropertySingleValue(libProcessType.libProcessType));
					}
				}


				MongoDBDAO.update(InstanceConstants.CONTAINER_COLL_NAME, Container.class, DBQuery.is("code", container.code)
						,DBUpdate.set("contents", container.contents)
						.set("traceInformation.modifyDate", new Date())
						.set("traceInformation.modifyUser", "lims"));
				

				// Update sampleOnContainer ReadSets associés à ce support et cette lane
				List<ReadSet> readSets=MongoDBDAO.find(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class,
						DBQuery.is("sampleOnContainer.containerCode",container.code)).toList();
				if(readSets.size()==0){
					Logger.error("Pas de readSet pour le support "+container.support.code + " piste "+container.support.line);
				}else {
					for(ReadSet readSet:readSets){
						UpdateSampleTypeCodeToContainer.migreReadSet(readSet);
					}
				}
			}
		}

		return ok("Update "+containerCodes.size()+" Container Exterieurs");
	}


	private SqlObject findLibProcessType(List<SqlObject> updateObjects, Content content) {

		for(SqlObject obj:updateObjects){

			Logger.debug("Sample Code :"+content.sampleCode + ",Sample Code to compare :"+obj.sampleCode);

			if(content.sampleCode.equals(obj.sampleCode) ) return obj;
			/*
			 * && ((content.properties.get("tag")==null && obj.tag==null) || (content.properties.get("tag").equals(obj.tag)))){
			 */
		}

		return null;
	}


	public class SqlObject {

		public String containerCode;
		public String sampleCode;
		public String libProcessType;
		public String tag;
	}

	public List<SqlObject> getLibProcessTypeFromContainer(String containerCode){
		String sql="select containerCode=substring(runhnom,len(runhnom)+2-patindex('%[_]%[_]%',reverse(runhnom)),len(runhnom))+'_'+convert(varchar,pi.pistnum) ,sampleCode=rtrim(u.prsco)+'_'+rtrim(a.adnnom),libProcessType=bs.tbhdco, tag=rs.tagkeyseq "				
				+" from Runhd r, Lotsequence rs, Piste pi, Materiel a,  Useadn u, Banquehautdebit bs "
				+" where r.runhco=pi.runhco and pi.pistco=rs.pistco "
				+" and bs.banco=rs.banco and bs.adnco=a.adnco and a.adnco=u.adnco "
				+" and r.runhnom like '%EXT%' and substring(runhnom,len(runhnom)+2-patindex('%[_]%[_]%',reverse(runhnom)),len(runhnom))+'_'+convert(varchar,pi.pistnum)=? ";

		Logger.debug("SQL : "+sql);
		List<SqlObject> sqlObjects=limsServices.jdbcTemplate.query(sql,new Object[]{containerCode} 
		,new RowMapper<SqlObject>() {
			@SuppressWarnings("rawtypes")
			public SqlObject mapRow(ResultSet rs, int rowNum) throws SQLException {
				SqlObject sqlObject=new SqlObject();
				sqlObject.containerCode=rs.getString("containerCode");
				sqlObject.sampleCode=rs.getString("sampleCode");
				sqlObject.libProcessType=rs.getString("libProcessType");
				sqlObject.tag=rs.getString("tag");
				return sqlObject;

			}
		});
		return sqlObjects;
	}
}
