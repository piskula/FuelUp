package sk.piskula.fuelup.screens.edit;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.j256.ormlite.android.apptools.OpenHelperManager;

import java.sql.SQLException;

import sk.piskula.fuelup.R;
import sk.piskula.fuelup.data.DatabaseHelper;
import sk.piskula.fuelup.entity.Vehicle;
import sk.piskula.fuelup.entity.VehicleType;
import sk.piskula.fuelup.screens.VehicleList;
import sk.piskula.fuelup.screens.VehicleTabbedDetail;

/**
 * @author Ondrej Oravcok
 * @version 18.6.2017
 */
public class UpdateVehicle extends AppCompatActivity {

    private static final String TAG = "UpdateCarActivity";

    public static final int REQUEST_PICTURE = 123456789;
    public static final int REQUEST_TAKE_PHOTOS = 98765;
    public static final int REQUEST_PIC_CROP = 1111;
    private static final String PHOTO = "photo";

    private String mCurrentPhotoPath;
    private Bitmap mCurrentPhotoLarge;

    private Vehicle mCar;

    private EditText mNick;
    private EditText mManufacturer;
    private EditText mMileage;
    private TextView mMileageUnit;
    private Spinner mTypeSpinner;
    private ImageView mImgCarPhoto;

    private VehicleType mCarType;
    private String mNickToUpdate;
    private String mManufacturerToUpdate;
    private Long mMileageToUpdate;
    private Long mActualMileageToUpdate;

    private View view;

