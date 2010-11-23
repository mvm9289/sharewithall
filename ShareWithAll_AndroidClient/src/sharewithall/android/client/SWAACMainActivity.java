package sharewithall.android.client;

import sharewithall.client.sockets.SWAClientSockets;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
    ProgressDialog progressDialog;
    ProgressThread progressThread;
    
    private void setDefaultPreferences()
    {
    	String swaprefs = getResources().getString(R.string.preferences);
    	SharedPreferences preferences = getSharedPreferences(swaprefs, MODE_PRIVATE);
    	SharedPreferences.Editor editor = preferences.edit();
    	editor.putBoolean("firstExecute", false);
    	editor.putBoolean("autoLogin", false);
    	editor.putString("serverIP", "mvm9289.dyndns.org");
    	editor.putInt("serverPort", 4040);
    	editor.putString("deviceName", "Android-Device");
    	editor.putBoolean("isPublic", false);
    	editor.commit();
    }
    
    private void toNextActivity()
    {
    	Intent intent = new Intent().setClass(this, SWAACLoggedInActivity.class);
		startActivity(intent);
		finish();
    }
    
    private void toCreateAccActivity()
    {
    	Intent intent = new Intent().setClass(this, SWAACCreateAccActivity.class);
		startActivity(intent);
    }
    
    private void printMessage(String message)
    {
    	Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
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
			String swaprefs = getResources().getString(R.string.preferences);
	    	SharedPreferences preferences = getSharedPreferences(swaprefs, MODE_PRIVATE);
	    	SharedPreferences.Editor editor = preferences.edit();
	    	editor.putString("username", username);
	    	editor.putString("password", password);
	    	editor.commit();
	    	
	    	progressDialog = ProgressDialog.show(this, "", getResources().getString(R.string.logginInMessage), true);
	    	progressThread = new ProgressThread(handler);
	    	progressThread.start();
		}
    }
    
    private void logged(String sessionID)
    {
    	progressDialog.dismiss();
		String swaprefs = getResources().getString(R.string.preferences);
    	SharedPreferences preferences = getSharedPreferences(swaprefs, MODE_PRIVATE);
    	SharedPreferences.Editor editor = preferences.edit();
    	editor.putBoolean("loggedIn", true);
    	editor.putString("sessionID", sessionID);
    	editor.commit();
    	toNextActivity();
    }
    
    private void executeThis()
    {
    	configureThis();
    	String swaprefs = getResources().getString(R.string.preferences);
    	SharedPreferences preferences = getSharedPreferences(swaprefs, MODE_PRIVATE);
    	String username = preferences.getString("username", null);
    	String password = preferences.getString("password", null);
    	if (preferences.getBoolean("autoLogin", false) && username != null && password != null)
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
        
        String swaprefs = getResources().getString(R.string.preferences);
    	SharedPreferences preferences = getSharedPreferences(swaprefs, MODE_PRIVATE);
        if (preferences.getBoolean("firstExecute", true)) setDefaultPreferences();      
        if (preferences.getBoolean("loggedIn", false)) toNextActivity();
        else executeThis();
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
	        	printMessage("Options");
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
    
    private class ProgressThread extends Thread
    {
    	Handler handler;
       
        ProgressThread(Handler handler)
        {
        	this.handler = handler;
        }
       
        public void run()
        {
        	String swaprefs = getResources().getString(R.string.preferences);
        	SharedPreferences preferences = getSharedPreferences(swaprefs, MODE_PRIVATE);
    		String serverIP = preferences.getString("serverIP", "mvm9289.dyndns.org");
        	int serverPort = preferences.getInt("serverPort", 4040);
        	SWAClientSockets socketsModule = new SWAClientSockets(serverIP, serverPort);
        	
        	String username = preferences.getString("username", null);
        	String password = preferences.getString("password", null);
        	String deviceName = preferences.getString("deviceName", "default-device");
        	boolean isPublic = preferences.getBoolean("isPublic", false);
        	
        	String sessionID = null;
    		try
    		{
    			sessionID = socketsModule.login(username, password, deviceName, isPublic);
    			Bundle b = new Bundle();
    			b.putBoolean("exception", false);
    			b.putString("sessionID", sessionID);
    			Message msg = handler.obtainMessage();
    			msg.setData(b);
    			handler.sendMessage(msg);
    		}
    		catch (Exception e)
    		{
    			Bundle b = new Bundle();
    			b.putBoolean("exception", true);
    			b.putString("message", e.getMessage());
    			Message msg = handler.obtainMessage();
    			msg.setData(b);
    			handler.sendMessage(msg);
    		}
        }
        
    }
    
}