package database;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;

import settings.configProg;
import systemio.mylogging;

public class DbUpdate
{

	static public boolean updateFlightSite(int flightId, String newSite)
	{		
    PreparedStatement pstmt = null;
    
    try 
    {
        pstmt = configProg.getDbConn().prepareStatement("UPDATE Vol SET V_Site = ? WHERE V_ID = ?");
        pstmt.setString(1, newSite);
        pstmt.setInt(2, flightId);
        pstmt.executeUpdate();
    		return true;
    }
    catch (Exception e)
    {
      StringBuilder sbError = new StringBuilder(DbUpdate.class.getName()+"."+Thread.currentThread().getStackTrace()[1].getMethodName());
      sbError.append("\r\n").append(e.getMessage());
      sbError.append("\r\n").append("Cannot update flight site");
      mylogging.log(Level.SEVERE, sbError.toString());
      return false;
    }    
    finally
    {
			try
			{
	    	if (pstmt != null)
	    		pstmt.close();
			}
			catch (SQLException e)
			{
				e.printStackTrace();
			}
    }                                                                            
	}

}
