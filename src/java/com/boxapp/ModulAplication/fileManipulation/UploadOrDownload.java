package src.java.com.boxapp.ModulAplication.fileManipulation;

import com.box.sdk.BoxAPIConnection;
import com.box.sdk.BoxFile;
import com.box.sdk.BoxFolder;
import com.box.sdk.BoxItem;

import java.io.*;

public class UploadOrDownload {

    public static void downloadFileMethod(BoxAPIConnection api, String downloadFileName, String idOfFolderFromDownloading, String folderForDownload) {
        BoxFolder inputFolder = new BoxFolder(api, idOfFolderFromDownloading);

        for (BoxItem.Info itemInfo : inputFolder) {
            if (itemInfo instanceof BoxFile.Info) {
                if (itemInfo.getName().equals(downloadFileName)) {
                    BoxFile file = new BoxFile(api, itemInfo.getID());
                    BoxFile.Info info = file.getInfo();
                    File directory = new File(System.getProperty("user.dir") + "\\" + folderForDownload);
                    if (! directory.exists()) {
                        directory.mkdir();
                    }
                    FileOutputStream stream = null;
                    try {
                        stream = new FileOutputStream(System.getProperty("user.dir") + "\\" + folderForDownload + "\\" + info.getName());
                        //stream = new FileOutputStream(pathToInputFolder);
                        file.download(stream);
                        stream.close();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }
        }
    }

    public static void uploadToFolderMethod(BoxAPIConnection api, String localFileName, String uploadedFileName, String idOfFolderWhereUploading, String folderFromWeUpload) {
        BoxFolder uploadFolder = new BoxFolder(api, idOfFolderWhereUploading);
        int sameFileName = 0;

        for (BoxItem.Info itemInfo : uploadFolder) {
            if (itemInfo instanceof BoxFile.Info) {
                BoxFile.Info fileInfo = (BoxFile.Info) itemInfo;
                //System.out.println(fileInfo.getName());
                if (fileInfo.getName().equals(uploadedFileName)) {
                    sameFileName++;
                }
            }
        }

        if (sameFileName == 0) {
            try {
                FileInputStream inputStream = new FileInputStream(System.getProperty("user.dir") + "\\" + folderFromWeUpload + "\\" + localFileName);
                BoxFile.Info newFileInfo = uploadFolder.uploadFile(inputStream, uploadedFileName);
                inputStream.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
