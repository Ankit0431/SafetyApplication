package com.example.safetyapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class First extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first);

        // Find the "Next" button
        Button nextButton = findViewById(R.id.next_button);

        // Set OnClickListener for the "Next" button
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Redirect to MainActivity when the button is clicked
                Intent intent = new Intent(First.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }
}