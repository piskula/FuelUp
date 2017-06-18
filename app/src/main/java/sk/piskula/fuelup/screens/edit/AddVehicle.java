package sk.piskula.fuelup.screens.edit;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.j256.ormlite.android.apptools.OpenHelperManager;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

import sk.piskula.fuelup.R;
import sk.piskula.fuelup.data.DatabaseHelper;
import sk.piskula.fuelup.entity.Vehicle;
import sk.piskula.fuelup.entity.VehicleType;
import sk.piskula.fuelup.entity.enums.DistanceUnit;

/**
 * @author Ondrej Oravcok
 * @version 18.6.2017
 */
public class AddVehicle extends AppCompatActivity implements OnClickListener {

    public static final String TAG = "AddCarActivity";
    private static final String PHOTO = "photo";
    public static final int REQUEST_PICTURE = 123456789;
    public static final int REQUEST_TAKE_PHOTOS = 98765;
    public static final int REQUEST_PIC_CROP = 1111;

    private String mCurrentPhotoPath;
    private Bitmap mCurrentPhotoLarge;
    private EditText mTxtNick;
    private EditText mTxtTypeName;
    private EditText mTxtActualMileage;
    private Spinner mTypeSpinner;
    private Spinner mCurrencySpinner;
    private RadioGroup mDistanceUnitRadioGroup;
    private Button mBtnAdd;
    private ImageView mImgCarPhoto;
    private LinearLayout layout;

