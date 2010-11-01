/**
 * 
 */
package ShareWithAll.Server.RMI;

import java.rmi.Naming;
import java.rmi.RMISecurityManager;

/**
 * Author: mvm9289
 *
 * Creation date: Oct 31, 2010
 */
public class SWAServerRMI
{

    public SWAServerRMI()
    {
        super();
        try
        {
        	if (System.getSecurityManager() == null) System.setSecurityManager(new RMISecurityManager());
            SWAServerRMIInterface SWASI = new SWAServerRMIImplementation();
            Naming.bind("SWAService", SWASI);
        }
        catch (Exception e)
        {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public static void main(String[] args)
    {
        new SWAServerRMI();
    }

}
