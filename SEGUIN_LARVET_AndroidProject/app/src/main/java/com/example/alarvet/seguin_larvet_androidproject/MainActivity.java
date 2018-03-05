package com.example.alarvet.seguin_larvet_androidproject;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private Bitmap bitmap;
    private Bitmap originalBitmap;
    private ImageView imageView;
    private Button galleryButton;
    private Button cameraButton;
    private static final int REQUEST_IMAGE_GALLERY = 1;
    private static final int REQUEST_TAKE_PHOTO = 2;

    //Variables used for applying filters to the image
    private Colour colourFilter;
    private ComplexFilter complexFilter;
    private Contrast contrastFilter;
    private Convolution convolutionFilter;
    private Luminosity luminosityFilter;

    private Uri fileSavePic;

    Matrix matrix = new Matrix();
    Matrix savedMatrix = new Matrix();

    // We can be in one of these 3 states
    private static final int NONE = 0;
    private static final int DRAG = 1;
    private static final int ZOOM = 2;
    int mode = NONE;

    // Variables used for scaling
    PointF start = new PointF();
    PointF mid = new PointF();
    float oldDist = 1f;


    // Variable used for saving the bitmap before an orientation change
    private static final String SAVE_BMP = "SaveBitmap";


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

        /** Computes the space between the first two fingers */
        private float spacing(MotionEvent event) {
            float x = event.getX(0) - event.getX(1);
            float y = event.getY(0) - event.getY(1);
            return (float)Math.sqrt(x * x + y * y);
        }

        /** Computes the mid point of the first two fingers */
        private void midPoint(PointF point, MotionEvent event) {
            float x = event.getX(0) + event.getX(1);
            float y = event.getY(0) + event.getY(1);
            point.set(x / 2, y / 2);
        }



        private View.OnClickListener galleryButtonListener = new View.OnClickListener() {
        public void onClick(View v) {
            onImageGalleryClicked();
        }
    };

    private View.OnClickListener cameraButtonListener = new View.OnClickListener() {
        public void onClick(View v) { takePicture(); }
    };

    private void takePicture() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        fileSavePic = Uri.fromFile(getOutputMediaFile());
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileSavePic);

        startActivityForResult(intent, REQUEST_TAKE_PHOTO);

    }

    private static File getOutputMediaFile(){
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "CameraFromApp");

        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()){
            return null;
        }

        String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date());
        return new File(mediaStorageDir.getPath() + File.separator +
                "IMG_"+ timeStamp + ".jpg");
    }

    public void onImageGalleryClicked() {
        Intent picturePickedIntent = new Intent(Intent.ACTION_PICK);

        File pictureDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_ALARMS);
        String pictureDirectoryPath = pictureDirectory.getPath();

        Uri data = Uri.parse(pictureDirectoryPath);

        picturePickedIntent.setDataAndType(data, "image/*");

        startActivityForResult(picturePickedIntent, REQUEST_IMAGE_GALLERY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK){
            switch(requestCode){
                case REQUEST_TAKE_PHOTO:
                    imageView.setImageURI(fileSavePic);
                    BitmapDrawable bitmapDrawable = ((BitmapDrawable) imageView.getDrawable());
                    if(bitmapDrawable == null){
                        imageView.buildDrawingCache();
                        bitmap = imageView.getDrawingCache();
                        imageView.buildDrawingCache(false);
                    } else{
                        bitmap = bitmapDrawable.getBitmap();
                    }
                    break;

                case REQUEST_IMAGE_GALLERY:
                    Uri imageUri = data.getData(); // address of image on SD card

                    InputStream inputStream; // stream to read the image data
                    try {
                        inputStream = getContentResolver().openInputStream(imageUri);
                        originalBitmap = BitmapFactory.decodeStream(inputStream);
                        bitmap = originalBitmap.copy(originalBitmap.getConfig(), true);
                        imageView.setImageBitmap(bitmap);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Impossible to open the image", Toast.LENGTH_LONG).show();
                    }
                    break;
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        outState.putParcelable(SAVE_BMP, bitmap);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = (ImageView) findViewById(R.id.imageView);

        if (savedInstanceState != null) {
            bitmap = savedInstanceState.getParcelable(SAVE_BMP);
            imageView.setImageBitmap(bitmap);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());

            bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
        }

        galleryButton = (Button) findViewById(R.id.galleryButton);
        galleryButton.setOnClickListener(galleryButtonListener);

        cameraButton = (Button) findViewById(R.id.cameraButton);
        cameraButton.setOnClickListener(cameraButtonListener);
        imageView.setOnTouchListener(handleTouch);

        colourFilter = new Colour(bitmap);
    }

   /* public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.changeTint:
                colourFilter.changeTint(50);
                return true;
            default:
                return false;
        }
    }*/
}
