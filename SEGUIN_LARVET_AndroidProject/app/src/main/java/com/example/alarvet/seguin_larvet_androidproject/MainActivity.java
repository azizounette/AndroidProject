package com.example.alarvet.seguin_larvet_androidproject;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Toast;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;


public class MainActivity extends AppCompatActivity {

    /**
     * The bitmap on which we apply changes that have been validated by the user.
     */
    private Bitmap bitmap;

    /**
     * The bitmap used for temporary changes.
     */
    private Bitmap appliedBitmap;

    /**
     * The bitmap we keep in case of a reset.
     */
    private Bitmap originalBitmap;

    /**
     * Button used to save the picture.
     */
    private Button saveButton;

    /**
     * Maximum width of the bitmap. It is used for rescaling.
     */
    private static final int MAX_BITMAP_WIDTH = 1000;

    /**
     * Maximum height of the bitmap. It is used for rescaling.
     */
    private static final int MAX_BITMAP_HEIGHT = 1000;

    /**
     * The View used to display the image.
     */
    private ImageView imageView;

    /**
     * The number bound to the request used when the user wants to pick an image
     * from the gallery.
     */
    private static final int REQUEST_IMAGE_GALLERY = 1;

    /**
     * The number bound to the request used when the user wants to take a picture
     * with the camera.
     */
    private static final int REQUEST_TAKE_PHOTO = 2;

    //Variables used for permissions
    /**
     * Used for permissions.
     * If permission for using camera is granted, it is true.
     */
    private static boolean canTakePicture =  false;

    /**
     * Used for permissions.
     * If permission for reading external storage is granted,it is true.
     */
    private static boolean canSave = false;

    /**
     * Used for permissions.
     * If permission for writing on external storage is granted, it is true.
     */
    private static boolean canPickFromGallery = false;

    //Variables used for applying filters to the image
    /**
     * The filter used to apply colour filters to the picture.
     * @see Colour
     */
    private Colour colourFilter;

    /**
     * The filter used to apply complex filters to the picture.
     * @see ComplexFilter
     */
    private ComplexFilter complexFilter;

    /**
     * The filter used to change the contrast of the picture.
     * @see Contrast
     */
    private Contrast contrastFilter;

    /**
     * The filter used to apply Convolution masks to the picture.
     * @see Convolution
     */
    private Convolution convolutionFilter;

    /**
     * The filter used to change the luminosity of the picture.
     * @see Luminosity
     */
    private Luminosity luminosityFilter;

    /**
     * Used to change the hue of the picture and keep track of it.
     */
    private int hue = 0;

    /**
     * Used to change the saturation of the picture and keep track of it.
     */
    private float saturation = 0;

    /**
     * Used to change the luminosity of the picture and keep track of it.
     */
    private float value = 0;

    /**
     * Bar used to change the hue of the picture.
     */
    private SeekBar hueBar;

    /**
     * Bar used to change the saturation of the picture.
     */
    private SeekBar saturationBar;

    /**
     * Used to change the luminosity of the picture.
     */
    private SeekBar valueBar;

    /**
     * Bar used to change the contrast of the picture.
     */
    private SeekBar contrastBar;

    /**
     * Bar used to change the luminosity of the picture.
     */
    private SeekBar luminosityBar;

    /**
     * Bar used to apply a surprising change to the picture.
     * @see Contrast#magicWand(int)
     */
    private SeekBar magicWandBar;

    /**
     * Bar used to apply a feeling of warmth/cold to the picture.
     */
    private SeekBar warmthBar;

    /**
     * Bar used to apply an Andy Warhol effect to the picture.
     */
    private SeekBar warholBar;

    /**
     * Used to save a picture to the gallery after it was taken with the camera.
     */
    private Uri fileSavePic;

    /**
     * Matrix used for scaling of the picture. It is used for touch events.
     * It is used for zooming and scrolling.
     */
    Matrix matrix = new Matrix();

