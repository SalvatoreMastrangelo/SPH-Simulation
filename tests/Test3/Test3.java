public class Test3 {
    public static double celsiusToFahrenheit(double celsius) {
        double res = celsius * 1.8 + 32;
        return res; 
    }

    public static void main(String[] args) {
        double temp_celsius = 55.8;

        double temp_fahrenheit = celsiusToFahrenheit(temp_celsius);
        System.out.println("Temperature in Fahrenheit: " + temp_fahrenheit);
    }
}