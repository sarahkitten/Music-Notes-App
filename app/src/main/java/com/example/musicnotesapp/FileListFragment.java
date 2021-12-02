package com.example.musicnotesapp;

import static android.content.ContentValues.TAG;

import android.app.AlertDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ImageView;
import android.app.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class FileListFragment extends Fragment implements AudioListAdapter.onItemListClick {


    private MediaPlayer mediaPlayer = null;
    private boolean isPlaying = false;

    private File fileToPlay = null;

    private Bundle bundle = null;

    //UI Elements
    private ImageButton playBtn;
    private View rootView;
    private ImageView imageView;
    private TextView playerHeader;
    private TextView playerFilename;

    private SeekBar playerSeekbar;
    private Handler seekbarHandler;
    private Runnable updateSeekbar;

    private NavController navController;
//    private Button NoteBtn;
//    private Button RecordBtn;

    private ConstraintLayout playerSheet; // get the audio player layout
    private BottomSheetBehavior bottomSheetBehavior; // get the bottom sheet class
    private RecyclerView FileList; // get the file displayer
//    private RecyclerView noteFileList; // get the file displayer
    private List<File> allFiles = new ArrayList<>();

    private AudioListAdapter audioListAdapter; // for AudioListAdapter

    public FileListFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);  // has options menu
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_file_list, container, false);
//        imageView = rootView.findViewById(R.id.imgPicker);

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Log.i(TAG, "onCreateOptionsMenu: called");
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.file_list_menu, menu);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        playBtn = view.findViewById(R.id.player_play_btn);
        playerHeader = view.findViewById(R.id.player_header_title);
        playerFilename = view.findViewById(R.id.player_filename);

        playerSeekbar = view.findViewById(R.id.player_seekbar);
        playerSheet = view.findViewById(R.id.player_sheet); // get audio player layout
        bottomSheetBehavior = BottomSheetBehavior.from(playerSheet); // get behavior of the player sheet

        navController = Navigation.findNavController(view); // set navController

        FileList = view.findViewById(R.id.file_list_view); // find file list display in layout

        // find directories of audio files for app
        String audioPath = getActivity().getExternalFilesDir("/").getAbsolutePath();
        File audioDirectory = new File(audioPath); // get directory
        File[] audioFiles = audioDirectory.listFiles(); // put all files in the directory into list

        // add audio files to display
        for(File path:audioFiles) {
            if(path.exists()) {
                allFiles.add(path);
            }
        }

        // find directories of note files for app
        ContextWrapper cw = new ContextWrapper(getContext());
        File noteDirectory = cw.getDir("imageDir", Context.MODE_PRIVATE); // get directory
        File[] noteFiles = (noteDirectory.listFiles()); // put all files in the directory into list

        // add not files to display
        for(File path:noteFiles) {
            if (path.exists()) {
                allFiles.add(path);
            }
        }

        audioListAdapter = new AudioListAdapter(allFiles, this);
        FileList.setHasFixedSize(true);
        FileList.setLayoutManager(new LinearLayoutManager(getContext()));
        FileList.setAdapter(audioListAdapter);


        bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if(newState == BottomSheetBehavior.STATE_HIDDEN){
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                // cant do anything here
            }
        });

        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isPlaying){
                    pauseAudio();
                } else {
                    if(fileToPlay != null){
                        resumeAudio();
                    }
                }
            }
        });

        playerSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                pauseAudio();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int progress = seekBar.getProgress();
                mediaPlayer.seekTo(progress);
                resumeAudio();
            }
        });
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.Record:  // invoke clear
                Log.d(TAG, "onClick: Record button clicked");
                navController.navigate(R.id.action_fileListFragment_to_recordFragment);
                break;

            case R.id.Note:  // invoke save
                Log.d(TAG, "onClick: Note button clicked");
                navController.navigate(R.id.action_fileListFragment_to_drawingFragment);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClickListener(File file, int position, boolean delete_flag)  {
        fileToPlay = file;
        if(!delete_flag)
        {
            if (fileToPlay.getName().endsWith(".3gp")) {
                if (isPlaying) {
                    stopAudio();
                    playAudio(fileToPlay);
                } else {
                    playAudio(fileToPlay);
                }
            }
            else if (fileToPlay.getName().endsWith(".jpg")) {
                Log.d("file: ", fileToPlay.getAbsolutePath());
                Bundle bundle = new Bundle();
                bundle.putString("key",fileToPlay.getAbsolutePath()); // Put anything what you want

                navController.navigate(R.id.action_fileListFragment_to_drawingFragment, bundle);
                Log.d("Im still in FILE LIST FRAGMENT", bundle.getString("key"));

            }
        }
        else{ // delete given file
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext()); //make alert box
            alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() { // make OKAY button
                @Override
                public void onClick(DialogInterface dialogInterface, int i) { // if clicked ok
                    delete_file(fileToPlay, position); // call delete function on file
                }
            });
            alertDialog.setNegativeButton("CANCEL", null); // make cancel button
            alertDialog.setTitle("Deleting File"); // title of alert box
            alertDialog.setMessage("Are you sure, you want to delete this file?"); // if pressed okay
            alertDialog.create().show(); // create and show the alert box
        }
    }

    private void delete_file(File file, int position){
        fileToPlay.delete(); // maybe delete file
        allFiles.remove(position); // remove entry from list
        audioListAdapter = new AudioListAdapter(allFiles, this);
        FileList.setHasFixedSize(true);
        FileList.setLayoutManager(new LinearLayoutManager(getContext()));
        FileList.setAdapter(audioListAdapter); // sets the new file list
    }

    private void pauseAudio() {
        mediaPlayer.pause();
        playBtn.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.player_play_btn, null));
        isPlaying = false;
        seekbarHandler.removeCallbacks(updateSeekbar);
    }

    private void resumeAudio() {
        mediaPlayer.start();
        playBtn.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.player_pause_btn, null));
        isPlaying = true;

        updateRunnable();
        seekbarHandler.postDelayed(updateSeekbar, 0);

    }

    private void stopAudio() {
        //Stop The Audio
        playBtn.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.player_play_btn, null));
        playerHeader.setText("Stopped");
        isPlaying = false;
        mediaPlayer.stop();
        seekbarHandler.removeCallbacks(updateSeekbar);
    }

    private void playAudio(File fileToPlay) {

        mediaPlayer = new MediaPlayer();
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        try {
            mediaPlayer.setDataSource(fileToPlay.getAbsolutePath());
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        playBtn.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.player_pause_btn, null));
        playerFilename.setText(fileToPlay.getName());
        playerHeader.setText("Playing");
        //Play the audio
        isPlaying = true;
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) { // once audio is done playing
                stopAudio(); // stop playback
                playerHeader.setText("Finished"); // display that you are done
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED); // collapse media player
            }
        });

        playerSeekbar.setMax(mediaPlayer.getDuration());

        seekbarHandler = new Handler();
        updateRunnable();
        seekbarHandler.postDelayed(updateSeekbar, 0);

    }

    private void updateRunnable() {
        updateSeekbar = new Runnable() {
            @Override
            public void run() {
                playerSeekbar.setProgress(mediaPlayer.getCurrentPosition());
                seekbarHandler.postDelayed(this, 500);
            }
        };
    }

    @Override
    public void onStop() {
        super.onStop();
        if(isPlaying) {
            stopAudio();
        }
    }
}
