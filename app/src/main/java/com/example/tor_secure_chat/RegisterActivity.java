package com.example.tor_secure_chat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.example.tor_secure_chat.binding.ClientManager;
import com.example.tor_secure_chat.core.protocol.Protocol;
import com.example.tor_secure_chat.utils.Utils;

public class RegisterActivity extends AppCompatActivity {

    private EditText usernameField;
    private EditText password1Field;
    private EditText password2Field;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        Utils.setCurrentAppContext(this);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        this.usernameField = (EditText) findViewById(R.id.registerUsernameField);
        this.password1Field = (EditText) findViewById(R.id.registerPassword1Field);
        this.password2Field = (EditText) findViewById(R.id.registerPassword2Field);

        String username = getIntent().getStringExtra("username");
        String password = getIntent().getStringExtra("password");
        if (!"".equals(username)) {
            usernameField.setText(username);
        }
        if (!"".equals(password)) {
            password1Field.setText(password);
        }

        if (!ClientManager.isConnected()) {
            Utils.alert("Connection error", "An error has occured. Reload the application");
            return;
        }
    }

    public void register(View view) {
        String username = usernameField.getText().toString();
        String password1 = password1Field.getText().toString();
        String password2 = password2Field.getText().toString();

        if (!password1.equals(password2)) {
            Utils.alert("Password error", "The passwords are not the same", "Sorry");
            return;
        }

        if (password1.isEmpty()) {
            Utils.alert("Password error", "Password can't be empty", "Sorry");
            return;
        }

        if (!Protocol.isUsernameValid(username)) {
            Utils.alert( "Username error", "The username is invalid");
            return;
        }

        startActivity(new Intent(this, ContactsActivity.class));

        /*if (!ClientManager.isConnected()) {
            Utils.alert("Connection error", "An error has occured. Reload the application");
            return;
        }

        Utils.loadingAlert("Loading...", "Contacting server");

        ClientManager.register(username, password1);

        var progressDialog = Utils.loadingAlert("Waiting...", "Contacting server");

        try {
            int code = ClientManager.waitNextCode().take();
            progressDialog.dismiss();
            if (code == Protocol.SUCCESSFUL_LOGIN_CODE) {
                startActivity(new Intent(this, ContactsActivity.class));
            } else if (code == Protocol.USERNAME_ALREADY_EXISTS_ERROR) {
                Utils.alert("Username error", "Username already in use");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/
    }

}