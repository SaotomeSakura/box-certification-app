package src.java.com.boxapp.ModulAplication.fileManipulation;

import com.box.sdk.BoxAPIConnection;

public class IDsAndTokens {
    public static String apiToken = "IwSJGWVjQldgy0BnPIEfrDjcpcEbjfoG";

    public static String folderArchiveID = "127141357948";
    public static String folderInputID = "127138717669";
    public static String folderExpiredID = "127140362709";
    public static String folderDeletedID = "127140376191";

    public static String expiryFolder = System.getProperty("user.dir") + "\\Expiry\\";
    public static String expiredFilesFolder = System.getProperty("user.dir") + "\\ExpiredFiles\\";
    public static String expiryDatesFile = System.getProperty("user.dir") + "\\ExpiryDatesFolder\\ExpiryDates4.txt";
    public static String downloadToFolder = System.getProperty("user.dir") + "\\DownloadTo\\";
    public static String policyFolder = System.getProperty("user.dir") + "\\policyall-xml";
    public static String reportsFolder = System.getProperty("user.dir") + "\\reports\\";

    public static String clientID = "dpn7gjta1hkff7gt2e2wsxp4m0rusf9k";
    public static String clientSecret = "LprKT3UihnOiKdNIRG8aZTLnpDdpavSN";
    public static BoxAPIConnection api;

    final public static String tspServer = "http://dss.nowina.lu/pki-factory/tsa/good-tsa";

    public static void setApiConnection(String key){
        api = new BoxAPIConnection(clientID,clientSecret,key);
    }

    public static BoxAPIConnection getApi() {
        return api;
    }

    public static String pkcs11Path = "C:\\\\Program Files (x86)\\EAC MW klient\\pkcs11_x64.dll";

}
