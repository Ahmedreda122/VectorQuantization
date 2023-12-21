module com.example.vectorquan {
  requires javafx.controls;
  requires javafx.fxml;

  requires com.dlsc.formsfx;
  requires java.desktop;

  opens com.example.vectorquan to javafx.fxml;
  exports com.example.vectorquan;
}