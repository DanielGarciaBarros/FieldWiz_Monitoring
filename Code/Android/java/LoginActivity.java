package com.example.danielgarcia.fieldwiz_monitoring;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.danielgarcia.fieldwiz_monitoring.DataModel.Stats;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.HashMap;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText editTextUsername;
    private EditText editTextPassword;
    private Button buttonLogin;
    private Button buttonCreateAccount;
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        editTextUsername = (EditText) findViewById(R.id.editTextUsername);
        editTextPassword = (EditText) findViewById(R.id.editTextPassword);
        buttonLogin = (Button) findViewById(R.id.buttonLogin);
        buttonCreateAccount = (Button) findViewById(R.id.buttonCreate);

        buttonCreateAccount.setOnClickListener(this);
        buttonLogin.setOnClickListener(this);
    }

    // gestion des événement des boutons
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.buttonLogin:
                intent = new Intent(this, MainActivity.class);
                OkHttpClient client = new OkHttpClient();
                HashMap<String,String> listInfo = new HashMap<String, String>();
                listInfo.put("username", editTextUsername.getText().toString());
                listInfo.put("password", editTextPassword.getText().toString());
                String json = new Gson().toJson(listInfo);
                MediaType JSON= MediaType.parse("application/json; charset=utf-8");
                RequestBody body = RequestBody.create(JSON, json);
                Request request = new Request.Builder()
                        .url(getString(R.string.POST_USER))
                        .post(body)
                        .build();
                client.newCall(request).enqueue(new okhttp3.Callback() {
                    @Override
                    public void onFailure(okhttp3.Call call, IOException e) {
                        Log.i("Fail", e.getMessage());
                    }
                    @Override
                    public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                        Log.i("Response", "Response");
                        boolean isConnected = Boolean.parseBoolean(response.body().string());
                        if(isConnected)
                            startMainActivity();
                        else{
                            showErrorMessage();
                        }
                    }
                });
                break;
            case R.id.buttonCreate:
                intent = new Intent(this, CreateAccountActivity.class);
                startActivity(intent);
                break;
        }

    }

    // lance la MainActivity
    private void startMainActivity() {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                intent.putExtra("username", editTextUsername.getText().toString());
                startActivity(intent);
            }
        });
    }

    // Affiche message d'erreur
    private void showErrorMessage(){
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder alertDialogError = new AlertDialog.Builder(LoginActivity.this);
                alertDialogError.setTitle("Incorrect login");
                alertDialogError.setMessage("The username or password is incorrect.");
                alertDialogError.setPositiveButton("Ok",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {}
                        });
                alertDialogError.show();
            }
        });
    }
}
