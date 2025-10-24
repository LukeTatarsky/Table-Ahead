package com.example.tableahead;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.os.Bundle;

import com.example.tableahead.search.SelectTypeScreen;
import com.google.android.material.textfield.TextInputEditText;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Register extends AppCompatActivity {

    TextInputEditText editTextEmail, editTextPassword, editTextFirstName, editTextLastName;
    Button button_register;
    FirebaseAuth mAuth;
    ProgressBar progressBar;
    TextView textView;
    FirebaseUser currentUser;

    @Override
    public void onStart() {
        super.onStart();
        currentUser = mAuth.getCurrentUser();
        if(currentUser != null) {
            // Change MainAcitivty to whatever class goes next!!!!!!

            Intent intent = new Intent(getApplicationContext(), SelectTypeScreen.class);
            startActivity(intent);
            finish();
        }else{
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                editTextEmail.setText(extras.get("email").toString());
            }
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        editTextEmail = findViewById(R.id.email);
        editTextPassword = findViewById(R.id.password);
        editTextFirstName = findViewById(R.id.firstName);
        editTextLastName = findViewById(R.id.lastName);
        button_register = findViewById(R.id.register_button);
        mAuth = FirebaseAuth.getInstance();
        progressBar = findViewById(R.id.progressBar);
        textView = findViewById(R.id.loginNow);

        textView.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
        });
        button_register.setOnClickListener(view -> {
            progressBar.setVisibility(View.VISIBLE);
            String email, password, firstName, lastName;
            email = String.valueOf(editTextEmail.getText()).trim();
            password = String.valueOf(editTextPassword.getText()).trim();
            firstName = String.valueOf(editTextFirstName.getText()).trim();
            lastName = String.valueOf(editTextLastName.getText()).trim();

            if (TextUtils.isEmpty(email)) {
                Toast.makeText(Register.this, "Enter email", Toast.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.isEmpty(password)) {
                Toast.makeText(Register.this, "Enter password", Toast.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.isEmpty(firstName)) {
                Toast.makeText(Register.this, "Enter your first name", Toast.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.isEmpty(lastName)) {
                Toast.makeText(Register.this, "Enter your last name", Toast.LENGTH_SHORT).show();
                return;
            }


            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                progressBar.setVisibility(View.GONE);
                if (task.isSuccessful()) {
                    Map<String, Object> user = new HashMap<>();
                    user.put("email", email);
                    user.put("firstName", firstName);
                    user.put("lastName", lastName);
                    user.put("password", password);
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    db.collection("userData").document(mAuth.getCurrentUser().getUid()).set(user);
                    Toast.makeText(Register.this, "Account creation success.", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getApplicationContext(), SelectTypeScreen.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(Register.this, "Account creation failed.", Toast.LENGTH_SHORT).show();
                    Log.w("Register", task.getException());
                }
            });
        });
    }
}
