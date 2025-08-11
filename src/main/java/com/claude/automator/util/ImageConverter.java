package com.claude.automator.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Utility to convert images with alpha channel to RGB format
 */
public class ImageConverter {
    
    public static void main(String[] args) throws IOException {
        // Convert all prompt images to RGB
        convertDirectoryToRGB("prompt");
        convertDirectoryToRGB("working");
    }
    
    public static void convertDirectoryToRGB(String dirPath) throws IOException {
        Path directory = Paths.get(dirPath);
        if (!Files.exists(directory)) {
            System.out.println("Directory not found: " + dirPath);
            return;
        }
        
        Files.walk(directory)
            .filter(Files::isRegularFile)
            .filter(p -> p.toString().endsWith(".png"))
            .forEach(ImageConverter::convertImageToRGB);
    }
    
    private static void convertImageToRGB(Path imagePath) {
        try {
            File inputFile = imagePath.toFile();
            BufferedImage original = ImageIO.read(inputFile);
            if (original == null) {
                System.out.println("Failed to load: " + imagePath);
                return;
            }
            
            // Check if it already has no alpha
            if (!original.getColorModel().hasAlpha()) {
                System.out.println("Already RGB (no alpha): " + imagePath);
                return;
            }
            
            // Convert to RGB
            BufferedImage rgbImage = new BufferedImage(
                original.getWidth(), 
                original.getHeight(), 
                BufferedImage.TYPE_INT_RGB
            );
            
            Graphics2D g = rgbImage.createGraphics();
            // Use white background for transparency
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, rgbImage.getWidth(), rgbImage.getHeight());
            g.setComposite(AlphaComposite.SrcOver);
            g.drawImage(original, 0, 0, null);
            g.dispose();
            
            // Save back to the same file
            ImageIO.write(rgbImage, "png", inputFile);
            System.out.println("Converted to RGB: " + imagePath);
            
        } catch (IOException e) {
            System.err.println("Error converting " + imagePath + ": " + e.getMessage());
        }
    }
}