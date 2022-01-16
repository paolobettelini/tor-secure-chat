package com.example.tor_secure_chat;

import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.example.tor_secure_chat.binding.ClientManager;
import com.example.tor_secure_chat.core.protocol.Protocol;
import com.example.tor_secure_chat.utils.Utils;

public class LoginActivity extends AppCompatActivity {

    private EditText usernameField;
    private EditText passwordField;

    static {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Utils.setCurrentAppContext(this);

        this.usernameField = (EditText) findViewById(R.id.loginUsernameField);
        this.passwordField = (EditText) findViewById(R.id.loginPasswordField);

        ClientManager.initClient();
    }

    public void login(View view) {
        String username = usernameField.getText().toString();
        String password = passwordField.getText().toString();

        if (!Protocol.isUsernameValid(username)) {
            Utils.alert("Username error", "The username is invalid");
            return;
        }

        if (password.isEmpty()) {
            Utils.alert("Password error", "Password can't be empty", "Sorry");
            return;
        }

        // temp
        startActivity(new Intent(this, ContactsActivity.class));

        /*if (!ClientManager.isConnected()) {
            Utils.alert("Connection error", "An error has occured. Reload the application");
            return;
        }

        startActivity(new Intent(this, ContactsActivity.class));
        var progressDialog = Utils.loadingAlert("Waiting...", "Contacting server");

        ClientManager.login(username, password);

        try {
            int code = ClientManager.waitNextCode().take();
            progressDialog.dismiss();
            if (code == Protocol.SUCCESSFUL_LOGIN_CODE) {
                startActivity(new Intent(this, ContactsActivity.class));
            } else if (code == Protocol.WRONG_PASSWORD_ERROR) {
                Utils.alert("Password error", "Wrong password");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/
    }

    public void register(View view) {
        Intent intent = new Intent(this, RegisterActivity.class);
        intent.putExtra("username", usernameField.getText().toString());
        intent.putExtra("password", passwordField.getText().toString());
        startActivity(intent);
    }

}