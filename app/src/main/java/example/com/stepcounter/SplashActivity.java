package example.com.stepcounter;

import android.content.Intent;
import android.content.SharedPreferences;

import com.daimajia.androidanimations.library.Techniques;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.viksaa.sssplash.lib.activity.AwesomeSplash;
import com.viksaa.sssplash.lib.cnst.Flags;
import com.viksaa.sssplash.lib.model.ConfigSplash;

import java.security.SecureRandom;

import example.example.Model.UserInfo;
import example.utils.AppConstants;


/**
 * Created by ayushgarg on 17/06/17.
 */

public class SplashActivity extends AwesomeSplash {

    SharedPreferences preferences;
    UserInfo userInfo;
    private boolean isTokenExist = false;
    private boolean isUserSignedIn = false;

    @Override
    public void initSplash(ConfigSplash configSplash) {

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
        SharedPreferences.Editor editor = preferences.edit();
        String token;

        if(!preferences.contains("token")){
            token = generateToken();
            editor.putString("token", token);
            editor.commit();
        }else{
            token = preferences.getString("token", "");
            isTokenExist = true;
            checkTokenExistOnFirebase(token);
        }
        userInfo = UserInfo.getInstance();
        userInfo.setToken(token);

    }

    private void checkTokenExistOnFirebase(final String token) {
        if(null != FirebaseAuth.getInstance().getCurrentUser()) {
            isUserSignedIn = true;
            /*FirebaseDatabase.getInstance().getReference().child(FirebaseAuth.getInstance().getCurrentUser()
                    .getUid()).child("Token").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot != null && dataSnapshot.getValue() != null) {
                        if (token.equalsIgnoreCase(dataSnapshot.getValue().toString())) {
                            isUserSignedIn = true;
                        } else {
                            isUserSignedIn = false;
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });*/
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
