package com.example.danielgarcia.fieldwiz_monitoring;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class CreateAccountActivity extends AppCompatActivity implements ImageView.OnTouchListener,
    View.OnClickListener{

    private CircleImageView imageViewAvatar;
    private EditText editTextUsername;
    private EditText editTextPassword;
    private EditText editTextFirstname;
    private EditText editTextName;
    private Button buttonCreate;
    private Bitmap imageBitmap;
    static final int REQUEST_IMAGE_CAPTURE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);
        imageViewAvatar = (CircleImageView) findViewById(R.id.imageViewAvatar);
        editTextUsername = (EditText) findViewById(R.id.editTextCreateAccountUsername);
        editTextPassword = (EditText) findViewById(R.id.editTextCreateAccountPassword);
        editTextFirstname = (EditText) findViewById(R.id.editTextCreateAccountFirstName);
        editTextName = (EditText) findViewById(R.id.editTextCreateAccountName);
        buttonCreate = (Button) findViewById(R.id.buttonCreateAccount);
        imageBitmap = BitmapFactory.decodeResource(getBaseContext().getResources(), R.drawable.profile_photo);
        imageViewAvatar.setOnTouchListener(this);
        buttonCreate.setOnClickListener(this);
    }

    // lancement de l'appareil photo
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (v.getId()){
            case R.id.imageViewAvatar:
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                }
                break;
        }
        return false;
    }

    // Récupération de l'image
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            imageBitmap = (Bitmap) extras.get("data");
            imageViewAvatar.setImageBitmap(imageBitmap);
        }
    }

    // Méthode lancé lorsque l'utilisateur appuie sur le bouton
    @Override
    public void onClick(View v) {
        OkHttpClient client = new OkHttpClient();
        final MediaType MEDIA_TYPE_BMP = MediaType.parse("image/png");
        //http://stackoverflow.com/questions/7769806/convert-bitmap-to-file
        File f = new File(getBaseContext().getCacheDir(), "avatar.png");
        try{
            f.createNewFile();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            imageBitmap.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos);
            byte[] bitmapdata = bos.toByteArray();
            FileOutputStream fos = null;
            fos = new FileOutputStream(f);
            fos.write(bitmapdata);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("img","img",
                        RequestBody.create(MultipartBody.FORM, f))
                .build();
        HashMap<String,String> listInfo = new HashMap<String, String>();
        listInfo.put("username", editTextUsername.getText().toString());
        listInfo.put("password", editTextPassword.getText().toString());
        listInfo.put("firstname", editTextFirstname.getText().toString());
        listInfo.put("name", editTextName.getText().toString());
        String json = new Gson().toJson(listInfo);
        MediaType JSON= MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(JSON, json);

        Request requestAvatar = new Request.Builder()
                .url(getString(R.string.POST_AVATAR))
                .post(requestBody)
                .build();
        Request requestCreateAccount = new Request.Builder()
                .url(getString(R.string.POST_NEW_ACCOUNT))
                .post(body)
                .build();

        client.newCall(requestCreateAccount).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {

                Log.i("Fail", e.getMessage());
            }
            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                Log.i("Response", "Response");
            }
        });

        client.newCall(requestAvatar).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {

                Log.i("Fail", e.getMessage());
            }
            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                Log.i("Response", response.body().string());
            }
        });
        finish();
    }
}
