package com.example.part2;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private TextView textView;
    private Button buttonContactID;
    private Button buttonDetails;
    private Button buttonCall;
    private static final int Perm_CTC = 1;
    private static final int PICK_CONTACT_REQUEST = 1;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = findViewById(R.id.result_id);
        buttonContactID = findViewById(R.id.contact_id);
        buttonDetails = findViewById(R.id.details);
        buttonCall = findViewById(R.id.call_id);

        buttonDetails.setEnabled(false);
        buttonCall.setEnabled(false);

        buttonContactID.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectContact();
            }
        });

        buttonDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showContactDetails();
            }
        });

        buttonCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeCall();
            }
        });

    }

    private void selectContact() {
        // Check for permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            // Request permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, Perm_CTC);
        } else {
            // Open contact picker
            Intent pickContactIntent = new Intent(Intent.ACTION_PICK, Uri.parse("content://contacts/people"));
            startActivityForResult(pickContactIntent, PICK_CONTACT_REQUEST);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Check permission type using the requestCode
        if (requestCode == Perm_CTC) {
            // The array is empty if not granted
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Open contact picker
                Intent pickContactIntent = new Intent(Intent.ACTION_PICK, Uri.parse("content://contacts/people"));
                startActivityForResult(pickContactIntent, PICK_CONTACT_REQUEST);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_CONTACT_REQUEST && resultCode == RESULT_OK) {

            if (data == null) {
                textView.setText("Operation cancelled.");
            } else {

                Uri contactUri = data.getData();
                textView.setText(contactUri.toString());

                buttonDetails.setEnabled(true);
                buttonCall.setEnabled(true);
            }
        }
    }


    private void showContactDetails() {
        // Get the contact ID from the TextView
        String contactId = textView.getText().toString();
        // Query the contacts database
        Cursor cursor = getContentResolver().query(Uri.parse(contactId), null, null, null, null);
        // Check if a contact was selected
        if (cursor.moveToFirst()) {
            // Get the contact name
            int nameIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
            if (nameIndex >= 0) {
                String name = cursor.getString(nameIndex);
                // Get the contact phone number
                int idIndex = cursor.getColumnIndex(ContactsContract.Contacts._ID);
                if (idIndex >= 0) {
                    String id = cursor.getString(idIndex);
                    Cursor cursorPhone = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER},
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ? AND " +
                                    ContactsContract.CommonDataKinds.Phone.TYPE + " = " +
                                    ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE,
                            new String[]{id},
                            null);
                    String phone = "";
                    if (cursorPhone.moveToFirst()) {
                        int phoneIndex = cursorPhone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                        if (phoneIndex >= 0) {
                            phone = cursorPhone.getString(phoneIndex);
                        }
                    }
                    cursorPhone.close();
                    // Display the contact name and phone number
                    textView.setText(name + ": " + phone);
                }
            }
        }
        cursor.close();
    }

    private void makeCall() {
        // Get the contact phone number from the TextView
        String phoneNumber = textView.getText().toString().split(":")[1];
        // Create the intent to call the number
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:" + phoneNumber));
        // Check for permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED) {
            // Request permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, Perm_CTC);
        } else {
            // Make the call
            startActivity(callIntent);
        }
    }
}





