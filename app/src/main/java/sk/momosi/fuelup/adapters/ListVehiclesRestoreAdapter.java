package sk.momosi.fuelup.adapters;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.res.ColorStateList;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import sk.momosi.fuelup.R;
import sk.momosi.fuelup.screens.MainActivity;

/**
 * @author Ondro
 * @version 13.11.2017
 */
public class ListVehiclesRestoreAdapter extends RecyclerView.Adapter<ListVehiclesRestoreAdapter.VehicleRestoreViewHolder> {

    private final Callback callback;
    private final List<String> vehiclesFromBackup;
    private final Set<String> vehiclesInDb;
    private final Set<String> vehiclesChosen;

    public ListVehiclesRestoreAdapter(final Callback callback, final Context context, final List<String> vehiclesFromBackup, final Set<String> vehiclesInDb) {
        super();
        this.callback = callback;
        
        this.vehiclesFromBackup = Collections.unmodifiableList(vehiclesFromBackup);
        this.vehiclesInDb = Collections.unmodifiableSet(vehiclesInDb);
        
        this.vehiclesChosen = new HashSet<>(vehiclesFromBackup);
        this.vehiclesChosen.removeAll(vehiclesInDb);

        callback.onVehiclesChosenChange(Collections.unmodifiableSet(vehiclesChosen));
    }

    public interface Callback {
        void onVehiclesChosenChange(Set<String> vehiclesChosen);
        void makeWarningToastForVehicle(String name);
    }

    @Override
    public VehicleRestoreViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_vehicle_restore, parent, false);
        return new VehicleRestoreViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final VehicleRestoreViewHolder holder, final int position) {
        final String name = vehiclesFromBackup.get(position);

        boolean disabled = vehiclesInDb.contains(name);
        boolean chosen = vehiclesChosen.contains(name);
        int color;
        int colorText;
        if (disabled) {
            color = R.drawable.vehicle_item_disabled;
            colorText = R.color.colorPrimary;
        } else {
            colorText = R.color.colorPrimaryDark;
            if (chosen)
                color = R.drawable.vehicle_item_chosen;
            else
                color = R.drawable.vehicle_item_unchosen;
        }
//        int color = disabled ? R.drawable.vehicle_item_disabled : R.drawable.account_list_shape;
        holder.itemView.setBackground(MainActivity.getInstance().getResources().getDrawable(color));

        holder.restoreVehicleBox.setText(name);
        holder.restoreVehicleBox.setChecked(chosen);
        holder.trashIcon.setVisibility(chosen ? View.GONE : View.VISIBLE);
//        holder.restoreVehicleBox.tint
        holder.restoreVehicleBox.setTextColor(MainActivity.getInstance().getResources().getColor(colorText));
        holder.restoreVehicleBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (vehiclesInDb.contains(name)) {
                    holder.restoreVehicleBox.setChecked(false);
                    callback.makeWarningToastForVehicle(name);
                } else if (isChecked) {
                    chooseVehicle(name);
                } else {
                    unchooseVehicle(name);
                }
            }
        });
    }

//    public void setChosenVehicles(Set<String> chosenVehicles) {
//        if (chosenVehicles != null) {
//            vehiclesChosen.addAll(chosenVehicles);
//            notifyDataSetChanged();
//            callback.onVehiclesChosenChange(Collections.unmodifiableSet(vehiclesChosen));
//        }
//    }

    private void unchooseVehicle(final @NonNull String name) {
        if (vehiclesChosen.remove(name)) {
            notifyDataSetChanged();
            callback.onVehiclesChosenChange(Collections.unmodifiableSet(vehiclesChosen));
        }
    }

    private void chooseVehicle(final @NonNull String name) {
        if (!vehiclesInDb.contains(name)) {
            vehiclesChosen.add(name);
            notifyDataSetChanged();
            callback.onVehiclesChosenChange(Collections.unmodifiableSet(vehiclesChosen));

        } else {
                // TODO toast?
        }
    }

    @Override
    public int getItemCount() {
        return vehiclesFromBackup.size();
    }

    class VehicleRestoreViewHolder extends RecyclerView.ViewHolder
    {
        final CheckBox restoreVehicleBox;
        final ImageView trashIcon;

        VehicleRestoreViewHolder(View view) {
            super(view);
            restoreVehicleBox = view.findViewById(R.id.checkbox_restore_vehicle);
            trashIcon = view.findViewById(R.id.restore_item_trash);
        }
    }

}
