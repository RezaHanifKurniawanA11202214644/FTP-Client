package id.my.rezahanif.Cache;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;


import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class IconLocalCache {
    private Map<String, Image> iconCache;
    private FileLocalCache fileSystemCache;
    private double iconWidth;
    private double iconHeight;

    public IconLocalCache() {
        iconCache = new HashMap<>();
        fileSystemCache = new FileLocalCache();
        // Initialize icon size using a local file icon
        determineIconSize();
    }

    public ImageView getSystemIcon(String path) {
        File file = fileSystemCache.getFile(path);
        Image image = iconCache.get(file.getName());

        if (image == null) {
            javax.swing.Icon icon = FileSystemView.getFileSystemView().getSystemIcon(file);
            if (icon != null) {
                BufferedImage bufferedImage = new BufferedImage(
                        icon.getIconWidth(),
                        icon.getIconHeight(),
                        BufferedImage.TYPE_INT_ARGB
                );
                Graphics g = bufferedImage.createGraphics();
                icon.paintIcon(null, g, 0, 0);
                g.dispose();
                image = SwingFXUtils.toFXImage(bufferedImage, null);
                iconCache.put(file.getName(), image);
            }
        }

        ImageView imageView = null;
        if (image != null) {
            imageView = new ImageView(image);
            imageView.setFitWidth(iconWidth);
            imageView.setFitHeight(iconHeight);
            imageView.setPreserveRatio(true);
        }
        return imageView;
    }

    public ImageView getIconByExtension(String extension) {
        String iconPath;
        switch (extension) {
            case "doc":
            case "docx":
                iconPath = "/id/my/rezahanif/Assets/Images/docx.png";
                break;
            case "txt":
                iconPath = "/id/my/rezahanif/Assets/Images/txt.png";
                break;
            case "exe":
                iconPath = "/id/my/rezahanif/Assets/Images/exe-file.png";
                break;
            case "jpg":
            case "jpeg":
                iconPath = "/id/my/rezahanif/Assets/Images/image.png";
                break;
            case "png":
                iconPath = "/id/my/rezahanif/Assets/Images/image.png";
                break;
            case "cpp":
                iconPath = "/id/my/rezahanif/Assets/Images/cpp.png";
                break;
            case "java":
                iconPath = "/id/my/rezahanif/Assets/Images/java.png";
                break;
            case "css":
                iconPath = "/id/my/rezahanif/Assets/Images/css.png";
                break;
            case "php":
                iconPath = "/id/my/rezahanif/Assets/Images/php.png";
                break;
            case "csv":
            case "xls":
                iconPath = "/id/my/rezahanif/Assets/Images/excel.png";
                break;
            case "pdf":
            case "pptx":
                iconPath = "/id/my/rezahanif/Assets/Images/pptx.png";
                break;
            default:
                iconPath = "/id/my/rezahanif/Assets/Images/file.png"; // ikon default jika tidak ada yang cocok
                break;
        }
        Image fileIcon = new Image(iconPath);
        ImageView imageView = new ImageView(fileIcon);
        imageView.setFitWidth(iconWidth);
        imageView.setFitHeight(iconHeight);
        imageView.setPreserveRatio(true);
        return imageView;
    }

    public ImageView getHomeServerIcon() {
        Image directoryIcon = new Image("/id/my/rezahanif/Assets/Images/homeServer.png");
        ImageView imageView = new ImageView(directoryIcon);
        imageView.setFitWidth(iconWidth);
        imageView.setFitHeight(iconHeight);
        imageView.setPreserveRatio(true);
        return imageView;
    }
    public ImageView getDir() {
        Image directoryIcon = new Image("/id/my/rezahanif/Assets/Images/folder.png");
        ImageView imageView = new ImageView(directoryIcon);
        imageView.setFitWidth(iconWidth);
        imageView.setFitHeight(iconHeight);
        imageView.setPreserveRatio(true);
        return imageView;
    }
    private void determineIconSize() {
        File file = new File(System.getProperty("user.home")); // Using user's home directory as a sample
        javax.swing.Icon icon = FileSystemView.getFileSystemView().getSystemIcon(file);
        if (icon != null) {
            this.iconWidth = icon.getIconWidth();
            this.iconHeight = icon.getIconHeight();
        }
    }
}
