package sk.momosi.fuelup.business.googledrive;

import android.content.Context;
import android.util.Log;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.FileList;

import java.io.IOException;
import java.util.Arrays;

/**
 * @author Martin Styk
 * @version 14.10.2017
 */
public class DriveBackupFileUtil {

    private static final String LOG_TAG = DriveBackupFileUtil.class.getSimpleName();

    public static final String BACKUP_DB_FOLDER = "FuelUp_backup";
    public static final String BACKUP_DB_FILE_NAME = "fuelup_backup.json";
    private static final String[] SCOPES = {DriveScopes.DRIVE};

    public static String getBackupFileId(Drive service, String folderId) throws IOException {
        if (folderId == null) {
            return null;
        }

        FileList jsonFile = service.files().list()
                .setQ("\'" + folderId + "\' in parents and name = \'" + BACKUP_DB_FILE_NAME + "\'")
                .setPageSize(1)
                .setFields("files(id)")
                .execute();

        if (jsonFile.getFiles() == null) {
            return null;
        } else if (jsonFile.getFiles().size() != 1) {
            Log.e(LOG_TAG, "There are more files " + BACKUP_DB_FILE_NAME + " in your FuelUp folder on Google Drive.");
            // TODO handle this with message
            return null;
        }
        return jsonFile.getFiles().get(0).getId();
    }

    public static String getBackupFolderId(Drive service) throws IOException {
        FileList fuelUpFolder = service.files().list()
                .setQ("\'root\' in parents and name = \'"+ BACKUP_DB_FOLDER +"\'")
                .setPageSize(1)
                .setFields("files(id)")
                .execute();
        if (fuelUpFolder.getFiles() == null) {
            return null;
        } else if (fuelUpFolder.getFiles().size() != 1) {
            Log.e(LOG_TAG, "There are more folders " + BACKUP_DB_FOLDER + " on your Google Drive Account.");
            // TODO handle this with message
            return null;
        }

        return fuelUpFolder.getFiles().get(0).getId();
    }

    public static Drive getServiceForCredentials(GoogleAccountCredential credential) {
        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();

        return new Drive.Builder(
                transport, jsonFactory, credential)
                .setApplicationName("fuelup")
                .build();
    }

    public static GoogleAccountCredential generateCredential(Context context) {
        return GoogleAccountCredential.usingOAuth2(context, Arrays.asList(DriveBackupFileUtil.SCOPES))
                .setBackOff(new ExponentialBackOff());
    }
}
