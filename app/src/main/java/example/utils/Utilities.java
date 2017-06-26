package example.utils;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.support.v4.app.NotificationCompat;

import example.com.stepcounter.MainActivity;
import example.com.stepcounter.R;

/**
 * Created by ayushgarg on 17/06/17.
 */

public class Utilities {

    private NotificationManager notificationManager;
    private static boolean isNotificationShown;

    public static void generateNotification(Context context){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setSmallIcon(R.drawable.pedo_1);
        builder.setContentTitle("Save Calories");
        builder.setContentText("Save your burned calories to your calbanq!!");
        builder.setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000 });
        builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        builder.setAutoCancel(true);

        Intent resultIntent = new Intent(context, MainActivity.class);
        TaskStackBuilder TSB = TaskStackBuilder.create(context);
        TSB.addParentStack(MainActivity.class);
        // Adds the Intent that starts the Activity to the top of the stack
        TSB.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = TSB.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        builder.setContentIntent(resultPendingIntent);
        builder.setAutoCancel(true);
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        // mId allows you to update the notification later on.

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(11221, builder.build());

    }

    public int convertFootToInch(int foot){
        return foot * 12;
    }

    public float convertInchToMeter(int inch){
        return (float) (inch * 2.54)/100;
    }

    public float calculateBMI(float height, float weight){
        float result = weight/height;
        return result/height;
    }


//    Use Harris Benedict Method to calculate calories.
    /*
    Little or No Exercise, Desk Job:	                1.2 x BMR
    Light Exercise, Sports 1 to 3 Times Per Week:	    1.375 x BMR
    Moderate Exercise, Sports 3 to 5 Times Per Week:	1.55 x BMR
    Heavy Exercise, Sports 6 to 7 Times Per Week:   	1.725 x BMR
    */

    public double calculateCalories(int age, float height, String weight, String gender) {
        double bmr = 0;
        if(gender.equalsIgnoreCase("F")){
            bmr = 655 + (4.35 * convertKgToPound(Float.parseFloat(weight))) + (4.7 * convertMeterToInch(height)) - (4.7 * age);
        }else if(gender.equalsIgnoreCase("M")){
            bmr = 66 + (6.23 * convertKgToPound(Float.parseFloat(weight))) + (12.7 * convertMeterToInch(height)) - (6.8 * age);
        }
        return bmr * 1.375;
    }

    private double convertKgToPound(float weight) {
        return 2.20 * weight;
    }

    private double convertMeterToInch(float height) {
        return height * 39.7;
    }

    public static boolean isIsNotificationShown() {
        return isNotificationShown;
    }

    public static void setIsNotificationShown(boolean isNotificationShown) {
        Utilities.isNotificationShown = isNotificationShown;
    }
}
