package models.laboratory.instrument.description.dao;

import java.util.List;

import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.stereotype.Repository;

import models.laboratory.instrument.description.InstrumentCategory;
import models.utils.dao.AbstractDAODefault;

@Repository
public class InstrumentCategoryDAO extends AbstractDAODefault<InstrumentCategory>{

	public InstrumentCategoryDAO() {
		super("instrument_category",InstrumentCategory.class,true);
	}

	@SuppressWarnings("deprecation")
	public List<InstrumentCategory> findByInstrumentUsedTypeCode(String intrumentUsedTypeCode){
		String sql = "select ic.name, ic.code FROM instrument_category ic, common_info_type cit inner join instrument_used_type iut on iut.fk_common_info_type = cit.id WHERE ic.id = iut.fk_instrument_category AND cit.code=?";
		BeanPropertyRowMapper<InstrumentCategory> mapper = new BeanPropertyRowMapper<>(entityClass);
		return this.jdbcTemplate.query(sql, mapper, intrumentUsedTypeCode);
	}
	
	@Override
	protected List<String> getColumns() {
		return enumColumns;
	}

}
