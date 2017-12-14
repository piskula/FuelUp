package sk.momosi.fuelup.screens.edit;

import android.content.ContentUris;
import android.content.ContentValues;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Currency;

import sk.momosi.fuelup.R;
import sk.momosi.fuelup.business.FillUpService;
import sk.momosi.fuelup.business.StatisticsService;
import sk.momosi.fuelup.data.FuelUpContract.FillUpEntry;
import sk.momosi.fuelup.data.provider.VehicleProvider;
import sk.momosi.fuelup.entity.FillUp;
import sk.momosi.fuelup.entity.util.CurrencyUtil;
import sk.momosi.fuelup.entity.util.DateUtil;
import sk.momosi.fuelup.entity.util.VolumeUtil;
import sk.momosi.fuelup.screens.detailfragments.FillUpsListFragment;
import sk.momosi.fuelup.screens.dialog.DeleteDialog;
import sk.momosi.fuelup.util.BigDecimalFormatter;


public class EditFillUpActivity extends FillUpAbstractActivity implements DeleteDialog.Callback {

    private static final String TAG = EditFillUpActivity.class.getSimpleName();

    private FillUp mSelectedFillUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        long fillUpId = getIntent().getLongExtra(FillUpsListFragment.FILLUP_ID_TO_EDIT, 0);
        mSelectedFillUp = FillUpService.getFillUpById(fillUpId, getApplicationContext());
        mVehicle = mSelectedFillUp.getVehicle();
        overalDistance = null;

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
            Currency currency = mVehicle.getCurrency();
            DecimalFormat formatter = BigDecimalFormatter.getCommonFormat();

            formatter.setMaximumFractionDigits(CurrencyUtil.getPerLitreFractionDigits(currency) + 1);
            formatter.setMinimumFractionDigits(CurrencyUtil.getPerLitreFractionDigits(currency));

            mTxtPrice.setText(formatter.format(mSelectedFillUp.getFuelPricePerLitre().multiply(CurrencyUtil.getCoefficientPerLitreMultiply(mSelectedFillUp.getVehicle().getCurrency()))));
        } else {
            mTxtPrice.setText(BigDecimalFormatter.getCommonFormat().format(mSelectedFillUp.getFuelPriceTotal()));
        }
        mCheckBoxIsFullFill.setChecked(mSelectedFillUp.isFullFillUp());

        mTxtInfo.setText(mSelectedFillUp.getInfo());

        setFillUpDate(DateUtil.transformToCal(mSelectedFillUp.getDate()));

        mBtnAdd.setText(R.string.update);

        mTxtDistanceUnit.setText(mVehicle.getDistanceUnit().toString());

        distanceMode = SwitchDistance.fromLast;
        isWholeDistanceTyped.setVisibility(View.GONE);
    }

    protected void initViews() {
        super.initViews();

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
        boolean isFull = mCheckBoxIsFullFill.isChecked();

        if (TextUtils.isEmpty(distance) || TextUtils.isEmpty(fuelVol) || TextUtils.isEmpty(price) && mVehicle != null) {
            Snackbar.make(findViewById(android.R.id.content), R.string.toast_emptyFields, Snackbar.LENGTH_LONG).show();
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

        if (!DateUtil.areTwoDatesEquals(createdDate, mSelectedFillUp.getDate())) {
            contentValues.put(FillUpEntry.COLUMN_DATE, createdDate.getTime().getTime());
        }

        if (isFull != mSelectedFillUp.isFullFillUp())
            contentValues.put(FillUpEntry.COLUMN_IS_FULL_FILLUP, isFull ? 1 : 0);

        if (!createdDistance.equals(mSelectedFillUp.getDistanceFromLastFillUp()))
            contentValues.put(FillUpEntry.COLUMN_DISTANCE_FROM_LAST, createdDistance);

        boolean isPriceNeeded = false;
        if (createdFuelVol.doubleValue() != mSelectedFillUp.getFuelVolume().doubleValue()) {
            contentValues.put(FillUpEntry.COLUMN_FUEL_VOLUME, createdFuelVol.doubleValue());
            isPriceNeeded = true;
        }

        if (!info.toString().trim().equals(mSelectedFillUp.getInfo()))
            contentValues.put(FillUpEntry.COLUMN_INFO, info.toString().trim());

        if (priceMode == SwitchPrice.perVolume) {
            BigDecimal subcurrencyCoefficient = CurrencyUtil.getCoefficientPerLitreMultiply(mSelectedFillUp.getVehicle().getCurrency());
            // if price has changed or fuel volume has changed
            if (!createdPrice.equals(mSelectedFillUp.getFuelPricePerLitre().multiply(subcurrencyCoefficient)) || isPriceNeeded) {
                BigDecimal perLitre = createdPrice.divide(subcurrencyCoefficient, 7, RoundingMode.HALF_UP);
                BigDecimal total = VolumeUtil.getTotalPriceFromPerLitre(createdFuelVol, perLitre, mVehicle.getVolumeUnit());

                contentValues.put(FillUpEntry.COLUMN_FUEL_PRICE_PER_LITRE, perLitre.doubleValue());
                contentValues.put(FillUpEntry.COLUMN_FUEL_PRICE_TOTAL, total.doubleValue());
            }

        } else {
            // if price has changed or fuel volume has changed
            if (!createdPrice.equals(mSelectedFillUp.getFuelPriceTotal()) || isPriceNeeded) {
                BigDecimal perLitre = VolumeUtil.getPerLitrePriceFromTotal(createdFuelVol, createdPrice, mVehicle.getVolumeUnit());

                contentValues.put(FillUpEntry.COLUMN_FUEL_PRICE_TOTAL, createdPrice.doubleValue());
                contentValues.put(FillUpEntry.COLUMN_FUEL_PRICE_PER_LITRE, perLitre.doubleValue());
            }
        }

        int result = getContentResolver().update(ContentUris.withAppendedId(FillUpEntry.CONTENT_URI, mSelectedFillUp.getId()), contentValues, null, null);
        if (result == 1) {
            Toast.makeText(getApplicationContext(), R.string.add_fillup_success_update, Toast.LENGTH_LONG).show();
            setResult(RESULT_OK);
        } else if (result == VehicleProvider.UPDATE_NO_CHANGE) {
            Toast.makeText(getApplicationContext(), R.string.add_fillup_success_update_noChange, Toast.LENGTH_LONG).show();
            setResult(RESULT_CANCELED);
        } else {
            Toast.makeText(getApplicationContext(), R.string.add_fillup_fail_update, Toast.LENGTH_LONG).show();
            setResult(RESULT_CANCELED);
        }

        finish();
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

        final int result = getContentResolver().delete(
                ContentUris.withAppendedId(FillUpEntry.CONTENT_URI, mSelectedFillUp.getId()), null, null);

        if (result != -1) {
            Toast.makeText(getApplicationContext(), getString(R.string.remove_fillup_success), Toast.LENGTH_LONG).show();
            setResult(RESULT_OK);
        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.remove_fillup_fail), Toast.LENGTH_LONG).show();
            setResult(RESULT_CANCELED);
        }

        dialog.dismiss();
        finish();
    }

    @Override
    public void onDeleteDialogNegativeClick(DeleteDialog dialog) {
        dialog.dismiss();
    }


}
