package example.com.stepcounter;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import example.example.Model.UserInfo;
import example.utils.AppConstants;

/**
 * Created by ayushgarg on 19/06/17.
 */

public class SignupLoginActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    @BindView(R.id.btn_google) Button btn_google;
    @BindView(R.id.et_name) EditText et_name;
    @BindView(R.id.et_email) EditText et_email;

    private String TAG = this.getClass().getSimpleName();
    private FirebaseAuth firebaseAuth;
    private GoogleApiClient googleApiClient;
    private FirebaseAuth.AuthStateListener authStateListener;

    private String email;
    private String name;
    private UserInfo userInfo;

    @OnClick(R.id.btn_google) void onClick(){
        signIn();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup_login);
        ButterKnife.bind(this);

        userInfo = UserInfo.getInstance();

        firebaseAuth = FirebaseAuth.getInstance();

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(FirebaseAuth firebaseAuth) {
                FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                if(firebaseUser != null){
                    Log.d(TAG, "User Signed-In" + firebaseUser.getUid());
                }else{
                    Log.d(TAG, "User Signed-Out");
                }
            }
        };

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
    }

    private void signIn() {
        Intent intent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
        startActivityForResult(intent, AppConstants.RC_SIGN_IN);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AppConstants.RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                // Google Sign In failed, update UI appropriately
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        AuthCredential authCredential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        email = account.getEmail();
        name = account.getDisplayName();

        userInfo.setEmail(email);

        firebaseAuth.signInWithCredential(authCredential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(Task<AuthResult> task) {
                        Log.d(TAG, String.valueOf(task.isSuccessful()));
                        if(!task.isSuccessful()){
                            Toast.makeText(SignupLoginActivity.this, "Failed to login", Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(SignupLoginActivity.this, "Successfully login via google", Toast.LENGTH_SHORT).show();

                            DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child(FirebaseAuth.getInstance().getCurrentUser().getUid());
                            reference.child("Token").setValue(userInfo.getToken());
                            reference.child("Age").setValue(userInfo.getAge());
                            reference.child("Height").setValue(userInfo.getHeight());
                            reference.child("Weight").setValue(userInfo.getWeight());
                            reference.child("BMI").setValue(userInfo.getBMI());
                            reference.child("Email").setValue(userInfo.getEmail());

                            Intent intent = new Intent(SignupLoginActivity.this, MainActivity.class);
                            startActivity(intent);
                        }
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(firebaseAuth != null){
            firebaseAuth.removeAuthStateListener(authStateListener);
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

}
