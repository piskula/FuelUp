package sk.piskula.fuelup.util;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static sk.piskula.fuelup.screens.edit.AddVehicleActivity.REQUEST_PICTURE;
import static sk.piskula.fuelup.screens.edit.AddVehicleActivity.REQUEST_PIC_CROP;
import static sk.piskula.fuelup.screens.edit.AddVehicleActivity.REQUEST_TAKE_PHOTOS;

/**
 * Created by Martin Styk on 23.06.2017.
 */

public class ImageUtils {

    /**
     * Start camera in order to take photos. Caller needs to handle of camera action in OnActivityResult and call
     *
     * @param caller activity which triggers this method
     */
    public static String onStartTakePhoto(@NonNull Activity caller) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(caller.getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;

            try {
                photoFile = ImageUtils.createImageFile();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(caller, GenericFileProvider.AUTHORITY, photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                caller.startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTOS);
            }
            return photoFile.getAbsolutePath();
        }
        return null;
    }

    /**
     * Caller needs to call this method on activityResult
     *
     * @param caller caller activity
     * @param data   intent comming as a parameter of a=onActivityResult
     * @return path to image file
     */
    public static String onActivityResultTakePhoto(@NonNull Activity caller, @NonNull Intent data) {
        if (data == null) {
            return null;
        }
        Uri selectedImage = data.getData();
        String[] filePathColumn = {MediaStore.Images.Media.DATA};

        Cursor cursor = caller.getContentResolver().query(selectedImage, filePathColumn, null, null, null);
        cursor.moveToFirst();

        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        String vehiclePicturePath = cursor.getString(columnIndex);
        cursor.close();

        return ImageUtils.performCrop(caller, vehiclePicturePath);
    }

    /**
     * Start gallery to choose picture
     *
     * @param caller activity which triggers this method
     */
    public static void selectPhotoFromGallery(Activity caller) {
        Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        caller.startActivityForResult(i, REQUEST_PICTURE);
    }


    private static File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp;

        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        storageDir = new File(storageDir, "FuelApp");
        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }
        return new File(storageDir, imageFileName + ".jpg");
    }

    public static String performCrop(Activity caller, String picturePath) {
        Intent cropIntent = new Intent("com.android.camera.action.CROP");
        cropIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

        //indicate image type and Uri
        Uri inputUri = FileProvider.getUriForFile(caller, GenericFileProvider.AUTHORITY, new File(picturePath));
        cropIntent.setDataAndType(inputUri, "image/*");

        cropIntent.putExtra("crop", "true");
        //indicate aspect of desired crop
        cropIntent.putExtra("aspectX", 16);
        cropIntent.putExtra("aspectY", 9);
        //retrieve data on return
        cropIntent.putExtra("scale", true);
        cropIntent.putExtra("return-data", false);
        //start the activity - we handle returning in onActivityResult
        File photoFile = null;
        try {
            photoFile = createImageFile();
        } catch (IOException ex) {
            // Error occurred while creating the File
        }
        // Continue only if the File was successfully created
        if (photoFile != null) {
            Uri outputPicture = FileProvider.getUriForFile(caller, GenericFileProvider.AUTHORITY, photoFile);
            GenericFileProvider.grantUriPermission(caller, cropIntent, inputUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            GenericFileProvider.grantUriPermission(caller, cropIntent, outputPicture, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);

            cropIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputPicture);
            caller.startActivityForResult(cropIntent, REQUEST_PIC_CROP);
        }
        return photoFile.getAbsolutePath();
    }
}
