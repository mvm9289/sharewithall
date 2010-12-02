/**
 * 
 */
package sharewithall.client.sockets;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Authors:
 *    Alex Catarineu
 *    Ferran Rigual
 *    Miguel Angel Vico
 *
 * Creation date: Nov 6, 2010
 */
public abstract class SWAReceiveSockets extends Thread
{

    protected static final int SEND_URL = 12;
    protected static final int SEND_TEXT = 13;
    protected static final int SEND_FILE = 14;
    protected static final int RECEIVE_GATEWAY = 17;
    protected static final int RETURN_VALUE = 0;
    protected static final int EXCEPTION = -1;
    protected static final int FILE_BUFFER_SIZE = 4096;
    
    private ServerSocket receiverSocket;
    private boolean gateway;
    private String serverIP;
    private int serverPort;
    
    public SWAReceiveSockets(int port)
    {
        super();
        try
        {
            receiverSocket = new ServerSocket(port);
            gateway = false;
        }
        catch (Exception e)
        {
            System.out.println("Server exception: " + e.getClass() + ":" + e.getMessage());
        }
    }
    
    public SWAReceiveSockets(String serverIP, int serverPort)
    {
        super();
        try
        {
            gateway = true;
            this.serverIP = serverIP;
            this.serverPort = serverPort;
        }
        catch (Exception e)
        {
            System.out.println("Server exception: " + e.getClass() + ":" + e.getMessage());
        }
    }

    public abstract void process(int instruction, String username, String client, ObjectInputStream in) throws Exception;
    
    public abstract String[] obtainSender(String token) throws Exception;
    public abstract String getSessionID();
        
    public void run()
    {
        if (gateway) {
            try {
                makeGateway();
            }
            catch (Exception e)
            {
                System.out.println("Server exception: " + e.getClass() + ":" + e.getMessage());
            }
            while (true); //Necessary?
        }
        else {
            while (true) {
                try
                {
                    Socket clientSocket = receiverSocket.accept();
                    Thread socket_thread = new SWASocketsThread(clientSocket);
                    socket_thread.start();
                }
                catch (Exception e)
                {
                    System.out.println("Server exception: " + e.getClass() + ":" + e.getMessage());
                }
            }
        }
    }
    
    private void makeGateway() throws Exception {
        Socket sock = new Socket(serverIP, serverPort);
        ObjectOutputStream out = new ObjectOutputStream(sock.getOutputStream());
        out.flush();
        ObjectInputStream in = new ObjectInputStream(sock.getInputStream());
        out.writeInt(RECEIVE_GATEWAY);
        out.writeObject(new Object[] {getSessionID()});
        out.flush();
        
        int returnCode = in.readInt();
        Object returnVal = in.readObject();
        if (returnCode == EXCEPTION) throw new Exception((String)returnVal);
        Thread socket_thread = new SWASocketsThread(sock);
        socket_thread.start();
    }
    
    private class SWASocketsThread extends Thread
    {
        
        private Socket clientSocket;
        
        private SWASocketsThread(Socket clientSocket)
        {
            super();
            this.clientSocket = clientSocket;
        }
        
        private void decodeAndProcess()
        {   
            try
            {
                ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
                out.flush();
                ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
                int instruction = in.readInt();
                String token = in.readUTF();
                
                if (gateway) makeGateway();

                try
                {
                    String[] sender = obtainSender(token);
                    String user = sender[0];
                    String client = sender[1];
                    process(instruction, user, client, in);
                    out.writeInt(RETURN_VALUE);
                    out.writeUTF("");
                }
                catch (Exception e)
                {
                    out.writeInt(EXCEPTION);
                    if (e.getClass() == Exception.class) out.writeUTF(e.getMessage());
                    else {
                        e.printStackTrace();
                        out.writeUTF("Remote Exception");
                    }
                }
                finally {
                    out.flush();
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
                //System.out.println("Server exception: " + e.getClass() + ":" + e.getMessage());
            }
        }
        
        public void run()
        {
            try
            {
                decodeAndProcess();
                clientSocket.close();
            }
            catch (Exception e)
            {
                System.out.println("Server exception: " + e.getClass() + ":" + e.getMessage());
            }
        }
        
    }
    
}