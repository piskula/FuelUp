package sk.piskula.fuelup.business;

import android.content.Context;
import android.util.Log;

import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;

import sk.piskula.fuelup.data.DatabaseProvider;
import sk.piskula.fuelup.entity.Expense;
import sk.piskula.fuelup.entity.Vehicle;

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

    /**
     * Creates car with default values
     *
     * @param expense
     * @return success result
     */
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
}
