/**
 * 
 */
package sharewithall.client.sockets;

import java.io.File;
import java.io.FileOutputStream;
import java.io.DataInputStream;

import sharewithall.client.FileGraphicalInterface;
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
                if (!this.client.receive_files && !this.client.username.equals(username))
                    throw new Exception("Receiving files disabled");
            
                String filename = in.readUTF();
                int filesize = in.readInt();

                boolean[] stopper = new boolean[] {false};
                
                File file = new File(filename);
                String name = file.getName();
                file = new File(name);
                
                int count = 1;
                int index = name.indexOf('.');
                
                while (file.exists()) {
                    if (index == -1) file = new File(name + "(" + count + ")");
                    else file = new File(name.substring(0, index) + "(" + count + ")" + name.substring(index));
                    count++;
                }
                
                FileGraphicalInterface graphical = this.client.program.newDownload(username + "@" + client, file.getAbsolutePath(), filesize, username.equals(this.client.username), stopper);
                FileOutputStream fileout = new FileOutputStream(file);
                int bytesRead;
                int totalBytes = 0;
                byte bytes[] = new byte[FILE_BUFFER_SIZE];
                while (!stopper[0] && (bytesRead = in.read(bytes)) > 0)
                {
                    fileout.write(bytes, 0, bytesRead);
                    fileout.flush();
                    totalBytes += bytesRead;
                    graphical.setProgress(totalBytes);
                }
                fileout.close();
                if (!stopper[0]) graphical.finishedDownload();
                else file.delete();
 
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