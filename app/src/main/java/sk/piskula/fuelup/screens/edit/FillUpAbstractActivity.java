package sk.piskula.fuelup.screens.edit;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.Calendar;

import sk.piskula.fuelup.R;
import sk.piskula.fuelup.entity.Vehicle;
import sk.piskula.fuelup.entity.util.DateUtil;

/**
 * @author Martin Styk
 * @version 07.10.2017.
 */
public abstract class FillUpAbstractActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {

    protected EditText mTxtDistance;
    protected EditText mTxtFuelVolume;
    protected TextView mTxtFuelVolumeUnit;
    protected EditText mTxtPrice;
    protected EditText mTxtInfo;
    protected TextView mTxtDate;

    protected TextInputLayout mTxtInputPrice;

    protected TextView mTxtDistanceUnit;
    protected TextView mTxtFuelPriceUnit;

    protected ToggleButton mBtnSwitchPrice;
    protected CheckBox mCheckBoxIsFullFill;

    protected Button mBtnAdd;

    protected ActionBar actionBar;

    protected SwitchPrice priceMode = SwitchPrice.perVolume;

    protected Calendar fillUpDate;

    protected Vehicle mVehicle;


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
        mTxtDate = findViewById(R.id.txt_addfillup_date);
        mTxtInfo = findViewById(R.id.txt_addfillup_information);
        mCheckBoxIsFullFill = findViewById(R.id.checkBox_fullFillUp);
        mBtnAdd = findViewById(R.id.btn_add_fillup);
        mBtnSwitchPrice = findViewById(R.id.btn_switch_price);

        mBtnSwitchPrice.setOnCheckedChangeListener(this);

        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
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
                priceMode = AddFillUpActivity.SwitchPrice.total;
                mTxtInputPrice.setHint(getString(R.string.add_fillup_priceTotal));
                mTxtFuelPriceUnit.setText(mVehicle.getCurrencySymbol());
            } else {
                priceMode = AddFillUpActivity.SwitchPrice.perVolume;
                mTxtInputPrice.setHint(getString(R.string.add_fillup_pricePerLitre));
                mTxtFuelPriceUnit.setText(getString(R.string.unit_pricePerLitre, mVehicle.getPerLitreSubcurrencySymbol()));
            }
        }
    }

    protected enum SwitchPrice {
        total,
        perVolume
    }
}
