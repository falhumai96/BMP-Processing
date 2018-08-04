import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;

public class DrawPanel extends JPanel {

    BufferedImage nextImage = null;

    public DrawPanel() {
    }

    @Override
    public void paint(Graphics g) {
        super.paintComponent(g);
        if (nextImage == null) {
            return;
        }

        for (int i = 0; i < nextImage.getWidth(); i++) {
            for (int j = 0; j < nextImage.getHeight(); j++) {
                g.setColor(new Color(nextImage.getRGB(i, j)));
                g.drawLine(i, j, i, j);
            }
        }
    }

    public void setImage(BufferedImage img) {
        this.nextImage = img;
        repaint();
    }
}
