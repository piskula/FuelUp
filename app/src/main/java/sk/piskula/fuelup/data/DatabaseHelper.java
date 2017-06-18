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
        } catch (SQLException e) {
            Log.e(TAG, "Unable to create databases.", e);
        }
        initSamlpeData();
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

    private List<VehicleType> initVehicleTypes() {
        List<String> types = Arrays.asList("Sedan", "Hatchback", "Combi", "Van", "Motocycle", "Pickup", "Quad", "Sport", "SUV", "Coupe");
        List<VehicleType> result = new ArrayList<>();
        for(String type : types) {
            try {
                getVehicleTypeDao().create(vehicleType(type));
                result.add(getVehicleTypeDao().queryBuilder().where().eq("name", type).query().get(0));
            } catch (SQLException e) {
                Log.e(TAG, "Unable to create sample VehicleType " + type, e);
            }
        }
        return result;
    }

    private void initSamlpeData() {
        List<VehicleType> types = initVehicleTypes();

        Vehicle mercedes = new Vehicle();
        mercedes.setName("Sprinterik");
        mercedes.setVehicleMaker("Mercedes");
        mercedes.setType(types.get(3));
        mercedes.setUnit(DistanceUnit.km);
        mercedes.setStartMileage(227880L);

        try {
            getVehicleDao().create(mercedes);
        } catch (SQLException e) {
            Log.e(TAG, "Unable to create sample Vehicle " + mercedes.getName(), e);
        }

        Calendar cal = Calendar.getInstance();
        cal.set(2017, Calendar.JUNE, 17);

        FillUp fillUp1 = new FillUp();
        fillUp1.setVehicle(mercedes);
        fillUp1.setDate(cal.getTime());
        fillUp1.setDistanceFromLastFillUp(350L);
        fillUp1.setFuelVolume(29.4d);
        fillUp1.setFullFillUp(true);
        fillUp1.setFuelPricePerLitre(BigDecimal.valueOf(1.176));
        fillUp1.setFuelPriceTotal(BigDecimal.valueOf(fillUp1.getFuelVolume() * 1.176));

        Calendar cal2 = Calendar.getInstance();
        cal2.set(2017, Calendar.JUNE, 16);

        FillUp fillUp2 = new FillUp();
        fillUp2.setVehicle(mercedes);
        fillUp2.setDate(cal2.getTime());
        fillUp2.setDistanceFromLastFillUp(450L);
        fillUp2.setFuelVolume(41.0d);
        fillUp2.setFullFillUp(true);
        fillUp2.setFuelPricePerLitre(BigDecimal.valueOf(1.099));
        fillUp2.setFuelPriceTotal(BigDecimal.valueOf(fillUp2.getFuelVolume() * 1.099));

        try {
            getFillUpDao().create(fillUp1);
            getFillUpDao().create(fillUp2);
        } catch (SQLException e) {
            Log.e(TAG, "Unable to create sample FillUps.", e);
        }

        Calendar cal3 = Calendar.getInstance();
        cal3.set(2017, Calendar.JUNE, 18);

        Expense tyreChange = new Expense();
        tyreChange.setVehicle(mercedes);
        tyreChange.setDate(cal3.getTime());
        tyreChange.setInfo("Tyre change");
        tyreChange.setPrice(BigDecimal.valueOf(40.0d));

        Calendar cal4 = Calendar.getInstance();
        cal4.set(2017, Calendar.JUNE, 19);

        Expense windowClean = new Expense();
        windowClean.setVehicle(mercedes);
        windowClean.setDate(cal4.getTime());
        windowClean.setInfo("window clean");
        windowClean.setPrice(BigDecimal.valueOf(7.0d));


        try {
            getExpenseDao().create(tyreChange);
            getExpenseDao().create(windowClean);
        } catch (SQLException e) {
            Log.e(TAG, "Unable to create sample Expenses.", e);
        }
    }

    private VehicleType vehicleType(String name) {
        VehicleType vehicleType = new VehicleType();
        vehicleType.setName(name);
        return vehicleType;
    }
}
