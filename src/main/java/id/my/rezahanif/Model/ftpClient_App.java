package id.my.rezahanif.Model;


import id.my.rezahanif.Cache.FileLocalCache;
import id.my.rezahanif.Cache.IconLocalCache;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import id.my.rezahanif.Controller.appsController;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import javax.swing.filechooser.FileSystemView;
import java.io.*;
import java.nio.file.Path;
import java.util.Arrays;


public class ftpClient_App {
    private FTPClient ftpClient = new FTPClient();
    private appsController controller;
    private FileSystemView fileSystemView;
    private IconLocalCache iconCache;
    private FileLocalCache fileLocalCache;

    public ftpClient_App(appsController controller) {
        this.controller = controller;
        this.fileSystemView = FileSystemView.getFileSystemView();
        this.iconCache = new IconLocalCache();
        this.fileLocalCache = new FileLocalCache();
    }
    public FTPClient getFtpClient() {
        return ftpClient;
    }
    // Method untuk koneksi ke FTP server
    public void quickConnect(String host, String username, String password, String port, Stage primaryStage) {
        try {
            // Jika port kosong, set ke 21
            if (port == null || port.isEmpty()) {
                port = "21";
            } else {
                port = String.valueOf(Integer.parseInt(port));
            }
            // Jika host kosong, muncul allert error
            if(host == null || host.isEmpty()){
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setContentText("Could not parse server address:\n" +
                        "No host given, please enter a host.");
                alert.showAndWait();
                return;
            }
            // allert jika ftp sudah terkoneksi
            if (ftpClient.isConnected()) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Information");
                alert.setContentText("FTP is already connected to a server @" + host + " on port " + port);
                alert.showAndWait();
            }else {
                // Pesan log jika belum terkoneksi dan sedang mencoba terkoneksi
                ftpClient.connect(host, Integer.parseInt(port));
                controller.addListLog("Status", "Connecting to " + host + " on port " + port, null);
                // Jika login gagal
                if (!ftpClient.login(username, password)) {
                    ftpClient.sendCommand("User", username);
                    controller.addListLog("Command", "USER " + username, Color.BLUE);
                    String replyCommandUser = ftpClient.getReplyString();
                    String replyWithoutNewLineCommandUser = replyCommandUser.replace("\n", "").replace("\r", "");
                    controller.addListLog("Response", replyWithoutNewLineCommandUser, Color.GREEN);
                    ftpClient.sendCommand("PASS", password);
                    String replyCommandPass = ftpClient.getReplyString();
                    String replyWithoutNewLineCommandPass = replyCommandPass.replace("\n", "").replace("\r", "");
                    String maskPassword = password.replace(password, "*".repeat(password.length()));
                    controller.addListLog("Command", "PASS " + maskPassword, Color.BLUE);
                    controller.addListLog("Response", replyWithoutNewLineCommandPass, Color.GREEN);
                    controller.addListLog("Error", "Critical error: Could not connect to server. Password is incorrect!", Color.RED);
                    disconnect(primaryStage);
                } else {
                    // Jika login berhasil
                    controller.addListLog("Status", "Connection established, waiting for welcome message...", null);
                    // Load root directory
                    loadRemoteTree("/", controller.getTreeRemote());
                    String reply = ftpClient.getReplyString();
                    String replyWithoutNewLine = reply.replace("\n", "").replace("\r", "");
                    controller.addListLog("Status", replyWithoutNewLine, null);
                    primaryStage.setTitle(username + "@" + host + " - FTP Client"); // Set the title with the username
                    controller.addListLog("Status", "Retrieving directory listing...", null);
                    // Aktifkan txtRemotePath setelah pengguna terhubung ke server FTP
                    controller.enableTxtRemotePath();
                    // menampilkan treeRemote
                    controller.setupRemoteTreeView();
                    // menampilkan refreshRemote
                    controller.enableRefreshRemote();
                }
            }
            // Jika gagal terkoneksi
        } catch (IOException ex) {
            controller.addListLog("Status", "Connecting to " + host + " on port " + port, null);
            controller.addListLog("Status", "Connection attempt failed with \"ECONNREFUSED - Connection refused by server\".", null);
            controller.addListLog("Error", "Could not connect to server", Color.RED);
        }
    }
    // Method untuk disconnect dari FTP server
    public void disconnect(Stage primaryStage) {
        if (ftpClient.isConnected()) {
            try {
                ftpClient.logout();
                ftpClient.disconnect();
                // Disable txtRemotePath setelah pengguna terputus dari server FTP
                controller.disableTxtRemotePath();
                // Disable refreshRemote setelah pengguna terputus dari server FTP
                controller.disableRefreshRemote();
                // Hapus semua item dari treeRemote
                controller.clearTreeRemote();
                // Hapus semua pathname dari txtRemotePath
                controller.clearTxtRemotePath();
                // Hapus semua item dari tableRemote
                controller.clearTableRemote();
                primaryStage.setTitle("FTP Client - EarlyBird PBO");
                controller.addListLog("Status", "Disconnected from FTP server.", null);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }else {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Information");
            alert.setContentText("FTP is not connected to any server.");
            alert.showAndWait();
        }
    }
    //////////////////////////////////////////
    /*FILE SYSTEM HANDLE*/
    //////////////////////////////////////////

    // Method untuk mengambil list file dan folder dari filesystem serta drive sebagi root
    public void loadFileSystem(TreeView<Path> treeView) {
        // Membuat root item
        TreeItem<Path> rootItem = new TreeItem<>(Path.of("This PC"));
        File[] roots = File.listRoots();
        // Looping untuk setiap root untuk mendapatkan list drive
        for (File root : roots) {
            TreeItem<Path> rootDrive = new TreeItem<>(root.toPath()) {
                @Override
                public String toString() {
                    return getDriveLabel(root);
                }
            };
            loadDirectories(rootDrive, root.toPath());
            rootItem.getChildren().add(rootDrive);
        }
        treeView.setRoot(rootItem);
        treeView.setShowRoot(true);
    }
    // Method untuk mengambil list folder dari filesystem untuk setiap drive
    // dan menampilkannya di TreeView
    public void loadDirectories(TreeItem<Path> parentItem, Path path) {
        File dir = path.toFile();
        File[] subDirs = dir.listFiles(File::isDirectory);

        if (subDirs != null) {
            for (File subDir : subDirs) {
                if (subDir.isHidden() || !subDir.canRead()) continue;
                // Membuat sub item(folder) untuk setiap drive
                TreeItem<Path> subItem = new TreeItem<>(subDir.toPath()) {
                    @Override
                    public String toString() {
                        return subDir.getName();
                    }
                };
                parentItem.getChildren().add(subItem);
                loadDirectories(subItem, subDir.toPath());
            }
        }
    }
    // Method untuk mengambil list file dari directory dan menampilkannya di TableView
    public void loadFilesInDirectory(TableView<File> tableView, File directory) {
        tableView.getItems().clear();
        File[] files = directory.listFiles(file -> file.isFile() && !file.isHidden() && file.canRead());

        if (files != null) {
            tableView.getItems().addAll(Arrays.asList(files));
        }
    }
    // Method untuk mencari directory pada treeItem yang tadi sudah ditambahkan
    public void searchDirectory(TreeView<Path> treeView, Path targetPath) {
        TreeItem<Path> root = treeView.getRoot();
        TreeItem<Path> result = searchTreeItem(root, targetPath);

        if (result != null) {
            treeView.getSelectionModel().select(result);
            treeView.scrollTo(treeView.getRow(result));
        } else {
            // Jika directory tidak ditemukan
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Search Result");
            alert.setHeaderText(null);
            alert.setContentText("Directory not found!");
            alert.showAndWait();
        }
    }
    // Method untuk mencari treeItem pada treeView
    private TreeItem<Path> searchTreeItem(TreeItem<Path> root, Path targetPath) {
        if (root.getValue().equals(targetPath)) {
            return root;
        }
        for (TreeItem<Path> child : root.getChildren()) {
            TreeItem<Path> result = searchTreeItem(child, targetPath);
            if (result != null) {
                return result;
            }
        }
        return null;
    }
    // Method untuk membuat directory pada local filesystem
    public void createDirectoryLocal(String directoryPath) {
        File newDir = new File(directoryPath);
        if (newDir.exists()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setContentText("Directory already exists.");
            alert.showAndWait();
        } else {
            if (newDir.mkdirs()) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Information");
                alert.setContentText("Directory created successfully.");
                alert.showAndWait();
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setContentText("Failed to create directory.");
                alert.showAndWait();
            }
        }
    }

    // Method untuk mendapatkan label drive
    public String getDriveLabel(File drive) {
        String driveName = fileSystemView.getSystemDisplayName(drive);
        if (driveName == null || driveName.trim().isEmpty()) {
            driveName = drive.getPath();
        }
        return driveName;
    }


    //////////////////////////////////////////
    /*END OF FILE SYSTEM HANDLE*/
    //////////////////////////////////////////

    //////////////////////////////////////////
    /*REMOTE HANDLE*/
    //////////////////////////////////////////
    public void loadRemoteTree(String path, TreeView<String> treeView) {
        try {
            FTPFile[] directories = ftpClient.listDirectories(path);
            TreeItem<String> rootItem = new TreeItem<>(path);
            for (FTPFile dir : directories) {
                TreeItem<String> item = new TreeItem<>(dir.getName());
                loadSubDirectories(item, path + "/" + dir.getName());
                rootItem.getChildren().add(item);
            }
            Platform.runLater(() -> {
                treeView.setRoot(rootItem);
                treeView.setShowRoot(true);
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadSubDirectories(TreeItem<String> parentItem, String path) {
        try {
            FTPFile[] directories = ftpClient.listDirectories(path);
            for (FTPFile dir : directories) {
                TreeItem<String> item = new TreeItem<>(dir.getName());
                parentItem.getChildren().add(item);
                // Rekursif memuat subdirektori dari direktori saat ini
                loadSubDirectories(item, path + "/" + dir.getName());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadFilesServerInDirectory(String path, TableView<FTPFile> tableView) {
        try {
            FTPFile[] files = ftpClient.listFiles(path, FTPFile::isFile);
            Platform.runLater(() -> {
                tableView.getItems().clear();
                tableView.getItems().addAll(files);
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void searchRemoteDirectory(TreeView<String> treeView, String targetPath) {
        TreeItem<String> root = treeView.getRoot();
        TreeItem<String> result = searchTreeItem(root, targetPath);

        if (result != null) {
            controller.expandPath(result);
            treeView.getSelectionModel().select(result);
            treeView.scrollTo(treeView.getRow(result));
        } else {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Search Result");
            alert.setHeaderText(null);
            alert.setContentText("Directory not found!");
            alert.showAndWait();
        }
    }

    private TreeItem<String> searchTreeItem(TreeItem<String> root, String targetPath) {
        String fullPath = getFullPath(root);
        if (fullPath.equals(targetPath)) {
            return root;
        }
        for (TreeItem<String> child : root.getChildren()) {
            TreeItem<String> result = searchTreeItem(child, targetPath);
            if (result != null) {
                return result;
            }
        }
        return null;
    }
    private String getFullPath(TreeItem<String> item) {
        StringBuilder path = new StringBuilder(item.getValue());
        TreeItem<String> parent = item.getParent();

        while (parent != null && parent.getValue() != null) {
            path.insert(0, parent.getValue() + "/");
            parent = parent.getParent();
        }
        String fullPath = path.toString().replaceFirst("//", "/");
        return fullPath;
    }

    public void createDirectoryRemote(String directoryPath) {
        try {
            if (ftpClient.makeDirectory(directoryPath)) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Information");
                alert.setContentText("Directory " + directoryPath + " created successfully.");
                alert.showAndWait();
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setContentText("Failed to create directory " + directoryPath + ".");
                alert.showAndWait();
            }
        } catch (IOException ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setContentText("Error creating directory: " + ex.getMessage());
            alert.showAndWait();
        }
    }
    //////////////////////////////////////////
    /*END OF REMOTE HANDLE*/
    //////////////////////////////////////////

    public boolean uploadFile(String localFilePath) {
        try {
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

            File localFile = new File(localFilePath);
            String fileName = localFile.getName();

            try (InputStream inputStream = new FileInputStream(localFile)) {
                if (!ftpClient.storeFile(fileName, inputStream)) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setContentText("Failed to upload file " + fileName + " to server.");
                    alert.showAndWait();
                    logFileActivity(fileName, "Upload", fileName, localFile.length(), "Failed");
                    return false;
                }
            }

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Information");
            alert.setContentText("File " + fileName + " uploaded successfully.");
            alert.showAndWait();
            logFileActivity(fileName, "Upload", fileName, localFile.length(), "Success");


        } catch (IOException ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setContentText("Error uploading file: " + ex.getMessage());
            alert.showAndWait();
            logFileActivity(localFilePath, "Upload", localFilePath, 0, "Error: " + ex.getMessage());
        }
        return false;
    }

    public boolean downloadFile(String remoteFilePath, String localDirectory) {
        try {
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

            FTPFile[] files = ftpClient.listFiles(remoteFilePath);
            if (files.length == 0) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setContentText("File " + remoteFilePath + " not found on server.");
                alert.showAndWait();
                return false;
            }

            FTPFile remoteFile = files[0];
            File localDir = new File(localDirectory);
            if (!localDir.exists()) {
                localDir.mkdirs();
            }

            File downloadFile = new File(localDir.getAbsolutePath() + File.separator + remoteFile.getName());
            try (OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(downloadFile))) {
                if (!ftpClient.retrieveFile(remoteFilePath, outputStream)) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setContentText("Failed to download file " + remoteFilePath + ".");
                    alert.showAndWait();
                    logFileActivity(remoteFile.getName(), "Download", remoteFilePath, remoteFile.getSize(), "Failed");
                    return false;
                }
            }

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Information");
            alert.setContentText("File " + remoteFile.getName() + " downloaded successfully.");
            alert.showAndWait();
            logFileActivity(remoteFile.getName(), "Download", remoteFilePath, remoteFile.getSize(), "Success");


        } catch (IOException ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setContentText("Error downloading file: " + ex.getMessage());
            alert.showAndWait();
            logFileActivity(remoteFilePath, "Download", remoteFilePath, 0, "Error: " + ex.getMessage());
        }
        return false;
    }

    private void logFileActivity(String fileName, String direction, String remoteFile, long size, String status) {
        LogFile logFile = new LogFile(fileName, direction, remoteFile, size, status);
        Platform.runLater(() -> controller.addLogFile(logFile));
    }
}

