package sharewithall.android.client;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;

public class SWAACLoggedInActivity extends Activity
{
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.swaac_loggedin);

    	String swaprefs = getResources().getString(R.string.preferences);
    	SharedPreferences preferences = getSharedPreferences(swaprefs, MODE_PRIVATE);
    	String username = preferences.getString("username", "null");
    	String password = preferences.getString("password", "null");
    	String sessionID = preferences.getString("sessionID", "null");
    	TextView loggedInTextview = (TextView) findViewById(R.id.loggedInTextview);
    	loggedInTextview.setText("Username: " + username + " Password: " + password + " SessionID: " + sessionID);
    }
	
}
