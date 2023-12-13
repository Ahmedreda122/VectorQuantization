import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;


public class VectorQuantization {
    private static LinkedHashMap<Integer, BufferedImage> codeBooks;
    private static ArrayList<BufferedImage> subImages;

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

    private static void DivideImgIntoSubImages(BufferedImage originalImg, int CBWidth, int CBHeight){
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

        for (int y = 0; y < newImg.getHeight() / CBHeight; y++) {
            for (int x = 0; x < newImg.getWidth() / CBWidth; x++) {
                BufferedImage sub = newImg.getSubimage(CBWidth * x, CBHeight * y, CBWidth, CBHeight);
                subImages.add(sub);
            }
        }
    }
    public static LinkedHashMap<Integer, BufferedImage> compress(BufferedImage originalImg, int nCodeBooks, int CBWidth, int CBHeight) {
        DivideImgIntoSubImages(originalImg, CBWidth, CBHeight);
        BufferedImage avgImg = getAVG(subImages);
        codeBooks.put(codeBooks.size(), avgImg);
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
                codeBooks.put(codeBooks.size(), newCodeBook);
                codeBooks.put(codeBooks.size(), newCodeBook2);
            }
            break;
        }

        // For ODD nCodeBooks
        while (codeBooks.size() != nCodeBooks) {
            codeBooks.replace(0, codeBooks.get(codeBooks.size()));
            codeBooks.remove(codeBooks.size());
        }

        // Key=> Distance, Value => CodeBookKey
        // Distance between current SubImage and the CodeBook that we store its key in the "Value"
        HashMap<Double, Integer> distances = new HashMap<>();
        // Key => CodeBook Key , List of SubImages that nearer to this codeBook
        HashMap<Integer, ArrayList<BufferedImage>> nearestSubImages = new HashMap<>();

        for (int i = 0; i < 50; ++i) {
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
            int counter = 0;
            for (Map.Entry<Integer, ArrayList<BufferedImage>> nearestSubImgs : nearestSubImages.entrySet()) {
                BufferedImage newCodeBook = getAVG(nearestSubImgs.getValue());
                BufferedImage oldCodeBooks = codeBooks.get(nearestSubImgs.getKey());
                if (newCodeBook.equals(oldCodeBooks)){
                    counter++;
                }
                codeBooks.replace(nearestSubImgs.getKey(), newCodeBook);
            }
            if(counter == codeBooks.size()){
                return codeBooks;
            }
            else{
                counter = 0;
            }
            nearestSubImages.clear();
        }
        return codeBooks;
    }

    private static BufferedImage assignCodeBooks(LinkedHashMap<Integer, BufferedImage> codeBooks, int height,int width){
        subImages.replaceAll(VectorQuantization::nearestCodeBook);
        // TODO: Saparate To method
        BufferedImage compressedImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        // Copy original image to the new image
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                for(BufferedImage subImg: subImages) {
                    compressedImg.setRGB(x, y, subImg.getRGB(x % CBWidth, y % CBHeight));
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
        int CBHeight = subImages.getFirst().getHeight();
        int CBWidth = subImages.getFirst().getWidth();
        BufferedImage avgImg = new BufferedImage(CBWidth, CBHeight, BufferedImage.TYPE_INT_RGB);
        int pixel;
        for (int y = 0; y < CBHeight; y++) {
            for (int x = 0; x < CBWidth; x++) {
                pixel = 0;
                for (BufferedImage subImage : subImages) {
                    pixel += subImage.getRGB(x, y);
                }
                avgImg.setRGB(x, y, (pixel / subImages.size()));
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


    public static void main(String[] args) throws IOException {
        BufferedImage img = loadImage(Paths.get("src/input.jpg"));
        compress(img, 4, 30, 30);
    }
}