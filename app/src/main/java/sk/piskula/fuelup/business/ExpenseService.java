package sk.piskula.fuelup.business;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import sk.piskula.fuelup.data.FuelUpContract;
import sk.piskula.fuelup.data.FuelUpContract.ExpenseEntry;
import sk.piskula.fuelup.entity.Expense;
import sk.piskula.fuelup.entity.Vehicle;

/**
 * @author Martin Styk
 * @version 23.06.2017.
 */
public class ExpenseService {

    private static final String LOG_TAG = ExpenseService.class.getSimpleName();

    public static Expense getExpenseById(long expenseId, Context context) {
        String[] selectionArgs = {String.valueOf(expenseId)};
        Cursor cursor = context.getContentResolver().query(ExpenseEntry.CONTENT_URI,
                FuelUpContract.ALL_COLUMNS_EXPENSES, ExpenseEntry._ID + "=?",
                selectionArgs, null);

        if (cursor == null || cursor.getCount() != 1) {
            Log.e(LOG_TAG, "Cannot get Expense for id=" + expenseId);
            return null;
        }

        cursor.moveToFirst();
        Expense expense = cursorToExpense(cursor, context);
        cursor.close();

        return expense;
    }

    public static List<Expense> getExpensesOfVehicle(long vehicleId, Context context) {
        String[] selectionArgs = {String.valueOf(vehicleId)};

        Cursor cursor = context.getContentResolver()
                .query(FuelUpContract.ExpenseEntry.CONTENT_URI,
                        FuelUpContract.ALL_COLUMNS_EXPENSES,
                        FuelUpContract.ExpenseEntry.COLUMN_VEHICLE + "=?",
                        selectionArgs,
                        FuelUpContract.ExpenseEntry.COLUMN_DATE + " DESC");

        List<Expense> expenses = new ArrayList<>(cursor.getCount());

        while (cursor.moveToNext()) {
            Expense expense = cursorToExpense(cursor, context);
            expenses.add(expense);
        }

        cursor.close();

        return expenses;
    }

    private static Expense cursorToExpense(Cursor cursor, Context context) {
        long vehicleId = cursor.getLong(cursor.getColumnIndexOrThrow(ExpenseEntry.COLUMN_VEHICLE));
        Vehicle vehicle = VehicleService.getVehicleById(vehicleId, context);

        Expense expense = new Expense();
        expense.setId(cursor.getLong(cursor.getColumnIndexOrThrow(ExpenseEntry._ID)));
        expense.setInfo(cursor.getString(cursor.getColumnIndexOrThrow(ExpenseEntry.COLUMN_INFO)));
        expense.setPrice(BigDecimal.valueOf(cursor.getDouble(cursor.getColumnIndexOrThrow(ExpenseEntry.COLUMN_PRICE))));
        expense.setDate(new Date(cursor.getLong(cursor.getColumnIndexOrThrow(ExpenseEntry.COLUMN_DATE))));
        expense.setVehicle(vehicle);

        return expense;
    }

}
