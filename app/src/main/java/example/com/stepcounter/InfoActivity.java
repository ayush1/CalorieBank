package example.com.stepcounter;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.StringTokenizer;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemSelected;
import example.example.Model.UserInfo;
import example.utils.AppConstants;
import example.utils.Utilities;

/**
 * Created by ayushgarg on 17/06/17.
 */

public class InfoActivity extends AppCompatActivity {

    @BindView(R.id.et_age) EditText et_age;
    @BindView(R.id.et_height) EditText et_height;
    @BindView(R.id.et_weight) EditText et_weight;
    @BindView(R.id.tv_BMI_value) TextView tv_BMI;
    @BindView(R.id.tv_BMI_status) TextView tv_BMI_status;
    @BindView(R.id.btn_submit) Button btn_submit;
    @BindView(R.id.rl_BMI) RelativeLayout rl_BMI;
    @BindView(R.id.sp_gender) Spinner sp_gender;

    private UserInfo userInfo;

    @OnClick(R.id.btn_submit) void onClick(){
        if(!TextUtils.isEmpty(et_age.getText().toString()) && !TextUtils.isEmpty(et_height.getText().toString())
                && !TextUtils.isEmpty(et_weight.getText().toString()) && !TextUtils.isEmpty(String.valueOf(BMI))
                && !"Select gender".equalsIgnoreCase(sp_gender.getSelectedItem().toString())){
            userInfo = UserInfo.getInstance();
            userInfo.setAge(Integer.parseInt(et_age.getText().toString()));
            userInfo.setHeight(height);
            userInfo.setWeight(weight);
            userInfo.setBMI(BMI);

        }
        Intent intent = new Intent(InfoActivity.this, SignupLoginActivity.class);
        startActivity(intent);
    }

    @OnItemSelected(R.id.sp_gender) void spinnerItemSlected(){
        String gender = sp_gender.getSelectedItem().toString();
        if(gender.equalsIgnoreCase(String.valueOf(AppConstants.Gender.Male))){
            UserInfo.getInstance().setGender("M");
        }else if(gender.equalsIgnoreCase(String.valueOf(AppConstants.Gender.Female))){
            UserInfo.getInstance().setGender("F");
        }else if(gender.equalsIgnoreCase(String.valueOf(AppConstants.Gender.Other))){
            UserInfo.getInstance().setGender("O");
        }
    }

    Utilities utilities = new Utilities();
    float height;
    String weight;
    float BMI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        ButterKnife.bind(this);

        et_height.addTextChangedListener(heightWatcher);
        et_weight.addTextChangedListener(weightWatcher);

        ArrayAdapter<CharSequence> arrayAdapter = ArrayAdapter.createFromResource(this, R.array.gender,
                android.R.layout.simple_spinner_item);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp_gender.setAdapter(arrayAdapter);

    }

    TextWatcher heightWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            int inch;
            float heightInMeter = 0;
            String rawHeight = editable.toString();
            String[] heightArray = new String[5];
            int i = 0;

            if(rawHeight.length() >= 3 && rawHeight.contains(".")){
                StringTokenizer tokenizer = new StringTokenizer(rawHeight, ".");
                while(tokenizer.hasMoreTokens()){
                    heightArray[i] = tokenizer.nextToken();
                    i++;
                }
                inch = utilities.convertFootToInch(Integer.parseInt(heightArray[0]));
                inch = inch + Integer.parseInt(heightArray[1]);
                heightInMeter = utilities.convertInchToMeter(inch);
            }
            height = heightInMeter;
            if(height != 0 && weight != null){
                calculateBMI(weight);
            }
        }
    };

    TextWatcher weightWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            weight = editable.toString();
            calculateBMI(weight);
        }
    };

    private void calculateBMI(String weight) {
        String BMIStatus = null;
        float rawBMI = 0;
        if(height != 0 && weight.length() >= 2){
            rawBMI = utilities.calculateBMI(height, Float.parseFloat(weight));
        }

        BMI = (float) (Math.round(rawBMI * 100.0) / 100.0);
        if (BMI < 18.5) {
            BMIStatus = "underweight";
        } else if (BMI >= 18.5 && BMI <= 24.99) {
            BMIStatus = "normal";
        } else if (BMI >= 25 && BMI <= 29.99) {
            BMIStatus = "Overweight";
        } else if (BMI >= 30) {
            BMIStatus = "Obese";
        }
        rl_BMI.setVisibility(View.VISIBLE);
        tv_BMI.setText(String.valueOf(BMI));
        tv_BMI_status.setText("(" + BMIStatus + ")");
    }
}
