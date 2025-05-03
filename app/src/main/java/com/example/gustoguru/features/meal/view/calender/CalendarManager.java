package com.example.gustoguru.features.meal.view.calender;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.CalendarContract;

import androidx.core.content.ContextCompat;

import com.example.gustoguru.R;
import com.example.gustoguru.model.pojo.Meal;

import java.util.Calendar;
import java.util.TimeZone;

public class CalendarManager
{
    private final Context context;
    private static final int REQUEST_CALENDAR_PERMISSION = 1001;

    public CalendarManager(Context context)
    {
        this.context = context;
    }

    public void addMealToCalendar(Meal meal, Calendar date, CalendarOperationCallback callback)
    {
        if (meal == null) {
            callback.onFailure("No meal selected");
            return;
        }

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_CALENDAR)
                != PackageManager.PERMISSION_GRANTED) {
            callback.onPermissionRequired(REQUEST_CALENDAR_PERMISSION);
            return;
        }

        try {
            ContentResolver cr = context.getContentResolver();
            ContentValues values = new ContentValues();

            values.put(CalendarContract.Events.DTSTART, date.getTimeInMillis());
            values.put(CalendarContract.Events.DTEND, date.getTimeInMillis() + 3600000);
            values.put(CalendarContract.Events.TITLE, context.getString(R.string.calendar_event_title, meal.getStrMeal()));
            values.put(CalendarContract.Events.DESCRIPTION, meal.getStrInstructions());
            values.put(CalendarContract.Events.CALENDAR_ID, 1);
            values.put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().getID());

            Uri uri = cr.insert(CalendarContract.Events.CONTENT_URI, values);
            if (uri != null) {
                callback.onSuccess();
            } else {
                callback.onFailure("Failed to add to calendar");
            }
        } catch (Exception e) {
            callback.onFailure(e.getMessage());
        }
    }

    public interface CalendarOperationCallback {
        void onSuccess();
        void onFailure(String message);
        void onPermissionRequired(int requestCode);
    }
}