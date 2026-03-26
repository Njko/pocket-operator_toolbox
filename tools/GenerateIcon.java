import java.awt.image.BufferedImage;
import java.awt.Color;
import java.awt.Font;
import java.awt.RenderingHints;
import javax.imageio.ImageIO;
import java.io.File;

public class GenerateIcon {
    public static void main(String[] args) throws Exception {
        var img = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);
        var g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Background
        g.setColor(new Color(0x11, 0x18, 0x27));
        g.fillRoundRect(0, 0, 256, 256, 40, 40);

        // Border
        g.setColor(new Color(0x14, 0xB8, 0xA6));
        g.drawRoundRect(4, 4, 247, 247, 36, 36);
        g.drawRoundRect(5, 5, 245, 245, 34, 34);

        // Text "PO"
        g.setColor(new Color(0x14, 0xB8, 0xA6));
        g.setFont(new Font("SansSerif", Font.BOLD, 100));
        g.drawString("PO", 48, 150);

        // Step dots
        for (int i = 0; i < 16; i++) {
            int x = 20 + i * 14;
            g.setColor(i % 4 == 0 ? new Color(0x14, 0xB8, 0xA6) : new Color(0x4B, 0x55, 0x63));
            g.fillOval(x, 195, 8, 8);
        }

        g.dispose();

        new File("src/main/resources/icons").mkdirs();
        ImageIO.write(img, "png", new File("src/main/resources/icons/icon.png"));
        System.out.println("Created icon.png (256x256)");
    }
}
