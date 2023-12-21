package cedge.pfms.av;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;

public class CodeTest {

	    public static void main(String[] args) {
	    	
	    	String Query="SELECT TRANRES.GET_CRTRANSTATUS('') FROM DUAL;";
	    	ArrayList<String> DBProperties = new ArrayList<String>();
	    	
	    	DBProperties.set(4, "oracle.jdbc.driver.OracleDriver");
	    	DBProperties.set(4, "jdbc:oracle:thin:@10.42.2.103:1521:F012BAND");
	    	DBProperties.set(6, "CPSMS");
	    	DBProperties.set(7, "CPSMS");
	    	
	    	CodeTest CD= new CodeTest();
	    	
	    	CD.getOraData(DBProperties,Query);
	    
	    	
	    }
	    
	    public List<List<Object>> getOraData(ArrayList<String> DBProperties, String Query) {
			Connection OraCon = null;
			boolean CloseConReq=false;
			List<List<Object>> output = new ArrayList<List<Object>>();
			try {
				Class.forName(DBProperties.get(4));
				String URL = DBProperties.get(5);
				String UserName = DBProperties.get(6);
				String Password = DBProperties.get(7);
				OraCon = DriverManager.getConnection(URL, UserName, Password);
				if (OraCon != null) {
					// System.out.println("Connected with URL: ==>"+URL);
					Statement OraSt = OraCon.createStatement();
					ResultSet OraRS = OraSt.executeQuery(Query);

					output = readRows(OraRS);

				}
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("[ " + e + " ] [ " + Query + " ]");
			} finally {
				try {
					if(!CloseConReq)
					{
					if (!OraCon.isClosed()) {
						OraCon.close();
					}
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}

			return output;
		}
	    
		private List<List<Object>> readRows(ResultSet rs) throws SQLException {
			ImmutableList.Builder<List<Object>> rows = ImmutableList.builder();
			int columnCount = rs.getMetaData().getColumnCount();
			while (rs.next()) {
				List<Object> row = new ArrayList<>();
				for (int i = 1; i <= columnCount; i++) {
					row.add(rs.getObject(i));
				}
				rows.add(row);
			}
			return rows.build();
		}
	}

