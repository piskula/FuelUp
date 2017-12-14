package sk.momosi.fuelup.business;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import sk.momosi.fuelup.data.DatabaseHelper;
import sk.momosi.fuelup.data.FuelUpContract.ExpenseEntry;
import sk.momosi.fuelup.data.FuelUpContract.FillUpEntry;
import sk.momosi.fuelup.entity.Vehicle;
import sk.momosi.fuelup.entity.dto.StatisticsDTO;
import sk.momosi.fuelup.entity.enums.VolumeUnit;
import sk.momosi.fuelup.entity.util.VolumeUtil;

/**
 * @author Martin Styk, Ondrej Oravcok
 * @version 17.8.2017
 */
public class StatisticsService {

    private static final String LOG_TAG = StatisticsService.class.getSimpleName();
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private final DatabaseHelper dbHelper;
    private final long mVehicleId;
    private final Vehicle vehicle;

    public StatisticsService(Context context, long vehicleId) {
        dbHelper = new DatabaseHelper(context);
        mVehicleId = vehicleId;
        vehicle = VehicleService.getVehicleById(vehicleId, context);
    }

    public StatisticsDTO getAll() {
        StatisticsDTO dto = new StatisticsDTO();

        // Totals
        BigDecimal totalCostsFuel = getTotalPriceFillUpsForVehicle();
        BigDecimal totalCostsExpenses = getTotalPriceOfExpensesForVehicle();
        BigDecimal totalPrice = totalCostsExpenses.add(totalCostsFuel);
        int totalNumberFillUps = getTotalNumberOfFillUpsForVehicle();
        int totalNumberExpenses = getTotalNumberOfExpensesForVehicle();
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
        Cursor cursor = dbHelper.getReadableDatabase().rawQuery(
                "SELECT TOTAL(" + FillUpEntry.COLUMN_DISTANCE_FROM_LAST + " * " + FillUpEntry.COLUMN_FUEL_CONSUMPTION + ") / SUM(" + FillUpEntry.COLUMN_DISTANCE_FROM_LAST
                        + ") FROM " + FillUpEntry.TABLE_NAME
                        + " WHERE " + FillUpEntry.COLUMN_VEHICLE + "=? AND " + FillUpEntry.COLUMN_FUEL_CONSUMPTION + " IS NOT NULL",
                getAsArgument(mVehicleId));
        if (cursor.moveToFirst()) {
            BigDecimal result = BigDecimal.valueOf(cursor.getDouble(0));
            cursor.close();
            return result;
        }
        Log.e(LOG_TAG, "Cannot retrieve average consumption for vehicleId=" + mVehicleId);
        return BigDecimal.ZERO;
    }

    private BigDecimal getAverageConsumptionReversed() {
        if (vehicle.getVolumeUnit() == VolumeUnit.LITRE) {
            // if default (NOT REVERSED) consumption unit is l/100km
            Cursor cursor = dbHelper.getReadableDatabase().rawQuery(
                    "SELECT TOTAL(" + FillUpEntry.COLUMN_DISTANCE_FROM_LAST + ") / SUM(" + FillUpEntry.COLUMN_DISTANCE_FROM_LAST + " * " + FillUpEntry.COLUMN_FUEL_CONSUMPTION + " / 100"
                            + ") FROM " + FillUpEntry.TABLE_NAME
                            + " WHERE " + FillUpEntry.COLUMN_VEHICLE + "=? AND " + FillUpEntry.COLUMN_FUEL_CONSUMPTION + " IS NOT NULL",
                    getAsArgument(mVehicleId));
            if (cursor.moveToFirst()) {
                BigDecimal result = BigDecimal.valueOf(cursor.getDouble(0));
                cursor.close();
                return result;
            }
            Log.e(LOG_TAG, "Cannot retrieve reversed consumption for vehicleId=" + mVehicleId);
            return BigDecimal.ZERO;

        } else {
            // if default consumption unit is miles per gallon
            BigDecimal milesPerOneLitre = getAverageConsumption().divide(
                    vehicle.getVolumeUnit() == VolumeUnit.GALLON_UK ?
                            VolumeUtil.ONE_UK_GALLON_IS_LITRES :
                            VolumeUtil.ONE_US_GALLON_IS_LITRES,
                    14, RoundingMode.HALF_UP);

            if (milesPerOneLitre.compareTo(BigDecimal.ZERO) == 0)
                return BigDecimal.ZERO;

            BigDecimal litrePerOneMile = BigDecimal.ONE.divide(milesPerOneLitre, 14, RoundingMode.HALF_UP);
            return litrePerOneMile.multiply(HUNDRED);
        }
    }

