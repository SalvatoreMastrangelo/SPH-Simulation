import java.awt.*;

class Particle {
    public final double smoothRadius;
    public final int radius;
    public final double mass;
    public double density; 
    public double pressure;
    private final double dampingFactor;
    public Vector2D position = new Vector2D(0, 0);
    public Vector2D velocity = new Vector2D(0, 0);
    public Vector2D acceleration = new Vector2D(0, 0);
    public int cellX;
    public int cellY;

    // Costruttore
    public Particle(int radius, Vector2D initialPosition, Vector2D initialVelocity, double smoothRadius, double mass, double dampingFactor) {
        this.radius = radius;
        this.smoothRadius = smoothRadius;
        this.mass = mass;
        this.density = 0;
        this.position.x = initialPosition.x;
        this.position.y = initialPosition.y;
        this.velocity.x = initialVelocity.x;
        this.velocity.y = initialVelocity.y;
        this.dampingFactor = dampingFactor;
    }

    // Metodo per applicare una forza alla particella
    public void applyForce(Vector2D force) {
        this.acceleration.x += force.x / this.mass;
        this.acceleration.y += force.y / this.mass;
    }

    // Metodo per gestire le collisioni al bordo della finestra, 
    // applicando una forza in stile molla proporzionale alla penetrazione della particella nel bordo,
    // e un dampening per simulare l'attrito con il bordo
    private void boundaryForce(int windowWidth, int windowHeight) {
        double boundaryStiffness = 100.0;
        double boundaryDamping = 50.0;

        if (this.position.x < this.radius) {
            double penetration = this.radius - this.position.x;
            double forceX = boundaryStiffness * penetration * this.mass - boundaryDamping * this.velocity.x;
            applyForce(new Vector2D(forceX, 0));
        } else if (this.position.x > windowWidth - this.radius) {
            double penetration = this.position.x - (windowWidth - this.radius);
            double forceX = -boundaryStiffness * penetration * this.mass - boundaryDamping * this.velocity.x;
            applyForce(new Vector2D(forceX, 0));
        }

        if (this.position.y < this.radius) {
            double penetration = this.radius - this.position.y;
            double forceY = boundaryStiffness * penetration * this.mass - boundaryDamping * this.velocity.y;
            applyForce(new Vector2D(0, forceY));
        } else if (this.position.y > windowHeight - this.radius) {
            double penetration = this.position.y - (windowHeight - this.radius);
            double forceY = -boundaryStiffness * penetration * this.mass - boundaryDamping * this.velocity.y;
            applyForce(new Vector2D(0, forceY));
        }
    }

    // Metodo per aggiornare gli indici della cella in cui si trova la particella nella
    // griglia per ottimizzare la ricerca dei neighbor
    public void updateCell() {
        this.cellX = (int) Math.floor(this.position.x / this.smoothRadius);
        this.cellY = (int) Math.floor(this.position.y / this.smoothRadius);  
        if (this.cellX < 0) this.cellX = 0;
        if (this.cellY < 0) this.cellY = 0;  
    }

    // Metodo per aggiornare la posizione e la velocitá della particella integrando l'accelerazione
    public void update(int windowWidth, int windowHeight,double dt, Vector2D gravity) {
        boundaryForce(windowWidth, windowHeight);
        this.position.x += this.velocity.x * dt + 0.5 * this.acceleration.x * dt * dt;
        this.position.y += this.velocity.y * dt + 0.5 * this.acceleration.y * dt * dt;
        this.velocity.x += this.acceleration.x * dt;
        this.velocity.y += this.acceleration.y * dt;      

        this.velocity.x *= dampingFactor;
        this.velocity.y *= dampingFactor;
        this.acceleration.x = 0;
        this.acceleration.y = 0;
        updateCell();   
    }
}