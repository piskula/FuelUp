package sk.momosi.fuelup.screens.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import sk.momosi.fuelup.R;
import sk.momosi.fuelup.adapters.ListVehiclesRestoreAdapter;

/**
 * @author Ondrej Oravcok
 * @version 13.10.2017
 */
public class RestoreVehicleDialog extends DialogFragment implements Dialog.OnShowListener, ListVehiclesRestoreAdapter.Callback {

    private static final String LOG_TAG = RestoreVehicleDialog.class.getSimpleName();
    private static final String BUNDLE_ARGS_VEHICLE_NAMES = "vehicle_names_to_restore_dialog";

    private Callback callback;

    private ArrayList<String> vehicleNames;
    private Set<String> vehicleNamesResult = new HashSet<>();
    private TextView vehicleName;
    private ListView vehicleList;

    @Override
    public void onItemClickAdd(String vehicleName) {
        vehicleNamesResult.add(vehicleName);
    }

    @Override
    public void onItemClickRemove(String vehicleName) {
        vehicleNamesResult.remove(vehicleName);
    }

    public interface Callback {
        void onDialogPositiveClick(Set<String> vehicleNames);
    }

    public static RestoreVehicleDialog newInstance(ArrayList<String> vehicleNames, Callback callback) {
        RestoreVehicleDialog frag = new RestoreVehicleDialog();
        Bundle args = new Bundle();
        args.putStringArrayList(BUNDLE_ARGS_VEHICLE_NAMES, vehicleNames);
        frag.setArguments(args);
        frag.setCallback(callback);
        return frag;
    }

    private void setCallback(Callback callback) {
        this.callback = callback;
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.confirm_restore_dialog, null);

        vehicleNames = getArguments().getStringArrayList(BUNDLE_ARGS_VEHICLE_NAMES);

        final AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setView(dialogView)
                .setTitle(R.string.create)
                .setPositiveButton(R.string.googledrive_startimport_btn,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                callback.onDialogPositiveClick(vehicleNamesResult);
                            }
                        }
                )
                .setNegativeButton(R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dismiss();
                            }
                        }
                )
                .create();
        dialog.setOnShowListener(this);

        vehicleName = dialogView.findViewById(R.id.restore_dialog_title);
        vehicleName.setText("Are you sure to import data?");

        vehicleList = dialogView.findViewById(R.id.list_vehicle_restore);
        vehicleList.setAdapter(new ListVehiclesRestoreAdapter(getContext(), vehicleNames, this));

        return dialog;
    }

    @Override
    public void onShow(DialogInterface dialog) {
    }

}
