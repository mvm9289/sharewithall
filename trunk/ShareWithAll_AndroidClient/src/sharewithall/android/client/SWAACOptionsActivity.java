package sharewithall.android.client;

import sharewithall.android.client.R;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class SWAACOptionsActivity extends PreferenceActivity
{
	
	private void resetServerOptions()
	{
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		SharedPreferences.Editor editor = preferences.edit();
		editor.putString("serverIPPref", "mvm9289.dyndns.org");
		editor.putString("serverPortPref", "4040");
		editor.commit();
	}
	
	private void resetFriendsOptions()
	{
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		SharedPreferences.Editor editor = preferences.edit();
    	editor.putBoolean("showSendedInvPref", true);
    	editor.putBoolean("showReceivedInvPref", true);
    	editor.putBoolean("showBlockedPref", true);
		editor.commit();
	}
	
	private void resetClientOptions()
	{
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		SharedPreferences.Editor editor = preferences.edit();
		editor.putString("deviceNamePref", "Android-Device");
		editor.putBoolean("isPublicPref", false);
		editor.putString("usernamePref", null);
		editor.putString("passwordPref", null);
		editor.putBoolean("autologinPref", false);
		editor.commit();
	}
	
	private void configureReloginListener(String item)
	{
		Preference preference = getPreferenceScreen().findPreference(item);
		preference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener()
		{
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue)
			{
				SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
				boolean loggedIn = preferences.getBoolean("loggedIn", false);
				if (loggedIn)
				{
					SharedPreferences.Editor editor = preferences.edit();
					editor.putBoolean("makeRelogin", true);
					editor.commit();
				}
				return true;
			}
		});
	}
	
	private void configureFriendListener(String item)
	{
		Preference preference = getPreferenceScreen().findPreference(item);
		preference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener()
		{
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue)
			{
				SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
				SharedPreferences.Editor editor = preferences.edit();
				editor.putBoolean("friendsListChanged", true);
				editor.commit();
				return true;
			}
		});
	}
	
	private void configureListeners()
	{
		configureReloginListener("deviceNamePref");
		configureReloginListener("isPublicPref");
		configureReloginListener("serverIPPref");
		configureReloginListener("serverPortPref");
		configureFriendListener("showSendedInvPref");
		configureFriendListener("showReceivedInvPref");
		configureFriendListener("showBlockedPref");
	}
	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.options);
        configureListeners();
	}
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.optionsmenu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
	        case R.id.resetDefaultMenu:
	        	resetClientOptions();
	        	resetServerOptions();
	        	finish();
	            return true;
	        case R.id.resetFriendsMenu:
	        	resetFriendsOptions();
	        	finish();
	        	return true;
	        case R.id.resetServerMenu:
	        	resetServerOptions();
	        	finish();
	        	return true;
	        case R.id.resetClientMenu:
	        	resetClientOptions();
	        	finish();
	        	return true;
	        default:
	            return super.onOptionsItemSelected(item);
        }
    }

}
