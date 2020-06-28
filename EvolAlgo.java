/////////////////////////////////////////////////////////
/// @file         EvolAlgo
/// @author       Siddharth
/// @language     Java
/// @compiler     JDK 1.6
/// @package      evolalgo
/// @project      default
/// @description  An implementation of Evolutionary algorithm for finding
///               the optimal set our Routers for destroying the compromised paths
/////////////////////////////////////////////////////////

//package evolalgo;

import java.io.*;
import java.util.*;

public class EvolAlgo {
  
  //Hashtable for storing the data in the network file
  static Hashtable<Integer,int[]>   fileHash  = new Hashtable<Integer,int[]>();

  //Hashtable for storing the population
  static Hashtable<Integer,int []>  popHash   = new Hashtable<Integer,int[]>();

  //Hashtable for storing the penalty/fitness of each population
  static Hashtable<Integer,Float>   penaltyHash=new Hashtable<Integer,Float>();

  static int noOfHosts=0;
  static int noOfRouters=0;
  static int [] candidate;
  static ConfigParser cp = null;

  public static void main(String args[]) {
      //  Config parsing begin
      
      if (args.length==0) {
          cp = new ConfigParser("default.cfg");
          cp.readConfig();
      } else if(args.length==1) {
          cp = new ConfigParser(args[0]);
          cp.readConfig();
      }
      // Config Parsing End
      // File Reading Start for capturing no of host and routers

      String [] ftemp;
      int [] pathrouters;
      // <editor-fold>

      try {
          FileInputStream fstream = new FileInputStream(cp.dataFilePath+cp.dataFileName);
          // Get the object of DataInputStream
          DataInputStream in = new DataInputStream(fstream);
          BufferedReader br = new BufferedReader(new InputStreamReader(in));
          String strLine;
          //Read File Line By Line
          int i=0;
          while ((strLine = br.readLine()) != null)   {
              // Print the content on the console
               if(i==1) {
                   noOfHosts=Integer.parseInt(strLine);
                   i++;
                   continue;
               }
               if(i==0 || i==2 ||i==4) {
                    i++;
                    continue;
               }
               if(i==3) {
                   noOfRouters=Integer.parseInt(strLine);
                   i++;
                   //mainRouterSet = new int[2*noOfRouters];
                   continue;
              } else  {
                  ftemp=strLine.split(" ");
                  pathrouters = new int[ftemp.length-1];
                  int ctr=0;
                  while(ctr<(ftemp.length-1)) {
                    pathrouters[ctr]=Integer.parseInt(ftemp[ctr+1]);
                    ctr++;
                  }
                  fileHash.put((Integer.parseInt(ftemp[0])+0), pathrouters);
                  //System.out.println(ftemp[0]+"+"+pathrouters[0]);
              }
          }
          //mainRouterSet=removeDuplicates(mainRouterSet,1);
          in.close();
      }
      catch (Exception e){//Catch exception if any
          e.printStackTrace();
          System.exit(0);
      }
      // end capturing no of hosts and routers form the data file
      // </editor-fold>

      System.out.println("No of Hosts: "+noOfHosts+"\tNo of Routers: "+noOfRouters);
      LogWriter log = new LogWriter(cp.logFilePath+cp.logFileName); //initialize log file
      //LogWriter graph = new LogWriter(cp.graphPath);                //initialize graph file
      LogWriter solution = new LogWriter(cp.solFileName);
 
      for(int runcount=1;runcount<=cp.runs;runcount++) {
        // <editor-fold>
        Random ran = new Random();
        int [] randCand;
        int evalcounter=0;
        int randomNumber;
        int [] newtemp;

        //Generating the 1st mu random candidates
        for(int i=0;i<cp.mu;i++) {
          randCand = new int[noOfHosts];
          //generating random candidates
          for(int k=0;k<noOfHosts;k++) {
            newtemp = fileHash.get(k);
            randomNumber= ran.nextInt(newtemp.length);
            randCand[k]=newtemp[randomNumber];
          }
          //end generating random candidate
          randCand = removeDuplicates(randCand,1);  //removing duplicates from random candicates
          popHash.put(evalcounter, randCand); //place random candidate in the population hashtable
          evalcounter++;  //incrementing evaluation counter
        }     //end generating random candicates
        //System.out.println("Finished selecting random candidates");
        //evaluating penalty for random candicates
        float fpen;
        for(int i=0;i<cp.mu;i++) {
          randCand=popHash.get(i);
          //fpen=0-randCand.length;
          fpen = evaluatePenalty(randCand); //evaluating penalty for each random candidate
          penaltyHash.put(i, fpen);         //storing the evaluated penalty value in the penalty hash
        }
        //System.out.println("Finished evaluating penalties for all mu candidates");
        //end evaluating penalty of random candidates
        //end work on random candidates. now starts parent selection, recombination and mutation.
        log.write("Run : "+runcount);
        //log.write(""+cp.mu);    //writing mu to the 1st row of the log file
        //graph.write("Run : "+runcount);

        if(cp.parentMode.equals("tournament")) {

          while(evalcounter<cp.noOfFitnessEvals) {
            float worstPenalty;
            int   worstIndex;
            float avgFitness=0;
            float bestFitness=0;
            double stdDev=0;
            //Begin k-tournament Selection for parent selection.
            int genCounter=0;
            do {
              if(evalcounter==cp.noOfFitnessEvals) {
                   break;
              }
              for(int i=0;i<cp.mu;i=i+2*cp.kparent) {
                int [] fatherIndex = new int[cp.kparent];
                float [] fatherPenalty = new float[cp.kparent];
                int [] motherIndex = new int[cp.kparent];
                float [] motherPenalty = new float[cp.kparent];
                float bestMpen;
                float bestFpen;
                int bestFather=i;
                int bestMother=i;
                //taking the 1st k candidates from population hash
                for(int j=0;j<cp.kparent;j++) {
                  fatherIndex[j]=i+j;
                  fatherPenalty[j]=penaltyHash.get(j+i);
                }
                //taking the next k candidates from the population hash
                for(int j=0;j<cp.kparent;j++) {
                  motherIndex[j]=i+j+cp.kparent;
                  motherPenalty[j]=penaltyHash.get(j+i+cp.kparent);
                }
                bestMpen=maxFloat(motherPenalty);
                bestFpen=maxFloat(fatherPenalty);
                //selecting the best father
                for(int k=0;k<cp.kparent;k++) {
                  if(fatherPenalty[k]==bestFpen) {
                    bestFather=fatherIndex[k];
                  }
                }
                //selecting the best mother
                for(int k=0;k<cp.kparent;k++) {
                  if(motherPenalty[k]==bestMpen) {
                    bestMother=motherIndex[k];
                  }
                }
                //end of seleciton
                //generating candidate from the selected parents
                candidate = generateOffspring(popHash.get(bestMother),popHash.get(bestFather));
                //evaluating offsprings penalty
                float candidatePenalty = evaluatePenalty(candidate);
                //incrementing eval counter for every candidate evaluated
                evalcounter++;

                //begin replacement of worst candidate in the population hash.
                worstPenalty=penaltyHash.get(0);
                worstIndex=0;
                avgFitness=0;
                bestFitness=0-noOfHosts;
                double [] penaltyArray = new double[cp.mu];
                for(int ik=0;ik<cp.mu;ik++) {
                  float tempf = penaltyHash.get(ik);
                  penaltyArray[ik]=tempf;
                  if(bestFitness<tempf) {
                    bestFitness=tempf;

                  }
                  avgFitness+=tempf;
                  if(worstPenalty>tempf) {
                    worstPenalty=tempf;
                    worstIndex=ik;
                  }
                }
                stdDev = sdFast(penaltyArray);
                //we found the worst candidate, now we have to replace it in the population hash
                avgFitness=avgFitness/cp.mu;  //calculating the average fitness
                if(candidatePenalty>worstPenalty) {
                  penaltyHash.put(worstIndex, candidatePenalty);
                  popHash.put(worstIndex, candidate);
                }
                //end replacement of worst candidate in the population hash.
                 genCounter++;

              }

            } while(genCounter<cp.lambda);
            //end one generation evaluation
            //now writing to log, the values after one generation
            String tempstr = ""+evalcounter+","+avgFitness+","+bestFitness+","+stdDev;
            log.write(tempstr);
            //tempstr=""+evalcounter+","+avgFitness;
            //graph.write(tempstr);
            //System.out.println("@Evaluation : "+evalcounter);
            //write them to log
          }
        //  log.close();
        //  graph.close();
        } else if (cp.parentMode.equals("fitness")) {
          while(evalcounter<cp.noOfFitnessEvals) {
            float worstPenalty;
            int   worstIndex;
            float avgFitness=0;
            float bestFitness=0;
            double stdDev=0;
            //Begin k-tournament Selection for parent selection.
            int genCounter=0;
            do {
              if(evalcounter==cp.noOfFitnessEvals) {
                   break;
              }
              
              for(int i=0;i<cp.mu;i=i=i+2) {
                for(int ik=0;ik<cp.mu;ik++) {

                  float tempf = penaltyHash.get(ik);
                  avgFitness+=tempf;
                }
                avgFitness=avgFitness/cp.mu;  //calculating the average fitness

                int [] fatherIndex = new int[noOfHosts];
                float [] fatherPenalty = new float[noOfHosts];
                int [] motherIndex = new int[noOfHosts-1];
                float [] motherPenalty = new float[noOfHosts-1];
                float bestMpen;
                float bestFpen;
                int bestFather=i;
                int bestMother=i;
                for(int j=0;j<noOfHosts;j++) {
                  fatherIndex[j]=j;
                  fatherPenalty[j]=penaltyHash.get(j);
                }
                bestFpen=maxFloat(fatherPenalty);

                for(int k=0;k<noOfHosts;k++) {
                  if(fatherPenalty[k]==bestFpen) {
                    bestFather=fatherIndex[k];
                  }
                }
                 //selecting the best father

                for(int j=0;j<noOfHosts-1;j++) {
                  if(j!=bestFather) {
                    motherIndex[j]=j;
                    motherPenalty[j]=penaltyHash.get(j);
                  }
                }

                bestMpen=maxFloat(motherPenalty);

                //selecting the best mother
                for(int k=0;k<noOfHosts-1;k++) {
                  if(motherPenalty[k]==bestMpen) {
                    bestMother=motherIndex[k];
                  }
                }
                //end of seleciton
                //generating candidate from the selected parents
                candidate = generateOffspring(popHash.get(bestMother),popHash.get(bestFather));
                //evaluating offsprings penalty
                float candidatePenalty = evaluatePenalty(candidate);
                //incrementing eval counter for every candidate evaluated
                evalcounter++;

                //begin replacement of worst candidate in the population hash.
                worstPenalty=penaltyHash.get(0);
                worstIndex=0;
                bestFitness=0-noOfHosts;
                double [] penaltyArray = new double[cp.mu];
                for(int ik=0;ik<cp.mu;ik++) {
                  float tempf = penaltyHash.get(ik);
                  penaltyArray[ik]=tempf;
                  if(bestFitness<tempf) {
                    bestFitness=tempf;
                  }
                  if(worstPenalty>tempf) {
                    worstPenalty=tempf;
                    worstIndex=ik;
                  }
                }
                stdDev = sdFast(penaltyArray);
                if(candidatePenalty>worstPenalty) {
                  penaltyHash.put(worstIndex, candidatePenalty);
                  popHash.put(worstIndex, candidate);
                }
                //end replacement of worst candidate in the population hash.
                 genCounter++;
                 if(genCounter==cp.lambda)
                   break;
              }

            } while(genCounter<cp.lambda);
            //end one generation evaluation
            //now writing to log, the values after one generation
            String tempstr = ""+evalcounter+","+avgFitness+","+bestFitness+","+stdDev;
            log.write(tempstr);
            tempstr=""+evalcounter+","+avgFitness;
            //graph.write(tempstr);
            //System.out.println("@Evaluation : "+evalcounter);
            //write them to log
          }
        }
        //System.out.println("Writing Solution");
        float sPenalty=0-noOfHosts;
        int   sIndex=0;
        for(int ik=0;ik<cp.mu;ik++) {
           if(sPenalty<penaltyHash.get(ik)) {
            sPenalty = penaltyHash.get(ik);
            //System.out.println("->"+sPenalty);
            sIndex=ik;
           }
        }
        int [] temparray = popHash.get(sIndex);
        String strtemp="";
        for(int ik=0;ik<temparray.length;ik++) {
          if(ik==temparray.length-1) {
             strtemp=strtemp+temparray[ik];
             break;
          }
          strtemp=strtemp+temparray[ik]+",";
        }
		solution.write("Run: "+runcount+",Sol: "+temparray.length);
        solution.write(strtemp);
        System.out.println("Run: "+runcount+" Sol: "+temparray.length);
         // </editor-fold>
      }
      
      log.close();
      //graph.close();
      solution.close();
  }

