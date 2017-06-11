package com.example.hossam.hossamraya7challenge.Network;

import android.os.AsyncTask;
import android.util.Log;

import com.example.hossam.hossamraya7challenge.Models.Distance;
import com.example.hossam.hossamraya7challenge.Models.Duration1;
import com.example.hossam.hossamraya7challenge.Models.Route;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by hossam on 09/06/17.
 */

public class JSONParser {
    private static final String DIRECTION_URL_API = "https://maps.googleapis.com/maps/api/directions/json?";
    private static final String GOOGLE_API_KEY = "AIzaSyDJ3NfYv6UU6Qc87RO_hQ98Ig8uIVGrpV4";
    com.example.hossam.hossamraya7challenge.Network.JSONParserInterface listener;
    LatLng currentLocation;
    LatLng desiredLocation;

    public JSONParser(com.example.hossam.hossamraya7challenge.Network.JSONParserInterface listener, LatLng currentLocation, LatLng desiredLocation) {
        this.listener = listener;
        this.currentLocation = currentLocation;
        this.desiredLocation = desiredLocation;
    }

    public void execute() throws UnsupportedEncodingException {
        listener.DirectionDrawer();
        new DownloadData().execute(createUrl());
    }

    private String createUrl() throws UnsupportedEncodingException{
        LatLng urlCurrentLocation = currentLocation;
        LatLng urlDesiredLocation = desiredLocation;
        double  urlCurrentLocation1 = urlCurrentLocation.latitude;
        double  urlCurrentLocation2 = urlCurrentLocation.longitude;
        double  urlDesiredLocation1 = urlDesiredLocation.latitude;
        double  urlDesiredLocation2 = urlDesiredLocation.longitude;

        Log.v("Trial",DIRECTION_URL_API + "origin=" + urlCurrentLocation1+","+urlCurrentLocation2 + "&destination=" + urlDesiredLocation1+","+urlDesiredLocation2 + "&key=" + GOOGLE_API_KEY);
        return DIRECTION_URL_API + "origin=" + urlCurrentLocation1+","+urlCurrentLocation2 + "&destination=" + urlDesiredLocation1+","+urlDesiredLocation2 + "&alternatives=true"+"&key=" + GOOGLE_API_KEY;
    }

    private class DownloadData extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String link = params[0];
            try {

                URL url = new URL(link);
                InputStream is = url.openConnection().getInputStream();
                StringBuffer buffer = new StringBuffer();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                return buffer.toString();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String res) {
            try {
                parseJSon(res);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    private void parseJSon(String data) throws JSONException {
            Log.v("Json",data);
        String[] Start_End = new String[2];
        if (data == null)
            return ;
        ArrayList<Route> routes = new ArrayList<Route>();
        JSONObject jsonData = new JSONObject(data);
        JSONArray jsonRoutes = jsonData.getJSONArray("routes");
        for (int i = 0; i < jsonRoutes.length(); i++) {

            JSONObject jsonRoute = jsonRoutes.getJSONObject(i);
            Route route = new Route();

            JSONObject overview_polylineJson = jsonRoute.getJSONObject("overview_polyline");
            JSONArray jsonLegs = jsonRoute.getJSONArray("legs");
            JSONObject jsonLeg = jsonLegs.getJSONObject(0);
            JSONObject jsonDistance = jsonLeg.getJSONObject("distance");
            JSONObject jsonDuration = jsonLeg.getJSONObject("duration");
            JSONObject jsonEndLocation = jsonLeg.getJSONObject("end_location");
            JSONObject jsonStartLocation = jsonLeg.getJSONObject("start_location");

            route.distance = new Distance(jsonDistance.getString("text"), jsonDistance.getInt("value"));
            route.duration = new Duration1(jsonDuration.getString("text"), jsonDuration.getInt("value"));
            route.endAddress = jsonLeg.getString("end_address");
            route.startAddress = jsonLeg.getString("start_address");
            route.startLocation = new LatLng(jsonStartLocation.getDouble("lat"), jsonStartLocation.getDouble("lng"));
            route.endLocation = new LatLng(jsonEndLocation.getDouble("lat"), jsonEndLocation.getDouble("lng"));
            route.points = decodePolyLine(overview_polylineJson.getString("points"));
            routes.add(route);

            if(i==0)
            {
                Start_End[0]=route.startAddress;
            }
            if(i==jsonRoutes.length()-1){
                Start_End[1]=route.endAddress;
            }
        }

        listener.DirectionDrawerSuccess(routes);
        listener.Route(Start_End);
    }


    private ArrayList<LatLng> decodePolyLine(final String poly) {
        int len = poly.length();
        int index = 0;
        ArrayList<LatLng> decoded = new ArrayList<LatLng>();
        int lat = 0;
        int lng = 0;

        while (index < len) {
            int b;
            int shift = 0;
            int result = 0;
            do {
                b = poly.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = poly.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            decoded.add(new LatLng(
                    lat / 100000d, lng / 100000d
            ));
        }

        return decoded;
    }
}
