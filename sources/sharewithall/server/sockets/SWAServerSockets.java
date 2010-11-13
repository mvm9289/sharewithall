/**
 * 
 */
package sharewithall.server.sockets;

import java.io.DataInputStream;
import java.io.DataOutputStream;
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
public class SWAServerSockets extends Thread
{

    private ServerSocket serverSocket;
    private SWAServer server;
    
    public SWAServerSockets(int port, SWAServer server)
    {
        super();
        try
        {
            serverSocket = new ServerSocket(port);
            this.server = server;
            run();
        }
        catch (Exception e)
        {
            System.out.println("Server exception: " + e.getClass() + ":" + e.getMessage());
        }
    }

    public void run()
    {
        while (true)
        {
            try
            {
                Socket clientSocket = serverSocket.accept();
                new SWASocketsThread(clientSocket);
            }
            catch (Exception e)
            {
                System.out.println("Server exception: " + e.getClass() + ":" + e.getMessage());
            }
        }
    }
    
    private class SWASocketsThread extends Thread
    {

        private static final int NEW_USER = 1;
        private static final int LOGIN = 2;
        private static final int LOGOUT = 3;
        private static final int GET_ONLINE_CLIENTS = 4;
        private static final int IP_AND_PORT_REQUEST = 5;
        private static final int DECLARE_FRIEND = 6;
        private static final int UPDATE_TIMESTAMP = 7;
        private static final int IGNORE_USER = 8;
        private static final int PENDING_INVITATIONS_REQUEST = 9;
        private static final int SHOW_LIST_OF_FRIENDS = 10;
        private static final int RETURN_VALUE = 0;
        private static final int EXCEPTION = -1;
        
        private Socket clientSocket;
        
        private SWASocketsThread(Socket clientSocket)
        {
            super();
            this.clientSocket = clientSocket;
            run();
        }
        
        private void decodeAndProcess()
        {   
            try
            {
                DataInputStream in = new DataInputStream(clientSocket.getInputStream());
                DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
                String[] petition = in.readUTF().split(";");
                
                int instruction = Integer.valueOf(petition[0]).intValue();
                
                try
                {
                    switch (instruction)
                    {
                        case NEW_USER:
                            server.newUser(petition[1], petition[2]);
                            out.writeUTF(String.valueOf(RETURN_VALUE));
                            break;
                        case LOGIN:
                            out.writeUTF(String.valueOf(RETURN_VALUE) + ";" +
                                    String.valueOf(server.login(petition[1], petition[2], petition[3], Boolean.valueOf(petition[4]).booleanValue(), clientSocket.getInetAddress().toString())));
                            break;
                        case LOGOUT:
                            server.logout(petition[1]);
                            out.writeUTF(String.valueOf(RETURN_VALUE));
                            break;
                        case GET_ONLINE_CLIENTS:
                            out.writeUTF(String.valueOf(RETURN_VALUE) + ";" + server.getOnlineClients(petition[1]));
                            break;
                        case IP_AND_PORT_REQUEST:
                            out.writeUTF(String.valueOf(RETURN_VALUE) + ";" + server.ipAndPortRequest(petition[1],petition[2]));
                            break;
                        case DECLARE_FRIEND:
                            server.declareFriend(petition[1], petition[2]);
                            out.writeUTF(String.valueOf(RETURN_VALUE));
                            break;
                        case IGNORE_USER:
                            server.ignoreUser(petition[1], petition[2]);
                            out.writeUTF(String.valueOf(RETURN_VALUE));
                            break;
                        case UPDATE_TIMESTAMP:
                            server.updateTimestamp(petition[1]);
                            out.writeUTF(String.valueOf(RETURN_VALUE));
                            break;
                        case PENDING_INVITATIONS_REQUEST:
                            out.writeUTF(String.valueOf(RETURN_VALUE) + ";" + server.pendingInvitationsRequest(petition[1]));
                            break;
                        case SHOW_LIST_OF_FRIENDS:
                            out.writeUTF(String.valueOf(RETURN_VALUE) + ";" + server.showListOfFriends(petition[1]));
                            break;
                        default:
                            //TODO: informar error?
                            break;
                    }
                }
                catch (Exception e)
                {
                    out.writeUTF(String.valueOf(EXCEPTION) + ";" + e.getMessage());
                }
            }
            catch (Exception e)
            {
                System.out.println("Server exception: " + e.getClass() + ":" + e.getMessage());
            }
        }
        
        public void run()
        {
            try
            {
                decodeAndProcess();
                clientSocket.close();
            }
            catch (Exception e)
            {
                System.out.println("Server exception: " + e.getClass() + ":" + e.getMessage());
            }
        }
        
    }

}
