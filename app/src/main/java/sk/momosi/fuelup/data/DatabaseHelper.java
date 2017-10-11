package sk.momosi.fuelup.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import sk.momosi.fuelup.data.FuelUpContract.ExpenseEntry;
import sk.momosi.fuelup.data.FuelUpContract.FillUpEntry;
import sk.momosi.fuelup.data.FuelUpContract.VehicleEntry;
import sk.momosi.fuelup.data.FuelUpContract.VehicleTypeEntry;

/**
 * @author Ondrej Oravcok
 * @version 16.6.2017.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String LOG_TAG = DatabaseHelper.class.getSimpleName();

    private static final String DATABASE_NAME = "fuelup.db";
    private static final int DATABASE_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String SQL_CREATE_VEHICLE_TYPES_TABLE =  "CREATE TABLE " + VehicleTypeEntry.TABLE_NAME + " ("
                + VehicleTypeEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + VehicleTypeEntry.COLUMN_NAME + " TEXT NOT NULL);";

        String SQL_CREATE_VEHICLES_TABLE =  "CREATE TABLE " + VehicleEntry.TABLE_NAME + " ("
                + VehicleEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + VehicleEntry.COLUMN_NAME + " TEXT NOT NULL UNIQUE, "
                + VehicleEntry.COLUMN_TYPE + " INTEGER NOT NULL, "
                + VehicleEntry.COLUMN_VOLUME_UNIT + " TEXT NOT NULL, "
                + VehicleEntry.COLUMN_VEHICLE_MAKER + " TEXT, "
                + VehicleEntry.COLUMN_START_MILEAGE + " INTEGER, "
                + VehicleEntry.COLUMN_CURRENCY + " TEXT NOT NULL, "
                + VehicleEntry.COLUMN_PICTURE + " TEXT, "

                + "FOREIGN KEY(" + VehicleEntry.COLUMN_TYPE + ") REFERENCES "
                + VehicleTypeEntry.TABLE_NAME + "(" + VehicleTypeEntry._ID + ") );";

        String SQL_CREATE_EXPENSES_TABLE =  "CREATE TABLE " + ExpenseEntry.TABLE_NAME + " ("
                + ExpenseEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + ExpenseEntry.COLUMN_VEHICLE + " INTEGER NOT NULL, "
                + ExpenseEntry.COLUMN_INFO + " TEXT NOT NULL, "
                + ExpenseEntry.COLUMN_DATE + " INTEGER NOT NULL, "
                + ExpenseEntry.COLUMN_PRICE + " REAL NOT NULL, "

                + "FOREIGN KEY(" + ExpenseEntry.COLUMN_VEHICLE + ") REFERENCES "
                + VehicleEntry.TABLE_NAME + "(" + VehicleEntry._ID + ") );";

        String SQL_CREATE_FILLUPS_TABLE =  "CREATE TABLE " + FillUpEntry.TABLE_NAME + " ("
                + FillUpEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + FillUpEntry.COLUMN_VEHICLE + " INTEGER NOT NULL, "
                + FillUpEntry.COLUMN_DISTANCE_FROM_LAST + " INTEGER, "
                + FillUpEntry.COLUMN_FUEL_VOLUME + " REAL NOT NULL, "
                + FillUpEntry.COLUMN_FUEL_PRICE_PER_LITRE + " REAL NOT NULL, "
                + FillUpEntry.COLUMN_FUEL_PRICE_TOTAL + " REAL NOT NULL, "
                + FillUpEntry.COLUMN_IS_FULL_FILLUP + " INTEGER NOT NULL, "
                + FillUpEntry.COLUMN_FUEL_CONSUMPTION + " INTEGER, "
                + FillUpEntry.COLUMN_DATE + " INTEGER NOT NULL UNIQUE, "
                + FillUpEntry.COLUMN_INFO + " TEXT, "

                + "FOREIGN KEY(" + FillUpEntry.COLUMN_VEHICLE + ") REFERENCES "
                + VehicleEntry.TABLE_NAME + "(" + VehicleEntry._ID + ") );";

        db.execSQL(SQL_CREATE_VEHICLE_TYPES_TABLE);
        db.execSQL(SQL_CREATE_VEHICLES_TABLE);
        db.execSQL(SQL_CREATE_EXPENSES_TABLE);
        db.execSQL(SQL_CREATE_FILLUPS_TABLE);
        loadSampleData(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    private void loadSampleData(SQLiteDatabase db) {
        List<String> types = Arrays.asList("Sedan", "Hatchback", "Combi", "Van", "Motocycle", "Pickup", "Quad", "Sport", "SUV", "Coupe");

        // initialize types
        for (String typeName : types) {
            db.execSQL("INSERT INTO "+ VehicleTypeEntry.TABLE_NAME +" ('name') VALUES ('" + typeName + "');");
        }

        // initialize Vehicle
        db.execSQL("INSERT INTO " + VehicleEntry.TABLE_NAME + "('name','type','currency','volume_unit') VALUES ('My Loved Car',6,'EUR','LITRE');");
    }

}
