import java.util.ArrayList;


public class SPH {
    public static void main(String[] args) {

        CommandLineParser parser = new CommandLineParser(args);
        
        // Print stats
        final boolean verbose = false;

        // Simulation Parameters
        final double dt = parser.getDt(); 
        final int substeps = parser.getSubsteps();
        final Vector2D gravity = new Vector2D(0, 0.02);
        // Vector2D gravity = new Vector2D(0, 0);
        final double mass = parser.getMass();
        final double smoothRadius = parser.getSmoothRadius();
        final int radius = 1;
        final double density = mass * Utils.spiky(0, smoothRadius);
        final double pressureConstant = parser.getPressureConstant();
        final double viscosityConstant = parser.getViscosityConstant();
        final double dampingFactor = 1;

        // Fluid and Particles parameters
        final int partAmount = parser.getParticleAmount();
        final int offset = 3;
        final int baseHeight = radius;

        // Window and Grid parameters
        final int targetFPS = 30;
        final int windowWidth = 200;
        final int windowHeight = 300;
        final int maxWidth = parser.getMaxWindowWidth();
        final int maxHeight = parser.getMaxWindowHeight();
        final int gridWidth = (int) Math.ceil(maxWidth / smoothRadius);
        final int gridHeight = (int) Math.ceil(maxHeight / smoothRadius);

        ArrayList<Particle> particles = new ArrayList<>();
        for (int x = 0; x < Math.floor(Math.sqrt(partAmount)); x++) {
            for (int y = 0; y < Math.floor(Math.sqrt(partAmount)); y++) {
                particles.add(new Particle(radius, new Vector2D(radius + x * offset, windowHeight - (baseHeight + y * offset)), new Vector2D(0, 0), smoothRadius, mass, dampingFactor));
            }
        }

        Fluid simulation = new Fluid(particles, density, pressureConstant, viscosityConstant, gravity, gridWidth, gridHeight);

        Animation animation = new Animation(targetFPS, windowWidth, windowHeight);
        animation.setEnvironment(simulation);
        animation.start(dt, substeps, verbose);
    }
}
