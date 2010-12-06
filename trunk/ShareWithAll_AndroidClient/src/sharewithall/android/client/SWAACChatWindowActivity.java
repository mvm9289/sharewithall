package sharewithall.android.client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;

import sharewithall.android.client.sockets.SWAACSendSockets;
import sharewithall.android.client.sockets.SWAACSendSockets.Command;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class SWAACChatWindowActivity extends Activity
{
	
	//****************************************************//
	//***** Preferences and configuration attributes *****//
	//****************************************************//
	private SharedPreferences preferences;
	private SharedPreferences.Editor editor;
	private BroadcastReceiver broadcastReceiver;
	private IntentFilter intentFilter;
	private NotificationManager notificator;
	private ArrayAdapter<String> outputText;
	private String idChatWindow;

	//*********************************************//
	//***** Connection and sending attributes *****//
	//*********************************************//
	private SWAACSendSockets sendSockets;
	private String user;
	private String client;
	private String token;
	private String clientIP;
	private String clientPort;
	private boolean preparedToSend;
	private ArrayList<String> pendingToSend;

	
	

	
	//***************************************//
	//***** Activity override functions *****//
	//***************************************//
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		configure();
	}
	
	@Override
	protected void onResume()
	{
		super.onResume();
		
		registerReceiver(broadcastReceiver, intentFilter);
		
		editor.putBoolean(idChatWindow + "isOn", true);
		editor.commit();
		
    	if (preferences.getBoolean(idChatWindow, false))
    	{
    		notificator.cancel(R.string.chatNotificationID);
    		reloadText();
    		editor.remove(idChatWindow);
    		editor.commit();
    	}
	}
	
	@Override
	protected void onPause()
	{
		unregisterReceiver(broadcastReceiver);
		
		editor.remove(idChatWindow + "isOn");
		editor.commit();
		
		super.onPause();
	}
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.chatmenu, menu);
        
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
	        case R.id.clearHistoryMenu:
	        	clearChatHistory();
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
        }
    }
    
    
    

    
	//***********************************//
	//***** Configuration functions *****//
	//***********************************//
    
    private void configure()
    {
		preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		editor = preferences.edit();
		configureBroadcastReceiver();
		
		String username = preferences.getString("username", null);
		String clientName = preferences.getString("deviceNamePref", getString(R.string.defaultDeviceName));
		
		user = getIntent().getStringExtra("clientUser");
		if (user.equals("")) user = username;
		client = getIntent().getStringExtra("client");
		getConnectionData(0);

    	idChatWindow = username + clientName + user + client;
		
    	preparedToSend = false;
    	pendingToSend = new ArrayList<String>();
		
		configureChatWindow();
    }
    
    private void configureBroadcastReceiver()
    {
    	notificator = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    	
    	broadcastReceiver = new BroadcastReceiver()
    	{
			@Override
			public void onReceive(Context context, Intent intent)
			{
				String action = intent.getAction();
				if (SWAACService.ERROR_ACTION.equals(action)) finish();
				else if(SWAACService.RECEIVED_MSG_ACTION.equals(action))
				{
					String id = intent.getStringExtra("idChatWindow");
					if(id.equals(idChatWindow))
					{
						String text = intent.getStringExtra("textMessage");
						outputText.add(text);
					}
				}
			}
		};
		intentFilter = new IntentFilter(SWAACService.ERROR_ACTION);
		intentFilter.addAction(SWAACService.RECEIVED_MSG_ACTION);
    }
    
    private void configureChatWindow()
    {
		setContentView(R.layout.swaac_chatwindow);
		
        outputText = new ArrayAdapter<String>(this, R.layout.swaac_chatmessage);
    	ListView viewText = (ListView) findViewById(R.id.chatOutput);
    	viewText.setAdapter(outputText);
        
    	Button sendButton = (Button) findViewById(R.id.chatSendButton);
    	sendButton.setOnClickListener(new OnClickListener()
    	{	
			@Override
			public void onClick(View v)
			{
				sendText();
			}
		});
    	
    	TextView title = (TextView) findViewById(R.id.chatTitle);
    	title.setText(getResources().getString(R.string.chatWindow) + " - " + client);

    	reloadText();
    }
    
    private void getConnectionData(int state)
    {
    	Object[] data;
    	switch (state)
    	{
			case 0:
		    	data = new Object[1];
				data[0] = user + ":" + client;
				sendSockets = new SWAACSendSockets(getBaseContext(), Command.IP_AND_PORT_REQUEST, handler, data);
		    	sendSockets.start();
				break;
			case 1:
		    	data = new Object[1];
				data[0] = user + ":" + client;
				sendSockets = new SWAACSendSockets(getBaseContext(), Command.GET_SEND_TOKEN, handler, data);
		    	sendSockets.start();
		    	break;
			case 2:
				while(pendingToSend.size() > 0)
		    	{
		    		data = new Object[4];
			    	data[0] = token;
			    	data[1] = clientIP;
			    	data[2] = clientPort;
			    	data[3] = pendingToSend.get(0);
					sendSockets = new SWAACSendSockets(getBaseContext(), Command.SEND_TEXT, handler, data);
			    	sendSockets.start();
			    	pendingToSend.remove(0);
		    	}
		    	preparedToSend = true;	
			default:
				break;
		}
    }
    
    
    

    
	//*************************************//
	//***** Chat management functions *****//
	//*************************************//

    private void reloadText()
    {
    	outputText.clear();
		try
		{
	    	FileInputStream fis = openFileInput(idChatWindow);
	    	BufferedInputStream bis = new BufferedInputStream(fis);
	    	DataInputStream dis = new DataInputStream(bis);
	    	while(dis.available() != 0) outputText.add(dis.readLine());
	    	dis.close();
	    	bis.close();
	    	fis.close();
		}
		catch (Exception e) {}
    }
    
    private void sendText()
    {
    	String text = SWAACUtils.getEditText(this, R.id.chatInput);
    	if (text != null && !text.equals(""))
    	{
	    	SWAACUtils.setEditText(this, R.id.chatInput, "");
	    	if (preparedToSend)
	    	{
	        	Object[] data = new Object[4];
	        	data[0] = token;
	        	data[1] = clientIP;
	        	data[2] = clientPort;
	        	data[3] = text;
				sendSockets = new SWAACSendSockets(getBaseContext(), Command.SEND_TEXT, handler, data);
		    	sendSockets.start();
	    	}
	    	else pendingToSend.add(text);
	    	writeText(text);
    	}
    }
    
    private void writeText(String text)
    {
    	outputText.add("I: " + text);
    	try
		{
	    	FileOutputStream fos = openFileOutput(idChatWindow, MODE_APPEND);
	    	BufferedOutputStream bos = new BufferedOutputStream(fos);
	    	DataOutputStream dos = new DataOutputStream(bos);
    		dos.writeBytes(getString(R.string.chatI) + text);
    		dos.writeByte('\n');
	    	dos.close();
	    	bos.close();
	    	fos.close();
		}
		catch (Exception e) {}
    }
    
    private void clearChatHistory()
    {
    	deleteFile(idChatWindow);
    	outputText.clear();
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
            	SWAACUtils.printMessage(SWAACChatWindowActivity.this, "Error: " + b.getString("message"));
            	finish();
            }
            else if (b.getBoolean("ipAndPortRequest"))
            {
            	String[] ipAndPort = b.getStringArray("ipAndPort");
            	clientIP = ipAndPort[0];
            	clientPort = ipAndPort[1];
            	getConnectionData(1);
            }
            else if (b.getBoolean("getSendToken"))
            {
            	token = b.getString("token");
            	getConnectionData(2);
            }
        }
    };
    
}
