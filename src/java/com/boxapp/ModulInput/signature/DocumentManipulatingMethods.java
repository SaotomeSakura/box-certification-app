package src.java.com.boxapp.ModulInput.signature;

import eu.europa.esig.dss.enumerations.SignatureLevel;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.model.InMemoryDocument;
import eu.europa.esig.dss.service.crl.OnlineCRLSource;
import eu.europa.esig.dss.service.http.commons.CommonsDataLoader;
import eu.europa.esig.dss.service.http.commons.OCSPDataLoader;
import eu.europa.esig.dss.service.http.commons.TimestampDataLoader;
import eu.europa.esig.dss.service.ocsp.OnlineOCSPSource;
import eu.europa.esig.dss.service.tsp.OnlineTSPSource;
import eu.europa.esig.dss.spi.tsl.TrustedListsCertificateSource;
import eu.europa.esig.dss.token.PasswordInputCallback;
import eu.europa.esig.dss.tsl.job.TLValidationJob;
import eu.europa.esig.dss.validation.CertificateVerifier;
import eu.europa.esig.dss.validation.CommonCertificateVerifier;
import src.java.com.boxapp.ModulAplication.fileManipulation.IDsAndTokens;
import src.java.com.boxapp.ModulInput.validation.SignatureValidator;

import java.io.*;
import java.util.ArrayList;

public class DocumentManipulatingMethods {
//    static SignatureLevel signatureLevel;


