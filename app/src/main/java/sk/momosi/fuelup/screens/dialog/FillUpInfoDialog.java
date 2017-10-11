package sk.momosi.fuelup.screens.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import sk.momosi.fuelup.R;
import sk.momosi.fuelup.databinding.DialogFillupDetailBinding;
import sk.momosi.fuelup.entity.FillUp;

/**
 * @author Martin Styk
 * @version 20.7.2017.
 */
public class FillUpInfoDialog extends DialogFragment {

    private static final String TAG = FillUpInfoDialog.class.getSimpleName();
    public static final String FILL_UP = "fillUp";

    public static FillUpInfoDialog newInstance(FillUp fillUp) {
        FillUpInfoDialog frag = new FillUpInfoDialog();
        Bundle args = new Bundle();
        args.putParcelable(FILL_UP, fillUp);
        frag.setArguments(args);
        return frag;
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        DialogFillupDetailBinding binding = DialogFillupDetailBinding.inflate(getActivity().getLayoutInflater());

        FillUp fillUp = getArguments().getParcelable(FILL_UP);
        binding.setFillUp(fillUp);

        return new AlertDialog.Builder(getActivity())
                .setView(binding.getRoot())
                .setTitle(R.string.fill_up_detail)
                .setNegativeButton(R.string.dismiss, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dismiss();
                    }
                })
                .create();
    }
}
