package com.example.rssreaderapplication;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.text.TextUtils;
import android.view.View;

import androidx.cardview.widget.CardView;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.rssreaderapplication.databinding.ActivityMainBinding;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

 EditText email;
 EditText password;
 TextView signIn;

 Button login;
 Button registerButton;

 FirebaseAuth firebaseAuth;
 FirebaseDatabase firebaseDatabase;
 DatabaseReference databaseReference;


 @Override
 protected void onCreate(@Nullable Bundle savedInstanceState) {
  super.onCreate(savedInstanceState);
  setContentView(R.layout.activity_main);

  firebaseAuth = firebaseAuth.getInstance();

  email = (EditText) findViewById(R.id.editTextEmailReg);
  password = (EditText) findViewById(R.id.editTextPasswordReg);
  signIn = (TextView) findViewById(R.id.signIn);

  registerButton = (Button) findViewById(R.id.cirRegButton);
 }


 public void viewLoginClick(View view)
 {
     CardView item = (CardView) findViewById(R.id.login);
     View child = View.inflate(getApplicationContext(), R.layout.layout_login, null);

     item.addView(child);
 }

 public void register(View view)
 {
   String userEmail = email.getText().toString();
   String userPassword = password.getText().toString();

   if(TextUtils.isEmpty(userEmail))
   {
       Toast.makeText(getApplicationContext(),
               "Please Enter E-mail Address",
               Toast.LENGTH_LONG).show();

       return;
   }
   if (TextUtils.isEmpty(userPassword))
   {
       Toast.makeText(getApplicationContext(),
               "Please Enter A Password",
               Toast.LENGTH_LONG).show();

       return;
   }


   firebaseAuth.createUserWithEmailAndPassword(userEmail, userPassword)
           .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
               @Override
               public void onComplete(@NonNull Task<AuthResult> task) {
                 if(task.isSuccessful())
                 {
                   Toast.makeText(MainActivity.this,
                           "User Created",
                           Toast.LENGTH_SHORT).show();

                   startActivity(new Intent(getApplicationContext(), MainActivity.class));

                 }
                 else
                 {
                     Toast.makeText(MainActivity.this,
                             "Error" + task.getException().getMessage(),
                             Toast.LENGTH_SHORT).show();
                 }
               }
           });
 }


 public void login(View view)
 {
     String userEmail = email.getText().toString();
     String userPassword = password.getText().toString();

     if(TextUtils.isEmpty(userEmail))
     {
         Toast.makeText(getApplicationContext(),
                 "Please Enter E-mail Address",
                 Toast.LENGTH_LONG).show();

         return;
     }
     if (TextUtils.isEmpty(userPassword))
     {
         Toast.makeText(getApplicationContext(),
                 "Please Enter A Password",
                 Toast.LENGTH_LONG).show();

         return;
     }


     firebaseAuth.signInWithEmailAndPassword(userEmail, userPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
         @Override
         public void onComplete(@NonNull Task<AuthResult> task) {

             if (task.isSuccessful())
             {
                 Toast.makeText(MainActivity.this, "Welcome " + userEmail, Toast.LENGTH_SHORT).show();
                 startActivity(new Intent(getApplicationContext(), RSSActivity.class));
             }
             else
             {
                 Toast.makeText(MainActivity.this, "Login Error: " +
                         task.getException().getMessage(), Toast.LENGTH_SHORT).show();
             }
         }
     });
 }
}