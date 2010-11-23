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
public class JDBCDBUsers extends JDBCDBTable
{

    public JDBCDBUsers()
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
        JDBCUser ret = null;
        try {
            ret = new JDBCUser(rs.getString("username"), rs.getString("password"));
        } catch (SQLException ex) {
            System.out.println("Server exception: " + ex.getClass() + ":" + ex.getMessage());
        }
        return ret;
    }

    @Override
    protected JDBCPredicate[] write_obj(Object obj)
    {
        JDBCUser u = (JDBCUser) obj;

        JDBCPredicate[] res = new JDBCPredicate[2];
        res[0] = new JDBCPredicate("username", u.username);
        res[1] = new JDBCPredicate("password", u.password);

        return res;
    }

}
