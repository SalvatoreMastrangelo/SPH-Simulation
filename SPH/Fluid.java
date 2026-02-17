import java.awt.*;
import java.util.ArrayList;

public class Fluid {
    private final int boxWidth;
    private final int boxHeight;
    private final int gridWidth;
    private final int gridHeight;
    private ArrayList<Particle> particles;
    private ArrayList<Integer>[] cellMatrix;
    private boolean[] cellOccupied;
    private final double density;
    private final double pressureConstant;
    private final double viscosityConstant;
    private final Vector2D gravity;
    private boolean firstStep = true;
    
    // Costruttore
    public Fluid(ArrayList<Particle> particles, double density, double pressureConstant, double viscosityConstant, Vector2D gravity, int gridWidth, int gridHeight) {
        this.particles = particles;
        this.density = density;
        this.pressureConstant = pressureConstant;
        this.viscosityConstant = viscosityConstant;
        this.gravity = gravity;
        this.gridWidth = gridWidth;
        this.gridHeight = gridHeight;
        this.boxWidth = (int) (gridWidth * particles.get(0).smoothRadius);
        this.boxHeight = (int) (gridHeight * particles.get(0).smoothRadius);
        cellMatrix = new ArrayList[gridWidth * gridHeight];
        for (int i = 0; i < cellMatrix.length; i++) {
            cellMatrix[i] = new ArrayList<>();
        }
        cellOccupied = new boolean[gridWidth * gridHeight];
    }

    // Helper method per ottenere l'indice della cella a partire dalle coordinate della cella stessa,
    private int getCellIndex(int cellX, int cellY) {
        return cellX + cellY * gridWidth;
    }

    // Metodo per resettare la matrice delle celle prima di aggiornarla con le nuove posizioni,
    // solo le celle che erano occupate vengono resettate
    private void clearCellMatrix() {
        for (int i = 0; i < cellMatrix.length; i++) {
            if (cellOccupied[i]) {
                cellMatrix[i].clear();
                cellOccupied[i] = false;
            }
        }
    }

    // Metodo per aggiornare la matrice delle celle per ottimizzare la ricerca delle particelle 
    // neighbor. l'indice di ciascuna particella viene salvato nell'array delle celle alla posizione corrispondente
    private void updateCellMatrix() {
        Particle supportParticle;
        for (int i = 0; i < particles.size(); i++) {
            supportParticle = particles.get(i);
            int cellIndex = getCellIndex((int)supportParticle.cellX, (int)supportParticle.cellY);
            cellMatrix[cellIndex].add(i);
            if (!cellOccupied[cellIndex]) {
                cellOccupied[cellIndex] = true;
            }
        }
    }

