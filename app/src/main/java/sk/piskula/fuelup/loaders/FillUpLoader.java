package sk.piskula.fuelup.loaders;

import android.content.Context;

import java.util.List;

import sk.piskula.fuelup.business.FillUpService;
import sk.piskula.fuelup.entity.FillUp;


/**
 * Loader async task FillUps
 * <p>
 * Created by Martin Styk on 15.06.2017.
 */
public class FillUpLoader extends FuelUpAbstractAsyncLoader<List<FillUp>> {
    private static final String TAG = FillUpLoader.class.getSimpleName();
    public static final int ID = 1;

    private FillUpService fillUpService;
    private long vehicleId;

    public FillUpLoader(Context context, long vehicleId, FillUpService fillUpService) {
        super(context);
        this.vehicleId = vehicleId;
        this.fillUpService = fillUpService;
    }

    /**
     * This is where the bulk of our work is done.  This function is
     * called in a background thread and should generate a new set of
     * data to be published by the loader.
     */
    @Override
    public List<FillUp> loadInBackground() {
        try {
            //TODO remove this is just for debugging purposes
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return fillUpService.findFillUpsOfVehicle(vehicleId);
    }
}


