package com.example.rssreaderapplication;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SubscribeActivity extends AppCompatActivity {

    TextView textView;
    EditText editText;

    Button subButton;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscribe);

       editText = (EditText) findViewById(R.id.editTextRSSURL);
       subButton = (Button) findViewById(R.id.cirSubscribeButton);

    }

    public void subscribe(View view)
    {
        String userURL = editText.getText().toString();

        if(TextUtils.isEmpty(userURL))
        {
            Toast.makeText(getApplicationContext(),
                    "Please Enter URL Link",
                    Toast.LENGTH_LONG).show();

            return;
                    }

        URLClass urlClass = new URLClass(userURL);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if(user != null)
        {
            FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
            DatabaseReference databaseReference = firebaseDatabase.getReference();

            databaseReference.child("users").child(user.getUid()).child("rss").push().setValue(urlClass);

            Intent intent = new Intent(SubscribeActivity.this, RSSActivity.class);
            startActivity(intent);

            Toast.makeText(getApplicationContext(),
                    "URL Uploaded",
                    Toast.LENGTH_LONG).show();
        }
    }
}
