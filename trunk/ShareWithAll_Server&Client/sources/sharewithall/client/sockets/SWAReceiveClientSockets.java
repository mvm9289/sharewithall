/**
 * 
 */
package sharewithall.client.sockets;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;

import sharewithall.client.SWAClient;

/**
 * Authors:
 *    Alex Catarineu
 *    Ferran Rigual
 *    Miguel Angel Vico
 *
 * Creation date: Nov 6, 2010
 */
public class SWAReceiveClientSockets extends SWAReceiveSockets
{
    
    private SWAClient client;

    public SWAReceiveClientSockets(int port, SWAClient client)
    {
        super(port);
        this.client = client;
    }

    @Override
    public void process(int instruction, String username, String client, ObjectInputStream in) throws Exception
    {
        switch (instruction)
        {
            case SEND_URL:
                this.client.receiveURL(username, client, in.readUTF());
                break;
            case SEND_TEXT:
                this.client.receiveText(username, client, in.readUTF());
                break;
            case SEND_FILE:
                String filename = in.readUTF();
                int filesize = in.readInt();
                int totalBytes = 0;
                //Security problem here!!! need to check the filename
                //Also have to check if the file already exists
                //Also a limit for the filesize, and a limit for the time reading it
                File file = new File(filename);
                FileOutputStream fileout = new FileOutputStream(file);

                System.out.println("Receiving file from " + username + ":" + client + "...");

                while (true) {
                    Object[] packet = (Object[])in.readObject();
                    int nBytes = (Integer)packet[0];
                    if (nBytes <= 0) break;
                    byte bytes[] = (byte[])packet[1];
                    fileout.write(bytes, 0, nBytes);
                }
                fileout.close();
                this.client.receiveFile(username, client, file.getAbsolutePath());
                break;
            default:
                break;
        }
    }

    @Override
    public String[] obtainSender(String token) throws Exception
    {
        String sessionID = client.getSessionID();
        SWASendSockets s = new SWASendSockets(SWAClient.DEFAULT_SERVER_IP, SWAClient.DEFAULT_SERVER_PORT);
        String clientName = s.clientNameRequest(sessionID, token);
        
        return clientName.split(":");
    }
    
}