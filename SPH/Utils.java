public class Utils {
    // Funzione kernel per il calcolo di densitá e pressione
    public static double poly6(double dist, double smoothRadius) {
        if (dist < 0 || dist > smoothRadius) return 0;
        
        double volume = (Math.PI * Math.pow(smoothRadius, 8)) / 4.0;
        return Math.pow((smoothRadius * smoothRadius - dist * dist), 3) / volume ;
    }

    // Derivata della funzione kernel per il calcolo della forza di pressione
    public static double poly6Gradient(double dist, double smoothRadius) {
        if (dist < 0 || dist > smoothRadius) return 0;

        double scale = -24.0 / (Math.PI * Math.pow(smoothRadius, 8));
        return scale * (smoothRadius * smoothRadius - dist * dist) * (smoothRadius * smoothRadius - dist * dist) * dist;
    }

    // Funzione kernel per il calcolo della forza di viscositá
    public static double spiky(double dist, double smoothRadius) {
        if (dist < 0 || dist > smoothRadius) return 0;

        double volume = (Math.PI * Math.pow(smoothRadius, 4)) / 6.0;
        return Math.pow(smoothRadius - dist, 2) / volume;
    }

    // Derivata della funzione kernel per il calcolo della forza di viscositá
    public static double spikyGradient(double dist, double smoothRadius) {
        if (dist <= 0 || dist > smoothRadius) return 0;

        double scale = 12.0 / (Math.PI * Math.pow(smoothRadius, 4));
        return scale * (dist - smoothRadius);
    }
}
