package sharewithall.android.client;

import sharewithall.client.sockets.SWAClientSockets;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class SWAACCreateAccActivity extends Activity
{

	ProgressDialog progressDialog;
	ProgressThread progressThread;
	
    private void printMessage(String message)
    {
    	Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
    
	private String getUsernameEdit()
    {
    	EditText usernameEdit = (EditText) findViewById(R.id.usernameEditCreate);
		return usernameEdit.getText().toString();
    }
    
    private String getPasswordEdit1()
    {
    	EditText passwordEdit = (EditText) findViewById(R.id.passwordEdit1Create);
		return passwordEdit.getText().toString();
    }
    
    private String getPasswordEdit2()
    {
    	EditText passwordEdit = (EditText) findViewById(R.id.passwordEdit2Create);
		return passwordEdit.getText().toString();
    }
    
    private void createAccount(String username, String password1, String password2)
    {
    	if (!password1.equals(password2))
    		printMessage(getResources().getString(R.string.passwordsNotMatch));
    	else
    	{
    		progressDialog = ProgressDialog.show(this, "", getResources().getString(R.string.creatingAccMessage), true);
	    	progressThread = new ProgressThread(handler, username, password1);
	    	progressThread.start();
    	}
    }
    
    private void created()
    {
    	printMessage(getResources().getString(R.string.createdSuccessful));
    	finish();
    }
    
	private void configureThis()
    {
    	setContentView(R.layout.swaac_createacc);
        
        Button createButton = (Button) findViewById(R.id.createButton);
        createButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				createAccount(getUsernameEdit(), getPasswordEdit1(), getPasswordEdit2());
			}
		});
    }
	
	@Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        configureThis();
    }
	
	
	
	final Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            progressDialog.dismiss();
            if (msg.getData().getBoolean("exception"))
            	printMessage("Error: " + msg.getData().getString("message"));
            else created();
        }
    };
    
    private class ProgressThread extends Thread
    {
    	Handler handler;
    	String username;
    	String password;
       
        ProgressThread(Handler handler, String username, String password)
        {
        	this.handler = handler;
        	this.username = username;
        	this.password = password;
        }
       
        public void run()
        {
        	String swaprefs = getResources().getString(R.string.preferences);
        	SharedPreferences preferences = getSharedPreferences(swaprefs, MODE_PRIVATE);
    		String serverIP = preferences.getString("serverIP", "mvm9289.dyndns.org");
        	int serverPort = preferences.getInt("serverPort", 4040);
        	SWAClientSockets socketsModule = new SWAClientSockets(serverIP, serverPort);
        	
    		try
    		{
    			socketsModule.newUser(username, password);
    			Bundle b = new Bundle();
    			b.putBoolean("exception", false);
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
