package src.java.com.boxapp.ModulInput.signature;

import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.model.DSSException;
import eu.europa.esig.dss.model.SignatureValue;
import eu.europa.esig.dss.model.ToBeSigned;
import eu.europa.esig.dss.token.DSSPrivateKeyEntry;
import eu.europa.esig.dss.token.PasswordInputCallback;
import eu.europa.esig.dss.token.Pkcs11SignatureToken;
import sun.security.pkcs11.wrapper.CK_C_INITIALIZE_ARGS;
import sun.security.pkcs11.wrapper.CK_SLOT_INFO;
import sun.security.pkcs11.wrapper.PKCS11;
import sun.security.pkcs11.wrapper.PKCS11Exception;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * This class extends the Pkcs11SignatureToken and offers:
 * - InvalidSlotIdException and TokenActionCancelledException in specific cases (instead of a more generic DSSException).
 * - Static getSlots function to load all slots with a token.
 */
public class ExtToken extends Pkcs11SignatureToken {
    /**
     * Checks if the supplied pkcs11 library is really a pkcs11 library.
     * @param pkcs11LibraryPath
     * @return
     */
    public static boolean isPKCS11Library(String pkcs11LibraryPath) throws IOException, PKCS11Exception {
        String functionList = "C_GetFunctionList";
        CK_C_INITIALIZE_ARGS initArgs = new CK_C_INITIALIZE_ARGS();
        PKCS11.getInstance(pkcs11LibraryPath, functionList, initArgs, false);
        return true;
    }

    /**
     * Reads all available slots containing a token using the supplied pkcs11 library.
     * @param pkcs11LibraryPath
     * @return HashMap containing slot Ids (keys) and slot info structures (values).
     */
    public static Map<Integer,CK_SLOT_INFO> getSlots(String pkcs11LibraryPath) throws IOException, PKCS11Exception {
        Map<Integer, CK_SLOT_INFO> slots = new LinkedHashMap<>();

        // Try to initialize PKCS11 (omitInitialization = false).
        // The function PKCS11.getInstance internally suppresses the CKR_CRYPTOKI_ALREADY_INITIALIZED error, so it's safe to always try to initialize the library.
        String functionList = "C_GetFunctionList";
        CK_C_INITIALIZE_ARGS initArgs = new CK_C_INITIALIZE_ARGS();
        PKCS11 tmpPKCS11 = PKCS11.getInstance(pkcs11LibraryPath, functionList, initArgs, false);
        long[] slotIds = tmpPKCS11.C_GetSlotList(true); // tokenPresent = true i.e. load only slots with token present.

        for (long slotId : slotIds) {
            CK_SLOT_INFO slotInfo = tmpPKCS11.C_GetSlotInfo(slotId);
            slots.put((int) slotId, slotInfo);
        }

        return slots;
    }

    public ExtToken(String pkcs11LibraryPath, PasswordInputCallback callback, int slotId) {
        super(pkcs11LibraryPath, callback, slotId);
    }

    public List<DSSPrivateKeyEntry> getKeys() throws DSSException {
        try {
            return super.getKeys();
        } catch (final DSSException ex) {
            if (InvalidSlotIdException.tryConvert(ex) != null) throw InvalidSlotIdException.tryConvert(ex);
            else if (TokenActionCancelledException.tryConvert(ex) != null) throw TokenActionCancelledException.tryConvert(ex);
            else if (TokenRemovedException.tryConvert(ex) != null) throw TokenRemovedException.tryConvert(ex);
            else if (IncorrectPinException.tryConvert(ex) != null) throw IncorrectPinException.tryConvert(ex);
            else if (BlockedPinException.tryConvert(ex) != null) throw BlockedPinException.tryConvert(ex);
            else throw ex;
        }
    }

    @Override
    public SignatureValue sign(ToBeSigned toBeSigned, DigestAlgorithm digestAlgorithm, DSSPrivateKeyEntry keyEntry) throws DSSException {
        try {
            return super.sign(toBeSigned, digestAlgorithm, keyEntry);
        } catch (final DSSException ex) {
            if (TokenActionCancelledException.tryConvert(ex) != null) throw TokenActionCancelledException.tryConvert(ex);
            else if (TokenRemovedException.tryConvert(ex) != null) throw TokenRemovedException.tryConvert(ex);
            else throw ex;
        }
    }
}
