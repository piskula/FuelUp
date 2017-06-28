package sk.piskula.fuelup.entity.util;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * @author Ondrej Oravcok
 * @version 28.6.2017
 */

public class DateUtil {

    private static final String TAG = "DateUtil";

    public static Calendar transformToCal(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(date.getTime());
        return cal;
    }

    public static String getDateLocalized(Calendar calendar) {
        return DateFormat.getDateInstance(DateFormat.SHORT).format(calendar.getTime());
    }

    public static String getDateLocalized(Date date) {
        return DateFormat.getDateInstance(DateFormat.SHORT).format(date);
    }
}
