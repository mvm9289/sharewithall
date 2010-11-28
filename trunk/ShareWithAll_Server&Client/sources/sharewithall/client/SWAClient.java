
package sharewithall.client;

import java.io.File;
import java.util.Scanner;

import net.sf.jmimemagic.Magic;
import net.sf.jmimemagic.MagicMatch;
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
    
    private static final String DEFAULT_SERVER_IP = "mvm9289.dyndns.org";
    private static final int DEFAULT_SERVER_PORT = 4040;
    
    private static SWASendSockets socketsModule;
    private static SWAReceiveClientSockets receiveSocketsModule;
    private String sessionID;
    private String username;
    private String password;
    private Scanner sc;
    
    private class SWAUpdateThread extends Thread
    {
        private static final int SLEEP_TIME = 30000;
        
        private SWAUpdateThread()
        {
            super();
        }
        
        public void run()
        {
            while (true) {
                try
                {
                    if (sessionID != null) {
                        socketsModule.updateTimestamp(sessionID);
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
    
    public SWAClient(String serverIP, int serverPort)
    {
        super();
        sc = new Scanner(System.in);
        
        socketsModule = new SWASendSockets(serverIP, serverPort);
        
        //Esto me daba problemas para leer, lo he comentado (alex)
        //sc.useDelimiter("[\\s]");
        Thread update = new SWAUpdateThread();
        update.start();
        SWAClientLoop();
    }

    private void newUserCommand()
    {
        if(sessionID != null)
        {
            System.out.println("Sorry, you are already logged in.");
            return;
        }                   
        username = sc.next();
        password = sc.next();
        try
        {
            socketsModule.newUser(username, password);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    private void loginCommand()
    {
        if(sessionID != null)
        {
            System.out.println("Sorry, you are already logged in.");
            return;
        }
        username = sc.next();
        password = sc.next();
        String name = sc.next();
        boolean isPublic = sc.nextBoolean();
        try
        {
            sessionID = socketsModule.login(username, password, name, isPublic);
            ////////// HE A�ADIDO ESTO, PONEDLO EN UNA FUNCION SI QUEREIS PERO TIENE QUE IR AQUI!!!!
            String[] myIPandPort = socketsModule.ipAndPortRequest(sessionID, name);
            int port = Integer.valueOf(myIPandPort[1]).intValue();
            receiveSocketsModule = new SWAReceiveClientSockets(port, this);
            receiveSocketsModule.start();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    private void getOnlineClientsCommand()
    {
        String[] clients = null;
        if(sessionID == null)
        {
            System.out.println("Sorry, you must be logged in.");
            return;
        }
        try
        {
            clients = socketsModule.getOnlineClients(sessionID);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        for(int i=0; i<clients.length; ++i)
            System.out.println(clients[i]);
    }
    
    private void ipAndPortRequestCommand()
    {
        String client = sc.next();
        if(sessionID == null)
        {
            System.out.println("Sorry, you must be logged in.");
            return;
        }
        try
        {
            String[] result = new String[2];
            result = socketsModule.ipAndPortRequest(sessionID, client);
            System.out.println(result[0] + ":" + result[1]);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
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
    private void declareFriendCommand()
    {
        String friend = sc.next();
        if(sessionID == null)
        {
            System.out.println("Sorry, you must be logged in.");
            return;
        }
        try
        {
            socketsModule.declareFriend(sessionID, friend);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    private void ignoreUserCommand()
    {
        String friend = sc.next();
        if(sessionID == null)
        {
            System.out.println("Sorry, you must be logged in.");
            return;
        }
        try
        {
            socketsModule.ignoreUser(sessionID, friend);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    private void pendingInvitationsRequesCommand()
    {
        if(sessionID == null)
        {
            System.out.println("Sorry, you must be logged in.");
            return;
        }
        try
        {
            String[] result;
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
    }
    
    private void showListOfFriendsCommand()
    {
        int property = sc.nextInt();
        
        if(sessionID == null)
        {
            System.out.println("Sorry, you must be logged in.");
            return;
        }
        try
        {
            String[] result;
            result = socketsModule.showListOfFriends(sessionID, property);
            for(int i=0; i<result.length; ++i)
            {
                System.out.println(result[i]);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    private void logoutCommand()
    {
        if(sessionID == null) return;
        try
        {
            socketsModule.logout(sessionID);
            sessionID = null;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    private void sendURLCommand()
    {
        if(sessionID == null)
        {
            System.out.println("Sorry, you must be logged in.");
            return;
        }
        
        String url = sc.next();
        String username = sc.next();
        String client = sc.next();
        
        try
        {
            String token = socketsModule.getSendToken(sessionID, username + ":" + client);
            String res[] = socketsModule.ipAndPortRequest(sessionID, username + ":" + client);
            socketsModule.sendURL(token, res[0], Integer.parseInt(res[1]), url);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    private void sendTextCommand()
    {
        if(sessionID == null)
        {
            System.out.println("Sorry, you must be logged in.");
            return;
        }
        
        String text = sc.next();
        String username = sc.next();
        String client = sc.next();
        
        try
        {
            String token = socketsModule.getSendToken(sessionID, username + ":" + client);
            String res[] = socketsModule.ipAndPortRequest(sessionID, username + ":" + client);
            socketsModule.sendText(token, res[0], Integer.parseInt(res[1]), text);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    private void sendFileCommand()
    {
        if(sessionID == null)
        {
            System.out.println("Sorry, you must be logged in.");
            return;
        }
        
        String path = sc.next();
        String username = sc.next();
        String client = sc.next();
        
        try
        {
            String token = socketsModule.getSendToken(sessionID, username + ":" + client);
            String res[] = socketsModule.ipAndPortRequest(sessionID, username + ":" + client);
            socketsModule.sendFile(token, res[0], Integer.parseInt(res[1]), path);
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
        System.out.println("[" + username + ":" + client + "] says: " + text);
    }
    public void receiveFile(String username, String client, String file)
    {
        try {
            File f = new File(file);
            MagicMatch match = Magic.getMagicMatch(f, true);
            System.out.println("[" + username + ":" + client + "] has sent you the file '" + file + "' with type '" + match.print() + "'");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public String[] obtainEmissor(String token)
    {   
            try
            {
                return socketsModule.obtainEmissor(sessionID, token);
            } catch (Exception e)
            {
                e.printStackTrace();
            }
            return null; //TODO: Si no pongo esto se queja de que puede ser que no se devuelva nada.
    }
    
    private void SWAClientLoop()
    {
        boolean end = false;
        
        while(!end)
        {
            System.out.println(
                    "Choose your commmand.\n" +
                    "                   New User: 00 username password\n" +
                    "                      Login: 01 username password name { true | false }\n" +
                    "         Get Online Clients: 02\n" +
                    "        IP and port request: 03 client\n" +
                    "             Declare friend: 04 friend\n" +
                    "                Ignore user: 05 users\n" +
                    "Pending invitations request: 06\n" +
                    "       Show list of friends: 07 property\n" +
                    "                   Send URL: 10 URL username client\n" +
                    "                  Send Text: 11 text username client\n" +
                    "                  Send File: 12 path username client\n" +
                    "                       Exit: 09\n" +
                    "--------------------------------------------");
            
            int commandIndex = sc.nextInt();
            switch(commandIndex)
            {
                case 0:
                    newUserCommand();
                    break;
                case 1:
                    loginCommand();
                    break;
                case 2:
                    getOnlineClientsCommand();
                    break;
                case 3:
                    ipAndPortRequestCommand();
                    break;
                case 4:
                    declareFriendCommand();
                    break;
                case 5:
                    ignoreUserCommand();
                    break;
                case 6:
                    pendingInvitationsRequesCommand();
                    break;
                case 7:
                    showListOfFriendsCommand();
                    break;
                /*case 8:
                    clientNameRequestCommand();
                    break;*/
                case 9:
                    logoutCommand();
                    end = true;
                    break;
                case 10:
                    sendURLCommand();
                    break;
                case 11:
                    sendTextCommand();
                    break;
                case 12:
                    sendFileCommand();
                    break;
                default:
                    System.out.println("Wrong command, try again.");
                    break;
            }
            System.out.println("Done!");
        }
    }
    
    private static void printUsage()
    {
        System.out.println(
            "\n\tUSAGE:\n\t\t" +
                "java sharewithall.server.SWAClient [serverIP:serverPort]" +
                "\n\n\t\tor\n\n\t\t" +
                "java sharewithall.server.SWAClient [serverIP]" +
            "\n\n\t*Arguments between [] are optional." +
            "Default server IP and port are " + DEFAULT_SERVER_IP + ":" + DEFAULT_SERVER_PORT + ".\n");
    }
    
    public static void main(String[] args)
    {
        if (args.length == 1)
        {
            String[] aux = args[0].split(":");
            if (aux.length == 1) new SWAClient(aux[0], DEFAULT_SERVER_PORT);
            else if (aux.length == 2) new SWAClient(aux[0], Integer.valueOf(aux[1]).intValue());
            else printUsage();
        }
        else if (args.length == 0) new SWAClient(DEFAULT_SERVER_IP, DEFAULT_SERVER_PORT);
        else printUsage();
    }



}

