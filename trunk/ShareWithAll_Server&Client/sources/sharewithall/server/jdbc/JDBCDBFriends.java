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
public class JDBCDBFriends extends JDBCDBTable
{

    public JDBCDBFriends()
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
        JDBCFriends ret = null;
        try {
            ret = new JDBCFriends(rs.getString("user1"), rs.getString("user2"), rs.getInt("status"));
        } catch (SQLException ex) {
            System.out.println("Server exception: " + ex.getClass() + ":" + ex.getMessage());
        }
        return ret;
    }

    @Override
    protected JDBCPredicate[] write_obj(Object obj)
    {
        JDBCFriends f = (JDBCFriends) obj;

        JDBCPredicate[] res = new JDBCPredicate[3];
        res[0] = new JDBCPredicate("user1", f.user1);
        res[1] = new JDBCPredicate("user2", f.user2);
        res[2] = new JDBCPredicate("status", f.status);
        
        return res;
    }

}
