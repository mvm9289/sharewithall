/**
 * 
 */
package ShareWithAll.Server.RMI;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import ShareWithAll.Server.JDBC.SWAServerJDBCDBUsers;
import ShareWithAll.Server.JDBC.SWAServerJDBCUser;

/**
 * Author: mvm9289
 *
 * Creation date: Oct 31, 2010
 */
@SuppressWarnings("serial")
public class SWAServerRMIImplementation extends UnicastRemoteObject implements SWAServerRMIInterface
{

    protected SWAServerRMIImplementation() throws RemoteException
    {
        super();
    }

    @Override
    public String getOnlineClients(int sessionID) throws RemoteException
    {
        return "getOnlineClients";
    }

    @Override
    public int login(String username, String password, String name, Boolean isPublic) throws RemoteException
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void logout(int sessionID) throws RemoteException
    {
        // TODO Auto-generated method stub

    }

    @Override
    public Boolean newUser(String username, String password) throws RemoteException
    {
        try
        {
            SWAServerJDBCDBUsers DBUsers = new SWAServerJDBCDBUsers();
            DBUsers.insert_obj(new SWAServerJDBCUser(username, password));
            
            return new Boolean(true);
        }
        catch (Exception e)
        {
            return new Boolean(false);
        }
    }

}
