package src.java.com.boxapp.ModulInput.signature;

import eu.europa.esig.dss.model.DSSException;
import sun.security.pkcs11.wrapper.PKCS11Exception;

public class BlockedPinException extends DSSException {

    private static final long serialVersionUID = 1L;

    public BlockedPinException() {
        super();
    }

    public BlockedPinException(String message) {
        super(message);
    }

    public BlockedPinException(Throwable cause) {
        super(cause);
    }

    public BlockedPinException(String message, Throwable cause) {
        super(message, cause);
    }

    public static BlockedPinException tryConvert(DSSException ex) {
        Throwable th = ex;
        while (th != null) {
            if ((th instanceof PKCS11Exception) && (th.getMessage() != null) && th.getMessage().equals("CKR_PIN_LOCKED"))
                return new BlockedPinException(th);
            th = th.getCause();
        }
        return null;
    }
}
