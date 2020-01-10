package lims.cns.dao;


import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import lims.models.Manip;
import lims.models.Plate;
import lims.models.User;
import lims.models.Well;
import models.laboratory.common.instance.TBoolean;
import models.utils.ListObject;

// import play.Logger;

@Repository
public class LimsManipDAO {

	private static final play.Logger.ALogger logger = play.Logger.of(LimsManipDAO.class);
	
	private JdbcTemplate jdbcTemplate;

    @Autowired
    @Qualifier("lims")
    public void setDataSource(DataSource dataSource) {
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public List<Manip> findManips(Integer emnco, Integer ematerielco,String prsco){
    	logger.info("pl_MaterielmanipChoisi @prsco='"+prsco+"', @emnco="+emnco+", @ematerielco="+ematerielco+", @plaque=1 ");
//        List<Manip> results = this.jdbcTemplate.query("pl_MaterielmanipChoisi @prsco=?, @emnco=?, @ematerielco=?, @plaque=?",
        List<Manip> results = jdbcTemplate.query("pl_MaterielmanipChoisi @prsco=?, @emnco=?, @ematerielco=?, @plaque=?",
        		new Object[]{prsco, emnco, ematerielco, 1},new BeanPropertyRowMapper<>(Manip.class));
        return results;
    }

    public void createBarcode(String barcode, Integer typeCode, String user){
    	logger.info("pc_PlaqueSolexa @plaqueId="+barcode+", @emnco="+typeCode);
//    	this.jdbcTemplate.update("pc_PlaqueSolexa @plaqueId=?, @emnco=?, @perlog=?", new Object[]{barcode, typeCode, user});
    	jdbcTemplate.update("pc_PlaqueSolexa @plaqueId=?, @emnco=?, @perlog=?", new Object[]{barcode, typeCode, user});    	
    }
    
    public void createPlate(Plate plate, String user) {
    	logger.info("pc_PlaqueSolexa @plaqueId="+plate.code+", @emnco="+plate.typeCode);
//    	this.jdbcTemplate.update("pc_PlaqueSolexa @plaqueId=?, @emnco=?, @valqc=?, @valrun=?, @plaquecom=?, @perlog=?", new Object[]{plate.code, plate.typeCode, getValValue(plate.validQC), getValValue(plate.validRun), plate.comment, user});
//    	this.jdbcTemplate.update("ps_MaterielmanipPlaque @plaqueId=?", new Object[]{plate.code});
    	jdbcTemplate.update("pc_PlaqueSolexa @plaqueId=?, @emnco=?, @valqc=?, @valrun=?, @plaquecom=?, @perlog=?", new Object[]{plate.code, plate.typeCode, getValValue(plate.validQC), getValValue(plate.validRun), plate.comment, user});
    	jdbcTemplate.update("ps_MaterielmanipPlaque @plaqueId=?", new Object[]{plate.code});
    	for(Well well: plate.wells){
    		logger.info("pm_MaterielmanipPlaque @matmaco="+well.code+", @plaqueId="+plate.code+", @plaqueX="+well.x+", @plaqueY="+well.y+"");
//    		this.jdbcTemplate.update("pm_MaterielmanipPlaque @matmaco=?, @plaqueId=?, @plaqueX=?, @plaqueY=?", well.code, plate.code, well.x.toString(), well.y);
    		jdbcTemplate.update("pm_MaterielmanipPlaque @matmaco=?, @plaqueId=?, @plaqueX=?, @plaqueY=?", well.code, plate.code, well.x.toString(), well.y);
    	}
    }

    public void updatePlate(Plate plate, String user) {
//    	this.jdbcTemplate.update("pm_PlaqueSolexa @plaqueId=?, @valqc=?, @valrun=?, @plaquecom=?, @perlog=?", new Object[]{plate.code, getValValue(plate.validQC), getValValue(plate.validRun), plate.comment, user});    	
    	jdbcTemplate.update("pm_PlaqueSolexa @plaqueId=?, @valqc=?, @valrun=?, @plaquecom=?, @perlog=?", new Object[]{plate.code, getValValue(plate.validQC), getValValue(plate.validRun), plate.comment, user});    	
    	logger.info("ps_MaterielmanipPlaque @plaqueId="+plate.code);
//    	this.jdbcTemplate.update("ps_MaterielmanipPlaque @plaqueId=?", new Object[]{plate.code});
    	jdbcTemplate.update("ps_MaterielmanipPlaque @plaqueId=?", new Object[]{plate.code});
    	for(Well well: plate.wells){
    		logger.info("pm_MaterielmanipPlaque @matmaco="+well.code+", @plaqueId="+plate.code+", @plaqueX="+well.x+", @plaqueY="+well.y+"");
//    		this.jdbcTemplate.update("pm_MaterielmanipPlaque @matmaco=?, @plaqueId=?, @plaqueX=?, @plaqueY=?", well.code, plate.code, well.x.toString(), well.y);
    		jdbcTemplate.update("pm_MaterielmanipPlaque @matmaco=?, @plaqueId=?, @plaqueX=?, @plaqueY=?", well.code, plate.code, well.x.toString(), well.y);
    	}
    }

    public List<Plate> findPlates(Integer emnco, String projetValue, String plaqueId, String matmanom, Integer percodc, String fromDate, String toDate) {
    	logger.info("pl_PlaqueSolexa @prsco="+projetValue+", @emnco="+emnco+", @fromDate="+fromDate+", @toDate="+toDate);
//		List<Plate> plates = this.jdbcTemplate.query("pl_PlaqueSolexa @prsco=?, @emnco=?, @plaqueId=?, @matmanom=?, @percodc=?, @fromDate=?, @toDate=?", new Object[]{projetValue, emnco, plaqueId, matmanom,percodc,fromDate,toDate}, new RowMapper<Plate>() {
		List<Plate> plates = jdbcTemplate.query("pl_PlaqueSolexa @prsco=?, @emnco=?, @plaqueId=?, @matmanom=?, @percodc=?, @fromDate=?, @toDate=?", new Object[]{projetValue, emnco, plaqueId, matmanom,percodc,fromDate,toDate}, new RowMapper<Plate>() {
	        @Override
			public Plate mapRow(ResultSet rs, int rowNum) throws SQLException {
	        	Plate plate = new Plate();
	        	//well.plateCode = rs.getString("plaqueId");
	        	plate.code = rs.getString("plaqueId");
	        	plate.typeCode = rs.getInt("emnco");
	        	plate.typeName = rs.getString("emnnom");
	        	plate.nbWells = rs.getInt("nombrePuitUtilises");
	        	plate.validQC = getTBoolean(rs.getInt("valqc"));
	        	plate.validRun = getTBoolean(rs.getInt("valrun"));
	        	plate.comment = rs.getString("plaquecom");
	        	plate.creationDate = rs.getDate("plaquedc");
	        	plate.modificationDate = rs.getDate("plaquedm");
	        	plate.creationUserId = rs.getInt("percodc");
	        	plate.modificationUserId = rs.getInt("percodm");	
	            return plate;
	        }
	    });
		return plates;
	}

    public List<String> findUnusedBarCodes(){
    	String query = "pl_PlaqueSolexaUnused";    	
//    	List<String> unusedBarcodes = this.jdbcTemplate.queryForList(query, String.class);
    	List<String> unusedBarcodes = jdbcTemplate.queryForList(query, String.class);
    	return unusedBarcodes;
    }
    
//    private TBoolean getTBoolean(int value) {
//    	TBoolean valid = TBoolean.UNSET;
//    	if (value == 1) {
//    		valid = TBoolean.TRUE;
//    	} else if (value == 0) {
//    		valid = TBoolean.FALSE;
//    	}
//    	return valid;
//    }
    private TBoolean getTBoolean(int value) {
    	switch (value) {
    	case 0  : return TBoolean.FALSE;
    	case 1  : return TBoolean.TRUE;
    	default : return TBoolean.UNSET;
    	}
    }
    
//    private int getValValue(TBoolean value) {
//    	int valid = 2;
//    	if (TBoolean.TRUE.equals(value)) {
//    		valid = 1;
//    	} else if (TBoolean.FALSE.equals(value)) {
//    		valid = 0;
//    	}
//    	return valid;
//    }
    // This is not consistent with TBoolean.value (0,1,-1). 
    private int getValValue(TBoolean value) {
    	if (value == null)
    		return 2;
    	switch (value) {
    	case TRUE  : return 1;
    	case FALSE : return 0;
    	default    : return 2;
    	}
    }
    
	/* *
	 * Return a plate with coordinate
	 * @param code
	 * @return
	 */
	public Plate getPlate(String code) {
		logger.info("pl_PlaqueSolexa @plaqueId="+code);
//		List<Plate> plates = this.jdbcTemplate.query("pl_PlaqueSolexa @plaqueId=?", new Object[]{code}, new RowMapper<Plate>() {
		List<Plate> plates = jdbcTemplate.query("pl_PlaqueSolexa @plaqueId=?", new Object[]{code}, new RowMapper<Plate>() {
	        @Override
			public Plate mapRow(ResultSet rs, int rowNum) throws SQLException {
	            Plate plate = new Plate();
	        	//well.plateCode = rs.getString("plaqueId");
	        	plate.code = rs.getString("plaqueId");
	        	plate.typeCode = rs.getInt("emnco");
	        	plate.typeName = rs.getString("emnnom");
	        	plate.nbWells = rs.getInt("nombrePuitUtilises");
	        	plate.validQC = getTBoolean(rs.getInt("valqc"));
	        	plate.validRun = getTBoolean(rs.getInt("valrun"));
	        	plate.comment = rs.getString("plaquecom");
	        	plate.creationDate = rs.getDate("plaquedc");
	        	plate.modificationDate = rs.getDate("plaquedm");
	        	plate.creationUserId = rs.getInt("percodc");
	        	plate.modificationUserId = rs.getInt("percodm");
	        	plate.creationUser = getUser(plate.creationUserId);
	        	plate.modificationUser = getUser(plate.modificationUserId);
	            return plate;
	        }
	    });
		if (plates.size() == 1) {
			Plate plate = plates.get(0);
			logger.info("pl_MaterielmanipPlaque @plaqueId="+plate.code);
			List<Well> wells = this.jdbcTemplate.query("pl_MaterielmanipPlaque @plaqueId=?", new Object[]{code}, new RowMapper<Well>() {
		        @Override
				public Well mapRow(ResultSet rs, int rowNum) throws SQLException {
			    Well well = new Well();
			    well.name = rs.getString("matmanom");
			    well.code = rs.getInt("matmaco");
			    well.x = Integer.valueOf(rs.getString("plaqueX"));
			    well.y = rs.getString("plaqueY");
			    well.typeCode = rs.getInt("emnco");
			    well.typeName = rs.getString("emnnom");
			    well.valid = getTBoolean(rs.getInt("val"));
			    well.typeMaterial = rs.getString("tadnom");
			    return well;
		        }
		    });
			plate.wells = wells.toArray(new Well[wells.size()]);
			return plate;
		} else {
			return null;
		}
	}

	public Well getWell(String nomManip){
		//Logger.debug("Nom = "+nomManip);
//		List<Well> wells = this.jdbcTemplate.query("pl_MaterielmanipUnNomToPlate @matmanom=?", new Object[]{nomManip}, new RowMapper<Well>() {
		List<Well> wells = jdbcTemplate.query("pl_MaterielmanipUnNomToPlate @matmanom=?", new Object[]{nomManip}, new RowMapper<Well>() {
	        @Override
			public Well mapRow(ResultSet rs, int rowNum) throws SQLException {
		    Well well = new Well();
		    well.name = rs.getString("matmanom");
		    well.code = rs.getInt("matmaco");
		    
		    String plaqueX = rs.getString("plaqueX"); 
		    if (plaqueX != null && !"null".equals(plaqueX)) {
		    	well.x = Integer.valueOf(plaqueX);
		    }
		    well.y = rs.getString("plaqueY");
		    well.typeCode = rs.getInt("emnco");
		    well.typeName = rs.getString("emnnom");
		    well.valid = getTBoolean(rs.getInt("val"));
		    well.typeMaterial = rs.getString("tadnom");
		    return well;
	        }
	    });
		if (wells.size() == 1) {
			return wells.get(0);
		} else {
			return null;
		}
	}
	
	public User getUser(Integer id) {
		logger.info("pl_PerintUn @perco="+id);
//		List<User> users = this.jdbcTemplate.query("pl_PerintUn @perco=?", new Object[]{id}, new RowMapper<User>() {
		List<User> users = jdbcTemplate.query("pl_PerintUn @perco=?", new Object[]{id}, new RowMapper<User>() {
	        @Override
			public User mapRow(ResultSet rs, int rowNum) throws SQLException {
	        	User user = new User();
	        	user.perco = rs.getString("perco");
	        	user.perlog = rs.getString("perlog");
	            return user;
	        }
	    });
		if (users.size() == 1) {
			return users.get(0);
		} else {
			return null;
		}
	}
	
	public List<User> getUsers() {
//		List<User> users = this.jdbcTemplate.query("pl_Perint", new RowMapper<User>() {
		List<User> users = jdbcTemplate.query("pl_Perint", new RowMapper<User>() {
	        @Override
			public User mapRow(ResultSet rs, int rowNum) throws SQLException {
	        	User user = new User();
	        	user.perco = rs.getString("perco");
	        	user.perlog = rs.getString("perlog");
	            return user;
	        }
	    });
		return users;
	}
	
	public boolean isPlateExist(String code) {
		logger.info("pl_PlaqueSolexa @plaqueId="+code);
//		List<Plate> plates = this.jdbcTemplate.query("pl_PlaqueSolexa @plaqueId=?", new Object[]{code}, new RowMapper<Plate>() {
		List<Plate> plates = jdbcTemplate.query("pl_PlaqueSolexa @plaqueId=?", new Object[]{code}, new RowMapper<Plate>() {
	        @Override
			public Plate mapRow(ResultSet rs, int rowNum) throws SQLException {
	        	Plate plate = new Plate();
	        	plate.code = rs.getString("plaqueId");
	            return plate;
	        }
	    });
		return (plates.size() > 0);
	}

	public List<ListObject> getListObjectFromProcedureLims(String procedure) {
//		List<ListObject> listObjects = this.jdbcTemplate.query(procedure,
		List<ListObject> listObjects = jdbcTemplate.query(procedure,
				new RowMapper<ListObject>() {
					@Override
					public ListObject mapRow(ResultSet rs, int rowNum) throws SQLException {
						ListObject value = new ListObject();
						value.name = rs.getString(1);
						value.code = rs.getString(2);
						return value;
					}
				});
		return listObjects;
	}

	public void deletePlate(String plateCode) {
		logger.info("ps_PlaqueSolexa @plaqueId="+plateCode);
//		this.jdbcTemplate.update("ps_PlaqueSolexa @plaqueId=?", new Object[]{plateCode});
		jdbcTemplate.update("ps_PlaqueSolexa @plaqueId=?", new Object[]{plateCode});
	}
	
}

