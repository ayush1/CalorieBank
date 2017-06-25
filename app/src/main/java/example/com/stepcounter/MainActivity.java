package example.com.stepcounter;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
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
import com.google.android.gms.fitness.HistoryApi;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.data.Session;
import com.google.android.gms.fitness.data.Value;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.request.DataSourcesRequest;
import com.google.android.gms.fitness.request.OnDataPointListener;
import com.google.android.gms.fitness.request.SensorRequest;
import com.google.android.gms.fitness.request.SessionInsertRequest;
import com.google.android.gms.fitness.request.SessionReadRequest;
import com.google.android.gms.fitness.result.DailyTotalResult;
import com.google.android.gms.fitness.result.DataReadResult;
import com.google.android.gms.fitness.result.DataSourcesResult;
import com.google.android.gms.fitness.result.SessionReadResult;
import com.google.android.gms.fitness.result.SessionStopResult;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;

import java.text.DecimalFormat;
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
import example.utils.Utilities;

/**
 * Created by ayushgarg on 10/06/17.
 */

public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{

    private static final String SESSION_NAME = "mySession";
    @BindView(R.id.tv_calorie_burnt_value) TextView tvCalorieBurnt;
    @BindView(R.id.tv_kilometer_value) TextView tvKilometer;
//    @BindView(R.id.btn_start_stop) Button btnStartStop;
    @BindView(R.id.tv_steps_value) TextView tvStepCount;
    private boolean isStartActive = false;
    Utilities utilities;
    private String message;

//    Calendar calendar = Calendar.getInstance();
//    Date now = new Date();
//
//    long walkStartTime;
//    long walkEndTime;
//    private long startTime = calendar.getTimeInMillis();
//    private long endTime = calendar.getTimeInMillis();

    Calendar c = Calendar.getInstance();
    SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd");
    String selecteddate =    dateformat.format(c.getTime());

    /*@OnClick(R.id.btn_start_stop) void onClick(){
        calendar.setTime(now);
        if(!isStartActive) {
            btnStartStop.setText("Stop");
            isStartActive = true;
            startSession();
        }else{
            btnStartStop.setText("Start");
            isStartActive = false;
            stopSession();
        }
    }*/

    private GoogleApiClient googleApiClient;
    private static final String AUTH_PENDING = "auth_state_pending";
    private static final int REQUEST_OAUTH = 1;
    private boolean authInProgress = false;
    private String TAG = "MainActivity";
    private Session session;
    private float expendedCalories;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        utilities = new Utilities();
        initializeGoogleAPIClient();
//        inActiveCalories = utilities.calculateCalories(UserInfo.getInstance().getAge(), UserInfo.getInstance().getHeight(),
//                UserInfo.getInstance().getWeight(), UserInfo.getInstance().getGender());
//        UserInfo.getInstance().setCalories(calories);

    }

    private void initializeGoogleAPIClient() {
        /*googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Fitness.SENSORS_API)
                .addScope(new Scope(Scopes.FITNESS_BODY_READ_WRITE))
                .addScope(new Scope(Scopes.FITNESS_ACTIVITY_READ_WRITE))
                .addScope(new Scope(Scopes.FITNESS_LOCATION_READ))
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();*/

        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Fitness.HISTORY_API)
                .addApi(Fitness.CONFIG_API)
                .addScope(new Scope(Scopes.FITNESS_ACTIVITY_READ))
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        /*googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Fitness.SESSIONS_API)
                .addApi(Fitness.HISTORY_API)
                .addScope(new Scope(Scopes.FITNESS_LOCATION_READ_WRITE))
                .addScope(new Scope(Scopes.FITNESS_ACTIVITY_READ_WRITE))
                .addConnectionCallbacks(this)
                .enableAutoManage(this, 0, this)
                .build();*/
    }

    @Override
    public void onConnected(Bundle bundle) {
        /*DataSourcesRequest dataSourcesRequest = new DataSourcesRequest.Builder()
                .setDataTypes(DataType.TYPE_STEP_COUNT_CUMULATIVE)
                .setDataSourceTypes(DataSource.TYPE_DERIVED)
                .build();

        ResultCallback<DataSourcesResult> resultCallback = new ResultCallback<DataSourcesResult>() {
            @Override
            public void onResult(DataSourcesResult dataSourcesResult) {
                for(DataSource dataSource: dataSourcesResult.getDataSources()){
                    if(DataType.TYPE_STEP_COUNT_CUMULATIVE.equals(dataSource.getDataType())){
                        registerFitnessDataListener(dataSource, DataType.TYPE_STEP_COUNT_CUMULATIVE);
                    }
                }
            }
        };

        Fitness.SensorsApi.findDataSources(googleApiClient, dataSourcesRequest)
                .setResultCallback(resultCallback);*/

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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

        new FetchStepsAsync().execute();
//        new FetchCalorieAsync().execute();
        fetchUserGoogleFitData(selecteddate);

        /*session = new Session.Builder()
                .setName(SESSION_NAME)
                .setIdentifier(getString(R.string.app_name) + " " + System.currentTimeMillis())
                .setDescription("Walking")
                .setStartTime(Calendar.getInstance().getTimeInMillis(), TimeUnit.MILLISECONDS)
                .setActivity(FitnessActivities.WALKING)
                .build();*/
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
                // The data request can specify multiple data types to return, effectively
                // combining multiple data queries into one call.
                // In this example, it's very unlikely that the request is for several hundred
                // datapoints each consisting of a few steps and a timestamp.  The more likely
                // scenario is wanting to see how many steps were walked per day, for 7 days.
                //.aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
                //.aggregate(DataType.TYPE_CALORIES_EXPENDED,DataType.AGGREGATE_CALORIES_EXPENDED)
                .aggregate(DataType.TYPE_CALORIES_EXPENDED, DataType.AGGREGATE_CALORIES_EXPENDED)
                // .read(DataType.TYPE_CALORIES_EXPENDED)
                // Analogous to a "Group By" in SQL, defines how data should be aggregated.
                // bucketByTime allows for a time span, whereas bucketBySession would allow
                // bucketing by "sessions", which would need to be defined in code.
                //.bucketByTime(1, TimeUnit.DAYS)
                .bucketByActivitySegment(1, TimeUnit.MILLISECONDS)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build();

    }

    public class GetCaloriesAsyncTask extends AsyncTask<Void, Void, DataReadResult> {
        DataReadRequest readRequest;
        String TAG = GetCaloriesAsyncTask.class.getName();
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
        // [START parse_read_data_result]
        // If the DataReadRequest object specified aggregated data, dataReadResult will be returned
        // as buckets containing DataSets, instead of just DataSets.
        if (dataReadResult.getBuckets().size() > 0) {
            Log.e(TAG, "Number of returned buckets of DataSets is: "+ dataReadResult.getBuckets().size());
            for (Bucket bucket : dataReadResult.getBuckets()) {
                String bucketActivity = bucket.getActivity();
                if (bucketActivity.contains(FitnessActivities.WALKING)) {
                    Log.e(TAG, "bucket type->" + bucket.getActivity());
                    List<DataSet> dataSets = bucket.getDataSets();
                    for (DataSet dataSet : dataSets) {
                        dumpDataSet(dataSet);
                    }
                }
            }

            // expendedCalories => total active calories
//            Log.e(TAG, "BurnedCalories->" + String.valueOf(expendedCalories));
//            Toast.makeText(this, "Active calories: " + expendedCalories, Toast.LENGTH_SHORT).show();
            tvCalorieBurnt.setText(String.valueOf((int)(Math.round(expendedCalories))));

        } else if (dataReadResult.getDataSets().size() > 0) {
            Log.e(TAG, "Number of returned DataSets is: "
                    + dataReadResult.getDataSets().size());
            for (DataSet dataSet : dataReadResult.getDataSets()) {
                dumpDataSet(dataSet);
            }
        }

        // [END parse_read_data_result]
    }

    // [START parse_dataset]
    private void dumpDataSet(DataSet dataSet) {
        Log.e(TAG, "Data returned for Data type: " + dataSet.getDataType().getName());

        for (DataPoint dp : dataSet.getDataPoints()) {
            if (dp.getEndTime(TimeUnit.MILLISECONDS) > dp.getStartTime(TimeUnit.MILLISECONDS)) {
                for (Field field : dp.getDataType().getFields()) {
                    expendedCalories = expendedCalories + dp.getValue(field).asFloat();
                }
            }
        }
    }


    /*private void stopSession() {
        walkEndTime = calendar.getTimeInMillis();

        PendingResult<SessionStopResult> pendingResult = Fitness.SessionsApi.stopSession(googleApiClient,
                session.getIdentifier());

        pendingResult.setResultCallback(new ResultCallback<SessionStopResult>() {
            @Override
            public void onResult(SessionStopResult sessionStopResult) {
                if( sessionStopResult.getStatus().isSuccess() ) {
                    Log.i("Tuts+", "Successfully stopped session");
                    getSessionData();
                } else {
                    Log.i("Tuts+", "Failed to stop session: " +
                            sessionStopResult.getStatus().getStatusMessage());
                }
            }
        });
    }

    private void startSession() {
        walkStartTime = calendar.getTimeInMillis();

        PendingResult<Status> pendingResult = Fitness.SessionsApi.startSession(googleApiClient, session);

        pendingResult.setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        if (status.isSuccess()) {
                            Log.i("Tuts+", "Successfully started session");
                            storeSessionData();
                        } else {
                            Log.i("Tuts+", "Failed to start session: " + status.getStatusMessage());
                        }
                    }
                }
        );
    }

    private void storeSessionData() {
        DataSource activitySegmentDataSource = new DataSource.Builder()
                .setAppPackageName(this.getPackageName())
                .setDataType(DataType.TYPE_CALORIES_EXPENDED)
                .setName("Tuts+ activity segments dataset")
                .setType(DataSource.TYPE_RAW)
                .build();

        DataSet activityDataSet = DataSet.create(activitySegmentDataSource);

        DataPoint walkingActivityDataPoint = activityDataSet.createDataPoint()
                .setTimeInterval(walkStartTime, walkEndTime, TimeUnit.MILLISECONDS);
        walkingActivityDataPoint.getValue(Field.FIELD_CALORIES).setActivity(FitnessActivities.WALKING);
        activityDataSet.add(walkingActivityDataPoint);


        Session session = new Session.Builder()
                .setName(SESSION_NAME)
                .setIdentifier(getString(R.string.app_name) + " " + System.currentTimeMillis())
                .setDescription("Running in Segments")
                .setStartTime(startTime, TimeUnit.MILLISECONDS)
                .setEndTime(endTime, TimeUnit.MILLISECONDS)
                .setActivity(FitnessActivities.RUNNING)
                .build();

        SessionInsertRequest insertRequest = new SessionInsertRequest.Builder()
                .setSession(session)
                .addDataSet(activityDataSet)
                .build();

        PendingResult<Status> pendingResult = Fitness.SessionsApi.insertSession(googleApiClient, insertRequest);

        pendingResult.setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(Status status) {
                if( status.isSuccess() ) {
                    Log.i("Tuts+", "successfully inserted running session");
                } else {
                    Log.i("Tuts+", "Failed to insert running session: " + status.getStatusMessage());
                }
            }
        });
    }

    private void getSessionData() {
        SessionReadRequest readRequest = new SessionReadRequest.Builder()
                .setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS)
                .read(DataType.TYPE_CALORIES_EXPENDED)
                .setSessionName(SESSION_NAME)
                .build();

        PendingResult<SessionReadResult> sessionReadResult =
                Fitness.SessionsApi.readSession(googleApiClient, readRequest);

        sessionReadResult.setResultCallback(new ResultCallback<SessionReadResult>() {
            @Override
            public void onResult(SessionReadResult sessionReadResult) {
                if (sessionReadResult.getStatus().isSuccess()) {
                    Log.i("Tuts+", "Successfully read session data");
                    for (Session session : sessionReadResult.getSessions()) {
                        Log.i("Tuts+", "Session name: " + session.getName());
                        for (DataSet dataSet : sessionReadResult.getDataSet(session)) {
                            for (DataPoint dataPoint : dataSet.getDataPoints()) {
                                Log.i("Tuts+", "Calories " + dataPoint.getValue(Field.FIELD_CALORIES));
                                tvCalorieBurnt.setText(String.valueOf(dataPoint.getValue(Field.FIELD_CALORIES)));
                            }
                        }
                    }
                } else {
                    Log.i("Tuts+", "Failed to read session data");
                }
            }
        });
    }*/

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("yes","yes");
                    new FetchDistanceAsync().execute();
                } else {
                    Log.d("yes","no");
                }
                return;
            }
        }
    }

    /*private void registerFitnessDataListener(DataSource dataSource, DataType dataType) {

        SensorRequest sensorRequest = new SensorRequest.Builder()
                .setDataSource(dataSource)
                .setDataType(dataType)
                .setSamplingRate(1, TimeUnit.SECONDS)
                .build();

        Fitness.SensorsApi.add(googleApiClient, sensorRequest, this)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        if(status.isSuccess()){
                            Log.e(TAG, "onResult: Sensor API added successfully");
                        }
                    }
                });
    }*/

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
        /*if(!authInProgress){
            try {
                authInProgress = true;
                connectionResult.startResolutionForResult(this, REQUEST_OAUTH);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        }else{
            Log.e(TAG, "onConnectionFailed: auth in progress");
        }*/

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

    /*@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(REQUEST_OAUTH == requestCode){
            if(RESULT_OK == resultCode){
                if(!googleApiClient.isConnected() && !googleApiClient.isConnecting()){
                    googleApiClient.connect();
                }
            }else if(RESULT_CANCELED == resultCode){
                Log.e(TAG, "onActivityResult: User canceled the process");
            }
        }else{
            Log.e(TAG, "onActivityResult: wrong request code");
        }
    }*/

    /*@Override
    public void onDataPoint(DataPoint dataPoint) {
        for(final Field field: dataPoint.getDataType().getFields()){
            final Value value = dataPoint.getValue(field);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
//                    Toast.makeText(MainActivity.this, "Field: " + field + ", Value: " + value, Toast.LENGTH_SHORT).show();
                    if(Integer.parseInt(value.toString()) < 10){
                        tvStepCount.setText("0" + value.toString());
                    }else {
                        tvStepCount.setText(value.toString());
                    }
                    tvCalorieBurnt.setText(String.valueOf((int)Math.round(calories)));
                }
            });
        }
    }*/

    @Override
    protected void onStart() {
        super.onStart();
        googleApiClient.connect();
    }

    /*@Override
    protected void onStop() {
        super.onStop();
        Fitness.SensorsApi.remove(googleApiClient, this)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        if(status.isSuccess()){
                            googleApiClient.disconnect();
                        }
                    }
                });
    }*/

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

   /* private class FetchCalorieAsync extends AsyncTask<Object, Object, Float> {
        protected Float doInBackground(Object... params) {
            float total = 0;
            PendingResult<DailyTotalResult> result = Fitness.HistoryApi.readDailyTotal(googleApiClient, DataType.TYPE_CALORIES_EXPENDED);
            DailyTotalResult totalResult = result.await(30, TimeUnit.SECONDS);
            if (totalResult.getStatus().isSuccess()) {
                DataSet totalSet = totalResult.getTotal();
                if (totalSet != null) {
                    total = totalSet.isEmpty() ? 0 : totalSet.getDataPoints().get(0).getValue(Field.FIELD_CALORIES).asFloat();
                }
            } else {
                Log.w(TAG, "There was a problem getting the calories.");
            }
            return total;
        }

        @Override
        protected void onPostExecute(Float aLong) {
            super.onPostExecute(aLong);
            tvCalorieBurnt.setText(String.valueOf((int)(Math.round(aLong))));
        }
    }*/

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
}