    private DatabaseHelper databaseHelper = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_vehicle);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        initViews();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mCurrentPhotoLarge != null)
            outState.putParcelable(PHOTO, mCurrentPhotoLarge);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState.containsKey(PHOTO)) {
            mCurrentPhotoLarge = savedInstanceState.getParcelable(PHOTO);
            ImageView im = (ImageView) findViewById(R.id.img_addVehicle_photo);
            im.setImageBitmap(mCurrentPhotoLarge);
        }
    }

    private void initViews() {
        this.mTxtNick = (EditText) findViewById(R.id.txt_addVehicle_name);
        this.mTxtTypeName = (EditText) findViewById(R.id.txt_addVehicle_manufacturer);
        this.mTxtActualMileage = (EditText) findViewById(R.id.txt_addVehicle_mileage);
        this.mBtnAdd = (Button) findViewById(R.id.btn_add);
        this.mTypeSpinner = (Spinner) findViewById(R.id.spinner_types);
        this.mCurrencySpinner = (Spinner) findViewById(R.id.spinner_currency);
        this.mDistanceUnitRadioGroup = (RadioGroup) findViewById(R.id.radio_distance_unit);
        this.mDistanceUnitRadioGroup.check(R.id.radio_km);
        this.layout = (LinearLayout) findViewById(R.id.addVehicle_layout);
        this.mImgCarPhoto = (ImageView) findViewById(R.id.img_addVehicle_photo);

        mTxtActualMileage.setRawInputType(Configuration.KEYBOARD_QWERTY);

        mImgCarPhoto.setOnClickListener(this);

        this.mBtnAdd.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_add:
                saveCar();
                break;
            case R.id.img_addVehicle_photo:
                //TODO camera photos
                createImageChooserDialog();
                break;
            default:
                break;
        }
    }

    private void saveCar() {
        Editable nick = mTxtNick.getText();
        Editable typeName = mTxtTypeName.getText();
        Editable actualMileage = mTxtActualMileage.getText();

        if (!TextUtils.isEmpty(nick)
                && !TextUtils.isEmpty(typeName)
                && !TextUtils.isEmpty(actualMileage)) {
            // add the car to database
            Vehicle createdVehicle = new Vehicle();
            Long createdMileage = Long.valueOf(0);
            String msg = null;

            try {
                createdMileage = Long.parseLong(actualMileage.toString());
            } catch (NumberFormatException ex) {
                Log.d(TAG, getString(R.string.addCarActivity_LOG_badLongNumberFormat));
                msg = getString(R.string.addCarActivity_wrong_number_format);
            }
            if (msg == null) {
                createdVehicle.setName(nick.toString());
                createdVehicle.setVehicleMaker(typeName.toString());
                createdVehicle.setStartMileage(createdMileage);
                //TODO set other fields
//                createdVehicle.setActualMileage(createdMileage);
//                createdVehicle.setAvgFuelConsumption(0.0);
//                createdVehicle.setCarCurrency(Car.CarCurrency.valueOf(mCurrencySpinner.getSelectedItem().toString()));
                try {
                    VehicleType type = getHelper().getVehicleTypeDao().queryBuilder().where().eq("name", mTypeSpinner.getSelectedItem().toString()).queryForFirst();
                    createdVehicle.setType(type);
                } catch (SQLException e) {
                    msg = "Choosen vehicleType does not exist";
                    Log.e(TAG, msg, e);
                    Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                    finish();
                }
                //TODO vehicle distance unit
//                createdVehicle.setUnit(DistanceUnit.valueOf(mDistanceSpinner.getSelectedItem().toString()));
                createdVehicle.setUnit(DistanceUnit.km);
                createdVehicle.setImage(mCurrentPhotoLarge);

                Log.d(TAG, getString(R.string.addCarActivity_LOG_wantToAdd) + " "
                        + createdVehicle.getName() + "-"
                        + createdVehicle.getVehicleMaker() + ":" + createdVehicle.getType().getName() + ":"
                    //  + createdVehicle.getCarCurrency().toString() + ":"
                        + createdVehicle.getUnit().toString());

                try {
                    getHelper().getVehicleDao().create(createdVehicle);
                    Toast.makeText(this, R.string.addCarActivity_Toast_successfullyCreated, Toast.LENGTH_LONG).show();
                } catch (SQLException e) {
                    String errorMsg = "Error occured while saving new Vehicle to DB.";
                    Log.e(TAG, errorMsg, e);
                    Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
                    finish();
                }

                Log.d(TAG, getString(R.string.addCarActivity_LOG_added) + " "
                        + createdVehicle.getName() + "-"
                        + createdVehicle.getType());

                finish();
            } else {
                Toast.makeText(this, msg + "- actualMileage", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, getString(R.string.addCarActivity_Toast_emptyFields), Toast.LENGTH_LONG).show();
        }
    }

    public void createImageChooserDialog() {
        final int TAKE_PHOTO = 0;
        final int SELECT_PHOTO = 1;
        final int DELETE_PHOTO = 2;

        final CharSequence[] opsWithDelete = {getResources().getString(R.string.add_car_activity_take_photo), getResources().getString(R.string.add_car_activity_select_photo),getResources().getString(R.string.add_car_activity_delete_photo)};
        final CharSequence[] opsWithOutDelete = {getResources().getString(R.string.add_car_activity_take_photo), getResources().getString(R.string.add_car_activity_select_photo)};

        final CharSequence[] opsChars = mCurrentPhotoLarge == null ? opsWithOutDelete : opsWithDelete;

        AlertDialog.Builder getImageFrom = new AlertDialog.Builder(this);
        getImageFrom.setTitle("Select:");

        getImageFrom.setItems(opsChars, new android.content.DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                //TODO repair photos
                if (which == TAKE_PHOTO) {
                    //takePhoto();
                } else if (which == SELECT_PHOTO) {
                    //selectImageFromGallery();
                } else if (which == DELETE_PHOTO) {
                    //deletePhoto();
                }
                dialog.dismiss();
            }
        });
        getImageFrom.create().show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_PICTURE) {
                Uri selectedImage = data.getData();
                String[] filePathColumn = {MediaStore.Images.Media.DATA};

                Cursor cursor = getContentResolver().query(selectedImage,
                        filePathColumn, null, null, null);
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                mCurrentPhotoPath = cursor.getString(columnIndex);
                cursor.close();
                performCrop();

            }
            if (requestCode == REQUEST_TAKE_PHOTOS) {
                performCrop();
            }
            if (requestCode == REQUEST_PIC_CROP) {
                setPictureLarge();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (databaseHelper != null) {
            OpenHelperManager.releaseHelper();
            databaseHelper = null;
        }
    }

    private void selectImageFromGallery() {
        Intent i = new Intent(
                Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        startActivityForResult(i, REQUEST_PICTURE);
    }

    private void takePhoto() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTOS);
            }

        }
    }

    public void deletePhoto() {
        mCurrentPhotoLarge = null;
        mImgCarPhoto.setImageDrawable(getResources().getDrawable(android.R.drawable.ic_menu_camera));
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";

        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        storageDir = new File(storageDir, getResources().getString(R.string.app_name));
        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }
        File image = new File(storageDir, imageFileName + ".jpg");

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void setPictureLarge() {
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = 1;
        bmOptions.inPurgeable = true;
        mCurrentPhotoLarge = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        mImgCarPhoto.setImageBitmap(mCurrentPhotoLarge);
    }

    private void performCrop() {
        Intent cropIntent = new Intent("com.android.camera.action.CROP");

        //indicate image type and Uri
        cropIntent.setDataAndType(Uri.fromFile(new File(mCurrentPhotoPath)), "image/*");

        cropIntent.putExtra("crop", "true");
        //indicate aspect of desired crop
        cropIntent.putExtra("aspectX", 16);
        cropIntent.putExtra("aspectY", 9);
        //indicate output X and Y
        cropIntent.putExtra("scaleUpIfNeeded", true);
        cropIntent.putExtra("outputX", 800);
        cropIntent.putExtra("outputY", 450);
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
            cropIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                    Uri.fromFile(photoFile));

            startActivityForResult(cropIntent, REQUEST_PIC_CROP);
        }

    }

    private DatabaseHelper getHelper() {
        if (databaseHelper == null) {
            databaseHelper = OpenHelperManager.getHelper(this,DatabaseHelper.class);
        }
        return databaseHelper;
    }

}
