import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;


public class SPH {
    public static void main(String[] args) {
        System.out.println("SPH Simulation Placeholder");

        // Simulation Parameters
        double dt = 0.016; // time step in seconds
        // Vector2D gravity = new Vector2D(0, 981);
        Vector2D gravity = new Vector2D(0, 0);
        double density = 20;
        double pressureConstant = 2000;
        int partAmount = 100;

        // Initialize Fluid and Particles
        Fluid fluid = new Fluid(density, pressureConstant);
        int offset = 15;

        ArrayList<Particle> particles = new ArrayList<>();
        for (int x = 0; x < Math.sqrt(partAmount); x++) {
            for (int y = 0; y < Math.sqrt(partAmount); y++) {
                particles.add(new Particle(new Vector2D(50 + x * offset, 50 + y * offset), new Vector2D(0, 0), 100, 10));
            }
        }

        fluid.particles = particles;

        // Graphics Setup
        int windowWidth = 800;
        int windowHeight = 600;
        JFrame f = new JFrame("Swing Graphics");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setSize(windowWidth, windowHeight);

        JPanel panel = new JPanel() {
            double lastTime = System.nanoTime();
            double now = System.nanoTime();
            double elapsedTimeSeconds;
            int frameCount = 0;

            {
                new Timer(16, e -> { // ~60 FPS
                    if (getWidth() == 0 || getHeight() == 0) return;

                    fluid.update(getWidth(), getHeight(), dt, gravity);

                    repaint();
                }).start();
            }
            
            @Override protected void paintComponent(Graphics gr) {
                super.paintComponent(gr);
                Graphics2D g = (Graphics2D) gr;

                fluid.showDensity(g, getWidth(), getHeight());

                fluid.show(g);

                frameCount++;
                now = System.nanoTime();
                elapsedTimeSeconds = (now - lastTime) / 1_000_000_000.0;
                if (elapsedTimeSeconds >= 1.0) {
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
