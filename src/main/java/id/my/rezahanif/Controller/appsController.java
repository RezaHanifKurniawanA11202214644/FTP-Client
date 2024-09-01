package id.my.rezahanif.Controller;

import id.my.rezahanif.Cache.FileLocalCache;
import id.my.rezahanif.Cache.IconLocalCache;
import id.my.rezahanif.Model.LogFile;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import id.my.rezahanif.Model.ftpClient_App;
import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.*;
import javafx.util.Callback;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.tika.Tika;


import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Optional;


public class appsController {
    @FXML
    private TextField txtHost, txtUsername, txtPassword, txtPort;
    @FXML
    private TextField txtLocalPath, txtRemotePath;
    @FXML
    private ListView<Text> listLog;
    @FXML
    private TreeView<Path> treeLocal;
    @FXML
    private TreeView<String> treeRemote;
    @FXML
    private TableView<File> tbLocal;
    @FXML
    private TableView<FTPFile>tbRemote;
    @FXML
    private TableColumn<File, String> tbLocal_colFilename, tbLocal_colFilesize, tbLocal_colFiletype, tbLocal_colLstModif;
    @FXML
    private TableColumn<FTPFile, String> tbRemote_colFilename, tbRemote_colFilesize, tbRemote_colFiletype, tbRemote_colLstModif;
    @FXML
    private TableView<LogFile> tbLogFiles;
    @FXML
    private TableColumn<LogFile, String> tbLogFiles_colFile;
    @FXML
    private TableColumn<LogFile, String> tbLogFiles_colDirection;
    @FXML
    private TableColumn<LogFile, String> tbLogFiles_colRemoteFile;
    @FXML
    private TableColumn<LogFile, String> tbLogFiles_colSize;
    @FXML
    private TableColumn<LogFile, String> tbLogFiles_colStatus;
    @FXML
    private Button refreshRemote;

    private ftpClient_App appLogic = new ftpClient_App(this);
    private FileLocalCache fileLocalCache;
    private IconLocalCache iconCache;
    private FTPClient ftpClient = new FTPClient();

    public appsController() {
        this.fileLocalCache = new FileLocalCache();
        this.iconCache = new IconLocalCache();
    }

    @FXML
    // Method to handle the MenuItem Close
    public void menuClose() {
        System.exit(0);
    }

    @FXML
    public void refreshLocalTree() {
        TreeItem<Path> selectedItem = treeLocal.getSelectionModel().getSelectedItem();
        if (selectedItem != null && selectedItem != treeLocal.getRoot()) {
            Path selectedPath = selectedItem.getValue();
            Platform.runLater(() -> {
                selectedItem.getChildren().clear();
                loadSelectedDirectory(selectedItem, selectedPath);
                selectedItem.setExpanded(true);
            });
        }
    }

    public void loadSelectedDirectory(TreeItem<Path> selectedItem, Path pathToLoad) {
        // Membuat item baru untuk setiap child dari pathToLoad
        File dir = pathToLoad.toFile();
        File[] subDirs = dir.listFiles(File::isDirectory);

        if (subDirs != null) {
            for (File subDir : subDirs) {
                if (subDir.isHidden() || !subDir.canRead()) continue;
                TreeItem<Path> subItem = new TreeItem<>(subDir.toPath());
                selectedItem.getChildren().add(subItem);
                appLogic.loadDirectories(subItem, subDir.toPath());
            }
        }
    }

    @FXML
    // Method to refresh the local table
    public void refreshLocalTable() {
        TreeItem<Path> selectedItem = treeLocal.getSelectionModel().getSelectedItem();
        if (selectedItem != null && selectedItem != treeLocal.getRoot()) {
            Path selectedPath = selectedItem.getValue();
            appLogic.loadFilesInDirectory(tbLocal, selectedPath.toFile());
        }
    }

    @FXML
    // Method to refresh the remote table
    public void refreshRemoteTable() {
        String path = txtRemotePath.getText();
        appLogic.loadFilesServerInDirectory(path, tbRemote);
    }

