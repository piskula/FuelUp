package sk.piskula.fuelup.screens.edit;

import android.app.DatePickerDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Calendar;

import sk.piskula.fuelup.R;
import sk.piskula.fuelup.business.FillUpService;
import sk.piskula.fuelup.data.FuelUpContract.FillUpEntry;
import sk.piskula.fuelup.entity.FillUp;
import sk.piskula.fuelup.entity.Vehicle;
import sk.piskula.fuelup.entity.util.DateUtil;
import sk.piskula.fuelup.entity.util.VolumeUtil;
import sk.piskula.fuelup.screens.detailfragments.FillUpsListFragment;
import sk.piskula.fuelup.screens.dialog.DeleteDialog;


public class EditFillUpActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener, DeleteDialog.Callback {

    public static final String TAG = EditFillUpActivity.class.getSimpleName();

    public static final String EXTRA_FILLUP = "extra_fillup_to_update";

    private EditText mTxtDistance;
    private EditText mTxtFuelVolume;
    private TextView mTxtFuelVolumeUnit;
    private EditText mTxtPrice;
    private EditText mTxtInfo;
    private TextView mTxtDate;

    private TextInputLayout mTxtInputDistance;
    private TextInputLayout mTxtInputPrice;

    private TextView mTxtDistanceUnit;
    private TextView mTxtCurrencySymbol;

    private Button mBtnAdd;
    private ToggleButton mBtnSwitchPrice;
    private CheckBox mCheckBoxIsFullFill;

    private Vehicle mVehicle;
    private FillUp mSelectedFillUp;
    private SwitchPrice priceMode = SwitchPrice.perVolume;

