package models.laboratory.run.description.dao;

import static models.utils.dao.DAOException.daoAssertNotNull;

import java.util.List;

import org.springframework.stereotype.Repository;

import models.laboratory.run.description.TreatmentContext;
import models.utils.dao.AbstractDAODefault;
import models.utils.dao.DAOException;

@Repository
public class TreatmentContextDAO extends AbstractDAODefault<TreatmentContext> {

	protected TreatmentContextDAO() {
		super("treatment_context",TreatmentContext.class,true);
	}
		
	@SuppressWarnings("deprecation")
	private void removeTreatmentTypesContexts(Long id) {
		String sql = "DELETE FROM treatment_type_context WHERE fk_treatment_context=?";
		jdbcTemplate.update(sql, id);	
	}
	
	@Override
	public void remove(TreatmentContext treatmentContext) throws DAOException {
//		if(null == treatmentContext){
//			throw new IllegalArgumentException("treatmentContext is null");
//		}
		daoAssertNotNull("treatmentContext",treatmentContext);
		
		removeTreatmentTypesContexts(treatmentContext.id);
		//Remove treatmentContext
		super.remove(treatmentContext);
	}

	@Override
	protected List<String> getColumns() {
		return enumColumns;
	}

}

