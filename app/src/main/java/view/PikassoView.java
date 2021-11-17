// PikassoView
// By Sarah Kitten based on https://youtu.be/H9CTVyu3Bi8
// View for drawing on the screen

package view;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.musicnotesapp.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;

public class PikassoView extends View {

    public static final float TOUCH_TOLERANCE = 10;
    private Bitmap bitmap;  // where we save the pixels
    private Canvas bitmapCanvas;  // draws the bitmap
    private Paint paintScreen;
    private Paint paintLine;
    private HashMap<Integer, Path> pathMap;
    private HashMap<Integer, Point> previousPointMap;

    public PikassoView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    void init() {
        // Initialization of PikassoView
        paintScreen = new Paint();
        paintLine = new Paint();
        paintLine.setAntiAlias(true); // make lines smooth
        paintLine.setColor(Color.BLACK); // initialize line color
        paintLine.setStyle(Paint.Style.STROKE);  // initialize style
        paintLine.setStrokeWidth(7);  // initialize line width
        paintLine.setStrokeCap(Paint.Cap.ROUND); // make ends of line round

        pathMap = new HashMap<>();  // map of paths
        previousPointMap = new HashMap<>();
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
        canvas.drawBitmap(bitmap, 0, 0, paintScreen);  // draw bitmap on the screen
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

        invalidate();  // redraws the screen for each touch event

        return true;
    }

    private void touchMoved(MotionEvent event) {

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

    public void clear() {
        pathMap.clear();  // removes all of the paths
        previousPointMap.clear();  // clear map of previous points
        bitmap.eraseColor(Color.WHITE);  // erase bitmap
        invalidate(); // refresh the screen
    }

    private void touchEnded(int pointerId) {
        Path path = pathMap.get(pointerId);  // get the corresponding path
        bitmapCanvas.drawPath(path, paintLine);  // draw to bitmapCanvas
        path.reset();  // reset path
    }

    private void touchStarted(float x, float y, int pointerId) {
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

    public void saveToInternalStorage() {
        // This is the preliminary code for saving images- it saves as a JPEG with a default filename
        // We should:
        // Add user filename input,
        // Figure out how to save/retrieve the text/drawings/images in an editable way (bitmap?),
        // Hook up to directory system
        // Keep the code for JPEG export because that still is useful

        ContextWrapper cw = new ContextWrapper(getContext());

        // Autogenerated filename-- we want to make this user-inputted (but maybe keep this as the default?)
        String filename = "Pikasso" + System.currentTimeMillis();

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
                // show success message-- this is a bit broken
                Toast message = Toast.makeText(getContext(), "Image Saved +" + directory.getAbsolutePath(), Toast.LENGTH_LONG);
                message.setGravity(Gravity.CENTER, message.getXOffset() / 2,
                        message.getYOffset() / 2);
                message.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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
}