package sk.piskula.fuelup.screens.edit;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.j256.ormlite.android.apptools.OpenHelperManager;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Currency;
import java.util.Date;

import sk.piskula.fuelup.R;
import sk.piskula.fuelup.adapters.SpinnerVehicleTypesAdapter;
import sk.piskula.fuelup.data.DatabaseHelper;
import sk.piskula.fuelup.entity.Vehicle;
import sk.piskula.fuelup.entity.VehicleType;
import sk.piskula.fuelup.entity.enums.DistanceUnit;
import sk.piskula.fuelup.screens.VehicleTabbedDetail;

/**
 * @author Ondrej Oravcok
 * @version 21.6.2017
 */

public class EditVehicle extends AppCompatActivity implements View.OnClickListener {

    public static final String TAG = "EditVehicle";

    private String currentPhotoPath;
    private EditText txtName;
    private EditText txtManufacturer;
    private Spinner spinnerType;
    private ImageView imgCarPhotoStatus;

    private Vehicle vehicle;
    private SpinnerVehicleTypesAdapter typeAdapter;

    private DatabaseHelper databaseHelper = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_vehicle);

        Intent intent = getIntent();
        vehicle = (Vehicle) intent.getSerializableExtra(VehicleTabbedDetail.VEHICLE_TO_FRAGMENT);

        if (vehicle == null) {
            setResult(RESULT_CANCELED);
            finish();
        }

        initViews();
        populateFields();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    private void initViews() {
        this.txtName = (EditText) findViewById(R.id.txt_editVehicle_name);
        this.txtManufacturer = (EditText) findViewById(R.id.txt_editVehicle_manufacturer);
        Button buttonAdd = (Button) findViewById(R.id.btn_editVehicle_add);
        this.spinnerType = (Spinner) findViewById(R.id.spinner_editVehicle_types);
        this.imgCarPhotoStatus = (ImageView) findViewById(R.id.img_editVehicle_photo);

        typeAdapter = new SpinnerVehicleTypesAdapter(this);
        spinnerType.setAdapter(typeAdapter);

        imgCarPhotoStatus.setOnClickListener(this);
        buttonAdd.setOnClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.vehicle_edit_create, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem actionViewItem = menu.findItem(R.id.btn_save_edit_create);
        actionViewItem.setTitle(getString(R.string.update));
        actionViewItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                saveVehicle();
                return true;
        }});
        return super.onPrepareOptionsMenu(menu);
    }

    private void populateFields() {
        txtName.setText(vehicle.getName());
        txtManufacturer.setText(vehicle.getVehicleMaker());
        spinnerType.setSelection(getAlreadySelectedTypePosition(vehicle.getType()));
        //TODO set currentPhotoPath from vehicle
        currentPhotoPath = "";
        imgCarPhotoStatus.setImageResource(
                isRemovablePhoto() ? R.drawable.ic_camera_deny : R.drawable.ic_camera);
    }

    private boolean isRemovablePhoto() {
        return currentPhotoPath != null && !currentPhotoPath.isEmpty();
    }

    private int getAlreadySelectedTypePosition(VehicleType type) {
        int i = 0;
        for(VehicleType found : typeAdapter.getVehicleTypes()) {
            if (found.equals(type)) {
                return i;
            }
            i++;
        }
        return i;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_save_edit_create:
            case R.id.btn_editVehicle_add:
                saveVehicle();
                break;
            case R.id.img_editVehicle_photo:
                //TODO camera photos
                if (isRemovablePhoto()) {
                    deletePhoto();
                } else {
//                    createImageChooserDialog();
                }
                break;
            default:
                break;
        }
    }

    private void saveVehicle() {
        String name = txtName.getText().toString();
        String vehicleManufacturer = txtManufacturer.getText().toString();

        if (name.isEmpty() || vehicleManufacturer.isEmpty()) {
            Toast.makeText(this, getString(R.string.addCarActivity_Toast_emptyFields), Toast.LENGTH_LONG).show();
            return;
        }

        // update vehicle in database
        vehicle.setName(name);
        vehicle.setVehicleMaker(vehicleManufacturer);
        vehicle.setType((VehicleType) spinnerType.getSelectedItem());

        try {
            getHelper().getVehicleDao().update(vehicle);
            vehicle = getHelper().getVehicleDao().queryForId(vehicle.getId());
            Log.i(TAG, "Successfully updated Vehicle: " + vehicle);
            Toast.makeText(this, R.string.carUpdate_Toast_successfullyUpdated, Toast.LENGTH_LONG).show();
        } catch (SQLException e) {
            String errorMsg = "Error occured while saving updated Vehicle to DB.";
            Log.e(TAG, errorMsg, e);
            Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
        }
        finish();
    }
