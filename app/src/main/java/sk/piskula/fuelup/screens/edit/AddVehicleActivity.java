package sk.piskula.fuelup.screens.edit;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
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
import sk.piskula.fuelup.business.ServiceResult;
import sk.piskula.fuelup.business.VehicleService;
import sk.piskula.fuelup.entity.Vehicle;
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

    public static final String TAG = "AddVehicleActivity";
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
    private RadioGroup radioGroupDistanceUnit;
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
        this.radioGroupDistanceUnit = (RadioGroup) findViewById(R.id.radio_distance_unit);
        this.imgCarPhotoStatus = (ImageView) findViewById(R.id.img_addVehicle_photo);

        spinnerCurrency.setAdapter(new SpinnerCurrencyAdapter(this));
        spinnerType.setAdapter(new SpinnerVehicleTypesAdapter(this));

        radioGroupDistanceUnit.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, @IdRes int i) {
                if (radioGroup.getCheckedRadioButtonId() == R.id.radio_km)
                    txtActualMileageDistanceUnit.setText(DistanceUnit.km.toString());
                else txtActualMileageDistanceUnit.setText(DistanceUnit.mi.toString());
            }
        });
        this.radioGroupDistanceUnit.check(R.id.radio_km);
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

    private void saveVehicle() {
        String name = txtName.getText().toString();
        String manufacturer = txtManufacturer.getText().toString();
        String actualMileage = txtActualMileage.getText().toString();

        if (name.isEmpty() || manufacturer.isEmpty() || actualMileage.isEmpty()) {
            Snackbar.make(findViewById(android.R.id.content), R.string.toast_emptyFields, Snackbar.LENGTH_LONG).show();
            return;
        }

        Vehicle createdVehicle = new Vehicle();
        createdVehicle.setName(name);
        createdVehicle.setVehicleMaker(manufacturer);
        createdVehicle.setCurrency((Currency) spinnerCurrency.getSelectedItem());
        createdVehicle.setType((VehicleType) spinnerType.getSelectedItem());
        createdVehicle.setPathToPicture(vehiclePicturePath);
        createdVehicle.setDistanceUnit(radioGroupDistanceUnit.getCheckedRadioButtonId() == R.id.radio_km ? DistanceUnit.km : DistanceUnit.mi);
        createdVehicle.setVolumeUnit(createdVehicle.getDistanceUnit() == DistanceUnit.km ? VolumeUnit.LITRE : VolumeUnit.GALLON_US);
        createdVehicle.setStartMileage(Long.parseLong(actualMileage));

        VehicleService vehicleService = new VehicleService(this);

        ServiceResult result = vehicleService.save(createdVehicle);
        if (ServiceResult.SUCCESS.equals(result)) {
            Toast.makeText(this, R.string.addVehicle_Toast_successfullyCreated, Toast.LENGTH_LONG).show();
            finish();
        }
        if (ServiceResult.ERROR_DUPLICATE.equals(result)) {
            Snackbar.make(findViewById(android.R.id.content), R.string.addVehicle_fail_duplicate, Snackbar.LENGTH_LONG).show();
        }
        if (ServiceResult.ERROR.equals(result)) {
            Snackbar.make(findViewById(android.R.id.content), R.string.addVehicle_fail, Snackbar.LENGTH_LONG).show();
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
