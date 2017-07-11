package sk.piskula.fuelup.loaders;

import android.content.Context;

import java.util.List;

import sk.piskula.fuelup.business.VehicleService;
import sk.piskula.fuelup.entity.Vehicle;


/**
 * Loader async task for vehicles
 * <p>
 * Created by Martin Styk on 15.06.2017.
 */
public class VehicleLoader extends FuelUpAbstractAsyncLoader<List<Vehicle>> {
    private static final String TAG = VehicleLoader.class.getSimpleName();
    public static final int ID = 4;

    private VehicleService vehicleService;

    public VehicleLoader(Context context, VehicleService vehicleService) {
        super(context);
        this.vehicleService = vehicleService;
    }

    @Override
    public List<Vehicle> loadInBackground() {
        return vehicleService.findAll();
    }
}


