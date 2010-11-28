package sharewithall.android.client.sockets;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;

public class SWAACSendSockets extends Thread
{
    
	public enum Command
	{
		NEW_USER, LOGIN, LOGOUT, GET_ONLINE_CLIENTS, IP_AND_PORT_REQUEST,
		DECLARE_FRIEND, UPDATE_TIMESTAMP, IGNORE_USER, PENDING_INVITATIONS_REQUEST,
		GET_LIST_OF_FRIENDS, CLIENT_NAME_REQUEST, SEND_URL, SEND_TEXT, SEND_FILE
	}
	
	public enum Property
	{
		FRIENDS, DECLARED, EXPECTING, IGNORED
	}
	
	Context baseContext;
	Command command;
	Handler handler;
	Object[] data;
	
	public SWAACSendSockets(Context baseContext, Command command, Handler handler)
	{
		super();
		this.baseContext = baseContext;
		this.command = command;
		this.handler = handler;
	}
	
	public SWAACSendSockets(Context baseContext, Command command, Handler handler, Object[] data)
	{
		this(baseContext, command, handler);
		this.data = data;
	}
	
	private void newUser()
	{
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(baseContext);
		String serverIP = preferences.getString("serverIPPref", "mvm9289.dyndns.org");
    	int serverPort = Integer.valueOf(preferences.getString("serverPort", "4040")).intValue();
    	SWASendSockets socketsModule = new SWASendSockets(serverIP, serverPort);

    	String username = String.valueOf(data[0]);
    	String password = String.valueOf(data[1]);
    	
		try
		{
			socketsModule.newUser(username, password);
			Bundle b = new Bundle();
			b.putBoolean("newUser", true);
			Message msg = handler.obtainMessage();
			msg.setData(b);
			handler.sendMessage(msg);
		}
		catch (Exception e)
		{
			Bundle b = new Bundle();
			b.putBoolean("exception", true);
			b.putString("message", e.getMessage());
			Message msg = handler.obtainMessage();
			msg.setData(b);
			handler.sendMessage(msg);
		}
	}
	
	private void login()
	{
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(baseContext);
		String serverIP = preferences.getString("serverIPPref", "mvm9289.dyndns.org");
    	int serverPort = Integer.valueOf(preferences.getString("serverPortPref", "4040")).intValue();
    	SWASendSockets socketsModule = new SWASendSockets(serverIP, serverPort);

    	String username = String.valueOf(data[0]);
    	String password = String.valueOf(data[1]);
    	String deviceName = preferences.getString("deviceNamePref", "Android-Device");
    	boolean isPublic = preferences.getBoolean("isPublicPref", false);
    	
    	String sessionID = null;
		try
		{
			sessionID = socketsModule.login(username, password, deviceName, isPublic);
			Bundle b = new Bundle();
			b.putBoolean("login", true);
			b.putString("sessionID", sessionID);
			Message msg = handler.obtainMessage();
			msg.setData(b);
			handler.sendMessage(msg);
		}
		catch (Exception e)
		{
			Bundle b = new Bundle();
			b.putBoolean("exception", true);
			b.putString("message", e.getMessage());
			Message msg = handler.obtainMessage();
			msg.setData(b);
			handler.sendMessage(msg);
		}
	}
	
	private void logout()
	{
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(baseContext);
		String serverIP = preferences.getString("serverIPPref", "mvm9289.dyndns.org");
    	int serverPort = Integer.valueOf(preferences.getString("serverPortPref", "4040")).intValue();
    	String sessionID = preferences.getString("sessionID", null);
    	SWASendSockets socketsModule = new SWASendSockets(serverIP, serverPort);
    	
		try
		{
			socketsModule.logout(sessionID);
			Bundle b = new Bundle();
			b.putBoolean("logout", true);
			Message msg = handler.obtainMessage();
			msg.setData(b);
			handler.sendMessage(msg);
		}
		catch (Exception e)
		{
			Bundle b = new Bundle();
			b.putBoolean("exception", true);
			b.putString("message", e.getMessage());
			Message msg = handler.obtainMessage();
			msg.setData(b);
			handler.sendMessage(msg);
		}
	}
	
