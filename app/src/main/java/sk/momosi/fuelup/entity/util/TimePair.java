package sk.momosi.fuelup.entity.util;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by Martin Styk on 24.07.2017.
 */

public class TimePair {
    public int year;
    public int month;

    public static TimePair from(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int year = calendar.get(Calendar.YEAR);
        //Add one to month {0 - 11}
        int month = calendar.get(Calendar.MONTH) + 1;
        return new TimePair(year, month);
    }

    public TimePair(int year, int month) {
        this.year = year;
        this.month = month;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TimePair pair = (TimePair) o;

        if (year != pair.year) return false;
        return month == pair.month;

    }

    @Override
    public int hashCode() {
        int result = year;
        result = 31 * result + month;
        return result;
    }

    public boolean isBefore(TimePair other) {
        return this.year < other.year || (this.year == other.year && this.month < other.month);
    }
}
