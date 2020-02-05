import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

/**
 * Plasma class.
 * 
 * @author Leonardo Ono (ono.leo@gmail.com)
 */
public class Plasma {
    
    private final BufferedImage offscreenHeight;
    private final BufferedImage offscreenColor;
    private final int[] dataHeight;
    private final int[] dataColor;
    private final int[] data;
    private final int[] palette = new int[256];
    private double t;
    
    public Plasma(int width, int height) {
        offscreenHeight = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        dataHeight = ((DataBufferInt) offscreenHeight.getRaster().getDataBuffer()).getData();
        offscreenColor = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        dataColor = ((DataBufferInt) offscreenColor.getRaster().getDataBuffer()).getData();
        data = new int[width * height];
        t = 1234.56;
        // precalculate palette
        for (int x = 0; x < 256; x++) {
            palette[x] = Color.HSBtoRGB(x / 255f, 1f, 1f);
        }        
    }

    public void update() {
        t += 0.5;
        for (int y = 0; y < offscreenHeight.getHeight(); y++) {
            for (int x = 0; x < offscreenHeight.getWidth(); x++) {
                double v1 = 128 - 64 * Math.sin((x + t * 0.73) * 0.2) 
                    - 64 * Math.cos((x - y * 0.001 * t * 0.55) * 0.3);
                
                double v2 = 128 + 64 * Math.cos((y - x * 0.0001 * t * 0.47) * 0.3) 
                    - 64 * Math.sin((y + t * 0.29) * 0.1);
                
                double v6 = (v1 + v2) / 2;
                data[x + y * offscreenHeight.getWidth()] = (int) v6;
            }
        }

        for (int i = 0; i < dataHeight.length; i++) {
            dataHeight[i] = (255 << 24) + data[i] + (data[i] << 8) + (data[i] << 16); 
            dataColor[i] = palette[data[i]];
        }
    }

    public BufferedImage getOffscreenHeight() {
        return offscreenHeight;
    }

    public BufferedImage getOffscreenColor() {
        return offscreenColor;
    }
    
}
