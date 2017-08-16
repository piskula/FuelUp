package sk.piskula.fuelup.business;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import java.math.BigDecimal;
import java.util.Date;

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
        String[] selectionArgs = { String.valueOf(expenseId) };
        Cursor cursor = context.getContentResolver().query(ExpenseEntry.CONTENT_URI,
                FuelUpContract.ALL_COLUMNS_EXPENSES, ExpenseEntry._ID + "=?",
                selectionArgs, null);

        if (cursor == null || cursor.getCount() != 1) {
            Log.e(LOG_TAG, "Cannot get Expense for id=" + expenseId);
            return null;
        }

        cursor.moveToFirst();
        long vehicleId = cursor.getLong(cursor.getColumnIndexOrThrow(ExpenseEntry.COLUMN_VEHICLE));
        Vehicle vehicle = VehicleService.getVehicleById(vehicleId, context);

        Expense expense = new Expense();
        expense.setId(cursor.getLong(cursor.getColumnIndexOrThrow(ExpenseEntry._ID)));
        expense.setInfo(cursor.getString(cursor.getColumnIndexOrThrow(ExpenseEntry.COLUMN_INFO)));
        expense.setPrice(BigDecimal.valueOf(cursor.getDouble(cursor.getColumnIndexOrThrow(ExpenseEntry.COLUMN_PRICE))));
        expense.setDate(new Date(cursor.getLong(cursor.getColumnIndexOrThrow(ExpenseEntry.COLUMN_DATE))));
        expense.setVehicle(vehicle);

        cursor.close();
        return expense;
    }

    private static ContentValues transformExpense(Expense expense) {
        ContentValues contentValues = new ContentValues();

        contentValues.put(ExpenseEntry.COLUMN_VEHICLE, expense.getVehicle().getId());
        contentValues.put(ExpenseEntry.COLUMN_PRICE, expense.getPrice().doubleValue());
        contentValues.put(ExpenseEntry.COLUMN_DATE, expense.getDate().getTime());
        contentValues.put(ExpenseEntry.COLUMN_INFO, expense.getInfo());

        return contentValues;
    }

    private static ContentValues transformExistingExpense(Expense expense) {
        ContentValues contentValues = transformExpense(expense);

        contentValues.put(ExpenseEntry._ID, expense.getId());

        return contentValues;
    }

    public Cursor findExpensesOfVehicle(long vehicleId, Context context) {
        ContentResolver contentResolver = context.getContentResolver();

        String selection = ExpenseEntry.COLUMN_VEHICLE + "=?";
        String[] selectionArgs = { String.valueOf(vehicleId) };

        return contentResolver.query(ExpenseEntry.CONTENT_URI,
                FuelUpContract.ALL_COLUMNS_EXPENSES,
                selection,
                selectionArgs,
                ExpenseEntry.COLUMN_DATE);
    }
}
