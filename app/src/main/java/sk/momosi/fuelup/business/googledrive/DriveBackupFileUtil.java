package sk.momosi.fuelup.business.googledrive;

import android.content.Context;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author Martin Styk
 * @version 14.10.2017
 */
public class DriveBackupFileUtil {

    public static final String BACKUP_DB_FOLDER = "appDataFolder";
    public static final String BACKUP_DB_FILE_NAME = "fuelup_backup.json";
    public static final String[] SCOPES = {DriveScopes.DRIVE_APPDATA, DriveScopes.DRIVE_FILE};

    public static String getBackupFileId(com.google.api.services.drive.Drive mService) throws IOException {
        FileList result = mService.files().list()
                .setSpaces(BACKUP_DB_FOLDER)
                .setPageSize(10)
                .setFields("nextPageToken, files(id, name, modifiedTime)")
                .execute();

        if (result == null || result.getFiles().isEmpty()) {
            return null;
        }

        List<File> files = result.getFiles();
        Collections.sort(files, new Comparator<File>() {
            @Override
            public int compare(File f1, File f2) {
                return Long.valueOf(f2.getModifiedTime().getValue()).compareTo(f1.getModifiedTime().getValue());
            }
        });
        return files.get(0).getId();
    }

    public static com.google.api.services.drive.Drive getServiceForCredentials(GoogleAccountCredential credential) {
        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();

        return new com.google.api.services.drive.Drive.Builder(
                transport, jsonFactory, credential)
                .setApplicationName("fuelup")
                .build();
    }

    public static GoogleAccountCredential generateCredential(Context context) {
        return GoogleAccountCredential.usingOAuth2(context, Arrays.asList(DriveBackupFileUtil.SCOPES))
                .setBackOff(new ExponentialBackOff());
    }
}
