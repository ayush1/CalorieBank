package example.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import example.utils.AppConstants;
import example.utils.Utilities;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by ayushgarg on 25/06/17.
 */

public class SaveCalorieReceiver extends BroadcastReceiver {
    SharedPreferences preferences;
    SharedPreferences.Editor editor;

    Calendar c = Calendar.getInstance();
    SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd");
    String selecteddate =    dateformat.format(c.getTime());

    @Override
    public void onReceive(Context context, Intent intent) {
        preferences = context.getSharedPreferences(AppConstants.PREF_NAME, MODE_PRIVATE);
        editor = preferences.edit();
        boolean isNotificationShown;
        try{
            if(!preferences.contains("isNotificationShown")){
                isNotificationShown = true;
                editor.putBoolean("isNotificationShown", true);
                editor.putString("currentDate", selecteddate);
                editor.commit();
                Utilities.generateNotification(context);
            }else{
                isNotificationShown = preferences.getBoolean("isNotificationShown", false);
            }
            Utilities.setIsNotificationShown(isNotificationShown);
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
