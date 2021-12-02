package com.example.musicnotesapp;

import static android.content.ContentValues.TAG;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class ImageFragment extends Fragment implements View.OnClickListener {

    private ImageView image;
    private String fileToPlay;

    private NavController navController;

    public ImageFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment


        View view = inflater.inflate(R.layout.image_picker, container, false);
        setHasOptionsMenu(true);

        Bundle bundle = this.getArguments();

        if(bundle != null){
            // handle your code here.
            Log.d("I HAVE A BUNDLE", bundle.getString("key"));

            try {
                fileToPlay = bundle.getString("key");
                File f = new File(fileToPlay);
                Bitmap b = BitmapFactory.decodeStream(new FileInputStream(f));
                ImageView imageView = (ImageView) view.findViewById(R.id.imgPicker);
                imageView.setImageBitmap(b);


            }catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Log.i(TAG, "onCreateOptionsMenu: called");
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.image_view_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.go_back:  // invoke save
                Log.d(TAG, "onClick: Note button clicked");
                navController.navigate(R.id.action_imageFragment_to_fileListFragment);
                break;
        }

        return super.onOptionsItemSelected(item);
    }


    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState); // call parent class

        navController = Navigation.findNavController(view); // set navController
    }

    @Override
    public void onClick(View view) {

    }
}
