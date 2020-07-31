package com.example.croller;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Croller croller = (Croller) findViewById(R.id.croller);
        croller.setOnCrollerChangeListener(new OnCrollerChangeListener() {
            @Override
            public void onProgressChanged(Croller croller, int progress) {
                Log.e("onChanged", progress+"");
            }

            @Override
            public void onStartTrackingTouch(Croller croller) {
                Log.e("onStartTrackingTouch", " : start");

            }

            @Override
            public void onStopTrackingTouch(Croller croller) {
                Log.e("onStopTrackingTouch", " : stop");

            }
        });
    }
}