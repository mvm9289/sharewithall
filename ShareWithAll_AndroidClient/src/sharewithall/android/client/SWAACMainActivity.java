package sharewithall.android.client;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class SWAACMainActivity extends Activity {
	
	private boolean loggedIn;
	private String username;
	private String password;
    
    private void login()
    {
    	EditText usernameEdit = (EditText) findViewById(R.id.usernameEdit);
		EditText passwordEdit = (EditText) findViewById(R.id.passwordEdit);
		username = usernameEdit.getText().toString();
		password = passwordEdit.getText().toString();
		
		if (username.length() < 6 || password.length() < 6)
		{
			String message = "Username and password must have 6 characters as minimum";
			Toast.makeText(SWAACMainActivity.this, message, Toast.LENGTH_SHORT).show();
		}
		else
		{
			loggedIn = true;
			toNextActivity();
		}
    }
    
    private void toNextActivity()
    {
    	Intent intent = new Intent().setClass(this, SWAACLoggedInActivity.class);
		intent.putExtra("username", username);
		intent.putExtra("password", password);
		startActivity(intent);
		finish();
    }
    
    
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        loggedIn = preferences.getBoolean("loggedIn", false);
        username = preferences.getString("username", null);
        password = preferences.getString("password", null);
        
        if (loggedIn) toNextActivity();
        else
        {
        	setContentView(R.layout.swaac_main);
	        
	        Button loginButton = (Button) findViewById(R.id.loginButton);
	        loginButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					login();
				}
			});
        }
    }
    
    @Override
    protected void onPause()
    {
    	super.onPause();

    	SharedPreferences preferences = getPreferences(MODE_PRIVATE);
    	SharedPreferences.Editor editor = preferences.edit();
    	
    	editor.putBoolean("loggedIn", loggedIn); 
    	editor.putString("username", username);
    	editor.putString("password", password);
    	
    	editor.commit();
    }
    
}