package sk.piskula.fuelup.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import sk.piskula.fuelup.R;
import sk.piskula.fuelup.entity.Expense;
import sk.piskula.fuelup.entity.FillUp;
import sk.piskula.fuelup.entity.Vehicle;
import sk.piskula.fuelup.entity.VehicleType;
import sk.piskula.fuelup.entity.enums.DistanceUnit;

/**
 * @author Ondrej Oravcok
 * @version 16.6.2017.
 */
public class DatabaseHelper extends OrmLiteSqliteOpenHelper {

    private static final String TAG = "DatabaseHelper";

    private static final String DATABASE_NAME = "fuelup.db";
    private static final int DATABASE_VERSION = 1;

    private Dao<Vehicle, Integer> vehicleDao;
    private Dao<FillUp, Integer> fillUpDao;
    private Dao<Expense, Integer> expenseDao;
    private Dao<VehicleType, Integer> vehicleTypeDao;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION, R.raw.ormlite_config);
    }

    @Override
    public void onCreate(SQLiteDatabase sqliteDatabase, ConnectionSource connectionSource) {
        try {

            // Create tables. This onCreate() method will be invoked only once
            // of the application life time i.e. the first time when the application starts.
            TableUtils.createTable(connectionSource, Vehicle.class);
            TableUtils.createTable(connectionSource, FillUp.class);
            TableUtils.createTable(connectionSource, Expense.class);
            TableUtils.createTable(connectionSource, VehicleType.class);
            Log.i(TAG, "Tables created successfully.");
        } catch (SQLException e) {
            Log.e(TAG, "Unable to create databases.", e);
        }

        try {
            initSamlpeData();
            Log.i(TAG, "Sample data initialized.");
        } catch (SQLException e) {
            Log.e(TAG, "Unable to create sample data.", e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqliteDatabase, ConnectionSource connectionSource, int oldVer, int newVer) {
        try {

            // In case of change in database of next version of application,
            // please increase the value of DATABASE_VERSION variable, then this method will
            // be invoked automatically. Developer needs to handle the upgrade logic here, i.e.
            // create a new table or a new column to an existing table, take the backups of the
            // existing database etc.

            TableUtils.dropTable(connectionSource, Vehicle.class, true);
            TableUtils.dropTable(connectionSource, FillUp.class, true);
            TableUtils.dropTable(connectionSource, Expense.class, true);
            TableUtils.dropTable(connectionSource, VehicleType.class, true);
            onCreate(sqliteDatabase, connectionSource);

        } catch (SQLException e) {
            Log.e(TAG, "Unable to upgrade database from version " + oldVer + " to new " + newVer, e);
        }
    }

    public Dao<Vehicle, Integer> getVehicleDao() throws SQLException {
        if (vehicleDao == null) {
            vehicleDao = getDao(Vehicle.class);
        }
        return vehicleDao;
    }

    public Dao<FillUp, Integer> getFillUpDao() throws SQLException {
        if (fillUpDao == null) {
            fillUpDao = getDao(FillUp.class);
        }
        return fillUpDao;
    }

    public Dao<Expense, Integer> getExpenseDao() throws SQLException {
        if (expenseDao == null) {
            expenseDao = getDao(Expense.class);
        }
        return expenseDao;
    }

    public Dao<VehicleType, Integer> getVehicleTypeDao() throws SQLException {
        if (vehicleTypeDao == null) {
            vehicleTypeDao = getDao(VehicleType.class);
        }
        return vehicleTypeDao;
    }

    private void initSamlpeData() throws SQLException {
        List<VehicleType> types = SampleDataUtils.addVehicleTypes(getVehicleTypeDao());
        List<Vehicle> vehicles = SampleDataUtils.addVehicles(getVehicleDao(), types);
        SampleDataUtils.addFillUps(getFillUpDao(), vehicles);
        SampleDataUtils.addExpenses(getExpenseDao(), vehicles);
    }
}
