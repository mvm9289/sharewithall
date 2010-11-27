package sharewithall.android.client;

import java.util.Timer;
import java.util.TimerTask;

import sharewithall.android.client.R;
import sharewithall.android.client.sockets.SWAACSendSockets;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.widget.Toast;

public class SWAACService extends Service
{
	
	Timer sessionUpdaterTimer;
	SessionUpdater sessionUpdater;

    private void printMessage(String message)
    {
    	Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
	
	private void programSessionUpdater()
	{
		sessionUpdater = new SessionUpdater(handler);
		sessionUpdaterTimer = new Timer();
		sessionUpdaterTimer.schedule(sessionUpdater, 0, 30000);
	}
	
	@Override
	public void onCreate()
	{
		super.onCreate();
		Toast.makeText(this, R.string.serviceStarted, Toast.LENGTH_SHORT).show();
		programSessionUpdater();
	}
	
	@Override
	public void onDestroy()
	{
		sessionUpdaterTimer.cancel();
		Toast.makeText(this, R.string.serviceStopped, Toast.LENGTH_SHORT).show();
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
            	printMessage("SWAService-Error: " + msg.getData().getString("message"));
        }
    };
    
    private class SessionUpdater extends TimerTask
    {
    	Handler handler;
    	SWAACSendSockets sendSockets;
    	
    	public SessionUpdater(Handler handler)
    	{
			super();
			this.handler = handler;
		}
    	
    	@Override
    	public void run()
    	{
    		sendSockets = new SWAACSendSockets(getBaseContext(), SWAACSendSockets.Command.UPDATE_TIMESTAMP, handler, null);
    		sendSockets.send();
    	}
    }

}
