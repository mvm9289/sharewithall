package sharewithall.android.client;

import sharewithall.android.client.R;
import sharewithall.android.client.sockets.SWAACSendSockets;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class SWAACFriendsListActivity extends Activity
{

    private void printMessage(String message)
    {
    	Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
    
    private void addFriendToList(String friendName, int property)
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
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.swaac_friendslist);
	}

}
