package sk.piskula.fuelup.data;

import android.content.ContentValues;
import android.content.Context;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

import sk.piskula.fuelup.data.FuelUpContract.FillUpEntry;
import sk.piskula.fuelup.data.FuelUpContract.ExpenseEntry;

/**
 * @author Ondrej Oravcok
 * @version 19.6.2017
 */
public class SampleDataUtils {

    private static final int NUMBER_OF_PREVIOUS_YEARS = 3;
    private static final double MI_TO_KM = 1.5d; //original value 1 mile = 1.609344 km
    private static final double LITRE_TO_GALLON = 0.264172d;

    private static final int MAX_FILLUPS = 60;
    private static final int MAX_EXPENSES = 60;

    public static void initializeWhenFirstRun(Context context) {
        Random random = new Random();

        Calendar end = Calendar.getInstance();
        Calendar start = Calendar.getInstance();
        start.add(Calendar.YEAR, -NUMBER_OF_PREVIOUS_YEARS);
        long diff = end.getTimeInMillis() - start.getTimeInMillis() + 1;

        addFillUps(context, random, start.getTimeInMillis(), diff);
        addExpenses(context, random, start.getTimeInMillis(), diff);
    }

    private static void addFillUps(Context context, Random random, long start, long diff) {
        for (int i = 0; i < MAX_FILLUPS; i++) {
            ContentValues values = new ContentValues();

            double currencyFactor = 0.16d; //whichVehicle == 1 ? 0.16d : 4d;
            double gallonFactor = 1d; //whichVehicle == 1 ? 1d : LITRE_TO_GALLON;
            int dist = random.nextInt(8) + 4;
            double pricePerLitre = (random.nextDouble() + 2) * 3.1 * currencyFactor;
            double fuelVolume = (dist + 3 * random.nextDouble())* 4 * 0.7 * gallonFactor;

            values.put(FillUpEntry.COLUMN_VEHICLE, 1);
            values.put(FillUpEntry.COLUMN_DATE, start + ((long) (Math.random() * diff)));
            values.put(FillUpEntry.COLUMN_FUEL_PRICE_PER_LITRE, pricePerLitre);
            values.put(FillUpEntry.COLUMN_DISTANCE_FROM_LAST, Long.valueOf(dist * 40));
            values.put(FillUpEntry.COLUMN_FUEL_VOLUME, fuelVolume);
            values.put(FillUpEntry.COLUMN_IS_FULL_FILLUP, random.nextBoolean() ? 1 : 0);
            values.put(FillUpEntry.COLUMN_FUEL_PRICE_TOTAL, fuelVolume * pricePerLitre);

            context.getContentResolver().insert(FillUpEntry.CONTENT_URI, values);
        }
    }

    private static void addExpenses(Context context, Random random, long start, long diff) {
        List<String> issues = Arrays.asList("Wheels - winter", "Clutch - new", "Exhaust tuning",
                "Wheels - summer", "Air condition - service", "Oil - change", "Fuel filter - change",
                "Front bumper", "Rear window - repair", "Steering wheel", "Stereo - new", "Brembo breaks",
                "NOS - nitro tuning", "Yearly insurance", "Tires interchange", "Air freshener",
                "Rear window - new", "Kid Seat", "STK & ETK", "Additional insurance", "Radiator - new",
                "Registration fee", "Spark plugs - replace", "Castrol Magnatec lubricate");
        List<Double> prices = Arrays.asList(7d, 50d, 3.6d, 220d, 315d, 450d, 11700d, 7400d, 3200d, 14d, 15d, 18d, 45d, 52d, 110d, 95d, 130d, 8400d, 8500d, 7600d, 350d, 420d, 710d, 700d, 1200d, 115d);

        int currencyFactor = 1; //whichVehicle == 1 ? 1 : 4;

        for (int i = 0; i < MAX_EXPENSES; i++) {
            ContentValues values = new ContentValues();

            values.put(ExpenseEntry.COLUMN_VEHICLE, 1);
            values.put(ExpenseEntry.COLUMN_DATE, start + ((long) (Math.random() * diff)));
            values.put(ExpenseEntry.COLUMN_INFO, issues.get(random.nextInt(issues.size())));
            values.put(ExpenseEntry.COLUMN_PRICE, prices.get(random.nextInt(prices.size())) * currencyFactor);

            context.getContentResolver().insert(ExpenseEntry.CONTENT_URI, values);
        }
    }
}
