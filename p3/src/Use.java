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
}