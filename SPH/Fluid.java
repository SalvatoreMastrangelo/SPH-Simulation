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

    private ArrayList<Particle> getNeighbors(Particle p) {
        return particles; // TODO: Implement spatial partitioning for optimization
    }

    private void calcDensity() {
        for (Particle pi : particles) {
            double densitySum = 0;
            for (Particle pj : getNeighbors(pi)) {
                Vector2D rij = new Vector2D(pj.position.x - pi.position.x, pj.position.y - pi.position.y);
                double dist = rij.magnitude();
                densitySum += pj.mass * Utils.spiky(dist, pi.smoothRadius);
            }
            pi.density = densitySum;
        }
    }

    private void calcDensityParallel() {
        particles.parallelStream().forEach(pi -> {
            double densitySum = 0;
            for (Particle pj : getNeighbors(pi)) {
                Vector2D rij = new Vector2D(pj.position.x - pi.position.x, pj.position.y - pi.position.y);
                double dist = rij.magnitude();
                densitySum += pj.mass * Utils.spiky(dist, pi.smoothRadius);
            }
            pi.density = densitySum;
        });
    }

    private void calcPressure() {
        for (Particle p : particles) {
            p.pressure = Math.max(0, pressureConstant * (p.density - this.density));
        }
    }

    private void calcPressureParallel() {
        particles.parallelStream().forEach(p -> {
            p.pressure = Math.max(0, pressureConstant * (p.density - this.density));
        });
    }

    private void applyPressureForce() {
        for (Particle pi : particles) {
            Vector2D pressureForce = new Vector2D(0, 0);
            for (Particle pj : getNeighbors(pi)) {
                if (pi == pj) continue;
                Vector2D rij = new Vector2D(pj.position.x - pi.position.x, pj.position.y - pi.position.y);
                double dist = rij.magnitude();
                if (dist > pi.smoothRadius) continue;

                double pressureMiddle = (pi.pressure + pj.pressure) / 2.0;
                double gradient = Utils.spikyGradient(dist, pi.smoothRadius);
                if (gradient == 0) continue;  
                Vector2D direction = rij.normalize();
                pressureForce = pressureForce.add(direction.scale(pressureMiddle * gradient * pi.mass / pj.density));
            }
            pi.applyForce(pressureForce);
        }
    }

    private void applyPressureForceParallel() {
        particles.parallelStream().forEach(pi -> {
            Vector2D pressureForce = new Vector2D(0, 0);
            for (Particle pj : getNeighbors(pi)) {
                if (pi == pj) continue;
                Vector2D rij = new Vector2D(pj.position.x - pi.position.x, pj.position.y - pi.position.y);
                double dist = rij.magnitude();
                if (dist > pi.smoothRadius) continue;

                double pressureMiddle = (pi.pressure + pj.pressure) / 2.0;
                double gradient = Utils.spikyGradient(dist, pi.smoothRadius);
                if (gradient == 0) continue;  
                Vector2D direction = rij.normalize();
                pressureForce = pressureForce.add(direction.scale(pressureMiddle * gradient));
            }
            pi.applyForce(pressureForce);
        });
    }

    private void applyForces(Vector2D gravity) {
        applyPressureForce();
        for (Particle p : particles) {
            p.applyForce(gravity);
        }
    }

    private void applyForcesParallel(Vector2D gravity) {
        applyPressureForceParallel();
        particles.parallelStream().forEach(p -> {
            p.applyForce(gravity);
        });
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
                    densitySum += pj.mass * Utils.spiky(dist, pj.smoothRadius);
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
        applyForces(gravity);
        for (Particle p : particles) {
            p.update(windowWidth, windowHeight, dt, gravity);
        }
    }

    public void updateParallel(int windowWidth, int windowHeight, double dt, Vector2D gravity) {
        calcDensityParallel();
        calcPressureParallel();
        applyForcesParallel(gravity);
        particles.parallelStream().forEach(p -> {
            p.update(windowWidth, windowHeight, dt, gravity);
        });
    }

    public void show(Graphics2D g){
        for (Particle p : particles) {
            p.show(g);
        }
    }
}
