public class Utils {
    public static double poly6(double dist, double smoothRadius) {
        if (dist < 0 || dist > smoothRadius) return 0;
        
        double volume = 4.0 / (Math.PI * Math.pow(smoothRadius, 8));
        return volume * Math.pow((smoothRadius * smoothRadius - dist * dist), 3);
    }
    
}
