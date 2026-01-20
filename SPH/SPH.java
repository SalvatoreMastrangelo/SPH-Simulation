import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;


public class SPH {
    public static void main(String[] args) {
        System.out.println("SPH Simulation Placeholder");

        // Simulation Parameters
        double dt = 0.05; 
        int substeps = 2;
        Vector2D gravity = new Vector2D(0, 0.02);
        // Vector2D gravity = new Vector2D(0, 0);
        double mass = 10;
        double smoothRadius = 4;
        int radius = 1;
        double density = mass * Utils.spiky(0, smoothRadius);
        double pressureConstant = 100;
        double viscosityConstant = 1;
        double dampingFactor = 1;

        // Initialize Fluid and Particles
        Fluid fluid = new Fluid(density, pressureConstant, viscosityConstant);
        int partAmountx = 66;
        int partAmounty = 40;
        int offset = 3;

        ArrayList<Particle> particles = new ArrayList<>();
        for (int x = 0; x < partAmountx; x++) {
            for (int y = 0; y < partAmounty; y++) {
                particles.add(new Particle(radius, new Vector2D(2 * radius + x * offset, 300 - (20 * radius + y * offset)), new Vector2D(0, 0), smoothRadius, mass, dampingFactor));
            }
        }

        fluid.particles = particles;

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

                new Timer(16, e -> {
                    if (getWidth() == 0 || getHeight() == 0) return;
                    for (int i = 0; i < substeps; i++) {
                        // fluid.update(getWidth(), getHeight(), dt, gravity);
                        fluid.updateParallel(getWidth(), getHeight(), dt, gravity);
                    }

                    repaint();
                }).start();
            }
            
            @Override protected void paintComponent(Graphics gr) {
                super.paintComponent(gr);
                Graphics2D g = (Graphics2D) gr;

                // fluid.showDensity(g, getWidth(), getHeight());

                fluid.show(g);

                frameCount++;
                now = System.nanoTime();
                elapsedTimeSeconds = (now - lastTime) / 1_000_000_000.0;
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
