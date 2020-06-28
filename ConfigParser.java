/////////////////////////////////////////////////////////
/// @file         ConfigParser
/// @author       Siddharth
/// @language     Java
/// @compiler     JDK 1.6
/// @package      evolalgo
/// @project      default
/// @description  A class to parse the specified configuration file
///               Takes the cfg file and parses that file into variables
/////////////////////////////////////////////////////////

//package evolalgo;

import java.io.*;
    
public class ConfigParser {

    String configFileName = new String();

    public int noOfFitnessEvals;
    
    public String dataFilePath;
    public String dataFileName;

    public int rngMode;
    public int mu;
    public int lambda;
    public int kparent;
    public int ksurvivor;
    public int pencoeff;
    public int runs;

    public String logFilePath;
    public String logFileName;
    public String solFileName;

    public String graphPath;
    public String parentMode;
    public String survivorMode;

    public String[] tempArray = new String[35];

    public ConfigParser(String configName) {
        configFileName=configName;
    }

    public ConfigParser(){
        configFileName="default.cfg";
    }
    public void readConfig() {
        try {
            FileInputStream fstream = new FileInputStream(configFileName);
            // Get the object of DataInputStream
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine;
            //Read File Line By Line
            int i=0;
            while ((strLine = br.readLine()) != null)   {
                // Print the content on the console
                 tempArray[i] = strLine;
                 i++;
            }
            noOfFitnessEvals=Integer.parseInt(tempArray[11]);
            dataFilePath = tempArray[3];
            dataFileName = tempArray[1];
            rngMode     = Integer.parseInt(tempArray[5]);
            logFileName = tempArray[7];
            logFilePath = tempArray[9];
            graphPath   = tempArray[13];
            mu          = Integer.parseInt(tempArray[15]);
            lambda      = Integer.parseInt(tempArray[17]);
            kparent     = Integer.parseInt(tempArray[19]);
            ksurvivor   = Integer.parseInt(tempArray[21]);
            pencoeff    = Integer.parseInt(tempArray[23]);
            parentMode  = tempArray[25];
            survivorMode  = tempArray[27];
            solFileName = tempArray[29];
            runs        = Integer.parseInt(tempArray[31]);
            
            if(!parentMode.equals("tournament") && !parentMode.equals("fitness")) {

              System.out.println("There is an error in the Config file, check the parent selection mode value");
              System.exit(0);

            }

            if(!survivorMode.equals("tournament") && !survivorMode.equals("truncation")) {

              System.out.println("There is an error in the Config file, check the survivor selection mode value");
              System.exit(0);

            }

            //Close the input stream

            System.out.println("Reading File: "+dataFileName+" .............");
            in.close();
        }   catch (Exception e){
                System.out.println("The Default config file was not found or the path was wrong");
                System.out.println("Please place a file named default.cfg in C:\\ or please specify the configfile with its complete path");
                System.err.println("Error: " + e.getMessage());
                e.printStackTrace();
                System.exit(0);
        }
    }
}
