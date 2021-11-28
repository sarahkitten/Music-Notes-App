package com.example.musicnotesapp;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.ContextWrapper;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

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
import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class FileListFragment extends Fragment implements View.OnClickListener {

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
        return inflater.inflate(R.layout.fragment_file_list, container, false);
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

        audioListAdapter = new AudioListAdapter(allFiles);

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
    public void onClick(View view) {
//        switch(view.getId()){
//            case R.id.Record:
//                Log.d(TAG, "onClick: Record button clicked");
//                navController.navigate(R.id.action_fileListFragment_to_recordFragment);
//                break;
//            case R.id.Note:
//                Log.d(TAG, "onClick: Note button clicked");
//                navController.navigate(R.id.action_fileListFragment_to_drawingFragment);
//                break;
//        }
    }
}
