package com.example.vectorquan;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;

import static com.example.vectorquan.VectorQuantization.decompressData;
import static com.example.vectorquan.VectorQuantization.writeCodeBooksToFile;

public class HelloController {
  @FXML
  private TextField nCodebooks;
  @FXML
  private TextField CDWidth;
  @FXML
  private TextField CDHeight;


  @FXML
  protected void onCompressButtonClick() throws IOException {
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Select a file to compress");
    // Set the initial directory to the current directory
    fileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
    File file = fileChooser.showOpenDialog(new Stage());
    BufferedImage img = VectorQuantization.loadImage(Path.of(file.getAbsolutePath()));
    LinkedHashMap<Integer, BufferedImage> codeBooks = VectorQuantization.compress(img, Integer.parseInt(nCodebooks.getText()), Integer.parseInt(CDWidth.getText()), Integer.parseInt(CDHeight.getText()));
    VectorQuantization.assignCodeBooks(codeBooks, img, Integer.parseInt(CDWidth.getText()), Integer.parseInt(CDHeight.getText()));
    VectorQuantization.writeCodeBooksToFile("compressed.bin");
  }
  @FXML
  protected void onDecompressButtonClick() throws IOException {
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Select a file to decompress");
    // Set the initial directory to the current directory
    fileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
    File file = fileChooser.showOpenDialog(new Stage());
    BufferedImage decompressedImg = VectorQuantization.decompressData(file.toString());
    VectorQuantization.saveImage(Paths.get("decompressed_output.bmp"), decompressedImg, "bmp");
  }
}
