/**
 * 
 */
package sharewithall.client.sockets;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
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
                long filesize = in.readLong();
                byte[] bytes = new byte[FILE_BUFFER_SIZE];
                int bytesRead;
                
                //Security problem here!!! need to check the filename
                //Also have to check if the file already exists
                File file = new File(filename, "rw");
                FileOutputStream fileout = new FileOutputStream(file);
                while ((bytesRead = in.read(bytes)) != -1) {
                    fileout.write(bytes, 0, bytesRead);
                    //We could call here to a function in the GUI to update the progress
                }
                fileout.close();
                //Now we need to save the file, maybe we could send the GUI some signals to show the progress
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