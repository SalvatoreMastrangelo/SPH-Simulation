public class CommandLineParser {
    private double dt;
    private int substeps;
    private double mass;
    private double smoothRadius;
    private double pressureConstant;
    private double viscosityConstant;
    private int particleAmount;
    private int maxWindowWidth;
    private int maxWindowHeight;

    public CommandLineParser(String[] args) {
        // Default values
        this.dt = 0.08;
        this.substeps = 25;
        this.mass = 10;
        this.smoothRadius = 4;
        this.pressureConstant = 100;
        this.viscosityConstant = 5;
        this.particleAmount = 1600;
        this.maxWindowWidth = 1200;
        this.maxWindowHeight = 600;
        
        // Parse arguments
        for (int i = 0; i < args.length; i += 2) {
            if (i + 1 >= args.length) break;
            switch (args[i]) {
                case "--dt":
                    this.dt = Double.parseDouble(args[i + 1]);
                    break;
                case "--substeps":
                    this.substeps = Integer.parseInt(args[i + 1]);
                    break;
                case "--mass":
                    this.mass = Double.parseDouble(args[i + 1]);
                    break;
                case "--smoothradius":
                    this.smoothRadius = Double.parseDouble(args[i + 1]);
                    break;
                case "--pressureconstant":
                    this.pressureConstant = Double.parseDouble(args[i + 1]);
                    break;
                case "--viscosityconstant":
                    this.viscosityConstant = Double.parseDouble(args[i + 1]);
                    break;
                case "--particleamount":
                    this.particleAmount = Integer.parseInt(args[i + 1]);
                    break;
                case "--maxwindowwidth":
                    this.maxWindowWidth = Integer.parseInt(args[i + 1]);
                    break;
                case "--maxwindowheight":
                    this.maxWindowHeight = Integer.parseInt(args[i + 1]);
                    break;
                default:
                    System.out.println("Unknown parameter: " + args[i]);
            }
        }
    }

    public double getDt() {
        return dt; 
    }
    public int getSubsteps() {
        return substeps; 
    }
    public double getMass() {
        return mass; 
    }
    public double getSmoothRadius() {
        return smoothRadius; 
    }
    public double getPressureConstant() {
        return pressureConstant; 
    }
    public double getViscosityConstant() {
        return viscosityConstant; 
    }
    public int getParticleAmount() {
        return particleAmount; 
    }
    public int getMaxWindowWidth() {
        return maxWindowWidth;
    }
    public int getMaxWindowHeight() {
        return maxWindowHeight;
    }
}
