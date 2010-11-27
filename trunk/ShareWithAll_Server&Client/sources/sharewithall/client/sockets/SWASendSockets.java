/**
 * 
 */
package sharewithall.client.sockets;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

/**
 * Authors:
 *    Alex Catarineu
 *    Ferran Rigual
 *    Miguel Angel Vico
 *
 * Creation date: Nov 6, 2010
 */
public class SWASendSockets
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
    private static final int SEND_URL = 12;
    private static final int SEND_TEXT = 13;
    private static final int SEND_FILE = 14;
    private static final int RETURN_VALUE = 0;
    private static final int EXCEPTION = -1;
    
    private String serverIP;
    private int serverPort;
    
    public SWASendSockets(String serverIP, int serverPort)
    {
        super();
        this.serverIP = serverIP;
        this.serverPort = serverPort;
    }
    
    private void getStreams(Object[] streams, Socket s) throws Exception
    {
        DataInputStream in = null;
        DataOutputStream out = null;
        
        try
        {
            in = new DataInputStream(s.getInputStream());
            out = new DataOutputStream(s.getOutputStream());
        }
        catch (Exception e)
        {
            System.out.println("Exception: " + e.getMessage());
            s.close();
            throw new Exception("Cannot open a connection with server.");
        }
        
        streams[0] = in;
        streams[1] = out;
    }
    
    public void newUser(String username, String password) throws Exception
    {
        Object[] streams = new Object[2];
        Socket socket = new Socket(serverIP, serverPort);
        getStreams(streams, socket);
        DataInputStream in = (DataInputStream)streams[0];
        DataOutputStream out = (DataOutputStream)streams[1];
        
        out.writeUTF(String.valueOf(NEW_USER) + ";" + username + ";" + password);
        String[] response = in.readUTF().split(";");
        socket.close();
        
        int responseCode = Integer.valueOf(response[0]).intValue();
        if (responseCode == EXCEPTION) throw new Exception(response[1]);
    }
    
    public String login(String username, String password, String name, boolean isPublic) throws Exception
    {
        Object[] streams = new Object[2];
        Socket socket = new Socket(serverIP, serverPort);
        getStreams(streams, socket);
        DataInputStream in = (DataInputStream)streams[0];
        DataOutputStream out = (DataOutputStream)streams[1];
        
        out.writeUTF(String.valueOf(LOGIN) + ";" + username + ";" + password + ";" + name + ";" + String.valueOf(isPublic));
        String[] response = in.readUTF().split(";");
        socket.close();
        
        int responseCode = Integer.valueOf(response[0]).intValue();
        if (responseCode == RETURN_VALUE) return String.valueOf(response[1]);

        throw new Exception(response[1]);
    }
    
    public void logout(String sessionID) throws Exception
    {
        Object[] streams = new Object[2];
        Socket socket = new Socket(serverIP, serverPort);
        getStreams(streams, socket);
        DataInputStream in = (DataInputStream)streams[0];
        DataOutputStream out = (DataOutputStream)streams[1];
        
        out.writeUTF(String.valueOf(LOGOUT) + ";" + sessionID);
        String[] response = in.readUTF().split(";");
        socket.close();
        
        int responseCode = Integer.valueOf(response[0]).intValue();
        if (responseCode == EXCEPTION) throw new Exception(response[1]);
    }
    
    public String[] getOnlineClients(String sessionID) throws Exception
    {
        Object[] streams = new Object[2];
        Socket socket = new Socket(serverIP, serverPort);
        getStreams(streams, socket);
        DataInputStream in = (DataInputStream)streams[0];
        DataOutputStream out = (DataOutputStream)streams[1];
        
        out.writeUTF(String.valueOf(GET_ONLINE_CLIENTS) + ";" + sessionID);
        String[] response = in.readUTF().split(";");
        socket.close();
        
        int responseCode = Integer.valueOf(response[0]).intValue();
        if (responseCode == RETURN_VALUE) return response[1].split(":");
            
        throw new Exception(response[1]);
    }
    
    public String[] ipAndPortRequest(String sessionID, String client) throws Exception
    {
        Object[] streams = new Object[2];
        Socket socket = new Socket(serverIP, serverPort);
        getStreams(streams, socket);
        DataInputStream in = (DataInputStream)streams[0];
        DataOutputStream out = (DataOutputStream)streams[1];
        
        out.writeUTF(String.valueOf(IP_AND_PORT_REQUEST) + ";" + sessionID + ";" + client);
        String[] response = in.readUTF().split(";");
        socket.close();
        
        int responseCode = Integer.valueOf(response[0]).intValue();
        if (responseCode == RETURN_VALUE) return response[1].split(":");
            
        throw new Exception(response[1]);
    }
    
    public String clientNameRequest(String sessionID, String ip, int port) throws Exception
    {
        Object[] streams = new Object[2];
        Socket socket = new Socket(serverIP, serverPort);
        getStreams(streams, socket);
        DataInputStream in = (DataInputStream)streams[0];
        DataOutputStream out = (DataOutputStream)streams[1];
        
        out.writeUTF(String.valueOf(CLIENT_NAME_REQUEST) + ";" + sessionID + ";" + ip + ";" + port);
        String[] response = in.readUTF().split(";");
        socket.close();
        
        int responseCode = Integer.valueOf(response[0]).intValue();
        if (responseCode == RETURN_VALUE) return String.valueOf(response[1]);
            
        throw new Exception(response[1]);
    }
    
    public void declareFriend(String sessionID, String friend) throws Exception
    {
        Object[] streams = new Object[2];
        Socket socket = new Socket(serverIP, serverPort);
        getStreams(streams, socket);
        DataInputStream in = (DataInputStream)streams[0];
        DataOutputStream out = (DataOutputStream)streams[1];
        
        out.writeUTF(String.valueOf(DECLARE_FRIEND) + ";" + sessionID + ";" + friend);
        String[] response = in.readUTF().split(";");
        socket.close();
        
        int responseCode = Integer.valueOf(response[0]).intValue();
        if (responseCode == EXCEPTION) throw new Exception(response[1]);
    }

    public void ignoreUser(String sessionID, String friend) throws Exception
    {
        Object[] streams = new Object[2];
        Socket socket = new Socket(serverIP, serverPort);
        getStreams(streams, socket);
        DataInputStream in = (DataInputStream)streams[0];
        DataOutputStream out = (DataOutputStream)streams[1];
        
        out.writeUTF(String.valueOf(IGNORE_USER) + ";" + sessionID + ";" + friend);
        String[] response = in.readUTF().split(";");
        socket.close();
        
        int responseCode = Integer.valueOf(response[0]).intValue();
        if (responseCode == EXCEPTION) throw new Exception(response[1]);
    }
    
    public void updateTimestamp(String sessionID) throws Exception
    {
        Object[] streams = new Object[2];
        Socket socket = new Socket(serverIP, serverPort);
        getStreams(streams, socket);
        DataInputStream in = (DataInputStream)streams[0];
        DataOutputStream out = (DataOutputStream)streams[1];
        
        out.writeUTF(String.valueOf(UPDATE_TIMESTAMP) + ";" + sessionID);
        String[] response = in.readUTF().split(";");
        socket.close();
        
        int responseCode = Integer.valueOf(response[0]).intValue();
        if (responseCode == EXCEPTION) throw new Exception(response[1]);
    }
    
    public String[] pendingInvitationsRequest(String sessionID) throws Exception
    {
        Object[] streams = new Object[2];
        Socket socket = new Socket(serverIP, serverPort);
        getStreams(streams, socket);
        DataInputStream in = (DataInputStream)streams[0];
        DataOutputStream out = (DataOutputStream)streams[1];
        
        out.writeUTF(String.valueOf(PENDING_INVITATIONS_REQUEST) + ";" + sessionID);
        String[] response = in.readUTF().split(";");
        socket.close();
        
        int responseCode = Integer.valueOf(response[0]).intValue();
        if (responseCode == RETURN_VALUE)
            if(response.length == 1)
            {
                String[] result = new String[1];
                result[0] = "There isn't pending invitations"; 
                return result;
            }
            else
                return response[1].split(":");
        
        throw new Exception(response[1]);
    }

    public String[] showListOfFriends(String sessionID, int property) throws Exception
    {
        Object[] streams = new Object[2];
        Socket socket = new Socket(serverIP, serverPort);
        getStreams(streams, socket);
        DataInputStream in = (DataInputStream)streams[0];
        DataOutputStream out = (DataOutputStream)streams[1];
         
        out.writeUTF(String.valueOf(GET_LIST_OF_FRIENDS) + ";" + sessionID + ";" + String.valueOf(property));
        String[] response = in.readUTF().split(";");
        socket.close();
         
        int responseCode = Integer.valueOf(response[0]).intValue();
        if (responseCode == RETURN_VALUE)
            if(response.length == 1)
            {
                String[] result = new String[1];
                result[0] = "There aren't users with this property."; 
                return result;
            }
            else
                return response[1].split(":");
        
        throw new Exception(response[1]);
    }
    
    public void sendURL(String ip, int port, String url) throws Exception
    {
        Object[] streams = new Object[2];
        Socket socket = new Socket(ip, port);
        getStreams(streams, socket);
        DataInputStream in = (DataInputStream)streams[0];
        DataOutputStream out = (DataOutputStream)streams[1];
        
        out.writeUTF(String.valueOf(SEND_URL) + ";" + url);
        String[] response = in.readUTF().split(";");
        socket.close();
        
        int responseCode = Integer.valueOf(response[0]).intValue();
        if (responseCode == EXCEPTION) throw new Exception(response[1]);
    }
    
    
    public void sendText(String ip, int port, String text) throws Exception
    {
        Object[] streams = new Object[2];
        Socket socket = new Socket(ip, port);
        getStreams(streams, socket);
        DataInputStream in = (DataInputStream)streams[0];
        DataOutputStream out = (DataOutputStream)streams[1];
        
        out.writeUTF(String.valueOf(SEND_TEXT) + ";" + text);
        String[] response = in.readUTF().split(";");
        socket.close();
        
        int responseCode = Integer.valueOf(response[0]).intValue();
        if (responseCode == EXCEPTION) throw new Exception(response[1]);       
    }
    
    //TODO: enviar archivo como bytes
    public void sendFile(String ip, int port, String path) throws Exception
    {
        Object[] streams = new Object[2];
        Socket socket = new Socket(ip, port);
        getStreams(streams, socket);
        DataInputStream in = (DataInputStream)streams[0];
        DataOutputStream out = (DataOutputStream)streams[1];
        
        out.writeUTF(String.valueOf(SEND_FILE) + ";" + path);
        String[] response = in.readUTF().split(";");
        socket.close();
        
        int responseCode = Integer.valueOf(response[0]).intValue();
        if (responseCode == EXCEPTION) throw new Exception(response[1]);        
    }
}