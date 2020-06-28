/////////////////////////////////////////////////////////
/// @file         LogWriter
/// @author       Siddharth
/// @language     Java
/// @compiler     JDK 1.6
/// @package      evolalgo
/// @project      default
/// @description  A small class with function to handle the log and graph files
///               Basic functions are to write to files line by line
/////////////////////////////////////////////////////////

//package evolalgo;

import java.io.*;

public class LogWriter {
    BufferedWriter out;
    LogWriter(String fileName) {
        try {
        out = new BufferedWriter(new FileWriter(fileName));
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    public void write(String line) {
        try {
            out.write(line);
            out.newLine();           
            //System.out.println("Writing to Log");
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public void close() {
        try {
            out.close();
        } catch (Exception e) {
            System.out.println(e);
        }
    }
    public String solSetToString(int[] solSet, String separator) {
        String result = "";
        if (solSet.length > 0) {
           result="";    // start with the first element
            for (int i=0; i<solSet.length; i++) {
                if(solSet[i]!=0) {
                    result = result + separator + solSet[i];
                }
            }
        }
        return result;
    }
}
