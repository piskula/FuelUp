package sk.piskula.fuelup.loaders;

/**
 * Created by Martin Styk on 20.06.2017.
 */

import android.content.Context;
import android.util.Log;

import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import sk.piskula.fuelup.entity.Expense;


/**
 * Loader async task for Expenses
 * <p>
 * Created by Martin Styk on 15.06.2017.
 */
public class ExpenseLoader extends FuelUpAbstractAsyncLoader<List<Expense>> {
    private static final String TAG = ExpenseLoader.class.getSimpleName();
    public static final int ID = 2;

    private Dao<Expense, Long> dao;
    private long vehicleId;

    public ExpenseLoader(Context context, long vehicleId, Dao<Expense, Long> dao) {
        super(context);
        this.vehicleId = vehicleId;
        this.dao = dao;
    }

    /**
     * This is where the bulk of our work is done.  This function is
     * called in a background thread and should generate a new set of
     * data to be published by the loader.
     */
    @Override
    public List<Expense> loadInBackground() {
        try {
            return dao.queryBuilder().where().eq("vehicle_id", vehicleId).query();
        } catch (SQLException e) {
            Log.e(TAG, "Error getting expenses from DB for vehicle id " + vehicleId, e);
            return new ArrayList<>();
        }
    }
}


