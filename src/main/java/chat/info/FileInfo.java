package chat.info;

import java.io.Serializable;

public class FileInfo implements Serializable {
    private String fileName;
    private byte[] fileData;

    public FileInfo(String fileName, byte[] fileData) {
        this.fileName = fileName;
        this.fileData = fileData;
    }

    public String getFileName() {
        return fileName;
    }

    public byte[] getFileData() {
        return fileData;
    }
}

