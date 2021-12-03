// DrawingFragment
// By Sarah Kitten based on https://youtu.be/H9CTVyu3Bi8
// View for options menu to change color/line width, clear, or save
// TODO: make back button, test

package com.example.musicnotesapp;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;


import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import view.PikassoView;

public class DrawingFragment extends Fragment implements OnClickListener {

    private PikassoView pikassoView;
    private AlertDialog.Builder currentAlertDialog;
    private ImageView widthImageView;
    private ImageView textImageView;
    private AlertDialog dialogLineWidth;
    private AlertDialog colorDialog;
    private AlertDialog textDialog;

    private SeekBar alphaSeekBar;
    private SeekBar redSeekBar;
    private SeekBar greenSeekBar;
    private SeekBar blueSeekBar;
    private View colorView;

    private Bitmap textBitmap;
    private Canvas textCanvas;
    private Paint textPaint;
    private String textValue;

    private Bundle bundle;
    private String fileToPlay = null;
    boolean saved_file = false; // if this fragment brought up by tapping on it in file frag

    private NavController navController; // have nav controller so we navigate through fragments

    private BottomNavigationView bottomNavigationView; // Bottom mode selection
    private BottomSheetBehavior mBottomSheetBehavior; // Note selection menu behavior

    private ImageButton quarterNote; // Quarter Note button
    private ImageButton halfNote; // Half Note button
    private ImageButton wholeNote; // Whole Note button
    private ImageButton staff; // Staff button


    public DrawingFragment() {
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

        View view = inflater.inflate(R.layout.fragment_drawing, container, false);

        setHasOptionsMenu(true);

        bundle = this.getArguments();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState); // call parent class

        navController = Navigation.findNavController(view); // set navController
        pikassoView = view.findViewById(R.id.view);  // set pikassoView

        quarterNote = view.findViewById(R.id.quarterNote); // Instantiate note buttons
        halfNote = view.findViewById(R.id.halfNote);
        wholeNote = view.findViewById(R.id.wholeNote);
        staff = view.findViewById(R.id.staff);

        quarterNote.setOnClickListener(new View.OnClickListener() { // Call if quarter note button is pressed
            @Override
            public void onClick(View v) { // Set note as quarter note
                pikassoView.setDraggable_img(PikassoView.MusicItem.QUARTER_NOTE);
            }
        });

        halfNote.setOnClickListener(new View.OnClickListener() { // Call if half note button is pressed
            @Override
            public void onClick(View v) { // Set note as half note
                pikassoView.setDraggable_img(PikassoView.MusicItem.HALF_NOTE);
            }
        });

        wholeNote.setOnClickListener(new View.OnClickListener() { // Call if whole note button is pressed
            @Override
            public void onClick(View v) { // Set note as whole note
                pikassoView.setDraggable_img(PikassoView.MusicItem.WHOLE_NOTE);
            }
        });
        staff.setOnClickListener(new View.OnClickListener() { // Call if staff button is pressed
            @Override
            public void onClick(View v) { // Set note as staff
                pikassoView.setDraggable_img(PikassoView.MusicItem.STAFF);
            }
        });

        if(bundle != null){
            // handle your code here.
            // Log.d("I HAVE A BUNDLE", bundle.getString("key"));

            fileToPlay = bundle.getString("key");
            if(fileToPlay != null){
                saved_file = true;
                pikassoView.getDrawableImgFromPath(fileToPlay);
            }
        }

        // Instantiate bottom mode selection menu and set listener on mode selected
        bottomNavigationView = view.findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(listener);

