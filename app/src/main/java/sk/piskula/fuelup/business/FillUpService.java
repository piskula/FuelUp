package sk.piskula.fuelup.business;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.j256.ormlite.dao.Dao;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import sk.piskula.fuelup.data.DatabaseProvider;
import sk.piskula.fuelup.entity.FillUp;

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
            Log.i(TAG, "Successfully persisted new v: " + fillUp);
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
            Log.i(TAG, "Successfully updated Expense: " + fillUp);
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

        //find first full older deleted fill up
        for (int i = indexOfNewFillUp + 1; i < allFillUps.size(); i++) {
            FillUp lookingAtFillUp = allFillUps.get(i);
            if (lookingAtFillUp.isFullFillUp()) {
                break;
            }
            fuelVol = fuelVol.add(lookingAtFillUp.getFuelVolume());
            distance += lookingAtFillUp.getDistanceFromLastFillUp();
            fillUpsToUpdate.add(lookingAtFillUp);
        }

        BigDecimal avgConsumption = isNewerFullFillUp ? fuelVol.multiply(new BigDecimal(100)).divide(new BigDecimal(distance), 2, RoundingMode.HALF_UP) : null;

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
            Log.i(TAG, "Successfully found expenses of vehicle id " + vehicleId);
        } catch (SQLException e) {
            Log.e(TAG, "Unexpected error. See logs for details.", e);
        }
        return fillUps;
    }

    private void recomputeConsumption(FillUp fillUp) {
        List<FillUp> allFillUps = findFillUpsOfVehicle(fillUp.getVehicle().getId());
        int indexOfNewFillUp = allFillUps.indexOf(fillUp);

        //we can not compute consumption from first fill up
        if (allFillUps.size() - 1 == indexOfNewFillUp) {
            return;
        }

        if (fillUp.isFullFillUp()) {
            //find first (excl. current) full before current fill up and update consumption
            List<FillUp> olderFillUpsToUpdate = new ArrayList<>();
            olderFillUpsToUpdate.add(fillUp);
            BigDecimal fuelVol = fillUp.getFuelVolume();
            Long distance = fillUp.getDistanceFromLastFillUp();
            for (int i = indexOfNewFillUp + 1; i < allFillUps.size(); i++) {
                FillUp lookingAtFillUp = allFillUps.get(i);
                if (lookingAtFillUp.isFullFillUp()) {
                    break;
                }
                fuelVol = fuelVol.add(lookingAtFillUp.getFuelVolume());
                distance += lookingAtFillUp.getDistanceFromLastFillUp();
                olderFillUpsToUpdate.add(lookingAtFillUp);
            }

            BigDecimal avgConsumption = fuelVol.multiply(new BigDecimal(100)).divide(new BigDecimal(distance), 2, RoundingMode.HALF_UP);

            for (FillUp fUp : olderFillUpsToUpdate) {
                fUp.setFuelConsumption(avgConsumption);
                update(fUp);
            }

            // if this is not most recent fillup, find closest full newer and compute consumption
            if (indexOfNewFillUp != 0) {
                List<FillUp> newerFillUpsToUpdate = new ArrayList<>();
                BigDecimal fuelVol1 = BigDecimal.ZERO;
                Long distance1 = 0l;
                for (int i = indexOfNewFillUp - 1; i >= 0; i--) {
                    FillUp lookingAtFillUp = allFillUps.get(i);
                    fuelVol1 = fuelVol1.add(lookingAtFillUp.getFuelVolume());
                    distance1 += lookingAtFillUp.getDistanceFromLastFillUp();
                    newerFillUpsToUpdate.add(lookingAtFillUp);
                    if (lookingAtFillUp.isFullFillUp()) {
                        break;
                    }
                }

                BigDecimal avgConsumption1 = fuelVol1.multiply(new BigDecimal(100)).divide(new BigDecimal(distance1), 2, RoundingMode.HALF_UP);

                for (FillUp fUp : newerFillUpsToUpdate) {
                    fUp.setFuelConsumption(avgConsumption1);
                    update(fUp);
                }

            }

        } else if (!fillUp.isFullFillUp()) {

            if (indexOfNewFillUp != 0) {

                // if it is not the most recent one, find two closest full and recompute consumption
                // otherwise we can not compute
                List<FillUp> fillUpsToUpdate = new ArrayList<>();
                fillUpsToUpdate.add(fillUp);
                BigDecimal fuelVol = BigDecimal.ZERO;
                Long distance = 0l;
                for (int i = indexOfNewFillUp + 1; i < allFillUps.size(); i++) {
                    FillUp lookingAtFillUp = allFillUps.get(i);
                    if (lookingAtFillUp.isFullFillUp()) {
                        break;
                    }
                    fuelVol = fuelVol.add(lookingAtFillUp.getFuelVolume());
                    distance += lookingAtFillUp.getDistanceFromLastFillUp();
                    fillUpsToUpdate.add(lookingAtFillUp);
                }
                boolean existsNewerFull = false;
                for (int i = indexOfNewFillUp; i >= 0; i--) {
                    FillUp lookingAtFillUp = allFillUps.get(i);
                    fuelVol = fuelVol.add(lookingAtFillUp.getFuelVolume());
                    distance += lookingAtFillUp.getDistanceFromLastFillUp();
                    fillUpsToUpdate.add(lookingAtFillUp);
                    if (lookingAtFillUp.isFullFillUp()) {
                        existsNewerFull = true;
                        break;
                    }
                }
                BigDecimal avgConsumption1 = existsNewerFull ? fuelVol.multiply(new BigDecimal(100)).divide(new BigDecimal(distance), 2, RoundingMode.HALF_UP) : null;

                for (FillUp fUp : fillUpsToUpdate) {
                    fUp.setFuelConsumption(avgConsumption1);
                    update(fUp);
                }
            } else {
                fillUp.setFuelConsumption(null);
                update(fillUp);
            }
        }
    }
}
