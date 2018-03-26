package com.example.danielgarcia.fieldwiz_monitoring;

import android.location.Address;
import android.location.Geocoder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.example.danielgarcia.fieldwiz_monitoring.DataModel.Stats;

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class InfoStatActivity extends AppCompatActivity {

    private TextView textViewNameSession;
    private TextView textViewLocalization;
    private TextView textViewStartSession;
    private TextView textViewEndSession;
    private TextView textViewDistance;
    private TextView textViewSpeedMax;
    private TextView textViewSpeedAvg;
    private Stats stats;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_stat);
        textViewNameSession = (TextView) findViewById(R.id.textViewNameSession);
        textViewLocalization = (TextView) findViewById(R.id.textViewLocalization);
        textViewStartSession = (TextView) findViewById(R.id.textViewStartSession);
        textViewEndSession = (TextView) findViewById(R.id.textViewEndSession);
        textViewDistance = (TextView) findViewById(R.id.textViewDistance);
        textViewSpeedMax = (TextView) findViewById(R.id.textViewSpeedMax);
        textViewSpeedAvg =(TextView) findViewById(R.id.textViewSpeedAverage);
        stats = (Stats) getIntent().getSerializableExtra("stats");
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        ArrayList<Address> localization = new ArrayList<Address>();
        try {
            localization = (ArrayList<Address>) geocoder.getFromLocation(stats.getLat(),stats.getLon(),1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        textViewNameSession.setText(stats.getNameSession() + ", " + stats.getStartSession().split(" ")[0]);
        textViewLocalization.setText(localization.get(0).getLocality());
        textViewStartSession.setText(stats.getStartSession().split(" ")[1]);
        textViewEndSession.setText(stats.getEndSession().split(" ")[1]);
        textViewDistance.setText(stats.getDist() + " km");
        textViewSpeedMax.setText(stats.getSpeedMax() + " km/h");
        textViewSpeedAvg.setText(stats.getSpeedAvg() + " km/h");
    }
}
