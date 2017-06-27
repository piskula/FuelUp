package sk.piskula.fuelup.business;

import android.content.Context;
import android.util.Log;

import com.j256.ormlite.dao.Dao;

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

    public ServiceResult save(FillUp fillUp) {
        try {
            fillUpDao.create(fillUp);
            Log.i(TAG, "Successfully persisted new v: " + fillUp);
            return ServiceResult.SUCCESS;
        } catch (SQLException e) {
            Log.e(TAG, "Unexpected error. See logs for details.", e);
        }
        return ServiceResult.ERROR;
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

    public List<FillUp> findFillUpsOfVehicle(long vehicleId) {
        List<FillUp> fillUps = new ArrayList<>();
        try {
            fillUps = fillUpDao.queryBuilder().orderBy("date", false).where().eq("vehicle_id", vehicleId).query();
            Log.i(TAG, "Successfully found expenses of vehicle id " + vehicleId);
        } catch (SQLException e) {
            Log.e(TAG, "Unexpected error. See logs for details.", e);
        }
        return fillUps;
    }
}
