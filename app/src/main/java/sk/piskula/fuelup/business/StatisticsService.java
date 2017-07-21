package sk.piskula.fuelup.business;

import android.content.Context;
import android.util.Log;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.GenericRawResults;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import sk.piskula.fuelup.data.DatabaseProvider;
import sk.piskula.fuelup.entity.Expense;
import sk.piskula.fuelup.entity.FillUp;
import sk.piskula.fuelup.entity.Vehicle;
import sk.piskula.fuelup.entity.dto.StatisticsDTO;
import sk.piskula.fuelup.entity.enums.VolumeUnit;
import sk.piskula.fuelup.entity.util.VolumeUtil;

/**
 * Created by Martin Styk on 23.06.2017.
 */
public class StatisticsService {

    private static final String TAG = StatisticsService.class.getSimpleName();
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private Dao<FillUp, Long> fillUpDao;
    private Dao<Expense, Long> expenseDao;
    private Vehicle vehicle;

    private long vehicleId;

    public StatisticsService(Context context, long vehicleId) {
        this.vehicleId = vehicleId;
        this.fillUpDao = DatabaseProvider.get(context).getFillUpDao();
        this.expenseDao = DatabaseProvider.get(context).getExpenseDao();

        try {
            this.vehicle = DatabaseProvider.get(context).getVehicleDao().queryForId(vehicleId);
        } catch (SQLException e) {
            Log.e(TAG, "SQL cannot find vehicle with id " + vehicleId, e);
        }
    }


