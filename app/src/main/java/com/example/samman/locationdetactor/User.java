package com.example.samman.locationdetactor;

import android.location.Location;

import com.google.android.gms.fitness.request.ListClaimedBleDevicesRequest;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

/**
 * Created by BinodNepali on 12/14/2016.
 */

public class User {

    //instance variables
    private  String userID;
    private  String  userName;
    private LatLng latlng;
    private  float distance;

    //constructor
    public  User(){

    }

    //properties
    public String getUserID() {
        return userID;
    }
    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getUserName() {return userName;}
    public void setUserName(String userName) {this.userName = userName;}

    public LatLng getLatlng() {return this.latlng;}
    public void setLatlng(LatLng latLng) {
        this.latlng = latLng;
    }

    public float getDistance() {return this.distance;}
    public void setDistance(float distance) {this.distance = distance;}

}
