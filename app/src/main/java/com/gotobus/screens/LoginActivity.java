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

public class LoginActivity extends AppCompatActivity {

    EditText phoneInput;
    Button loginButton;
    TextView signupButton;

    String phone;
    String baseUrl;

    SharedPreferences sharedPreferences;
    String PREFS_NAME = "MyApp_Settings";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        if (sharedPreferences.getString("access_token", null) != null) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }

        baseUrl = getResources().getString(R.string.base_url);

        AndroidNetworking.initialize(getApplicationContext());

        phoneInput = findViewById(R.id.phone);
        loginButton = findViewById(R.id.login);
        signupButton = findViewById(R.id.signup);

        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
                startActivity(intent);
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                phone = phoneInput.getText().toString();

                AndroidNetworking.post(baseUrl + "/login.php")
                        .setOkHttpClient(NetworkCookies.okHttpClient)
                        .addBodyParameter("phone", phone)
                        .setPriority(Priority.MEDIUM)
                        .build()
                        .getAsJSONObject(new JSONObjectRequestListener() {
                            @Override
                            public void onResponse(JSONObject response) {
                            try {
                                if (ResponseValidator.validate(LoginActivity.this, response)){
                                    Intent intent = new Intent(getApplicationContext(), OTPActivity.class);
                                    intent.putExtra("phone", phone);
                                    startActivity(intent);
                                }
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
