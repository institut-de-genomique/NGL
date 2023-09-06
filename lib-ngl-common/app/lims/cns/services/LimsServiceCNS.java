package lims.cns.services;
import javax.sql.DataSource;

import lims.services.ILimsServices;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import play.Logger;

@Repository
public class LimsServiceCNS implements ILimsServices {

        private JdbcTemplate jdbcTemplate;
        
   
    @Autowired
    @Qualifier("lims")
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

	@Override    
    public void updateContainerState(String limsCode, String stateCodeLims){
    	Logger.info("pm_MaterielmanipEtat @matmaco="+limsCode+", @ematerielco="+stateCodeLims+", @val=null");
    	this.jdbcTemplate.update("pm_MaterielmanipEtat @matmaco=?, @ematerielco=?,@val=null", new Object[]{Integer.valueOf(limsCode),Integer.valueOf(stateCodeLims)});
    }

}