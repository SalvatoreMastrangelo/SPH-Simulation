public class Test2 {
    public static void main(String[] args) {
        int score = 105;
        
        if (score >= 100) {
            System.out.println("You won!");
        } else {
            System.out.println("Game Over");

            int counter = 0;
            while (counter < 3) {
                System.out.println("Retrying...");
                counter++;
            }
        }
    }
}
