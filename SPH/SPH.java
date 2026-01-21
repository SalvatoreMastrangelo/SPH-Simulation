import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;


public class SPH {
    public static void main(String[] args) {
        
        // Print stats
        boolean verbose = false;

        // Simulation Parameters
        double dt = 0.08; 
        int substeps = 25;
        Vector2D gravity = new Vector2D(0, 0.02);
        // Vector2D gravity = new Vector2D(0, 0);
        double mass = 10;
        double smoothRadius = 4;
        int radius = 1;
        double density = mass * Utils.spiky(0, smoothRadius);
        double pressureConstant = 100;
        double viscosityConstant = 5;
        double dampingFactor = 1;

        // Initialize Fluid and Particles
        int partAmountx = 66;
        int partAmounty = 60;
        // int partAmountx = 100;
        // int partAmounty = 100;
        int offset = 3;

        ArrayList<Particle> particles = new ArrayList<>();
        for (int x = 0; x < partAmountx; x++) {
            for (int y = 0; y < partAmounty; y++) {
                particles.add(new Particle(radius, new Vector2D(2 * radius + x * offset, 300 - (19 * radius + y * offset)), new Vector2D(0, 0), smoothRadius, mass, dampingFactor));
            }
        }

        Fluid fluid = new Fluid(particles, density, pressureConstant, viscosityConstant);

        // Graphics Setup
        int windowWidth = 200;
        int windowHeight = 300;
        JFrame f = new JFrame("Swing Graphics");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setSize(windowWidth, windowHeight);

        JPanel panel = new JPanel() {
            double lastTime = System.nanoTime();
            double now = System.nanoTime();
            double elapsedTimeSeconds;
            int frameCount = 0;

            {
                setBackground(Color.BLACK);
                setOpaque(true);

                new Timer(32, e -> {
                    if (getWidth() == 0 || getHeight() == 0) return;
                    
                    for (int i = 0; i < substeps; i++) {
                        if (verbose) {
                            System.out.println("---- Substep " + (i + 1) + "/" + substeps + " ----");
                            double computeTime = System.nanoTime();
                            fluid.updateParallel(getWidth(), getHeight(), dt, gravity, verbose);
                            System.out.println("Update Time (ms):               " + (System.nanoTime() - computeTime) / 1_000_000.0);
                        } else {
                            fluid.updateParallel(getWidth(), getHeight(), dt, gravity, verbose);
                        }
                    }

                    repaint();
                }).start();
            }
            
            @Override protected void paintComponent(Graphics gr) {
                super.paintComponent(gr);
                Graphics2D g = (Graphics2D) gr;

                // fluid.showDensity(g, getWidth(), getHeight());

                if (verbose) {
                    double showTime = System.nanoTime();
                    fluid.show(g);
                    System.out.println("Render Time (ms):               " + (System.nanoTime() - showTime) / 1_000_000.0);
                } else {
                    fluid.show(g);
                }

                frameCount++;
                now = System.nanoTime();
                elapsedTimeSeconds = (now - lastTime) / 1000000000.0;
                if (elapsedTimeSeconds >= 0.5) {
                    double fps = frameCount / elapsedTimeSeconds;
                    System.out.println("FPS: " + fps);
                    frameCount = 0;
                    lastTime = now;
                }
            }
        };

        f.add(panel);
        f.setVisible(true);
    }
}
