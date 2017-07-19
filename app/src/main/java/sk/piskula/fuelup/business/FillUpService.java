package sk.piskula.fuelup.business;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.GenericRawResults;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import sk.piskula.fuelup.data.DatabaseProvider;
import sk.piskula.fuelup.entity.FillUp;
import sk.piskula.fuelup.entity.enums.DistanceUnit;

/**
 * Created by Martin Styk on 23.06.2017.
 */

public class FillUpService {

    private static final String TAG = FillUpService.class.getSimpleName();

    private Context context;
    private Dao<FillUp, Long> fillUpDao;

    public FillUpService(Context context) {
        this.context = context;
        this.fillUpDao = DatabaseProvider.get(context).getFillUpDao();
    }

    public ServiceResult save(@NonNull FillUp fillUp) {
        try {
            fillUpDao.create(fillUp);
            Log.i(TAG, "Successfully persisted new FillUp: " + fillUp);
            return ServiceResult.SUCCESS;
        } catch (SQLException e) {
            Log.e(TAG, "Unexpected error. See logs for details.", e);
        }
        return ServiceResult.ERROR;
    }

    public ServiceResult saveWithConsumptionCalculation(@NonNull FillUp fillUp) {
        // save new fill up
        ServiceResult serviceResult = save(fillUp);
        if (serviceResult != ServiceResult.SUCCESS) {
            return serviceResult;
        }
        recomputeConsumption(fillUp);

        return serviceResult;
    }

    public ServiceResult update(FillUp fillUp) {
        try {
            fillUpDao.update(fillUp);
            Log.i(TAG, "Successfully updated FillUp: " + fillUp);
            return ServiceResult.SUCCESS;
        } catch (SQLException e) {
            Log.e(TAG, "Unexpected error. See logs for details.", e);
        }
        return ServiceResult.ERROR;
    }

    public ServiceResult updateWithConsumptionCalculation(FillUp fillUp) {
        ServiceResult serviceResult = update(fillUp);
        if (serviceResult != ServiceResult.SUCCESS) {
            return serviceResult;
        }
        recomputeConsumption(fillUp);

        return serviceResult;
    }


    public ServiceResult delete(FillUp fillUp) {
        try {
            fillUpDao.delete(fillUp);
            Log.i(TAG, "Successfully deleted FillUp: " + fillUp);
            return ServiceResult.SUCCESS;
        } catch (SQLException e) {
            Log.e(TAG, "Unexpected error. See logs for details.", e);
        }
        return ServiceResult.ERROR;
    }

    public ServiceResult deleteWithConsumptionCalculation(FillUp fillUp) {

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
    }

    public List<FillUp> findFillUpsOfVehicle(long vehicleId) {
        List<FillUp> fillUps = new ArrayList<>();
        try {
            fillUps = fillUpDao.queryBuilder().orderBy("date", false).orderBy("id", false).where().eq("vehicle_id", vehicleId).query();
            Log.i(TAG, "Successfully found fillups of vehicle id " + vehicleId);
        } catch (SQLException e) {
            Log.e(TAG, "Unexpected error. See logs for details.", e);
        }
        return fillUps;
    }

    public BigDecimal getAverageConsumptionOfVehicle(long vehicleId) {
        try {
            GenericRawResults<String[]> results = fillUpDao.queryRaw("SELECT SUM(distance_from_last_fill_up * consumption) / SUM(distance_from_last_fill_up) FROM fill_ups WHERE consumption is not null and vehicle_id = " + vehicleId);
            DecimalFormat f = new DecimalFormat();
            f.setParseBigDecimal(true);
            return (BigDecimal) f.parseObject(results.getResults().get(0)[0]);
        } catch (SQLException e1) {
            Log.e(TAG, "Unexpected error during computing average fuel consumption.", e1);
        } catch (ParseException e2) {
            Log.e(TAG, "SQL return not parsable number.", e2);
        }
        return BigDecimal.ZERO;
    }

    private BigDecimal getConsumptionFromVolumeDistance(BigDecimal volume, Long distance, ConsumtpionUnit unit) {
        if (unit == ConsumtpionUnit.litresPer100km) {
            return volume.multiply(new BigDecimal(100)).divide(new BigDecimal(distance), 2, RoundingMode.HALF_UP);
        } else {
            return BigDecimal.valueOf(distance).divide(volume, 1, RoundingMode.HALF_UP);
        }
    }

    private void recomputeConsumption(FillUp fillUp) {
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
    }

    enum ConsumtpionUnit {
        milesPerGallon, litresPer100km
    }
}

