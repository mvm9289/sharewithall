package sharewithall.android.client;

import sharewithall.android.client.sockets.SWAACSendSockets;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
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
	
	ProgressDialog progressDialog;
	SWAACSendSockets sendSockets;

    private void printMessage(String message)
    {
    	Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
    
    private void toMainActivity()
    {
    	SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
    	SharedPreferences.Editor editor = preferences.edit();
    	editor.putBoolean("loggedIn", false);
    	editor.putBoolean("loggedOut", true);
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
    
    private void logout()
    {
		progressDialog = ProgressDialog.show(this, "", getResources().getString(R.string.loggingOutMessage), true);
    	sendSockets = new SWAACSendSockets(getBaseContext(), SWAACSendSockets.Command.LOGOUT, handler, null);
    	sendSockets.start();
    }
    
    private void loggedOut()
    {
    	stopService(new Intent().setClass(this, SWAACService.class));
    	toMainActivity();
    }
    
    private void addClientToList(String clientName, String clientUser)
    {
    	LinearLayout clientsList = (LinearLayout) findViewById(R.id.clientsList);
    	LinearLayout friendsClientsList = (LinearLayout) findViewById(R.id.friendsClientsList);
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        
        View clientsListItem = inflater.inflate(R.layout.swaac_clientslistitem, null);
        TextView tv1 = (TextView) clientsListItem.findViewById(R.id.clientsListItemClient);
        tv1.setText(clientName);
        TextView tv2 = (TextView) clientsListItem.findViewById(R.id.clientsListItemUser);
        tv2.setText(clientUser);
        
        clientsListItem.setClickable(true);
        clientsListItem.setOnClickListener(new OnClickListener()
        {
			@Override
			public void onClick(View v)
			{
		        TextView clientName = (TextView) v.findViewById(R.id.clientsListItemClient);
		        TextView clientUser = (TextView) v.findViewById(R.id.clientsListItemUser);
		        printMessage(clientName.getText().toString() + " " + clientUser.getText().toString());
			}
		});
        
        if (clientUser == null) clientsList.addView(clientsListItem);
        else friendsClientsList.addView(clientsListItem);
    }
    
    private void updateClientsList()
    {
		progressDialog = ProgressDialog.show(this, "", getResources().getString(R.string.gettingClientsList), true);
    	sendSockets = new SWAACSendSockets(getBaseContext(), SWAACSendSockets.Command.GET_ONLINE_CLIENTS, handler, null);
    	sendSockets.start();
    }
    
    private void updatedClientsList(String[] onlineClients)
    {
    	for (int i = 0; i < onlineClients.length; i++)
    	{
    		String[] aux = onlineClients[i].split("-");
    		if (aux.length == 2) addClientToList(aux[0], aux[1]);
    		else addClientToList(aux[0], null);
    	}
    }
    
    
    
	@Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.swaac_clientslist);

    	addClientToList("client1", "user1");
    	addClientToList("client2", null);
    	addClientToList("client1", "user1");
    	addClientToList("client2", null);
    	addClientToList("client1", "user1");
    	addClientToList("client2", null);
    	addClientToList("client1", "user1");
    	addClientToList("client2", null);
    	addClientToList("client1", "user1");
    	addClientToList("client2", null);
    	addClientToList("client1", "user1");
    	addClientToList("client2", null);
    	addClientToList("client1", "user1");
    	addClientToList("client2", null);
    	
    	//updateClientsList();
    	
    	//startService(new Intent().setClass(this, SWAACService.class));
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
    
    
    
    final Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            progressDialog.dismiss();
            if (msg.getData().getBoolean("exception"))
            {
            	printMessage("Error: " + msg.getData().getString("message") + ". Please make re-login.");
            	toMainActivity();
            }
            else if (msg.getData().getBoolean("logout"))
            	loggedOut();
            else if (msg.getData().getBoolean("getOnlineClients"))
            	updatedClientsList(msg.getData().getStringArray("onlineClients"));
        }
    };
    
}
