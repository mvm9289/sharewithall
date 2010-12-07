package sharewithall.android.client;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

import sharewithall.android.client.sockets.SWAACReceiveSockets;
import sharewithall.android.client.sockets.SWAACSendSockets;
import sharewithall.android.client.sockets.SWAACSendSockets.Command;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.preference.PreferenceManager;

public class SWAACService extends Service
{

	//****************************************//
	//***** Intent actions for broadcast *****//
	//****************************************//
	public static final String ERROR_ACTION = "SWAACServiceError";
	public static final String RECEIVED_MSG_ACTION = "SWAACServiceReceivedMsg";
	public static final String CLIENTS_CHANGED_ACTION = "SWAACServiceClientsChanged";
	public static final String FRIENDS_CHANGED_ACTION = "SWAACServiceFriendsChanged";

	//****************************************************//
	//***** Preferences and configuration attributes *****//
	//****************************************************//
	private SharedPreferences preferences;
	private SharedPreferences.Editor editor;
	private NotificationManager notificator;
	private PendingIntent alarmSender;

	//*********************************************//
	//***** Connection and sending attributes *****//
	//*********************************************//
	private SWAACReceiveSockets receiveSockets;
	private ArrayList<String> audioExtensions;
	private ArrayList<String> imageExtensions;
	private ArrayList<String> videoExtensions;
	
	
	
	
	
	//***************************************//
	//***** Service override functions *****//
	//***************************************//
	
	@Override
	public void onCreate()
	{
		super.onCreate();
		
		configure();
	}
	
	@Override
	public void onStart(Intent intent, int startId)
	{
		super.onStart(intent, startId);
		
		updateTimestamp();
	}
	
	@Override
	public void onDestroy()
	{
        receiveSockets.stop_receiver();
        AlarmManager alarm = (AlarmManager)getSystemService(ALARM_SERVICE);
        alarm.cancel(alarmSender);
        notificator.cancelAll();
		super.onDestroy();
	}
	
	@Override
	public IBinder onBind(Intent intent)
	{
		return null;
	}
	
	
	
	
	
	//***********************************//
	//***** Configuration functions *****//
	//***********************************//
    
	private void configure()
	{
		setForeground(true);
		
		preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		editor = preferences.edit();
    	notificator = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    	
    	audioExtensions = new ArrayList<String>();
    	audioExtensions.add("m4a");
    	audioExtensions.add("mp3");
    	audioExtensions.add("mid");
    	audioExtensions.add("xmf");
    	audioExtensions.add("mxmf");
    	audioExtensions.add("rtttl");
    	audioExtensions.add("rtx");
    	audioExtensions.add("ota");
    	audioExtensions.add("imy");
    	audioExtensions.add("ogg");
    	audioExtensions.add("wav");
    	
    	imageExtensions = new ArrayList<String>();
    	imageExtensions.add("jpg");
    	imageExtensions.add("jpeg");
    	imageExtensions.add("gif");
    	imageExtensions.add("png");
    	imageExtensions.add("bmp");
    	
    	videoExtensions = new ArrayList<String>();
    	videoExtensions.add("3gp");
    	videoExtensions.add("mp4");
		
		configureListenSocket();
		
		configureAlarm();
	}

    private void configureListenSocket()
    {
		String serverIP = preferences.getString("serverIPPref", getString(R.string.defaultServerIP));
		int serverPort = Integer.valueOf(preferences.getString("serverPortPref", getString(R.string.defaultServerPort))).intValue();
    	receiveSockets = new SWAACReceiveSockets(getBaseContext(), handler, serverIP, serverPort);
    	receiveSockets.start();
    }

