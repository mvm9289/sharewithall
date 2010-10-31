/**
 * 
 */
package ShareWithAll.Server.JDBC;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Author: a.catarineu
 *
 * Creation date: Oct 31, 2010
 */
public class SWAServerJDBCDBClients extends SWAServerJDBCDBTable
{
    
    public SWAServerJDBCDBClients() throws FileNotFoundException, IOException, ClassNotFoundException, SQLException
    {
        super();
    }
    
    @Override
    protected String get_name()
    {
        return "clients";
    }

    @Override
    protected Object read_obj(ResultSet rs)
    {
        SWAServerJDBCClient ret = null;
        try
        {
            ret = new SWAServerJDBCClient(rs.getString("ip"), rs.getInt("port"), rs.getString("name"),
              rs.getBoolean("is_public"), rs.getTimestamp("last_time"), rs.getString("username"));
        } catch (SQLException ex)
        {
            Logger.getLogger(SWAServerJDBCDBClients.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ret;
    }

    @Override
    protected SWAServerJDBCPredicate[] write_obj(Object obj)
    {
        SWAServerJDBCClient cl = (SWAServerJDBCClient) obj;

        SWAServerJDBCPredicate[] res = new SWAServerJDBCPredicate[6];
        res[0] = new SWAServerJDBCPredicate("ip", cl.ip);
        res[1] = new SWAServerJDBCPredicate("port", cl.port);
        res[2] = new SWAServerJDBCPredicate("name", cl.name);
        res[3] = new SWAServerJDBCPredicate("is_public", cl.is_public);
        res[4] = new SWAServerJDBCPredicate("last_time", cl.last_time);
        res[5] = new SWAServerJDBCPredicate("username", cl.username);

        return res;
    }

}
