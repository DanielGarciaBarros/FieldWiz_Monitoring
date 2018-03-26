package com.example.danielgarcia.fieldwiz_monitoring;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.danielgarcia.fieldwiz_monitoring.DataModel.Stats;
import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class UploadActivity extends AppCompatActivity implements View.OnClickListener{

    private EditText editTextNameSession;
    private Button buttonUpload;
    private ListView listViewFile;
    private ListView listViewSession;
    private ProgressBar progressBar;
    private TextView textViewNoSession;
    private File dir_pod = new File("/storage");
    private ArrayList<String> arrayListFiles;
    private Stats[] arrayListSessions;
    private ArrayAdapter<String> arrayFilesAdapter;
    private ArrayAdapter<Stats> arraySessionsAdapter;
    private String selectedFile;
    private String username;
    private String sessions;
    private String selectedStartSession;
    private String selectedEndSession;
    private int selectedItem;
    private boolean fromUpload;
    private Context mContext;

    BroadcastReceiver mUsbAttachReceiver = new BroadcastReceiver() { // lorsqu'on met le câble
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                File listFile[] = dir_pod.listFiles();
                int size = listFile.length;
                while(size == listFile.length){ // afin de laisser le temps de monter le périférique, sinon trop rapide
                    listFile = dir_pod.listFiles();
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                arrayListFiles = new ArrayList<String>();
                walkdir(dir_pod, 0);
                arrayFilesAdapter = new ArrayAdapter<String>(getBaseContext(), android.R.layout.simple_list_item_1, arrayListFiles);
                listViewFile.setAdapter(arrayFilesAdapter);
            }
        }
    };

    BroadcastReceiver mUsbDetachReceiver = new BroadcastReceiver() { // lorsqu'on enlève le câble
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                arrayListFiles = new ArrayList<String>();
                arrayFilesAdapter = new ArrayAdapter<String>(getBaseContext(), android.R.layout.simple_list_item_1, arrayListFiles);
                listViewFile.setAdapter(arrayFilesAdapter);
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);
        mContext = getBaseContext();
        editTextNameSession = (EditText) findViewById(R.id.editTextNameSession);
        listViewFile = (ListView) findViewById(R.id.listViewFiles);
        listViewSession = (ListView) findViewById(R.id.listViewSessions);
        buttonUpload = (Button) findViewById(R.id.buttonUpload);
        progressBar = (ProgressBar) findViewById(R.id.progressBarSession);
        textViewNoSession = (TextView) findViewById(R.id.textViewNoSession);
        arrayListFiles = new ArrayList<String>();
        arrayListSessions = new Stats[]{};
        username = getIntent().getStringExtra("username");
        fromUpload = false;

        IntentFilter filter = new IntentFilter(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        registerReceiver(mUsbAttachReceiver , filter);
        filter = new IntentFilter(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(mUsbDetachReceiver , filter);
        buttonUpload.setOnClickListener(this);
        onItemClick();
        walkdir(dir_pod, 0);
        arrayFilesAdapter = new ArrayAdapter<String>(getBaseContext(), android.R.layout.simple_list_item_1, arrayListFiles);
        listViewFile.setAdapter(arrayFilesAdapter);
    }

    // Méthode appelée lorsqu'on appuie sur le bouton "upload"
    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.buttonUpload:
                OkHttpClient client = new OkHttpClient();
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("file","file",
                                RequestBody.create(MultipartBody.FORM, new File(selectedFile)))
                        .build();
                HashMap<String,String> listInfo = new HashMap<String, String>();
                listInfo.put("hourStartSession", selectedStartSession);
                listInfo.put("hourEndSession", selectedEndSession);
                listInfo.put("nameSession", editTextNameSession.getText().toString());
                String json = new Gson().toJson(listInfo);
                MediaType JSON= MediaType.parse("application/json; charset=utf-8");
                RequestBody body = RequestBody.create(JSON, json);

                Request requestFile = new Request.Builder()
                        .url(getString(R.string.POST_FILE))
                        .post(requestBody)
                        .build();
                Request requestInfoSession = new Request.Builder()
                        .url(getString(R.string.POST_INFO_SESSION))
                        .post(body)
                        .build();

                Stats[] temp = new Stats[arrayListSessions.length-1];
                int j = 0;
                for(int i = 0; i < arrayListSessions.length; i++){
                    if(!arrayListSessions[selectedItem].getStartSession()
                            .equals(arrayListSessions[i].getStartSession()))
                        temp[j++] = arrayListSessions[i];
                }
                arrayListSessions = temp;

                client.newCall(requestFile).enqueue(new okhttp3.Callback() {
                    @Override
                    public void onFailure(okhttp3.Call call, IOException e) {

                        Log.i("Fail", e.getMessage());
                    }

                    @Override
                    public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                        //nameFile = response.body().string(); // On peut n'appeler qu'une seule fois !!
                        Log.i("Response", response.body().string());
                        fromUpload = true;
                        reloadData();
                    }
                });

                client.newCall(requestInfoSession).enqueue(new okhttp3.Callback() {
                    @Override
                    public void onFailure(okhttp3.Call call, IOException e) {

                        Log.i("Fail", e.getMessage());
                    }

                    @Override
                    public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                        Log.i("Response", "Response");
                    }
                });
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mUsbDetachReceiver);
        unregisterReceiver(mUsbAttachReceiver);
    }

    // Permet de lister les fichier .FWZ d'un répertoire
    public void walkdir(File dir, int depth) {
        String pdfPattern = ".FWZ";
        File listFile[] = dir.listFiles();
        if (listFile != null) {
            for (int i = 0; i < listFile.length; i++) {
                if (listFile[i].isDirectory() && depth < 1) {
                    walkdir(listFile[i], depth++);
                } else {
                    if (listFile[i].getName().endsWith(pdfPattern)){
                        arrayListFiles.add(listFile[i].getPath());
                    }
                }
            }
        }
    }

    // Méthode lancée lorsque l'utilisateur appuie Sur la listeView des fichiers
    public void onItemClick() {
        listViewFile.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                showProgressBar();
                selectedFile = arrayListFiles.get(position);
                OkHttpClient client = new OkHttpClient.Builder()
                        .connectTimeout(30, TimeUnit.SECONDS)
                        .readTimeout(30, TimeUnit.SECONDS)
                        .writeTimeout(30, TimeUnit.SECONDS)
                        .build();
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("file","file",
                                RequestBody.create(MultipartBody.FORM, new File(selectedFile)))
                        .build();
                Request requestFile = new Request.Builder()
                        .url(getString(R.string.POST_SESSION))
                        .post(requestBody)
                        .build();
                client.newCall(requestFile).enqueue(new okhttp3.Callback() {
                    @Override
                    public void onFailure(okhttp3.Call call, IOException e) {
                        Log.i("Fail", e.getMessage());
                    }

                    @Override
                    public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                        Gson gson =  new Gson();
                        try(Reader reader = response.body().charStream()){
                            arrayListSessions = gson.fromJson(reader, Stats[].class);
                        }
                        fromUpload = false;
                        reloadData();
                    }
                });
                listViewFile.setSelected(true);
            }
        });
        listViewSession.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedStartSession = arrayListSessions[position].getStartSession();
                selectedEndSession = arrayListSessions[position].getEndSession();
                selectedItem = position;
                listViewFile.setSelected(true);
            }
        });
    }

    // Permet de mettre à jour la listView suite à un changement au niveau des données
    private void reloadData() {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBar.setVisibility(View.INVISIBLE);
                if(arrayListSessions.length == 0)
                    textViewNoSession.setVisibility(View.VISIBLE);
                arraySessionsAdapter = new ArrayAdapter<Stats>(getBaseContext(),
                        android.R.layout.simple_list_item_2,android.R.id.text1, arrayListSessions) {
                    @Override
                    public View getView(int position, View convertView, ViewGroup parent) {
                        View listView = super.getView(position, convertView, parent);
                        TextView text1 = (TextView) listView.findViewById(android.R.id.text1);
                        text1.setText(arrayListSessions[position].getStartSession() + "\n" +
                                arrayListSessions[position].getEndSession());
                        return listView;
                    }
                };
                listViewSession.setAdapter(arraySessionsAdapter);
                if(fromUpload){
                    Toast toast = Toast.makeText(getBaseContext(), "Upload successful", Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        });
    }

    // Permet d'afficher la bar de progression
    private void showProgressBar(){
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textViewNoSession.setVisibility(View.INVISIBLE);
                progressBar.setVisibility(View.VISIBLE);
            }
        });
    }
}