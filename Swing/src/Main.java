import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.awt.image.BufferedImage;
import java.io.File;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.util.LinkedList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.*;
import javax.imageio.*;

public class Main {
    public static final int DEFAULT_WINDOW_SIZEX = 700;
    public static final int DEFAULT_WINDOW_SIZEY = 500;

    public static final int toolBarWidth = 200;
    public static final int windowExraHeight = 20;

    public static final int defaultBoxSizeX = 106;
    public static final int defaultBoxSizeY = 90;
    public static int boxOffsetX = 0;
    public static int boxOffsetY = 0;
    public static int currentImageIndex = 0;

    public static BufferedImage[] images;
    public static File currentOpen = new File(System.getProperty("user.home") + "/Desktop");
    public static File currentSave = new File(currentOpen.getPath());
    public static BufferedImage image = new BufferedImage(DEFAULT_WINDOW_SIZEX, DEFAULT_WINDOW_SIZEY, BufferedImage.TYPE_INT_ARGB);

    public static void main(String[] args) {
        int boxSize = 50;
        int padding = 4;
        int buffer = 30;

        JFrame frame = new JFrame("Vias Batch Cropper");
        frame.setResizable(false);
        JButton open = new JButton("Import Directory");
        JButton save = new JButton("Export Destination");
        JButton export = new JButton("Export");

        JButton nextImage = new JButton("Next Image");
        JButton prevImage = new JButton("Prev Image");

        JCheckBox staggeredButton = new JCheckBox("Stagger Bounding Box");
        JCheckBox staggeredOddButton = new JCheckBox("Stagger Odd");
        JCheckBox topCrop = new JCheckBox("Crop From Top", true);
        JSlider imageSelection = new JSlider(1, 10);

        JLabel imageSelectionTextBox = new JLabel("Image Preview: 1");
        JSlider boxSizeXSlider = new JSlider(5, image.getWidth() - 1);
        JLabel boxSizeXSliderTextBox = new JLabel("Box Size X: " + defaultBoxSizeX);
        JTextField boxXTextField = new JTextField(4);
        boxXTextField.setText(defaultBoxSizeX + "");

        JSlider boxSizeYSlider = new JSlider(5, image.getHeight() - 1);
        JLabel boxSizeYSliderTextBox = new JLabel("Box Size Y: " + defaultBoxSizeY);
        JTextField boxYTextField = new JTextField(4);
        boxYTextField.setText(defaultBoxSizeY + "");

        JSlider boxOffsetXSlider = new JSlider(5, image.getHeight() - 1);
        JLabel boxOffsetXSliderTextBox = new JLabel("Box Offset X: " + boxOffsetX);
        JTextField boxOffsetXTextField = new JTextField(4);
        boxOffsetXTextField.setText(boxOffsetX + "");

        JSlider thresholdSlider = new JSlider(0, 255);
        JLabel thresholdSliderTextBox = new JLabel("Detection Threshold: " + buffer);

        ImageIcon imageIcon = new ImageIcon(image);
        JLabel imageLabel = new JLabel(imageIcon);
        JLabel previewLabel = new JLabel();
        JLabel currentFileTextBox = new JLabel("\tCurrent Directory: ");

        Box container1 = new Box(2);
        boxXTextField.setMaximumSize(new Dimension(50, 30));
        container1.add(boxSizeXSlider);
        container1.add(boxXTextField);

        Box container2 = new Box(2);
        boxYTextField.setMaximumSize(new Dimension(50, 30));
        container2.add(boxSizeYSlider);
        container2.add(boxYTextField);

        Box container3 = new Box(2);
        boxOffsetXTextField.setMaximumSize(new Dimension(50, 30));
        container3.add(boxOffsetXSlider);
        container3.add(boxOffsetXTextField);

        boxSizeXSlider.setMajorTickSpacing(1);
        boxSizeYSlider.setMajorTickSpacing(1);
        boxSizeXSlider.setSnapToTicks(true);
        boxSizeYSlider.setSnapToTicks(true);
        boxSizeXSlider.setValue(defaultBoxSizeX);
        boxSizeYSlider.setValue(defaultBoxSizeY);

        boxSizeXSlider.setEnabled(false);
        boxSizeYSlider.setEnabled(false);
        imageSelection.setEnabled(false);
        staggeredButton.setEnabled(false);
        topCrop.setEnabled(false);
        thresholdSlider.setEnabled(false);
        boxXTextField.setEnabled(false);
        boxYTextField.setEnabled(false);
        boxOffsetXTextField.setEnabled(false);
        boxOffsetXSlider.setEnabled(false);
        export.setEnabled(false);
        nextImage.setEnabled(false);
        prevImage.setEnabled(false);

        thresholdSlider.setValue(buffer);
        staggeredOddButton.setEnabled(false);

        imageSelection.setValue(1);

        Box bottomBox = new Box(2);

        Box box = new Box(1);
        box.add(boxSizeXSliderTextBox);
        box.add(container1);
        box.add(boxSizeYSliderTextBox);
        box.add(container2);
        //box.add(boxOffsetXSliderTextBox);
        //box.add(container3);
        box.add(thresholdSliderTextBox);
        box.add(thresholdSlider);
        box.add(staggeredButton);
        box.add(staggeredOddButton);
        box.add(topCrop);
        box.add(imageSelection);
        box.add(imageSelectionTextBox);
        box.add(previewLabel);

        bottomBox.add(open);
        bottomBox.add(save);
        bottomBox.add(export);
        bottomBox.add(currentFileTextBox);
        bottomBox.add(prevImage);
        bottomBox.add(nextImage);

        frame.getContentPane().add(BorderLayout.CENTER, box);
        frame.getContentPane().add(BorderLayout.SOUTH, bottomBox);
        frame.add(BorderLayout.WEST, imageLabel);
        frame.setBounds(0, 0, image.getWidth() + toolBarWidth, image.getHeight() + windowExraHeight);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        BufferedImage imageCopy = deepCopy(image);
        redraw(imageCopy, boxSizeXSlider.getValue(), boxSizeYSlider.getValue(), boxOffsetX, boxOffsetY, staggeredButton.isSelected(), staggeredOddButton.isSelected());
        refresh(imageLabel, imageCopy);

        ImageParser imageParser = new ImageParser(image);
        imageParser.doRun = false;
        imageParser.setOdd(staggeredOddButton.isSelected());
        imageParser.setStaggered(staggeredButton.isSelected());
        imageParser.setBoxSize(boxSizeXSlider.getValue(), boxSizeYSlider.getValue());
        topCrop.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                AutoCrop.doTopCrop = topCrop.isSelected();
                BufferedImage imageCopy = deepCopy(image);
                redraw(imageCopy, boxSizeXSlider.getValue(), boxSizeYSlider.getValue(), boxOffsetX, boxOffsetY, staggeredButton.isSelected(), staggeredOddButton.isSelected());
                refresh(imageLabel, imageCopy);

                imageParser.setOdd(staggeredOddButton.isSelected());
                imageParser.setStaggered(staggeredButton.isSelected());
                imageParser.setBoxSize(boxSizeXSlider.getValue(), boxSizeYSlider.getValue());

                LinkedList<BufferedImage> croppedImages = imageParser.parseImage();
                imageSelection.setMaximum(croppedImages.size());
                imageSelection.setMinimum(1);
                if (imageSelection.getValue() > imageSelection.getMaximum())
                    imageSelection.setValue(imageSelection.getMaximum());
                BufferedImage cropped = AutoCrop.autoCrop(croppedImages.get(imageSelection.getValue() - 1), boxSize, padding, thresholdSlider.getValue());
                setPreview(previewLabel, cropped, 150, 150);
            }
        });
        staggeredOddButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                BufferedImage imageCopy = deepCopy(image);
                redraw(imageCopy, boxSizeXSlider.getValue(), boxSizeYSlider.getValue(), boxOffsetX, boxOffsetY, staggeredButton.isSelected(), staggeredOddButton.isSelected());
                refresh(imageLabel, imageCopy);

                imageParser.setOdd(staggeredOddButton.isSelected());
                imageParser.setStaggered(staggeredButton.isSelected());
                imageParser.setBoxSize(boxSizeXSlider.getValue(), boxSizeYSlider.getValue());

                LinkedList<BufferedImage> croppedImages = imageParser.parseImage();
                imageSelection.setMaximum(croppedImages.size());
                imageSelection.setMinimum(1);
                if (imageSelection.getValue() > imageSelection.getMaximum())
                    imageSelection.setValue(imageSelection.getMaximum());
                BufferedImage cropped = AutoCrop.autoCrop(croppedImages.get(imageSelection.getValue() - 1), boxSize, padding, thresholdSlider.getValue());
                setPreview(previewLabel, cropped, 150, 150);
            }
        });
        staggeredButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                BufferedImage imageCopy = deepCopy(image);
                redraw(imageCopy, boxSizeXSlider.getValue(), boxSizeYSlider.getValue(), boxOffsetX, boxOffsetY, staggeredButton.isSelected(), staggeredOddButton.isSelected());
                refresh(imageLabel, imageCopy);
                staggeredOddButton.setEnabled(staggeredButton.isSelected());
                imageParser.setOdd(staggeredOddButton.isSelected());
                imageParser.setStaggered(staggeredButton.isSelected());
                imageParser.setBoxSize(boxSizeXSlider.getValue(), boxSizeYSlider.getValue());

                LinkedList<BufferedImage> croppedImages = imageParser.parseImage();
                imageSelection.setMaximum(croppedImages.size());
                imageSelection.setMinimum(1);
                if (imageSelection.getValue() > imageSelection.getMaximum())
                    imageSelection.setValue(imageSelection.getMaximum());
                BufferedImage cropped = AutoCrop.autoCrop(croppedImages.get(imageSelection.getValue() - 1), boxSize, padding, thresholdSlider.getValue());
                setPreview(previewLabel, cropped, 150, 150);
            }
        });
        boxXTextField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                BufferedImage imageCopy = deepCopy(image);
                redraw(imageCopy, boxSizeXSlider.getValue(), boxSizeYSlider.getValue(), boxOffsetX, boxOffsetY, staggeredButton.isSelected(), staggeredOddButton.isSelected());
                refresh(imageLabel, imageCopy);

                imageParser.setOdd(staggeredOddButton.isSelected());
                imageParser.setStaggered(staggeredButton.isSelected());
                imageParser.setBoxSize(boxSizeXSlider.getValue(), boxSizeYSlider.getValue());

                int newValue = boxSizeXSlider.getValue();
                try {
                    newValue = Integer.parseInt(boxXTextField.getText());
                } catch (Exception e) {

                }
                if (newValue >= boxSizeXSlider.getMaximum())
                    newValue = boxSizeXSlider.getMaximum() - 1;
                if (newValue < boxSizeXSlider.getMinimum())
                    newValue = boxSizeXSlider.getMinimum();
                boxSizeXSlider.setValue(newValue);
                boxXTextField.setText(newValue + "");
                boxSizeXSliderTextBox.setText("Box Size X: " + boxSizeXSlider.getValue());

                LinkedList<BufferedImage> croppedImages = imageParser.parseImage();
                imageSelection.setMaximum(croppedImages.size());
                imageSelection.setMinimum(1);
                if (imageSelection.getValue() > imageSelection.getMaximum())
                    imageSelection.setValue(imageSelection.getMaximum());
                BufferedImage cropped = AutoCrop.autoCrop(croppedImages.get(imageSelection.getValue() - 1), boxSize, padding, thresholdSlider.getValue());
                setPreview(previewLabel, cropped, 150, 150);
            }
        });
        boxSizeXSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent ce) {
                BufferedImage imageCopy = deepCopy(image);
                redraw(imageCopy, boxSizeXSlider.getValue(), boxSizeYSlider.getValue(), boxOffsetX, boxOffsetY, staggeredButton.isSelected(), staggeredOddButton.isSelected());
                refresh(imageLabel, imageCopy);

                imageParser.setOdd(staggeredOddButton.isSelected());
                imageParser.setStaggered(staggeredButton.isSelected());
                imageParser.setBoxSize(boxSizeXSlider.getValue(), boxSizeYSlider.getValue());

                boxXTextField.setText(boxSizeXSlider.getValue() + "");
                boxSizeXSliderTextBox.setText("Box Size X: " + boxSizeXSlider.getValue());

                LinkedList<BufferedImage> croppedImages = imageParser.parseImage();
                imageSelection.setMaximum(croppedImages.size());
                imageSelection.setMinimum(1);
                if (imageSelection.getValue() > imageSelection.getMaximum())
                    imageSelection.setValue(imageSelection.getMaximum());
                BufferedImage cropped = AutoCrop.autoCrop(croppedImages.get(imageSelection.getValue() - 1), boxSize, padding, thresholdSlider.getValue());
                setPreview(previewLabel, cropped, 150, 150);
            }
        });
        boxYTextField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                BufferedImage imageCopy = deepCopy(image);
                redraw(imageCopy, boxSizeXSlider.getValue(), boxSizeYSlider.getValue(), boxOffsetX, boxOffsetY, staggeredButton.isSelected(), staggeredOddButton.isSelected());
                refresh(imageLabel, imageCopy);

                imageParser.setOdd(staggeredOddButton.isSelected());
                imageParser.setStaggered(staggeredButton.isSelected());
                imageParser.setBoxSize(boxSizeXSlider.getValue(), boxSizeYSlider.getValue());

                int newValue = boxSizeYSlider.getValue();
                try {
                    newValue = Integer.parseInt(boxYTextField.getText());
                } catch (Exception e) {

                }
                if (newValue >= boxSizeYSlider.getMaximum())
                    newValue = boxSizeYSlider.getMaximum() - 1;
                if (newValue < boxSizeYSlider.getMinimum())
                    newValue = boxSizeYSlider.getMinimum();
                boxSizeYSlider.setValue(newValue);
                boxYTextField.setText(newValue + "");
                boxSizeYSliderTextBox.setText("Box Size Y: " + boxSizeYSlider.getValue());

                LinkedList<BufferedImage> croppedImages = imageParser.parseImage();
                imageSelection.setMaximum(croppedImages.size());
                imageSelection.setMinimum(1);
                if (imageSelection.getValue() > imageSelection.getMaximum())
                    imageSelection.setValue(imageSelection.getMaximum());
                imageSelectionTextBox.setText("Image Preview: " + imageSelection.getValue());
                BufferedImage cropped = AutoCrop.autoCrop(croppedImages.get(imageSelection.getValue() - 1), boxSize, padding, thresholdSlider.getValue());
                setPreview(previewLabel, cropped, 150, 150);
            }
        });
        boxSizeYSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent ce) {
                BufferedImage imageCopy = deepCopy(image);
                redraw(imageCopy, boxSizeXSlider.getValue(), boxSizeYSlider.getValue(), boxOffsetX, boxOffsetY, staggeredButton.isSelected(), staggeredOddButton.isSelected());
                refresh(imageLabel, imageCopy);

                imageParser.setOdd(staggeredOddButton.isSelected());
                imageParser.setStaggered(staggeredButton.isSelected());
                imageParser.setBoxSize(boxSizeXSlider.getValue(), boxSizeYSlider.getValue());

                boxYTextField.setText(boxSizeYSlider.getValue() + "");
                boxSizeYSliderTextBox.setText("Box Size Y: " + boxSizeYSlider.getValue());

                LinkedList<BufferedImage> croppedImages = imageParser.parseImage();
                imageSelection.setMaximum(croppedImages.size());
                imageSelection.setMinimum(1);
                if (imageSelection.getValue() > imageSelection.getMaximum())
                    imageSelection.setValue(imageSelection.getMaximum());
                BufferedImage cropped = AutoCrop.autoCrop(croppedImages.get(imageSelection.getValue() - 1), boxSize, padding, thresholdSlider.getValue());
                setPreview(previewLabel, cropped, 150, 150);
            }
        });
        imageSelection.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent ce) {
                imageParser.setOdd(staggeredOddButton.isSelected());
                imageParser.setStaggered(staggeredButton.isSelected());
                imageParser.setBoxSize(boxSizeXSlider.getValue(), boxSizeYSlider.getValue());

                LinkedList<BufferedImage> croppedImages = imageParser.parseImage();
                imageSelection.setMaximum(croppedImages.size());
                imageSelection.setMinimum(1);
                imageSelectionTextBox.setText("Image Preview: " + imageSelection.getValue());
                BufferedImage cropped = AutoCrop.autoCrop(croppedImages.get(imageSelection.getValue() - 1), boxSize, padding, thresholdSlider.getValue());
                setPreview(previewLabel, cropped, 150, 150);
            }
        });
        thresholdSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent ce) {
                imageParser.setOdd(staggeredOddButton.isSelected());
                imageParser.setStaggered(staggeredButton.isSelected());
                imageParser.setBoxSize(boxSizeXSlider.getValue(), boxSizeYSlider.getValue());

                LinkedList<BufferedImage> croppedImages = imageParser.parseImage();
                thresholdSliderTextBox.setText("Detection Threshold: " + thresholdSlider.getValue());
                BufferedImage cropped = AutoCrop.autoCrop(croppedImages.get(imageSelection.getValue() - 1), boxSize, padding, thresholdSlider.getValue());
                setPreview(previewLabel, cropped, 150, 150);
            }
        });
        open.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser c = new JFileChooser();
                c.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                c.setCurrentDirectory(currentOpen);
                int rVal = c.showOpenDialog(frame);
                if (rVal == JFileChooser.APPROVE_OPTION) {
                    LinkedList<BufferedImage> currentImages = new LinkedList<>();
                    for(File file : c.getSelectedFile().listFiles())
                    {
                        if(ImageParser.isImage(file)) {
                            try {
                                BufferedImage temp = ImageIO.read(file);
                                currentImages.add(temp);
                            }catch (Exception ee){
                                //ee.printStackTrace();
                            }
                        }
                    }
                    images = new BufferedImage[currentImages.size()];
                    int index = 0;
                    for(BufferedImage img : currentImages)
                    {
                        images[index] = img;
                        index++;
                    }
                    try {
                        Main.currentImageIndex = 0;
                        Main.image = images[currentImageIndex];
                        boxSizeXSlider.setEnabled(true);
                        boxSizeYSlider.setEnabled(true);
                        imageSelection.setEnabled(true);
                        staggeredButton.setEnabled(true);
                        thresholdSlider.setEnabled(true);
                        boxXTextField.setEnabled(true);
                        boxYTextField.setEnabled(true);
                        boxOffsetXTextField.setEnabled(true);
                        boxOffsetXSlider.setEnabled(true);
                        nextImage.setEnabled(true);
                        prevImage.setEnabled(true);
                        topCrop.setEnabled(true);
                        boxSizeXSlider.setMaximum(image.getWidth() - 1);
                        boxSizeYSlider.setMaximum(image.getHeight() - 1);
                        boxOffsetXSlider.setMaximum(image.getWidth() - 1);
                        imageParser.doRun = true;
                    } catch (Exception _e) {
                        //_e.printStackTrace();
                    }
                    Main.currentOpen = new File(c.getSelectedFile().getPath());
                    currentFileTextBox.setText("\tCurrent Directory: " + c.getSelectedFile().getName());
                    BufferedImage imageCopy = deepCopy(image);
                    redraw(imageCopy, boxSizeXSlider.getValue(), boxSizeYSlider.getValue(), boxOffsetX, boxOffsetY, staggeredButton.isSelected(), staggeredOddButton.isSelected());
                    refresh(imageLabel, imageCopy);

                    imageParser.setOdd(staggeredOddButton.isSelected());
                    imageParser.setStaggered(staggeredButton.isSelected());
                    imageParser.setBoxSize(boxSizeXSlider.getValue(), boxSizeYSlider.getValue());

                    boxSizeXSliderTextBox.setText("Box Size X: " + boxSizeXSlider.getValue());

                    imageParser.setImage(Main.image);
                    LinkedList<BufferedImage> croppedImages = imageParser.parseImage();
                    imageSelection.setMaximum(croppedImages.size());
                    imageSelection.setMinimum(1);
                    if (imageSelection.getValue() > imageSelection.getMaximum())
                        imageSelection.setValue(imageSelection.getMaximum());
                    BufferedImage cropped = AutoCrop.autoCrop(croppedImages.get(imageSelection.getValue() - 1), boxSize, padding, thresholdSlider.getValue());
                    setPreview(previewLabel, cropped, 150, 150);
                }
                if (rVal == JFileChooser.CANCEL_OPTION) {

                }
            }
        });
        save.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser c = new JFileChooser();
                c.setCurrentDirectory(currentSave);
                c.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int rVal = c.showOpenDialog(frame);
                if (rVal == JFileChooser.APPROVE_OPTION) {
                    Main.currentSave = new File(c.getSelectedFile().getPath());
                    export.setEnabled(true);
                }
                if (rVal == JFileChooser.CANCEL_OPTION) {

                }
            }
        });
        export.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                LinkedList<BufferedImage> croppedImages = imageParser.parseImage();
                int index = 0;
                File tempFile = new File(Main.currentSave.getPath() + "/Image" + index + ".png");
                boolean exists = tempFile.exists();
                while (exists) {
                    index++;
                    tempFile = new File(Main.currentSave.getPath()+ "/Image" + index + ".png");
                    exists = tempFile.exists();
                }
                System.out.println(Main.currentSave.getPath() + "/Image" + index + ".png");
                for (BufferedImage image : croppedImages) {
                    BufferedImage cropped = AutoCrop.autoCrop(image, boxSize, padding, thresholdSlider.getValue());
                    ImageParser.saveImage(cropped, Main.currentSave.getPath() + "/", "Image" + index + ".png");
                    index++;
                }
            }
        });
        nextImage.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(currentImageIndex < images.length - 1)
                {
                    currentImageIndex++;
                }
                image = images[currentImageIndex];
                BufferedImage imageCopy = deepCopy(image);
                redraw(imageCopy, boxSizeXSlider.getValue(), boxSizeYSlider.getValue(), boxOffsetX, boxOffsetY, staggeredButton.isSelected(), staggeredOddButton.isSelected());
                refresh(imageLabel, imageCopy);

                imageParser.setOdd(staggeredOddButton.isSelected());
                imageParser.setStaggered(staggeredButton.isSelected());
                imageParser.setBoxSize(boxSizeXSlider.getValue(), boxSizeYSlider.getValue());

                boxSizeXSliderTextBox.setText("Box Size X: " + boxSizeXSlider.getValue());

                imageParser.setImage(Main.image);
                LinkedList<BufferedImage> croppedImages = imageParser.parseImage();
                imageSelection.setMaximum(croppedImages.size());
                imageSelection.setMinimum(1);
                if (imageSelection.getValue() > imageSelection.getMaximum())
                    imageSelection.setValue(imageSelection.getMaximum());
                BufferedImage cropped = AutoCrop.autoCrop(croppedImages.get(imageSelection.getValue() - 1), boxSize, padding, thresholdSlider.getValue());
                setPreview(previewLabel, cropped, 150, 150);
            }
        });
        prevImage.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(currentImageIndex > 1)
                {
                    currentImageIndex--;
                }
                image = images[currentImageIndex];
                BufferedImage imageCopy = deepCopy(image);
                redraw(imageCopy, boxSizeXSlider.getValue(), boxSizeYSlider.getValue(), boxOffsetX, boxOffsetY, staggeredButton.isSelected(), staggeredOddButton.isSelected());
                refresh(imageLabel, imageCopy);

                imageParser.setOdd(staggeredOddButton.isSelected());
                imageParser.setStaggered(staggeredButton.isSelected());
                imageParser.setBoxSize(boxSizeXSlider.getValue(), boxSizeYSlider.getValue());

                boxSizeXSliderTextBox.setText("Box Size X: " + boxSizeXSlider.getValue());

                imageParser.setImage(Main.image);
                LinkedList<BufferedImage> croppedImages = imageParser.parseImage();
                imageSelection.setMaximum(croppedImages.size());
                imageSelection.setMinimum(1);
                if (imageSelection.getValue() > imageSelection.getMaximum())
                    imageSelection.setValue(imageSelection.getMaximum());
                BufferedImage cropped = AutoCrop.autoCrop(croppedImages.get(imageSelection.getValue() - 1), boxSize, padding, thresholdSlider.getValue());
                setPreview(previewLabel, cropped, 150, 150);
            }
        });
        frame.setVisible(true);
    }

    public static void setPreview(JLabel imageLabel, BufferedImage image, int sizeX, int sizeY) {
        BufferedImage scaledImage = resize(image, sizeX, sizeY);
        ImageIcon imageIcon = new ImageIcon(scaledImage);
        imageLabel.setIcon(imageIcon);
    }

    public static void refresh(JLabel imageLabel, BufferedImage img) {
        ImageIcon imageIcon = new ImageIcon(img);
        imageLabel.setIcon(imageIcon);
    }

    public static BufferedImage redraw(BufferedImage image, int sizeX, int sizeY, int offsetX, int offsetY, boolean staggered, boolean oddStaggered) {
        Color red = new Color(165, 50, 50);
        int boxSizeX = sizeX;
        int boxSizeY = sizeY;
        int xDivide = 0;
        for (int x = 0; x < image.getWidth(); x++) {
            if ((x - offsetX) % boxSizeX == 0)
                xDivide++;
            int adjustedOffsetY = offsetY;
            if (staggered && (xDivide % 2 == 0) == oddStaggered) {
                adjustedOffsetY = offsetY + boxSizeY / 2;
            }
            for (int y = 0; y < image.getHeight(); y++) {
                if ((x - offsetX) % boxSizeX == 0 || (y - adjustedOffsetY) % boxSizeY == 0)
                    image.setRGB(x, y, red.getRGB());
            }
        }
        return image;
    }

    static BufferedImage deepCopy(BufferedImage bi) {
        ColorModel cm = bi.getColorModel();
        boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        WritableRaster raster = bi.copyData(null);
        return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
    }

    public static BufferedImage resize(BufferedImage img, int newW, int newH) {
        Image tmp = img.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
        BufferedImage dimg = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = dimg.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();

        return dimg;
    }
}