import java.awt.image.BufferedImage;
import java.io.File;

public class GenerateFromTransitons {
    public static void main(String[] args) {

        String dir = "/Users/brendan/Documents/USCWorkStudy/ViasProjectContinued/Code/Transitions/";
        File directory = new File(dir);
        BufferedImage concatImage = null;
        int row = 0;
        for(File innerDirectory : directory.listFiles())
        {
            if(!innerDirectory.isDirectory())
                continue;
            int column = 0;
            File[] files = innerDirectory.listFiles();
            files = sortOrder(files, innerDirectory);
            for(File file : files)
            {
                System.out.println(file.getName());
                String[] split = file.getName().split("\\.");
                int len = split.length;
                if (!split[len-1].equals("txt"))
                    continue;
                BufferedImage img = FilteredImageToPNG.TSVToImage(innerDirectory.getPath() + "/" + file.getName());
                if(concatImage==null)
                    concatImage = img;
                else if(column==0)
                    concatImage = appendColumn(concatImage,img);
                else if(row==0)
                    concatImage = appendRow(concatImage,img);
                else
                    concatImage = append(concatImage,img, column);
                column++;
            }
            row++;
        }
        ImageParser.saveImage(concatImage, "", "concat.png");
    }

    public static File[] sortOrder(File[] files, File parentDirectory)
    {
        File[] ret = new File[files.length];
        for(int i=0; i<files.length; i++)
        {
            ret[i] = new File(parentDirectory.getPath() + "/" + i + ".txt");
        }
        return ret;
    }

    public static BufferedImage append(BufferedImage base, BufferedImage img, int column)
    {
        int w = base.getWidth();
        int h = base.getHeight();
        BufferedImage newImage = new BufferedImage(w,h,BufferedImage.TYPE_INT_ARGB);

        for(int x=0; x<base.getWidth(); x++)
        {
            for(int y=0; y<base.getHeight(); y++)
            {
                newImage.setRGB(x,y,base.getRGB(x,y));
            }
        }
        for(int x=0; x<img.getWidth(); x++)
        {
            for(int y=0; y<img.getHeight(); y++)
            {
                newImage.setRGB(x+column*img.getWidth(),h - img.getHeight() + y,img.getRGB(x,y));
            }
        }
        return newImage;
    }

    public static BufferedImage appendRow(BufferedImage base, BufferedImage img)
    {
        int w = base.getWidth() + img.getWidth();
        int h = base.getHeight();
        BufferedImage newImage = new BufferedImage(w,h,BufferedImage.TYPE_INT_ARGB);

        for(int x=0; x<base.getWidth(); x++)
        {
            for(int y=0; y<base.getHeight(); y++)
            {
                newImage.setRGB(x,y,base.getRGB(x,y));
            }
        }
        for(int x=0; x<img.getWidth(); x++)
        {
            for(int y=0; y<img.getHeight(); y++)
            {
                newImage.setRGB(x+base.getWidth(),h - img.getHeight() + y,img.getRGB(x,y));
            }
        }
        return newImage;
    }

    public static BufferedImage appendColumn(BufferedImage base, BufferedImage img)
    {
        int w = base.getWidth();
        int h = base.getHeight() + img.getHeight();
        BufferedImage newImage = new BufferedImage(w,h,BufferedImage.TYPE_INT_ARGB);

        for(int x=0; x<base.getWidth(); x++)
        {
            for(int y=0; y<base.getHeight(); y++)
            {
                newImage.setRGB(x,y,base.getRGB(x,y));
            }
        }
        for(int x=0; x<img.getWidth(); x++)
        {
            for(int y=0; y<img.getHeight(); y++)
            {
                newImage.setRGB(x,y+base.getHeight(),img.getRGB(x,y));
            }
        }
        return newImage;
    }
}
