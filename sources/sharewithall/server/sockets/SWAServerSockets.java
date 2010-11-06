/**
 * 
 */
package sharewithall.server.sockets;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import sharewithall.server.SWAServer;

/**
 * Authors:
 *    Alex Catarineu
 *    Ferran Rigual
 *    Miguel Angel Vico
 *
 * Creation date: Oct 31, 2010
 */
public class SWAServerSockets
{

    private static final int NEW_USER = 1;
    private static final int LOGIN = 2;
    private static final int LOGOUT = 3;
    private static final int GET_ONLINE_CLIENTS = 4;
    private static final int IP_AND_PORT_REQUEST = 5;
    private static final int SEND_INVITATION = 6;
    private static final int RETURN_VALUE = 0;
    private static final int EXCEPTION = -1;
    
    private SWAServerThread thread;
    private ServerSocket serverSocket;
    private SWAServer server;
    
    public SWAServerSockets(int port, SWAServer server)
    {
        super();
        try
        {
            serverSocket = new ServerSocket(port);
            this.server = server;
            thread = new SWAServerThread();
            thread.start();
        }
        catch (Exception e)
        {
            System.out.println("Exception: " + e.getMessage());
        }
    }

    public void decodeAndProcess(Socket clientSocket)
    {   
        try
        {
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            String[] petition = in.readLine().split(";");
            
            int instruction = Integer.valueOf(petition[0]).intValue();
            
            try
            {
                switch (instruction)
                {
                    case NEW_USER:
                        out.print(String.valueOf(RETURN_VALUE) + ";" +
                                String.valueOf(server.newUser(petition[1], petition[2])));
                        break;
                    case LOGIN:
                        out.print(String.valueOf(RETURN_VALUE) + ";" +
                                String.valueOf(server.login(petition[1], petition[2], petition[3],
                                        Boolean.valueOf(petition[4]).booleanValue())));
                        break;
                    case LOGOUT:
                        server.logout(Integer.valueOf(petition[1]).intValue());
                        out.print(String.valueOf(RETURN_VALUE));
                        break;
                    case GET_ONLINE_CLIENTS:
                        out.print(String.valueOf(RETURN_VALUE) + ";" +
                                server.getOnlineClients(Integer.valueOf(petition[1])));
                        break;
                    case IP_AND_PORT_REQUEST:
                        out.print(String.valueOf(RETURN_VALUE) + ";" +
                                server.ipAndPortRequest(Integer.valueOf(petition[1]).intValue(),
                                        petition[2]));
                        break;
                    case SEND_INVITATION:
                        out.print(String.valueOf(RETURN_VALUE) + ";" +
                                String.valueOf(server.sendInvitation(Integer.valueOf(petition[1]).intValue(),
                                        petition[2])));
                        break;
                    default:
                        break;
                }
            }
            catch (Exception e)
            {
                out.print(String.valueOf(EXCEPTION) + ";" + e.getMessage());
            }
        }
        catch (Exception e)
        {
            System.out.println("Exception: " + e.getMessage());
        }
    }
    
    
    private class SWAServerThread extends Thread
    {
        
        public SWAServerThread()
        {
            super();
        }
        
        public void run()
        {
            while(true)
            {
                try
                {
                    Socket clientSocket = serverSocket.accept();
                    decodeAndProcess(clientSocket);
                    clientSocket.close();
                }
                catch (Exception e)
                {
                    System.out.println("Exception: " + e.getMessage());
                }
            }
        }
        
    }

}