    public StatisticsDTO getAll() {
        StatisticsDTO dto = new StatisticsDTO();

        // Totals
        BigDecimal totalCostsFuel = getTotalPriceFillUps();
        BigDecimal totalCostsExpenses = getTotalPriceExpenses();
        BigDecimal totalPrice = totalCostsExpenses.add(totalCostsFuel);
        int totalNumberFillUps = getTotalNumberFillUps();
        int totalNumberExpenses = getTotalNumberExpenses();
        BigDecimal totalFuelVolume = getTotalFuelVolume();
        long totalDrivenDistance = getTotalDrivenDistance();

        // Costs per distance
        BigDecimal totalCostsPerDistance = totalDrivenDistance > 0 ?
                totalPrice.multiply(HUNDRED).divide(new BigDecimal(totalDrivenDistance), 2, BigDecimal.ROUND_HALF_UP) : BigDecimal.ZERO;
        BigDecimal fuelCostsPerDistance = totalDrivenDistance > 0 ?
                totalCostsFuel.multiply(HUNDRED).divide(new BigDecimal(totalDrivenDistance), 2, BigDecimal.ROUND_HALF_UP) : BigDecimal.ZERO;
        BigDecimal expenseCostsPerDistance = totalDrivenDistance > 0 ?
                totalCostsExpenses.multiply(HUNDRED).divide(new BigDecimal(totalDrivenDistance), 2, BigDecimal.ROUND_HALF_UP) : BigDecimal.ZERO;

        // Fuel unit price
        // TODO
        BigDecimal averageFuelVolumePerFillUp = totalNumberFillUps > 0 ?
                totalFuelVolume.divide(BigDecimal.valueOf(totalNumberFillUps), 2, BigDecimal.ROUND_HALF_UP) : BigDecimal.ZERO;
        BigDecimal averageFuelPricePerFillUp = totalNumberFillUps > 0 ?
                totalCostsFuel.divide(BigDecimal.valueOf(totalNumberFillUps), 2, BigDecimal.ROUND_HALF_UP) : BigDecimal.ZERO;

        int averageDistanceBetweenFillUps = totalNumberFillUps > 0 ?
                BigDecimal.valueOf(totalDrivenDistance).divide(BigDecimal.valueOf(totalNumberFillUps), 2, BigDecimal.ROUND_HALF_UP).intValue() : 0;

        // FuelUp usage
        long trackingDays = getTrackingDays();

        // Consumption
        dto.setAvgConsumption(getAverageConsumption());
        dto.setAvgConsumptionReversed(getAverageConsumptionReversed());
        dto.setConsumptionReversedUnitMpg(vehicle.getConsumptionUnit().equals("mpg"));
        dto.setFuelConsumptionBest(getFuelConsumptionBest());
        dto.setFuelConsumptionWorst(getFuelConsumptionWorst());

        // Totals
        dto.setTotalCosts(totalCostsFuel.add(totalCostsExpenses));
        dto.setTotalCostsFuel(totalCostsFuel);
        dto.setTotalCostsExpenses(totalCostsExpenses);
        dto.setTotalNumberFillUps(totalNumberFillUps);
        dto.setTotalNumberExpenses(totalNumberExpenses);
        dto.setTotalFuelVolume(totalFuelVolume);
        dto.setTotalDrivenDistance(totalDrivenDistance);

        // Costs per distance
        dto.setTotalCostsPerDistance(totalCostsPerDistance);
        dto.setExpenseCostsPerDistance(expenseCostsPerDistance);
        dto.setFuelCostsPerDistance(fuelCostsPerDistance);

        // Costs per time
        dto.setAverageTotalCostPerWeek(getAveragePerTime(totalPrice, trackingDays, TimePeriod.WEEK));
        dto.setAverageTotalCostPerMonth(getAveragePerTime(totalPrice, trackingDays, TimePeriod.MONTH));
        dto.setAverageTotalCostPerYear(getAveragePerTime(totalPrice, trackingDays, TimePeriod.YEAR));
        dto.setAverageFuelCostPerWeek(getAveragePerTime(totalCostsFuel, trackingDays, TimePeriod.WEEK));
        dto.setAverageFuelCostPerMonth(getAveragePerTime(totalCostsFuel, trackingDays, TimePeriod.MONTH));
        dto.setAverageFuelCostPerYear(getAveragePerTime(totalCostsFuel, trackingDays, TimePeriod.YEAR));
        dto.setAverageExpenseCostPerWeek(getAveragePerTime(totalCostsExpenses, trackingDays, TimePeriod.WEEK));
        dto.setAverageExpenseCostPerMonth(getAveragePerTime(totalCostsExpenses, trackingDays, TimePeriod.MONTH));
        dto.setAverageExpenseCostPerYear(getAveragePerTime(totalCostsExpenses, trackingDays, TimePeriod.YEAR));

        // Refuelling statistics
        dto.setAverageFuelVolumePerFillUp(averageFuelVolumePerFillUp);
        dto.setAverageFuelPricePerFillUp(averageFuelPricePerFillUp);
        dto.setAverageNumberOfFillUpsPerWeek(getAveragePerTime(BigDecimal.valueOf(totalNumberFillUps), trackingDays, TimePeriod.WEEK));
        dto.setAverageNumberOfFillUpsPerMonth(getAveragePerTime(BigDecimal.valueOf(totalNumberFillUps), trackingDays, TimePeriod.MONTH));
        dto.setAverageNumberOfFillUpsPerYear(getAveragePerTime(BigDecimal.valueOf(totalNumberFillUps), trackingDays, TimePeriod.YEAR));
        dto.setDistanceBetweenFillUpsHighest(getDistanceBetweenFillUpsHighest());
        dto.setDistanceBetweenFillUpsLowest(getDistanceBetweenFillUpsLowest());
        dto.setDistanceBetweenFillUpsAverage(averageDistanceBetweenFillUps);

        // Fuel unit price
        dto.setFuelUnitPriceAverage(getFuelUnitPriceAverage());
        dto.setFuelUnitPriceHighest(getFuelUnitPriceHighest());
        dto.setFuelUnitPriceLowest(getFuelUnitPriceLowest());

        // Distance per time
        dto.setAverageDistancePerDay(getAveragePerTime(BigDecimal.valueOf(totalDrivenDistance), trackingDays, TimePeriod.DAY).longValue());
        dto.setAverageDistancePerWeek(getAveragePerTime(BigDecimal.valueOf(totalDrivenDistance), trackingDays, TimePeriod.WEEK).longValue());
        dto.setAverageDistancePerMonth(getAveragePerTime(BigDecimal.valueOf(totalDrivenDistance), trackingDays, TimePeriod.MONTH).longValue());
        dto.setAverageDistancePerYear(getAveragePerTime(BigDecimal.valueOf(totalDrivenDistance), trackingDays, TimePeriod.YEAR).longValue());

        // Expense statistics
        dto.setAverageNumberOfExpensesPerWeek(getAveragePerTime(BigDecimal.valueOf(totalNumberExpenses), trackingDays, TimePeriod.WEEK));
        dto.setAverageNumberOfExpensesPerMonth(getAveragePerTime(BigDecimal.valueOf(totalNumberExpenses), trackingDays, TimePeriod.MONTH));
        dto.setAverageNumberOfExpensesPerYear(getAveragePerTime(BigDecimal.valueOf(totalNumberExpenses), trackingDays, TimePeriod.YEAR));

        // FuelUp usage
        dto.setTrackingDays(trackingDays);

        return dto;
    }

