package sk.momosi.fuelup.entity.util;

import android.content.Context;

import java.text.DateFormat;
import java.text.ParseException;
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

    public static Calendar parseDateTimeFromString(String date, Context context) throws ParseException {
        Calendar createdDate = Calendar.getInstance();

        Calendar timePart = Calendar.getInstance();
        createdDate.setTime(getDateFormat(context).parse(date));
        createdDate.set(Calendar.HOUR, timePart.get(Calendar.HOUR));
        createdDate.set(Calendar.MINUTE, timePart.get(Calendar.MINUTE));
        createdDate.set(Calendar.SECOND, timePart.get(Calendar.SECOND));
        createdDate.set(Calendar.MILLISECOND, timePart.get(Calendar.MILLISECOND));

        return createdDate;
    }

    /**
     * @return true if Date-parts of timestamps are equals (d.m.yyyy)
     */
    public static boolean areTwoDatesEquals(final Calendar c1, final Date d2) {
        Calendar c2 = Calendar.getInstance();
        c2.setTime(d2);

        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR)
                && c1.get(Calendar.MONTH) == c2.get(Calendar.MONTH)
                && c1.get(Calendar.DAY_OF_MONTH) == c2.get(Calendar.DAY_OF_MONTH);
    }

    public static DateFormat getDateFormat(Context context) {
        return android.text.format.DateFormat.getDateFormat(context);
    }
}
