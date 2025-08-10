package com.claude.automator.debug;

import io.github.jspinak.brobot.model.element.Region;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Debug utility to directly test highlighting without going through Brobot's layers.
 */
@Component
public class HighlightDebugger {
    
    /**
     * Directly highlight a region using Java AWT/Swing to bypass SikuliX.
     */
    public void highlightWithSwing(Region region, int durationMs) {
        System.out.println("\n=== SWING HIGHLIGHT DEBUG ===");
        System.out.println("Region to highlight: " + region.toString());
        System.out.println("  X: " + region.getX());
        System.out.println("  Y: " + region.getY());
        System.out.println("  Width: " + region.getW());
        System.out.println("  Height: " + region.getH());
        
        SwingUtilities.invokeLater(() -> {
            JWindow window = new JWindow();
            window.setAlwaysOnTop(true);
            window.setBounds(region.getX(), region.getY(), region.getW(), region.getH());
            window.setBackground(new Color(0, 0, 255, 50)); // Semi-transparent blue
            
            JPanel panel = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setColor(Color.BLUE);
                    g2.setStroke(new BasicStroke(3));
                    g2.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
                }
            };
            panel.setOpaque(false);
            window.add(panel);
            window.setVisible(true);
            
            System.out.println("Swing highlight window created at: " + window.getBounds());
            
            Timer timer = new Timer(durationMs, e -> {
                window.dispose();
                System.out.println("Swing highlight window disposed");
            });
            timer.setRepeats(false);
            timer.start();
        });
    }
    
    /**
     * Test SikuliX highlighting with detailed debugging.
     */
    public void testSikuliXHighlight(Region region) {
        System.out.println("\n=== SIKULIX HIGHLIGHT DEBUG ===");
        System.out.println("Input Region: " + region.toString());
        
        try {
            // Create SikuliX region
            org.sikuli.script.Region sikuliRegion = region.sikuli();
            System.out.println("SikuliX Region created: " + sikuliRegion);
            System.out.println("  X: " + sikuliRegion.x);
            System.out.println("  Y: " + sikuliRegion.y);
            System.out.println("  Width: " + sikuliRegion.w);
            System.out.println("  Height: " + sikuliRegion.h);
            System.out.println("  Screen: " + sikuliRegion.getScreen());
            System.out.println("  Valid: " + sikuliRegion.isValid());
            
            // Get the screen
            try {
                org.sikuli.script.Screen screen = (org.sikuli.script.Screen) sikuliRegion.getScreen();
                if (screen != null) {
                    System.out.println("Screen info:");
                    System.out.println("  ID: " + screen.getID());
                    System.out.println("  Bounds: x=" + screen.x + ", y=" + screen.y + 
                                     ", w=" + screen.w + ", h=" + screen.h);
                }
            } catch (Exception e) {
                System.out.println("Could not get screen info: " + e.getMessage());
            }
            
            // Attempt highlight
            System.out.println("\nAttempting SikuliX highlight...");
            sikuliRegion.highlight(2);
            System.out.println("SikuliX highlight called successfully");
            
        } catch (Exception e) {
            System.err.println("ERROR in SikuliX highlight: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Compare multiple highlighting methods.
     */
    public void compareHighlightMethods() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("HIGHLIGHT COMPARISON TEST");
        System.out.println("=".repeat(80));
        
        // Create the lower left quarter region
        Region lowerLeft = Region.builder()
            .withScreenPercentage(0.0, 0.5, 0.5, 0.5)
            .build();
        
        System.out.println("\nTarget Region (Lower Left Quarter):");
        System.out.println("  " + lowerLeft.toString());
        System.out.println("  Should cover: X(0-960), Y(540-1080) on 1920x1080 screen");
        
        // Test 1: Direct Swing highlighting
        System.out.println("\n1. Testing Swing highlight (should be accurate):");
        highlightWithSwing(lowerLeft, 2000);
        
        // Wait a bit
        try {
            Thread.sleep(2500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Test 2: SikuliX highlighting
        System.out.println("\n2. Testing SikuliX highlight:");
        testSikuliXHighlight(lowerLeft);
        
        System.out.println("\n" + "=".repeat(80));
        System.out.println("TEST COMPLETE");
        System.out.println("Compare the two highlights to identify discrepancies");
        System.out.println("=".repeat(80));
    }
    
    /**
     * Draw a grid to help visualize screen quadrants.
     */
    public void drawDebugGrid() {
        System.out.println("\n=== DRAWING DEBUG GRID ===");
        
        SwingUtilities.invokeLater(() -> {
            // Get screen dimensions
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            int width = screenSize.width;
            int height = screenSize.height;
            
            System.out.println("Screen size: " + width + "x" + height);
            
            // Create transparent window covering entire screen
            JWindow gridWindow = new JWindow();
            gridWindow.setAlwaysOnTop(true);
            gridWindow.setBounds(0, 0, width, height);
            gridWindow.setBackground(new Color(0, 0, 0, 0));
            
            JPanel gridPanel = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                                       RenderingHints.VALUE_ANTIALIAS_ON);
                    
                    // Draw grid lines
                    g2.setColor(new Color(255, 0, 0, 100)); // Red, semi-transparent
                    g2.setStroke(new BasicStroke(2));
                    
                    // Vertical center line
                    g2.drawLine(width / 2, 0, width / 2, height);
                    
                    // Horizontal center line
                    g2.drawLine(0, height / 2, width, height / 2);
                    
                    // Label quadrants
                    g2.setColor(Color.BLACK);
                    g2.setFont(new Font("Arial", Font.BOLD, 20));
                    
                    // Upper left
                    g2.drawString("UPPER LEFT", 10, 30);
                    g2.drawString("(0,0) to (960,540)", 10, 55);
                    
                    // Upper right
                    g2.drawString("UPPER RIGHT", width / 2 + 10, 30);
                    g2.drawString("(960,0) to (1920,540)", width / 2 + 10, 55);
                    
                    // Lower left
                    g2.drawString("LOWER LEFT", 10, height / 2 + 30);
                    g2.drawString("(0,540) to (960,1080)", 10, height / 2 + 55);
                    
                    // Lower right
                    g2.drawString("LOWER RIGHT", width / 2 + 10, height / 2 + 30);
                    g2.drawString("(960,540) to (1920,1080)", width / 2 + 10, height / 2 + 55);
                    
                    // Highlight lower left quadrant
                    g2.setColor(new Color(0, 0, 255, 30)); // Blue, very transparent
                    g2.fillRect(0, height / 2, width / 2, height / 2);
                    
                    g2.setColor(Color.BLUE);
                    g2.setStroke(new BasicStroke(3));
                    g2.drawRect(0, height / 2, width / 2, height / 2);
                }
            };
            
            gridPanel.setOpaque(false);
            gridWindow.add(gridPanel);
            gridWindow.setVisible(true);
            
            System.out.println("Grid overlay displayed");
            System.out.println("Lower left quadrant highlighted in blue");
            
            // Keep grid visible for 5 seconds
            Timer timer = new Timer(5000, e -> {
                gridWindow.dispose();
                System.out.println("Grid overlay removed");
            });
            timer.setRepeats(false);
            timer.start();
        });
    }
}