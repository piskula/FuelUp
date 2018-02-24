package sk.momosi.fuelup.entity.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;

import sk.momosi.fuelup.FuelUp;
import sk.momosi.fuelup.R;
import sk.momosi.fuelup.entity.enums.VolumeUnit;

/**
 * @author Ondrej Oravcok
 * @version 13.7.2017
 */

public class VolumeUtil {

    public static final BigDecimal ONE_LITRE_IS_US_GALLONS = BigDecimal.valueOf(0.26417205235815d);
    public static final BigDecimal ONE_LITRE_IS_UK_GALLONS = BigDecimal.valueOf(0.21996923465436d);
    public static final BigDecimal ONE_UK_GALLON_IS_LITRES = BigDecimal.ONE.divide(ONE_LITRE_IS_UK_GALLONS, 14, RoundingMode.HALF_UP);
    public static final BigDecimal ONE_US_GALLON_IS_LITRES = BigDecimal.ONE.divide(ONE_LITRE_IS_US_GALLONS, 14, RoundingMode.HALF_UP);

    private static final String LOG_TAG = "VolumeUtil";

    public static String getPricePerVolumeShortString(VolumeUnit volumeUnit) {
        String textOff;
        if (volumeUnit == VolumeUnit.LITRE)
            textOff = FuelUp.getInstance().getString(R.string.add_fillup_pricePerLitre_short);
        else
            textOff = FuelUp.getInstance().getString(R.string.add_fillup_pricePerGallon_short);
        return textOff;
    }

    public static String getPricePerVolumeLongString(VolumeUnit volumeUnit) {
        if (volumeUnit == VolumeUnit.LITRE)
            return FuelUp.getInstance().getString(R.string.add_fillup_pricePerLitre);
        else
            return FuelUp.getInstance().getString(R.string.add_fillup_pricePerGallon);
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
                total = price.divide(volume, 7, RoundingMode.HALF_UP);
                break;
            case GALLON_UK:
                total = price.divide(getLitresFromUkGallon(volume), 7, RoundingMode.HALF_UP);
                break;
            case GALLON_US:
                total = price.divide(getLitresFromUsGallon(volume), 7, RoundingMode.HALF_UP);
                break;
        }
        return total;
    }

    public static BigDecimal getUsGallonsFromLitre(BigDecimal value) {
        return ONE_LITRE_IS_US_GALLONS.multiply(value);
    }

    public static BigDecimal getLitresFromUsGallon(BigDecimal value) {
        return value.divide(ONE_LITRE_IS_US_GALLONS, 7, BigDecimal.ROUND_HALF_UP);
    }

    public static BigDecimal getUkGallonsFromLitre(BigDecimal value) {
        return ONE_LITRE_IS_UK_GALLONS.multiply(value);
    }

    public static BigDecimal getLitresFromUkGallon(BigDecimal value) {
        return value.divide(ONE_LITRE_IS_UK_GALLONS, 7, BigDecimal.ROUND_HALF_UP);
    }

    public static String getFuelVolume(double value) {
        DecimalFormat bddf = new DecimalFormat();
        bddf.setGroupingUsed(false);
        bddf.setMaximumFractionDigits(2);
        bddf.setMinimumFractionDigits(0);

        return bddf.format(value);
    }

    public static DecimalFormat getFormatterForConsumption(String consumptionUnit) {
        DecimalFormat format = new DecimalFormat();

        if (FuelUp.getInstance().getString(R.string.units_mpg).equals(consumptionUnit)) {
            format.setMaximumFractionDigits(1);
            format.setMinimumFractionDigits(1);
        } else {
            format.setMaximumFractionDigits(2);
            format.setMinimumFractionDigits(2);
        }

        return format;
    }
}
