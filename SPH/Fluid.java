import java.awt.*;
import java.util.ArrayList;

public class Fluid {
    private int gridWidth = 500;
    private int gridHeight = 500;
    private ArrayList<Particle> particles;
    private ArrayList<Integer>[] cellMatrix = new ArrayList[gridWidth * gridHeight];
    private boolean[] cellOccupied = new boolean[gridWidth * gridHeight];
    private double density;
    private double pressureConstant;
    private double viscosityConstant;
    private boolean firstStep = true;
    

    public Fluid(ArrayList<Particle> particles, double density, double pressureConstant, double viscosityConstant) {
        this.particles = particles;
        this.density = density;
        this.pressureConstant = pressureConstant;
        this.viscosityConstant = viscosityConstant;
        for (int i = 0; i < cellMatrix.length; i++) {
            cellMatrix[i] = new ArrayList<>();
        }
    }

    private void clearCellMatrix() {
        for (int i = 0; i < cellMatrix.length; i++) {
            if (cellOccupied[i]) {
                cellMatrix[i].clear();
                cellOccupied[i] = false;
            }
        }
    }

    private void updateCellMatrix() {
        for (int i = 0; i < particles.size(); i++) {
            Particle p = particles.get(i);
            cellMatrix[(int)p.cell.x + (int)p.cell.y * gridWidth].add(i);
            if (!cellOccupied[(int)p.cell.x + (int)p.cell.y * gridWidth]) {
                cellOccupied[(int)p.cell.x + (int)p.cell.y * gridWidth] = true;
            }
        }
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
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    int cellX = (int)(p.cell.x + dx);
                    int cellY = (int)(p.cell.y + dy);
                    if (cellX < 0 || cellY < 0 || cellX >= gridWidth || cellY >= gridHeight) continue;
                    for (Integer pjIndex : cellMatrix[cellX + cellY * gridWidth]) {
                        Particle pj = particles.get(pjIndex);
                        rij.x = pj.position.x - p.position.x;
                        rij.y = pj.position.y - p.position.y; 
                        double dist = rij.magnitude();
                        if (dist > p.smoothRadius) continue; 
                        neighbors.add(pjIndex);
                    }
                }
            }
            // for (int i = 0; i < particles.size(); i++) {
            //     int xdiff = (int)particles.get(i).cell.x - (int)p.cell.x;
            //     int ydiff = (int)particles.get(i).cell.y - (int)p.cell.y;
            //     if (Math.abs(xdiff) <= 1 && Math.abs(ydiff) <= 1) {
            //         rij.x = particles.get(i).position.x - p.position.x;
            //         rij.y = particles.get(i).position.y - p.position.y; 
            //         double dist = rij.magnitude();
            //         if (dist > p.smoothRadius) continue; 
            //         neighbors.add(i);
            //     }
            }
        // System.out.println("Neighbors found: " + neighbors.size());
        return neighbors;
    }

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

    private void applyPressureForce(boolean verbose) {
        double totalNeighborSearchTime = 0;
        for (Particle pi : particles) {
            Vector2D pressureForce = new Vector2D(0, 0);
            Vector2D rij = new Vector2D(0, 0);
            double neighborTime = System.nanoTime();
            ArrayList<Integer> neighbors = getNeighbors(pi);
            if (verbose) {
                double neighborEndTime = System.nanoTime();
                totalNeighborSearchTime += (neighborEndTime - neighborTime);
            }
            for (int pjIndex : neighbors) {
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
        if (verbose) {
            System.out.println("Neighbor Time Pressure (ms):    " + (totalNeighborSearchTime) / 1_000_000.0);
        }
        totalNeighborSearchTime = 0;
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
                velocityDiff.x = pj.velocity.x - pi.velocity.x;
                velocityDiff.y = pj.velocity.y - pi.velocity.y;
                viscosityForce = viscosityForce.add(velocityDiff.scale(-viscosityConstant * laplacian * pj.mass / pj.density));
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

    private void applyForces(Vector2D gravity, boolean verbose) {
        double forcesStartTime = System.nanoTime();
        applyPressureForce(verbose);
        if (verbose) {
            double pressureTime = System.nanoTime();
            System.out.println("Pressure Force Time (ms):       " + (pressureTime - forcesStartTime) / 1_000_000.0);
            forcesStartTime = pressureTime;
        }

        applyViscosityForce();
        if (verbose) {
            double viscosityTime = System.nanoTime();
            System.out.println("Viscosity Force Time (ms):      " + (viscosityTime - forcesStartTime) / 1_000_000.0);
            forcesStartTime = viscosityTime;
        }

        for (Particle p : particles) {
            p.applyForce(gravity.scale(p.mass));
        }
        if (verbose) {
            double gravityTime = System.nanoTime();
            System.out.println("Gravity Force Time (ms):        " + (gravityTime - forcesStartTime) / 1_000_000.0);
        }
    }

    private void applyForcesParallel(Vector2D gravity, boolean verbose) {
        double forcesStartTime = System.nanoTime();
        applyPressureForceParallel();
        if (verbose) {
            double pressureTime = System.nanoTime();
            System.out.println("Pressure Force Time (ms):       " + (pressureTime - forcesStartTime) / 1_000_000.0);
            forcesStartTime = pressureTime;
        }

        applyViscosityForceParallel();
        if (verbose) {
            double viscosityTime = System.nanoTime();
            System.out.println("Viscosity Force Time (ms):      " + (viscosityTime - forcesStartTime) / 1_000_000.0);
            forcesStartTime = viscosityTime;
        }

        particles.parallelStream().forEach(p -> {
            p.applyForce(gravity.scale(p.mass));
        });
        if (verbose) {
            double gravityTime = System.nanoTime();
            System.out.println("Gravity Force Time (ms):        " + (gravityTime - forcesStartTime) / 1_000_000.0);
        }
    }

    public void update(int windowWidth, int windowHeight, double dt, Vector2D gravity, boolean verbose) {
        double startTime = System.nanoTime();
        calcDensity();
        if (verbose) {
            double densityTime = System.nanoTime();
            System.out.println("Density Calc Time (ms):         " + (densityTime - startTime) / 1_000_000.0);
            startTime = densityTime;
        }

        calcPressure();
        if (verbose) {
            double pressureTime = System.nanoTime();
            System.out.println("Pressure Calc Time (ms):        " + (pressureTime - startTime) / 1_000_000.0);
            startTime = pressureTime;
        }

        applyForces(gravity, verbose);
        if (verbose) {
            double forcesTime = System.nanoTime();
            System.out.println("Forces Application Time (ms):   " + (forcesTime - startTime) / 1_000_000.0);
            startTime = forcesTime;
        }

        for (Particle p : particles) {
            p.update(windowWidth, windowHeight, dt, gravity);
        }
        if (verbose) {
            double updateTime = System.nanoTime();
            System.out.println("Particles Update Time (ms):     " + (updateTime - startTime) / 1_000_000.0);
        }

        clearCellMatrix();
        if (verbose) {
            double clearTime = System.nanoTime();
            System.out.println("Cell Matrix Clear Time (ms):    " + (clearTime - startTime) / 1_000_000.0);
            startTime = clearTime;
        }

        updateCellMatrix();
        if (verbose) {
            double cellMatrixTime = System.nanoTime();
            System.out.println("Cell Matrix Update Time (ms):   " + (cellMatrixTime - startTime) / 1_000_000.0);
        }
        
        firstStep = false;
    }

    public void updateParallel(int windowWidth, int windowHeight, double dt, Vector2D gravity, boolean verbose) {
        double startTime = System.nanoTime();
        calcDensityParallel();
        if (verbose) {
            double densityTime = System.nanoTime();
            System.out.println("Density Calc Time (ms):         " + (densityTime - startTime) / 1_000_000.0);
            startTime = densityTime;
        }

        calcPressureParallel();
        if (verbose) {
            double pressureTime = System.nanoTime();
            System.out.println("Pressure Calc Time (ms):        " + (pressureTime - startTime) / 1_000_000.0);
            startTime = pressureTime;
        }

        applyForcesParallel(gravity, verbose);
        if (verbose) {
            double forcesTime = System.nanoTime();
            System.out.println("Forces Application Time (ms):   " + (forcesTime - startTime) / 1_000_000.0);
            startTime = forcesTime;
        }

        particles.parallelStream().forEach(p -> {
            p.update(windowWidth, windowHeight, dt, gravity);
        });
        if (verbose) {
            double updateTime = System.nanoTime();
            System.out.println("Particles Update Time (ms):     " + (updateTime - startTime) / 1_000_000.0);
            startTime = updateTime;
        }

        if (firstStep == false) {
            clearCellMatrix();
            if (verbose) {
                double clearTime = System.nanoTime();
                System.out.println("Cell Matrix Clear Time (ms):    " + (clearTime - startTime) / 1_000_000.0);
                startTime = clearTime;
            }
        }

        updateCellMatrix();
        if (verbose) {
            double cellMatrixTime = System.nanoTime();
            System.out.println("Cell Matrix Update Time (ms):   " + (cellMatrixTime - startTime) / 1_000_000.0);
        }

        firstStep = false;
    }

    public void show(Graphics2D g){
        for (Particle p : particles) {
            p.show(g);
        }
    }

    public void showDensity(Graphics2D g, int windowWidth, int windowHeight) {
        // TODO: Optimize with parallel processing
        double maxDensity = 0;
        double[][] densityField = new double[windowWidth][windowHeight];
        Particle mockParticle = new Particle(0, new Vector2D(0,0), new Vector2D(0,0), particles.get(0).smoothRadius, 0, 0);
        for (int x = 0; x < windowWidth; x ++) {
            mockParticle.position.x = x;
            for (int y = 0; y < windowHeight; y ++) {
                mockParticle.position.y = y;
                mockParticle.updateCell();
                double densitySum = 0;
                for (Integer pjIndex : getNeighbors(mockParticle)) {
                    Particle pj = particles.get(pjIndex);
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

        // System.out.println("Max Density: " + maxDensity);
        for (int x = 0; x < windowWidth; x ++) {
            for (int y = 0; y < windowHeight; y ++) {
                int colorValue = (int) Math.min(255, densityField[x][y] / maxDensity * 255);
                g.setColor(new Color(colorValue, 0, 0));
                g.fillRect(x, y, 1, 1);
            }
        }
    }

}