    // Metodo per ottenere la lista degli indici delle particelle neighbor di una specifica
    // particella. Si intende vicina una particella che si trova nella stessa cella o in una delle
    // celle del quadrato 3x3 circostante
    private ArrayList<Integer> getNeighbors(Particle p) {
        ArrayList<Integer> neighbors = new ArrayList<>();

        // Al primo step non viene ancora fatta una compilazione della matrice delle celle, 
        // si ritornano tutte le particelle
        if (firstStep) {
            for (int i = 0; i < particles.size(); i++) {
                neighbors.add(i);
            }
            return neighbors; // Skip neighbor search on first step

        // Negli step successivi, la ricerca avviene come descritto sopra. Le particelle piú lontane
        // dello smoothingRadius vengono scartate, essendo ininfluenti
        } else {
            double squaredSmoothRadius = p.smoothRadius * p.smoothRadius;
            Vector2D rij = new Vector2D(0, 0);
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    int cellX = (int)(p.cellX + dx);
                    int cellY = (int)(p.cellY + dy);
                    if (cellX < 0 || cellY < 0 || cellX >= gridWidth || cellY >= gridHeight) continue;
                    for (Integer pjIndex : cellMatrix[getCellIndex(cellX, cellY)]) {
                        Particle pj = particles.get(pjIndex);
                        rij.x = pj.position.x - p.position.x;
                        rij.y = pj.position.y - p.position.y; 
                        double distSquared = rij.magnitudeSquared();
                        if (distSquared > squaredSmoothRadius) continue; 
                        neighbors.add(pjIndex);
                    }
                }
            }
        }
        // System.out.println("Neighbors found: " + neighbors.size());
        return neighbors;
    }

    // Metodo per il calcolo della densitá di ciascuna particella, in relazione a tutti i suoi vicini
    // in parallelo usando parallelStream essendo le operazioni indipendenti
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

    // Metodo per il calcolo della pressione di ciascuna particella, in relazione alla 
    // sua densitá e quella di riferimento del liquido (ovvero di una particella nel vuoto)
    //in parallelo usando parallelStream
    private void calcPressureParallel() {
        particles.parallelStream().forEach(p -> {
            p.pressure = Math.max(0, pressureConstant * (p.density - this.density));
        });
    }

    // Metodo che calcola e applica in un unico passaggio entrambe le forze di pressione e viscositá
    // per ridurre il numero di neighbor search
    private void applyMergedForcesParallel() {
        particles.parallelStream().forEach(pi -> {
            Vector2D pressureForce = new Vector2D(0, 0);
            Vector2D viscosityForce = new Vector2D(0, 0);
            Vector2D rij = new Vector2D(0, 0);
            Vector2D velocityDiff = new Vector2D(0, 0);
            Vector2D direction = new Vector2D(0, 0);
            for (int pjIndex : getNeighbors(pi)) {
                Particle pj = particles.get(pjIndex);
                if (pi == pj) continue;
                rij.x = pj.position.x - pi.position.x;
                rij.y = pj.position.y - pi.position.y;
                double dist = rij.magnitude();

                double pressureMiddle = (pi.pressure + pj.pressure) / 2.0;
                double gradient = Utils.spikyGradient(dist, pi.smoothRadius);
                double laplacian = Utils.poly6Gradient(dist, pi.smoothRadius);
                velocityDiff.x = pj.velocity.x - pi.velocity.x;
                velocityDiff.y = pj.velocity.y - pi.velocity.y;
                viscosityForce.addThis(velocityDiff.scaleThis(-viscosityConstant * laplacian * pj.mass / pj.density));
                if (gradient == 0) continue;  
                direction = rij.normalizeThis();
                pressureForce.addThis(direction.scaleThis(pressureMiddle * gradient * pj.mass / pj.density));
            }
            pi.applyForce(viscosityForce);
            pi.applyForce(pressureForce);
        });
    }

    // Metoodo per applicare tutte le forze su ciascuna particella (gravitá compresa)
    // Viene profilato il tempo di applicazione di ciascuna forza
    // in parallelo usando i metodi parallelizzati
    // Le forze di pressione e viscositá vengono applicate con il metodo aggregato
    private void applyForcesParallel(Vector2D gravity, boolean verbose) {
        double forcesStartTime = System.nanoTime();

        // Applicazione forze di interazione
        applyMergedForcesParallel();
        if (verbose) {
            double mergedForcesTime = System.nanoTime();
            System.out.println("    Merged Force Time (ms):     " + (mergedForcesTime - forcesStartTime) / 1_000_000.0);
            forcesStartTime = mergedForcesTime;
        }

        // Applicazione gravitá
        particles.parallelStream().forEach(p -> {
            p.applyForce(gravity.scale(p.mass));
        });
        if (verbose) {
            double gravityTime = System.nanoTime();
            System.out.println("    Gravity Force Time (ms):    " + (gravityTime - forcesStartTime) / 1_000_000.0);
        }
    }

    // Metodo di update generale del fluido, che aggiorna: densitá, pressione, forze e posizione di 
    // ciascuna particella, oltre ad aggiornare la matrice delle celle
    // Vengono profilati i tempi di esecuzione di ciascun metodo
    public void updateParallel(int windowWidth, int windowHeight, double dt, Vector2D gravity, boolean verbose) {
        double startTime = System.nanoTime();

        // calcolo densitá
        calcDensityParallel();
        if (verbose) {
            double densityTime = System.nanoTime();
            System.out.println("Density Calc Time (ms):         " + (densityTime - startTime) / 1_000_000.0);
            startTime = densityTime;
        }

        // calcolo pressione
        calcPressureParallel();
        if (verbose) {
            double pressureTime = System.nanoTime();
            System.out.println("Pressure Calc Time (ms):        " + (pressureTime - startTime) / 1_000_000.0);
            startTime = pressureTime;
        }

        // applicazione forze
        applyForcesParallel(gravity, verbose);
        if (verbose) {
            double forcesTime = System.nanoTime();
            System.out.println("Forces Application Time (ms):   " + (forcesTime - startTime) / 1_000_000.0);
            startTime = forcesTime;
        }

        // aggiornamento posizione e velocitá delle particelle
        particles.parallelStream().forEach(p -> {
            p.update(windowWidth, windowHeight, dt, gravity);
        });
        if (verbose) {
            double updateTime = System.nanoTime();
            System.out.println("Particles Update Time (ms):     " + (updateTime - startTime) / 1_000_000.0);
            startTime = updateTime;
        }

        // pulizia e aggiornamento della matrice dei neighbor
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

        // ricerca completa dei neighbor solo al primo step
        firstStep = false;
    }

    // Metodo per eseguire un nuovo step di simulazione, ovvero un ciclo di substeps
    public void newStep(double dt, int substeps, int windowWidth, int windowHeight, boolean verbose) {
        int simulationWidth = windowWidth;
        int simulationHeight = windowHeight;
        if (windowWidth > boxWidth) {
            simulationWidth = boxWidth;
        }
        if (windowHeight > boxHeight) {
            simulationHeight = boxHeight;
        }
        for (int i = 0; i < substeps; i++) {
            if (verbose) {
                System.out.println("---- Substep " + (i + 1) + "/" + substeps + " ----");
                double computeTime = System.nanoTime();
                updateParallel(simulationWidth, simulationHeight, dt, gravity, verbose);
                System.out.println("Update Time (ms):               " + (System.nanoTime() - computeTime) / 1_000_000.0);
            } else {
                updateParallel(simulationWidth, simulationHeight, dt, gravity, verbose);
            }
        }
    }

    // Metodo per ottenere la lista delle particelle, usato dall'oggetto rendering
    public ArrayList<Particle> getParticles() {
        return particles;
    }

    // Metodo per otterenere le dimensioni del box di simulazione, usato dall'oggetto rendering
    public Vector2D returnBoundary() {
        return new Vector2D(boxWidth, boxHeight);
    }

    // Metodo per visualizzare la densitá del fluido
    // Usata solo per debug
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
