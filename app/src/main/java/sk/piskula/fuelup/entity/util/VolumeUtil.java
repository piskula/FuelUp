package sk.piskula.fuelup.entity.util;

import android.content.Context;

import sk.piskula.fuelup.R;
import sk.piskula.fuelup.entity.enums.VolumeUnit;

/**
 * @author Ondrej Oravcok
 * @version 13.7.2017
 */

public class VolumeUtil {

    private static final String TAG = "VolumeUtil";

    public static String getPricePerVolumeShortString(VolumeUnit volumeUnit, Context context) {
        String textOff;
        if (volumeUnit == VolumeUnit.LITRE)
            textOff = context.getString(R.string.add_fillup_pricePerLitre_short);
        else
            textOff = context.getString(R.string.add_fillup_pricePerGallon_short);
        return textOff;
    }

    public static String getPricePerVolumeLongString(VolumeUnit volumeUnit, Context context) {
        if (volumeUnit == VolumeUnit.LITRE)
            return context.getString(R.string.add_fillup_pricePerLitre);
        else
            return context.getString(R.string.add_fillup_pricePerGallon);
    }
}
