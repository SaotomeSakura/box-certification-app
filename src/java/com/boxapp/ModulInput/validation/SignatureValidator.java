package src.java.com.boxapp.ModulInput.validation;

import eu.europa.esig.dss.enumerations.Indication;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.model.DSSException;
import eu.europa.esig.dss.service.http.commons.CommonsDataLoader;
import eu.europa.esig.dss.service.http.commons.FileCacheDataLoader;
import eu.europa.esig.dss.simplereport.SimpleReport;
import eu.europa.esig.dss.spi.client.http.DSSFileLoader;
import eu.europa.esig.dss.spi.client.http.IgnoreDataLoader;
import eu.europa.esig.dss.spi.tsl.TrustedListsCertificateSource;
import eu.europa.esig.dss.spi.x509.CertificateSource;
import eu.europa.esig.dss.spi.x509.KeyStoreCertificateSource;
import eu.europa.esig.dss.tsl.alerts.LOTLAlert;
import eu.europa.esig.dss.tsl.alerts.TLAlert;
import eu.europa.esig.dss.tsl.alerts.detections.LOTLLocationChangeDetection;
import eu.europa.esig.dss.tsl.alerts.detections.OJUrlChangeDetection;
import eu.europa.esig.dss.tsl.alerts.detections.TLExpirationDetection;
import eu.europa.esig.dss.tsl.alerts.detections.TLSignatureErrorDetection;
import eu.europa.esig.dss.tsl.alerts.handlers.log.LogLOTLLocationChangeAlertHandler;
import eu.europa.esig.dss.tsl.alerts.handlers.log.LogOJUrlChangeAlertHandler;
import eu.europa.esig.dss.tsl.alerts.handlers.log.LogTLExpirationAlertHandler;
import eu.europa.esig.dss.tsl.alerts.handlers.log.LogTLSignatureErrorAlertHandler;
import eu.europa.esig.dss.tsl.cache.CacheCleaner;
import eu.europa.esig.dss.tsl.function.OfficialJournalSchemeInformationURI;
import eu.europa.esig.dss.tsl.job.TLValidationJob;
import eu.europa.esig.dss.tsl.source.LOTLSource;
import eu.europa.esig.dss.tsl.sync.AcceptAllStrategy;
import eu.europa.esig.dss.validation.CertificateVerifier;
import eu.europa.esig.dss.validation.SignaturePolicyProvider;
import eu.europa.esig.dss.validation.SignedDocumentValidator;
import eu.europa.esig.dss.validation.executor.ValidationLevel;
import eu.europa.esig.dss.validation.reports.Reports;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * Helper class wrapping calls to DSS for validating AdES signatures.
 *
 */
public class SignatureValidator {
    private static final String LOTL_URL = "https://ec.europa.eu/tools/lotl/eu-lotl.xml";
    private static final String OJ_URL = "https://eur-lex.europa.eu/legal-content/EN/TXT/?uri=uriserv:OJ.C_.2019.276.01.0001.01.ENG";


    public static SignatureValidatorOutput validateDocumentSignature(DSSDocument documentToValidate, CertificateVerifier certificateVerifier, Map<String, DSSDocument> signaturePolicies) {
        if (documentToValidate == null) throw new IllegalArgumentException("documentToValidate");
        if (certificateVerifier == null) throw new IllegalArgumentException("certificateVerifier");

        SignedDocumentValidator validator = SignedDocumentValidator.fromDocument(documentToValidate);

        /*
        Set validation level.
         */
        validator.setValidationLevel(ValidationLevel.ARCHIVAL_DATA);

        /*
        Set certificate verifier.
         */
        validator.setCertificateVerifier(certificateVerifier);

        /*
        Add signature policies, if any.
         */
        if (signaturePolicies != null) {
            SignaturePolicyProvider signaturePolicyProvider = new SignaturePolicyProvider();
            signaturePolicyProvider.setSignaturePoliciesById(signaturePolicies);
            validator.setSignaturePolicyProvider(signaturePolicyProvider);
        }


        /*
        Validate.
         */
        Reports reports = validator.validateDocument();
        return new SignatureValidatorOutput(validator.getSignatures(), reports);
    }

    public static int getNumberOfFailedSignatures(SimpleReport simpleReport) {
        int c = 0;
        for (String id : simpleReport.getSignatureIdList()) {
            Indication indication = simpleReport.getIndication(id);
            if ((indication != Indication.TOTAL_PASSED) && (indication != Indication.INDETERMINATE)) c++;
        }
        return c;
    }

    public static  int getNumberOfIndeterminateSignatures(SimpleReport simpleReport) {
        int c = 0;
        for (String id : simpleReport.getSignatureIdList()) {
            Indication indication = simpleReport.getIndication(id);
            if (indication == Indication.INDETERMINATE) c++;
        }
        return c;
    }



