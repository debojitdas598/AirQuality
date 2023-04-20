package com.example.airquality;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import eightbitlab.com.blurview.BlurView;
import eightbitlab.com.blurview.RenderScriptBlur;

public class CustomCity extends AppCompatActivity {
    double longitude,latitude;
    RequestQueue requestQueue,requestQueue2;
    String formattedDate, longdate, dateString2;
    String city;
    ArrayList<String> pm10 = new ArrayList<String>();
    ArrayList<String> pm2_5 = new ArrayList<String>();
    ArrayList<String> aqi = new ArrayList<String>();
    ArrayList<String> co = new ArrayList<String>();
    LineChart lineChart1;
    double aqiavg,pm10avg,pm2_5avg,coavg;
    TextView cityname,datetext,aqitext,pm10text,pm2_5text,cotext,today,tomorrow,tendays;
    ImageView rating;
    BlurView blurView1,blurView2,blurView3,blurView4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_city);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        getSupportActionBar().hide();

        initializer();
        dateManager();

        longitude = getIntent().getDoubleExtra("longitudunal val", 0);
        latitude = getIntent().getDoubleExtra("latitudunal val", 0);
        rounder(longitude,latitude);
        city = getIntent().getStringExtra("city name");

        JSONparsing(longitude,latitude);
        bottomnav();
        blurview();
        backgroundImageParsing(city);
        cityname.setText(city.substring(0,1).toUpperCase()+city.substring(1).toLowerCase()+"'s AQI" );
        datetext.setText(longdate);


    }
    private void initializer(){
        rating = findViewById(R.id.rating);
        pm10text = findViewById(R.id.pm10);
        pm2_5text = findViewById(R.id.pm2_5);
        cotext = findViewById(R.id.co);
        today = findViewById(R.id.today);
        tomorrow = findViewById(R.id.tomorrow);
        tendays = findViewById(R.id.tendays);
        cityname = findViewById(R.id.cityname);
        aqitext = findViewById(R.id.aqi);
        datetext = findViewById(R.id.date);

        lineChart1 = findViewById(R.id.lineChart);
        lineChart1.setNoDataText("Loading data...");
    }


    public void JSONparsing(double l1,double l2){
        Cache cache = new DiskBasedCache(getCacheDir(), 1024 * 1024);
        Network network = new BasicNetwork(new HurlStack());
        requestQueue = new RequestQueue(cache, network);
        requestQueue.start();
        String server_url_temp = "https://air-quality-api.open-meteo.com/v1/air-quality?latitude="+latitude+"&longitude="+longitude+"&hourly=pm10,pm2_5,carbon_monoxide,us_aqi&start_date="+formattedDate+"&end_date="+dateString2;
        StringRequest stringRequest = new StringRequest(Request.Method.GET, server_url_temp, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    JSONObject jsonHourly = jsonResponse.getJSONObject("hourly");
                    JSONArray jsonPM10 = jsonHourly.getJSONArray("pm10");
                    JSONArray jsonPM2_5 = jsonHourly.getJSONArray("pm2_5");
                    JSONArray jsonaqi = jsonHourly.getJSONArray("us_aqi");
                    JSONArray jsonco = jsonHourly.getJSONArray("carbon_monoxide");
                    Log.d("hio","https://air-quality-api.open-meteo.com/v1/air-quality?latitude="+latitude+"&longitude="+longitude+"&hourly=pm10,pm2_5,carbon_monoxide,us_aqi&start_date="+formattedDate+"&end_date="+dateString2);
                    for (int i=0;i<jsonPM10.length();i++){
                        Log.d("BRO", "onResponse: "+jsonPM10.get(i).toString()+" ");
                        pm10.add(i,jsonPM10.get(i).toString());
                        pm2_5.add(i,jsonPM2_5.get(i).toString());
                        aqi.add(i,jsonaqi.get(i).toString());
                        co.add(i,jsonco.get(i).toString());
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

    public void backgroundImageParsing(String city){
        requestQueue2 = Volley.newRequestQueue(getApplicationContext());
        String url = "https://source.unsplash.com/720x1200/?+city=" + city;
        ImageRequest imageRequest = new ImageRequest(url, new Response.Listener<Bitmap>() {
            @Override
            public void onResponse(Bitmap response) {
                ImageView imageView =findViewById(R.id.bgimage);
                imageView.setImageBitmap(response);
            }
        },0,0,null,null);
        requestQueue2.add(imageRequest);

    }


    public void Graph1(){
        double sumpm2_5=0,sumpm10=0,sumaqi=0,sumco=0;
        ArrayList<Entry> entries10 = new ArrayList<>();
        for (int i=0;i<24;i++){
            Log.d("hyuo",pm10.get(i) + " ");
            float x = i;
            float y = Float.parseFloat(pm10.get(i));
            sumpm10 = sumpm10 +y;
            entries10.add(new Entry(x,y));
        }
        pm10avg = sumpm10/24;

        ArrayList<Entry> entries2_5 = new ArrayList<>();
        for (int i=0;i<24;i++){
            Log.d("hyuo",pm2_5.get(i) + " ");
            float x = i;
            float y = Float.parseFloat(pm2_5.get(i));
            sumpm2_5 = sumpm2_5 +y;
            entries2_5.add(new Entry(x,y));
        }
        pm2_5avg = sumpm2_5/24;


        ArrayList<Entry> entriesaqi = new ArrayList<>();
        for (int i=0;i<24;i++){
            Log.d("hyuo",aqi.get(i) + " ");
            float x = i;
            float y = Float.parseFloat(aqi.get(i));
            sumaqi = sumaqi +y;
            entriesaqi.add(new Entry(x,y));
        }
        aqiavg = sumaqi/24;

        for (int i=0;i<24;i++){
            Log.d("hyuo",co.get(i) + " ");
            float y = Float.parseFloat(co.get(i));
            sumco = sumco +y;
        }
        coavg = sumco/24;

        LineDataSet dataSet10 = new LineDataSet(entries10,"PM 10");
        dataSet10.setColor(Color.BLUE);

        LineDataSet dataSet2_5 = new LineDataSet(entries2_5,"PM 2.5");
        dataSet2_5.setColor(Color.RED);

        LineDataSet dataSetaqi = new LineDataSet(entriesaqi,"AQI");
        dataSetaqi.setColor(Color.GREEN);

        LineData lineData = new LineData();
        lineData.addDataSet(dataSet10);
        lineData.addDataSet(dataSet2_5);
        lineData.addDataSet(dataSetaqi);
        lineChart1.setData(lineData);
        lineChart1.setTouchEnabled(true);
        lineChart1.setDragEnabled(true);
        lineChart1.setScaleEnabled(true);
        lineChart1.animateX(1500);
        lineChart1.animateY(1500);

        dataSetaqi.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSetaqi.setCubicIntensity(0.2f);
        dataSetaqi.setDrawFilled(true);
        dataSetaqi.setDrawCircles(false);
        dataSetaqi.setDrawValues(false);
        dataSetaqi.setLineWidth(3.5f);
        dataSetaqi.setFillColor(Color.parseColor("#57ac4d"));


        dataSet2_5.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet2_5.setCubicIntensity(0.2f);
        dataSet2_5.setDrawFilled(true);
        dataSet2_5.setDrawCircles(false);
        dataSet2_5.setDrawValues(false);
        dataSet2_5.setLineWidth(3.5f);
        dataSet2_5.setFillColor(Color.parseColor("#b94141"));

        dataSet10.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet10.setCubicIntensity(0.2f);
        dataSet10.setDrawFilled(true);
        dataSet10.setDrawCircles(false);
        dataSet10.setDrawValues(false);
        dataSet10.setLineWidth(3.5f);
        dataSet10.setFillColor(Color.parseColor("#5141b9"));


        XAxis xAxis = lineChart1.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setAxisMinimum(0);
        xAxis.setTextColor(Color.WHITE);
        xAxis.setAxisLineColor(Color.WHITE);


        YAxis yAxis = lineChart1.getAxisLeft();
        yAxis.setAxisMinimum(0);
        yAxis.setDrawGridLines(false);
        yAxis.setTextColor(Color.WHITE);
        yAxis.setAxisLineColor(Color.WHITE);

        lineChart1.getAxisRight().setEnabled(false);
        lineChart1.invalidate();

        setValues();

    }

    public void Graph2(){
        double sumpm2_5=0,sumpm10=0,sumaqi=0,sumco=0;
        ArrayList<Entry> entries10 = new ArrayList<>();
        for (int i=24;i<48;i++){
            Log.d("hyuo",pm10.get(i) + " ");
            float x = i-24;
            float y = Float.parseFloat(pm10.get(i));
            sumpm10 = sumpm10 +y;
            entries10.add(new Entry(x,y));
        }
        pm10avg = sumpm10/24;

        ArrayList<Entry> entries2_5 = new ArrayList<>();
        for (int i=24;i<48;i++){
            Log.d("hyuo",pm2_5.get(i) + " ");
            float x = i-24;
            float y = Float.parseFloat(pm2_5.get(i));
            sumpm2_5 = sumpm2_5 +y;
            entries2_5.add(new Entry(x,y));
        }
        pm2_5avg = sumpm2_5/24;


        ArrayList<Entry> entriesaqi = new ArrayList<>();
        for (int i=24;i<48;i++){
            Log.d("hyuo",aqi.get(i) + " ");
            float x = i-24;
            float y = Float.parseFloat(aqi.get(i));
            sumaqi = sumaqi +y;
            entriesaqi.add(new Entry(x,y));
        }
        aqiavg = sumaqi/24;

        for (int i=24;i<48;i++){
            Log.d("hyuo",co.get(i) + " ");
            float y = Float.parseFloat(co.get(i));
            sumco = sumco +y;
        }
        coavg = sumco/24;

        LineDataSet dataSet10 = new LineDataSet(entries10,"PM 10");
        dataSet10.setColor(Color.BLUE);

        LineDataSet dataSet2_5 = new LineDataSet(entries2_5,"PM 2.5");
        dataSet2_5.setColor(Color.RED);

        LineDataSet dataSetaqi = new LineDataSet(entriesaqi,"AQI");
        dataSetaqi.setColor(Color.GREEN);

        LineData lineData = new LineData();
        lineData.addDataSet(dataSet10);
        lineData.addDataSet(dataSet2_5);
        lineData.addDataSet(dataSetaqi);
        lineChart1.setData(lineData);
        lineChart1.setTouchEnabled(true);
        lineChart1.setDragEnabled(true);
        lineChart1.setScaleEnabled(true);
        lineChart1.animateX(1500);
        lineChart1.animateY(1500);

        dataSetaqi.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSetaqi.setCubicIntensity(0.2f);
        dataSetaqi.setDrawFilled(true);
        dataSetaqi.setDrawCircles(false);
        dataSetaqi.setDrawValues(false);
        dataSetaqi.setLineWidth(3.5f);
        dataSetaqi.setFillColor(Color.parseColor("#57ac4d"));


        dataSet2_5.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet2_5.setCubicIntensity(0.2f);
        dataSet2_5.setDrawFilled(true);
        dataSet2_5.setDrawCircles(false);
        dataSet2_5.setDrawValues(false);
        dataSet2_5.setLineWidth(3.5f);
        dataSet2_5.setFillColor(Color.parseColor("#b94141"));

        dataSet10.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet10.setCubicIntensity(0.2f);
        dataSet10.setDrawFilled(true);
        dataSet10.setDrawCircles(false);
        dataSet10.setDrawValues(false);
        dataSet10.setLineWidth(3.5f);
        dataSet10.setFillColor(Color.parseColor("#5141b9"));



        XAxis xAxis = lineChart1.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setAxisMinimum(0);


        YAxis yAxis = lineChart1.getAxisLeft();
        yAxis.setAxisMinimum(0);
        yAxis.setDrawGridLines(false);

        lineChart1.getAxisRight().setEnabled(false);
        lineChart1.invalidate();

        setValues();

    }

    public void Graph3(){
        double sumpm2_5=0,sumpm10=0,sumaqi=0,sumco=0;
        ArrayList<Entry> entries10 = new ArrayList<>();
        for (int i=0;i<pm10.size();i++){
            Log.d("hyuo",pm10.get(i) + " ");
            float x = i;
            float y = Float.parseFloat(pm10.get(i));
            sumpm10 = sumpm10 +y;
            entries10.add(new Entry(x,y));
        }
        pm10avg = sumpm10/pm10.size();

        ArrayList<Entry> entries2_5 = new ArrayList<>();
        for (int i=0;i<pm2_5.size();i++){
            Log.d("hyuo",pm2_5.get(i) + " ");
            float x = i;
            float y = Float.parseFloat(pm2_5.get(i));
            sumpm2_5 = sumpm2_5 +y;
            entries2_5.add(new Entry(x,y));
        }
        pm2_5avg = sumpm2_5/pm2_5.size();


        ArrayList<Entry> entriesaqi = new ArrayList<>();
        for (int i=0;i<aqi.size();i++){
            Log.d("hyuo",aqi.get(i) + " ");
            float x = i;
            float y = Float.parseFloat(aqi.get(i));
            sumaqi = sumaqi +y;
            entriesaqi.add(new Entry(x,y));
        }
        aqiavg = sumaqi/aqi.size();

        for (int i=0;i<co.size();i++){
            Log.d("hyuo",co.get(i) + " ");
            if(co.get(i)!=null){
                float y = Float.parseFloat(co.get(i));
                sumco = sumco +y;
            }
            else break;
        }
        coavg = sumco/co.size();

        LineDataSet dataSet10 = new LineDataSet(entries10,"PM 10");
        dataSet10.setColor(Color.BLUE);

        LineDataSet dataSet2_5 = new LineDataSet(entries2_5,"PM 2.5");
        dataSet2_5.setColor(Color.RED);

        LineDataSet dataSetaqi = new LineDataSet(entriesaqi,"AQI");
        dataSetaqi.setColor(Color.GREEN);

        LineData lineData = new LineData();
        lineData.addDataSet(dataSet10);
        lineData.addDataSet(dataSet2_5);
        lineData.addDataSet(dataSetaqi);
        lineChart1.setData(lineData);
        lineChart1.setTouchEnabled(true);
        lineChart1.setDragEnabled(true);
        lineChart1.setScaleEnabled(true);
        lineChart1.animateX(1500);
        lineChart1.animateY(1500);

        dataSetaqi.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSetaqi.setCubicIntensity(0.2f);
        dataSetaqi.setDrawFilled(true);
        dataSetaqi.setDrawCircles(false);
        dataSetaqi.setDrawValues(false);
        dataSetaqi.setLineWidth(3.5f);
        dataSetaqi.setFillColor(Color.parseColor("#57ac4d"));


        dataSet2_5.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet2_5.setCubicIntensity(0.2f);
        dataSet2_5.setDrawFilled(true);
        dataSet2_5.setDrawCircles(false);
        dataSet2_5.setDrawValues(false);
        dataSet2_5.setLineWidth(3.5f);
        dataSet2_5.setFillColor(Color.parseColor("#b94141"));

        dataSet10.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet10.setCubicIntensity(0.2f);
        dataSet10.setDrawFilled(true);
        dataSet10.setDrawCircles(false);
        dataSet10.setDrawValues(false);
        dataSet10.setLineWidth(3.5f);
        dataSet10.setFillColor(Color.parseColor("#5141b9"));


        XAxis xAxis = lineChart1.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setAxisMinimum(0);
        xAxis.setTextColor(Color.WHITE);
        xAxis.setAxisLineColor(Color.WHITE);


        YAxis yAxis = lineChart1.getAxisLeft();
        yAxis.setAxisMinimum(0);
        yAxis.setDrawGridLines(false);
        yAxis.setTextColor(Color.WHITE);
        xAxis.setAxisLineColor(Color.WHITE);

        lineChart1.getAxisRight().setEnabled(false);
        lineChart1.invalidate();

        setValues();

    }

    private void setValues(){
        aqitext.setText(String.valueOf(Math.round(aqiavg)));
        pm10text.setText(String.valueOf(Math.round(pm10avg)));
        pm2_5text.setText(String.valueOf(Math.round(pm2_5avg)));
        cotext.setText(String.valueOf(Math.round(coavg)));

        if(Math.round(aqiavg)<=55){
            rating.setImageResource(R.drawable.one);
            aqitext.setTextColor(Color.parseColor("#2CA756"));
        }
        else if(Math.round(aqiavg)>55 && Math.round(aqiavg)<=70){
            rating.setImageResource(R.drawable.two);
            aqitext.setTextColor(Color.parseColor("#93DC7F"));

        }
        else if(Math.round(aqiavg)>70 && Math.round(aqiavg)<=85){
            rating.setImageResource(R.drawable.three);
            aqitext.setTextColor(Color.parseColor("#F6F90E"));

        }
        else if(Math.round(aqiavg)>85 && Math.round(aqiavg)<=105){
            rating.setImageResource(R.drawable.four);
            aqitext.setTextColor(Color.parseColor("#FDD017"));

        }
        else if(Math.round(aqiavg)>105 && Math.round(aqiavg)<=200){
            rating.setImageResource(R.drawable.five);
            aqitext.setTextColor(Color.parseColor("#FF412E"));

        }
    }

    public void bottomnav(){

        tomorrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Graph2();

                tomorrow.setTextColor(Color.parseColor("#FFFFFF"));
                tomorrow.setClickable(false);
                today.setTextColor(Color.parseColor("#000000"));
                today.setClickable(true);
                tendays.setTextColor(Color.parseColor("#000000"));
                tendays.setClickable(true);
            }
        });
        today.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Graph1();
                today.setTextColor(Color.parseColor("#FFFFFF"));
                today.setClickable(false);
                tomorrow.setTextColor(Color.parseColor("#000000"));
                tomorrow.setClickable(true);
                tendays.setTextColor(Color.parseColor("#000000"));
                tendays.setClickable(true);
            }
        });

        tendays.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Graph3();

                tendays.setTextColor(Color.parseColor("#FFFFFF"));
                tendays.setClickable(false);
                tomorrow.setTextColor(Color.parseColor("#000000"));
                tomorrow.setClickable(true);
                today.setTextColor(Color.parseColor("#000000"));
                today.setClickable(true);
            }
        });

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

    public void dateManager(){

        Calendar calendar = Calendar.getInstance(); // Get current date
        calendar.add(Calendar.DAY_OF_YEAR, 2);
        Date dateAfterTenDays = calendar.getTime();


        Date currentDate = new Date();
        @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        formattedDate = dateFormat.format(currentDate);

        SimpleDateFormat dateFormat2 = new SimpleDateFormat("yyyy-MM-dd"); // Define the date format
        dateString2 = dateFormat.format(dateAfterTenDays); // Format the date into a string


        Date longerDate = new Date();
        SimpleDateFormat longerDateFormat = new SimpleDateFormat("dd-MMMM-yyyy");
        longdate = longerDateFormat.format(longerDate);

    }

    public void blurview(){

        float radius = 20f;
        View decorView = getWindow().getDecorView();
        ViewGroup rootView = decorView.findViewById(android.R.id.content);
        Drawable windowBackground = decorView.getBackground();
        blurView1 = findViewById(R.id.blur1);
        blurView1.setupWith(rootView, new RenderScriptBlur(this)).setFrameClearDrawable(windowBackground).setBlurRadius(radius);

        blurView2 = findViewById(R.id.blur2);
        blurView2.setupWith(rootView, new RenderScriptBlur(this)).setFrameClearDrawable(windowBackground).setBlurRadius(radius);


        blurView3 = findViewById(R.id.blur3);
        blurView3.setupWith(rootView, new RenderScriptBlur(this)).setFrameClearDrawable(windowBackground).setBlurRadius(radius);

        blurView4 = findViewById(R.id.blur4);
        blurView4.setupWith(rootView, new RenderScriptBlur(this)).setFrameClearDrawable(windowBackground).setBlurRadius(radius);

    }


}