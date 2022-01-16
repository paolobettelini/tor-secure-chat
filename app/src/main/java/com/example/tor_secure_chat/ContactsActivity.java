package com.example.tor_secure_chat;

import android.os.Bundle;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.example.tor_secure_chat.binding.ClientManager;
import com.example.tor_secure_chat.utils.Utils;


public class ContactsActivity extends AppCompatActivity {

    private EditText newChatField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);
        Utils.setCurrentAppContext(this);

        ClientManager.setOnNewMessage(null);

        this.newChatField = (EditText) findViewById(R.id.newChatField);

        this.newChatField.setOnEditorActionListener((textView, i, keyEvent) -> {
            String username = newChatField.getText().toString();
            newChatField.setText("");

            ContactFragment.newChat(username);

            return false;
        });
    }

}