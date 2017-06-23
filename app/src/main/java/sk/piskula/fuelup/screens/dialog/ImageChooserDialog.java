package sk.piskula.fuelup.screens.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import sk.piskula.fuelup.R;

/**
 * Created by Martin Styk on 23.06.2017.
 */

public class ImageChooserDialog extends DialogFragment implements DialogInterface.OnClickListener {

    private static final String TAG = ImageChooserDialog.class.getSimpleName();
    final int TAKE_PHOTO = 0;
    final int SELECT_PHOTO = 1;

    public interface Callback {
        void onSelectFromGallery(ImageChooserDialog dialog);

        void onTakePhoto(ImageChooserDialog dialog);
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final CharSequence[] opsChars = {getString(R.string.addVehicle_takePhoto), getString(R.string.addVehicle_selectPhotoFromGallery)};

        AlertDialog.Builder getImageDialog = new AlertDialog.Builder(getActivity());
        getImageDialog.setTitle(getString(R.string.addVehicle_vehicle_picture));
        getImageDialog.setItems(opsChars, this);
        return getImageDialog.create();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (which == TAKE_PHOTO) {
            ((ImageChooserDialog.Callback) getActivity()).onTakePhoto(ImageChooserDialog.this);
        } else if (which == SELECT_PHOTO) {
            ((ImageChooserDialog.Callback) getActivity()).onSelectFromGallery(ImageChooserDialog.this);
        }
        dialog.dismiss();
    }
}
