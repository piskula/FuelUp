package sk.piskula.fuelup.business;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
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
import sk.piskula.fuelup.entity.enums.DistanceUnit;
import sk.piskula.fuelup.entity.enums.VolumeUnit;

/**
 * Created by Martin Styk on 23.06.2017.
 */

public class FillUpService {

    private static final String LOG_TAG = FillUpService.class.getSimpleName();


    /*public ServiceResult deleteWithConsumptionCalculation(FillUp fillUp) {

        List<FillUp> allFillUps = findFillUpsOfVehicle(fillUp.getVehicle().getId());
        int indexOfNewFillUp = allFillUps.indexOf(fillUp);

        ServiceResult serviceResult = delete(fillUp);
        if (serviceResult != ServiceResult.SUCCESS) {
            return serviceResult;
        }

        List<FillUp> fillUpsToUpdate = new ArrayList<>();
        BigDecimal fuelVol = BigDecimal.ZERO;
        Long distance = 0l;
        boolean isNewerFullFillUp = false;

        // find first full newer than deleted
        for (int i = indexOfNewFillUp - 1; i >= 0; i--) {
            FillUp lookingAtFillUp = allFillUps.get(i);
            fuelVol = fuelVol.add(lookingAtFillUp.getFuelVolume());
            distance += lookingAtFillUp.getDistanceFromLastFillUp();
            fillUpsToUpdate.add(lookingAtFillUp);
            if (lookingAtFillUp.isFullFillUp()) {
                isNewerFullFillUp = true;
                break;
            }
        }

        boolean isOlderFullFillUp = false;

        //find first full older deleted fill up
        for (int i = indexOfNewFillUp + 1; i < allFillUps.size(); i++) {
            FillUp lookingAtFillUp = allFillUps.get(i);
            if (lookingAtFillUp.isFullFillUp()) {
                isOlderFullFillUp = true;
                break;
            }
            fuelVol = fuelVol.add(lookingAtFillUp.getFuelVolume());
            distance += lookingAtFillUp.getDistanceFromLastFillUp();
            fillUpsToUpdate.add(lookingAtFillUp);
        }

        BigDecimal avgConsumption = isNewerFullFillUp & isOlderFullFillUp ? fuelVol.multiply(new BigDecimal(100)).divide(new BigDecimal(distance), 2, RoundingMode.HALF_UP) : null;

        for (FillUp fUp : fillUpsToUpdate) {
            fUp.setFuelConsumption(avgConsumption);
            update(fUp);
        }

        return serviceResult;
    }*/

    public List<FillUp> findFillUpsOfVehicle(long vehicleId) {
        List<FillUp> fillUps = new ArrayList<>();
//        try {
//            fillUps = fillUpDao.queryBuilder().orderBy("date", false).orderBy("id", false).where().eq("vehicle_id", vehicleId).query();
//            Log.i(TAG, "Successfully found fillups of vehicle id " + vehicleId);
//        } catch (SQLException e) {
//            Log.e(TAG, "Unexpected error. See logs for details.", e);
//        }
        return fillUps;
    }

    public List<FillUp> findFillUpsOfVehicleWithComputedConsumption(long vehicleId) {
        List<FillUp> fillUps = new ArrayList<>();
//        try {
//            fillUps = fillUpDao.queryBuilder().orderBy("date", false).orderBy("id", false).where().eq("vehicle_id", vehicleId).and().isNotNull("consumption").query();
//            Log.i(TAG, "Successfully found fillups of vehicle id " + vehicleId);
//        } catch (SQLException e) {
//            Log.e(TAG, "Unexpected error. See logs for details.", e);
//        }
        return fillUps;
    }

    public BigDecimal getAverageConsumptionOfVehicle(long vehicleId) {
//        try {
//            GenericRawResults<String[]> results = fillUpDao.queryRaw("SELECT SUM(distance_from_last_fill_up * consumption) / SUM(distance_from_last_fill_up) FROM fill_ups WHERE consumption is not null and vehicle_id = " + vehicleId);
//            DecimalFormat f = new DecimalFormat();
//            f.setParseBigDecimal(true);
//            return (BigDecimal) f.parseObject(results.getResults().get(0)[0]);
//        } catch (SQLException e1) {
//            Log.e(TAG, "Unexpected error during computing average fuel consumption.", e1);
//        } catch (ParseException e2) {
//            Log.e(TAG, "SQL return not parsable number.", e2);
//        }
        return BigDecimal.ZERO;
    }

    public static BigDecimal getConsumptionFromVolumeDistance(BigDecimal volume, Long distance, VolumeUnit unit) {
        if (unit == VolumeUnit.LITRE) {
            return volume.multiply(new BigDecimal(100)).divide(new BigDecimal(distance), 14, RoundingMode.HALF_UP);
        } else {
            return BigDecimal.valueOf(distance).divide(volume, 14, RoundingMode.HALF_UP);
        }
    }

