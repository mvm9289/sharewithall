/**
 * 
 */
package sharewithall.client.sockets;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

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
    protected static final int RETURN_VALUE = 0;
    protected static final int EXCEPTION = -1;
    
    private ServerSocket receiverSocket;
    
    public SWAReceiveSockets(int port)
    {
        super();
        try
        {
            receiverSocket = new ServerSocket(port);
        }
        catch (Exception e)
        {
            System.out.println("Server exception: " + e.getClass() + ":" + e.getMessage());
        }
    }

    public abstract void process(int instruction, Object data, String username, String client);
    
    public void run()
    {
        while (true)
        {
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
    
    private class SWASocketsThread extends Thread
    {
        
        private Socket clientSocket;
        private String user, client;
        
        private SWASocketsThread(Socket clientSocket)
        {
            super();
            this.clientSocket = clientSocket;
        }
        
        private void decodeAndProcess()
        {   
            try
            {
                user = null; //TODO: Obtener usuario mediante token.
                client = null; //TODO: Obtener cliente mediante token.
                //TODO: Comprobar amistad mediante token.
                
                ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
                out.flush();
                ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
                int instruction = in.readInt();
                Object[] params = (Object[])in.readObject();
                
                try
                {
                    switch (instruction)
                    {
                        case SEND_URL:
                            //TODO: Leer url (texto utf)
                            process(SEND_URL, new Object()/*TODO: String*/, user, client);
                            out.writeUTF(String.valueOf(RETURN_VALUE));
                            break;
                        case SEND_TEXT:
                            //TODO: Leer texto utf
                            process(SEND_TEXT, (String) params[0], user, client);
                            out.writeUTF(String.valueOf(RETURN_VALUE));
                            break;
                        case SEND_FILE:
                            //TODO: Leer archivo (bytes)
                            process(SEND_FILE, new Object()/*TODO: File*/, user, client);
                            out.writeUTF(String.valueOf(RETURN_VALUE));
                            break;
                        default:
                            throw new Exception("Wrong instruction identifier.");
                    }
                }
                catch (Exception e)
                {
                    if (e.getClass() == Exception.class) out.writeUTF(String.valueOf(EXCEPTION) + ";" + e.getMessage());
                    else out.writeUTF(String.valueOf(EXCEPTION) + ";Server Exception");
                }
            }
            catch (Exception e)
            {
                System.out.println("Server exception: " + e.getClass() + ":" + e.getMessage());
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