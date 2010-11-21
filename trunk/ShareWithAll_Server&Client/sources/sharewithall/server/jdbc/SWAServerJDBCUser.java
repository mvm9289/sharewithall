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
public class SWAServerJDBCUser
{
    
    String username;
    String password;
    
    public SWAServerJDBCUser()
    {
        super();
    }

    public SWAServerJDBCUser(String username, String password)
    {
        super();
        this.username = username;
        this.password = password;
    }

}
