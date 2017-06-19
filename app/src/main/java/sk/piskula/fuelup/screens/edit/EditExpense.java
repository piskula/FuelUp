package sk.piskula.fuelup.screens.edit;

import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import com.j256.ormlite.android.apptools.OpenHelperManager;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import sk.piskula.fuelup.R;
import sk.piskula.fuelup.data.DatabaseHelper;
import sk.piskula.fuelup.entity.Expense;
import sk.piskula.fuelup.entity.Vehicle;
import sk.piskula.fuelup.screens.detailfragments.ExpensesListFragment;

/**
 * @author Ondrej Oravcok
 * @version 18.6.2017
 */
public class EditExpense extends AppCompatActivity {

    private static final String TAG = "EditExpense";

//    private Bundle savedInstanceState;

    private EditText mTxtInfo;
    private EditText mTxtPrice;
    private EditText mTxtDate;
//    private TextView mTxtPriceUnit;

    private Button mBtnAdd;

    private Vehicle vehicle;
    private Expense expense;
    private Calendar expenseDate = Calendar.getInstance();
    private Mode mode;

    private DatabaseHelper databaseHelper = null;

//    private DatePickerDialog datePickerDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.edit_expense);
        initViews();

        Intent intent = getIntent();
        expense = (Expense) intent.getSerializableExtra(ExpensesListFragment.EXPENSE_TO_EDIT);

        if (expense != null) {
            mode = Mode.UPDATING;
            vehicle = expense.getVehicle();
            populateFields(expense);
        } else {
            mode = Mode.CREATING;
            vehicle = (Vehicle) intent.getSerializableExtra(ExpensesListFragment.VEHICLE_FROM_FRAGMENT_TO_EDIT_EXPENSE);
            expense = new Expense();
            expense.setVehicle(vehicle);
        }

        initActionBar();
        mTxtDate.setOnClickListener(getDatePickerListener());
        mBtnAdd.setOnClickListener(getSaveButtonOnClickListener());
        //TODO currency formatted
        // mTxtPriceUnit.setText("USD");
    }

    private void initViews() {
        this.mTxtInfo = (EditText) findViewById(R.id.txt_addexpense_information);
        this.mTxtPrice = (EditText) findViewById(R.id.txt_addexpense_price);
        this.mTxtDate = (EditText) findViewById(R.id.txt_addexpense_date);
        this.mBtnAdd = (Button) findViewById(R.id.btn_addexpense_add);
        //TODO currency formatted
        // this.mTxtPriceUnit = (TextView) findViewById(R.id.txt_addexpense_priceunit);
    }

    private void initActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            if (mode == Mode.UPDATING) {
                actionBar.setTitle("Update expense");
            } else {
                actionBar.setTitle("Create expense");
            }
        }
    }

    private void populateFields(Expense selectedExpense) {
        if (selectedExpense != null) {
            mTxtInfo.setText(selectedExpense.getInfo());
            mTxtPrice.setText(getPrice(selectedExpense.getPrice()));
            mTxtDate.setText(android.text.format.DateFormat.getDateFormat(getApplicationContext()).format(selectedExpense.getDate().getTime()));
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(selectedExpense.getDate().getTime());
            setExpenseDate(cal);
            mBtnAdd.setText(getString(R.string.addExpenseActivity_btnTxt_update));
        }
    }

    private void setExpenseDate(Calendar calendar) {
        this.expenseDate = calendar;
        mTxtDate.setText(android.text.format.DateFormat.getDateFormat(getApplicationContext()).format(calendar.getTime()));
    }

    private String getPrice(BigDecimal price) {
        DecimalFormat bddf = new DecimalFormat();
        bddf.setGroupingUsed(false);
//        bddf.setMinimumFractionDigits(0);
//        bddf.setMinimumFractionDigits(2);
        return bddf.format(price.doubleValue());
    }

    private OnClickListener getDatePickerListener() {
        return new OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(EditExpense.this, new OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int y, int m, int d) {
                        Calendar serviceDate = Calendar.getInstance();
                        serviceDate.set(y, m, d);
                        setExpenseDate(serviceDate);
                    }
                }, expenseDate.get(Calendar.YEAR),
                        expenseDate.get(Calendar.MONTH),
                        expenseDate.get(Calendar.DAY_OF_MONTH)).show();
            }
        };
    }

    private OnClickListener getSaveButtonOnClickListener() {
        return new OnClickListener() {
            @Override
            public void onClick(View view) {
                String info = mTxtInfo.getText().toString();
                String price = mTxtPrice.getText().toString();
                String date = mTxtDate.getText().toString();

                if (info.isEmpty() || price.isEmpty() || date.isEmpty()) {
                    Toast.makeText(EditExpense.this, R.string.addExpenseActivity_emptyFields,
                            Toast.LENGTH_LONG).show();
                    return;
                }

                BigDecimal createdPrice = null;
                Date createdDate = null;

                try {
                    createdPrice = new BigDecimal(price.toString());
                    createdDate = getDateFormatter().parse(date);
                } catch (NumberFormatException | ParseException e) {
                    Log.e(TAG, "Error formatting data for Expense while saving.");
                    Toast.makeText(EditExpense.this, "ERROR: Bad format of input data, see logs.",
                            Toast.LENGTH_LONG).show();
                }

                expense.setDate(createdDate);
                expense.setInfo(info);
                expense.setPrice(createdPrice);

                try {
                    getHelper().getExpenseDao().createOrUpdate(expense);
                    Toast.makeText(EditExpense.this, mode == Mode.UPDATING
                                    ? R.string.addExpense_Toast_updatedSuccessfully
                                    : R.string.addExpense_Toast_createdSuccessfully,
                            Toast.LENGTH_LONG).show();
                } catch (SQLException e) {
                    String errorMsg = "Error occured while saving Expense to DB.";
                    Log.e(TAG, errorMsg, e);
                    Toast.makeText(EditExpense.this, errorMsg, Toast.LENGTH_LONG).show();
                }
                finish();
            }
        };
    }

    private DateFormat getDateFormatter() {
        return android.text.format.DateFormat.getDateFormat(getApplicationContext());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (databaseHelper != null) {
            OpenHelperManager.releaseHelper();
            databaseHelper = null;
        }
    }

    enum Mode {
        UPDATING, CREATING
    }

    private DatabaseHelper getHelper() {
        if (databaseHelper == null) {
            databaseHelper = OpenHelperManager.getHelper(this,DatabaseHelper.class);
        }
        return databaseHelper;
    }
}
