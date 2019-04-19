package com.gotobus.screens;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.gotobus.utility.NetworkCookies;
import com.gotobus.R;
import com.gotobus.utility.ResponseValidator;

import org.json.JSONException;
import org.json.JSONObject;

public class OTPActivity extends AppCompatActivity {

    EditText otpInput;
    Button verifyButton;

    String otp;

    String baseUrl;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    String PREFS_NAME = "MyApp_Settings";

    String phone;
    TextView resendOtp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);

        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        editor = sharedPreferences.edit();

        baseUrl = getResources().getString(R.string.base_url);

        otpInput = findViewById(R.id.otp);
        verifyButton = findViewById(R.id.verify);
        resendOtp = findViewById(R.id.resend_otp);

        phone = getIntent().getExtras().getString("phone", null);


        verifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                otp = otpInput.getText().toString();

                AndroidNetworking.post(baseUrl + "/verifyOTP.php")
                        .setOkHttpClient(NetworkCookies.okHttpClient)
                        .addBodyParameter("otp", otp)
                        .setPriority(Priority.MEDIUM)
                        .build()
                        .getAsJSONObject(new JSONObjectRequestListener() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    if (ResponseValidator.validate(OTPActivity.this, response)) {
                                        JSONObject result = response.getJSONObject("result");
                                        JSONObject data = result.getJSONObject("data");
                                        String access_token = data.get("access_token").toString();
                                        editor.putString("access_token", access_token);
                                        editor.commit();
                                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                        finish();
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onError(ANError error) {
                                Toast.makeText(getApplicationContext(), error.getErrorBody(), Toast.LENGTH_LONG).show();
                            }
                        });
            }
        });

        resendOtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), "OTP send", Toast.LENGTH_LONG).show();
                resendOtp.setVisibility(View.INVISIBLE);
                AndroidNetworking.post(baseUrl + "/login.php")
                        .setOkHttpClient(NetworkCookies.okHttpClient)
                        .addBodyParameter("phone", phone)
                        .setPriority(Priority.MEDIUM)
                        .build()
                        .getAsJSONObject(new JSONObjectRequestListener() {

                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    ResponseValidator.validate(OTPActivity.this, response);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onError(ANError error) {
                                Toast.makeText(getApplicationContext(), error.getErrorBody(), Toast.LENGTH_LONG).show();
                            }
                        });
            }
        });

    }
}
