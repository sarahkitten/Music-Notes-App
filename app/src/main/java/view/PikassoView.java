// PikassoView
// By Sarah Kitten based on https://youtu.be/H9CTVyu3Bi8
// View for drawing on the screen

package view;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;

import com.example.musicnotesapp.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Random;
import java.util.Stack;

public class PikassoView extends View {

    public static final float TOUCH_TOLERANCE = 10;
    private Bitmap bitmap;  // where we save the pixels
    private Bitmap bitmapSaveState;  // save state to restore for drag and drop
    private Boolean hasImgLoaded = false;
    private Canvas bitmapCanvas;  // draws the bitmap
    private MusicItem imgCurr;
    private Paint paintScreen;
    private Paint paintLine;
    private Paint paintText;
    private HashMap<Integer, Path> pathMap;
    private HashMap<Integer, Point> previousPointMap;
    public String inputMode = "draw"; // input mode can be "draw", "drag", or "type"

    Drawable draggable_img;  // image to drag/drop
    private float drag_img_width;  // x dimension of draggable img
    private float drag_img_height;  // y dimension of draggable img

    private Drawable loadedImg; // image loaded from save
    private Bitmap bitmapLoaded; // image loaded from save

    private Stack<Path> pathHistory = new Stack<Path>(); // history of drawn paths
    private Stack<Paint> lineHistory = new Stack<Paint>(); // history of each path attr
    private Stack<Float> imgX = new Stack<Float>(); // history of each img x position
    private Stack<Float> imgY = new Stack<Float>(); // history of each img y position
    private Stack<Float> txtX = new Stack<>(); // history of each text x position
    private Stack<Float> txtY = new Stack<Float>(); // history of each text y position
    private Stack<MusicItem> imgHistory = new Stack<MusicItem>(); // history of note types
    private Stack<Integer> drawingMode = new Stack<Integer>(); // history of modes
    private Stack<String> typedTextHistory = new Stack<String>(); // history of texts
    private Stack<Integer> textSizeHistory = new Stack<Integer>(); // history of each text size
    private Stack<Paint> paintTextHistory = new Stack<Paint>(); // history of each text attr
    private float xCoord;
    private float yCoord;

    public String typedText = "text";  // the text to be dragged/dropped in typing mode

    private Context mContext;


    public PikassoView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    public enum MusicItem {
        QUARTER_NOTE,
        HALF_NOTE,
        WHOLE_NOTE,
        STAFF
    }

