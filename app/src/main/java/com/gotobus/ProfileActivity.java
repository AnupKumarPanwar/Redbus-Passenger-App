package com.gotobus;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;

public class ProfileActivity extends AppCompatActivity {

    Spinner gender;
    String[] genders;
    ArrayAdapter<String> adapter;

    EditText phoneInput, nameInput, emailInput, ageInput;

    Button signupButton;

    String baseUrl;
    String accessToken;
    SharedPreferences sharedPreferences;

    String phone, name, email, age, type;
    String PREFS_NAME = "MyApp_Settings";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        accessToken = sharedPreferences.getString("access_token", null);

        baseUrl = getResources().getString(R.string.base_url);

        genders = new String[]{"Gender", "Female", "Male", "Others"};
        adapter = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, genders);

        gender = findViewById(R.id.gender);
        gender.setAdapter(adapter);

        phoneInput = findViewById(R.id.phone);
        nameInput = findViewById(R.id.name);
        emailInput = findViewById(R.id.email);
        ageInput = findViewById(R.id.age);
        signupButton = findViewById(R.id.signup);

        getUserProfile();


        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                phone = phoneInput.getText().toString();
                name = nameInput.getText().toString();
                email = emailInput.getText().toString();
                age = ageInput.getText().toString();
                type = gender.getSelectedItem().toString();

                if (!type.equals("Gender")) {

                    AndroidNetworking.post(baseUrl + "/editProfile.php")
                            .setOkHttpClient(NetworkCookies.okHttpClient)
                            .addHeaders("Authorization", accessToken)
                            .addBodyParameter("phone", phone)
                            .addBodyParameter("name", name)
                            .addBodyParameter("email", email)
                            .addBodyParameter("age", age)
                            .addBodyParameter("gender", type)
                            .setPriority(Priority.MEDIUM)
                            .build()
                            .getAsJSONObject(new JSONObjectRequestListener() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    try {
//                                        Toast.makeText(getApplicationContext(), response.toString(), Toast.LENGTH_LONG).show();
                                        JSONObject result = response.getJSONObject("result");
                                        boolean success = Boolean.parseBoolean(result.get("success").toString());
                                        if (success) {
                                            Intent intent = new Intent(getApplicationContext(), OTPActivity.class);
                                            intent.putExtra("phone", phone);
                                            startActivity(intent);
                                        } else {
                                            String message = result.get("message").toString();
                                            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
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
            }
        });

    }

    private void getUserProfile() {
        AndroidNetworking.post(baseUrl + "/getProfile.php")
                .setOkHttpClient(NetworkCookies.okHttpClient)
                .addHeaders("Authorization", accessToken)
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject result = response.getJSONObject("result");
                            boolean success = Boolean.parseBoolean(result.get("success").toString());
                            if (success) {
                                JSONObject data = result.getJSONObject("data");
                                phone = data.get("phone").toString();
                                name = data.get("name").toString();
                                email = data.get("email").toString();
                                age = data.get("age").toString();
                                type = data.get("gender").toString();

                                int pos = 0;
                                for (int i = 0; i < genders.length; i++) {
                                    if (genders[i].equals(type)) {
                                        pos = i;
                                        break;
                                    }
                                }

                                phoneInput.setText(phone);
                                nameInput.setText(name);
                                emailInput.setText(email);
                                ageInput.setText(age);
                                gender.setSelection(pos, true);
                            } else {
                                String message = result.get("message").toString();
                                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
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
}
