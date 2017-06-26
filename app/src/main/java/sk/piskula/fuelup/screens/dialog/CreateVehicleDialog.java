package sk.piskula.fuelup.screens.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import sk.piskula.fuelup.R;

/**
 * @author Martin Styk
 * @version 23.06.2017.
 */
public class CreateVehicleDialog extends DialogFragment implements Dialog.OnShowListener {

    private static final String TAG = CreateVehicleDialog.class.getSimpleName();

    private EditText vehicleName;

    public interface Callback {
        void onDialogCreateBtnClick(CreateVehicleDialog dialog, Editable vehicleName);

        void onDialogAdvancedBtnClick(CreateVehicleDialog dialog, Editable vehicleName);

    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.create_vehicle_dialog, null);

        final AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setView(dialogView)
                .setTitle(R.string.create)
                .setPositiveButton(R.string.create,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                ((CreateVehicleDialog.Callback) getActivity()).onDialogCreateBtnClick(CreateVehicleDialog.this, vehicleName.getText());
                            }
                        }
                )
                .setNegativeButton(R.string.advanced,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                ((CreateVehicleDialog.Callback) getActivity()).onDialogAdvancedBtnClick(CreateVehicleDialog.this, vehicleName.getText());
                            }
                        }
                )
                .create();
        dialog.setOnShowListener(this);

        vehicleName = dialogView.findViewById(R.id.createVehicleDialog_name);

        vehicleName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.toString().isEmpty()) {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                } else {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                }
            }
        });
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        return dialog;
    }

    @Override
    public void onShow(DialogInterface dialog) {
        Button positiveButton = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
        positiveButton.setEnabled(vehicleName != null && vehicleName.getText() != null && !vehicleName.getText().toString().isEmpty());
    }

}
