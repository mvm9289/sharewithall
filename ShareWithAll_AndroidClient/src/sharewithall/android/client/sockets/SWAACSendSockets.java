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
		NEW_USER, LOGIN, LOGOUT, GET_ONLINE_CLIENTS, GET_SEND_TOKEN, IP_AND_PORT_REQUEST,
		DECLARE_FRIEND, UPDATE_TIMESTAMP, IGNORE_USER, PENDING_INVITATIONS_REQUEST,
		GET_LIST_OF_FRIENDS, CLIENT_NAME_REQUEST, SEND_URL, SEND_TEXT, SEND_FILE
	}
	
	public enum Property
	{
		FRIENDS, DECLARED, EXPECTING, IGNORED
	}
	
	private Command command;
	private Handler handler;
	private Object[] data;
	private SharedPreferences preferences;
	private SWASendSockets socketsModule;
	
	public SWAACSendSockets(Context baseContext, Command command, Handler handler)
	{
		super();
		this.command = command;
		this.handler = handler;
		preferences = PreferenceManager.getDefaultSharedPreferences(baseContext);
		socketsModule = new SWASendSockets(preferences.getString("serverIPPref", "mvm9289.dyndns.org"),
				Integer.valueOf(preferences.getString("serverPortPref", "4040")).intValue());
	}
	
	public SWAACSendSockets(Context baseContext, Command command, Handler handler, Object[] data)
	{
		this(baseContext, command, handler);
		this.data = data;
	}
	
	private void newUser()
	{
    	String username = (String) data[0];
    	String password = (String) data[1];
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
    	String username = (String) data[0];
    	String password = (String) data[1];
    	String deviceName = preferences.getString("deviceNamePref", "Android-Device");
    	boolean isPublic = preferences.getBoolean("isPublicPref", false);
		try
		{
			String sessionID = socketsModule.login(username, password, deviceName, isPublic);
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
    	String sessionID = preferences.getString("sessionID", null);
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
    	String sessionID = preferences.getString("sessionID", null);
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
	
	private void getSendToken()
	{
    	String sessionID = preferences.getString("sessionID", null);
    	String client = (String) data[0];
		try
		{
			String token = socketsModule.getSendToken(sessionID, client);
			Bundle b = new Bundle();
			b.putBoolean("getSendToken", true);
			b.putString("token", token);
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
	
	private void ipAndPortRequest()
	{
    	String sessionID = preferences.getString("sessionID", null);
    	String client = (String) data[0];
		try
		{
			String[] ipAndPort = socketsModule.ipAndPortRequest(sessionID, client);
			Bundle b = new Bundle();
			b.putBoolean("ipAndPortRequest", true);
			b.putStringArray("ipAndPort", ipAndPort);
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
	
	private void declareFriend()
	{
    	String sessionID = preferences.getString("sessionID", null);
    	String friend = (String) data[0];
    	try
    	{
			socketsModule.declareFriend(sessionID, friend);
			Bundle b = new Bundle();
			b.putBoolean("declareFriend", true);
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
    	String sessionID = preferences.getString("sessionID", null);
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
	
	private void ignoreFriend()
	{
    	String sessionID = preferences.getString("sessionID", null);
    	String friend = (String) data[0];
    	try
    	{
			socketsModule.ignoreUser(sessionID, friend);
			Bundle b = new Bundle();
			b.putBoolean("ignoreFriend", true);
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
		String sessionID = preferences.getString("sessionID", null);
		try
		{
			String[] aux1 = socketsModule.showListOfFriends(sessionID, SWASendSockets.PROPERTY_FRIENDS);
			String[] aux2 = socketsModule.showListOfFriends(sessionID, SWASendSockets.PROPERTY_DECLARED_FRIEND);
			String[] aux3 = socketsModule.showListOfFriends(sessionID, SWASendSockets.PROPERTY_EXPECTING);
			String[] aux4 = socketsModule.showListOfFriends(sessionID, SWASendSockets.PROPERTY_IGNORED);
			
			String[] friends = new String[2*(aux1.length + aux2.length + aux3.length + aux4.length)];
			int k = 0;
			for (int i = 0; i < aux1.length; i++)
			{
				friends[k++] = aux1[i];
				friends[k++] = Property.FRIENDS.toString();
			}
			for (int i = 0; i < aux2.length; i++)
			{
				friends[k++] = aux2[i];
				friends[k++] = Property.DECLARED.toString();
			}
			for (int i = 0; i < aux3.length; i++)
			{
				friends[k++] = aux3[i];
				friends[k++] = Property.EXPECTING.toString();
			}
			for (int i = 0; i < aux4.length; i++)
			{
				friends[k++] = aux4[i];
				friends[k++] = Property.IGNORED.toString();
			}
			
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
	
	private void sendText()
	{
		String token = (String) data[0];
    	String ip = (String) data[1];
    	int port = Integer.valueOf((String) data[2]).intValue();
    	String text = (String) data[3];
    	try
    	{
			socketsModule.sendText(token, ip, port, text);
			Bundle b = new Bundle();
			b.putBoolean("sendText", true);
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
			case GET_SEND_TOKEN:
				getSendToken();
				break;
			case IP_AND_PORT_REQUEST:
				ipAndPortRequest();
				break;
			case DECLARE_FRIEND:
				declareFriend();
				break;
			case UPDATE_TIMESTAMP:
				updateTimestamp();
				break;
			case IGNORE_USER:
				ignoreFriend();
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
				sendText();
				break;
			case SEND_FILE:
				break;
			default:
				break;
		}
    }
	
}
