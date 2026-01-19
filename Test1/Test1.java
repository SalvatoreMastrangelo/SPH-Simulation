public class Test1 {
    
    public static void main(String[] args) {
        // 1. int (Whole number)
        int studentAge = 20;

        // 2. double (Decimal number)
        double studentGPA = 3.85;

        double addition = 0.5;

        double newGPA = studentGPA + addition;

        // 3. boolean (True/False)
        boolean isEnrolled = true;

        // 4. char (Single character)
        char studentGrade = 'A';

        // 5. String (Text - Note the Capital 'S')
        String studentName = "Alice";

        // Print them out
        System.out.println("Name: " + studentName);
        System.out.println("Age: " + studentAge);
        System.out.println("GPA: " + studentGPA);
        System.out.println("New GPA: " + newGPA);
        System.out.println("Enrolled: " + isEnrolled);
        System.out.println("Grade: " + studentGrade);
    }
}
