package sk.piskula.fuelup.screens.edit;

import android.app.DatePickerDialog;
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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.j256.ormlite.android.apptools.OpenHelperManager;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import sk.piskula.fuelup.R;
import sk.piskula.fuelup.data.DatabaseHelper;
import sk.piskula.fuelup.entity.Expense;
import sk.piskula.fuelup.entity.Vehicle;
import sk.piskula.fuelup.screens.VehicleTabbedDetail;
import sk.piskula.fuelup.screens.detailfragments.ExpensesListFragment;

/**
 * @author Ondrej Oravcok
 * @version 18.6.2017
 */
public class EditExpense extends AppCompatActivity implements OnClickListener {

    private static final String TAG = "AddExpenseActivity";

    private Bundle savedInstanceState;

    private EditText mTxtInfo;
    private EditText mTxtPrice;
    private TextView mTxtDate;
    private TextView mTxtPriceUnit;
    private RelativeLayout mLayDate;

    private Button mBtnAdd;

    private Vehicle selectedVehicle;
    private Expense selectedExpense;
    private Mode mode;

    private DatabaseHelper databaseHelper = null;

    private DatePickerDialog datePickerDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.savedInstanceState = savedInstanceState;
        setContentView(R.layout.edit_expense);

        Intent intent = getIntent();

        selectedExpense = (Expense) intent.getSerializableExtra(ExpensesListFragment.EXPENSE_TO_EDIT);
        //getInstanceMode();
        if (selectedExpense != null) {
            selectedVehicle = selectedExpense.getVehicle();
            mode = Mode.UPDATING;
        } else {
            selectedVehicle = (Vehicle) intent.getSerializableExtra(ExpensesListFragment.VEHICLE_FROM_FRAGMENT_TO_EDIT_EXPENSE);
            mode = Mode.CREATING;
        }

        //DB

        initViews();
        initActionBar();

//        mTxtPriceUnit.setText(selectedVehicle.getCurrencyFormatted());
        //TODO currency formatted
        mTxtPriceUnit.setText("USD");
        if (mode == Mode.UPDATING && savedInstanceState == null) {
            populateFields();
        }
    }

    private void initViews() {
        this.mTxtInfo = (EditText) findViewById(R.id.txt_addexpense_information);
        this.mTxtPrice = (EditText) findViewById(R.id.txt_addexpense_price);
        this.mTxtDate = (TextView) findViewById(R.id.txt_addexpense_date);
        this.mBtnAdd = (Button) findViewById(R.id.btn_addexpense_add);
        this.mLayDate = (RelativeLayout) findViewById(R.id.rel_addexpense_lay_date);
        this.mTxtPriceUnit = (TextView) findViewById(R.id.txt_addexpense_priceunit);

        this.mBtnAdd.setOnClickListener(this);
        setDateTimeField();
    }

    private void setDateTimeField() {
        mLayDate.setOnClickListener(this);

        Calendar serviceDate = Calendar.getInstance();
        if (mode == Mode.CREATING && savedInstanceState == null) {
            mTxtDate.setText(android.text.format.DateFormat.getDateFormat(getApplicationContext()).format(Calendar.getInstance().getTime()));
        }
        datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {

            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar serviceDate = Calendar.getInstance();
                if (mode == Mode.UPDATING) {
                    serviceDate.setTimeInMillis(selectedExpense.getDate().getTime());
                }
                serviceDate.set(year, monthOfYear, dayOfMonth);
                mTxtDate.setText(android.text.format.DateFormat.getDateFormat(getApplicationContext()).format(serviceDate.getTime()));
            }
        }, serviceDate.get(Calendar.YEAR), serviceDate.get(Calendar.MONTH), serviceDate.get(Calendar.DAY_OF_MONTH));
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

    private void populateFields() {
        if (selectedExpense != null) {
            mTxtInfo.setText(selectedExpense.getInfo());
            mTxtPrice.setText(selectedExpense.getPrice().toString());
            mTxtDate.setText(android.text.format.DateFormat.getDateFormat(getApplicationContext()).format(selectedExpense.getDate().getTime()));
            mBtnAdd.setText(getString(R.string.addExpenseActivity_btnTxt_update));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (databaseHelper != null) {
            OpenHelperManager.releaseHelper();
            databaseHelper = null;
        }
    }

//    @Override
//    public Intent getParentActivityIntent() {
//        Intent intent = new Intent(this, VehicleTabbedDetail.class);
//        intent.putExtra(VehicleTabbedDetail.VEHICLE_TO_FRAGMENT, selectedVehicle);
//        return intent;
//    }

    private void getInstanceMode() {
        if (selectedExpense != null) {
            mode = Mode.UPDATING;
        } else {
            mode = Mode.CREATING;
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_addexpense_add:

                Editable info = mTxtInfo.getText();
                Editable price = mTxtPrice.getText();
                String date = mTxtDate.getText().toString();

                if (!TextUtils.isEmpty(info) && !TextUtils.isEmpty(price) && !TextUtils.isEmpty(date)) {

                    //Toast.makeText(this, "AddExpenseActivity - button click", Toast.LENGTH_LONG).show();	//TO DO delete
                    String msg = null;
                    BigDecimal createdPrice = null;
                    Date createdDate = null;
                    try {
                        createdPrice = new BigDecimal(price.toString());
                    } catch (NumberFormatException ex) {
                        Log.d(TAG, "tried bad number format", ex);
                        msg = "price";
                    }
                    try {
                        DateFormat dateFormatter = android.text.format.DateFormat.getDateFormat(getApplicationContext());
                        createdDate = dateFormatter.parse(date);
                    } catch (ParseException ex) {
                        Log.d(TAG, "tried bad date", ex);
                        msg = "date";
                    }
                    if (msg == null) {
                        String errorMsg;
                        if (mode == Mode.CREATING) {
                            Expense createdExpense = new Expense();
                            createdExpense.setDate(createdDate);
                            createdExpense.setInfo(info.toString());
                            createdExpense.setPrice(createdPrice);
                            createdExpense.setVehicle(selectedVehicle);

                            try {
                                getHelper().getExpenseDao().create(createdExpense);
                                Toast.makeText(this, R.string.addExpense_Toast_createdSuccessfully, Toast.LENGTH_LONG).show();
                            } catch (SQLException e) {
                                errorMsg = "Error occured while saving new Expense to DB.";
                                Log.e(TAG, errorMsg, e);
                                Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
                            }
                            finish();
                        } else {
                            selectedExpense.setDate(createdDate);
                            selectedExpense.setInfo(info.toString());
                            selectedExpense.setPrice(createdPrice);

                            //TODO update expense
//                            mExpenseManager.updateExpense(mSelectedExpense);
                            try {
                                getHelper().getExpenseDao().update(selectedExpense);
                                Toast.makeText(this, R.string.addExpense_Toast_updatedSuccessfully, Toast.LENGTH_LONG).show();
                            } catch (SQLException e) {
                                errorMsg = "Error occured while saving updated Expense to DB.";
                                Log.e(TAG, errorMsg, e);
                                Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
                            }
                            finish();
                        }
                    }
                } else {
                    Toast.makeText(this, R.string.addExpenseActivity_emptyFields,
                            Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.rel_addexpense_lay_date:
                datePickerDialog.show();
                break;
            default:
                break;
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