    public static FillUp getFillUpById(long fillUpId, Context context) {
        String[] selectionArgs = { String.valueOf(fillUpId) };
        Cursor cursor = context.getContentResolver().query(FillUpEntry.CONTENT_URI,
                FuelUpContract.ALL_COLUMNS_FILLUPS, FillUpEntry._ID + "=?",
                selectionArgs, null);

        if (cursor == null || cursor.getCount() != 1) {
            Log.e(LOG_TAG, "Cannot get FillUp for id=" + fillUpId);
            return null;
        }

        cursor.moveToFirst();
        long vehicleId = cursor.getLong(cursor.getColumnIndexOrThrow(FillUpEntry.COLUMN_VEHICLE));
        Vehicle vehicle = VehicleService.getVehicleById(vehicleId, context);

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

        cursor.close();
        return fillUp;
    }

    /*private void recomputeConsumption(FillUp fillUp) {
        List<FillUp> allFillUps = findFillUpsOfVehicle(fillUp.getVehicle().getId());
        int indexOfNewFillUp = allFillUps.indexOf(fillUp);

        if (fillUp.isFullFillUp()) {
            //find first (excl. current) full before current fill up and update consumption
            List<FillUp> olderFillUpsToUpdate = new ArrayList<>();
            olderFillUpsToUpdate.add(fillUp);
            BigDecimal fuelVol = fillUp.getFuelVolume();
            Long distance = fillUp.getDistanceFromLastFillUp();

            boolean existsOlderFullFillUp = false;

            for (int i = indexOfNewFillUp + 1; i < allFillUps.size(); i++) {
                FillUp lookingAtFillUp = allFillUps.get(i);
                if (lookingAtFillUp.isFullFillUp()) {
                    existsOlderFullFillUp = true;
                    break;
                }
                fuelVol = fuelVol.add(lookingAtFillUp.getFuelVolume());
                distance += lookingAtFillUp.getDistanceFromLastFillUp();
                olderFillUpsToUpdate.add(lookingAtFillUp);
            }

            ConsumtpionUnit unit = fillUp.getVehicle().getDistanceUnit() == DistanceUnit.mi ? ConsumtpionUnit.milesPerGallon : ConsumtpionUnit.litresPer100km;
            BigDecimal avgConsumption = existsOlderFullFillUp ? getConsumptionFromVolumeDistance(fuelVol, distance, unit) : null;

            for (FillUp fUp : olderFillUpsToUpdate) {
                fUp.setFuelConsumption(avgConsumption);
                update(fUp);
            }

            // if this is not most recent fillup, find closest full newer and compute consumption

            List<FillUp> newerFillUpsToUpdate = new ArrayList<>();
            BigDecimal fuelVol1 = BigDecimal.ZERO;
            Long distance1 = 0l;

            boolean existsNewerFullFillUp = false;

            for (int i = indexOfNewFillUp - 1; i >= 0; i--) {
                FillUp lookingAtFillUp = allFillUps.get(i);
                fuelVol1 = fuelVol1.add(lookingAtFillUp.getFuelVolume());
                distance1 += lookingAtFillUp.getDistanceFromLastFillUp();
                newerFillUpsToUpdate.add(lookingAtFillUp);
                if (lookingAtFillUp.isFullFillUp()) {
                    existsNewerFullFillUp = true;
                    break;
                }
            }

            BigDecimal avgConsumption1 = existsNewerFullFillUp ? fuelVol1.multiply(new BigDecimal(100)).divide(new BigDecimal(distance1), 2, RoundingMode.HALF_UP) : null;

            for (FillUp fUp : newerFillUpsToUpdate) {
                fUp.setFuelConsumption(avgConsumption1);
                update(fUp);
            }

        } else if (!fillUp.isFullFillUp()) {

            // if it is not the most recent one, find two closest full and recompute consumption
            // otherwise we can not compute
            List<FillUp> fillUpsToUpdate = new ArrayList<>();
            fillUpsToUpdate.add(fillUp);
            BigDecimal fuelVol = BigDecimal.ZERO;
            Long distance = 0l;

            boolean existsOlderFullFillUp = false;

            for (int i = indexOfNewFillUp + 1; i < allFillUps.size(); i++) {
                FillUp lookingAtFillUp = allFillUps.get(i);
                if (lookingAtFillUp.isFullFillUp()) {
                    existsOlderFullFillUp = true;
                    break;
                }
                fuelVol = fuelVol.add(lookingAtFillUp.getFuelVolume());
                distance += lookingAtFillUp.getDistanceFromLastFillUp();
                fillUpsToUpdate.add(lookingAtFillUp);
            }
            boolean existsNewerFullFillUp = false;
            for (int i = indexOfNewFillUp; i >= 0; i--) {
                FillUp lookingAtFillUp = allFillUps.get(i);
                fuelVol = fuelVol.add(lookingAtFillUp.getFuelVolume());
                distance += lookingAtFillUp.getDistanceFromLastFillUp();
                fillUpsToUpdate.add(lookingAtFillUp);
                if (lookingAtFillUp.isFullFillUp()) {
                    existsNewerFullFillUp = true;
                    break;
                }
            }
            BigDecimal avgConsumption1 = existsNewerFullFillUp && existsOlderFullFillUp ? fuelVol.multiply(new BigDecimal(100)).divide(new BigDecimal(distance), 2, RoundingMode.HALF_UP) : null;

            for (FillUp fUp : fillUpsToUpdate) {
                fUp.setFuelConsumption(avgConsumption1);
                update(fUp);
            }
        }
    }*/

    enum ConsumtpionUnit {
        milesPerGallon, litresPer100km
    }
}

