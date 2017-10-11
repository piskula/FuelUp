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

import java.util.List;

import sk.momosi.fuelup.R;
import sk.momosi.fuelup.adapters.ListFillUpsAdapter;
import sk.momosi.fuelup.data.FuelUpContract;
import sk.momosi.fuelup.data.FuelUpContract.FillUpEntry;
import sk.momosi.fuelup.entity.FillUp;
import sk.momosi.fuelup.entity.Vehicle;
import sk.momosi.fuelup.screens.edit.AddFillUpActivity;
import sk.momosi.fuelup.screens.edit.EditFillUpActivity;
import sk.momosi.fuelup.screens.VehicleTabbedDetailActivity;

import static android.app.Activity.RESULT_OK;

/**
 * @author Ondrej Oravcok
 * @author Martin Styk
 * @version 16.8.2017
 */
public class FillUpsListFragment extends Fragment implements ListFillUpsAdapter.Callback,
        View.OnClickListener, LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = "FillUpsListFragment";

    public static final String FILLUP_ID_TO_EDIT = "fillup_to_edit";
    public static final int FILLUP_ACTION_REQUEST_CODE = 32;
    public static final String VEHICLE_FROM_FRAGMENT_TO_EDIT_FILLUP = "fromFragmentToFillUp";
    public static final int FILLUP_LOADER_ID = 713;

    private Vehicle vehicle;
    private List<FillUp> data;
    private ListFillUpsAdapter adapter;

    private RecyclerView recyclerView;
    private ProgressBar loadingBar;
    private TextView emptyList;

    private CollapsingToolbarLayout appBarLayout;
    private FloatingActionButton addButton;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        vehicle = getArguments().getParcelable(VehicleTabbedDetailActivity.VEHICLE_TO_FRAGMENT);
        setRetainInstance(true);
        adapter = new ListFillUpsAdapter(this, vehicle);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_fillups_list, container, false);

        appBarLayout = getActivity().findViewById(R.id.toolbar_layout);
        appBarLayout.setTitle(getResources().getString(R.string.title_fillUps));

        loadingBar = view.findViewById(R.id.fill_ups_list_loading);
        emptyList = view.findViewById(R.id.fill_ups_list_empty);

        addButton = getActivity().findViewById(R.id.fab_add);
        addButton.setVisibility(View.VISIBLE);
        addButton.setOnClickListener(this);

        recyclerView = view.findViewById(R.id.fill_ups_list);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(),
                ((LinearLayoutManager) recyclerView.getLayoutManager()).getOrientation()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);

        getLoaderManager().initLoader(FILLUP_LOADER_ID, null, this);

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        addButton.setVisibility(View.GONE);
    }



    @Override
    public void onItemClick(long fillUpId) {
        Intent i = new Intent(getActivity(), EditFillUpActivity.class);
        i.putExtra(FILLUP_ID_TO_EDIT, fillUpId);
        startActivityForResult(i, FILLUP_ACTION_REQUEST_CODE);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == addButton.getId()) {
            Intent i = new Intent(getActivity(), AddFillUpActivity.class);
            i.putExtra(VEHICLE_FROM_FRAGMENT_TO_EDIT_FILLUP, vehicle);
            startActivityForResult(i, FILLUP_ACTION_REQUEST_CODE);
        }
    }

    /**
     * Take care of notifying loader that data refresh is needed
     */

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILLUP_ACTION_REQUEST_CODE && resultCode == RESULT_OK)
            getLoaderManager().getLoader(FILLUP_LOADER_ID).onContentChanged();
    }


    // LOADER:

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        loadingBar.setVisibility(View.VISIBLE);
        String[] selectionArgs = { String.valueOf(vehicle.getId()) };
        return new CursorLoader(getContext(),
                FillUpEntry.CONTENT_URI,
                FuelUpContract.ALL_COLUMNS_FILLUPS,
                FillUpEntry.COLUMN_VEHICLE + "=?",
                selectionArgs,
                FillUpEntry.COLUMN_DATE + " DESC");
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
