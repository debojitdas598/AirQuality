package com.example.airquality;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

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
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity  {
    Intent intent;
    String city;
    double longitude,latitude;
    TextView textView;
    RequestQueue requestQueue;
    String formattedDate;
    String server_url = "https://air-quality-api.open-meteo.com/v1/air-quality?latitude=&longitude=&hourly=pm10,pm2_5";
    LineChart lineChart1;
    ArrayList<String> pm10 = new ArrayList<String>();
    ArrayList<String> pm2_5 = new ArrayList<String>();




    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();
        Date currentDate = new Date();
        @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        formattedDate = dateFormat.format(currentDate);
//        textView = findViewById(R.id.tv);
        LocationHelper locationHelper = new LocationHelper(this);
        locationHelper.startLocationUpdates();
        latitude = locationHelper.getLatitude();
        longitude = locationHelper.getLongitude();
        lineChart1 = (LineChart) findViewById(R.id.lineChart);


        rounder(longitude,latitude);
        Toast.makeText(getApplicationContext(),"Lat : "+latitude+"\n"+"Longi : "+longitude,Toast.LENGTH_SHORT).show();
        JSONparsing(latitude,longitude);




    }


    public void JSONparsing(double l1,double l2){
        Cache cache = new DiskBasedCache(getCacheDir(), 1024 * 1024);
        Network network = new BasicNetwork(new HurlStack());
        requestQueue = new RequestQueue(cache, network);
        requestQueue.start();
        String server_url_temp = "https://air-quality-api.open-meteo.com/v1/air-quality?latitude="+latitude+"&longitude="+longitude+"&hourly=pm10,pm2_5&start_date="+formattedDate+"&end_date="+formattedDate;
        StringRequest stringRequest = new StringRequest(Request.Method.GET, server_url_temp, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    JSONObject jsonHourly = jsonResponse.getJSONObject("hourly");
                    JSONArray jsonPM10 = jsonHourly.getJSONArray("pm10");
                    JSONArray jsonPM2_5 = jsonHourly.getJSONArray("pm2_5");
                  Log.d("hio","https://air-quality-api.open-meteo.com/v1/air-quality?latitude="+latitude+"&longitude="+longitude+"&hourly=pm10,pm2_5&start_date="+formattedDate+"&end_date="+formattedDate);
                    for (int i=0;i<24;i++){
                        Log.d("BRO", "onResponse: "+jsonPM10.get(i).toString()+" ");
                        pm10.add(i,jsonPM10.get(i).toString());
                        pm2_5.add(i,jsonPM2_5.get(i).toString());
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Graph1();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), "Something went wrong...", Toast.LENGTH_LONG).show();
                error.printStackTrace();
            }
        });
        requestQueue.add(stringRequest);
    }


    public void Graph1(){


       ArrayList<Entry> entries10 = new ArrayList<>();
        for (int i=0;i<pm10.size();i++){
            Log.d("hyuo",pm10.get(i) + " ");
            float x = i;
            float y = Float.parseFloat(pm10.get(i));
            entries10.add(new Entry(x,y));
        }

        ArrayList<Entry> entries2_5 = new ArrayList<>();
        for (int i=0;i<pm10.size();i++){
            Log.d("hyuo",pm2_5.get(i) + " ");
            float x = i;
            float y = Float.parseFloat(pm2_5.get(i));
            entries2_5.add(new Entry(x,y));
        }



        LineDataSet dataSet10 = new LineDataSet(entries10,"PM 10");
        dataSet10.setColor(Color.BLUE);

        LineDataSet dataSet2_5 = new LineDataSet(entries2_5,"PM 2.5");
        dataSet10.setColor(Color.RED);



        LineData lineData = new LineData();
        lineData.addDataSet(dataSet10);
        lineData.addDataSet(dataSet2_5);
        lineChart1.setData(lineData);
        lineChart1.setTouchEnabled(false);
        lineChart1.setDragEnabled(true);
        lineChart1.setScaleEnabled(true);
        lineChart1.animateX(1500);
        lineChart1.animateY(1500);


        XAxis xAxis = lineChart1.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setAxisMinimum(0);
        xAxis.setAxisMaximum(24);



        YAxis yAxis = lineChart1.getAxisLeft();
        yAxis.setAxisMinimum(0);
        yAxis.setAxisMaximum(100);
        yAxis.setDrawGridLines(false);



        lineChart1.getAxisRight().setEnabled(false);
        lineChart1.invalidate();

    }




    public void rounder(double l1,double l2){
        String longi = String.valueOf(l1);
        String lati = String.valueOf(l2);
        String newlongi = "";
        String newlati = "";
        for(int i=0;i<5;i++){
            newlongi = newlongi + longi.charAt(i);
            newlati = newlati + lati.charAt(i);
        }
        longitude = Double.valueOf(newlongi);
        latitude = Double.valueOf(newlati);
    }





    @RequiresApi(api = Build.VERSION_CODES.O)
    public static String formatDate(LocalDate date) {
        LocalDate presentDate = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String formattedDate = presentDate.format(formatter);
        return formattedDate;
    }

//    private class MyXAxisValueFormatter extends ValueFormatter implements IAxisValueFormatter {
//        private String[] mValues = new String[]{"Jan", "Feb", "Mar", "Apr", "May", "Jun"};
//
//        @Override
//        public String getFormattedValue(float value, AxisBase axis) {
//            return mValues[(int) value % mValues.length];
//        }
//    }

}

