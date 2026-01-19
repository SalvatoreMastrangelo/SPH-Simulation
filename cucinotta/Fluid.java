import java.awt.*;
import java.util.ArrayList;

public class Fluid {
    ArrayList<Particle> particles;
    double density;
    double pressureConstant; // pressure constant

    public Fluid(double density, double pressureConstant) {
        this.density = density;
        this.pressureConstant = pressureConstant;
    }

    private void calcDensity() {
        for (Particle pi : particles) {
            double densitySum = 0;
            for (Particle pj : particles) {
                Vector2D rij = new Vector2D(pj.position.x - pi.position.x, pj.position.y - pi.position.y);
                double dist = rij.magnitude();
                densitySum += pj.mass * Utils.poly6(dist, pi.smoothRadius);
            }
            pi.density = densitySum;
        }
    }

    private void calcPressure() {
        for (Particle p : particles) {
            p.pressure = Math.max(0, pressureConstant * (p.density - this.density));
        }
    }

    private void applyForces(Vector2D gravity, double dt) {
        for (Particle p : particles) {
            p.applyGravity(dt, gravity);
        }
    }

    public void showDensity(Graphics2D g, int windowWidth, int windowHeight) {
        // TODO: Optimize with spatial partitioning
        // TODO: Optimize with parallel processing
        double maxDensity = 0;
        double[][] densityField = new double[windowWidth][windowHeight];
        for (int x = 0; x < windowWidth; x ++) {
            for (int y = 0; y < windowHeight; y ++) {
                double densitySum = 0;
                for (Particle pj : particles) {
                    Vector2D rij = new Vector2D(pj.position.x - x, pj.position.y - y);
                    double dist = rij.magnitude();
                    densitySum += pj.mass * Utils.poly6(dist, pj.smoothRadius);
                }
                densityField[x][y] = densitySum;

                if (densitySum > maxDensity) {
                    maxDensity = densitySum;
                }
            }
        }
        System.out.println("Max Density: " + maxDensity);
        for (int x = 0; x < windowWidth; x ++) {
            for (int y = 0; y < windowHeight; y ++) {
                int colorValue = (int) Math.min(255, densityField[x][y] / maxDensity * 255);
                g.setColor(new Color(colorValue, 0, 0));
                g.fillRect(x, y, 1, 1);
            }
        }
    }

    public void update(int windowWidth, int windowHeight, double dt, Vector2D gravity) {
        calcDensity();
        calcPressure();
        applyForces(gravity, dt);
        for (Particle p : particles) {
            p.update(windowWidth, windowHeight, dt, gravity);
        }
    }

    public void show(Graphics2D g){
        for (Particle p : particles) {
            p.show(g);
        }
    }
}
