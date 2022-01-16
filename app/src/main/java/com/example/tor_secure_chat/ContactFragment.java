package com.example.tor_secure_chat;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.SimpleAdapter;

import androidx.fragment.app.ListFragment;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ContactFragment extends ListFragment implements AdapterView.OnItemClickListener {

    private static List<Map<String, String>> contactList;
    private static SimpleAdapter adapter;

    public static void newChat(String username) {
        Map<String, String> temp = new HashMap<>();
        temp.put("key1", username);
        contactList.add(temp);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (contactList == null) {
            contactList = new LinkedList<>();

            // Add test data
            Map<String, String> a = new HashMap<>();
            a.put("key1", "Giorgio");

            Map<String, String> b = new HashMap<>();
            b.put("key1", "Marco");

            contactList.add(a);
            contactList.add(b);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState){
        adapter = new SimpleAdapter(
                getContext(),
                contactList,
                android.R.layout.simple_list_item_1,
                new String[]{"key1"},
                new int[]{android.R.id.text1});
        setListAdapter(adapter);

        return inflater.inflate(R.layout.listfragment_contacts, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        getListView().setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        String nome = contactList.get(i).get("key1");

        Intent intent = new Intent(getActivity(), MessageListActivity.class);
        intent.putExtra("receiver", nome);
        startActivity(intent);
    }

}
