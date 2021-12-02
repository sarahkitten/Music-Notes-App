package com.example.musicnotesapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); // create saved
        setContentView(R.layout.activity_main); // give navigation rights to nav graph
    }

    @Override
    public void onBackPressed(){
        //super.onBackPressed();
    }

}