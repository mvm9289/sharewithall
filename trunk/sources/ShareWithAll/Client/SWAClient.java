/**
 * 
 */
package ShareWithAll.Client;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import ShareWithAll.Server.RMI.SWAServerRMIInterface;

/**
 * Author: mvm9289
 *
 * Creation date: Oct 31, 2010
 */
public class SWAClient
{

    public static void main(String[] args)
    {
        try
        {
        	Registry registry = LocateRegistry.getRegistry("192.168.1.122", 4040);
        	SWAServerRMIInterface SWASI = (SWAServerRMIInterface)registry.lookup("SWAService");
            //SWAServerRMIInterface SWASI = (SWAServerRMIInterface)Naming.lookup("rmi://192.168.1.122:4040/SWAService");
            System.out.println(SWASI.getOnlineClients(0));
            System.out.println(SWASI.newUser("mvm9289", "mvm9289"));
            SWASI.logout(0);
        }
        catch (Exception e)
        {
            System.out.println("Error: " + e.getMessage());
        }
    }

}
