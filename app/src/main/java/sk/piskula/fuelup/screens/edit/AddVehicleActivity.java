package sk.piskula.fuelup.screens.edit;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Currency;

import sk.piskula.fuelup.R;
import sk.piskula.fuelup.adapters.SpinnerCurrencyAdapter;
import sk.piskula.fuelup.adapters.SpinnerVehicleTypesAdapter;
import sk.piskula.fuelup.business.VehicleService;
import sk.piskula.fuelup.data.FuelUpContract.VehicleEntry;
import sk.piskula.fuelup.entity.VehicleType;
import sk.piskula.fuelup.entity.enums.DistanceUnit;
import sk.piskula.fuelup.entity.enums.VolumeUnit;
import sk.piskula.fuelup.screens.dialog.ImageChooserDialog;
import sk.piskula.fuelup.util.ImageUtils;

/**
 * @author Ondrej Oravcok
 * @version 18.6.2017
 */
public class AddVehicleActivity extends AppCompatActivity implements ImageChooserDialog.Callback {

    private static final String LOG_TAG = AddVehicleActivity.class.getSimpleName();

    private static final String PHOTO = "photo";
    public static final int REQUEST_PICTURE = 1113;
    public static final int REQUEST_TAKE_PHOTOS = 1112;
    public static final int REQUEST_PIC_CROP = 1111;
    public static final int STORAGE_PERMISSIONS_REQUEST = 1114;

    private EditText txtName;
    private EditText txtManufacturer;
    private EditText txtActualMileage;
    private TextView txtActualMileageDistanceUnit;
    private Spinner spinnerType;
    private Spinner spinnerCurrency;
    private RadioGroup radioGroupVolumeUnit;
    private Button buttonAdd;
    private ImageView imgCarPhotoStatus;

    private String vehiclePicturePath;

