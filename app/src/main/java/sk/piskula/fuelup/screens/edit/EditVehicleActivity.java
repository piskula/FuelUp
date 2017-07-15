package sk.piskula.fuelup.screens.edit;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.File;

import sk.piskula.fuelup.R;
import sk.piskula.fuelup.adapters.SpinnerVehicleTypesAdapter;
import sk.piskula.fuelup.business.ServiceResult;
import sk.piskula.fuelup.business.VehicleService;
import sk.piskula.fuelup.entity.Vehicle;
import sk.piskula.fuelup.entity.VehicleType;
import sk.piskula.fuelup.screens.VehicleListActivity;
import sk.piskula.fuelup.screens.VehicleTabbedDetailActivity;
import sk.piskula.fuelup.screens.dialog.DeleteDialog;
import sk.piskula.fuelup.screens.dialog.ImageChooserDialog;
import sk.piskula.fuelup.util.ImageUtils;

import static sk.piskula.fuelup.screens.edit.AddVehicleActivity.REQUEST_PICTURE;
import static sk.piskula.fuelup.screens.edit.AddVehicleActivity.REQUEST_TAKE_PHOTOS;
import static sk.piskula.fuelup.screens.edit.AddVehicleActivity.STORAGE_PERMISSIONS_REQUEST;

/**
 * @author Ondrej Oravcok
 * @version 21.6.2017
 */
public class EditVehicleActivity extends AppCompatActivity implements ImageChooserDialog.Callback, MenuItem.OnMenuItemClickListener, DeleteDialog.Callback {

    public static final String TAG = EditVehicleActivity.class.getSimpleName();

    private String currentPhotoPath;
    private EditText txtName;
    private EditText txtManufacturer;
    private Spinner spinnerType;
    private ImageView imgCarPhotoStatus;

    private Vehicle vehicle;
    private SpinnerVehicleTypesAdapter typeAdapter;

    private boolean showImageChooseDialog = false;

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
        this.txtName = (EditText) findViewById(R.id.txt_editVehicle_name);
        this.txtManufacturer = (EditText) findViewById(R.id.txt_editVehicle_manufacturer);
        this.spinnerType = (Spinner) findViewById(R.id.spinner_editVehicle_types);
        this.imgCarPhotoStatus = (ImageView) findViewById(R.id.img_editVehicle_photo);

        typeAdapter = new SpinnerVehicleTypesAdapter(this);
        spinnerType.setAdapter(typeAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // This is workaround to display image chooser dialog when selecting photo
        // see https://stackoverflow.com/questions/33264031/calling-dialogfragments-show-from-within-onrequestpermissionsresult-causes
        if (showImageChooseDialog) {
            if (currentPhotoPath != null) {
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
        currentPhotoPath = vehicle.getPathToPicture();
        imgCarPhotoStatus.setImageResource(currentPhotoPath != null && !currentPhotoPath.isEmpty() ? R.drawable.ic_camera_deny : R.drawable.ic_camera);
    }

    public void onClickAdd(View w) {
        saveVehicle();
    }

    public void onClickPhoto(View w) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSIONS_REQUEST);
        } else {
            if (currentPhotoPath != null && !currentPhotoPath.isEmpty()) {
                deletePhoto();
            } else {
                new ImageChooserDialog().show(getSupportFragmentManager(), ImageChooserDialog.class.getSimpleName());
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_PICTURE) {
            currentPhotoPath = ImageUtils.onActivityResultTakePhoto(this, data);
        }
        if (requestCode == REQUEST_TAKE_PHOTOS) {
            currentPhotoPath = ImageUtils.performCrop(this, currentPhotoPath);
        }
        if (currentPhotoPath != null)
            Snackbar.make(findViewById(android.R.id.content), R.string.addVehicle_picture_added, Snackbar.LENGTH_SHORT).show();
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
        currentPhotoPath = ImageUtils.onStartTakePhoto(this);
    }

    public void deletePhoto() {
        currentPhotoPath = null;
        imgCarPhotoStatus.setImageResource(R.drawable.ic_camera);
        Snackbar.make(findViewById(android.R.id.content), R.string.delete_vehicle_photo, Snackbar.LENGTH_SHORT).show();
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
        String name = txtName.getText().toString();
        String vehicleManufacturer = txtManufacturer.getText().toString();

        if (name.isEmpty() || vehicleManufacturer.isEmpty()) {
            Snackbar.make(findViewById(android.R.id.content), R.string.toast_emptyFields, Snackbar.LENGTH_LONG).show();
            return;
        }

        vehicle.setName(name);
        vehicle.setVehicleMaker(vehicleManufacturer);
        vehicle.setType((VehicleType) spinnerType.getSelectedItem());

        if (vehicle.getPathToPicture() != currentPhotoPath) {
            if (vehicle.getPathToPicture() != null && !vehicle.getPathToPicture().isEmpty()) {
                File file = new File(vehicle.getPathToPicture());
                if (!file.delete()) {
                    Toast.makeText(this, getString(R.string.editVehicle_toast_oldPhotoWasNotRemovedDueToError), Toast.LENGTH_LONG).show();
                }
            }
            vehicle.setPathToPicture(currentPhotoPath);
        }

        VehicleService vehicleService = new VehicleService(this);

        ServiceResult result = vehicleService.update(vehicle);
        if (ServiceResult.SUCCESS.equals(result)) {
            Toast.makeText(this, R.string.carUpdate_Toast_successfullyUpdated, Toast.LENGTH_LONG).show();
            setResult(RESULT_OK);
        } else {
            setResult(RESULT_CANCELED);
            Snackbar.make(findViewById(android.R.id.content), R.string.carUpdate_Toast_updateFail, Snackbar.LENGTH_LONG).show();
        }
        finish();
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

        VehicleService vehicleService = new VehicleService(EditVehicleActivity.this);
        ServiceResult result = vehicleService.delete(vehicle);

        if (ServiceResult.SUCCESS.equals(result)) {
            Toast.makeText(getApplicationContext(), getString(R.string.delete_vehicle_success, vehicle.getName()), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getApplicationContext(), R.string.delete_vehicle_fail, Toast.LENGTH_LONG).show();
        }

        Intent intent = new Intent(this, VehicleListActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @Override
    public void onDeleteDialogNegativeClick(DeleteDialog deleteDialog) {
        deleteDialog.dismiss();
    }
}
