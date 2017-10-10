package sk.piskula.fuelup.screens.edit;

import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Currency;

import sk.piskula.fuelup.R;
import sk.piskula.fuelup.adapters.SpinnerCurrencyAdapter;
import sk.piskula.fuelup.adapters.SpinnerVehicleTypesAdapter;
import sk.piskula.fuelup.data.FuelUpContract.VehicleEntry;
import sk.piskula.fuelup.entity.VehicleType;
import sk.piskula.fuelup.entity.enums.DistanceUnit;
import sk.piskula.fuelup.entity.enums.VolumeUnit;

/**
 * @author Ondrej Oravcok
 * @version 18.6.2017
 */
public class AddVehicleActivity extends VehicleAbstractActivity {

    private static final String LOG_TAG = AddVehicleActivity.class.getSimpleName();

    private EditText txtName;
    private EditText txtManufacturer;
    private EditText txtActualMileage;
    private TextView txtActualMileageDistanceUnit;
    private Spinner spinnerType;
    private Spinner spinnerCurrency;
    private RadioGroup radioGroupVolumeUnit;

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

    private void initViews() {
        this.txtName = findViewById(R.id.txt_addVehicle_name);
        this.txtManufacturer = findViewById(R.id.txt_addVehicle_manufacturer);
        this.txtActualMileage = findViewById(R.id.txt_addVehicle_mileage);
        this.txtActualMileageDistanceUnit = findViewById(R.id.txt_addVehicle_mileage_distanceUnit);
        this.spinnerType = findViewById(R.id.spinner_addVehicle_types);
        this.spinnerCurrency = findViewById(R.id.spinner_currency);
        this.radioGroupVolumeUnit = findViewById(R.id.radio_volume_unit);
        this.imgCarPhoto = findViewById(R.id.img_addVehicle_photo);
        this.imgCarPhotoRemove = findViewById(R.id.img_addVehicle_removePhoto);

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

}
