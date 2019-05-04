package com.gotobus.utility;

import android.content.Context;
import android.content.SharedPreferences;

import static android.content.Context.MODE_PRIVATE;

public class UserVariables {
    SharedPreferences sharedPreferences;
    String PREFS_NAME = "MyApp_Settings";
    Context context;
    String accessToken;

    public UserVariables(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
    }
}
