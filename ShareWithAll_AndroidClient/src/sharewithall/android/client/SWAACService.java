package sharewithall.android.client;

import sharewithall.android.client.sockets.SWAACSendSockets;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class SWAACService extends Service
{
	
	public static final String ERROR_ACTION = "SWAACServiceError";
	
	private PendingIntent alarmSender;
	private SharedPreferences preferences;
	private SharedPreferences.Editor editor;

    private void printMessage(String message)
    {
    	Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
    
    private void updateTimestamp()
    {
    	SWAACSendSockets sendSockets = new SWAACSendSockets(getBaseContext(), SWAACSendSockets.Command.UPDATE_TIMESTAMP, handler, null);
		sendSockets.send();
    }
    
    private void configureAlarm()
    {
        alarmSender = PendingIntent.getService(this, 0, new Intent(this, SWAACService.class), 0);
        
        long firstTime = SystemClock.elapsedRealtime() + 30000;
        AlarmManager alarm = (AlarmManager)getSystemService(ALARM_SERVICE);
        alarm.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstTime, 30000, alarmSender);
    }
    
    private void sendErrorNotification()
    {
    	editor.putBoolean("loggedIn", false);
    	editor.commit();
    	
    	Intent error = new Intent();
    	error.setAction(ERROR_ACTION);
    	sendBroadcast(error);
    }
	
	@Override
	public void onCreate()
	{
		super.onCreate();
		setForeground(true);
		preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		editor = preferences.edit();
		configureAlarm();
	}
	
	@Override
	public void onStart(Intent intent, int startId)
	{
		super.onStart(intent, startId);
		updateTimestamp();
	}
	
	@Override
	public void onDestroy()
	{
        AlarmManager alarm = (AlarmManager)getSystemService(ALARM_SERVICE);
        alarm.cancel(alarmSender);
		super.onDestroy();
	}
	
	@Override
	public IBinder onBind(Intent intent)
	{
		return null;
	}

	
	
	final Handler handler = new Handler() {
        public void handleMessage(Message msg)
        {
            if (msg.getData().getBoolean("exception"))
            {
            	printMessage("Error: " + msg.getData().getString("message"));
            	sendErrorNotification();
            	stopSelf();
            }
        }
    };

}
