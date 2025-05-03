package com.example.gustoguru.model.sessionmanager;


import android.content.Context;
import android.content.SharedPreferences;

import com.example.gustoguru.model.pojo.Meal;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class SessionManager {
    private static final String PREF_NAME = "GustoGuruPref";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_USER_EMAIL = "userEmail";
    private static final String KEY_USER_NAME = "userName";
    private static final String KEY_IS_GUEST = "isGuest";

    private static final String KEY_MEAL_OF_THE_DAY = "meal_of_the_day";
    private static final String KEY_MEAL_DATE = "meal_date";



    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private Context context;

    public SessionManager(Context context) {
        this.context = context;
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    public void updateUserName(String newName) {
        editor.putString(KEY_USER_NAME, newName);
        editor.apply();
    }

    public String getUserName() {
        return pref.getString(KEY_USER_NAME, "");
    }

    // Update createLoginSession to include name
    public void createLoginSession(String userId, String email, String name) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_USER_ID, userId);
        editor.putString(KEY_USER_EMAIL, email);
        editor.putString(KEY_USER_NAME, name);
        editor.apply();
    }





    public String getUserId() {
        return pref.getString(KEY_USER_ID, null);
    }



    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public String getUserEmail() {
        return pref.getString(KEY_USER_EMAIL, "No email");
    }

    public void logoutUser() {
        editor.putBoolean(KEY_IS_LOGGED_IN, false);
        editor.putBoolean(KEY_IS_GUEST, false);
        editor.apply();
    }

    public void setGuestMode(boolean isGuest) {
        editor.putBoolean(KEY_IS_GUEST, isGuest);
        editor.apply();
    }

    public boolean isGuest() {
        return pref.getBoolean(KEY_IS_GUEST, false);
    }

    public void saveMealOfTheDay(Meal meal) {
        editor.putString(KEY_MEAL_OF_THE_DAY, new Gson().toJson(meal));
        editor.putString(KEY_MEAL_DATE, getCurrentDate());
        editor.apply();
    }

    public Meal getMealOfTheDay() {
        String savedDate = pref.getString(KEY_MEAL_DATE, "");
        String today = getCurrentDate();

        if (savedDate.equals(today)) {
            String mealJson = pref.getString(KEY_MEAL_OF_THE_DAY, null);
            if (mealJson != null) {
                try {
                    return new Gson().fromJson(mealJson, Meal.class);
                } catch (Exception e) {
                    return null;
                }
            }
        }
        return null;
    }

    private String getCurrentDate() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
    }
}