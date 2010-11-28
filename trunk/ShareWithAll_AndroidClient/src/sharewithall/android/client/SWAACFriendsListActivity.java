package sharewithall.android.client;

import sharewithall.android.client.sockets.SWAACSendSockets;
import sharewithall.android.client.sockets.SWAACSendSockets.Command;
import sharewithall.android.client.sockets.SWAACSendSockets.Property;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class SWAACFriendsListActivity extends Activity
{

	private ProgressDialog progressDialog;
	private SWAACSendSockets sendSockets;
	private BroadcastReceiver broadcastReceiver;
	private IntentFilter intentFilter;
	private String clickedFriend;
	private String clickedProperty;
	
    private void printMessage(String message)
    {
    	Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
    
    private void toOptionsActivity()
    {
    	Intent intent = new Intent().setClass(this, SWAACOptionsActivity.class);
		startActivity(intent);
    }
    
    private void processClickedDialog(int item)
    {
    	Object[] data = new Object[1];
    	data[0] = clickedFriend;
    	
    	if (clickedProperty.equals("Invitation received"))
    	{
    		if (item == 0)
    		{
    			sendSockets = new SWAACSendSockets(this, Command.DECLARE_FRIEND, handler, data);
    			sendSockets.send();
    		}
    		else if (item == 1)
    		{
    			sendSockets = new SWAACSendSockets(this, Command.IGNORE_USER, handler, data);
    			sendSockets.send();
    		}
    	}
    	else if (clickedProperty.equals("Invitation sent") && item == 0)
    	{
    		sendSockets = new SWAACSendSockets(this, Command.IGNORE_USER, handler, data);
    		sendSockets.send();
    	}
    	else if (clickedProperty.equals("Accepted") && item == 0)
    	{
    		sendSockets = new SWAACSendSockets(this, Command.IGNORE_USER, handler, data);
    		sendSockets.send();
    	}
    	else if (clickedProperty.equals("Blocked") && item == 0)
    	{
    		sendSockets = new SWAACSendSockets(this, Command.DECLARE_FRIEND, handler, data);
    		sendSockets.send();
    	}
    }
    
    private void showAddFriendDialog()
    {
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setTitle(R.string.addFriendDialogTitle);
    	final EditText inputText = new EditText(this);
    	builder.setView(inputText);
    	builder.setPositiveButton(R.string.addFriendButton, new DialogInterface.OnClickListener()
    	{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				Object[] data = new Object[1];
				data[0] = inputText.getText().toString();
				sendSockets = new SWAACSendSockets(SWAACFriendsListActivity.this, Command.DECLARE_FRIEND, handler, data);
	    		sendSockets.send();
			}
		});
    	AlertDialog alert = builder.create();
    	alert.show();
    }
    
    private void showFriendDialog()
    {
    	CharSequence[] items = null;
    	if (clickedProperty.equals("Invitation received"))
    		items = new CharSequence[]{"Accept invitation", "Ignore invitation"};
    	else if (clickedProperty.equals("Invitation sent"))
    		items = new CharSequence[]{"Cancel invitation"};
    	else if (clickedProperty.equals("Accepted"))
    		items = new CharSequence[]{"Block friend"};
    	else items = new CharSequence[]{"Unblock friend"};

    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setTitle(R.string.friendsListDialogTitle);
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
		        clickedFriend = friendName.getText().toString();
		        TextView property = (TextView) v.findViewById(R.id.friendsListItemStatus);
		        clickedProperty = property.getText().toString();
		        showFriendDialog();
			}
		});
        
        friendsList.addView(friendsListItem);
    }
    
    private void updateFriendsList()
    {
    	LinearLayout friendsList = (LinearLayout) findViewById(R.id.friendsList);
    	friendsList.removeAllViews();

    	SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
    	Object[] data = new Object[3];
    	data[0] = preferences.getBoolean("showSendedInvPref", true);
    	data[1] = preferences.getBoolean("showReceivedInvPref", true);
    	data[2] = preferences.getBoolean("showBlockedPref", true);
		progressDialog = ProgressDialog.show(this, "", getResources().getString(R.string.gettingFriendsList), true);
    	sendSockets = new SWAACSendSockets(getBaseContext(), SWAACSendSockets.Command.GET_LIST_OF_FRIENDS, handler, data);
    	sendSockets.start();

		SharedPreferences.Editor editor = preferences.edit();
		editor.remove("friendsListChanged");
		editor.commit();
    }
    
    private void updatedFriendsList(String[] friends)
    {
    	for (int i = 0; i < friends.length; i++)
    	{
    		String[] aux = friends[i].split(";");
    		if (aux[1].equals("declared")) addFriendToList(aux[0], Property.DECLARED);
    		else if (aux[1].equals("expecting")) addFriendToList(aux[0], Property.EXPECTING);
    		else if (aux[1].equals("ignored")) addFriendToList(aux[0], Property.IGNORED);
    		else if (aux[1].equals("accepted")) addFriendToList(aux[0], Property.FRIENDS);
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
				if (SWAACService.ERROR_ACTION.equals(action)) finish();
			}
		};
		intentFilter = new IntentFilter(SWAACService.ERROR_ACTION);
    }
    
    
    
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.swaac_friendslist);

    	configureBroadcastReceiver();
		updateFriendsList();
	}
	
	@Override
	protected void onResume()
	{
		super.onResume();
		
		registerReceiver(broadcastReceiver, intentFilter);
		
    	SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
    	if (!preferences.getBoolean("loggedIn", false)) finish();
    	else if (preferences.getBoolean("friendsListChanged", false)) updateFriendsList();
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
        inflater.inflate(R.menu.friendslistmenu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
	        case R.id.addFriendMenu:
	        	showAddFriendDialog();
	            return true;
	        case R.id.optionsMenu:
	        	toOptionsActivity();
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
            	printMessage("Error: " + msg.getData().getString("message"));
            	if (!msg.getData().getString("message").equals("Friend doesn't exist") &&
            			!msg.getData().getString("message").equals("Relation already declared.")) finish();
            }
            else if (msg.getData().getBoolean("getListOfFriends"))
            	updatedFriendsList(msg.getData().getStringArray("friends"));
            else if (msg.getData().getBoolean("declareFriend") || msg.getData().getBoolean("ignoreFriend"))
            {
            	SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
            	SharedPreferences.Editor editor = preferences.edit();
            	editor.putBoolean("clientListChanged", true);
            	editor.commit();
            	updateFriendsList();
            }
        }
    };
}