    private BigDecimal getTotalPriceFillUpsForVehicle() {
        Cursor cursor = dbHelper.getReadableDatabase().rawQuery(
                "SELECT TOTAL(" + FillUpEntry.COLUMN_FUEL_PRICE_TOTAL
                        + ") FROM " + FillUpEntry.TABLE_NAME
                        + " WHERE " + FillUpEntry.COLUMN_VEHICLE + "=?", getAsArgument(mVehicleId));
        if (cursor.moveToFirst()) {
            BigDecimal result = BigDecimal.valueOf(cursor.getDouble(0));
            cursor.close();
            return result;
        }
        Log.e(LOG_TAG, "Cannot retrieve total price of fillups for vehicleId=" + mVehicleId);
        return BigDecimal.ZERO;
    }

    private BigDecimal getTotalPriceOfExpensesForVehicle() {
        Cursor cursor = dbHelper.getReadableDatabase().rawQuery(
                "SELECT TOTAL(" + ExpenseEntry.COLUMN_PRICE
                        + ") FROM " + ExpenseEntry.TABLE_NAME
                        + " WHERE " + ExpenseEntry.COLUMN_VEHICLE + "=?", getAsArgument(mVehicleId));
        if (cursor.moveToFirst()) {
            BigDecimal result = BigDecimal.valueOf(cursor.getDouble(0));
            cursor.close();
            return result;
        }
        Log.e(LOG_TAG, "Cannot retrieve total price of expenses for vehicleId=" + mVehicleId);
        return BigDecimal.ZERO;
    }

    private int getTotalNumberOfFillUpsForVehicle() {
        Cursor cursor = dbHelper.getReadableDatabase().rawQuery(
                "SELECT COUNT(*"
                        + ") FROM " + FillUpEntry.TABLE_NAME
                        + " WHERE " + FillUpEntry.COLUMN_VEHICLE + "=?", getAsArgument(mVehicleId));
        if (cursor.moveToFirst()) {
            int result = cursor.getInt(0);
            cursor.close();
            return result;
        }
        Log.e(LOG_TAG, "Cannot retrieve total count of fillups for vehicleId=" + mVehicleId);
        return 0;
    }

    private int getTotalNumberOfExpensesForVehicle() {
        Cursor cursor = dbHelper.getReadableDatabase().rawQuery(
                "SELECT COUNT(*"
                        + ") FROM " + ExpenseEntry.TABLE_NAME
                        + " WHERE " + ExpenseEntry.COLUMN_VEHICLE + "=?", getAsArgument(mVehicleId));
        if (cursor.moveToFirst()) {
            int result = cursor.getInt(0);
            cursor.close();
            return result;
        }
        Log.e(LOG_TAG, "Cannot retrieve total count of expenses for vehicleId=" + mVehicleId);
        return 0;
    }

    private long getTotalDrivenDistance() {
        Cursor cursor = dbHelper.getReadableDatabase().rawQuery(
                "SELECT TOTAL(" + FillUpEntry.COLUMN_DISTANCE_FROM_LAST
                        + ") FROM " + FillUpEntry.TABLE_NAME
                        + " WHERE " + FillUpEntry.COLUMN_VEHICLE + "=?", getAsArgument(mVehicleId));
        if (cursor.moveToFirst()) {
            long result = cursor.getLong(0);
            cursor.close();
            return result;
        }
        Log.e(LOG_TAG, "Cannot retrieve total driven distance for vehicleId=" + mVehicleId);
        return 0L;
    }

