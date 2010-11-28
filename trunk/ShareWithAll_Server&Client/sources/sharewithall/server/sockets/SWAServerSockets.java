package sharewithall.server.sockets;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
        }
        catch (Exception e)
        {
            System.out.println("Server exception: " + e.getClass() + ":" + e.getMessage());
        }
    }

    public void run()
    {
        Thread cleaner = new SWACleanerThread();
        cleaner.start();
        
        while (true)
        {
            try
            {
                Socket clientSocket = serverSocket.accept();
                Thread socket_thread = new SWASocketsThread(clientSocket);
                socket_thread.start();
            }
            catch (Exception e)
            {
                System.out.println("Server exception: " + e.getClass() + ":" + e.getMessage());
            }
        }
    }
    
    private class SWACleanerThread extends Thread
    {
        private static final int CLEANER_SLEEP_TIME = 30000;
        private static final int CLIENT_EXP_TIME = 60000;
        
        private SWACleanerThread()
        {
            super();
        }
        
        public void run()
        {
            while (true) {
                JDBCDBClients clients = new JDBCDBClients();
                Timestamp last_time = new Timestamp((new Date()).getTime() - CLIENT_EXP_TIME);
                try {
                    int nClients = clients.delete_gen(new JDBCPredicate("last_time", last_time, "<"));
                    clients.commit();
                    if (nClients > 0) System.out.println(nClients + " clientes eliminados");
                }
                catch (Exception e1) {
                    e1.printStackTrace();
                }
                finally {
                    clients.close();
                }
                
                Runtime.getRuntime().gc();
                
                try {
                    SWACleanerThread.sleep(CLEANER_SLEEP_TIME);
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
        private static final int GET_SEND_TOKEN = 15;
        private static final int OBTAIN_EMISSOR = 16;
        private static final int RETURN_VALUE = 0;
        private static final int EXCEPTION = -1;
        
        private Socket clientSocket;
        
        private SWASocketsThread(Socket clientSocket)
        {
            super();
            this.clientSocket = clientSocket;
        }
        
        private void decodeAndProcess()
        {   
            try
            {
                ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
                out.flush();
                ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
                int instruction = in.readInt();
                Object[] params = (Object[])in.readObject();
                Object ret;
                try
                {
                    switch (instruction)
                    {
                        case NEW_USER:
                            server.newUser(params);
                            out.writeInt(RETURN_VALUE);
                            out.writeObject(null);
                            break;
                        case LOGIN:
                            ret = server.login(params, clientSocket.getInetAddress().getHostAddress());
                            out.writeInt(RETURN_VALUE);
                            out.writeObject(ret);
                            break;
                        case LOGOUT:
                            server.logout(params);
                            out.writeInt(RETURN_VALUE);
                            out.writeObject(null);
                            break;
                        case GET_ONLINE_CLIENTS:
                            ret = server.getOnlineClients(params);
                            out.writeInt(RETURN_VALUE);
                            out.writeObject(ret);
                            break;
                        case IP_AND_PORT_REQUEST:
                            ret = server.ipAndPortRequest(params);
                            out.writeInt(RETURN_VALUE);
                            out.writeObject(ret);
                            break;
                        case DECLARE_FRIEND:
                            server.declareFriend(params);
                            out.writeInt(RETURN_VALUE);
                            out.writeObject(null);
                            break;
                        case IGNORE_USER:
                            server.ignoreUser(params);
                            out.writeInt(RETURN_VALUE);
                            out.writeObject(null);
                            break;
                        case UPDATE_TIMESTAMP:
                            server.updateTimestamp(params);
                            out.writeInt(RETURN_VALUE);
                            out.writeObject(null);
                            break;
                        case PENDING_INVITATIONS_REQUEST:
                            ret = server.pendingInvitationsRequest(params);
                            out.writeInt(RETURN_VALUE);
                            out.writeObject(ret);
                            break;
                        case GET_LIST_OF_FRIENDS:
                            ret = server.getListOfFriends(params);
                            out.writeInt(RETURN_VALUE);
                            out.writeObject(ret);
                            break;
                        case CLIENT_NAME_REQUEST:
                            ret = server.clientNameRequest(params);
                            out.writeInt(RETURN_VALUE);
                            out.writeObject(ret);
                            break;
                        case GET_SEND_TOKEN:
                            ret = server.getSendToken(params);
                            out.writeInt(RETURN_VALUE);
                            out.writeObject(ret);
                            break;
                        case OBTAIN_EMISSOR:
                            ret = server.obtainEmissor(params);
                            out.writeInt(RETURN_VALUE);
                            out.writeObject(ret);
                            break;
                        default:
                        	throw new Exception("Wrong instruction identifier.");
                    }
                }
                catch (Exception e)
                {
                    out.writeInt(EXCEPTION);
                    if (e.getClass() == Exception.class) out.writeObject(e.getMessage());
                    else {
                        e.printStackTrace();
                        out.writeObject("Server Exception");
                    }
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
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

