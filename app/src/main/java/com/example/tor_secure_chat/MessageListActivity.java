package com.example.tor_secure_chat;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tor_secure_chat.binding.ClientManager;
import com.example.tor_secure_chat.binding.Message;
import com.example.tor_secure_chat.utils.Utils;

import java.util.List;

public class MessageListActivity extends AppCompatActivity {

    private RecyclerView mMessageRecycler;
    private static MessageListAdapter mMessageAdapter;
    private String receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_list);
        Utils.setCurrentAppContext(this);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        this.receiver = intent.getStringExtra("receiver");

        List<Message> messageList = ClientManager.getMessagesFor(receiver);

        mMessageRecycler = (RecyclerView) findViewById(R.id.recycler_gchat);
        mMessageAdapter = new MessageListAdapter(this, messageList);
        mMessageRecycler.setLayoutManager(new LinearLayoutManager(this));
        mMessageRecycler.setAdapter(mMessageAdapter);

        EditText sendMessageField = (EditText) findViewById(R.id.edit_gchat_message);

        sendMessageField.setOnEditorActionListener((textView, i, keyEvent) -> {
            String message = sendMessageField.getText().toString();

            if (message.isEmpty()) {
                return false;
            }

            ClientManager.sendMessage(receiver, message);
            mMessageAdapter.notifyItemInserted(messageList.size() - 1);
            sendMessageField.setText("");
            mMessageRecycler.smoothScrollToPosition(messageList.size() - 1);

            return false;
        });

        ClientManager.setOnNewMessage(() -> mMessageAdapter.notifyItemInserted(messageList.size() - 1));
    }

}