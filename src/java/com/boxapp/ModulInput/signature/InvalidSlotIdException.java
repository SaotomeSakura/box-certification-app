package src.java.com.boxapp.ModulInput.signature;

import eu.europa.esig.dss.model.DSSException;
import sun.security.pkcs11.wrapper.PKCS11Exception;

public class InvalidSlotIdException extends DSSException {

    private static final long serialVersionUID = 1L;

    public InvalidSlotIdException() {
        super();
    }

    public InvalidSlotIdException(String message) {
        super(message);
    }

    public InvalidSlotIdException(Throwable cause) {
        super(cause);
    }

    public InvalidSlotIdException(String message, Throwable cause) {
        super(message, cause);
    }

    public static InvalidSlotIdException tryConvert(DSSException ex) {
        Throwable th = ex;
        while (th != null) {
            if ((th instanceof PKCS11Exception) && (th.getMessage() != null) && th.getMessage().equals("CKR_SLOT_ID_INVALID"))
                return new InvalidSlotIdException(th);
            th = th.getCause();
        }
        return null;
    }
}
