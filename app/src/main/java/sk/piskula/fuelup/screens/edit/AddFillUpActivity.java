package sk.piskula.fuelup.screens.edit;

import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import sk.piskula.fuelup.R;
import sk.piskula.fuelup.business.FillUpService;
import sk.piskula.fuelup.data.FuelUpContract.FillUpEntry;
import sk.piskula.fuelup.entity.Vehicle;
import sk.piskula.fuelup.entity.util.DateUtil;
import sk.piskula.fuelup.entity.util.VolumeUtil;
import sk.piskula.fuelup.screens.detailfragments.FillUpsListFragment;


public class AddFillUpActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {

    public static final String TAG = AddFillUpActivity.class.getSimpleName();

    private EditText mTxtDistance;
    private EditText mTxtFuelVolume;
    private TextView mTxtFuelVolumeUnit;
    private EditText mTxtPrice;
    private EditText mTxtInfo;
    private TextView mTxtDate;

    private TextInputLayout mTxtInputPrice;

    private TextView mTxtDistanceUnit;
    private TextView mTxtCurrencySymbol;

    private ToggleButton mBtnSwitchPrice;
    private CheckBox mCheckBoxIsFullFill;

    private Vehicle mVehicle;
    private SwitchPrice priceMode = SwitchPrice.perVolume;

    private Calendar fillUpDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fillup_edit);

        mVehicle = getIntent()
                .getParcelableExtra(FillUpsListFragment.VEHICLE_FROM_FRAGMENT_TO_EDIT_FILLUP);

        initViews();
    }

    @Override
    protected void onStart() {
        super.onStart();

        setFillUpDate(Calendar.getInstance());
        mTxtCurrencySymbol.setText(mVehicle.getCurrencySymbol());
        mTxtDistanceUnit.setText(mVehicle.getDistanceUnit().toString());
        mTxtFuelVolumeUnit.setText(mVehicle.getVolumeUnit().toString());
        this.onCheckedChanged(mBtnSwitchPrice, false);
    }

    private void initViews() {

        mTxtDistance = findViewById(R.id.txt_addfillup_distance_from_last_fillup_adding);
        mTxtDistanceUnit = findViewById(R.id.txt_addfillup_distance_unit);
        mTxtFuelVolume = findViewById(R.id.txt_addfillup_fuel_volume);
        mTxtFuelVolumeUnit = findViewById(R.id.txt_addfillup_volumeUnit);
        mTxtPrice = findViewById(R.id.txt_addfillup_price);
        mTxtCurrencySymbol = findViewById(R.id.txt_addfillup_currency);
        mTxtInputPrice = findViewById(R.id.txt_input_addfillup_price);
        mTxtDate = findViewById(R.id.txt_addfillup_date);
        mTxtInfo = findViewById(R.id.txt_addfillup_information);
        mCheckBoxIsFullFill = findViewById(R.id.checkBox_fullFillUp);
        mBtnSwitchPrice = findViewById(R.id.btn_switch_price);

        mBtnSwitchPrice.setOnCheckedChangeListener(this);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(R.string.add_fillup_title_create);
    }

    public void onClickAdd(View view) {
        Editable distance = mTxtDistance.getText();
        Editable fuelVol = mTxtFuelVolume.getText();
        Editable price = mTxtPrice.getText();
        Editable info = mTxtInfo.getText();
        String date = mTxtDate.getText().toString();
        boolean isFull = mCheckBoxIsFullFill.isChecked();

        if (TextUtils.isEmpty(distance) || TextUtils.isEmpty(fuelVol) || TextUtils.isEmpty(date) || TextUtils.isEmpty(price) && mVehicle != null) {
            Toast.makeText(this, R.string.toast_emptyFields, Toast.LENGTH_LONG).show();
            return;
        }

        DecimalFormat decimalFormat = new DecimalFormat();
        decimalFormat.setParseBigDecimal(true);

        Long createdDistance = Long.parseLong(distance.toString());

        Calendar createdDate;
        BigDecimal createdFuelVol;
        BigDecimal createdPrice;
        try {
            createdFuelVol = (BigDecimal) decimalFormat.parse(fuelVol.toString());
            createdPrice = (BigDecimal) decimalFormat.parse(price.toString());
            createdDate = DateUtil.parseDateTimeFromString(date, getApplicationContext());
        } catch (ParseException ex) {
            Log.d(TAG, "tried bad format", ex);
            throw new RuntimeException(ex);
        }

        ContentValues contentValues = new ContentValues();

        contentValues.put(FillUpEntry.COLUMN_VEHICLE, mVehicle.getId());
        contentValues.put(FillUpEntry.COLUMN_DISTANCE_FROM_LAST, createdDistance);
        contentValues.put(FillUpEntry.COLUMN_FUEL_VOLUME, createdFuelVol.doubleValue());
        contentValues.put(FillUpEntry.COLUMN_IS_FULL_FILLUP, isFull ? 1 : 0);
        contentValues.put(FillUpEntry.COLUMN_DATE, createdDate.getTime().getTime());
        contentValues.put(FillUpEntry.COLUMN_INFO, info.toString().trim());

        if (isFull) {
            contentValues.put(FillUpEntry.COLUMN_FUEL_CONSUMPTION,
                    FillUpService.getConsumptionFromVolumeDistance(createdFuelVol, createdDistance,
                            mVehicle.getVolumeUnit()).doubleValue());
        }

        if (priceMode == SwitchPrice.perVolume) {
            contentValues.put(FillUpEntry.COLUMN_FUEL_PRICE_PER_LITRE, createdPrice.doubleValue());
            contentValues.put(FillUpEntry.COLUMN_FUEL_PRICE_TOTAL, VolumeUtil.getTotalPriceFromPerLitre(
                    createdFuelVol, createdPrice, mVehicle.getVolumeUnit()).doubleValue());
        } else {
            contentValues.put(FillUpEntry.COLUMN_FUEL_PRICE_TOTAL, createdPrice.doubleValue());
            contentValues.put(FillUpEntry.COLUMN_FUEL_PRICE_PER_LITRE, VolumeUtil.getPerLitrePriceFromTotal(
                    createdFuelVol, createdPrice, mVehicle.getVolumeUnit()).doubleValue());
        }

        if (getContentResolver().insert(FillUpEntry.CONTENT_URI, contentValues) == null) {
            Toast.makeText(this, R.string.add_fillup_fail, Toast.LENGTH_LONG).show();
            setResult(RESULT_CANCELED);
        } else {
            Toast.makeText(this, R.string.add_fillup_success, Toast.LENGTH_LONG).show();
            setResult(RESULT_OK);
        }

        finish();
    }

    /**
     * OnClick listener for date text view
     */

    public void onClickDatePicker(View view) {
        new DatePickerDialog(AddFillUpActivity.this, new DatePickerDialog.OnDateSetListener() {
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
        mTxtDate.setText(DateUtil.getDateFormat(getApplicationContext()).format(calendar.getTime()));
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

    enum SwitchPrice {
        total,
        perVolume
    }

}
