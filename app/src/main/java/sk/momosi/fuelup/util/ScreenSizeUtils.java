package sk.momosi.fuelup.util;

import android.app.Activity;
import android.graphics.Point;
import android.view.Display;

/**
 * Created by Martin Styk on 27.10.2017.
 */

public class ScreenSizeUtils {

    public static int getActualWidth(Activity activity){
        Display display = activity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size.x;
    }
}