    /**
     * Matrix used for scaling of the picture. It is used for touch events.
     * It is used for zooming and scrolling.
     * It is used to save the state of the matrix before zooming/scrolling on the picture.
     */
    Matrix savedMatrix = new Matrix();

    // States used for scrolling/zooming.
    /**
     * Mode in which we are when we are not zooming or scrolling. Idle state.
     */
    private static final int NONE = 0;

    /**
     * Mode in which we are when we are scrolling.
     */
    private static final int DRAG = 1;

    /**
     * Mode in which we are when we are zooming.
     */
    private static final int ZOOM = 2;

    /**
     * Mode in which we currently are.
     */
    int mode = NONE;

    // Variables used for scaling
    /**
     * Point used to represent the position on the screen
     * of the first finger touching the screen.
     */
    PointF start = new PointF();

    /**
     * Point used to represent the position on the screen
     * of the middle position between the first two fingers touching the screen.
     */
    PointF mid = new PointF();

    /**
     * Distance between the positions where the fingers touched the screen initially.
     */
    float oldDist = 1f;


    // Variable used for saving the bitmap before an orientation change
    /**
     * Used to save the bitmap on which we apply the changes before an orientation change.
     */
    private static final String SAVE_BMP = "SaveBitmap";

    /**
     * Used to save the original bitmap before an orientation change.
     */
    private static final String SAVE_ORIGINAL_BMP = "SaveOriginalBitmap";

    /**
     * Used to save the bitmap used to apply temporary changes before an orientation change.
     */
    private static final String SAVE_APPLIED_BMP = "SaveAppliedBitmap";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = (ImageView) findViewById(R.id.imageView);

        if (savedInstanceState != null) {
            originalBitmap = savedInstanceState.getParcelable(SAVE_ORIGINAL_BMP);
            bitmap = savedInstanceState.getParcelable(SAVE_BMP);
            appliedBitmap = savedInstanceState.getParcelable(SAVE_APPLIED_BMP);
            imageView.setImageBitmap(bitmap);
        } else {
            checkPermissions();
            originalBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
            appliedBitmap = originalBitmap.copy(originalBitmap.getConfig(), true);
            bitmap = originalBitmap.copy(originalBitmap.getConfig(), true);
            imageView.setImageBitmap(bitmap);
        }

        imageView.setOnTouchListener(handleTouch);

        createSeekBar();

        setBarVisibility(View.GONE,View.GONE,View.GONE,View.GONE,View.GONE,View.GONE,View.GONE,View.GONE);

