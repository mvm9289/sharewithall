/**
 * 
 */
package ShareWithAll.Server.RMI;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Author: mvm9289
 *
 * Creation date: Oct 31, 2010
 */
public interface SWAServerRMIInterface extends Remote
{
    
    public int login(String username, String password, String name, Boolean isPublic) throws RemoteException;
    public void logout(int sessionID) throws RemoteException;
    Boolean newUser(String username, String password) throws RemoteException;
    String getOnlineClients(int sessionID) throws RemoteException;
    
}
