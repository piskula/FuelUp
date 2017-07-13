package sk.piskula.fuelup.entity.util;

import android.content.Context;

import java.math.BigDecimal;

import sk.piskula.fuelup.R;
import sk.piskula.fuelup.entity.enums.VolumeUnit;

/**
 * @author Ondrej Oravcok
 * @version 13.7.2017
 */

public class VolumeUtil {

    public static final BigDecimal ONE_LITRE_IS_US_GALLONS = BigDecimal.valueOf(0.26417205235815d);
    public static final BigDecimal ONE_LITRE_IS_UK_GALLONS = BigDecimal.valueOf(0.21996923465436d);

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

    public static BigDecimal getUsGallonsFromLitre(BigDecimal value) {
        return ONE_LITRE_IS_US_GALLONS.multiply(value);
    }

    public static BigDecimal getLitresFromUsGallon(BigDecimal value) {
        return value.divide(ONE_LITRE_IS_US_GALLONS);
    }

    public static BigDecimal getUkGallonsFromLitre(BigDecimal value) {
        return ONE_LITRE_IS_UK_GALLONS.multiply(value);
    }

    public static BigDecimal getLitresFromUkGallon(BigDecimal value) {
        return value.divide(ONE_LITRE_IS_UK_GALLONS);
    }
}
