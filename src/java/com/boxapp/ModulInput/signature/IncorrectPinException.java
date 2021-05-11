package src.java.com.boxapp.ModulInput.signature;

import eu.europa.esig.dss.model.DSSException;
import sun.security.pkcs11.wrapper.PKCS11Exception;

public class IncorrectPinException extends DSSException {

    private static final long serialVersionUID = 1L;

    public IncorrectPinException() {
        super();
    }

    public IncorrectPinException(String message) {
        super(message);
    }

    public IncorrectPinException(Throwable cause) {
        super(cause);
    }

    public IncorrectPinException(String message, Throwable cause) {
        super(message, cause);
    }

    public static IncorrectPinException tryConvert(DSSException ex) {
        Throwable th = ex;
        while (th != null) {
            if ((th instanceof PKCS11Exception) && (th.getMessage() != null) && th.getMessage().equals("CKR_PIN_INCORRECT"))
                return new IncorrectPinException(th);
            th = th.getCause();
        }
        return null;
    }
}
