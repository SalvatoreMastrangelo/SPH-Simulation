import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class Animation {
    private final int initialWindowWidth;
    private final int initialWindowHeight;
    private final int targetFPS;
    private Fluid simulation;
    private Vector2D boundary;
    private JFrame f;
    
    // Costruttore
    public Animation(int targetFPS, int windowWidth, int windowHeight) {
        this.targetFPS = targetFPS;
        this.initialWindowWidth = windowWidth;
        this.initialWindowHeight = windowHeight;

        f = new JFrame("Swing Graphics");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setSize(windowWidth, windowHeight);
    }
 
    // Metodo di retrieval delle particelle
    public void setEnvironment(Fluid simulation) {
        this.simulation = simulation;
        this.boundary = simulation.returnBoundary();
    }

    // Metodo per disegnare i bordi del box di simulazione
    private void drawBoundary(Graphics2D g, int windowWidth, int windowHeight) {
        g.setColor(Color.WHITE);
        g.setStroke(new BasicStroke(2));
        int boundaryX = windowWidth;
        int boundaryY = windowHeight;
        if (windowWidth > boundary.x) {
            boundaryX = (int) boundary.x;
        }
        if (windowHeight > boundary.y) {
            boundaryY = (int) boundary.y;
        }
        g.drawRect(0, 0, boundaryX, boundaryY);
    }
    
    // Metodo per disegnare le particelle, assegna un colore diverso in base alla velocitá:
    // rosso = veloce, blu = lento
    private void showSimulation(Graphics2D g) {
        ArrayList<Particle> particles = simulation.getParticles();
        for (Particle p : particles) {
            double speed = p.velocity.x * p.velocity.x + p.velocity.y * p.velocity.y;
            double maxSpeed = 4;

            double t = speed / maxSpeed;
            if (t > 1) t = 1;

            float hue = (float) ((2.0 / 3.0) * (1.0 - t)); // 2/3=blue, 0=red
            Color heatColor = Color.getHSBColor(hue, 1.0f, 1.0f);

            g.setColor(heatColor);
            g.fillOval((int)p.position.x - (int)p.radius, (int)p.position.y - (int)p.radius, (int)(2 * p.radius), (int)(2 * p.radius));
        }
    }

    // Metodo di rendering della simulazione
    public void start(double dt, int substeps, boolean verbose) {
        JPanel panel = new JPanel() {
            double lastTime = System.nanoTime();
            double now = System.nanoTime();
            double elapsedTimeSeconds;
            int frameCount = 0;

            {
                setBackground(Color.BLACK);
                setOpaque(true);

                new Timer(1000 / targetFPS, e -> {
                    if (getWidth() == 0 || getHeight() == 0) return;

                    simulation.newStep(dt, substeps, getWidth(), getHeight(), verbose);
                    
                    repaint();
                }).start();
            }
            
            @Override protected void paintComponent(Graphics gr) {
                super.paintComponent(gr);
                Graphics2D g = (Graphics2D) gr;

                // fluid.showDensity(g, getWidth(), getHeight());
                
                // Rendering loop
                if (verbose) {
                    double showTime = System.nanoTime();
                    showSimulation(g);
                    System.out.println("Render Time (ms):               " + (System.nanoTime() - showTime) / 1_000_000.0);
                } else {
                    showSimulation(g);
                }

                drawBoundary(g, getWidth(), getHeight());


                // Ciclo per il calcolo dei FPS
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

        panel.setPreferredSize(new Dimension(initialWindowWidth, initialWindowHeight));
        f.add(panel);
        f.pack();
        f.setVisible(true);
    }
}
