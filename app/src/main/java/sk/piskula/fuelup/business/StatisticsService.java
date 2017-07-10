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
import sk.piskula.fuelup.entity.Expense;
import sk.piskula.fuelup.entity.FillUp;

/**
 * Created by Martin Styk on 23.06.2017.
 */

public class StatisticsService {

    private static final String TAG = StatisticsService.class.getSimpleName();

    private Context context;
    private Dao<FillUp, Long> fillUpDao;
    private Dao<Expense, Long> expenseDao;


    public StatisticsService(Context context) {
        this.context = context;
        this.fillUpDao = DatabaseProvider.get(context).getFillUpDao();
        this.expenseDao = DatabaseProvider.get(context).getExpenseDao();
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
}

