package sharewithall.android.client;

import sharewithall.android.client.sockets.SWAACSendSockets;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class SWAACLoggedInActivity extends Activity
{
	
	private ProgressDialog progressDialog;
	private SWAACSendSockets sendSockets;
	private BroadcastReceiver broadcastReceiver;
	private IntentFilter intentFilter;
	private SharedPreferences preferences;
	private SharedPreferences.Editor editor;
	private String clickedClient;
	private String clickedUser;
	

    private void printMessage(String message)
    {
    	Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
    
    private void toMainActivity()
    {
    	stopService(new Intent().setClass(this, SWAACService.class));
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
    
    private void makeRelogin()
    {
    	String username = preferences.getString("username", null);
    	String password = preferences.getString("password", null);
    	Object[] data = new Object[2];
    	data[0] = username;
    	data[1] = password;
    	sendSockets = new SWAACSendSockets(getBaseContext(), SWAACSendSockets.Command.LOGIN, handler, data);
    	sendSockets.send();
    }
    
    private void relogged(String sessionID)
    {
    	editor.putString("sessionID", sessionID);
    	editor.commit();
    	updateClientsList();
    }
    
    private void logout()
    {
        if (!preferences.getBoolean("makeRelogin", false))
        	progressDialog = ProgressDialog.show(this, "", getResources().getString(R.string.loggingOutMessage), true);
    	sendSockets = new SWAACSendSockets(getBaseContext(), SWAACSendSockets.Command.LOGOUT, handler);
    	sendSockets.start();
    }
    
    private void loggedOut()
    {
    	if (preferences.getBoolean("makeRelogin", false)) makeRelogin();
    	else
    	{
    		progressDialog.dismiss();
        	editor.putBoolean("loggedOut", true);
        	editor.remove("username");
        	editor.remove("password");
        	editor.commit();
    		toMainActivity();
    	}
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
    	else printMessage(item + ": " + clickedClient + " " + clickedUser);
    }
    
    private void showClientDialog()
    {
    	final CharSequence[] items = {"Open chat window", "Send an URL", "Send a file"};

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
    
    private void updateClientsList()
    {
		editor.remove("makeRelogin");
    	editor.commit();
    	
		sendSockets = new SWAACSendSockets(getBaseContext(), SWAACSendSockets.Command.GET_ONLINE_CLIENTS, handler);
    	sendSockets.start();

		editor.remove("clientListChanged");
		editor.commit();
    }
    
    private void updatedClientsList(String[] onlineClients)
    {
    	LinearLayout clientsList = (LinearLayout) findViewById(R.id.clientsList);
    	LinearLayout friendsClientsList = (LinearLayout) findViewById(R.id.friendsClientsList);
    	clientsList.removeAllViews();
    	friendsClientsList.removeAllViews();
    	
    	for (int i = 0; i < onlineClients.length; i++)
    	{
    		String[] aux = onlineClients[i].split(":");
    		if (aux.length == 2) addClientToList(aux[1], aux[0]);
    		else addClientToList(aux[0], null);
    	}
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
			}
		};
		intentFilter = new IntentFilter(SWAACService.ERROR_ACTION);
    }
    
    
    
	@Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.swaac_clientslist);
    	preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
    	editor = preferences.edit();
    	configureBroadcastReceiver();
    	if (!getIntent().getBooleanExtra("regetClients", true))
    		updatedClientsList(getIntent().getStringArrayExtra("onlineClients"));
    	else updateClientsList();
    	startService(new Intent().setClass(this, SWAACService.class));
    }
	
	@Override
	protected void onResume()
	{
		super.onResume();
		
		registerReceiver(broadcastReceiver, intentFilter);
		
    	if (!preferences.getBoolean("loggedIn", false)) toMainActivity();
    	else if (preferences.getBoolean("makeRelogin", false)) logout();
    	else if (preferences.getBoolean("clientListChanged", false)) updateClientsList();
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
	        	logout();
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
        }
    }
    
    
    
    final Handler handler = new Handler()
    {
        public void handleMessage(Message msg)
        {
            if (msg.getData().getBoolean("exception"))
            {
            	printMessage("Error: " + msg.getData().getString("message"));
            	if (msg.getData().getString("message").equals("Server error")) toMainActivity();
            }
            else if (msg.getData().getBoolean("login"))
            	relogged(msg.getData().getString("sessionID"));
            else if (msg.getData().getBoolean("logout"))
            	loggedOut();
            else if (msg.getData().getBoolean("getOnlineClients"))
            	updatedClientsList(msg.getData().getStringArray("onlineClients"));
        }
    };
    
}
