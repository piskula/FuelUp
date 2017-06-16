package sk.piskula.fuelup.data;

import com.j256.ormlite.android.apptools.OrmLiteConfigUtil;

import java.io.IOException;
import java.sql.SQLException;

/**
 * @author Ondrej Oravcok
 * @version 16.6.2017.
 */
public class DatabaseConfigUtil extends OrmLiteConfigUtil {

    public static void main(String[] args) throws SQLException, IOException {

        writeConfigFile("ormlite_config.txt");
    }

}
