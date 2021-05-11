package src.java.com.boxapp.ModulInput.signature;


import eu.europa.esig.dss.model.DSSException;

public class TokenActionCancelledException extends DSSException {

    private static final long serialVersionUID = 1L;

    public TokenActionCancelledException() {
        super();
    }

    public TokenActionCancelledException(String message) {
        super(message);
    }

    public TokenActionCancelledException(Throwable cause) {
        super(cause);
    }

    public TokenActionCancelledException(String message, Throwable cause) {
        super(message, cause);
    }

    public static TokenActionCancelledException tryConvert(DSSException ex) {
        Throwable th = ex;
        while (th != null) {
            if ((th.getMessage() != null) && (th.getMessage().equals("CKR_CANCEL") || th.getMessage().equals("CKR_FUNCTION_CANCELED"))) // Treat a PKCS11 cancel as a TokenActionCancelledException.
                return new TokenActionCancelledException(th);
            else if (th instanceof TokenActionCancelledException) // Treat an "embedded" TokenActionCancelledException. This occurs only within my password callback where I throw cancelled exception if the user presses the cancel button. In this case, I'll also use the original exception as the cause.
                return new TokenActionCancelledException(ex);
            th = th.getCause();
        }
        return null;
    }
}
