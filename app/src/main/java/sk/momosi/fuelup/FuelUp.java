package sk.momosi.fuelup;

import android.app.Application;
import android.content.Context;

import com.squareup.leakcanary.LeakCanary;

/**
 * @author Martin Styk
 * @date 24.02.2018.
 */

public class FuelUp extends Application {

    private static Application instance;

    public static Context getInstance() {
        return instance.getApplicationContext();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        if (!LeakCanary.isInAnalyzerProcess(this)) {
            LeakCanary.install(this);
        }

    }
}
