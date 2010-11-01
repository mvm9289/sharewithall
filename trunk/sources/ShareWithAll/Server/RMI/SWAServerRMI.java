/**
 * 
 */
package ShareWithAll.Server.RMI;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

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
            SWAServerRMIInterface SWASI = new SWAServerRMIImplementation();
        	Registry registry = LocateRegistry.getRegistry(4040);
        	registry.rebind("SWAService", SWASI);
            //Naming.rebind("rmi://localhost:4040/SWAService", SWASI);
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
