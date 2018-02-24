package sk.momosi.fuelup.screens.detailfragments;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import sk.momosi.fuelup.R;
import sk.momosi.fuelup.adapters.ListExpensesAdapter;
import sk.momosi.fuelup.data.FuelUpContract;
import sk.momosi.fuelup.data.FuelUpContract.ExpenseEntry;
import sk.momosi.fuelup.entity.Vehicle;
import sk.momosi.fuelup.screens.VehicleTabbedDetailActivity;
import sk.momosi.fuelup.screens.edit.EditExpenseActivity;

import static android.app.Activity.RESULT_OK;

/**
 * @author Ondrej Oravcok
 * @author Martin Styk
 * @version 16.8.2017
 */
public class ExpensesListFragment extends Fragment implements ListExpensesAdapter.Callback,
        View.OnClickListener, LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = ExpensesListFragment.class.getSimpleName();

    public static final String EXPENSE_ID_TO_EDIT = "expense_to_edit";
    public static final String VEHICLE_FROM_FRAGMENT_TO_EDIT_EXPENSE = "fromFragmentToExpense";

    private static final int EXPENSE_ACTION_REQUEST_CODE = 31;
    private static final int EXPENSE_LOADER_ID = 714;

    private Vehicle vehicle;
    private ListExpensesAdapter adapter;

    private RecyclerView recyclerView;
    private ProgressBar loadingBar;
    private TextView emptyList;

    private FloatingActionButton addButton;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        vehicle = getArguments().getParcelable(VehicleTabbedDetailActivity.VEHICLE_TO_FRAGMENT);
        adapter = new ListExpensesAdapter(this, vehicle);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_expenses_list, container, false);

        CollapsingToolbarLayout appBarLayout = getActivity().findViewById(R.id.toolbar_layout);
        appBarLayout.setTitle(getResources().getString(R.string.title_expenses));

        loadingBar = view.findViewById(R.id.expense_list_loading);
        emptyList = view.findViewById(R.id.expense_list_empty);

        addButton = getActivity().findViewById(R.id.fab_add);
        addButton.setVisibility(View.VISIBLE);
        addButton.setOnClickListener(this);

        recyclerView = view.findViewById(R.id.expense_list);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(),
                ((LinearLayoutManager) recyclerView.getLayoutManager()).getOrientation()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);

        getLoaderManager().initLoader(EXPENSE_LOADER_ID, null, this);

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        addButton.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == addButton.getId()) {
            Intent i = new Intent(getActivity(), EditExpenseActivity.class);
            i.putExtra(VEHICLE_FROM_FRAGMENT_TO_EDIT_EXPENSE, vehicle);
            startActivityForResult(i, EXPENSE_ACTION_REQUEST_CODE);
        }
    }

    /**
     * Take care of notifying loader that data refresh is needed
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == EXPENSE_ACTION_REQUEST_CODE && resultCode == RESULT_OK)
            getLoaderManager().getLoader(EXPENSE_LOADER_ID).onContentChanged();
    }

    @Override
    public void onItemClick(long expenseId) {
        Intent i = new Intent(getActivity(), EditExpenseActivity.class);
        i.putExtra(EXPENSE_ID_TO_EDIT, expenseId);
        startActivityForResult(i, EXPENSE_ACTION_REQUEST_CODE);
    }


    // LOADER:

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        loadingBar.setVisibility(View.VISIBLE);
        String[] selectionArgs = {String.valueOf(vehicle.getId())};
        return new CursorLoader(getContext(),
                ExpenseEntry.CONTENT_URI,
                FuelUpContract.ALL_COLUMNS_EXPENSES,
                ExpenseEntry.COLUMN_VEHICLE + "=?",
                selectionArgs,
                ExpenseEntry.COLUMN_DATE + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        loadingBar.setVisibility(View.GONE);
        if (cursor.getCount() == 0) {
            emptyList.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyList.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
        adapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }

}