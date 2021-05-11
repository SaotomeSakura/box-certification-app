package src.java.com.boxapp.ModulAplication.GridManipulation;

public class FileToManipulate {
    private String fileName;
    private String fileId;

    public FileToManipulate(String initialNameOfFile, String fileId) {
        this.fileName = initialNameOfFile;
        this.fileId = fileId;
    }

    public String getFileId() {
        return fileId;
    }

    public String getFileName(){
        return this.fileName;
    }

    @Override
    public String toString() {
        return fileName;
    }
}