	private void getOnlineClients()
	{
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(baseContext);
		String serverIP = preferences.getString("serverIPPref", "mvm9289.dyndns.org");
    	int serverPort = Integer.valueOf(preferences.getString("serverPortPref", "4040")).intValue();
    	String sessionID = preferences.getString("sessionID", null);
    	SWASendSockets socketsModule = new SWASendSockets(serverIP, serverPort);
    	
		try
		{
			String[] onlineClients = socketsModule.getOnlineClients(sessionID);
			Bundle b = new Bundle();
			b.putBoolean("getOnlineClients", true);
			b.putStringArray("onlineClients", onlineClients);
			Message msg = handler.obtainMessage();
			msg.setData(b);
			handler.sendMessage(msg);
		}
		catch (Exception e)
		{
			Bundle b = new Bundle();
			b.putBoolean("exception", true);
			b.putString("message", e.getMessage());
			Message msg = handler.obtainMessage();
			msg.setData(b);
			handler.sendMessage(msg);
		}
	}
	
	private void updateTimestamp()
	{
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(baseContext);
    	String serverIP = preferences.getString("serverIPPref", "mvm9289.dyndns.org");
    	int serverPort = Integer.valueOf(preferences.getString("serverPortPref", "4040")).intValue();
    	String sessionID = preferences.getString("sessionID", null);
    	
    	SWASendSockets socketsModule = new SWASendSockets(serverIP, serverPort);
    	try
    	{
			socketsModule.updateTimestamp(sessionID);
			Bundle b = new Bundle();
			b.putBoolean("updateTimestamp", true);
			Message msg = handler.obtainMessage();
			msg.setData(b);
			handler.sendMessage(msg);
		}
    	catch (Exception e)
    	{
    		Bundle b = new Bundle();
			b.putBoolean("exception", true);
			b.putString("message", e.getMessage());
			Message msg = handler.obtainMessage();
			msg.setData(b);
			handler.sendMessage(msg);
		}
	}
	
	private void getListOfFriends()
	{
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(baseContext);
		String serverIP = preferences.getString("serverIPPref", "mvm9289.dyndns.org");
		int serverPort = Integer.valueOf(preferences.getString("serverPortPref", "4040")).intValue();
		String sessionID = preferences.getString("sessionID", null);
		
		SWASendSockets socketsModule = new SWASendSockets(serverIP, serverPort);
		try
		{
			String[] aux1 = socketsModule.showListOfFriends(sessionID, SWASendSockets.PROPERTY_FRIENDS);
			String[] aux2 = socketsModule.showListOfFriends(sessionID, SWASendSockets.PROPERTY_DECLARED_FRIEND);
			String[] aux3 = socketsModule.showListOfFriends(sessionID, SWASendSockets.PROPERTY_EXPECTING);
			String[] aux4 = socketsModule.showListOfFriends(sessionID, SWASendSockets.PROPERTY_IGNORED);
			String[] friends = new String[aux1.length + aux2.length + aux3.length + aux4.length];
			int k = 0;
			for (int i = 0; i < aux1.length; i++)
				friends[k++] = aux1[i] + ";accepted";
			for (int i = 0; i < aux2.length; i++)
				friends[k++] = aux2[i] + ";declared";
			for (int i = 0; i < aux3.length; i++)
				friends[k++] = aux3[i] + ";expecting";
			for (int i = 0; i < aux4.length; i++)
				friends[k++] = aux4[i] + ";ignored";
			
			Bundle b = new Bundle();
			b.putBoolean("getListOfFriends", true);
			b.putStringArray("friends", friends);
			Message msg = handler.obtainMessage();
			msg.setData(b);
			handler.sendMessage(msg);
		}
		catch (Exception e)
		{
			Bundle b = new Bundle();
			b.putBoolean("exception", true);
			b.putString("message", e.getMessage());
			Message msg = handler.obtainMessage();
			msg.setData(b);
			handler.sendMessage(msg);
		}
	}
	
	public void send()
	{
		start();
	}
	
	public void run()
    {
    	switch (command)
    	{
			case NEW_USER:
				newUser();
				break;
			case LOGIN:
				login();
				break;
			case LOGOUT:
				logout();
				break;
			case GET_ONLINE_CLIENTS:
				getOnlineClients();
				break;
			case IP_AND_PORT_REQUEST:
				break;
			case DECLARE_FRIEND:
				break;
			case UPDATE_TIMESTAMP:
				updateTimestamp();
				break;
			case IGNORE_USER:
				break;
			case PENDING_INVITATIONS_REQUEST:
				break;
			case GET_LIST_OF_FRIENDS:
				getListOfFriends();
				break;
			case CLIENT_NAME_REQUEST:
				break;
			case SEND_URL:
				break;
			case SEND_TEXT:
				break;
			case SEND_FILE:
				break;
			default:
				break;
		}
    }
	
}
