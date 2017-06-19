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
        for (String type : types) {
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

        Vehicle mercedes = createCar("Sprinterik", "Mercedes", types.get(3), DistanceUnit.km, 227880L);
        Vehicle civic = createCar("Civic", "Honda", types.get(1), DistanceUnit.mi, 142000L);


        // create sample fill ups
        for (int i = 1; i < 10; i++) {
            createFillUp(mercedes, i * 20, 15.1 * i, true, i, 2017, i, i + 1);
            createFillUp(civic, i * 10, 5 * i, true, i, 2016, i, i + 10);
        }

        // create sample expenses
        for (int i = 1; i < 11; i++) {
            createExpense(mercedes, 2017, i, i + 10, "My Sprinter expense " + i, i * 15.5);
            createExpense(civic, 2016, i, i , "My Civic expense " + i, i * 5.5);
        }
    }

    private VehicleType vehicleType(String name) {
        VehicleType vehicleType = new VehicleType();
        vehicleType.setName(name);
        return vehicleType;
    }


    private Vehicle createCar(String name, String maker, VehicleType type, DistanceUnit unit, long mileage) {
        Vehicle car = new Vehicle();
        car.setName(name);
        car.setVehicleMaker(maker);
        car.setType(type);
        car.setUnit(unit);
        car.setStartMileage(mileage);

        try {
            getVehicleDao().create(car);
        } catch (SQLException e) {
            Log.e(TAG, "Unable to create sample Vehicle " + car.getName(), e);
        }
        return car;
    }

    private void createFillUp(Vehicle vehicle, long distanceFromLast, double fuelVolume, boolean isFull, double pricePerLitre,
                              int year, int month, int day) {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month, day);

        FillUp fillUp = new FillUp();
        fillUp.setVehicle(vehicle);
        fillUp.setDate(cal.getTime());
        fillUp.setDistanceFromLastFillUp(distanceFromLast);
        fillUp.setFuelVolume(fuelVolume);
        fillUp.setFullFillUp(isFull);
        fillUp.setFuelPricePerLitre(BigDecimal.valueOf(pricePerLitre));
        fillUp.setFuelPriceTotal(BigDecimal.valueOf(fillUp.getFuelVolume() * pricePerLitre));

        try {
            getFillUpDao().create(fillUp);
        } catch (SQLException e) {
            Log.e(TAG, "Unable to create sample FillUps.", e);
        }
    }

    private void createExpense(Vehicle vehicle, int year, int month, int day, String info, double price) {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month, day);

        Expense expense = new Expense();
        expense.setVehicle(vehicle);
        expense.setDate(cal.getTime());
        expense.setInfo(info);
        expense.setPrice(BigDecimal.valueOf(price));

        try {
            getExpenseDao().create(expense);
        } catch (SQLException e) {
            Log.e(TAG, "Unable to create sample Expenses.", e);
        }
    }
}
