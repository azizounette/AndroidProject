package com.example.alarvet.seguin_larvet_androidproject;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    private Bitmap bitmap;
    private Bitmap originalBitmap;
    private ImageView imageView;
    private Button galleryButton;
    private Button cameraButton;
    private static int IMAGE_GALLERY_REQUEST = 0;
    private static int REQUEST_TAKE_PHOTO = 0;


    private View.OnClickListener galleryButtonListener = new View.OnClickListener() {
        public void onClick(View v) {
            onImageGalleryClicked();
        }
    };

    private View.OnClickListener cameraButtonListener = new View.OnClickListener() {
        public void onClick(View v) {
            dispatchTakePictureIntent();
        }
    };

    //static final int REQUEST_TAKE_PHOTO = 1;

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            REQUEST_TAKE_PHOTO = 1;
            IMAGE_GALLERY_REQUEST = 0;
            startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
        }
    }

    public void onImageGalleryClicked() {
        Intent picturePickedIntent = new Intent(Intent.ACTION_PICK);
        IMAGE_GALLERY_REQUEST = 1;
        REQUEST_TAKE_PHOTO = 0;

        File pictureDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_ALARMS);
        String pictureDirectoryPath = pictureDirectory.getPath();

        Uri data = Uri.parse(pictureDirectoryPath);

        picturePickedIntent.setDataAndType(data, "image/*");

        startActivityForResult(picturePickedIntent, IMAGE_GALLERY_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            imageView.setImageBitmap(imageBitmap);
        }

        if (resultCode == RESULT_OK) {
            //success
            if (requestCode == IMAGE_GALLERY_REQUEST) {
                // hearing from image gallery
                Uri imageUri = data.getData(); // address of image on SD card

                InputStream inputStream; // stream to read the image data
                try {
                    inputStream = getContentResolver().openInputStream(imageUri);
                    bitmap = BitmapFactory.decodeStream(inputStream);
                    originalBitmap = BitmapFactory.decodeStream(inputStream);
                    imageView.setImageBitmap(bitmap);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Impossible to open the image", Toast.LENGTH_LONG).show();
                }

            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = (ImageView) findViewById(R.id.imageView);

        galleryButton = (Button) findViewById(R.id.galleryButton);
        galleryButton.setOnClickListener(galleryButtonListener);

        cameraButton = (Button) findViewById(R.id.cameraButton);
        cameraButton.setOnClickListener(cameraButtonListener);
    }
}
