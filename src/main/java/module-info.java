module com.seti.seti_boardgame {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.logging;

    opens com.seti to javafx.fxml;
    opens com.seti.ui to javafx.fxml;
    exports com.seti;
    exports com.seti.ui;
}