package sharewithall.android.client;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class SWAACUtils
{

	//******************************************************//
	//***** Some util functions shared by some classes *****//
	//******************************************************//
	
	public static void printMessage(Context context, String message)
	{
    	Toast.makeText(context, message, Toast.LENGTH_LONG).show();
	}
	
	public static void setEditText(View view, int res, String text)
	{
		((EditText) view.findViewById(res)).setText(text);
	}
	
	public static String getEditText(View view, int res)
	{
		return ((EditText) view.findViewById(res)).getText().toString();
	}
	
	public static void setEditText(Activity activity, int res, String text)
	{
		((EditText) activity.findViewById(res)).setText(text);
	}
	
	public static String getEditText(Activity activity, int res)
	{
		return ((EditText) activity.findViewById(res)).getText().toString();
	}

}
