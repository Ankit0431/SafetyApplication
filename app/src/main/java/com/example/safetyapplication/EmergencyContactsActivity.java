package com.example.safetyapplication;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;

public class EmergencyContactsActivity extends AppCompatActivity {

    private EditText nameEditText;
    private EditText contactEditText;
    private Button addButton;
    private Button deleteButton;
    private Button editButton;
    private ListView contactsListView;

    private SQLiteDatabase database;
    private ArrayAdapter<String> contactsAdapter;

    // Variables to hold the currently selected contact's original name and contact for editing
    private String selectedContactName = null;
    private String selectedContactNumber = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_contacts);

        nameEditText = findViewById(R.id.nameEditText);
        contactEditText = findViewById(R.id.contactEditText);
        addButton = findViewById(R.id.addButton);
        deleteButton = findViewById(R.id.deleteButton);
        editButton = findViewById(R.id.editButton);
        contactsListView = findViewById(R.id.emergency_contacts_list);

        database = openOrCreateDatabase("EmergencyContacts.db", MODE_PRIVATE, null);
        database.execSQL("CREATE TABLE IF NOT EXISTS contacts (name TEXT, contact TEXT)");

        displayContacts();

        contactsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String item = (String) parent.getItemAtPosition(position);
                String[] parts = item.split(": ");
                if (parts.length == 2) {
                    selectedContactName = parts[0].trim();
                    selectedContactNumber = parts[1].trim();
                    nameEditText.setText(selectedContactName);
                    contactEditText.setText(selectedContactNumber);
                }
            }
        });

        addButton.setOnClickListener(v -> {
            String name = nameEditText.getText().toString();
            String contact = contactEditText.getText().toString();
            if (!name.isEmpty() && !contact.isEmpty()) {
                insertContact(name, contact);
                displayContacts();
                clearInputFields();
                Toast.makeText(EmergencyContactsActivity.this, "Emergency contact added", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(EmergencyContactsActivity.this, "Name and Contact cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });

        deleteButton.setOnClickListener(v -> {
            if (!selectedContactNumber.isEmpty()) {
                deleteContact(selectedContactNumber);
                displayContacts();
                clearInputFields();
                Toast.makeText(EmergencyContactsActivity.this, "Emergency contact deleted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(EmergencyContactsActivity.this, "Select a contact to delete", Toast.LENGTH_SHORT).show();
            }
        });

        editButton.setOnClickListener(v -> {
            String newName = nameEditText.getText().toString();
            String newContact = contactEditText.getText().toString();
            if (!newName.isEmpty() && !newContact.isEmpty()) {
                if (selectedContactName != null && selectedContactNumber != null) {
                    updateContact(newName, newContact, selectedContactNumber);
                    displayContacts();
                    clearInputFields();
                    Toast.makeText(EmergencyContactsActivity.this, "Emergency contact edited", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(EmergencyContactsActivity.this, "Please select a contact to edit", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(EmergencyContactsActivity.this, "Name and Contact cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayContacts() {
        ArrayList<String> contactsList = new ArrayList<>();
        Cursor cursor = database.rawQuery("SELECT * FROM contacts", null);
        if (cursor.moveToFirst()) {
            do {
                String name = cursor.getString(0);
                String contact = cursor.getString(1);
                contactsList.add(name + ": " + contact);
            } while (cursor.moveToNext());
        }
        cursor.close();
        contactsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, contactsList);
        contactsListView.setAdapter(contactsAdapter);
    }

    private void insertContact(String name, String contact) {
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("contact", contact);
        database.insert("contacts", null, values);
    }

    private void deleteContact(String contact) {
        database.delete("contacts", "contact = ?", new String[]{contact});
    }

    private void updateContact(String name, String newContact, String oldContact) {
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("contact", newContact);
        database.update("contacts", values, "contact = ?", new String[]{oldContact});
    }

    private void clearInputFields() {
        nameEditText.setText("");
        contactEditText.setText("");
        selectedContactName = null;
        selectedContactNumber = null; // Clear the selected contact info after editing
    }
}
