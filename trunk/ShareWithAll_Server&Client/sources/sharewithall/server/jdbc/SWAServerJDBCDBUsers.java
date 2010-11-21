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
public class SWAServerJDBCDBUsers extends SWAServerJDBCDBTable
{

    public SWAServerJDBCDBUsers()
    {
        super();
    }

    @Override
    protected String get_name()
    {
        return "users";
    }

    @Override
    protected Object read_obj(ResultSet rs)
    {
        SWAServerJDBCUser ret = null;
        try {
            ret = new SWAServerJDBCUser(rs.getString("username"), rs.getString("password"));
        } catch (SQLException ex) {
            System.out.println("Server exception: " + ex.getClass() + ":" + ex.getMessage());
        }
        return ret;
    }

    @Override
    protected SWAServerJDBCPredicate[] write_obj(Object obj)
    {
        SWAServerJDBCUser u = (SWAServerJDBCUser) obj;

        SWAServerJDBCPredicate[] res = new SWAServerJDBCPredicate[2];
        res[0] = new SWAServerJDBCPredicate("username", u.username);
        res[1] = new SWAServerJDBCPredicate("password", u.password);

        return res;
    }

}
