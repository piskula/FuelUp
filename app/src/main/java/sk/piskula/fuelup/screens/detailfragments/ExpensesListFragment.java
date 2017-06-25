package sk.piskula.fuelup.screens.detailfragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import java.util.List;

import sk.piskula.fuelup.R;
import sk.piskula.fuelup.adapters.ListExpensesAdapter;
import sk.piskula.fuelup.business.ExpenseService;
import sk.piskula.fuelup.entity.Expense;
import sk.piskula.fuelup.entity.Vehicle;
import sk.piskula.fuelup.loaders.ExpenseLoader;
import sk.piskula.fuelup.screens.VehicleTabbedDetail;
import sk.piskula.fuelup.screens.edit.EditExpense;

import static android.app.Activity.RESULT_OK;

/**
 * @author Ondrej Oravcok
 * @author Martin Styk
 * @version 21.6.2017
 */
public class ExpensesListFragment extends Fragment implements ListExpensesAdapter.Callback, View.OnClickListener,
        LoaderManager.LoaderCallbacks<List<Expense>> {

    private static final String TAG = ExpensesListFragment.class.getSimpleName();

    public static final String EXPENSE_TO_EDIT = "expense to edit";
    public static final int EXPENSE_ACTION_REQUEST_CODE = 31;
    public static final String VEHICLE_FROM_FRAGMENT_TO_EDIT_EXPENSE = "fromFragmentToExpense";

    private Bundle args;
    private Vehicle vehicle;
    private List<Expense> data;
    private ListExpensesAdapter adapter;

    private RecyclerView recyclerView;
    private ProgressBar loadingBar;

    private CollapsingToolbarLayout appBarLayout;
    private FloatingActionButton addButton;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View view = inflater.inflate(R.layout.expenses_list, container, false);

        args = getArguments();
        vehicle = args.getParcelable(VehicleTabbedDetail.VEHICLE_TO_FRAGMENT);

        appBarLayout = getActivity().findViewById(R.id.toolbar_layout);
        appBarLayout.setTitle(getResources().getString(R.string.title_expenses));

        loadingBar = view.findViewById(R.id.expense_list_loading);

        addButton = getActivity().findViewById(R.id.fab_add);
        addButton.setVisibility(View.VISIBLE);
        addButton.setOnClickListener(this);

        recyclerView = view.findViewById(R.id.expense_list);
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(),
                ((LinearLayoutManager) recyclerView.getLayoutManager()).getOrientation()));

        if (adapter == null)
            adapter = new ListExpensesAdapter(this);

        recyclerView.setAdapter(adapter);

        getLoaderManager().initLoader(ExpenseLoader.ID, args, this);

        return view;
    }

    @Override
    public Loader<List<Expense>> onCreateLoader(int id, Bundle args) {
        Vehicle vehicle = args.getParcelable(VehicleTabbedDetail.VEHICLE_TO_FRAGMENT);
        long vehicleId = vehicle.getId();
        return new ExpenseLoader(getActivity(), vehicleId, new ExpenseService(getActivity()));
    }

    @Override
    public void onLoadFinished(Loader<List<Expense>> loader, List<Expense> data) {
        this.data = data;
        adapter.dataChange(data);
        loadingBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onLoaderReset(Loader<List<Expense>> loader) {
        if (!data.isEmpty())
            data.clear();
    }

    @Override
    public void onItemClick(View v, Expense expense, int position) {
        Intent i = new Intent(getActivity(), EditExpense.class);
        i.putExtra(EXPENSE_TO_EDIT, expense);
        startActivityForResult(i, EXPENSE_ACTION_REQUEST_CODE);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == addButton.getId()) {
            Intent i = new Intent(getActivity(), EditExpense.class);
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
            getLoaderManager().getLoader(ExpenseLoader.ID).onContentChanged();
    }
}