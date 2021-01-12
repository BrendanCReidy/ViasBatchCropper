import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

/*
    Takes txt files from gwyddion and turns them into pngs
*/


public class txtToPNG {
    public static void main(String args[])
    {
        String inputDirectoryName = "/Users/brendan/Documents/USCWorkStudy/Gwyddion/histogram plot data-2/40um/"; // The directory where the txt files are


        File inputDirectory = new File(inputDirectoryName);
        for(File file : inputDirectory.listFiles())
        {
            if(file.getName().endsWith(".txt"))
            {
                String filePath = file.getPath();
                System.out.println(filePath);
                String filePrefix = file.getName().substring(0,file.getName().length()-4);
                BufferedImage image = TSVToImage(filePath);
                if(image==null)
                    continue;
                ImageParser.saveImage(image, inputDirectoryName, filePrefix + ".png");
            }
        }
    }

    static BufferedImage TSVToImage(String file)
    {
        double[][] tsv = readTSV(file, getLength(file));
        BufferedImage image;
        try {
            image = new BufferedImage(tsv[0].length, tsv.length, BufferedImage.TYPE_INT_ARGB);
        }catch (Exception e){
            System.out.println("[Warning] '" + file + "' was not formatted properly... Ignoring");
            return null;
        }
        int width = tsv[0].length;
        int height = tsv.length;
        double min = 999999;
        double max = -999999;
        for(int x=0; x<width; x++)
        {
            for(int y=0; y<height; y++)
            {
                if(tsv[y][x] > max){
                    max = tsv[y][x];
                }else if(tsv[y][x] < min){
                    min = tsv[y][x];
                }
            }
        }
        for(int x=0; x<width; x++)
        {
            for(int y=0; y<height; y++)
            {
                double current = tsv[y][x];
                double norm = (current - min) / (max - min);
                tsv[y][x] = norm;
            }
        }

        for(int x=0; x<image.getWidth(); x++)
        {
            for(int y=0; y<image.getHeight(); y++)
            {
                float current = (float) (tsv[y][x]);
                Color color = new Color(current,current,current);
                image.setRGB(x,y,color.getRGB());
            }
        }
        return image;
    }


    public static int getLength(String aFileName)
    {
        if(aFileName==null)
        {
            System.out.println("[FATAL] Input file name is null");
            return -1;
        }
        try {
            //System.out.println("Counting file: " + aFileName);
            Scanner fileScanner = new Scanner(new File(aFileName));
            int count = 0;
            while(fileScanner.nextLine().substring(0,1).equalsIgnoreCase("#")){

            }
            while(fileScanner.hasNext())
            {
                fileScanner.nextLine();
                count++;
            }
            fileScanner.close();
            return count + 1;
        }catch(FileNotFoundException e){
            System.out.println("File '" + aFileName + "' does not exist");
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
        return -1;
    }


    static double[][] readTSV(String aFileName, int length)
    {
        if(aFileName==null)
        {
            System.out.println("[FATAL] Input file name is null");
            return null;
        }
        try {
            double[][] dataArray = new double[length][];
            //System.out.println("Reading file: " + aFileName);
            Scanner fileScanner = new Scanner(new File(aFileName));
            int count = 0;
            String fileLine = "#";
            while(fileLine.substring(0,1).equalsIgnoreCase("#")){
                fileLine = fileScanner.nextLine();
            }
            while(true)
            {
                String[] splitLines = fileLine.split("\t");
                double[] values = new double[splitLines.length];
                for(int i=0; i<splitLines.length; i++){
                    double current = Double.parseDouble(splitLines[i]);
                    values[i] = current;
                }
                if(count>=dataArray.length) {
                    System.out.println("[WARNING] Reading stopped early. File is larger than specified");
                    break;
                }
                dataArray[count] = values;
                count++;
                if(fileScanner.hasNext())
                    fileLine = fileScanner.nextLine();
                else
                    break;
            }
            fileScanner.close();
            return dataArray;
        }catch(FileNotFoundException e){
            System.out.println("File '" + aFileName + "' does not exist");
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
        return null;
    }
}
