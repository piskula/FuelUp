package sk.piskula.fuelup.business;

import android.content.Context;
import android.util.Log;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.GenericRawResults;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.ParseException;

import sk.piskula.fuelup.data.DatabaseProvider;
import sk.piskula.fuelup.entity.Expense;
import sk.piskula.fuelup.entity.FillUp;

/**
 * Created by Martin Styk on 23.06.2017.
 */

public class StatisticsService {

    private static final String TAG = StatisticsService.class.getSimpleName();

    private Dao<FillUp, Long> fillUpDao;
    private Dao<Expense, Long> expenseDao;


    public StatisticsService(Context context) {
        this.fillUpDao = DatabaseProvider.get(context).getFillUpDao();
        this.expenseDao = DatabaseProvider.get(context).getExpenseDao();
    }

    public BigDecimal getAverageConsumptionOfVehicle(long vehicleId) {
        try {
            GenericRawResults<String[]> results = fillUpDao.queryRaw("SELECT SUM(distance_from_last_fill_up * consumption) / SUM(distance_from_last_fill_up) FROM fill_ups WHERE consumption is not null and vehicle_id = " + vehicleId);
            return getBigDecimal(results);
        } catch (SQLException e1) {
            Log.e(TAG, "Unexpected error during computing average fuel consumption.", e1);
        } catch (ParseException e2) {
            Log.e(TAG, "SQL return not parsable number.", e2);
        }
        return BigDecimal.ZERO;
    }

    public BigDecimal getTotalPriceFillUps(long vehicleId) {
        try {
            GenericRawResults<String[]> results = fillUpDao.queryRaw("SELECT SUM(fuel_price_total) FROM fill_ups WHERE vehicle_id = " + vehicleId);
            return getBigDecimal(results);
        } catch (SQLException e1) {
            Log.e(TAG, "Unexpected error during computing total price of all fill ups", e1);
        } catch (ParseException e2) {
            Log.e(TAG, "SQL return not parsable number.", e2);
        }
        return BigDecimal.ZERO;
    }

    public BigDecimal getTotalPriceExpenses(long vehicleId) {
        try {
            GenericRawResults<String[]> results = expenseDao.queryRaw("SELECT SUM(price) FROM expenses WHERE vehicle_id = " + vehicleId);
            return getBigDecimal(results);
        } catch (SQLException e1) {
            Log.e(TAG, "Unexpected error during computing total price of all expenses", e1);
        } catch (ParseException e2) {
            Log.e(TAG, "SQL return not parsable number.", e2);
        }
        return BigDecimal.ZERO;
    }

    private BigDecimal getBigDecimal(GenericRawResults<String[]> results) throws SQLException, ParseException {
        DecimalFormat f = new DecimalFormat();
        f.setParseBigDecimal(true);
        String[] parsedResult = results.getResults().get(0);
        if (parsedResult.length > 0 && parsedResult[0] != null) {
            return (BigDecimal) f.parseObject(parsedResult[0]);
        } else {
            return BigDecimal.ZERO;
        }
    }
}

