package example.com.stepcounter;

import android.content.DialogInterface;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessActivities;
import com.google.android.gms.fitness.FitnessStatusCodes;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DailyTotalResult;
import com.google.android.gms.fitness.result.DataReadResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import example.example.Model.UserInfo;
import example.utils.AppConstants;
import example.utils.Utilities;


/**
 * Created by ayushgarg on 10/06/17.
 */

public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{

    @BindView(R.id.tv_calorie_burnt_value) TextView tvCalorieBurnt;
    @BindView(R.id.tv_kilometer_value) TextView tvKilometer;
    @BindView(R.id.tv_steps_value) TextView tvStepCount;
    @BindView(R.id.tv_steps_label) TextView tvStepLabel;
    @BindView(R.id.my_toolbar) android.support.v7.widget.Toolbar toolbar;

    Calendar c = Calendar.getInstance();
    SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd");
    String selecteddate = dateformat.format(c.getTime());

    private GoogleApiClient googleApiClient;
    private static final String AUTH_PENDING = "auth_state_pending";
    private static final int REQUEST_OAUTH = 1;

    private String TAG = "MainActivity";
    private boolean authInProgress = false;
    private String message;
    private String calorieInWallet;

    private float expendedCalories;
    private UserInfo userInfo;
    private DatabaseReference reference;
    private SharedPreferences preferences;
    private Utilities utilities;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        preferences = getSharedPreferences(AppConstants.PREF_NAME, MODE_PRIVATE);
        editor = preferences.edit();

        userInfo = UserInfo.getInstance();
        tvStepLabel.setText("Today's Steps");

        utilities = new Utilities();
        reference = FirebaseDatabase.getInstance().getReference().child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        getCalorieWalletFromFirebase();
        initializeGoogleAPIClient();
    }

    private void initializeGoogleAPIClient() {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Fitness.HISTORY_API)
                .addApi(Fitness.RECORDING_API)
                .addApi(Fitness.CONFIG_API)
                .addScope(new Scope(Scopes.FITNESS_ACTIVITY_READ))
                .addScope(new Scope(Scopes.FITNESS_LOCATION_READ))
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .enableAutoManage(this, 0, this)
                .build();
    }

    @Override
    public void onConnected(Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                message = getString(R.string.permission_denied_message);
                AlertDialog.Builder dialog = new AlertDialog.Builder(this);
                dialog.setMessage(message)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
//                                checkLocationSettings();
                                new FetchDistanceAsync().execute();
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                finish();
                            }
                        });
                dialog.show();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }else{
            new FetchDistanceAsync().execute();
        }

        subscribe();
        new FetchStepsAsync().execute();
        fetchUserGoogleFitData(selecteddate);
    }

    private void subscribe() {
        Fitness.RecordingApi.subscribe(googleApiClient, DataType.TYPE_STEP_COUNT_DELTA)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        if (status.isSuccess()) {
                            if (status.getStatusCode() == FitnessStatusCodes.SUCCESS_ALREADY_SUBSCRIBED) {
                                Log.e( "RecordingAPI", "Already subscribed to the get STEPS Recording API");
                            } else {
                                Log.e("RecordingAPI", "Subscribed to Count Steps using Recording API");
                            }
                        }
                    }
                });

        Fitness.RecordingApi.subscribe(googleApiClient, DataType.TYPE_CALORIES_EXPENDED)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        if (status.isSuccess()) {
                            if (status.getStatusCode() == FitnessStatusCodes.SUCCESS_ALREADY_SUBSCRIBED) {
                                Log.e( "RecordingAPI", "Already subscribed to the get Calories Recording API");
                            } else {
                                Log.e("RecordingAPI", "Subscribed to get Calories using Recording API");
                            }

                        }
                    }
                });
    }

    public void fetchUserGoogleFitData(String date) {
        if (googleApiClient != null && googleApiClient.isConnected()) {

            Date d1 = null;
            try{
                d1 = dateformat.parse(date);
            }catch (Exception e){

            }
            Calendar calendar = Calendar.getInstance();

            try{
                calendar.setTime(d1);
            }catch (Exception e){
                calendar.setTime(new Date());
            }
            DataReadRequest readRequest = queryDateFitnessData(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
            new GetCaloriesAsyncTask(readRequest, googleApiClient).execute();

        }
    }

    private DataReadRequest queryDateFitnessData(int year, int month, int day_of_Month) {
        Calendar startCalendar = Calendar.getInstance(Locale.getDefault());
        startCalendar.set(Calendar.YEAR, year);
        startCalendar.set(Calendar.MONTH, month);
        startCalendar.set(Calendar.DAY_OF_MONTH, day_of_Month);
        startCalendar.set(Calendar.HOUR_OF_DAY, 23);
        startCalendar.set(Calendar.MINUTE, 59);
        startCalendar.set(Calendar.SECOND, 59);
        startCalendar.set(Calendar.MILLISECOND, 999);
        long endTime = startCalendar.getTimeInMillis();


        startCalendar.set(Calendar.HOUR_OF_DAY, 0);
        startCalendar.set(Calendar.MINUTE, 0);
        startCalendar.set(Calendar.SECOND, 0);
        startCalendar.set(Calendar.MILLISECOND, 0);
        long startTime = startCalendar.getTimeInMillis();

        return new DataReadRequest.Builder()
                .aggregate(DataType.TYPE_CALORIES_EXPENDED, DataType.AGGREGATE_CALORIES_EXPENDED)
                .bucketByActivitySegment(1, TimeUnit.MILLISECONDS)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build();
    }

    public class GetCaloriesAsyncTask extends AsyncTask<Void, Void, DataReadResult> {
        DataReadRequest readRequest;
        GoogleApiClient mClient = null;

        public GetCaloriesAsyncTask(DataReadRequest dataReadRequest_, GoogleApiClient googleApiClient) {
            this.readRequest = dataReadRequest_;
            this.mClient = googleApiClient;
        }

        @Override
        protected DataReadResult doInBackground(Void... params) {
            return Fitness.HistoryApi.readData(mClient, readRequest).await(1, TimeUnit.MINUTES);
        }

        @Override
        protected void onPostExecute(DataReadResult dataReadResult) {
            super.onPostExecute(dataReadResult);
            printData(dataReadResult);
        }
    }

    private void printData(DataReadResult dataReadResult) {
        if (dataReadResult.getBuckets().size() > 0) {
            for (Bucket bucket : dataReadResult.getBuckets()) {
                String bucketActivity = bucket.getActivity();
                if (bucketActivity.contains(FitnessActivities.WALKING)) {
                    List<DataSet> dataSets = bucket.getDataSets();
                    for (DataSet dataSet : dataSets) {
                        dumpDataSet(dataSet);
                    }
                }
            }
            tvCalorieBurnt.setText(String.valueOf((int)(Math.round(expendedCalories))));
            userInfo.setCaloriesBurn(String.valueOf((int)(Math.round(expendedCalories))));

            if(Utilities.isIsNotificationShown() &&
                    selecteddate.equalsIgnoreCase(preferences.getString("currentDate", "")) &&
                    !preferences.getBoolean("isCalorieSavedInWallet", false)){
                new AlertDialog.Builder(this)
                .setTitle("Store calories in CalBanq!!")
                .setMessage("Do you want to store " + String.valueOf((int)(Math.round(expendedCalories))) +
                                        " calories in your CalBanq?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                reference.child("CaloriesWallet").setValue(userInfo.getCaloriesBurn());
                                Toast.makeText(MainActivity.this, "Calories saved in your Calorie Wallet.",
                                        Toast.LENGTH_SHORT).show();
                                editor.putBoolean("isCalorieSavedInWallet", true);
                                editor.commit();
                                dialogInterface.dismiss();
                            }
                        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        editor.putBoolean("isCalorieSavedInWallet", false);
                        editor.commit();
                        dialogInterface.dismiss();
                    }
                }).show();
            }
            reference.child("BurnedCalories").setValue(userInfo.getCaloriesBurn());

        } else if (dataReadResult.getDataSets().size() > 0) {
            Log.e(TAG, "Number of returned DataSets is: "
                    + dataReadResult.getDataSets().size());
            for (DataSet dataSet : dataReadResult.getDataSets()) {
                dumpDataSet(dataSet);
            }
        }
    }

    private void dumpDataSet(DataSet dataSet) {
        for (DataPoint dp : dataSet.getDataPoints()) {
            if (dp.getEndTime(TimeUnit.MILLISECONDS) > dp.getStartTime(TimeUnit.MILLISECONDS)) {
                for (Field field : dp.getDataType().getFields()) {
                    expendedCalories = expendedCalories + dp.getValue(field).asFloat();
                }
            }
        }
    }

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("yes","yes");
                    Fitness.RecordingApi.subscribe(googleApiClient, DataType.TYPE_DISTANCE_DELTA)
                            .setResultCallback(new ResultCallback<Status>() {
                                @Override
                                public void onResult(Status status) {
                                    if (status.isSuccess()) {
                                        if (status.getStatusCode() == FitnessStatusCodes.SUCCESS_ALREADY_SUBSCRIBED) {
                                            Log.e( "RecordingAPI", "Already subscribed to get Distance Recording API");
                                        } else {
                                            Log.e("RecordingAPI", "Subscribed to get distance using Recording API");
                                        }
                                    }
                                    new FetchDistanceAsync().execute();
                                }
                            });
                } else {
                    Log.d("yes","no");
                }
                return;
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        if (i == GoogleApiClient.ConnectionCallbacks.CAUSE_NETWORK_LOST) {
            Log.i(TAG, "Connection lost.  Cause: Network Lost.");
        } else if (i == GoogleApiClient.ConnectionCallbacks.CAUSE_SERVICE_DISCONNECTED) {
            Log.i(TAG, "Connection lost.  Reason: Service Disconnected");
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(TAG, "Connection failed. Cause: " + connectionResult.toString());
        if (!connectionResult.hasResolution()) {
            // Show the localized error dialog
            GooglePlayServicesUtil.getErrorDialog(connectionResult.getErrorCode(),
                    MainActivity.this, 0).show();
            return;
        }
        // The failure has a resolution. Resolve it.
        // Called typically when the app is not yet authorized, and an
        // authorization dialog is displayed to the user.
        if (!authInProgress) {
            try {
                Log.i(TAG, "Attempting to resolve failed connection");
                authInProgress = true;
                connectionResult.startResolutionForResult(MainActivity.this, REQUEST_OAUTH);
            } catch (IntentSender.SendIntentException e) {
                Log.e(TAG,
                        "Exception while starting resolution activity", e);
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        googleApiClient.connect();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(AUTH_PENDING, authInProgress);
    }

    private class FetchStepsAsync extends AsyncTask<Object, Object, Long> {
        protected Long doInBackground(Object... params) {
            long total = 0;
            PendingResult<DailyTotalResult> result = Fitness.HistoryApi.readDailyTotal(googleApiClient, DataType.TYPE_STEP_COUNT_DELTA);
            DailyTotalResult totalResult = result.await(30, TimeUnit.SECONDS);
            if (totalResult.getStatus().isSuccess()) {
                DataSet totalSet = totalResult.getTotal();
                if (totalSet != null) {
                    total = totalSet.isEmpty() ? 0 : totalSet.getDataPoints().get(0).getValue(Field.FIELD_STEPS).asInt();
                }
            } else {
                Log.w(TAG, "There was a problem getting the step count.");
            }
            return total;
        }


        @Override
        protected void onPostExecute(Long aLong) {
            super.onPostExecute(aLong);
            if(Integer.parseInt(aLong.toString()) < 10){
                tvStepCount.setText("0" + aLong.toString());
            }else {
                tvStepCount.setText(aLong.toString());
            }
        }
    }

    private class FetchDistanceAsync extends AsyncTask<Object, Object, Float>{
        @Override
        protected Float doInBackground(Object... objects) {
            float distance = 0;
            PendingResult<DailyTotalResult> result = Fitness.HistoryApi.readDailyTotal(googleApiClient, DataType.TYPE_DISTANCE_DELTA);
            DailyTotalResult totalResult = result.await(30, TimeUnit.SECONDS);
            if (totalResult.getStatus().isSuccess()) {
                DataSet totalSet = totalResult.getTotal();
                if (totalSet != null) {
                    distance = totalSet.isEmpty() ? 0 : totalSet.getDataPoints().get(0).getValue(Field.FIELD_DISTANCE).asFloat();
                }
            } else {
                Log.w(TAG, "There was a problem getting the calories.");
            }
            return distance;
        }

        @Override
        protected void onPostExecute(Float aLong) {
            super.onPostExecute(aLong);
            float distance = aLong/1000;
            double dis = Math.round(distance * 100.0) / 100.0;
            tvKilometer.setText(String.valueOf(dis));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_cal_cart:
                new AlertDialog.Builder(this)
                        .setTitle("Calorie wallet")
                        .setMessage("You have " + calorieInWallet +
                                " calories in your CalBanq wallet")
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        }).show();

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void getCalorieWalletFromFirebase() {
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot: dataSnapshot.getChildren()){
                    if(snapshot.getKey().equalsIgnoreCase("CaloriesWallet")){
                        calorieInWallet =  String.valueOf(snapshot.getValue());
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
