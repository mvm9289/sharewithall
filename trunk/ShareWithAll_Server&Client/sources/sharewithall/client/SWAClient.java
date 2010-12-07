
package sharewithall.client;

import java.net.Socket;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

import sharewithall.client.sockets.SWAReceiveClientSockets;
import sharewithall.client.sockets.SWASendSockets;
import sharewithall.server.jdbc.JDBCPredicate;

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
    private enum SendOperation {
        SEND_URL, SEND_TEXT, SEND_FILE
    }
    public static final String DEFAULT_SERVER_IP = ShareWithAll.DEFAULT_SERVER_IP;
    public static final int DEFAULT_SERVER_PORT = ShareWithAll.DEFAULT_SERVER_PORT;
    public static final int PROPERTY_FRIENDS = SWASendSockets.PROPERTY_FRIENDS;
    public static final int PROPERTY_DECLARED_FRIEND = SWASendSockets.PROPERTY_DECLARED_FRIEND;
    public static final int PROPERTY_EXPECTING = SWASendSockets.PROPERTY_EXPECTING;
    public static final int PROPERTY_IGNORED = SWASendSockets.PROPERTY_IGNORED;
    public static final int MAX_SENDS = 5;
    public String serverIP;
    public int serverPort;
    public MainGraphicalInterface program = null;
    public boolean gateway = true;
    public boolean receive_files = true;
    public boolean open_links = true;
    private SWAReceiveClientSockets receiveSocketsModule;
    private String sessionID;
    private Object lock = new Object();
    private Semaphore sem = new Semaphore(MAX_SENDS); 
    private HashMap<String, ArrayList<String>> cache_clients = new HashMap<String, ArrayList<String>>();
    public String username;
    
    private class SWAUpdateThread extends Thread
    {
        private static final int SLEEP_TIME = 30000;
        private SWASendSockets updateSocketsModule;

        private SWAUpdateThread()
        {
            super();
            updateSocketsModule = new SWASendSockets(serverIP, serverPort);
        }
        
        public void run()
        {
            while (true) {
                try
                {
                    synchronized(lock) {
                        if (sessionID != null) {
                            updateSocketsModule.updateTimestamp(sessionID);
                        }
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
                
                Runtime.getRuntime().gc();
                
                try {
                    SWAUpdateThread.sleep(SLEEP_TIME);
                }
                catch(InterruptedException e) {
                    System.out.println("Update thread interrupted");
                }
            }
        }
    }
    private class SWASendThread extends Thread
    {
        private SWASendSockets socketsModule;
        private SendOperation op;
        private Object[] params;
        private SWASendThread(SendOperation op, Object... params)
        {
            super();
            this.socketsModule = new SWASendSockets(serverIP, serverPort);
            this.op = op;
            this.params = params;
        }
        public void run()
        {
            try {
                sem.acquire();
                String sessionID = (String)params[0];
                String token = (String)params[1];
                String ip = (String)params[2];
                int port = (Integer)params[3];
                String data = (String)params[4];
                
                switch (op) {
                    case SEND_FILE:
                        socketsModule.sendFile(sessionID, token, ip, port, data);
                        break;
                    case SEND_TEXT:
                        socketsModule.sendText(sessionID, token, ip, port, data);
                        break;
                    case SEND_URL:
                        socketsModule.sendURL(sessionID, token, ip, port, data);
                        break;
                    default:
                        break;
                }
            }
            catch (Exception e) {
                threadException(op, e);
            }
            finally {
                sem.release();
            }
        }
    }
    private void threadException(SendOperation op, Exception e) {
        switch (op) {
            case SEND_FILE:
                if (e.getClass() == Exception.class) program.showErrorMessage("Send file error", e.getMessage());
                else program.showErrorMessage("Send file error", "Error in the file transfer");
                break;
            case SEND_TEXT:
                if (e.getClass() == Exception.class) program.showErrorMessage("Send text error", e.getMessage());
                else program.showErrorMessage("Send text error", "Error sending the text message");
                break;
            case SEND_URL:
                if (e.getClass() == Exception.class) program.showErrorMessage("Send URL error", e.getMessage());
                else program.showErrorMessage("Send URL error", "Error sending the URL");
                break;
        }
        
        e.printStackTrace();
    }
    public SWAClient(String serverIP, int serverPort)
    {
        super();
        
        this.serverIP = serverIP;
        this.serverPort = serverPort;
        
        //Esto me daba problemas para leer, lo he comentado (alex)
        //sc.useDelimiter("[\\s]");
        Thread update = new SWAUpdateThread();
        update.start();
        //SWAClientLoop();
    }
    
    public String getSessionID() {
        return sessionID;
    }
    
    public void RefreshListOfFriends() {
        program.RefreshListOfFriends();
        System.out.println("Refresh list of friends");
    }
    
    public void RefreshListOfOnlineClients() {
        program.RefreshListOfOnlineClients();
        System.out.println("Refresh online clients");
    }
    
    public void RefreshInvitations() {
        System.out.println("Refresh invitations");
    }
    
    public void newUserCommand(String username, String password) throws Exception
    {
        try
        {
            SWASendSockets socketsModule = new SWASendSockets(serverIP, serverPort);
            socketsModule.newUser(username, password);
        }
        catch (Exception e)
        {
            if (e.getClass() == Exception.class) throw e;
            else throw new Exception("Error");
        }
    }
    
    public void loginCommand(String username, String password, String name, boolean isPublic) throws Exception
    {
        try
        {
            SWASendSockets socketsModule = new SWASendSockets(serverIP, serverPort);
            sessionID = socketsModule.login(username, password, name, isPublic);
            ////////// HE ANADIDO ESTO, PONEDLO EN UNA FUNCION SI QUEREIS PERO TIENE QUE IR AQUI!!!!
            String[] myIPandPort = socketsModule.ipAndPortRequest(sessionID, name);
            int port = Integer.valueOf(myIPandPort[1]).intValue();
            this.username = username;
            
            if (gateway) receiveSocketsModule = new SWAReceiveClientSockets(serverIP, serverPort, this);
            else receiveSocketsModule = new SWAReceiveClientSockets(port, this);
            receiveSocketsModule.start();
        }
        catch (Exception e)
        {
            if (e.getClass() == Exception.class) throw e;
            else throw new Exception("Error");
        }
    }
    
    public String[] getOnlineClientsCommand() throws Exception
    {
        try
        {
            SWASendSockets socketsModule = new SWASendSockets(serverIP, serverPort);
            String[] res = socketsModule.getOnlineClients(sessionID);
            
            for (Iterator<String> it = cache_clients.keySet().iterator(); it.hasNext();) {
                String key = it.next();
                String[] key_split = key.split(":");
                String searchkey = key;
                if (key_split[0].equals(username)) searchkey = key_split[1];
                
                boolean found = false;
                for (int i = 0; !found && i < res.length; ++i)
                    if (res[i].equals(searchkey))
                        found = true;
                if (!found) cache_clients.remove(key);
            }
            
            return res;
        }
        catch (Exception e)
        {
            if (e.getClass() == Exception.class) throw e;
            else throw new Exception("Error");
        }
    }
    
    /*public void ipAndPortRequestCommand(String client)
    {
        if(sessionID == null)
        {
            System.out.println("Sorry, you must be logged in.");
            return;
        }
        try
        {
            SWASendSockets socketsModule = new SWASendSockets(serverIP, serverPort);
            String[] result = new String[2];
            result = socketsModule.ipAndPortRequest(sessionID, client);
            System.out.println(result[0] + ":" + result[1]);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }*/
    
    /*private void clientNameRequestCommand()
    {   
        String ip = sc.next();
        int port = sc.nextInt();
        if(sessionID == null)
        {
            System.out.println("Sorry, you must be logged in.");
            return;
        }
        try
        {
            String result = socketsModule.clientNameRequest(sessionID, ip, port);
            System.out.println(result);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }*/
    
    public void declareFriendCommand(String friend) throws Exception
    {
        try
        {
            SWASendSockets socketsModule = new SWASendSockets(serverIP, serverPort);
            socketsModule.declareFriend(sessionID, friend);
        }
        catch (Exception e)
        {
            if (e.getClass() == Exception.class) throw e;
            else throw new Exception("Error");
        }
    }
    
    public void ignoreUserCommand(String friend) throws Exception
    {
        try
        {
            SWASendSockets socketsModule = new SWASendSockets(serverIP, serverPort);
            socketsModule.ignoreUser(sessionID, friend);
        }
        catch (Exception e)
        {
            if (e.getClass() == Exception.class) throw e;
            else throw new Exception("Error");
        }
    }
    
    /*public void pendingInvitationsRequesCommand()
    {
        if(sessionID == null)
        {
            System.out.println("Sorry, you must be logged in.");
            return;
        }
        try
        {
            String[] result;
            SWASendSockets socketsModule = new SWASendSockets(serverIP, serverPort);
            result = socketsModule.pendingInvitationsRequest(sessionID);
            for(int i=0; i<result.length; ++i)
            {
                System.out.println(result[i]);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }*/
    
    public String[] showListOfFriendsCommand(int property) throws Exception
    {
        try
        {
            String[] result;
            SWASendSockets socketsModule = new SWASendSockets(serverIP, serverPort);
            result = socketsModule.showListOfFriends(sessionID, property);
            return result;
        }
        catch (Exception e)
        {
            if (e.getClass() == Exception.class) throw e;
            else throw new Exception("Error");
        }
    }
    
    public void logoutCommand() throws Exception
    {
        try
        {
            synchronized(lock) {
                receiveSocketsModule.stop_receiver();
                SWASendSockets socketsModule = new SWASendSockets(serverIP, serverPort);
                socketsModule.logout(sessionID);
            }
        }
        catch (Exception e)
        {
            if (e.getClass() == Exception.class) throw e;
            else throw new Exception("Error");
        }
        finally {
            receiveSocketsModule = null;
            sessionID = null;
            username = null;
            cache_clients.clear();
        }
    }
    
    private ArrayList<String> getInfoClient(String username, String client) throws Exception{
        ArrayList<String> info = cache_clients.get(username + ":" + client);
        if (info == null) {
            SWASendSockets socketsModule = new SWASendSockets(serverIP, serverPort);
            String token = socketsModule.getSendToken(sessionID, username + ":" + client);
            String res[] = socketsModule.ipAndPortRequest(sessionID, username + ":" + client);
            info = new ArrayList<String>();
            info.add(token);
            info.add(res[0]);
            info.add(res[1]);
            cache_clients.put(username + ":" + client, info);
        }
        return info;
    }
    
    public void sendURLCommand(String url, String username, String client) throws Exception
    {    
        try
        {
            ArrayList<String> info = getInfoClient(username, client);
            Thread t = new SWASendThread(SendOperation.SEND_URL, sessionID, info.get(0), info.get(1), Integer.parseInt(info.get(2)), url);
            t.start();
        }
        catch (Exception e)
        {
            if (e.getClass() == Exception.class) throw e;
            else throw new Exception("Error");
        }
    }

    public void sendTextCommand(String text, String username, String client) throws Exception
    {
        try
        {
            ArrayList<String> info = getInfoClient(username, client);
            Thread t = new SWASendThread(SendOperation.SEND_TEXT, sessionID, info.get(0), info.get(1), Integer.parseInt(info.get(2)), text);
            t.start();
        }
        catch (Exception e)
        {
            if (e.getClass() == Exception.class) throw e;
            else throw new Exception("Error");
        }
    }
    
    public void sendFileCommand(String path, String username, String client) throws Exception
    {
        try
        {
            ArrayList<String> info = getInfoClient(username, client);
            Thread t = new SWASendThread(SendOperation.SEND_FILE, sessionID, info.get(0), info.get(1), Integer.parseInt(info.get(2)), path);
            t.start();
        }
        catch (Exception e)
        {
            if (e.getClass() == Exception.class) throw e;
            else throw new Exception("Error");
        }
    }

    public void receiveURL(String username, String client, String url)
    {
        program.receiveURL(username, client, url);
    }
    public void receiveText(String username, String client, String text)
    {
        program.receiveText(username, client, text);
    }
}

