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
public class SWAServerJDBCDBFriends extends SWAServerJDBCDBTable
{

    public SWAServerJDBCDBFriends()
    {
        super();
    }

    @Override
    protected String get_name()
    {
        return "friends";
    }

    @Override
    protected Object read_obj(ResultSet rs)
    {
        SWAServerJDBCFriends ret = null;
        try {
            ret = new SWAServerJDBCFriends(rs.getString("user1"), rs.getString("user2"), rs.getInt("status"));
        } catch (SQLException ex) {
            System.out.println("Server exception: " + ex.getClass() + ":" + ex.getMessage());
        }
        return ret;
    }

    @Override
    protected SWAServerJDBCPredicate[] write_obj(Object obj)
    {
        SWAServerJDBCFriends f = (SWAServerJDBCFriends) obj;

        SWAServerJDBCPredicate[] res = new SWAServerJDBCPredicate[3];
        res[0] = new SWAServerJDBCPredicate("user1", f.user1);
        res[1] = new SWAServerJDBCPredicate("user2", f.user2);
        res[2] = new SWAServerJDBCPredicate("status", f.status);
        
        return res;
    }

}
