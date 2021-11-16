package com.example.musicnotesapp;


import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.os.SystemClock;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


/**
 * A simple {@link Fragment} subclass.
 */
public class RecordFragment extends Fragment implements View.OnClickListener {

    private NavController navController; // have nav controller so we navigate through fragments
    private ImageButton recordBtn; // recording button
    private Button backBtn; // back button to return to precvious menu
    private TextView filenameText; // get the text box to display filename

    private String recordPermission = Manifest.permission.RECORD_AUDIO; // permission string
    private int PERMISSION_CODE = 5; // permission code var

    private boolean isRecording = false; // boolean to check if recording
    private MediaRecorder recorder; // create the object that records
    private String recordFile; // output file of recording

    private Chronometer timer; // to create timer of how long recording has gone on for

    public RecordFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_record, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState); // call parent class

        navController = Navigation.findNavController(view); // set navController
        recordBtn = view.findViewById(R.id.record_btn); // find recording button
        backBtn = view.findViewById(R.id.go_back); // find back button
        timer = view.findViewById(R.id.record_timer); // find the timer on the xml
        filenameText = view.findViewById(R.id.record_filename); // find text box

        backBtn.setOnClickListener(this); // call OnClick if button is pressed
        recordBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.record_btn:
                if(isRecording) { // stop recording
                    // Stop Recording
                    stopRecording();

                    // put in the is not recording button
                    recordBtn.setImageDrawable(getResources().getDrawable(R.drawable.record_btn_stopped, null));
                    isRecording = false; // set recording to false
                } else {
                    // Start Recording
                    if(checkPermissions()){ // check permission to record audio
                        startRecording();
                        // put in the is recording button
                        recordBtn.setImageDrawable(getResources().getDrawable(R.drawable.record_btn_recording, null));
                        isRecording = true; // set recording to true
                    }
                }
                break;

            case R.id.go_back: // if pressed back button
                // goto file view fragment
                navController.navigate(R.id.action_recordFragment_to_fileListFragment);
                break;
        }
    }

    private void stopRecording() {
        timer.stop(); // stop the timer

        filenameText.setText("Recording Stopped, File Saved : " + recordFile);

        recorder.stop(); // stop recording
        recorder.release(); // release the recorder
        recorder = null; // set recorder to null so can use again
    }

    private void startRecording() {
        timer.setBase(SystemClock.elapsedRealtime()); // set the initial time of clock
        timer.start(); // start the timer
        // get file path
        String recordPath = getActivity().getExternalFilesDir("/").getAbsolutePath();
        // get the date
        SimpleDateFormat formatter = new SimpleDateFormat("yyy_MM_dd_hh_mm_ss", Locale.US);
        // create date object
        Date now = new Date();

        // format file string
        recordFile = "Recording_" + formatter.format(now) + ".3gp"; // filename

        // fill text box with what file is recording
        filenameText.setText("Recording, File Name : " + recordFile);

        recorder = new MediaRecorder(); // create media recorder
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC); // get microphone
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP); // record in THREE_GPP
        recorder.setOutputFile(recordPath + "/" + recordFile); // set the output file of recording
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB); // set the audio encoder

        // prepare the recorder it requires it be wrapped around try and catch
        try{
            recorder.prepare();
        } catch (IOException e){
            e.printStackTrace();
        }

        recorder.start(); // start the recording
    }

    private boolean checkPermissions() {
        // if have permission return true
        if(ActivityCompat.checkSelfPermission(getContext(), recordPermission) == PackageManager.PERMISSION_GRANTED){
            return true;
        }
        else{ // if dont have permission request
            ActivityCompat.requestPermissions(getActivity(), new String[]{recordPermission}, PERMISSION_CODE);
            return false;
        }
    }
}