    public static TLValidationJob job() {
        TLValidationJob job = new TLValidationJob();
        job.setOfflineDataLoader(offlineLoader());
        job.setOnlineDataLoader(onlineLoader());
        job.setTrustedListCertificateSource(trustedCertificateSource());
        job.setSynchronizationStrategy(new AcceptAllStrategy());
        job.setCacheCleaner(cacheCleaner());

        LOTLSource europeanLOTL = europeanLOTL();
        job.setListOfTrustedListSources(europeanLOTL);
//        job.setAlerts(Arrays.asList(tlSigningAlert(), tlExpirationDetection(), ojUrlAlert(europeanLOTL),
//                lotlLocationAlert(europeanLOTL)));

        job.onlineRefresh();
        return job;
    }

    public static TrustedListsCertificateSource trustedCertificateSource() {
        return new TrustedListsCertificateSource();
    }

    public static LOTLSource europeanLOTL() {
        LOTLSource lotlSource = new LOTLSource();
        lotlSource.setUrl(LOTL_URL);
		lotlSource.setCertificateSource(officialJournalContentKeyStore());
        System.out.println(" MNAU " + lotlSource.getCertificateSource().getCertificates());
//        lotlSource.setCertificateSource(new CommonCertificateSource());
        lotlSource.setSigningCertificatesAnnouncementPredicate(new OfficialJournalSchemeInformationURI(OJ_URL));
//        lotlSource.setPivotSupport(true);

//        lotlSource.getCertificateSource();
        return lotlSource;
    }


    public static CertificateSource officialJournalContentKeyStore() {
        try {
            return new KeyStoreCertificateSource(new File("src/resources/keystore.p12"), "PKCS12", "dss-password");
        } catch (IOException e) {
            throw new DSSException("Unable to load the keystore", e);
        }
    }

    public static DSSFileLoader offlineLoader() {
        FileCacheDataLoader offlineFileLoader = new FileCacheDataLoader();
        offlineFileLoader.setCacheExpirationTime(Long.MAX_VALUE);
        offlineFileLoader.setDataLoader(new IgnoreDataLoader());
        offlineFileLoader.setFileCacheDirectory(tlCacheDirectory());
        return offlineFileLoader;
    }

    public static DSSFileLoader onlineLoader() {
        FileCacheDataLoader onlineFileLoader = new FileCacheDataLoader();
        onlineFileLoader.setCacheExpirationTime(0);
        onlineFileLoader.setDataLoader(dataLoader());
        onlineFileLoader.setFileCacheDirectory(tlCacheDirectory());
        return onlineFileLoader;
    }

    public static File tlCacheDirectory() {
        File rootFolder = new File(System.getProperty("java.io.tmpdir"));
        File tslCache = new File(rootFolder, "dss-tsl-loader");
        if (tslCache.mkdirs()) {
            System.out.println("TL Cache folder : {" + tslCache.getAbsolutePath() + "}");
        }
        return tslCache;
    }

    public static CommonsDataLoader dataLoader() {
        return new CommonsDataLoader();
    }

    public static CacheCleaner cacheCleaner() {
        CacheCleaner cacheCleaner = new CacheCleaner();
        cacheCleaner.setCleanMemory(true);
        cacheCleaner.setCleanFileSystem(true);
//        cacheCleaner.setDSSFileLoader(offlineLoader());
        cacheCleaner.setDSSFileLoader(getDSSFileLoader());
        return cacheCleaner;
    }

    private static FileCacheDataLoader getDSSFileLoader() {
        FileCacheDataLoader fileLoader = new FileCacheDataLoader();
        fileLoader.setCacheExpirationTime(0);
        fileLoader.setFileCacheDirectory(tlCacheDirectory());
        return fileLoader;
    }

    // Recommended detections : OJUrlChangeDetection + LOTLLocationChangeDetection
    public static TLAlert tlSigningAlert() {
        TLSignatureErrorDetection signingDetection = new TLSignatureErrorDetection();
        LogTLSignatureErrorAlertHandler handler = new LogTLSignatureErrorAlertHandler();
        return new TLAlert(signingDetection, handler);
    }

    public static TLAlert tlExpirationDetection() {
        TLExpirationDetection expirationDetection = new TLExpirationDetection();
        LogTLExpirationAlertHandler handler = new LogTLExpirationAlertHandler();
        return new TLAlert(expirationDetection, handler);
    }

    public static LOTLAlert ojUrlAlert(LOTLSource source) {
        OJUrlChangeDetection ojUrlDetection = new OJUrlChangeDetection(source);
        LogOJUrlChangeAlertHandler handler = new LogOJUrlChangeAlertHandler();
        return new LOTLAlert(ojUrlDetection, handler);
    }

    public static LOTLAlert lotlLocationAlert(LOTLSource source) {
        LOTLLocationChangeDetection lotlLocationDetection = new LOTLLocationChangeDetection(source);
        LogLOTLLocationChangeAlertHandler handler = new LogLOTLLocationChangeAlertHandler();
        return new LOTLAlert(lotlLocationDetection, handler);
    }











}
