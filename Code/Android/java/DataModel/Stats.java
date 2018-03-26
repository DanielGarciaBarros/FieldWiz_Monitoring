package com.example.danielgarcia.fieldwiz_monitoring.DataModel;


import java.io.Serializable;

/**
 * Created by danielgarcia on 03.05.17.
 */

public class Stats implements Serializable {
    private String nameSession;
    private String startSession;
    private String endSession;
    private double speedMax;
    private double speedAvg;
    private double dist;
    private double lat;
    private double lon;

    public Stats(String nameSession, String startSession, String endSession, double speedMax, double speedAvg, double dist, double lat, double lon) {
        this.nameSession = nameSession;
        this.startSession = startSession;
        this.endSession = endSession;
        this.speedMax = speedMax;
        this.speedAvg = speedAvg;
        this.dist = dist;
        this.lat = lat;
        this.lon = lon;
    }

    public String getNameSession() {
        return nameSession;
    }

    public void setNameSession(String nameSession) {
        this.nameSession = nameSession;
    }

    public String getStartSession() {
        return startSession;
    }

    public void setStartSession(String startSession) {
        this.startSession = startSession;
    }

    public String getEndSession() {
        return endSession;
    }

    public void setEndSession(String endSession) {
        this.endSession = endSession;
    }

    public double getSpeedMax() {
        return speedMax;
    }

    public void setSpeedMax(double speedMax) {
        this.speedMax = speedMax;
    }

    public double getSpeedAvg() {
        return speedAvg;
    }

    public void setSpeedAvg(double speedAvg) {
        this.speedAvg = speedAvg;
    }

    public double getDist() {
        return dist;
    }

    public void setDist(double dist) {
        this.dist = dist;
    }

    public double getLat() { return lat; }

    public void setLat(double lat) { this.lat = lat; }

    public double getLon() { return lon; }

    public void setLon(double lon) { this.lon = lon; }
}