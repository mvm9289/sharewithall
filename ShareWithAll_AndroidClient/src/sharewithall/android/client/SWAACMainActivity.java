package sharewithall.android.client;

import sharewithall.android.client.R;
import sharewithall.android.client.sockets.SWAACSendSockets;
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
import android.widget.EditText;
import android.widget.Toast;

public class SWAACMainActivity extends Activity
{
	
    private ProgressDialog progressDialog;
    private SWAACSendSockets sendSockets;
    
    private void setDefaultPreferences()
    {
    	SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
    	SharedPreferences.Editor editor = preferences.edit();
    	editor.putBoolean("firstExecute", false);
    	editor.putString("deviceNamePref", "Android-Device");
    	editor.putBoolean("isPublicPref", false);
    	editor.putBoolean("autologinPref", false);
    	editor.putString("serverIPPref", "mvm9289.dyndns.org");
    	editor.putString("serverPortPref", "4040");
    	editor.commit();
    }
    
    private void toNextActivity()
    {
    	SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
    	SharedPreferences.Editor editor = preferences.edit();
		editor.remove("loggedOut");
		editor.commit();
    	Intent intent = new Intent().setClass(this, SWAACLoggedInActivity.class);
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
    
    private void printMessage(String message)
    {
    	Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void setUsernameEdit(String username)
    {
    	EditText usernameEdit = (EditText) findViewById(R.id.usernameEdit);
    	usernameEdit.setText(username);
    }

    private void setPasswordEdit(String password)
    {
    	EditText passwordEdit = (EditText) findViewById(R.id.passwordEdit);
    	passwordEdit.setText(password);
    }
    
    private String getUsernameEdit()
    {
    	EditText usernameEdit = (EditText) findViewById(R.id.usernameEdit);
		return usernameEdit.getText().toString();
    }
    
    private String getPasswordEdit()
    {
    	EditText passwordEdit = (EditText) findViewById(R.id.passwordEdit);
		return passwordEdit.getText().toString();
    }
    
    private void login(String username, String password)
    {
		if (username.length() < 6 || password.length() < 6)
			printMessage(getResources().getString(R.string.userAndPassMinimum));
		else
		{
	    	progressDialog = ProgressDialog.show(this, "", getResources().getString(R.string.loggingInMessage), true);
	    	Object[] data = new Object[2];
	    	data[0] = username;
	    	data[1] = password;
	    	sendSockets = new SWAACSendSockets(getBaseContext(), SWAACSendSockets.Command.LOGIN, handler, data);
	    	sendSockets.send();
		}
    }
    
    private void logged(String sessionID)
    {
    	SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
    	SharedPreferences.Editor editor = preferences.edit();
    	editor.putBoolean("loggedIn", true);
    	editor.putString("sessionID", sessionID);
    	editor.commit();
    	toNextActivity();
    }
    
    private void executeThis()
    {
    	configureThis();
    	SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
    	String username = preferences.getString("usernamePref", null);
    	String password = preferences.getString("passwordPref", null);
    	if (!preferences.getBoolean("loggedOut", false) && preferences.getBoolean("autologinPref", false) &&
    			username != null && password != null)
    		login(username, password);
    	else
    	{
    		if (username != null) setUsernameEdit(username);
    		if (password != null) setPasswordEdit(password);
    	}
    }
    
    private void configureThis()
    {
    	setContentView(R.layout.swaac_main);
        
        Button loginButton = (Button) findViewById(R.id.loginButton);
        loginButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				login(getUsernameEdit(), getPasswordEdit());
			}
		});
    }
    
    
    
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

    	SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        if (preferences.getBoolean("firstExecute", true)) setDefaultPreferences();      
        if (preferences.getBoolean("loggedIn", false)) toNextActivity();
        else executeThis();
    }
    
    @Override
    protected void onDestroy()
    {
    	SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
    	SharedPreferences.Editor editor = preferences.edit();
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
    
    
    
    final Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            progressDialog.dismiss();
            if (msg.getData().getBoolean("exception"))
            	printMessage("Error: " + msg.getData().getString("message"));
            else logged(msg.getData().getString("sessionID"));
        }
    };
    
}