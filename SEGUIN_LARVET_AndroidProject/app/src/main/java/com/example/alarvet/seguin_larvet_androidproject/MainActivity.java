package com.example.alarvet.seguin_larvet_androidproject;

import android.Manifest;
import android.content.DialogInterface;
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
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
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

    private Bitmap bitmap;
    private Bitmap originalBitmap;
    private ImageView imageView;
    private static final int REQUEST_IMAGE_GALLERY = 1;
    private static final int REQUEST_TAKE_PHOTO = 2;

    //Variables used for applying filters to the image
    private Colour colourFilter;
    private ComplexFilter complexFilter;
    private Contrast contrastFilter;
    private Convolution convolutionFilter;
    private Luminosity luminosityFilter;

    private int hue = 0;
    private float saturation = 1;
    private float value = 1;

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
                        originalBitmap = imageView.getDrawingCache();
                        imageView.buildDrawingCache(false);
                    } else{
                        originalBitmap = bitmapDrawable.getBitmap();
                    }
                    bitmap = originalBitmap.copy(originalBitmap.getConfig(), true);
                    createFilters();
                    break;

                case REQUEST_IMAGE_GALLERY:
                    Uri imageUri = data.getData(); // address of image on SD card

                    InputStream inputStream; // stream to read the image data
                    try {
                        inputStream = getContentResolver().openInputStream(imageUri);
                        originalBitmap = BitmapFactory.decodeStream(inputStream);
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

    protected void saveImage(){
        BitmapDrawable draw = (BitmapDrawable) imageView.getDrawable();
        Bitmap bitmap = draw.getBitmap();

        FileOutputStream fos;
        File sdCard = Environment.getExternalStorageDirectory();
        File dir = new File(sdCard.getAbsolutePath() + "/ImagesFromApp");
        dir.mkdirs();
        String fileName = String.format("%d.jpg", System.currentTimeMillis());
        File outFile = new File(dir, fileName);
        try {
            fos = new FileOutputStream(outFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (java.io.IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Impossible to save the image", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        outState.putParcelable(SAVE_BMP, bitmap);
    }

    private void createFilters() {
        colourFilter = new Colour(bitmap);
        convolutionFilter = new Convolution(bitmap);
        complexFilter = new ComplexFilter(bitmap);
        contrastFilter = new Contrast(bitmap);
        luminosityFilter = new Luminosity(bitmap);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = (ImageView) findViewById(R.id.imageView);

        if (savedInstanceState != null) {
            originalBitmap = savedInstanceState.getParcelable(SAVE_BMP);
            bitmap = originalBitmap.copy(originalBitmap.getConfig(), true);
            imageView.setImageBitmap(bitmap);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());

            originalBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
            bitmap = originalBitmap.copy(originalBitmap.getConfig(), true);
        }

        imageView.setOnTouchListener(handleTouch);

        SeekBar hueBar = (SeekBar) findViewById(R.id.hueBar);
        hueBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar hBar, int progress, boolean fromUser) {
                hue = progress;
                colourFilter.changeTint(hue, saturation, value);
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        SeekBar saturationBar = (SeekBar) findViewById(R.id.saturationBar);
        saturationBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar saturationBar, int progress, boolean fromUser) {
                saturation = progress;
                colourFilter.changeTint(hue, saturation, value);
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        SeekBar valueBar = (SeekBar) findViewById(R.id.valueBar);
        valueBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar valueBar, int progress, boolean fromUser) {
                value = progress;
                colourFilter.changeTint(hue, saturation, value);
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        SeekBar contrastBar = (SeekBar) findViewById(R.id.valueBar);
        contrastBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar hBar, int progress, boolean fromUser) {
                value = progress;
                colourFilter.changeTint(hue, saturation, value);
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {

        AlertDialog.Builder mBuilder = new AlertDialog.Builder(MainActivity.this);
        View mView = getLayoutInflater().inflate(R.layout.dialog_spinner, null);
        final Spinner mSpinner = (Spinner) mView.findViewById(R.id.spinner);
        AlertDialog dialog;
        ArrayAdapter<String> adapter;
        String[] mArray;

        switch (item.getItemId()) {
            case R.id.resetButton:
                bitmap = originalBitmap.copy(originalBitmap.getConfig(), true);
                imageView.setImageBitmap(bitmap);
                createFilters();
                return true;
            case R.id.phototaking:
                takePicture();
                return true;
            case R.id.gallery:
                onImageGalleryClicked();
                return true;
            case R.id.luminosityChange:
                return true;
            case R.id.overexposure:
                luminosityFilter.overexposure();
            case R.id.magicWand:
                return true;
            case R.id.warmthChange:
                return true;
            case R.id.contrastChange:
                return true;
            case R.id.equalizeColors:
                contrastFilter.equalizeColors();
                return true;
            case R.id.equalizeGray:
                contrastFilter.equalizeGray();
                return true;
            case R.id.sepia:
                colourFilter.sepia();
                return true;
            case R.id.grayAndTint:
                mBuilder.setTitle("Choose the hue of the color wanted");
                mArray = getResources().getStringArray(R.array.colorHue);
                adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_item, mArray);
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
                colourFilter.toGray();
                return true;
            case R.id.changeTint:
                return true;
            case R.id.averageBlurring:
                mBuilder.setTitle("Amount of Blur desired");
                mArray = getResources().getStringArray(R.array.amountBlur);
                adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_item, mArray);
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
                mBuilder.setTitle("Amount of Blur desired");
                mArray = getResources().getStringArray(R.array.amountBlur);
                adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_item, mArray);
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
