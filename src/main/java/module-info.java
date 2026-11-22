module org.example.emirp_number {
    requires javafx.controls;
    requires javafx.fxml;


    opens org.example.emirp_number to javafx.fxml;
    exports org.example.emirp_number;
}