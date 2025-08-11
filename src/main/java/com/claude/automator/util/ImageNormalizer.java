package com.claude.automator.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Normalizes images to ensure consistent bit depth and format for matching.
 * This addresses issues where saved images have different bit depths (24-bit vs 32-bit)
 * or scaling issues that prevent pattern matching.
 */
@Slf4j
@Component
public class ImageNormalizer {
    
    /**
     * Normalizes an image to consistent RGB format (24-bit) without alpha channel.
     * This ensures saved and loaded images have the same bit depth.
     * 
     * @param source The source image
     * @return A normalized RGB image
     */
    public BufferedImage normalizeToRGB(BufferedImage source) {
        if (source == null) {
            return null;
        }
        
        // If already RGB without alpha, return as-is
        if (source.getType() == BufferedImage.TYPE_INT_RGB || 
            source.getType() == BufferedImage.TYPE_3BYTE_BGR) {
            return source;
        }
        
        // Convert to RGB
        BufferedImage rgbImage = new BufferedImage(
            source.getWidth(),
            source.getHeight(),
            BufferedImage.TYPE_INT_RGB
        );
        
        Graphics2D g = rgbImage.createGraphics();
        
        // Use high quality rendering to preserve image details
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Set background color for transparent areas (match Claude's dark theme)
        g.setColor(new Color(30, 30, 30));
        g.fillRect(0, 0, source.getWidth(), source.getHeight());
        
        // Draw the image
        g.drawImage(source, 0, 0, null);
        g.dispose();
        
        log.debug("Normalized image from type {} to RGB", getImageTypeName(source.getType()));
        
        return rgbImage;
    }
    
    /**
     * Normalizes an image to consistent ARGB format (32-bit) with alpha channel.
     * 
     * @param source The source image
     * @return A normalized ARGB image
     */
    public BufferedImage normalizeToARGB(BufferedImage source) {
        if (source == null) {
            return null;
        }
        
        // If already ARGB, return as-is
        if (source.getType() == BufferedImage.TYPE_INT_ARGB) {
            return source;
        }
        
        // Convert to ARGB
        BufferedImage argbImage = new BufferedImage(
            source.getWidth(),
            source.getHeight(),
            BufferedImage.TYPE_INT_ARGB
        );
        
        Graphics2D g = argbImage.createGraphics();
        
        // Use high quality rendering
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Clear with transparent background
        g.setComposite(AlphaComposite.Clear);
        g.fillRect(0, 0, source.getWidth(), source.getHeight());
        
        // Draw the image
        g.setComposite(AlphaComposite.SrcOver);
        g.drawImage(source, 0, 0, null);
        g.dispose();
        
        log.debug("Normalized image from type {} to ARGB", getImageTypeName(source.getType()));
        
        return argbImage;
    }
    
    /**
     * Saves an image with consistent RGB format to ensure matching compatibility.
     * 
     * @param image The image to save
     * @param file The file to save to
     * @throws IOException If save fails
     */
    public void saveNormalizedImage(BufferedImage image, File file) throws IOException {
        BufferedImage normalized = normalizeToRGB(image);
        ImageIO.write(normalized, "png", file);
        log.debug("Saved normalized RGB image to: {}", file.getPath());
    }
    
    /**
     * Loads and normalizes an image to RGB format.
     * 
     * @param file The file to load from
     * @return The normalized image
     * @throws IOException If load fails
     */
    public BufferedImage loadNormalizedImage(File file) throws IOException {
        BufferedImage loaded = ImageIO.read(file);
        if (loaded == null) {
            throw new IOException("Failed to load image from: " + file.getPath());
        }
        return normalizeToRGB(loaded);
    }
    
    /**
     * Checks if two images have compatible formats for matching.
     * 
     * @param img1 First image
     * @param img2 Second image
     * @return true if formats are compatible
     */
    public boolean areFormatsCompatible(BufferedImage img1, BufferedImage img2) {
        if (img1 == null || img2 == null) {
            return false;
        }
        
        int type1 = img1.getType();
        int type2 = img2.getType();
        
        // Check if both have alpha or both don't
        boolean hasAlpha1 = img1.getColorModel().hasAlpha();
        boolean hasAlpha2 = img2.getColorModel().hasAlpha();
        
        if (hasAlpha1 != hasAlpha2) {
            log.warn("Image format mismatch - Image1: {} (alpha: {}), Image2: {} (alpha: {})",
                getImageTypeName(type1), hasAlpha1,
                getImageTypeName(type2), hasAlpha2);
            return false;
        }
        
        return true;
    }
    
    /**
     * Diagnoses image format issues.
     * 
     * @param image The image to diagnose
     * @param label A label for the image in logs
     */
    public void diagnoseImage(BufferedImage image, String label) {
        if (image == null) {
            log.info("[{}] Image is null", label);
            return;
        }
        
        log.info("[{}] Image diagnostics:", label);
        log.info("  - Dimensions: {}x{}", image.getWidth(), image.getHeight());
        log.info("  - Type: {} ({})", image.getType(), getImageTypeName(image.getType()));
        log.info("  - Has Alpha: {}", image.getColorModel().hasAlpha());
        log.info("  - Color Model: {}", image.getColorModel().getClass().getSimpleName());
        log.info("  - Num Components: {}", image.getColorModel().getNumComponents());
        log.info("  - Bits Per Pixel: {}", image.getColorModel().getPixelSize());
    }
    
    private String getImageTypeName(int type) {
        switch (type) {
            case BufferedImage.TYPE_INT_RGB: return "RGB (24-bit)";
            case BufferedImage.TYPE_INT_ARGB: return "ARGB (32-bit)";
            case BufferedImage.TYPE_3BYTE_BGR: return "BGR (24-bit)";
            case BufferedImage.TYPE_4BYTE_ABGR: return "ABGR (32-bit)";
            case BufferedImage.TYPE_BYTE_GRAY: return "GRAY (8-bit)";
            case BufferedImage.TYPE_BYTE_BINARY: return "BINARY (1-bit)";
            case BufferedImage.TYPE_INT_BGR: return "BGR (24-bit int)";
            default: return "CUSTOM/UNKNOWN (type=" + type + ")";
        }
    }
}