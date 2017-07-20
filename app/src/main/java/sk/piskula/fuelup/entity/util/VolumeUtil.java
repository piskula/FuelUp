package sk.piskula.fuelup.entity.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

import sk.piskula.fuelup.R;
import sk.piskula.fuelup.entity.enums.VolumeUnit;
import sk.piskula.fuelup.screens.MainActivity;

/**
 * @author Ondrej Oravcok
 * @version 13.7.2017
 */

public class VolumeUtil {

    public static final BigDecimal ONE_LITRE_IS_US_GALLONS = BigDecimal.valueOf(0.26417205235815d);
    public static final BigDecimal ONE_LITRE_IS_UK_GALLONS = BigDecimal.valueOf(0.21996923465436d);

    private static final String TAG = "VolumeUtil";

    public static String getPricePerVolumeShortString(VolumeUnit volumeUnit) {
        String textOff;
        if (volumeUnit == VolumeUnit.LITRE)
            textOff = MainActivity.getInstance().getString(R.string.add_fillup_pricePerLitre_short);
        else
            textOff = MainActivity.getInstance().getString(R.string.add_fillup_pricePerGallon_short);
        return textOff;
    }

    public static String getPricePerVolumeLongString(VolumeUnit volumeUnit) {
        if (volumeUnit == VolumeUnit.LITRE)
            return MainActivity.getInstance().getString(R.string.add_fillup_pricePerLitre);
        else
            return MainActivity.getInstance().getString(R.string.add_fillup_pricePerGallon);
    }

    public static BigDecimal getTotalPriceFromPerLitre(BigDecimal volume, BigDecimal price, VolumeUnit volumeUnit){
        BigDecimal total = null;
        switch (volumeUnit) {
            case LITRE:
                total = volume.multiply(price);
                break;
            case GALLON_UK:
                total = getLitresFromUkGallon(volume).multiply(price);
                break;
            case GALLON_US:
                total = getLitresFromUsGallon(volume).multiply(price);
                break;
        }
        return total;
    }

    public static BigDecimal getPerLitrePriceFromTotal(BigDecimal volume, BigDecimal price, VolumeUnit volumeUnit){
        BigDecimal total = null;
        switch (volumeUnit) {
            case LITRE:
                total = price.divide(volume, 3, RoundingMode.HALF_UP);
                break;
            case GALLON_UK:
                total = price.divide(getLitresFromUkGallon(volume), 3, RoundingMode.HALF_UP);
                break;
            case GALLON_US:
                total = price.divide(getLitresFromUsGallon(volume), 3, RoundingMode.HALF_UP);
                break;
        }
        return total;
    }

    public static BigDecimal getUsGallonsFromLitre(BigDecimal value) {
        return ONE_LITRE_IS_US_GALLONS.multiply(value);
    }

    public static BigDecimal getLitresFromUsGallon(BigDecimal value) {
        return value.divide(ONE_LITRE_IS_US_GALLONS, BigDecimal.ROUND_HALF_UP);
    }

    public static BigDecimal getUkGallonsFromLitre(BigDecimal value) {
        return ONE_LITRE_IS_UK_GALLONS.multiply(value);
    }

    public static BigDecimal getLitresFromUkGallon(BigDecimal value) {
        return value.divide(ONE_LITRE_IS_UK_GALLONS, BigDecimal.ROUND_HALF_UP);
    }
}
