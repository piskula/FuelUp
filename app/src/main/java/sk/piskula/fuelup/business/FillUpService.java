package sk.piskula.fuelup.business;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import sk.piskula.fuelup.data.FuelUpContract;
import sk.piskula.fuelup.data.FuelUpContract.FillUpEntry;
import sk.piskula.fuelup.entity.FillUp;
import sk.piskula.fuelup.entity.Vehicle;
import sk.piskula.fuelup.entity.enums.VolumeUnit;

/**
 * @author Martin Styk, Ondrej Oravcok
 */
public class FillUpService {

    private static final String LOG_TAG = FillUpService.class.getSimpleName();

    public static List<FillUp> findFillUpsOfVehicle(long vehicleId, Context context) {

        String[] selectionArgs = {String.valueOf(vehicleId)};
        Cursor cursor = context.getContentResolver()
                .query(FillUpEntry.CONTENT_URI,
                        FuelUpContract.ALL_COLUMNS_FILLUPS,
                        FillUpEntry.COLUMN_VEHICLE + "=?",
                        selectionArgs,
                        FillUpEntry.COLUMN_DATE + " DESC");

        List<FillUp> fillUps = new ArrayList<>(cursor.getCount());

        while (cursor.moveToNext()) {
            FillUp fillUp = cursorToFillup(cursor, context);
            fillUps.add(fillUp);
        }

        cursor.close();

        return fillUps;
    }

    public List<FillUp> findFillUpsOfVehicleWithComputedConsumption(long vehicleId, Context context) {

        String[] selectionArgs = {String.valueOf(vehicleId)};
        Cursor cursor = context.getContentResolver()
                .query(FillUpEntry.CONTENT_URI,
                        FuelUpContract.ALL_COLUMNS_FILLUPS,
                        FillUpEntry.COLUMN_VEHICLE + "=? AND " + FillUpEntry.COLUMN_FUEL_CONSUMPTION + " IS NOT NULL",
                        selectionArgs,
                        FillUpEntry.COLUMN_DATE + " DESC");

        List<FillUp> fillUps = new ArrayList<>(cursor.getCount());

        while (cursor.moveToNext()) {
            FillUp fillUp = cursorToFillup(cursor, context);
            fillUps.add(fillUp);
        }

        cursor.close();

        return fillUps;
    }

    public static BigDecimal getConsumptionFromVolumeDistance(BigDecimal volume, Long distance, VolumeUnit unit) {
        if (unit == VolumeUnit.LITRE) {
            return volume.multiply(new BigDecimal(100)).divide(new BigDecimal(distance), 14, RoundingMode.HALF_UP);
        } else {
            return BigDecimal.valueOf(distance).divide(volume, 14, RoundingMode.HALF_UP);
        }
    }

    public static FillUp getFillUpById(long fillUpId, Context context) {
        String[] selectionArgs = {String.valueOf(fillUpId)};
        Cursor cursor = context.getContentResolver().query(FillUpEntry.CONTENT_URI,
                FuelUpContract.ALL_COLUMNS_FILLUPS, FillUpEntry._ID + "=?",
                selectionArgs, null);

        if (cursor == null || cursor.getCount() != 1) {
            Log.e(LOG_TAG, "Cannot get FillUp for id=" + fillUpId);
            return null;
        }

        cursor.moveToFirst();
        FillUp fillUp = cursorToFillup(cursor, context);

        cursor.close();
        return fillUp;
    }

    private static FillUp cursorToFillup(Cursor cursor, Context ctx) {
        long vehicleId = cursor.getLong(cursor.getColumnIndexOrThrow(FillUpEntry.COLUMN_VEHICLE));
        Vehicle vehicle = VehicleService.getVehicleById(vehicleId, ctx);

        FillUp fillUp = new FillUp();
        fillUp.setId(cursor.getLong(cursor.getColumnIndexOrThrow(FillUpEntry._ID)));
        fillUp.setInfo(cursor.getString(cursor.getColumnIndexOrThrow(FillUpEntry.COLUMN_INFO)));
        fillUp.setFuelPriceTotal(BigDecimal.valueOf(cursor.getDouble(cursor.getColumnIndexOrThrow(FillUpEntry.COLUMN_FUEL_PRICE_TOTAL))));
        fillUp.setFuelPricePerLitre(BigDecimal.valueOf(cursor.getDouble(cursor.getColumnIndexOrThrow(FillUpEntry.COLUMN_FUEL_PRICE_PER_LITRE))));
        fillUp.setFuelVolume(BigDecimal.valueOf(cursor.getDouble(cursor.getColumnIndexOrThrow(FillUpEntry.COLUMN_FUEL_VOLUME))));
        fillUp.setDistanceFromLastFillUp(cursor.getLong(cursor.getColumnIndexOrThrow(FillUpEntry.COLUMN_DISTANCE_FROM_LAST)));
        fillUp.setFullFillUp(cursor.getInt(cursor.getColumnIndexOrThrow(FillUpEntry.COLUMN_IS_FULL_FILLUP)) == 1);
        fillUp.setDate(new Date(cursor.getLong(cursor.getColumnIndexOrThrow(FillUpEntry.COLUMN_DATE))));
        fillUp.setFuelConsumption(BigDecimal.valueOf(cursor.getDouble(cursor.getColumnIndexOrThrow(FillUpEntry.COLUMN_FUEL_CONSUMPTION))));
        fillUp.setVehicle(vehicle);

        return fillUp;
    }
}

