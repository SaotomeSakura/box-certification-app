package src.java.com.boxapp.ModulAplication.GridManipulation;

import com.box.sdk.BoxFile;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Input;
import eu.europa.esig.dss.detailedreport.DetailedReport;
import eu.europa.esig.dss.enumerations.SignatureLevel;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.model.InMemoryDocument;
import eu.europa.esig.dss.service.crl.OnlineCRLSource;
import eu.europa.esig.dss.service.http.commons.CommonsDataLoader;
import eu.europa.esig.dss.service.http.commons.OCSPDataLoader;
import eu.europa.esig.dss.service.ocsp.OnlineOCSPSource;
import eu.europa.esig.dss.simplereport.SimpleReport;
import eu.europa.esig.dss.spi.tsl.*;
import eu.europa.esig.dss.tsl.job.TLValidationJob;
import eu.europa.esig.dss.validation.CertificateVerifier;
import eu.europa.esig.dss.validation.CommonCertificateVerifier;
import org.apache.commons.io.FileUtils;
import src.java.com.boxapp.ModulAplication.fileManipulation.IDsAndTokens;
import src.java.com.boxapp.ModulAplication.fileManipulation.UploadOrDownload;
import src.java.com.boxapp.ModulInput.signature.DocumentManipulatingMethods;
import src.java.com.boxapp.ModulInput.validation.SignatureValidator;
import src.java.com.boxapp.ModulInput.validation.SignatureValidatorOutput;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class CreateButtons {
    static HashMap<String, Boolean> validityMap = new HashMap<>();
    static HashMap<String, SignatureLevel> signatureLevelMap = new HashMap<>();
    static HashMap<String, String> certExpiryDateMap = new HashMap<>();
    static HashMap<String, String> downloadedArchiveLines = new HashMap<>();



    public static Button createDeleteButton(FileToManipulate item) {
        @SuppressWarnings("unchecked")
        Button fileButton = new Button("Delete");
        BoxFile file = new BoxFile(IDsAndTokens.getApi(), item.getFileId());
        ArrayList<String> deletedLines = new ArrayList<>();
        ArrayList<String> allLines = new ArrayList<>();
        File fileWithDates = new File(IDsAndTokens.expiryDatesFile);
        fileButton.addClickListener(e -> {

            try (Scanner read = new Scanner(fileWithDates)) {
                while (read.hasNextLine()) {
                    String row = read.nextLine();
                    allLines.add(row);
                    String[] parts = row.split(",");
                    String fileName = parts[0];
                    if(fileName.equals(item.getFileName())) {
//                    if (row.contains(item.getFileName())) {
                        deletedLines.add(row);
                    }
                }
                DocumentManipulatingMethods.removeLine(allLines, deletedLines);
                DocumentManipulatingMethods.writeToFile(fileWithDates, allLines);
                allLines.clear();
                deletedLines.clear();
            } catch (FileNotFoundException exception) {
                exception.printStackTrace();
            }

            file.delete();
//            DocumentManipulatingMethods.removeLine(allLines, deletedLines);
            UI.getCurrent().getPage().reload();
        });
        return fileButton;
    }


    public static Button createDownloadButton(FileToManipulate item, String folderId, String localFolderId) {
        @SuppressWarnings("unchecked")
        Button fileButton = new Button("Download");
        ArrayList<String> downloadedLines = new ArrayList<>();
        ArrayList<String> allLines = new ArrayList<>();
        File fileWithDates = new File(IDsAndTokens.expiryDatesFile);

        fileButton.addClickListener(e -> {
            UploadOrDownload.downloadFileMethod(IDsAndTokens.getApi(), item.getFileName(), folderId, localFolderId);

            if (folderId.equals(IDsAndTokens.folderArchiveID)) {
                try (Scanner read = new Scanner(fileWithDates)) {
                    while (read.hasNextLine()) {
                        String row = read.nextLine();
                        allLines.add(row);
                        String[] parts = row.split(",");
                        String fileName = parts[0];
                        if(fileName.equals(item.getFileName())) {
//                    if (row.contains(item.getFileName())) {
                            downloadedLines.add(row);
                            downloadedArchiveLines.put(item.getFileName(), parts[2]);
                        }
                    }
                    DocumentManipulatingMethods.removeLine(allLines, downloadedLines);
                    DocumentManipulatingMethods.writeToFile(fileWithDates, allLines);
                    allLines.clear();
                    downloadedLines.clear();
                } catch (FileNotFoundException exception) {
                    exception.printStackTrace();
                }
//                DocumentManipulatingMethods.removeLine(allLines, downloadedLines);
//                DocumentManipulatingMethods.writeToFile(fileWithDates, allLines);
//                allLines.clear();
//                downloadedLines.clear();

            }
        });
        return fileButton;
    }

    public static Button createUploadButton(FileToManipulate item, String folderId) {
        @SuppressWarnings("unchecked")
        Button fileButton = new Button("Upload");
        fileButton.addClickListener(e -> {
//            UploadOrDownload.uploadToFolderMethod(IDsAndTokens.getApi(), item.getFile(), item.getFile(), folderId, IDsAndTokens.downloadFolder);
            Dialog archiveDateDialog = new Dialog();
            Input dateInput = new Input();
            archiveDateDialog.add(new Text("Zadajte počet rokov na archiváciu: "));
            archiveDateDialog.setWidth("400px");
            archiveDateDialog.setHeight("150px");
            archiveDateDialog.add(dateInput);

            archiveDateDialog.open();
            Button archivationButton = new Button("Uložte dobu archvácie");
            archiveDateDialog.add(archivationButton);
            archivationButton.addClickListener(d -> {
                int date = Integer.valueOf(dateInput.getValue());
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd");
                LocalDate today = LocalDate.now();
                LocalDate plusYears = today.plusYears(date);
                String archivationDate = plusYears.format(dtf);
                String certExpirationDate = certExpiryDateMap.get(item.getFileName());
                if (date != 0) {
                    String lineToAdd = item.getFileName() + "," + certExpirationDate + "," + archivationDate;

                    DocumentManipulatingMethods.addLineToDateDocument(lineToAdd);

                    UploadOrDownload.uploadToFolderMethod(IDsAndTokens.getApi(), item.getFileName(), item.getFileName(), folderId, IDsAndTokens.downloadToFolder);
                    validityMap.remove(item.getFileName());
                    signatureLevelMap.remove(item.getFileName());
                    certExpiryDateMap.remove(item.getFileName());
                }
                File deleteFile = new File(IDsAndTokens.downloadToFolder + item.getFileName());
                deleteFile.delete();

                archiveDateDialog.close();


            });
            UI.getCurrent().getPage().reload();
        });
        return fileButton;
    }

    public static Button createLocalDeleteButton(FileToManipulate item) {
        @SuppressWarnings("unchecked")
        Button fileButton = new Button("Delete");
        File deleteFile = new File(IDsAndTokens.downloadToFolder + item.getFileName());
        fileButton.addClickListener(e -> {
            deleteFile.delete();
        });
        return fileButton;
    }

    public static Button createSignButton(FileToManipulate item) {
        Button signButton = new Button("Sign");

        signButton.addClickListener(event -> {
            File signFile = new File(IDsAndTokens.downloadToFolder + item.getFileName());
            File output = new File(IDsAndTokens.downloadToFolder + item.getFileName());

            DocumentManipulatingMethods.signingOfDocument(signFile, output);
//            validityMap.put(item.getFileName(), true);
//            signatureLevelMap.put(item.getFileName(), SignatureLevel.PAdES_BASELINE_T);
            DocumentManipulatingMethods.extendingOfDocument(signFile, output);
//            signatureLevelMap.put(item.getFileName(), SignatureLevel.PAdES_BASELINE_LTA);
            try {
                hasValidSignature(item);
            } catch (IOException e) {
                e.printStackTrace();
            }
            UI.getCurrent().getPage().reload();
        });

        return signButton;
    }

    public static Button createValidateButton(FileToManipulate file) {
        final Button[] validateButton = {new Button("Validate")};
        validateButton[0].addClickListener(e -> {
            try {
                validityMap.put(file.getFileName(), hasValidSignature(file));
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
            UI.getCurrent().getPage().reload();
        });

        if (validityMap.containsKey(file.getFileName())) {
            if (signatureLevelMap.containsKey(file.getFileName())) {
                if (signatureLevelMap.get(file.getFileName()).equals(SignatureLevel.PAdES_BASELINE_LTA)) {
                    validateButton[0] = createUploadButton(file, IDsAndTokens.folderArchiveID);
                } else if (!signatureLevelMap.get(file.getFileName()).equals(SignatureLevel.PAdES_BASELINE_LTA)) {
                    validateButton[0] = createExtendButton(file);
                }
            } else {
                validateButton[0] = createSignButton(file);
            }
        }

        return validateButton[0];
    }

    public static Button createResignAndUploadButton(FileToManipulate item) {
        Button signButton = new Button("Resign and Upload");
        signButton.addClickListener(event -> {
            File resignFile = new File(IDsAndTokens.expiryFolder + item.getFileName());
            File output = new File(IDsAndTokens.expiryFolder + item.getFileName());
            DocumentManipulatingMethods.signingOfDocument(resignFile, output);
            DocumentManipulatingMethods.extendingOfDocument(resignFile, output);
            try {
                hasValidSignature(item);
            } catch (IOException e) {
                e.printStackTrace();
            }

            Dialog archiveDateDialog = new Dialog();
            Input dateInput = new Input();
            archiveDateDialog.add(new Text("Zadajte počet rokov na archiváciu: "));
            archiveDateDialog.setWidth("400px");
            archiveDateDialog.setHeight("150px");
            archiveDateDialog.add(dateInput);

            archiveDateDialog.open();
            Button archivationButton = new Button("Uložte dobu archivácie");
            archiveDateDialog.add(archivationButton);
            archivationButton.addClickListener(d -> {
                int date = Integer.valueOf(dateInput.getValue());
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd");
                LocalDate today = LocalDate.now();
//                LocalDate plusYears = today.plusYears(date);
//                String expiryDate = plusYears.format(dtf);
//                String archivationDate = plusYears.format(dtf);
                String archivationDate = downloadedArchiveLines.get(item.getFileName());
                String certExpirationDate = String.valueOf(certExpiryDateMap.get(item.getFileName()));

                if (date != 0) {
                    archivationDate = today.plusYears(date).format(dtf);
                }
                String lineToAdd = item.getFileName() + "," + certExpirationDate + "," + archivationDate;
                DocumentManipulatingMethods.addLineToDateDocument(lineToAdd);
                UploadOrDownload.uploadToFolderMethod(IDsAndTokens.getApi(), item.getFileName(), item.getFileName(), IDsAndTokens.folderArchiveID, IDsAndTokens.expiryFolder);

                downloadedArchiveLines.remove(item.getFileName());
                certExpiryDateMap.remove(item.getFileName());

                File deleteFile = new File(IDsAndTokens.downloadToFolder + item.getFileName());
                deleteFile.delete();

                archiveDateDialog.close();

            });

//            UploadOrDownload.uploadToFolderMethod(IDsAndTokens.getApi(), item.getFileName(), item.getFileName(), IDsAndTokens.folderArchiveID, IDsAndTokens.expiryFolder);
            UI.getCurrent().getPage().reload();
        });
        return signButton;
    }


    public static Button createExtendButton(FileToManipulate item) {
        Button extendButton = new Button("Extend");
        extendButton.addClickListener(event -> {
            File extendFile = new File(IDsAndTokens.downloadToFolder + item.getFileName());
            File output = new File(IDsAndTokens.downloadToFolder + item.getFileName());
            DocumentManipulatingMethods.extendingOfDocument(extendFile, output);

            signatureLevelMap.put(item.getFileName(), SignatureLevel.PAdES_BASELINE_LTA);
            UI.getCurrent().getPage().reload();
        });
        return extendButton;
    }



    public static boolean hasValidSignature(FileToManipulate file) throws IOException {
//        CommonTrustedCertificateSource tcl = null;
//        try {
//            tcl = SignatureValidator.getTrustedLists();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        certificateVerifier.setTrustedCertSource(tcl);

        CertificateVerifier certificateVerifier = new CommonCertificateVerifier();

        TLValidationJob job = SignatureValidator.job();
        TrustedListsCertificateSource trustedListsCertificateSource = new TrustedListsCertificateSource();
//        CommonTrustedCertificateSource commonTrustedCertificateSource = new CommonTrustedCertificateSource()
        job.setTrustedListCertificateSource(trustedListsCertificateSource);

        // A cache content summary can be computed on request
        TLValidationJobSummary summary = job.getSummary();


        // All information about processed LOTLSources
        List<LOTLInfo> lotlInfos = summary.getLOTLInfos();
        LOTLInfo lotlInfo = lotlInfos.get(0);
        // All data about the download (last occurrence, cache status, error,...)
        DownloadInfoRecord downloadCacheInfo = lotlInfo.getDownloadCacheInfo();
        // All data about the parsing (date, extracted data, cache status,...)
        ParsingInfoRecord parsingCacheInfo = lotlInfo.getParsingCacheInfo();
        // All data about the signature validation (signing certificate, validation
        // result, cache status,...)
        ValidationInfoRecord validationCacheInfo = lotlInfo.getValidationCacheInfo();
        // All information about processed TLSources (which are not linked to a LOTLSource)
        List<TLInfo> otherTLInfos = summary.getOtherTLInfos();
        // or the last update can be collected from the TrustedListsCertificateSource
        TLValidationJobSummary lastSynchronizedSummary = trustedListsCertificateSource.getSummary();

        trustedListsCertificateSource.setSummary(summary);


//        trustedListsCertificateSource.isTrusted();
//        CommonTrustedCertificateSource trustedListsCertificateSource = new TrustedListsCertificateSource();


        OnlineOCSPSource onlineOCSPSource = new OnlineOCSPSource();
        OCSPDataLoader ocspDataLoader = new OCSPDataLoader();
        onlineOCSPSource.setDataLoader(ocspDataLoader);

        OnlineCRLSource onlineCRLSource = new OnlineCRLSource();
        CommonsDataLoader crlDataLoader = new CommonsDataLoader();
        onlineCRLSource.setDataLoader(crlDataLoader);


        certificateVerifier.setTrustedCertSources(trustedListsCertificateSource);
        CommonsDataLoader certificateVerifierDataLoader = new CommonsDataLoader();
        certificateVerifier.setDataLoader(certificateVerifierDataLoader);
        certificateVerifier.setOcspSource(onlineOCSPSource);
        certificateVerifier.setCrlSource(onlineCRLSource);
        certificateVerifier.setIncludeCertificateTokenValues(true);
        certificateVerifier.setCheckRevocationForUntrustedChains(true);


        File fileValidate = new File(IDsAndTokens.downloadToFolder + file.getFileName());

        String methodNameValidate = new Object() {
        }.getClass().getEnclosingMethod().getName();

        DSSDocument documentToValidate = null;
        try {
            documentToValidate = new InMemoryDocument(new FileInputStream(fileValidate), methodNameValidate + ".in.pdf");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

//        Map<String, DSSDocument> signaturePolicies = new HashMap<>();
//        File[] policyFiles = new File(IDsAndTokens.policyFolder).listFiles();
//        int index = 0;
//        for (File item : policyFiles) {
//            DSSDocument policy = null;
//            try {
//                policy = new InMemoryDocument(new FileInputStream(item), item.getName() + ".in.pdf");
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//            }
//            signaturePolicies.put(String.valueOf(index), policy);
//            index++;
//        }

//        SignatureValidatorOutput output = SignatureValidator.validateDocumentSignature(documentToValidate, certificateVerifier, signaturePolicies);
        SignatureValidatorOutput output = SignatureValidator.validateDocumentSignature(documentToValidate, certificateVerifier, null);
        SimpleReport outputReport = output.getReports().getSimpleReport();
        DetailedReport detailed = output.getReports().getDetailedReport();


        FileUtils.writeStringToFile(new File(IDsAndTokens.reportsFolder + file.getFileName() + "_simple-report.xml"), output.getReports().getXmlSimpleReport(), "UTF-8");
        FileUtils.writeStringToFile(new File(IDsAndTokens.reportsFolder + file.getFileName() + "_validation-report.xml"), output.getReports().getXmlValidationReport(), "UTF-8");
        FileUtils.writeStringToFile(new File(IDsAndTokens.reportsFolder + file.getFileName() + "_diagnostic-report.xml"), output.getReports().getXmlDiagnosticData(), "UTF-8");
        FileUtils.writeStringToFile(new File(IDsAndTokens.reportsFolder + file.getFileName() + "_detailed-report.xml"), output.getReports().getXmlDetailedReport(), "UTF-8");


        if(outputReport.getValidSignaturesCount() > 0){
//        if (outputReport.getSignaturesCount() > 0) {
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd");
            LocalDate notAfterReport = detailed.getBasicBuildingBlockById(detailed.getFirstSignatureId()).getSAV().getCryptographicInfo().getNotAfter()
                    .toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            String notAfter = notAfterReport.format(dtf);
//            signatureLevelMap.put(file.getFileName(), outputReport.getSignatureFormat(String.valueOf(outputReport.getSignaturesCount())));
            signatureLevelMap.put(file.getFileName(), outputReport.getSignatureFormat(outputReport.getFirstSignatureId()));
            certExpiryDateMap.put(file.getFileName(), notAfter);
            System.out.println("returning true");
            return true;
        } else {
            System.out.println("returning false");
            return false;
        }

    }


//    public static boolean hasValidSignature(FileToManipulate file){
//        CertificateVerifier certificateVerifier = new CommonCertificateVerifier();
//        certificateVerifier.setDataLoader(new CommonsDataLoader());
//        certificateVerifier.setOcspSource(new OnlineOCSPSource());
//        certificateVerifier.setCrlSource(new OnlineCRLSource());
//
//        CommonTrustedCertificateSource tcl = null;
//        try {
//            tcl = SignatureValidator.getTrustedLists();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        certificateVerifier.setTrustedCertSource(tcl);
//
//        File fileValidate = new File(IDsAndTokens.downloadToFolder + file.getFileName());
//        String methodNameValidate = new Object() {
//        }.getClass().getEnclosingMethod().getName();
//        DSSDocument documentToValidate = null;
//        try {
//            documentToValidate = new InMemoryDocument(new FileInputStream(fileValidate), methodNameValidate + ".in.pdf");
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
//
//        Map<String, DSSDocument> signaturePolicies = new HashMap<>();
//        File[] policyFiles = new File(IDsAndTokens.policyFolder).listFiles();
//        int index = 0;
//        for (File item : policyFiles) {
//            DSSDocument policy = null;
//            try {
//                policy = new InMemoryDocument(new FileInputStream(item), item.getName() + ".in.pdf");
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//            }
//            signaturePolicies.put(String.valueOf(index), policy);
//            index++;
//        }
//
//        SignatureValidatorOutput output = SignatureValidator.validateDocumentSignature(documentToValidate, certificateVerifier, signaturePolicies);
//        SimpleReport outputReport = output.getReports().getSimpleReport();
//        DiagnosticData diagnosticData = output.getReports().getDiagnosticData();
//
////        diagnosticData.getSignature
//
//
////        if(outputReport.getValidSignaturesCount() > 0){
////            return true;
////        } else {
////            return false;
////        }
//
//        if(outputReport.getValidSignaturesCount() > 0){
//            signatureLevelMap.put(file.getFileName(), outputReport.getSignatureFormat(String.valueOf(outputReport.getSignaturesCount())));
//            System.out.println("returning true");
//            return true;
//        } else {
//            System.out.println("returning false");
//            return false;
//        }
//
//    }


}