        saveButton = (Button) findViewById(R.id.saveButton);
        saveButton.setOnClickListener(saveButtonListener);
        if (!canSave) {
            saveButton.setVisibility(View.INVISIBLE);
        }
    }


    @Override
    protected void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        outState.putParcelable(SAVE_BMP, bitmap);
        outState.putParcelable(SAVE_ORIGINAL_BMP, originalBitmap);
        outState.putParcelable(SAVE_APPLIED_BMP, appliedBitmap);
    }

    /**
     * Creates all the bars that we need to apply changes to our picture.
     */
    public void createSeekBar() {
        hueBar = (SeekBar) findViewById(R.id.hueBar);
        hueBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar hBar, int progress, boolean fromUser) {
                resetAppliedBitmap();
                hue = progress;
                colourFilter.changeTint(hue, saturation, value);
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        saturationBar = (SeekBar) findViewById(R.id.saturationBar);
        saturationBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar saturationBar, int progress, boolean fromUser) {
                resetAppliedBitmap();
                saturation = (float) progress/100;
                colourFilter.changeTint(hue, saturation, value);
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        valueBar = (SeekBar) findViewById(R.id.valueBar);
        valueBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar valueBar, int progress, boolean fromUser) {
                resetAppliedBitmap();
                value = (float) progress/100;
                colourFilter.changeTint(hue, saturation, value);
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        contrastBar = (SeekBar) findViewById(R.id.contrastBar);
        contrastBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar contrastBar, int progress, boolean fromUser) {
                resetAppliedBitmap();
                contrastFilter.contrastChange(progress-128);
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        luminosityBar = (SeekBar) findViewById(R.id.luminosityBar);
        luminosityBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar luminosityBar, int progress, boolean fromUser) {
                resetAppliedBitmap();
                luminosityFilter.luminosityChange(progress-100);
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        magicWandBar = (SeekBar) findViewById(R.id.magicWandBar);
        magicWandBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar magicWandBar, int progress, boolean fromUser) {
                resetAppliedBitmap();
                contrastFilter.magicWand(progress-100);
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        warmthBar = (SeekBar) findViewById(R.id.warmthBar);
        warmthBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar warmthBar, int progress, boolean fromUser) {
                resetAppliedBitmap();
                contrastFilter.warmthChange(progress-100);
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        warholBar = (SeekBar) findViewById(R.id.warholBar);
        warholBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar warholBar, int progress, boolean fromUser) {
                resetAppliedBitmap();
                complexFilter.warhol(progress);
            }

            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    /* HANDLING TOUCH EVENTS */

    View.OnTouchListener handleTouch = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {

            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    savedMatrix.set(matrix);
                    start.set(event.getX(), event.getY());
                    mode = DRAG;
                    break;

                case MotionEvent.ACTION_POINTER_DOWN:
                    oldDist = spacing(event);
                    if (oldDist > 10f) {
                        savedMatrix.set(matrix);
                        midPoint(mid, event);
                        mode = ZOOM;
                    }
                    break;

                case MotionEvent.ACTION_POINTER_UP:
                    mode = NONE;
                    break;

                case MotionEvent.ACTION_UP:
                    mode = NONE;
                    break;

                case MotionEvent.ACTION_MOVE:
                    if (mode == DRAG) {
                        matrix.set(savedMatrix);
                        matrix.postTranslate(event.getX() - start.x, event.getY()
                                - start.y);
                    } else if (mode == ZOOM) {
                        float newDist = spacing(event);
                        if (newDist > 10f) {
                            matrix.set(savedMatrix);
                            float scale = newDist / oldDist;
                            matrix.postScale(scale, scale, mid.x, mid.y);
                        }
                    }
                    break;
            }
            imageView.setImageMatrix(matrix);
            return true;
        }
    };

    /**
     * Computes the distance between positions of fingers given by a MotionEvent.
     * @param event The touch event we get positions of the fingers from.
     * @return The distance between the first two fingers.
     */
    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float)Math.sqrt(x * x + y * y);
    }


    /**
     * Computes the mid point of the first two fingers
     * @param point The point that is going to be the computed mid point.
     * @param event The MotionEvent we get positions of the fingers from.
     */
    private void midPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }

    /* END OF TOUCH EVENTS */


    /* OPERATIONS ON BITMAPS */

    /**
     * Resets the ImageView to an icon and clears all the bitmaps that we use.
     * It is used for performance and memory leaks issues.
     */
    private void clearBitmap(){
        imageView.setImageResource(R.mipmap.ic_launcher);
        bitmap.recycle();
        bitmap = null;

        appliedBitmap.recycle();
        appliedBitmap = null;

        originalBitmap.recycle();
        originalBitmap = null;
    }

    /**
     * Re-sizes a bitmap thanks to the given parameters.
     * @param bm The bitmap to resize.
     * @param maxWidth The maximum width we want for our resized bitmap.
     * @param maxHeight The maximum height we want for our resized bitmap.
     * @return The resized bitmap.
     */
    public Bitmap getResizedBitmap(Bitmap bm, int maxWidth, int maxHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        int max = Math.max(width, height);
        int newW = (width * maxWidth) / max;
        int newH = (height * maxHeight) / max;
        return Bitmap.createScaledBitmap(
                bm, newW, newH, false);
    }

    /**
     * Resets the bitmap we apply changes on to the original bitmap (that does not
     * have any change) and recreates the filters.
     */
    private void resetBitmap(){
        bitmap = originalBitmap.copy(originalBitmap.getConfig(), true);
        appliedBitmap = originalBitmap.copy(originalBitmap.getConfig(), true);
        imageView.setImageBitmap(bitmap);
        createFilters();
    }

    /**
     * Resets the appliedBitmap to the original bitmap (that does not
     * have any change) and recreates the filters.
     */
    private void resetAppliedBitmap () {
        bitmap = appliedBitmap.copy(originalBitmap.getConfig(), true);
        imageView.setImageBitmap(bitmap);
        createFilters();
    }

    /* END OPERATIONS ON BITMAPS */

    /* GETTING PICTURES FROM CAMERA OR GALLERY */

    /**
     * Handles the case where the user wants to take a picture thanks to an intent.
     */
    private void takePicture(){
        clearBitmap();

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            fileSavePic = Uri.fromFile(getOutputMediaFile());
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileSavePic);

            startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
        } else {
            Toast.makeText(this, "Impossible to take a picture", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Creates a directory and a file.
     * @return The created file.
     */
    private static File getOutputMediaFile(){
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "SEGUIN_LARVET");

        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()){
            return null;
        }

        String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date());
        return new File(mediaStorageDir.getPath() + File.separator +
                "IMG_"+ timeStamp + ".jpg");
    }

    /**
     * Handles the case where the user wants to pick a picture from the gallery
     * thanks to an intent.
     */
    public void onImageGalleryClicked() {
        clearBitmap();

        Intent picturePickedIntent = new Intent(Intent.ACTION_PICK);

        if (picturePickedIntent.resolveActivity(getPackageManager()) != null) {
            File pictureDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_ALARMS);
            String pictureDirectoryPath = pictureDirectory.getPath();

            Uri data = Uri.parse(pictureDirectoryPath);

            picturePickedIntent.setDataAndType(data, "image/*");
            startActivityForResult(picturePickedIntent, REQUEST_IMAGE_GALLERY);
        } else {
            Toast.makeText(this, "Impossible to pick a picture from the gallery", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK){
            switch(requestCode){
                case REQUEST_TAKE_PHOTO:
                    imageView.setImageURI(fileSavePic);
                    BitmapDrawable bitmapDrawable = ((BitmapDrawable) imageView.getDrawable());
                    if(bitmapDrawable == null){
                        imageView.buildDrawingCache();
                        originalBitmap = imageView.getDrawingCache();
                        originalBitmap = getResizedBitmap(originalBitmap, MAX_BITMAP_WIDTH, MAX_BITMAP_HEIGHT);
                        imageView.buildDrawingCache(false);
                    } else{
                        originalBitmap = bitmapDrawable.getBitmap();
                        originalBitmap = getResizedBitmap(originalBitmap, MAX_BITMAP_WIDTH, MAX_BITMAP_HEIGHT);
                    }
                    appliedBitmap = originalBitmap.copy(originalBitmap.getConfig(), true);
                    bitmap = originalBitmap.copy(originalBitmap.getConfig(), true);
                    imageView.setImageBitmap(bitmap);
                    createFilters();
                    break;

                case REQUEST_IMAGE_GALLERY:
                    Uri imageUri = data.getData(); // address of image on SD card

                    InputStream inputStream; // stream to read the image data
                    try {
                        inputStream = getContentResolver().openInputStream(imageUri);
                        originalBitmap = BitmapFactory.decodeStream(inputStream);
                        originalBitmap = getResizedBitmap(originalBitmap, MAX_BITMAP_WIDTH, MAX_BITMAP_HEIGHT);
                        appliedBitmap = originalBitmap.copy(originalBitmap.getConfig(), true);
                        bitmap = originalBitmap.copy(originalBitmap.getConfig(), true);
                        imageView.setImageBitmap(bitmap);
                        createFilters();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Impossible to open the image", Toast.LENGTH_LONG).show();
                    }
                    break;
            }
        }
    }

    /* END GETTING PICTURES */

    /**
     * Saves an image to the gallery of the phone.
     */
    protected void saveImage(){
        File saveFile = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "SEGUIN_LARVET");
        String root = saveFile.toString();

        File myDir = new File(root);
        myDir.mkdirs();
        String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date());

        String imageName = "IMG_" + timeStamp + ".jpg";
        File file = new File(myDir, imageName);
        if (file.exists()){
            file.delete();
        }

        try {
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
            Toast.makeText(this, "Image saved", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Impossible to save the image", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Creates all the filters used to apply changes to the bitmap.
     */
    private void createFilters() {
        colourFilter = new Colour(bitmap);
        convolutionFilter = new Convolution(bitmap);
        complexFilter = new ComplexFilter(bitmap);
        contrastFilter = new Contrast(bitmap);
        luminosityFilter = new Luminosity(bitmap);
    }

    /**
     * Checks the permissions for the application.
     */
    private void checkPermissions() {
        int apiLevel = Build.VERSION.SDK_INT;
        String[] permissions;
        if (apiLevel < 16) {
            permissions = new String[]{Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE};
        } else {
            permissions = new String[]{Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE};
        }

        ActivityCompat.requestPermissions(this,permissions, 0);
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
    }

    /**
     * The usual onRequestPermissionsResult is used. Then, the booleans used to save the permissions
     * are updated according to the user's wishes. The menu is invalidated in order to be reset
     * according to the new permissions: if the permission to read from external storage is granted,
     * the menu item bound to picking a picture from the gallery is visible, and hidden otherwise ;
     * if the permission to use the camera is granted, the menu item bound to taking a picture is
     * visible, and hidden otherwise.
     * The visibility of the button used to save the picture is changed according to the permission
     * to write on external storage.
     * @param requestCode Code given by ActivityCompat.requestPermissions().
     * @param permissions The permissions we ask for.
     * @param grantResults The results for the permissions.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        canTakePicture = (grantResults[0] == PackageManager.PERMISSION_GRANTED);
        canSave = (grantResults[1] == PackageManager.PERMISSION_GRANTED);
        canPickFromGallery = (grantResults[2] == PackageManager.PERMISSION_GRANTED);
        if (!canSave) {
            saveButton.setVisibility(View.INVISIBLE);
        } else {
            saveButton.setVisibility(View.VISIBLE);
        }
        invalidateOptionsMenu();
    }

    /**
     * Changes the bars' visibility.
     * @param hueVisibility The new visibility of the hue bar.
     * @param saturationVisibility The new visibility of the saturation bar.
     * @param valueVisibility The new visibility of the value bar.
     * @param contrastVisibility The new visibility of the contrast bar.
     * @param luminosityVisibility The new visibility of the luminosity bar.
     * @param magicWandVisibility The new visibility of the magic wand bar.
     * @param warmthVisibility The new visibility of the warmth bar.
     * @param warholVisibility The new visibility of the warhol bar.
     */
    public void setBarVisibility(int hueVisibility, int saturationVisibility, int valueVisibility,
                                 int contrastVisibility, int luminosityVisibility, int magicWandVisibility,
                                 int warmthVisibility, int warholVisibility){
        hueBar.setVisibility(hueVisibility);
        saturationBar.setVisibility(saturationVisibility);
        valueBar.setVisibility(valueVisibility);
        contrastBar.setVisibility(contrastVisibility);
        luminosityBar.setVisibility(luminosityVisibility);
        magicWandBar.setVisibility(magicWandVisibility);
        warmthBar.setVisibility(warmthVisibility);
        warholBar.setVisibility(warholVisibility);
    }

    /**
     * Changes the visibility of seekBars and reset the bitmap used for temporary changes.
     * This way, when the user wants to make a new change to the picture, the temporary changes vanish
     * and the right seekBars appear.
     *
     * @param hueVisibility The new visibility of the hue bar.
     * @param saturationVisibility The new visibility of the saturation bar.
     * @param valueVisibility The new visibility of the value bar.
     * @param contrastVisibility The new visibility of the contrast bar.
     * @param luminosityVisibility The new visibility of the luminosity bar.
     * @param magicWandVisibility The new visibility of the magic wand bar.
     * @param warmthVisibility The new visibility of the warmth bar.
     * @param warholVisibility The new visibility of the warhol bar.
     */
    public void onFilterCalled (int hueVisibility, int saturationVisibility, int valueVisibility,
                                int contrastVisibility, int luminosityVisibility, int magicWandVisibility,
                                int warmthVisibility, int warholVisibility) {
        setBarVisibility(hueVisibility, saturationVisibility, valueVisibility, contrastVisibility,
                luminosityVisibility, magicWandVisibility, warmthVisibility, warholVisibility);
        resetAppliedBitmap();
    }

    /**
     * Listener bound to the button used to save a picture.
     */
    private View.OnClickListener saveButtonListener = new View.OnClickListener(){
        public void onClick(View v){
            saveImage();
        }
    };

    public boolean onPrepareOptionsMenu(Menu menu) {
        invalidateOptionsMenu();
        menu.getItem(0).setVisible(canTakePicture);
        menu.getItem(1).setVisible(canPickFromGallery);
        super.onPrepareOptionsMenu(menu);
        return true;
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(MainActivity.this);
        View mView = getLayoutInflater().inflate(R.layout.dialog_spinner, null);
        final Spinner mSpinner = mView.findViewById(R.id.spinner);
        AlertDialog dialog;
        ArrayAdapter<String> adapter;
        String[] mArray;

        switch (item.getItemId()) {
            case R.id.applyButton:
                appliedBitmap = bitmap.copy(bitmap.getConfig(), true);
                return true;
            case R.id.resetButton:
                resetBitmap();
                return true;
            case R.id.phototaking:
                takePicture();
                return true;
            case R.id.gallery:
                onImageGalleryClicked();
                return true;
            case R.id.cartoon:
                onFilterCalled(View.GONE,View.GONE,View.GONE,View.GONE,View.GONE,View.GONE,View.GONE,View.GONE);
                mBuilder.setTitle("Amount of Blur desired");
                mArray = getResources().getStringArray(R.array.threshold);
                adapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_spinner_item, mArray);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                mSpinner.setAdapter(adapter);
                mBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        complexFilter.cartoon(mSpinner.getSelectedItemPosition());
                        dialogInterface.dismiss();
                    }
                });
                mBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                mBuilder.setView(mView);
                dialog = mBuilder.create();
                dialog.show();
                return true;
            case R.id.warhol:
                onFilterCalled(View.GONE,View.GONE,View.GONE,View.GONE,View.GONE,View.GONE,View.GONE,View.VISIBLE);
                return true;
            case R.id.sharpening:
                onFilterCalled(View.GONE,View.GONE,View.GONE,View.GONE,View.GONE,View.GONE,View.GONE,View.GONE);
                convolutionFilter.convolution(convolutionFilter.convolutionMatrix(4,3));
                return true;
            case R.id.laplacien:
                onFilterCalled(View.GONE,View.GONE,View.GONE,View.GONE,View.GONE,View.GONE,View.GONE,View.GONE);
                convolutionFilter.laplacien();
                return true;
            case R.id.contouring:
                onFilterCalled(View.GONE,View.GONE,View.GONE,View.GONE,View.GONE,View.GONE,View.GONE,View.GONE);
                convolutionFilter.contouring();
                return true;
            case R.id.luminosityChange:
                onFilterCalled(View.GONE,View.GONE,View.GONE,View.GONE,View.VISIBLE,View.GONE,View.GONE,View.GONE);
                return true;
            case R.id.overexposure:
                onFilterCalled(View.GONE,View.GONE,View.GONE,View.GONE,View.GONE,View.GONE,View.GONE,View.GONE);
                luminosityFilter.overexposure();
                return true;
            case R.id.magicWand:
                onFilterCalled(View.GONE,View.GONE,View.GONE,View.GONE,View.GONE,View.GONE,View.VISIBLE,View.GONE);
                return true;
            case R.id.warmthChange:
                onFilterCalled(View.GONE,View.GONE,View.GONE,View.GONE,View.GONE,View.VISIBLE,View.GONE,View.GONE);
                return true;
            case R.id.contrastChange:
                onFilterCalled(View.GONE,View.GONE,View.GONE,View.VISIBLE,View.GONE,View.GONE,View.GONE,View.GONE);
                return true;
            case R.id.equalizeColors:
                onFilterCalled(View.GONE,View.GONE,View.GONE,View.GONE,View.GONE,View.GONE,View.GONE,View.GONE);
                contrastFilter.equalizeColors();
                return true;
            case R.id.equalizeGray:
                onFilterCalled(View.GONE,View.GONE,View.GONE,View.GONE,View.GONE,View.GONE,View.GONE,View.GONE);
                contrastFilter.equalizeGray();
                return true;
            case R.id.sepia:
                onFilterCalled(View.GONE,View.GONE,View.GONE,View.GONE,View.GONE,View.GONE,View.GONE,View.GONE);
                colourFilter.sepia();
                return true;
            case R.id.grayAndTint:
                onFilterCalled(View.GONE,View.GONE,View.GONE,View.GONE,View.GONE,View.GONE,View.GONE,View.GONE);
                mBuilder.setTitle("Choose the hue of the color wanted");
                mArray = getResources().getStringArray(R.array.colorHue);
                adapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_spinner_item, mArray);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                mSpinner.setAdapter(adapter);
                mBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        colourFilter.grayAndTint(10*mSpinner.getSelectedItemPosition());
                        dialogInterface.dismiss();
                    }
                });
                mBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                mBuilder.setView(mView);
                dialog = mBuilder.create();
                dialog.show();
                return true;
            case R.id.toGray:
                onFilterCalled(View.GONE,View.GONE,View.GONE,View.GONE,View.GONE,View.GONE,View.GONE,View.GONE);
                colourFilter.toGray();
                return true;
            case R.id.changeTint:
                onFilterCalled(View.VISIBLE,View.VISIBLE,View.VISIBLE,View.GONE,View.GONE,View.GONE,View.GONE,View.GONE);
                return true;
            case R.id.averageBlurring:
                onFilterCalled(View.GONE,View.GONE,View.GONE,View.GONE,View.GONE,View.GONE,View.GONE,View.GONE);
                mBuilder.setTitle("Amount of Blur desired");
                mArray = getResources().getStringArray(R.array.amountBlur);
                adapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_spinner_item, mArray);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                mSpinner.setAdapter(adapter);
                mBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        convolutionFilter.averageBlurring(mSpinner.getSelectedItemPosition());
                        dialogInterface.dismiss();
                    }
                });
                mBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                mBuilder.setView(mView);
                dialog = mBuilder.create();
                dialog.show();
                return true;
            case R.id.gaussianBlurring:
                onFilterCalled(View.GONE,View.GONE,View.GONE,View.GONE,View.GONE,View.GONE,View.GONE,View.GONE);
                mBuilder.setTitle("Amount of Blur desired");
                mArray = getResources().getStringArray(R.array.amountBlur);
                adapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_spinner_item, mArray);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                mSpinner.setAdapter(adapter);
                mBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        convolutionFilter.convolution(convolutionFilter.convolutionMatrix(2, mSpinner.getSelectedItemPosition()));
                        dialogInterface.dismiss();
                    }
                });
                mBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                mBuilder.setView(mView);
                dialog = mBuilder.create();
                dialog.show();
                return true;
            default:
                return false;
        }
    }
}