  //this function takes a float array and returns the maximum value from that array
  public static float maxFloat(float[] t) {
    float maximum = t[0];   // start with the first value
    for (int i=1; i<t.length; i++) {
        if (t[i] > maximum && t[i]!=0) {
            maximum = t[i];   // new maximum
        }
    }
    return maximum;
  }//end method max

  //this function takes in a routerset which is an integer array
  //it evaluates the fitness/penalty of that routerset and then returns a float value
  public static float evaluatePenalty(int[] routerSet) {
    float fpen=0;
    int[] path;
    int l=0;
    int[] patharray = new int[7*noOfHosts];
    //finding all the paths that are destroyed by the routerset
    for(int i=0;i<routerSet.length;i++) {   
      for(int k=0;k<noOfHosts;k++) {
        path=fileHash.get(k);
        //System.out.println(path.length);
        for(int j=0;j<path.length;j++){
          if(path[j]==routerSet[i])  {
            patharray[l]=k;
            l++;
            break;
          }        
        }
      }
    }
    
    patharray = removeDuplicates(patharray,0); //removing all the duplicate paths in the path array
    //calculating penalty/fitness
    //penalty= -cardinality - penaltycoefficient*no of alive paths
    fpen=0-routerSet.length-(cp.pencoeff*(noOfHosts-patharray.length));
    return fpen;
  }

