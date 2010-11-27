/**
 * 
 */
package sharewithall.client.sockets;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
    private static final int FILE_BUFFER_SIZE = 4096; 
    private String serverIP;
    private int serverPort;
    private Socket clientSocket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    
    public SWASendSockets(String serverIP, int serverPort)
    {
        super();
        this.serverIP = serverIP;
        this.serverPort = serverPort;
    }
    
    private void connect(String ip, int port) throws Exception
    {
        clientSocket = new Socket(ip, port);
        out = new ObjectOutputStream(clientSocket.getOutputStream());
        out.flush();
        in = new ObjectInputStream(clientSocket.getInputStream());
    }
    
    public void newUser(String username, String password) throws Exception
    {
        connect(serverIP, serverPort);
        out.writeInt(NEW_USER);
        out.writeObject(new Object[] {username, password});
        out.flush();
        out.close();
        
        int responseCode = in.readInt();
        Object responseVal = in.readObject();
        in.close();
        clientSocket.close();
        
        if (responseCode == EXCEPTION) throw new Exception((String)responseVal);
    }
    
    public String login(String username, String password, String name, boolean isPublic) throws Exception
    {
        connect(serverIP, serverPort);
        out.writeInt(LOGIN);
        out.writeObject(new Object[] {username, password, name, isPublic});
        
        int responseCode = in.readInt();
        Object responseVal = in.readObject();        
        clientSocket.close();
        
        if (responseCode == RETURN_VALUE) return (String)responseVal;

        throw new Exception((String)responseVal);
    }
    
    public void logout(String sessionID) throws Exception
    {
        connect(serverIP, serverPort);
        out.writeInt(LOGOUT);
        out.writeObject(new Object[] {sessionID});
        
        int responseCode = in.readInt();
        Object responseVal = in.readObject();        
        clientSocket.close();
        
        if (responseCode == EXCEPTION) throw new Exception((String)responseVal);
    }
    
    public String[] getOnlineClients(String sessionID) throws Exception
    {
        connect(serverIP, serverPort);
        out.writeInt(GET_ONLINE_CLIENTS);
        out.writeObject(new Object[] {sessionID});
        
        int responseCode = in.readInt();
        Object responseVal = in.readObject();        
        clientSocket.close();
        
        if (responseCode == RETURN_VALUE) return (String[])responseVal;
            
        throw new Exception((String)responseVal);
    }
    
    public String[] ipAndPortRequest(String sessionID, String client) throws Exception
    {
        connect(serverIP, serverPort);
        out.writeInt(IP_AND_PORT_REQUEST);
        out.writeObject(new Object[] {sessionID, client});
        
        int responseCode = in.readInt();
        Object responseVal = in.readObject();        
        clientSocket.close();
        
        if (responseCode == RETURN_VALUE) return ((String)responseVal).split(":");
            
        throw new Exception((String)responseVal);
    }
    
    public String clientNameRequest(String sessionID, String ip, int port) throws Exception
    {
        connect(serverIP, serverPort);
        out.writeInt(CLIENT_NAME_REQUEST);
        out.writeObject(new Object[] {sessionID, ip, port});
        
        int responseCode = in.readInt();
        Object responseVal = in.readObject();        
        clientSocket.close();
        
        if (responseCode == RETURN_VALUE) return (String)responseVal;
            
        throw new Exception((String)responseVal);
    }
    
    public void declareFriend(String sessionID, String friend) throws Exception
    {
        connect(serverIP, serverPort);
        out.writeInt(DECLARE_FRIEND);
        out.writeObject(new Object[] {sessionID, friend});
        
        int responseCode = in.readInt();
        Object responseVal = in.readObject();        
        clientSocket.close();
        
        if (responseCode == EXCEPTION) throw new Exception((String)responseVal);
    }

    public void ignoreUser(String sessionID, String friend) throws Exception
    {
        connect(serverIP, serverPort);
        out.writeInt(IGNORE_USER);
        out.writeObject(new Object[] {sessionID, friend});
        
        int responseCode = in.readInt();
        Object responseVal = in.readObject();        
        clientSocket.close();
        
        if (responseCode == EXCEPTION) throw new Exception((String)responseVal);
    }
    
    public void updateTimestamp(String sessionID) throws Exception
    {
        connect(serverIP, serverPort);
        out.writeInt(UPDATE_TIMESTAMP);
        out.writeObject(new Object[] {sessionID});
        
        int responseCode = in.readInt();
        Object responseVal = in.readObject();        
        clientSocket.close();
        
        if (responseCode == EXCEPTION) throw new Exception((String)responseVal);
    }
    
    public String[] pendingInvitationsRequest(String sessionID) throws Exception
    {
        connect(serverIP, serverPort);
        out.writeInt(PENDING_INVITATIONS_REQUEST);
        out.writeObject(new Object[] {sessionID});
        
        int responseCode = in.readInt();
        Object responseVal = in.readObject();        
        clientSocket.close();
        
        if (responseCode == RETURN_VALUE) return (String[])responseVal;
        
        throw new Exception((String)responseVal);
    }

    public String[] showListOfFriends(String sessionID, int property) throws Exception
    {
        connect(serverIP, serverPort);
        out.writeInt(GET_LIST_OF_FRIENDS);
        out.writeObject(new Object[] {sessionID, property});
        
        int responseCode = in.readInt();
        Object responseVal = in.readObject();        
        clientSocket.close();
         
        if (responseCode == RETURN_VALUE) return (String[])responseVal;
        
        throw new Exception((String)responseVal);
    }
    
    public void sendURL(String ip, int port, String url) throws Exception
    {
        connect(ip, port);
        out.writeInt(SEND_URL);
        out.writeUTF(url);
        
        int responseCode = in.readInt();
        Object responseVal = in.readObject();        
        clientSocket.close();
        
        if (responseCode == EXCEPTION) throw new Exception((String)responseVal);
    }
    
    
    public void sendText(String ip, int port, String text) throws Exception
    {
        connect(ip, port);
        out.writeInt(SEND_TEXT);
        out.writeUTF(text);
        
        int responseCode = in.readInt();
        Object responseVal = in.readObject();
        clientSocket.close();

        if (responseCode == EXCEPTION) throw new Exception((String)responseVal);       
    }
    
    public void sendFile(String ip, int port, String path) throws Exception
    {
        connect(ip, port);
        File f = new File(path, "r");
        FileInputStream filein = new FileInputStream(f);
        byte[] bytes = new byte[FILE_BUFFER_SIZE];
        int bytesRead;
        
        out.writeInt(SEND_FILE);
        out.writeObject(new Object[] {f.getName(), f.length()});
        
        while ((bytesRead = filein.read(bytes)) != -1)
            out.write(bytes, 0, bytesRead);
        
        int responseCode = in.readInt();
        Object responseVal = in.readObject();
        clientSocket.close();
        
        if (responseCode == EXCEPTION) throw new Exception((String)responseVal);        
    }
}