package com.example.safetyapplication;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
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

        // Open or create the database
        database = openOrCreateDatabase("EmergencyContacts.db", MODE_PRIVATE, null);

        // Create the contacts table if it doesn't exist
        database.execSQL("CREATE TABLE IF NOT EXISTS contacts (name TEXT, contact TEXT)");

        displayContacts();

        contactsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String item = (String) parent.getItemAtPosition(position);
                String[] parts = item.split(": ");
                if (parts.length == 2) {
                    String name = parts[0].trim();
                    String contact = parts[1].trim();
                    nameEditText.setText(name);
                    contactEditText.setText(contact);
                }
            }
        });

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String contact = contactEditText.getText().toString();
                if (!contact.isEmpty()) {
                    deleteContact(contact);
                    displayContacts();
                    clearInputFields();
                } else {
                    Toast.makeText(EmergencyContactsActivity.this, "Contact cannot be empty", Toast.LENGTH_SHORT).show();
                }
            }
        });

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = nameEditText.getText().toString();
                String contact = contactEditText.getText().toString();
                if (!name.isEmpty() && !contact.isEmpty()) {
                    String oldContact = contactsAdapter.getItem(contactsListView.getSelectedItemPosition());
                    updateContact(name, contact, oldContact);
                    displayContacts();
                    clearInputFields();
                    Toast.makeText(EmergencyContactsActivity.this, "Emergency contact edited", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(EmergencyContactsActivity.this, "Name and Contact cannot be empty", Toast.LENGTH_SHORT).show();
                }
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

//    private void deleteContact(String contact) {
//        database.delete("contacts", "contact = ?", new String[]{contact});
//    }
    private void deleteContact(final String contact) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirmation")
                .setMessage("Are you sure you want to delete this contact?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Delete the contact from the database
                        database.delete("contacts", "contact = ?", new String[]{contact});
                        //Confirmation Toast
                        Toast.makeText(EmergencyContactsActivity.this, "Emergency contact deleted", Toast.LENGTH_SHORT).show();
                        //Update the ListView
                        displayContacts();
                    }
                })
                .setNegativeButton("No", null)
                .show();

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
    }
}
