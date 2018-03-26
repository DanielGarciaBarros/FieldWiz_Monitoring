package com.example.danielgarcia.fieldwiz_monitoring;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.example.danielgarcia.fieldwiz_monitoring.DataModel.Person;
import com.example.danielgarcia.fieldwiz_monitoring.DataModel.Stats;
import com.google.gson.Gson;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private Button buttonStats;
    private Button buttonUpload;
    private TextView textViewFirstnameName;
    private CircleImageView imageViewAvatar;
    private String username;
    private Person[] tabPerson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        buttonStats = (Button) findViewById(R.id.buttonStat);
        buttonUpload = (Button) findViewById(R.id.buttonUpload);
        textViewFirstnameName = (TextView) findViewById(R.id.textViewFirstnameName);
        imageViewAvatar = (CircleImageView) findViewById(R.id.imageViewAvatarUser);
        username = getIntent().getStringExtra("username");

        HashMap<String,String> listInfo = new HashMap<String, String>();
        listInfo.put("username", username);
        String json = new Gson().toJson(listInfo);
        MediaType JSON= MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(JSON, json);
        OkHttpClient client = new OkHttpClient();

        // récupération de l'avatar
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    URL url = new URL(getString(R.string.GET_AVATAR_USER) + "/" + username);
                    final Bitmap bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                    imageViewAvatar.post(new Runnable() {
                        @Override
                        public void run() {
                            imageViewAvatar.setImageBitmap(bmp);
                        }
                    });
                }catch (Exception e){
                    Log.e("error", e.toString());
                }
            }
        }).start();

        Request requestGetStats = new Request.Builder()
                .url(getString(R.string.GET_INFO_USER) + "/" + username)
                .get()
                .build();

        client.newCall(requestGetStats).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {

                Log.i("Fail", e.getMessage());
            }

            @Override
            public void onResponse(okhttp3.Call call, final okhttp3.Response response) throws IOException {
                //String stringStats = response.body().string(); // On peut n'appeler qu'une seule fois !!
                Gson gson =  new Gson();
                try(Reader reader = response.body().charStream()){
                    tabPerson = gson.fromJson(reader, Person[].class);
                }
                updateView();
            }
        });

        buttonStats.setOnClickListener(this);
        buttonUpload.setOnClickListener(this);
    }

    // lorsque l'utilisateur appuie sur le bouton "back"
    @Override
    public void onBackPressed() {
        showDisconect();
    }

    // création du menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_menu, menu);
        return true;
    }

    // lorsque l'utilisateur appuie sur un des "item" du menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_disconnect:
                showDisconect();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    // gestion des boutons de l'activité
    @Override
    public void onClick(View v) {
        Intent intent;
        switch(v.getId()){
            case R.id.buttonStat:
                intent = new Intent(this,StatsActivity.class);
                intent.putExtra("username", username);
                startActivity(intent);
                break;
            case R.id.buttonUpload:
                intent = new Intent(this,UploadActivity.class);
                intent.putExtra("username", username);
                startActivity(intent);
                break;
        }
    }

    // affiche le message de déconnexion
    private void showDisconect(){
        AlertDialog.Builder alertDialogInfo = new AlertDialog.Builder(this);
        alertDialogInfo.setTitle("Disconnect");
        alertDialogInfo.setMessage("Do you want to disconnect?");
        alertDialogInfo.setPositiveButton("Yes",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
        alertDialogInfo.setNegativeButton("No",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
        alertDialogInfo.show();
    }

    // met à jour l'affichage (le nom et le prénom de l'utilisateur)
    private void updateView(){
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textViewFirstnameName.setText(tabPerson[0].getFirstname() + " " + tabPerson[0].getName());
            }
        });
    }
}