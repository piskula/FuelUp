package sk.momosi.fuelup.screens.edit;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.File;

import sk.momosi.fuelup.R;
import sk.momosi.fuelup.adapters.SpinnerVehicleTypesAdapter;
import sk.momosi.fuelup.business.VehicleService;
import sk.momosi.fuelup.data.FuelUpContract.VehicleEntry;
import sk.momosi.fuelup.data.provider.VehicleProvider;
import sk.momosi.fuelup.entity.Vehicle;
import sk.momosi.fuelup.entity.VehicleType;
import sk.momosi.fuelup.screens.MainActivity;
import sk.momosi.fuelup.screens.VehicleTabbedDetailActivity;
import sk.momosi.fuelup.screens.dialog.DeleteDialog;

/**
 * @author Ondrej Oravcok
 * @version 21.6.2017
 */
public class EditVehicleActivity extends VehicleAbstractActivity implements MenuItem.OnMenuItemClickListener, DeleteDialog.Callback {

    public static final String TAG = EditVehicleActivity.class.getSimpleName();

    private EditText txtName;
    private EditText txtManufacturer;
    private Spinner spinnerType;

    private Vehicle vehicle;
    private SpinnerVehicleTypesAdapter typeAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vehicle_edit);

        Intent intent = getIntent();
        vehicle = intent.getParcelableExtra(VehicleTabbedDetailActivity.VEHICLE_TO_FRAGMENT);

        if (vehicle == null) {
            setResult(RESULT_CANCELED);
            finish();
        }

        initViews();
        populateFields();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    private void initViews() {
        this.txtName = findViewById(R.id.txt_editVehicle_name);
        this.txtManufacturer = findViewById(R.id.txt_editVehicle_manufacturer);
        this.spinnerType = findViewById(R.id.spinner_editVehicle_types);
        this.imgCarPhoto = findViewById(R.id.img_editVehicle_photo);
        this.imgCarPhotoRemove = findViewById(R.id.img_editVehicle_removePhoto);

        typeAdapter = new SpinnerVehicleTypesAdapter(this);
        spinnerType.setAdapter(typeAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.vehicle_edit_create, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem actionViewItem = menu.findItem(R.id.btn_removeVehicle_bar);
        actionViewItem.setTitle(getString(R.string.update));
        actionViewItem.setOnMenuItemClickListener(this);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        return deleteVehicle();
    }

    private void populateFields() {
        txtName.setText(vehicle.getName());
        txtManufacturer.setText(vehicle.getVehicleMaker());
        spinnerType.setSelection(getAlreadySelectedTypePosition(vehicle.getType()));
        vehiclePicturePath = vehicle.getPathToPicture();
        if (vehiclePicturePath != null && new File(vehiclePicturePath).exists()) {
            imgCarPhoto.setImageBitmap(BitmapFactory.decodeFile(vehiclePicturePath));
            imgCarPhoto.setAlpha(REMOVE_PHOTO_ALPHA_CHANNEL);
            imgCarPhotoRemove.setVisibility(View.VISIBLE);
        } else {
            vehiclePicturePath = "";
            imgCarPhoto.setImageResource(R.drawable.ic_insert_photo);
            imgCarPhoto.setAlpha(1f);
            imgCarPhotoRemove.setVisibility(View.GONE);
        }
    }

    public void onClickAdd(View w) {
        saveVehicle();
    }

    private boolean deleteVehicle() {
        DeleteDialog.newInstance(
                getString(R.string.delete_vehicle_dialog_title, vehicle.getName()),
                getString(R.string.delete_vehicle_dialog_message, vehicle.getName()),
                R.drawable.tow)
                .show(getSupportFragmentManager(), DeleteDialog.class.getSimpleName());
        return true;
    }

    private void saveVehicle() {
        ContentValues contentValues = new ContentValues();
        String name = txtName.getText().toString();
        String vehicleManufacturer = txtManufacturer.getText().toString();

        if (name.isEmpty()) {
            Snackbar.make(findViewById(android.R.id.content), R.string.toast_emptyName, Snackbar.LENGTH_LONG).show();
            return;
        }
        if (VehicleService.isVehicleNameTaken(name, this)) {
            Snackbar.make(findViewById(android.R.id.content), R.string.toast_nameNotUnique, Snackbar.LENGTH_LONG).show();
            return;
        }

        contentValues.put(VehicleEntry.COLUMN_NAME, name);
        contentValues.put(VehicleEntry.COLUMN_VEHICLE_MAKER, vehicleManufacturer);
        contentValues.put(VehicleEntry.COLUMN_TYPE, ((VehicleType) spinnerType.getSelectedItem()).getId());

        if (!vehiclePicturePath.equals(vehicle.getPathToPicture())) {
            if (vehicle.getPathToPicture() != null && !vehicle.getPathToPicture().isEmpty()) {
                File file = new File(vehicle.getPathToPicture());
                if (!file.delete()) {
                    Toast.makeText(this, getString(R.string.editVehicle_toast_oldPhotoWasNotRemovedDueToError), Toast.LENGTH_LONG).show();
                }
            }
            contentValues.put(VehicleEntry.COLUMN_PICTURE, vehiclePicturePath);
        }

        final int result = getContentResolver().update(
                ContentUris.withAppendedId(VehicleEntry.CONTENT_URI, vehicle.getId()),
                contentValues, null, null);

        if (result == 1) {  //update OK
            setResult(RESULT_OK);
            Toast.makeText(this, R.string.carUpdate_Toast_successfullyUpdated, Toast.LENGTH_LONG).show();
            finish();

        } else if (result == VehicleProvider.VEHICLE_UPDATE_NAME_NOT_UNIQUE) {
            Toast.makeText(this, R.string.addVehicle_nameNotUnique, Toast.LENGTH_SHORT).show();

        } else {    //update FAIL
            setResult(RESULT_CANCELED);
            Snackbar.make(findViewById(android.R.id.content), R.string.carUpdate_Toast_updateFail, Snackbar.LENGTH_LONG).show();
            finish();
        }
    }

    private int getAlreadySelectedTypePosition(VehicleType type) {
        int i = 0;
        for (VehicleType found : typeAdapter.getVehicleTypes()) {
            if (found.equals(type)) {
                return i;
            }
            i++;
        }
        return i;
    }

    @Override
    public void onDeleteDialogPositiveClick(DeleteDialog deleteDialog) {
        deleteDialog.dismiss();

        final int result = getContentResolver().delete(
                ContentUris.withAppendedId(VehicleEntry.CONTENT_URI, vehicle.getId()), null, null);

        if (result != -1) {
            Toast.makeText(getApplicationContext(), getString(R.string.delete_vehicle_success, vehicle.getName()), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getApplicationContext(), R.string.delete_vehicle_fail, Toast.LENGTH_LONG).show();
        }

        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @Override
    public void onDeleteDialogNegativeClick(DeleteDialog deleteDialog) {
        deleteDialog.dismiss();
    }
}
