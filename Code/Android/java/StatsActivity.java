package com.example.danielgarcia.fieldwiz_monitoring;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.danielgarcia.fieldwiz_monitoring.DataModel.Stats;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class StatsActivity extends AppCompatActivity implements ListView.OnItemClickListener{

    private ListView listViewStats;
    private String username;
    ArrayAdapter<Stats> adapter;
    private Stats[] tabStats;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        listViewStats = (ListView) findViewById(R.id.listViewStats);
        listViewStats.setOnItemClickListener(this);
        username = getIntent().getStringExtra("username");
        tabStats = new Stats[]{};
        reloadData();

        OkHttpClient client = new OkHttpClient();
        HashMap<String,String> listInfo = new HashMap<String, String>();
        listInfo.put("username", username);
        String json = new Gson().toJson(listInfo);
        MediaType JSON= MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(JSON, json);
        Request requestGetStats = new Request.Builder()
                .url(getString(R.string.POST_STATS)+ "/" + username)
                .get()
                .build();

        client.newCall(requestGetStats).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {

                Log.i("Fail", e.getMessage());
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                //String stringStats = response.body().string(); // On peut n'appeler qu'une seule fois !!
                Gson gson =  new Gson();
                try(Reader reader = response.body().charStream()){
                    tabStats = gson.fromJson(reader, Stats[].class);
                }
                reloadData();
            }
        });
    }

    // Permet de mettre à jour la listView suite à un changement au niveau des données
    public void reloadData() {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter = new ArrayAdapter<Stats>(getBaseContext(),
                        android.R.layout.simple_list_item_2,android.R.id.text1, tabStats) {
                    @Override
                    public View getView(int position, View convertView, ViewGroup parent) {
                        View listView = super.getView(position, convertView, parent);
                        TextView text1 = (TextView) listView.findViewById(android.R.id.text1);
                        TextView text2 = (TextView) listView.findViewById(android.R.id.text2);
                        text1.setText(tabStats[position].getNameSession());
                        text2.setText(tabStats[position].getStartSession() + " | "
                                + tabStats[position].getEndSession());
                        return listView;
                    }
                };
                listViewStats.setAdapter(adapter);
            }
        });
    }

    // Gère lorsque l'utilisateur appuie sur une des session de la liste
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(this,InfoStatActivity.class);
        intent.putExtra("stats", tabStats[position]);
        startActivity(intent);
    }
}