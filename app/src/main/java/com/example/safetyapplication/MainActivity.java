package com.example.safetyapplication;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Find the "Manage Contacts" button
        Button manageContactsButton = findViewById(R.id.manage_contacts_button);

        // Set OnClickListener for the "Manage Contacts" button
        manageContactsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start the EmergencyContactsActivity when the button is clicked
                startActivity(new Intent(MainActivity.this, EmergencyContactsActivity.class));
            }
        });

        // Find the "Panic Button"
        Button panicButton = findViewById(R.id.panic_button);

        // Set OnClickListener for the "Panic Button"
        panicButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Trigger panic mode
                PanicModeManager.triggerPanicMode(MainActivity.this);
            }
        });
    }
}
