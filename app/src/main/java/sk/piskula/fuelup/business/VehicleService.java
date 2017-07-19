package sk.piskula.fuelup.business;

import android.content.Context;
import android.util.Log;

import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;
import java.util.Currency;
import java.util.List;

import sk.piskula.fuelup.data.DatabaseProvider;
import sk.piskula.fuelup.entity.Vehicle;
import sk.piskula.fuelup.entity.enums.DistanceUnit;

/**
 * Created by Martin Styk on 23.06.2017.
 */

public class VehicleService {

    private static final String TAG = VehicleService.class.getSimpleName();

    private Context context;
    private Dao<Vehicle, Long> vehicleDao;
    private VehicleTypeService vehicleTypeService;

    public VehicleService(Context context) {
        this.context = context;
        this.vehicleDao = DatabaseProvider.get(context).getVehicleDao();
        vehicleTypeService = new VehicleTypeService(context);
    }

    /**
     * Creates car with default values
     *
     * @param name name of car
     * @return success result
     */
    public ServiceResult save(String name) {
        Vehicle vehicle = new Vehicle();
        vehicle.setName(name);
        vehicle.setDistanceUnit(DistanceUnit.km);
        vehicle.setCurrency(Currency.getInstance("EUR"));
        vehicle.setType(vehicleTypeService.getFirst());

        return save(vehicle);
    }

    /**
     * Creates car with default values
     *
     * @param vehicle
     * @return success result
     */
    public ServiceResult save(Vehicle vehicle) {
        try {
            vehicleDao.create(vehicle);
            Log.i(TAG, "Successfully persisted new Vehicle: " + vehicle);
            return ServiceResult.SUCCESS;
        } catch (SQLException e) {
            if (e.getCause().getCause().getMessage().contains("UNIQUE")) {
                return ServiceResult.ERROR_DUPLICATE;
            } else {
                Log.e(TAG, "Unexpected error. See logs for details.", e);
            }
        }
        return ServiceResult.ERROR;
    }

    public ServiceResult update(Vehicle vehicle) {
        try {
            vehicleDao.update(vehicle);
            Log.i(TAG, "Successfully updated Vehicle: " + vehicle);
            return ServiceResult.SUCCESS;
        } catch (SQLException e) {
            Log.e(TAG, "Unexpected error. See logs for details.", e);
        }
        return ServiceResult.ERROR;
    }

    public Vehicle find(long vehicleId) {
        Vehicle vehicle = null;
        try {
            vehicle = vehicleDao.queryBuilder().where().eq("id", vehicleId).queryForFirst();
        } catch (SQLException e) {
            Log.e(TAG, "Unexpected error. See logs for details.", e);
        }
        return vehicle;
    }

    public List<Vehicle> findAll() {
        List<Vehicle> vehicles = null;
        try {
            vehicles = vehicleDao.queryForAll();
        } catch (SQLException e) {
            Log.e(TAG, "Unexpected error. See logs for details.", e);
        }
        return vehicles;
    }

    public ServiceResult delete(Vehicle vehicle) {
        try {
            vehicleDao.delete(vehicle);
            Log.i(TAG, "Successfully deleted Vehicle: " + vehicle);
            return ServiceResult.SUCCESS;
        } catch (SQLException e) {
            Log.e(TAG, "Unexpected error. See logs for details.", e);
        }
        return ServiceResult.ERROR;
    }
}
