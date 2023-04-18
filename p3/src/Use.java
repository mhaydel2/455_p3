import java.util.concurrent.ThreadLocalRandom;

// Code by Milan Haydel C00419477
public class Use {
    public static int randNum(int min, int max){
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }
    public static void print(String name, String msg){
        System.out.printf(
                "\n%-15s | " + msg, name
        );
    }
    public static String getAlg(int S){
        switch (S){
            case 1:
                return "Running FCFS Algorithm";
            case 2:
                return "Running RR Algorithm";
            case 3:
                return "Running Non Preemptive - Shortest Job First Algorithm";
            case 4:
                return "Running Preemptive - Shortest Job First Algorithm";
        }
        return "Invalid Algorithm Passed";
    }
}