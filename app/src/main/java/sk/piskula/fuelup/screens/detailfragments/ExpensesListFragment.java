package sk.piskula.fuelup.screens.detailfragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.j256.ormlite.android.apptools.OpenHelperManager;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import sk.piskula.fuelup.R;
import sk.piskula.fuelup.adapters.ListExpensesAdapter1;
import sk.piskula.fuelup.data.DatabaseHelper;
import sk.piskula.fuelup.entity.Expense;
import sk.piskula.fuelup.entity.Vehicle;
import sk.piskula.fuelup.screens.VehicleTabbedDetail;
import sk.piskula.fuelup.screens.edit.EditExpense;

/**
 * @author Ondrej Oravcok
 * @version 17.6.2017
 */
public class ExpensesListFragment extends Fragment implements ListExpensesAdapter1.Callback {

    private static final String TAG = "ExpensesListFragment";

    public static final String EXPENSE_TO_EDIT = "expense to edit";
    public static final int REQUEST_CODE_UPDATE_EXPENSE = 31;
    public static final String VEHICLE_FROM_FRAGMENT_TO_EDIT_EXPENSE = "fromFragmentToExpense";

    private Bundle args;
    private Vehicle vehicle;
    private List<Expense> listExpenses;
    private ListExpensesAdapter1 adapter;

    private RecyclerView recyclerView;

    private DatabaseHelper databaseHelper = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        args = getArguments();
        if (args != null) {
            vehicle = (Vehicle) args.getSerializable(VehicleTabbedDetail.VEHICLE_TO_FRAGMENT);
            listExpenses = expensesOfVehicle(vehicle);
        }

        View view = inflater.inflate(R.layout.expenses_list, container, false);

        recyclerView = view.findViewById(R.id.expense_list);
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(),
                ((LinearLayoutManager) recyclerView.getLayoutManager()).getOrientation()));


        adapter = new ListExpensesAdapter1(getActivity(), this, listExpenses);
        recyclerView.setAdapter(adapter);

        return view;
    }

    private List<Expense> expensesOfVehicle(Vehicle vehicle) {
        try {
            return getHelper().getExpenseDao().queryBuilder().where().eq("vehicle_id", vehicle.getId()).query();
        } catch (SQLException e) {
            Log.e(TAG, "Error getting expenses from DB for vehicle " + vehicle, e);
            return new ArrayList<>();
        }
    }

    private void refreshList() {
        if (args != null) {
            vehicle = (Vehicle) args.getSerializable(VehicleTabbedDetail.VEHICLE_TO_FRAGMENT);
        }
        if (vehicle != null) {
            listExpenses = expensesOfVehicle(vehicle);
            adapter.dataChange(listExpenses);
        }
    }

    private DatabaseHelper getHelper() {
        if (databaseHelper == null) {
            databaseHelper = OpenHelperManager.getHelper(getActivity(), DatabaseHelper.class);
        }
        return databaseHelper;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (databaseHelper != null) {
            OpenHelperManager.releaseHelper();
            databaseHelper = null;
        }
    }

    @Override
    public void onResume() {
        refreshList();
        super.onResume();
    }

    @Override
    public void onItemClick(View v, Expense expense, int position) {
        Intent i = new Intent(getActivity(), EditExpense.class);
        i.putExtra(EXPENSE_TO_EDIT, expense);
        startActivityForResult(i, REQUEST_CODE_UPDATE_EXPENSE);
    }

}