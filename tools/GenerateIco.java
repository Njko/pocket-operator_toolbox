import javax.imageio.ImageIO;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class GenerateIco {
    public static void main(String[] args) throws Exception {
        // Read the PNG file
        var pngFile = new File("src/main/resources/icons/icon.png");
        var pngBytes = pngFile.toPath().toUri().toURL().openStream().readAllBytes();
        var img = ImageIO.read(pngFile);
        int w = img.getWidth();
        int h = img.getHeight();

        // ICO file format: ICONDIR + ICONDIRENTRY + PNG data
        var out = new ByteArrayOutputStream();
        var buf = ByteBuffer.allocate(22).order(ByteOrder.LITTLE_ENDIAN);

        // ICONDIR header (6 bytes)
        buf.putShort((short) 0);        // reserved
        buf.putShort((short) 1);        // type: 1 = ICO
        buf.putShort((short) 1);        // count: 1 image

        // ICONDIRENTRY (16 bytes)
        buf.put((byte) (w >= 256 ? 0 : w));   // width (0 = 256)
        buf.put((byte) (h >= 256 ? 0 : h));   // height
        buf.put((byte) 0);              // color palette
        buf.put((byte) 0);              // reserved
        buf.putShort((short) 1);        // color planes
        buf.putShort((short) 32);       // bits per pixel
        buf.putInt(pngBytes.length);    // image data size
        buf.putInt(22);                 // offset to image data

        out.write(buf.array());
        out.write(pngBytes);

        var icoFile = new File("src/main/resources/icons/icon.ico");
        try (var fos = new FileOutputStream(icoFile)) {
            fos.write(out.toByteArray());
        }
        System.out.println("Created icon.ico (" + icoFile.length() + " bytes)");
    }
}