  //this function takes in two routersets which are integer arrays
  //it performs single point crossover on both the routersets
  //it then mutates the resulting offspring
  //it then eliminates the duplicates and returns the offspring
  public static int [] generateOffspring(int[] mother, int[] father) {
    int randomNumber,temp;
    int max = Math.max(father.length, mother.length);
    int [] son = new int[max];
    Random ran = new Random();
    //for single point crossover, we generate a random number and then
    //split the arrays at the index=randomnumber and then merge the 1st part of
    //father and 2nd part of mother or vice versa to get the offspring
    randomNumber = ran.nextInt(Math.max(mother.length,father.length)-3);
    if(mother.length>father.length) {
      for(int i=0;i<randomNumber;i++) {
        son[i]=father[i];
      }
      for(int i=randomNumber;i<max;i++) {
        son[i]=mother[i];
      }
    } else  {
      for(int i=0;i<randomNumber;i++) {
        son[i]=mother[i];
      }
      for(int i=randomNumber;i<max;i++) {
        son[i]=father[i];
      }
    }

    //Mutating the offspring
    //to mutate an offspring we replace a single router in the offspring with
    //a randomly generated router. This way we can ensure that mutation occurs
    randomNumber = ran.nextInt(noOfRouters-1);
    temp=ran.nextInt(noOfRouters)+noOfHosts;
    randomNumber = ran.nextInt(max-1);
    son[randomNumber]=temp;
    //end mutation
    son=removeDuplicates(son,1); //removing duplicates from the offspring
    return son;
  }

