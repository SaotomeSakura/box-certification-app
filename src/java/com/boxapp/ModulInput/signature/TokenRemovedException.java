package src.java.com.boxapp.ModulInput.signature;

import eu.europa.esig.dss.model.DSSException;

import java.security.ProviderException;

public class TokenRemovedException extends DSSException {

    private static final long serialVersionUID = 1L;

    public TokenRemovedException() {
        super();
    }

    public TokenRemovedException(String message) {
        super(message);
    }

    public TokenRemovedException(Throwable cause) {
        super(cause);
    }

    public TokenRemovedException(String message, Throwable cause) {
        super(message, cause);
    }

    public static TokenRemovedException tryConvert(DSSException ex) {
        Throwable th = ex;
        while (th != null) {
            if ((th instanceof ProviderException) && (th.getMessage() != null) && th.getMessage().equals("Token has been removed"))
                return new TokenRemovedException(th);
            th = th.getCause();
        }
        return null;
    }
}
