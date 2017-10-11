package sk.momosi.fuelup.screens.edit;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.theartofdev.edmodo.cropper.CropImage;

import java.io.File;

import sk.momosi.fuelup.R;

/**
 * Created by Martin Styk on 22.09.2017.
 */

public abstract class VehicleAbstractActivity extends AppCompatActivity {

    protected static final String PHOTO = "photo";

    public static final float REMOVE_PHOTO_ALPHA_CHANNEL = 0.5f;

    protected ImageView imgCarPhoto;
    protected ImageView imgCarPhotoRemove;

    protected String vehiclePicturePath;
    protected Uri cropImageUri;

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (vehiclePicturePath != null)
            outState.putString(PHOTO, vehiclePicturePath);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState.containsKey(PHOTO)) {
            vehiclePicturePath = savedInstanceState.getString(PHOTO);
            imgCarPhoto.setImageBitmap(BitmapFactory.decodeFile(vehiclePicturePath));
            imgCarPhotoRemove.setVisibility(View.VISIBLE);
        } else {
            vehiclePicturePath = null;
            imgCarPhoto.setImageResource(R.drawable.ic_insert_photo);
            imgCarPhotoRemove.setVisibility(View.GONE);
        }
    }


    public abstract void onClickAdd(View w);

    public void onClickPhoto(View w) {
        if (vehiclePicturePath != null && new File(vehiclePicturePath).exists()) {
            deletePhoto();
        } else {
            if (CropImage.isExplicitCameraPermissionRequired(this)) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CropImage.CAMERA_CAPTURE_PERMISSIONS_REQUEST_CODE);
            } else {
                CropImage.startPickImageActivity(this);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // handle result of pick image chooser
        if (requestCode == CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                Uri imageUri = CropImage.getPickImageResultUri(this, data);

                if (CropImage.isReadExternalStoragePermissionsRequired(this, imageUri)) {
                    cropImageUri = imageUri;
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, CropImage.PICK_IMAGE_PERMISSIONS_REQUEST_CODE);
                } else {
                    startCropImageActivity(imageUri);
                }
            } else {
                Snackbar.make(findViewById(android.R.id.content), getString(R.string.addVehicle_picture_add_fail, ""), Snackbar.LENGTH_SHORT).show();
            }
        } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                vehiclePicturePath = result.getUri().getPath();
                imgCarPhoto.setImageBitmap(BitmapFactory.decodeFile(vehiclePicturePath));
                imgCarPhoto.setAlpha(REMOVE_PHOTO_ALPHA_CHANNEL);
                imgCarPhotoRemove.setVisibility(View.VISIBLE);
                Snackbar.make(findViewById(android.R.id.content), getString(R.string.addVehicle_picture_add_success), Snackbar.LENGTH_SHORT).show();
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Snackbar.make(findViewById(android.R.id.content), getString(R.string.addVehicle_picture_add_fail, error), Snackbar.LENGTH_SHORT).show();

            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == CropImage.CAMERA_CAPTURE_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                CropImage.startPickImageActivity(this);
            } else {
                Toast.makeText(this, "Cancelling, required permissions are not granted", Toast.LENGTH_LONG).show();
            }
        }
        if (requestCode == CropImage.PICK_IMAGE_PERMISSIONS_REQUEST_CODE) {
            if (cropImageUri != null && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // required permissions granted, start crop image activity
                startCropImageActivity(cropImageUri);
            } else {
                Toast.makeText(this, "Cancelling, required permissions are not granted", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void startCropImageActivity(Uri imageUri) {
        CropImage.activity(imageUri)
                .setAspectRatio(16, 9)
                .setOutputCompressQuality(50)
                .setOutputCompressFormat(Bitmap.CompressFormat.JPEG)
                .setOutputUri(android.net.Uri.fromFile(new File(getFilesDir(), System.currentTimeMillis() + ".jpg")))
                .start(this);
    }

    public void deletePhoto() {
        vehiclePicturePath = null;
        imgCarPhoto.setImageResource(R.drawable.ic_insert_photo);
        imgCarPhoto.setAlpha(1f);
        imgCarPhotoRemove.setVisibility(View.GONE);
        Snackbar.make(findViewById(android.R.id.content), R.string.delete_vehicle_photo, Snackbar.LENGTH_SHORT).show();
    }

}
