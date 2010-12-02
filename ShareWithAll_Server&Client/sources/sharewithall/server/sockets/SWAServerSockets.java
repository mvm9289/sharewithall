package sharewithall.server.sockets;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

import sharewithall.server.SWAServer;
import sharewithall.server.jdbc.JDBCClient;
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
    private ConcurrentHashMap<String, ArrayBlockingQueue<Socket>> connections = new ConcurrentHashMap<String, ArrayBlockingQueue<Socket>>();
    
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
        private static final int SEND_GATEWAY = 16;
        private static final int RECEIVE_GATEWAY = 17;
        private static final int RETURN_VALUE = 0;
        private static final int EXCEPTION = -1;
        
        private Socket clientSocket;
        private int instruction;
        
        private SWASocketsThread(Socket clientSocket)
        {
            super();
            this.clientSocket = clientSocket;
        }
        
        private void send_gateway(Object[] params, ObjectInputStream in1, ObjectOutputStream out1) throws Exception
        {
            String sessionID = (String)params[0];
            String ip = (String)params[1];
            int port = (Integer)params[2];
            
            JDBCDBClients cl = new JDBCDBClients();
            ArrayList<Object> clients = cl.select_gen(new JDBCPredicate("session_id", sessionID));
            if (clients.isEmpty()) throw new Exception("Invalid session id");
            
            clients = cl.select_gen(new JDBCPredicate("ip", ip), new JDBCPredicate("port", port));
            if (clients.isEmpty()) throw new Exception("No client exists with that ip/port");
            String client_sessionID = ((JDBCClient)clients.get(0)).session_id;
            
            cl.close();
            
            ArrayBlockingQueue<Socket> sockets = connections.get(client_sessionID);
            if (sockets == null) throw new Exception("Client is not listening to the gateway");
            Socket destSocket = sockets.poll();
            if (destSocket == null) throw new Exception("Client is not listening to the gateway");
            
            ObjectOutputStream out2 = new ObjectOutputStream(destSocket.getOutputStream());
            out2.flush();
            ObjectInputStream in2 = new ObjectInputStream(destSocket.getInputStream());
            
            byte[] bytes = new byte[4096];
            while (true) {
                int bytesRead = in1.read(bytes);
                if (bytesRead == -1) break;
                out2.write(bytes, 0, bytesRead);
                out2.flush();
            }
            while (true) {
                int bytesRead = in2.read(bytes);
                if (bytesRead == -1) break;
                out1.write(bytes, 0, bytesRead);
                out1.flush();
            }
            destSocket.close();
        }
        
        void receive_gateway(Object[] params, ObjectInputStream in, ObjectOutputStream out) throws Exception 
        {
            String sessionID = (String)params[0];
            
            JDBCDBClients cl = new JDBCDBClients();
            ArrayList<Object> clients = cl.select_gen(new JDBCPredicate("session_id", sessionID));
            if (clients.isEmpty()) throw new Exception("Invalid session id");
            
            ArrayBlockingQueue<Socket> sockets = connections.get(sessionID);
            if (sockets == null) {
                sockets = new ArrayBlockingQueue<Socket>(10);
                connections.put(sessionID, sockets);
            }
            sockets.put(clientSocket);
            out.writeInt(RETURN_VALUE);
            out.writeObject(null);
        }
        
        private void decodeAndProcess()
        {   
            try
            {
                ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
                out.flush();
                ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
                instruction = in.readInt();
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
                        case SEND_GATEWAY:
                            send_gateway(params, in, out);
                            break;
                        case RECEIVE_GATEWAY:
                            receive_gateway(params, in, out);
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
                if (instruction != RECEIVE_GATEWAY) clientSocket.close();
            }
            catch (Exception e)
            {
                System.out.println("Server exception: " + e.getClass() + ":" + e.getMessage());
            }
        }
        
    }

}

