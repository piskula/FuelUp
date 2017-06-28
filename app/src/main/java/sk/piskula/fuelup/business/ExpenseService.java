package sk.piskula.fuelup.business;

import android.content.Context;
import android.util.Log;

import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import sk.piskula.fuelup.data.DatabaseProvider;
import sk.piskula.fuelup.entity.Expense;

/**
 * Created by Martin Styk on 23.06.2017.
 */

public class ExpenseService {

    private static final String TAG = ExpenseService.class.getSimpleName();

    private Context context;
    private Dao<Expense, Long> expensesDao;

    public ExpenseService(Context context) {
        this.context = context;
        this.expensesDao = DatabaseProvider.get(context).getExpenseDao();
    }

    public ServiceResult save(Expense expense) {
        try {
            expensesDao.create(expense);
            Log.i(TAG, "Successfully persisted new Expense: " + expense);
            return ServiceResult.SUCCESS;
        } catch (SQLException e) {
            Log.e(TAG, "Unexpected error. See logs for details.", e);
        }
        return ServiceResult.ERROR;
    }

    public ServiceResult update(Expense expense) {
        try {
            expensesDao.update(expense);
            Log.i(TAG, "Successfully updated Expense: " + expense);
            return ServiceResult.SUCCESS;
        } catch (SQLException e) {
            Log.e(TAG, "Unexpected error. See logs for details.", e);
        }
        return ServiceResult.ERROR;
    }

    public ServiceResult delete(Expense expense) {
        try {
            expensesDao.delete(expense);
            Log.i(TAG, "Successfully delete Expense: " + expense);
            return ServiceResult.SUCCESS;
        } catch (SQLException e) {
            Log.e(TAG, "Unexpected error. See logs for details.", e);
        }
        return ServiceResult.ERROR;
    }

    public List<Expense> findExpensesOfVehicle(long vehicleId) {
        List<Expense> expenses = new ArrayList<>();
        try {
            expenses = expensesDao.queryBuilder().orderBy("date", false).where().eq("vehicle_id", vehicleId).query();
            Log.i(TAG, "Successfully found expenses of vehicle id " + vehicleId);
        } catch (SQLException e) {
            Log.e(TAG, "Unexpected error. See logs for details.", e);
        }
        return expenses;
    }
}
