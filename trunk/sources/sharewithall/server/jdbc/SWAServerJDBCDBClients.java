/**
 * 
 */
package sharewithall.server.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Authors:
 *    Alex Catarineu
 *    Ferran Rigual
 *    Miguel Angel Vico
 *
 * Creation date: Oct 31, 2010
 */
public class SWAServerJDBCDBClients extends SWAServerJDBCDBTable
{
    
    public SWAServerJDBCDBClients()
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
              rs.getBoolean("is_public"), rs.getTimestamp("last_time"), rs.getString("username"), rs.getString("session_id"));
        } catch (SQLException ex)
        {
            System.out.println("Server exception: " + ex.getClass() + ":" + ex.getMessage());
        }
        return ret;
    }

    @Override
    protected SWAServerJDBCPredicate[] write_obj(Object obj)
    {
        SWAServerJDBCClient cl = (SWAServerJDBCClient) obj;

        SWAServerJDBCPredicate[] res = new SWAServerJDBCPredicate[7];
        res[0] = new SWAServerJDBCPredicate("ip", cl.ip);
        res[1] = new SWAServerJDBCPredicate("port", cl.port);
        res[2] = new SWAServerJDBCPredicate("name", cl.name);
        res[3] = new SWAServerJDBCPredicate("is_public", cl.is_public);
        res[4] = new SWAServerJDBCPredicate("last_time", cl.last_time);
        res[5] = new SWAServerJDBCPredicate("username", cl.username);
        res[6] = new SWAServerJDBCPredicate("sesionID", cl.session_id);

        return res;
    }

}
