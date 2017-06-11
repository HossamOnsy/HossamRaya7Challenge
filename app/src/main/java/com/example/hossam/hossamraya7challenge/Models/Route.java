package com.example.hossam.hossamraya7challenge.Models;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;


/**
 * Created by hossam on 09/06/17.
 */

public class Route {
    public Distance distance;
    public Duration1 duration;
    public String endAddress;
    public LatLng endLocation;
    public String startAddress;
    public LatLng startLocation;

    public ArrayList<LatLng> points;
}
