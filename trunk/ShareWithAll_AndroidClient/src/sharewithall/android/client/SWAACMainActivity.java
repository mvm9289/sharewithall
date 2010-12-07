package sharewithall.android.client;

import sharewithall.android.client.sockets.SWAACSendSockets;
import sharewithall.android.client.sockets.SWAACSendSockets.Command;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
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
import android.widget.Button;

public class SWAACMainActivity extends Activity
{

	//****************************************************//
	//***** Preferences and configuration attributes *****//
	//****************************************************//
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    
	//*********************************************//
	//***** Connection and sending attributes *****//
	//*********************************************//
    private ProgressDialog progressDialog;
    private SWAACSendSockets sendSockets;
    private String username;
    private String password;
    private String sessionID;
    private String[] onlineClients;
    
    
    
    

	//***************************************//
	//***** Activity override functions *****//
	//***************************************//

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        configure();
    }
    
    @Override
    protected void onResume()
    {
    	super.onResume();
        if (preferences.getBoolean("loggedIn", false)) toNextActivity(true, false);
        else configure2();
    }
    
    @Override
    protected void onDestroy()
    {
		editor.remove("loggedOut");
		editor.commit();
    	super.onDestroy();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mainmenu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
	        case R.id.createAccMenu:
	        	toCreateAccActivity();
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
        preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        editor = preferences.edit();

        if (preferences.getBoolean("firstExecute", true)) setDefaultPreferences();
        if (preferences.getBoolean("loggedIn", false)) toNextActivity(true, false);
        else
        {
        	setContentView(R.layout.swaac_main);
            
            Button loginButton = (Button) findViewById(R.id.loginButton);
            loginButton.setOnClickListener(new OnClickListener() {
    			@Override
    			public void onClick(View v)
    			{
    				username = SWAACUtils.getEditText(SWAACMainActivity.this, R.id.usernameEdit);
    				password = SWAACUtils.getEditText(SWAACMainActivity.this, R.id.passwordEdit);
    				login(0);
    			}
    		});
        }
    }
    
    private void configure2()
    {
    	username = preferences.getString("usernamePref", null);
    	password = preferences.getString("passwordPref", null);
    	if (!preferences.getBoolean("loggedOut", false) && preferences.getBoolean("autologinPref", false) &&
    			username != null && password != null)
    		login(0);
    	else
    	{
    		if (username != null) SWAACUtils.setEditText(this, R.id.usernameEdit, username);
    		if (password != null) SWAACUtils.setEditText(this, R.id.passwordEdit, password);
    	}
    }
    
    private void setDefaultPreferences()
    {
    	editor.putBoolean("firstExecute", false);
    	editor.putString("deviceNamePref", getString(R.string.defaultDeviceName));
    	editor.putBoolean("isPublicPref", false);
    	editor.putBoolean("autologinPref", false);
    	editor.putBoolean("autolaunchWebPref", true);
    	editor.putBoolean("allowReceiveFilesPref", true);
    	editor.putBoolean("autolaunchFilePref", true);
    	editor.putBoolean("showSendedInvPref", true);
    	editor.putBoolean("showReceivedInvPref", true);
    	editor.putBoolean("showBlockedPref", true);
    	editor.putString("serverIPPref", getString(R.string.defaultServerIP));
    	editor.putString("serverPortPref", getString(R.string.defaultServerPort));
    	editor.commit();
    }
    
    
    
    
    
	//*****************************************//
	//***** Activity navigation functions *****//
	//*****************************************//
    
    private void toNextActivity(boolean regetClients, boolean firstLogin)
    {
		editor.remove("loggedOut");
		editor.commit();
    	Intent intent = new Intent().setClass(this, SWAACLoggedInActivity.class);
    	intent.putExtra("regetClients", regetClients);
    	intent.putExtra("firstLogin", firstLogin);
    	intent.putExtra("onlineClients", onlineClients);
		startActivity(intent);
		finish();
    }
    
    private void toCreateAccActivity()
    {
    	Intent intent = new Intent().setClass(this, SWAACCreateAccActivity.class);
		startActivity(intent);
    }
    
    private void toOptionsActivity()
    {
    	Intent intent = new Intent().setClass(this, SWAACOptionsActivity.class);
		startActivity(intent);
    }

    
    
    
    
	//*****************************************//
	//***** Activity navigation functions *****//
	//*****************************************//
    
    private void login(int state)
    {
    	switch (state)
    	{
			case 0:
				if (username.length() < 6 || password.length() < 6)
					SWAACUtils.printMessage(this, getResources().getString(R.string.userAndPassMinimum));
				else
				{
			    	editor.putString("username", username);
			    	editor.putString("password", password);
			    	editor.commit();
			    	
			    	progressDialog = ProgressDialog.show(this, "", getResources().getString(R.string.loggingInMessage), true);
			    	Object[] data = new Object[2];
			    	data[0] = username;
			    	data[1] = password;
			    	sendSockets = new SWAACSendSockets(getBaseContext(), Command.LOGIN, handler, data);
			    	sendSockets.send();
				}
				break;
			case 1:
				editor.putBoolean("loggedIn", true);
		    	editor.putString("sessionID", sessionID);
		    	editor.commit();
				sendSockets = new SWAACSendSockets(getBaseContext(), Command.GET_ONLINE_CLIENTS, handler);
		    	sendSockets.start();
				break;
			case 2:
				progressDialog.dismiss();
		    	toNextActivity(false, true);
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
                progressDialog.dismiss();
            	SWAACUtils.printMessage(SWAACMainActivity.this, "Error: " + b.getString("message"));
            }
            else if (b.getBoolean("login"))
            {
            	sessionID = b.getString("sessionID");
            	login(1);
            }
            else
            {
            	onlineClients = b.getStringArray("onlineClients");
            	login(2);
            }
        }
    };
    
}