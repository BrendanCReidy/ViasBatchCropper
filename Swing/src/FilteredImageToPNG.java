import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Scanner;

public class FilteredImageToPNG {
    public static void main(String[] args)
    {
        /*
        String file1 = "/Users/brendan/Documents/USCWorkStudy/ViasProjectContinued/Out/CroppedData/UnfliteredData/Data0.txt";//*
        String filteredData = "/Users/brendan/Documents/USCWorkStudy/ViasProjectContinued/Out/CroppedData/FilteredData/Data0.txt";
        String outImage = "/Users/brendan/Documents/USCWorkStudy/ViasProjectContinued/Code/data0out.txt";
        String outlier = "/Users/brendan/Documents/USCWorkStudy/ViasProjectContinued/Code/outlier1.txt";

        BufferedImage img = TSVToImage(outlier);
        ImageParser.saveImage(img, "","Filter1.png");

        BufferedImage outliers = getOutliers();
        ImageParser.saveImage(outliers, "", "outliers.png");
        //*/
        ///*
        String inDir = "/Users/brendan/Documents/USCWorkStudy/ViasProjectContinued/VaryingFilters/LargePitch/Filter5/Outliers/";
        String outDir = "/Users/brendan/Documents/USCWorkStudy/ViasProjectContinued/VaryingFilters/LargePitch/Filter5/OutlierImages";
        File directory = new File(inDir);
        for(File file : directory.listFiles())
        {
            String[] split = file.getName().split("\\.");
            int len = split.length;
            System.out.println(file.getPath());
            if (!split[len-1].equals("txt"))
                continue;
            BufferedImage img = TSVToImage(inDir + file.getName());
            Color color = new Color(img.getRGB(0,0));
            float gray = (color.getRed() + color.getGreen() + color.getBlue()) / (float) (255*3);
            ImageParser.saveImage(img, outDir, "/" + split[0] + ".png");
        }
        //*/
    }

    static BufferedImage getOutliers()
    {
        String find = "outlier";
        String dataDir = "/Users/brendan/Documents/USCWorkStudy/ViasProjectContinued/Code/";
        File directory = new File(dataDir);
        BufferedImage concatImage = null;
        int row = 0;
        for(File file : directory.listFiles())
        {
            if(file.getName().startsWith(find))
            {
                BufferedImage img = FilteredImageToPNG.TSVToImage( dataDir + file.getName());
                if(concatImage==null)
                    concatImage = img;
                else
                    concatImage = GenerateFromTransitons.appendRow(concatImage,img);
            }
        }
        return concatImage;
    }

    static void saveToFile(double[][] aInput, String aFileName)
    {
        if(aFileName==null)
            return;
        try{
            PrintWriter writer = new PrintWriter(aFileName, "UTF-8");
            for(int x = 0; x<aInput.length; x++) {
                for(int y=0;y<aInput[x].length; y++) {
                    writer.print(aInput[x][y]);
                    if(y < aInput[x].length-1)
                        writer.print(",");
                }
                writer.println();
            }
            writer.close();
        }catch(FileNotFoundException e){
            System.out.println("File does not exist");
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
    }

    static BufferedImage TSVToImage(String file)
    {
        double[][] tsv = readTSV(file, getLength(file));
        BufferedImage image = new BufferedImage(tsv[0].length, tsv.length, BufferedImage.TYPE_INT_ARGB);
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
