package sk.momosi.fuelup.screens.edit;

import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Calendar;

import sk.momosi.fuelup.R;
import sk.momosi.fuelup.business.ExpenseService;
import sk.momosi.fuelup.business.googledrive.syncing.SyncAdapterContentObserver;
import sk.momosi.fuelup.data.FuelUpContract;
import sk.momosi.fuelup.data.FuelUpContract.ExpenseEntry;
import sk.momosi.fuelup.entity.Expense;
import sk.momosi.fuelup.entity.Vehicle;
import sk.momosi.fuelup.entity.util.DateUtil;
import sk.momosi.fuelup.screens.detailfragments.ExpensesListFragment;
import sk.momosi.fuelup.screens.dialog.DeleteDialog;

/**
 * @author Ondrej Oravcok
 * @version 18.6.2017
 */
public class EditExpenseActivity extends AppCompatActivity implements DeleteDialog.Callback {

    private static final String TAG = EditExpenseActivity.class.getSimpleName();

    private EditText mTxtInfo;
    private EditText mTxtPrice;
    private EditText mTxtDate;
    private TextView mTxtPriceUnit;

    private Button mBtnAdd;

    private Vehicle mVehicle;
    private Expense expense;
    private Calendar expenseDate;
    private Mode mode;

    private SyncAdapterContentObserver mObserver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense_edit);

        Intent intent = getIntent();
        long expenseId = intent.getLongExtra(ExpensesListFragment.EXPENSE_ID_TO_EDIT, 0);
        expense = ExpenseService.getExpenseById(expenseId, getApplicationContext());
        mode = expense == null ? Mode.CREATING : Mode.UPDATING;

        initViews();

        if (mode == Mode.UPDATING) {
            mVehicle = expense.getVehicle();
            populateFields(expense);
        } else if (mode == Mode.CREATING) {
            mVehicle = intent.getParcelableExtra(ExpensesListFragment.VEHICLE_FROM_FRAGMENT_TO_EDIT_EXPENSE);
            setExpenseDate(Calendar.getInstance());
        }

        mTxtPriceUnit.setText(mVehicle.getCurrencySymbol());
    }

    private void initViews() {
        this.mTxtInfo = findViewById(R.id.txt_addexpense_information);
        this.mTxtPrice = findViewById(R.id.txt_addexpense_price);
        this.mTxtDate = findViewById(R.id.txt_addexpense_date);
        this.mBtnAdd = findViewById(R.id.btn_addexpense_add);
        this.mTxtPriceUnit = findViewById(R.id.txt_addexpense_priceunit);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            if (mode == Mode.UPDATING) {
                actionBar.setTitle(R.string.editExpense_updateExpense);
            } else {
                actionBar.setTitle(R.string.editExpense_addExpense);

            }
        }
    }

    private void populateFields(Expense selectedExpense) {
        if (selectedExpense != null) {
            mTxtInfo.setText(selectedExpense.getInfo());
            mTxtPrice.setText(getPrice(selectedExpense.getPrice()));
            setExpenseDate(DateUtil.transformToCal(selectedExpense.getDate()));
            mBtnAdd.setText(getString(R.string.update));
        }
    }

    /**
     * On click listener for add button
     *
     * @param view
     */
    public void onClickAdd(View view) {
        saveExpense();
    }


    /**
     * OnClick listener for date text view
     */
    public void onClickDatePicker(View view) {
        new DatePickerDialog(EditExpenseActivity.this, new OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int y, int m, int d) {
                Calendar serviceDate = Calendar.getInstance();
                serviceDate.set(y, m, d);
                setExpenseDate(serviceDate);
            }
        }, expenseDate.get(Calendar.YEAR), expenseDate.get(Calendar.MONTH), expenseDate.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void saveExpense() {
        String info = mTxtInfo.getText().toString();
        String price = mTxtPrice.getText().toString();
        String date = mTxtDate.getText().toString();

        if (info.isEmpty() || price.isEmpty() || date.isEmpty()) {
            Snackbar.make(findViewById(android.R.id.content), R.string.toast_emptyFields, Snackbar.LENGTH_LONG).show();
            return;
        }

        BigDecimal createdPrice = null;

        try {
            createdPrice = new BigDecimal(price);
        } catch (NumberFormatException e) {
            Log.e(TAG, "Error formatting price for Expense while saving.");
            Snackbar.make(findViewById(android.R.id.content), R.string.updateCarActivity_wrong_number_format, Snackbar.LENGTH_SHORT).show();
            return;
        }

        ContentValues contentValues = new ContentValues();
        contentValues.put(ExpenseEntry.COLUMN_INFO, info);
        contentValues.put(ExpenseEntry.COLUMN_DATE, expenseDate.getTime().getTime());
        contentValues.put(ExpenseEntry.COLUMN_PRICE, createdPrice.doubleValue());
        contentValues.put(ExpenseEntry.COLUMN_VEHICLE, mVehicle.getId());

        if (mode == Mode.UPDATING) {
            if (getContentResolver().update(ContentUris.withAppendedId(ExpenseEntry.CONTENT_URI, expense.getId()), contentValues, null, null) == 1) {
                Toast.makeText(getApplicationContext(), R.string.editExpense_toast_updatedSuccessfully, Toast.LENGTH_LONG).show();
                setResult(RESULT_OK);
            } else {
                Toast.makeText(getApplicationContext(), R.string.editExpense_toast_updateFailed, Toast.LENGTH_LONG).show();
                setResult(RESULT_CANCELED);
            }
        } else {
            if (getContentResolver().insert(ExpenseEntry.CONTENT_URI, contentValues) == null) {
                Toast.makeText(EditExpenseActivity.this, R.string.editExpense_toast_createFailed, Toast.LENGTH_LONG).show();
                setResult(RESULT_CANCELED);
            } else {
                Toast.makeText(EditExpenseActivity.this, R.string.editExpense_toast_createdSuccessfully, Toast.LENGTH_LONG).show();
                setResult(RESULT_OK);
            }
        }
        finish();
    }

    private void setExpenseDate(Calendar calendar) {
        this.expenseDate = calendar;
        mTxtDate.setText(DateUtil.getDateLocalized(calendar));
    }

    private String getPrice(BigDecimal price) {
        DecimalFormat bddf = new DecimalFormat();
        bddf.setGroupingUsed(false);
        return bddf.format(price.doubleValue());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (mode == Mode.UPDATING)
            getMenuInflater().inflate(R.menu.expense_edit, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.btn_delete_expense:
                DeleteDialog.newInstance(getString(R.string.remove_expense_dialog_title),
                        getString(R.string.remove_expense_dialog_message), R.drawable.tow)
                        .show(getSupportFragmentManager(), DeleteDialog.class.getSimpleName());
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDeleteDialogPositiveClick(DeleteDialog dialog) {
        final int result = getContentResolver().delete(
                ContentUris.withAppendedId(ExpenseEntry.CONTENT_URI, expense.getId()), null, null);

        if (result != -1) {
            Toast.makeText(getApplicationContext(), getString(R.string.remove_expense_success), Toast.LENGTH_LONG).show();
            setResult(RESULT_OK);
        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.remove_expense_fail), Toast.LENGTH_LONG).show();
            setResult(RESULT_CANCELED);
        }

        dialog.dismiss();
        finish();
    }

    @Override
    public void onDeleteDialogNegativeClick(DeleteDialog dialog) {
        dialog.dismiss();
    }

    @Override
    public void onResume () {
        super.onResume();
        if (mObserver == null)
            mObserver = new SyncAdapterContentObserver(new Handler());
        getContentResolver().registerContentObserver(FuelUpContract.ExpenseEntry.CONTENT_URI, true, mObserver);
    }

    @Override
    public void onPause () {
        super.onPause();
        getContentResolver().unregisterContentObserver(mObserver);
    }

    private enum Mode {
        UPDATING, CREATING
    }
}
