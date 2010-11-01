/**
 * 
 */
package ShareWithAll.Client;

import java.rmi.Naming;
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
        	SWAServerRMIInterface SWASI = (SWAServerRMIInterface)Naming.lookup("rmi://192.168.1.122:4040/SWAService");
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
