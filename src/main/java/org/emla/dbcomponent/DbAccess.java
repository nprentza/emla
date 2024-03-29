package org.emla.dbcomponent;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.emla.learning.LearningUtils;
import org.emla.learning.oner.Frequency;
import org.emla.learning.oner.FrequencyTable;
import tech.tablesaw.api.ColumnType;

public class DbAccess {

private static final Logger logger = LogManager.getLogger(DbAccess.class);
	
	private static String connectionString = "jdbc:sqlite::memory:"; 
	private static Connection conn;
	
	// TODO throw exception
	public static void initiateConnection() {
		try {
			conn = DriverManager.getConnection(connectionString);
			//stmt = conn.createStatement();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	// TODO throw exception
	public static void closeConnection() {
		try {
			conn.close();
			logger.info("Connection to SQLite has been closed.");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			
	}
		
	public static boolean createDbTable(DatasetAbstract ds) {
		boolean success = false;
		try {
		    Statement stmt = conn.createStatement();
		    stmt.executeUpdate("DROP TABLE IF EXISTS " + ds.getDatasetName());
		    // create table
		    String sql = "CREATE TABLE " + ds.getDatasetName() + "(" ;
		    for (int i=0; i<ds.getDsTable().columns().size()-1; i++) {
		    	sql += ds.getDsTable().column(i).name() + " " +  columnTypeToSql(ds.getDsTable().column(i).type()) + ", ";
		    }
		    int lastColIndex = 	ds.getDsTable().columns().size()-1;
	    	sql += ds.getDsTable().column(lastColIndex).name() + " " +  
	    			columnTypeToSql(ds.getDsTable().column(lastColIndex).type()) + ")";

		    stmt.executeUpdate(sql);
			    
		    // insert data
		    for (int i=0; i<ds.getRowCout(); i++) {
		    	sql = "INSERT INTO " + ds.getDatasetName() + "(" + String.join(",", ds.getColumnNames()) + ")" +
		    " VALUES(" + ds.dataRowToCommaSeparatedString(i) + ")";
		    	 stmt.execute(sql);
		    }
		    success = true;
		    stmt.close();
		 } catch (SQLException e) {
		         e.printStackTrace();
		 } 
		return success;
	}
	
	public static FrequencyTable getFrequencies(String predictor, String targetVariable, List<String> targetValues, String tblName,
												String datasplit, List<Integer> caseIDs){
		//	ColumnType is set to STRING assuming that DbAccess.getFrequencies is called only for categorical features
		FrequencyTable freqTable = new FrequencyTable(predictor, ColumnType.STRING, targetValues);

		String inSql=null;
		if (caseIDs!=null){
			inSql = "caseID IN (";
			for (Integer caseId : caseIDs){
				inSql += caseId + ",";
			}
			inSql = inSql.substring(0, inSql.length()-1) + ")";
		}
		
		String sqlString = "SELECT " + predictor + ", " + targetVariable + ", count(*) as instances, GROUP_CONCAT(caseID) as caseIDs FROM " + tblName
				+ " WHERE datasplit='" + datasplit + "'" + (inSql!=null ? " AND " + inSql : "")
				+ " GROUP BY " + predictor + ", " + targetVariable 
				+ " ORDER BY " + predictor + ";";
	
		Frequency f = null; String tmpPredictorValue;
		try {
			Statement stmt=conn.createStatement();
			ResultSet rs = stmt.executeQuery(sqlString);
			if (rs.next()) {
				f = new Frequency(predictor, LearningUtils.Operator.EQUALS, rs.getString(1), rs.getString(4));
				tmpPredictorValue=rs.getString(1);
				f = addFrequency(f,rs.getString(2), rs.getInt(3)); // f.addFrequency(rs.getString(2), rs.getInt(3));
				while (rs.next()) {
					if (!rs.getString(1).equals(tmpPredictorValue)) {
						freqTable.addFrequency(f);
						f = new Frequency(predictor, LearningUtils.Operator.EQUALS, rs.getString(1), rs.getString(4));
						tmpPredictorValue = rs.getString(1);
					}
					f = addFrequency(f,rs.getString(2), rs.getInt(3)); // f.addFrequency(rs.getString(2), rs.getInt(3));
				}
				freqTable.addFrequency(f);
				// case rs contains a single recordset:
				//if (freqTable.isFrequenciesEmpty()){
				//		freqTable.addFrequency(f);
				//}
			}
			stmt.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
		return freqTable;
	}

	private static Frequency addFrequency(Frequency f, String targetValue, int instances){
		if (instances>0){
			f.addFrequency(targetValue,instances);
		}
		return f;
	}
	
	/*
	 * private methods
	 */
	
	private static String columnTypeToSql(ColumnType type) {
		String sqlType=null;
		
		if (type==ColumnType.INTEGER) {
			sqlType = "INT";
		}else {		// currently only integer and text supported
			sqlType = "VARCHAR(20)";
		}
		
		return sqlType;
	}
}
