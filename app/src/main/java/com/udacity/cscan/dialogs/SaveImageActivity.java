package com.udacity.cscan.dialogs;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.udacity.cscan.CscanApplication;
import com.udacity.cscan.R;
import com.udacity.cscan.daoimage.Image;
import com.udacity.cscan.events.AddedImageEvent;
import com.udacity.cscan.events.CancelSaveImageEvent;
import com.udacity.cscan.events.SaveImageEvent;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import de.greenrobot.event.EventBus;

public class SaveImageActivity extends AppCompatActivity {
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final String TAG = "SaveImageActivity";

    EditText titleEditText;
    EditText commentEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.dialog_save_image_title);
        setContentView(R.layout.activity_save_image);
        EventBus.getDefault().register(this);

        if (getIntent() != null) {
            if (getIntent().hasExtra("data")) {
                final byte[] data = getIntent().getByteArrayExtra("data");
                ImageView save_imageView = (ImageView) findViewById(R.id.save_imageView);
                Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                save_imageView.setImageBitmap(bmp);
                Button cancel_button = (Button) findViewById(R.id.cancel_button);
                cancel_button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        EventBus.getDefault().post(new CancelSaveImageEvent());
                        finish();
                    }
                });
                Button save_button = (Button) findViewById(R.id.save_button);
                save_button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        EventBus.getDefault().post(new SaveImageEvent(data));
                        finish();
                    }
                });
                titleEditText = (EditText) findViewById(R.id.image_title_editText);
                commentEditText = (EditText) findViewById(R.id.image_comment_editText);
            } else {
                Toast.makeText(this, "No picture data sent", Toast.LENGTH_LONG);
            }
        }
    }

    /**
     * Create a File for saving an image or video
     */
    @SuppressLint("SimpleDateFormat")
    private static File getOutputMediaFile(int type) {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "CScan");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_" + timeStamp + ".jpg");
        } else {
            return null;
        }

        return mediaFile;
    }

    private void addImage(String title, String comment, String imageurl) {
        String imageDescription = "Description";
        final DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);
        String imageURL = imageurl;
        Image image = new Image(null, title, comment, imageURL, new Date(), df.format(new Date()));
        CscanApplication.getInstance().imageDao.insert(image);
        Log.d(TAG, "Inserted new image, ID: " + image.getId());
        EventBus.getDefault().post(new AddedImageEvent(image));

    }

    public void onEventMainThread(SaveImageEvent saveImageEvent) {
        //we could just add this to top or replace element instead of refreshing whole list
        File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
        if (pictureFile == null) {
            Log.d(TAG, "Error creating media file, check storage permissions");
            return;
        }
        byte[] pictureData = saveImageEvent.getPictureData();
        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            fos.write(pictureData);
            fos.close();
            Toast.makeText(this, "Picture saved on: " + pictureFile.getPath(), Toast.LENGTH_SHORT).show();
            addImage(titleEditText.getText().toString(), commentEditText.getText().toString(), pictureFile.getPath());
//            captureButton.setVisibility(View.VISIBLE);
            // return to Preview Mode once the picture is saved
//            startCameraPreview();

        } catch (FileNotFoundException e) {
            Log.d(TAG, "File not found: " + e.getMessage());
        } catch (IOException e) {
            Log.d(TAG, "Error accessing file: " + e.getMessage());
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            EventBus.getDefault().unregister(this);
        } catch (Throwable t) {
            //this may crash if registration did not go through. just be safe
        }
    }
}
