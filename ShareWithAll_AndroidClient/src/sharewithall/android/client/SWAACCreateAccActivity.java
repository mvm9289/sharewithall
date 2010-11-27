package sharewithall.android.client;

import sharewithall.android.client.R;
import sharewithall.android.client.sockets.SWAACSendSockets;
import android.app.Activity;
import android.app.ProgressDialog;
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
	SWAACSendSockets sendSockets;
	
    private void printMessage(String message)
    {
    	Toast.makeText(this, message, Toast.LENGTH_LONG).show();
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
    		Object[] data = new Object[2];
    		data[0] = username;
    		data[1] = password1;
    		progressDialog = ProgressDialog.show(this, "", getResources().getString(R.string.creatingAccMessage), true);
	    	sendSockets = new SWAACSendSockets(getBaseContext(), SWAACSendSockets.Command.NEW_USER, handler, data);
	    	sendSockets.send();
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
    
}