    public String getActualMileageIfPossible() {
        if (vehicle.getStartMileage() == null) {
            return "";
        } else {
            return (vehicle.getStartMileage() + getTotalDrivenDistance()) + vehicle.getDistanceUnit().toString();
        }
    }

    public Long getActualMileageAsLongIfPossible() {
        if (vehicle.getStartMileage() == null) {
            return null;
        } else {
            return vehicle.getStartMileage() + getTotalDrivenDistance();
        }
    }

    private BigDecimal getTotalFuelVolume() {
        Cursor cursor = dbHelper.getReadableDatabase().rawQuery(
                "SELECT TOTAL(" + FillUpEntry.COLUMN_FUEL_VOLUME
                        + ") FROM " + FillUpEntry.TABLE_NAME
                        + " WHERE " + FillUpEntry.COLUMN_VEHICLE + "=?", getAsArgument(mVehicleId));
        if (cursor.moveToFirst()) {
            BigDecimal result = BigDecimal.valueOf(cursor.getDouble(0));
            cursor.close();
            return result;
        }
        Log.e(LOG_TAG, "Cannot retrieve total fuel amount for vehicleId=" + mVehicleId);
        return BigDecimal.ZERO;
    }

    private long getTrackingDays() {
        Cursor cursor = dbHelper.getReadableDatabase().rawQuery(
                "SELECT MIN(" + FillUpEntry.COLUMN_DATE
                        + ") FROM " + FillUpEntry.TABLE_NAME
                        + " WHERE " + FillUpEntry.COLUMN_VEHICLE + "=?",
                getAsArgument(mVehicleId));
        if (cursor.moveToFirst()) {
            long firstEntry = (new Date(cursor.getLong(0))).getTime();
            long today = (new Date()).getTime();
            cursor.close();
            return TimeUnit.DAYS.convert(Math.abs(today - firstEntry), TimeUnit.MILLISECONDS);
        }
        Log.e(LOG_TAG, "Cannot retrieve number of tracking days for vehicleId=" + mVehicleId);
        return 0L;
    }

