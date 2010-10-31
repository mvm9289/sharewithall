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
public class SWAServerJDBCDBUsers extends SWAServerJDBCDBTable
{

    public SWAServerJDBCDBUsers() throws FileNotFoundException, IOException, ClassNotFoundException, SQLException
    {
        super();
    }

    @Override
    protected String get_name()
    {
        return "Users";
    }

    @Override
    protected Object read_obj(ResultSet rs)
    {
        SWAServerJDBCUser ret = null;
        try {
            ret = new SWAServerJDBCUser(rs.getString("username"), rs.getString("password"));
        } catch (SQLException ex) {
            Logger.getLogger(SWAServerJDBCDBUsers.class.getName()).log(Level.SEVERE, null, ex);
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