    private void configureAlarm()
    {
        alarmSender = PendingIntent.getService(this, 0, new Intent(this, SWAACService.class), 0);
        long firstTime = SystemClock.elapsedRealtime() + 30000;
        AlarmManager alarm = (AlarmManager)getSystemService(ALARM_SERVICE);
        alarm.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstTime, 30000, alarmSender);
    }
    
    
    
    
    
	//***********************************//
	//***** Update session function *****//
	//***********************************//
    
    private void updateTimestamp()
    {
    	SWAACSendSockets sendSockets = new SWAACSendSockets(getBaseContext(), Command.UPDATE_TIMESTAMP, handler);
		sendSockets.send();
    }
    
    
    
    

	//**********************************//
	//***** Receive data functions *****//
	//**********************************//
    
    private void messageReceived(String user, String client, String textMessage)
    {
		String idChatWindow = preferences.getString("username", null) +
			preferences.getString("deviceNamePref", getString(R.string.defaultDeviceName)) + user + client;
    	try
		{
	    	FileOutputStream fos = openFileOutput(idChatWindow, MODE_APPEND);
	    	BufferedOutputStream bos = new BufferedOutputStream(fos);
	    	DataOutputStream dos = new DataOutputStream(bos);
    		dos.writeBytes(textMessage);
    		dos.writeByte('\n');
	    	dos.close();
	    	bos.close();
	    	fos.close();
		}
		catch (Exception e) {}
		sendMessageReceivedNotification(idChatWindow, user, client, textMessage);
    }
    
    private void URLReceived(String user, String client, String URL)
    {
    	if (URL.indexOf("http://") != 0) URL = "http://" + URL;
        Uri uriUrl = Uri.parse(URL);
        Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
        launchBrowser.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (!preferences.getString("username", null).equals(user) && !preferences.getBoolean("autolaunchWebPref", true))
        	sendURLNotification(uriUrl, launchBrowser, user, client);
        else startActivity(launchBrowser);
    }
    
    private void FileReceived(String user, String client, String filename, int filesize)
    {
    	int dotExt = filename.lastIndexOf('.');
    	String fileExt = null;
    	if (dotExt != 1) fileExt = filename.substring(dotExt + 1).toLowerCase();
    	File file = new File("/sdcard/ShareWithAll/" + filename);
    	Uri uri = Uri.fromFile(file);
    	Intent launchApp = new Intent();
    	launchApp.setAction(Intent.ACTION_VIEW);
    	if (audioExtensions.contains(fileExt)) launchApp.setDataAndType(uri, "audio/*");
    	else if (imageExtensions.contains(fileExt)) launchApp.setDataAndType(uri, "image/*");
    	else if (videoExtensions.contains(fileExt)) launchApp.setDataAndType(uri, "video/*");
    	else if (fileExt.equals("txt")) launchApp.setDataAndType(uri, "text/*");
    	else launchApp.setDataAndType(uri, "application/" + fileExt);
    	launchApp.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    	if (fileExt == null || !preferences.getString("username", null).equals(user) && !preferences.getBoolean("autolaunchFilePref", true))
    		sendFileReceivedNotification(user, client, filename, filesize, launchApp);
    	else
    	{
    		try
    		{
    			startActivity(launchApp);
    		}
    		catch (Exception e)
    		{
				SWAACUtils.printMessage(this, "Error: " + getString(R.string.fileExtensionError) + " ." + fileExt);
				sendFileReceivedNotification(user, client, filename, filesize, launchApp);
			}
    	}
    	
    }
    
    
    
    

	//**********************************//
	//***** Notification functions *****//
	//**********************************//

    private void sendMessageReceivedNotification(String idChatWindow, String user, String client, String textMessage)
    {
    	if (preferences.getBoolean(idChatWindow + "isOn", false))
    	{
        	Intent messageReceived = new Intent();
        	messageReceived.putExtra("idChatWindow", idChatWindow);
        	messageReceived.putExtra("textMessage", textMessage);
        	messageReceived.setAction(RECEIVED_MSG_ACTION);
        	sendBroadcast(messageReceived);
    	}
    	else
    	{
	    	Notification notification = new Notification(R.drawable.tinyicon, textMessage, System.currentTimeMillis());
	    	Intent intent = new Intent().setClass(this, SWAACChatWindowActivity.class);
	    	intent.putExtra("clientUser", user);
        	intent.putExtra("client", client);
	    	PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
	    	notification.setLatestEventInfo(this, getString(R.string.newMessageNotificationTittle),
	    			getString(R.string.newMessageNotificationMsg) + " " + client, pendingIntent);
	    	notification.defaults = Notification.DEFAULT_VIBRATE;
	    	notificator.notify(R.string.chatNotificationID, notification);
			editor.putBoolean(idChatWindow, true);
			editor.commit();
    	}
    }
    
    private void sendURLNotification(Uri uriUrl, Intent launchBrowser, String user, String client)
    {
    	Notification notification = new Notification(R.drawable.tinyicon, "URL received: " + uriUrl.toString(), System.currentTimeMillis());
    	PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, launchBrowser, 0);
    	notification.setLatestEventInfo(this, getString(R.string.urlNotificationTittle) + " " + user + "@" + client,
    			getString(R.string.urlNotificationMsg) + " " + uriUrl.toString(), pendingIntent);
    	notification.defaults = Notification.DEFAULT_VIBRATE;
    	notification.flags = Notification.FLAG_AUTO_CANCEL;
    	notificator.notify(R.string.urlNotificationID, notification);
    }
    
    private void sendFileReceivedNotification(String user, String client, String filename, int filesize, Intent launchApp)
    {
    	Notification notification = new Notification(R.drawable.tinyicon, getString(R.string.fileReceivedNotificationTicker) + filename +
    			" (" + filesize + "B)", System.currentTimeMillis());
    	PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, launchApp, 0);
    	notification.setLatestEventInfo(this, getString(R.string.fileReceivedNotificationTittle1) + " " + filename + " (" + filesize + "B) " +
    			getString(R.string.fileReceivedNotificationTittle2), getString(R.string.fileReceivedNotificationMsg) + " " + user + "@" +
    			client, pendingIntent);
    	notification.defaults = Notification.DEFAULT_VIBRATE;
    	notification.flags = Notification.FLAG_AUTO_CANCEL;
    	notificator.notify(R.string.fileNotificationID, notification);
    }
    
    private void sendErrorNotification()
    {
    	editor.putBoolean("loggedIn", false);
    	editor.commit();
    	Intent error = new Intent();
    	error.setAction(ERROR_ACTION);
    	sendBroadcast(error);
    }

    private void clientsListChanged()
    {
    	editor.putBoolean("clientListChanged", true);
    	editor.commit();
    	Intent clientsChanged = new Intent();
    	clientsChanged.setAction(CLIENTS_CHANGED_ACTION);
    	sendBroadcast(clientsChanged);
    }
    
    private void friendsListChanged()
    {
    	editor.putBoolean("friendsListChanged", true);
    	editor.commit();
    	Intent friendsChanged = new Intent();
    	friendsChanged.setAction(FRIENDS_CHANGED_ACTION);
    	sendBroadcast(friendsChanged);
    }
    
    private void newInvitation(int invitations)
    {
    	Notification notification = new Notification(R.drawable.tinyicon, getString(R.string.invitationReceivedTicker),
    			System.currentTimeMillis());
    	Intent intent = new Intent().setClass(this, SWAACFriendsListActivity.class);
    	PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
    	notification.setLatestEventInfo(this, getString(R.string.invitationReceivedTittle),
    			getString(R.string.invitationReceivedMsg) + " " + invitations, pendingIntent);
    	notification.defaults = Notification.DEFAULT_VIBRATE;
    	notification.flags = Notification.FLAG_AUTO_CANCEL;
    	notificator.notify(R.string.invitationNotificationID, notification);
    	
    	editor.putBoolean("friendsListChanged", true);
    	editor.commit();
    	Intent friendsChanged = new Intent();
    	friendsChanged.setAction(FRIENDS_CHANGED_ACTION);
    	sendBroadcast(friendsChanged);
    }
    
    
    
    
    
	//********************************************************//
	//***** Handler for connections module communication *****//
	//********************************************************//
    
	final Handler handler = new Handler()
	{
        public void handleMessage(Message msg)
        {
        	Bundle b = msg.getData();
            if (b.getBoolean("exception"))
            {
            	SWAACUtils.printMessage(SWAACService.this, "Error: " + b.getString("message"));
            	sendErrorNotification();
            	stopSelf();
            }
            else if (b.getBoolean("sendText"))
            	messageReceived(b.getString("userReceived"), b.getString("clientReceived"), b.getString("textMessage"));
            else if (b.getBoolean("sendURL"))
            	URLReceived(b.getString("userReceived"), b.getString("clientReceived"), b.getString("URLMessage"));
            else if (b.getBoolean("sendFile"))
            	FileReceived(b.getString("userReceived"), b.getString("clientReceived"),
            			b.getString("filename"), b.getInt("filesize"));
            else if (b.getBoolean("clientsChanged")) clientsListChanged();
            else if (b.getBoolean("friendsChanged")) friendsListChanged();
            else if (b.getBoolean("pendingInvitation")) newInvitation(b.getInt("nInvitations"));
        }
    };

}
