package sharewithall.android.client;

import sharewithall.android.client.sockets.SWAACSendSockets;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class SWAACChatWindowActivity extends Activity
{
	private ProgressDialog progressDialog;
	private SWAACSendSockets sendSockets;
	private SharedPreferences preferences;
	private SharedPreferences.Editor editor;
	private ArrayAdapter<String> outputText;
	private String user;
	private String client;
	private String token;
	private String clientIP;
	private String clientPort;

    private void printMessage(String message)
    {
    	Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
    
    private String getEditText()
    {
    	EditText editText = (EditText) findViewById(R.id.chatInput);
    	String text = editText.getText().toString();
    	editText.setText("");
    	
    	return text;
    }
    
    private void sendText()
    {
    	String text = getEditText();
    	Object[] data = new Object[4];
    	data[0] = token;
    	data[1] = clientIP;
    	data[2] = clientPort;
    	data[3] = text;
		sendSockets = new SWAACSendSockets(getBaseContext(), SWAACSendSockets.Command.SEND_TEXT, handler, data);
    	sendSockets.start();
    	outputText.add("I: " + text);
    }
    
    private void configureChat()
    {
        outputText = new ArrayAdapter<String>(this, R.layout.swaac_chatmessage);
    	ListView viewText = (ListView) findViewById(R.id.chatOutput);
    	viewText.setAdapter(outputText);
        
    	Button sendButton = (Button) findViewById(R.id.chatSendButton);
    	sendButton.setOnClickListener(new OnClickListener()
    	{	
			@Override
			public void onClick(View v)
			{
				sendText();
			}
		});
    	
    	TextView title = (TextView) findViewById(R.id.chatTitle);
    	title.setText(getResources().getString(R.string.chatWindow) + " - " + client);
    }
    
    private void getIPandPort()
    {
    	progressDialog = ProgressDialog.show(this, "", getResources().getString(R.string.connectingWithClient), true);
    	Object[] data = new Object[1];
		data[0] = user + ":" + client;
		sendSockets = new SWAACSendSockets(getBaseContext(), SWAACSendSockets.Command.IP_AND_PORT_REQUEST, handler, data);
    	sendSockets.start();
    }
    
    private void getSendToken(String[] ipAndPort)
    {
    	clientIP = ipAndPort[0];
    	clientPort = ipAndPort[1];
    	
    	Object[] data = new Object[1];
		data[0] = user + ":" + client;
		sendSockets = new SWAACSendSockets(getBaseContext(), SWAACSendSockets.Command.GET_SEND_TOKEN, handler, data);
    	sendSockets.start();
    }
    
    private void tokenGot(String token)
    {
    	progressDialog.dismiss();
    	this.token = token;
    }
    
    
    
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.swaac_chatwindow);
		preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		editor = preferences.edit();
		user = getIntent().getStringExtra("clientUser");
		if (user.equals("")) user = preferences.getString("username", null);
		client = getIntent().getStringExtra("client");
		configureChat();
		getIPandPort();
	}

	
	
    final Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.getData().getBoolean("exception"))
            {
                progressDialog.dismiss();
            	printMessage("Error: " + msg.getData().getString("message"));
            	finish();
            }
            else if (msg.getData().getBoolean("ipAndPortRequest"))
            	getSendToken(msg.getData().getStringArray("ipAndPort"));
            else if (msg.getData().getBoolean("getSendToken"))
            	tokenGot(msg.getData().getString("token"));
        }
    };
    
}
