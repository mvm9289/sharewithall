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
    protected static final int FILE_BUFFER_SIZE = 4096;
    
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

    public abstract void process(int instruction, String username, String client, ObjectInputStream in) throws Exception;
    
    public abstract String[] obtainSender(String token) throws Exception;
    
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

                try
                {
                    String[] sender = obtainSender(token);
                    String user = sender[0];
                    String client = sender[1];
                    process(instruction, user, client, in);
                    out.writeInt(RETURN_VALUE);
                    out.writeObject(null);
                }
                catch (Exception e)
                {
                    out.writeInt(EXCEPTION);
                    if (e.getClass() == Exception.class) out.writeObject(e.getMessage());
                    else {
                        e.printStackTrace();
                        out.writeObject("Remote Exception");
                    }
                }
                finally {
                    out.flush();
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