package models;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import models.laboratory.common.instance.PropertyValue;
import models.laboratory.common.instance.property.PropertySingleValue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import play.Logger;
import validation.ContextValidation;

@Repository
public class TaraDAO {

	private JdbcTemplate jdbcTemplate;

	protected final String  SELECT_MATERIEL_TARA= "select * from V_SAMPLE_NGL" ;
			/*
			" SELECT"+
			"  cast(AREA.AREA_CODE as SIGNED INT) as station ,"+
			"  AREA.AREA_NAME,"+
			"  FRACTION.FRACTION_CODE as filtreCode,"+
			"  FRACTION.FRACTION_NAME as filtre,"+
			"  ITERATION.ITERATION_CODE as iteration,"+
			"  ITERATION.ITERATION_NAME,"+
			"  LOCUS.LOCUS_CODE as profondeurCode,"+
			"  LOCUS.LOCUS_NAME as profondeur,"+
			"  MATERIAL.MATERIAL_CODE,"+
			"  MATERIAL.MATERIAL_NAME as materiel,"+
			"  CROSS_REF.REF_ID as ref_id,"+
			"  COLLAB_INFOS.INFO as codebarre"+
			" FROM "+
			" AREA INNER JOIN SAMPLE ON (AREA.AREA_ID=SAMPLE.AREA_ID)"+
			"  INNER JOIN CROSS_REF ON (SAMPLE.SAMPLE_ID=CROSS_REF.SAMPLE_ID and CROSS_REF.REF_DB='lims')"+
			"  INNER JOIN FRACTION ON (SAMPLE.FRACTION_ID=FRACTION.FRACTION_ID)"+
			"  INNER JOIN ITERATION ON (ITERATION.ITERATION_ID=SAMPLE.ITERATION_ID)"+
			"  INNER JOIN LOCUS ON (SAMPLE.LOCUS_ID=LOCUS.LOCUS_ID)"+
			"  LEFT JOIN COLLAB_INFOS ON (COLLAB_INFOS.SAMPLE_ID=SAMPLE.SAMPLE_ID AND COLLAB_INFOS.INFOS_TYPE_CODE=1)"+
			"  INNER JOIN MATERIAL ON (MATERIAL.MATERIAL_ID=SAMPLE.MATERIAL_ID)";
*/
//	@Autowired
	@Qualifier("tara")
	public void setDataSourceTara(DataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);              
	}

	@SuppressWarnings("rawtypes")
	public Map<String,PropertyValue> findTaraSampleFromLimsCode(Integer limsCode,ContextValidation contextValidation){

		List<Map<String,PropertyValue>> results =  this.jdbcTemplate.query(SELECT_MATERIEL_TARA +
				"  WHERE REF_ID=? ", 
				new Object[]{limsCode},new RowMapper<Map<String,PropertyValue>>() {

			public Map<String,PropertyValue> mapRow(ResultSet rs, int rowNum) throws SQLException {
				
				return mapRowTara(rs, rowNum);
			}

		});     
		
		if(results.size()==1){
			return results.get(0);
		} else {
			contextValidation.addErrors("taraRefId","error.propertyNotExist","Tara Reference Id", limsCode);
			return null;
		}


	}
	
	@SuppressWarnings("rawtypes")
	public List<Map<String,PropertyValue>> findTaraSampleUpdated(List<String> limsCodes){

		String sql=null;
		if(limsCodes==null){
			sql=SELECT_MATERIEL_TARA+" WHERE TO_DAYS(NOW()) - TO_DAYS(LAST_UPD_TARA_DB) <= 10";
		}else {
			//Pour les tests unitaires
			sql=SELECT_MATERIEL_TARA+" WHERE REF_ID in (";
			for(String code:limsCodes){
				sql=sql+"'"+code+"',";
			}
			sql=sql+"'')";
		}
		//Logger.debug("Query :"+sql);
		List<Map<String,PropertyValue>> results =  this.jdbcTemplate.query(sql 
				,new RowMapper<Map<String,PropertyValue>>() {

			public Map<String,PropertyValue> mapRow(ResultSet rs, int rowNum) throws SQLException {
				
				Map<String,PropertyValue> properMap= mapRowTara(rs, rowNum);
				properMap.put("limsCode",new PropertySingleValue(rs.getInt("ref_id")));
				return properMap;
			}

		});     
		
		return results;
		
	}
	
	
	public Map<String,PropertyValue> mapRowTara(ResultSet rs, int rowNum) throws SQLException {
		
		//Logger.debug("Tara :"+rs.getInt("ref_id"));
		
		Map<String,PropertyValue> properMap=new HashMap<String, PropertyValue>();
		properMap.put("taraStation", new PropertySingleValue(rs.getInt("station")));
		properMap.put("taraDepth", new PropertySingleValue(rs.getString("profondeur")));
		properMap.put("taraFilter", new PropertySingleValue(rs.getString("filtre")));
		properMap.put("taraIteration", new PropertySingleValue(rs.getString("iteration")));
		properMap.put("taraSample", new PropertySingleValue(rs.getString("materiel")));
		if(rs.getString("codebarre")!=null){
			properMap.put("taraBarCode", new PropertySingleValue(rs.getString("codebarre")));
		}
		properMap.put("taraDepthCode", new PropertySingleValue(rs.getString("profondeurCode")));
		properMap.put("taraFilterCode", new PropertySingleValue(rs.getString("filtreCode")));
		
		return properMap;
	}
	
	

}
