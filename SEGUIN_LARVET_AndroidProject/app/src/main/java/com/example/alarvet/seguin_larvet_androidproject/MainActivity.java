package com.example.alarvet.seguin_larvet_androidproject;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
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
    private static int IMAGE_GALLERY_REQUEST = 1;
    private ImageView picture;
    private Button galleryButton;


    private View.OnClickListener galleryButtonListener = new View.OnClickListener() {
        public void onClick(View v) {
            onImageGalleryClicked(v);
        }
    };

    public void onImageGalleryClicked(View v) {
        Intent picturePickedIntent = new Intent(Intent.ACTION_PICK);

        File pictureDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_ALARMS);
        String pictureDirectoryPath = pictureDirectory.getPath();

        Uri data = Uri.parse(pictureDirectoryPath);

        picturePickedIntent.setDataAndType(data, "image/*");

        startActivityForResult(picturePickedIntent, IMAGE_GALLERY_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
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
                    picture.setImageBitmap(bitmap);
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

        picture = (ImageView) findViewById(R.id.imageView);

        galleryButton = (Button) findViewById(R.id.galleryButton);
        galleryButton.setOnClickListener(galleryButtonListener);
    }
}
