/**
 * 
 */
package sharewithall.client.sockets;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;

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
    protected static final int NOTIFY_CLIENTS_CHANGED = 18;
    protected static final int NOTIFY_FRIENDS_CHANGED = 19;
    protected static final int NOTIFY_INVITATION = 20;
    protected static final int RETURN_VALUE = 0;
    protected static final int EXCEPTION = -1;
    protected static final int FILE_BUFFER_SIZE = 4096;
    protected static final int MAX_THREADS = 20;
    
    private ServerSocket receiverSocket;
    private boolean gateway;
    private String serverIP;
    private int serverPort;
    private ArrayBlockingQueue<Thread> threads_queue = new ArrayBlockingQueue<Thread>(MAX_THREADS);
    private boolean stop = false;
    
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

    public abstract void process(int instruction, String username, String client, DataInputStream in) throws Exception;
    
    public abstract String[] obtainSender(String token) throws Exception;
    public abstract String getSessionID();
    
    public void stop_receiver() {
        System.out.println("Closing receiver...");
        stop = true;
        this.interrupt();
    }
    
    private void kill_threads() {
        while (!threads_queue.isEmpty()) {
            try {
                    Thread t = threads_queue.poll();
                    t.interrupt();
            }
            catch (Exception e) {
                
            }
        }
    }
    
    public void run()
    {
        System.out.println("Starting main thread");
        if (gateway) {
            while (true) {
                try {

                    makeGateway();
                }
                catch (Exception e)
                {
                    System.out.println("Server exception: " + e.getClass() + ":" + e.getMessage());
                }
                finally {
                    if (stop) {
                        kill_threads();
                        System.out.println("Exiting main thread");
                        return;
                    }
                }
            }
        }
        else {
            while (true) {
                try
                {
                    Socket clientSocket = receiverSocket.accept();
                    DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
                    out.flush();
                    DataInputStream in = new DataInputStream(clientSocket.getInputStream());
                    Thread socket_thread = new SWASocketsThread(clientSocket, in, out);
                    threads_queue.put(socket_thread);
                    if (!stop) socket_thread.start();
                }
                catch (Exception e)
                {
                    System.out.println("Server exception: " + e.getClass() + ":" + e.getMessage());
                }
                finally {
                    if (stop) {
                        kill_threads();
                        System.out.println("Exiting main thread");
                        return;
                    }
                }
            }
        }
        
    }
    
    private void makeGateway() throws Exception {
        Socket sock = new Socket(serverIP, serverPort);
        DataOutputStream out = new DataOutputStream(sock.getOutputStream());
        out.writeInt(RECEIVE_GATEWAY);
        out.writeUTF(getSessionID());
        out.flush();
        
        DataInputStream in = new DataInputStream(sock.getInputStream());
        int returnCode = in.readInt();
        if (returnCode == EXCEPTION) throw new Exception(in.readUTF());
        Thread socket_thread = new SWASocketsThread(sock, in, out);
        threads_queue.put(socket_thread);
        if (!stop) socket_thread.start();
    }
    
    private class SWASocketsThread extends Thread
    {
        
        private Socket clientSocket;
        private DataInputStream in;
        private DataOutputStream out;
        
        private SWASocketsThread(Socket clientSocket, DataInputStream in, DataOutputStream out)
        {
            super();
            this.clientSocket = clientSocket;
            this.in = in;
            this.out = out;
        }
        
        private void decodeAndProcess()
        {   
            try
            {
                int instruction = in.readInt();
                String token = in.readUTF();
                try
                {
                    if (instruction == NOTIFY_INVITATION || instruction == NOTIFY_CLIENTS_CHANGED || instruction == NOTIFY_FRIENDS_CHANGED) {
                        //if (clientSocket.getInetAddress().getHostAddress().equals(serverIP)) {
                            process(instruction, "server", "server", null);
                        //}
                    }
                    else {
                        String[] sender = obtainSender(token);
                        String user = sender[0];
                        String client = sender[1];
                        process(instruction, user, client, in);
                    }
                    out.writeInt(RETURN_VALUE);
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
            System.out.println("Starting child thread " + threads_queue.size());
            
            try
            {
                decodeAndProcess();
                clientSocket.close();
            }
            catch (Exception e)
            {
                System.out.println("Server exception: " + e.getClass() + ":" + e.getMessage());
            }
            finally {
                threads_queue.remove(this);
            }
            System.out.println("Exiting child thread " + threads_queue.size());
        }
        
    }
    
}