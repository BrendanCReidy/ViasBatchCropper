import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class AutoCrop {

    public class Vector2D
    {
        int x;
        int y;
        public Vector2D(int x, int y)
        {
            this.x = x;
            this.y = y;
        }
    }

    public static boolean doTopCrop = true;

    public static BufferedImage autoCrop(BufferedImage img, int finalSize, int padding, int buffer)
    {
        if(doTopCrop)
            return topCrop(img, finalSize, padding, buffer);
        return bottomCrop(img, finalSize, padding, buffer);
    }

    public Vector2D getTopIntercept(BufferedImage img, int finalSize, int padding, int buffer)
    {
        int baseLine = 0;
        int num = 0;
        boolean allTransparent = true;
        int yVal = 0;
        baseLine = getBaseLineColor(img);
        int xIntercept = 0;
        int yIntercept = 0;
        int xEnd = 0;
        int yEnd = 0;
        int range = 5;
        int pixelBuffer = padding;
        for(int x=0; x<img.getWidth(); x++)
        {
            for(int y=0; y<img.getHeight(); y++)
            {
                if(isTransparent(x,y,img)){
                    continue;
                }
                Color color = new Color(img.getRGB(x, y));
                int grayScale = ((color.getRed() + color.getGreen() + color.getBlue())/3);
                if(!(grayScale>baseLine-buffer && grayScale<baseLine+buffer) && grayScale>10 && x>pixelBuffer){
                    xIntercept = x;
                    break;
                }
            }
            if(xIntercept!=0)
            {
                break;
            }
        }
        for(int y=0; y<img.getHeight(); y++)
        {
            for(int x=0; x<img.getWidth(); x++)
            {
                if(isTransparent(x,y,img)){
                    continue;
                }
                Color color = new Color(img.getRGB(x, y));
                int grayScale = ((color.getRed() + color.getGreen() + color.getBlue())/3);
                if(!(grayScale>baseLine-buffer && grayScale<baseLine+buffer) && grayScale>10) {
                    if (yIntercept == 0 && y>pixelBuffer) {
                        yIntercept = y;
                        break;
                    }
                }
            }
            if(yIntercept!=0){
                break;
            }
        }
        int boxSize = finalSize + pixelBuffer;
        yIntercept-=pixelBuffer;
        xIntercept-=pixelBuffer;
        Vector2D ret = new Vector2D(xIntercept,yIntercept);
        return ret;
    }


    public Vector2D getBottomIntercept(BufferedImage img, int finalSize, int padding, int buffer)
    {
        int baseLine = 0;
        int num = 0;
        boolean allTransparent = true;
        int yVal = 0;
        baseLine = getBaseLineColor(img);
        int xIntercept = 0;
        int yIntercept = 0;
        int xEnd = 0;
        int yEnd = 0;
        int range = 5;
        int pixelBuffer = padding;
        for(int x=img.getWidth()-1; x>0; x--)
        {
            for(int y=img.getHeight()-1; y>0; y--)
            {
                if(isTransparent(x,y,img)){
                    continue;
                }
                Color color = new Color(img.getRGB(x, y));
                int grayScale = ((color.getRed() + color.getGreen() + color.getBlue())/3);
                if(!(grayScale>baseLine-buffer && grayScale<baseLine+buffer) && grayScale>10 && x<img.getWidth() - pixelBuffer){
                    xIntercept = x;
                    break;
                }
            }
            if(xIntercept!=0)
            {
                break;
            }
        }
        for(int y=img.getHeight()-1; y>0; y--)
        {
            for(int x=img.getWidth()-1; x>0; x--)
            {
                if(isTransparent(x,y,img)){
                    continue;
                }
                Color color = new Color(img.getRGB(x, y));
                int grayScale = ((color.getRed() + color.getGreen() + color.getBlue())/3);
                if(!(grayScale>baseLine-buffer && grayScale<baseLine+buffer) && grayScale>10) {
                    if (yIntercept == 0 && y<img.getHeight() - pixelBuffer) {
                        yIntercept = y;
                        break;
                    }
                }
            }
            if(yIntercept!=0){
                break;
            }
        }
        int boxSize = finalSize + pixelBuffer;
        yIntercept+=pixelBuffer;
        xIntercept+=pixelBuffer;
        Vector2D ret = new Vector2D(xIntercept,yIntercept);
        return ret;
    }



    public static BufferedImage topCrop(BufferedImage img, int finalSize, int padding, int buffer)
    {
        int baseLine = 0;
        int num = 0;
        boolean allTransparent = true;
        int yVal = 0;
        baseLine = getBaseLineColor(img);
        int xIntercept = 0;
        int yIntercept = 0;
        int xEnd = 0;
        int yEnd = 0;
        int range = 5;
        int pixelBuffer = padding;
        for(int x=0; x<img.getWidth(); x++)
        {
            for(int y=0; y<img.getHeight(); y++)
            {
                if(isTransparent(x,y,img)){
                    continue;
                }
                Color color = new Color(img.getRGB(x, y));
                int grayScale = ((color.getRed() + color.getGreen() + color.getBlue())/3);
                if(!(grayScale>baseLine-buffer && grayScale<baseLine+buffer) && grayScale>10 && x>pixelBuffer){
                    xIntercept = x;
                    break;
                }
            }
            if(xIntercept!=0)
            {
                break;
            }
        }
        for(int y=0; y<img.getHeight(); y++)
        {
            for(int x=0; x<img.getWidth(); x++)
            {
                if(isTransparent(x,y,img)){
                    continue;
                }
                Color color = new Color(img.getRGB(x, y));
                int grayScale = ((color.getRed() + color.getGreen() + color.getBlue())/3);
                if(!(grayScale>baseLine-buffer && grayScale<baseLine+buffer) && grayScale>10) {
                    if (yIntercept == 0 && y>pixelBuffer) {
                        yIntercept = y;
                        break;
                    }
                }
            }
            if(yIntercept!=0){
                break;
            }
        }
        int boxSize = finalSize + pixelBuffer;
        yIntercept-=pixelBuffer;
        xIntercept-=pixelBuffer;
        BufferedImage crop = new BufferedImage(boxSize, boxSize, BufferedImage.TYPE_INT_ARGB);
        for(int x=0; x<boxSize; x++)
        {
            for(int y=0; y<boxSize; y++)
            {
                int rgb = getBaseLineColor(img);
                if(xIntercept+x >= 0 && xIntercept+x<img.getWidth()) {
                    if(yIntercept+y >= 0 && yIntercept+y<img.getHeight()) {
                        rgb = img.getRGB(xIntercept + x, yIntercept + y);
                    }
                }
                if(x >= 0 && x<crop.getWidth()) {
                    if(y >= 0 && y<crop.getHeight()) {
                        crop.setRGB(x, y, rgb);
                    }
                }
            }
        }
        return crop;
    }
    public static BufferedImage bottomCrop(BufferedImage img, int finalSize, int padding, int buffer)
    {
        int baseLine = 0;
        int num = 0;
        boolean allTransparent = true;
        int yVal = 0;
        baseLine = getBaseLineColor(img);
        int xIntercept = 0;
        int yIntercept = 0;
        int xEnd = 0;
        int yEnd = 0;
        int range = 5;
        int pixelBuffer = padding;
        for(int x=img.getWidth()-1; x>0; x--)
        {
            for(int y=img.getHeight()-1; y>0; y--)
            {
                if(isTransparent(x,y,img)){
                    continue;
                }
                Color color = new Color(img.getRGB(x, y));
                int grayScale = ((color.getRed() + color.getGreen() + color.getBlue())/3);
                if(!(grayScale>baseLine-buffer && grayScale<baseLine+buffer) && grayScale>10 && x<img.getWidth() - pixelBuffer){
                    xIntercept = x;
                    break;
                }
            }
            if(xIntercept!=0)
            {
                break;
            }
        }
        for(int y=img.getHeight()-1; y>0; y--)
        {
            for(int x=img.getWidth()-1; x>0; x--)
            {
                if(isTransparent(x,y,img)){
                    continue;
                }
                Color color = new Color(img.getRGB(x, y));
                int grayScale = ((color.getRed() + color.getGreen() + color.getBlue())/3);
                if(!(grayScale>baseLine-buffer && grayScale<baseLine+buffer) && grayScale>10) {
                    if (yIntercept == 0 && y<img.getHeight() - pixelBuffer) {
                        yIntercept = y;
                        break;
                    }
                }
            }
            if(yIntercept!=0){
                break;
            }
        }
        int boxSize = finalSize + pixelBuffer;
        yIntercept+=pixelBuffer;
        xIntercept+=pixelBuffer;
        BufferedImage crop = new BufferedImage(boxSize, boxSize, BufferedImage.TYPE_INT_ARGB);
        for(int x=0; x<boxSize; x++)
        {
            for(int y=0; y<boxSize; y++)
            {
                int rgb = getBaseLineColor(img);
                if(xIntercept-x >= 0 && xIntercept-x<img.getWidth()) {
                    if(yIntercept-y >= 0 && yIntercept - y<img.getHeight()) {
                        rgb = img.getRGB(xIntercept - x, yIntercept - y);
                    }
                }
                if(x >= 0 && x<crop.getWidth()) {
                    if(y >= 0 && y<crop.getHeight()) {
                        crop.setRGB(x, y, rgb);
                    }
                }
            }
        }
        return crop;
    }
    public static int getBaseLineColor(BufferedImage img)
    {
        int baseLine = 0;
        int num = 0;
        int yVal = 0;
        boolean allTransparent = true;
        while(allTransparent && yVal < img.getHeight()) {
            baseLine = 0;
            num = 0;
            for (int x = 0; x < img.getWidth(); x++) {
                boolean transparent = isTransparent(x, yVal, img);
                if (!transparent) {
                    allTransparent = false;
                    Color color = new Color(img.getRGB(x, yVal));
                    baseLine += ((color.getRed() + color.getGreen() + color.getBlue()) / 3);
                    num++;
                }
            }
            yVal++;
            if(baseLine<10)
            {
                allTransparent=true;
            }
        }
        baseLine/=num;
        return baseLine;
    }
    public static boolean isTransparent( int x, int y, BufferedImage img) {
        int pixel = img.getRGB(x,y);
        if( (pixel>>24) == 0x00 ) {
            return true;
        }
        return false;
    }
}
