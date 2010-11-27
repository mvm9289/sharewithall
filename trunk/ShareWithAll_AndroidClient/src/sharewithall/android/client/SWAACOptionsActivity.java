package sharewithall.android.client;

import sharewithall.android.client.R;
import android.os.Bundle;
import android.preference.PreferenceActivity;

public class SWAACOptionsActivity extends PreferenceActivity
{
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.options);
	}

}
