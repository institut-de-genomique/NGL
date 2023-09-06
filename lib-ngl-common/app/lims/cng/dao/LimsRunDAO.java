package lims.cng.dao;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.stereotype.Repository;

import lims.models.experiment.Experiment;
import lims.models.instrument.Instrument;

@SuppressWarnings("deprecation")
@Repository
public class LimsRunDAO {
	
	private SimpleJdbcTemplate jdbcTemplate;
	
    @Autowired
    @Qualifier("lims")
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new SimpleJdbcTemplate(dataSource);       
    }
    /*
     * Return the list of active sequencers.
     * @return
     */
     // 14/09/2015 FDS obsolete ???
    public List<Instrument> getInstruments(){
    	
    	
    	String sql = "SELECT DISTINCT m.pc_name as code, m.run_path as path, mt.name as categoryCode, 1 as active  " +
    			"FROM t_machine m JOIN t_machine_type mt ON m.type_id=mt.id WHERE mt.type in ('HS','H2') AND (m.status=2 OR m.status=1) " +
    			"ORDER BY m.pc_name";
    	BeanPropertyRowMapper<Instrument> mapper = new BeanPropertyRowMapper<>(Instrument.class);
    	return this.jdbcTemplate.query(sql, mapper);
    }
    
    /*
     * Returns the list of experiments (illumina runs) for a containerSupportCode (flowcell barcode).
     * @return experiments (illumina runs) for a containerSupportCode (flowcell barcode)
     */
    // 14/09/2015 FDS utilisation de la vue v_get_experiments

    public List<LimsExperiment> getExperiments(Experiment experiment){

        if(null != experiment.date){
            String sql =" SELECT  code, date, categoryCode,nb_cycles FROM v_get_experiments"
                               +" WHERE barcode=? and date between ? and ?";
            BeanPropertyRowMapper<LimsExperiment> mapper = new BeanPropertyRowMapper<>(LimsExperiment.class);
            return this.jdbcTemplate.query(sql, mapper, experiment.containerSupportCode, minus(experiment.date,5), add(experiment.date,5));

        }else{
            String sql =" SELECT  code, date, categoryCode, nb_cycles FROM v_get_experiments"
                               +" WHERE barcode=?";
            BeanPropertyRowMapper<LimsExperiment> mapper = new BeanPropertyRowMapper<>(LimsExperiment.class);
            return this.jdbcTemplate.query(sql, mapper, experiment.containerSupportCode);
        }
    }
    
    private Date minus(Date date, int nbDay) {
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(date.getTime());		
		c.set(Calendar.DAY_OF_YEAR, c.get(Calendar.DAY_OF_YEAR) - nbDay);
    	return c.getTime();
	}
    
    private Date add(Date date, int nbDay) {
    	Calendar c = Calendar.getInstance();
		c.setTimeInMillis(date.getTime());		
		c.set(Calendar.DAY_OF_YEAR, c.get(Calendar.DAY_OF_YEAR) + nbDay);
    	return c.getTime();
	}
    
    // 14/09/2015 FDS obsolete ??? nom incorrect manque un T...
	public List<LimsLibrary> geContainerSupport(String supportCode){
		/*
		 SELECT l.number as lane_number, et.name as exp_name, s.barcode as aliquot_barcode, s.stock_barcode, i.short_name as index_short,
			i.sequence as index_sequence, i.type as index_type
			FROM t_flowcell f
			JOIN t_lane l ON l.flowcell_id=f.id
			JOIN t_sample_lane sl ON sl.lane_id=l.id
			JOIN t_sample s ON sl.sample_id=s.id
			JOIN t_exp_type et on sl.exp_type_id=et.id
			JOIN t_individual ind on s.individual_id=ind.id
			JOIN t_index i ON ((sl.index=i.cng_name OR sl.index=i.short_name) AND i.type !=3 )
			WHERE f.barcode='$fcbarcode'
			ORDER BY l.number"; 

		 */
		
		
    	String sql = "SELECT distinct l.number as laneNumber, et.short_name as experimentTypeCode, s.stock_barcode as sampleBarCode," +
    			" i.short_name as indexName, i.type as indexTypeCode, i.sequence as indexSequence ,ind.name as sampleCode, sl.size as insertLength, fn_getsampleid_project(s.id) as projectCode"
    			+ " FROM t_flowcell f"
    			+ " JOIN t_lane l ON l.flowcell_id=f.id"
    			+ " JOIN t_sample_lane sl ON sl.lane_id=l.id"
    			+ " JOIN t_sample s ON sl.sample_id=s.id"
    			+ " JOIN t_exp_type et on sl.exp_type_id=et.id"
    			+ " JOIN t_individual ind on s.individual_id=ind.id"
    			+ " LEFT OUTER JOIN t_index i ON (sl.index=i.cng_name OR sl.index=i.short_name)"
    			+ " WHERE f.barcode=?"
    			+ " ORDER BY l.number";
    	BeanPropertyRowMapper<LimsLibrary> mapper = new BeanPropertyRowMapper<>(LimsLibrary.class);
    	return this.jdbcTemplate.query(sql, mapper, supportCode);    	
    	
    }
}
