package models.laboratory.container.description.dao;

import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.stereotype.Repository;

import models.laboratory.container.description.ContainerCategory;
import models.utils.dao.AbstractDAODefault;
import models.utils.dao.DAOException;

@Repository
public class ContainerCategoryDAO extends AbstractDAODefault<ContainerCategory> {

	protected ContainerCategoryDAO() {
		super("container_category", ContainerCategory.class, true);
	}
	
	@SuppressWarnings("deprecation")
	public ContainerCategory findByContainerSupportCategoryCode(String containerSupportCategoryCode) {
		if (null == containerSupportCategoryCode) {
			throw new DAOException("containerSupportCategoryCode is mandatory");
		}
		String sql = sqlCommon + " inner join container_support_category as c ON c.fk_container_category=t.id" +
				" WHERE c.code=? order by t.name";
		BeanPropertyRowMapper<ContainerCategory> mapper = new BeanPropertyRowMapper<>(entityClass);
		return this.jdbcTemplate.queryForObject(sql, mapper, containerSupportCategoryCode);
	}
	
}
