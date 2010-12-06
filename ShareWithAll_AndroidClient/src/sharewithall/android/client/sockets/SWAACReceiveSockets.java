package sharewithall.android.client.sockets;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;

import sharewithall.android.client.R;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;

public class SWAACReceiveSockets extends SWAReceiveSockets
{

	private SharedPreferences preferences;
	private Handler handler;
	private Context baseContext;
	
	public SWAACReceiveSockets(Context baseContext, Handler handler, String serverIP, int serverPort)
	{
		super(serverIP, serverPort);
		preferences = PreferenceManager.getDefaultSharedPreferences(baseContext);
		this.baseContext = baseContext;
		this.handler = handler;
	}

	@Override
	public String[] obtainSender(String token) throws Exception
	{
		String sessionID = preferences.getString("sessionID", null);
		String serverIP = preferences.getString("serverIPPref", baseContext.getString(R.string.defaultServerIP));
		int serverPort = Integer.valueOf(preferences.getString("serverPortPref", baseContext.getString(R.string.defaultServerPort))).intValue();
	    SWASendSockets sendSockets = new SWASendSockets(serverIP, serverPort);
	    String clientName = sendSockets.clientNameRequest(sessionID, token);
	    
	    return clientName.split(":");
	}

	@Override
	public String getSessionID()
	{
		return preferences.getString("sessionID", null);
	}

	@Override
	public void process(int instruction, String username, String client, DataInputStream in) throws Exception
	{
		Bundle b = new Bundle();
		Message msg;
		switch (instruction)
		{
			case SEND_TEXT:
				String text = client + ": " + in.readUTF();
				b.putBoolean("sendText", true);
				b.putString("userReceived", username);
				b.putString("clientReceived", client);
				b.putString("textMessage", text);
				msg = handler.obtainMessage();
				msg.setData(b);
				handler.sendMessage(msg);
				break;
			case SEND_URL:
				String URL = in.readUTF();
				b.putBoolean("sendURL", true);
				b.putString("userReceived", username);
				b.putString("clientReceived", client);
				b.putString("URLMessage", URL);
				msg = handler.obtainMessage();
				msg.setData(b);
				handler.sendMessage(msg);
				break;
			case SEND_FILE:
				if (!preferences.getString("username", null).equals(username) &&
						!preferences.getBoolean("allowReceiveFilesPref", true))
					throw new Exception("File reception not allowed");
				
				String filename = in.readUTF();
                int filesize = in.readInt();
                
                File root = Environment.getExternalStorageDirectory();
                if (!root.canWrite()) throw new Exception("External memory error");
                
                File dir = new File("/sdcard/ShareWithAll");
                dir.mkdir();
                
                File file = new File(dir, filename);
                FileOutputStream fileout = new FileOutputStream(file);
                int bytesRead;
                byte bytes[] = new byte[FILE_BUFFER_SIZE];
                while ((bytesRead = in.read(bytes)) > 0)
                {
                    fileout.write(bytes, 0, bytesRead);
                    fileout.flush();
                }
                fileout.close();
				b.putBoolean("sendFile", true);
				b.putString("userReceived", username);
				b.putString("clientReceived", client);
				b.putString("filename", filename);
				b.putInt("filesize", filesize);
				msg = handler.obtainMessage();
				msg.setData(b);
				handler.sendMessage(msg);
				break;
			case NOTIFY_CLIENTS_CHANGED:
				b.putBoolean("clientsChanged", true);
				msg = handler.obtainMessage();
				msg.setData(b);
				handler.sendMessage(msg);
				break;
			case NOTIFY_FRIENDS_CHANGED:
				b.putBoolean("friendsChanged", true);
				msg = handler.obtainMessage();
				msg.setData(b);
				handler.sendMessage(msg);
				break;
			case NOTIFY_INVITATION:
				String sessionID = preferences.getString("sessionID", null);
				String serverIP = preferences.getString("serverIPPref", baseContext.getString(R.string.defaultServerIP));
				int serverPort = Integer.valueOf(preferences.getString("serverPortPref", baseContext.getString(R.string.defaultServerPort))).intValue();
			    SWASendSockets sendSockets = new SWASendSockets(serverIP, serverPort);
			    String[] invitations = sendSockets.pendingInvitationsRequest(sessionID);
				b.putBoolean("pendingInvitation", true);
				b.putInt("nInvitations", invitations.length);
				msg = handler.obtainMessage();
				msg.setData(b);
				handler.sendMessage(msg);
				break;
			default:
				break;
		}
	}
}
