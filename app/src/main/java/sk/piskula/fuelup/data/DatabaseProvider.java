package sk.piskula.fuelup.data;

import android.content.Context;

import com.j256.ormlite.android.apptools.OpenHelperManager;

/**
 * Created by Martin Styk on 20.06.2017.
 */

public class DatabaseProvider {

    private static DatabaseHelper instance;

    public static DatabaseHelper get(Context context){
        if (instance == null){
            instance = OpenHelperManager.getHelper(context, DatabaseHelper.class);
        }
        return instance;
    }

    private DatabaseProvider(){
    }

}
