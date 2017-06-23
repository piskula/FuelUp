package sk.piskula.fuelup.business;

import android.content.Context;
import android.util.Log;

import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;

import sk.piskula.fuelup.data.DatabaseProvider;
import sk.piskula.fuelup.entity.VehicleType;

/**
 * Created by Martin Styk on 23.06.2017.
 */
public class VehicleTypeService {

    private static final String TAG = VehicleTypeService.class.getSimpleName();

    private Context context;
    private Dao<VehicleType, Long> vehicleTypeDao;

    public VehicleTypeService(Context context) {
        this.context = context;
        this.vehicleTypeDao = DatabaseProvider.get(context).getVehicleTypeDao();
    }

    public VehicleType getFirst() {
        try {
            return vehicleTypeDao.queryBuilder().queryForFirst();
        } catch (SQLException e) {
            Log.e(TAG, e.getLocalizedMessage(), e);
        }
        return null;
    }
}
