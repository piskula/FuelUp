package sk.piskula.fuelup.loaders;

import android.content.Context;

import java.math.BigDecimal;

import sk.piskula.fuelup.business.FillUpService;
import sk.piskula.fuelup.business.StatisticsService;
import sk.piskula.fuelup.entity.dto.StatisticsDTO;


/**
 * Loader async task statistics data
 * <p>
 * Created by Martin Styk on 10.07.2017.
 */
public class StatisticsLoader extends FuelUpAbstractAsyncLoader<StatisticsDTO> {
    private static final String TAG = StatisticsLoader.class.getSimpleName();
    public static final int ID = 3;

    private StatisticsService statisticsService;
    private long vehicleId;

    public StatisticsLoader(Context context, long vehicleId, StatisticsService statisticsService) {
        super(context);
        this.vehicleId = vehicleId;
        this.statisticsService = statisticsService;
    }

    /**
     * This is where the bulk of our work is done.  This function is
     * called in a background thread and should generate a new set of
     * data to be published by the loader.
     */
    @Override
    public StatisticsDTO loadInBackground() {
        StatisticsDTO dto = new StatisticsDTO();

        dto.setAvgConsumption(statisticsService.getAverageConsumptionOfVehicle(vehicleId));
        dto.setTotalPricePerDistance(new BigDecimal(10.5));

        return dto;
    }
}


