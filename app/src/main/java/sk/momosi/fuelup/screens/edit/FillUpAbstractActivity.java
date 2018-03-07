package sk.momosi.fuelup.screens.edit;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.Calendar;

import sk.momosi.fuelup.R;
import sk.momosi.fuelup.business.googledrive.syncing.SyncAdapterContentObserver;
import sk.momosi.fuelup.data.FuelUpContract;
import sk.momosi.fuelup.entity.Vehicle;
import sk.momosi.fuelup.entity.util.DateUtil;
import sk.momosi.fuelup.util.NonZeroTextWatcher;
import sk.momosi.fuelup.util.PreferencesUtils;

/**
 * @author Martin Styk
 * @version 07.10.2017.
 */
public abstract class FillUpAbstractActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {

    private static final String LOG_TAG = FillUpAbstractActivity.class.getSimpleName();

    protected EditText mTxtDistance;
    protected EditText mTxtFuelVolume;
    protected TextView mTxtFuelVolumeUnit;
    protected EditText mTxtPrice;
    protected EditText mTxtInfo;
    protected TextView mTxtDate;

    protected TextInputLayout mTxtInputPrice;
    protected TextInputLayout mTxtInputDistance;

    protected TextView mTxtDistanceUnit;
    protected TextView mTxtFuelPriceUnit;

    protected Switch mBtnSwitchPrice;
    protected CheckBox mCheckBoxIsFullFill;
    protected Switch isWholeDistanceTyped;

    protected Button mBtnAdd;
    protected ActionBar actionBar;

    protected SwitchPrice priceMode = SwitchPrice.perVolume;
    protected SwitchDistance distanceMode = SwitchDistance.overall;

    private Calendar fillUpDate;

    protected Vehicle mVehicle;
    protected Long overalDistance;

    private SyncAdapterContentObserver mObserver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fillup_edit);
    }


    protected void initViews() {
        mTxtDistance = findViewById(R.id.txt_addfillup_distance_from_last_fillup_adding);
        mTxtDistanceUnit = findViewById(R.id.txt_addfillup_distance_unit);
        mTxtFuelVolume = findViewById(R.id.txt_addfillup_fuel_volume);
        mTxtFuelVolumeUnit = findViewById(R.id.txt_addfillup_volumeUnit);
        mTxtPrice = findViewById(R.id.txt_addfillup_price);
        mTxtFuelPriceUnit = findViewById(R.id.txt_addfillup_fuel_price_unit);
        mTxtInputPrice = findViewById(R.id.txt_input_addfillup_price);
        mTxtInputDistance = findViewById(R.id.txt_input_addfillup_distance_from_last_fillup_adding);
        mTxtDate = findViewById(R.id.txt_addfillup_date);
        mTxtInfo = findViewById(R.id.txt_addfillup_information);
        mCheckBoxIsFullFill = findViewById(R.id.checkBox_fullFillUp);
        mBtnAdd = findViewById(R.id.btn_add_fillup);
        mBtnSwitchPrice = findViewById(R.id.btn_switch_price);
        isWholeDistanceTyped = findViewById(R.id.switch_overal_fromLast);

        mBtnSwitchPrice.setOnCheckedChangeListener(this);
        isWholeDistanceTyped.setChecked(overalDistance != null);
        isWholeDistanceTyped.setOnCheckedChangeListener(this);

        actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);

        mTxtFuelVolume.addTextChangedListener(new NonZeroTextWatcher(mTxtFuelVolume));
        mTxtDistance.addTextChangedListener(new NonZeroTextWatcher(mTxtDistance));
        mTxtDistance.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
            @Override
            public void afterTextChanged(Editable editable) {
                invalidateOptionsMenu();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_fillup, menu);

        MenuItem overalDistanceMenuItem = menu.findItem(R.id.txt_vehicle_mileage);

        if (this.overalDistance != null) {
            String distanceTypedString = mTxtDistance.getText().toString();
            Long distanceTyped = distanceTypedString.isEmpty() ? 0 : Long.valueOf(distanceTypedString);

            if (distanceMode == SwitchDistance.fromLast) {
                Long distance = distanceTyped + this.overalDistance;
                overalDistanceMenuItem.setTitle(getString(R.string.add_fillup_actualStatus)
                        + " " + distance.toString() + mVehicle.getDistanceUnit().toString());
                overalDistanceMenuItem.setVisible(true);

            } else {
                Long distance = distanceTyped - this.overalDistance;
                if (distance > 0) {
                    overalDistanceMenuItem.setTitle(getString(R.string.add_fillup_madeStatus)
                            + " " + distance.toString() + mVehicle.getDistanceUnit().toString());
                    overalDistanceMenuItem.setVisible(true);
                } else {
                    overalDistanceMenuItem.setVisible(false);
                }
            }
        } else {
            overalDistanceMenuItem.setVisible(false);
        }

        return super.onCreateOptionsMenu(menu);
    }

    /**
     * OnClick listener for date text view
     */
    public void onClickDatePicker(View view) {
        new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int y, int m, int d) {
                Calendar serviceDate = Calendar.getInstance();
                serviceDate.set(y, m, d);
                setFillUpDate(serviceDate);
            }
        }, fillUpDate.get(Calendar.YEAR), fillUpDate.get(Calendar.MONTH), fillUpDate.get(Calendar.DAY_OF_MONTH)).show();
    }

    protected void setFillUpDate(Calendar calendar) {
        this.fillUpDate = calendar;
        mTxtDate.setText(DateUtil.getDateFormat(getApplicationContext()).format(calendar.getTime()));
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
        if (compoundButton.getId() == mBtnSwitchPrice.getId()) {
            if (isChecked) {
                priceMode = SwitchPrice.total;
                mTxtInputPrice.setHint(getString(R.string.add_fillup_priceTotal));
                mTxtFuelPriceUnit.setText(mVehicle.getCurrencySymbol());
            } else {
                priceMode = SwitchPrice.perVolume;
                mTxtInputPrice.setHint(getString(R.string.add_fillup_pricePerLitre));
                mTxtFuelPriceUnit.setText(getString(R.string.unit_pricePerLitre, mVehicle.getPerLitreSubcurrencySymbol()));
            }
        } else if (compoundButton.getId() == isWholeDistanceTyped.getId()) {
            if (isChecked) {
                distanceMode = SwitchDistance.overall;
                mTxtInputDistance.setHint(getString(R.string.add_fillup_actual_mileage));
            } else {
                distanceMode = SwitchDistance.fromLast;
                mTxtInputDistance.setHint(getString(R.string.add_fillup_distanceFromLast));
            }
            invalidateOptionsMenu();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        boolean isSyncEnabled = PreferencesUtils.getAccountName(this) != null;
        if (mObserver == null && isSyncEnabled)
            mObserver = new SyncAdapterContentObserver(new Handler(), getApplicationContext());
        if (isSyncEnabled)
            getContentResolver().registerContentObserver(
                    FuelUpContract.FillUpEntry.CONTENT_URI, true, mObserver);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mObserver != null) {
            getContentResolver().unregisterContentObserver(mObserver);
        }
    }

    protected enum SwitchPrice {
        total,
        perVolume
    }

    protected enum SwitchDistance {
        fromLast,
        overall
    }
}
