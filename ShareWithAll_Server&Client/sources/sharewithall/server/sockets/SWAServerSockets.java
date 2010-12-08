package sharewithall.server.sockets;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
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
    private static final int NOTIFY_CLIENTS_CHANGED = 18;
    private static final int NOTIFY_FRIENDS_CHANGED = 19;
    private static final int NOTIFY_INVITATION = 20;
    private static final int RETURN_VALUE = 0;
    private static final int MAX_GATEWAY_SOCKETS = 50;
    private static final int TAM_BUFFER = 4096;
    private static final int EXCEPTION = -1;
    
    private ServerSocket serverSocket;
    private SWAServer server;
    private ConcurrentHashMap<String, ArrayBlockingQueue<Socket>> connections = new ConcurrentHashMap<String, ArrayBlockingQueue<Socket>>();
    ArrayBlockingQueue<ArrayList<Object>> notify = new ArrayBlockingQueue<ArrayList<Object>>(1000);
    
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
        Thread notifier = new SWANotifierThread();
        notifier.start();
        
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
    
    private void cleanSockets(String sessionID) {
        ArrayBlockingQueue<Socket> sockets = connections.get(sessionID);
        if (sockets != null) {
            Socket sock = sockets.poll();
            while (sock != null) {
                try {
                    sock.close();
                }
                catch (IOException e) {}
                sock = sockets.poll();
            }
            connections.remove(sessionID);
        }
    }
    
    public void notifyInvitation(String sessionID) {
        ArrayList<Object> entry = new ArrayList<Object>();
        entry.add(sessionID);
        entry.add(NOTIFY_INVITATION);
        try {
            notify.put(entry);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    public void notifyClientListChanged(String sessionID) {
        ArrayList<Object> entry = new ArrayList<Object>();
        entry.add(sessionID);
        entry.add(NOTIFY_CLIENTS_CHANGED);
        try {
            notify.put(entry);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    public void notifyFriendListChanged(String sessionID) {
        ArrayList<Object> entry = new ArrayList<Object>();
        entry.add(sessionID);
        entry.add(NOTIFY_FRIENDS_CHANGED);
        try {
            notify.put(entry);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    private void notify(String sessionID, int notification) {
        Socket destSocket = null;
        try {
            ArrayBlockingQueue<Socket> sockets = connections.get(sessionID);
            destSocket = sockets.poll();
        }
        catch (Exception e) {

        }
        finally {
            try {
                JDBCDBClients clients = new JDBCDBClients();
                ArrayList<Object> cls = clients.select_gen(new JDBCPredicate("session_id", sessionID));
                JDBCClient client = (JDBCClient)cls.get(0);
                destSocket = new Socket(client.ip, client.port);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (destSocket == null) return;
        try {
            DataOutputStream out = new DataOutputStream(destSocket.getOutputStream());
            out.flush();
            DataInputStream in = new DataInputStream(destSocket.getInputStream());
            out.writeInt(notification);
            out.writeUTF("server");
            out.flush();
            destSocket.shutdownOutput();
            
            int responseCode = in.readInt();
            if (responseCode == EXCEPTION) throw new Exception(in.readUTF());
            destSocket.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        try {
            destSocket.close();
        }
        
        catch (Exception e) {}
    }
    
    private class SWANotifierThread extends Thread
    {
        private SWANotifierThread()
        {
            super();
        }
        
        public void run()
        {
            while (true) {
                ArrayList<Object> entry = notify.poll();
                if (entry != null) {
                    String sessionID = (String)entry.get(0);
                    int notification = (Integer)entry.get(1);
                    SWAServerSockets.this.notify(sessionID, notification);
                }
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
                    
                    for (Enumeration<String> e = connections.keys(); e.hasMoreElements();) {
                        String session_id = e.nextElement();
                        if (!clients.exists_gen(new JDBCPredicate("session_id", session_id)))
                            cleanSockets(session_id);
                    }
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
        private Socket clientSocket;
        private int instruction;
        
        private SWASocketsThread(Socket clientSocket)
        {
            super();
            this.clientSocket = clientSocket;
        }
        
        private void send_gateway(String sessionID, String ip, int port, DataInputStream in1, DataOutputStream out1) throws Exception
        {
            JDBCDBClients cl = new JDBCDBClients();
            ArrayList<Object> clients = cl.select_gen(new JDBCPredicate("session_id", sessionID));
            if (clients.isEmpty()) throw new Exception("Invalid session id");
            
            clients = cl.select_gen(new JDBCPredicate("ip", ip), new JDBCPredicate("port", port));
            if (clients.isEmpty()) throw new Exception("No client exists with that ip/port");
            String client_sessionID = ((JDBCClient)clients.get(0)).session_id;
            
            cl.close();
            
            ArrayBlockingQueue<Socket> sockets = connections.get(client_sessionID);
            if (sockets == null) throw new Exception("Client is not listening to the gateway");
            Socket destSocket = sockets.take();
            
            out1.writeInt(RETURN_VALUE);
            
            DataOutputStream out2 = new DataOutputStream(destSocket.getOutputStream());
            out2.flush();
            DataInputStream in2 = new DataInputStream(destSocket.getInputStream());
            
            byte[] bytes = new byte[TAM_BUFFER];
            int bytesRead;
            while ((bytesRead = in1.read(bytes)) > 0)
            {
                out2.write(bytes, 0, bytesRead);
                out2.flush();
            }
            destSocket.shutdownOutput();
            while ((bytesRead = in2.read(bytes)) > 0)
            {
                out1.write(bytes, 0, bytesRead);
                out1.flush();
            }
            destSocket.close();
        }
        
        void receive_gateway(String sessionID, DataInputStream in, DataOutputStream out) throws Exception 
        {            
            JDBCDBClients cl = new JDBCDBClients();
            ArrayList<Object> clients = cl.select_gen(new JDBCPredicate("session_id", sessionID));
            if (clients.isEmpty()) throw new Exception("Invalid session id");
            
            ArrayBlockingQueue<Socket> sockets = connections.get(sessionID);
            if (sockets == null) {
                sockets = new ArrayBlockingQueue<Socket>(MAX_GATEWAY_SOCKETS);
                connections.put(sessionID, sockets);
            }
            sockets.put(clientSocket);
            out.writeInt(RETURN_VALUE);
        }
        
        private void write_array(String[] a, DataOutputStream out) throws Exception {
            out.writeInt(a.length);
            for (int i = 0; i < a.length; ++i)
                out.writeUTF(a[i]);
        }
        
        private void decodeAndProcess()
        {   
            try
            {
                DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
                out.flush();
                DataInputStream in = new DataInputStream(clientSocket.getInputStream());
                instruction = in.readInt();
                String ret_s;
                String[] ret_a;
                try
                {
                    switch (instruction)
                    {
                        case NEW_USER:
                            server.newUser(in.readUTF(), in.readUTF());
                            out.writeInt(RETURN_VALUE);
                            break;
                        case LOGIN:
                            ret_s = server.login(in.readUTF(), in.readUTF(), in.readUTF(), in.readBoolean(), clientSocket.getInetAddress().getHostAddress());
                            out.writeInt(RETURN_VALUE);
                            out.writeUTF(ret_s);
                            break;
                        case LOGOUT:
                            ret_s = in.readUTF();
                            server.logout(ret_s);
                            cleanSockets(ret_s);
                            out.writeInt(RETURN_VALUE);
                            break;
                        case GET_ONLINE_CLIENTS:
                            ret_a = server.getOnlineClients(in.readUTF());
                            out.writeInt(RETURN_VALUE);
                            write_array(ret_a, out);
                            break;
                        case IP_AND_PORT_REQUEST:
                            ret_s = server.ipAndPortRequest(in.readUTF(), in.readUTF());
                            out.writeInt(RETURN_VALUE);
                            out.writeUTF(ret_s);
                            break;
                        case DECLARE_FRIEND:
                            server.declareFriend(in.readUTF(), in.readUTF());
                            out.writeInt(RETURN_VALUE);
                            break;
                        case IGNORE_USER:
                            server.ignoreUser(in.readUTF(), in.readUTF());
                            out.writeInt(RETURN_VALUE);
                            break;
                        case UPDATE_TIMESTAMP:
                            server.updateTimestamp(in.readUTF());
                            out.writeInt(RETURN_VALUE);
                            break;
                        case PENDING_INVITATIONS_REQUEST:
                            ret_a = server.pendingInvitationsRequest(in.readUTF());
                            out.writeInt(RETURN_VALUE);
                            write_array(ret_a, out);
                            break;
                        case GET_LIST_OF_FRIENDS:
                            ret_a = server.getListOfFriends(in.readUTF(), in.readInt());
                            out.writeInt(RETURN_VALUE);
                            write_array(ret_a, out);
                            break;
                        case CLIENT_NAME_REQUEST:
                            ret_s = server.clientNameRequest(in.readUTF(), in.readUTF());
                            out.writeInt(RETURN_VALUE);
                            out.writeUTF(ret_s);
                            break;
                        case GET_SEND_TOKEN:
                            ret_s = server.getSendToken(in.readUTF(), in.readUTF());
                            out.writeInt(RETURN_VALUE);
                            out.writeUTF(ret_s);
                            break;
                        case SEND_GATEWAY:
                            send_gateway(in.readUTF(), in.readUTF(), in.readInt(), in, out);
                            break;
                        case RECEIVE_GATEWAY:
                            receive_gateway(in.readUTF(), in, out);
                            break;
                        default:
                        	throw new Exception("Wrong instruction identifier.");
                    }
                }
                catch (Exception e)
                {
                    out.writeInt(EXCEPTION);
                    if (e.getClass() == Exception.class) out.writeUTF(e.getMessage());
                    else {
                        e.printStackTrace();
                        out.writeUTF("Server Exception");
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

