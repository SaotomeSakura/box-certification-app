package src.java.com.boxapp.ModulExspiration;


import src.java.com.boxapp.ModulAplication.fileManipulation.IDsAndTokens;
import src.java.com.boxapp.ModulAplication.fileManipulation.UploadOrDownload;
import src.java.com.boxapp.ModulInput.signature.DocumentManipulatingMethods;

import java.io.File;
import java.io.FileNotFoundException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

public class ExpiryCheck {

    public static void timingExpiry(){
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                ArrayList<String> downloadedLines = new ArrayList<>();
                ArrayList<String> allLines = new ArrayList<>();
                File fileWithDates = new File(IDsAndTokens.expiryDatesFile);

                try(Scanner read = new Scanner(fileWithDates)) {
                    while (read.hasNextLine()) {
                        String row = read.nextLine();
                        allLines.add(row);
                        String[] parts = row.split(",");
                        String fileName = parts[0];
                        String certExpiryDate = parts[1];
                        String fileArchiveDate = parts[2];

                        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd");
                        LocalDate expirationDate = LocalDate.parse(certExpiryDate, dtf);
                        LocalDate archivationDate = LocalDate.parse(fileArchiveDate, dtf);
                        LocalDate today = LocalDate.now();
                        LocalDate twoWeek = today.plusWeeks(2);

                        if (expirationDate.isBefore(twoWeek) && expirationDate.isAfter(today) && expirationDate.isBefore(archivationDate)) {
                            UploadOrDownload.downloadFileMethod(IDsAndTokens.getApi(), fileName, IDsAndTokens.folderArchiveID, IDsAndTokens.expiryFolder);
                            downloadedLines.add(row);
                        } else if (archivationDate.isBefore(twoWeek)){
                            UploadOrDownload.downloadFileMethod(IDsAndTokens.getApi(), fileName, IDsAndTokens.folderArchiveID, IDsAndTokens.expiredFilesFolder);
                            UploadOrDownload.uploadToFolderMethod(IDsAndTokens.getApi(), fileName, fileName, IDsAndTokens.folderExpiredID, IDsAndTokens.expiredFilesFolder);
                            downloadedLines.add(row);
                        }
                    }
                    DocumentManipulatingMethods.removeLine(allLines, downloadedLines);
                    DocumentManipulatingMethods.writeToFile(fileWithDates, allLines);
                    downloadedLines.clear();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }, 0, 60000);//wait 0 ms before doing the action and do it every 1000ms (1second)
    }



}
