package example.com.stepcounter;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;

import com.daimajia.androidanimations.library.Techniques;
import com.google.firebase.auth.FirebaseAuth;
import com.viksaa.sssplash.lib.activity.AwesomeSplash;
import com.viksaa.sssplash.lib.cnst.Flags;
import com.viksaa.sssplash.lib.model.ConfigSplash;

import java.security.SecureRandom;
import java.util.Calendar;

import example.example.Model.UserInfo;
import example.receiver.SaveCalorieReceiver;
import example.utils.AppConstants;


/**
 * Created by ayushgarg on 17/06/17.
 */

public class SplashActivity extends AwesomeSplash {

    UserInfo userInfo;
    private boolean isTokenExist = false;
    private boolean isUserSignedIn = false;
    private PendingIntent pendingIntent;

    SharedPreferences preferences;
    SharedPreferences.Editor editor;

    @Override
    public void initSplash(ConfigSplash configSplash) {

        Calendar calendar = Calendar.getInstance();

        calendar.set(Calendar.HOUR_OF_DAY, 22);
        calendar.set(Calendar.MINUTE, 30);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.AM_PM,Calendar.PM);

        Intent myIntent = new Intent(SplashActivity.this, SaveCalorieReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(SplashActivity.this, 0, myIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);

        configSplash.setBackgroundColor(R.color.white); //any color you want form colors.xml
        configSplash.setAnimCircularRevealDuration(100); //int ms
        configSplash.setRevealFlagX(Flags.REVEAL_RIGHT);  //or Flags.REVEAL_LEFT
        configSplash.setRevealFlagY(Flags.REVEAL_BOTTOM); //or Flags.REVEAL_TOP

        //Choose LOGO OR PATH; if you don't provide String value for path it's logo by default

        getTokenFromPreference();

        //Customize Logo
        configSplash.setLogoSplash(R.mipmap.splash_logo); //or any other drawable
        configSplash.setAnimLogoSplashDuration(2000); //int ms
        configSplash.setAnimLogoSplashTechnique(Techniques.Bounce); //choose one form Techniques (ref: https://github.com/daimajia/AndroidViewAnimations)

        //Customize Title
        configSplash.setTitleSplash("Calbanq");
        configSplash.setTitleTextColor(R.color.colorPrimaryDark);
        configSplash.setTitleTextSize(40f); //float value
        configSplash.setAnimTitleDuration(3000);
        configSplash.setAnimTitleTechnique(Techniques.FlipInX);
        configSplash.setTitleFont("fonts/streatwear.otf");
    }

    private void getTokenFromPreference() {
        preferences = getSharedPreferences(AppConstants.PREF_NAME, MODE_PRIVATE);
        editor = preferences.edit();
        String token;

        if(!preferences.contains("token")){
            token = generateToken();
            editor.putString("token", token);
            editor.commit();
        }else{
            token = preferences.getString("token", "");
            isTokenExist = true;
            isUserLoggedIn();
        }
        userInfo = UserInfo.getInstance();
        userInfo.setToken(token);

    }

    private void isUserLoggedIn() {
        if(null != FirebaseAuth.getInstance().getCurrentUser()) {
            isUserSignedIn = true;
        }else{
            isUserSignedIn = false;
        }
    }

    private String generateToken() {
        SecureRandom secureRandom = new SecureRandom();
        byte bytes[] = new byte[10];
        secureRandom.nextBytes(bytes);
        return bytes.toString();
    }

    @Override
    public void animationsFinished() {
        finish();
        if(!isTokenExist || !isUserSignedIn) {
            startActivity(new Intent(getApplicationContext(), InfoActivity.class));
        }else{
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
        }
    }
}
