
package sharewithall.client;

import java.util.Scanner;

import sharewithall.client.sockets.SWAReceiveClientSockets;
import sharewithall.client.sockets.SWASendSockets;

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
    public String serverIP;
    public int serverPort;
    public MainGraphicalInterface program = null;
    private boolean gateway = true;
    private static SWAReceiveClientSockets receiveSocketsModule;
    private String sessionID;
    private Object lock = new Object();
    
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
        }
    }
    private void threadException(SendOperation op, Exception e) {
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
    public void newUserCommand(String username, String password)
    {
        try
        {
            SWASendSockets socketsModule = new SWASendSockets(serverIP, serverPort);
            socketsModule.newUser(username, password);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    public void loginCommand(String username, String password, String name, boolean isPublic) throws Exception {
        if(sessionID != null)
        {
            System.out.println("Sorry, you are already logged in.");
            return;
        }

        try
        {
            SWASendSockets socketsModule = new SWASendSockets(serverIP, serverPort);
            sessionID = socketsModule.login(username, password, name, isPublic);
            ////////// HE ANADIDO ESTO, PONEDLO EN UNA FUNCION SI QUEREIS PERO TIENE QUE IR AQUI!!!!
            String[] myIPandPort = socketsModule.ipAndPortRequest(sessionID, name);
            int port = Integer.valueOf(myIPandPort[1]).intValue();

            if (gateway) receiveSocketsModule = new SWAReceiveClientSockets(serverIP, serverPort, this);
            else receiveSocketsModule = new SWAReceiveClientSockets(port, this);
            receiveSocketsModule.start();
        }
        catch (Exception e)
        {
            if (e.getClass() == Exception.class) throw e;
            e.printStackTrace();
        }
    }
    
    public String[] getOnlineClientsCommand()
    {
        if(sessionID == null)
        {
            System.out.println("Sorry, you must be logged in.");
            return null;
        }
        try
        {
            SWASendSockets socketsModule = new SWASendSockets(serverIP, serverPort);
            return socketsModule.getOnlineClients(sessionID);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
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
    
    public void declareFriendCommand(String friend)
    {
        if(sessionID == null)
        {
            System.out.println("Sorry, you must be logged in.");
            return;
        }
        try
        {
            SWASendSockets socketsModule = new SWASendSockets(serverIP, serverPort);
            socketsModule.declareFriend(sessionID, friend);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    public void ignoreUserCommand(String friend)
    {
        if(sessionID == null)
        {
            System.out.println("Sorry, you must be logged in.");
            return;
        }
        try
        {
            SWASendSockets socketsModule = new SWASendSockets(serverIP, serverPort);
            socketsModule.ignoreUser(sessionID, friend);
        }
        catch (Exception e)
        {
            e.printStackTrace();
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
    
    public String[] showListOfFriendsCommand(int property)
    {
       
        if(sessionID == null)
        {
            System.out.println("Sorry, you must be logged in.");
            return null;
        }
        try
        {
            String[] result;
            SWASendSockets socketsModule = new SWASendSockets(serverIP, serverPort);
            result = socketsModule.showListOfFriends(sessionID, property);
            return result;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }
    
    public void logoutCommand()
    {
        if(sessionID == null) return;
        try
        {
            synchronized(lock) {
                receiveSocketsModule.stop_receiver();
                receiveSocketsModule = null;
                SWASendSockets socketsModule = new SWASendSockets(serverIP, serverPort);
                socketsModule.logout(sessionID);
                sessionID = null;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    public void sendURLCommand(String url, String username, String client)
    {
        if(sessionID == null)
        {
            System.out.println("Sorry, you must be logged in.");
            return;
        }
                
        try
        {
            SWASendSockets socketsModule = new SWASendSockets(serverIP, serverPort);
            String token = socketsModule.getSendToken(sessionID, username + ":" + client);
            String res[] = socketsModule.ipAndPortRequest(sessionID, username + ":" + client);
            Thread t = new SWASendThread(SendOperation.SEND_URL, sessionID, token, res[0], Integer.parseInt(res[1]), url);
            t.start();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    public void sendTextCommand(String text, String username, String client)
    {
        if(sessionID == null)
        {
            System.out.println("Sorry, you must be logged in.");
            return;
        }
        
        try
        {
            SWASendSockets socketsModule = new SWASendSockets(serverIP, serverPort);
            String token = socketsModule.getSendToken(sessionID, username + ":" + client);
            String res[] = socketsModule.ipAndPortRequest(sessionID, username + ":" + client);
            Thread t = new SWASendThread(SendOperation.SEND_TEXT, sessionID, token, res[0], Integer.parseInt(res[1]), text);
            t.start();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    public void sendFileCommand(String path, String username, String client)
    {
        if(sessionID == null)
        {
            System.out.println("Sorry, you must be logged in.");
            return;
        }
        
        try
        {
            SWASendSockets socketsModule = new SWASendSockets(serverIP, serverPort);
            String token = socketsModule.getSendToken(sessionID, username + ":" + client);
            String res[] = socketsModule.ipAndPortRequest(sessionID, username + ":" + client);
            Thread t = new SWASendThread(SendOperation.SEND_FILE, sessionID, token, res[0], Integer.parseInt(res[1]), path);
            t.start();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void receiveURL(String username, String client, String url)
    {
        boolean ok = false;
        java.awt.Desktop desktop = null;
        if (java.awt.Desktop.isDesktopSupported() ) {
            desktop = java.awt.Desktop.getDesktop();
            ok = desktop.isSupported(java.awt.Desktop.Action.BROWSE);
        }

        System.out.println("[" + username + ":" + client + "] has sent you the URL '" + url + "'");
        if (ok) {
            System.out.println("Trying to open it in your default browser...");
            try {
                java.net.URI uri = new java.net.URI( url );
                desktop.browse( uri );
            }
            catch ( Exception e ) {
                e.printStackTrace();
            }
        }
        else System.out.println("Cannot open it in your default browser");
    }
    public void receiveText(String username, String client, String text)
    {
        if(program == null)
            System.out.println("[" + username + ":" + client + "] says: " + text);
        else
            program.receiveText(username, client, text);
    }
    public void receiveFile(String username, String client, String file)
    {
        try {
            //File f = new File(file);
            //MagicMatch match = Magic.getMagicMatch(f, true);
            System.out.println("[" + username + ":" + client + "] has sent you the file '" + file);// + "' with type '" + match.print() + "'");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}

