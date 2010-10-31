/**
 * 
 */
package ShareWithAll.Server.JDBC;

import java.sql.Timestamp;

/**
 * Author: a.catarineu
 *
 * Creation date: Oct 31, 2010
 */
public class SWAServerJDBCClient
{
    
    public String ip;
    public int port;
    public String name;
    public boolean is_public;
    public Timestamp last_time;
    public String username;
    
    public SWAServerJDBCClient()
    {
        super();
    }
    
    public SWAServerJDBCClient(String ip, int port, String name, boolean is_public, Timestamp last_time, String username)
    {
        super();
        this.ip = ip;
        this.port = port;
        this.name = name;
        this.is_public = is_public;
        this.last_time = last_time;
        this.username = username;
    }

}
