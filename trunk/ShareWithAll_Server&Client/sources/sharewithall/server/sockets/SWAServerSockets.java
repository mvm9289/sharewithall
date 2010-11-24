/**
 * 
 */
package sharewithall.server.sockets;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Timestamp;
import java.util.Date;

import sharewithall.server.SWAServer;
import sharewithall.server.jdbc.JDBCDBClients;
import sharewithall.server.jdbc.JDBCPredicate;

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
        new SWACleanerThread();
        
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
    
    private class SWACleanerThread extends Thread
    {
        private SWACleanerThread()
        {
            super();
            run();
        }
        
        public void run()
        {
            while (true) {
                JDBCDBClients clients = new JDBCDBClients();
                Timestamp last_time = new Timestamp((new Date()).getTime() - 10000);
                try
                {
                    System.out.println(clients.delete_gen(new JDBCPredicate("last_time", last_time, "<")) + " clientes eliminados");
                    clients.commit();
                } catch (Exception e1)
                {
                    e1.printStackTrace();
                }
                clients.close();
                
                Runtime.getRuntime().gc();
                
                try {
                    SWACleanerThread.sleep(10000);
                }
                catch(InterruptedException e) {
                    System.out.println("Cleaner thread interrupted");
                }
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
        private static final int GET_LIST_OF_FRIENDS = 10;
        private static final int CLIENT_NAME_REQUEST = 11;
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
                                    String.valueOf(server.login(petition[1], petition[2], petition[3], Boolean.valueOf(petition[4]).booleanValue(), clientSocket.getInetAddress().toString().substring(1))));
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
                        case GET_LIST_OF_FRIENDS:
                            out.writeUTF(String.valueOf(RETURN_VALUE) + ";" + server.getListOfFriends(petition[1], Integer.valueOf(petition[2])));
                            break;
                        case CLIENT_NAME_REQUEST:
                            out.writeUTF(String.valueOf(RETURN_VALUE) + ";" + server.clientNameRequest(petition[1], petition[2], Integer.valueOf(petition[3])));
                            break;
                        default:
                        	throw new Exception("Wrong instruction identifier.");
                    }
                }
                catch (Exception e)
                {
                    if (e.getClass() == Exception.class) out.writeUTF(String.valueOf(EXCEPTION) + ";" + e.getMessage());
                    else out.writeUTF(String.valueOf(EXCEPTION) + ";Server Exception");
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

