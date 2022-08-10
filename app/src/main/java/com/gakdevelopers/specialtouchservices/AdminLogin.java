package com.gakdevelopers.specialtouchservices;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class AdminLogin extends AppCompatActivity {

    EditText editPassword;

    Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_login);

        editPassword = (EditText) findViewById(R.id.editPassword);

        btnLogin = (Button) findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String password = editPassword.getText().toString().trim();

                if (!password.equals("secretadmin")) {
                    editPassword.setError("Incorrect password!");
                    editPassword.requestFocus();
                    return;
                }

                Intent intent = new Intent(AdminLogin.this, MainActivity.class);
                intent.putExtra("admin", "admin");
                startActivity(intent);
            }
        });

    }
}