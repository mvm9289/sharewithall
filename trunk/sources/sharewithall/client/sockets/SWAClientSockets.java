/**
 * 
 */
package sharewithall.client.sockets;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Authors:
 *    Alex Catarineu
 *    Ferran Rigual
 *    Miguel Angel Vico
 *
 * Creation date: Nov 6, 2010
 */
public class SWAClientSockets
{

    private static final int NEW_USER = 1;
    private static final int LOGIN = 2;
    private static final int LOGOUT = 3;
    private static final int GET_ONLINE_CLIENTS = 4;
    private static final int IP_AND_PORT_REQUEST = 5;
    private static final int SEND_INVITATION = 6;
    private static final int RETURN_VALUE = 0;
    private static final int EXCEPTION = -1;
    
    private Socket clientSocket;
    private String serverIP;
    private int serverPort;
    
    public SWAClientSockets(String serverIP, int serverPort)
    {
        super();
        this.serverIP = serverIP;
        this.serverPort = serverPort;
    }
    
    public boolean newUser(String username, String password) throws Exception
    {
        BufferedReader in = null;
        PrintWriter out = null;
        
        clientSocket = new Socket(serverIP, serverPort);
        
        try
        {
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new PrintWriter(clientSocket.getOutputStream(), true);
        }
        catch (Exception e)
        {
            System.out.println("Exception: " + e.getMessage());
            throw new Exception("Can not connect to server.");
        }
        
        out.print(String.valueOf(NEW_USER) + ";" + username + ";" + password);
        String[] response = in.readLine().split(";");
        int responseCode = Integer.valueOf(response[0]).intValue();
        
        if (responseCode == RETURN_VALUE) return Boolean.valueOf(response[1]).booleanValue();
        
        throw new Exception(response[1]);
    }
    
    public int login(String username, String password, String name, boolean isPublic) throws Exception
    {
        BufferedReader in = null;
        PrintWriter out = null;
        
        clientSocket = new Socket(serverIP, serverPort);
        
        try
        {
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new PrintWriter(clientSocket.getOutputStream(), true);
        }
        catch (Exception e)
        {
            System.out.println("Exception: " + e.getMessage());
            throw new Exception("Can not connect to server.");
        }
        
        out.print(String.valueOf(LOGIN) + ";" + username + ";" + password + ";" + name + ";" + String.valueOf(isPublic));
        String[] response = in.readLine().split(";");
        int responseCode = Integer.valueOf(response[0]).intValue();
        
        if (responseCode == RETURN_VALUE) return Integer.valueOf(response[1]).intValue();
        
        throw new Exception(response[1]);
    }
    
    public void logout(int sessionID) throws Exception
    {
        BufferedReader in = null;
        PrintWriter out = null;
        
        clientSocket = new Socket(serverIP, serverPort);
        
        try
        {
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new PrintWriter(clientSocket.getOutputStream(), true);
        }
        catch (Exception e)
        {
            System.out.println("Exception: " + e.getMessage());
            throw new Exception("Can not connect to server.");
        }
        
        out.print(String.valueOf(LOGOUT) + ";" + String.valueOf(sessionID));
        String[] response = in.readLine().split(";");
        int responseCode = Integer.valueOf(response[0]).intValue();
        
        if (responseCode == EXCEPTION) throw new Exception(response[1]);
    }
    
    public String getOnlineClients(int sessionID) throws Exception
    {
        BufferedReader in = null;
        PrintWriter out = null;
        
        clientSocket = new Socket(serverIP, serverPort);
        
        try
        {
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new PrintWriter(clientSocket.getOutputStream(), true);
        }
        catch (Exception e)
        {
            System.out.println("Exception: " + e.getMessage());
            throw new Exception("Can not connect to server.");
        }
        
        out.print(String.valueOf(GET_ONLINE_CLIENTS) + ";" + String.valueOf(sessionID));
        String[] response = in.readLine().split(";");
        int responseCode = Integer.valueOf(response[0]).intValue();
        
        if (responseCode == RETURN_VALUE) return response[1];
            
        throw new Exception(response[1]);
    }
    
    public String ipAndPortRequest(int sessionID, String client) throws Exception
    {
        BufferedReader in = null;
        PrintWriter out = null;
        
        clientSocket = new Socket(serverIP, serverPort);
        
        try
        {
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new PrintWriter(clientSocket.getOutputStream(), true);
        }
        catch (Exception e)
        {
            System.out.println("Exception: " + e.getMessage());
            throw new Exception("Can not connect to server.");
        }
        
        out.print(String.valueOf(IP_AND_PORT_REQUEST) + ";" + String.valueOf(sessionID) + ";" + client);
        String[] response = in.readLine().split(";");
        int responseCode = Integer.valueOf(response[0]).intValue();
        
        if (responseCode == RETURN_VALUE) return response[1];
            
        throw new Exception(response[1]);
    }
    
    public boolean sendInvitation(int sessionID, String friend) throws Exception
    {
        BufferedReader in = null;
        PrintWriter out = null;
        
        clientSocket = new Socket(serverIP, serverPort);
        
        try
        {
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new PrintWriter(clientSocket.getOutputStream(), true);
        }
        catch (Exception e)
        {
            System.out.println("Exception: " + e.getMessage());
            throw new Exception("Can not connect to server.");
        }
        
        out.print(String.valueOf(SEND_INVITATION) + ";" + String.valueOf(sessionID) + ";" + friend);
        String[] response = in.readLine().split(";");
        int responseCode = Integer.valueOf(response[0]).intValue();
        
        if (responseCode == RETURN_VALUE) return Boolean.valueOf(response[1]).booleanValue();
            
        throw new Exception(response[1]);
    }
    
}
