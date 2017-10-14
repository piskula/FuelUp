package sk.momosi.fuelup.business.googledrive;

import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Martin Styk on 14.10.2017.
 */

public class DriveBackupFileUtil {

    static final String BACKUP_DB_FOLDER = "appDataFolder";
    static final String BACKUP_DB_FILE_NAME = "fuelup_backup.json";

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
}
