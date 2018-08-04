
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class BMPViewer extends javax.swing.JDialog {

    // Constructor for the viewer class.
    public BMPViewer(java.awt.Frame parent, boolean modal, byte[] bytes, String fileName) {
        super(parent, modal);
        imageBytes = bytes.clone();
        initComponents();
        setTitle(String.format("BMP Operations on \"%s\"", fileName));
        setupImages();
        getPrev();
        renderPanel(getNext());
    }

    // Convert 24 bit image to BufferedImage, and (optionally) fill-in a histogram RGB.
    private BufferedImage createImageFromBytes(byte[] imageData, int[][] histogramRGB) {
        int bitmapOffset = getInt(imageData[13], imageData[12], imageData[11], imageData[10]);
        int width = getInt(imageData[21], imageData[20], imageData[19], imageData[18]);
        int height = getInt(imageData[25], imageData[24], imageData[23], imageData[22]);

        int colorsUsed = getInt(imageData[49], imageData[48], imageData[47], imageData[46]);
        short bitsPerPixel = getShort(imageData[29], imageData[28]);

        if (colorsUsed == 0) {
            if (bitsPerPixel < 16) {
                colorsUsed = 1 << bitsPerPixel;
            } else {
                colorsUsed = 0;
            }
        }

        // This is where color palets and data starts.
        int current = 50;

        // Collect the color pallet.
        byte[] r, g, b;
        if (colorsUsed > 0) {
            r = new byte[colorsUsed];
            g = new byte[colorsUsed];
            b = new byte[colorsUsed];

            for (int i = 0; i < colorsUsed; i++) {
                b[i] = imageData[current];
                current++;
                g[i] = imageData[current];
                current++;
                r[i] = imageData[current];
                current += 2;
            }
        }

        if (histogramRGB != null) {
            Arrays.fill(histogramRGB[0], 0);
            Arrays.fill(histogramRGB[1], 0);
            Arrays.fill(histogramRGB[2], 0);
        }

        BufferedImage toReturnImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics toReturnG = toReturnImage.getGraphics();

        int len = ((width * bitsPerPixel + 31) / 32) * 4;
        long skip = bitmapOffset - current;
        current += skip;

        // Draw the actual image, and collect the RGB histogram while iterating.
        int rawOffset = 0;
        byte[] rawData = new byte[len * height];
        for (int i = height - 1; i >= 0; i--) {

            // Collect the data per width.
            for (int j = rawOffset; j < (rawOffset + len) && current < imageData.length; j++) {
                rawData[j] = imageData[current];
                current++;
            }

            int k = rawOffset;
            int mask = 0xff;

            // Collect the data per pixle (pixle == 3 bytes) and draw the pixle
            // (as well as add the RGB histogram data).
            for (int l = 0; l < width; l++) {
                int b0 = (((int) (rawData[k++])) & mask);
                int b1 = (((int) (rawData[k++])) & mask) << 8;
                int b2 = (((int) (rawData[k++])) & mask) << 16;
                int rgbVal = 0xff000000 | b0 | b1 | b2;
                Color c = new Color(rgbVal);
                toReturnG.setColor(c);
                toReturnG.drawLine(l, i, l, i);
                if (histogramRGB != null) {
                    histogramRGB[0][c.getRed()]++;
                    histogramRGB[1][c.getGreen()]++;
                    histogramRGB[2][c.getBlue()]++;
                }
            }
            rawOffset += len;
        }

        toReturnG.dispose();
        return toReturnImage;
    }

    private int getInt(byte b1, byte b2, byte b3, byte b4) {
        return ((0xFF & b1) << 24) | ((0xFF & b2) << 16)
                | ((0xFF & b3) << 8) | (0xFF & b4);
    }

    private short getShort(byte b1, byte b2) {
        return (short) ((b1 << 8) + b2);
    }

    private void renderPanel(ImageItem img) {
        ((DrawPanel) drawPanel).setImage(img.getImg());
        jLabel1.setText(img.getDescription());
    }

    // Cache all panels to buffered images to draw.
    private void setupImages() {
        pages = new LinkedList<>();
        pages.add(getOriginalImage());
        pages.add(getHistogramRGB(0));
        pages.add(getHistogramRGB(1));
        pages.add(getHistogramRGB(2));
        pages.add(getBrighterImage());
        pages.add(getGrayScaleImage());
        pages.add(getDitheredImage());
    }

    private ImageItem getOriginalImage() {
        return new ImageItem(createImageFromBytes(imageBytes, null), "Original Image", null);
    }

    // 0 = r. 1 = g. 2 = b.
    private ImageItem getHistogramRGB(int rgb) {
        BufferedImage img = new BufferedImage(drawPanel.getWidth(), drawPanel.getHeight(), BufferedImage.TYPE_INT_RGB);
        int[][] histogramRGB = new int[3][256];
        createImageFromBytes(imageBytes, histogramRGB);
        Graphics g = img.createGraphics();

        g.setColor(Color.WHITE);
        g.fillRect(0, 0, img.getWidth(), img.getHeight());

        // Collect the maximum value in each R, G, and B histograms,
        // to be used as the Y value max to compare to.
        int maximumRed = 0;
        int maximumGreen = 0;
        int maximumBlue = 0;
        boolean isMaximumRedSet = false;
        boolean isMaximumGreenSet = false;
        boolean isMaximumBlueSet = false;
        for (int i = 0; i < histogramRGB[0].length; i++) {
            if (!isMaximumRedSet) {
                maximumRed = histogramRGB[0][i];
                isMaximumRedSet = true;
            } else if (histogramRGB[0][i] > maximumRed) {
                maximumRed = histogramRGB[0][i];
            }
            if (!isMaximumGreenSet) {
                maximumGreen = histogramRGB[1][i];
                isMaximumGreenSet = true;
            } else if (histogramRGB[1][i] > maximumGreen) {
                maximumGreen = histogramRGB[1][i];
            }
            if (!isMaximumBlueSet) {
                maximumBlue = histogramRGB[2][i];
                isMaximumBlueSet = true;
            } else if (histogramRGB[2][i] > maximumBlue) {
                maximumBlue = histogramRGB[2][i];
            }
        }

        // Draw the histograms.
        for (int i = 0; i < histogramRGB[0].length; i++) {
            float redFrequency = histogramRGB[0][i];
            float greenFrequency = histogramRGB[1][i];
            float blueFrequency = histogramRGB[2][i];

            float currentX = (i * ((float) img.getWidth() / (float) histogramRGB[0].length));
            int ignoreValue = 100; // Ignore some pixles as they do not appear on the draw panel.
            int availableHeight = img.getHeight() - ignoreValue;
            int barHeight;
            int startY;

            switch (rgb) {
                case 0:
                    barHeight = (int) (availableHeight * (redFrequency / maximumRed));
                    startY = availableHeight - barHeight;
                    g.setColor(Color.RED);
                    g.drawLine((int) currentX, startY + ignoreValue, (int) currentX, availableHeight + ignoreValue);
                    break;
                case 1:
                    barHeight = (int) (availableHeight * (greenFrequency / maximumGreen));
                    startY = availableHeight - barHeight;
                    g.setColor(Color.GREEN);
                    g.drawLine((int) currentX, startY + ignoreValue, (int) currentX, availableHeight + ignoreValue);
                    break;
                default:
                    barHeight = (int) (availableHeight * (blueFrequency / maximumBlue));
                    startY = availableHeight - barHeight;
                    g.setColor(Color.BLUE);
                    g.drawLine((int) currentX, startY + ignoreValue, (int) currentX, availableHeight + ignoreValue);
            }
        }
        g.dispose();
        switch (rgb) {
            case 0:
                return new ImageItem(img, "Histogram R", new TwoDimentionalGraphData(0, 0, 255, maximumRed, "Color Value", "Color Intensity"));
            case 1:
                return new ImageItem(img, "Histogram G", new TwoDimentionalGraphData(0, 0, 255, maximumGreen, "Color Value", "Color Intensity"));
            default:
                return new ImageItem(img, "Histogram B", new TwoDimentionalGraphData(0, 0, 255, maximumBlue, "Color Value", "Color Intensity"));
        }
    }

    private ImageItem getBrighterImage() {
        BufferedImage src = createImageFromBytes(imageBytes, null);
        BufferedImage dst = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics dstG = dst.getGraphics();
        for (int i = 0; i < src.getHeight(); i++) {
            for (int j = 0; j < src.getWidth(); j++) {
                Color srsC = new Color(src.getRGB(j, i));
                float[] dstHSB = RGBtoHSB(srsC.getRed(), srsC.getGreen(), srsC.getBlue(), null);
                dstHSB[2] = dstHSB[2] * 1.5f;

                // Check if they do not exceed the maximum value.
                if (dstHSB[2] > 1) {
                    dstHSB[2] = 1;
                }
                Color dstC = new Color(HSBtoRGB(dstHSB[0], dstHSB[1], dstHSB[2]));
                dstG.setColor(dstC);
                dstG.drawLine(j, i, j, i);
            }
        }
        dstG.dispose();
        return new ImageItem(dst, "Brighter (by Factor 1.5) Image", null);
    }

    public int HSBtoRGB(float hue, float saturation, float brightness) {
        int r = 0, g = 0, b = 0;
        if (saturation != 0) {
            int val = (int) ((hue - (float) Math.floor(hue)) * 6.0f);
            if (val == 5) {
                r = (int) (brightness * 255.0f + 0.5f);
                g = (int) ((brightness * (1.0f - saturation)) * 255.0f + 0.5f);
                b = (int) ((brightness * (1.0f - saturation * ((((hue - (float) Math.floor(hue)) * 6.0f))
                        - (float) java.lang.Math.floor(((hue - (float) Math.floor(hue)) * 6.0f))))) * 255.0f + 0.5f);
            } else if (val == 4) {
                r = (int) ((brightness * (1.0f - (saturation * (1.0f - ((((hue - (float) Math.floor(hue)) * 6.0f))
                        - (float) java.lang.Math.floor(((hue - (float) Math.floor(hue)) * 6.0f))))))) * 255.0f + 0.5f);
                g = (int) ((brightness * (1.0f - saturation)) * 255.0f + 0.5f);
                b = (int) (brightness * 255.0f + 0.5f);
            } else if (val == 3) {
                r = (int) ((brightness * (1.0f - saturation)) * 255.0f + 0.5f);
                g = (int) ((brightness * (1.0f - saturation * ((((hue - (float) Math.floor(hue)) * 6.0f))
                        - (float) java.lang.Math.floor(((hue - (float) Math.floor(hue)) * 6.0f))))) * 255.0f + 0.5f);
                b = (int) (brightness * 255.0f + 0.5f);
            } else if (val == 2) {
                r = (int) ((brightness * (1.0f - saturation)) * 255.0f + 0.5f);
                g = (int) (brightness * 255.0f + 0.5f);
                b = (int) ((brightness * (1.0f - (saturation * (1.0f - ((((hue - (float) Math.floor(hue)) * 6.0f))
                        - (float) java.lang.Math.floor(((hue - (float) Math.floor(hue)) * 6.0f))))))) * 255.0f + 0.5f);
            } else if (val == 1) {
                r = (int) ((brightness * (1.0f - saturation * ((((hue - (float) Math.floor(hue)) * 6.0f))
                        - (float) java.lang.Math.floor(((hue - (float) Math.floor(hue)) * 6.0f))))) * 255.0f + 0.5f);
                g = (int) (brightness * 255.0f + 0.5f);
                b = (int) ((brightness * (1.0f - saturation)) * 255.0f + 0.5f);
            } else if (val == 0) {
                r = (int) (brightness * 255.0f + 0.5f);
                g = (int) ((brightness * (1.0f - (saturation * (1.0f - ((((hue - (float) Math.floor(hue)) * 6.0f))
                        - (float) java.lang.Math.floor(((hue - (float) Math.floor(hue)) * 6.0f))))))) * 255.0f + 0.5f);
                b = (int) ((brightness * (1.0f - saturation)) * 255.0f + 0.5f);
            }
            return 0xff000000 | (r << 16) | (g << 8) | (b);
        }
        r = (int) (brightness * 255.0f + 0.5f);
        g = (int) (brightness * 255.0f + 0.5f);
        b = (int) (brightness * 255.0f + 0.5f);
        return 0xff000000 | (r << 16) | (g << 8) | (b);
    }

    public static float[] RGBtoHSB(int r, int g, int b, float[] hsbvals) {
        float hue = 0, saturation = 0;

        int cmax = (r > g) ? r : g;
        cmax = b > cmax ? b : cmax;

        int cmin = (r < g) ? r : g;
        cmin = b < cmin ? b : cmin;
        saturation = cmax == 0 ? saturation : ((float) (cmax - cmin)) / ((float) cmax);

        if (saturation > 0 || saturation < 0) {
            if (r == cmax) {
                hue = ((float) (cmax - b))
                        / ((float) (cmax - cmin)) - ((float) (cmax - g)) / ((float) (cmax - cmin));
            } else if (g == cmax) {
                hue = 2.0f + ((float) (cmax - r))
                        / ((float) (cmax - cmin)) - ((float) (cmax - b)) / ((float) (cmax - cmin));
            } else {
                hue = 4.0f + ((float) (cmax - g))
                        / ((float) (cmax - cmin)) - ((float) (cmax - r)) / ((float) (cmax - cmin));
            }
            hue /= 6.0f;
            if (hue < 0) {
                hue += 1.0f;
            }
        }

        hsbvals = hsbvals != null ? hsbvals : new float[3];
        if (hsbvals != null) {
            hsbvals[0] = hue;
            hsbvals[1] = saturation;
            hsbvals[2] = ((float) cmax) / 255.0f;
        }
        return hsbvals;
    }

    private BufferedImage getGrayScaleBufferedImage() {
        BufferedImage toReturn = createImageFromBytes(imageBytes, null);
        for (int i = 0; i < toReturn.getHeight(); i++) {
            for (int j = 0; j < toReturn.getWidth(); j++) {
                int p = toReturn.getRGB(j, i);

                // Get respective data.
                int a = (p >> 24) & 0xff;
                int r = (p >> 16) & 0xff;
                int g = (p >> 8) & 0xff;
                int b = p & 0xff;

                p = (a << 24) | (((r + g + b) / 3) << 16) | (((r + g + b) / 3) << 8) | ((r + g + b) / 3);

                toReturn.setRGB(j, i, p);
            }
        }
        return toReturn;
    }

    private ImageItem getGrayScaleImage() {
        return new ImageItem(getGrayScaleBufferedImage(), "Grayscale Image", null);
    }

    // Get dithered image of the gray scale image.
    private ImageItem getDitheredImage() {
        BufferedImage initialImage = getGrayScaleBufferedImage();
        BufferedImage newImage = new BufferedImage(initialImage.getWidth(), initialImage.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics newImageGraphics = newImage.getGraphics();
        newImageGraphics.setColor(Color.WHITE);
        newImageGraphics.fillRect(0, 0, newImage.getWidth(), newImage.getHeight());
        
        // This one works best!
        int[][] ditherMatrix = new int[][]{
            new int[]{0, 7, 3},
            new int[]{6, 5, 2},
            new int[]{4, 1, 8}
        };

        // Apply dithering.
        for (int i = 0; i < initialImage.getWidth(); i++) {
            for (int j = 0; j < initialImage.getHeight(); j++) {
                int x = i % ditherMatrix.length;
                int y = j % ditherMatrix[0].length;

                Color tempColor = new Color(initialImage.getRGB(i, j));
                float[] hsbTempColor = RGBtoHSB(tempColor.getRed(), tempColor.getGreen(), tempColor.getBlue(), null);

                // Compare with the pixle's brightness.
                int brightness = (int) (hsbTempColor[2] * 10);
                if (brightness < ditherMatrix[x][y]) {
                    newImageGraphics.setColor(Color.BLACK);
                    newImageGraphics.drawLine(i, j, i, j);
                }
            }
        }

        newImageGraphics.dispose();
        return new ImageItem(newImage, "Dithered Image", null);
    }

    private ImageItem getNext() {
        ImageItem toReturn = ((ImageItem) ((LinkedList) pages).removeFirst());
        ((LinkedList) pages).addLast(toReturn);
        return (ImageItem) ((LinkedList) pages).getFirst();
    }

    private ImageItem getPrev() {
        ImageItem toAdd = ((ImageItem) ((LinkedList) pages).removeLast());
        ((LinkedList) pages).addFirst(toAdd);
        return (ImageItem) ((LinkedList) pages).getFirst();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        nextButton = new javax.swing.JButton();
        closeButton = new javax.swing.JButton();
        canvas1 = new java.awt.Canvas();
        drawPanel = new DrawPanel();
        jLabel1 = new javax.swing.JLabel();
        prevButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        nextButton.setText("Next");
        nextButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nextButtonActionPerformed(evt);
            }
        });

        closeButton.setText("Close");
        closeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout drawPanelLayout = new javax.swing.GroupLayout(drawPanel);
        drawPanel.setLayout(drawPanelLayout);
        drawPanelLayout.setHorizontalGroup(
            drawPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 625, Short.MAX_VALUE)
        );
        drawPanelLayout.setVerticalGroup(
            drawPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 605, Short.MAX_VALUE)
        );

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel1.setText("[Description]");

        prevButton.setText("Prev");
        prevButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                prevButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(84, 84, 84)
                        .addComponent(drawPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(nextButton)
                            .addComponent(prevButton)
                            .addComponent(closeButton)))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(71, 71, 71)
                        .addComponent(canvas1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel1)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(50, 50, 50)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(nextButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(prevButton)
                        .addGap(485, 485, 485)
                        .addComponent(closeButton))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addGap(18, 18, 18)
                        .addComponent(drawPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(canvas1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(64, 64, 64))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void nextButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nextButtonActionPerformed
        renderPanel(getNext());
    }//GEN-LAST:event_nextButtonActionPerformed

    private void closeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeButtonActionPerformed
        dispose();
    }//GEN-LAST:event_closeButtonActionPerformed

    private void prevButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_prevButtonActionPerformed
        renderPanel(getPrev());
    }//GEN-LAST:event_prevButtonActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(BMPViewer.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(() -> {
            BMPViewer dialog = new BMPViewer(new javax.swing.JFrame(), true, null, null);
            dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent e) {
                    System.exit(0);
                }
            });
            dialog.setVisible(true);
        });
    }

    // Used to store the next item to draw.
    private static class ImageItem {

        private final BufferedImage img;
        private final String description;
        private final TwoDimentionalGraphData twoDimentionalGraphData;

        public ImageItem(BufferedImage img, String description, TwoDimentionalGraphData twoDimentionalGraphData) {
            this.img = img;
            this.description = description;
            this.twoDimentionalGraphData = twoDimentionalGraphData;
        }

        public String getDescription() {
            return description;
        }

        public BufferedImage getImg() {
            return img;
        }

        public TwoDimentionalGraphData getTwoDimentionalGraphData() {
            return twoDimentionalGraphData;
        }
    }

    // Used to store 2D graph data.
    private static class TwoDimentionalGraphData {

        private final float xMin;
        private final float yMin;
        private final float xMax;
        private final float yMax;
        private final String xLabel;
        private final String yLabel;

        public TwoDimentionalGraphData(float xMin, float yMin, float xMax, float yMax, String xLabel, String yLabel) {
            this.xMin = xMin;
            this.yMin = xMin;
            this.xMax = xMin;
            this.yMax = xMin;
            this.xLabel = xLabel;
            this.yLabel = yLabel;
        }

        public float getXMin() {
            return xMin;
        }

        public float getYMin() {
            return yMin;
        }

        public float getXMax() {
            return xMax;
        }

        public float getYMax() {
            return yMax;
        }

        public String getXLabel() {
            return xLabel;
        }

        public String getYLabel() {
            return yLabel;
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private java.awt.Canvas canvas1;
    private javax.swing.JButton closeButton;
    public javax.swing.JPanel drawPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JButton nextButton;
    private javax.swing.JButton prevButton;
    // End of variables declaration//GEN-END:variables
    private List<ImageItem> pages;
    private final byte[] imageBytes;
}
