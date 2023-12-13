import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Scanner;
import javax.imageio.ImageIO;

public class AveragePixelsWithoutSeparation {
  private static BufferedImage getAVG(ArrayList<BufferedImage> subImages) {
    int CBHeight = subImages.get(0).getHeight();
    int CBWidth = subImages.get(0).getWidth();
    BufferedImage avgImg = new BufferedImage(CBWidth, CBHeight, BufferedImage.TYPE_INT_RGB);

    for (int y = 0; y < CBHeight; y++) {
      for (int x = 0; x < CBWidth; x++) {
        int redSum = 0;
        int greenSum = 0;
        int blueSum = 0;

        for (BufferedImage subImage : subImages) {
          int pixel = subImage.getRGB(x, y);
          redSum += (pixel >> 16) & 0xFF;
          greenSum += (pixel >> 8) & 0xFF;
          blueSum += (pixel) & 0xFF;
        }

        int avgRed = redSum / subImages.size();
        int avgGreen = greenSum / subImages.size();
        int avgBlue = blueSum / subImages.size();

        int avgPixel = (avgRed << 16) | (avgGreen << 8) | avgBlue;
        avgImg.setRGB(x, y, avgPixel);
      }
    }
    return avgImg;
  }


  public static void main(String[] args) {
//    try {
//      Scanner scan = new Scanner(System.in);
//      int codeBookSize = scan.nextInt();
////    Vector Dimensions (n*m) (block)
//      int n = scan.nextInt();
//      int m = scan.nextInt();
//      BufferedImage inputImage = VectorQuantization.loadImage(Paths.get("src/input1.jpg"));
//      int width = inputImage.getWidth();
//      int height = inputImage.getHeight();
//      BufferedImage sub = inputImage.getSubimage(0 , 0, width, height);
//      File fileout = new File("src/output.jpg");

//      BufferedImage image2 = new BufferedImage(w, h, BufferedImage.TYPE_3BYTE_BGR);
//
//      for (int x = 0; x < h; x++)
//        for (int y = 0; y < w; y++) {
//          int value = 0xff000000 | ((int) pixels[x][y] << 16) | ((int) pixels[x][y] << 8) | (int) pixels[x][y];
//          image2.setRGB(y, x, value);
//        }
//      System.out.println("mmmm");
//  convertToGrayscale(sub);
//      ImageIO.write(sub, "bmp", fileout);

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
//    } catch (IOException e) {
//      System.out.println("Error occurred: " + e.getMessage());
//    }
    try {
      BufferedImage img1 = VectorQuantization.loadImage(Paths.get("src/input1.jpg"));
      BufferedImage img2 = VectorQuantization.loadImage(Paths.get("src/input.jpg"));
      ArrayList<BufferedImage> subImages = new ArrayList<>();
      subImages.add(img1);
      subImages.add(img2);
      BufferedImage avg = getAVG(subImages);
     // saveImage(Paths.get("src/compressed_output.jpg"), compressedImg, "bmp");

    } catch (IOException e) {
      throw new RuntimeException(e);
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


  static double calcDistance(BufferedImage img1, BufferedImage img2) {
    if (img1.getWidth() != img2.getWidth() || img1.getHeight() != img2.getHeight()) {
      throw new IllegalArgumentException("Images should have the same dimensions");
    }

    double mse = 0;
    for (int y = 0; y < img1.getHeight(); y++) {
      for (int x = 0; x < img1.getWidth(); x++) {
        int rgb1 = img1.getRGB(x, y);
        int rgb2 = img2.getRGB(x, y);

        int red1 = (rgb1 >> 16) & 0xFF;
        int green1 = (rgb1 >> 8) & 0xFF;
        int blue1 = rgb1 & 0xFF;

        int red2 = (rgb2 >> 16) & 0xFF;
        int green2 = (rgb2 >> 8) & 0xFF;
        int blue2 = rgb2 & 0xFF;

        double redDiff = Math.pow(red1 - red2, 2);
        double greenDiff = Math.pow(green1 - green2, 2);
        double blueDiff = Math.pow(blue1 - blue2, 2);
        mse += redDiff + greenDiff + blueDiff;
      }
      // Calculate mean squared error
      mse /= (double) (img1.getWidth() * img1.getHeight());

    }
    return mse;

  }}



