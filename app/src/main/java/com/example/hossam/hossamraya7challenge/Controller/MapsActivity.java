package com.example.hossam.hossamraya7challenge.Controller;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.hossam.hossamraya7challenge.Models.Route;
import com.example.hossam.hossamraya7challenge.Network.JSONParser;
import com.example.hossam.hossamraya7challenge.Network.JSONParserInterface;
import com.example.hossam.hossamraya7challenge.R;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;

import java.util.ArrayList;
import java.util.HashMap;

import static com.example.hossam.hossamraya7challenge.R.id.map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, JSONParserInterface, LocationListener {
    Button showMeThePathButton;
    Button useMyLocationButton;
    LatLng sLatLng;
    LatLng dLatLng;
    Marker mPerthMarker;
    LocationManager locationManager;
    Location gpsLocation;
    View imageViewSearch;
    View imageView2Search;
    View imageViewX;
    View imageView2X;
    ProgressBar progressBar;
    String backupCurrent = " ";
    String backupDestination = " ";
    EditText editTextD;
    EditText editTextS;
    Route chosenRoute;
    HashMap<Polyline, Route> routePolylines;
    AlertDialog alert;
    AlertDialog.Builder builder;
    boolean flag;
    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    Boolean flag2;
    private GoogleMap mMap;
    private ArrayList<Marker> currentLocationMarkers = new ArrayList<>();
    private ArrayList<Marker> desiredDestinationMarkers = new ArrayList<>();
    private ArrayList<Polyline> polylinePaths = new ArrayList<>();
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        routePolylines = new HashMap<>();
        showMeThePathButton = (Button) findViewById(R.id.ShowMeThePathButton);
        useMyLocationButton = (Button) findViewById(R.id.TakeMyCar);
        progressBar= (ProgressBar) findViewById(R.id.MapProgressBar);
        progressBar.getIndeterminateDrawable()
                .setColorFilter(ContextCompat.getColor(this, R.color.colorGreen), PorterDuff.Mode.SRC_IN );
        sLatLng = new LatLng(29.9285429, 30.9187827);
        dLatLng = new LatLng(0, 0);
        mPerthMarker = null;
        builder = new AlertDialog.Builder(MapsActivity.this);
        builder.setTitle("Would you Like to Choose This Route ?");

        // Fragment 1 for the Source AutoComplete Search
        // --------------------------------------------------------------------------------
        final PlaceAutocompleteFragment autocompleteFragment1 = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment1);
        ViewGroup viewS = (ViewGroup) autocompleteFragment1.getView();
        editTextS = (EditText) viewS.findViewById(R.id.place_autocomplete_search_input);
        imageViewSearch = (View) viewS.findViewById(R.id.place_autocomplete_search_button);
        imageViewX = (View) viewS.findViewById(R.id.place_autocomplete_clear_button);


        autocompleteFragment1.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                sLatLng = place.getLatLng();
            }

            @Override
            public void onError(Status status) {

            }
        });
        //------------------------------------------------------------------------------------------------
        //2nd Fragment for the Destination Autocomplete Search
        PlaceAutocompleteFragment autocompleteFragment2 = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment2);

        ViewGroup viewD = (ViewGroup) autocompleteFragment2.getView();
        editTextD = (EditText) viewD.findViewById(R.id.place_autocomplete_search_input);
        imageView2Search = (View) viewD.findViewById(R.id.place_autocomplete_search_button);
        imageView2X = (View) viewS.findViewById(R.id.place_autocomplete_clear_button);

        //Shaklyat == For Appearance Purposes
        editTextS.setHint("Enter Your Starting Point");
        editTextD.setHint("Enter Your Destination Point");
        editTextS.setBackgroundColor(0xffffffff);
        editTextD.setBackgroundColor(0xffffffff);
        Drawable drawable = getResources().getDrawable(R.drawable.rounded_edittext);
        editTextS.setBackground(drawable);
        editTextD.setBackground(drawable);
        viewS.removeView(imageViewSearch);
        viewD.removeView(imageView2Search);
        autocompleteFragment2.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                dLatLng = place.getLatLng();
            }

            @Override
            public void onError(Status status) {

            }
        });
        //-----------------------------------------------------------------------------------------
        //Map Fragment Sync
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(map);
        mapFragment.getMapAsync(this);

        //---------------------------------------------------------------------------------------
        //Button Listeners
        useMyLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LocationManager locationManager = (LocationManager)
                        getSystemService(Context.LOCATION_SERVICE);
                if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                gpsLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                LatLng currentLocation = new LatLng(gpsLocation.getLatitude(), gpsLocation.getLongitude());
                sLatLng = currentLocation;
                sendRequest();
                // Add a marker in Sydney and move the camera
            }
        });
      /*  showMeThePathButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendRequest();
            }
        });
        */
        showMeThePathButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendRequest();
            }
        });
        //---------------------------------------------------------------------------------
        //Current Location Manager
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
    }

    //-----------------------------------------------------------------------------------

    //-----------------------------------------------------------------------------------
    //JSON Parser Request Sender
    private void sendRequest() {
        LatLng currentLocation = sLatLng;
        LatLng desiredLocation = dLatLng;

        if (currentLocation == null) {
            Toast.makeText(this, "Please Enter Your Start Destination", Toast.LENGTH_SHORT).show();
            return;
        }
        if (desiredLocation == null) {
            Toast.makeText(this, "Please Enter Your End Destination", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            progressBar.setVisibility(View.VISIBLE);
            new JSONParser(this, currentLocation, desiredLocation).execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //-----------------------------------------------------------------------------------
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        progressBar.setVisibility(View.VISIBLE);

        LocationManager locationManager = (LocationManager)
                getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        gpsLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        LatLng currentLocation = new LatLng(gpsLocation.getLatitude(), gpsLocation.getLongitude());
        sLatLng = currentLocation;
        // Add a marker in Sydney and move the camera
        LatLng location = new LatLng(sLatLng.latitude, sLatLng.longitude);

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 18));
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                flag = true;
                for (final Polyline polyline : polylinePaths) {
                    flag2 = false;
                    if (PolyUtil.isLocationOnPath(latLng, polyline.getPoints(), true, 50) && flag) {
                        chosenRoute = new Route();
                        chosenRoute = routePolylines.get(polyline);
                        builder.setMessage("Estimated Time for Arrival : " + chosenRoute.duration.text + "\n" +
                                "Estimated Distance for Arrival : " + chosenRoute.distance.text)
                                .setCancelable(false)
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        HashMap<Polyline, Route> hashMap = new HashMap<Polyline, Route>(routePolylines);

                                        for (Polyline polyine : hashMap.keySet()) {
                                            if (polyline.equals(polyine) && chosenRoute.equals(hashMap.get(polyine))) {
                                                polyline.setColor(Color.argb(255, 173, 255, 173));
                                                polyine.setWidth(15f);
                                                flag2 = true;
                                            } else if (!chosenRoute.equals(hashMap.get(polyine))) {
                                                polyine.setColor(Color.argb(255,183,158,206));
                                                polyine.setWidth(10f);
                                                flag2 = false;
                                            }
                                        }
                                        flag = false;
                                        alert.dismiss();
                                    }
                                })
                                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                        dialog.dismiss();
                                        alert.dismiss();
                                        chosenRoute = routePolylines.get(polyline);
                                    }
                                });
                        if (flag) {
                            alert = builder.create();
                            alert.show();
                        }
                    }
                    if (flag2) {
                        polyline.setWidth(15f);
                        polyline.setColor(Color.argb(00, 60, 100, 60));
                    } else {
                        polyline.setWidth(10f);
                        polyline.setColor(Color.argb(255,183,158,206));
                    }
                }
            }
        });
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                dLatLng = latLng;
                sendRequest();
            }
        });
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setTrafficEnabled(true);
        mMap.setMyLocationEnabled(true);;
        progressBar.setVisibility(View.INVISIBLE);
    }
    public Bitmap resizeMapIcons(String iconName, int width, int height) {
        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(), getResources().getIdentifier(iconName, "drawable", getPackageName()));
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, width, height, false);
        return resizedBitmap;
    }

    @Override
    public void DirectionDrawer() {
        progressDialog = ProgressDialog.show(this, "Please wait.",
                "Finding direction..!", true);
        if (currentLocationMarkers != null) {
            for (Marker marker : currentLocationMarkers) {
                marker.remove();
            }
        }
        if (desiredDestinationMarkers != null) {
            for (Marker marker : desiredDestinationMarkers) {
                marker.remove();
            }
        }
        if (polylinePaths != null) {
            for (Polyline polyline : polylinePaths) {
                polyline.remove();
            }
        }
    }

    @Override
    public void DirectionDrawerSuccess(ArrayList<Route> route) {
        progressDialog.dismiss();
        polylinePaths = new ArrayList<>();
        currentLocationMarkers = new ArrayList<>();
        desiredDestinationMarkers = new ArrayList<>();
        routePolylines = new HashMap<>();
        int j = 0;
        boolean flagForLocation=true;
        for (Route routes : route) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(routes.startLocation, 16));
            if(flagForLocation){
                currentLocationMarkers.add(mMap.addMarker(new MarkerOptions()
                        .icon(BitmapDescriptorFactory.defaultMarker())
                        .title(routes.startAddress)
                        .position(routes.startLocation)));
                flagForLocation=false;
            }
            desiredDestinationMarkers.add(mMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons("markergreen", 100, 150)))
                    .title(routes.endAddress)
                    .position(routes.endLocation)));
            PolylineOptions polylineOptions = new PolylineOptions().
                    geodesic(true).
                    color(Color.BLUE).
                    width(10);
            for (int i = 0; i < routes.points.size(); i++) {
                polylineOptions.add(routes.points.get(i));
            }
            Polyline polyline = mMap.addPolyline(polylineOptions.width(10f));
            polylinePaths.add(polyline);
            routePolylines.put(polylinePaths.get(j), routes);
            j++;
        }
        progressBar.setVisibility(View.INVISIBLE);
    }

    @Override
    public void Route(String[] StringArray) {
        if (StringArray != null) {
            backupCurrent = StringArray[0];
            backupDestination = StringArray[1];
            editTextS.setText(backupCurrent);
            editTextD.setText(backupDestination);
        }
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

}