    @FXML
    // Method to refresh the remote tree
    public void refreshRemoteTree() {
        TreeItem<String> selectedItem = treeRemote.getSelectionModel().getSelectedItem();
        if (selectedItem != null && selectedItem != treeRemote.getRoot()) {
            String selectedPath = selectedItem.getValue();
            Platform.runLater(() -> {
                selectedItem.getChildren().clear();
                appLogic.loadSubDirectories(selectedItem, selectedPath);
                selectedItem.setExpanded(true);
            });
        }
    }

    // Method to handle the refresh button local
    @FXML
    public void handleRefreshLocal() {
        refreshLocalTree();
        refreshLocalTable();
    }
    // Method to handle the refresh button remote
    @FXML
    public void handleRefreshRemote() {
        refreshRemoteTree();
        refreshRemoteTable();
    }

    @FXML
    // Method to handle the MenuItem About
    public void menuAbout() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About FTP Client");
        alert.setHeaderText("FTP Client");
        alert.setContentText("Tugas Early Bird\n" +
                "Aplikasi FTP Client Sederhana\n" +
                "Reza Hanif Hanif\n" +
                "A11.2022.14644\n" +
                "Dian Nuswantoro\n" +
                "2024");
        alert.showAndWait();
    }
    // Method to handle the Quick Connect button
    @FXML
    public void handleQuickConnect() {
        String host = txtHost.getText();
        String username = txtUsername.getText();
        String password = txtPassword.getText();
        String port = txtPort.getText();

        Stage primaryStage = (Stage) txtHost.getScene().getWindow(); // Mendapatkan primary stage untuk mengeset judul ke nama user
        appLogic.quickConnect(host, username, password, port, primaryStage); // Memanggil method quickConnect dari ftpClient_App

    }
    // Method to handle the Disconnect button
    @FXML
    public void handleDisconnect() {
        Stage primaryStage = (Stage) txtHost.getScene().getWindow(); // Mendapatkan primary stage untuk mengeset judul ke default
        appLogic.disconnect(primaryStage);
    }
    // Method to handle the List View
    @FXML
    public void addListLog(String status, String message, Color color) {
        int firstsapce = 12; // Menentukan lebar sebelum ":"
        int lastspace = 5; // Menentukan jumlah spasi setelah ":"
        StringBuilder sb = new StringBuilder();
        sb.append(status);
        for (int i = 0; i < firstsapce - status.length(); i++) {
            sb.append(" ");
        }
        sb.append(":");
        for (int i = 0; i < lastspace; i++) {
            sb.append(" ");
        }
        sb.append(message);
        Text text = new Text(sb.toString()); // Membuat objek Text baru dengan string dari StringBuilder
        text.setFont(new Font("Courier New",  14)); // Mengubah font menjadi Courier New dengan ukuran 14

        text.setFill(color != null ? color : Color.BLACK);
        listLog.getItems().add(text);
    }
    // Method to handle the List Log for the ListView
    public void setupListLog() {
        //metode yang digunakan untuk menentukan bagaimana setiap sel dalam ListView harus ditampilkan
        listLog.setCellFactory(param -> new ListCell<Text>() {
            @Override
            protected void updateItem(Text item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(null);
                    setGraphic(item);
                }
            }
        });
    }
    // Method to handle the Enter Key Handlers
    public void setupEnterKeyHandlers() {
        txtHost.setOnAction(event -> handleQuickConnect());
        txtUsername.setOnAction(event -> handleQuickConnect());
        txtPassword.setOnAction(event -> handleQuickConnect());
        txtPort.setOnAction(event -> handleQuickConnect());
    }
    //////////////////////////////////////////
    /*FILE SYSTEM HANDLE*/
    //////////////////////////////////////////
    @FXML
    // Method to handle the txtLocalPath TextField
    public void setupTxtLocalPath() {
        //menambahkan event handler ketika tombol enter ditekan dan ambil path dari txtLocalPath
        txtLocalPath.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                String directory = txtLocalPath.getText();
                Path path = Paths.get(directory);
                appLogic.searchDirectory(treeLocal, path);
            }
        });
    }
    @FXML
    // Method to handle the treeLocal TreeView
    public void setupLocalTreeView() {
        // menentukan bagaimana setiap sel dalam treeLocal harus ditampilkan
        treeLocal.setCellFactory(new Callback<>() {
            @Override
            public TreeCell<Path> call(TreeView<Path> param) {
                return new TreeCell<>() {
                    @Override
                    protected void updateItem(Path item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item != null) {
                            File file = fileLocalCache.getFile(item.toString());
                            setGraphic(null);  // Hapus ikon sebelumnya
                            // Muat ikon secara asinkron
                            new Thread(() -> {
                                ImageView icon = iconCache.getSystemIcon(file.getPath());
                                Platform.runLater(() -> setGraphic(icon));
                            }).start();
                            setText(appLogic.getDriveLabel(file)); // Menampilkan label driver
                        } else {
                            setText(null);
                            setGraphic(null);
                        }
                    }
                };
            }
        });
        appLogic.loadFileSystem(treeLocal); // panggil method loadFileSystem untuk meload root dan drive
        // mengatur jika yang diselect adalah root (This PC) maka txtLocalPath akan di set ke root karena ini hanya dummy bukan asli path
        treeLocal.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                Path selectedPath = newValue.getValue();
                if (!selectedPath.toString().equals("This PC")) {
                    txtLocalPath.setText(selectedPath.toString());
                    appLogic.loadFilesInDirectory(tbLocal, selectedPath.toFile());
                } else {
                    txtLocalPath.setText("\\");
                    tbLocal.getItems().clear();
                }
            }
        });
        // Tambahkan event handler untuk klik kanan
        treeLocal.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.SECONDARY) { // Right click
                TreeItem<Path> selectedItem = treeLocal.getSelectionModel().getSelectedItem();
                if (selectedItem != null) {
                    Path selectedPath = selectedItem.getValue();
                    ContextMenu contextMenu = new ContextMenu();
                    MenuItem createDirectoryItem = getMenuItem(String.valueOf(selectedPath));
                    contextMenu.getItems().addAll(createDirectoryItem);
                    contextMenu.show(treeLocal, event.getScreenX(), event.getScreenY());
                }
            }
        });
    }

    private MenuItem getMenuItem(String selectedPath) {
        MenuItem createDirectoryItem = new MenuItem("Create Directory");
        createDirectoryItem.setOnAction(event -> {
            if (selectedPath.equals("This PC") || selectedPath.equals("user.home")) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Invalid Location");
                alert.setContentText("Cannot create a directory in " + selectedPath + ". Please select a valid location.");
                alert.showAndWait();
            } else {
                TextInputDialog dialog = new TextInputDialog();
                dialog.setTitle("Create Directory");
                dialog.setHeaderText("Create Directory");
                dialog.setContentText("Enter directory name:");
                Optional<String> result = dialog.showAndWait();
                result.ifPresent(name -> {
                    String directoryPath = selectedPath + File.separator + name;
                    appLogic.createDirectoryLocal(directoryPath);
                });
            }
        });
        return createDirectoryItem;
    }

    @FXML
    // Method to handle the tbLocal TableView
    public void setupTbLocal(){
        // menentukan bagaimana setiap sel dalam tbLocal harus ditampilkan
        tbLocal_colFilename.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null && !empty) {
                    File file = getTableView().getItems().get(getIndex());
                    setText(item);
                    setGraphic(iconCache.getSystemIcon(file.getPath()));
                } else {
                    setText(null);
                    setGraphic(null);
                }
            }
        });
        // Konfigurasi kolom Filename
        tbLocal_colFilename.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));
        // Konfigurasi kolom Filesize
        tbLocal_colFilesize.setCellValueFactory(cellData -> new SimpleStringProperty(Long.toString(cellData.getValue().length())));
        // Konfigurasi kolom Filetype
        tbLocal_colFiletype.setCellValueFactory(cellData -> {
            try {
                Tika tika = new Tika();
                return new SimpleStringProperty(tika.detect(cellData.getValue().toPath()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        // Konfigurasi kolom Last Modified
        tbLocal_colLstModif.setCellValueFactory(cellData -> new SimpleStringProperty(
                new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(cellData.getValue().lastModified())
        ));
        // Konfigurasi double click untuk upload file
        tbLocal.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) { // Double click detected
                File selectedFile = tbLocal.getSelectionModel().getSelectedItem();
                if (selectedFile != null) {
                    String localFilePath = selectedFile.getAbsolutePath();
                    ftpClient = appLogic.getFtpClient();
                    appLogic.uploadFile(localFilePath); // Upload file
                }
            }
        });


    }
    //////////////////////////////////////////
    /*END OF FILE SYSTEM HANDLE*/
    //////////////////////////////////////////



    //////////////////////////////////////////
    /*REMOT HANDLE*/
    //////////////////////////////////////////

    // Method untuk mengaktifkan txtRemotePath
    public void enableTxtRemotePath() {
        txtRemotePath.setDisable(false);
    }
    // Method untuk menonaktifkan txtRemotePath
    public void disableTxtRemotePath() {
        txtRemotePath.setDisable(true);
    }
    // Method untuk mengaktifkan refreshRemote
    public void enableRefreshRemote() {
        refreshRemote.setDisable(false);
    }
    // Method untuk menonaktifkan refreshRemote
    public void disableRefreshRemote() {
        refreshRemote.setDisable(true);
    }
    // Method untuk mengosongkan treeRemote
    public void clearTreeRemote() {
        if (treeRemote.getRoot() != null) {
            treeRemote.getRoot().getChildren().clear();
            treeRemote.setShowRoot(false);
        }
    }
    // Method untuk mengosongkan txtRemotePath
    public void clearTxtRemotePath() {
        txtRemotePath.clear();
    }
    // Method untuk mengupdate txtRemotePath
    public void updateTxtRemotePath(String path) {
        String Path = normalizePath(path);
        // Menjalankan di thread JavaFX
        Platform.runLater(() -> txtRemotePath.setText(Path));
    }
    // Metode untuk melakukan normalisasi path
    private String normalizePath(String path) {
        return path.replace("//", "/");
    }
    // Method untuk mengosongkan tbRemote
    public void clearTableRemote() {
        tbRemote.getItems().clear();
    }
    // getter untuk treeRemote
    public TreeView<String> getTreeRemote() {
        return treeRemote;
    }
    @FXML
    // Method untuk menambahkan event handler ketika tombol enter ditekan dan ambil path dari txtRemotePath
    public void setupTxtRemotePath() {
        txtRemotePath.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                String path = txtRemotePath.getText();
                appLogic.searchRemoteDirectory(treeRemote, path);
            }
        });
    }
    @FXML
    // Method untuk menentukan bagaimana setiap sel dalam treeRemote harus ditampilkan
    public void setupRemoteTreeView() {
        treeRemote.setCellFactory(new Callback<>() {
            @Override
            public TreeCell<String> call(TreeView<String> param) {
                return new TreeCell<>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            setText(item);
                            if (item.equals("/")) { // Assume root directory is "/"
                                setGraphic(iconCache.getHomeServerIcon());
                            } else {
                                setGraphic(iconCache.getDir());
                            }
                        }
                    }
                };
            }
        });
        // Menambahkan event handler dan menerima path yang dipilih
        treeRemote.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                String selectedPath = getFullPath(newValue);
                updateTxtRemotePath(selectedPath);
                appLogic.loadFilesServerInDirectory(selectedPath, tbRemote);
            }
        });
        // Tambahkan penanganan event klik kanan
        treeRemote.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.SECONDARY) { // Right click
                TreeItem<String> selectedItem = treeRemote.getSelectionModel().getSelectedItem();
                if (selectedItem != null) {
                    String selectedPath = getFullPath(selectedItem);
                    ContextMenu contextMenu = new ContextMenu();
                    MenuItem createDirectoryItem = getMenuItemRemote(selectedPath);
                    contextMenu.getItems().addAll(createDirectoryItem);
                    contextMenu.show(treeRemote, event.getScreenX(), event.getScreenY());
                }
            }
        });
    }

    private MenuItem getMenuItemRemote(String selectedPath) {
        MenuItem createDirectoryItem = new MenuItem("Create Directory");
        createDirectoryItem.setOnAction(event -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Create Directory");
            dialog.setHeaderText("Create Directory");
            dialog.setContentText("Enter directory name:");
            Optional<String> result = dialog.showAndWait();
            result.ifPresent(name -> {
                String directoryPath = selectedPath + "/" + name;
                appLogic.createDirectoryRemote(directoryPath);
            });
        });
        return createDirectoryItem;
    }

    public void expandPath(TreeItem<String> item) {
        if (item != null) {
            TreeItem<String> parent = item.getParent();
            while (parent != null) {
                parent.setExpanded(true);
                parent = parent.getParent();
            }
        }
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

    @FXML
    public void setupTbRemote() {
        tbRemote_colFilename.setCellFactory(column ->
                new TableCell<>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item != null && !empty) {
                            FTPFile file = getTableView().getItems().get(getIndex());
                            String fileName = file.getName();
                            String extension = "";

                            int i = fileName.lastIndexOf('.');
                            if (i > 0) {
                                extension = fileName.substring(i + 1);
                            }
                            setText(item);
                            setGraphic(iconCache.getIconByExtension(extension)); // Menggunakan ikon berdasarkan ekstensi file
                        } else {
                            setText(null);
                            setGraphic(null);
                        }
                    }
                });
        tbRemote_colFilename.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));
        tbRemote_colFilesize.setCellValueFactory(cellData -> new SimpleStringProperty(Long.toString(cellData.getValue().getSize())));
        tbRemote_colFiletype.setCellValueFactory(cellData -> {
            Tika tika = new Tika();
            return new SimpleStringProperty(tika.detect(cellData.getValue().getName()));
        });
        tbRemote_colLstModif.setCellValueFactory(cellData -> new SimpleStringProperty(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(cellData.getValue().getTimestamp().getTime())));
        tbRemote.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) { // Double click detected
                FTPFile selectedFile = tbRemote.getSelectionModel().getSelectedItem();
                if (selectedFile != null) {
                    String remoteFilePath = getFullPath(treeRemote.getSelectionModel().getSelectedItem()) + "/" + selectedFile.getName();
                    String localDirectory = System.getProperty("user.home") + File.separator + "Downloads";
                    appLogic.downloadFile(remoteFilePath, localDirectory); // Download file
                }
            }
        });

    }
    @FXML
    public void setupTbLogFiles() {
        tbLogFiles_colFile.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null && !empty) {
                    LogFile logFile = getTableView().getItems().get(getIndex());
                    String fileName = logFile.getFileName();
                    String extension = "";

                    int i = fileName.lastIndexOf('.');
                    if (i > 0) {
                        extension = fileName.substring(i + 1);
                    }
                    setText(item);
                    setGraphic(iconCache.getIconByExtension(extension)); // Menggunakan ikon berdasarkan ekstensi file
                } else {
                    setText(null);
                    setGraphic(null);
                }
            }
        });
        tbLogFiles_colFile.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getFileName()));
        tbLogFiles_colDirection.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDirection()));
        tbLogFiles_colRemoteFile.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getRemoteFile()));
        tbLogFiles_colSize.setCellValueFactory(cellData -> new SimpleStringProperty(Long.toString(cellData.getValue().getSize())));
        tbLogFiles_colStatus.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatus()));
    }

    public void addLogFile(LogFile logFile) {
        tbLogFiles.getItems().add(logFile);
    }
    //////////////////////////////////////////
    /*END OF REMOTE HANDLE*/
    //////////////////////////////////////////
    @FXML
    public void initialize() {
        setupListLog();
        setupEnterKeyHandlers();
        setupLocalTreeView();
        setupTxtLocalPath();
        setupTbLocal();
        setupTxtRemotePath();
        setupTbRemote();
        setupTbLogFiles();

    }

}
