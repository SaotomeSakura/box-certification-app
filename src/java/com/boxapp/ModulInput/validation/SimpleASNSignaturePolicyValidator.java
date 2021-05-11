package src.java.com.boxapp.ModulInput.validation;

import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.pades.validation.PAdESSignature;
import eu.europa.esig.dss.spi.DSSASN1Utils;
import eu.europa.esig.dss.spi.DSSUtils;
import eu.europa.esig.dss.utils.Utils;
import eu.europa.esig.dss.validation.SignaturePolicy;
import eu.europa.esig.dss.validation.policy.BasicASNSignaturePolicyValidator;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleASNSignaturePolicyValidator extends BasicASNSignaturePolicyValidator {
    private static final Logger LOG = LoggerFactory.getLogger(BasicASNSignaturePolicyValidator.class);

    @Override
    public void validate() {
        final SignaturePolicy signaturePolicy = getSignaturePolicy();

        final DSSDocument policyContent = signaturePolicy.getPolicyContent();
        final byte[] policyBytes = DSSUtils.toByteArray(policyContent);
        final DigestAlgorithm signPolicyHashAlgFromSignature = signaturePolicy.getDigest().getAlgorithm();

        setStatus(true);
        setIdentified(true);

        try {
            final ASN1Sequence asn1Sequence = DSSASN1Utils.toASN1Primitive(policyBytes);

            if (asn1Sequence != null) {
                setAsn1Processable(true);

                /**
                 * a) If the resulting document is based on TR 102 272 [i.2] (ESI: ASN.1 format for signature policies),
                 * use the digest value present in the
                 * SignPolicyDigest element from the resulting document. Check that the digest algorithm indicated in
                 * the SignPolicyDigestAlg from the resulting
                 * document is equal to the digest algorithm indicated in the property.
                 */

                final ASN1Sequence signPolicyHashAlgObject = (ASN1Sequence) asn1Sequence.getObjectAt(0);
                final AlgorithmIdentifier signPolicyHashAlgIdentifier = AlgorithmIdentifier.getInstance(signPolicyHashAlgObject);
                final DigestAlgorithm signPolicyHashAlgFromPolicy = DigestAlgorithm.forOID(signPolicyHashAlgIdentifier.getAlgorithm().getId());

                /**
                 * b) If the resulting document is based on TR 102 038 [i.3] ((ESI) XML format for signature policies),
                 * use the digest value present in
                 * signPolicyHash element from the resulting document. Check that the digest algorithm indicated in the
                 * signPolicyHashAlg from the resulting
                 * document is equal to the digest algorithm indicated in the attribute.
                 */

                /**
                 * The use of a zero-sigPolicyHash value is to ensure backwards compatibility with earlier versions of
                 * the current document. If sigPolicyHash is
                 * zero, then the hash value should not be checked against the calculated hash value of the signature
                 * policy.
                 */
                if (!signPolicyHashAlgFromPolicy.equals(signPolicyHashAlgFromSignature)) {
                    addError("general", "The digest algorithm indicated in the SignPolicyHashAlg from the resulting document (" + signPolicyHashAlgFromPolicy
                            + ") is not equal to the digest " + "algorithm (" + signPolicyHashAlgFromSignature + ").");
                    setDigestAlgorithmsEqual(false);
                    setStatus(false);
                    return;
                } else {
                    setDigestAlgorithmsEqual(true);
                }

                // Recalculate the ASN1 digest and compare it to the digest value in the policy.
                // If they are equal, then we know that the ASN1 policy (content) itself is formally valid.
                final String asn1PolicyDigestValue = Utils.toBase64(DSSASN1Utils.getAsn1SignaturePolicyDigest(signPolicyHashAlgFromPolicy, policyBytes));
                final ASN1OctetString signPolicyHash = (ASN1OctetString) asn1Sequence.getObjectAt(2);
                final String policyDigestValueFromPolicy = Utils.toBase64(signPolicyHash.getOctets());
                boolean equal = Utils.areStringsEqual(asn1PolicyDigestValue, policyDigestValueFromPolicy);
                setStatus(equal);
                if (!equal) {
                    addError("general", "The re-calculated policy digest value (" + asn1PolicyDigestValue + ") does not match the digest value from the policy file (" + policyDigestValueFromPolicy + ").");
                    return;
                }

                final String policyDigestValueFromSignature = signaturePolicy.getDigest().getValue().toString();
                if (getSignature() instanceof PAdESSignature == false) {
                    // For non-PAdES signatures compare the policy digest value from the signature and the digest value in the policy.
                    // If they are equal, then we know that the policy from the signature is corresponding to this policy.
                    equal = Utils.areStringsEqual(policyDigestValueFromSignature, policyDigestValueFromPolicy);
                    setStatus(equal);
                    if (!equal) {
                        addError("general", "The policy digest value from the signature (" + policyDigestValueFromSignature + ") does not match the digest value from the policy file (" + policyDigestValueFromPolicy + ").");
                    }
                } else {
                    // For PAdES signatures compare the policy digest value from the signature and the digest value of the whole policy.
                    // If they are equal, then we know that the policy from the signature is corresponding to this policy.
                    final String policyDigestValue = Utils.toBase64(DSSUtils.digest(signPolicyHashAlgFromPolicy, policyBytes));
                    equal = Utils.areStringsEqual(policyDigestValueFromSignature, policyDigestValue);
                    setStatus(equal);
                    if (!equal) {
                        addError("general", "The policy digest value from the signature (" + policyDigestValueFromSignature + ") does not match the digest value of the policy file (" + policyDigestValue + ").");
                    }
                }
            }
        } catch (Exception e) {
            // When any error (communication) we just set the status to false
            setStatus(false);
            addError("general", e.getMessage());
            // Do nothing
            LOG.warn(e.getMessage(), e);
        }
    }
}
