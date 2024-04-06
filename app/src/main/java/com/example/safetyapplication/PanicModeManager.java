package com.example.safetyapplication;
//1
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.Manifest;
import android.telephony.SmsManager;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import java.util.ArrayList;
import java.util.List;

public class PanicModeManager {

    private static final String CHANNEL_ID = "panic_notification_channel";
    private static final int NOTIFICATION_ID = 1;

    public static void triggerPanicMode(Context context) {
        // Send SMS to emergency contacts with current location
        Location currentLocation = getCurrentLocation(context);
        List<String> emergencyContacts = getEmergencyContacts(context);

        for (String contact : emergencyContacts) {
            assert currentLocation != null;
            sendSMS(contact, "Help! I am in danger. My current location: " + currentLocation.getLatitude() + ", " + currentLocation.getLongitude());
        }

        // Show notification
        showNotification(context);
    }

    private static Location getCurrentLocation(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        // Check for permissions
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // Handle the case where permissions are not granted
                return null;
            }

        // Request location updates
        locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                // Do nothing, we only need one location update
            }

            @Override
            public void onProviderEnabled(String provider) {
                // Not needed
            }

            @Override
            public void onProviderDisabled(String provider) {
                // Not needed
            }
        }, null);

        // Get last known location
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (location == null) {
            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }
        return location;
    }

    private static List<String> getEmergencyContacts(Context context) {
        List<String> emergencyContacts = new ArrayList<>();

        // Open database for reading
        SQLiteDatabase database = context.openOrCreateDatabase("EmergencyContacts.db", Context.MODE_PRIVATE, null);

        // Query the database for emergency contacts
        Cursor cursor = database.query("contacts", new String[]{"contact"}, null, null, null, null, null);

        // Check if the cursor has the "contact" column
        int columnIndex = cursor.getColumnIndex("contact");
        if (columnIndex < 0) {
            // Handle the case where the column does not exist
            // For example, log an error or return an empty list
            cursor.close();
            database.close();
            return emergencyContacts;
        }

        // Iterate through the cursor and add contacts to the list
        while (cursor.moveToNext()) {
            String contact = cursor.getString(columnIndex);
            emergencyContacts.add(contact);
        }
        // Close cursor and database
        cursor.close();
        database.close();

        return emergencyContacts;
    }


    private static void sendSMS(String phoneNumber, String message) {
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, message, null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void showNotification(Context context) {
        // Create notification channel (required for Android 8.0 and above)
        createNotificationChannel(context);

        // Build notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Panic Mode Activated")
                .setContentText("Emergency message sent to your contacts.")
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        // Show notification
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        try {
            notificationManager.notify(NOTIFICATION_ID, builder.build());
        }catch (SecurityException E){
            //because it just wont let me run without it
        }
    }

    private static void createNotificationChannel(Context context) {
        CharSequence name = "Panic Notification Channel";
        String description = "Channel for panic mode notifications";
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
        channel.setDescription(description);
        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }
}
