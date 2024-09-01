module id.my.rezahanif {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.swing;
    requires org.apache.commons.net;
    requires java.desktop;
    requires org.apache.tika.core;
    requires org.apache.commons.io;
    requires org.slf4j;
    requires java.sql;
    requires jdk.compiler;

    opens id.my.rezahanif to javafx.fxml;
    opens id.my.rezahanif.Controller to javafx.fxml;
    exports id.my.rezahanif;
}