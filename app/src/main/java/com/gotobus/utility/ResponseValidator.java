package com.gotobus.utility;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.gotobus.screens.LoginActivity;

import org.json.JSONObject;

import es.dmoral.toasty.Toasty;

import static android.content.Context.MODE_PRIVATE;

public class ResponseValidator {
    public static boolean validate(final Activity activity, JSONObject response) {
        SharedPreferences sharedPreferences;
        String PREFS_NAME = "MyApp_Settings";
        String accessToken;
        SharedPreferences.Editor editor;
        try {
            sharedPreferences = activity.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            editor = sharedPreferences.edit();
            accessToken = sharedPreferences.getString("access_token", null);
            JSONObject result = response.getJSONObject("result");
            boolean success = Boolean.parseBoolean(result.get("success").toString());
            if (!success) {
                String message = result.get("message").toString();
                if (message.equals("Invalid access token.")) {
                    editor.putString("access_token", null);
                    editor.apply();
                    final Intent intent = new Intent(activity.getApplicationContext(), LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                    Toasty.error(activity, "Session expired. Please login again!", Toast.LENGTH_SHORT, true).show();
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);
                    alertDialogBuilder.setCancelable(false);
                    alertDialogBuilder.setMessage("Session expired. Please login again!");
                    alertDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            activity.startActivity(intent);
                            activity.finish();
                        }
                    });

                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();
                    return false;
                } else {
                    Toasty.error(activity, message, Toast.LENGTH_SHORT, true).show();
                    return false;
                }
            } else {
                return true;
            }
        } catch (Exception e) {
            Toast.makeText(activity, e.getMessage(), Toast.LENGTH_LONG).show();
            return false;
        }
    }
}
