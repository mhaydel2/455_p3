import java.util.concurrent.ThreadLocalRandom;

public class Use {
    public static int randNum(int min, int max){
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }
    public static void print(String msg, String name){
        System.out.printf(
                "\n%-15s | " + msg, name
        );
    }
}