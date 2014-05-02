/**
 *
 * Copyright (C) 2013 Geoffrey Falk
 *
 */

package org.maskmedia.roboliterate.robotcomms;

import java.io.IOException;

public interface SoundUploader extends Runnable {

    boolean deleteAllSoundsOnRobot(int numberOfFiles);

    void uploadResourceToRobot(int resID, String targetFilename, String localFilename) throws IOException;

    void addFilesToUpload(String[] targetFileNames, String[] filesToUpload);

    void addFileToUpload(String localFilename, String targetFilename);

    void quitUploading();

}
