package com.example.airquality;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class Splash extends AppCompatActivity {
    TextView tv1,tv2,tv3;
    Animation top,bottom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        getSupportActionBar().hide();


        tv1 = findViewById(R.id.textView1);
        tv2 = findViewById(R.id.textView2);
        tv3 = findViewById(R.id.textView3);

        top =  AnimationUtils.loadAnimation(this,R.anim.top);
        bottom = AnimationUtils.loadAnimation(this,R.anim.bottom);

        tv1.setAnimation(bottom);
        bottom.setDuration(1500);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                tv2.setAnimation(bottom);
            }
        },800);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                tv3.setAnimation(bottom);
            }
        },1600);


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent i = new Intent(Splash.this,Search.class);
                startActivity(i);
            }
        },2500);
    }
}