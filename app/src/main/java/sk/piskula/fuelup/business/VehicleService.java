package sk.piskula.fuelup.business;

import android.content.Context;
import android.util.Log;

import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;
import java.util.Currency;

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
    public boolean save(String name) {
        Vehicle vehicle = new Vehicle();
        vehicle.setName(name);
        vehicle.setUnit(DistanceUnit.km);
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
    public boolean save(Vehicle vehicle) {
        try {
            vehicleDao.create(vehicle);
            return true;
        } catch (SQLException e) {
            String status;
            if (e.getCause().getCause().getMessage().contains("UNIQUE")) {
                status = "Cannot create duplicate vehicle";
            } else {
                status = "Unexpected error. See logs for details.";
            }
            Log.e(TAG, status, e);
        }
        return false;
    }
}
