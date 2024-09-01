package id.my.rezahanif.Model;


public class LogFile {
    private String fileName;
    private String direction;
    private String remoteFile;
    private long size;
    private String status;

    // Constructor, getters, and setters
    // Constructor, getters, and setters
    public LogFile(String fileName, String direction, String remoteFile, long size, String status) {
        this.fileName = fileName;
        this.direction = direction;
        this.remoteFile = remoteFile;
        this.size = size;
        this.status = status;
    }

    public String getFileName() {
        return fileName;
    }


    public String getDirection() {
        return direction;
    }

    public String getRemoteFile() {
        return remoteFile;
    }


    public long getSize() {
        return size;
    }


    public String getStatus() {
        return status;
    }

}






