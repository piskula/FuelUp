package sk.momosi.fuelup.adapters;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import java.util.List;

import sk.momosi.fuelup.R;
import sk.momosi.fuelup.business.VehicleService;

/**
 * @author Ondrej Oravcok
 * @version 13.10.2017
 */
public class ListVehiclesRestoreAdapter extends ArrayAdapter<String> {

    private static final String LOG_TAG = ListVehiclesRestoreAdapter.class.getSimpleName();

    private ListVehiclesRestoreAdapter.Callback callback;
    private List<String> vehicleNames;

    public interface Callback {
        void onItemClickAdd(String vehicleName);
        void onItemClickRemove(String vehicleName);
    }

    public ListVehiclesRestoreAdapter(Context context, List<String> vehicleNames, Callback callback) {
        super(context, R.layout.list_item_vehicle_restore, vehicleNames);
        this.vehicleNames = vehicleNames;
        this.callback = callback;
    }

    @Override
    @NonNull
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = ((Activity)getContext()).getLayoutInflater();
        convertView = inflater.inflate(R.layout.list_item_vehicle_restore, parent, false);

        String name = vehicleNames.get(position);
        CheckBox vehicleName = convertView.findViewById(R.id.checkbox_restore_vehicle);
        vehicleName.setText(name);
        vehicleName.setEnabled(!VehicleService.isVehicleNameTaken(name, getContext()));
        vehicleName.setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                        if (isChecked)
                            callback.onItemClickAdd(compoundButton.getText().toString());
                        else
                            callback.onItemClickRemove(compoundButton.getText().toString());
                    }
                }
        );
        return convertView;
    }
}