    public BigDecimal getAverageConsumption() {
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

    public BigDecimal getAverageConsumptionReversed() {
        if (vehicle.getVolumeUnit() == VolumeUnit.LITRE) {
            // if default (NOT REVERSED) consumption unit is l/100km
            try {
                GenericRawResults<String[]> results = fillUpDao.queryRaw("SELECT SUM(distance_from_last_fill_up) / SUM(distance_from_last_fill_up * consumption / 100) FROM fill_ups WHERE consumption is not null and vehicle_id = " + vehicleId);
                return getBigDecimal(results);
            } catch (SQLException e1) {
                Log.e(TAG, "Unexpected error during computing average fuel consumption.", e1);
            } catch (ParseException e2) {
                Log.e(TAG, "SQL return not parsable number.", e2);
            }
            return BigDecimal.ZERO;

        } else {
            // if default consumption unit is miles per gallon
            BigDecimal milesPerOneLitre = getAverageConsumption().divide(
                    vehicle.getVolumeUnit() == VolumeUnit.GALLON_UK ?
                            VolumeUtil.ONE_UK_GALLON_IS_LITRES :
                            VolumeUtil.ONE_US_GALLON_IS_LITRES,
                    14, RoundingMode.HALF_UP);
            BigDecimal litrePerOneMile = BigDecimal.ONE.divide(milesPerOneLitre, 14, RoundingMode.HALF_UP);
            return litrePerOneMile.multiply(HUNDRED);
        }
    }

    public BigDecimal getTotalPriceFillUps() {
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

    public BigDecimal getTotalPriceExpenses() {
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

    public int getTotalNumberFillUps() {
        try {
            return (int) fillUpDao.queryBuilder().where().eq("vehicle_id", vehicleId).countOf();
        } catch (SQLException e) {
            Log.e(TAG, e.toString(), e);
        }
        return 0;
    }

    public int getTotalNumberExpenses() {
        try {
            return (int) expenseDao.queryBuilder().where().eq("vehicle_id", vehicleId).countOf();
        } catch (SQLException e) {
            Log.e(TAG, e.toString(), e);
        }
        return 0;
    }

    public long getTotalDrivenDistance() {
        try {
            return fillUpDao.queryRawValue("SELECT SUM(distance_from_last_fill_up) FROM fill_ups WHERE vehicle_id = " + vehicleId);
        } catch (SQLException e) {
            Log.e(TAG, e.toString(), e);
        }
        return 0;
    }

    public BigDecimal getTotalFuelVolume() {
        try {
            GenericRawResults<String[]> results = fillUpDao.queryRaw("SELECT SUM(fuel_volume) FROM fill_ups WHERE vehicle_id = " + vehicleId);
            return getBigDecimal(results);
        } catch (SQLException e1) {
            Log.e(TAG, "Unexpected error during computing total price of all fill ups", e1);
        } catch (ParseException e2) {
            Log.e(TAG, "SQL return not parsable number.", e2);
        }
        return BigDecimal.ZERO;
    }

    public long getTrackingDays() {
        try {
            //TODO check possibility to use min(date)
            GenericRawResults<String[]> results = fillUpDao.queryRaw("SELECT id FROM fill_ups WHERE vehicle_id = " + vehicleId + " ORDER BY date LIMIT 1");
            long firstEntryId;
            String[] firstResult = results.getFirstResult();
            if (firstResult != null) {
                if (firstResult.length > 0 && firstResult[0] != null) {
                    firstEntryId = Long.valueOf(firstResult[0]);
                    FillUp fillUp = fillUpDao.queryBuilder().where().eq("id", firstEntryId).queryForFirst();

                    long durationMillis = new Date().getTime() - fillUp.getDate().getTime();

                    return TimeUnit.MILLISECONDS.toDays(durationMillis);
                } else {
                    return 0;
                }
            } else {
                return 0;
            }
        } catch (SQLException e) {
            Log.e(TAG, e.getLocalizedMessage(), e);
            return 0;
        }
    }

    private BigDecimal getAveragePerTime(BigDecimal totalCostAllTime, long trackingPeriod, TimePeriod timePeriod) {
        if (trackingPeriod == 0) return BigDecimal.ZERO;

        double trackingIntervals = trackingPeriod / (double) timePeriod.getDays();
        return totalCostAllTime.divide(BigDecimal.valueOf(trackingIntervals), 2, RoundingMode.HALF_UP);
    }

    public BigDecimal getFuelConsumptionWorst() {
        try {
            GenericRawResults<String[]> results = fillUpDao.queryRaw("SELECT MAX(consumption) FROM fill_ups WHERE consumption IS NOT NULL AND vehicle_id = " + vehicleId);
            return getBigDecimal(results);
        } catch (SQLException e1) {
            Log.e(TAG, e1.toString(), e1);
        } catch (ParseException e2) {
            Log.e(TAG, "SQL return not parsable number.", e2);
        }
        return BigDecimal.ZERO;
    }

    public BigDecimal getFuelConsumptionBest() {
        try {
            GenericRawResults<String[]> results = fillUpDao.queryRaw("SELECT MIN(consumption) FROM fill_ups WHERE consumption IS NOT NULL AND vehicle_id = " + vehicleId);
            return getBigDecimal(results);
        } catch (SQLException e1) {
            Log.e(TAG, e1.toString(), e1);
        } catch (ParseException e2) {
            Log.e(TAG, "SQL return not parsable number.", e2);
        }
        return BigDecimal.ZERO;
    }

    public BigDecimal getFuelUnitPriceHighest() {
        try {
            GenericRawResults<String[]> results = fillUpDao.queryRaw("SELECT MAX(fuel_price_per_litre) FROM fill_ups WHERE vehicle_id = " + vehicleId);
            return getBigDecimal(results);
        } catch (SQLException e1) {
            Log.e(TAG, e1.toString(), e1);
        } catch (ParseException e2) {
            Log.e(TAG, "SQL return not parsable number.", e2);
        }
        return BigDecimal.ZERO;
    }

    public BigDecimal getFuelUnitPriceAverage() {
        try {
            GenericRawResults<String[]> results = fillUpDao.queryRaw("SELECT SUM(fuel_price_per_litre)/ COUNT(*) FROM fill_ups WHERE vehicle_id = " + vehicleId);
            return getBigDecimal(results);
        } catch (SQLException e1) {
            Log.e(TAG, e1.toString(), e1);
        } catch (ParseException e2) {
            Log.e(TAG, "SQL return not parsable number.", e2);
        }
        return BigDecimal.ZERO;
    }

    public BigDecimal getFuelUnitPriceLowest() {
        try {
            GenericRawResults<String[]> results = fillUpDao.queryRaw("SELECT MIN(fuel_price_per_litre) FROM fill_ups WHERE vehicle_id = " + vehicleId);
            return getBigDecimal(results);
        } catch (SQLException e1) {
            Log.e(TAG, e1.toString(), e1);
        } catch (ParseException e2) {
            Log.e(TAG, "SQL return not parsable number.", e2);
        }
        return BigDecimal.ZERO;
    }

    public int getDistanceBetweenFillUpsHighest() {
        try {
            GenericRawResults<String[]> results = fillUpDao.queryRaw("SELECT MAX(distance_from_last_fill_up) FROM fill_ups WHERE vehicle_id = " + vehicleId);
            return (int) getLong(results);
        } catch (SQLException e1) {
            Log.e(TAG, e1.toString(), e1);
        } catch (ParseException e2) {
            Log.e(TAG, "SQL return not parsable number.", e2);
        }
        return 0;
    }

    public int getDistanceBetweenFillUpsLowest() {
        try {
            GenericRawResults<String[]> results = fillUpDao.queryRaw("SELECT MIN(distance_from_last_fill_up) FROM fill_ups WHERE vehicle_id = " + vehicleId);
            return (int) getLong(results);
        } catch (SQLException e1) {
            Log.e(TAG, e1.toString(), e1);
        } catch (ParseException e2) {
            Log.e(TAG, "SQL return not parsable number.", e2);
        }
        return 0;
    }

    //TODO handle better
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

    private long getLong(GenericRawResults<String[]> results) throws SQLException, ParseException {
        String[] firstResult = results.getFirstResult();
        if (firstResult != null && firstResult.length > 0 && firstResult[0] != null) {
            return Long.valueOf(firstResult[0]);
        } else {
            return 0l;
        }
    }


    private enum TimePeriod {
        DAY(1),
        WEEK(7),
        MONTH(30),
        YEAR(365);

        private int days;

        TimePeriod(int days) {
            this.days = days;
        }

        public int getDays() {
            return days;
        }
    }

}

