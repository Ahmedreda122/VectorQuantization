import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class VectorQuantization {
    public static BufferedImage loadImage(Path imgPath) throws IOException {
        // Load the image
        return ImageIO.read(imgPath.toFile());
    }

    public static void saveImage(Path imgPath, BufferedImage img, String formatName){
        try {
            ImageIO.write(img, formatName, imgPath.toFile());
            System.out.println("Compressed image saved successfully.");
        } catch (IOException e) {
            System.out.println("Error occurred: " + e.getMessage());
        }
    }
}
