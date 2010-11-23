/**
 * 
 */
package sharewithall.client;

import java.io.File;
import java.net.URL;
import java.util.Scanner;

import net.sf.jmimemagic.Magic;
import net.sf.jmimemagic.MagicMatch;

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
    
    private static final String DEFAULT_SERVER_IP = "mvm9289.dyndns.org";
    private static final int DEFAULT_SERVER_PORT = 4040;
    
    private static SWAClientSockets socketsModule;
    private String sessionID;
    private String username;
    private String password;
    private Scanner sc;
    
    public SWAClient(String serverIP, int serverPort)
    {
        super();
        socketsModule = new SWAClientSockets(serverIP, serverPort);
        sc = new Scanner(System.in);
        //Esto me daba problemas para leer, lo he comentado (alex)
        //sc.useDelimiter("[\\s]");
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
            String res[] = socketsModule.ipAndPortRequest(sessionID, username + "#" + client);
            socketsModule.sendURL(username, res[0], Integer.parseInt(res[1]), url);
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
            String res[] = socketsModule.ipAndPortRequest(sessionID, username + "#" + client);
            socketsModule.sendText(username, res[0], Integer.parseInt(res[1]), text);
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
            String res[] = socketsModule.ipAndPortRequest(sessionID, username + "#" + client);
            socketsModule.sendFile(username, res[0], Integer.parseInt(res[1]), path);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void receiveURL(String username, String client, String url)
    {
        boolean ok = false;
        java.awt.Desktop desktop = null;
        if (java.awt.Desktop.isDesktopSupported() ) {
            desktop = java.awt.Desktop.getDesktop();
            ok = desktop.isSupported(java.awt.Desktop.Action.BROWSE);
        }

        System.out.println("[" + username + "#" + client + "] has sent you the URL '" + url + "'");
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
    
    private void receiveText(String username, String client, String text)
    {
        System.out.println("[" + username + "#" + client + "] says: " + text);
    }
    
    private void receiveFile(String username, String client, String file)
    {
        try {
            File f = new File(file);
            MagicMatch match = Magic.getMagicMatch(f, true);
            System.out.println("[" + username + "#" + client + "] has sent you the file '" + file + "' with type '" + match.print() + "'");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    private void SWAClientLoop()
    {
        boolean end = false;
        
        while(!end)
        {
            System.out.println(
                    "Choose your commmand.\n" +
                    "                   New User: 0 username password\n" +
                    "                      Login: 1 username password name { true | false }\n" +
                    "         Get Online Clients: 2\n" +
                    "        IP and port request: 3 client\n" +
                    "             Declare friend: 4 friend\n" +
                    "                Ignore user: 5 users\n" +
                    "Pending invitations request: 6\n" +
                    "       Show list of friends: 7 property\n" +
                    "                       Exit: 8\n" +
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
                case 8:
                    logoutCommand();
                    end = true;
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

