package sk.piskula.fuelup.loaders;

import android.content.Context;

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

    public StatisticsLoader(Context context, StatisticsService statisticsService) {
        super(context);
        this.statisticsService = statisticsService;
    }

    @Override
    public StatisticsDTO loadInBackground() {
        return statisticsService.getAll();
    }
}