    private Calendar fillUpDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fillup_edit);

        long fillUpId = getIntent().getLongExtra(FillUpsListFragment.FILLUP_ID_TO_EDIT, 0);
        mSelectedFillUp = FillUpService.getFillUpById(fillUpId, getApplicationContext());
        mVehicle = mSelectedFillUp.getVehicle();

        initViews();
    }

    @Override
    protected void onStart() {
        super.onStart();

        mTxtDistance.setText(mSelectedFillUp.getDistanceFromLastFillUp().toString());
        mTxtFuelVolumeUnit.setText(mVehicle.getVolumeUnit().toString());
        mTxtFuelVolume.setText(NumberFormat.getNumberInstance().format(mSelectedFillUp.getFuelVolume()));
        this.onCheckedChanged(mBtnSwitchPrice, false);
        if (priceMode == SwitchPrice.perVolume) {
            mTxtPrice.setText(mSelectedFillUp.getFuelPricePerLitre().setScale(2, BigDecimal.ROUND_HALF_UP).toString());
        } else {
            mTxtPrice.setText(mSelectedFillUp.getFuelPriceTotal().setScale(2, BigDecimal.ROUND_HALF_UP).toString());
        }
        if (mSelectedFillUp.isFullFillUp()) {
            mCheckBoxIsFullFill.setChecked(true);
        } else {
            mCheckBoxIsFullFill.setChecked(false);
        }
        mTxtInfo.setText(mSelectedFillUp.getInfo());

        setFillUpDate(DateUtil.transformToCal(mSelectedFillUp.getDate()));
        mTxtDate.setEnabled(false);

        mBtnAdd.setText(R.string.update);

        mTxtCurrencySymbol.setText(mVehicle.getCurrencySymbol());
        mTxtDistanceUnit.setText(mVehicle.getDistanceUnit().toString());
    }

    private void initViews() {
        mTxtDistance = findViewById(R.id.txt_addfillup_distance_from_last_fillup_adding);
        mTxtDistanceUnit = findViewById(R.id.txt_addfillup_distance_unit);
        mTxtInputDistance = findViewById(R.id.txt_input_addfillup_distance_from_last_fillup_adding);
        mTxtFuelVolume = findViewById(R.id.txt_addfillup_fuel_volume);
        mTxtFuelVolumeUnit = findViewById(R.id.txt_addfillup_volumeUnit);
        mTxtPrice = findViewById(R.id.txt_addfillup_price);
        mTxtCurrencySymbol = findViewById(R.id.txt_addfillup_currency);
        mTxtInputPrice = findViewById(R.id.txt_input_addfillup_price);
        mTxtDate = findViewById(R.id.txt_addfillup_date);
        mTxtInfo = findViewById(R.id.txt_addfillup_information);
        mCheckBoxIsFullFill = findViewById(R.id.checkBox_fullFillUp);
        mBtnAdd = findViewById(R.id.btn_add_fillup);
        mBtnSwitchPrice = findViewById(R.id.btn_switch_price);

        mBtnSwitchPrice.setOnCheckedChangeListener(this);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(R.string.add_fillup_title_update);
    }

    /**
     * OnClickListener for add button
     */
    public void onClickAdd(View view) {
        Editable distance = mTxtDistance.getText();
        Editable fuelVol = mTxtFuelVolume.getText();
        Editable price = mTxtPrice.getText();
        Editable info = mTxtInfo.getText();
//        String date = mTxtDate.getText().toString();
        boolean isFull = mCheckBoxIsFullFill.isChecked();

        if (TextUtils.isEmpty(distance) || TextUtils.isEmpty(fuelVol) || TextUtils.isEmpty(price) && mVehicle != null) {
            Toast.makeText(this, R.string.toast_emptyFields, Toast.LENGTH_LONG).show();
            return;
        }

        DecimalFormat decimalFormat = new DecimalFormat();
        decimalFormat.setParseBigDecimal(true);

        Long createdDistance = Long.parseLong(distance.toString());

        BigDecimal createdFuelVol = null;
        BigDecimal createdPrice = null;

        try {
            createdFuelVol = (BigDecimal) decimalFormat.parse(fuelVol.toString());
            createdPrice = (BigDecimal) decimalFormat.parse(price.toString());
        } catch (ParseException ex) {
            Log.d(TAG, "tried bad format", ex);
            throw new RuntimeException(ex);
        }

        ContentValues contentValues = new ContentValues();

        contentValues.put(FillUpEntry.COLUMN_IS_FULL_FILLUP, isFull ? 1 : 0);
        contentValues.put(FillUpEntry.COLUMN_DISTANCE_FROM_LAST, createdDistance);
        contentValues.put(FillUpEntry.COLUMN_FUEL_VOLUME, createdFuelVol.doubleValue());
        contentValues.put(FillUpEntry.COLUMN_INFO, info.toString().trim());

        if (priceMode == SwitchPrice.perVolume) {
            contentValues.put(FillUpEntry.COLUMN_FUEL_PRICE_PER_LITRE, createdPrice.doubleValue());
            contentValues.put(FillUpEntry.COLUMN_FUEL_PRICE_TOTAL, VolumeUtil.getTotalPriceFromPerLitre(
                    createdFuelVol, createdPrice, mVehicle.getVolumeUnit()).doubleValue());
        } else {
            contentValues.put(FillUpEntry.COLUMN_FUEL_PRICE_TOTAL, createdPrice.doubleValue());
            contentValues.put(FillUpEntry.COLUMN_FUEL_PRICE_PER_LITRE, VolumeUtil.getPerLitrePriceFromTotal(
                    createdFuelVol, createdPrice, mVehicle.getVolumeUnit()).doubleValue());
        }

        if (getContentResolver().update(ContentUris.withAppendedId(FillUpEntry.CONTENT_URI, mSelectedFillUp.getId()), contentValues, null, null) == 1) {
            Toast.makeText(getApplicationContext(), R.string.add_fillup_success_update, Toast.LENGTH_LONG).show();
            setResult(RESULT_OK);
        } else {
            Toast.makeText(getApplicationContext(), R.string.add_fillup_fail_update, Toast.LENGTH_LONG).show();
            setResult(RESULT_CANCELED);
        }

        finish();
    }

    /**
     * OnClick listener for date text view
     */
    public void onClickDatePicker(View view) {
        new DatePickerDialog(EditFillUpActivity.this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int y, int m, int d) {
                Calendar serviceDate = Calendar.getInstance();
                serviceDate.set(y, m, d);
                setFillUpDate(serviceDate);
            }
        }, fillUpDate.get(Calendar.YEAR), fillUpDate.get(Calendar.MONTH), fillUpDate.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void setFillUpDate(Calendar calendar) {
        this.fillUpDate = calendar;
        mTxtDate.setText(DateUtil.getDateLocalized(calendar));
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
        if (compoundButton.getId() == mBtnSwitchPrice.getId()) {
            if (isChecked) {
                priceMode = SwitchPrice.total;
                mTxtInputPrice.setHint(getString(R.string.add_fillup_priceTotal));
                mTxtCurrencySymbol.setText(mVehicle.getCurrencySymbol());
            } else {
                priceMode = SwitchPrice.perVolume;
                mTxtInputPrice.setHint(getString(R.string.add_fillup_pricePerLitre));
                mTxtCurrencySymbol.setText(getString(R.string.unit_pricePerLitre, mVehicle.getCurrencySymbol()));
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.fillup_edit, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.btn_delete_fillup:
                DeleteDialog.newInstance(getString(R.string.remove_fillup_dialog_title),
                        getString(R.string.remove_fillup_dialog_message), R.drawable.tow)
                        .show(getSupportFragmentManager(), DeleteDialog.class.getSimpleName());
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDeleteDialogPositiveClick(DeleteDialog dialog) {
        // TODO delete fillUp correctly with
        // service.deleteWithConsumptionCalculation(mSelectedFillUp);

        final int result = getContentResolver().delete(
                ContentUris.withAppendedId(FillUpEntry.CONTENT_URI, mSelectedFillUp.getId()), null, null);

        if (result != -1) {
            Toast.makeText(getApplicationContext(), getString(R.string.remove_expense_success), Toast.LENGTH_LONG).show();
            setResult(RESULT_OK);
        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.remove_expense_fail), Toast.LENGTH_LONG).show();
            setResult(RESULT_CANCELED);
        }

        dialog.dismiss();
        finish();
    }

    @Override
    public void onDeleteDialogNegativeClick(DeleteDialog dialog) {
        dialog.dismiss();
    }

    private enum SwitchPrice {
        total,
        perVolume
    }
}
