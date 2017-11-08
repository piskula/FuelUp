package sk.momosi.fuelup.util;

import java.math.RoundingMode;
import java.text.DecimalFormat;

/**
 * @author Martin Styk
 * @version 10.7.2017
 */
public class BigDecimalFormatter {
    private static DecimalFormat format;

    public static DecimalFormat getCommonFormat() {
        if (format == null) {
            format = new DecimalFormat();
            format.setMaximumFractionDigits(2);
            format.setMinimumFractionDigits(2);
            format.setRoundingMode(RoundingMode.HALF_UP);
        }
        return format;
    }

}
