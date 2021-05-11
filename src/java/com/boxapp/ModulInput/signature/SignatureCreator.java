package src.java.com.boxapp.ModulInput.signature;

import eu.europa.esig.dss.AbstractSignatureParameters;
import eu.europa.esig.dss.enumerations.SignatureLevel;
import eu.europa.esig.dss.enumerations.SignaturePackaging;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.model.SignatureValue;
import eu.europa.esig.dss.model.ToBeSigned;
import eu.europa.esig.dss.pades.PAdESSignatureParameters;
import eu.europa.esig.dss.pades.SignatureImageParameters;
import eu.europa.esig.dss.pades.signature.PAdESService;
import eu.europa.esig.dss.spi.x509.tsp.TSPSource;
import eu.europa.esig.dss.token.DSSPrivateKeyEntry;
import eu.europa.esig.dss.token.SignatureTokenConnection;
import eu.europa.esig.dss.validation.CertificateVerifier;
import eu.europa.esig.dss.validation.CommonCertificateVerifier;

/**
 * Helper class wrapping calls to DSS for creating PAdES
 */
public class SignatureCreator {
    private static void setSigningKey(AbstractSignatureParameters parameters, DSSPrivateKeyEntry keyEntry) {
        // The default signature algorithm is SignatureAlgorithm.RSA_SHA256
        parameters.setSigningCertificate(keyEntry.getCertificate()); // We set the signing certificate.
        parameters.setCertificateChain(keyEntry.getCertificateChain()); // We set the certificate chain.
        parameters.setEncryptionAlgorithm(keyEntry.getEncryptionAlgorithm());
        // Basic level parameters are created in AbstractSerializableSignatureParameters() and contain current date as the signing date.
    }

    /**
     * Creates a PAdES for a given file and signature parameters.
     * @param documentToSign
     * @param signatureImageParameters
     * @param tspSource
     * @param token
     * @param keyEntry
     * @return
     */
    public static DSSDocument createPAdES(DSSDocument documentToSign, SignatureImageParameters signatureImageParameters, TSPSource tspSource, SignatureTokenConnection token, DSSPrivateKeyEntry keyEntry) {
        if (documentToSign == null) throw new IllegalArgumentException("documentToSign");
        if (token == null) throw new IllegalArgumentException("token");
        if (keyEntry == null) throw new IllegalArgumentException("keyEntry");

        /*
        Signature parameters.
         */
        PAdESSignatureParameters parameters = new PAdESSignatureParameters();
        setSigningKey(parameters, keyEntry);
        // We choose the level of the signature (-B, -T).
        if (tspSource == null) parameters.setSignatureLevel(SignatureLevel.PAdES_BASELINE_B);
        else parameters.setSignatureLevel(SignatureLevel.PAdES_BASELINE_T);
        parameters.setSignaturePackaging(SignaturePackaging.ENVELOPED); // We choose the type of the signature packaging (ENVELOPING, DETACHED).
        parameters.setContentSize(16384); // Set the signature size to 16kB instead of the default size 9472B (see https://ec.europa.eu/cefdigital/tracker/browse/DSS-1248).

        // Set the signature image if we are creating a visible PAdES.
        if (signatureImageParameters != null) parameters.setImageParameters(signatureImageParameters);

        /*
        Certificate verifier.
         */
        // - the source of trusted cert (based on the TL);
        // - the source of intermediate certificates used to build the certificate parameters.setSigningCertificate(keyEntry.getCertificate());  till the trust anchor. This source is only needed when these certificates are not included in the signature itself;
        // - the source of OCSP, CRL.
        CommonCertificateVerifier certificateVerifier = new CommonCertificateVerifier();

        /*
        Signature creation.
         */
        PAdESService service = new PAdESService(certificateVerifier);
        if (tspSource != null) service.setTspSource(tspSource);

        // Get the SignedInfo segment that need to be signed.
        ToBeSigned dataToSign = service.getDataToSign(documentToSign, parameters);

        // This function obtains the signature value for signed information using the private key and specified algorithm.
        SignatureValue signatureValue = token.sign(dataToSign, parameters.getDigestAlgorithm(), keyEntry);

        // We invoke the service to sign the document with the signature value obtained in the previous step.
        DSSDocument signedDocument = service.signDocument(documentToSign, parameters, signatureValue);
        return signedDocument;
    }

    public static DSSDocument extendPAdES(DSSDocument toExtendDocument, SignatureLevel signatureLevel, TSPSource tspSource, CertificateVerifier certificateVerifier) {
        if (toExtendDocument == null) throw new IllegalArgumentException("toExtendDocument");
        if (signatureLevel == null) throw new IllegalArgumentException("signatureLevel");
        if (tspSource == null) throw new IllegalArgumentException("tspSource");
        if (certificateVerifier == null) {
            if (signatureLevel == SignatureLevel.PAdES_BASELINE_LT || signatureLevel == SignatureLevel.PAdES_BASELINE_LTA)
                throw new IllegalArgumentException("certificateVerifier");
            else certificateVerifier = new CommonCertificateVerifier();
        }

        /*
        Signature parameters.
         */
        PAdESSignatureParameters parameters = new PAdESSignatureParameters();
        parameters.setSignatureLevel(signatureLevel);
        parameters.setSignaturePackaging(SignaturePackaging.ENVELOPED);
        parameters.setContentSize(16384);

        /*
        Signature extension.
         */
        PAdESService service = new PAdESService(certificateVerifier);
        service.setTspSource(tspSource);

        // We invoke the service to extend the signature.
        DSSDocument extendedDocument = service.extendDocument(toExtendDocument, parameters);
        return extendedDocument;
    }


}
