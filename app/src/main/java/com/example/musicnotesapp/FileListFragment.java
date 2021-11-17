package com.example.musicnotesapp;

import static android.content.ContentValues.TAG;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
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

/**
 * A simple {@link Fragment} subclass.
 */
public class FileListFragment extends Fragment implements View.OnClickListener {

    private NavController navController;
    private Button NoteBtn;
    private Button RecordBtn;

    private ConstraintLayout playerSheet; // get the audio player layout
    private BottomSheetBehavior bottomSheetBehavior; // get the bottom sheet class
    private RecyclerView FileList; // get the file displayer
    private File[] allFiles;

    private AudioListAdpater audioListAdpater; // for AudioListAdapter

    public FileListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_file_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        playerSheet = view.findViewById(R.id.player_sheet); // get audio player layout
        bottomSheetBehavior = BottomSheetBehavior.from(playerSheet); // get behavior of the player sheet

        FileList = view.findViewById(R.id.file_list_view); // find file list display in layout
        // find directories of saved files for app
        String path = getActivity().getExternalFilesDir("/").getAbsolutePath();
        File directory = new File(path); // get directory
        allFiles = directory.listFiles(); // put all files in the directory into list

        audioListAdpater = new AudioListAdpater(allFiles);

        FileList.setHasFixedSize(true);
        FileList.setLayoutManager(new LinearLayoutManager(getContext()));
        FileList.setAdapter(audioListAdpater);

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
        //Initialize Variables
        navController = Navigation.findNavController(view);
        NoteBtn = view.findViewById(R.id.Note);
        RecordBtn = view.findViewById(R.id.Record);

        RecordBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.Record:
                Log.d(TAG, "onClick: Record button clicked");
                navController.navigate(R.id.action_fileListFragment_to_drawingFragment);
                //navController.navigate(R.id.action_fileListFragment_to_recordFragment);
                break;
            case R.id.Note:
                Log.d(TAG, "onClick: Note button clicked");
                navController.navigate(R.id.action_fileListFragment_to_drawingFragment);
                break;
        }
    }
}
