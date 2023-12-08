import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.imageio.ImageIO;

public class AveragePixelsWithoutSeparation {
    public static void main(String[] args) {
        try {
            File file = new File("C:\\Users\\ahmal\\IdeaProjects\\VectorQuantization\\src\\input.jpg");
            // Load the image
            BufferedImage image = VectorQuantization.loadImage(Paths.get("C:\\Users\\ahmal\\IdeaProjects\\VectorQuantization\\src\\input.jpg"));

            // Get ARGB values of two pixels (for example, pixels at coordinates (10,10) and (20,20))
            int pixel1 = image.getRGB(10, 10);
            int pixel2 = image.getRGB(20, 20);

            // Calculate average ARGB values
            int avgPixel = ((pixel1 & 0xFFFEFEFF) + (pixel2 & 0xFFFEFEFF)) / 2; // Average without alpha

            // Create a new pixel with the average ARGB value
            BufferedImage outputImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
            outputImage.setRGB(0, 0, avgPixel);

            // Save the new pixel to an output image
            VectorQuantization.saveImage(Paths.get("C:\\Users\\ahmal\\IdeaProjects\\VectorQuantization\\src\\output.jpg") ,
                    outputImage, "jpg");

            System.out.println("Average pixel image saved successfully.");
        } catch (IOException e) {
            System.out.println("Error occurred: " + e.getMessage());
        }
    }
}

