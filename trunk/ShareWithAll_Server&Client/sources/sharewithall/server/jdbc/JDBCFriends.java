/**
 * 
 */
package sharewithall.server.jdbc;

/**
 * Authors:
 *    Alex Catarineu
 *    Ferran Rigual
 *    Miguel Angel Vico
 *
 * Creation date: Oct 31, 2010
 */
public class JDBCFriends
{
    
    public String user1;
    public String user2;
    public int status;
    
    public JDBCFriends()
    {
        super();
    }

    public JDBCFriends(String user1, String user2, int status)
    {
        super();
        this.user1 = user1;
        this.user2 = user2;
        this.status = status;
    }

}
