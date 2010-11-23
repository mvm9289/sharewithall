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
public class JDBCDBClients extends JDBCDBTable
{
    
    public JDBCDBClients()
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
        JDBCClient ret = null;
        try
        {
            ret = new JDBCClient(rs.getString("ip"), rs.getInt("port"), rs.getString("name"),
              rs.getBoolean("is_public"), rs.getTimestamp("last_time"), rs.getString("username"), rs.getString("session_id"));
        } catch (SQLException ex)
        {
            System.out.println("Server exception: " + ex.getClass() + ":" + ex.getMessage());
        }
        return ret;
    }

    @Override
    protected JDBCPredicate[] write_obj(Object obj)
    {
        JDBCClient cl = (JDBCClient) obj;

        JDBCPredicate[] res = new JDBCPredicate[7];
        res[0] = new JDBCPredicate("ip", cl.ip);
        res[1] = new JDBCPredicate("port", cl.port);
        res[2] = new JDBCPredicate("name", cl.name);
        res[3] = new JDBCPredicate("is_public", cl.is_public);
        res[4] = new JDBCPredicate("last_time", cl.last_time);
        res[5] = new JDBCPredicate("username", cl.username);
        res[6] = new JDBCPredicate("session_id", cl.session_id);

        return res;
    }

}