        // Instantiate note selection menu and behavior from note selection view
        View bottomSheet = view.findViewById(R.id.bottom_nested_view);
        mBottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
    }

    private BottomNavigationView.OnItemSelectedListener listener =
            new BottomNavigationView.OnItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    // Call if item is selected from mode selection menu
                    switch (item.getItemId()) {
                        case R.id.drawingMode: // Switch to drawing mode
                            pikassoView.inputMode = "draw";
                            // Hide note selection menu
                            mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                            break;

                        case R.id.draggingMode: // Switch to dragging mode
                            pikassoView.inputMode = "drag";
                            // Show note selection menu
                            mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                            break;

                        case R.id.textId: // Switch to text mode
                            pikassoView.inputMode = "type";

                            mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                            showTextDialog(); // Bring up text edit window
                            break;
                    }
                    return true;
                }
            };

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Log.i(TAG, "onCreateOptionsMenu: called");
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.clearId:  // invoke clear
                pikassoView.clear();
                break;

            case R.id.saveid:  // invoke save
                pikassoView.saveToInternalStorage();
                break;

            case R.id.undoId:  // invoke undo
                pikassoView.undo();
                break;

            case R.id.backId:  // return to menu
                navController.navigate(R.id.action_drawingFragment_to_fileListFragment);
                break;

            case R.id.Record:  // invoke color select
                showColorDialog();
                break;

            case R.id.Note:  // invoke line width select
                showLineWidthDialog();
                break;

        }

        return super.onOptionsItemSelected(item);
    }

    void showColorDialog() {
        currentAlertDialog = new AlertDialog.Builder(requireActivity());  // create alert dialog
        View view = getLayoutInflater().inflate(R.layout.color_dialog, null);  // show dialog

        // get ARGB seekbars
        alphaSeekBar = view.findViewById(R.id.alphaSeekBar);
        redSeekBar = view.findViewById(R.id.redSeekBar);
        greenSeekBar = view.findViewById(R.id.greenSeekBar);
        blueSeekBar = view.findViewById(R.id.blueSeekBar);
        colorView = view.findViewById(R.id.colorView);

        // register SeekBar event listeners
        alphaSeekBar.setOnSeekBarChangeListener(colorSeekBarChanged);
        redSeekBar.setOnSeekBarChangeListener(colorSeekBarChanged);
        greenSeekBar.setOnSeekBarChangeListener(colorSeekBarChanged);
        blueSeekBar.setOnSeekBarChangeListener(colorSeekBarChanged);

        int color = pikassoView.getDrawingColor();
        alphaSeekBar.setProgress(Color.alpha(color));
        redSeekBar.setProgress(Color.red(color));
        greenSeekBar.setProgress(Color.green(color));
        blueSeekBar.setProgress(Color.blue(color));

        Button setColorButton = view.findViewById(R.id.setColorButton);
        setColorButton.setOnClickListener(new OnClickListener() {
            // set drawing color to the selected ARGB values
            @Override
            public void onClick(View v) {
                pikassoView.setDrawingColor(Color.argb(
                        alphaSeekBar.getProgress(),
                        redSeekBar.getProgress(),
                        greenSeekBar.getProgress(),
                        blueSeekBar.getProgress()
                ));

                colorDialog.dismiss();  // dismiss dialog
                currentAlertDialog = null;
            }
        });

        currentAlertDialog.setView(view);
        currentAlertDialog.setTitle("Choose Color");
        colorDialog = currentAlertDialog.create();
        colorDialog.show();
    }

    void showLineWidthDialog() {
        // show line width dialog with line width seekbar and preview image

        currentAlertDialog = new AlertDialog.Builder(requireActivity());  // create alert dialog
        View view = getLayoutInflater().inflate(R.layout.width_dialog, null);  // show dialog

        SeekBar widthSeekbar = view.findViewById(R.id.widthSeekBar);
        Button setLineWidthButton = view.findViewById(R.id.widthDialogButton);
        widthImageView = view.findViewById(R.id.imageViewId);
        setLineWidthButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // set line width and dismiss dialog
                pikassoView.setLineWidth(widthSeekbar.getProgress());
                dialogLineWidth.dismiss();
                currentAlertDialog = null;
            }
        });

        widthSeekbar.setOnSeekBarChangeListener(widthSeekbarChange);
        widthSeekbar.setProgress(pikassoView.getLineWidth());

        currentAlertDialog.setView(view);
        dialogLineWidth = currentAlertDialog.create();
        dialogLineWidth.setTitle("Set line Width");

        dialogLineWidth.show();
    }

    void showTextDialog() {
        // show text input dialog with text size seekbar and preview image

        currentAlertDialog = new AlertDialog.Builder(requireActivity());  // create alert dialog
        View view = getLayoutInflater().inflate(R.layout.text_dialog, null);  // show dialog

        // access components
        SeekBar textSizeSeekbar = view.findViewById(R.id.textSizeSeekBar);
        Button setTextButton = view.findViewById(R.id.textDialogButton);
        textImageView = view.findViewById(R.id.imageViewId);
        EditText editText = (EditText) view.findViewById(R.id.editTextId);

        // initialize text box to most recent text input
        textValue = pikassoView.typedText;
        editText.setText(textValue, TextView.BufferType.EDITABLE);

        // initialize text preview
        textBitmap = Bitmap.createBitmap(1000, 200, Bitmap.Config.ARGB_8888);
        textCanvas = new Canvas(textBitmap);

        // TODO
        if(saved_file){ // if brought up is saved
            ;
        }else{ // if user wants to create a new file
            ;
        }


        textPaint = new Paint();
        textPaint.setColor(Color.BLACK);
        textPaint.setShadowLayer(1f, 0f, 1f, Color.WHITE); // text shadow
        updateTextPreview();

        setTextButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // set pikassoview text value and size and dismiss dialog
                pikassoView.typedText = textValue;
                pikassoView.setTextSize(textSizeSeekbar.getProgress());
                textDialog.dismiss();
                currentAlertDialog = null;
            }
        });

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // set text preview to typed text
                textValue = editText.getText().toString();
                updateTextPreview();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        textSizeSeekbar.setOnSeekBarChangeListener(textSizeSeekbarChange);
        textSizeSeekbar.setProgress(pikassoView.getTextSize());

        currentAlertDialog.setView(view);
        textDialog = currentAlertDialog.create();
        textDialog.setTitle("Enter text");

        textDialog.show();
    }

    private void updateTextPreview() {
        textBitmap.eraseColor(Color.WHITE);
        textCanvas.drawText(textValue, 0, 150, textPaint);
        textImageView.setImageBitmap(textBitmap);
    }

    private SeekBar.OnSeekBarChangeListener textSizeSeekbarChange = new SeekBar.OnSeekBarChangeListener() {


        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            // set preview image text size based on seekbar progress
            textPaint.setTextSize(progress);
            updateTextPreview();
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {}

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {}
    };

    private SeekBar.OnSeekBarChangeListener colorSeekBarChanged = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            pikassoView.setBackgroundColor(Color.argb(
                    alphaSeekBar.getProgress(),
                    redSeekBar.getProgress(),
                    greenSeekBar.getProgress(),
                    blueSeekBar.getProgress()
            ));

            // display the current color
            colorView.setBackgroundColor(Color.argb(
                    alphaSeekBar.getProgress(),
                    redSeekBar.getProgress(),
                    greenSeekBar.getProgress(),
                    blueSeekBar.getProgress()
            ));
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };

    private SeekBar.OnSeekBarChangeListener widthSeekbarChange = new SeekBar.OnSeekBarChangeListener() {
        Bitmap bitmap = Bitmap.createBitmap(400, 100, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            // set stroke width and preview image based on seekbar progress

            Paint p = new Paint();
            p.setColor(pikassoView.getDrawingColor());
            p.setStrokeCap(Paint.Cap.ROUND);
            p.setStrokeWidth(progress);

            bitmap.eraseColor(Color.WHITE);
            canvas.drawLine(30, 50, 370, 50, p);
            widthImageView.setImageBitmap(bitmap);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.go_back) { // if pressed back button
            // goto file view fragment
            navController.navigate(R.id.action_drawingFragment_to_fileListFragment);
        }
    }

    @Override
    public void onStop(){ // on leaving current fragment
        super.onStop(); // if navigate to diff fragment
    }
}