    private BigDecimal getAveragePerTime(BigDecimal totalCostAllTime, long trackingPeriod, TimePeriod timePeriod) {
        if (trackingPeriod == 0) return BigDecimal.ZERO;

        double trackingIntervals = trackingPeriod / (double) timePeriod.getDays();
        return totalCostAllTime.divide(BigDecimal.valueOf(trackingIntervals), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal getFuelConsumptionWorst() {
        Cursor cursor = dbHelper.getReadableDatabase().rawQuery(
                "SELECT MAX(" + FillUpEntry.COLUMN_FUEL_CONSUMPTION
                        + ") FROM " + FillUpEntry.TABLE_NAME
                        + " WHERE " + FillUpEntry.COLUMN_VEHICLE + "=? AND " + FillUpEntry.COLUMN_FUEL_CONSUMPTION + " IS NOT NULL",
                getAsArgument(mVehicleId));
        if (cursor.moveToFirst()) {
            BigDecimal result = BigDecimal.valueOf(cursor.getDouble(0));
            cursor.close();
            return result;
        }
        Log.e(LOG_TAG, "Cannot retrieve max-consumption for vehicleId=" + mVehicleId);
        return BigDecimal.ZERO;
    }

    public BigDecimal getFuelConsumptionBest() {
        Cursor cursor = dbHelper.getReadableDatabase().rawQuery(
                "SELECT MIN(" + FillUpEntry.COLUMN_FUEL_CONSUMPTION
                        + ") FROM " + FillUpEntry.TABLE_NAME
                        + " WHERE " + FillUpEntry.COLUMN_VEHICLE + "=? AND " + FillUpEntry.COLUMN_FUEL_CONSUMPTION + " IS NOT NULL",
                getAsArgument(mVehicleId));
        if (cursor.moveToFirst()) {
            BigDecimal result = BigDecimal.valueOf(cursor.getDouble(0));
            cursor.close();
            return result;
        }
        Log.e(LOG_TAG, "Cannot retrieve min-consumption for vehicleId=" + mVehicleId);
        return BigDecimal.ZERO;
    }

    private BigDecimal getFuelUnitPriceHighest() {
        Cursor cursor = dbHelper.getReadableDatabase().rawQuery(
                "SELECT MAX(" + FillUpEntry.COLUMN_FUEL_PRICE_PER_LITRE
                        + ") FROM " + FillUpEntry.TABLE_NAME
                        + " WHERE " + FillUpEntry.COLUMN_VEHICLE + "=?",
                getAsArgument(mVehicleId));
        if (cursor.moveToFirst()) {
            BigDecimal result = BigDecimal.valueOf(cursor.getDouble(0));
            cursor.close();
            return result;
        }
        Log.e(LOG_TAG, "Cannot retrieve max fuel price per litre for vehicleId=" + mVehicleId);
        return BigDecimal.ZERO;
    }

    public BigDecimal getFuelUnitPriceAverage() {
        Cursor cursor = dbHelper.getReadableDatabase().rawQuery(
                "SELECT TOTAL(" + FillUpEntry.COLUMN_FUEL_PRICE_PER_LITRE + ") / COUNT(*"
                        + ") FROM " + FillUpEntry.TABLE_NAME
                        + " WHERE " + FillUpEntry.COLUMN_VEHICLE + "=?",
                getAsArgument(mVehicleId));
        if (cursor.moveToFirst()) {
            BigDecimal result = BigDecimal.valueOf(cursor.getDouble(0));
            cursor.close();
            return result;
        }
        Log.e(LOG_TAG, "Cannot retrieve average fuel price per litre for vehicleId=" + mVehicleId);
        return BigDecimal.ZERO;
    }

    private BigDecimal getFuelUnitPriceLowest() {
        Cursor cursor = dbHelper.getReadableDatabase().rawQuery(
                "SELECT MIN(" + FillUpEntry.COLUMN_FUEL_PRICE_PER_LITRE
                        + ") FROM " + FillUpEntry.TABLE_NAME
                        + " WHERE " + FillUpEntry.COLUMN_VEHICLE + "=?",
                getAsArgument(mVehicleId));
        if (cursor.moveToFirst()) {
            BigDecimal result = BigDecimal.valueOf(cursor.getDouble(0));
            cursor.close();
            return result;
        }
        Log.e(LOG_TAG, "Cannot retrieve min fuel price per litre for vehicleId=" + mVehicleId);
        return BigDecimal.ZERO;
    }

    private int getDistanceBetweenFillUpsHighest() {
        Cursor cursor = dbHelper.getReadableDatabase().rawQuery(
                "SELECT MAX(" + FillUpEntry.COLUMN_DISTANCE_FROM_LAST
                        + ") FROM " + FillUpEntry.TABLE_NAME
                        + " WHERE " + FillUpEntry.COLUMN_VEHICLE + "=?",
                getAsArgument(mVehicleId));
        if (cursor.moveToFirst()) {
            int result = cursor.getInt(0);
            cursor.close();
            return result;
        }
        Log.e(LOG_TAG, "Cannot retrieve max distance between fillups for vehicleId=" + mVehicleId);
        return 0;
    }

    private int getDistanceBetweenFillUpsLowest() {
        Cursor cursor = dbHelper.getReadableDatabase().rawQuery(
                "SELECT MIN(" + FillUpEntry.COLUMN_DISTANCE_FROM_LAST
                        + ") FROM " + FillUpEntry.TABLE_NAME
                        + " WHERE " + FillUpEntry.COLUMN_VEHICLE + "=?",
                getAsArgument(mVehicleId));
        if (cursor.moveToFirst()) {
            int result = cursor.getInt(0);
            cursor.close();
            return result;
        }
        Log.e(LOG_TAG, "Cannot retrieve min distance between fillups for vehicleId=" + mVehicleId);
        return 0;
    }

    private String[] getAsArgument(long value) {
        return new String[]{String.valueOf(value)};
    }

    private enum TimePeriod {
        DAY(1),
        WEEK(7),
        MONTH(30),
        YEAR(365);

        private final int days;

        TimePeriod(int days) {
            this.days = days;
        }

        public int getDays() {
            return days;
        }
    }

}

