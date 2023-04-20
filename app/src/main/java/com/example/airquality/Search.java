package com.example.airquality;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import eightbitlab.com.blurview.BlurView;
import eightbitlab.com.blurview.RenderScriptBlur;

public class Search extends AppCompatActivity {
    Button search;
    RequestQueue requestQueue;
    TextView location,github,insta;
    double latitude, longitude;
    Intent intent;
    String server_url = "https://geocoding-api.open-meteo.com/v1/search?name=";
    FusedLocationProviderClient fusedLocationClient;
    LocationRequest locationRequest;
    AutoCompleteTextView cityAutocompleteTextView;
    BlurView blurView1;
    CardView progressbar;
    private static final int REQUEST_LOCATION_PERMISSION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        getSupportActionBar().hide();
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
         cityAutocompleteTextView = findViewById(R.id.searchbar);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, getResources().getStringArray(R.array.india_top_places));
        cityAutocompleteTextView.setAdapter(adapter);

        progressbar = findViewById(R.id.progressbar);

        blurview();
        aboutLink();

        search = findViewById(R.id.search);
        search.setPaintFlags(search.getPaintFlags() |   Paint.UNDERLINE_TEXT_FLAG);
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                     searcher();
            }
        });

        location = findViewById(R.id.location);
        location.setPaintFlags(location.getPaintFlags() |   Paint.UNDERLINE_TEXT_FLAG);
        location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestlocation();
                createLocationRequest();

                if(latitude == 12345 && longitude ==12345){
                    Toast.makeText(getApplicationContext(),"Error 69 : Cant fetch data",Toast.LENGTH_SHORT ).show();
                }
                else{
                    intent = new Intent(Search.this, MainActivity.class);
                    intent.putExtra("longitudunal val", longitude);
                    intent.putExtra("latitudunal val", latitude);
                    startActivity(intent);

                }
            }
        });
    }

    //search city
    public void searcher() {
        Cache cache = new DiskBasedCache(getCacheDir(), 1024 * 1024);
        Network network = new BasicNetwork(new HurlStack());
        requestQueue = new RequestQueue(cache, network);
        requestQueue.start();
        search = findViewById(R.id.search);
        location = findViewById(R.id.location);
                if (cityAutocompleteTextView.getText().toString().trim().isEmpty() == true) {
                    Toast.makeText(getApplicationContext(), "Enter a city name", Toast.LENGTH_LONG).show();
                } else {
                    progressbar.setVisibility(View.VISIBLE);
                    String server_url_temp = server_url + cityAutocompleteTextView.getText().toString().trim();
                    StringRequest stringRequest = new StringRequest(Request.Method.GET, server_url_temp, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            progressbar.setVisibility(View.INVISIBLE);
                            try {
                                JSONObject jsonResponse = new JSONObject(response);
                                JSONArray jsonArray = jsonResponse.getJSONArray("results");
                                JSONObject firstObject = jsonArray.getJSONObject(0);
                                latitude = firstObject.getDouble("latitude");
                                longitude = firstObject.getDouble("longitude");
                                Toast.makeText(getApplicationContext(), String.valueOf(latitude) + " " + String.valueOf(longitude), Toast.LENGTH_LONG).show();

                                Intent intent1 = new Intent(Search.this, CustomCity.class);
                                Log.d("dbharry", "onCreate: "+longitude);
                                intent1.putExtra("longitudunal val", longitude);
                                intent1.putExtra("latitudunal val", latitude);
                                String s = cityAutocompleteTextView.getText().toString();
                                intent1.putExtra("city name", s);

                                startActivity(intent1);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            progressbar.setVisibility(View.INVISIBLE);
                            Toast.makeText(getApplicationContext(), "Something went wrong...", Toast.LENGTH_LONG).show();
                            error.printStackTrace();
                        }
                    });
                    requestQueue.add(stringRequest);
                }

    }

    //gps location lat and long

    public void requestlocation() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)) {
            Toast.makeText(this, "Please grant location permission to use this app",
                    Toast.LENGTH_SHORT).show();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT);
                finish();
            }
        }
    }

    private void createLocationRequest() {
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void startLocationUpdates() {

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if (locationResult != null) {
                    Location location = locationResult.getLastLocation();
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                    Toast.makeText(getApplicationContext(),String.valueOf(longitude)+" "+String.valueOf(latitude),Toast.LENGTH_SHORT).show();
                    fusedLocationClient.removeLocationUpdates(this);
                }
            }
        }, null);
    }

    public void blurview(){

        float radius = 10f;
        View decorView = getWindow().getDecorView();
        ViewGroup rootView = decorView.findViewById(android.R.id.content);
        blurView1 = findViewById(R.id.blurview1);
        Drawable windowBackground = decorView.getBackground();
        blurView1.setupWith(rootView, new RenderScriptBlur(this)).setFrameClearDrawable(windowBackground).setBlurRadius(radius);


    }
    private void aboutLink(){
        insta = findViewById(R.id.insta);
        insta.setPaintFlags(insta.getPaintFlags() |   Paint.UNDERLINE_TEXT_FLAG);
        github = findViewById(R.id.github);
        github.setPaintFlags(github.getPaintFlags() |   Paint.UNDERLINE_TEXT_FLAG);

        insta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle the click event here, for example:
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.instagram.com/debo_is_here_/"));
                startActivity(browserIntent);
            }
        });
        github.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle the click event here, for example:
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/debojitdas598"));
                startActivity(browserIntent);
            }
        });


    }



}
