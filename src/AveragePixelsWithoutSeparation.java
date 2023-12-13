import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
import javax.imageio.ImageIO;

public class AveragePixelsWithoutSeparation {


  public static void main(String[] args) {
    try {
      Scanner scan = new Scanner(System.in);
      int codeBookSize = scan.nextInt();
//    Vector Dimensions (n*m) (block)
      int n = scan.nextInt();
      int m = scan.nextInt();
      BufferedImage inputImage = VectorQuantization.loadImage(Paths.get("src/input1.jpg"));
      int width = inputImage.getWidth();
      int height = inputImage.getHeight();
      BufferedImage sub = inputImage.getSubimage(0 , 0, width, height);
      File fileout = new File("src/output.jpg");

//      BufferedImage image2 = new BufferedImage(w, h, BufferedImage.TYPE_3BYTE_BGR);
//
//      for (int x = 0; x < h; x++)
//        for (int y = 0; y < w; y++) {
//          int value = 0xff000000 | ((int) pixels[x][y] << 16) | ((int) pixels[x][y] << 8) | (int) pixels[x][y];
//          image2.setRGB(y, x, value);
//        }
//      System.out.println("mmmm");
  convertToGrayscale(sub);
      ImageIO.write(sub, "bmp", fileout);

      // Create the output image with the same dimensions as the input image
//            BufferedImage outputImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
//
//            // Set the pixel values of the output image to match the input image
//            outputImage.setRGB(0, 0, width, height, blocks, 0, width);
//
//            // Save the new image to an output file
//            VectorQuantization.saveImage(Paths.get("src/output.jpg"), outputImage, "jpg");
//
//            System.out.println("Output image saved successfully.");
    } catch (IOException e) {
      System.out.println("Error occurred: " + e.getMessage());
    }

  }
  public static BufferedImage convertToGrayscale(BufferedImage image) {
    int width = image.getWidth();
    int height = image.getHeight();

    for (int i = 0; i < height; i++) {
      for (int j = 0; j < width; j++) {
        image.setRGB(j, i, image.getRGB(j, i) & 0xFF);
      }
    }
    return image;
  }
}



