import java.util.ArrayList;

public class Test5 {
    public static void main(String[] args) {
        ArrayList<Integer> myArray = new ArrayList<>();
        
        for (int i = 0; i < 4; i++) {
            myArray.add((i + 1) * 10);
        }

        int sum = 0;
        for (int i = 0; i < myArray.size(); i++) {
            sum += myArray.get(i);
        }

        System.out.println("The sum of the elements of myArray is: " + sum);
    }
}