    void init() {
        // Initialization of PikassoView
        paintScreen = new Paint();

        paintText = new Paint();
        paintText.setAntiAlias(true); // make lines smooth
        paintText.setColor(Color.BLACK); // initialize text color
        paintText.setTextSize(100); // text size in pixels
        paintText.setShadowLayer(1f, 0f, 1f, Color.WHITE); // text shadow

        paintLine = new Paint();
        paintLine.setAntiAlias(true); // make lines smooth
        paintLine.setColor(Color.BLACK); // initialize line color
        paintLine.setStyle(Paint.Style.STROKE);  // initialize style
        paintLine.setStrokeWidth(7);  // initialize line width
        paintLine.setStrokeCap(Paint.Cap.ROUND); // make ends of line round

        pathMap = new HashMap<>();  // map of paths
        previousPointMap = new HashMap<>();

        setDraggable_img(MusicItem.QUARTER_NOTE);

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        // initialize bitmap with screen width/height, use configuration with alpha/red/green/blue channels
        bitmapCanvas = new Canvas(bitmap); // initialize canvas
        bitmap.eraseColor(Color.WHITE);  // set bitmap to all white
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // called whenever the screen appears
        if (hasImgLoaded) {
            loadedImg.setBounds(0,0,getWidth(),getHeight());
        }

        canvas.drawBitmap(bitmap, 0, 0, paintScreen);  // draw bitmap on the screen
        drawCanvas();
        for (Integer key: pathMap.keySet()) {  // loop through pathMap
            canvas.drawPath(pathMap.get(key), paintLine);  // draw pathMap
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        int action = event.getActionMasked();  // event type (pointer down or pointer up)
        int actionIndex = event.getActionIndex();  // index of the pointer ( finger )

        if (action == MotionEvent.ACTION_DOWN ||
                action == MotionEvent.ACTION_POINTER_DOWN) {  // touch has started

            touchStarted(event.getX(actionIndex),  // start touch with pointer coordinates
                    event.getY(actionIndex),
                    event.getPointerId(actionIndex));

        } else if (action == MotionEvent.ACTION_UP ||
                action == MotionEvent.ACTION_POINTER_UP) {  // touch has ended

            touchEnded(event.getPointerId(actionIndex));  // end touch

        } else {
            touchMoved(event);  // move touch
        }

        return true;
    }

    private void drawCanvas() {
        // Draw loaded image (if any) and the paths and paint lines which are in the history
        String lastInputMode = inputMode;  // save current input mode
        MusicItem lastImg = imgCurr;  // save current draggable img
        String lastTypedText = typedText; // save current text
        Integer lastTextSize = getTextSize(); // save current text size

        bitmap.eraseColor(Color.WHITE);  // erase canvas
        if (hasImgLoaded) {
            loadedImg.draw(bitmapCanvas);  // draw loaded image (if any)
        }
        for (int i = 0; i < pathHistory.size(); i++) {
            bitmapCanvas.drawPath(pathHistory.get(i), lineHistory.get(i));
        }
        for (int i = 0; i < imgHistory.size(); i++) {
            inputMode = "drag";
            setDraggable_img(imgHistory.get(i));
            drawDraggable_img(imgX.get(i), imgY.get(i));

        }
        for (int i = 0; i < paintTextHistory.size(); i++) {
            inputMode = "type";
            paintText = paintTextHistory.get(i);
            typedText = typedTextHistory.get(i);
            setTextSize(textSizeHistory.get(i));
            drawDraggable_img(txtX.get(i), txtY.get(i));
        }
        inputMode = lastInputMode;  // revert to saved input mode
        setDraggable_img(lastImg);  // revert to saved draggable img
        typedText = lastTypedText; // revert to saved text
        setTextSize(lastTextSize); // revert to saved text size
    }

    public void getDrawableImgFromPath(String path) {
        hasImgLoaded = true;
        File image = new File(path);
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bitmapLoaded = BitmapFactory.decodeFile(image.getAbsolutePath(),bmOptions);
        //bitmapLoaded = Bitmap.createScaledBitmap(bitmapLoaded,100,100,true);
        loadedImg = new BitmapDrawable(getResources(), bitmapLoaded);
        Log.i(TAG, "getDrawableImgFromPath: called with path " + path);
    }

    private void touchStarted(float x, float y, int pointerId) {
        switch (inputMode) {
            case "draw":
                drawingTouchStarted(x, y, pointerId);
                invalidate();  // redraws the screen for each touch event-- only for drawing
                break;
            case "drag":
            case "type":
                imgDragTouchStarted(x, y, pointerId);
                break;
        }
    }

    private void touchMoved(MotionEvent event) {
        switch (inputMode) {
            case "draw":
                drawingTouchMoved(event);
                invalidate();  // redraws the screen for each touch event-- only for drawing
                break;
            case "drag":
            case "type":
                imgDragTouchMoved(event);
                break;
        }
    }

    private void touchEnded(int pointerId) {
        switch (inputMode) {
            case "draw":
                drawingTouchEnded(pointerId);
                invalidate();  // redraws the screen for each touch event-- only for drawing
                break;
            case "drag":
            case "type":
                imgDragTouchEnded(pointerId);
                invalidate();  // redraws the screen for each touch event-- only for drawing?
                break;
        }
    }

    private void drawingTouchStarted(float x, float y, int pointerId) {
        Path path;  // store the path for a given touch
        Point point;  // store the last point in path

        if (pathMap.containsKey(pointerId)) {  // path map is not empty, continue touch
            path = pathMap.get(pointerId);  // get path
            point = previousPointMap.get(pointerId);  // get point
        } else {  // path map is empty, this is a new touch
            path = new Path();  // create path
            pathMap.put(pointerId, path);  // create path map
            point = new Point();  // create point
            previousPointMap.put(pointerId, point);  // put point in previous point map
        }

        // move to the coordinates of the touch
        path.moveTo(x, y);
        // assign point coordinates
        point.x = (int) x;
        point.y = (int) y;
    }

    private void drawingTouchMoved(MotionEvent event) {
        for (int i = 0; i < event.getPointerCount(); i++) {  // loop through pointers

            int pointerId = event.getPointerId(i);  // get pointer id
            int pointerIndex = event.findPointerIndex(pointerId);  // get pointer index

            if (pathMap.containsKey(pointerId)) {
                // get new coordinates
                float newX = event.getX(pointerIndex);
                float newY = event.getY(pointerIndex);

                Path path = pathMap.get(pointerId);  // get the path associated with this pointer
                Point point = previousPointMap.get(pointerId);  // get the previous point on this path

                // Calculate how far the user moved from the last update
                float deltaX = Math.abs(newX - point.x);
                float deltaY = Math.abs(newY - point.y);

                // Check if the distance is significant enough to be considered a movement
                if (deltaX >= TOUCH_TOLERANCE ||
                        deltaY >= TOUCH_TOLERANCE) {
                    // move the path to the new location
                    path.quadTo(point.x, point.y,
                            (newX + point.x) / 2,
                            (newY + point.y) / 2);

                    // store the new coordinates
                    point.x = (int) newX;
                    point.y = (int) newY;
                }
            }
        }
    }

    private void drawingTouchEnded(int pointerId) {
        Path path = pathMap.get(pointerId);  // get the corresponding path
        pathHistory.push(new Path(path)); // push line and attr to history
        lineHistory.push(new Paint(paintLine));
        drawingMode.push(1);
        bitmapCanvas.drawPath(path, paintLine);  // draw to bitmapCanvas
        path.reset();  // reset path
    }

    private void imgDragTouchStarted(float x, float y, int pointerId) {
        // save bitmap state and draw draggable_img at x, y
        bitmapSaveState = bitmap.copy(Bitmap.Config.ARGB_8888, true);  // save bitmap state
        drawDraggable_img(x, y);  // draw img
        xCoord = x;
        yCoord = y;
        Log.i(TAG, "imgDragTouchStarted: Calling drawDraggableImg");
        addImgToHistory();
        invalidate();
    }

    private void imgDragTouchMoved(MotionEvent event) {
        // get rid of previous drawn image, draw image at new pointer position
        bitmapCanvas.drawBitmap(bitmapSaveState, 0, 0, null);  // restore bitmap save state
        for (int i = 0; i < event.getPointerCount(); i++) {  // loop through pointers
            int pointerId = event.getPointerId(i);  // get pointer id
            int pointerIndex = event.findPointerIndex(pointerId);  // get pointer index
            // get new coordinates
            xCoord = event.getX(pointerIndex);
            yCoord = event.getY(pointerIndex);
            drawDraggable_img(xCoord, yCoord);  // draw img
            undo();
            addImgToHistory();
            invalidate();
        }
    }

    private void imgDragTouchEnded(int pointerId) {
        undo();
        addImgToHistory();
    }

    private void addImgToHistory() { // Add image (text or image) and attributes to history
        if (inputMode.equals("type")) { // add text
            typedTextHistory.push(typedText);
            paintTextHistory.push(paintText);
            textSizeHistory.push(getTextSize());
            drawingMode.push(2);
            txtX.push(xCoord);
            txtY.push(yCoord);
        }
        else { // add image
            imgHistory.push(imgCurr);
            drawingMode.push(0);
            imgX.push(xCoord);
            imgY.push(yCoord);
        }
        Log.i(TAG, "addImgToHistory: x: " + xCoord + "y: " + yCoord + "img: " + imgCurr);
    }

    private void drawDraggable_img(float x, float y) {
        // draw current draggable image centered on coords x, y
        if (inputMode.equals("drag")) {  // drag image
            draggable_img.setBounds(  // set img bounds (size/position)
                    (int) (x - drag_img_width /2), // left
                    (int) (y - drag_img_height /2), // top
                    (int) (x + drag_img_width /2),  // right
                    (int) (y + drag_img_height /2));  // bottom
            draggable_img.draw(bitmapCanvas);  // draw image
        } else {  // drag text
            bitmapCanvas.drawText(typedText, x, y, paintText);
        }

        //Log.i(TAG, "drawDraggable_img: (" + xCoord + ", " + yCoord + ')');
    }

    public void setDrawingColor(int color) {
        paintLine.setColor(color);
    }

    public int getDrawingColor() {
        return  paintLine.getColor();
    }

    public void setLineWidth(int width) {
        paintLine.setStrokeWidth(width);
    }

    public int getLineWidth() {
        return (int) paintLine.getStrokeWidth();
    }

    public void setTextSize(int size) {
        paintText.setTextSize(size);
    }

    public int getTextSize() {
        return (int) paintText.getTextSize();
    }

    public void setDraggable_img(MusicItem item) {
        // set the draggable image to a drawable in the res folder
        Log.i(TAG, "setDraggable_img: setting to " + item);
        float size_multiplier_x = 1f;
        float size_multiplier_y = 1f;
        switch (item) {
            case HALF_NOTE:
                draggable_img = ResourcesCompat.getDrawable(getResources(), R.drawable.half_note, null);
                size_multiplier_x = size_multiplier_y = 0.15f;
                break;
            case QUARTER_NOTE:
                draggable_img = ResourcesCompat.getDrawable(getResources(), R.drawable.quarter_note, null);
                size_multiplier_x = size_multiplier_y = 0.17f;
                break;
            case WHOLE_NOTE:
                draggable_img = ResourcesCompat.getDrawable(getResources(), R.drawable.whole_note, null);
                size_multiplier_x = size_multiplier_y = 0.0345f;
                break;
            case STAFF:
                draggable_img = ResourcesCompat.getDrawable(getResources(), R.drawable.staff, null);
                size_multiplier_x = 0.15f;
                size_multiplier_y = 0.1f;
                break;
        }
        imgCurr = item; // keep track of current note type
        // set the width/height
        drag_img_width = draggable_img.getIntrinsicWidth() * size_multiplier_x;
        drag_img_height = draggable_img.getIntrinsicHeight() * size_multiplier_y;
    }

    public void rotateInputMode() {
        // temp: go to next input mode
        switch (inputMode) {
            case "draw":
                inputMode = "drag";
                break;
            case "drag":
                inputMode = "type";
                break;
            case "type":
                inputMode = "draw";
                break;
        }
    }

    public void clear() { // clears bitmap of all paths/images and resets history
        pathMap.clear();  // removes all of the paths
        previousPointMap.clear();  // clear map of previous points
        pathHistory.clear(); // clear line and path history
        lineHistory.clear(); // clear image history
        drawingMode.clear();
        imgX.clear();
        imgY.clear();
        imgHistory.clear();
        txtX.clear();
        txtY.clear();
        typedTextHistory.clear();
        paintTextHistory.clear();
        textSizeHistory.clear();
        bitmap.eraseColor(Color.WHITE);  // erase bitmap
        hasImgLoaded = false;
        invalidate(); // refresh the screen
    }

    public void saveToInternalStorage() {
        // This is the preliminary code for saving images- it saves as a JPEG with a default filename
        // We should:
        // Add user filename input,
        // Figure out how to save/retrieve the text/drawings/images in an editable way (bitmap?),
        // Hook up to directory system
        // Keep the code for JPEG export because that still is useful



        ContextWrapper cw = new ContextWrapper(getContext());

        // Autogenerated filename-- we want to make this user-inputted (but maybe keep this as the default?)

        SimpleDateFormat formatter = new SimpleDateFormat("yyy_MM_dd_hh_mm_ss", Locale.US);
        // create date object
        Date now = new Date();

        String filename = "Note_" + formatter.format(now);

        //path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        // create imageDir
        File mypath = new File(directory, filename + ".jpg");

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            // use the compress method on the BitMap object to write image to the OutputStream
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.flush();
                fos.close();
                Log.d("Image:", directory.getAbsolutePath());
                // show success message
                Toast message = Toast.makeText(getContext(), "Image Saved" + directory.getAbsolutePath(), Toast.LENGTH_LONG);
                message.setGravity(Gravity.CENTER, message.getXOffset() / 2,
                        message.getYOffset() / 2);
                message.show();
                //showSuccessMessage();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void showSuccessMessage() {
        Toast message = Toast.makeText(getContext(), "Image Saved", Toast.LENGTH_LONG);
        message.setGravity(Gravity.CENTER, message.getXOffset() / 2,
                message.getYOffset() / 2);
        message.show();
    }

    private void loadImageFromStorage(String path) {
        // currently unused
        try {
            File f = new File(path, "profile.jpg");
            Bitmap b = BitmapFactory.decodeStream(new FileInputStream(f));
            // Code to retrieve image
//            ImageView img = (ImageView)findViewById(R.id.imgPicker);
//            img.setImageBitmap(b);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void undo() { // undoes last added line/image/text
        Log.i(TAG, "undo: called");
        if (drawingMode.size() > 0) // checks if anything is drawn
        {
            int n = drawingMode.pop();
            if (n == 1) { // if last element is in drawing mode
                pathHistory.pop(); // Remove the last path from the history and attrs
                lineHistory.pop();
            }
            else if (n == 0){ // if last element is in dragging mode
                imgHistory.pop(); // Remove the last img from the history and attrs
                imgX.pop();
                imgY.pop();
            }
            else if (n == 2){ // if last element is in text mode
                paintTextHistory.pop(); // Remove the last text from the history and attrs
                typedTextHistory.pop();
                textSizeHistory.pop();
                txtX.pop();
                txtY.pop();
            }
        }
        drawCanvas(); // Redraw canvas
    }
}
