package sk.piskula.fuelup.util;

import java.text.DecimalFormat;

/**
 * Created by Martin Styk on 10.07.2017.
 */

public class BigDecimalFormatter {
    private static DecimalFormat format;

    public static DecimalFormat getCommonFormat() {
        if (format == null) {
            format = new DecimalFormat();
            format.setMaximumFractionDigits(2);
            format.setMinimumFractionDigits(2);
        }
        return format;
    }

    public static DecimalFormat getFormat(int minFractions, int maxFractions) {
        DecimalFormat customFormat = new DecimalFormat();
        customFormat.setMaximumFractionDigits(maxFractions);
        customFormat.setMinimumFractionDigits(minFractions);

        return customFormat;
    }
}
