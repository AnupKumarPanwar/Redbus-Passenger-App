package com.gotobus.screens;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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
import com.gotobus.R;
import com.gotobus.utility.NetworkCookies;
import com.gotobus.utility.ResponseValidator;

import org.json.JSONObject;

public class SignupActivity extends AppCompatActivity {

    private Spinner gender;
    private String[] genders;
    private ArrayAdapter<String> adapter;

    private EditText phoneInput;
    private EditText nameInput;
    private EditText emailInput;
    private EditText ageInput;

    private Button signupButton;

    private String baseUrl;

    private String phone;
    private String name;
    private String email;
    private String age;
    private String type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

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


        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                phone = phoneInput.getText().toString();
                name = nameInput.getText().toString();
                email = emailInput.getText().toString();
                age = ageInput.getText().toString();
                type = gender.getSelectedItem().toString();

                phone = phoneInput.getText().toString();
                if (phone.contains("+91")) {
                    phone = phone.replace("+91", "");
                }

                if (!type.equals("Gender")) {

                    AndroidNetworking.post(baseUrl + "/signup.php")
                            .setOkHttpClient(NetworkCookies.okHttpClient)
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
                                        if (ResponseValidator.validate(SignupActivity.this, response)) {
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
            }
        });

    }
}
