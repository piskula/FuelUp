package sk.piskula.fuelup.loaders;

/**
 * Created by Martin Styk on 20.06.2017.
 */

import android.content.Context;

import java.util.List;

import sk.piskula.fuelup.business.ExpenseService;
import sk.piskula.fuelup.entity.Expense;


/**
 * Loader async task for Expenses
 * <p>
 * Created by Martin Styk on 15.06.2017.
 */
public class ExpenseLoader extends FuelUpAbstractAsyncLoader<List<Expense>> {
    private static final String TAG = ExpenseLoader.class.getSimpleName();
    public static final int ID = 2;

    private ExpenseService expenseService;
    private long vehicleId;

    public ExpenseLoader(Context context, long vehicleId, ExpenseService expenseService) {
        super(context);
        this.vehicleId = vehicleId;
        this.expenseService = expenseService;
    }

    /**
     * This is where the bulk of our work is done.  This function is
     * called in a background thread and should generate a new set of
     * data to be published by the loader.
     */
    @Override
    public List<Expense> loadInBackground() {
        return expenseService.findExpensesOfVehicle(vehicleId);
    }
}


