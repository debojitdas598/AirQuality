package com.example.airquality;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class Splash extends AppCompatActivity {
    ImageView air, block, quality;
    Animation top, bottom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        getSupportActionBar().hide();
        air = findViewById(R.id.imageView4);
        block = findViewById(R.id.imageView2);
        quality = findViewById(R.id.imageView3);

        bottom = AnimationUtils.loadAnimation(this,R.anim.bottom);
        quality.setAnimation(bottom);
        bottom.setDuration(1000);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent i = new Intent(Splash.this, Search.class);
                startActivity(i);
                finish();
            }
        }, 2000);
    }
}