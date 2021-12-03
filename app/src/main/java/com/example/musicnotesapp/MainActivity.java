/*
Source Code for the initialization used in the Audio Recording, CIS422 FA21
Author(s): Alex Summers
Last Edited: 12/2/21
Sources: N/A
*/

package com.example.musicnotesapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); // create parent save instance
        setContentView(R.layout.activity_main); // give navigation rights to nav graph
    }

    @Override
    public void onBackPressed(){
        // make this function do nothing so the android system back button does
        // nothing because it brought bugs into our file display where if navigating
        // through fragments it would duplicate files in the file display
    }

}