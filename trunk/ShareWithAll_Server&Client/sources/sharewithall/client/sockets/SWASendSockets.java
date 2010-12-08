/**
 * 
 */
package sharewithall.client.sockets;

import java.io.File;
import java.io.FileInputStream;
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
    private static final int GET_SEND_TOKEN = 15;
    private static final int SEND_GATEWAY = 16;
    private static final int RETURN_VALUE = 0;
    private static final int EXCEPTION = -1;
    public static final int PROPERTY_FRIENDS = 0;
    public static final int PROPERTY_DECLARED_FRIEND = 1;
    public static final int PROPERTY_EXPECTING = 2;
    public static final int PROPERTY_IGNORED = 3;
    private static final int FILE_BUFFER_SIZE = 4096;
    
    private String serverIP;
    private int serverPort;
    private Socket clientSocket;
    private DataInputStream in;
    private DataOutputStream out;
    
    public SWASendSockets(String serverIP, int serverPort)
    {
        super();
        this.serverIP = serverIP;
        this.serverPort = serverPort;
    }

    private void connect(String ip, int port) throws Exception
    {
        clientSocket = new Socket(ip, port);
        out = new DataOutputStream(clientSocket.getOutputStream());
        out.flush();
        in = new DataInputStream(clientSocket.getInputStream());
    }
    
    private boolean connectGateway(String sessionID, String ip, int port) {
        try {
            connect(serverIP, serverPort);

            out.writeInt(SEND_GATEWAY);
            out.writeUTF(sessionID);
            out.writeUTF(ip);
            out.writeInt(port);
            out.flush();
            
            int responseCode = in.readInt();
            if (responseCode == EXCEPTION) throw new Exception(in.readUTF());
            return true;
        }
        catch (Exception e) {
        }
        try {
            clientSocket.close();
        }
        catch(Exception e) {
        }
        
        return false;
    }
    
    private String[] read_array() throws Exception {
        int size = in.readInt();
        String[] res = new String[size];
        for (int i = 0; i < size; ++i)
            res[i] = in.readUTF();
        return res;
    }
    
    public void newUser(String username, String password) throws Exception
    {
        connect(serverIP, serverPort);
        out.writeInt(NEW_USER);
        out.writeUTF(username);
        out.writeUTF(password);
        out.flush();
        clientSocket.shutdownOutput();
        
        int responseCode = in.readInt();
        if (responseCode == EXCEPTION) throw new Exception(in.readUTF());
    }
    
    public String login(String username, String password, String name, boolean isPublic) throws Exception
    {
        connect(serverIP, serverPort);
        out.writeInt(LOGIN);
        out.writeUTF(username);
        out.writeUTF(password);
        out.writeUTF(name);
        out.writeBoolean(isPublic);
        out.flush();
        clientSocket.shutdownOutput();
        
        int responseCode = in.readInt();
        String responseMsg = in.readUTF();
        clientSocket.close();
        if (responseCode == RETURN_VALUE) return responseMsg;
        throw new Exception(responseMsg);
    }
    
    public void logout(String sessionID) throws Exception
    {
        connect(serverIP, serverPort);
        out.writeInt(LOGOUT);
        out.writeUTF(sessionID);
        out.flush();
        clientSocket.shutdownOutput();
        
        int responseCode = in.readInt();
        if (responseCode == EXCEPTION) throw new Exception(in.readUTF());
    }
    
    public String[] getOnlineClients(String sessionID) throws Exception
    {
        connect(serverIP, serverPort);
        out.writeInt(GET_ONLINE_CLIENTS);
        out.writeUTF(sessionID);
        out.flush();
        clientSocket.shutdownOutput();
        
        int responseCode = in.readInt();
        if (responseCode == EXCEPTION) throw new Exception(in.readUTF());
        return read_array();
    }
    
    public String getSendToken(String sessionID, String client) throws Exception
    {
        connect(serverIP, serverPort);
        out.writeInt(GET_SEND_TOKEN);
        out.writeUTF(sessionID);
        out.writeUTF(client);
        out.flush();
        clientSocket.shutdownOutput();
        
        int responseCode = in.readInt();
        String responseMsg = in.readUTF();
        clientSocket.close();
        if (responseCode == RETURN_VALUE) return responseMsg;
        throw new Exception(responseMsg);
    }
    
    public String[] ipAndPortRequest(String sessionID, String client) throws Exception
    {
        connect(serverIP, serverPort);
        out.writeInt(IP_AND_PORT_REQUEST);
        out.writeUTF(sessionID);
        out.writeUTF(client);
        out.flush();
        clientSocket.shutdownOutput();
        
        int responseCode = in.readInt();
        String responseMsg = in.readUTF();
        clientSocket.close();
        if (responseCode == RETURN_VALUE) return responseMsg.split(":");
        throw new Exception(responseMsg);
    }
    
    public String clientNameRequest(String sessionID, String token) throws Exception
    {
        connect(serverIP, serverPort);
        out.writeInt(CLIENT_NAME_REQUEST);
        out.writeUTF(sessionID);
        out.writeUTF(token);
        out.flush();
        clientSocket.shutdownOutput();
        
        int responseCode = in.readInt();
        String responseMsg = in.readUTF();
        clientSocket.close();
        if (responseCode == RETURN_VALUE) return responseMsg;
        throw new Exception(responseMsg);
    }
    
    public void declareFriend(String sessionID, String friend) throws Exception
    {
        connect(serverIP, serverPort);
        out.writeInt(DECLARE_FRIEND);
        out.writeUTF(sessionID);
        out.writeUTF(friend);
        out.flush();
        clientSocket.shutdownOutput();
        
        int responseCode = in.readInt();
        if (responseCode == EXCEPTION) throw new Exception(in.readUTF());
    }

    public void ignoreUser(String sessionID, String friend) throws Exception
    {
        connect(serverIP, serverPort);
        out.writeInt(IGNORE_USER);
        out.writeUTF(sessionID);
        out.writeUTF(friend);
        out.flush();
        
        int responseCode = in.readInt();
        if (responseCode == EXCEPTION) throw new Exception(in.readUTF());
    }
    
    public void updateTimestamp(String sessionID) throws Exception
    {
        connect(serverIP, serverPort);
        out.writeInt(UPDATE_TIMESTAMP);
        out.writeUTF(sessionID);
        out.flush();
        clientSocket.shutdownOutput();
        
        int responseCode = in.readInt();
        if (responseCode == EXCEPTION) throw new Exception(in.readUTF());
    }
    
    public String[] pendingInvitationsRequest(String sessionID) throws Exception
    {
        connect(serverIP, serverPort);
        out.writeInt(PENDING_INVITATIONS_REQUEST);
        out.writeUTF(sessionID);
        out.flush();
        clientSocket.shutdownOutput();
        
        int responseCode = in.readInt();
        if (responseCode == EXCEPTION) throw new Exception(in.readUTF());
        return read_array();
    }

    public String[] showListOfFriends(String sessionID, int property) throws Exception
    {
        connect(serverIP, serverPort);
        out.writeInt(GET_LIST_OF_FRIENDS);
        out.writeUTF(sessionID);
        out.writeInt(property);
        out.flush();
        clientSocket.shutdownOutput();
        
        int responseCode = in.readInt();
        if (responseCode == EXCEPTION) throw new Exception(in.readUTF());
        return read_array();
    }
    
    public void sendURL(String sessionID, String token, String ip, int port, String url) throws Exception
    {
        if (!connectGateway(sessionID, ip, port)) connect(ip, port);
        out.writeInt(SEND_URL);
        out.writeUTF(token);
        out.writeUTF(url);
        out.flush();
        clientSocket.shutdownOutput();
        
        int responseCode = in.readInt();
        if (responseCode == EXCEPTION) throw new Exception(in.readUTF());
    }

    public void sendText(String sessionID, String token, String ip, int port, String text) throws Exception
    {
        if (!connectGateway(sessionID, ip, port)) connect(ip, port);
        out.writeInt(SEND_TEXT);
        out.writeUTF(token);
        out.writeUTF(text);
        out.flush();
        clientSocket.shutdownOutput();
        
        int responseCode = in.readInt();
        if (responseCode == EXCEPTION) throw new Exception(in.readUTF());    
    }
    
    public void sendFile(String sessionID, String token, String ip, int port, String path) throws Exception
    {
        File f = new File(path);
        FileInputStream filein = new FileInputStream(f);
        
        if (!connectGateway(sessionID, ip, port)) connect(ip, port);
        int filesize = (int)f.length();
        out.writeInt(SEND_FILE);
        out.writeUTF(token);
        out.writeUTF(f.getName());
        out.writeInt(filesize);

        int bytesRead;
        byte[] bytes = new byte[FILE_BUFFER_SIZE];
        while ((bytesRead = filein.read(bytes)) > 0)
        {
            out.write(bytes, 0, bytesRead);
            out.flush();
        }
        clientSocket.shutdownOutput();

        int responseCode = in.readInt();
        if (responseCode == EXCEPTION) throw new Exception(in.readUTF());     
    }
}