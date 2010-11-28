package sharewithall.android.client;

import sharewithall.android.client.sockets.SWAACSendSockets;
import sharewithall.android.client.sockets.SWAACSendSockets.Property;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class SWAACFriendsListActivity extends Activity
{

	private ProgressDialog progressDialog;
	private SWAACSendSockets sendSockets;
	
    private void printMessage(String message)
    {
    	Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
    
    private void addFriendToList(String friendName, SWAACSendSockets.Property property)
    {
    	LinearLayout friendsList = (LinearLayout) findViewById(R.id.friendsList);
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        
        View friendsListItem = inflater.inflate(R.layout.swaac_friendslistitem, null);
        TextView friendNameTV = (TextView) friendsListItem.findViewById(R.id.friendsListItemFriend);
        friendNameTV.setText(friendName);
        TextView friendStatus = (TextView) friendsListItem.findViewById(R.id.friendsListItemStatus);
        ImageView icon = (ImageView) friendsListItem.findViewById(R.id.friendsListItemIcon);
        switch (property)
        {
			case DECLARED:
				icon.setImageResource(R.drawable.listiconwaiting);
				friendStatus.setText("Invitation sent");
				break;
			case EXPECTING:
				icon.setImageResource(R.drawable.listiconpending);
				friendStatus.setText("Invitation received");
				break;
			case FRIENDS:
				icon.setImageResource(R.drawable.listiconaccepted);
				friendStatus.setText("Accepted");
				break;
			case IGNORED:
				icon.setImageResource(R.drawable.listiconblocked);
				friendStatus.setText("Blocked");
				break;
			default:
				break;
		}
        
        friendsListItem.setClickable(true);
        friendsListItem.setOnClickListener(new OnClickListener()
        {
			@Override
			public void onClick(View v)
			{
		        TextView friendName = (TextView) v.findViewById(R.id.friendsListItemFriend);
		        printMessage(friendName.getText().toString());
			}
		});
        
        friendsList.addView(friendsListItem);
    }
    
    private void updateFriendsList()
    {
		progressDialog = ProgressDialog.show(this, "", getResources().getString(R.string.gettingFriendsList), true);
    	sendSockets = new SWAACSendSockets(getBaseContext(), SWAACSendSockets.Command.GET_LIST_OF_FRIENDS, handler);
    	sendSockets.start();
    }
    
    private void updatedFriendsList(String[] friends)
    {
    	for (int i = 0; i < friends.length; i++)
    	{
    		String[] aux = friends[i].split(";");
    		if (aux[1].equals("declared")) addFriendToList(friends[i], Property.DECLARED);
    		else if (aux[1].equals("expecting")) addFriendToList(friends[i], Property.EXPECTING);
    		else if (aux[1].equals("ignored")) addFriendToList(friends[i], Property.IGNORED);
    		else if (aux[1].equals("accepted")) addFriendToList(friends[i], Property.FRIENDS);
    	}
    }
    
    
    
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.swaac_friendslist);
		
		updateFriendsList();
	}

	
	
    final Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            progressDialog.dismiss();
            if (msg.getData().getBoolean("exception"))
            {
            	printMessage("Error: " + msg.getData().getString("message"));
            	finish();
            }
            else if (msg.getData().getBoolean("getListOfFriends"))
            	updatedFriendsList(msg.getData().getStringArray("friends"));
        }
    };
}
