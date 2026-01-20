import java.awt.*;
import java.lang.reflect.Array;
import java.util.ArrayList;

public class Fluid {
    ArrayList<Particle> particles;
    double density;
    double pressureConstant;
    double viscosityConstant;
    boolean firstStep = true;
    

    public Fluid(double density, double pressureConstant, double viscosityConstant) {
        this.density = density;
        this.pressureConstant = pressureConstant;
        this.viscosityConstant = viscosityConstant;
    }

    private ArrayList<Integer> getNeighbors(Particle p) {
        ArrayList<Integer> neighbors = new ArrayList<>();
        if (firstStep) {
            for (int i = 0; i < particles.size(); i++) {
                neighbors.add(i);
            }
            return neighbors; // Skip neighbor search on first step
        } else {
            Vector2D rij = new Vector2D(0, 0);
            for (int i = 0; i < particles.size(); i++) {
                int xdiff = (int)particles.get(i).cell.x - (int)p.cell.x;
                int ydiff = (int)particles.get(i).cell.y - (int)p.cell.y;
                if (Math.abs(xdiff) <= 1 && Math.abs(ydiff) <= 1) {
                    rij.x = particles.get(i).position.x - p.position.x;
                    rij.y = particles.get(i).position.y - p.position.y; 
                    double dist = rij.magnitude();
                    if (dist > p.smoothRadius) continue; 
                    neighbors.add(i);
                }
            }
            // System.out.println("Neighbors found: " + neighbors.size());
            return neighbors;
        }
    }

    // private ArrayList<Integer> getNeighbors(Particle p) {
    //     ArrayList<Integer> neighbors = new ArrayList<>();
    //     if (firstStep) {
    //         for (int i = 0; i < particles.size(); i++) {
    //             neighbors.add(i);
    //         }
    //         return neighbors; // Skip neighbor search on first step
    //     } else {
    //         long[][] neighborHashes = new long[3][3];
    //         for (int dx = -1; dx <= 1; dx++) {
    //             for (int dy = -1; dy <= 1; dy++) {
    //                 neighborHashes[dx + 1][dy + 1] = Utils.getHash((int)(p.cell.x + dx), (int)(p.cell.y + dy));
    //             }
    //         }
    //         for (int i = 0; i < particles.size(); i++) {
    //             Particle pj = particles.get(i);
    //             for (int dx = -1; dx <= 1; dx++) {
    //                 for (int dy = -1; dy <= 1; dy++) {
    //                     if (pj.hash == neighborHashes[dx + 1][dy + 1]) {
    //                         // if (pj == p) break; // Skip self
    //                         Vector2D rij = new Vector2D(pj.position.x - p.position.x, pj.position.y - p.position.y);
    //                         double dist = rij.magnitude();
    //                         // if (dist > p.smoothRadius) break; // Outside smoothing radius
    //                         neighbors.add(i);
    //                         break;
    //                     }
    //                 }
    //             }
    //         }       
    //         // System.out.println("Neighbors found: " + neighbors.size());     
    //         return neighbors;
    //     }
    // }

    private void calcDensity() {
        for (Particle pi : particles) {
            double densitySum = 0;
            Vector2D rij = new Vector2D(0, 0);
            for (int pjIndex : getNeighbors(pi)) {
                Particle pj = particles.get(pjIndex);
                rij.x = pj.position.x - pi.position.x;
                rij.y = pj.position.y - pi.position.y;
                double dist = rij.magnitude();
                densitySum += pj.mass * Utils.spiky(dist, pi.smoothRadius);
            }
            pi.density = densitySum;
        }
    }