    private DatabaseHelper databaseHelper = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.update_vehicle);

        Intent intent = getIntent();
        mCar = (Vehicle) intent.getSerializableExtra(VehicleTabbedDetail.VEHICLE_TO_FRAGMENT);

        if (mCar == null) {
            setResult(RESULT_CANCELED);
            finish();
        }

        initViews();

        if (savedInstanceState == null) {
            populateFields();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mCurrentPhotoLarge != null) {
            outState.putParcelable(PHOTO, mCurrentPhotoLarge);
        }
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState.containsKey(PHOTO)) {
            mCurrentPhotoLarge = savedInstanceState.getParcelable(PHOTO);
            ImageView im = (ImageView) findViewById(R.id.img_addcar_car);
            im.setImageBitmap(mCurrentPhotoLarge);
        }
    }

    private void initViews() {
        mNick = (EditText) findViewById(R.id.txt_carupdate_nick);
        mManufacturer = (EditText) findViewById(R.id.txt_carupdate_manufacturer);
        mMileage = (EditText) findViewById(R.id.txt_carupdate_mileage);
        mMileageUnit = (TextView) findViewById(R.id.txt_carupdate_unit);
        mTypeSpinner = (Spinner) findViewById(R.id.spinner_carupdate_types);
        mImgCarPhoto = (ImageView) findViewById(R.id.img_addcar_car);
    }

    private void populateFields() {
        mNick.setText(mCar.getName());
        mManufacturer.setText(mCar.getVehicleMaker());
        mMileage.setText(mCar.getStartMileage().toString());
        //TODO set type
        // mTypeSpinner.setSelection(mCar.getCarType().ordinal());
        mMileageUnit.setText(mCar.getUnit().toString());
        if (mCar.getImage() != null) {
            mImgCarPhoto.setImageBitmap(mCar.getImage());
            mCurrentPhotoLarge = mCar.getImage();
        }
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

                Snackbar.make(view, "Take photo", Snackbar.LENGTH_SHORT)
                        .setAction("Action", null).show();
                //TODO
                //performCrop();
            }
            if (requestCode == REQUEST_TAKE_PHOTOS) {
                Snackbar.make(view, "Take photo", Snackbar.LENGTH_SHORT)
                        .setAction("Action", null).show();
                //TODO
                //performCrop();
            }
            if (requestCode == REQUEST_PIC_CROP) {
                Snackbar.make(view, "Take photo", Snackbar.LENGTH_SHORT)
                        .setAction("Action", null).show();
                //TODO
                //setPictureLarge();
            }
        }
    }

    public void onCarImageBtnClick(final View v) {
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
                if (which == TAKE_PHOTO) {
                    takePhoto(v);
                } else if (which == SELECT_PHOTO) {
                    selectFromGallerty(v);
                } else if (which == DELETE_PHOTO) {
                    deletePhoto(v);
                }
                dialog.dismiss();
            }
        });
        getImageFrom.create().show();
    }

    private void takePhoto(View v) {
        Snackbar.make(v, "Take photo", Snackbar.LENGTH_SHORT)
                .setAction("Action", null).show();
        //TODO
        /*Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
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

        }*/
    }

    private void selectFromGallerty(View v) {
        Snackbar.make(v, "Select photo from gallery", Snackbar.LENGTH_SHORT)
                .setAction("Action", null).show();
        //TODO
        //Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI); startActivityForResult(i, REQUEST_PICTURE);
    }

    public void deletePhoto(View v) {
        Snackbar.make(v, "Delete photo", Snackbar.LENGTH_SHORT)
                .setAction("Action", null).show();
        //TODO
        //mCurrentPhotoLarge = null; mImgCarPhoto.setImageDrawable(getResources().getDrawable(android.R.drawable.ic_menu_camera));
    }

    public void onUpdateBtnClick(View v) {
        Editable nick = mNick.getText();
        Editable manufacturer = mManufacturer.getText();
        Editable mileage = mMileage.getText();

        Log.d(TAG, getString(R.string.updateCarActivity_LOG_updateBtnClicked));
        if (TextUtils.isEmpty(nick)
                || TextUtils.isEmpty(manufacturer)
                || TextUtils.isEmpty(mileage)) {
            Toast.makeText(this, R.string.updateCarActivity_emptyFields, Toast.LENGTH_LONG).show();
        } else {
            String msg = null;
            Long createdMileage = Long.valueOf(0);

            try {
                createdMileage = Long.parseLong(mileage.toString());
            } catch (NumberFormatException ex) {
                Log.d(TAG, getString(R.string.updateCarActivity_LOG_badLongNumberFormat));
                msg = getString(R.string.updateCarActivity_wrong_number_format);
            }
            if (msg != null) {
                Toast.makeText(this, msg + " - " + getString(R.string.car_update_mileage), Toast.LENGTH_LONG).show();
            } else {
//                if (nick.toString().equals(mCar.getNick())
//                        && manufacturer.toString().equals(mCar.getTypeName())
//                        && createdMileage.equals(mCar.getStartMileage())
//                        && Car.CarType.valueOf(mTypeSpinner.getSelectedItem().toString()).equals(mCar.getCarType())
//                        ) {
//                    if ((mCurrentPhoto == null && mCar.getImage() == null) || (mCurrentPhoto != null && mCurrentPhoto.equals(mCar.getImage())))
//                    {
//                        //ziadna zmena = konec
//                        Toast.makeText(this, getString(R.string.updateCarActivity_Toast_carNoUpdate), Toast.LENGTH_LONG).show();
//                        setResult(RESULT_OK);
//                        finish();
//                    }
                if (!createdMileage.equals(mCar.getStartMileage())) {
                    //varovanie pri zmene startMielage
                    mNickToUpdate = nick.toString();
                    mManufacturerToUpdate = manufacturer.toString();
                    mMileageToUpdate = createdMileage;
                    mCarType = getTypeFromSpinner();

                    //TODO showUpdateDialogConfirmation
                    //showUpdateDialogConfirmation(createdMileage);
                } else {
                    //pri zmene nazvov len zmena nazvov
                    mCar.setName(nick.toString());
                    mCar.setVehicleMaker(manufacturer.toString());
                    mCar.setType(getTypeFromSpinner());
                    mCar.setImage(mCurrentPhotoLarge);

                    try {
                        getHelper().getVehicleDao().update(mCar);
                        Toast.makeText(this, R.string.addCarActivity_Toast_successfullyCreated, Toast.LENGTH_LONG).show();
                    } catch (SQLException e) {
                        String errorMsg = "Error occured while saving new Vehicle to DB.";
                        Log.e(TAG, errorMsg, e);
                        Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
                        finish();
                    }
                    Toast.makeText(this, getString(R.string.updateCarActivity_Toast_carUpdatedSuccesfully01) + " \""
                                    + mCar.getName() + "\" " + getString(R.string.updateCarActivity_Toast_carUpdatedSuccesfully02),
                            Toast.LENGTH_LONG).show();

                    setResult(RESULT_OK);
                    finish();
                }
            }
        }
    }

    private VehicleType getTypeFromSpinner() {
        try {
            return getHelper().getVehicleTypeDao().queryBuilder().where().eq("name", mTypeSpinner.getSelectedItem().toString()).queryForFirst();
        } catch (SQLException e) {
            VehicleType type;
            try {
                type = getHelper().getVehicleTypeDao().queryBuilder().queryForFirst();
                String msg = "Vehicle type not found, '" + type.getName() + "' was used.";
                Log.e(TAG, msg, e);
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                return type;
            } catch (SQLException ex) {
                String msg = "No vehicle type can be used.";
                Log.e(TAG, msg, e);
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                return null;
            }

        }
    }

    /*private void showUpdateDialogConfirmation(Long mileage) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        mActualMileageToUpdate = mileage - mCar.getStartMileage() + mCar.getActualMileage();
        alertDialogBuilder.setTitle(getString(R.string.updateCarActivity_DialogUpdate_title));
        alertDialogBuilder.setMessage(getString(R.string.updateCarActivity_DialogUpdate_msg01) + " "    //you want change from
                + mCar.getStartMileage().toString() + mCar.getDistanceUnitString() + " "                //VALUE
                + getString(R.string.updateCarActivity_DialogUpdate_msg02) + " " + mileage.toString()    //to VALUE
                + mCar.getDistanceUnitString()
                + getString(R.string.updateCarActivity_DialogUpdate_msg03) + " "                        //this also change from
                + mCar.getActualMileage().toString() + mCar.getDistanceUnitString() + " "                //VALUE
                + getString(R.string.updateCarActivity_DialogUpdate_msg04) + " "                        //to
                + mActualMileageToUpdate.toString() + mCar.getDistanceUnitString()                        //VALUE
                + getString(R.string.updateCarActivity_DialogUpdate_msg05));                            //are you sure?

        // set positive button YES message
        alertDialogBuilder.setPositiveButton(android.R.string.yes,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        Log.d(TAG, getString(R.string.updateCarActivity_LOG_dialogConfirm));
                        Dialog dialog = (Dialog) dialogInterface;
                        Context context = dialog.getContext();

                        mCar.setName(mNickToUpdate);
                        mCar.setVehicleMaker(mManufacturerToUpdate);
                        mCar.setStartMileage(mMileageToUpdate);
                        //TODO setActualMileage
                        //mCar.setActualMileage(mActualMileageToUpdate);
                        mCar.setType(mCarType);

                        //TODO update Vehicle
                        //mCarManager.updateCar(mCar);
                        Toast.makeText(context, getString(R.string.updateCarActivity_Toast_carUpdatedSuccesfully01) + " \""
                                        + mCar.getName() + "\" " + getString(R.string.updateCarActivity_Toast_carUpdatedSuccesfully02),
                                Toast.LENGTH_LONG).show();
                        dialog.dismiss();
                        setResult(RESULT_OK);
                        finish();
                    }
                });

        // set neutral button OK
        alertDialogBuilder.setNeutralButton(android.R.string.no,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Dismiss the dialog
                        Log.d(TAG, getString(R.string.updateCarActivity_LOG_dialogDecline));
                        dialog.dismiss();
                    }
                });

        AlertDialog alertDialog = alertDialogBuilder.create();
        // show alert
        alertDialog.show();
    }*/

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (databaseHelper != null) {
            OpenHelperManager.releaseHelper();
            databaseHelper = null;
        }
    }

    private DatabaseHelper getHelper() {
        if (databaseHelper == null) {
            databaseHelper = OpenHelperManager.getHelper(this,DatabaseHelper.class);
        }
        return databaseHelper;
    }

}
