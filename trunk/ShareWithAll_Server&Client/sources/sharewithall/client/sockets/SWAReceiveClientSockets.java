/**
 * 
 */
package sharewithall.client.sockets;

import java.io.File;
import java.io.FileOutputStream;
import java.io.DataInputStream;

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
    public SWAReceiveClientSockets(String serverIP, int serverPort, SWAClient client)
    {
        super(serverIP, serverPort);
        this.client = client;
    }
    @Override
    public void process(int instruction, String username, String client, DataInputStream in) throws Exception
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
                
                //Security problem here!!! need to check the filename
                //Also have to check if the file already exists
                //Also a limit for the filesize, and a limit for the time reading it
                System.out.println("Receiving file of " + filesize + " bytes");
                File file = new File(filename);
                FileOutputStream fileout = new FileOutputStream(file);
                int bytesRead;
                byte bytes[] = new byte[FILE_BUFFER_SIZE];
                while ((bytesRead = in.read(bytes)) > 0)
                {
                    fileout.write(bytes, 0, bytesRead);
                    fileout.flush();
                }
                fileout.close();
                this.client.receiveFile(username, client, file.getAbsolutePath());
                break;
            case NOTIFY_CLIENTS_CHANGED:
                this.client.RefreshListOfOnlineClients();
                break;
            case NOTIFY_FRIENDS_CHANGED:
                this.client.RefreshListOfFriends();
                break;
            case NOTIFY_INVITATION:
                this.client.RefreshListOfFriends();
                //this.client.RefreshInvitations();
                break;
            default:
                break;
        }
    }

    @Override
    public String[] obtainSender(String token) throws Exception
    {
        String sessionID = client.getSessionID();
        SWASendSockets s = new SWASendSockets(client.serverIP, client.serverPort);
        String clientName = s.clientNameRequest(sessionID, token);
        
        return clientName.split(":");
    }
    @Override
    public String getSessionID()
    {
        return client.getSessionID();
    }
    
}