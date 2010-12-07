package sharewithall.android.client;

import sharewithall.android.client.sockets.SWAACSendSockets;
import sharewithall.android.client.sockets.SWAACSendSockets.Command;
import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class SWAACCreateAccActivity extends Activity
{

	//*********************************************//
	//***** Connection and sending attributes *****//
	//*********************************************//
	private ProgressDialog progressDialog;
	private SWAACSendSockets sendSockets;
	private String username;
	private String password1;
	private String password2;
	
	
	

	
	//***************************************//
	//***** Activity override functions *****//
	//***************************************//

	@Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        configure();
    }
	
	
	
	
	
	//***********************************//
	//***** Configuration functions *****//
	//***********************************//

	private void configure()
    {
    	setContentView(R.layout.swaac_createacc);
        
        Button createButton = (Button) findViewById(R.id.createButton);
        createButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				username = SWAACUtils.getEditText(SWAACCreateAccActivity.this, R.id.usernameEditCreate);
				password1 = SWAACUtils.getEditText(SWAACCreateAccActivity.this, R.id.passwordEdit1Create);
				password2 = SWAACUtils.getEditText(SWAACCreateAccActivity.this, R.id.passwordEdit2Create);
				createAccount(0);
			}
		});
    }
	
	
	
	
	
	//************************************//
	//***** Create account functions *****//
	//************************************//
	
	private void createAccount(int state)
	{
		switch(state)
		{
			case 0:
				if (!password1.equals(password2))
		    		SWAACUtils.printMessage(this, getResources().getString(R.string.passwordsNotMatch));
				else if (username.length() < 6 || password1.length() < 6)
					SWAACUtils.printMessage(this, getResources().getString(R.string.userAndPassMinimum));
		    	else
		    	{
		    		Object[] data = new Object[2];
		    		data[0] = username;
		    		data[1] = password1;
		    		progressDialog = ProgressDialog.show(this, "", getString(R.string.creatingAccMessage), true);
			    	sendSockets = new SWAACSendSockets(getBaseContext(), Command.NEW_USER, handler, data);
			    	sendSockets.send();
		    	}
				break;
			case 1:
				SWAACUtils.printMessage(this, getString(R.string.createdSuccessful));
		    	finish();
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
            progressDialog.dismiss();
            Bundle b = msg.getData();
            if (b.getBoolean("exception"))
            	SWAACUtils.printMessage(SWAACCreateAccActivity.this, "Error: " + b.getString("message"));
            else createAccount(1);
        }
    };
    
}
