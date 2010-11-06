/**
 * 
 */
package sharewithall.client;

import sharewithall.client.sockets.SWAClientSockets;

/**
 * Authors:
 *    Alex Catarineu
 *    Ferran Rigual
 *    Miguel Angel Vico
 *
 * Creation date: Oct 31, 2010
 */
public class SWAClient
{
    
    private static final String serverIP = "mvm9289.dyndns.org";
    private static final int serverPort = 4040;

    public static void main(String[] args)
    {
        SWAClientSockets socketsModule = new SWAClientSockets(serverIP, serverPort);
        
        try
        {
        	System.out.println(socketsModule.getOnlineClients(0));
            System.out.println(socketsModule.newUser("mvm9289", "mvm9289"));
            socketsModule.logout(0);
        }
        catch (Exception e)
        {
            System.out.println("Exception: " + e.getMessage());
        }
    }

}
