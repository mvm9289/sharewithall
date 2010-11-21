package sharewithall.android.client;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class SWAACLoggedInActivity extends Activity {
	@Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.swaac_loggedinactivity);
    	
    	Bundle data = getIntent().getExtras();
    	String username = data.getString("username");
    	String password = data.getString("password");
    	TextView loggedInTextview = (TextView) findViewById(R.id.loggedInTextview);
    	loggedInTextview.setText("Username: " + username + " Password: " + password);
    }
}