    private void calcDensityParallel() {
        particles.parallelStream().forEach(pi -> {
            double densitySum = 0;
            Vector2D rij = new Vector2D(0, 0);
            for (int pjIndex : getNeighbors(pi)) {
                Particle pj = particles.get(pjIndex);
                rij.x = pj.position.x - pi.position.x;
                rij.y = pj.position.y - pi.position.y;
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
            Vector2D rij = new Vector2D(0, 0);
            for (int pjIndex : getNeighbors(pi)) {
                Particle pj = particles.get(pjIndex);
                if (pi == pj) continue;
                rij.x = pj.position.x - pi.position.x;
                rij.y = pj.position.y - pi.position.y;
                double dist = rij.magnitude();

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
            Vector2D rij = new Vector2D(0, 0);
            for (int pjIndex : getNeighbors(pi)) {
                Particle pj = particles.get(pjIndex);
                if (pi == pj) continue;
                rij.x = pj.position.x - pi.position.x;
                rij.y = pj.position.y - pi.position.y;
                double dist = rij.magnitude();

                double pressureMiddle = (pi.pressure + pj.pressure) / 2.0;
                double gradient = Utils.spikyGradient(dist, pi.smoothRadius);
                if (gradient == 0) continue;  
                Vector2D direction = rij.normalize();
                pressureForce = pressureForce.add(direction.scale(pressureMiddle * gradient * pi.mass / pj.density));
            }
            pi.applyForce(pressureForce);
        });
    }

    private void applyViscosityForce() {
        for (Particle pi : particles) {
            Vector2D viscosityForce = new Vector2D(0, 0);
            Vector2D rij = new Vector2D(0, 0);
            Vector2D velocityDiff = new Vector2D(0, 0);
            for (int pjIndex : getNeighbors(pi)) {
                Particle pj = particles.get(pjIndex);
                if (pi == pj) continue;
                rij.x = pj.position.x - pi.position.x;
                rij.y = pj.position.y - pi.position.y;
                double dist = rij.magnitude();

                double laplacian = Utils.poly6Gradient(dist, pi.smoothRadius);
                if (laplacian == 0) continue;  
                velocityDiff.x = pj.velocity.x - pi.velocity.x;
                velocityDiff.y = pj.velocity.y - pi.velocity.y;
                viscosityForce = viscosityForce.add(velocityDiff.scale(laplacian * pj.mass / pj.density));
            }
            pi.applyForce(viscosityForce);
        }
    }

    private void applyViscosityForceParallel() {
        particles.parallelStream().forEach(pi -> {
            Vector2D viscosityForce = new Vector2D(0, 0);
            Vector2D rij = new Vector2D(0, 0);
            Vector2D velocityDiff = new Vector2D(0, 0);
            for (int pjIndex : getNeighbors(pi)) {
                Particle pj = particles.get(pjIndex);
                if (pi == pj) continue;
                rij.x = pj.position.x - pi.position.x;
                rij.y = pj.position.y - pi.position.y;
                double dist = rij.magnitude();

                double laplacian = Utils.poly6Gradient(dist, pi.smoothRadius);
                velocityDiff.x = pj.velocity.x - pi.velocity.x;
                velocityDiff.y = pj.velocity.y - pi.velocity.y;
                viscosityForce = viscosityForce.add(velocityDiff.scale(-viscosityConstant * laplacian * pj.mass / pj.density));
            }
            pi.applyForce(viscosityForce);
        });
    }

    private void applyForces(Vector2D gravity) {
        applyPressureForce();
        applyViscosityForce();
        for (Particle p : particles) {
            p.applyForce(gravity.scale(p.mass));
        }
    }

    private void applyForcesParallel(Vector2D gravity) {
        applyPressureForceParallel();
        applyViscosityForceParallel();
        particles.parallelStream().forEach(p -> {
            p.applyForce(gravity.scale(p.mass));
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
        // for (Particle p : particles) {
        //     p.computeHash();
        // }
        calcDensity();
        calcPressure();
        applyForces(gravity);
        for (Particle p : particles) {
            p.update(windowWidth, windowHeight, dt, gravity);
        }
        firstStep = false;
    }

    public void updateParallel(int windowWidth, int windowHeight, double dt, Vector2D gravity) {
        // for (Particle p : particles) {
        //     p.computeHash();
        // }
        calcDensityParallel();
        calcPressureParallel();
        applyForcesParallel(gravity);
        particles.parallelStream().forEach(p -> {
            p.update(windowWidth, windowHeight, dt, gravity);
        });
        firstStep = false;
    }

    public void show(Graphics2D g){
        for (Particle p : particles) {
            p.show(g);
        }
    }
}
