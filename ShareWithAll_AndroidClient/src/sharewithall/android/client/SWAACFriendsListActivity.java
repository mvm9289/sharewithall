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

public class SWAACFriendsListActivity extends Activity
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
	private SWAACSendSockets sendSockets;
	private String clickedFriend;
	private String clickedProperty;
	private boolean toUpdateList;
	private String[] friendsList;

	//**********************************************//
	//***** Friends list management attributes *****//
	//**********************************************//
	private ArrayList<String> acceptedFriends;
	private ArrayList<String> invitedFriends;
	private ArrayList<String> expectingFriends;
	private ArrayList<String> blockedFriends;
	
    
	
	

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
    	else if (preferences.getBoolean("friendsListChanged", false)) updateFriendsList(0);
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
    
    
    
    
    
	//***********************************//
	//***** Configuration functions *****//
	//***********************************//
    
    private void configure()
    {
		setContentView(R.layout.swaac_friendslist);
		
    	preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
    	editor = preferences.edit();
    	configureBroadcastReceiver();
    	
    	reloadLastFriendsList();
    	
		updateFriendsList(0);
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
				else if (SWAACService.FRIENDS_CHANGED_ACTION.equals(action))
				{
					editor.remove("friendsListChanged");
					editor.commit();
					updateFriendsList(0);
				}
			}
		};
		intentFilter = new IntentFilter(SWAACService.ERROR_ACTION);
		intentFilter.addAction(SWAACService.FRIENDS_CHANGED_ACTION);
    }
    
    
    
    
    
	//*****************************************//
	//***** Activity navigation functions *****//
	//*****************************************//
    
    private void toOptionsActivity()
    {
    	Intent intent = new Intent().setClass(this, SWAACOptionsActivity.class);
		startActivity(intent);
    }
    
    
    
    
    
	//****************************************//
	//***** Dialogs management functions *****//
	//****************************************//

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
    	if (clickedProperty.equals(getString(R.string.friendInvitationReceived)))
    		items = new CharSequence[]{getString(R.string.friendDialogAccept), getString(R.string.friendDialogIgnore)};
    	else if (clickedProperty.equals(getString(R.string.friendInvitationSent)))
    		items = new CharSequence[]{getString(R.string.friendDialogCancel)};
    	else if (clickedProperty.equals(getString(R.string.friendAccepted)))
    		items = new CharSequence[]{getString(R.string.friendDialogBlock)};
    	else items = new CharSequence[]{getString(R.string.friendDialogUnblock)};

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
    
    private void processClickedDialog(int item)
    {
    	toUpdateList = true;
    	editor.putBoolean("clientListChanged", true);
    	editor.commit();
    	if (clickedProperty.equals(getString(R.string.friendInvitationReceived)))
    	{
    		if (item == 0) acceptFriend();
    		else if (item == 1) blockFriend();
    	}
    	else if (clickedProperty.equals(getString(R.string.friendInvitationSent)) && item == 0) blockFriend();
    	else if (clickedProperty.equals(getString(R.string.friendAccepted)) && item == 0) blockFriend();
    	else if (clickedProperty.equals(getString(R.string.friendBlocked)) && item == 0) acceptFriend();
    	showFriendsList();
    }
    
    
    
    
    
	//*********************************************//
	//***** Friends list management functions *****//
	//*********************************************//
    
    private void updateFriendsList(int state)
    {
    	switch (state)
    	{
			case 0:
				sendSockets = new SWAACSendSockets(getBaseContext(), Command.GET_LIST_OF_FRIENDS, handler);
		    	sendSockets.start();
				editor.remove("friendsListChanged");
				editor.commit();
				break;
			case 1:
		    	acceptedFriends.clear();
		    	invitedFriends.clear();
		    	expectingFriends.clear();
		    	blockedFriends.clear();
		    	for (int i = 0; i < friendsList.length - 1; i+=2)
		    	{
		    		Property property = Property.valueOf(friendsList[i + 1]);
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
		    	showFriendsList();
				break;
			default:
				break;
		}
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
    
    private void acceptFriend()
    {
    	Object[] data = new Object[1];
    	data[0] = clickedFriend;
    	sendSockets = new SWAACSendSockets(this, Command.DECLARE_FRIEND, handler, data);
		sendSockets.send();
		
		if (clickedProperty.equals(getString(R.string.friendInvitationReceived)))
			expectingFriends.remove(expectingFriends.indexOf(clickedFriend));
		else if (clickedProperty.equals(getString(R.string.friendBlocked)))
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
		
		if (clickedProperty.equals(getString(R.string.friendAccepted)))
			acceptedFriends.remove(acceptedFriends.indexOf(clickedFriend));
		else if (clickedProperty.equals(getString(R.string.friendInvitationSent)))
			invitedFriends.remove(invitedFriends.indexOf(clickedFriend));
		else if (clickedProperty.equals(getString(R.string.friendInvitationReceived)))
			expectingFriends.remove(expectingFriends.indexOf(clickedFriend));
		
		int i;
		for (i = 0; i < blockedFriends.size() && blockedFriends.get(i).compareTo(clickedFriend) < 0; i++);
		blockedFriends.add(i, clickedFriend);
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
				friendStatus.setText(getString(R.string.friendInvitationSent));
				break;
			case EXPECTING:
				icon.setImageResource(R.drawable.listiconpending);
				friendStatus.setText(getString(R.string.friendInvitationReceived));
				break;
			case FRIENDS:
				icon.setImageResource(R.drawable.listiconaccepted);
				friendStatus.setText(getString(R.string.friendAccepted));
				break;
			case IGNORED:
				icon.setImageResource(R.drawable.listiconblocked);
				friendStatus.setText(getString(R.string.friendBlocked));
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
    	String[] files = {"acceptedFriendsList", "invitedFriendsList", "expectingFriendsList", "blockedFriendsList"};
    	ArrayList<ArrayList<String>> lists = new ArrayList<ArrayList<String>>();
    	lists.add(acceptedFriends);
    	lists.add(invitedFriends);
    	lists.add(expectingFriends);
    	lists.add(blockedFriends);
		try
		{
			for (int i = 0; i < lists.size(); i++)
			{
				deleteFile(files[i]);
				FileOutputStream fos = openFileOutput(files[i], MODE_PRIVATE);
		    	BufferedOutputStream bos = new BufferedOutputStream(fos);
		    	DataOutputStream dos = new DataOutputStream(bos);
		    	for (int j = 0; j < lists.get(i).size(); j++)
		    	{
		    		dos.writeBytes(lists.get(i).get(j));
		    		dos.writeByte('\n');
		    	}
		    	dos.close();
		    	bos.close();
		    	fos.close();
			}
		}
		catch (Exception e) {}
    }
    
    private void reloadLastFriendsList()
    {
    	acceptedFriends = new ArrayList<String>();
    	invitedFriends = new ArrayList<String>();
    	expectingFriends = new ArrayList<String>();
    	blockedFriends = new ArrayList<String>();
    	String[] files = {"acceptedFriendsList", "invitedFriendsList", "expectingFriendsList", "blockedFriendsList"};
    	ArrayList<ArrayList<String>> lists = new ArrayList<ArrayList<String>>();
    	lists.add(acceptedFriends);
    	lists.add(invitedFriends);
    	lists.add(expectingFriends);
    	lists.add(blockedFriends);
		try
		{
			for (int i = 0; i < lists.size(); i++)
			{
				FileInputStream fis = openFileInput(files[i]);
		    	BufferedInputStream bis = new BufferedInputStream(fis);
		    	DataInputStream dis = new DataInputStream(bis);
		    	while(dis.available() != 0) lists.get(i).add(dis.readLine());
		    	dis.close();
		    	bis.close();
		    	fis.close();
			}
	    	showFriendsList();
		}
		catch (Exception e) {}
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
            	SWAACUtils.printMessage(SWAACFriendsListActivity.this, "Error: " + b.getString("message"));
            	if (!b.getString("message").equals("Friend doesn't exist") &&
            			!b.getString("message").equals("Relation already declared."))
            		finish();
            }
            else if (b.getBoolean("getListOfFriends"))
            {
            	friendsList = b.getStringArray("friends");
            	updateFriendsList(1);
            }
            else if (toUpdateList && (b.getBoolean("declareFriend") || b.getBoolean("ignoreFriend")))
            {
            	updateFriendsList(0);
            	toUpdateList = false;
            }
        }
    };
}
