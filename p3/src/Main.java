import java.util.Arrays;
import java.util.Scanner;

// Done by Milan Haydel C00419477
public class Main {

    public static void main(String[] args) {
        /*
        // Time code by Chris Walther C00408978
        // Comment out time code during normal operation and uncomment out for use on Task 1 Question 1
        long start = System.currentTimeMillis();
        System.out.println("Timer start");
        // Other code in between
        long end = System.currentTimeMillis();
        System.out.println("Timer end");
        System.out.println("Elapsed Time in milliseconds: "+ (end-start));
         */
        int S = 0, C = 1, quanT = 0;


        try{
            for (int i = 0; i < args.length; i++){
                switch (args[i]){
                    case "-S":
                        try {
                            S = Integer.parseInt(args[++i]);
                        } catch (NumberFormatException e) {
                            throw new IllegalArgumentException(
                                    "ERROR: INVALID/MISSING ALGORITHM NUMBER");
                        } catch (ArrayIndexOutOfBoundsException e) {
                            throw new IllegalArgumentException(
                                    "ERROR: INVALID/MISSING ALGORITHM NUMBER");
                        }
                        if(S < 1 || S > 4){
                            throw new NumberFormatException(
                                    "ERROR: INVALID/MISSING ALGORITHM NUMBER");
                        }
                        else if (S == 2){
                            try {
                                quanT = Integer.parseInt(args[++i]);
                            } catch (NumberFormatException e) {
                                throw new IllegalArgumentException(
                                        "ERROR: INVALID/MISSING ALGORITHM NUMBER");
                            } catch (ArrayIndexOutOfBoundsException e){
                                throw new IllegalArgumentException(
                                        "ERROR: INVALID/MISSING ALGORITHM NUMBER");
                            }
                            if(quanT < 2 || quanT > 10){
                                throw new NumberFormatException(
                                        "ERROR: INVALID ALGORITHM NUMBER; OUT OF RANGE");
                            }
                        }
                        break;

                    // if there is the argument '-C', there must be a number following
                    case "-C":
                        try {
                            C = Integer.parseInt(args[++i]);
                        } catch (NumberFormatException e) {
                            throw new IllegalArgumentException(
                                    "ERROR: INVALID/MISSING ALGORITHM NUMBER");
                        }
                        catch (ArrayIndexOutOfBoundsException e){
                            throw new IllegalArgumentException(
                                    "ERROR: INVALID/MISSING ALGORITHM NUMBER");
                        }
                }
            }
            if( S == 4 && C > 1){
                System.out.println(
                        "\nWARNING: CORES WILL BE IGNORED FOR PREEMPTIVE SHORTEST JOB FIRST AND CHANGED TO 1"
                );
                C = 1;
            }
            System.out.print("\tScheduler: ");
            switch (S){
                case 1:
                    System.out.print("FCFS");
                    break;
                case 2:
                    System.out.print("RR");
                    break;
                case 3:
                    System.out.print("NPSJF");
                    break;
                case 4:
                    System.out.print("PSJF");
                    break;
            }
            System.out.println(
                    "\n\tTime Quantum: " + quanT + "\n\tCores: " + C
            );
            // Time code by Chris Walther C00408978
            // Comment out time code during normal operation and uncomment out for use on Task 1 Question 1
            long start = System.currentTimeMillis();
            System.out.println("Timer start");
            // call scheduler
            new Scheduler(S, quanT, C);
            long end = System.currentTimeMillis();
            System.out.println("Timer end");
            System.out.println("Elapsed Time in milliseconds: "+ (end-start));

        } catch (NumberFormatException e) {
            System.out.println(
                    e +
                            "\n\nValid Parameters:" +
                            "\n\tScheduler:   1 - 4" +
                            "\n\tTime Quantum: 2 - 10" +
                            "\n\tCores:       1 - 4"
            );
        } catch (IllegalArgumentException e){
            System.out.println(
                    e +
                            "\n\nValid Arguments: " +
                            "\n\t-S # : specifies algorithm selection (required)" +
                            "\n\t\t1 - First Come First Serve" +
                            "\n\t\t2 - Round Robin (requires time quantum specification)" +
                            "\n\t\t3 - Non-Preemptive Shortest Job First" +
                            "\n\t\t4 - Preemptive Shortest Job First" +
                            "\n\t-C # : specifies number of cores to be utilized (optional)" +
                            "\n\t\t- Ignored for Preemptive Shortest Job First (#4)" +
                            "\n\t\t- Defaults to one core if not specified"
            );
        }
    }
}