package sk.momosi.fuelup.util;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

/**
 * Created by Martin Styk on 27.10.2017.
 */

public class NonZeroTextWatcher implements TextWatcher {

    private EditText editText;

    public NonZeroTextWatcher(EditText editText) {
        this.editText = editText;
    }

    @Override
    public void afterTextChanged(Editable editable) {
        if (editable.length() == 1 && editable.toString().equals("0"))
            editText.setText("");
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
    }
}