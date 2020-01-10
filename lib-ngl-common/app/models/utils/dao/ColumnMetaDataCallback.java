package models.utils.dao;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.springframework.jdbc.support.DatabaseMetaDataCallback;
import org.springframework.jdbc.support.MetaDataAccessException;

/**
 * Utility class to get all column name from a table
 * 
 * @author ejacoby
 *
 */
public class ColumnMetaDataCallback implements DatabaseMetaDataCallback {

	private String tableName;
		
	public ColumnMetaDataCallback(String tableName) {
//		super();
		this.tableName = tableName;
	}

//	@Override
//	public Object processMetaData(DatabaseMetaData dbmd) throws SQLException, MetaDataAccessException {
//		ResultSet rs = null ;
//		ArrayList<String> colNames = new ArrayList<>();
//		try {
//			// u can specify table name instead of %
//			rs = dbmd.getColumns(null,null,tableName,"%");
//			while (rs.next()) {
//				colNames.add(rs.getString(4));
//			}
//		} catch (Exception e) {
//			throw new SQLException() ;
//		} finally {
//			if (rs != null)
//				rs.close() ;
//		}
//		return colNames;
//	}

	@Override
	public Object processMetaData(DatabaseMetaData dbmd) throws SQLException, MetaDataAccessException {
		ArrayList<String> colNames = new ArrayList<>();
		// u can specify table name instead of %
		try (ResultSet rs = dbmd.getColumns(null,null,tableName,"%")) {
			while (rs.next()) 
				colNames.add(rs.getString(4));
		} catch (Exception e) {
			throw new SQLException() ;
		}
		return colNames;
	}

}


