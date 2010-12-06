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
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SWAACLoggedInActivity extends Activity
{

	//****************************************************//
	//***** Preferences and configuration attributes *****//
	//****************************************************//
	private SharedPreferences preferences;
	private SharedPreferences.Editor editor;
	private BroadcastReceiver broadcastReceiver;
	private IntentFilter intentFilter;
	
	//*********************************************//
	//***** Connection and sending attributes *****//
	//*********************************************//
	private ProgressDialog progressDialog;
	private SWAACSendSockets sendSockets;
	private String clickedClient;
	private String clickedUser;
	private String[] onlineClients;
	private String sessionID;

	//**********************************************//
	//***** Clients list management attributes *****//
	//**********************************************//
	private ArrayList<String> ownClients;
	private ArrayList<String[]> friendsClients;
	
	//*****************************************************//
	//***** On activity result request code attribute *****//
	//*****************************************************//
	private static final int REQUEST_CODE_PICK_FILE_TO_OPEN = 1;

	
	
	
	
	//***************************************//
	//***** Activity override functions *****//
	//***************************************//

	@Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	
    	configure();
    }
	
	@Override
	protected void onResume()
	{
		super.onResume();
		
		registerReceiver(broadcastReceiver, intentFilter);
		
    	if (!preferences.getBoolean("loggedIn", false)) toMainActivity();
    	else if (preferences.getBoolean("makeRelogin", false)) logout(0);
    	else if (preferences.getBoolean("clientListChanged", false)) updateClientsList(0);
	}
	
	@Override
	protected void onPause()
	{
		super.onPause();
		
		unregisterReceiver(broadcastReceiver);
	}
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.loggedinmenu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
	        case R.id.optionsMenu:
	        	toOptionsActivity();
	            return true;
	        case R.id.friendsListMenu:
	        	toFriendsListActivity();
	        	return true;
	        case R.id.logoutMenu:
	        	logout(0);
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
    	super.onActivityResult(requestCode, resultCode, data);
    	
    	if (resultCode != RESULT_OK || data == null) return ;
		
		Uri uri = data.getData();
		
		if (requestCode == REQUEST_CODE_PICK_FILE_TO_OPEN && uri != null)
		{
			Object[] dat = new Object[3];
			dat[0] = uri.getPath();
			if (clickedUser.equals("")) dat[1] = preferences.getString("username", null);
			else dat[1] = clickedUser;
			dat[2] = clickedClient;
			sendSockets = new SWAACSendSockets(SWAACLoggedInActivity.this, Command.SEND_FILE, handler, dat);
			sendSockets.send();
		}
    }
    
    
    
    
    
	//***********************************//
	//***** Configuration functions *****//
	//***********************************//
    
    private void configure()
    {
    	setContentView(R.layout.swaac_clientslist);
    	
    	preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
    	editor = preferences.edit();
    	configureBroadcastReceiver();
    	
    	reloadClientsList();
    	showClientsList();
    	if (!getIntent().getBooleanExtra("regetClients", true))
    	{
    		onlineClients = getIntent().getStringArrayExtra("onlineClients");
    		updateClientsList(1);
    	}
    	else updateClientsList(0);
    	
    	startService(new Intent().setClass(this, SWAACService.class));
    }

    private void configureBroadcastReceiver()
    {
    	broadcastReceiver = new BroadcastReceiver()
    	{
			@Override
			public void onReceive(Context context, Intent intent)
			{
				String action = intent.getAction();
				if (SWAACService.ERROR_ACTION.equals(action)) toMainActivity();
				else if (SWAACService.CLIENTS_CHANGED_ACTION.equals(action))
				{
					editor.remove("clientListChanged");
					editor.commit();
					updateClientsList(0);
				}
			}
		};
		intentFilter = new IntentFilter(SWAACService.ERROR_ACTION);
		intentFilter.addAction(SWAACService.CLIENTS_CHANGED_ACTION);
    }
    
    
    
    
    
	//*****************************************//
	//***** Activity navigation functions *****//
	//*****************************************//
    
    void toMainActivity()
    {
    	editor.putBoolean("loggedIn", false);
    	editor.remove("sessionID");
    	editor.commit();
    	Intent intent = new Intent().setClass(this, SWAACMainActivity.class);
		startActivity(intent);
		finish();
    }
    
    private void toOptionsActivity()
    {
    	Intent intent = new Intent().setClass(this, SWAACOptionsActivity.class);
		startActivity(intent);
    }
    
    private void toFriendsListActivity()
    {
    	Intent intent = new Intent().setClass(this, SWAACFriendsListActivity.class);
		startActivity(intent);
    }
    
    
    
    
    
	//****************************************//
	//***** Dialogs management functions *****//
	//****************************************//

    private void showClientDialog()
    {
    	final CharSequence[] items = {getString(R.string.clientDialogOpenChat),
    			getString(R.string.clientDialogSendURL),
    			getString(R.string.clientDialogSendFile)};

    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setTitle(R.string.clientListDialogTitle);
    	builder.setItems(items, new DialogInterface.OnClickListener()
    	{
    	    public void onClick(DialogInterface dialog, int item)
    	    {
    	        processClickedDialog(item);
    	    }
    	});
    	AlertDialog alert = builder.create();
    	alert.show();
    }
    
    private void showSendURLDialog()
    {
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setTitle(R.string.sendURLTittle);
    	final EditText inputText = new EditText(this);
    	builder.setView(inputText);
    	builder.setPositiveButton(R.string.sendButton, new DialogInterface.OnClickListener()
    	{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				Object[] data = new Object[3];
				data[0] = inputText.getText().toString();
				if (clickedUser.equals("")) data[1] = preferences.getString("username", null);
				else data[1] = clickedUser;
				data[2] = clickedClient;
				sendSockets = new SWAACSendSockets(SWAACLoggedInActivity.this, Command.SEND_URL, handler, data);
	    		sendSockets.send();
			}
		});
    	AlertDialog alert = builder.create();
    	alert.show();
    }
    
    private void processClickedDialog(int item)
    {
    	if (item == 0)
    	{
        	Intent intent = new Intent().setClass(this, SWAACChatWindowActivity.class);
        	intent.putExtra("clientUser", clickedUser);
        	intent.putExtra("client", clickedClient);
        	startActivity(intent);
    	}
    	else if (item == 1) showSendURLDialog();
    	else
    	{
        	try
        	{
        		Intent intent = new Intent("com.estrongs.action.PICK_FILE");
        		startActivityForResult(intent, REQUEST_CODE_PICK_FILE_TO_OPEN);
    	    }
        	catch (Exception e)
        	{
    			SWAACUtils.printMessage(this, "Error: " + getString(R.string.errorNoEStrongs));
    		}

    	}
    }
    
    
    
    
    
	//*********************************************//
	//***** Clients list management functions *****//
	//*********************************************//
    
    private void showClientsList()
    {
    	LinearLayout clientsList = (LinearLayout) findViewById(R.id.clientsList);
    	LinearLayout friendsClientsList = (LinearLayout) findViewById(R.id.friendsClientsList);
    	clientsList.removeAllViews();
    	friendsClientsList.removeAllViews();
    	for (int i = 0; i < ownClients.size(); i++) addClientToList(ownClients.get(i), null);
    	for (int i = 0; i < friendsClients.size(); i++) addClientToList(friendsClients.get(i)[0], friendsClients.get(i)[1]);
    }
    
    private void saveClientsList()
    {
    	try
    	{
	    	deleteFile("ownClientsList");
			FileOutputStream fos = openFileOutput("ownClientsList", MODE_PRIVATE);
	    	BufferedOutputStream bos = new BufferedOutputStream(fos);
	    	DataOutputStream dos = new DataOutputStream(bos);
	    	for (int i = 0; i < ownClients.size(); i++)
	    	{
	    		dos.writeBytes(ownClients.get(i));
	    		dos.writeByte('\n');
	    	}
	    	dos.close();
	    	bos.close();
	    	fos.close();
	    	
	    	deleteFile("friendsClientsList");
			fos = openFileOutput("friendsClientsList", MODE_PRIVATE);
	    	bos = new BufferedOutputStream(fos);
	    	dos = new DataOutputStream(bos);
	    	for (int i = 0; i < friendsClients.size(); i++)
	    	{
	    		dos.writeBytes(friendsClients.get(i)[0]);
	    		dos.writeByte('\n');
	    		dos.writeBytes(friendsClients.get(i)[1]);
	    		dos.writeByte('\n');
	    	}
	    	dos.close();
	    	bos.close();
	    	fos.close();
    	}
    	catch (Exception e) {}
    }
    
    private void reloadClientsList()
    {
    	try
    	{
	    	ownClients = new ArrayList<String>();
	    	friendsClients = new ArrayList<String[]>();
	
	    	FileInputStream fis = openFileInput("ownClientsList");
	    	BufferedInputStream bis = new BufferedInputStream(fis);
	    	DataInputStream dis = new DataInputStream(bis);
	    	while(dis.available() != 0) ownClients.add(dis.readLine());
	    	dis.close();
	    	bis.close();
	    	fis.close();
	
	    	fis = openFileInput("friendsClientsList");
	    	bis = new BufferedInputStream(fis);
	    	dis = new DataInputStream(bis);
	    	while(dis.available() != 0)
	    	{
	    		String[] aux = new String[2];
	    		aux[0] = dis.readLine();
	    		aux[1] = dis.readLine();
	    		friendsClients.add(aux);
	    	}
	    	dis.close();
	    	bis.close();
	    	fis.close();
    	}
    	catch (Exception e) {}
    }

    private void updateClientsList(int state)
    {
    	switch (state) {
			case 0:
				editor.remove("makeRelogin");
		    	editor.commit();
				sendSockets = new SWAACSendSockets(getBaseContext(), Command.GET_ONLINE_CLIENTS, handler);
		    	sendSockets.start();
				editor.remove("clientListChanged");
				editor.commit();
				break;
			case 1:
				ownClients = new ArrayList<String>();
				friendsClients = new ArrayList<String[]>();
		    	for (int i = 0; i < onlineClients.length; i++)
		    	{
		    		String[] aux = onlineClients[i].split(":");
		    		if (aux.length == 2)
		    		{
		    			String aux2 = aux[0];
		    			aux[0] = aux[1];
		    			aux[1] = aux2;
		    			friendsClients.add(aux);
		    		}
		    		else ownClients.add(aux[0]);
		    	}
		    	showClientsList();
		    	saveClientsList();
				break;
			default:
				break;
		}
    }
    
    private void addClientToList(String clientName, String clientUser)
    {
    	LinearLayout clientsList = (LinearLayout) findViewById(R.id.clientsList);
    	LinearLayout friendsClientsList = (LinearLayout) findViewById(R.id.friendsClientsList);
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        
        View clientsListItem = inflater.inflate(R.layout.swaac_clientslistitem, null);
        TextView clientNameTV = (TextView) clientsListItem.findViewById(R.id.clientsListItemClient);
        clientNameTV.setText(clientName);
        TextView clientUserTV = (TextView) clientsListItem.findViewById(R.id.clientsListItemUser);
        clientUserTV.setText(clientUser);
        
        clientsListItem.setClickable(true);
        clientsListItem.setOnClickListener(new OnClickListener()
        {
			@Override
			public void onClick(View v)
			{
		        TextView clientName = (TextView) v.findViewById(R.id.clientsListItemClient);
		        TextView clientUser = (TextView) v.findViewById(R.id.clientsListItemUser);
		        clickedClient = clientName.getText().toString();
		        clickedUser = clientUser.getText().toString();
		        showClientDialog();
			}
		});
        
        if (clientUser == null) clientsList.addView(clientsListItem);
        else friendsClientsList.addView(clientsListItem);
    }
    
    
    
    
    
	//****************************************//
	//***** Session management functions *****//
	//****************************************//
    
    private void relogin(int state)
    {
    	switch (state)
    	{
			case 0:
		    	String username = preferences.getString("username", null);
		    	String password = preferences.getString("password", null);
		    	Object[] data = new Object[2];
		    	data[0] = username;
		    	data[1] = password;
		    	sendSockets = new SWAACSendSockets(getBaseContext(), Command.LOGIN, handler, data);
		    	sendSockets.send();
				break;
			case 1:
		    	startService(new Intent().setClass(this, SWAACService.class));
				editor.putString("sessionID", sessionID);
		    	editor.commit();
		    	updateClientsList(0);
				break;
			default:
				break;
		}
    }
    
    private void logout(int state)
    {
    	switch (state)
    	{
			case 0:
		        if (!preferences.getBoolean("makeRelogin", false))
		        	progressDialog = ProgressDialog.show(this, "", getResources().getString(R.string.loggingOutMessage), true);
		    	stopService(new Intent().setClass(this, SWAACService.class));
		    	sendSockets = new SWAACSendSockets(getBaseContext(), Command.LOGOUT, handler);
		    	sendSockets.start();
				break;
			case 1:
				if (preferences.getBoolean("makeRelogin", false)) relogin(0);
		    	else
		    	{
		    		progressDialog.dismiss();
		        	editor.putBoolean("loggedOut", true);
		        	editor.remove("username");
		        	editor.remove("password");
		        	editor.commit();
		    		toMainActivity();
		    	}
				break;
			default:
				break;
		}
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
            	SWAACUtils.printMessage(SWAACLoggedInActivity.this, "Error: " + b.getString("message"));
            	if (b.getString("message").equals("Server error")) toMainActivity();
            }
            else if (b.getBoolean("login"))
            {
            	sessionID = b.getString("sessionID");
            	relogin(1);
            }
            else if (b.getBoolean("logout")) logout(1);
            else if (b.getBoolean("getOnlineClients"))
            {
            	onlineClients = b.getStringArray("onlineClients");
            	updateClientsList(1);
            }
        }
    };
    
}
