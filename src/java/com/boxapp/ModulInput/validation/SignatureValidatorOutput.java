package src.java.com.boxapp.ModulInput.validation;

import eu.europa.esig.dss.validation.AdvancedSignature;
import eu.europa.esig.dss.validation.reports.Reports;

import java.util.List;

/**
 * Data holder class to hold signature validator output.
 *
 */
public class SignatureValidatorOutput {
    private List<AdvancedSignature> signatures;

    public List<AdvancedSignature> getSignatures() {
        return signatures;
    }

    private Reports reports;

    public Reports getReports() {
        return reports;
    }

    protected SignatureValidatorOutput(List<AdvancedSignature> signatures, Reports reports) {
        this.signatures = signatures;
        this.reports = reports;
    }
}