//
//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if (resultCode == RESULT_OK) {
//            if (requestCode == REQUEST_PICTURE) {
//                Uri selectedImage = data.getData();
//                String[] filePathColumn = {MediaStore.Images.Media.DATA};
//
//                Cursor cursor = getContentResolver().query(selectedImage,
//                        filePathColumn, null, null, null);
//                cursor.moveToFirst();
//
//                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
//                mCurrentPhotoPath = cursor.getString(columnIndex);
//                cursor.close();
//                performCrop();
//
//            }
//            if (requestCode == REQUEST_TAKE_PHOTOS) {
//                performCrop();
//            }
//            if (requestCode == REQUEST_PIC_CROP) {
//                setPictureLarge();
//            }
//        }
//    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (databaseHelper != null) {
            OpenHelperManager.releaseHelper();
            databaseHelper = null;
        }
    }

//    public void createImageChooserDialog() {
//        final int TAKE_PHOTO = 0;
//        final int SELECT_PHOTO = 1;
//
//        final CharSequence[] opsChars = {getResources().getString(R.string.add_car_activity_take_photo), getResources().getString(R.string.add_car_activity_select_photo)};
//
//        AlertDialog.Builder getImageDialog = new AlertDialog.Builder(this);
//        getImageDialog.setTitle("Select:");
//        getImageDialog.setItems(opsChars, new android.content.DialogInterface.OnClickListener() {
//
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                //TODO repair photos
//                if (which == TAKE_PHOTO) {
//                    takePhoto();
//                } else if (which == SELECT_PHOTO) {
//                    selectImageFromGallery();
//                }
//                dialog.dismiss();
//            }
//        });
//        getImageDialog.create().show();
//    }
//
//    private void selectImageFromGallery() {
//        Intent i = new Intent(
//                Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//        startActivityForResult(i, REQUEST_PICTURE);
//    }
//
//    private void takePhoto() {
//        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        // Ensure that there's a camera activity to handle the intent
//        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
//            // Create the File where the photo should go
//            File photoFile = null;
//            try {
//                photoFile = createImageFile();
//            } catch (IOException ex) {
//                // Error occurred while creating the File
//            }
//            // Continue only if the File was successfully created
//            if (photoFile != null) {
//                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
//                        Uri.fromFile(photoFile));
//                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTOS);
//            }
//        }
//    }

    public void deletePhoto() {
        currentPhotoPath = null;
        imgCarPhotoStatus.setImageResource(R.drawable.ic_camera);
    }

//    private File createImageFile() throws IOException {
//        // Create an image file name
//        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
//        String imageFileName = "JPEG_" + timeStamp + "_";
//
//        File storageDir = Environment.getExternalStoragePublicDirectory(
//                Environment.DIRECTORY_PICTURES);
//        storageDir = new File(storageDir, getResources().getString(R.string.app_name));
//        if (!storageDir.exists()) {
//            storageDir.mkdirs();
//        }
//        File image = new File(storageDir, imageFileName + ".jpg");
//
//        // Save a file: path for use with ACTION_VIEW intents
//        mCurrentPhotoPath = image.getAbsolutePath();
//        return image;
//    }
//
//    private void setPictureLarge() {
//        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
//        bmOptions.inJustDecodeBounds = false;
//        bmOptions.inSampleSize = 1;
//        bmOptions.inPurgeable = true;
//        currentPhoto = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
//        imgCarPhotoStatus.setImageResource(R.drawable.ic_camera_deny);
////        imgCarPhotoStatus.setImageBitmap(currentPhoto);
//    }
//
//    private void performCrop() {
//        Intent cropIntent = new Intent("com.android.camera.action.CROP");
//
//        //indicate image type and Uri
//        cropIntent.setDataAndType(Uri.fromFile(new File(mCurrentPhotoPath)), "image/*");
//
//        cropIntent.putExtra("crop", "true");
//        //indicate aspect of desired crop
//        cropIntent.putExtra("aspectX", 16);
//        cropIntent.putExtra("aspectY", 9);
//        //indicate output X and Y
//        cropIntent.putExtra("scaleUpIfNeeded", true);
//        cropIntent.putExtra("outputX", 800);
//        cropIntent.putExtra("outputY", 450);
//        //retrieve data on return
//        cropIntent.putExtra("scale", true);
//        cropIntent.putExtra("return-data", false);
//        //start the activity - we handle returning in onActivityResult
//        File photoFile = null;
//        try {
//            photoFile = createImageFile();
//        } catch (IOException ex) {
//            // Error occurred while creating the File
//        }
//        // Continue only if the File was successfully created
//        if (photoFile != null) {
//            cropIntent.putExtra(MediaStore.EXTRA_OUTPUT,
//                    Uri.fromFile(photoFile));
//
//            startActivityForResult(cropIntent, REQUEST_PIC_CROP);
//        }
//
//    }

    private DatabaseHelper getHelper() {
        if (databaseHelper == null) {
            databaseHelper = OpenHelperManager.getHelper(this, DatabaseHelper.class);
        }
        return databaseHelper;
    }
}
