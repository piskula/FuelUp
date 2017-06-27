package sk.piskula.fuelup.screens.edit;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
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
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Calendar;

import sk.piskula.fuelup.R;
import sk.piskula.fuelup.business.FillUpService;
import sk.piskula.fuelup.business.ServiceResult;
import sk.piskula.fuelup.entity.FillUp;
import sk.piskula.fuelup.entity.Vehicle;


public class EditFillUp extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {

    public static final String TAG = EditFillUp.class.getSimpleName();

    public static final String EXTRA_FILLUP = "extra_fillup_to_update";

    private EditText mTxtDistance;
    private EditText mTxtFuelVolume;
    private EditText mTxtPrice;
    private EditText mTxtInfo;
    private TextView mTxtDate;

    private TextInputLayout mTxtInputDistance;
    private TextInputLayout mTxtInputPrice;

    private TextView mTxtDistanceUnit;
    private TextView mTxtCurrencySymbol;

    private Button mBtnAdd;
    private ToggleButton mBtnSwitchDistance;
    private ToggleButton mBtnSwitchPrice;
    private CheckBox mCheckBoxIsFullFill;

    private Vehicle mSelectedCar;
    private FillUp mSelectedFillUp;
    private SwitchDistance distanceMode = SwitchDistance.fromLast;
    private SwitchPrice priceMode = SwitchPrice.perLitre;

    private Calendar fillUpDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_fillup);
        initViews();

        Intent intent = getIntent();

        mSelectedFillUp = intent.getParcelableExtra(EXTRA_FILLUP);
        mSelectedCar = mSelectedFillUp.getVehicle();
    }

    @Override
    protected void onStart() {
        super.onStart();

        mTxtDistance.setText(mSelectedFillUp.getDistanceFromLastFillUp().toString());
        mTxtFuelVolume.setText(NumberFormat.getNumberInstance().format(mSelectedFillUp.getFuelVolume()));
        if (priceMode == SwitchPrice.perLitre) {
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

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(mSelectedFillUp.getDate().getTime());
        setFillUpDate(cal);

        mBtnAdd.setText(R.string.add_fillup_update);
        mBtnSwitchDistance.setEnabled(false);

        mTxtCurrencySymbol.setText(mSelectedCar.getCurrencySymbol());
        mTxtDistanceUnit.setText(mSelectedCar.getUnit().toString());
    }

    private void initViews() {
        mTxtDistance = (EditText) findViewById(R.id.txt_addfillup_distance_from_last_fillup_adding);
        mTxtDistanceUnit = (TextView) findViewById(R.id.txt_addfillup_distance_unit);
        mTxtInputDistance = (TextInputLayout) findViewById(R.id.txt_input_addfillup_distance_from_last_fillup_adding);
        mTxtFuelVolume = (EditText) findViewById(R.id.txt_addfillup_fuel_volume);
        mTxtPrice = (EditText) findViewById(R.id.txt_addfillup_price);
        mTxtCurrencySymbol = (TextView) findViewById(R.id.txt_addfillup_currency);
        mTxtInputPrice = (TextInputLayout) findViewById(R.id.txt_input_addfillup_price);
        mTxtDate = (TextView) findViewById(R.id.txt_addfillup_date);
        mTxtInfo = (EditText) findViewById(R.id.txt_addfillup_information);
        mCheckBoxIsFullFill = (CheckBox) findViewById(R.id.checkBox_fullFillUp);
        mBtnAdd = (Button) findViewById(R.id.btn_add_fillup);
        mBtnSwitchDistance = (ToggleButton) findViewById(R.id.btn_switch_distance);
        mBtnSwitchPrice = (ToggleButton) findViewById(R.id.btn_switch_price);

        mBtnSwitchPrice.setOnCheckedChangeListener(this);
        mBtnSwitchDistance.setOnCheckedChangeListener(this);

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
        String date = mTxtDate.getText().toString();

        if (TextUtils.isEmpty(distance) || TextUtils.isEmpty(fuelVol) || TextUtils.isEmpty(date) || TextUtils.isEmpty(price) && mSelectedCar != null) {
            Toast.makeText(this, R.string.toast_emptyFields, Toast.LENGTH_LONG).show();
            return;
        }

        DecimalFormat decimalFormat = new DecimalFormat();
        decimalFormat.setParseBigDecimal(true);

        Long createdDistance = Long.parseLong(distance.toString());

        Calendar createdDate = Calendar.getInstance();
        DateFormat dateFormatter = android.text.format.DateFormat.getDateFormat(getApplicationContext());
        BigDecimal createdFuelVol = null;
        BigDecimal createdPrice = null;
        try {
            createdFuelVol = (BigDecimal) decimalFormat.parse(fuelVol.toString());
            createdPrice = (BigDecimal) decimalFormat.parse(price.toString());
            createdDate.setTime(dateFormatter.parse(date));
        } catch (ParseException ex) {
            Log.d(TAG, "tried bad date");
            throw new RuntimeException(ex);
        }

        FillUpService fillUpService = new FillUpService(getApplicationContext());

        mSelectedFillUp.setFullFillUp(mCheckBoxIsFullFill.isChecked());
        mSelectedFillUp.setDistanceFromLastFillUp(createdDistance);

        mSelectedFillUp.setFuelVolume(createdFuelVol);
        if (priceMode == SwitchPrice.perLitre) {
            mSelectedFillUp.setFuelPricePerLitre(createdPrice);
            mSelectedFillUp.setFuelPriceTotal(createdFuelVol.multiply(createdPrice));
        } else {
            mSelectedFillUp.setFuelPriceTotal(createdPrice);
            mSelectedFillUp.setFuelPricePerLitre(createdPrice.divide(createdFuelVol,2, BigDecimal.ROUND_HALF_UP));
        }
        mSelectedFillUp.setDate(createdDate.getTime());
        mSelectedFillUp.setInfo(info.toString());

        ServiceResult serviceResultUpdate = fillUpService.update(mSelectedFillUp);
        if (ServiceResult.SUCCESS.equals(serviceResultUpdate)) {
            Toast.makeText(this, R.string.add_fillup_success_update, Toast.LENGTH_LONG).show();
            setResult(RESULT_OK);
        } else {
            Toast.makeText(this, R.string.add_fillup_fail_update, Toast.LENGTH_LONG).show();
            setResult(RESULT_CANCELED);
        }

        finish();
    }

    /**
     * OnClick listener for date text view
     */
    public void onClickDatePicker(View view) {
        new DatePickerDialog(EditFillUp.this, new DatePickerDialog.OnDateSetListener() {
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
        mTxtDate.setText(android.text.format.DateFormat.getDateFormat(getApplicationContext()).format(calendar.getTime()));
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
        if (compoundButton.getId() == mBtnSwitchDistance.getId()) {
            if (isChecked) {
                distanceMode = SwitchDistance.whole;
                mTxtInputDistance.setHint(getString(R.string.add_fillup_distance_overall));
            } else {
                distanceMode = SwitchDistance.fromLast;
                mTxtInputDistance.setHint(getString(R.string.add_fillup_distance_from_last));
            }
        }
        if (compoundButton.getId() == mBtnSwitchPrice.getId()) {
            if (isChecked) {
                priceMode = SwitchPrice.total;
                mTxtInputPrice.setHint(getString(R.string.add_fillup_priceTotal));
            } else {
                priceMode = SwitchPrice.perLitre;
                mTxtInputPrice.setHint(getString(R.string.add_fillup_pricePerLitre));
            }
        }
    }

    enum SwitchPrice {
        total,
        perLitre
    }

    public enum SwitchDistance {
        fromLast,
        whole
    }
}