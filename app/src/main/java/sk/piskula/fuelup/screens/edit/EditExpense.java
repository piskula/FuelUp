package sk.piskula.fuelup.screens.edit;

import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.Intent;
import android.os.Bundle;
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
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;

import sk.piskula.fuelup.R;
import sk.piskula.fuelup.business.ExpenseService;
import sk.piskula.fuelup.business.ServiceResult;
import sk.piskula.fuelup.entity.Expense;
import sk.piskula.fuelup.entity.Vehicle;
import sk.piskula.fuelup.screens.detailfragments.ExpensesListFragment;
import sk.piskula.fuelup.screens.dialog.DeleteDialog;

/**
 * @author Ondrej Oravcok
 * @version 18.6.2017
 */
public class EditExpense extends AppCompatActivity implements DeleteDialog.Callback {

    private static final String TAG = EditExpense.class.getSimpleName();

    private EditText mTxtInfo;
    private EditText mTxtPrice;
    private EditText mTxtDate;
    private TextView mTxtPriceUnit;

    private Button mBtnAdd;

    private Vehicle vehicle;
    private Expense expense;
    private Calendar expenseDate;
    private Mode mode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_expense);

        Intent intent = getIntent();
        expense = intent.getParcelableExtra(ExpensesListFragment.EXPENSE_TO_EDIT);
        mode = expense == null ? Mode.CREATING : Mode.UPDATING;

        initViews();

        if (mode == Mode.UPDATING) {
            vehicle = expense.getVehicle();
            populateFields(expense);
        } else if (mode == Mode.CREATING) {
            vehicle = intent.getParcelableExtra(ExpensesListFragment.VEHICLE_FROM_FRAGMENT_TO_EDIT_EXPENSE);
            expense = new Expense();
            setExpenseDate(Calendar.getInstance());
            expense.setVehicle(vehicle);
        }

        mTxtPriceUnit.setText(vehicle.getCurrencySymbol(this));
    }

    private void initViews() {
        this.mTxtInfo = (EditText) findViewById(R.id.txt_addexpense_information);
        this.mTxtPrice = (EditText) findViewById(R.id.txt_addexpense_price);
        this.mTxtDate = (EditText) findViewById(R.id.txt_addexpense_date);
        this.mBtnAdd = (Button) findViewById(R.id.btn_addexpense_add);
        this.mTxtPriceUnit = (TextView) findViewById(R.id.txt_addexpense_priceunit);
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
            setExpenseDate(transformToCal(selectedExpense.getDate()));
            mBtnAdd.setText(getString(R.string.update));
        }
    }

    private Calendar transformToCal(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(date.getTime());
        return cal;
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
        new DatePickerDialog(EditExpense.this, new OnDateSetListener() {
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
        }

        expense.setDate(this.expenseDate.getTime());
        expense.setInfo(info);
        expense.setPrice(createdPrice);

        ExpenseService expenseService = new ExpenseService(EditExpense.this);
        ServiceResult result = (mode == Mode.UPDATING) ? expenseService.update(expense) : expenseService.save(expense);

        if (ServiceResult.SUCCESS.equals(result)) {
            Toast.makeText(EditExpense.this, mode == Mode.UPDATING ? R.string.editExpense_toast_updatedSuccessfully : R.string.editExpense_toast_createdSuccessfully, Toast.LENGTH_LONG).show();
            setResult(RESULT_OK);
        }
        if (ServiceResult.ERROR.equals(result)) {
            Toast.makeText(EditExpense.this, mode == Mode.UPDATING ? R.string.editExpense_toast_updateFailed : R.string.editExpense_toast_createFailed, Toast.LENGTH_LONG).show();
            setResult(RESULT_CANCELED);
        }
        finish();
    }

    private void setExpenseDate(Calendar calendar) {
        this.expenseDate = calendar;
        mTxtDate.setText(DateFormat.getDateInstance(DateFormat.SHORT).format(calendar.getTime()));
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
        ExpenseService service = new ExpenseService(EditExpense.this);
        ServiceResult result = service.delete(expense);

        if (ServiceResult.SUCCESS.equals(result)) {
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

    private enum Mode {
        UPDATING, CREATING
    }
}