    private boolean showImageChooseDialog = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vehicle_add);
        Intent intent = getIntent();

        initViews();

        String nameFromDialog = intent.getStringExtra("vehicleName");
        if (nameFromDialog != null) {
            txtName.setText(nameFromDialog);
            txtName.setSelection(nameFromDialog.length());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // This is workaround to display image chooser dialog when selecting photo
        // see https://stackoverflow.com/questions/33264031/calling-dialogfragments-show-from-within-onrequestpermissionsresult-causes
        if (showImageChooseDialog) {
            if (vehiclePicturePath != null) {
                deletePhoto();
            } else {
                getSupportFragmentManager();
                ImageChooserDialog d = new ImageChooserDialog();
                d.show(getSupportFragmentManager(), ImageChooserDialog.class.getSimpleName());
            }
            showImageChooseDialog = false;
        }

    }

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
            imgCarPhotoStatus.setImageResource(R.drawable.ic_camera_deny);
        }
    }

    private void initViews() {
        this.txtName = (EditText) findViewById(R.id.txt_addVehicle_name);
        this.txtManufacturer = (EditText) findViewById(R.id.txt_addVehicle_manufacturer);
        this.txtActualMileage = (EditText) findViewById(R.id.txt_addVehicle_mileage);
        this.txtActualMileageDistanceUnit = (TextView) findViewById(R.id.txt_addVehicle_mileage_distanceUnit);
        this.buttonAdd = (Button) findViewById(R.id.btn_addVehicle_add);
        this.spinnerType = (Spinner) findViewById(R.id.spinner_addVehicle_types);
        this.spinnerCurrency = (Spinner) findViewById(R.id.spinner_currency);
        this.radioGroupVolumeUnit = (RadioGroup) findViewById(R.id.radio_volume_unit);
        this.imgCarPhotoStatus = (ImageView) findViewById(R.id.img_addVehicle_photo);

        spinnerCurrency.setAdapter(new SpinnerCurrencyAdapter(this));
        spinnerType.setAdapter(new SpinnerVehicleTypesAdapter(this));

        radioGroupVolumeUnit.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, @IdRes int i) {
                if (radioGroup.getCheckedRadioButtonId() == R.id.radio_litres)
                    txtActualMileageDistanceUnit.setText(DistanceUnit.km.toString());
                else txtActualMileageDistanceUnit.setText(DistanceUnit.mi.toString());
            }
        });
        this.radioGroupVolumeUnit.check(R.id.radio_litres);
    }

    public void onClickAdd(View w) {
        saveVehicle();
    }

    public void onClickPhoto(View w) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSIONS_REQUEST);
        } else {
            if (vehiclePicturePath != null) {
                deletePhoto();
            } else {
                new ImageChooserDialog().show(getSupportFragmentManager(), ImageChooserDialog.class.getSimpleName());
            }
        }
    }

    private VolumeUnit getVolumeUnitFromRadio() {
        switch (radioGroupVolumeUnit.getCheckedRadioButtonId()) {
            case R.id.radio_litres:
                return VolumeUnit.LITRE;
            case R.id.radio_us_gallons:
                return VolumeUnit.GALLON_US;
            case R.id.radio_uk_gallons:
                return VolumeUnit.GALLON_UK;
        }
        return null;
    }

    private void saveVehicle() {
        String name = txtName.getText().toString();
        String manufacturer = txtManufacturer.getText().toString();
        String actualMileage = txtActualMileage.getText().toString();
        String currency = ((Currency) spinnerCurrency.getSelectedItem()).getCurrencyCode();
        long typeId = ((VehicleType) spinnerType.getSelectedItem()).getId();

        if (name.isEmpty()) {
            Snackbar.make(findViewById(android.R.id.content), R.string.toast_emptyName, Snackbar.LENGTH_LONG).show();
            return;
        }

        ContentValues contentValues = new ContentValues();
        contentValues.put(VehicleEntry.COLUMN_NAME, name);
        contentValues.put(VehicleEntry.COLUMN_VEHICLE_MAKER, manufacturer);
        contentValues.put(VehicleEntry.COLUMN_CURRENCY, currency);
        contentValues.put(VehicleEntry.COLUMN_TYPE, typeId);
        contentValues.put(VehicleEntry.COLUMN_PICTURE, vehiclePicturePath);
        contentValues.put(VehicleEntry.COLUMN_VOLUME_UNIT, getVolumeUnitFromRadio().name());
        contentValues.put(VehicleEntry.COLUMN_START_MILEAGE, actualMileage.isEmpty() ? null : Long.parseLong(actualMileage));

        // TODO check name unique
        if (getContentResolver().insert(VehicleEntry.CONTENT_URI, contentValues) == null) {
            Log.e(LOG_TAG, "Cannot create vehicle " + contentValues);
            Snackbar.make(findViewById(android.R.id.content), R.string.addVehicle_fail, Snackbar.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, R.string.addVehicle_Toast_successfullyCreated, Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_PICTURE) {
                vehiclePicturePath = ImageUtils.onActivityResultTakePhoto(this, data);
            }
            if (requestCode == REQUEST_TAKE_PHOTOS) {
                vehiclePicturePath = ImageUtils.performCrop(this, vehiclePicturePath);
            }
            if (vehiclePicturePath != null)
                Snackbar.make(findViewById(android.R.id.content), R.string.addVehicle_picture_added, Snackbar.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case STORAGE_PERMISSIONS_REQUEST: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    showImageChooseDialog = true;
                }
            }
        }
    }

    @Override
    public void onSelectFromGallery(ImageChooserDialog dialog) {
        ImageUtils.selectPhotoFromGallery(this);
    }

    @Override
    public void onTakePhoto(ImageChooserDialog dialog) {
        vehiclePicturePath = ImageUtils.onStartTakePhoto(this);
    }

    public void deletePhoto() {
        // TODO remove photo from storage
        vehiclePicturePath = null;
        imgCarPhotoStatus.setImageResource(R.drawable.ic_camera);
        Snackbar.make(findViewById(android.R.id.content), R.string.delete_vehicle_photo, Snackbar.LENGTH_SHORT).show();
    }

}
