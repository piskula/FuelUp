package sk.momosi.fuelup.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

import sk.momosi.fuelup.entity.Vehicle;
import sk.momosi.fuelup.entity.VehicleType;

/**
 * @author Ondrej Oravcok
 * @version 7.8.2017
 */
public class FuelUpContract {

    private FuelUpContract() {
    }

    public static final String CONTENT_AUTHORITY = "sk.momosi.fuelup";
    private static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_EXPENSES = "expenses";
    public static final String PATH_VEHICLES = "vehicles";
    public static final String PATH_FILLUPS = "fillups";
    public static final String PATH_VEHICLE_TYPES = "vehicle_types";



    public static final String[] ALL_COLUMNS_EXPENSES = {
            ExpenseEntry._ID,
            ExpenseEntry.COLUMN_VEHICLE,
            ExpenseEntry.COLUMN_PRICE,
            ExpenseEntry.COLUMN_DATE,
            ExpenseEntry.COLUMN_INFO
    };

    /**
     * Inner class that defines constant values for the expenses database table.
     * Each Entry in the table represents a single Expense.
     */
    public static final class ExpenseEntry implements BaseColumns {

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_EXPENSES);
        public static final String TABLE_NAME = "expenses";

        public final static String _ID = BaseColumns._ID;
        public final static String COLUMN_VEHICLE = "vehicle";
        public final static String COLUMN_PRICE = "price";
        public final static String COLUMN_DATE = "date";
        public final static String COLUMN_INFO = "info";

        public static final String CONTENT_LIST_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/" + CONTENT_AUTHORITY + "/" + PATH_EXPENSES;
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/" + CONTENT_AUTHORITY + "/" + PATH_EXPENSES;
    }



    public static final String[] ALL_COLUMNS_VEHICLES = {
            VehicleEntry._ID,
            VehicleEntry.COLUMN_NAME,
            VehicleEntry.COLUMN_TYPE,
            VehicleEntry.COLUMN_VOLUME_UNIT,
            VehicleEntry.COLUMN_VEHICLE_MAKER,
            VehicleEntry.COLUMN_START_MILEAGE,
            VehicleEntry.COLUMN_CURRENCY,
            VehicleEntry.COLUMN_PICTURE
    };

    /**
     * Inner class that defines constant values for the vehicles database table.
     * Each Entry in the table represents a single Vehicle.
     */
    public static final class VehicleEntry implements BaseColumns {

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_VEHICLES);
        public static final String TABLE_NAME = "vehicles";

        public final static String _ID = BaseColumns._ID;
        public final static String COLUMN_NAME = "name";
        public final static String COLUMN_TYPE = "type";
        public final static String COLUMN_VOLUME_UNIT = "volume_unit";
        public final static String COLUMN_VEHICLE_MAKER = "vehicle_maker";
        public final static String COLUMN_START_MILEAGE = "start_mileage";
        public final static String COLUMN_CURRENCY = "currency";
        public final static String COLUMN_PICTURE = "picture";

        public static final String CONTENT_LIST_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/" + CONTENT_AUTHORITY + "/" + PATH_VEHICLES;
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/" + CONTENT_AUTHORITY + "/" + PATH_VEHICLES;
    }



    public static final String[] ALL_COLUMNS_VEHICLE_TYPES = {
            VehicleTypeEntry._ID,
            VehicleTypeEntry.COLUMN_NAME
    };

    /**
     * Inner class that defines constant values for the vehicle types database table.
     * Each Entry in the table represents a single VehicleType.
     */
    public static final class VehicleTypeEntry implements BaseColumns {

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_VEHICLE_TYPES);
        public static final String TABLE_NAME = "vehicle_types";

        public final static String _ID = BaseColumns._ID;
        public final static String COLUMN_NAME = "name";

        public static final String CONTENT_LIST_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/" + CONTENT_AUTHORITY + "/" + PATH_VEHICLE_TYPES;
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/" + CONTENT_AUTHORITY + "/" + PATH_VEHICLE_TYPES;
    }



    public static final String[] ALL_COLUMNS_FILLUPS = {
            FillUpEntry._ID,
            FillUpEntry.COLUMN_VEHICLE,
            FillUpEntry.COLUMN_DISTANCE_FROM_LAST,
            FillUpEntry.COLUMN_FUEL_VOLUME,
            FillUpEntry.COLUMN_FUEL_PRICE_PER_LITRE,
            FillUpEntry.COLUMN_FUEL_PRICE_TOTAL,
            FillUpEntry.COLUMN_IS_FULL_FILLUP,
            FillUpEntry.COLUMN_FUEL_CONSUMPTION,
            FillUpEntry.COLUMN_DATE,
            FillUpEntry.COLUMN_INFO
    };

    /**
     * Inner class that defines constant values for the fillups database table.
     * Each Entry in the table represents a single FillUp.
     */
    public static final class FillUpEntry implements BaseColumns {

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_FILLUPS);
        public static final String TABLE_NAME = "fillups";

        public final static String _ID = BaseColumns._ID;
        public final static String COLUMN_VEHICLE = "vehicle";
        public final static String COLUMN_DISTANCE_FROM_LAST = "distance_from_last_fillup";
        public final static String COLUMN_FUEL_VOLUME = "fuel_volume";
        public final static String COLUMN_FUEL_PRICE_PER_LITRE = "fuel_price_per_litre";
        public final static String COLUMN_FUEL_PRICE_TOTAL = "fuel_price_total";
        public final static String COLUMN_IS_FULL_FILLUP = "is_full_fillup";
        public final static String COLUMN_FUEL_CONSUMPTION = "fuel_consumption";
        public final static String COLUMN_DATE = "date";
        public final static String COLUMN_INFO = "info";

        public static final String CONTENT_LIST_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/" + CONTENT_AUTHORITY + "/" + PATH_FILLUPS;
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/" + CONTENT_AUTHORITY + "/" + PATH_FILLUPS;
    }

}
