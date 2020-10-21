import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;

public class ImageParser {
    public static final int defaultSize = 20;
    public static final int defaultX = 0;
    public static final int defaultY = 0;
    BufferedImage image;
    String fileLocation;
    String outputDirectory;
    int startX, startY;
    int boxSizeX;
    int boxSizeY;
    int idNumber;
    int numCreated;
    boolean doRun;
    boolean odd;
    boolean isStaggered;
    private double rads;

    public ImageParser(BufferedImage img)
    {
        init(img, "");
    }
    public ImageParser(String fileLocation, String outputDirectory)
    {
        File imageFile = new File(fileLocation);
        BufferedImage image = null;
        try {
            image = ImageIO.read(imageFile);
        }catch(Exception e){
            System.out.println("[ERROR] Unable to load image");
            System.out.println(e.getMessage());
        }
        this.image = image;
        init(image, outputDirectory);
    }
    public void init(BufferedImage img, String output)
    {
        this.doRun = true;
        this.idNumber = 0;
        this.numCreated = 0;
        this.odd = true;
        this.fileLocation = fileLocation;
        this.outputDirectory = output + "/";
        File directory = new File(outputDirectory);
        if (! directory.exists()){
            directory.mkdir();
        }
        this.startX = defaultX;
        this.startY = defaultY;
        this.boxSizeX = defaultSize;
        this.boxSizeY = defaultSize;
        this.image = img;
    }
    public void setStartLocation(int x, int y)
    {
        this.startX = x;
        this.startY = y;
    }
    public void setStaggered(boolean aValue){
        this.isStaggered = aValue;
    }
    public void setImage(BufferedImage img)
    {
        this.image = img;
    }
    public void setOdd(boolean aValue)
    {
        this.odd = aValue;
    }
    public void setBoxSize(int sizeX, int sizeY)
    {
        this.boxSizeX = sizeX;
        this.boxSizeY = sizeY;
    }
    public int getSizePixels()
    {
        return image.getHeight() * image.getWidth();
    }
    public int getNumCreated()
    {
        return this.numCreated;
    }
    public void setIdNumber(int aNumber)
    {
        this.idNumber = aNumber;
    }
    public LinkedList<BufferedImage> parseImage()
    {
        int grayScale = AutoCrop.getBaseLineColor(image);
        int r = (int) (grayScale);
        int g = (int) (grayScale);
        int b = (int) (grayScale);
        Color bgColor = new Color(r, g, b);

        LinkedList<BufferedImage> images = new LinkedList<>();
        int boxArea = boxSizeX*boxSizeY;
        int imageArea = getSizePixels();
        int numImages = imageArea / boxArea;
        int k = this.idNumber;
        for(int x=0; x+boxSizeX<image.getWidth(); x+=boxSizeX)
        {
            int offsetX = this.startX;
            if(x+boxSizeX + offsetX > image.getWidth())
                break;
            for(int y=0; y+boxSizeY<image.getHeight(); y+=boxSizeY)
            {
                int offsetY = this.startY;
                if(isStaggered) {
                    if ((((x+offsetX) / boxSizeX) % 2 == 1) == this.odd) {
                        offsetY = this.startY + boxSizeY / 2;
                    }
                }
                if(y+boxSizeY+offsetY > image.getHeight())
                    break;
                BufferedImage img = new BufferedImage(boxSizeX, boxSizeY, BufferedImage.TYPE_INT_ARGB);
                for(int x2=0; x2<boxSizeX; x2++)
                {
                    for(int y2=0; y2<boxSizeY; y2++)
                    {
                        int rgb = image.getRGB(x+x2+offsetX, y+y2+offsetY);
                        boolean transparent = AutoCrop.isTransparent(x+x2+offsetX, y+y2+offsetY, image);
                        if(transparent){
                            img.setRGB(x2, y2, bgColor.getRGB());
                        }else{
                            img.setRGB(x2, y2, rgb);
                        }
                    }
                }
                k++;
                numCreated = k;
                images.add(img);
                //saveImage(img, this.outputDirectory, "testImage" + k + ".png");
            }
        }
        return images;
    }
    public static void saveImage(BufferedImage anImage, String location, String fileName)
    {
        try {
            // retrieve image
            BufferedImage bi = anImage;
            File outputfile = new File(location + fileName);
            ImageIO.write(bi, "png", outputfile);
        } catch (IOException e) {

        }
    }
    public void saveReadMe()
    {
        try (PrintWriter out = new PrintWriter(outputDirectory + this.idNumber + "README.txt")) {
            String text = this.boxSizeX + "\n" + this.boxSizeY + "\n" + this.idNumber + "\n" +
                    this.startX + "\n" + this.startY + "\n" + this.odd;
            out.println(text);
        }catch (Exception e)
        {
            System.out.println("README unable to save");
        }
    }
    public static BufferedImage rotateAndCrop(BufferedImage image, int degrees)
    {
        int maxSize = 54;

        int grayScale = AutoCrop.getBaseLineColor(image);
        int r = (int) (grayScale);
        int g = (int) (grayScale);
        int b = (int) (grayScale);
        Color bgColor = new Color(r, g, b);
        BufferedImage crop = new BufferedImage(maxSize, maxSize, BufferedImage.TYPE_INT_ARGB);
        BufferedImage rotated = rotateImage(image, degrees);
        int centerX = rotated.getWidth() / 2;
        int centerY = rotated.getHeight() / 2;
        int startX = centerX - maxSize/2;
        int startY = centerY - maxSize/2;
        /*
        System.out.println("Size: " + rotated.getWidth() + ", " + rotated.getHeight());
        System.out.println("Original Size: " + image.getWidth() + ", " + image.getHeight());
        System.out.println("Centers: " + centerX + ", " + centerY);
        System.out.println("Start: " + startX + ", " + startY);
        System.out.println("RGB: " + r + ", " + g + ", " + b);
        System.out.println("Gray scale: " + grayScale);
        */
        for(int x=0; x<maxSize; x++)
        {
            for(int y=0; y<maxSize; y++)
            {
                boolean transparent = AutoCrop.isTransparent(startX+x, startY+y, rotated);
                int rgb = rotated.getRGB(startX+x, startY+y);
                if(transparent){
                    crop.setRGB(x, y, bgColor.getRGB());
                }else{
                    crop.setRGB(x, y, rgb);
                }
            }
        }
        return crop;
    }
    public static boolean isImage(File file)
    {
        boolean isImage = false;
        if (file.getName().split("\\.").length >= 2) {
            int len = file.getName().split("\\.").length;
            if (file.getName().split("\\.")[len-1].equals("png")) {
                isImage = true;
            }
        }
        return isImage;
    }
    public static BufferedImage rotateImage(BufferedImage image, int degrees)
    {
        final double rads = Math.toRadians(degrees);
        final double sin = Math.abs(Math.sin(rads));
        final double cos = Math.abs(Math.cos(rads));
        final int w = (int) Math.floor(image.getWidth() * cos + image.getHeight() * sin);
        final int h = (int) Math.floor(image.getHeight() * cos + image.getWidth() * sin);
        final BufferedImage rotatedImage = new BufferedImage(w, h, image.getType());
        final AffineTransform at = new AffineTransform();
        at.translate(w / 2, h / 2);
        at.rotate(rads,0, 0);
        at.translate(-image.getWidth() / 2, -image.getHeight() / 2);
        final AffineTransformOp rotateOp = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
        rotateOp.filter(image,rotatedImage);
        return rotatedImage;
    }
}