  //Imported Function from http://docs.google.com/Doc?id=ah9xr5g2vzfz_32gvtwfw
  //modified parameters for convinience
  public static int[] removeDuplicates(int[] array, int condition) {
      // Take care of the cases where the array is null or is empty.
      if (array == null) return null;
      if (array.length == 0) return new int[0];
      // Use a LinkedHashSet as a mean to remove the duplicate entries.
      // The LinkedHashSet has two characteristics that fit for the job:
      // First, it retains the insertion order, which ensure the output's
      // order is the same as the input's. Secondly, by being a set, it
      // only accept each entries once; a LinkedHashSet ignores subsequent
      // insertion of the same entry.
      LinkedHashSet<Integer> set = new LinkedHashSet<Integer>();
      for (int n: array) {
        if(condition==1) {
          if(n!=0) {
            set.add(new Integer(n));
          }
        } else if(condition==0) {
           set.add(new Integer(n));
        }
      }
      // At this point, the LinkedHashSet contains only unique entries.
      // Since the function must return an int[], we need to copy entries
      // from the LinkedHashSet to a brand new array.
      int cursor = 0;
      int[] newArray = new int[set.size()];
      for (Integer n: set)
        newArray[cursor++] = n.intValue();
      return newArray;
    }
  
  public static double sdFast ( double[] data ) {
      // sd is sqrt of sum of (values-mean) squared divided by n - 1
      // Calculate the mean
      double mean = 0;
      final int n = data.length;
      if ( n < 2 ) {
         return Double.NaN;
      }
      for ( int i=0; i<n; i++ ) {
         mean += data[i];
      }
      mean /= n;
      // calculate the sum of squares
      double sum = 0;
      for ( int i=0; i<n; i++ ) {
       final double v = data[i] - mean;
       sum += v * v;
      }
      // Change to ( n - 1 ) to n if you have complete data instead of a sample.
      return Math.sqrt( sum / ( n - 1 ) );
  }
}