module chat {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.rmi;
    requires java.sql;

    opens chat to javafx.fxml;
    exports chat;
    exports chat.main;
    exports chat.info;
    exports chat.UI;
    exports chat.utils;
    exports chat.database;
}
