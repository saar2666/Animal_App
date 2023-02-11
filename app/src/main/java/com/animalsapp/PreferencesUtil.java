package com.animalsapp;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferencesUtil {

//    SharedPreferences Class

    Context mContext;
//  The preferenceutil is the class in which we intialize the shared preference and save the value into it and we get these value when we restart the app
    public PreferencesUtil(Context context) {

        mContext = context;
    }

    public void setString(String key, String value) {
        SharedPreferences.Editor editor = mContext.getSharedPreferences("Pref", Context.MODE_PRIVATE).edit();
        editor.putString(key, value);
        editor.apply();
    }

    public String getString(String key) {
        SharedPreferences editor = mContext.getSharedPreferences("Pref", Context.MODE_PRIVATE);
        return editor.getString(key, "");
    }




}
