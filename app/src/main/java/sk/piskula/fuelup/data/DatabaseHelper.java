package sk.piskula.fuelup.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Month;
import java.util.Calendar;

import lombok.extern.slf4j.Slf4j;
import sk.piskula.fuelup.R;
import sk.piskula.fuelup.entity.FillUp;
import sk.piskula.fuelup.entity.Vehicle;
import sk.piskula.fuelup.entity.enums.DistanceUnit;
import sk.piskula.fuelup.entity.enums.VehicleType;

/**
 * @author Ondrej Oravcok
 * @version 16.6.2017.
 */
@Slf4j
public class DatabaseHelper extends OrmLiteSqliteOpenHelper {

    private static final String DATABASE_NAME = "fuelup.db";
    private static final int DATABASE_VERSION = 1;

    private Dao<Vehicle, Integer> vehicleDao;
    private Dao<FillUp, Integer> fillUpDao;

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

        } catch (SQLException e) {
            log.error("Unable to create databases.", e);
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
            onCreate(sqliteDatabase, connectionSource);

        } catch (SQLException e) {
            log.error("Unable to upgrade database from version " + oldVer + " to new " + newVer, e);
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

    private void initSamlpeData() {
        Vehicle mercedes = new Vehicle();
        mercedes.setName("Sprinterik");
        mercedes.setVehicleMaker("Mercedes");
        mercedes.setType(VehicleType.VAN);
        mercedes.setUnit(DistanceUnit.KM);
        mercedes.setStartMileage(227880L);

        try {
            getVehicleDao().create(mercedes);
        } catch (SQLException e) {
            log.error("Unable to create sample Vehicle " + mercedes.getName(), e);
        }

        Calendar cal = Calendar.getInstance();
        cal.set(2017, Calendar.JUNE, 17);

        FillUp fillUp1 = new FillUp();
        fillUp1.setDate(cal);
        fillUp1.setDistanceFromLastFillUp(350L);
        fillUp1.setFuelVolume(29.4d);
        fillUp1.setFullFillUp(true);
        fillUp1.setFuelPricePerLitre(BigDecimal.valueOf(1.176));

        Calendar cal2 = Calendar.getInstance();
        cal2.set(2017, Calendar.JUNE, 17);

        FillUp fillUp2 = new FillUp();
        fillUp2.setDate(cal2);
        fillUp2.setDistanceFromLastFillUp(450L);
        fillUp2.setFuelVolume(41.0d);
        fillUp2.setFullFillUp(true);
        fillUp2.setFuelPricePerLitre(BigDecimal.valueOf(1.099));

        try {
            getFillUpDao().create(fillUp1);
            getFillUpDao().create(fillUp2);
        } catch (SQLException e) {
            log.error("Unable to create sample FillUps.", e);
        }
    }
}