    public static DSSDocument signingOfDocument(File signFile, File output) {
//        File signFile = new File(System.getProperty("user.dir") + "\\DownloadTo\\" + item.getFile());
//        File output = new File(System.getProperty("user.dir") + "\\DownloadTo\\" + item.getFile());

        PasswordInputCallback password = new PasswordInputCallback() {
            @Override
            public char[] getPassword() {
                return new char[0];
            }
        };

        ExtToken token = new ExtToken(IDsAndTokens.pkcs11Path, password, 1);
        SignatureValidator.officialJournalContentKeyStore().addCertificate(token.getKeys().get(0).getCertificate());
        System.out.println("tu");
        System.out.println(token.getKeys().get(0).getCertificateChain());

        String methodName = new Object() {
        }.getClass().getEnclosingMethod().getName();

        DSSDocument documentToSign = null;
        try {
            documentToSign = new InMemoryDocument(new FileInputStream(signFile), methodName + ".in.pdf");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }


        OnlineTSPSource tspSource = new OnlineTSPSource(IDsAndTokens.tspServer);
        tspSource.setDataLoader(new TimestampDataLoader()); // uses the specific content-type

        CertificateVerifier certificateVerifier = new CommonCertificateVerifier();

        TLValidationJob job = SignatureValidator.job();
        TrustedListsCertificateSource trustedListsCertificateSource = new TrustedListsCertificateSource();

        job.setTrustedListCertificateSource(trustedListsCertificateSource);



//        TrustedListsCertificateSource certificateSource = new TrustedListsCertificateSource();
//        trustedListsCertificateSource.importAsTrusted(certificateSource);

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

        DSSDocument signedDocument = SignatureCreator.createPAdES(documentToSign, null, tspSource, token, token.getKeys().get(0));
//        DSSDocument signedDocument = SignatureCreator.createPAdES(documentToSign, null, null, token, token.getKeys().get(0));
        try {
            signedDocument.save(output.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        token.close();
        return signedDocument;
    }

    public static DSSDocument extendingOfDocument(File extendFile, File output){

//        File fileExtend = new File(System.getProperty("user.dir") + "\\Download\\Signed.pdf");

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
        job.setTrustedListCertificateSource(trustedListsCertificateSource);


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

        String methodNameExtend = new Object() {
        }.getClass().getEnclosingMethod().getName();

        DSSDocument documentToExtend = null;
        try {
            documentToExtend = new InMemoryDocument(new FileInputStream(extendFile), methodNameExtend + ".in.pdf");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        // Set the Timestamp source
        OnlineTSPSource onlineTSPSource = new OnlineTSPSource(IDsAndTokens.tspServer);
        onlineTSPSource.setDataLoader(new TimestampDataLoader()); // uses the specific content-type


//        OnlineTSPSource onlineTSPSource = new OnlineTSPSource("http://tsa.belgium.be/connect");
//        onlineTSPSource.setDataLoader(new TimestampDataLoader());

//        Map<String, DSSDocument> signaturePolicies = new HashMap<>();
//        File[] files = new File(System.getProperty("user.dir") + "\\policyall-xml").listFiles();
//        int index = 0;
//        for (File item : files) {
//            DSSDocument policy = null;
//            try {
//                policy = new InMemoryDocument(new FileInputStream(item), item.getName() + ".in.pdf");
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//            }
//            signaturePolicies.put(String.valueOf(index), policy);
//            index++;
//        }

        DSSDocument extendedDocumentLTA = SignatureCreator.extendPAdES(documentToExtend, SignatureLevel.PAdES_BASELINE_LTA, onlineTSPSource, certificateVerifier);

        try {
            extendedDocumentLTA.save(output.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return extendedDocumentLTA;
    }


//    public static boolean hasValidSignature(FileToManipulate file) throws IOException {
////        CommonTrustedCertificateSource tcl = null;
////        try {
////            tcl = SignatureValidator.getTrustedLists();
////        } catch (IOException e) {
////            e.printStackTrace();
////        }
////        certificateVerifier.setTrustedCertSource(tcl);
//
//        CertificateVerifier certificateVerifier = new CommonCertificateVerifier();
//
//        TLValidationJob job = SignatureValidator.job();
//        TrustedListsCertificateSource trustedListsCertificateSource = new TrustedListsCertificateSource();
//        job.setTrustedListCertificateSource(trustedListsCertificateSource);
//
//        OnlineOCSPSource onlineOCSPSource = new OnlineOCSPSource();
//        OCSPDataLoader ocspDataLoader = new OCSPDataLoader();
//        onlineOCSPSource.setDataLoader(ocspDataLoader);
//
//        OnlineCRLSource onlineCRLSource = new OnlineCRLSource();
//        CommonsDataLoader crlDataLoader = new CommonsDataLoader();
//        onlineCRLSource.setDataLoader(crlDataLoader);
//
//        certificateVerifier.setTrustedCertSources(trustedListsCertificateSource);
//        CommonsDataLoader certificateVerifierDataLoader = new CommonsDataLoader();
//        certificateVerifier.setDataLoader(certificateVerifierDataLoader);
//        certificateVerifier.setOcspSource(onlineOCSPSource);
//        certificateVerifier.setCrlSource(onlineCRLSource);
//        certificateVerifier.setIncludeCertificateTokenValues(true);
//
//
//
//
//
//        File fileValidate = new File(IDsAndTokens.downloadToFolder + file.getFileName());
//
//        String methodNameValidate = new Object() {
//        }.getClass().getEnclosingMethod().getName();
//
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
//
////        signatureLevel = outputReport.getSignatureFormat(String.valueOf(outputReport.getSignaturesCount()));
//
//
//        FileUtils.writeStringToFile(new File(IDsAndTokens.downloadToFolder + "_simple-report.xml"), output.getReports().getXmlSimpleReport(), "UTF-8");
//        System.out.print(file.getFileName() + " ");
//        System.out.println("Ma to valid? " + outputReport.getSignatureFormat(String.valueOf(outputReport.getSignaturesCount())));
//        if(outputReport.getValidSignaturesCount() > 0){
//            CreateButtons.signatureLevelMap.put(file.getFileName(), outputReport.getSignatureFormat(String.valueOf(outputReport.getSignaturesCount())));
//            System.out.println("returning true");
//            return true;
//        } else {
//            System.out.println("returning false");
//            return false;
//        }
//
//    }



    public static void addLineToDateDocument(String line) {
        File fileWithDates = new File(IDsAndTokens.expiryDatesFile);
        Writer output;
        //clears file every time
        try {
            output = new BufferedWriter(new FileWriter(fileWithDates));
            output.append(line);
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeToFile(File file, ArrayList<String> allLines) {
        FileWriter writer = null;
        try {
            writer = new FileWriter(file);
            writer.write("");
            for (String line : allLines){
                writer.write(line + "\n");
            }
            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void removeLine(ArrayList<String> allLines, ArrayList<String> downloadedLines) {
        allLines.removeAll(downloadedLines);
    }


}
