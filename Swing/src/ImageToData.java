import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;

public class ImageToData {
    public static void main(String[] args)
    {
        String dir = "/Users/brendan/Documents/USCWorkStudy/ViasProjectContinued/Out/SmallPitch/CroppedData/UnfilteredData/";
        File directory = new File(dir);
        String imageData = "";
        String labels = "";
        for(File file : directory.listFiles())
        {
            System.out.println(file.getName());
            String[] split = file.getName().split("\\.");
            int len = split.length;
            if (!split[len-1].equals("png"))
                continue;
            BufferedImage img = null;
            try {
                img = ImageIO.read(file);
                imageData += imageToCSV(img) + "\n";
                labels+=file.getName() + "\n";
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        saveToFile(imageData,"imageData.csv");
        saveToFile(labels,"imageMap.csv");
    }

    static void saveToFile(String aInput, String aFileName)
    {
        if(aFileName==null)
            return;
        try{
            PrintWriter writer = new PrintWriter(aFileName, "UTF-8");
            writer.println(aInput);
            writer.close();
        }catch(FileNotFoundException e){
            System.out.println("File does not exist");
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String imageToCSV(BufferedImage img)
    {
        int maxValue = 255;
        String ret = "";
        for(int x = 0; x<img.getWidth(); x++)
        {
            for(int y = 0; y < img.getHeight(); y++)
            {
                int value = img.getRGB(x, y);
                Color color = new Color(value);
                float avg = (float) (color.getRed() + color.getGreen() + color.getBlue()) / 3.0f;
                ret += ((avg) / (float) maxValue);
                if(y!=img.getHeight()-1 || x!=img.getWidth()-1)
                    ret += ",";
            }
        }
        return ret;
    }
}
