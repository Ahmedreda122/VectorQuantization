import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;


public class VectorQuantization {
  private static LinkedHashMap<Integer, BufferedImage> codeBooks = new LinkedHashMap<>();
  private static int width, height;
  private static int CBW, CBH;

  public static BufferedImage loadImage(Path imgPath) throws IOException {
    // Load the image
    return ImageIO.read(imgPath.toFile());
  }

  public static void saveImage(Path imgPath, BufferedImage img, String formatName) {
    try {
      ImageIO.write(img, formatName, imgPath.toFile());
      System.out.println("Compressed image saved successfully.");
    } catch (IOException e) {
      System.out.println("Error occurred: " + e.getMessage());
    }
  }

  static double calcDistance(BufferedImage img1, BufferedImage img2) {
    double distance = 0;
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

        double redDiff = Math.abs(red1 - red2);
        double greenDiff = Math.abs(green1 - green2);
        double blueDiff = Math.abs(blue1 - blue2);
        distance += redDiff + greenDiff + blueDiff;
      }
    }
    return distance;
  }

  private static ArrayList<BufferedImage> DivideImgIntoSubImages(BufferedImage originalImg, int CBWidth, int CBHeight) {
    BufferedImage newImg = MakeImgDividable(originalImg, CBWidth, CBHeight);
    ArrayList<BufferedImage> subImages = new ArrayList<>();
    for (int y = 0; y < newImg.getHeight() / CBHeight; y++) {
      for (int x = 0; x < newImg.getWidth() / CBWidth; x++) {
        BufferedImage sub = newImg.getSubimage(CBWidth * x, CBHeight * y, CBWidth, CBHeight);
        subImages.add(sub);
      }
    }
    return subImages;
  }

  private static BufferedImage MakeImgDividable(BufferedImage originalImg, int CBWidth, int CBHeight) {
    int width = originalImg.getWidth();
    int height = originalImg.getHeight();
    int heightDiff = 0;
    int widthDiff = 0;
    int modX = width % CBWidth;
    int modY = height % CBHeight;
    if (modX != 0) {
      // The amount of pixels we will add to the image width to make it dividable by CodeBook Width
      widthDiff = CBWidth - modX;
    }
    if (modY != 0) {
      // The amount of pixels we will add to the image height to make it dividable by CodeBook height
      heightDiff = CBHeight - modY;
    }
    BufferedImage newImg;
    if (widthDiff != 0 && heightDiff != 0) {
      // Create a new BufferedImage with the new dimensions which can be dividable by codeBooks width and height
      newImg = new BufferedImage(width + widthDiff, height + heightDiff, BufferedImage.TYPE_INT_RGB);

      // Copy original image to the new image
      for (int y = 0; y < height; y++) {
        for (int x = 0; x < width; x++) {
          newImg.setRGB(x, y, originalImg.getRGB(x, y));
        }
      }

    } else {
      newImg = originalImg;
    }
    return newImg;
  }

  private static ArrayList<ArrayList<BufferedImage>> DivideImgToMatrixofSubImgs(BufferedImage originalImg, int CBWidth, int CBHeight) {
    BufferedImage newImg = MakeImgDividable(originalImg, CBWidth, CBHeight);
    ArrayList<ArrayList<BufferedImage>> subImgsMatrix = new ArrayList<>();
    for (int y = 0; y < newImg.getHeight() / CBHeight; y++) {
      ArrayList<BufferedImage> rowSubImages = new ArrayList<>();
      for (int x = 0; x < newImg.getWidth() / CBWidth; x++) {
        BufferedImage sub = newImg.getSubimage(CBWidth * x, CBHeight * y, CBWidth, CBHeight);
        rowSubImages.add(sub);
//                saveImage(Paths.get("src/codeBook"+  y  +" "+ x +".jpg"), sub, "jpg");
      }
      subImgsMatrix.add(rowSubImages);
    }
    return subImgsMatrix;
  }

  public static LinkedHashMap<Integer, BufferedImage> compress(BufferedImage originalImg, int nCodeBooks, int CBWidth, int CBHeight) {
    width = originalImg.getWidth();
    height = originalImg.getHeight();
    CBH = CBHeight;
    CBW = CBWidth;
    ArrayList<BufferedImage> subImages = DivideImgIntoSubImages(originalImg, CBWidth, CBHeight);
    BufferedImage avgImg = getAVG(subImages);
    codeBooks.put(codeBooks.size(), avgImg);
//        saveImage(Paths.get("src/avg_output.jpg"), avgImg, "jpg");
    for (Map.Entry<Integer, BufferedImage> codeBook : codeBooks.entrySet()) {
      while (codeBooks.size() < nCodeBooks) {
        BufferedImage rightImg = new BufferedImage(CBWidth, CBHeight, BufferedImage.TYPE_INT_RGB);
        BufferedImage leftImg = new BufferedImage(CBWidth, CBHeight, BufferedImage.TYPE_INT_RGB);
        // Splitting
        for (int y = 0; y < CBHeight; y++) {
          for (int x = 0; x < CBWidth; x++) {
            rightImg.setRGB(x, y, codeBook.getValue().getRGB(x, y) + 1);
            leftImg.setRGB(x, y, codeBook.getValue().getRGB(x, y) - 1);
          }
        }

        ArrayList<BufferedImage> leftSubImages = new ArrayList<>();
        ArrayList<BufferedImage> rightSubImages = new ArrayList<>();
        for (BufferedImage subImage : subImages) {
          double rightDistance = calcDistance(subImage, rightImg);
          double leftDistance = calcDistance(subImage, leftImg);

          if (rightDistance <= leftDistance) {
            rightSubImages.add(subImage);
          } else {
            leftSubImages.add(subImage);
          }
        }
        BufferedImage newCodeBook = getAVG(leftSubImages);
        BufferedImage newCodeBook2 = getAVG(rightSubImages);
        codeBooks.remove(codeBook.getKey());
        int size = codeBooks.size();
        codeBooks.put(size, newCodeBook);
        codeBooks.put(size + 1, newCodeBook2);
//                System.out.println(codeBooks.size());
      }
      break;
    }

    // For ODD nCodeBooks
    while (codeBooks.size() > nCodeBooks) {
      codeBooks.replace(0, codeBooks.get(codeBooks.size()));
      codeBooks.remove(codeBooks.size());
    }

    // Key=> Distance, Value => CodeBookKey
    // Distance between current SubImage and the CodeBook that we store its key in the "Value"
    HashMap<Double, Integer> distances = new HashMap<>();
    // Key => CodeBook Key , List of SubImages that nearer to this codeBook
    HashMap<Integer, ArrayList<BufferedImage>> nearestSubImages = new HashMap<>();
    int counter = 0;
    for (int i = 0; i < 200; ++i) {
      for (BufferedImage subImage : subImages) {
        for (Map.Entry<Integer, BufferedImage> codeBook : codeBooks.entrySet()) {
          // calc all subImage distances with the codebooks
          distances.put(calcDistance(subImage, codeBook.getValue()), codeBook.getKey());
        }
        // Finding the Nearest Distance
        Double minDistance = findMinValue(distances);
        // Nearest CodeBook to the current SubImage
        Integer codeBookKey = distances.get(minDistance);

        if (nearestSubImages.containsKey(codeBookKey)) {
          nearestSubImages.get(codeBookKey).add(subImage);
        } else {
          ArrayList<BufferedImage> subImgs = new ArrayList<>();
          subImgs.add(subImage);
          nearestSubImages.put(codeBookKey, subImgs);
        }
        distances.clear();
      }

      for (Map.Entry<Integer, ArrayList<BufferedImage>> nearestSubImgs : nearestSubImages.entrySet()) {
        BufferedImage newCodeBook = getAVG(nearestSubImgs.getValue());
        BufferedImage oldCodeBooks = codeBooks.get(nearestSubImgs.getKey());
        if (newCodeBook.equals(oldCodeBooks)) {
          counter++;
        }
        codeBooks.replace(nearestSubImgs.getKey(), newCodeBook);
      }
      if (counter == codeBooks.size()) {
        return codeBooks;
      } else {
        counter = 0;
      }
      nearestSubImages.clear();
    }
    return codeBooks;
  }

  static Map<Integer, ArrayList<int[]>> codeBookPositions = new HashMap<>();

  public static void writeCodeBooksToFile(String outputPath) throws IOException {
    // Try-with-resources statement to auto-close the writer
    try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath))) {
      for (Map.Entry<Integer, BufferedImage> entry : codeBooks.entrySet()) {
        Integer key = entry.getKey();
        BufferedImage image = entry.getValue();

        writer.write(key.toString());
        writer.write(" ");

        for (int y = 0; y < image.getHeight(); y++) {
          for (int x = 0; x < image.getWidth(); x++) {
            int rgb = image.getRGB(x, y);

            // Extract individual RGB components
            int red = (rgb >> 16) & 0xFF;
            int green = (rgb >> 8) & 0xFF;
            int blue = rgb & 0xFF;
            writer.write(red + " " + green + " " + blue + " ");
          }
        }

        writer.newLine();
      }
      writer.write("-------------------------------");
      writer.newLine();

      for (Map.Entry<Integer, ArrayList<int[]>> entry : codeBookPositions.entrySet()) {
        Integer key = entry.getKey();
        ArrayList<int[]> positions = entry.getValue();
        // Write key to file
        writer.write(key.toString());
        writer.newLine();

        // Write positions to file
        for (int[] position : positions) {
          writer.write(position[0] + " " + position[1] + " ");
          writer.newLine();
        }

        // separator line
        writer.write("-------------------------------");
        writer.newLine();
      }

    }
  }

  public static void printCodeBooks() {
    for (Map.Entry<Integer, BufferedImage> entry : codeBooks.entrySet()) {
      Integer key = entry.getKey();
      BufferedImage image = entry.getValue();

      // Get the RGB values for each pixel
      for (int y = 0; y < image.getHeight(); y++) {
        for (int x = 0; x < image.getWidth(); x++) {
          int rgb = image.getRGB(x, y);

          // Extract individual RGB components
          int red = (rgb >> 16) & 0xFF;
          int green = (rgb >> 8) & 0xFF;
          int blue = rgb & 0xFF;

          // Do something with the RGB values
//          System.out.println("Key: " + key + ", Pixel at (" + x + ", " + y + "): R=" + red + ", G=" + green + ", B=" + blue);
        }
      }
    }
  }


  private static Integer getKeyByValue(Map<Integer, BufferedImage> map, BufferedImage value) {
    for (Map.Entry<Integer, BufferedImage> entry : map.entrySet()) {
      if (value.equals(entry.getValue())) {
        return entry.getKey();
      }
    }
    return null;
  }

  public static BufferedImage assignCodeBooks(LinkedHashMap<Integer, BufferedImage> codeBooks, BufferedImage originalImg, int CBWidth, int CBHeight) throws IOException {
    printCodeBooks();

    ArrayList<ArrayList<BufferedImage>> matrix = DivideImgToMatrixofSubImgs(originalImg, CBWidth, CBHeight);

    for (int i = 0; i < matrix.size(); i++) {
      ArrayList<BufferedImage> rowSubImages = matrix.get(i);
      ArrayList<int[]> rowPositions = new ArrayList<>();

      for (int j = 0; j < rowSubImages.size(); j++) {
        BufferedImage subImage = rowSubImages.get(j);
        BufferedImage nearestCodeBook = nearestCodeBook(subImage);
        Integer key = getKeyByValue(codeBooks, nearestCodeBook);
        int[] position = { (CBW*j),(CBH*i)};
        rowPositions.add(position);

        ArrayList<int[]> positions = codeBookPositions.get(key);
        if (positions == null) {
          positions = new ArrayList<>();
          codeBookPositions.put(key, positions);
        }
        positions.add(position);
      }
    }

    writeCodeBooksToFile("compressed.bin");
    for (ArrayList<BufferedImage> rowSubImages : matrix) {
      rowSubImages.replaceAll(VectorQuantization::nearestCodeBook);
    }

    BufferedImage compressedImg = new BufferedImage(originalImg.getWidth(), originalImg.getHeight(), BufferedImage.TYPE_INT_RGB);
    int posX = 0;
    int posY = 0;

    for (ArrayList<BufferedImage> row : matrix) {
      for (BufferedImage subImage : row) {
        int subImgWidth = subImage.getWidth();
        int subImgHeight = subImage.getHeight();

        for (int y = 0; y < subImgHeight && posY + y < compressedImg.getHeight(); y++) {
          for (int x = 0; x < subImgWidth && posX + x < compressedImg.getWidth(); x++) {
            compressedImg.setRGB(posX + x, posY + y, subImage.getRGB(x, y));
          }
        }
        posX += CBWidth;
        if (posX >= compressedImg.getWidth()) {
          posX = 0;
          posY += CBHeight;
        }
      }
    }
    return compressedImg;
  }

  private static BufferedImage nearestCodeBook(BufferedImage img) {
    double minDistance = Double.MAX_VALUE;
    BufferedImage nearestCodeBook = null;
    for (Map.Entry<Integer, BufferedImage> codeBook : codeBooks.entrySet()) {
      double distance = calcDistance(img, codeBook.getValue());
      if (distance < minDistance) {
        minDistance = distance;
        nearestCodeBook = codeBook.getValue();
      }
    }
    return nearestCodeBook;
  }

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

  private static Double findMinValue(HashMap<Double, Integer> map) {
    // Initialize min with the maximum possible integer value
    Double min = Double.MAX_VALUE;
    // Iterate through the entries to find the minimum value
    for (HashMap.Entry<Double, Integer> entry : map.entrySet()) {
      Double currentValue = entry.getKey();
      if (currentValue < min) {
        min = currentValue;
      }
    }
    return min;
  }

  private static Map<Integer, BufferedImage> decompressedCodeBooks = new HashMap<>();
  private static Map<Integer, ArrayList<int[]>> decompressedCodeBookPositions = new HashMap<>();

  public static BufferedImage decompressData(String compressedDataPath) throws IOException {
    try (BufferedReader reader = new BufferedReader(new FileReader(compressedDataPath))) {
      String line;
      boolean isCodeBookPositions = false;
      String key;
      while ((line = reader.readLine()) != null) {
        if (line.startsWith("-------------------------------")) {
          isCodeBookPositions = true;
          line = reader.readLine(); // key
          key = line;
//          System.out.println("Key: " + key);
          while ((line = reader.readLine()) != null && !line.startsWith("-------------------------------")) {
            readCodeBookPosition(key, line);
          }
        } else {
          if (!isCodeBookPositions)
            readCodeBook(line);
          else {
            key = line;
            System.out.println("Key: " + key);
            while ((line = reader.readLine()) != null && !line.startsWith("-------------------------------")) {
              readCodeBookPosition(key, line);
            }
          }
        }

      }
      return generateDecompressedImage();
    }
  }

  private static void readCodeBookPosition(String key, String line) throws IOException {
    Integer keyINT = Integer.parseInt(key);
    ArrayList<int[]> positions = new ArrayList<>();
    String[] positionParts = line.split(" ");

    int x = Integer.parseInt(positionParts[0]);
    int y = Integer.parseInt(positionParts[1]);
//    System.out.println(x);
//    System.out.println(y);
    positions.add(new int[]{x, y});

    if (decompressedCodeBookPositions.containsKey(keyINT)) {
      ArrayList<int[]> pos = decompressedCodeBookPositions.get(keyINT);
      pos.add(new int[]{x, y});
      decompressedCodeBookPositions.put(keyINT, pos);
    } else {
      decompressedCodeBookPositions.put(keyINT, positions);
    }

    // Add the positions to the map
  }

  private static void readCodeBook(String line) {
    // Split the line and parse key and RGB values
    String[] parts = line.split(" ");

    Integer key = Integer.parseInt(parts[0]);

    // new BufferedImage for the code book entry
    BufferedImage image = new BufferedImage(CBW, CBH, BufferedImage.TYPE_INT_RGB);

    int pixelIndex = 1;
    for (int y = 0; y < image.getHeight(); y++) {
      for (int x = 0; x < image.getWidth(); x++) {
        int red = Integer.parseInt(parts[pixelIndex++]);
        int green = Integer.parseInt(parts[pixelIndex++]);
        int blue = Integer.parseInt(parts[pixelIndex++]);
        int rgb = (red << 16) | (green << 8) | blue;
        image.setRGB(x, y, rgb);
      }
    }
    decompressedCodeBooks.put(key, image);
  }


  private static BufferedImage generateDecompressedImage() {
    BufferedImage decompressedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

    for (Map.Entry<Integer, ArrayList<int[]>> entry : decompressedCodeBookPositions.entrySet()) {
      Integer key = entry.getKey();
      ArrayList<int[]> positions = entry.getValue();
      BufferedImage codeBook = decompressedCodeBooks.get(key);
      for (int[] position : positions) {
        int x = position[0];
        int y = position[1];

        for (int yy = 0; yy < CBH; yy++) {
          for (int xx = 0; xx < CBW; xx++) {

            int codeBookRGB = codeBook.getRGB(xx, yy);

            decompressedImage.setRGB(x + xx, y + yy, codeBookRGB);
          }
        }
      }
    }
    return decompressedImage;
  }

  public static void main(String[] args) throws IOException {
    BufferedImage img = loadImage(Paths.get("src/img.png"));
    LinkedHashMap<Integer, BufferedImage> codeBooks = compress(img, 4, 8, 8);
    BufferedImage compressedImg = assignCodeBooks(codeBooks, img, 8, 8);
            saveImage(Paths.get("src/compressed_output.bmp"), compressedImg, "bmp");
    BufferedImage decompressedImg = decompressData("compressed.bin");

    // Optionally, you can save the decompressed image
    saveImage(Paths.get("src/decompressed_output.bmp"), decompressedImg, "bmp");
  }
}