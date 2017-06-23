package sk.piskula.fuelup.screens.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import sk.piskula.fuelup.R;

/**
 * Created by Martin Styk on 23.06.2017.
 */
public class DeleteDialog extends DialogFragment {

    private static final String TAG = DeleteDialog.class.getSimpleName();

    private static final String ARG_TITLE = "title";
    private static final String ARG_MESSAGE = "message";
    private static final String ARG_ICON = "icon";

    public interface Callback {
        void onDeleteDialogPositiveClick(DeleteDialog deleteDialog);

        void onDeleteDialogNegativeClick(DeleteDialog deleteDialog);
    }


    public static DeleteDialog newInstance(String title, String message, @DrawableRes @Nullable Integer icon) {
        DeleteDialog frag = new DeleteDialog();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putString(ARG_MESSAGE, message);
        if (icon != null) {
            args.putInt(ARG_ICON, icon);
        }
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String title = getArguments().getString(ARG_TITLE);
        String message = getArguments().getString(ARG_MESSAGE);
        @DrawableRes Integer icon = getArguments().getInt(ARG_ICON);

        return new AlertDialog.Builder(getActivity())
                .setTitle(title)
                .setMessage(message)
                .setIcon(icon)
                .setPositiveButton(R.string.delete,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                ((Callback) getActivity()).onDeleteDialogPositiveClick(DeleteDialog.this);
                            }
                        }
                )
                .setNegativeButton(R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                ((Callback) getActivity()).onDeleteDialogNegativeClick(DeleteDialog.this);
                            }
                        }
                )
                .create();
    }
}
