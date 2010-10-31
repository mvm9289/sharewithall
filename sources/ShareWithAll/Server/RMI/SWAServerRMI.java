/**
 * 
 */
package ShareWithAll.Server.RMI;

import java.rmi.Naming;

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
            Naming.rebind("rmi://localhost:4040/SWAService", SWASI);
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
