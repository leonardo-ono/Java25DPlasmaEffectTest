import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/**
 * 2.5D Plasma Effect.
 * 
 * @author Leonardo Ono (ono.leo@gmail.com);
 */
public class View extends Canvas {
    
    private static final int SCREEN_WIDTH = 640, SCREEN_HEIGHT = 480;
    private static final int MAP_WIDTH = 1024, MAP_HEIGHT = 1024;
    
    private final BufferedImage offscreen;
    private final Plasma plasma;
    private final Color fogColor = Color.DARK_GRAY; 
    private double viewX, viewY; // view position
    private double viewDirectionAngle; // view direction angle in rad
    private BufferStrategy bs;
    private boolean running;
    
    public View() {
        offscreen = new BufferedImage(400, 300, BufferedImage.TYPE_INT_RGB);
        plasma = new Plasma(100, 100);
    }
    
    public void start() {
        createBufferStrategy(2);
        bs = getBufferStrategy();
        running = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (running) {
                    update();
                    Graphics2D g = (Graphics2D) bs.getDrawGraphics();
                    draw(offscreen.createGraphics());
                    g.drawImage(offscreen, 0, 0, SCREEN_WIDTH, SCREEN_HEIGHT, 
                        0, 0, 400, 300, null);
                    
                    g.dispose();
                    bs.show();

                    try {
                        Thread.sleep(1000 / 60);
                    } catch (InterruptedException ex) {
                    }
                }
            }
        }).start();
    }

    private void update() {
        viewDirectionAngle += 0.0025;
        viewX = 512 + 100 * Math.sin(viewDirectionAngle - 3.14);
        viewY = 512 + 100 * Math.cos(viewDirectionAngle + 1.57);
        
        plasma.update();
    }
    
    private void draw(Graphics2D g) {
        g.setBackground(fogColor);
        g.clearRect(0, 0, getWidth(), getHeight());
        
        // cast rays
        int sx = 0;
        for (double angle = -0.5; angle < 1; angle += 0.0035) {
            int maxScreenHeight = getHeight();
            double s = Math.cos(viewDirectionAngle + angle);
            double c = Math.sin(viewDirectionAngle + angle);
            for (int depth = 10; depth < 600; depth += 1) {
                int hmx = (int) (viewX + depth * s);
                int hmy = (int) (viewY + depth * c);
                if (hmx < 0 || hmy < 0 || hmx > MAP_WIDTH - 1 || hmy > MAP_HEIGHT - 1) {
                    continue;
                }
                
                int mapX = (int) (plasma.getOffscreenColor().getWidth() * (hmx / 1024.0));
                int mapY = (int) (plasma.getOffscreenColor().getHeight() * (hmy / 1024.0));
                int height = (int) (1.5 * (plasma.getOffscreenHeight().getRGB(mapX, mapY) & 255));
                int color = addFog(plasma.getOffscreenColor().getRGB(mapX, mapY), depth);
                //int color = plasma.getOffscreenColor().getRGB(mapX, mapY);
                
                // draw 3D vertical terrain line / circular projection
                double sy = 120 * (600 - height) / depth; 
                if (sy > maxScreenHeight) {
                    continue;
                }
                for (int y = (int) sy; y <= maxScreenHeight; y++) {
                    if (y < 0 || sx > offscreen.getWidth() - 1 || y > offscreen.getHeight() - 1) {
                        continue;
                    }
                    offscreen.setRGB(sx, y, color);
                }
                maxScreenHeight = (int) sy;
            }
            sx++;
        }
        
        //g.drawImage(plasma.getOffscreenColor(), 0, 0, null);
        //g.drawImage(plasma.getOffscreenHeight(), plasma.getOffscreenColor().getWidth(), 0, null);
    }
    
    private int addFog(int color, int depth) {
        int r = (color >> 16) & 255;
        int g = (color >> 8) & 255;
        int b = color & 255;
        double p = depth > 350 ? (Math.min(100, depth - 350)) / 100.0 : 0;
        r = (int) (r + (fogColor.getRed() - r) * p);
        g = (int) (g + (fogColor.getGreen() - g) * p);
        b = (int) (b + (fogColor.getBlue() - b) * p);
        return (r << 16) + (g << 8) + b;
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                View view = new View();
                view.setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
                JFrame frame = new JFrame();
                frame.setTitle("Java 2.5D Plasma Effect Test");
                frame.getContentPane().add(view);
                frame.setResizable(false);
                frame.pack();
                frame.setLocationRelativeTo(null);
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setVisible(true);
                view.start();
            }
        });
    }
    
}
