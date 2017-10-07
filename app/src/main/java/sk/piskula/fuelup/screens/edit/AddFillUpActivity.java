package sk.piskula.fuelup.screens.edit;

import android.content.ContentValues;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Toast;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Calendar;

import sk.piskula.fuelup.R;
import sk.piskula.fuelup.business.FillUpService;
import sk.piskula.fuelup.data.FuelUpContract.FillUpEntry;
import sk.piskula.fuelup.entity.util.DateUtil;
import sk.piskula.fuelup.entity.util.VolumeUtil;
import sk.piskula.fuelup.screens.detailfragments.FillUpsListFragment;


public class AddFillUpActivity extends FillUpAbstractActivity implements CompoundButton.OnCheckedChangeListener {

    public static final String TAG = AddFillUpActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mVehicle = getIntent()
                .getParcelableExtra(FillUpsListFragment.VEHICLE_FROM_FRAGMENT_TO_EDIT_FILLUP);

        initViews();
    }

    @Override
    protected void onStart() {
        super.onStart();

        setFillUpDate(Calendar.getInstance());
        mTxtFuelPriceUnit.setText(mVehicle.getCurrencySymbol());
        mTxtDistanceUnit.setText(mVehicle.getDistanceUnit().toString());
        mTxtFuelVolumeUnit.setText(mVehicle.getVolumeUnit().toString());
        this.onCheckedChanged(mBtnSwitchPrice, false);
    }

    protected void initViews() {
        super.initViews();

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

}
