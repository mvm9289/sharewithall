/**
 * 
 */
package sharewithall.server;

import sharewithall.server.jdbc.SWAServerJDBCDBUsers;
import sharewithall.server.jdbc.SWAServerJDBCUser;
import sharewithall.server.sockets.SWAServerSockets;

/**
 * Authors:
 *    Alex Catarineu
 *    Ferran Rigual
 *    Miguel Angel Vico
 *
 * Creation date: Nov 6, 2010
 */
public class SWAServer
{
    
    private static final int DEFAULT_SERVER_PORT = 4040;
    
    @SuppressWarnings("unused")
    private SWAServerSockets socketsModule;
    
    public SWAServer()
    {
        super();
        socketsModule = new SWAServerSockets(DEFAULT_SERVER_PORT, this);
    }
    
    public boolean newUser(String username, String password) throws Exception
    {
        SWAServerJDBCDBUsers DBUsers = new SWAServerJDBCDBUsers();
        DBUsers.insert_obj(new SWAServerJDBCUser(username, password));
        DBUsers.commit();
        
        return true;
    }
    
    public int login(String username, String password, String name, boolean isPublic)
    {
        return 0;
    }
    
    public void logout(int sessionID)
    {
        
    }
    
    public String getOnlineClients(int sessionID)
    {
        return "getOnlineClients called";
    }
    
    public String ipAndPortRequest(int sessionID, String client)
    {
        return "ipAndPortRequest called";
    }
    
    public boolean sendInvitation(int sessionID, String friend)
    {
        return true;
    }
    
    public static void main(String[] args)
    {
        new SWAServer();
    }

}
