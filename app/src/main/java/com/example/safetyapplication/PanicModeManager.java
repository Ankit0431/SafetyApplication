package com.example.safetyapplication;
//1
import static androidx.core.content.ContextCompat.getSystemService;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.Manifest;
import android.telephony.SmsManager;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import java.util.ArrayList;
import java.util.List;

public class PanicModeManager extends BroadcastReceiver {

    private static final String CHANNEL_ID = "panic_notification_channel";
    private static final int NOTIFICATION_ID = 1;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    private static final int SMS_PERMISSION_REQUEST_CODE = 101;


    public static void triggerPanicMode(Context context) {
        // Check if location and SMS permissions are granted
        if (checkLocationPermission(context) && checkSMSPermission(context)) {
            // Location and SMS permissions granted, proceed with panic mode
            performPanicMode(context);
        } else {
            // Request location and SMS permissions from the user
            requestPermissions((Activity) context);
        }
    }

    private static boolean checkLocationPermission(Context context) {
        // Check if location permissions are granted
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private static boolean checkSMSPermission(Context context) {
        // Check if SMS permissions are granted
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED;
    }

    private static void requestPermissions(Activity activity) {
        // Create an array of permissions to request
        String[] permissions = {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.SEND_SMS
        };

        // Request location and SMS permissions from the user
        ActivityCompat.requestPermissions(activity, permissions, LOCATION_PERMISSION_REQUEST_CODE);
    }


    private static void performPanicMode(Context context) {
        Location currentLocation = getCurrentLocation(context);
        List<String> emergencyContacts = getEmergencyContacts(context);

        if (currentLocation != null) {
            for (String contact : emergencyContacts) {
                sendSMS(contact, "Help! I am in danger. My current location: " + currentLocation.getLatitude() + ", " + currentLocation.getLongitude());
            }
        } else {
            requestPermissions((Activity) context);
        }
        showNotification(context, emergencyContacts);
    }

    private static Location getCurrentLocation(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        // Check for permissions
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions((Activity) context);
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

    @SuppressLint("MissingPermission")
    public static void showNotification(Context context, List<String> emergencyContacts) {
        // Create notification channel (required for Android 8.0 and above)
        createNotificationChannel(context);

        // Build notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "12")
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Panic Mode Activated")
                .setContentText("Tap to send relieved text to your contacts.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true); // Automatically dismiss the notification when clicked

        // Create an intent to handle the emergency message sending
        Intent sendMessagesIntent = new Intent(context, PanicModeManager.class);
        sendMessagesIntent.setAction("SEND_EMERGENCY_MESSAGES");
        sendMessagesIntent.putStringArrayListExtra("emergencyContacts", (ArrayList<String>) emergencyContacts);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0, sendMessagesIntent, PendingIntent.FLAG_IMMUTABLE);
        builder.setContentIntent(pendingIntent);  // Set the pending intent

        // Show notification
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(123, builder.build());
    }


    public void onReceive(Context context, Intent intent) {
        if (intent != null && "SEND_EMERGENCY_MESSAGES".equals(intent.getAction())) {
            List<String> emergencyContacts = intent.getStringArrayListExtra("emergencyContacts");
            if (emergencyContacts != null) {
                for (String contact : emergencyContacts) {
                    sendSMS(contact, "I am fine now");
                }
            }
        }
    }


    private static void createNotificationChannel(Context context) {
        CharSequence name = "Panic Notification Channel";
        String description = "Channel for panic mode notifications";
        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel channel = new NotificationChannel("12", name, importance);
        channel.setDescription(description);
        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }
}
