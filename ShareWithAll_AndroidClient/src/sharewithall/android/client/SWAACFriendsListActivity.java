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
import sharewithall.android.client.sockets.SWAACSendSockets.Property;
import android.app.Activity;
import android.app.AlertDialog;
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

	private SWAACSendSockets sendSockets;
	private BroadcastReceiver broadcastReceiver;
	private IntentFilter intentFilter;
	private SharedPreferences preferences;
	private SharedPreferences.Editor editor;
	private ArrayList<String> acceptedFriends;
	private ArrayList<String> invitedFriends;
	private ArrayList<String> expectingFriends;
	private ArrayList<String> blockedFriends;
	private String clickedFriend;
	private String clickedProperty;
	private boolean toUpdateList;
	
    private void printMessage(String message)
    {
    	Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
    
    private void toOptionsActivity()
    {
    	Intent intent = new Intent().setClass(this, SWAACOptionsActivity.class);
		startActivity(intent);
    }

    private void acceptFriend()
    {
    	Object[] data = new Object[1];
    	data[0] = clickedFriend;
    	sendSockets = new SWAACSendSockets(this, Command.DECLARE_FRIEND, handler, data);
		sendSockets.send();
		
		if (clickedProperty.equals("Invitation received"))
			expectingFriends.remove(expectingFriends.indexOf(clickedFriend));
		else if (clickedProperty.equals("Blocked"))
			blockedFriends.remove(blockedFriends.indexOf(clickedFriend));
		
		int i;
		for (i = 0; i < acceptedFriends.size() && acceptedFriends.get(i).compareTo(clickedFriend) < 0; i++);
		acceptedFriends.add(i, clickedFriend);
    }
    
    private void blockFriend()
    {
    	Object[] data = new Object[1];
    	data[0] = clickedFriend;
    	sendSockets = new SWAACSendSockets(this, Command.IGNORE_USER, handler, data);
		sendSockets.send();
		
		if (clickedProperty.equals("Accepted"))
			acceptedFriends.remove(acceptedFriends.indexOf(clickedFriend));
		else if (clickedProperty.equals("Invitation sent"))
			invitedFriends.remove(invitedFriends.indexOf(clickedFriend));
		else if (clickedProperty.equals("Invitation received"))
			expectingFriends.remove(expectingFriends.indexOf(clickedFriend));
		
		int i;
		for (i = 0; i < blockedFriends.size() && blockedFriends.get(i).compareTo(clickedFriend) < 0; i++);
		blockedFriends.add(i, clickedFriend);
    }
    
    private void processClickedDialog(int item)
    {
    	Object[] data = new Object[1];
    	data[0] = clickedFriend;
    	
    	toUpdateList = false;
    	if (clickedProperty.equals("Invitation received"))
    	{
    		if (item == 0) acceptFriend();
    		else if (item == 1) blockFriend();
    	}
    	else if (clickedProperty.equals("Invitation sent") && item == 0) blockFriend();
    	else if (clickedProperty.equals("Accepted") && item == 0) blockFriend();
    	else if (clickedProperty.equals("Blocked") && item == 0) acceptFriend();
    	showFriendsList();
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
				toUpdateList = true;
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
    
    private void saveFriendsList()
    {
    	String ACCEPTED_FILE = "acceptedFriendsList";
    	String INVITED_FILE = "invitedFriendsList";
    	String EXPECTING_FILE = "expectingFriendsList";
    	String BLOCKED_FILE = "blockedFriendsList";
		try
		{
			deleteFile(ACCEPTED_FILE);
			FileOutputStream fos = openFileOutput(ACCEPTED_FILE, MODE_PRIVATE);
	    	BufferedOutputStream bos = new BufferedOutputStream(fos);
	    	DataOutputStream dos = new DataOutputStream(bos);
	    	for (int i = 0; i < acceptedFriends.size(); i++)
	    	{
	    		dos.writeBytes(acceptedFriends.get(i));
	    		dos.writeByte('\n');
	    	}
	    	dos.close();
	    	bos.close();
	    	fos.close();

			deleteFile(INVITED_FILE);
			fos = openFileOutput(INVITED_FILE, MODE_PRIVATE);
	    	bos = new BufferedOutputStream(fos);
	    	dos = new DataOutputStream(bos);
	    	for (int i = 0; i < invitedFriends.size(); i++)
	    	{
	    		dos.writeBytes(invitedFriends.get(i));
	    		dos.writeByte('\n');
	    	}
	    	dos.close();
	    	bos.close();
	    	fos.close();

			deleteFile(EXPECTING_FILE);
			fos = openFileOutput(EXPECTING_FILE, MODE_PRIVATE);
	    	bos = new BufferedOutputStream(fos);
	    	dos = new DataOutputStream(bos);
	    	for (int i = 0; i < expectingFriends.size(); i++)
	    	{
	    		dos.writeBytes(expectingFriends.get(i));
	    		dos.writeByte('\n');
	    	}
	    	dos.close();
	    	bos.close();
	    	fos.close();

			deleteFile(BLOCKED_FILE);
			fos = openFileOutput(BLOCKED_FILE, MODE_PRIVATE);
	    	bos = new BufferedOutputStream(fos);
	    	dos = new DataOutputStream(bos);
	    	for (int i = 0; i < blockedFriends.size(); i++)
	    	{
	    		dos.writeBytes(blockedFriends.get(i));
	    		dos.writeByte('\n');
	    	}
	    	dos.close();
	    	bos.close();
	    	fos.close();
		}
		catch (Exception e) {}
    }
    
    private void reloadLastFriendsList()
    {
    	String ACCEPTED_FILE = "acceptedFriendsList";
    	String INVITED_FILE = "invitedFriendsList";
    	String EXPECTING_FILE = "expectingFriendsList";
    	String BLOCKED_FILE = "blockedFriendsList";
    	acceptedFriends = new ArrayList<String>();
    	invitedFriends = new ArrayList<String>();
    	expectingFriends = new ArrayList<String>();
    	blockedFriends = new ArrayList<String>();
		try
		{
			FileInputStream fis = openFileInput(ACCEPTED_FILE);
	    	BufferedInputStream bis = new BufferedInputStream(fis);
	    	DataInputStream dis = new DataInputStream(bis);
	    	while(dis.available() != 0) acceptedFriends.add(dis.readLine());
	    	dis.close();
	    	bis.close();
	    	fis.close();

	    	fis = openFileInput(INVITED_FILE);
	    	bis = new BufferedInputStream(fis);
	    	dis = new DataInputStream(bis);
	    	while(dis.available() != 0) invitedFriends.add(dis.readLine());
	    	dis.close();
	    	bis.close();
	    	fis.close();
	    	
	    	fis = openFileInput(EXPECTING_FILE);
	    	bis = new BufferedInputStream(fis);
	    	dis = new DataInputStream(bis);
	    	while(dis.available() != 0) expectingFriends.add(dis.readLine());
	    	dis.close();
	    	bis.close();
	    	fis.close();
	    	
	    	fis = openFileInput(BLOCKED_FILE);
	    	bis = new BufferedInputStream(fis);
	    	dis = new DataInputStream(bis);
	    	while(dis.available() != 0) blockedFriends.add(dis.readLine());
	    	dis.close();
	    	bis.close();
	    	fis.close();
	    	
	    	showFriendsList();
		}
		catch (Exception e) {}
    }
    
    private void showFriendsList()
    {
    	LinearLayout friendsListView = (LinearLayout) findViewById(R.id.friendsList);
    	friendsListView.removeAllViews();

    	for (int i = 0; i < acceptedFriends.size(); i++)
    		addFriendToList(acceptedFriends.get(i), Property.FRIENDS);
    	for (int i = 0; i < invitedFriends.size(); i++)
    		addFriendToList(invitedFriends.get(i), Property.DECLARED);
    	for (int i = 0; i < expectingFriends.size(); i++)
    		addFriendToList(expectingFriends.get(i), Property.EXPECTING);
    	for (int i = 0; i < blockedFriends.size(); i++)
    		addFriendToList(blockedFriends.get(i), Property.IGNORED);
    }
    
    private void updateFriendsList()
    {
    	sendSockets = new SWAACSendSockets(getBaseContext(), SWAACSendSockets.Command.GET_LIST_OF_FRIENDS, handler);
    	sendSockets.start();

		editor.remove("friendsListChanged");
		editor.commit();
    }
    
    private void updatedFriendsList(String[] friendsList)
    {
    	LinearLayout friendsListView = (LinearLayout) findViewById(R.id.friendsList);
    	friendsListView.removeAllViews();
    	
    	acceptedFriends.clear();
    	invitedFriends.clear();
    	expectingFriends.clear();
    	blockedFriends.clear();
    	
    	for (int i = 0; i < friendsList.length - 1; i+=2)
    	{
    		Property property = Property.valueOf(friendsList[i + 1]);
    		addFriendToList(friendsList[i], property);
    		switch (property)
    		{
				case FRIENDS:
					acceptedFriends.add(friendsList[i]);
					break;
				case DECLARED:
					invitedFriends.add(friendsList[i]);
					break;
				case EXPECTING:
					expectingFriends.add(friendsList[i]);
					break;
				case IGNORED:
					blockedFriends.add(friendsList[i]);
					break;
				default:
					break;
			}
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
    	preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
    	editor = preferences.edit();
    	configureBroadcastReceiver();
    	reloadLastFriendsList();
		updateFriendsList();
	}
	
	@Override
	protected void onDestroy()
	{
		saveFriendsList();
		super.onDestroy();
	}
	
	@Override
	protected void onResume()
	{
		super.onResume();
		registerReceiver(broadcastReceiver, intentFilter);
    	if (!preferences.getBoolean("loggedIn", false)) finish();
    	else if (preferences.getBoolean("friendsListChanged", false)) updateFriendsList();
	}
	
	@Override
	protected void onPause()
	{
		unregisterReceiver(broadcastReceiver);
		super.onPause();
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
            if (msg.getData().getBoolean("exception"))
            {
            	printMessage("Error: " + msg.getData().getString("message"));
            	if (!msg.getData().getString("message").equals("Friend doesn't exist") &&
            			!msg.getData().getString("message").equals("Relation already declared."))
            		finish();
            }
            else if (msg.getData().getBoolean("getListOfFriends"))
            	updatedFriendsList(msg.getData().getStringArray("friends"));
            else if (toUpdateList && (msg.getData().getBoolean("declareFriend") ||
            		msg.getData().getBoolean("ignoreFriend")))
            {
            	editor.putBoolean("clientListChanged", true);
            	editor.commit();
            	updateFriendsList();
            	toUpdateList = false;
            }
        }
    };
}
